package com.kola.kmp.logic.level;

public class FightEvaluateData {
	//最小评价等级：D
	public final static byte MIN_FIGHT_LEVEL = 1; 
	//最大评价等级：S
	public final static byte MAX_FIGHT_LEVEL = 5; 
	
	/**
	 * 战斗评级：从1开始，最大5
	 */
	public byte fightLv;
	/**
	 * 战斗时间(单位：秒)
	 */
	public int fightTime;
	/**
	 * 杀怪连击数
	 */
	public int maxHitCount;
	/**
	 * 受击数量
	 */
	public int hitByCount;
}
