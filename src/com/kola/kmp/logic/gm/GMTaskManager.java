package com.kola.kmp.logic.gm;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.gameserver.KGameServer;
import com.koala.game.gameserver.KGameServerHandler;
import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.gm.ProtocolGs;

/**
 * <pre>
 * 本类管理模块定义的所有任务
 * 
 * @author CamusHuang
 * @creation 2013-1-9 下午2:39:58
 * </pre>
 */
class GMTaskManager implements ProtocolGs {

	private GMTaskManager() {
	}

	private static final Logger _LOGGER = KGameLogger.getLogger(GMTaskManager.class);
	/** GS状态同步任务（内存安全需要） */
	private static final GSStateTask gsStateTask = new GSStateTask();
	private static final long ONE_M = 1024 * 1024;// 1M=多少Byte

	static void notifyCacheLoadComplete() {
		KGame.newTimeSignal(gsStateTask, KGMConfig.getInstance().GSStateTaskPeroid, TimeUnit.MILLISECONDS);
		//
	}

	/**
	 * <pre>
	 * GM离线邮件重发任务
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:52:58
	 * </pre>
	 */
	static class GMMailCacheClearTask implements KGameTimerTask {

		/**
		 * <pre>
		 * 军团资源战状态定时切换任务
		 * 
		 * @param status
		 * @param delayTime
		 * @author CamusHuang
		 * @creation 2013-8-28 下午9:00:53
		 * </pre>
		 */
		static void submitTast(long delayTime) {
			if (delayTime < Timer.ONE_SECOND) {
				delayTime = Timer.ONE_SECOND;
			}
			GMMailCacheClearTask instance = new GMMailCacheClearTask();
			KGame.newTimeSignal(instance, delayTime, TimeUnit.MILLISECONDS);
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			try {
				try {
					KGMSupportImpl.onTimeSignalToClearGMMailCache();
				} catch (Exception ex) {
					_LOGGER.error(ex.getMessage(), ex);
					throw new KGameServerException(ex);
				}
				return null;
			} finally {
			}
		}

		@Override
		public void done(KGameTimeSignal arg0) {
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}
	}

	/**
	 * <pre>
	 * GS状态同步任务（内存安全需要）
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-9 下午2:54:03
	 * </pre>
	 */
	static class GSStateTask implements KGameTimerTask {

		private static final Logger _MEMLOGGER = KGameLogger.getLogger("memRecord");

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
				sendGSStateToGMS();
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			} finally {
				arg0.getTimer().newTimeSignal(this, KGMConfig.getInstance().GSStateTaskPeroid, TimeUnit.MILLISECONDS);
			}
			return null;
		}

		private void sendGSStateToGMS() {
			// 广播服务器状态到GMS
			Runtime runtime = Runtime.getRuntime();

			int maxMem = (int) (runtime.maxMemory() / ONE_M);
			int alloMem = (int) (runtime.totalMemory() / ONE_M);
			int useMem = (int) ((runtime.totalMemory() - runtime.freeMemory()) / ONE_M);

			int sessionNum = KGameServerHandler.handshakedplayersessions.size();
			int playerNum = KGameServer.getInstance().getPlayerManager().getCachedPlayerSessionSize();
			int roleNum = KSupportFactory.getRoleModuleSupport().getAllOnLineRoleNum();

			_MEMLOGGER.warn("mem-use/total: {} / {}   online-role/player/handshake: {} / {} / {}", useMem, alloMem, roleNum, playerNum, sessionNum);

			KGamePlayerSession session = KGMLogic.getGMSession();
			if (session == null) {
				return;
			}

			KGameMessage msg = KGame.newLogicMessage(GS_GMS_SERVER_STATE_INFO);

			// msg.writeInt(KGameServer.getInstance().getGSID());
			msg.writeLong(System.currentTimeMillis());
			msg.writeInt(maxMem);
			msg.writeInt(alloMem);
			msg.writeInt(useMem);
			msg.writeInt(sessionNum);
			msg.writeInt(playerNum);
			msg.writeInt(roleNum);

			session.send(msg);
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}
	}

}
