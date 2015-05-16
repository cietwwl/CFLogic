package com.kola.kmp.logic.competition.teampvp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.kola.kmp.logic.combat.api.ICombatMount;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.combat.api.ICombatSkillSupport;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountTemplate;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.support.KSupportFactory;

public class KTeamPVPRobotMount implements ICombatMount, ICombatSkillSupport {

	private static final AtomicLong _ID_GENERATOR = new AtomicLong();
	
	private long _id;
	private KMountTemplate _template;
	private int _level;	
	List<ICombatSkillData> _skillList;
	
	KTeamPVPRobotMount(int templateId, int lv) {
		this._id = _ID_GENERATOR.incrementAndGet();
		this.init(templateId, lv);
	}
	
	void init(int templateId, int lv) {
		KMountTemplate template = KSupportFactory.getMountModuleSupport().getMountTemplate(templateId);
		_template = template;
		this._level = lv;
		List<ICombatSkillData> skillList = new ArrayList<ICombatSkillData>();
		this._skillList = Collections.unmodifiableList(skillList);
		List<Integer> skillIds = template.skillIdList;
		for(int i = 0; i < skillIds.size(); i++) {
			skillList.add(new KRobotMountSkillImpl(skillIds.get(i), 1));
		}
	}
	
	@Override
	public boolean canBeAttack() {
		return true;
	}

	@Override
	public byte getObjectType() {
		return OBJECT_TYPE_VEHICLE;
	}

	@Override
	public int getTemplateId() {
		return _template.mountsID;
	}

	@Override
	public long getId() {
		return _id;
	}

	@Override
	public String getName() {
		return _template.Name;
	}

	@Override
	public int getHeadResId() {
		return _template.HeadID;
	}

	@Override
	public int getInMapResId() {
		return _template.res_id;
	}

	@Override
	public int getLevel() {
		return _level;
	}

	@Override
	public long getCurrentHp() {
		return 0;
	}

	@Override
	public long getMaxHp() {
		return 0;
	}

	@Override
	public int getBattleMoveSpeedX() {
		return 0;
	}

	@Override
	public int getBattleMoveSpeedY() {
		return 0;
	}

	@Override
	public int getVision() {
		return 0;
	}

	@Override
	public List<ICombatSkillData> getUsableSkills() {
		return _skillList;
	}

	@Override
	public List<ICombatSkillData> getPassiveSkills() {
		return Collections.emptyList();
	}

	@Override
	public float getSpeedUpTimes() {
		return _template.fightMoveSpeed;
	}

	@Override
	public int getAtkCountPerTime() {
		return _template.atkCountPerTime;
	}

	@Override
	public int getFullImmunityDuration() {
		return _template.armortime;
	}

	@Override
	public int getFullImmunityIteration() {
		return _template.armorinterval;
	}

	@Override
	public String getAI() {
		return _template.mountsAI;
	}

	@Override
	public Map<Integer, Integer> getBeanTime() {
		return _template.beanTimeMap;
	}

	@Override
	public Map<KGameAttrType, Integer> getBasicAttrs() {
		return _template.getWarAtts();
	}

	@Override
	public Map<KGameAttrType, Integer> getEquipmentAttrs() {
		return Collections.emptyMap();
	}
	
	private static class KRobotMountSkillImpl implements ICombatSkillData {

		private int _templateId;
		private int _lv;
		
		public KRobotMountSkillImpl(int pTemplateId, int pLv) {
			this._templateId = pTemplateId;
			this._lv = pLv;
		}
		
		@Override
		public int getSkillTemplateId() {
			return _templateId;
		}

		@Override
		public int getLv() {
			return _lv;
		}

		@Override
		public boolean isSuperSkill() {
			return false;
		}

		@Override
		public boolean onlyEffectInPVP() {
			return false;
		}
		
	}

}
