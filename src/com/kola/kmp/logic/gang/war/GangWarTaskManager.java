package com.kola.kmp.logic.gang.war;

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
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.gang.war.message.KGWRaceSynMsg;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 本类管理模块定义的所有任务
 * 
 * @author CamusHuang
 * @creation 2013-1-9 下午2:39:58
 * </pre>
 */
class GangWarTaskManager {

	private GangWarTaskManager() {
	}

	/**
	 * <pre>
	 * 时效任务启动
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-9 下午4:36:38
	 * </pre>
	 */
	static void notifyCacheLoadComplete() {
		// CTODO 
	}

	/**
	 * <pre>
	 * 停止所有时效任务
	 * 
	 * @author CamusHuang
	 * @creation 2013-10-11 下午11:24:17
	 * </pre>
	 */
	static void stopGangWar() {

		GangWarStatusTask.cancel();
		RoundJudgeTask.cancel();
		WarErrorScanTask.cancel();
		StartCountdownTask.cancel();
		RaceBroadcastTask.cancel();
		WorldBroadcastTask.cancelAll();
	}

	/**
	 * <pre>
	 * 军团战状态定时切换通知
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:52:58
	 * </pre>
	 */
	static class GangWarStatusTask implements KGameTimerTask {
	
		/** 军团战状态任务 */
		private static GangWarStatusTask instance;
	
		/**
		 * <pre>
		 * 军团战状态任务
		 * 
		 * @param status
		 * @param delayTime
		 * @author CamusHuang
		 * @creation 2013-8-28 下午9:00:53
		 * </pre>
		 */
		static void submitTast(GangWarStatusEnum status, long delayTime) {
			if (delayTime < Timer.ONE_SECOND) {
				delayTime = Timer.ONE_SECOND;
			}
			GangWarLogic.lock.lock();
			try {
				cancel();
				instance = new GangWarStatusTask(status);
				KGame.newTimeSignal(instance, delayTime, TimeUnit.MILLISECONDS);
			} finally {
				GangWarLogic.lock.unlock();
			}
		}
		
		static GangWarStatusEnum getNextWarStatusEnum(){
			GangWarLogic.lock.lock();
			try {
				if(instance!=null){
					return instance.status;
				}
			} finally {
				GangWarLogic.lock.unlock();
			}
			return null;
		}
	
		static void cancel() {
			GangWarLogic.lock.lock();
			try {
				if (instance != null) {
					instance.cancel = true;
				}
			} finally {
				GangWarLogic.lock.unlock();
			}
		}
	
		/**
		 * <pre>
		 * 加速当前状态结束
		 * 
		 * @author CamusHuang
		 * @creation 2013-10-11 下午9:56:28
		 * </pre>
		 */
		static void speedToRun() {
			GangWarLogic.lock.lock();
			try {
				if (instance == null || instance.cancel) {
					return;
				}
				
				//提前执行
				try {
					instance.onTimeSignal(null);
				} catch (KGameServerException e) {
					GangWarLogic.GangWarLogger.error(e.getMessage(), e);
				}
			} finally {
				GangWarLogic.lock.unlock();
			}
		}
	
		private final GangWarStatusEnum status;
	
		private boolean cancel;
	
		private GangWarStatusTask(GangWarStatusEnum status) {
			this.status = status;
		}
	
		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			GangWarLogic.lock.lock();
			try {
				if (cancel) {
					return null;
				}
				
				//保证不会被时效重复执行
				cancel=true;
				
				try {
					GangWarStatusManager.TaskNotify_Status(status);
				} catch (Exception ex) {
					GangWarLogic.GangWarLogger.error(ex.getMessage(), ex);
					throw new KGameServerException(ex);
				}
				return null;
			} finally {
				GangWarLogic.lock.unlock();
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
			GangWarLogic.GangWarLogger.error(ex.getMessage(), ex);
		}
	}

	/**
	 * <pre>
	 * 用于对战中进行扫描，完成以下任务内容:
	 * 1.执行裁决
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:52:58
	 * </pre>
	 */
	static class RoundJudgeTask implements KGameTimerTask {

		static RoundJudgeTask instance;

		/**
		 * <pre>
		 * 
		 * @param broadDataList
		 * @author CamusHuang
		 * @creation 2013-8-28 下午9:00:46
		 * </pre>
		 */
		static void submitTask() {
			GangWarLogic.lock.lock();
			try {
				cancel();
				instance = new RoundJudgeTask();
				KGame.newTimeSignal(instance, KGangWarConfig.getInstance().WarScanStartDelay, TimeUnit.MILLISECONDS);
			} finally {
				GangWarLogic.lock.unlock();
			}
		}

