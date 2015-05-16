package com.kola.kmp.logic.vip;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.DayClearTask;
import com.kola.kmp.logic.vip.message.KSyncVipDataMsg;

/**
 * <pre>
 * 本类管理模块定义的所有任务
 * 
 * @author CamusHuang
 * @creation 2013-1-9 下午2:39:58
 * </pre>
 */
class KVIPTaskManager {

	private KVIPTaskManager() {
	}

	private static final Logger _LOGGER = KGameLogger.getLogger(KVIPTaskManager.class);

	static void notifyCacheLoadComplete() {
		// 跨天触发任务
		KVIPDayTask.instance.start();
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
	static class KVIPDayTask extends DayClearTask {

		static final KVIPDayTask instance = new KVIPDayTask(3 * Timer.ONE_SECOND);

		private KVIPDayTask(long delay) {
			super(delay);
		}

		public void doWork() throws KGameServerException {
			//
			long nowTime = System.currentTimeMillis();
			for (long roleId : KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds()) {
				KRoleVIP vip = KVIPRoleExtCACreator.getRoleVIP(roleId);
				if (vip == null) {
					continue;
				}
				
				vip.notifyForDayChange(nowTime);
				//
				KSyncVipDataMsg.sendMsg(roleId, vip);
			}
		}

		@Override
		public String getNameCN() {
			return "VIP数据跨天清0任务";
		}
	}
}
