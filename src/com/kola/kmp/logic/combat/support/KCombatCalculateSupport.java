package com.kola.kmp.logic.combat.support;

import java.util.Random;

import org.slf4j.Logger;

import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.KGameGlobalConfig;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatEvent;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.impl.KCombatConfig;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatCalculateSupport {

	private static final Logger _LOGGER = ICombat.LOGGER;
	private static final int TEN_THOUSAND = UtilTool.TEN_THOUSAND_RATIO_UNIT;
	private static final int HUNDREN = UtilTool.HUNDRED_RATIO_UNIT;
	private static final int MIN_DM = 1;
	
	/**
	 * 
	 * 判断是否命中
	 * 
	 * @param combat
	 * @param attacker
	 * @param defender
	 * @return
	 */
	private static boolean validateHit(Random ran, ICombatMember attacker, ICombatMember defender) {
		boolean hit = false;
		int finalHitRating = attacker.getHitRating() - defender.getDodgeRating() - KGameGlobalConfig.getBasicHitRating();
//		if (finalHitRating < KCombatConfig.getLowHitRating()) {
//			finalHitRating = Math.min(Math.max(UtilTool.round(TEN_THOUSAND + finalHitRating / KCombatConfig.getLowHitRatingPara()), 0), KCombatConfig.getLowHitRating());
//		}
//		int randomValue = ran.nextInt(TEN_THOUSAND);
//		if(randomValue < finalHitRating) {
//			hit = true;
//		}
		if (finalHitRating > 0) {
			finalHitRating = Math.round(TEN_THOUSAND * KCombatConfig.getHitRatingBasicPara() + KCombatConfig.getLowHitRatingCalPara() * finalHitRating
					/ (KCombatConfig.getHitRatingFixPara() + finalHitRating));
		} else {
			finalHitRating = Math.round(TEN_THOUSAND
					* (KCombatConfig.getHitRatingBasicPara() - KCombatConfig.getHighHitRatingCalPara() * (float)(-finalHitRating) / (KCombatConfig.getHitRatingFixPara() - finalHitRating)));
		}
		int randomValue = ran.nextInt(TEN_THOUSAND);
		if(randomValue < finalHitRating) {
			hit = true;
		}
		return hit;
	}
	
	/**
	 * 
	 * 判断是否暴击
	 * 
	 * @param combat
	 * @param attacker
	 * @param defender
	 * @return
	 */
	private static boolean checkIfCrit(Random ran, ICombatMember attacker, ICombatMember defender) {
		int finalCritRating = Math.max((attacker.getCritRating() - defender.getResilienceRating()), 0);
		int randomValue = ran.nextInt(TEN_THOUSAND);
		if(randomValue < finalCritRating) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * 计算伤害
	 * 
	 * @param originalDm
	 * @param percentage
	 * @param critPara
	 * @return
	 */
	private static int calculateDm(int originalDm, int percentage, float critPara) {
		if (critPara > 1) {
			return (int) Math.ceil(originalDm * (float) percentage / HUNDREN * critPara);
		} else {
			return (int) Math.ceil(originalDm * (float) percentage / HUNDREN);
		}
	}
	
	private static boolean canRecoverHp(ICombat combat, ICombatMember attacker) {
		return false;
	}
	
	private static int calculateRecoveryQuantity(ICombat combat, ICombatMember attacker, int dm) {
		return 0;
	}
	
	/**
	 * 
	 * 执行攻击计算
	 * 
	 * @param combat
	 * @param attacker
	 * @param defender
	 * @param pct
	 * @param add
	 * @return
	 */
	private static IOperationRecorder doAttack(ICombat combat, ICombatMember attacker, ICombatMember defender, int pct, int add, long happenTime, boolean isSkillAttack) {
		KNormalOperationRecorder recorder = new KNormalOperationRecorder();
		recorder.targetId = defender.getShadowId();
		boolean allowAttack = defender.isAlive() && defender.canBeAttacked() && attacker.isInDiffForce(defender);
		boolean hit = false;
		if (allowAttack) {
			hit = validateHit(combat.getCombatRandomInstance(), attacker, defender);
		} else {
			if (!defender.isAlive()) {
				// 计算一下连击次数
				attacker.getCombatRecorder().recordAttack(happenTime);
			}
		}
		if (hit) {
			// defender 仍然生存的情况下才执行伤害
			// 计算流程：是否命中-->是否暴击-->计算伤害
			KDmResult result = calculateDm(attacker, defender, combat.getCombatRandomInstance(), pct, add, isSkillAttack);
			int dm = result.dm;
			long preHp = defender.getCurrentHp();
			defender.decreaseHp(dm, happenTime);
			if (defender.isAlive()) {
				attacker.getCombatRecorder().recordDm(dm);
			} else {
				attacker.getCombatRecorder().recordKillMember(defender);
				if (dm > preHp) {
					attacker.getCombatRecorder().recordDm(preHp);
				} else {
					attacker.getCombatRecorder().recordDm(dm);
				}
			}
			if (canRecoverHp(combat, attacker)) {
				int recoveryCount = calculateRecoveryQuantity(combat, attacker, dm);
				attacker.increaseHp(recoveryCount);
				attacker.getCombatRecorder().recordHpRecovery(recoveryCount, attacker.getCurrentHp());
			}
			recorder.dm = dm;
			recorder.crit = result.crit;
			recorder.hit = true;
			attacker.getCombatRecorder().recordLastTarget(defender);
			attacker.getCombatRecorder().recordLastAttackType(isSkillAttack);
			attacker.combatEventNotify(ICombatEvent.EVENT_AFTER_ATTACK, happenTime);
			defender.combatEventNotify(ICombatEvent.EVENT_UNDER_ATTACK, happenTime);
			if (result.recordBeHit) {
				// 2014-09-29 添加：机甲不算攻击
				defender.getCombatRecorder().recordBeHit(happenTime);
			}
			attacker.getCombatRecorder().recordAttack(happenTime);
		}
//		_LOGGER.warn("攻击者：[{},{}]，受击者：[{},{},{}]，是否命中：{}，是否暴击：{}，伤害：{}", attacker.getName(), attacker.getShadowId(), defender.getName(), defender.getShadowId(), defender.getCurrentHp(), recorder.hit,
//				recorder.crit, recorder.dm);
//		if(attacker.getMemberType() == ICombatMember.MEMBER_TYPE_VEHICLE) {
//			_LOGGER.warn("攻击者：[{},{}]，受击者：[{},{},{}]，是否命中：{}，是否暴击：{}，伤害：{}", attacker.getName(), attacker.getShadowId(), defender.getName(), defender.getShadowId(), defender.getCurrentHp(),
//					recorder.hit, recorder.crit, recorder.dm);
//		}
		return recorder;
	}
	
	/**
	 * 
	 * 计算暴击伤害倍数
	 * 
	 * @param attacker
	 * @param defender
	 * @return
	 */
	public static float calculateCritPercentage(ICombatMember attacker, ICombatMember defender) {
		int critMultiple = attacker.getCritMultiple() + (attacker.getCritRating() - defender.getResilienceRating());
		critMultiple = Math.min(critMultiple, KCombatConfig.getMaxCritMultiple());
		return (float) critMultiple / TEN_THOUSAND;
	}
	
	public static KDmResult calculateDm(ICombatMember attacker, ICombatMember defender, Random ran, int pct, int add, boolean isSkillAtk) {
		int dm = 0;
		boolean crit = false;
		if (attacker.isAtkByPercentage()) {
			dm = (int) UtilTool.calculateTenThousandRatioL(defender.getMaxHp(), attacker.getAtk());
		} else {
			defender.getCombatRecorder().recordLastAttacker(attacker);
			crit = checkIfCrit(ran, attacker, defender);
			int atk = attacker.getAtk();
			if (pct > 0) {
				// 攻击力百分比>0，先算出攻击力
				if(isSkillAtk) {
					pct += UtilTool.calculateTenThousandRatio(pct, attacker.getSkillDmPctInc());
				}
				atk = UtilTool.calculateTenThousandRatio(atk, pct);
			} else if (attacker.getSecondWeaponDmPct() > 0) {
				// 处理副武器的聚力伤害比例
				atk = UtilTool.calculateTenThousandRatio(atk, attacker.getSecondWeaponDmPct());
//				_LOGGER.info("副武器伤害加成：{}，attacker：{}", attacker.getSecondWeaponDmPct(), attacker.getName());
			}
			int lastDef = defender.getDef();
			if (attacker.getDefIgnore() > 0) {
				int defIgnore = attacker.getDefIgnore() > TEN_THOUSAND ? TEN_THOUSAND : attacker.getDefIgnore();
				lastDef = Math.round(lastDef * (1 - (float) defIgnore / TEN_THOUSAND));
			}
			dm = atk - lastDef;
			if (attacker.getSecondWeaponFixedDm() > 0) {
				dm += attacker.getSecondWeaponFixedDm(); // 加上副武器固定伤害加成
			}
			if (attacker.getAtkCountPerTime() > 1) {
				dm = Math.max(dm / attacker.getAtkCountPerTime(), MIN_DM); // 攻击力+无视防御-防御力
			} else {
				dm = Math.max(dm, MIN_DM); // 攻击力+无视防御-防御力
			}
			float critPara = 1;
			if (crit) {
				critPara = calculateCritPercentage(attacker, defender); // 计算暴击参数
			}
			int dmPct = ran.nextInt(KCombatConfig.getNormalAtkDmSubPercentage()) + KCombatConfig.getNormalAtkDmMinPercentage();
			dm = calculateDm(dm, dmPct, critPara) + add;
			if (defender.getDmReducePct() > 0) {
				dm = (int) Math.ceil(dm * (1 - (float) defender.getDmReducePct() / TEN_THOUSAND));
			}
			dmPct = ran.nextInt(KCombatConfig.getNormalAtkMinDmFormularPctMax()) + KCombatConfig.getNormalAtkMinDmFormularPctMin();
			int minDm = calculateDm(atk, dmPct, critPara) + add; // 计算最小伤害
			if(dm < minDm) {
				dm = minDm;
			}
		}
//		boolean recordBeHit = defender.getMemberType() != ICombatMember.MEMBER_TYPE_VEHICLE; // 坐骑不记录受击
		boolean recordBeHit = true;
		if (defender.getDmBlock() > 0 && dm > 0) {
			// 处理第二武器格挡
			dm = Math.max(dm - defender.getDmBlock(), 0);
//			_LOGGER.info("格挡伤害减免：{}，defender：{}", defender.getDmBlock(), defender.getName());
			if(dm <= 0) {
				recordBeHit = false;
			}
		}
		return new KDmResult(crit, dm, recordBeHit);
	}
	
	public static IOperationRecorder doNormalAttack(ICombat combat, ICombatMember attacker, ICombatMember defender, long happenTime) {
		IOperationRecorder recorder = doAttack(combat, attacker, defender, 0, 0, happenTime, false);
		defender.getCombatRecorder().recordUnderAttack(attacker.getShadowId(), 0, 0, recorder.isHit(), recorder.isCrit(), recorder.getDm(), false);
		defender.getCombatRecorder().recordBeNormalAttackTime(attacker.getShadowId(), happenTime);
		return recorder;
	}
	
	public static IOperationRecorder doSkillAttack(ICombat combat, ICombatMember attacker, ICombatMember defender, int pct, int add, long happenTime) {
		IOperationRecorder recorder =  doAttack(combat, attacker, defender, pct, add, happenTime, true);
		defender.getCombatRecorder().recordUnderAttack(attacker.getShadowId(), pct, add, recorder.isHit(), recorder.isCrit(), recorder.getDm(), true);
		return recorder;
	}
	
	public static IOperationRecorder doCure(ICombat combat, ICombatMember operator, ICombatMember target, int pct, int add) {
		KNormalOperationRecorder recorder = new KNormalOperationRecorder();
		recorder.targetId = target.getShadowId();
		recorder.isDamage = false;
		recorder.crit = true;
		if (target.isAlive()) {
//			float critPara = 1.0f;
			/*int cureValue = calculateDm(operator.getAtk(), pct, critPara);*/
			int cureValue = add;
			if (pct > 0) {
				cureValue += UtilTool.calculateTenThousandRatioL(operator.getMaxHp(), pct);
			}
			if(target.isAlive()) {
				target.increaseHp(cureValue);
				recorder.dm = cureValue;
			}
		}
		return recorder;
	}
	
//	public static int calculateProduceEnergy(int ctnAtkCount) {
//		return ctnAtkCount / KCombatConfig.getEnergyCalPara();
//	}
	
	public static class KDmResult {
		public final boolean crit;
		public final int dm;
		public final boolean recordBeHit; // 是否记录受击
		
		private KDmResult(boolean pCrit, int pDm, boolean pRecordBeHit) {
			this.crit = pCrit;
			this.dm = pDm;
			this.recordBeHit = pRecordBeHit;
		}
	}
	
	static class KNormalOperationRecorder implements IOperationRecorder {

		short targetId;
		boolean isDamage;
		boolean hit;
		boolean crit;
		int dm;
		
		KNormalOperationRecorder() {
			this.isDamage = true;
			this.hit = false;
			this.crit = false;
			this.dm = 0;
		}
		
		@Override
		public short getTargetId() {
			return targetId;
		}
		
		@Override
		public boolean isDamage() {
			return isDamage;
		}
		
		@Override
		public boolean isHit() {
			return hit;
		}

		@Override
		public boolean isCrit() {
			return crit;
		}

		@Override
		public int getDm() {
			return dm;
		}
		
	}
}
