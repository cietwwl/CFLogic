package com.kola.kmp.logic.util.tips;

import com.koala.game.util.StringUtil;

/**
 * 
 * @author PERRY CHAN
 */
public class WorldBossTips {

	private static String _tipsKillRewardMailTitle = ""; // 击杀奖励标题
	private static String _tipsKillRewardMailContent = ""; // 击杀奖励邮件内容
	private static String _tipsRankRewardMailTitle = ""; // 排名奖励邮件标题
	private static String _tipsRankRewardMailContent = ""; // 排名奖励邮件内容
	private static String _tipsDmRewardMailTitle = ""; // 伤害奖励邮件标题
	private static String _tipsDmRewardMailContent = ""; // 伤害奖励邮件内容
	private static String _tipsDmRewardMailContentWithInspire = ""; // 使用了激励功能的伤害奖励邮件内容
	private static String _tipsCombatResult = ""; // 战斗结果
	private static String _tipsCombatResultActivityEnd = ""; // 活动已经结束的战斗后结果提示
	private static String _tipsWorldBossFieldFinish = ""; // 某场活动完结
	private static String _tipsWorldBossActivityFinish = ""; // 世界boss活动完结
	private static String _tipsMonsterNotExists = ""; // 怪物不存在
	private static String _tipsYouAreWaitingToRelive = ""; // 等待复活
	private static String _tipsMonsterIsDead = ""; // 怪物已经死亡
	private static String _tipsWorldBossNotStart = ""; // 世界boss活动未开始
	private static String _tipsLevelNotReach = ""; // 等级未达到参加要求
	private static String _tipsYouAreNotInActivity = ""; // 没有参加活动
	private static String _tipsYourInpireLvIsMax = ""; // 激励等级达到最大等级
	private static String _tipsInspireSuccess = ""; // 鼓舞成功
	private static String _tipsWorldBossWarnUp = ""; // 世界boss预热
	private static String _tipsWorldBossStart = ""; // 世界boss正式开始
	private static String _tipsWorldBossStartPromptUp = ""; // 世界boss开始的弹框消息
	private static String _tipsBroadcastKillMonster = ""; // 击杀广播
	private static String _tipsInspireDescFormat = ""; // 激励等级格式
	private static String _tipsReliveSuccess = "";
	private static String _tipsActivityFinished = "";
	private static String _tipsActivityFinishedWithBossKilled = "";
	private static String _tipsAutoJoinNotOpen = "";
	private static String _tipsAutoJoinTurnOnSuccess = "";
	private static String _tipsAutoJoinTurnOffSuccess = "";
	private static String _tipsAutoJoinIntroduceTitle = "";
	private static String _tipsAutoJoinIntroduceContent = "";
	private static String _tipsAutoJoinMailTitle = "";
	private static String _tipsAutoJoinMailContent = "";
	private static String _tipsAutoJoinFailMailTitle;
	private static String _tipsAutoJoinFailMailContent;
	
	public static String getTipsRankRewardMailTitle() {
		return _tipsRankRewardMailTitle;
	}

	public static String getTipsRankRewardMailContent(long totalDm, int rank) {
		return StringUtil.format(_tipsRankRewardMailContent, totalDm, rank);
	}
	
	public static String getTipsDmRewardMailTitle() {
		return _tipsDmRewardMailTitle;
	}

	public static String getTipsDmRewardMailContent(long dm, int inspireLv, int pct) {
		if (inspireLv > 0) {
			int hPct = pct / 100;
			return StringUtil.format(_tipsDmRewardMailContentWithInspire, dm, inspireLv, hPct);
		} else {
			return StringUtil.format(_tipsDmRewardMailContent, dm);
		}
	}

	public static String getTipsCombatResult(int dm) {
		return StringUtil.format(_tipsCombatResult, dm);
	}

	public static String getTipsWorldBossFieldFinish() {
		return _tipsWorldBossFieldFinish;
	}

	public static String getTipsMonsterNotExists() {
		return _tipsMonsterNotExists;
	}

	public static String getTipsYouAreWaitingToRelive() {
		return _tipsYouAreWaitingToRelive;
	}

	public static String getTipsMonsterIsDead() {
		return _tipsMonsterIsDead;
	}

	public static String getTipsWorldBossNotStart() {
		return _tipsWorldBossNotStart;
	}

	public static String getTipsLevelNotReach() {
		return _tipsLevelNotReach;
	}

	public static String getTipsWorldBossActivityFinish() {
		return _tipsWorldBossActivityFinish;
	}

	public static String getTipsYouAreNotInActivity() {
		return _tipsYouAreNotInActivity;
	}

	public static String getTipsKillRewardMailTitle() {
		return _tipsKillRewardMailTitle;
	}

	public static String getTipsKillRewardMailContent() {
		return _tipsKillRewardMailContent;
	}

	public static String getTipsCombatResultActivityEnd() {
		return _tipsCombatResultActivityEnd;
	}

	public static String getTipsYourInpireLvIsMax() {
		return _tipsYourInpireLvIsMax;
	}

	public static String getTipsInspireSuccess() {
		return _tipsInspireSuccess;
	}

	public static String getTipsWorldBossWarnUp(int minute) {
		return StringUtil.format(_tipsWorldBossWarnUp, minute);
	}

	public static String getTipsWorldBossStart() {
		return _tipsWorldBossStart;
	}

	public static String getTipsBroadcastKillMonster(String roleName, String monsterName) {
		return StringUtil.format(_tipsBroadcastKillMonster, roleName, monsterName);
	}

	public static String getTipsInspireDescFormat(String attrName, String data) {
		return StringUtil.format(_tipsInspireDescFormat, attrName, data);
	}

	public static String getTipsReliveSuccess() {
		return _tipsReliveSuccess;
	}

	public static String getTipsActivityFinished(int rank) {
		return StringUtil.format(_tipsActivityFinished, rank);
	}

	public static String getTipsActivityFinishedWithBossKilled(String killer, int rank) {
		return StringUtil.format(_tipsActivityFinishedWithBossKilled, killer, rank);
	}

	public static String getTipsWorldBossStartPromptUp() {
		return _tipsWorldBossStartPromptUp;
	}

	public static String getTipsAutoJoinNotOpen(int vipLv) {
		return StringUtil.format(_tipsAutoJoinNotOpen, vipLv);
	}

	public static String getTipsAutoJoinTurnOnSuccess() {
		return _tipsAutoJoinTurnOnSuccess;
	}

	public static String getTipsAutoJoinTurnOffSuccess() {
		return _tipsAutoJoinTurnOffSuccess;
	}

	public static String getTipsAutoJoinIntroduceTitle() {
		return _tipsAutoJoinIntroduceTitle;
	}

	public static String getTipsAutoJoinIntroduceContent() {
		return _tipsAutoJoinIntroduceContent;
	}

	public static String getTipsAutoJoinMailTitle() {
		return _tipsAutoJoinMailTitle;
	}

	public static String getTipsAutoJoinMailContent(String dateTimeStr, int price) {
		return StringUtil.format(_tipsAutoJoinMailContent, dateTimeStr, price);
	}

	public static String getTipsAutoJoinFailMailTitle() {
		return _tipsAutoJoinFailMailTitle;
	}
	
	public static String getTipsAutoJoinFailMailContent(String dateTimeStr) {
		return StringUtil.format(_tipsAutoJoinFailMailContent, dateTimeStr);
	}
}
