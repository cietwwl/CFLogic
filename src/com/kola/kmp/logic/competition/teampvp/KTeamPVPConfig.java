package com.kola.kmp.logic.competition.teampvp;

import java.util.concurrent.TimeUnit;

import org.jdom.Element;

import com.koala.game.util.KGameExcelFile;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kmp.logic.other.KTableInfo;

/**
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPConfig {

	private static int _priceForCreateTeam;
	private static int _challengeCountPerDay;
	private static int _joinMinLevel;
	private static int _maxTeamMemberCount;
	private static float _teamLvGreaterPara;
	private static float _teamLvLessPara;
	private static int _loseCountForDemote; // 连续失败几场后降级
	private static int _teamNameLengthMin;
	private static int _teamNameLengthMax;
	private static int _firstDanRankId;
	private static String _combatEnvFileName;
	private static int _combatEnvMusicId;
	private static int _timeoutMillis;
	private static int _maxKingCount; // 最强王者的数量
	private static int _maxHistorySize; // 最大的历史记录数量
	private static int _maxOfflineDays; // 成员允许最大的不在线时长
	private static long _maxOfflineMillis; // 成员允许最大的不在线时长
	private static int _maxOfflineHours; // 成员允许最大的不在线时长（小时）
	private static int _broadcastCtnWinTimes; // 播放世界播报的连胜数量
	private static int _maxGetHonorRewardTimes;
	private static KTeamPVPBattlefield _env;
	
	static void init(Element element, KTableInfo tableInfo) throws Exception {
		KGameExcelFile file = new KGameExcelFile(element.getTextTrim());
		ReflectPaser.parseExcel(KTeamPVPConfig.class, file.getTable(tableInfo.tableName, tableInfo.headerIndex).getAllDataRows());
		_env = new KTeamPVPBattlefield();
		_env.initBattlefield(_combatEnvFileName, _combatEnvMusicId);
		_maxOfflineMillis = TimeUnit.MILLISECONDS.convert(_maxOfflineDays, TimeUnit.DAYS);
		_maxOfflineHours = (int)TimeUnit.HOURS.convert(_maxOfflineMillis, TimeUnit.MILLISECONDS);
	}
	
	static void setFirstDanRankId(int pFirstDanRankId) {
		_firstDanRankId = pFirstDanRankId;
	}
	
	static void setMinJoinLevel(int pLevel) {
		_joinMinLevel = pLevel;
	}

	public static int getPriceForCreateTeam() {
		return _priceForCreateTeam;
	}

	public static int getChallengeCountPerDay() {
		return _challengeCountPerDay;
	}

	public static int getJoinMinLevel() {
		return _joinMinLevel;
	}

	public static int getMaxTeamMemberCount() {
		return _maxTeamMemberCount;
	}

	public static float getTeamLvGreaterPara() {
		return _teamLvGreaterPara;
	}

	public static float getTeamLvLessPara() {
		return _teamLvLessPara;
	}

	public static int getFirstDanRankId() {
		return _firstDanRankId;
	}

	public static int getLoseCountForDemote() {
		return _loseCountForDemote;
	}

	public static int getTeamNameLengthMin() {
		return _teamNameLengthMin;
	}

	public static int getTeamNameLengthMax() {
		return _teamNameLengthMax;
	}

	public static KTeamPVPBattlefield getCombatEnv() {
		return _env;
	}

	public static int getTimeoutMillis() {
		return _timeoutMillis;
	}

	public static int getMaxKingCount() {
		return _maxKingCount;
	}

	public static int getMaxHistorySize() {
		return _maxHistorySize;
	}

	public static long getMaxOfflineMillis() {
		return _maxOfflineMillis;
	}

	public static int getMaxOfflineDays() {
		return _maxOfflineDays;
	}
	
	public static int getMaxOfflineHours() {
		return _maxOfflineHours;
	}

	public static int getBroadcastCtnWinTimes() {
		return _broadcastCtnWinTimes;
	}

	public static int getMaxGetHonorRewardTimes() {
		return _maxGetHonorRewardTimes;
	}
}
