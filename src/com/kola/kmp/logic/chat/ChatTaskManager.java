package com.kola.kmp.logic.chat;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;

/**
 * <pre>
 * 本类管理模块定义的所有任务
 * 
 * @author CamusHuang
 * @creation 2013-1-9 下午2:39:58
 * </pre>
 */
class ChatTaskManager {

	private ChatTaskManager() {
	}

	private static final Logger _LOGGER = KGameLogger.getLogger(ChatTaskManager.class);

	/**
	 * <pre>
	 * 用于角色登陆时，异步推送离线私聊
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:52:58
	 * </pre>
	 */
	static class PrivateChatSendTask implements KGameTimerTask {
		private final String taskName = this.getClass().getSimpleName();

		private List<ChatDataAbs> list;

		private PrivateChatSendTask(List<ChatDataAbs> list) {
			this.list = list;
		}

		static void submitTast(List<ChatDataAbs> list) {
			KGame.newTimeSignal(new PrivateChatSendTask(list), 10, TimeUnit.SECONDS);
		}

		@Override
		public void done(KGameTimeSignal arg0) {
		}

		@Override
		public String getName() {
			return taskName;
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			try {
				for (ChatDataAbs chatData : list) {
					chatData.notifyDelay();
					KChatLogic.sendChatFinally(chatData);
				}
			} catch (Exception ex) {
				_LOGGER.error(ex.getMessage(), ex);
				throw new KGameServerException(ex);
			}
			return null;
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			_LOGGER.error(ex.getMessage(), ex);
		}
	}
}
