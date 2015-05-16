package com.kola.kmp.logic.util.tips;

import com.koala.game.util.StringUtil;

public class TimeLimitProducActivityTips {
	public static String tipsCopyActivityRewardMailTitle;
	public static String tipsCopyActivityRewardMailContent;
	
	public static String getTipsCopyActivityRewardMailTitle(String activityName) {
		return StringUtil.format(tipsCopyActivityRewardMailTitle, activityName);
	}

	public static String getTipsCopyActivityRewardMailContent(String activityName) {
		return StringUtil.format(tipsCopyActivityRewardMailContent, activityName);
	}
}
