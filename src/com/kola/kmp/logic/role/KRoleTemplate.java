package com.kola.kmp.logic.role;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KGameGender;
import com.kola.kmp.logic.other.KJobTypeEnum;

/**
 * 
 * @author PERRY CHAN
 */
public class KRoleTemplate /*implements ICharacterBattleAttr*/ {

	private static final String AUDIO_SEPARATOR = ",";
	
	/** 模板的唯一标识id */
	public final int templateId;
	/** 职业 */
	public final byte job;
	/** 性别 */
	public final KGameGender gender;
	/** 地图资源id */
	public final int inMapResId; 
	/** 头像资源id */
	public final int headResId; 
	/** 剧情使用的头像资源id */
	public final int animationHeadResId;
	/** 出生地图id */
	public final int bornMapId; 
	/** 出生x坐标 */
	public final int bornCorX; 
	/** 出生y坐标 */
	public final int bornCorY; 
	/** 初始等级 */
	public final int level;
	/** 近程攻击间隔，单位：毫秒 */
	public final int shortRaAtkItr; 
	/** 远程攻击间隔，单位：毫秒 */
	public final int longRaAtkItr; 
	/** 移动速度，单位：像素/秒 */
	public final int moveSpeedX;
	/** 移动速度，单位：像素/秒 */
	public final int moveSpeedY;
	/** 战场x移动速度，单位：像素/秒 */
	public final int battleMoveSpeedX;
	/** 战场y移动速度，单位：像素/秒 */
	public final int battleMoveSpeedY;
	/** 近程攻击距离 */
	public final int shortRaAtkDist;
	/** 远程攻击距离 */
	public final int longRaAtkDist;
	/** 攻击AI */
	public final String fightAI;
	/** 视野 */
	public final int vision;
	/** 近程攻击音效 */
	public final int[] normalAtkAudios;
//	/** 受击音效 */
//	public final int[] onHitAudios;
	/** 受击尖叫音效 */
	public final int[] injuryAudios;
	/** 死亡声 */
	public final int[] deadAudios;
	
	public KRoleTemplate(KGameExcelRow baseRow, KGameExcelRow attrRow) {
		this.templateId = baseRow.getInt("templateId");
		this.job = KJobTypeEnum.getJobByName(baseRow.getData("occupation")).getJobType();
		this.gender = KGameGender.getGender(baseRow.getInt("gender"));
		this.inMapResId = baseRow.getInt("inMapResId");
		this.headResId = baseRow.getInt("headResId");
		this.animationHeadResId = baseRow.getInt("animationHeadResId");
		this.bornMapId = baseRow.getInt("mapId");
		this.bornCorX = baseRow.getInt("x");
		this.bornCorY = baseRow.getInt("y");
		this.level = attrRow.getInt("level");
//		this.atk = attrRow.getInt("atk");
//		this.def = attrRow.getInt("def");
//		this.maxHp = attrRow.getInt("maxHp");
//		this.maxEnergy = attrRow.getInt("maxEnergy");
//		this.hitRating = attrRow.getInt("hitRating");
//		this.critRating = attrRow.getInt("critRating");
//		this.dodgeRating = attrRow.getInt("dodgeRating");
//		this.resilienceRating = attrRow.getInt("resilienceRating");
//		this.critMultiple = attrRow.getInt("critMultiple");
//		this.cdReduce = attrRow.getInt("cdReduce");
//		this.hpAbsorb = attrRow.getInt("hpAbsorb");
//		this.defIgnore = attrRow.getInt("defIgnore");
//		this.faintResistRating = attrRow.getInt("faintResistibility");
//		this.hpRecovery = attrRow.getInt("hpRecovery");
		this.shortRaAtkItr = (int) attrRow.getFloat("shortRaAtkItr") * 1000; // 策划填的数据是秒，需要转化为毫秒
		this.longRaAtkItr = (int) attrRow.getFloat("longRaAtkItr") * 1000; // 策划填的数据是秒，需要转化为毫秒
		this.moveSpeedX = attrRow.getInt("moveSpeedX");
		this.moveSpeedY = attrRow.getInt("moveSpeedY");
		this.battleMoveSpeedX = attrRow.getInt("battleMoveSpeedX");
		this.battleMoveSpeedY = attrRow.getInt("battleMoveSpeedY");
		this.shortRaAtkDist = attrRow.getInt("shortRaAtkDist");
		this.longRaAtkDist = attrRow.getInt("longRaAtkDist");
		this.fightAI = baseRow.getData("fightAI");
		this.vision = baseRow.getInt("vision");
		this.normalAtkAudios = UtilTool.getStringToIntArray(baseRow.getData("normal_atk_audios"), AUDIO_SEPARATOR);
//		this.onHitAudios = UtilTool.getStringToIntArray(baseRow.getData("hitted_audios"), AUDIO_SEPARATOR);
		this.injuryAudios = UtilTool.getStringToIntArray(baseRow.getData("hitted_scream_audios"), AUDIO_SEPARATOR);
		this.deadAudios = UtilTool.getStringToIntArray(baseRow.getData("dead_audio"), AUDIO_SEPARATOR);
	}

//	@Override
//	public int getMaxHp() {
//		return maxHp;
//	}
//
//	@Override
//	public int getMaxEnergy() {
//		return maxEnergy;
//	}
//
//	@Override
//	public int getHpRecovery() {
//		return hpRecovery;
//	}
//
//	@Override
//	public int getAtk() {
//		return atk;
//	}
//
//	@Override
//	public int getDef() {
//		return def;
//	}
//
//	@Override
//	public int getCritRating() {
//		return critRating;
//	}
//
//	@Override
//	public int getDodgeRating() {
//		return dodgeRating;
//	}
//
//	@Override
//	public int getHitRating() {
//		return hitRating;
//	}
//
//	@Override
//	public int getResilienceRating() {
//		return resilienceRating;
//	}
//	
//	@Override
//	public int getFaintResistRating() {
//		return faintResistRating;
//	}
//
//	@Override
//	public int getCritMultiple() {
//		return critMultiple;
//	}
//
//	@Override
//	public int getCdReduce() {
//		return cdReduce;
//	}
//
//	@Override
//	public int getHpAbsorb() {
//		return hpAbsorb;
//	}
//
//	@Override
//	public int getDefIgnore() {
//		return defIgnore;
//	}
//
//	@Override
//	public int getShortRaAtkItr() {
//		return shortRaAtkItr;
//	}
//
//	@Override
//	public int getLongRaAtkItr() {
//		return longRaAtkItr;
//	}
//
//	@Override
//	public int getShortRaAtkDist() {
//		return shortRaAtkDist;
//	}
//
//	@Override
//	public int getLongRaAtkDist() {
//		return longRaAtkDist;
//	}
//
//	@Override
//	public int getMoveSpeed() {
//		return moveSpeedX;
//	}
//	
//	@Override
//	public int getBattleMoveSpeedX() {
//		return battleMoveSpeedX;
//	}
//	
//	@Override
//	public int getBattleMoveSpeedY() {
//		return battleMoveSpeedY;
//	}
}
