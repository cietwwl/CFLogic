package com.kola.kmp.logic.shop;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.shop.random.KRandomShopTaskManager;
import com.kola.kmp.logic.shop.random.message.KPushRandomShopConstanceMsg;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.DayClearTask;

/**
 * <pre>
 * 本类管理模块定义的所有任务
 * 
 * @author CamusHuang
 * @creation 2013-1-9 下午2:39:58
 * </pre>
 */
public class KShopTaskManager {

	private KShopTaskManager() {
	}

	private static final Logger _LOGGER = KGameLogger.getLogger(KShopTaskManager.class);

	static void notifyCacheLoadComplete() {
		// 跨天触发任务
		KShopDayTask.instance.start();
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
	static class KShopDayTask extends DayClearTask {

		static final KShopDayTask instance = new KShopDayTask(3 * Timer.ONE_SECOND);

		private KShopDayTask(long delay) {
			super(delay);
		}

		public void doWork() throws KGameServerException {
			long nowTime = System.currentTimeMillis();
			long nextRefreshDelayTime = KRandomShopTaskManager.KRandomShopHourTask.instance.getNextRunTime() - nowTime;
			if (nextRefreshDelayTime < Timer.ONE_MINUTE) {
				nextRefreshDelayTime = Timer.ONE_MINUTE;
			}
			//
			RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
			for (long roleId : roleSupport.getAllOnLineRoleIds()) {
				KRoleShop shop = KShopRoleExtCACreator.getRoleShop(roleId);
				if (shop == null) {
					continue;
				}
				
				int roleLv = roleSupport.getLevel(roleId);
				
				// 免费次数重置
				shop.notifyForDayChange(nowTime, roleLv);
				//
				KPushRandomShopConstanceMsg.sendMsg(roleId, nextRefreshDelayTime);
			}
		}

		@Override
		public String getNameCN() {
			return "随机商店数据跨天清0任务";
		}
	}
}
