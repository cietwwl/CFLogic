package com.kola.kmp.logic.combat.skill;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.impl.KCombatManager;
import com.kola.kmp.logic.combat.support.IOperationRecorder;

/**
 * 
 * @author PERRY CHAN
 */
public class KSummonSkillParser implements ICombatSkillExecutionParser {

	private static final int INDEX_CD_TIME = 0;
	private static final int INDEX_MINION_TEMPLATE_ID = 1;
	private static final int INDEX_COUNT = 2;

	@Override
	public ICombatSkillExecution parse(int pSkillTemplateId, int skillLv, List<Integer> paras) {
		KSummonSkillExecution instance = new KSummonSkillExecution();
		instance._cdMillis = (int)TimeUnit.MILLISECONDS.convert(paras.get(INDEX_CD_TIME), TimeUnit.SECONDS);
		instance._skillTemplateId = pSkillTemplateId;
		instance._minionTemplateId = paras.get(INDEX_MINION_TEMPLATE_ID);
		instance._count = paras.get(INDEX_COUNT);
		KCombatManager.addSummonSkillInfo(pSkillTemplateId, skillLv, instance._minionTemplateId);
		return instance;
	}

	private static class KSummonSkillExecution implements ICombatSkillExecution {

		private int _cdMillis;
		private int _skillTemplateId;
		private int _minionTemplateId;
		private int _count;

		@Override
		public List<IOperationRecorder> execute(ICombat combat, ICombatMember operator, List<ICombatMember> targets, String useCode, long happenTime) {
			if (operator.getSkillActor().isNewSettle(this._skillTemplateId, useCode)) {
				operator.getSkillActor().summon(_minionTemplateId, _count, happenTime);
				operator.getSkillActor().recordSkillCoolDown(_skillTemplateId, happenTime, _cdMillis);
				operator.getSkillActor().recordSkillUse(_skillTemplateId, useCode, happenTime);
			}
			return null;
		}
	}

}
