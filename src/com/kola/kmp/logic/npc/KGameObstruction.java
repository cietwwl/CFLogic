package com.kola.kmp.logic.npc;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.kola.kmp.logic.combat.api.ICombatBlock;
import com.kola.kmp.logic.npc.KNPCDataStructs.ObstructionTemplate;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KObstructionTargetType;
import com.kola.kmp.logic.util.KGameUtilTool;

/**
 * 
 * @author PERRY CHAN
 */
public class KGameObstruction implements ICombatBlock {

	private static final int[] EMPTY_AUDIO_ARRAY = new int[0]; // 长度为0的数组
	private static final AtomicLong _idGenerator = new AtomicLong();
	private ObstructionTemplate _template;
	private long _id;
	private boolean _canDestory;
	private KObstructionTargetType _targetType;
	
	public KGameObstruction(ObstructionTemplate pTemplate) {
		this._template = pTemplate;
		this._id = _idGenerator.incrementAndGet();
		this._canDestory = _template.destroy > 0;
		this._targetType = KObstructionTargetType.getEnum(_template.target);
	}
	
	@Override
	public boolean canBeAttack() {
		return _canDestory;
	}
	
	@Override
	public byte getObjectType() {
		return OBJECT_TYPE_BLOCK;
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
		return _template.headResId;
	}

	@Override
	public int getInMapResId() {
		return _template.inMapResId;
	}

	@Override
	public int getLevel() {
		return 0;
	}

	@Override
	public long getCurrentHp() {
		return KGameUtilTool.getAttrValueSafely(_template.allEffects, KGameAttrType.MAX_HP);
	}

	@Override
	public long getMaxHp() {
		return KGameUtilTool.getAttrValueSafely(_template.allEffects, KGameAttrType.MAX_HP);
	}

	@Override
	public int getBattleMoveSpeedX() {
		return 0;
	}
	
	@Override
	public int getBattleMoveSpeedY() {
		return 0;
	}
	
	public int getVision() {
		return 0;
	}

	@Override
	public int getAtk() {
		return KGameUtilTool.getAttrValueSafely(_template.allEffects, KGameAttrType.ATK);
	}

	@Override
	public int getDef() {
		return KGameUtilTool.getAttrValueSafely(_template.allEffects, KGameAttrType.DEF);
	}

	@Override
	public int getHitRating() {
		return KGameUtilTool.getAttrValueSafely(_template.allEffects, KGameAttrType.HIT_RATING);
	}

	@Override
	public int getDodgeRating() {
		return KGameUtilTool.getAttrValueSafely(_template.allEffects, KGameAttrType.DODGE_RATING);
	}

	@Override
	public int getCritRating() {
		return KGameUtilTool.getAttrValueSafely(_template.allEffects, KGameAttrType.CRIT_RATING);
	}
	
	@Override
	public int getFaintResistRating() {
		return 0;
	}

	@Override
	public int getCritMultiple() {
		return 0;
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
		return KGameUtilTool.getAttrValueSafely(_template.allEffects, KGameAttrType.FAINT_RESIST_RATING);
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
	public int[] getNormalAtkAudioResIdArray() {
		return _template.attack_audios_array;
	}

	@Override
	public int[] getOnHitAudioResIdArray() {
		return _template.hitted_audios_array;
	}

	@Override
	public int[] getInjuryAudioResIdArray() {
		return _template.hitted_scream_audios_array;
	}

	@Override
	public int[] getDeadAudioResIdArray() {
		return _template.dead_audio_array;
	}

	@Override
	public KObstructionTargetType getTargetType() {
		return _targetType;
	}
	
	@Override
	public List<Integer> getDropId() {
		return _template.getRandomDropId();
	}
	
	@Override
	public int getStateIdDuringAlive() {
		return _template.directstatus_id;
	}
	
	@Override
	public int getStateIdAfterDestoryed() {
		return _template.destroystatus_id;
	}
	
	@Override
	public int getAppearRate() {
		return _template.probability;
	}
}