		static void cancel() {
			GangWarLogic.lock.lock();
			try {
				if (instance != null) {
					instance.cancel = true;
				}
			} finally {
				GangWarLogic.lock.unlock();
			}
		}

		//

		private boolean cancel;

		private RoundJudgeTask() {
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {

			boolean isAllGroupEnd = false;// 是否所有分组均已决出胜负

			GangWarLogic.lock.lock();
			try {
				if (cancel) {
					return null;
				}
				try {
					isAllGroupEnd = GangWarLogic.onTimeSignalForJudgeInRound();
					return null;
				} catch (Exception ex) {
					GangWarLogic.GangWarLogger.error(ex.getMessage(), ex);
					throw new KGameServerException(ex);
				} finally {
					KGame.newTimeSignal(instance, KGangWarConfig.getInstance().WarScanPeroid, TimeUnit.MILLISECONDS);
				}

			} finally {
				GangWarLogic.lock.unlock();

				if (isAllGroupEnd) {
					// 加速单场结束
					GangWarStatusTask.speedToRun();
				}
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
			GangWarLogic.GangWarLogger.error(ex.getMessage(), ex);
		}
	}

	/**
	 * <pre>
	 * 定时扫描处理错误状态
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:52:58
	 * </pre>
	 */
	static class WarErrorScanTask implements KGameTimerTask {

		static WarErrorScanTask instance;

		/**
		 * <pre>
		 * 
		 * @param broadDataList
		 * @author CamusHuang
		 * @creation 2013-8-28 下午9:00:46
		 * </pre>
		 */
		static void submitTask() {
			GangWarLogic.lock.lock();
			try {
				cancel();
				instance = new WarErrorScanTask();
				KGame.newTimeSignal(instance, 2, TimeUnit.SECONDS);
			} finally {
				GangWarLogic.lock.unlock();
			}
		}

		static void cancel() {
			GangWarLogic.lock.lock();
			try {
				if (instance != null) {
					instance.cancel = true;
				}
			} finally {
				GangWarLogic.lock.unlock();
			}
		}

		//

		private boolean cancel;

		private WarErrorScanTask() {
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {

			GangWarLogic.lock.lock();
			try {
				if (cancel) {
					return null;
				}
				try {
					GangWarLogic.onTimeSignalForWarErrorScan();
					return null;
				} catch (Exception ex) {
					GangWarLogic.GangWarLogger.error(ex.getMessage(), ex);
					throw new KGameServerException(ex);
				} finally {
					KGame.newTimeSignal(instance, 10, TimeUnit.SECONDS);
				}

			} finally {
				GangWarLogic.lock.unlock();
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
			GangWarLogic.GangWarLogger.error(ex.getMessage(), ex);
		}
	}
	/**
	 * <pre>
	 * 开战10秒倒计时触发时效
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:52:58
	 * </pre>
	 */
	static class StartCountdownTask implements KGameTimerTask {

		/** 开战倒计时触发时效 */
		private static StartCountdownTask instance;

		/**
		 * <pre>
		 * 开战倒计时触发时效
		 * 
		 * @param broadDataList
		 * @author CamusHuang
		 * @creation 2013-8-28 下午9:00:46
		 * </pre>
		 */
		static void submitTask(long delayTime) {
			GangWarLogic.lock.lock();
			try {
				cancel();
				instance = new StartCountdownTask();
				KGame.newTimeSignal(instance, delayTime, TimeUnit.MILLISECONDS);
			} finally {
				GangWarLogic.lock.unlock();
			}
		}

		static void cancel() {
			GangWarLogic.lock.lock();
			try {
				if (instance != null) {
					instance.cancel = true;
				}
			} finally {
				GangWarLogic.lock.unlock();
			}
		}

		//

		private boolean cancel;

		private StartCountdownTask() {
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			GangWarLogic.lock.lock();
			try {
				if (cancel) {
					return null;
				}
				try {
					// 通知所有军团战场景内的玩家
					KGWRaceSynMsg.sendCountDownMsg();
				} catch (Exception ex) {
					GangWarLogic.GangWarLogger.error(ex.getMessage(), ex);
					throw new KGameServerException(ex);
				}
				return null;
			} finally {
				GangWarLogic.lock.unlock();
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
			GangWarLogic.GangWarLogger.error(ex.getMessage(), ex);
		}
	}

	/**
	 * <pre>
	 * 对战场景内定时世界广播
	 * ----活动开始前30秒，每隔10秒钟，系统会自动向当前场景内的所有玩家发送系统消息：“当前距离活动开始还有X秒”；达到10秒以内后不再进行播报
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 下午9:06:18
	 * </pre>
	 */
	static class RaceBroadcastTask extends LoopTask {
		/** 战斗场景内定时定时世界广播 */
		private static RaceBroadcastTask instance;

		/**
		 * <pre>
		 * 战斗场景内定时定时世界广播
		 * 
		 * @param broadDataList
		 * @author CamusHuang
		 * @creation 2013-8-28 下午9:00:46
		 * </pre>
		 */
		static void submitTask(List<BroadData> broadDataList) {
			cancel();
			instance = new RaceBroadcastTask(broadDataList);
		}

		static void cancel() {
			GangWarLogic.lock.lock();
			try {
				if (instance != null) {
					instance.beCancel();
				}
			} finally {
				GangWarLogic.lock.unlock();
			}
		}

		//

		private RaceBroadcastTask(List<BroadData> broadDataList) {
			super(broadDataList);
		}

		void doWork(BroadData data) {
			if (data != null && data.tips != null) {
				// 通知所有军团战场景内的玩家
				KGangWarMsgPackCenter.sendMsgToRoleInWarOfRound(KSupportFactory.getChatSupport().genSystemChatMsg(data.tips, data.type));
			}
		}
	}

	/**
	 * <pre>
	 * 全世界定时世界广播
	 * ----活动开始前10分钟会发送活动即将开启的广播公告，公告与系统消息同时进行。
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 下午9:06:18
	 * </pre>
	 */
	static class WorldBroadcastTask extends LoopTask {
		/** 全世界定时世界广播，最多支持两个并行实例，可以N个，具体看代码调用到底需要多少个实现 */
		private static WorldBroadcastTask[] instance = new WorldBroadcastTask[2];

		/**
		 * <pre>
		 * 全世界定时世界广播
		 * 
		 * @param broadDataList
		 * @author CamusHuang
		 * @creation 2013-8-28 下午9:00:46
		 * </pre>
		 */
		static void submitTask(int index, List<BroadData> broadDataList) {
			cancel(index);
			instance[index] = new WorldBroadcastTask(broadDataList);
		}

		static void cancel(int index) {
			GangWarLogic.lock.lock();
			try {
				if (instance[index] != null) {
					instance[index].beCancel();
				}
			} finally {
				GangWarLogic.lock.unlock();
			}
		}

		static void cancelAll() {
			GangWarLogic.lock.lock();
			try {
				for (WorldBroadcastTask temp : instance) {
					if (temp != null) {
						temp.beCancel();
					}
				}
			} finally {
				GangWarLogic.lock.unlock();
			}
		}

		//
		private WorldBroadcastTask(List<BroadData> broadDataList) {
			super(broadDataList);
		}

		void doWork(BroadData data) {
			if (data != null && data.tips != null) {
				KSupportFactory.getChatSupport().sendSystemChat(data.tips, data.type);
			}
		}
	}

	/**
	 * <pre>
	 * 循环任务，直到所有任务发送完毕
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 下午9:06:18
	 * </pre>
	 */
	abstract static class LoopTask implements KGameTimerTask {
		private List<BroadData> broadDataList;
		private int broadIndex = -1;

		private boolean cancel;

		private LoopTask(List<BroadData> broadDataList) {
			this.broadDataList = broadDataList;
			goForNext();
		}

		private void goForNext() {
			broadIndex++;
			long nowTime= System.currentTimeMillis();
			while (broadDataList.size() > broadIndex) {
				BroadData data = broadDataList.get(broadIndex);
				if(data.sendTime > nowTime){
					KGame.newTimeSignal(this, data.sendTime - nowTime, TimeUnit.MILLISECONDS);
					break;
				} else {
					broadIndex++;
				}
			}
		}

		void beCancel() {
			GangWarLogic.lock.lock();
			try {
				cancel = true;
			} finally {
				GangWarLogic.lock.unlock();
			}
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal arg0) throws KGameServerException {
			GangWarLogic.lock.lock();
			try {
				if (cancel) {
					return null;
				}
				try {
					BroadData data = broadDataList.get(broadIndex);
					if (data != null) {
						doWork(data);
					}
				} catch (Exception ex) {
					GangWarLogic.GangWarLogger.error(ex.getMessage(), ex);
					throw new KGameServerException(ex);
				} finally {
					goForNext();
				}
				return null;
			} finally {
				GangWarLogic.lock.unlock();
			}
		}

		abstract void doWork(BroadData data);

		@Override
		public void done(KGameTimeSignal arg0) {
		}

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public void rejected(RejectedExecutionException ex) {
			GangWarLogic.GangWarLogger.error(ex.getMessage(), ex);
		}

	}

	/**
	 * <pre>
	 * 广播内容
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 下午9:01:57
	 * </pre>
	 */
	static class BroadData {
		long sendTime;
		String tips;
		KWordBroadcastType type;

		BroadData(long sendTime, String tips, KWordBroadcastType type) {
			this.sendTime = sendTime;
			this.tips = tips;
			this.type = type;
		}
	}
}
