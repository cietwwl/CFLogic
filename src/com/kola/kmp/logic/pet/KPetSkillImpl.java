package com.kola.kmp.logic.pet;

import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KPetSkillImpl implements IPetSkill {

	private int _templateId;
	private int _level;
	private byte _type; // 主动或被动
	private boolean _activeSkill;
	private int _lvUpRate;
	private int _maxLv;
	private int _skillType;
	
	public KPetSkillImpl(int pTemplateId, int pLevel) {
		this._templateId = pTemplateId;
		this._level = pLevel;
		KRoleIniSkillTemp skillTemplate = KSupportFactory.getSkillModuleSupport().getPetSkillTemplate(_templateId);	
		if (skillTemplate != null && skillTemplate.isIniSkill) {
			_type = SKILL_TYPE_ACTIVE;
			_lvUpRate = skillTemplate.lvUpRate;
			_maxLv = skillTemplate.max_lvl;
		} else {
			_type = SKILL_TYPE_PASSIVE;
			KRolePasSkillTemp pasSkillTemplate = KSupportFactory.getSkillModuleSupport().getPetPasSkillTemplate(_templateId);
			_lvUpRate = pasSkillTemplate.lvUpRate;
			_maxLv = pasSkillTemplate.max_lvl;
		}
		_activeSkill = _type == SKILL_TYPE_ACTIVE;
	}
	
	void updateLv(int value) {
		this._level += value;
		if(this._level > _maxLv) {
			this._level = _maxLv;
		}
	}
	
	@Override
	public int getSkillTemplateId() {
		return _templateId;
	}

	@Override
	public int getLv() {
		return _level;
	}

	@Override
	public byte getType() {
		return _type;
	}
	
	@Override
	public boolean isActiveSkill() {
		return _activeSkill;
	}

	@Override
	public boolean isSuperSkill() {
		return false;
	}
	
	@Override
	public boolean onlyEffectInPVP() {
		return false;
	}
	
	@Override
	public int getLvUpRate() {
		return _lvUpRate;
	}
	
	@Override
	public boolean isMaxLv() {
		return this._level == _maxLv;
	}
	
	@Override
	public int getRate() {
		return 100;
	}

}
