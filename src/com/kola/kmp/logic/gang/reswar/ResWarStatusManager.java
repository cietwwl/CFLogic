package com.kola.kmp.logic.gang.reswar;

import java.util.Date;
import java.util.Set;

import javax.management.timer.Timer;

import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.KGameCheatCenter.CheatResult;
import com.kola.kmp.logic.gang.reswar.ResWarDataCenter.CacheData;
import com.kola.kmp.logic.gang.reswar.ResWarTaskManager.GangResWarStatusTask;
import com.kola.kmp.logic.gang.reswar.ResWarTaskManager.JudgeInWarTask;
import com.kola.kmp.logic.gang.reswar.ResWarTaskManager.ScoreProduceTask;
import com.kola.kmp.logic.gang.reswar.ResWarTaskManager.WarErrorScanTask;
import com.kola.kmp.logic.gang.reswar.message.KGrwSynMsg;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.tips.GangResWarTips;

/**
 * <pre>
 * 军团资源战状态管理
 * 
 * @author CamusHuang
 * @creation 2013-8-29 下午6:27:29
 * </pre>
 */
class ResWarStatusManager {
	/**
	 * <pre>
	 * 当前军团资源战的状态
	 * </pre>
	 */
	private static ResWarStatusEnum nowWarStatus;

	// 管理本周军团资源战的时间节点
	private static WarTime mWarTime;

