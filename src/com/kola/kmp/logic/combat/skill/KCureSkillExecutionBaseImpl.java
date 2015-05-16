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
public abstract class KCureSkillExecutionBaseImpl implements ICombatSkillExecution {

	protected static final int INDEX_CD_MILLIS = 0;
	protected static final int INDEX_CURE_VALUE = 1;
	protected static final int INDEX_CURE_PCT = 2;
	protected static final int INDEX_SETTLE_TIMES = 3;

	protected int skillTemplateId;
	protected int cureValue;
	protected int curePct;
	protected int cdMillis;
	protected int settleTimes;
	
	protected void baseParse(int pSkillTemplateId, List<Integer> paras) {
		this.skillTemplateId = pSkillTemplateId;
		this.cdMillis = (int)TimeUnit.MILLISECONDS.convert(paras.get(INDEX_CD_MILLIS), TimeUnit.SECONDS);
		this.cureValue = paras.get(INDEX_CURE_VALUE);
		this.curePct = paras.get(INDEX_CURE_PCT);
		this.settleTimes = paras.get(INDEX_SETTLE_TIMES);
	}
	
	protected List<IOperationRecorder> baseExecute(ICombat combat, ICombatMember operator, List<ICombatMember> targets, long happenTime, String useCode) {
//		List<IOperationRecorder> resultList = new ArrayList<IOperationRecorder>();
//		int settleCount = operator.getSkillActor().getSkillSettleCount(skillTemplateId, useCode);
//		if (settleTimes > settleCount) {
//			ICombatMember target;
//			for (int i = 0; i < targets.size(); i++) {
//				target = targets.get(i);
//				if (target.isAlive()) {
//					resultList.add(KCombatCalculateSupport.doCure(combat, operator, target, curePct, cureValue));
//				}
//			}
//			if (settleCount == 0) {
//				operator.getSkillActor().recordSkillCoolDown(skillTemplateId, happenTime, cdMillis);
//			}
//			operator.getSkillActor().recordSkillUsed(skillTemplateId, useCode, happenTime);
//		}
//		return resultList;
		List<IOperationRecorder> resultList = new ArrayList<IOperationRecorder>();
//		int settleCount = operator.getSkillActor().getSkillSettleCount(skillTemplateId, useCode);
//		if (settleCount < this.settleTimes || operator.getSkillActor().isTimeInSettleRecord(skillTemplateId, useCode, happenTime)) {
			boolean isNew = operator.getSkillActor().isNewSettle(skillTemplateId, useCode);
			ICombatMember target;
			boolean canSettle;
			int settleTargetCount = 0;
			for (int i = 0; i < targets.size(); i++) {
				target = targets.get(i);
				if (target.isAlive()) {
					canSettle = operator.getSkillActor().getTargetSettleCount(skillTemplateId, useCode, target.getShadowId(), happenTime) < this.settleTimes;
					if (canSettle) {
						resultList.add(KCombatCalculateSupport.doCure(combat, operator, target, curePct, cureValue));
						operator.getSkillActor().recordSkillUsed(skillTemplateId, target.getShadowId(), useCode, happenTime);
						settleTargetCount++;
					}
				}
			}
			if (isNew) {
				operator.getSkillActor().recordSkillCoolDown(skillTemplateId, happenTime, cdMillis);
				if(settleTargetCount == 0) {
					// 由于useCode需要在至少一个目标被结算才会替换，所以如果正好上面没有目标被结算，而这里又记录了cd时间
					// 那么下次再结算的时候，就会出现cd时间未到的问题
					// 所以这里，如果没有目标被结算，就记录一下，目的就是替换成新的userCode
					operator.getSkillActor().recordSkillUse(skillTemplateId, useCode, happenTime);
				}
			}
//		}
		return resultList;
	}
}
