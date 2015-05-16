package com.kola.kmp.logic.npc;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.combat.api.ICombatMonster;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.combat.api.ICombatSkillSupport;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.util.KGameUtilTool;

/**
 * 
 * @author PERRY CHAN
 */
public class KGameMonster implements ICombatMonster, ICombatSkillSupport {
	
	private static final AtomicLong _idGenerator = new AtomicLong();
	private KMonstTemplate _template;
	private long _id;
	private int _atkItr;
	private int[] _atkAudioResIds;
	private int[] _onHitAudioResIds;
	private int[] _onHitScreamAudioResIds;
	private int[] _deadAudioResIds;
	
	public KGameMonster(KMonstTemplate pTemplate) {
		this._template = pTemplate;
		this._id = _idGenerator.incrementAndGet();
		this._atkItr = UtilTool.timeConvert(_template.atk_period, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
		_atkAudioResIds = new int[]{_template.monstUIData.attack_audios};
		_onHitAudioResIds = new int[]{_template.monstUIData.hitted_audios};
		_onHitScreamAudioResIds = new int[]{_template.monstUIData.hitted_scream_audios};
		_deadAudioResIds = new int[]{_template.monstUIData.dead_audio};
	}
	
	@Override
	public boolean canBeAttack() {
		return true;
	}
	
	@Override
	public byte getObjectType() {
		switch (this._template.getMonsterTypeEnum()) {
		case ELITIST:
			return OBJECT_TYPE_MONSTER_ELITIST;
		case BOSS:
			return OBJECT_TYPE_MONSTER_BOSS;
		case COMMON:
		default:
			return OBJECT_TYPE_MONSTER;
		}
	}
	
	@Override
	public int getTemplateId() {
		return _template.id;
	}

	@Override
	public long getId() {
		return _id;
	}

	@Override
	public String getName() {
		return _template.name;
	}

	@Override
	public int getHeadResId() {
		return _template.monstUIData.monster_head;
	}

	@Override
	public int getInMapResId() {
		return _template.monstUIData.res_id;
	}

	@Override
	public int getLevel() {
		return _template.lvl;
	}

	@Override
	public long getCurrentHp() {
		return KGameUtilTool.getAttrValueSafelyL(_template.allEffects, KGameAttrType.MAX_HP);
	}

	@Override
	public long getMaxHp() {
		return KGameUtilTool.getAttrValueSafelyL(_template.allEffects, KGameAttrType.MAX_HP);
		
	}

	@Override
	public int getBattleMoveSpeedX() {
		return _template.walk_speed;
	}
	
	@Override
	public int getBattleMoveSpeedY() {
		return _template.walk_speed;
	}
	
	@Override
	public int getVision() {
		return _template.vision;
	}

	@Override
	public int getAtk() {
		return (int)KGameUtilTool.getAttrValueSafelyL(_template.allEffects, KGameAttrType.ATK);
	}

	@Override
	public int getDef() {
		return (int)KGameUtilTool.getAttrValueSafelyL(_template.allEffects, KGameAttrType.DEF);
	}

	@Override
	public int getHitRating() {
		return (int)KGameUtilTool.getAttrValueSafelyL(_template.allEffects, KGameAttrType.HIT_RATING);
	}

	@Override
	public int getDodgeRating() {
		return (int)KGameUtilTool.getAttrValueSafelyL(_template.allEffects, KGameAttrType.DODGE_RATING);
	}

	@Override
	public int getCritRating() {
		return (int)KGameUtilTool.getAttrValueSafelyL(_template.allEffects, KGameAttrType.CRIT_RATING);
	}
	
	@Override
	public int getFaintResistRating() {
		return (int)KGameUtilTool.getAttrValueSafelyL(_template.allEffects, KGameAttrType.FAINT_RESIST_RATING);
	}

	@Override
	public int getCritMultiple() {
		return (int)KGameUtilTool.getAttrValueSafelyL(_template.allEffects, KGameAttrType.CRIT_MULTIPLE);
	}

	@Override
	public int getCdReduce() {
		return (int)KGameUtilTool.getAttrValueSafelyL(_template.allEffects, KGameAttrType.CD_REDUCE);
	}

	@Override
	public int getHpAbsorb() {
		return (int)KGameUtilTool.getAttrValueSafelyL(_template.allEffects, KGameAttrType.HP_ABSORB);
	}

	@Override
	public int getDefIgnore() {
		return (int)KGameUtilTool.getAttrValueSafelyL(_template.allEffects, KGameAttrType.DEF_IGNORE);
	}

	@Override
	public int getResilienceRating() {
		return (int)KGameUtilTool.getAttrValueSafelyL(_template.allEffects, KGameAttrType.RESILIENCE_RATING);
	}

	@Override
	public int getShortRaAtkItr() {
		return _atkItr;
	}

	@Override
	public int getLongRaAtkItr() {
		return _atkItr;
	}

	@Override
	public int getShortRaAtkDist() {
		return _template.monstUIData.att_range;
	}

	@Override
	public int getLongRaAtkDist() {
		return _template.monstUIData.att_range;
	}
	
	@Override
	public int getAtkCountPerTime() {
		return _template.monstUIData.atkCountPerTime;
	}

	@Override
	public List<Integer> getDropId() {
		return Collections.emptyList();
	}
	
	@Override
	public int getKilledEnergy() {
		return _template.per_anger;
	}

	@Override
	public int getColor() {
		return 0;
	}
	
	@Override
	public String getAIId() {
		return _template.aiid;
	}
	
	@Override
	public int[] getNormalAtkAudioResIdArray() {
		return _atkAudioResIds;
	}

	@Override
	public int[] getOnHitAudioResIdArray() {
		return _onHitAudioResIds;
	}

	@Override
	public int[] getInjuryAudioResIdArray() {
		return _onHitScreamAudioResIds;
	}

	@Override
	public int[] getDeadAudioResIdArray() {
		return _deadAudioResIds;
	}
	
	@Override
	public ICombatSkillSupport getCombatSkillSupport() {
		return this;
	}

	@Override
	public List<ICombatSkillData> getUsableSkills() {
		return _template.combatSkills;
	}

	@Override
	public List<ICombatSkillData> getPassiveSkills() {
		return Collections.emptyList();
	}
	
	@Override
	public boolean isFullImmunity() {
		return _template.isFullImmunity();
	}
	
	@Override
	public int getFullImmunityDuration() {
		return _template.armortime;
	}
	
	@Override
	public int getFullImmunityIteration() {
		return _template.armorinterval;
	}

}
