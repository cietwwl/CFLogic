package com.kola.kmp.logic.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kmp.protocol.gm.ProtocolGs;

/**
 * <pre>
 * 本类管理模块定义的所有任务
 * 
 * @author CamusHuang
 * @creation 2015-1-28 上午11:29:37
 * </pre>
 */
public class ItemTaskManager implements ProtocolGs {

	private ItemTaskManager() {
	}

	private static final Logger _LOGGER = KGameLogger.getLogger(ItemTaskManager.class);

	static void notifyCacheLoadComplete() throws KGameServerException {
		// 启动自动开宝箱任务
		KGame.newTimeSignal(AutoOpenBoxTask.instance, 1, TimeUnit.SECONDS);
	}
	
	/**
	 * <pre>
	 * 此任务负责扫描指定的角色背包，若角色存在且包含可自动打开的宝箱，则打开此类宝箱
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-28 上午11:26:08
	 * </pre>
	 */
	public static class AutoOpenBoxTask implements KGameTimerTask {
		// 所有物品发生改变的角色
		private final Set<Long> roleIdSet = new HashSet<Long>();
		private final ReentrantLock lock = new ReentrantLock();

		public final static AutoOpenBoxTask instance = new AutoOpenBoxTask();

		private AutoOpenBoxTask() {
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		public void addData(long roleId) {
			lock.lock();
			try {
				roleIdSet.add(roleId);
			} finally {
				lock.unlock();
			}
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			try {

				List<Long> roleIdSetCopy = null;
				lock.lock();
				try {
					if (!roleIdSet.isEmpty()) {
						roleIdSetCopy = new ArrayList(roleIdSet);
						roleIdSet.clear();
					}
				} finally {
					lock.unlock();
				}

				if (roleIdSetCopy != null) {
					KItemLogic.forAndAutoOpenBoxForRoles(roleIdSetCopy);
					
					if(!roleIdSetCopy.isEmpty()){
						lock.lock();
						try {
							roleIdSet.addAll(roleIdSetCopy);
						} finally {
							lock.unlock();
						}
					}
				}
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
				arg0.getTimer().newTimeSignal(this, 1, TimeUnit.SECONDS);
			}
			return null;
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}

		@Override
		public void done(KGameTimeSignal arg0) {
		}
	}
}
