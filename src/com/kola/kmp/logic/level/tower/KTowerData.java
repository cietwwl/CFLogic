package com.kola.kmp.logic.level.tower;

import java.util.HashMap;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.support.KSupportFactory;

public class KTowerData {
	// 怪物波数编号
	private int towerId;
	// 本波在本次战斗中序号（第几波）
	private int waveNum;
	// 本次战斗中下一波序号
	private int nextWaveNum;
	// 下一怪物波数编号
	private int nextTowerId;
	// 本波出现时间间隔，单位：秒（上一波结束到本波出现的间隔秒数）
	private int intervalSeconds;
	// 是否第一波
	private boolean isFirstWave;
	// 是否最后一波
	private boolean isLastWave;
	// 是否任务类型塔防
	private boolean isMission;

	// 左边怪物信息列表
	private Map<Integer, Integer> leftMonsterMap = new HashMap<Integer, Integer>();
	// 右边怪物信息列表
	private Map<Integer, Integer> rightMonsterMap = new HashMap<Integer, Integer>();

	public void init(String tableName, KGameExcelRow xlsRow)
			throws KGameServerException {
		this.towerId = xlsRow.getInt("towerId");
		this.waveNum = xlsRow.getInt("waveNum");
		this.nextTowerId = xlsRow.getInt("nextTowerId");
		this.intervalSeconds = xlsRow.getInt("interval");
		this.isMission = (xlsRow.getData("isMission").toUpperCase()).equals("TRUE");
		
		for (int i = 1; i <= 3; i++) {
			String monLeftTempKey = "mon1-"+i;
			String monLeftCountKey = "count1-"+i;
			String monRightTempKey = "mon2-"+i;
			String monRightCountKey = "count2-"+i;
			
			int leftMonTempId = xlsRow.getInt(monLeftTempKey);
			int rightMonTempId = xlsRow.getInt(monRightTempKey);
			int leftMonCount = xlsRow.getInt(monLeftCountKey);
			int rightMonCount = xlsRow.getInt(monRightCountKey);
			
			if(KSupportFactory.getNpcModuleSupport().getMonstTemplate(leftMonTempId) == null){
				throw new KGameServerException("读取表<"+ tableName
						+ ">的字段："+monLeftTempKey+"错误,找不到怪物模版=" + leftMonTempId + "！xls行数："
						+ xlsRow.getIndexInFile());
			}
			if(KSupportFactory.getNpcModuleSupport().getMonstTemplate(rightMonTempId) == null){
				throw new KGameServerException("读取表<"+ tableName
						+ ">的字段："+monRightTempKey+"错误,找不到怪物模版=" + rightMonTempId + "！xls行数："
						+ xlsRow.getIndexInFile());
			}
			leftMonsterMap.put(leftMonTempId, leftMonCount);
			rightMonsterMap.put(rightMonTempId, rightMonCount);
		}
		
		
	}

	public int getTowerId() {
		return towerId;
	}

	public int getWaveNum() {
		return waveNum;
	}

	public int getNextWaveNum() {
		return nextWaveNum;
	}

	public int getNextTowerId() {
		return nextTowerId;
	}

	public int getIntervalSeconds() {
		return intervalSeconds;
	}

	public boolean isFirstWave() {
		return isFirstWave;
	}

	public boolean isLastWave() {
		return isLastWave;
	}

	public Map<Integer, Integer> getLeftMonsterMap() {
		return leftMonsterMap;
	}

	public Map<Integer, Integer> getRightMonsterMap() {
		return rightMonsterMap;
	}

	public void setNextWaveNum(int nextWaveNum) {
		this.nextWaveNum = nextWaveNum;
	}

	public void setFirstWave(boolean isFirstWave) {
		this.isFirstWave = isFirstWave;
	}

	public void setLastWave(boolean isLastWave) {
		this.isLastWave = isLastWave;
	}

	public boolean isMission() {
		return isMission;
	}

	
}
