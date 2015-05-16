package com.kola.kmp.logic.competition.teampvp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.kola.kmp.logic.combat.api.ICombatPet;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.combat.api.ICombatSkillSupport;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.pet.KPetTemplate;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;

public class KTeamPVPRobotPet implements ICombatPet, ICombatSkillSupport {

	private static final AtomicLong _ID_GENERATOR = new AtomicLong();
	
	private int _templateId;
	private long _id;
	private String _name;
	private int _headResId;
	private int _inMapResId;
	private int _level;	
	private int _speedX;
	private int _speedY;
	private int _vision;
	
	private long _maxHp;
	private int _atk;
	private int _def;
	private int _hitRating;
	private int _dodgeRating;
	private int _critRating;
	private int _critMultiple;
	private int _cdReduce;
	private int _hpAbsorb;
	private int _defIgnore;
	private int _resilienceRating;
	private int _faintResistRating;
	private int _shortRaAtkItr;
	private int _longRaAtkItr;
	private int _shortRaAtkDist;
	private int _longRaAtkDist;
	private int[] _shortRaAtkAudios;
	private int[] _onHitAudios;
	private int[] _onHitScreamAudios;
	private int[] _deadAudios;
	
	private String _aiId;
	private int _fullImmunityDuration;
	private int _fullImmunityIterval;
	private int _atkCountPerTime;
	
	private long _ownerId;
	private List<ICombatSkillData> _skillList;
	
	public KTeamPVPRobotPet(int templateId, int lv) {
		this._id = _ID_GENERATOR.incrementAndGet();
		this.init(templateId, lv);
	}
	
	void init(int templateId, int lv) {
		KPetTemplate template = KSupportFactory.getPetModuleSupport().getPetTemplate(templateId);
		this._level = lv;
		this._templateId = templateId;
		this._name = template.defaultName;
		this._headResId = template.getHeadResId();
		this._inMapResId = template.getInMapResId();
		this._speedX = template.moveSpeed;
		this._speedY = this._speedX / 2;
		this._vision = template.vision;
		this._shortRaAtkAudios = Arrays.copyOf(template.getNormalAtkAudios(), template.getNormalAtkAudios().length);
		this._onHitAudios = Arrays.copyOf(template.getHittedAudios(), template.getHittedAudios().length);
		this._onHitScreamAudios = Arrays.copyOf(template.getInjuryAudios(), template.getInjuryAudios().length);
		this._deadAudios = Arrays.copyOf(template.getDeadAudios(), template.getDeadAudios().length);
		Map<KGameAttrType, Integer> attrMap = KSupportFactory.getPetModuleSupport().calcualteAttrs(template, lv, 0, template.growMax);
		this._maxHp = KGameUtilTool.getAttrValueSafely(attrMap, KGameAttrType.MAX_HP);
		this._atk = KGameUtilTool.getAttrValueSafely(attrMap, KGameAttrType.ATK);
		this._def = KGameUtilTool.getAttrValueSafely(attrMap, KGameAttrType.DEF);
		this._hitRating = KGameUtilTool.getAttrValueSafely(attrMap, KGameAttrType.HIT_RATING);
		this._dodgeRating = KGameUtilTool.getAttrValueSafely(attrMap, KGameAttrType.DODGE_RATING);
		this._critRating = KGameUtilTool.getAttrValueSafely(attrMap, KGameAttrType.CRIT_RATING);
		this._critMultiple = template.critMultiple;
		this._defIgnore = KGameUtilTool.getAttrValueSafely(attrMap, KGameAttrType.DEF_IGNORE);
		this._resilienceRating = KGameUtilTool.getAttrValueSafely(attrMap, KGameAttrType.RESILIENCE_RATING);
		this._faintResistRating = KGameUtilTool.getAttrValueSafely(attrMap, KGameAttrType.FAINT_RESIST_RATING);
		this._shortRaAtkItr = template.atkPeriod;
		this._shortRaAtkDist = template.getAtkRange();
		this._aiId = template.aiId;
		this._fullImmunityIterval = template.fullImmunityIterval;
		this._fullImmunityDuration = template.fullImmunityDuration;
		this._atkCountPerTime = template.getAtkCountPerTime();
		if(template.getSkillList() != null) {
			this._skillList = new ArrayList<ICombatSkillData>(template.getSkillList());
		} else {
			this._skillList = Collections.emptyList();
		}
	}
	
	void setOwnerId(long pOwnerId) {
		this._ownerId = pOwnerId;
	}
	
	@Override
	public boolean canBeAttack() {
		return true;
	}

	@Override
	public byte getObjectType() {
		return OBJECT_TYPE_PET;
	}

	@Override
	public int getTemplateId() {
		return _templateId;
	}

	@Override
	public long getId() {
		return _id;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public int getHeadResId() {
		return _headResId;
	}

	@Override
	public int getInMapResId() {
		return _inMapResId;
	}

	@Override
	public int getLevel() {
		return _level;
	}

	@Override
	public long getCurrentHp() {
		return _maxHp;
	}

	@Override
	public long getMaxHp() {
		return _maxHp;
	}

	@Override
	public int getBattleMoveSpeedX() {
		return _speedX;
	}

	@Override
	public int getBattleMoveSpeedY() {
		return _speedY;
	}

	@Override
	public int getVision() {
		return _vision;
	}

	@Override
	public int getAtk() {
		return _atk;
	}

	@Override
	public int getDef() {
		return _def;
	}

	@Override
	public int getHitRating() {
		return _hitRating;
	}

	@Override
	public int getDodgeRating() {
		return _dodgeRating;
	}

	@Override
	public int getCritRating() {
		return _critRating;
	}

	@Override
	public int getCritMultiple() {
		return _critMultiple;
	}

	@Override
	public int getCdReduce() {
		return _cdReduce;
	}

	@Override
	public int getHpAbsorb() {
		return _hpAbsorb;
	}

	@Override
	public int getDefIgnore() {
		return _defIgnore;
	}

	@Override
	public int getResilienceRating() {
		return _resilienceRating;
	}

	@Override
	public int getFaintResistRating() {
		return _faintResistRating;
	}

	@Override
	public int getShortRaAtkItr() {
		return _shortRaAtkItr;
	}

	@Override
	public int getLongRaAtkItr() {
		return _longRaAtkItr;
	}

	@Override
	public int getShortRaAtkDist() {
		return _shortRaAtkDist;
	}

	@Override
	public int getLongRaAtkDist() {
		return _longRaAtkDist;
	}

	@Override
	public int[] getNormalAtkAudioResIdArray() {
		return _shortRaAtkAudios;
	}

	@Override
	public int[] getOnHitAudioResIdArray() {
		return _onHitAudios;
	}

	@Override
	public int[] getInjuryAudioResIdArray() {
		return _onHitScreamAudios;
	}

	@Override
	public int[] getDeadAudioResIdArray() {
		return _deadAudios;
	}

	@Override
	public long getOwnerId() {
		return _ownerId;
	}

	@Override
	public String getAIId() {
		return _aiId;
	}

	@Override
	public ICombatSkillSupport getCombatSkillSupport() {
		return this;
	}

	@Override
	public int getFullImmunityDuration() {
		return _fullImmunityDuration;
	}

	@Override
	public int getFullImmunityIterval() {
		return _fullImmunityIterval;
	}

	@Override
	public int getAtkCountPerTime() {
		return _atkCountPerTime;
	}

	@Override
	public List<ICombatSkillData> getUsableSkills() {
		return _skillList;
	}

	@Override
	public List<ICombatSkillData> getPassiveSkills() {
		return Collections.emptyList();
	}

}
