package com.kola.kmp.logic.competition;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kgame.cache.util.UtilTool;

/**
 * 
 * @author PERRY CHAN
 */
public class KCompetitionModuleConfig {

	private static int _joinedCompetitionLv; // 参加竞技场的最低等级
	private static String _competitionBattlePath; // 竞技场地图资源文件名
	private static int _maxChallengeTimePerDay; // 每天可以挑战的次数
	private static int _competitionSettleWeekday; // 竞技场结算事件日子（每周的周几，0表示周日）
	private static String _competitionSettleTimeStr; // 竞技场结算事件的时刻（当天时刻，0表示0点）
	private static int _competitionSettleTime;
	private static int _addChallengeTimeMoney; // 增加一次挑战次数所需要的元宝
	private static int _competitionAudioResId; // 竞技场背景音乐

	@SuppressWarnings("unchecked")
	static void init(String path) throws Exception {
		Document doc = XmlUtil.openXml(path);
		Element root = doc.getRootElement();
		ReflectPaser.parse(KCompetitionModuleConfig.class, doc.getRootElement()
				.getChildren("config"));
		String[] settleTime = _competitionSettleTimeStr.split(":");
		_competitionSettleTime = (int) ((TimeUnit.SECONDS.convert(
				Integer.parseInt(settleTime[0]), TimeUnit.HOURS) + TimeUnit.SECONDS
				.convert(Integer.parseInt(settleTime[1]), TimeUnit.MINUTES)));
	}

	public static int getJoinedCompetitionLv() {
		return _joinedCompetitionLv;
	}

	public static String getCompetitionBattlePath() {
		return _competitionBattlePath;
	}

	public static int getMaxChallengeTimePerDay() {
		return _maxChallengeTimePerDay;
	}

//	public static long getCompetitionSettleTime() {
//		Calendar c = Calendar.getInstance();
//		c.set(Calendar.HOUR_OF_DAY, 0);
//		c.set(Calendar.MINUTE, 0);
//		c.set(Calendar.SECOND, 0);
//		c.add(Calendar.SECOND, _competitionSettleTime);
//		return TimeUnit.SECONDS.convert(c.getTimeInMillis(),
//				TimeUnit.MILLISECONDS);
//	}

	public static String getCompetitionSettleTimeStr() {
		return _competitionSettleTimeStr;
	}

	public static int getAddChallengeTimeMoney() {
		return _addChallengeTimeMoney;
	}

	public static int getCompetitionAudioResId() {
		return _competitionAudioResId;
	}

	public static int getCompetitionSettleWeekday() {
		return _competitionSettleWeekday;
	}

	public static int getCompetitionSettleTime() {
		return _competitionSettleTime;
	}
	
	public static long getCompetitionTodayRewardSettleTime() {
		try {
			return UtilTool.parseHHmmToMillis(_competitionSettleTimeStr);
		} catch (ParseException e) {			
			e.printStackTrace();
		}
		return 0;
	}
}
