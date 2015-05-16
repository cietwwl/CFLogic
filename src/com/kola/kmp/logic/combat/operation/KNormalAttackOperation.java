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
import com.kola.kmp.logic.combat.impl.KCombatRecorderBaseImpl;
import com.kola.kmp.logic.combat.support.IOperationRecorder;
import com.kola.kmp.logic.combat.support.KCombatCalculateSupport;

/**
 * 
 * @author PERRY CHAN
 */
public class KNormalAttackOperation extends KOperationBaseImpl {
	
	private long _opBeginTime; // 出手时间
	private short _attackerId;
	private int _dmCount;
	private short[] _defenders;
	private boolean _selfExplose; // 是否自爆
	
	static int MAX_NORMAL_ATK_COUNT_EACH_TIME_OF_ROLE_SIDE = 5;
	
	KNormalAttackOperation(long pOpTime, long pOpBeginTime, short pAttackerId, int pDmCount, short[] pDefenders, boolean pSelfExplose) {
		super(pOpTime);
		this._opBeginTime = pOpBeginTime;
		this._attackerId = pAttackerId;
		this._dmCount = pDmCount;
		this._defenders = pDefenders;
		this._selfExplose = pSelfExplose;
	}
	
	private boolean isAliveToAttack(ICombatMember member) {
		if (member.isAlive()) {
			return true;
		} else {
			if (member.getMemberType() == ICombatMember.MEMBER_TYPE_BLOCK) {
//				LOGGER.info("障碍物执行死亡伤害，id={}，名字={}", member.getShadowId(), member.getName());
				long lastTime = member.getCombatRecorder().getTimeOfDmByDead();
				if (member.getCombatRecorder().getTimeOfDmByDead() > 0) {
					if( opTime - lastTime < 1001) {
						return true;// 1秒之内有效
					} else {
//						LOGGER.info("障碍物执行死亡伤害，超时，上次={}，本次={}", lastTime, opTime);
						return false;
					}
				} else {
					member.getCombatRecorder().recordTimeOfDmByDead(opTime);
					return true;
				}
			} else if (_selfExplose) {
				long lastTime = member.getCombatRecorder().getTimeOfDmByDead();
				if(opTime - lastTime < 2001) {
//					LOGGER.info("再次自爆攻击！id={}，名字={}，时间：{}，上次：{}", member.getShadowId(), member.getName(), opTime, lastTime);
					return true; // 2秒内有效
				}
				return false;
			} else {
				if(_opBeginTime < member.getDeadTime()) {
//					LOGGER.info("攻击者：{}，出手时间（{}）比死亡时间（{}）要晚，攻击有效！", member.getName(), _opBeginTime, member.getDeadTime());
					return true;
				}
				return false;
			}
		}
	}
	
