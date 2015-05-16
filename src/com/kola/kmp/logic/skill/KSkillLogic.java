package com.kola.kmp.logic.skill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.PropertyTypeEnum;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.reward.redress.KRedressDataManagerV1.KSkillRedressDataManager.SkillRedressData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleSkillTempAbs;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleSkillTempAbs.SkillTempLevelData;
import com.kola.kmp.logic.skill.message.KPushSkillsMsg;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.SkillResul;
import com.kola.kmp.logic.util.ResultStructs.SkillResult;
import com.kola.kmp.logic.util.tips.ShopTips;
import com.kola.kmp.logic.util.tips.SkillTips;

/**
 * <pre>
 * 
 * 
 * @author camus
 * @creation 2012-12-30 下午2:50:58
 * </pre>
 */
public class KSkillLogic {

	public static final Logger _LOGGER = KGameLogger.getLogger(KSkillLogic.class);

	/**
	 * <pre>
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-13 下午3:15:31
	 * </pre>
	 */
	static boolean addSkillToRole(long roleId, boolean isInitiative, int templateId, String sourceTips) {

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role == null) {
			return false;
		}

		KRoleSkillTempAbs template = null;

		if (isInitiative) {
			template = KSkillDataManager.mRoleIniSkillTempManager.getTemplate(templateId);
			if (template == null) {
				return false;
			}
		} else {
			template = KSkillDataManager.mRolePasSkillTempManager.getTemplate(templateId);
			if (template == null) {
				return false;
			}
		}

