package com.kola.kmp.logic.level;

import java.util.ArrayList;
import java.util.List;

import com.kola.kmp.logic.level.KBattleObjectDataStruct.ExitData;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.MonsterData;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.ObstructionData;

/**
 * 表示某个战斗场景中某一段（某一波）的所有怪物、npc、障碍物、出入口、出生点的数据信息
 * @author zhaizl
 *
 */
public class BattlefieldWaveViewInfo implements Comparable<BattlefieldWaveViewInfo>{
	
	private int waveId;
	// 战斗场景编号
	private int _battlefieldId;
	// 本段的所有怪物
	public List<MonsterData> _allMonsters = new ArrayList<KBattleObjectDataStruct.MonsterData>();
	// 本段的所有障碍物
	public List<ObstructionData> _allObstructions = new ArrayList<KBattleObjectDataStruct.ObstructionData>();
	
	// 本段是否有出入口
	public boolean _isHasExit;
	// 本段出入口数据
	public ExitData _exitData; 
	
	

	public BattlefieldWaveViewInfo(int waveId,int battlefieldId){
		this.waveId = waveId;
		this._battlefieldId = battlefieldId;
		this._isHasExit = false;
	}
	
	public int getWaveId() {
		return waveId;
	}

	public void setWaveId(int waveId) {
		this.waveId = waveId;
	}

	public int getBattlefieldId() {
		return _battlefieldId;
	}
	public void setBattlefieldId(int battlefieldId) {
		this._battlefieldId = battlefieldId;
	}
	public List<MonsterData> getAllMonsters() {
		return _allMonsters;
	}
	public List<ObstructionData> getAllObstructions() {
		return _allObstructions;
	}
	public boolean isHasExit() {
		return _isHasExit;
	}
	public void setHasExit(boolean isHasExit) {
		this._isHasExit = isHasExit;
	}
	public ExitData getExitData() {
		return _exitData;
	}
	public void setExitData(ExitData exitData) {
		this._exitData = exitData;
	}

	@Override
	public int compareTo(BattlefieldWaveViewInfo o) {
		if (waveId > o.waveId)
			return 1;
		else if (waveId < o.waveId)
			return -1;
		return 0;
	}
	
	public void resetAllMonsterData(List<MonsterData> monDataList){
		_allMonsters.clear();
		_allMonsters.addAll(monDataList);
	}
	

}