	@Override
	public IOperationResult executeOperation(ICombat combat) {
		KNormalAttackOperationResult result = new KNormalAttackOperationResult();
		result.attackerId = _attackerId;
//		LOGGER.info("普通攻击，攻击者id={}，受击者id={}，伤害数量={}", _attackerId, _defenders, _dmCount);
		ICombatMember attacker = combat.getCombatMember(_attackerId);
		if (attacker != null && attacker.canOperate()) {
			if (isAliveToAttack(attacker)) {
				Map<Short, ICombatMember> defenderMap = new HashMap<Short, ICombatMember>();
				List<IOperationRecorder> tempList;
				ICombatMember defender;
				short defenderId;
				for (int i = 0; i < _defenders.length; i++) {
					defenderId = _defenders[i];
					defender = defenderMap.get(defenderId);
					if (defender == null) {
						defender = combat.getCombatMember(defenderId);
						if (defender != null) {
							defenderMap.put(defender.getShadowId(), defender);
						} else {
							LOGGER.error("defender为null！！defenderId：{}", defenderId);
							continue;
						}
					}
					if (defender.isAlive()) {
						if (defender.getCombatRecorder().isNormalAttackDuplicate(_attackerId, opTime) && defender.isGeneralMonster()) {
							List<Long> list = defender.getCombatRecorder().getNormalAtkTimeRecord(_attackerId);
							int count = 0;
							for (int k = 0; k < list.size(); k++) {
								if (list.get(k) == opTime) {
									count++;
								}
							}
							if (count > MAX_NORMAL_ATK_COUNT_EACH_TIME_OF_ROLE_SIDE) {
								LOGGER.error("attacker=[{},{}],defenderId={},combatId={},opTime={},同一时间普通攻击结算超过5次", attacker.getName(), _attackerId, defender.getShadowId(), combat.getSerialId(), opTime);
								continue;
							}
						}
						tempList = result.opRecordList.get(defenderId);
						if (tempList == null) {
							if (defender.getCombatRecorder().isNormalAttackDuplicate(_attackerId, opTime)) {
								LOGGER.error("attacker=[{},{}],defenderId={},opTime={},普通攻击重复结算", attacker.getName(), _attackerId, defender.getShadowId(), opTime);
								continue;
							}
							tempList = new ArrayList<IOperationRecorder>();
							result.opRecordList.put(defenderId, tempList);
						}
						for (int k = 0; k < _dmCount; k++) {
							tempList.add(KCombatCalculateSupport.doNormalAttack(combat, attacker, defender, opTime));
//							LOGGER.info("attacker=[{},{}],defenderId={},opTime={},普通攻击正常结算", attacker.getName(), _attackerId, defender.getShadowId(), opTime);
						}
					} else {
//						LOGGER.info("普通攻击，防守者[id={},名字={}]已经死亡，不执行攻击指令，指令时间={}！", defender.getShadowId(), defender.getName(), opTime);
//						return null;
						for (int k = 0; k < _dmCount; k++) {
							attacker.getCombatRecorder().recordAttack(opTime); // 计算连击次数
						}
						combat.addRoundDeadMemberAgain(defender); // 再通知一次客户端
						continue;
					}
				}
				result._defenderMap = defenderMap;
			} else {
//				LOGGER.info("普通攻击，攻击者[id={},名字={}]已经死亡，不执行攻击指令，指令时间={}！", attacker.getShadowId(), attacker.getName(), opTime);
				return null;
			}
		} else {
			LOGGER.error("普通攻击，攻击者[{}]无法操作！opTime={}", (attacker == null ? _attackerId : attacker.getName()), opTime);
		}
		return result;
	}
	
	@Override
	public int getPriority() {
		return PRIORITY_URGENT;
	}
	
	private class KNormalAttackOperationResult implements IOperationResult {

		short attackerId;
		Map<Short, List<IOperationRecorder>> opRecordList = new HashMap<Short, List<IOperationRecorder>>();
		Map<Short, ICombatMember> _defenderMap = new HashMap<Short, ICombatMember>();
		
		@Override
		public byte getOperationType() {
			return IOperation.OPERATION_TYPE_NORMAL_ATTACK;
		}
		
		@Override
		public void fillMsg(KGameMessage msg) {
			msg.writeShort(attackerId);
			int writerIndex = msg.writerIndex();
			msg.writeByte(opRecordList.size());
			Map.Entry<Short, List<IOperationRecorder>> entry;
			List<IOperationRecorder> tempList;
			IOperationRecorder tempRecorder;
			ICombatMember defender;
			int sendCount = 0;
			for (Iterator<Map.Entry<Short, List<IOperationRecorder>>> itr = opRecordList.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				tempList = entry.getValue();
				defender = _defenderMap.get(entry.getKey());
				if (defender.getMemberType() != ICombatMember.MEMBER_TYPE_VEHICLE) {
					// 机甲不用同步伤害
					msg.writeShort(entry.getKey());
					msg.writeByte(tempList.size());
					for (int i = 0; i < tempList.size(); i++) {
						tempRecorder = tempList.get(i);
						msg.writeBoolean(tempRecorder.isHit());
						if (tempRecorder.isHit()) {
							msg.writeBoolean(tempRecorder.isCrit());
							msg.writeInt(tempRecorder.getDm());
//							LOGGER.info("发送消息：序号={},名字={},伤害={}", i, defender.getName(), tempRecorder.getDm());
						}
					}
					msg.writeLong(defender.getCurrentHp());
					sendCount++;
				}
			}
			msg.setByte(writerIndex, sendCount);
		}
		
	}
}
