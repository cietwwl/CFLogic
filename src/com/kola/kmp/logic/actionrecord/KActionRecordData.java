package com.kola.kmp.logic.actionrecord;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * @author PERRY CHAN
 */
public class KActionRecordData {

	private String _roleName;
	private int _roleLv;
	private final Map<KActionType, Integer> _actionRecord = new HashMap<KActionType, Integer>();
	public KActionRecordData(String pRoleName) {
		this._roleName = pRoleName;
		for (int i = 0; i < KActionType.values().length; i++) {
			_actionRecord.put(KActionType.values()[i], 0);
		}
	}
	
	void updateRoleLv(int roleLv) {
		this._roleLv = roleLv;
	}
	
	public String getRoleName() {
		return _roleName;
	}
	
	public int getRoleLv() {
		return this._roleLv;
	}
	
	public Integer getActionCount(KActionType type) {
		return _actionRecord.get(type);
	}
	
	public void recordAction(KActionType type, int count) {
		Integer original = this._actionRecord.get(type);
		this._actionRecord.put(type, original + count);
	}
	
	public void reset() {
		for (Iterator<Map.Entry<KActionType, Integer>> itr = this._actionRecord.entrySet().iterator(); itr.hasNext();) {
			itr.next().setValue(0);
		}
	}
}
