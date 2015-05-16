package com.kola.kmp.logic.combat.mirror;

import java.util.ArrayList;
import java.util.List;

import com.kola.kmp.logic.combat.api.ICombatObjectBase;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.combat.api.ICombatSkillSupport;

/**
 * 
 * @author PERRY CHAN
 */
public abstract class KCombatMirrorBase implements ICombatObjectBase, ICombatSkillSupport {
	
	private boolean _canBeAttack;
	private byte _objectType;
	private int _templateId;
	private long _id;
	private String _name;
	private int _headResId;
	private int _inMapResId;
	private int _level;
	private long _maxHp;
	private int _speedX;
	private int _speedY;
	private int _vision;
	
	private List<ICombatSkillData> _activateSkills;
	private List<ICombatSkillData> _passiveSkills;
	
	protected KCombatMirrorBase(ICombatObjectBase base, List<ICombatSkillData> pActivateSkills, List<ICombatSkillData> pPassiveSkills) {
		this.initFromBase(base);
		this._activateSkills = new ArrayList<ICombatSkillData>(pActivateSkills);
		if (pPassiveSkills != null) {
			this._passiveSkills = new ArrayList<ICombatSkillData>(pPassiveSkills);
		} else {
			this._passiveSkills = new ArrayList<ICombatSkillData>();
		}
	}

	private void initFromBase(ICombatObjectBase base) {
		this._id = base.getId();
		this._canBeAttack = base.canBeAttack();
		this._objectType = base.getObjectType();
		this._templateId = base.getTemplateId();
		this._name = base.getName();
		this._headResId = base.getHeadResId();
		this._inMapResId = base.getInMapResId();
		this._level = base.getLevel();
		this._maxHp = base.getMaxHp();
		this._speedX = base.getBattleMoveSpeedX();
		this._speedY = base.getBattleMoveSpeedY();
		this._vision = base.getVision();
	}
	
	@Override
	public boolean canBeAttack() {
		return _canBeAttack;
	}

	@Override
	public byte getObjectType() {
		return _objectType;
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
	public List<ICombatSkillData> getUsableSkills() {
		return _activateSkills;
	}

	@Override
	public List<ICombatSkillData> getPassiveSkills() {
		return _passiveSkills;
	}

}
