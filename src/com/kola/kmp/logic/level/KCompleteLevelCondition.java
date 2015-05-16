package com.kola.kmp.logic.level;

public class KCompleteLevelCondition {
	/**
	 * 是否限制时间的关卡
	 */
	public boolean isTimeLimit;
	/**
	 * 限制的时间，单位：（秒）
	 */
	public int limitTimeSeconds;

	/**
	 * 是否以杀怪数量为完成关卡条件
	 */
	public boolean isLimitKillCount;

	/**
	 * 击杀怪物数量
	 */
	public int limitKillCount;

	/**
	 * 是否以击杀boss为完成条件
	 */
	public boolean isKillBoss;
	/**
	 * 需要击杀的boss模版ID
	 */
	public int bossTemplateId;

}
