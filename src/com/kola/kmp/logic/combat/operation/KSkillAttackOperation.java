package com.kola.kmp.logic.combat.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.IOperationResult;
import com.kola.kmp.logic.combat.skill.ICombatSkillExecution;
import com.kola.kmp.logic.combat.support.IOperationRecorder;

/**
 * 
 * @author PERRY CHAN
 */
public class KSkillAttackOperation extends KOperationBaseImpl {

	private long _opBeginTime; // 出手时间
	private String _useCode;
	private short _operatorId;
	private int _skillTemplateId;
	private int _dmTimes;
	private short[] _targets;
	
	KSkillAttackOperation(long pHappenTime, long pOpBeginTime, String pUseCode, short pOperatorId, int pSkillTemplateId, int pDmTimes, short[] pTargets) {
		super(pHappenTime);
		this._opBeginTime = pOpBeginTime;
		this._useCode = pUseCode;
		this._operatorId = pOperatorId;
		this._skillTemplateId = pSkillTemplateId;
		this._dmTimes = pDmTimes;
		this._targets = pTargets;
	}
	
	private boolean validateOperation(ICombatMember operator) {
		if(operator == null) {
			LOGGER.error("技能攻击，operator为null！operatorId={}", _operatorId);
			return false;
		} else if (!operator.getSkillActor().isCanUseSkill(_skillTemplateId, _useCode, opTime)) {
			LOGGER.error("技能攻击，[{}]不能使用技能（技能id={}, useCode={}, 操作时间={}）", operator.getName(), _skillTemplateId, _useCode, opTime);
			return false;
		} else if (!operator.getSkillActor().executeSkillConsume(_skillTemplateId, _useCode)) {
			LOGGER.error("技能攻击，[{}]技能消耗执行失败（技能id={}, useCode={}, 操作时间={}）", operator.getName(), _skillTemplateId, _useCode, opTime);
			return false;
		} else if (!operator.canOperate()) {
			LOGGER.error("技能攻击，[{}]不能操作！", operator.getName());
			return false;
		} else if (!operator.isAlive()) {
			if (operator.getDeadTime() > _opBeginTime) {
				LOGGER.error("技能攻击，[{}]死亡时间（{}）比出手时间（{}）晚，攻击有效！", operator.getName(), operator.getDeadTime(), _opBeginTime);
				return true;
			}
		}
		return true;
	}
	
	@Override
	public IOperationResult executeOperation(ICombat combat) {
		ICombatMember operator = combat.getCombatMember(_operatorId);
		if(validateOperation(operator)) {
//			LOGGER.info("技能攻击，[{}]使用技能，目标：{}，技能id={}，useCode={}, 次数={}", operator.getName(), _targets, _skillTemplateId, _useCode, _dmTimes);
			List<ICombatMember> targets = new ArrayList<ICombatMember>();
			Map<Short, ICombatMember> targetMap = new HashMap<Short, ICombatMember>();
			ICombatMember temp;
			short shadowId;
			for (int i = 0; i < this._targets.length; i++) {
				shadowId = this._targets[i];
				temp = combat.getCombatMember(shadowId);
				if (temp != null) {
//					if (temp.isAlive()) {
//						targets.add(temp);
//						targetMap.put(temp.getShadowId(), temp);
//					} else {
//						LOGGER.info("技能攻击，targetId={}，已经死亡！", shadowId);
//					}
					targets.add(temp);
					targetMap.put(temp.getShadowId(), temp);
					if (!temp.isAlive()) {
						combat.addRoundDeadMemberAgain(temp); // 再通知一次客户端
					}
				} else {
					LOGGER.info("技能攻击，targetId={}，target为null！", shadowId);
				}
			}
			ICombatSkillExecution execution = operator.getSkillActor().getSkillExecution(_skillTemplateId);
			List<IOperationRecorder> recordList = new ArrayList<IOperationRecorder>();
			List<IOperationRecorder> tempList;
			for (int i = 0; i < _dmTimes; i++) {
				tempList = execution.execute(combat, operator, targets, _useCode, opTime);
				if (tempList != null) {
					recordList.addAll(tempList);
				}
			}
			if (recordList.isEmpty()) {
				return null;
			} else {
				return new KSkillOperationResult(this._operatorId, targetMap, recordList);
			}
		}
		return null;
	}
	
