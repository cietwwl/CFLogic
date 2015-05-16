package com.kola.kmp.logic.util.tips;

import java.util.Date;

import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.UtilTool;

/**
 * 全民竞猜tips
 * 
 * @author Alex
 * @create 2015年3月4日 下午6:37:48
 */
public class PeopleGuessTips {

	public static String tipsNotRacingTime = "";

	public static String mailTitle = "";

	public static String rewardMailContent = "";

	public static String rewardMailContent1;

	public static String noRewardMailContent = "";

	public static String notYabaoTime = "";

	public static String tipsYabaoFieldByNotFoundHorse = "";

	public static String tipsYabaoFieldByIgotError = "";

	public static String tipsNotEnoughMoney = "";

	public static String tipsYabaoSuccess = "";

	public static String tipsPeopleGuessSettlePriceTime = "";

	public static String getMailTitle() {
		String time = UtilTool.DATE_FORMAT11.format(new Date());
		return time + mailTitle;
	}

	public static String getRewardMailContent(int horseNum, int[] vote,int reward) {
		return StringUtil.format(rewardMailContent, horseNum, vote[0], vote[1],vote[2],vote[3],vote[4], reward);
	}
	public static String getNoRewardMailContent(int horseNum, int[] vote) {
		return StringUtil.format(noRewardMailContent, horseNum, vote[0], vote[1],vote[2],vote[3],vote[4]);
	}

	public static String getTipsNotYabaoTime() {
		return notYabaoTime;
	}

	public static String getTipsNotFoundHorse(int horseId) {
		return StringUtil.format(tipsYabaoFieldByNotFoundHorse, horseId);
	}

	public static String getTipsYabaoFieldByIgotError() {
		return tipsYabaoFieldByIgotError;
	}

	public static String getTipsNotEnoughMoney() {
		return tipsNotEnoughMoney;
	}

	public static String getTipsYabaoSuccess(int horseId, String horseName, int count) {
		return StringUtil.format(tipsYabaoSuccess, horseId, horseName, count);
	}

	public static String getTipsSettlePriceTime() {
		return tipsPeopleGuessSettlePriceTime;
	}

	public static String getTipsNotRacingTime() {
		return tipsNotRacingTime;
	}

}
