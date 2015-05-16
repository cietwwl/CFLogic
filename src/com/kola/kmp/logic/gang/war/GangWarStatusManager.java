package com.kola.kmp.logic.gang.war;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.management.timer.Timer;

import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.KGameCheatCenter.CheatResult;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.gang.war.GangWarDataCenter.CacheData;
import com.kola.kmp.logic.gang.war.GangWarTaskManager.GangWarStatusTask;
import com.kola.kmp.logic.gang.war.GangWarTaskManager.RoundJudgeTask;
import com.kola.kmp.logic.gang.war.GangWarTaskManager.WarErrorScanTask;
import com.kola.kmp.logic.gang.war.KGangWarConfig.RoundTimeConfig;
import com.kola.kmp.logic.gang.war.message.KGWPushMsg;
import com.kola.kmp.logic.rank.gang.GangRank;
import com.kola.kmp.logic.rank.gang.GangRankElementWarSignUp;
import com.kola.kmp.logic.rank.gang.KGangRankLogic;
import com.kola.kmp.logic.rank.gang.KGangRankTypeEnum;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.tips.GangWarTips;

/**
 * <pre>
 * 军团战状态管理
 * 1.开服->2
 * 2.报名->3
 * 3.等待->4
 * 4.准备->5
 * 5.对战
 * 		非最后一场->3
 * 		最后一场->6
 * 6.休战->2
 * 
 * @author CamusHuang
 * @creation 2013-8-29 下午6:27:29
 * </pre>
 */
class GangWarStatusManager {
	/**
	 * <pre>
	 * 当前军团战的状态
	 * </pre>
	 */
	private static GangWarStatusEnum warStatusEnum;

	// 管理本周军团战的时间节点
	private static WarTime mWarTime;

	/**
	 * <pre>
	 * 服务器启动完毕
	 * 初始化军团战的相关状态
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-14 下午11:27:11
	 * </pre>
	 */
	static void notifyCacheLoadComplete() {
		GangWarLogic.GangWarLogger.warn("军团战：开始初始化...");

		// 初始化军团战的相关状态
		GangWarLogic.lock.lock();
		try {
			{// 初始化本周军团战的相关时间节点
				long nowTime = System.currentTimeMillis();
				long thisWeekStartTime = UtilTool.getThisWeekStart(nowTime).getTimeInMillis();
				mWarTime = new WarTime(thisWeekStartTime, KGangWarConfig.getInstance(), true);
				GangWarLogic.GangWarLogger.warn("军团战：初始化本周军团战的相关时间节点，第1场开始时间：{}({})", KGameUtilTool.genTimeStrForClient(mWarTime.getTime_Start(1)), new Date(mWarTime.getTime_Start(1)));
			}
			{// 决策：进行本周军团战，还是等下周再继续

				// 加载数据
				CacheData cacheData = GangWarDataCenter.loadData();

				long delayTimeForSignUpEnd = mWarTime.signUpEndTime-System.currentTimeMillis();
				if (delayTimeForSignUpEnd <= 0) {
					// 报名期已过：尝试恢复军团战运行时状态及数据
					if (!tryToRestData(cacheData)) {
						// 尝试重置军团战运行时数据失败：当前时间已经过了本周报名期，直接进入休战期，等待下周再战
						GangWarLogic.GangWarLogger.warn("军团战：当前时间已经过了本周报名期，军团战状态恢复失败，直接进入休战期、清空报名表，等待下周再战");
						// 清空报名榜、通报到各军团
						GangWarDataCenter.clearSignUpRankAndNotifyGangs(false, 0, KGangRankLogic.getRank(KGangRankTypeEnum.军团战报名), Collections.<Long> emptySet());
						// 进入下周军团战
						gotoRestForNextWeek();
					}
				} else {
					// 报名期：直接进入报名期
					GangWarLogic.GangWarLogger.warn("军团战：进入本周报名期");
					TaskNotify_SignUpStart();
				}
			}
		} finally {
			GangWarLogic.lock.unlock();
		}
	}

