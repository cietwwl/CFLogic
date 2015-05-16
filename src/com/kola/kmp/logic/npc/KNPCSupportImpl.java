package com.kola.kmp.logic.npc;

import java.util.Map;

import com.kola.kmp.logic.combat.api.ICombatDropInfoTemplate;
import com.kola.kmp.logic.combat.api.ICombatMonster;
import com.kola.kmp.logic.combat.api.ICombatObjectBase;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.KNPCTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.MonstUIData;
import com.kola.kmp.logic.npc.KNPCDataStructs.ObstructionTemplate;
import com.kola.kmp.logic.support.NPCModuleSupport;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-21 下午4:59:20
 * </pre>
 */
public class KNPCSupportImpl implements NPCModuleSupport {

	@Override
	public KNPCTemplate getNPCTemplate(int templateId) {
		return KNPCDataManager.mNPCTemplateManager.getTemplate(templateId);
	}

	@Override
	public KMonstTemplate getMonstTemplate(int templateId) {
		return KNPCDataManager.mMonstTemplateManager.getTemplate(templateId);
	}
	
	@Override
	public MonstUIData getMonsterUIData(int monsterUITemplateId) {
		return KNPCDataManager.mMonstUIDataManager.getData(monsterUITemplateId);
	}
	
	@Override
	public ObstructionTemplate getObstructionTemp(int templateId){
		return KNPCDataManager.mObstructionTempDataManager.getData(templateId);
	}
	
	@Override
	public Map<Integer, ObstructionTemplate> getAllObstructionTemps() {
		return KNPCDataManager.mObstructionTempDataManager.getCache();
	}
	
	@Override
	public ICombatMonster getCombatMonster(KMonstTemplate template) {
		return KNPCDataManager.mMonstTemplateManager.getCombatMonster(template);
	}
	
	@Override
	public ICombatObjectBase getCombatObstruction(ObstructionTemplate template) {
		return KNPCDataManager.mObstructionTempDataManager.getObstruction(template.id);
	}
	
	@Override
	public ICombatObjectBase getCombatObstruction(int templateId) {
		return KNPCDataManager.mObstructionTempDataManager.getObstruction(templateId);
	}
	
	@Override
	public ICombatDropInfoTemplate getDropInfoTemplate(int dropId) {
		return KNPCDataManager.mDropInfoTempDataMamanger.getDropInfoTemplate(dropId);
	}
	
	@Override
	public int getKillEnergy(int templateId) {
		KMonstTemplate template = KNPCDataManager.mMonstTemplateManager.getTemplate(templateId);
		if (template != null) {
			return template.per_anger;
		}
		return 0;
	}
}
