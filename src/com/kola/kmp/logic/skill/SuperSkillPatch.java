package com.kola.kmp.logic.skill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kmp.logic.RunTimeTask;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.PropertyTypeEnum;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleSkillTempAbs.SkillTempLevelData;
import com.kola.kmp.logic.skill.message.KPushSkillsMsg;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.MailResult;

/**
 * <pre>
 * 超杀技能处理补丁
 * 原由：原超杀三职业通用，新版改成限职业1，另外两个职业各新增新的超杀技能;
 * 但是升版本时，职业2，3的原有超杀没有删除，所以本补丁要定时扫描删除
 * 删除技能前，要计算相应的升级成本，转到新的超杀技能中，并将新的超杀技能放到快捷栏
 * 
 * @author CamusHuang
 * @creation 2015-2-4 下午4:29:07
 * </pre>
 */
public class SuperSkillPatch implements RunTimeTask {
	private static Logger _LOGGER = KGameLogger.getLogger(SuperSkillPatch.class);

	public static SuperSkillTask instance = new SuperSkillTask();;
	static KRoleIniSkillTemp job1Temp;
	static KRoleIniSkillTemp job2Temp;
	static KRoleIniSkillTemp job3Temp;

	static long TaskPeriod = 10;// 秒

	static String mailTitle = "超杀技能消耗返还";
	static String mailContent = "亲爱的友友，由于新版本新增1个超杀技能，导致部分友友原超杀技能降至1级，可能导致战力下降。现返还原超杀技能升级所消耗的潜能、金币。祝你游戏愉快！";

	static void notifyCacheLoadComplete() throws KGameServerException {
		job1Temp = KSkillDataManager.mRoleIniSkillTempManager.getTemplate(410401);
		job2Temp = KSkillDataManager.mRoleIniSkillTempManager.getTemplate(410402);
		job3Temp = KSkillDataManager.mRoleIniSkillTempManager.getTemplate(410403);
		if (job1Temp == null || job2Temp == null || job3Temp == null) {
			throw new KGameServerException("技能超杀补丁：技能超杀模板不存在");
		}
		if(!job1Temp.isSuperSkill || !job2Temp.isSuperSkill || !job3Temp.isSuperSkill){
			throw new KGameServerException("技能超杀补丁：技能模板不是超杀技能");
		}
		
		if(job1Temp.job!=KJobTypeEnum.WARRIOR.getJobType() 
				|| job2Temp.job!=KJobTypeEnum.SHADOW.getJobType()  
				|| job3Temp.job!=KJobTypeEnum.GUNMAN.getJobType() ){
			throw new KGameServerException("技能超杀补丁：技能模板职业错误");
		}
	}
	
	public String run(String param) {
		if (param.equals("run")) {
			if (instance != null) {
				instance.isCancel = true;
			}
			instance = new SuperSkillTask();
			KGame.newTimeSignal(instance, TaskPeriod, TimeUnit.SECONDS);
		} else if (param.equals("start")) {
			if (instance != null) {
				instance.isCancel = false;
			}
		} else {//stop
			if (instance != null) {
				instance.isCancel = true;
			}
		}
		return "执行完毕";
	}

	public static class SuperSkillTask implements KGameTimerTask {

		boolean isCancel = false;

		private SuperSkillTask() {
		}

		@Override
		public void done(KGameTimeSignal arg0) {
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			if (isCancel) {
				return null;
			}
			try {
				for (long roleId : KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds()) {
					KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
					doWork(role, true);
				}
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
				arg0.getTimer().newTimeSignal(this, TaskPeriod, TimeUnit.SECONDS);
			}
			return null;
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}

		public void doWork(KRole role, boolean isSyn) {
			if (isCancel) {
				return;
			}
			
			if (role == null || role.getJob() == KJobTypeEnum.WARRIOR.getJobType()) {
				return;
			}

			if (role.getJob() == KJobTypeEnum.SHADOW.getJobType()) {
				doWork2(role, job1Temp, job2Temp, isSyn);
			} else {
				doWork2(role, job1Temp, job3Temp, isSyn);
			}
		}
		

		/**
		 * <pre>
		 * 原由：原超杀三职业通用，新版改成限职业1，另外两个职业各新增新的超杀技能;
		 * 但是升版本时，职业2，3的原有超杀没有删除，所以本补丁要定时扫描删除
		 * 删除技能前，要计算相应的升级成本，转到新的超杀技能中，并将新的超杀技能放到快捷栏
		 * 
		 * @param roleId
		 * @param role
		 * @param job1Temp
		 * @param newJobTemp
		 * @return
		 * @author CamusHuang
		 * @creation 2015-2-4 下午5:19:32
		 * </pre>
		 */
		private void doWork2(KRole role, KRoleIniSkillTemp job1Temp, KRoleIniSkillTemp newJobTemp, boolean isSyn) {
			long roleId = role.getId();
			//
			KSkillSet set = KSkillModuleExtension.getSkillSet(roleId);
			if (set == null) {
				return;
			}
			

			boolean isChange = false;
			set.rwLock.lock();
			try {

				try {
					KSkill oldSkill = set.searchSkill(true, job1Temp.id);
					if (oldSkill == null) {
						return;
					}

					List<KCurrencyCountStruct> moneys = new ArrayList<KCurrencyCountStruct>();
					for (int lv = 2; lv <= oldSkill.getLv(); lv++) {
						SkillTempLevelData lvdata = job1Temp.getLevelData(lv);
						moneys.addAll(lvdata.learnLvMoneys);
					}

					if (!moneys.isEmpty()) {
						moneys = KCurrencyCountStruct.mergeCurrencyCountStructs(moneys);
						if (!moneys.isEmpty()) {
							MailResult result = KSupportFactory.getMailModuleSupport().sendMoneyMailBySystem(role.getId(), moneys, PresentPointTypeEnum.改版补偿V2, mailTitle, mailContent);
							if (!result.isSucess) {
								return;
							}
						}
					}

					set.notifyElementDelete(oldSkill._id);
					isChange = true;
					// 财产日志
					FlowManager.logPropertyAddOrDelete(role.getId(), PropertyTypeEnum.技能, oldSkill.getId() + "", job1Temp.id, job1Temp.name, false, "补丁");
				} finally {
					if(isChange){
						KSkill newSkill = set.searchSkill(true, newJobTemp.id);
						if (newSkill != null) {
							// 尝试放入空置的快捷栏
							set.tryToJoinSkillSlot(Arrays.asList(newSkill));
						}
					}
				}
				return;
			} finally {
				set.rwLock.unlock();
				
				if(isChange){
					if(isSyn){
						KPushSkillsMsg.pushAllSkills(role);
						KPushSkillsMsg.pushSelectedSkills(role);
					}

					KSupportFactory.getRoleModuleSupport().notifySkillListChange(roleId);
					// 刷新角色属性
					KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(roleId, KSkillAttributeProvider.getType());
				}
			}
		}
	}
}
