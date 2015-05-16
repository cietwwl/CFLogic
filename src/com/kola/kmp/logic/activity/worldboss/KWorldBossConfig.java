package com.kola.kmp.logic.activity.worldboss;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kmp.logic.activity.worldboss.KWorldBossManager.KWorldBossTableInfo;
import com.kola.kmp.logic.other.KTableInfo;
import com.kola.kmp.logic.util.KGameUtilTool;

/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossConfig {

	private static int _waitingForAliveTime; // 等待复活时间（每次挑战失败后需要等待的时间）
	private static int _durationSeconds; // 持续的时间
	private static long _minActivityTime; // 活动最少的时间（这个是防止服务器启动期间正好是活动时间，需要界定一个时间值，看还举不举行这次活动）
	private static int _sendRakingCount; // 发送排行榜条目的数量
	private static int _warnUpDurationSeconds; // 预热的时长
	private static int _relivePrice;
	private static int _lastRankingSendCount; // 上一届伤害排名发送的条目数量
	private static int[] _broadcastMinutes; // 广播的分钟
	private static int[] _broadcastHpPct; // 广播的百分比
	private static int _expRewardParaAdd; // 公式 _expRewardParaAdd + Math.min(_expRewardParaMultiple, 伤害值/伤害基数) * 人物等级
	private static float _expRewardParaCmp; // 公式 _expRewardParaAdd + Math.min(_expRewardParaMultiple, 伤害值/伤害基数) * 人物等级
	private static int _moneyRewardParaAdd; // 公式 _moneyRewardParaAdd + Math.min(_moneyRewardParaMultiple, 伤害值/伤害基数) * 人物等级
	private static float _moneyRewardParaCmp; // 公式 _moneyRewardParaAdd + Math.min(_moneyRewardParaMultiple, 伤害值/伤害基数) * 人物等级
	private static int _firstStartLvCalRanking; // 第一次开始世界boss时，取的排行榜前多少名来计算平均等级
	private static String _activityName; // 活动名字
	private static int _maxBossLv;
	private static int _defaultBossStartLv;
	private static int _worldBossMinJoinLv;
	private static int _worldBossAutoJoinVipLv;
	private static int _autoJoinPrice;
	private static int _autoJoinMaxCountPerTime;
	
	public static void init(String path, Map<Byte, KTableInfo> tableMap) throws Exception {
		KGameExcelFile file = new KGameExcelFile(path);
		KGameExcelRow[] allRows = KGameUtilTool.getAllDataRows(file, tableMap, KWorldBossTableInfo.TABLE_CONFIG);
		ReflectPaser.parseExcel(KWorldBossConfig.class, allRows);
		_waitingForAliveTime = (int) TimeUnit.MILLISECONDS.convert(_waitingForAliveTime, TimeUnit.SECONDS);
		_minActivityTime = TimeUnit.MILLISECONDS.convert(_minActivityTime, TimeUnit.MINUTES);
	}
	
	static void setActivityName(String name) {
		_activityName = name;
	}
	
	static void setMaxBossLv(int pLv) {
		_maxBossLv = pLv;
	}
	
	static void setDefaultBossStartLv(int pDefaultBossStartLv) {
		KWorldBossConfig._defaultBossStartLv = pDefaultBossStartLv;
	}
	
	static void setWorldBossMinJoinLv(int minLv) {
		_worldBossMinJoinLv = minLv;
	}

	static void setWolrdBossAutoJoinVipLv(int vipLv) {
		_worldBossAutoJoinVipLv = vipLv;
	}
	
	public static int getWaitingForAliveTime() {
		return _waitingForAliveTime;
	}

	public static int getDurationSecond() {
		return _durationSeconds;
	}

	public static long getMinActivityTime() {
		return _minActivityTime;
	}

	public static int getSendRakingCount() {
		return _sendRakingCount;
	}

	public static int getWarnUpDurationSeconds() {
		return _warnUpDurationSeconds;
	}

	public static int getRelivePrice() {
		return _relivePrice;
	}

	public static int getLastRankingSendCount() {
		return _lastRankingSendCount;
	}

	public static boolean containBroadcastMinutes(int minute) {
		for (int i = 0; i < _broadcastMinutes.length; i++) {
			if (_broadcastMinutes[i] == minute) {
				return true;
			}
		}
		return false;
	}

	public static int[] getBroadcastHpPct() {
		return _broadcastHpPct;
	}

	public static int getExpRewardParaAdd() {
		return _expRewardParaAdd;
	}

	public static float getExpRewardParaMultiple() {
		return _expRewardParaCmp;
	}

	public static int getMoneyRewardParaAdd() {
		return _moneyRewardParaAdd;
	}

	public static float getMoneyRewardParaMultiple() {
		return _moneyRewardParaCmp;
	}

	public static int getFirstStartLvCalRanking() {
		return _firstStartLvCalRanking;
	}
	
	public static String getActivityName() {
		return _activityName;
	}

	public static int getMaxBossLv() {
		return _maxBossLv;
	}

	public static int getDefaultBossStartLv() {
		return _defaultBossStartLv;
	}

	public static int getWorldBossMinJoinLv() {
		return _worldBossMinJoinLv;
	}

	public static int getWorldBossAutoJoinVipLv() {
		return _worldBossAutoJoinVipLv;
	}

	public static int getAutoJoinPrice() {
		return _autoJoinPrice;
	}

	public static int getAutoJoinMaxCountPerTime() {
		return _autoJoinMaxCountPerTime;
	}
	
}
