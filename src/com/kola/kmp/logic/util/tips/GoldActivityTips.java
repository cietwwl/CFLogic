package com.kola.kmp.logic.util.tips;

import com.koala.game.util.StringUtil;

public class GoldActivityTips {
	private static String _tipsMaxChallengeCount;// 已达到当天最大挑战次数

	public static String getTipsMaxChallengeCount(int maxCount) {
		return StringUtil.format(_tipsMaxChallengeCount, maxCount);
	}
	
	
}
