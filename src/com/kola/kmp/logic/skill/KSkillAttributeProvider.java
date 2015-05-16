package com.kola.kmp.logic.skill;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.kola.kmp.logic.item.KItemAttributeProvider;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.IRoleAttributeProvider;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-4-8 下午8:49:10
 * </pre>
 */
public class KSkillAttributeProvider implements IRoleAttributeProvider {

	private static int _type;

	public static int getType() {
		return KItemAttributeProvider.getType();
//		return _type;
	}

	@Override
	public Map<KGameAttrType, Integer> getEffectAttr(KRole role) {
		return getEffectAttr(role.getId());
	}
	
	private static Map<KGameAttrType, Integer> getEffectAttr(long roleId) {
		KSkillSet set = KSkillModuleExtension.getSkillSet(roleId);
		set.rwLock.lock();
		try {
			Map<Integer, KSkill> pasSkills = set.searchAllSkills(false);
			if (pasSkills == null || pasSkills.isEmpty()) {
				return Collections.emptyMap();
			}

			Map<KGameAttrType, Integer> map = new HashMap<KGameAttrType, Integer>();
			for (KSkill skill : pasSkills.values()) {
				KRolePasSkillTemp temp = KSkillDataManager.mRolePasSkillTempManager.getTemplate(skill.getSkillTemplateId());
				if (temp != null) {
					Map<KGameAttrType, Integer> attMap = temp.allEffects.get(skill.getLv());
					if (attMap != null) {
//						map.putAll(attMap);
						//
						for (Entry<KGameAttrType, Integer> e : attMap.entrySet()) {
							Integer oldValue = map.get(e.getKey());
							if (oldValue == null) {
								map.put(e.getKey(), e.getValue());
							} else {
								map.put(e.getKey(), e.getValue() + oldValue);
							}
						}
					}
				}
			}
			return map;
		} finally {
			set.rwLock.unlock();
		}
	}
	
	/**
	 * <pre>
	 * 被动技能加成C = 累加单件装备基础属性*被动技能额外增加比例
	 * 
	 * @param baseResult
	 * @param totalResult
	 * @author CamusHuang
	 * @creation 2015-3-3 上午10:46:51
	 * </pre>
	 */
	public static Map<KGameAttrType, AtomicInteger> getEffectForEqui(long roleId, Map<KGameAttrType, AtomicInteger> baseResult, Map<KGameAttrType, AtomicInteger> totalResult){
		if (totalResult == null) {
			totalResult = new HashMap<KGameAttrType, AtomicInteger>();
		}
		if (baseResult == null || baseResult.isEmpty()) {
			return totalResult;
		}
		
		Map<KGameAttrType, Integer> skillAtts = getEffectAttr(roleId);
		if (skillAtts == null || skillAtts.isEmpty()) {
			return totalResult;
		}
		
		for(Entry<KGameAttrType, Integer> skillEntry:skillAtts.entrySet()){
			KGameAttrType skillAttType = skillEntry.getKey();
			AtomicInteger baseValue = baseResult.get(skillAttType);
			if(baseValue!=null){
				// 技能属性和基础属性一样，叠加
				AtomicInteger totalValue = totalResult.get(skillAttType);
				if(totalValue==null){
					totalValue = new AtomicInteger();
					totalResult.put(skillAttType, totalValue);
				}
				totalValue.addAndGet(skillEntry.getValue());
			} else {
				// 技能属性和基础属性不一样
				if(skillAttType.isPercentageType){
					//技能属性是比例值
					KGameAttrType srcAttType = KGameAttrType.getSrcAttrTypeOfPercentage(skillAttType);
					if(srcAttType==null){
						continue;
					}
					AtomicInteger value = baseResult.get(srcAttType);
					if(value==null){
						continue;
					}
					//
					AtomicInteger totalValue = totalResult.get(srcAttType);
					if(totalValue==null){
						totalValue = new AtomicInteger();
						totalResult.put(srcAttType, totalValue);
					}
					totalValue.addAndGet((int)(value.get()*(((float)skillEntry.getValue())/10000)));
				} else {
					//技能属性是绝对值，忽略
				}
			}
		}
		
		return totalResult;
	}

	@Override
	public void notifyTypeAssigned(int type) {
		_type = type;
	}

}
