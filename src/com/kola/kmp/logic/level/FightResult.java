package com.kola.kmp.logic.level;

import java.util.HashMap;
import java.util.Map;

import com.kola.kmp.logic.level.petcopy.KPetCopyBattlefield.KPetCopyBattlefieldDropData;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;

/**
 * 战斗结果数据
 * 
 * @author zhaizl
 * 
 */
public class FightResult {
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
	// 本场战斗杀掉的怪物的数量，key=怪物模板id，value=杀怪的数量
	private Map<Integer, Short> _killMonsterCount;
	// 战斗中获得的奖励
	private ICombatAdditionalReward battleReward;

	private byte endType; // 战斗结束类型：FIGHT_END_TYPE_NORMAL=1，正常结束；FIGHT_END_TYPE_ESCAPE=2，逃跑结束

	// 战斗时间
	private long battleTime;
	// 总计伤害
	private int totalDamage;
	// 角色最大连击数
	private int maxDoubleHitCount;
	// 角色最大受击数
	private int maxBeHitCount;
	// 角色结束时战斗剩余的HP
	private long roleCurrentHp;
	// 随从结束时战斗剩余的HP
	private long petCurrentHp;
	// 随从副本摧毁笼子统计，Key:笼子实例对象，Value:是否摧毁。(当战场类型为随从副本战场时有效)
	private Map<KPetCopyBattlefieldDropData,Boolean> petCopyResultMap = new HashMap<KPetCopyBattlefieldDropData, Boolean>();

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

	/**
	 * 
	 * 获取杀怪的数量，key=怪物模板id，value=杀怪的数量
	 * 
	 * @return
	 */
	public Map<Integer, Short> getKillMonsterCount() {
		return this._killMonsterCount;
	}

	/**
	 * 
	 * @param map
	 */
	public void setKillMonsterCount(Map<Integer, Short> map) {
		this._killMonsterCount = map;
	}

	public ICombatAdditionalReward getBattleReward() {
		return battleReward;
	}

	public void setBattleReward(ICombatAdditionalReward battleReward) {
		this.battleReward = battleReward;
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
	 * @return
	 */
	public int getTotalDamage() {
		return totalDamage;
	}
	
	/**
	 * 设置战斗总伤害
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
	 * 
	 * 获取战斗结束时，角色剩余的血量
	 * 
	 * @return
	 */
	public long getRoleCurrentHp() {
		return roleCurrentHp;
	}

	/**
	 * 
	 * 设置战斗结束时，角色剩余的血量
	 * 
	 * @return
	 */
	public void setRoleCurrentHp(long roleCurrentHp) {
		this.roleCurrentHp = roleCurrentHp;
	}

	/**
	 * 
	 * 获取战斗结束时，随从剩余的血量
	 * 
	 * @return
	 */
	public long getPetCurrentHp() {
		return petCurrentHp;
	}

	/**
	 * 
	 * 设置战斗结束时，随从剩余的血量
	 * 
	 * @return
	 */
	public void setPetCurrentHp(long petCurrentHp) {
		this.petCurrentHp = petCurrentHp;
	}

	/**
	 * 战斗结束类型：FIGHT_END_TYPE_NORMAL=0，正常结束；FIGHT_END_TYPE_ESCAPE=1，逃跑结束
	 * 
	 * @return
	 */
	public byte getEndType() {
		return endType;
	}

	/**
	 * 设置战斗结束类型，参考FIGHT_END_TYPE_NORMAL=0，正常结束；FIGHT_END_TYPE_ESCAPE=1，逃跑结束
	 * 
	 * @param endType
	 */
	public void setEndType(byte endType) {
		this.endType = endType;
	}

	/**
	 * 获取随从副本摧毁笼子统计，Key:笼子实例对象，Value:是否摧毁。(当战场类型为随从副本战场时有效)
	 * @return
	 */
	public Map<KPetCopyBattlefieldDropData, Boolean> getPetCopyResultMap() {
		return petCopyResultMap;
	}
	
	/**
	 * 
	 * @param map
	 */
	public void setPetCopyResultMap(Map<KPetCopyBattlefieldDropData, Boolean> map) {
		this.petCopyResultMap.putAll(map);
	}
	
	
}
