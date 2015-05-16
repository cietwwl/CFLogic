package com.kola.kmp.logic.pet;

import java.util.List;
import java.util.Map;

import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KPetAtkType;
import com.kola.kmp.logic.other.KPetQuality;
import com.kola.kmp.logic.other.KPetType;

/**
 * 
 * @author PERRY CHAN
 */
public interface ITransferable {
	
	/**
	 * 
	 * 获取模板id
	 * 
	 * @return
	 */
	public int getTemplateId();
	
	/**
	 * 
	 * @return
	 */
	public int getHeadResId();
	
	/**
	 * 
	 * @return
	 */
	public int getInMapResId();
	
	/**
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * 
	 * @return
	 */
	public int getLevel();
	
	/**
	 * 
	 * @return
	 */
	public int getMaxLevel();
	
	/**
	 * 
	 * @return
	 */
	public int getGrowValue();
	
	/**
	 * 
	 * @return
	 */
	public int getMaxGrowValue();
	
	/**
	 * 
	 * @return
	 */
	public int getCurrentExp();
	
	/**
	 * 
	 * @return
	 */
	public int getUpgradeExp();
	
	/**
	 * 
	 * 获取被吞噬时所能提供的经验
	 * 
	 * @return
	 */
	public int getBeComposedExp();
	
	/**
	 * 
	 * @return
	 */
	public int getSwallowFee();
	
	/**
	 * 
	 * @return
	 */
	public KPetQuality getQuality();
	
	/**
	 * 
	 * @return
	 */
	public KPetType getPetType();
	
	/**
	 * 
	 * @return
	 */
	public KPetAtkType getAtkType();
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public int getAttributeByType(KGameAttrType type);
	
//	/**
//	 * 
//	 * 获取当前开放的技能槽数量
//	 * 
//	 * @return
//	 */
//	public int getOpenSkillCount();
	
	/**
	 * 
	 * @return
	 */
	public List<IPetSkill> getSkillList();
	
	/**
	 * 
	 * @return
	 */
	public int getStarLv();
	
	/**
	 * 
	 * @return
	 */
	public Map<KGameAttrType, Integer> getAttrOfStar();
	
	/**
	 * 获取影响的升星成功率（百分比的分子）
	 * @return
	 */
	public int getStarLvUpRateHundred();
	
	/**
	 * 
	 * @return
	 */
	public boolean isCanBeAutoSelected();
}
