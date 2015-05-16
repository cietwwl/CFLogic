package com.kola.kmp.logic.support;

import java.util.Map;

import com.kola.kmp.logic.combat.api.ICombatDropInfoTemplate;
import com.kola.kmp.logic.combat.api.ICombatMonster;
import com.kola.kmp.logic.combat.api.ICombatObjectBase;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.KNPCTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.MonstUIData;
import com.kola.kmp.logic.npc.KNPCDataStructs.ObstructionTemplate;

public interface NPCModuleSupport {
	
	/**
	 * <pre>
	 * 根据NPC模板ID获取NPC模板实例
	 * 
	 * @param templateId
	 * @return
	 * </pre>
	 */
	public KNPCTemplate getNPCTemplate(int templateId);

	/**
	 * <pre>
	 * 获取怪物模板
	 * 
	 * @param templateId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-8 下午12:02:16
	 * </pre>
	 */
	public KMonstTemplate getMonstTemplate(int templateId);
	
	/**
	 * 
	 * @param monsterUITemplateId
	 * @return
	 */
	public MonstUIData getMonsterUIData(int monsterUITemplateId);
	
	/**
	 * <pre>
	 * 获取战场障碍物模板
	 * 
	 * @param templateId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-8 下午12:01:59
	 * </pre>
	 */
	public ObstructionTemplate getObstructionTemp(int templateId);
	
	/**
	 * 
	 * <pre>
	 * 获取所有的障碍物模板
	 * </pre>
	 * 
	 * @return 一个只读的障碍物模板集合
	 */
	public Map<Integer, ObstructionTemplate> getAllObstructionTemps();
	
	/**
	 * <pre>
	 * 通过模板id，获取ICombatMonster实例
	 * 注意：这个实例是全局数据，全服共用一个数据
	 * </pre>
	 * @param templateId
	 * @return
	 */
	public ICombatMonster getCombatMonster(KMonstTemplate template);
	
	/**
	 * <pre>
	 * 通过模板id，创建一个新的Obstruction实例
	 * 注意：这个实例是全局数据，全服共用一个数据
	 * </pre>
	 * @param templateId
	 * @return
	 */
	public ICombatObjectBase getCombatObstruction(ObstructionTemplate template);
	
	/**
	 * 
	 * @param templateId
	 * @return
	 */
	public ICombatObjectBase getCombatObstruction(int templateId);
	
	/**
	 * 
	 * @param dropId
	 * @return
	 */
	public ICombatDropInfoTemplate getDropInfoTemplate(int dropId);

	/**
	 * 
	 * @param templateId
	 * @return
	 */
	public int getKillEnergy(int templateId);
}
