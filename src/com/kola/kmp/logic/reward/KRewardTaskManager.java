package com.kola.kmp.logic.reward;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.reward.KRewardDataStruct.FengceSaveOnlineRoleData;
import com.kola.kmp.logic.reward.KRewardDataStruct.PhyPowerRewardData;
import com.kola.kmp.logic.reward.message.KPhyPowSyncIconMsg;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.DayClearTask;
import com.kola.kmp.logic.util.KGameUtilTool;

/**
 * <pre>
 * 本类管理模块定义的所有任务
 * 
 * @author CamusHuang
 * @creation 2013-1-9 下午2:39:58
 * </pre>
 */
public class KRewardTaskManager {

	private KRewardTaskManager() {
	}

	private static final Logger _LOGGER = KGameLogger.getLogger(KRewardTaskManager.class);

	static void notifyCacheLoadComplete() {
		// 跨天触发任务
		KRewardDayTask.instance.start();
	}

	/**
	 * <pre>
	 * 跨天触发任务
	 * 每天凌晨00:00:03清0
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-28 下午6:35:32
	 * </pre>
	 */
	static class KRewardDayTask extends DayClearTask {

		static final KRewardDayTask instance = new KRewardDayTask(3 * Timer.ONE_SECOND);

		private KRewardDayTask(long delay) {
			super(delay);
		}

		public void doWork() throws KGameServerException {

			// 通知子模块进行全局数据刷新
			long nowTime = System.currentTimeMillis();
			for (KRewardSonModuleAbs impl : KRewardModuleExtension.rewardSonImplMap.values()) {
				impl.notifyForDayChangeTask(nowTime);
			}
			
			long todayStartTime = UtilTool.getTodayStart().getTimeInMillis();

			RoleModuleSupport mRoleModuleSupport = KSupportFactory.getRoleModuleSupport();
			for (long roleId : mRoleModuleSupport.getAllOnLineRoleIds()) {
				KRoleReward data = KRewardRoleExtCACreator.getRoleReward(roleId);
				if (data == null) {
					continue;
				}
				
				// 通知在线角色进行个人数据刷新
				data.notifyForDayChange();
				//
				// 通知子模块进行在线数据同步
				for (KRewardSonModuleAbs impl : KRewardModuleExtension.rewardSonImplMap.values()) {
					impl.notifyAfterDayChangeTask(roleId);
				}
				
				
				// 同步体力免费领取ICON
				KRole role = mRoleModuleSupport.getRole(roleId);
				if (role != null) {
					KPhyPowSyncIconMsg.instance.sendSynMsg(role, KRewardLogic.isShowPhyPowerIcon(role, nowTime, todayStartTime));
				}
			}
		}

		@Override
		public String getNameCN() {
			return "每日奖励数据跨天清0任务";
		}
	}

	public static class OlineRoleToXMLTask implements KGameTimerTask {

		private static final String dir = "./res/output/onlineRoles/";
		private static final String fileName = "onlineRoles";

		private FengceSaveOnlineRoleData data;

		public OlineRoleToXMLTask(FengceSaveOnlineRoleData data) {
			long delayTime = data.dateTimeInMills - System.currentTimeMillis();
			if (delayTime < 1) {
				return;
			}

			this.data = data;
			KGame.newTimeSignal(this, delayTime, TimeUnit.MILLISECONDS);
		}

		@Override
		public String getName() {
			return this.getClass().getName();
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
			try {
				// 执行具体工作
				doWork();
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
			}
			return null;
		}

		private void doWork() {

			List<JSONObject> dialys = new ArrayList<JSONObject>();

			RoleModuleSupport support = KSupportFactory.getRoleModuleSupport();
			for (long roleId : support.getAllOnLineRoleIds()) {
				KRole role = support.getRole(roleId);
				if (role != null) {
					if (data.minLv <= role.getLevel() && role.getLevel() <= data.maxLv) {
						JSONObject json = new JSONObject();
						try {
							json.put("playerId", role.getPlayerId());
							json.put("roleId", role.getId());
							json.put("roleName", role.getName());
							json.put("lv", role.getLevel());
						} catch (JSONException e) {
							_LOGGER.error(e.getMessage(), e);
						}
						dialys.add(json);
					}
				}
			}

			KGameUtilTool.saveSimpleDialy(dir, fileName + UtilTool.DATE_FORMAT7.format(new Date(data.dateTimeInMills))+".xml", "", dialys);
		}

		@Override
		public void done(KGameTimeSignal timeSignal) {
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}
	}
}
