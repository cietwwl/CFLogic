package com.kola.kmp.logic.pet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KPetAtkType;
import com.kola.kmp.logic.other.KPetGetWay;
import com.kola.kmp.logic.other.KPetQuality;
import com.kola.kmp.logic.other.KPetType;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * 随从图鉴数据结构
 * 
 * @author PERRY
 *
 */
public class KPetTemplateHandbookModel implements ITransferable, Comparable<KPetTemplateHandbookModel> {

	private KPetTemplate _template;
	
	private List<IPetSkill> _skillList;
	private int _atk;
	private int _def;
	private int _maxHp;
	private int _hitRating;
	private int _dodgeRating;
	private int _critRating;
	private int _resilienceRating;
	private int _faintResistRating;
	private int _defIgnore;
	
	public KPetTemplateHandbookModel(KPetTemplate pTemplate) {
		_template = pTemplate;
	}
	
	void onGameWorldInitCompete() {
		KPetAttrPara attrDeviPara = KPetModuleManager.getAttrDeviPara(_template.attributeDeviation);
		KPetAttrPara lvPara = KPetModuleManager.getLvAttrPara(_template.maxLv);
		float lvProportion = KPetModuleConfig.getLvProportion();
		this._maxHp = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.maxHpPara, lvPara.maxHpPara, _template.growMax, lvProportion));
		this._atk = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.atkPara, lvPara.atkPara, _template.growMax, lvProportion));
		this._def = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.defPara, lvPara.defPara, _template.growMax, lvProportion));
		this._hitRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.hitRatingPara, lvPara.hitRatingPara, _template.growMax, lvProportion));
		this._dodgeRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.dodgeRatingPara, lvPara.dodgeRatingPara, _template.growMax, lvProportion));
		this._critRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.critRatingPara, lvPara.critRatingPara, _template.growMax, lvProportion));
		this._resilienceRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.resilienceRatingPara, lvPara.resilienceRatingPara, _template.growMax, lvProportion));
		this._faintResistRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.faintResistRatingPara, lvPara.faintResistRatingPara, _template.growMax, lvProportion));
		this._defIgnore = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.defIgnoreParaPara, lvPara.defIgnoreParaPara, _template.growMax, lvProportion));
		if (_template.skillMap.size() > 0) {
			List<IPetSkill> list = new ArrayList<IPetSkill>();
			Map.Entry<Integer, Integer> entry;
			for (Iterator<Map.Entry<Integer, Integer>> itr = _template.skillMap.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				list.add(new KPetSkillHandbookImpl(entry.getKey(), entry.getValue()));
			}
			this._skillList = Collections.unmodifiableList(list);
		} else {
			this._skillList = Collections.emptyList();
		}
	}
	
	@Override
	public int getTemplateId() {
		return _template.templateId;
	}

	@Override
	public int getHeadResId() {
		return _template.getHeadResId();
	}

	@Override
	public int getInMapResId() {
		return _template.getInMapResId();
	}

	@Override
	public String getName() {
		return _template.defaultName;
	}

	@Override
	public int getLevel() {
		return _template.maxLv;
	}

	@Override
	public int getMaxLevel() {
		return _template.maxLv;
	}

	@Override
	public int getGrowValue() {
		return _template.growMax;
	}

	@Override
	public int getMaxGrowValue() {
		return _template.growMax;
	}

	@Override
	public int getCurrentExp() {
		return 0;
	}

	@Override
	public int getUpgradeExp() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int getBeComposedExp() {
		return _template.basicComposeExp;
	}

	@Override
	public int getSwallowFee() {
		return 0;
	}

	@Override
	public KPetQuality getQuality() {
		return _template.quality;
	}

	@Override
	public KPetType getPetType() {
		return _template.type;
	}

	@Override
	public KPetAtkType getAtkType() {
		return _template.atkType;
	}

	@Override
	public int getAttributeByType(KGameAttrType type) {
		switch (type) {
		case ATK:
			return _atk;
		case DEF:
			return _def;
		case MAX_HP:
			return _maxHp;
		case HIT_RATING:
			return _hitRating;
		case DODGE_RATING:
			return _dodgeRating;
		case CRIT_RATING:
			return _critRating;
		case RESILIENCE_RATING:
			return _resilienceRating;
		case FAINT_RESIST_RATING:
			return _faintResistRating;
		case DEF_IGNORE:
			return _defIgnore;
		case MOVE_SPEED_X:
			return _template.moveSpeed;
		case BATTLE_POWER:
			return 0;
		default:
			return 0;
		}
	}

	@Override
	public List<IPetSkill> getSkillList() {
		return _skillList;
	}

	@Override
	public int getStarLv() {
		return 0;
	}

	@Override
	public Map<KGameAttrType, Integer> getAttrOfStar() {
		return Collections.emptyMap();
	}

	@Override
	public int getStarLvUpRateHundred() {
		return _template.starLvUpRateHundred;
	}

	@Override
	public boolean isCanBeAutoSelected() {
		return _template.canBeAutoSelected;
	}

	@Override
	public int compareTo(KPetTemplateHandbookModel o) {
		return this.getGrowValue() > o.getGrowValue() ? 1 : -1;
	}
	
	public boolean isShowInHandbook() {
		return this._template.showInHandbook;
	}
	
	public Map<KPetGetWay, String> getPetGetWayMap() {
		return this._template.getWayMap;
	}
	
	private static class KPetSkillHandbookImpl implements IPetSkill {

		private int _templateId;
		private byte _type; // 主动或被动
		private boolean _activeSkill;
		private int _lvUpRate;
		private int _maxLv;
		private int _rate;
//		private int _skillType;
		
		public KPetSkillHandbookImpl(int pTemplateId, int rate) {
			this._templateId = pTemplateId;
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
			this._rate = rate / UtilTool.HUNDRED_RATIO_UNIT;
		}
		@Override
		public int getSkillTemplateId() {
			return _templateId;
		}

		@Override
		public int getLv() {
			return _maxLv;
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
		public byte getType() {
			return _type;
		}

		@Override
		public boolean isActiveSkill() {
			return _activeSkill;
		}

		@Override
		public int getLvUpRate() {
			return _lvUpRate;
		}

		@Override
		public boolean isMaxLv() {
			return true;
		}
		
		@Override
		public int getRate() {
			return _rate;
		}
		
	}

}