	@Override
	public int getPriority() {
		return PRIORITY_URGENT;
	}
	
	private static class KSkillOperationResult implements IOperationResult {

		private short _operatorId;
		private boolean _isCure;
		private Map<Short, List<IOperationRecorder>> _resultMap = new HashMap<Short, List<IOperationRecorder>>();
		Map<Short, ICombatMember> _defenderMap = new HashMap<Short, ICombatMember>();
		
		KSkillOperationResult(short pOperatorId, Map<Short, ICombatMember> pDefenderMap, List<IOperationRecorder> recordList) {
			_operatorId = pOperatorId;
			if (recordList.size() > 0) {
				_isCure = !recordList.get(0).isDamage();
			}
			IOperationRecorder record;
			for (int i = 0; i < recordList.size(); i++) {
				record = recordList.get(i);
				List<IOperationRecorder> temp = _resultMap.get(record.getTargetId());
				if (temp == null) {
					temp = new ArrayList<IOperationRecorder>();
					_resultMap.put(record.getTargetId(), temp);
				}
				temp.add(record);
			}
			_defenderMap.putAll(pDefenderMap);
		}
		@Override
		public byte getOperationType() {
			return IOperation.OPERATION_TYPE_SKILL_ATTACK;
		}

		@Override
		public void fillMsg(KGameMessage msg) {
			msg.writeShort(_operatorId);
			msg.writeBoolean(_isCure);
			msg.writeByte(_resultMap.size());
//			LOGGER.info("！！！！技能攻击，operatorId={}，是否治疗：{}，防守者数量：{}！！！！", _operatorId, _isCure, _resultMap.size());
			ICombatMember member;
			if(_isCure) {
				Map.Entry<Short, List<IOperationRecorder>> entry;
				List<IOperationRecorder> list;
				IOperationRecorder temp;
				for (Iterator<Map.Entry<Short, List<IOperationRecorder>>> itr = _resultMap.entrySet().iterator(); itr.hasNext();) {
					entry = itr.next();
					list = entry.getValue();
					member = _defenderMap.get(entry.getKey());
					msg.writeShort(entry.getKey());
					msg.writeByte(list.size());
					for (int i = 0; i < list.size(); i++) {
						temp = list.get(i);
						msg.writeBoolean(temp.isCrit());
						msg.writeInt(temp.getDm());
					}
					msg.writeLong(member.getCurrentHp());
				}
			} else {
				Map.Entry<Short, List<IOperationRecorder>> entry;
				List<IOperationRecorder> list;
				IOperationRecorder temp;
				for (Iterator<Map.Entry<Short, List<IOperationRecorder>>> itr = _resultMap.entrySet().iterator(); itr.hasNext();) {
					entry = itr.next();
					list = entry.getValue();
					member = _defenderMap.get(entry.getKey());
					msg.writeShort(entry.getKey());
					msg.writeByte(list.size());
					for (int i = 0; i < list.size(); i++) {
						temp = list.get(i);
						msg.writeBoolean(temp.isHit());
						if (temp.isHit()) {
							msg.writeBoolean(temp.isCrit());
							msg.writeInt(temp.getDm());
						}
//						LOGGER.info("技能攻击，打包消息，防守者id：{}，是否命中：{}，是否暴击：{}，伤害：{}", entry.getKey(), temp.isHit(), temp.isCrit(), temp.getDm());
					}
					msg.writeLong(member.getCurrentHp());
				}
			}
		}
		
	}

}