	/**
	 * <pre>
	 * 非报名期：尝试重置军团战运行时状态及数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-10-29 下午8:13:23
	 * </pre>
	 */
	private static boolean tryToRestData(CacheData cacheData) {

		if (cacheData == null) {
			GangWarLogic.GangWarLogger.warn("军团战【开服恢复】：还原数据不存在");
			return false;
		}

		GangWarLogic.GangWarLogger.warn("军团战【开服恢复】：开服尝试恢复状态");

		// 数据填充
		warStatusEnum = cacheData.nowStatus;
		GangWarDataCenter.resetFromCacheData(cacheData);

		// 根据停服时的军团战状态，执行
		switch (warStatusEnum) {
		case SIGNUP_START_NOW:
			// 报名期：直接进入报名期
			GangWarLogic.GangWarLogger.warn("军团战【开服恢复】：-->本周报名期");
			TaskNotify_Status(GangWarStatusEnum.SIGNUP_START_NOW);
			return true;
		case WAR_WAIT_NOW:
			// 等待期
			GangWarLogic.GangWarLogger.warn("军团战【开服恢复】：-->等待期");
			TaskNotify_Status(GangWarStatusEnum.WAR_WAIT_NOW);
			return true;
		case WAR_ROUND_READY_NOW:
			// 准备期
			GangWarLogic.GangWarLogger.warn("军团战【开服恢复】：-->准备期");
			TaskNotify_Status(GangWarStatusEnum.WAR_ROUND_READY_NOW);
			return true;
		case WAR_ROUND_START_NOW:
			// 军团战开始
			GangWarLogic.GangWarLogger.warn("军团战【开服恢复】：-->开战期");
			TaskNotify_Status(GangWarStatusEnum.WAR_ROUND_START_NOW);
			return true;
		case REST_START_NOW:
			GangWarLogic.GangWarLogger.warn("军团战【开服恢复】：-->休战期->直接返回恢复失败");
			return false;
//			
//			if (cacheData.nextStatus == null) {
//				// 非法状态，直接返回恢复失败
//				GangWarLogic.GangWarLogger.warn("军团战【开服恢复】：-->休战期->非法状态，恢复失败");
//				return false;
//			} else {
//				switch (cacheData.nextStatus) {
//				case SIGNUP_START_NOW:
//					// 进入报名期，直接返回恢复失败
//					GangWarLogic.GangWarLogger.warn("军团战【开服恢复】：-->休战期->进入报名期->直接返回恢复失败");
//					return false;
//				case WAR_WAIT_NOW:
//				// 当前休战期，下一状态进入等待期
//				{
//					GangWarLogic.GangWarLogger.warn("军团战【开服恢复】：-->休战期->进入等待期");
//					final int round = GangWarDataCenter.getNowRoundId();
//					final int lastRound = round - 1;
//					// 公告
//					KWordBroadcastType _boradcastType = KWordBroadcastType.军团战_第x场军团战结束;
//					KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(_boradcastType.content, lastRound), _boradcastType);
//					GangWarLogic.GangWarLogger.warn(StringUtil.format(_boradcastType.content, lastRound));
//
//					/** --------------------- */
//					long nowTime = System.currentTimeMillis();
//					// 当前状态：【结束第{}场对战期】&【进入休战期】
//					warStatusEnum = GangWarStatusEnum.REST_START_NOW;
//					// 下一状态：【第{}场准备】
//					long delayTime = mWarTime.getTime_Ready(round) - nowTime;
//					GangWarTaskManager.GangWarStatusTask.submitTast(GangWarStatusEnum.WAR_WAIT_NOW, delayTime);
//
//					/** --------------------- */
//					return true;
//				}
//				default:
//					GangWarLogic.GangWarLogger.warn("军团战【开服恢复】：-->休战期->非法状态，恢复失败");
//					// 非法状态，直接返回恢复失败
//					return false;
//				}
//			}
		default:
			GangWarLogic.GangWarLogger.warn("军团战【开服恢复】：-->非法状态，恢复失败");
			// 非法状态，直接返回恢复失败
			return false;
		}
	}

