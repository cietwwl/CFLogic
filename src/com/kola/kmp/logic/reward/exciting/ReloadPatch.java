package com.kola.kmp.logic.reward.exciting;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.RunTimeTask;
import com.kola.kmp.logic.reward.exciting.ExcitingTaskManager.ExcitingActivityTaskDataManager;
import com.kola.kmp.logic.reward.exciting.ExcitingTaskManager.TimeLimitActivityTaskDataManager;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.ExcitingDataManager;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.ExcitingRuleManager;
import com.kola.kmp.logic.reward.exciting.message.KPushExcitingActivityDataMsg;

/**
 * <pre>
 * 由于加载新规则ID有BOG，暂时使用补丁进行新活动加载
 * 
 * @author CamusHuang
 * @creation 2014-10-23 下午12:30:00
 * </pre>
 */
public class ReloadPatch implements RunTimeTask {
	
	private static final Logger _LOGGER = KGameLogger.getLogger(ReloadPatch.class);

	public String run(String data) {

		// 精彩活动数据管理器
		ExcitingDataManager mExcitingDataManager = KExcitingDataManager.mExcitingDataManager;
		// 奖励规则数据管理器
		ExcitingRuleManager mExcitingRuleManager = KExcitingDataManager.mExcitingRuleManager;
		
		
		try {
			KExcitingDataManager.reloadData(false);
		} catch (Exception e) {
			_LOGGER.error(e.getMessage(), e);
			KExcitingDataManager.mExcitingDataManager = mExcitingDataManager;
			KExcitingDataManager.mExcitingRuleManager = mExcitingRuleManager;
			return "reloadData异常="+e.getMessage();
		}

		try {
			KExcitingDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			_LOGGER.error(e.getMessage(), e);
			KExcitingDataManager.mExcitingDataManager = mExcitingDataManager;
			KExcitingDataManager.mExcitingRuleManager = mExcitingRuleManager;
			return "notifyCacheLoadComplete异常="+e.getMessage();
		}

		try {
			// 启动所有精彩活动的起始和结束任务
			ExcitingActivityTaskDataManager.restartActivityTast();
			// 启动排行榜排名奖励自动发奖任务
			ExcitingTaskManager.restartAutoCollectedRankRewardTask();
			// 启动限时奖励活动起始和结束任务
			TimeLimitActivityTaskDataManager.restartAllActivityTast();
			
		} catch (Exception e) {
			_LOGGER.error(e.getMessage(), e);
			KExcitingDataManager.mExcitingDataManager = mExcitingDataManager;
			KExcitingDataManager.mExcitingRuleManager = mExcitingRuleManager;
			return "ExcitingTaskManager异常="+e.getMessage();
		}
			
		try {
			KPushExcitingActivityDataMsg.sendMsgToAllOnlineRole();

			return "执行成功";
		} catch (Exception e) {
			_LOGGER.error(e.getMessage(), e);
			return "sendMsgToAllOnlineRole异常="+e.getMessage();
		}
	}
}
