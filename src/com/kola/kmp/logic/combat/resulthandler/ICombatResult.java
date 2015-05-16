package com.kola.kmp.logic.combat.resulthandler;

import java.util.List;

import com.kola.kmp.logic.combat.ICombatGlobalCommonResult;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.level.ICombatAdditionalReward;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatResult extends ICombatGlobalCommonResult {
	
	String KEY_ATTACHMENT = "attachment";
	String KEY_CLEAR_TOWER_ID = "towerId";
	String KEY_CLEAR_TOWER_WAVE_NUM = "towerWave";
	
	/**
	 * 
	 * @return
	 */
	public List<Long> getAllRoleIds();

	/**
	 * 
	 * @param roleId
	 * @return
	 */
	public ICombatRoleResult getRoleResult(long roleId);
	
	/**
	 * 
	 * @return
	 */
	public boolean isRoleWin();
	
	/**
	 * 
	 * 获取角色的战场奖励
	 * 
	 * @param roleId
	 * @return
	 */
	public ICombatAdditionalReward getCombatReward(long pRoleId);
	
	/**
	 * 
	 * 获取本场战斗一共进行的时间
	 * 
	 * @return
	 */
	public long getTotalCombatTime();
	
//	/**
//	 * <pre>
//	 * 获取本场战斗的最后一张战斗地图的战场id（关卡表的配置的id）
//	 * <pre>
//	 * 
//	 * @return
//	 */
//	public int getLastBattleFieldId();
//	
//	/**
//	 * <pre>
//	 * 获取本场战斗的最后一张战斗地图的战场类型（关卡表的配置的）
//	 * <pre>
//	 *  
//	 * @return
//	 */
//	public KGameBattlefieldTypeEnum getLastBattleFieldType();
	
	/**
	 * 
	 * 获取战斗中关卡的一些信息
	 * 
	 * @return
	 */
	public ICombatGameLevelInfo getGameLevelInfo();
	
	/**
	 * 
	 * @return
	 */
	public Object getAttachment();
	
	/**
	 * 
	 * @param key
	 * @param obj
	 */
	public void putAttributeToResult(String key, Object obj);
	
	/**
	 * 
	 * @param key
	 */
	public Object getAttributeFromResult(String key);
	
	/**
	 * 
	 * @param instanceId
	 * @param monster
	 */
	public void recordMonsterHpInfo(int instanceId, ICombatMember monster);
	
	/**
	 * 
	 * 记录被击杀的instanceId，这个只是在随从副本中有用
	 * 
	 * @param instanceId
	 */
	public void recordKillInstanceId(int instanceId);
	
	/**
	 * 
	 * @return
	 */
	public List<Integer> getKillInstanceIds();
}