	/**
	 * <pre>
	 * 停止当前军团战、重新加载配置，且重新开启军团战
	 * 一般在测试环境中使用GM指令调用
	 * 
	 * 不允许在军团战在进入、开战状态下进行重启操作
	 * 
	 * @author CamusHuang
	 * @creation 2013-10-11 下午10:57:08
	 * </pre>
	 */
	static CheatResult stopGangWar(boolean isForce) {

		CheatResult result = new CheatResult();

		GangWarLogic.lock.lock();
		try {
			if (warStatusEnum == GangWarStatusEnum.WAR_ROUND_READY_NOW || warStatusEnum == GangWarStatusEnum.WAR_ROUND_START_NOW) {
				if (!isForce) {
					result.tips = GangWarTips.军团战已开场不允许暂停;
					return result;
				}
			}

			// 停止时效任务
			GangWarTaskManager.stopGangWar();
			// 重置状态和时间
			warStatusEnum = null;
			mWarTime = null;

			// 清理数据
			GangWarDataCenter.clearData();

			result.isSuccess = true;
			return result;
		} finally {
			GangWarLogic.lock.unlock();
		}
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
	public static void TaskNotify_Status(GangWarStatusEnum status) {
		GangWarLogic.lock.lock();
		try {
			int roundId = GangWarDataCenter.getNowRoundId();

			GangWarLogic.GangWarLogger.warn("军团战：下一个状态通知：{} 场次={}", status.name, roundId);
			switch (status) {
			case SIGNUP_START_NOW:// 【开始报名期】
				TaskNotify_SignUpStart();
				break;
			case SIGNUP_END:// 【结束报名期】
				boolean isSuccess = TaskNotify_SignUpEnd();
				if (isSuccess) {
					TaskNotify_Round_Wait(roundId);
				}
				break;
			case WAR_WAIT_NOW:// 【结束对战期】，【开始第x场等待期】
				TaskNotify_Round_Wait(roundId);
				break;
			case WAR_ROUND_READY_NOW:// 【结束第x场等待期】，【开始第x场准备期】
				TaskNotify_Round_WaitEnd_WarReady(roundId);
				break;
			case WAR_ROUND_START_NOW:// 【结束第x场准备期】，【开始第x场对战期】
				TaskNotify_Round_ReadyEnd_WarStart(roundId);
				break;
			case WAR_ROUND_END_NOW:// 【结束第x场对战期】，【开始等待期】或【开始休战期】
				if (roundId != KGangWarConfig.getInstance().MaxRound) {
					TaskNotify_Round_WarEnd(roundId);
				} else {
					TaskNotify_FinalEnd();
				}
				break;
			default:
				GangWarLogic.GangWarLogger.error("军团战：错误的状态通知：{}", status.name);
				break;
			}
		} finally {
			GangWarLogic.lock.unlock();
		}
	}

	/**
	 * <pre>
	 * 当前状态：【开始报名期】 下一状态：【开始准备期】
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-15 上午12:02:36
	 * </pre>
	 */
	private static void TaskNotify_SignUpStart() {
		final int nextRound = 1;
		GangWarDataCenter.setNowRoundId(nextRound);

		// 系统公告
		GangWarSystemBrocast.onSignupStart();

		// CTODO 需要做什么？
		//
		//
		//
		/** --------------------- */
		long nowTime = System.currentTimeMillis();
		// 当前状态：【开始报名期】
		warStatusEnum = GangWarStatusEnum.SIGNUP_START_NOW;
		// 下一状态：【开始等待期】
		long delayTime = mWarTime.signUpEndTime - nowTime;
		GangWarTaskManager.GangWarStatusTask.submitTast(GangWarStatusEnum.SIGNUP_END, delayTime);

		/** --------------------- */
		// 报名前清空相关数据
		if (Math.abs(nowTime - mWarTime.signUpStartTime) < 3 * Timer.ONE_MINUTE) {
			// 清空排行榜
			GangRank<GangRankElementWarSignUp> rank = KGangRankLogic.getRank(KGangRankTypeEnum.军团战报名);
			rank.clear();
			// 清空所有军团的繁荣度
			KGangLogic.clearAllGangFlourish();
		}

		// 同步客户端状态
		KGWPushMsg.syncGangWarState();

		GangWarLogic.GangWarLogger.warn("军团战：状态:【开始报名期】=>【开始等待期】");
	}

	/**
	 * <pre>
	 * 【结束报名期】
	 * 
	 * @return 是否顺利开启活动
	 * @author CamusHuang
	 * @creation 2014-5-20 下午2:54:56
	 * </pre>
	 */
	private static boolean TaskNotify_SignUpEnd() {

		// 清理上周军团战勋章
		KGangLogic.clearGangWarMedalForSignUpEnd();
		GangWarLogic.GangWarLogger.warn("军团战：第1场：报名结束，进行分组");
		// CTODO 需要做什么？

		// 审核报名榜，选出入围军团战的军团，同时初始化各场对战数据
		boolean isSuccess = GangWarDataCenter.checkUpSignUpRanksAndInitRounds();
		if (!isSuccess) {
			// 公告：活动开启失败
			KWordBroadcastType _boradcastType = KWordBroadcastType.军团战_由于第1场军团战不满足分组要求本周军团战取消;
			KSupportFactory.getChatSupport().sendSystemChat(_boradcastType.content, _boradcastType);
			GangWarLogic.GangWarLogger.warn("军团战：开启失败，由于不满足分组要求，本周军团战取消！");

			// 如果活动开启失败，则直接进入休战期，等待下周再战
			gotoRestForNextWeek();
			return isSuccess;
		}

		// 公告：报名结束，名单公报
		GangWarSystemBrocast.onSignupEnd();

		// 第1场分组
		GangWarLogic.GangWarLogger.warn("军团战：第1场：分组开始");
		//
		GangWarDataCenter.groupRound(1);
		//
		GangWarLogic.GangWarLogger.warn("军团战：第1场：分组完成");

		return isSuccess;
	}

	/**
	 * <pre>
	 * 当前状态：【开始等待期】 下一状态：【开始准备期】
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-14 下午5:04:20
	 * </pre>
	 */
	private static void TaskNotify_Round_Wait(final int round) {

		// / 初始化等待期定时广播
		GangWarSystemBrocast.onRoundWait(round);

		// CTODO 需要做什么？
		//
		/** --------------------- */
		long nowTime = System.currentTimeMillis();
		// 当前状态：【开始第{}场等待期】
		warStatusEnum = GangWarStatusEnum.WAR_WAIT_NOW;
		// 下一状态：【开始第{}场准备期】
		long delayTime = mWarTime.getTime_Ready(round) - nowTime;
		GangWarTaskManager.GangWarStatusTask.submitTast(GangWarStatusEnum.WAR_ROUND_READY_NOW, delayTime);

		// 同步客户端状态
		KGWPushMsg.syncGangWarState();

		GangWarLogic.GangWarLogger.warn("军团战：状态:【开始第{}场等待期】=>【开始第{}场准备期】", round, round);
	}

	/**
	 * <pre>
	 * 当前状态：【开始准备期】 下一状态：【开始对战期】
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-14 下午5:04:20
	 * </pre>
	 */
	private static void TaskNotify_Round_WaitEnd_WarReady(final int round) {

		// 系统公告
		GangWarSystemBrocast.onRoundReady(round);

		// CTODO 需要做什么？
		//
		/** --------------------- */
		long nowTime = System.currentTimeMillis();
		// 当前状态：【开始第{}场准备期】
		warStatusEnum = GangWarStatusEnum.WAR_ROUND_READY_NOW;
		// 下一状态：【开始第{}场对战期】
		long delayTime = mWarTime.getTime_Start(round) - nowTime;
		GangWarTaskManager.GangWarStatusTask.submitTast(GangWarStatusEnum.WAR_ROUND_START_NOW, delayTime);

		/** --------------------- */

		// 通知活动模块
		KGangWarActivity.instance.notifyActivityOpenStatus(true);

		// 同步客户端状态
		KGWPushMsg.syncGangWarState();
		KGWPushMsg.syncGangWarIcon(true);
		//
		GangWarLogic.GangWarLogger.warn("军团战：状态:【开始第{}场准备期】=>【开始第{}场对战期】", round, round);
	}

	/**
	 * <pre>
	 * 当前状态：【开始对战期】 下一状态：【开始等待期】或【开始休战期】
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-14 下午5:04:20
	 * </pre>
	 */
	private static void TaskNotify_Round_ReadyEnd_WarStart(final int round) {

		// 系统公告、定时广播
		GangWarSystemBrocast.onRoundStart(round);

		// CTODO 需要做什么？
		//
		//
		/** --------------------- */
		long nowTime = System.currentTimeMillis();
		// 当前状态：【开始第{}场对战期】
		warStatusEnum = GangWarStatusEnum.WAR_ROUND_START_NOW;
		// 下一状态：【开始等待期】或【开始休战期】
		long delayTime = mWarTime.getTime_End(round) - nowTime;
		GangWarStatusTask.submitTast(GangWarStatusEnum.WAR_ROUND_END_NOW, delayTime);

		/** --------------------- */

		// 开始时效任务
		WarErrorScanTask.submitTask();
		RoundJudgeTask.submitTask();

		// 同步客户端状态
		KGWPushMsg.syncGangWarState();
		//
		GangWarLogic.GangWarLogger.warn("军团战：状态:【开始第{}场对战期】=>【开始等待期】或【开始休战期】", round);
	}

	/**
	 * <pre>
	 * 当前状态：【开始等待期】 下一状态：【开始准备期】
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-14 下午5:06:55
	 * </pre>
	 */
	private static void TaskNotify_Round_WarEnd(final int round) {

		// 系统公告
		GangWarSystemBrocast.onRoundEnd(round);

		// CTODO 需要做什么？

		// 关闭时效任务
		WarErrorScanTask.cancel();
		RoundJudgeTask.cancel();

		//
		GangWarRound nowRound = GangWarDataCenter.getNowRoundData();
		{
			// 确定胜负，清理现场
			nowRound.judgeForRoundEnd();

			// 发放单场奖励
			nowRound.sendRoundReward();
		}
		
		// 保存数据
		GangWarDataCenter.saveData("-" + UtilTool.DATE_FORMAT7.format(new Date()) + "-第" + round + "场结束");

		//
		// 下场
		final int nextRound = round + 1;
		GangWarLogic.GangWarLogger.warn("军团战：状态:【第{}场对战结束】=>【开始第{}场等待期】", round, nextRound);

		// 下场分组
		GangWarDataCenter.groupRound(nextRound);

		/** --------------------- */

		TaskNotify_Round_Wait(nextRound);

		/** --------------------- */
		// 同步客户端状态
		KGWPushMsg.syncGangWarIcon(false);
		
		// 通知活动模块
		KGangWarActivity.instance.notifyActivityOpenStatus(false);

		// 保存数据
		GangWarDataCenter.saveData("-" + UtilTool.DATE_FORMAT7.format(new Date()) + "-第" + nextRound + "场分组完成");
	}

	/**
	 * <pre>
	 * 时效通知：最后一场结束
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-14 下午5:06:55
	 * </pre>
	 */
	private static void TaskNotify_FinalEnd() {
		final int round = KGangWarConfig.getInstance().MaxRound;

		// 关闭时效任务
		WarErrorScanTask.cancel();
		RoundJudgeTask.cancel();

		//
		GangWarRound nowRound = GangWarDataCenter.getNowRoundData();
		{
			// 确定胜负，清理现场
			nowRound.judgeForRoundEnd();

			// 发送本场奖励
			nowRound.sendRoundReward();

			// 系统通知
			GangWarSystemBrocast.onRoundEnd(round);
		}

		// 发送最终奖励
		GangWarDataCenter.sendFinallyRewardToAllWarGangs();

		// 系统通知
		GangWarSystemBrocast.onWarEnd();
		//
		/** --------------------- */
		gotoRestForNextWeek();

		/** --------------------- */

		// 保存数据
		GangWarDataCenter.saveData("-" + UtilTool.DATE_FORMAT7.format(new Date()) + "-第" + round + "场结束");

		// 同步客户端状态
		KGWPushMsg.syncGangWarState();

		// 通知活动模块
		KGangWarActivity.instance.notifyActivityOpenStatus(false);

		//
		GangWarLogic.GangWarLogger.warn("军团战：状态:【第{}场对战结束】=>【开始休战期】", round);
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

		// CTODO 开服或军团战结束或对阵分组失败进入休战期需要做什么？军团战数据清理
		GangWarLogic.GangWarLogger.warn("军团战：状态(进入休战期，等待下周再战):【进入休战期】=>【休战期结束：进入报名期】");
		//
		//
		/** --------------------- */
		// 设置下一周的时间节点
		long nowTime = System.currentTimeMillis();
		long nextWeekStartTime = UtilTool.getNextWeekStart(nowTime).getTimeInMillis();
		mWarTime = new WarTime(nextWeekStartTime, KGangWarConfig.getInstance(), true);
		// 进入休战期
		warStatusEnum = GangWarStatusEnum.REST_START_NOW;
		// 提交时效任务：休战期结束->进入报名期
		long delayTime = mWarTime.signUpStartTime - nowTime;
		GangWarTaskManager.GangWarStatusTask.submitTast(GangWarStatusEnum.SIGNUP_START_NOW, delayTime);

		/** --------------------- */
	}

	static boolean isCanJoinMap() {
		switch (warStatusEnum) {
		case SIGNUP_START_NOW:
		case WAR_WAIT_NOW:
			return false;
		case WAR_ROUND_READY_NOW:
		case WAR_ROUND_START_NOW:
			return true;
		case REST_START_NOW:
			return false;
		default:
			return false;
		}
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 此机制已作废，代码保留
	 * @return
	 * @author CamusHuang
	 * @creation 2014-6-4 上午11:14:45
	 * </pre>
	 */
	static boolean isShopUIIcon() {
		switch (warStatusEnum) {
		case SIGNUP_START_NOW:
			return false;
		case WAR_WAIT_NOW:
		case WAR_ROUND_READY_NOW:
		case WAR_ROUND_START_NOW:
			return true;
		case REST_START_NOW:
			return false;
		default:
			return false;
		}
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @return -1表示可进入，否则表示下场开启进入时间
	 * @author CamusHuang
	 * @creation 2013-9-18 下午9:14:03
	 * </pre>
	 */
	static long 下一场开启进入时间() {
		int nextRoundId = 0;
		switch (warStatusEnum) {
		case SIGNUP_START_NOW:
			nextRoundId = 1;
			break;
		case WAR_WAIT_NOW:
			nextRoundId = GangWarDataCenter.getNowRoundId();
			break;
		case WAR_ROUND_READY_NOW:
		case WAR_ROUND_START_NOW:
			return -1;
		case REST_START_NOW:
			nextRoundId = GangWarDataCenter.getNowRoundId();
			break;
		default:
			nextRoundId = GangWarDataCenter.getNowRoundId();
			break;
		}
		return mWarTime.getTime_Ready(nextRoundId);
	}

	static GangWarStatusEnum getNowStatus() {
		return warStatusEnum;
	}

	static WarTime getWarTime() {
		return mWarTime;
	}

	/**
	 * <pre>
	 * 离指定场次结束还有多长时间（秒）
	 * 
	 * @param roundId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-11-27 下午9:23:43
	 * </pre>
	 */
	static int getReleaseTimeToEnd(int roundId) {
		long endTime = GangWarStatusManager.getWarTime().getTime_End(roundId);
		long releaseTime = endTime - System.currentTimeMillis();
		releaseTime = Math.max(0, releaseTime) / Timer.ONE_SECOND;
		return (int) releaseTime;
	}

	/**
	 * <pre>
	 * 离指定场次开始还有多长时间（秒）
	 * 
	 * @param roundId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-11-27 下午9:23:43
	 * </pre>
	 */
	static int getReleaseTimeToStart(int roundId) {
		// 倒计时时效
		long startTime = GangWarStatusManager.getWarTime().getTime_Start(roundId);
		long releaseTime = startTime - System.currentTimeMillis();
		releaseTime = Math.max(0, releaseTime) / Timer.ONE_SECOND;
		return (int) releaseTime;
	}

	/**
	 * <pre>
	 * 管理本周军团战的时间节点
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-27 下午4:30:23
	 * </pre>
	 */
	static class WarTime {
		// =========================
		/** 活动报名时间为：周一0：00至周五21:00 */
		final long signUpStartTime;// 结束休息，开始报名
		final long signUpEndTime;// 结束报名
		/** 1~5场的时间 */
		final Map<Integer, RoundTime> roundTimeMap = new HashMap<Integer, RoundTime>();

		class RoundTime {
			/** 第N场开始和结束时间 */
			final long ReadyTime;
			final long StartTime;
			final long EndTime;

			private RoundTime(long readyTime, long startTime, long endTime) {
				ReadyTime = readyTime;
				StartTime = startTime;
				EndTime = endTime;
			}
		}

		WarTime(long thisWeekStartTime, KGangWarConfig config, boolean print) {
			//
			signUpStartTime = thisWeekStartTime + config.signUpStartTime;
			signUpEndTime = thisWeekStartTime + config.signUpEndTime;

			if(print){
				GangWarLogic.GangWarLogger.warn("军团战：报名开始时间={}", UtilTool.DATE_FORMAT.format(signUpStartTime));
				GangWarLogic.GangWarLogger.warn("军团战：报名结束时间={}", UtilTool.DATE_FORMAT.format(signUpEndTime));
			}
			//
			for (RoundTimeConfig timeConfig : config.roundTimeMap.values()) {
				long ReadyTime = thisWeekStartTime + timeConfig.ReadyTime;
				long StartTime = thisWeekStartTime + timeConfig.StartTime;
				long EndTime = thisWeekStartTime + timeConfig.EndTime;
				RoundTime time = new RoundTime(ReadyTime, StartTime, EndTime);
				roundTimeMap.put(timeConfig.Round, time);
				
				if(print){
					GangWarLogic.GangWarLogger.warn("军团战第{}场：准备时间={}，开战时间={}，结束时间={}", timeConfig.Round, UtilTool.DATE_FORMAT.format(ReadyTime), UtilTool.DATE_FORMAT.format(StartTime),
							UtilTool.DATE_FORMAT.format(EndTime));
				}
			}
		}

		long getTime_Ready(int round) {
			RoundTime time = roundTimeMap.get(round);
			return time.ReadyTime;
		}

		long getTime_Start(int round) {
			RoundTime time = roundTimeMap.get(round);
			return time.StartTime;
		}

		long getTime_End(int round) {
			RoundTime time = roundTimeMap.get(round);
			return time.EndTime;
		}

		// boolean 是否报名中(){
		// long nowTime = System.currentTimeMillis();
		// if(signStartTime<=nowTime && nowTime<=signEndTime){
		// return true;
		// }
		// return false;
		// }

//		/**
//		 * <pre>
//		 * 计算现在距第N场对战准备还有多久（毫秒）
//		 * 
//		 * @return 负数表示已过准备期
//		 * @author CamusHuang
//		 * @creation 2013-7-15 上午11:32:22
//		 * </pre>
//		 */
//		private long getDelayTimeForRoundReady(int round) {
//			RoundTime time = roundTimeMap.get(round);
//			long nowTime = System.currentTimeMillis();
//			return time.ReadyTime - nowTime;
//		}
	}
}
