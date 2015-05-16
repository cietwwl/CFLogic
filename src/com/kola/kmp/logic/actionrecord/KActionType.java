package com.kola.kmp.logic.actionrecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * 行为类型
 * 
 * @author PERRY CHAN
 */
public enum KActionType {

	
	ACTION_TYPE_WORLDBOSS(1, "世界boss"),
	ACTION_TYPE_SENIOR_FB(2, "精英副本"),
	ACTION_TYPE_BOSS_FB(3, "BOSS副本"),
	ACTION_TYPE_NORMAL_FB(4, "普通副本"),
	ACTION_TYPE_TRANSPORT(5, "物资运输"),
	ACTION_TYPE_INTERCEPT(6, "物资拦截"),
	ACTION_TYPE_COMPETITION(7, "竞技场"),
	ACTION_TYPE_DAILY_MISSION(8, "日常任务"),
	ACTION_TYPE_WISH(9, "许愿"),
	ACTION_TYPE_TOWER_COPY(10, "爬塔副本"),
	;
	public static final List<String> ALL_ACTION_NAME;
	static {
		List<String> temp = new ArrayList<String>();
		ALL_ACTION_NAME = Collections.unmodifiableList(temp);
		for (int i = 0; i < values().length; i++) {
			temp.add(values()[i].name);
		}
	}
	
	public final int sign;
	public final String name;
	
	private KActionType(int pSign, String pName) {
		this.sign = pSign;
		this.name = pName;
	}
}
