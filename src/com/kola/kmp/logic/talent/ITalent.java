package com.kola.kmp.logic.talent;

import java.util.Map;

import com.kola.kmp.logic.other.KGameAttrType;

/**
 * 
 * @author PERRY CHAN
 */
public interface ITalent {
	
	/**
	 * 
	 * 获取天赋的id
	 * 
	 * @return
	 */
	public int getTalentId();
	
	/**
	 * 
	 * 获取天赋的名字
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * 
	 * 获取前置天赋id
	 * 
	 * @return
	 */
	public int getPreTalentId();
	
	/**
	 * 
	 * 是否主线天赋
	 * 
	 * @return
	 */
	public boolean isMainline();
	
	/**
	 * 
	 * 是否已经开通（等级>0就表示被开通了）
	 * 
	 * @return
	 */
	public boolean isEnable();
	
	/**
	 * 
	 * 获取当前等级
	 * 
	 * @return
	 */
	public int getCurrentLevel();
	
	/**
	 * 
	 * 是否满级
	 * 
	 * @return
	 */
	public boolean isMaxLv();
	
	/**
	 * 
	 * @return
	 */
	public boolean isSkillTalent();
	
	/**
	 * 
	 * @return
	 */
	public int getSkillTemplateId();
	
	/**
	 * 
	 * 获取天赋的影响属性
	 * 
	 * @return
	 */
	public Map<KGameAttrType, Integer> getEffectAttr();

}