	/**
	 * <pre>
	 * 服务器启动完毕
	 * 初始化军团资源战的相关状态
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-14 下午11:27:11
	 * </pre>
	 */
	static void notifyCacheLoadComplete() {
		// 初始化军团资源战的相关状态
		ResWarDataCenter.rwLock.writeLock().lock();
		try {
			{// 初始化本周军团资源战的相关时间节点
				long nowTime = System.currentTimeMillis();
				long thisWeekStartTime = UtilTool.getThisWeekStart(nowTime).getTimeInMillis();
				mWarTime = new WarTime(thisWeekStartTime);
				ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：初始化本周军团资源战的相关时间节点，开战时间：{}({})", KGameUtilTool.genTimeStrForClient(mWarTime.warStartTime), new Date(mWarTime.warStartTime));
			}
			{// 决策：进行本周军团资源战，还是等下周再继续

				// 加载数据
				CacheData cacheData = ResWarDataCenter.loadData();

				long releaseTimeForBidEnd = mWarTime.getReleaseTimeForBidEnd();
				if (releaseTimeForBidEnd < Timer.ONE_MINUTE) {
					// 非竞价期（准备、开战、休战）：尝试恢复运行时状态及数据
					if (!tryToRestData(cacheData)) {
						// 尝试恢复运行时状态及数据失败：直接进入休战期，等待下周再战
						ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：当前时间已经过了本周竞价期，运行时状态及数据恢复失败，直接进入休战期、清空竞价表，等待下周再战");
						// 清空数据，清空竞价，竞价费用全返还
						clearDataForResetFail();
						// 进入下周军团资源战
						gotoRestForNextWeek();
					}
				} else {
					// 竞价期：直接进入竞价期
					ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：进入本周竞价期");
					TaskNotify_RestEnd_BidStart();
				}
			}
		} finally {
			ResWarDataCenter.rwLock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 非竞价期：尝试恢复运行时状态及数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-10-29 下午8:13:23
	 * </pre>
	 */
	private static boolean tryToRestData(CacheData cacheData) {

		if (cacheData == null) {
			ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战【开服恢复】：还原数据不存在");
			return false;
		}

		ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战【开服恢复】：开服尝试恢复状态");

		// 数据填充
		nowWarStatus = cacheData.nowStatus;

		// 根据停服时的军团资源战状态，执行
		switch (nowWarStatus) {
		case REST_START:
			ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战【开服恢复】：-->休战期->进入报名期->直接返回恢复失败");
			return false;// 返回失败，由外部执行进入下周资源战的相关操作
		case BID_START:
			// 竞价期：直接进入竞价期
			ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战【开服恢复】：-->本周竞价期");
			TaskNotify_Status(ResWarStatusEnum.BID_START);
			return true;
		case READY_START:
			// 准备期
			ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战【开服恢复】：-->准备期");
			TaskNotify_Status(ResWarStatusEnum.READY_START);
			return true;
		case WAR_START:
			// 军团资源战开始
			ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战【开服恢复】：-->开战期");
			TaskNotify_Status(ResWarStatusEnum.WAR_START);
			return true;
		default:
			ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战【开服恢复】：-->非法状态，恢复失败");
			// 非法状态，直接返回恢复失败
			return false;// 返回失败，由外部执行进入下周资源战的相关操作
		}
	}

	/**
	 * <pre>
	 * 清空数据，清空竞价，竞价费用全返还
	 * 
	 * @author CamusHuang
	 * @creation 2014-5-14 下午9:15:43
	 * </pre>
	 */
	private static void clearDataForResetFail() {
		for (ResWarCity city : ResWarDataCenter.allCityMap.values()) {
			city.clearDataForResetFail();
		}
	}

	/**
	 * <pre>
	 * 直接进入休战期，等待下周再战
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-15 上午12:02:36
	 * </pre>
	 */
	private static void gotoRestForNextWeek() {

		// CTODO 开服或军团资源战结束或对阵分组失败进入休战期需要做什么？军团资源战数据清理
		ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：状态(进入休战期，等待下周再战):进入休战期=>【休战期结束->进入竞价期】");
		//
		//
		/** --------------------- */
		// 设置下一周的时间节点
		long nowTime = System.currentTimeMillis();
		long nextWeekStartTime = UtilTool.getNextWeekStart(nowTime).getTimeInMillis();
		mWarTime = new WarTime(nextWeekStartTime);
		// 进入休战期
		nowWarStatus = ResWarStatusEnum.REST_START;
		// 提交时效任务：休战期结束->进入竞价期
		long delayTime = mWarTime.bidStartTime - nowTime;
		GangResWarStatusTask.submitTast(ResWarStatusEnum.BID_START, delayTime);

		/** --------------------- */
	}

	/**
	 * <pre>
	 * 时效通知
	 * 
	 * @param status
	 * @author CamusHuang
	 * @creation 2013-7-14 下午4:49:28
	 * </pre>
	 */
	static void TaskNotify_Status(ResWarStatusEnum status) {
		ResWarDataCenter.rwLock.writeLock().lock();
		try {
			ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：下一个状态通知：{}", status.name);
			switch (status) {
			case BID_START:// 【开始竞价期】
				TaskNotify_RestEnd_BidStart();
				break;
			case READY_START:// 【结束竞价期】，【开始准备期】
				TaskNotify_BidEnd_ReadyStart();
				break;
			case WAR_START:// 【结束准备期】，【开始对战期】
				TaskNotify_ReadyEnd_WarStart();
				break;
			case REST_START:// 【结束对战期】，【开始休战期】
				TaskNotify_WarEnd_RestStart();
				break;
			default:
				ResWarDataCenter.RESWAR_LOGGER.error("军团资源战：错误的状态通知：{}", status.name);
				break;
			}
		} finally {
			ResWarDataCenter.rwLock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 【开始竞价期】->【结束竞价期】&【开始准备期】
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-15 上午12:02:36
	 * </pre>
	 */
	private static void TaskNotify_RestEnd_BidStart() {

		ResWarDataCenter.RESWAR_LOGGER.warn(GangResWarTips.军团资源战竞价开始);

		// 广播
		ResWarSystemBrocast.onBidStart();

		// CTODO 需要做什么？
		//
		/** --------------------- */
		long nowTime = System.currentTimeMillis();
		// 当前状态：【开始竞价期】
		nowWarStatus = ResWarStatusEnum.BID_START;
		// 下一状态：【结束竞价期】&【开始准备期】
		long delayTime = mWarTime.warReadyTime - nowTime;
		GangResWarStatusTask.submitTast(ResWarStatusEnum.READY_START, delayTime);

		/** --------------------- */

		ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：状态:【结束休战期】【开始竞价期】下一状态：【结束竞价期】【开始准备期】");
	}

	/**
	 * <pre>
	 * 【开始准备期】=>【结束准备期】&【开始对战期】
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-14 下午5:04:20
	 * </pre>
	 */
	private static void TaskNotify_BidEnd_ReadyStart() {

		// 收集所有城市竞价榜军团名单
		Set<Long> bidGangIds = ResWarDataCenter.getGangIdsInAllCityRanks();
		{
			// 入围判决
			ResWarDataCenter.notifyForBidEnd();
			// 广播
			ResWarSystemBrocast.onBidEnd();
		}

		ResWarDataCenter.RESWAR_LOGGER.warn(StringUtil.format(GangResWarTips.军团资源战将于x时间开始, UtilTool.DATE_FORMAT2.format(new Date(mWarTime.warStartTime))));
		// 广播
		ResWarSystemBrocast.onWarReady();

		// CTODO 需要做什么？
		//
		/** --------------------- */
		long nowTime = System.currentTimeMillis();
		// 当前状态：【开始准备期】
		nowWarStatus = ResWarStatusEnum.READY_START;
		// 下一状态：【结束准备期】&【开始对战期】
		long delayTime = mWarTime.warStartTime - nowTime;
		GangResWarStatusTask.submitTast(ResWarStatusEnum.WAR_START, delayTime);

		// 通知客户端刷新城市列表
		KGrwSynMsg.sendCityListStatusChangeMsg(bidGangIds);

		ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：状态:【结束竞价】【开始准备期】 下一状态：【结束准备期】【开始对战期】");
	}

	/**
	 * <pre>
	 * 【开始对战期】=>【结束对战期】&【进入休战期】
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-14 下午5:04:20
	 * </pre>
	 */
	private static void TaskNotify_ReadyEnd_WarStart() {

		// 收集所有城市对战军团名单
		Set<Long> pkGangIds = ResWarDataCenter.getGangIdsInAllCityWars();

		// 广播
		ResWarSystemBrocast.onWarStart();

		// CTODO 需要做什么？
		//
		/** --------------------- */
		long nowTime = System.currentTimeMillis();
		// 当前状态：【结束准备期】&【开始对战期】
		nowWarStatus = ResWarStatusEnum.WAR_START;
		// 下一状态：【结束对战期】&【开始休战期】
		long delayTime = mWarTime.warEndTime - nowTime;
		GangResWarStatusTask.submitTast(ResWarStatusEnum.REST_START, delayTime);

		/** --------------------- */
		// 开始时效任务
		ScoreProduceTask.submitTask();
		WarErrorScanTask.submitTask();
		JudgeInWarTask.submitTask();

		// 通知客户端刷新城市列表
		KGrwSynMsg.sendCityListStatusChangeMsg(pkGangIds);
		
		// 通知活动模块
		KResWarActivity.instance.notifyActivityOpenStatus(true);
		//
		ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：状态:【结束准备期】【开始对战期】下一状态：【结束对战期】【开始休战期】");
	}

	/**
	 * <pre>
	 * 时效通知：对战结束
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-14 下午5:06:55
	 * </pre>
	 */
	private static void TaskNotify_WarEnd_RestStart() {

		// 收集所有城市对战军团名单
		Set<Long> pkGangIds = ResWarDataCenter.getGangIdsInAllCityWars();

		// 关闭时效任务
		ScoreProduceTask.cancel();
		WarErrorScanTask.cancel();
		JudgeInWarTask.cancel();

		// 确定胜负，发送最终奖励，清理现场
		ResWarDataCenter.judgeForWarEnd();

		// 系统公告
		ResWarSystemBrocast.onWarEnd();

		//
		/** --------------------- */
		gotoRestForNextWeek();

		/** --------------------- */

		// 保存数据
		ResWarDataCenter.saveData("-" + UtilTool.DATE_FORMAT7.format(new Date()) + "-对战结束");

		// 通知客户端刷新城市列表
		KGrwSynMsg.sendCityListStatusChangeMsg(pkGangIds);
		
		// 通知活动模块
		KResWarActivity.instance.notifyActivityOpenStatus(false);
		//
		ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：状态:【结束对战期】【开始休战期】下一状态：【结束休战期】【开始竞价期】");
	}

	/**
	 * <pre>
	 * 停止当前军团资源战、重新加载配置，且重新开启军团资源战
	 * 一般在测试环境中使用GM指令调用
	 * 
	 * 不允许在军团资源战在开战状态下进行重启操作
	 * 
	 * @author CamusHuang
	 * @creation 2013-10-11 下午10:57:08
	 * </pre>
	 */
	static CheatResult stopGangWar(boolean isForce) {

		CheatResult result = new CheatResult();

		ResWarDataCenter.rwLock.writeLock().lock();
		try {
			if (nowWarStatus == ResWarStatusEnum.WAR_START) {
				if (!isForce) {
					result.tips = GangResWarTips.军团资源战已开场不允许暂停;
					return result;
				}
			}

			// 停止时效任务
			ResWarTaskManager.stopGangResWar();
			// 重置状态和时间
			nowWarStatus = null;
			mWarTime = null;

			// 清理数据
			ResWarDataCenter.clearData();

			result.isSuccess = true;
			return result;
		} finally {
			ResWarDataCenter.rwLock.writeLock().unlock();
		}
	}

	static ResWarStatusEnum getNowStatus() {
		return nowWarStatus;
	}

	static WarTime getWarTime() {
		return mWarTime;
	}

	static int getWarReleaseTime() {
		ResWarDataCenter.rwLock.writeLock().lock();
		try {
			if (nowWarStatus != ResWarStatusEnum.WAR_START) {
				return 0;
			}
			return Math.max(0, (int) ((mWarTime.warEndTime - System.currentTimeMillis()) / Timer.ONE_SECOND));
		} finally {
			ResWarDataCenter.rwLock.writeLock().unlock();
		}
	}

	/**
	 * <pre>
	 * 管理本周军团资源战的时间节点
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-27 下午4:30:23
	 * </pre>
	 */
	static class WarTime {
		// =========================
		/** 竞价时间为：周一0：00至周日00:00(距周一0点0分的毫秒数) */
		final long bidStartTime;// 结束休息，开始竞价
		/** 战斗开始和结束时间(距周一0点0分的毫秒数) */
		final long warReadyTime;// 结束竞价，开始准备
		final long warStartTime;// 结束准备，开始对战
		final long warEndTime;// 结束对战，开始休息

		private WarTime(long thisWeekStartTime) {
			//
			bidStartTime = thisWeekStartTime + KResWarConfig.bidStartTime;
			//
			warReadyTime = thisWeekStartTime + KResWarConfig.bidEndTime;
			warStartTime = thisWeekStartTime + KResWarConfig.warStartTime;
			warEndTime = thisWeekStartTime + KResWarConfig.warEndTime;

			ResWarDataCenter.RESWAR_LOGGER.warn("军团资源战：竞价开始时间={}", UtilTool.DATE_FORMAT2.format(bidStartTime));
		}

		long getReleaseTimeForBidEnd() {
			return warReadyTime - System.currentTimeMillis();
		}
	}
}
