package com.kola.kmp.logic.combat.skill;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.support.IOperationRecorder;
import com.kola.kmp.logic.combat.support.KCombatCalculateSupport;

/**
 * 
 * @author PERRY CHAN
 */
public abstract class KDmSkillExecutionBaseImpl implements ICombatSkillExecution {

	protected static final int INDEX_DM_TIMES = 0;
	protected static final int INDEX_DM_PCT = 1;
	protected static final int INDEX_CD_MILLIS = 2;

	protected int skillTemplateId;
	protected int dmTimes;
	protected int dmPct;
	protected int cdMillis;
	
	protected void baseParse(int pSkillTemplateId, List<Integer> paras) {
		this.skillTemplateId = pSkillTemplateId;
		this.dmTimes = paras.get(INDEX_DM_TIMES);
		this.dmPct = paras.get(INDEX_DM_PCT);
		this.cdMillis = (int)TimeUnit.MILLISECONDS.convert(paras.get(INDEX_CD_MILLIS), TimeUnit.SECONDS);
	}
	
	protected List<IOperationRecorder> baseExecute(ICombat combat, ICombatMember operator, List<ICombatMember> targets, String useCode, long happenTime) {
//		List<IOperationRecorder> resultList = new ArrayList<IOperationRecorder>();
//		boolean settleBefore = operator.getSkillActor().isTimeHasBeenSettle(skillTemplateId, useCode, happenTime);
//		int settleCount = operator.getSkillActor().getSkillSettleCount(skillTemplateId, useCode);
//		boolean canSettle = false;
//		if (settleBefore) {
////			LOGGER.info("operator：{}，技能id：{}，但是结算时间（{}）曾经被结算过，本次直接结算！", operator.getName(), skillTemplateId, happenTime);
//			canSettle = true;
//		} else {
//			if (dmTimes > settleCount) {
//				canSettle = true;
//			} else {
//				canSettle = settleBefore;
//			}
//		}
//		if (canSettle) {
//			ICombatMember target;
//			for (int i = 0; i < targets.size(); i++) {
//				target = targets.get(i);
//				if (target.isAlive()) {
//					resultList.add(KCombatCalculateSupport.doSkillAttack(combat, operator, target, dmPct, 0, happenTime));
//				} else {
//					operator.getCombatRecorder().recordAttack(happenTime); // 计算一次连击
//				}
//			}
//			if (settleCount == 0) {
//				operator.getSkillActor().recordSkillCoolDown(skillTemplateId, happenTime, cdMillis);
//			}
//			if (!settleBefore) {
//				operator.getSkillActor().recordSkillUsed(skillTemplateId, useCode, happenTime);
//			}
//		} else {
//			LOGGER.error("技能id={}，useCode={}，happenTime={}，dmTimes={}，settleCount={}", skillTemplateId, useCode, happenTime, dmTimes, settleCount);
//		}
		
		List<IOperationRecorder> resultList = new ArrayList<IOperationRecorder>();
//		int settleCount = operator.getSkillActor().getSkillSettleCount(skillTemplateId, useCode);
//		if (settleCount < this.dmTimes || operator.getSkillActor().isTimeInSettleRecord(skillTemplateId, useCode, happenTime)) {
			ICombatMember target;
			boolean canSettle;
			boolean firstSettle = operator.getSkillActor().isNewSettle(skillTemplateId, useCode);
			int settleTargetCount = 0;
			for (int i = 0; i < targets.size(); i++) {
				target = targets.get(i);
				if (target.isAlive()) {
					canSettle = operator.getSkillActor().getTargetSettleCount(skillTemplateId, useCode, target.getShadowId(), happenTime) < this.dmTimes;
					if (canSettle) {
						resultList.add(KCombatCalculateSupport.doSkillAttack(combat, operator, target, dmPct, 0, happenTime));
						operator.getSkillActor().recordSkillUsed(skillTemplateId, target.getShadowId(), useCode, happenTime);
//						LOGGER.info("技能正常结算：target:{}, skillTemplateId:{}, useCode:{}, happenTime:{}", target.getShadowId(), skillTemplateId, useCode, happenTime);
						settleTargetCount++;
					} else {
						LOGGER.warn("operator:[{},{}], target:{}, skillTemplateId:{}, useCode:{}, happenTime:{}, duplicate settle!", operator.getName(), operator.getShadowId(), target.getShadowId(),
								skillTemplateId, useCode, happenTime);
					}
				} else {
					operator.getCombatRecorder().recordAttack(happenTime); // 计算一次连击
				}
			}
			if (firstSettle) {
				operator.getSkillActor().recordSkillCoolDown(skillTemplateId, happenTime, cdMillis);
				if(settleTargetCount == 0) {
					// 由于useCode需要在至少一个目标被结算才会替换，所以如果正好上面没有目标被结算，而这里又记录了cd时间
					// 那么下次再结算的时候，就会出现cd时间未到的问题
					// 所以这里，如果没有目标被结算，就记录一下，目的就是替换成新的userCode
					operator.getSkillActor().recordSkillUse(skillTemplateId, useCode, happenTime);
				}
			}
//		} else {
//			LOGGER.error("operator:[{},{}], skillTemplateId:{}, useCode:{}, happenTime:{}, 超过最大结算次数！", operator.getName(), operator.getShadowId(), skillTemplateId, useCode, happenTime);
//		}
		return resultList;
	}
}
