package com.kola.kmp.logic.competition.teampvp;

import java.util.List;


/**
 * 
 * @author PERRY CHAN
 */
public enum KTeamPVPDanType {

	青铜(10001),
	白银(10002),
	黄金(10003),
	白金(10004),
	钻石(10005),
	最强王者(10006),
	;
	private int _flag;
	
	private KTeamPVPDanType(int pFlag) {
		this._flag = pFlag;
	}
	
	public int getFlag() {
		return _flag;
	}
	
	static void init(List<Integer> danIds) {
		for (int i = 0; i < danIds.size(); i++) {
			values()[i]._flag = danIds.get(i);
		}
	}
	
	public static KTeamPVPDanType getDanType(int pFlag) {
		KTeamPVPDanType dan;
		for(int i = 0; i < values().length; i++) {
			dan = values()[i];
			if(dan._flag == pFlag) {
				return dan;
			}
		}
		return null;
	}
}
