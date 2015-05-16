package com.kola.kmp.logic.role;

import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kmp.logic.other.KTableInfo;
import com.kola.kmp.logic.util.KBattlePowerCalculator;

/**
 * 
 * @author PERRY CHAN
 */
public class KRoleModuleConfig {

	private static int _roleNameLengthMin; // 名字最短的长度
	private static int _roleNameLengthMax; // 名字最长的长度
	private static int _maxRoleCountOfPlayer; // 最大的角色数量
//	private static boolean _openPlayStory; // 是否开启开场动画
	private static int _maxPhyPower; // 体力值上限
	private static int _recoverPhyPowerTimeItr; // 恢复体力的时间间隔
	private static int _phyPowerRecoverItr; // 恢复体力的增量
	private static int _roleMaxLv; // 角色的等级上限
	private static int _maxEnergyBean; // 怒气豆上限
	private static boolean _joinGameAfterCreate; // 创建角色后，是否马上登陆游戏
	private static int _maxRoleShowCountOfPlayer;
	
	private static long _lastIncreasePhyPowerTime; // 上一次服务器恢复体力的时间
	
	public static void init(String path, KTableInfo tableInfo) throws Exception {
		KGameExcelFile file = new KGameExcelFile(path);
		ReflectPaser.parseExcel(KRoleModuleConfig.class, file.getTable(tableInfo.tableName, tableInfo.headerIndex).getAllDataRows());
	}
	
	public static void loadBattlePowerPara(KGameExcelTable table) {
		KGameExcelRow[] allRows = table.getAllDataRows();
		KBattlePowerCalculator.initRoleCalculatePara(allRows);
	}
	
	public static int getRoleNameLengthMin() {
		return _roleNameLengthMin;
	}
	
	public static int getRoleNameLengthMax() {
		return _roleNameLengthMax;
	}

	public static int getMaxRoleCountOfPlayer() {
		return _maxRoleCountOfPlayer;
	}

//	public static boolean isOpenPlayStory() {
//		return _openPlayStory;
//	}
	
	public static int getMaxPhyPower() {
		return _maxPhyPower;
	}
	
	public static int getRecoverPhyPowerTimeItr() {
		return _recoverPhyPowerTimeItr;
	}
	
	public static int getPhyPowerRecoverItr() {
		return _phyPowerRecoverItr;
	}
	
	public static int getRoleMaxLv() {
		return _roleMaxLv;
	}
	
	public static long getLastIncreasePhyPowerTime() {
		return _lastIncreasePhyPowerTime;
	}
	
	static void setLastIncreasePhyPowerTime(long time) {
		_lastIncreasePhyPowerTime = time;
	}

	public static int getMaxEnergyBean() {
		return _maxEnergyBean;
	}
	
	public static boolean isJoinGameAfterCreate() {
		return _joinGameAfterCreate;
	}

	public static int getMaxRoleShowCountOfPlayer() {
		return _maxRoleShowCountOfPlayer;
	}
	
	
}
