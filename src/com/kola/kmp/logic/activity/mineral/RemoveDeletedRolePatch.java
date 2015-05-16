package com.kola.kmp.logic.activity.mineral;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kmp.logic.RunTimeTask;
import com.kola.kmp.logic.activity.mineral.KDigMineralDataManager.MineralDataManger.KMineral;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;

/**
 * <pre>
 * 这是一个清理无效角色的补丁
 * 
 * @author CamusHuang
 * @creation 2015-1-4 上午11:04:09
 * </pre>
 */
public class RemoveDeletedRolePatch implements RunTimeTask {
	private static Logger _LOGGER = KGameLogger.getLogger(RemoveDeletedRolePatch.class);

	public String run(String date) {
		KGame.newTimeSignal(new RemoveDeletedRoleTask(), 1, TimeUnit.SECONDS);
		return "执行完毕";
	}

	/**
	 * <pre>
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:52:58
	 * </pre>
	 */
	private static class RemoveDeletedRoleTask implements KGameTimerTask {

		private RemoveDeletedRoleTask() {
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
				RoleModuleSupport mRoleModuleSupport = KSupportFactory.getRoleModuleSupport();
				for (KMineral mineral : KDigMineralDataManager.mMineralDataManger.getDataCache().values()) {
					mineral.rwLock.lock();
					try {
						for (long roleId : mineral.getAllDiggerCache()) {
							KRole role = mRoleModuleSupport.getRole(roleId);
							if (role == null) {
								mineral.removeDigger(roleId);
								_LOGGER.warn("挖矿补丁：清理被删除的角色="+roleId);
							}
						}
					} finally {
						mineral.rwLock.unlock();
					}
				}
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
				arg0.getTimer().newTimeSignal(this, 5, TimeUnit.SECONDS);
			}
			return null;
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}
	}
}
