package com.kola.kmp.logic.skill;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.kola.kmp.logic.combat.api.ICombatMinionTemplateData;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.combat.api.ICombatSkillTemplateData;
import com.kola.kmp.logic.combat.state.ICombatStateTemplate;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;
import com.kola.kmp.logic.skill.message.KPushSkillsMsg;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.SkillModuleSupport;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-21 下午4:59:04
 * </pre>
 */
public class KSkillSupportImpl implements SkillModuleSupport {

	private AtomicBoolean _getAllSkillTemplateDataStatus = new AtomicBoolean();
	private AtomicBoolean _getAllStateTemplateDataStatus = new AtomicBoolean();
	private AtomicBoolean _getAllPassiveSkillTemplateDataStatus = new AtomicBoolean();

	private List<ICombatSkillTemplateData> getAllSkillTemplateData(Map<Integer, KRoleIniSkillTemp> map) {
		KRoleIniSkillTemp skillTemplate;
		List<ICombatSkillTemplateData> list = new ArrayList<ICombatSkillTemplateData>(map.size());
		for (Iterator<KRoleIniSkillTemp> itr = map.values().iterator(); itr.hasNext();) {
			skillTemplate = itr.next();
			list.addAll(skillTemplate.skillLevelDatas);
		}
		return list;
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<ICombatSkillTemplateData> getAllSkillTemplateData() {
		List<ICombatSkillTemplateData> list = null;
		if (_getAllSkillTemplateDataStatus.compareAndSet(false, true)) {
			// 排除重复调用
			int size = KSkillDataManager.mRoleIniSkillTempManager.getCache().size();
			size += KSkillDataManager.mPetSkillTempManager.getCache().size();
			size += KSkillDataManager.mMountSkillTempManager.getCache().size();
			list = new ArrayList<ICombatSkillTemplateData>(size);
			list.addAll(this.getAllSkillTemplateData(KSkillDataManager.mRoleIniSkillTempManager.getCache()));
			list.addAll(this.getAllSkillTemplateData(KSkillDataManager.mPetSkillTempManager.getCache()));
			list.addAll(this.getAllSkillTemplateData(KSkillDataManager.mMountSkillTempManager.getCache()));
			list.addAll(this.getAllSkillTemplateData(KSkillDataManager.mMonsterSkillTempManager.getCache()));
		}
		return list;
	}

	@SuppressWarnings("deprecation")
	public List<ICombatSkillTemplateData> getAllPassiveSkillTemplateData() {
		List<ICombatSkillTemplateData> list = new ArrayList<ICombatSkillTemplateData>();
		if (_getAllPassiveSkillTemplateDataStatus.compareAndSet(false, true)) {
			Map<Integer, KRolePasSkillTemp> allMap = new HashMap<Integer, KRolePasSkillTemp>(KSkillDataManager.mRolePasSkillTempManager.getCache());
			allMap.putAll(KSkillDataManager.mPetPasSkillTempManager.getCache());
			KRolePasSkillTemp template;
			for (Iterator<KRolePasSkillTemp> itr = allMap.values().iterator(); itr.hasNext();) {
				template = itr.next();
				list.addAll(template.skillLevelDatas);
			}
		}
		return list;
	}

	@Override
	public List<ICombatStateTemplate> getAllStateTemplateData() {
		List<ICombatStateTemplate> list = new ArrayList<ICombatStateTemplate>();
		if (_getAllStateTemplateDataStatus.compareAndSet(false, true)) {
			list.addAll(KSkillDataManager.mSkillStatusManager.getCache().values());
		}
		return list;
	}

	@Override
	public boolean addSkillToRole(long roleId, boolean isInitiative, int templateId, String sourceTips) {
		return KSkillLogic.addSkillToRole(roleId, isInitiative, templateId, sourceTips);
	}

	@Override
	public KRoleIniSkillTemp getRoleIniSkillTemplate(int templateId) {
		return KSkillDataManager.mRoleIniSkillTempManager.getTemplate(templateId);
	}

	@Override
	public KRolePasSkillTemp getRolePasSkillTemplate(int templateId) {
		return KSkillDataManager.mRolePasSkillTempManager.getTemplate(templateId);
	}
	
	@Override
	public KRolePasSkillTemp getPetPasSkillTemplate(int templateId) {
		return KSkillDataManager.mPetPasSkillTempManager.getTemplate(templateId);
	}
	
	@Override
	public KRolePasSkillTemp getPasSkillTemplate(int templateId) {
		KRolePasSkillTemp skillTemplate = KSkillDataManager.mRolePasSkillTempManager.getTemplate(templateId);
		if(skillTemplate == null) {
			skillTemplate = KSkillDataManager.mPetPasSkillTempManager.getTemplate(templateId);
		}
		return skillTemplate;
	}

	public KRoleIniSkillTemp getMountSkillTemplate(int templateId) {
		return KSkillDataManager.mMountSkillTempManager.getTemplate(templateId);
	}

	public KRoleIniSkillTemp getMonsterSkillTemplate(int templateId) {
		return KSkillDataManager.mMonsterSkillTempManager.getTemplate(templateId);
	}

	@Override
	public KRoleIniSkillTemp getPetSkillTemplate(int templateId) {
		return KSkillDataManager.mPetSkillTempManager.getTemplate(templateId);
	}

	@Override
	public List<Integer> getRoleAllIniSkills(long roleId) {
		KSkillSet set = KSkillModuleExtension.getSkillSet(roleId);
		set.rwLock.lock();
		try {
			return new ArrayList<Integer>(set.searchAllSkills(true).keySet());
		} finally {
			set.rwLock.unlock();
		}
	}

	public List<Integer> getRoleAllPasSkills(long roleId) {
		KSkillSet set = KSkillModuleExtension.getSkillSet(roleId);
		set.rwLock.lock();
		try {
			return new ArrayList<Integer>(set.searchAllSkills(false).keySet());
		} finally {
			set.rwLock.unlock();
		}
	}

	@Override
	public List<Integer> getRoleInUseIniSkills(long roleId) {
		KSkillSet set = KSkillModuleExtension.getSkillSet(roleId);
		set.rwLock.lock();
		try {
			List<Integer> result = new ArrayList<Integer>();
			int[] skillTempSlot = set.getSkillSlotDataCache();
			for (int tempId : skillTempSlot) {
				result.add(tempId);
			}
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	@Override
	public List<ICombatSkillData> getRoleInUseIniSkillInstances(long roleId) {
		KSkillSet set = KSkillModuleExtension.getSkillSet(roleId);
		set.rwLock.lock();
		try {
			@SuppressWarnings("deprecation")
			int[] slotData = set.getSkillSlotDataCache();
			List<ICombatSkillData> result = new ArrayList<ICombatSkillData>();
			for (int templateId : slotData) {
				KSkill skill = set.searchSkill(true, templateId);
				if (skill != null) {
					result.add(skill);
				}
			}
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	@Override
	public List<ICombatSkillData> getRolePasSkillInstances(long roleId) {
		List<ICombatSkillData> result = new ArrayList<ICombatSkillData>();
		KSkillSet set = KSkillModuleExtension.getSkillSet(roleId);
		set.rwLock.lock();
		try {
			result.addAll(set.searchAllSkills(false).values());
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	@Override
	public ICombatStateTemplate getStateTemplate(int stateTemplateId) {
		return KSkillDataManager.mSkillStatusManager.getData(stateTemplateId);
	}

	@Override
	public ICombatMinionTemplateData getMinionTemplateData(int templateId) {
		return KSkillDataManager.mSkillMinionManager.getMinion(templateId);
	}

	@Override
	public int getSkillBattlePower(long roleId, int roleLv) {
		int battlePower = 0;
		KSkillSet set = KSkillModuleExtension.getSkillSet(roleId);
		set.rwLock.lock();
		try {
			KSkill tempSkill;
			int[] skillTempSlot = set.getSkillSlotDataCache();
			for (int tempId : skillTempSlot) {
				tempSkill = set.searchSkill(true, tempId);
				if (tempSkill != null) {
					battlePower += KSkillLogic.ExpressionForPower(KSkillDataManager.mRoleIniSkillTempManager.getTemplate(tempId), tempSkill.getLv(), roleLv);
				}
			}
		} finally {
			set.rwLock.unlock();
		}
		return battlePower;
	}

	@Override
	public int getMaxSkillSlotCount() {
		return KSkillConfig.getInstance().MaxSkillForWar;
	}

	@Override
	public void cloneRoleByCamus(KRole myRole, KRole srcRole) {

		KSkillSet srcset = KSkillModuleExtension.getSkillSet(srcRole.getId());
		Map<Integer, KSkill> scrInits = srcset.searchAllSkills(true);
		Map<Integer, KSkill> scrPas = srcset.searchAllSkills(false);

		KSkillSet myset = KSkillModuleExtension.getSkillSet(myRole.getId());
		myset.rwLock.lock();
		try {
			Map<Integer, KSkill> myInits = myset.searchAllSkills(true);
			Map<Integer, KSkill> myPas = myset.searchAllSkills(false);

			// 清除多余的主动技能
			for (Iterator<KSkill> it = myInits.values().iterator(); it.hasNext();) {
				KSkill myskill = it.next();
				if (!scrInits.containsKey(myskill._templateId)) {
					myset.notifyElementDelete(myskill._id);
				}
			}
			// 清除多余的被动技能
			for (Iterator<KSkill> it = myPas.values().iterator(); it.hasNext();) {
				KSkill myskill = it.next();
				if (!scrInits.containsKey(myskill._templateId)) {
					myset.notifyElementDelete(myskill._id);
				}
			}

			// 复制主动技能
			for (KSkill srcskill : scrInits.values()) {
				KSkill myskill = myset.searchSkill(true, srcskill._templateId);
				if (myskill == null) {
					myskill = new KSkill(myset, true, srcskill._templateId);
					myset.addSkill(myskill);
				}
				myskill.setLv(srcskill.getLv());
			}
			// 复制被动技能
			for (KSkill srcskill : scrPas.values()) {
				KSkill myskill = myset.searchSkill(false, srcskill._templateId);
				if (myskill == null) {
					myskill = new KSkill(myset, false, srcskill._templateId);
					myset.addSkill(myskill);
				}
				myskill.setLv(srcskill.getLv());
			}

		} finally {
			myset.rwLock.unlock();

			KPushSkillsMsg.pushAllSkills(myRole);
			KPushSkillsMsg.pushSelectedSkills(myRole);

			KSupportFactory.getRoleModuleSupport().notifySkillListChange(myRole.getId());
			// 刷新角色属性
			KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(myRole.getId(), KSkillAttributeProvider.getType());
		}
	}
	
	@Override
	public boolean isRoleSkill(int templateId) {
		return KSkillDataManager.mRoleIniSkillTempManager.containTemplate(templateId) || KSkillDataManager.mRolePasSkillTempManager.containTemplate(templateId);
	}
}
