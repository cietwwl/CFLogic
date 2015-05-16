package com.kola.kmp.logic.role;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.character.ICharacterBattleAttr;

/**
 * 
 * @author PERRY CHAN
 */
public class KRoleLevelAttribute implements ICharacterBattleAttr {

	private int _maxHp;
	private int _maxEnergy;
	private int _hpRecovery;
	private int _atk;
	private int _def;
	private int _critRating;
	private int _dodgeRating;
	private int _hitRating;
	private int _resilienceRating;
	private int _faintResistRating;
	private int _critMultiple;
	private int _cdReduce;
	private int _hpAbsorb;
	private int _defIgnore;
	private int _shortRaAtkItr;
	private int _longRaAtkItr;
	private int _shortRaAtkDist;
	private int _longRaAtkDist;
	private int _moveSpeedX;
	private int _moveSpeedY;
	private int _battleMoveSpeedX;
	private int _battleMoveSpeedY;
	
	public KRoleLevelAttribute(KGameExcelRow row, KRoleTemplate template) {
		this._atk = row.getInt("atk");
		this._def = row.getInt("def");
		this._maxHp = row.getInt("maxHp");
		this._maxEnergy = row.getInt("maxEnergy");
		this._hitRating = row.getInt("hitRating");
		this._critRating = row.getInt("critRating");
		this._dodgeRating = row.getInt("dodgeRating");
		this._resilienceRating = row.getInt("resilienceRating");
		this._critMultiple = row.getInt("critMultiple");
		this._faintResistRating = row.getInt("faintResistibility");
		this._critMultiple = row.getInt("critMultiple");
		this._cdReduce = row.getInt("cdReduce");
		this._hpAbsorb = row.getInt("hpAbsorb");
		this._defIgnore = row.getInt("defIgnore");
		this._hpRecovery = row.getInt("hpRecovery");
		this._shortRaAtkItr = template.shortRaAtkItr;
		this._longRaAtkItr = template.longRaAtkItr;
		this._shortRaAtkDist = template.shortRaAtkDist;
		this._longRaAtkDist = template.longRaAtkDist;
		this._moveSpeedX = template.moveSpeedX;
		this._moveSpeedY = template.moveSpeedY;
		this._battleMoveSpeedX = template.battleMoveSpeedX;
		this._battleMoveSpeedY = template.battleMoveSpeedY;
	}
	
	@Override
	public long getMaxHp() {
		return _maxHp;
	}

	@Override
	public int getMaxEnergy() {
		return _maxEnergy;
	}

	@Override
	public int getHpRecovery() {
		return _hpRecovery;
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
	public int getCritRating() {
		return _critRating;
	}

	@Override
	public int getDodgeRating() {
		return _dodgeRating;
	}

	@Override
	public int getHitRating() {
		return _hitRating;
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
	public int getMoveSpeedX() {
		return _moveSpeedX;
	}
	
	public int getMoveSpeedY() {
		return _moveSpeedY;
	}

	@Override
	public int getBattleMoveSpeedX() {
		return _battleMoveSpeedX;
	}
	
	@Override
	public int getBattleMoveSpeedY() {
		return _battleMoveSpeedY;
	}

	@Override
	public int getBlock() {
		// 格挡值不随着等级成长
		return 0;
	}

	@Override
	public int getCohesionDm() {
		// 聚力伤害不随着等级成长
		return 0;
	}

	@Override
	public int getBulletDm() {
		// 子弹伤害不随着等级成长
		return 0;
	}

}
