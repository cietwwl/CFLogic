package com.kola.kmp.logic.combat.operation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.ICombatRecorder.IRecordOfAttack;
import com.kola.kmp.logic.combat.IOperationResult;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.combat.support.KCombatCalculateSupport;

/**
 * 
 * @author PERRY CHAN
 */
public class KClientClaimDeadOperation extends KOperationBaseImpl {

	private static final int _ALLOW_PCT_MISTAKE = 11; // 当客户端声称死亡的时候，允许的血量百分比差值。（不包含这个值，即血量是10%）
	private static final int _ALLOW_PCT_MISTAKE_BOSS = 11; // 当客户端声称死亡的时候，允许的血量百分比差值。（不包含这个值，即血量是6%）
	
	private short _shadowId;
	private boolean _selfExplose;
	private long _roleId;
	
	public KClientClaimDeadOperation(long roleId, long pOpTime, short pShadowId, boolean pSelfExplose) {
		super(pOpTime);
		this._shadowId = pShadowId;
		this._selfExplose = pSelfExplose;
		this._roleId = roleId;
	}

	private boolean check(ICombat combat, ICombatMember defender) {
		Map<Short, List<IRecordOfAttack>> history = defender.getCombatRecorder().getAttackRecord();
		if (history.size() > 0) {
			Map.Entry<Short, List<IRecordOfAttack>> temp;
			ICombatMember attacker;
			List<IRecordOfAttack> list;
			IRecordOfAttack tempRecord;
			int missHp = 0;
			for (Iterator<Map.Entry<Short, List<IRecordOfAttack>>> itr = history.entrySet().iterator(); itr.hasNext();) {
				temp = itr.next();
				attacker = combat.getCombatMember(temp.getKey());
				if (attacker == null) {
					LOGGER.error("客户端确认死亡，检查中，发现attacker不存在，attackerId={}，战场id：", temp.getKey(), combat.getSerialId());
					continue;
				}
				if(attacker.getAtk() - defender.getDef() > defender.getMaxHp()) {
//					LOGGER.info("客户端声称死亡，攻击历史中，attacker：{}，defender：{}，的攻击力（{}）与其防御力（{}）之差大于其最大血量：{}", attacker.getName(), defender.getName(), attacker.getAtk(), defender.getDef(), defender.getMaxHp());
					return true;
				} else {
					Random ran = new Random();
					list = temp.getValue();
					int critRating = attacker.getCritRating() - defender.getResilienceRating();
					int critTimes = Math.max(UtilTool.round((float) critRating / UtilTool.TEN_THOUSAND_RATIO_UNIT * list.size()), 1);
					List<IRecordOfAttack> notCritList = new ArrayList<IRecordOfAttack>();
					for (int k = 0; k < list.size(); k++) {
						tempRecord = list.get(k);
						if(!tempRecord.isHit()) {
							missHp += KCombatCalculateSupport.calculateDm(attacker, defender, ran, tempRecord.getPct(), tempRecord.getAdd(), tempRecord.isSkillAtk()).dm;
							continue;
						}
						if (tempRecord.isCrit()) {
							if(critTimes > 0) {
								critTimes--;
							}
						} else {
							notCritList.add(tempRecord);
						}
					}
					if(critTimes > 0 && notCritList.size() > 0) {
						int critHp = 0;
						float critPara = KCombatCalculateSupport.calculateCritPercentage(attacker, defender);
						for(int i = 0; i < notCritList.size(); i++) {
							tempRecord = notCritList.get(i);
							critHp += UtilTool.round(tempRecord.getDm() * critPara) - tempRecord.getDm();
							critTimes--;
							if(critTimes == 0) {	
								break;
							}
						}
						if(critHp > 0) {
							LOGGER.info("客户确认称死亡，攻击历史中，defender：({},{})，合理的暴击误差伤害：{}", defender.getName(), defender.getShadowId(), critHp);
							missHp += critHp;
						}
					}
				}
			}
			if(missHp > defender.getCurrentHp()) {
//				LOGGER.info("客户端声称死亡，攻击历史中，defender：({},{})，因闪避所避免造成的伤害值：{}，比其当前的血量（{}）要大！可判断死亡！", defender.getName(), defender.getShadowId(), missHp, defender.getCurrentHp());
				return true;
			}
			return false;
		} else {
			return false;
		}
	}
	
	@Override
	public IOperationResult executeOperation(ICombat combat) {
		ICombatMember member = combat.getCombatMember(_shadowId);
		if (member != null && member.isAlive() && member.canBeAttacked()) {
			if ((combat.getCombatType() != KCombatType.WORLD_BOSS || member.getMemberType() != ICombatMember.MEMBER_TYPE_BOSS_MONSTER)) {
				// 世界boss无效
				long dm = member.getCurrentHp();
				if(combat.getCombatType() == KCombatType.OFFLINE_COMBAT) {
//					LOGGER.info("收到客户端确认死亡指令，战场id：{}，shadowId：{}，当前血量：{}，战斗类型为离线战斗，直接判定死亡！", combat.getSerialId(), _shadowId, member.getCurrentHp());
					member.decreaseHp(member.getCurrentHp(), opTime);
				} else if (_selfExplose) {
					member.sentenceToDead(opTime);
					member.getCombatRecorder().recordTimeOfDmByDead(opTime);
				} else if (check(combat, member)) {
					member.decreaseHp(member.getCurrentHp(), opTime);
				} else {
					int pct = UtilTool.calculatePercentageL(member.getCurrentHp(), member.getMaxHp(), false);
					LOGGER.info("收到客户端确认死亡指令，战场id：{}，shadowId：{}，当前剩余血量百分比：{}，当前时间：{}", combat.getSerialId(), _shadowId, pct, getOperationTime());
					switch (member.getMemberType()) {
					case ICombatMember.MEMBER_TYPE_BOSS_MONSTER:
					case ICombatMember.MEMBER_TYPE_ROLE_MONSTER:
						if (pct < _ALLOW_PCT_MISTAKE_BOSS) {
							member.decreaseHp(member.getCurrentHp(), opTime);
						}
						break;
					case ICombatMember.MEMBER_TYPE_ROLE:
					case ICombatMember.MEMBER_TYPE_TEAM_MATE_ROLE:
						// 如果客户端认为角色死亡，则马上死亡
						member.decreaseHp(member.getCurrentHp(), opTime);
						break;
					default:
						if (pct < _ALLOW_PCT_MISTAKE) {
							member.decreaseHp(member.getCurrentHp(), opTime);
						}
						break;
					}
				}
				if (!member.isAlive()) {
					ICombatMember roleMember = combat.getRoleTypeMemberBySrcId(_roleId);
					if (roleMember != null) {
						roleMember.getCombatRecorder().recordKillMember(member);
						roleMember.getCombatRecorder().recordDm(dm);
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public int getPriority() {
		return PRIORITY_NORMAL;
	}

}
