package com.kola.kmp.logic.shop.random;

import javax.management.timer.Timer;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.shop.KShopRoleExtCACreator;
import com.kola.kmp.logic.shop.random.message.KPushRandomGoodsMsg;
import com.kola.kmp.logic.shop.random.message.KPushRandomShopConstanceMsg;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.HourClearTask;

/**
 * <pre>
 * 本类管理模块定义的所有任务
 * 
 * @author CamusHuang
 * @creation 2013-1-9 下午2:39:58
 * </pre>
 */
public class KRandomShopTaskManager {

	private KRandomShopTaskManager() {
	}

	static void notifyCacheLoadComplete() {
		// 小时触发任务
		KRandomShopHourTask.instance.start();
	}

	/**
	 * <pre>
	 * 小时触发任务
	 * 
	 * @author CamusHuang
	 * @creation 2013-4-28 下午6:35:32
	 * </pre>
	 */
	public static class KRandomShopHourTask extends HourClearTask {

		public static final KRandomShopHourTask instance = new KRandomShopHourTask(KRandomShopDataManager.RandomPeriod, 3 * Timer.ONE_SECOND);

		private KRandomShopHourTask(int hours, long delay) {
			super(hours, delay);
		}

		public void doWork() throws KGameServerException {
			long nowTime = System.currentTimeMillis();
			
			long nextRefreshDelayTime = getNextRunTime() - nowTime;
			if (nextRefreshDelayTime < Timer.ONE_MINUTE) {
				nextRefreshDelayTime = Timer.ONE_MINUTE;
			}
			//
			KRole role = null;
			KRoleRandomData roleData = null;
			for (long roleId : KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds()) {
				role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
				if (role == null) {
					continue;
				}
				roleData = KShopRoleExtCACreator.getRoleRandomData(roleId);
				if (roleData == null) {
					continue;
				}
				
				// 随机商品自动刷新
				KRandomShopCenter.refreshRandomGoods(role, roleData, null);
				roleData.setLastSystemRereshTime(nowTime);
				//
				KPushRandomShopConstanceMsg.sendMsg(roleId, nextRefreshDelayTime);
				KPushRandomGoodsMsg.pushMsg(roleId, null);
			}
		}

		@Override
		public String getNameCN() {
			return "随机商店数据定时刷新任务";
		}
	}
}
