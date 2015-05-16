package com.kola.kmp.logic.combat.mirror;

import java.util.Arrays;
import java.util.List;

import com.kola.kmp.logic.combat.api.ICombatObjectBase;
import com.kola.kmp.logic.combat.api.ICombatObjectFight;
import com.kola.kmp.logic.combat.api.ICombatSkillData;

/**
 * 
 * @author PERRY CHAN
 */
public abstract class KCombatMirrorFight extends KCombatMirrorBase implements ICombatObjectFight {

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
	
	protected KCombatMirrorFight(ICombatObjectBase base, ICombatObjectFight fight, List<ICombatSkillData> pActivateSkills, List<ICombatSkillData> pPassiveSkills) {
		super(base, pActivateSkills, pPassiveSkills);
		this.initFightAttribute(fight);
	}
	
	private void initFightAttribute(ICombatObjectFight obj) {
		this._atk = obj.getAtk();
		this._def = obj.getDef();
		this._hitRating = obj.getHitRating();
		this._dodgeRating = obj.getDodgeRating();
		this._critRating = obj.getCritRating();
		this._resilienceRating = obj.getResilienceRating();
		this._faintResistRating = obj.getFaintResistRating();
		this._critMultiple = obj.getCritMultiple();
		this._hpAbsorb = obj.getHpAbsorb();
		this._defIgnore = obj.getDefIgnore();
		this._cdReduce = obj.getCdReduce();
		this._shortRaAtkDist = obj.getShortRaAtkDist();
		this._longRaAtkDist = obj.getLongRaAtkDist();
		this._shortRaAtkItr = obj.getShortRaAtkItr();
		this._longRaAtkItr = obj.getLongRaAtkItr();
		this._shortRaAtkAudios = Arrays.copyOf(obj.getNormalAtkAudioResIdArray(), obj.getNormalAtkAudioResIdArray().length);
		this._onHitAudios = Arrays.copyOf(obj.getOnHitAudioResIdArray(), obj.getOnHitAudioResIdArray().length);
		this._onHitScreamAudios = Arrays.copyOf(obj.getInjuryAudioResIdArray(), obj.getInjuryAudioResIdArray().length);
		this._deadAudios = Arrays.copyOf(obj.getDeadAudioResIdArray(), obj.getDeadAudioResIdArray().length);
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
}
