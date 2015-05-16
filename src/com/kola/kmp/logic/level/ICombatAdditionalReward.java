package com.kola.kmp.logic.level;

import java.util.Map;

import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.role.KRole;


/**
 * 
 * 战斗额外奖励
 * 
 * @author PERRY CHAN
 */
public interface ICombatAdditionalReward {

	/**
	 * 
	 * 获取这个附加奖励的角色id
	 * 
	 * @return
	 */
	public long getRoleId();
	
	/**
	 * 获取附加的货币奖励
	 * 
	 * @return 货币奖励集合，key=货币类型，value=奖励的数值
	 */
	public Map<KCurrencyTypeEnum, Integer> getAdditionalCurrencyReward();
	
	/**
	 * 
	 * @param type
	 * @param value
	 */
	public void addCurrencyReward(KCurrencyTypeEnum type, int value);

	/**
	 * 获取附加的道具奖励
	 * 
	 * @return 道具奖励集合，key=道具编号，value=奖励的数量
	 */
	public Map<String, Integer> getAdditionalItemReward();
	
	/**
	 * 
	 * @param itemCode
	 * @param value
	 */
	public void addItemReward(String itemCode, int value);
	
	/**
	 * 
	 * 获取附加的宠物奖励
	 * 
	 * @return
	 */
	public Map<Integer, Integer> getAdditionalPetReward();
	
	/**
	 * 
	 * @param templateId
	 * @param count
	 */
	public void addAdditionalPetReward(int templateId, int count);
	
	/**
	 * 
	 * @param role
	 */
	public void executeReward(KRole role);
}
