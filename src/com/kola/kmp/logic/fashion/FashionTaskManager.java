package com.kola.kmp.logic.fashion;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

class FashionTaskManager {

	private FashionTaskManager() {
	}

	final static Logger _LOGGER = KGameLogger.getLogger(FashionTaskManager.class);

	static void notifyCacheLoadComplete() {

		TimeOutFashionClearTask instance = new TimeOutFashionClearTask();
		KGame.newTimeSignal(instance, KFashionConfig.getInstance().TimeOutScanTaskPeriod, TimeUnit.MILLISECONDS);
	}

	/**
	 * <pre>
	 * 时装超时失效扫描时效任务
	 * 
	 * @author CamusHuang
	 * @creation 2014-2-21 上午9:44:51
	 * </pre>
	 */
	static class TimeOutFashionClearTask implements KGameTimerTask {

		private long periodTime;

		private TimeOutFashionClearTask() {
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

			try {
				long startTime = System.currentTimeMillis()-Timer.ONE_MINUTE;
				for (long roleId : KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds()) {
					try{
						KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
						if (role == null) {
							continue;
						}
						if(role.getLastLeaveGameTime() > role.getLastJoinGameTime()){
							continue;
						}
						//上线时间不足1分钟的无视
						if(role.getLastJoinGameTime() > startTime){
							continue;
						}
						
						KFashionLogic.clearTimeOutFashion(role, true);
						
					} catch (Exception ex) {
						_LOGGER.error(ex.getMessage(), ex);
					} 
				}
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
				arg0.getTimer().newTimeSignal(this, periodTime, TimeUnit.MILLISECONDS);
			}
			return null;
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}
	}
}
