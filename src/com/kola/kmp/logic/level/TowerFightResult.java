package com.kola.kmp.logic.level;

import java.util.Map;

import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;

public class TowerFightResult {
	/**
	 * 表示战斗正常结束类型
	 */
	public static final byte FIGHT_END_TYPE_NORMAL = 0;

	/**
	 * 表示战斗逃跑结束类型
	 */
	public static final byte FIGHT_END_TYPE_ESCAPE = 1;

	// 战斗是否胜利
	private boolean isWin;
	// 战场ID
	private int battlefieldId;
	// 战场类型
	private KGameBattlefieldTypeEnum battlefieldType;
	// 战斗结束类型：FIGHT_END_TYPE_NORMAL=1，正常结束；FIGHT_END_TYPE_ESCAPE=2，逃跑结束
	private byte endType;

	// 战斗时间
	private long battleTime;
	// 总计伤害
	private int totalDamage;
	// 角色最大连击数
	private int maxDoubleHitCount;
	// 角色最大受击数
	private int maxBeHitCount;
    // 本次战斗总共完成波数
	private int finishWave;
	// 塔防波数编号
	private int lastTowerId;
	// 参战的好友角色ID，-1为没有好友参战
	private long friendId;
	// 好友角色最大连击数
	private int maxFriendDoubleHitCount;
	// 好友角色最大受击数
	private int maxFriendBeHitCount;
	// 好友角色总伤害
	private int friendTotalDamage;

	/**
	 * 战斗是否胜利
	 * 
	 * @return
	 */
	public boolean isWin() {
		return isWin;
	}

	/**
	 * 设置战斗是否胜利
	 * 
	 * @param isWin
	 */
	public void setWin(boolean isWin) {
		this.isWin = isWin;
	}

	/**
	 * 获取战场ID
	 * 
	 * @return
	 */
	public int getBattlefieldId() {
		return battlefieldId;
	}

	/**
	 * 设置战场ID
	 * 
	 * @param battlefieldId
	 */
	public void setBattlefieldId(int battlefieldId) {
		this.battlefieldId = battlefieldId;
	}

	public KGameBattlefieldTypeEnum getBattlefieldType() {
		return battlefieldType;
	}

	public void setBattlefieldType(KGameBattlefieldTypeEnum battlefieldType) {
		this.battlefieldType = battlefieldType;
	}

	/**
	 * 获取战斗时间
	 * 
	 * @return
	 */
	public long getBattleTime() {
		return battleTime;
	}

	/**
	 * 设置战斗时间
	 * 
	 * @return
	 */
	public void setBattleTime(long battleTime) {
		this.battleTime = battleTime;
	}

	/**
	 * 获得战斗总伤害
	 * 
	 * @return
	 */
	public int getTotalDamage() {
		return totalDamage;
	}

	/**
	 * 设置战斗总伤害
	 * 
	 * @return
	 */
	public void setTotalDamage(int totalDamage) {
		this.totalDamage = totalDamage;
	}

	/**
	 * 获取角色最大连击数
	 * 
	 * @return
	 */
	public int getMaxDoubleHitCount() {
		return maxDoubleHitCount;
	}

	/**
	 * 设置角色最大连击数
	 * 
	 * @return
	 */
	public void setMaxDoubleHitCount(int maxDoubleHitCount) {
		this.maxDoubleHitCount = maxDoubleHitCount;
	}

	/**
	 * 获取角色最大受击数
	 * 
	 * @return
	 */
	public int getMaxBeHitCount() {
		return maxBeHitCount;
	}

	/**
	 * 设置角色最大受击数
	 * 
	 * @return
	 */
	public void setMaxBeHitCount(int maxBeHitCount) {
		this.maxBeHitCount = maxBeHitCount;
	}
	
	
	/**
	 * 获取好友角色总伤害
	 * @return
	 */
	public int getFriendTotalDamage() {
		return friendTotalDamage;
	}
	
	/**
	 * 设置好友角色总伤害
	 * @return
	 */
	public void setFriendTotalDamage(int friendTotalDamage) {
		this.friendTotalDamage = friendTotalDamage;
	}

	/**
	 * 本次战斗总共完成波数
	 * @return
	 */
	public int getFinishWave() {
		return finishWave;
	}

	/**
	 * 设置本次战斗总共完成波数
	 * @param finishWave
	 */
	public void setFinishWave(int finishWave) {
		this.finishWave = finishWave;
	}

	/**
	 * 战斗结束时的塔防波数编号，当battlefieldType == KGameBattlefieldTypeEnum.好友副本战场 时有效
	 * 
	 * @return
	 */
	public int getLastTowerId() {
		return lastTowerId;
	}

	/**
	 * 设置战斗结束时的塔防波数编号，当battlefieldType == KGameBattlefieldTypeEnum.好友副本战场 时有效
	 * 
	 * @param towerId
	 */
	public void setLastTowerId(int towerId) {
		this.lastTowerId = towerId;
	}

	public long getFriendId() {
		return friendId;
	}

	public void setFriendId(long friendId) {
		this.friendId = friendId;
	}

	public int getMaxFriendDoubleHitCount() {
		return maxFriendDoubleHitCount;
	}

	public void setMaxFriendDoubleHitCount(int maxFriendDoubleHitCount) {
		this.maxFriendDoubleHitCount = maxFriendDoubleHitCount;
	}

	public int getMaxFriendBeHitCount() {
		return maxFriendBeHitCount;
	}

	public void setMaxFriendBeHitCount(int maxFriendBeHitCount) {
		this.maxFriendBeHitCount = maxFriendBeHitCount;
	}

	public byte getEndType() {
		return endType;
	}

	public void setEndType(byte endType) {
		this.endType = endType;
	}
	
	
}