		KSkill skill = null;
		KSkillSet set = KSkillModuleExtension.getSkillSet(roleId);
		set.rwLock.lock();
		try {
			KSkill oldSkill = set.searchSkill(isInitiative, templateId);
			if (oldSkill != null) {
				return false;
			}
			skill = new KSkill(set, isInitiative, templateId);
			set.addSkill(skill);

			// 尝试放入空置的快捷栏
			set.tryToJoinSkillSlot(Arrays.asList(skill));
			return true;
		} finally {
			set.rwLock.unlock();

			if (skill != null) {
				KPushSkillsMsg.pushNewSkills(role, Arrays.asList(skill));
				KPushSkillsMsg.pushSelectedSkills(role);

				KSupportFactory.getRoleModuleSupport().notifySkillListChange(roleId);
				if (!isInitiative) {
					// 刷新角色属性
					KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(roleId, KSkillAttributeProvider.getType());
				}

				// 财产日志
				FlowManager.logPropertyAddOrDelete(role.getId(), PropertyTypeEnum.技能, skill.getId() + "", template.id, template.name, true, sourceTips);
			}
		}
	}

	public static void newSkillsForUplv(KRole role, boolean isSyn) {
		// 主动、被动技能按等级开放
		// 主动技能：本职业和不限职业
		// 被动技能：本职业和不限职业，且是按等级开放的
		int roleLv = role.getLevel();
		Map<Integer, KRoleSkillTempAbs> targetIniSkillTemps = new HashMap<Integer, KSkillDataStructs.KRoleSkillTempAbs>();
		Map<Integer, KRoleSkillTempAbs> targetPasSkillTemps = new HashMap<Integer, KSkillDataStructs.KRoleSkillTempAbs>();
		// 搜索角色当按前等级可以拥有的(1级价格为0的)主动和(1级价格为0且开启类型为初始类型的)被动技能
		{
			Map<Integer, KRoleIniSkillTemp> map = KSkillDataManager.mRoleIniSkillTempManager.getCache();
			for (KRoleIniSkillTemp temp : map.values()) {
				if (temp.isAddForRoleUpLv()) {
					if (temp.job == 0 || temp.job == role.getJob()) {
						SkillTempLevelData lvData = temp.getLevelData(1);
						if (lvData.learn_lvl <= roleLv) {
							targetIniSkillTemps.put(temp.id, temp);
						}
					}
				}
			}
		}
		{
			Map<Integer, KRolePasSkillTemp> map = KSkillDataManager.mRolePasSkillTempManager.getCache();
			for (KRolePasSkillTemp temp : map.values()) {
				if (temp.isAddForRoleUpLv()) {
					if (temp.job == 0 || temp.job == role.getJob()) {
						SkillTempLevelData lvData = temp.getLevelData(1);
						if (lvData.learn_lvl <= roleLv) {
							targetPasSkillTemps.put(temp.id, temp);
						}
					}
				}
			}
		}

		boolean isAddPasSkill = false;
		List<KSkill> newSkills = new ArrayList<KSkill>();
		KSkillSet set = KSkillModuleExtension.getSkillSet(role.getId());
		// 如果角色当前未拥有指定技能，则新增
		set.rwLock.lock();
		try {
			if (!targetIniSkillTemps.isEmpty()) {
				Map<Integer, KSkill> existSkills = set.searchAllSkills(true);
				for (KRoleSkillTempAbs temp : targetIniSkillTemps.values()) {
					if (!existSkills.containsKey(temp.id)) {
						KSkill skill = new KSkill(set, temp.isIniSkill, temp.id);
						set.addSkill(skill);
						newSkills.add(skill);

						// 财产日志
						FlowManager.logPropertyAddOrDelete(role.getId(), PropertyTypeEnum.技能, skill.getId() + "", temp.id, temp.name, true, "升级");
					}
				}
			}

			if (!targetPasSkillTemps.isEmpty()) {
				Map<Integer, KSkill> existSkills = set.searchAllSkills(false);
				for (KRoleSkillTempAbs temp : targetPasSkillTemps.values()) {
					if (!existSkills.containsKey(temp.id)) {
						KSkill skill = new KSkill(set, temp.isIniSkill, temp.id);
						set.addSkill(skill);
						newSkills.add(skill);
						isAddPasSkill = true;

						// 财产日志
						FlowManager.logPropertyAddOrDelete(role.getId(), PropertyTypeEnum.技能, skill.getId() + "", temp.id, temp.name, true, "升级");
					}
				}
			}

			// 尝试放入空置的快捷栏
			if (!newSkills.isEmpty()) {
				set.tryToJoinSkillSlot(newSkills);
			}

		} finally {
			set.rwLock.unlock();
			if (!newSkills.isEmpty()) {
				if (isSyn) {
					KPushSkillsMsg.pushNewSkills(role, newSkills);
					KPushSkillsMsg.pushSelectedSkills(role);
				}
				KSupportFactory.getRoleModuleSupport().notifySkillListChange(role.getId());
			}

			if (isAddPasSkill) {
				// 刷新角色属性
				KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(role.getId(), KSkillAttributeProvider.getType());
			}
		}
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 演示代码：删除
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-13 下午3:15:31
	 * </pre>
	 */
	static boolean deleteSkill(long roleId, long skillId) {
		KSkillSet set = KSkillModuleExtension.getSkillSet(roleId);
		KSkill skill = null;
		set.rwLock.lock();
		try {
			skill = set.notifyElementDelete(skillId);
			if (skill != null) {
				KPushSkillsMsg.pushAllSkills(roleId);
				return true;
			}
			return false;
		} finally {
			set.rwLock.unlock();

			if (skill != null) {
				KSupportFactory.getRoleModuleSupport().notifySkillListChange(roleId);
				if (!skill.isInitiative()) {
					// 刷新角色属性
					KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(roleId, KSkillAttributeProvider.getType());
				}
			}
		}
	}

	public static SkillResult dealMsg_uplvSkill(KRole role, boolean isInit, int skillTempId) {
		SkillResult result = new SkillResult();
		KRoleSkillTempAbs temp = null;
		KSkill skill = null;
		//
		KSkillSet set = KSkillModuleExtension.getSkillSet(role.getId());
		set.rwLock.lock();
		try {
			skill = set.searchSkill(isInit, skillTempId);
			if (skill == null) {
				result.tips = SkillTips.未掌握此技能;
				return result;
			}
			if (skill.isInitiative()) {
				temp = KSkillDataManager.mRoleIniSkillTempManager.getTemplate(skill._templateId);
			} else {
				temp = KSkillDataManager.mRolePasSkillTempManager.getTemplate(skill._templateId);
			}
			if (temp == null) {
				result.tips = SkillTips.未掌握此技能;
				return result;
			}

			int nextLv = skill.getLv() + 1;
			// 检查最高等级
			if (nextLv > temp.max_lvl) {
				result.tips = SkillTips.此技能已达等级上限;
				return result;
			}

			SkillTempLevelData nextLvData = temp.getLevelData(nextLv);
			// 升级条件检测(角色等级、金币、潜能)
			if (role.getLevel() < nextLvData.learn_lvl) {
				// 角色等级不足
				result.tips = StringUtil.format(SkillTips.角色等级必须达到x级才能升级此技能, nextLvData.learn_lvl);
				return result;
			}

			if (!nextLvData.learnLvMoneys.isEmpty()) {
				KCurrencyCountStruct moneyResult = KSupportFactory.getCurrencySupport().decreaseMoneys(role.getId(), nextLvData.learnLvMoneys, UsePointFunctionTypeEnum.技能升级, true);
				if (moneyResult != null) {
					// 货币不足
					result.isGoMoneyUI = true;
					result.goMoneyUIType = moneyResult.currencyType;
					result.goMoneyUICount = moneyResult.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), moneyResult.currencyType);
					result.tips = StringUtil.format(ShopTips.x货币数量不足x, moneyResult.currencyType.extName, moneyResult.currencyCount);
					return result;
				}
			}
			skill.setLv(nextLv);
			//
			result.isSucess = true;
			result.skill = skill;
			result.tips = SkillTips.升级技能成功;
			// 主策要求不显示消耗
			// if (!nextLvData.learnLvMoneys.isEmpty()) {
			// result.dataUprisingTips = new ArrayList<String>();
			// for (KCurrencyCountStruct money : nextLvData.learnLvMoneys) {
			// result.addDataUprisingTips(StringUtil.format(ShopTips.x减x,
			// money.currencyType.extName, money.currencyCount));
			// }
			// }
			return result;
		} finally {
			set.rwLock.unlock();

			if (result.isSucess) {
				// 财产日志
				FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.技能, skill.getId() + "", temp.id, temp.name, "升级到:" + skill.getLv());
			}
		}
	}

	public static SkillResul dealMsg_updateSkillSlot(KRole role, Map<Integer, Integer> skillTempIdMap) {
		SkillResul result = new SkillResul();
		//
		KSkillSet set = KSkillModuleExtension.getSkillSet(role.getId());
		set.rwLock.lock();
		try {
			int orgSize = skillTempIdMap.size();
			// 锡除不存在的技能、技能栏
			int slotMaxId = set.getSkillSlotLen() - 1;// 5:0~4
			for (Iterator<Entry<Integer, Integer>> it = skillTempIdMap.entrySet().iterator(); it.hasNext();) {
				Entry<Integer, Integer> entry = it.next();

				int slotId = entry.getKey();
				if (slotId > slotMaxId) {
					// 非法
					it.remove();
					continue;
				}
				int skillTempId = entry.getValue();
				if (skillTempId < 1) {
					// 取消
					continue;
				}

				KSkill skill = set.searchSkill(true, skillTempId);
				if (skill == null) {
					// 取消
					entry.setValue(0);
					continue;
				}
				//
				KRoleIniSkillTemp temp = KSkillDataManager.mRoleIniSkillTempManager.getTemplate(skillTempId);
				if (temp == null) {
					// 取消
					entry.setValue(0);
					continue;
				}

				if (slotId == slotMaxId) {
					if (temp.is_commonskill != 1) {
						// 普通技能不允许设置到最后一栏
						it.remove();
						continue;
					}
				} else {
					if (temp.is_commonskill == 1) {
						// 超杀技能只能设置到最后一栏
						it.remove();
						continue;
					}
				}
			}
			result.isChange = set.updateSlotSkills(skillTempIdMap);
			//
			if (orgSize == skillTempIdMap.size()) {
				result.isSucess = true;
				result.tips = SkillTips.设置技能成功;
			} else {
				result.isSucess = false;
				result.tips = SkillTips.技能快捷栏设置异常;
			}
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 技能战斗力计算公式
	 * 技能等级*该技能参数*人物等级^参数1
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-9 下午6:35:08
	 * </pre>
	 */
	public static int ExpressionForPower(KRoleSkillTempAbs temp, int skillLv, int roleLv) {
		// int result = (int) Math.pow(skillLv * temp.count1 * roleLv,
		// temp.count2);
		int result = (int) Math.round(skillLv * temp.count1 * Math.pow(roleLv, temp.count2));
		return result;
	}

	public static Map<Integer, Integer> clearIllegalSkill(boolean isInit, KRole role, Map<Integer, Map<Integer, SkillRedressData>> redressDatas) {
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		//
		KSkillSet set = KSkillModuleExtension.getSkillSet(role.getId());
		set.rwLock.lock();
		try {

			long roleLv = role.getLevel();

			Set<Integer> deleteSkills = new HashSet<Integer>();
			List<KSkill> keepSkills = new ArrayList<KSkill>();

			KRoleSkillTempAbs temp = null;
			List<KSkill> tempSkillList = new ArrayList<KSkill>(set.getAllSkillsCache().values());
			for (KSkill skill : tempSkillList) {
				if (skill.isInitiative() != isInit) {
					continue;
				}

				Map<Integer, SkillRedressData> tempMap = redressDatas.get(skill._templateId);
				if (tempMap == null) {
					continue;
				}

				if (skill.isInitiative()) {
					temp = KSkillDataManager.mRoleIniSkillTempManager.getTemplate(skill._templateId);
				} else {
					temp = KSkillDataManager.mRolePasSkillTempManager.getTemplate(skill._templateId);
				}

				// 先判断技能开放等级，如果技能开放等级小于或等于人物等级，则技能开放，等级都为1；
				// 如果技能开放等级大于人物等级技能不开放
				SkillRedressData  data = tempMap.get(skill.getLv());
				if(data.IsDelete){
					int skillFirstLv = temp.skillLevelDatas.get(0).learn_lvl;
					if (skillFirstLv > roleLv) {
						// 删除技能
						result.put(temp.id, skill.getLv());
						deleteSkills.add(temp.id);
						skill = set.notifyElementDelete(skill._id);

						// 财产日志
						FlowManager.logPropertyAddOrDelete(role.getId(), PropertyTypeEnum.技能, skill.getId() + "", temp.id, temp.name, false, "改版删除:" + skill.getLv());
					} else {
						// 重置等级
						result.put(temp.id, skill.getLv());
						keepSkills.add(skill);
						skill.setLv(1);

						// 财产日志
						FlowManager.logPropertyAddOrDelete(role.getId(), PropertyTypeEnum.技能, skill.getId() + "", temp.id, temp.name, false, "改版降级:" + skill.getLv());
					}
				}else {
					// 重置等级
					result.put(temp.id, skill.getLv());
					keepSkills.add(skill);
					skill.setLv(1);

					// 财产日志
					FlowManager.logPropertyAddOrDelete(role.getId(), PropertyTypeEnum.技能, skill.getId() + "", temp.id, temp.name, false, "改版降级:" + skill.getLv());
				}
			}

			if (isInit && !deleteSkills.isEmpty()) {
				// 清空快捷栏
				int[] slot = set.getSkillSlotDataCache();
				for (int i = 0; i < slot.length; i++) {
					if (deleteSkills.contains(slot[i])) {
						slot[i] = 0;
					}
				}

				// 尝试放入空置的快捷栏
				if (!keepSkills.isEmpty()) {
					set.tryToJoinSkillSlot(keepSkills);
				}
			}

			return result;
		} finally {
			set.rwLock.unlock();

			if (!result.isEmpty()) {
				KSupportFactory.getRoleModuleSupport().notifySkillListChange(role.getId());

				if (!isInit) {
					// 刷新角色属性
					KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(role.getId(), KSkillAttributeProvider.getType());
				}
			}
		}
	}
}
