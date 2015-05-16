package com.kola.kmp.logic.combat.impl;

import java.util.Collections;
import java.util.List;

import com.koala.game.KGameMessage;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.combat.ICombatForce;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.ICombatMinion;
import com.kola.kmp.logic.combat.api.ICombatMinionTemplateData;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.combat.api.ICombatSkillSupport;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatMinion implements ICombatMinion, ICombatSkillSupport {

	private static final int[] EMPTY_AUDIO = new int[0];
//	private static final AtomicInteger _idGenerator = new AtomicInteger();
	private ICombatMinionTemplateData _template;
//	private int _serialId;
	private long _hp;
	private int _atk;
	private int _def;
	private int _hitRating;
	private int _dodgeRating;
	private int _critRating;
	private int _resilienceRating;
	private byte _forceType;
	private int _killEngery;
	private int _critMultiple;
	private int _dmReducePct;
	
	KCombatMinion(ICombatMinionTemplateData pTemplateData, ICombatMember master) {
//		_idGenerator.compareAndSet(Integer.MAX_VALUE, 0); // 如果已经是int的最大值，则重置一下
//		this._serialId = _idGenerator.incrementAndGet();
		this._template = pTemplateData;
		if(this._template.isGenerateByOwner()) {
			this._hp = UtilTool.calculateTenThousandRatioL(master.getMaxHp(), (int)_template.getMaxHp());
			this._atk = UtilTool.calculateTenThousandRatio(master.getAtk(), _template.getAtk());
			this._def = UtilTool.calculateTenThousandRatio(master.getDef(), _template.getDef());
			this._hitRating = UtilTool.calculateTenThousandRatio(master.getHitRating(), _template.getHitRating());
			this._dodgeRating = UtilTool.calculateTenThousandRatio(master.getDodgeRating(), _template.getDodgeRating());
			this._critRating = UtilTool.calculateTenThousandRatio(master.getCritRating(), _template.getCritRating());
			this._resilienceRating = UtilTool.calculateTenThousandRatio(master.getResilienceRating(), _template.getResilienceRating());
		} else {
			this._hp = this._template.getMaxHp();
			this._atk = this._template.getAtk();
			this._def = this._template.getDef();
			this._hitRating = this._template.getHitRating();
			this._dodgeRating = this._template.getDodgeRating();
			this._critRating = this._template.getCritRating();
			this._resilienceRating = this._template.getResilienceRating();
		}
		this._critMultiple = this._template.getCritMultiple();
		switch (this._template.getTargetType()) {
		case TARGET_ON_ALL:
		case TARGET_ON_NONE:
			_forceType = ICombatForce.FORCE_TYPE_NEUTRAL;
			break;
		case TARGET_ON_MONSTER:
			switch (master.getMemberType()) {
			case ICombatMember.MEMBER_TYPE_ROLE:
				_forceType = ICombatForce.FORCE_TYPE_ROLE_SIDE;
				break;
			default:
				// 如果主人的阵营是角色阵营，则召唤物的阵营也是角色阵营；否则，即使是以怪物为目标，也跟随主人的阵营，这里主要是竞技场的问题
				_forceType = master.getForceType();
				break;
			}
			break;
		case TARGET_ON_ROLE:
			_forceType = ICombatForce.FORCE_TYPE_MONSTER_SIDE;
			break;
		}
	}
	
	public void packDataToMsg(KGameMessage msg) {
		msg.writeInt(_template.getTemplateId());
		msg.writeUtf8String(this._template.getName());
		msg.writeShort(this._template.getLevel());
		msg.writeInt(this._template.getHeadResId());
		msg.writeInt(this._template.getInMapResId());
		msg.writeShort(this._template.getDuration());
		msg.writeInt(this._template.getAtkPeriod());
		msg.writeShort(this._template.getAtkRange());
		msg.writeBoolean(this._template.canBeAttack());
		msg.writeByte(this._template.getTargetType().sign);
		msg.writeBoolean(this._template.isFullImmunity());
		if(this._template.isFullImmunity()) {
			msg.writeInt(_template.getFullImmunityDuration());
			msg.writeInt(_template.getFullImmunityIteration());
		}
		msg.writeInt(this._template.getBattleMoveSpeedX());
		msg.writeInt(this._template.getBattleMoveSpeedY());
		msg.writeShort(this._template.getVision());
		msg.writeShort(this._killEngery);
//		msg.writeInt(this._hp);
		msg.writeLong(this._hp);
		msg.writeByte(this._template.getAtkCountPerTime());
		msg.writeInt(this._atk);
		msg.writeInt(this._def);
		msg.writeInt(this._hitRating);
		msg.writeInt(this._dodgeRating);
		msg.writeInt(this._critRating);
		msg.writeInt(this._resilienceRating);
		msg.writeInt(this._critMultiple);
		msg.writeInt(this._dmReducePct);
		msg.writeUtf8String(this._template.getAIId()); // AI编号
		List<ICombatSkillData> list = this._template.getAllSkills();
		ICombatSkillData skillData;
		msg.writeByte(list.size());
		for(int i = 0; i < list.size(); i++) {
			skillData = list.get(i);
			msg.writeInt(skillData.getSkillTemplateId());
			msg.writeByte(skillData.getLv());
		}
	}
	
	@Override
	public byte getForceType() {
		return _forceType;
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
		return 0;
	}

	@Override
	public int getHpAbsorb() {
		return 0;
	}

	@Override
	public int getDefIgnore() {
		return 0;
	}

	@Override
	public int getResilienceRating() {
		return _resilienceRating;
	}

	@Override
	public int getFaintResistRating() {
		return 0;
	}

	@Override
	public int getShortRaAtkItr() {
		return 0;
	}

	@Override
	public int getLongRaAtkItr() {
		return 0;
	}

	@Override
	public int getShortRaAtkDist() {
		return 0;
	}

	@Override
	public int getLongRaAtkDist() {
		return 0;
	}

	@Override
	public boolean canBeAttack() {
		return _template.canBeAttack();
	}

	@Override
	public byte getObjectType() {
		return _template.getObjectType();
	}

	@Override
	public int getTemplateId() {
		return _template.getTemplateId();
	}

	@Override
	public long getId() {
		return 0;
	}

	@Override
	public String getName() {
		return _template.getName();
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
	public int getLevel() {
		return _template.getLevel();
	}

	@Override
	public long getCurrentHp() {
		return _hp;
	}

	@Override
	public long getMaxHp() {
		return _hp;
	}

	@Override
	public int getBattleMoveSpeedX() {
		return _template.getBattleMoveSpeedX();
	}

	@Override
	public int getBattleMoveSpeedY() {
		return _template.getBattleMoveSpeedY();
	}

	@Override
	public int getVision() {
		return _template.getVision();
	}
	
	@Override
	public ICombatSkillSupport getSKillSupport() {
		return this;
	}
	
	@Override
	public int getDuration() {
		return _template.getDuration();
	}
	
	@Override
	public boolean isFullImmunity() {
		return _template.isFullImmunity();
	}
	
	@Override
	public int getFullImmunityDuration() {
		return _template.getFullImmunityDuration();
	}
	
	@Override
	public int getFullImmunityIteration() {
		return _template.getFullImmunityIteration();
	}
	
	@Override
	public List<ICombatSkillData> getPassiveSkills() {
		return Collections.emptyList();
	}

	@Override
	public List<ICombatSkillData> getUsableSkills() {
		return this._template.getAllSkills();
	}

	@Override
	public int[] getNormalAtkAudioResIdArray() {
		return EMPTY_AUDIO;
	}
	
	@Override
	public int[] getOnHitAudioResIdArray() {
		return EMPTY_AUDIO;
	}

	@Override
	public int[] getInjuryAudioResIdArray() {
		return EMPTY_AUDIO;
	}

	@Override
	public int[] getDeadAudioResIdArray() {
		return EMPTY_AUDIO;
	}

}
