package com.kola.kmp.logic.combat.operation;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.koala.game.KGameMessage;
import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.other.KGameAttrType;

/**
 * 
 * @author PERRY CHAN
 */
public class KOperationMsgHandlerImpl implements IOperationMsgHandler {

	private static final int ALLOW_MISTAKE = 3000;
//	private static final int WARNING_DM_COUNT = 20;
	
	@Override
	public List<IOperation> handleOperationMsg(long roleId, ICombat combat, KGameMessage msg) {
		byte opType;
		long opTime;
		short opCount = msg.readShort();
		List<IOperation> opList = new ArrayList<IOperation>(opCount);
		IOperation op;
//		LOGGER.info("收到operation消息，数量：{}，战场id：{}", opCount, combat.getSerialId());
		long checkMillis = System.currentTimeMillis() + ALLOW_MISTAKE; // 放宽三秒
		for(int i = 0; i < opCount; i++) {
			int clientOpTime = msg.readInt();
//			LOGGER.info("客户端指令时间：{}", clientOpTime);
			opTime = clientOpTime + combat.getCombatStartTime();
			opType = msg.readByte();
			switch(opType) {
			case IOperation.OPERATION_TYPE_NORMAL_ATTACK:
				op = (this.createNormalAttackOperation(roleId, opTime, combat, msg));
				break;
			case IOperation.OPERATION_TYPE_SKILL_ATTACK:
				op = (this.createSkillAttackOperation(roleId, opTime, combat, msg));
				break;
			case IOperation.OPERATION_TYPE_MOUNT:
				op = (this.createSummonMountOperation(opTime, msg));
				break;
			case IOperation.OPERATION_TYPE_SWITCH_SENCE:
				op = (this.createSwitchSenceOperation(opTime, msg));
				break;
			case IOperation.OPERATION_TYPE_PICK:
				op = (this.createPickupOperation(roleId, opTime, msg));
				break;
			case IOperation.OPERATION_TYPE_OBSTRUCTION_BUFF:
//				LOGGER.info("障碍物buff，客户端时间：{}", clientOpTime);
				op = (this.createObstructionBuffOperation(opTime, msg));
				break;
			case IOperation.OPERATION_TYPE_SWITCH_WEAPON:
				op = this.createSwitchWeaponOperation(opTime, msg);
				break;
			case IOperation.OPERATION_TYPE_COHESION:
				op = this.createCohesionOperation(opTime, msg);
				break;
			case IOperation.OPERATION_TYPE_BLOCK_SWITCH:
				op = this.createBlockSwitchOperation(opTime, msg);
				break;
			case IOperation.OPERATION_TYPE_BUFF_OVER:
//				LOGGER.info("buffover，客户端时间：{}", clientOpTime);
				op = this.createBuffOverOperation(opTime, msg);
				break;
			case IOperation.OPERATION_TYPE_CLIENT_CLAIM_DEAD:
				op = this.createClientClaimDeadOperation(roleId, opTime, msg);
				break;
			case IOperation.OPERATION_TYPE_SELF_EXPLOSE:
				op = this.createSelfExploseOperation(opTime, combat, msg);
				break;
			case IOperation.OPERATION_TYPE_AI_CMD:
				op = this.createAIOperation(opTime, msg);
				break;
			default:
				LOGGER.info("#### opType={}, opTime={}, opCount={}, i={} ####", opType, opTime, opCount, i);
				op = null;
				break;
			}
			if (clientOpTime < 0) {
				LOGGER.error("读取到clientOpTime < 0！！角色id：{}，op类型：{}，clientOpTime={}", roleId, opType, clientOpTime);
			} else {
				if (op != null) {
					if (op.getOperationTime() > checkMillis) {
						LOGGER.error("客户端指令的时间比服务器当前时间还要大！！客户端时间：{}，服务器时间：{}，op类型：{}，角色id：{}", op.getOperationTime(), checkMillis, opType, roleId);
					}
					opList.add(op);
				}
			}
		}
		return opList;
	}
	
	private IOperation createNormalAttackOperation(long roleId, long opTime, ICombat combat, KGameMessage msg) {
		short attackerId = msg.readShort();
		int readOpBeginTime = msg.readInt();
		long opBeginTime = readOpBeginTime + combat.getCombatStartTime();
		int dmCount = msg.readByte();
		int defenderCount = msg.readByte();
		short[] defenders = new short[defenderCount];
		for (int i = 0; i < defenderCount; i++) {
			defenders[i] = msg.readShort();
		}
		if (dmCount > 1) {
			LOGGER.warn("createNormalAttackOperation, dmCount > 1, combatId:{}", combat.getSerialId());
			dmCount = 1;
		}
		if(readOpBeginTime < 0) {
			LOGGER.error("createNormalAttackOperation, readOpBeginTime < 0, 战场id：{}", combat.getSerialId());
			return null;
		} else {
			if(readOpBeginTime == 0) {
				LOGGER.warn("createSkillAttackOperation, readOpBeginTime = 0, 战场id：{}, roleId：{}", combat.getSerialId(), roleId);
			}
			return new KNormalAttackOperation(opTime, opBeginTime, attackerId, dmCount, defenders, false);
		}
	}
	
	private IOperation createSkillAttackOperation(long roleId, long opTime, ICombat combat, KGameMessage msg) {
		String useCode = msg.readUtf8String();
		short operatorId = msg.readShort();
		int readOpBeginTime = msg.readInt();
		long opBeginTime = readOpBeginTime + combat.getCombatStartTime();
		int skillTemplateId = msg.readInt();
		int dmCount = msg.readByte();
		int targetCount = msg.readByte();
		short[] targets;
		if (targetCount < 0) {
			LOGGER.error("createSkillAttackOperation, targetCount < 0, 战场id：{}", combat.getSerialId());
			targets = new short[0];
		} else {
			targets = new short[targetCount];
			for (int i = 0; i < targetCount; i++) {
				targets[i] = msg.readShort();
			}
		}
		if (readOpBeginTime < 0) {
			LOGGER.error("createSkillAttackOperation, readOpBeginTime < 0, 战场id：{}", combat.getSerialId());
			return null;
		} else {
			if(readOpBeginTime == 0) {
				LOGGER.warn("createSkillAttackOperation, readOpBeginTime = 0, 战场id：{}, roleId：{}", combat.getSerialId(), roleId);
			}
			return new KSkillAttackOperation(opTime, opBeginTime, useCode, operatorId, skillTemplateId, dmCount, targets);
		}
	}
	
	private IOperation createSummonMountOperation(long opTime, KGameMessage msg) {
		short operatorId = msg.readShort();
		boolean upOrDown = msg.readBoolean();
		return new KSummonMountOperation(operatorId, opTime, upOrDown);
	}
	
	private IOperation createSwitchSenceOperation(long opTime, KGameMessage msg) {
		short operatorId = msg.readShort();
		int currentSenceId = msg.readInt();
		int targetSenceId = msg.readInt();
		return new KSwitchSenceOperation(opTime, operatorId, currentSenceId, targetSenceId);
	}
	
	private IOperation createPickupOperation(long roleId, long opTime, KGameMessage msg) {
		int dropId = msg.readInt();
		return new KPickUpOperation(opTime, roleId, dropId);
	}
	
	private IOperation createObstructionBuffOperation(long opTime, KGameMessage msg) {
		int buffId = msg.readInt();
		short obstructionId = msg.readShort();
		byte targetCounts = msg.readByte();
		short[] targetIds = new short[targetCounts];
		for (int i = 0; i < targetCounts; i++) {
			targetIds[i] = msg.readShort();
		}
		return new KObstructionBuffOperation(opTime, buffId, obstructionId, targetIds);
	}
	
	private IOperation createCohesionOperation(long opTime, KGameMessage msg) {
		short shadowId = msg.readShort();
		int time = msg.readInt();
		int clientResult = msg.readInt();
		return new KCohesionOperation(opTime, shadowId, time, clientResult);
	}
	
	private IOperation createSwitchWeaponOperation(long opTime, KGameMessage msg) {
		short shadowId = msg.readShort();
		boolean switchToSecond = msg.readBoolean();
		return new KSwitchSecondWeaponOperation(opTime, shadowId, switchToSecond);
	}
	
	private IOperation createBlockSwitchOperation(long opTime, KGameMessage msg) {
		short shadowId = msg.readShort();
		boolean start = msg.readBoolean();
		return new KSwitchBlockOperation(opTime, shadowId, start);
	}
	
	private IOperation createBuffOverOperation(long opTime, KGameMessage msg) {
		short shadowId = msg.readShort();
		return new KBuffOverOperation(opTime, shadowId);
	}
	
	private IOperation createClientClaimDeadOperation(long pRoleId, long opTime, KGameMessage msg) {
		short shadowId = msg.readShort();
		boolean selfExplose = msg.readBoolean();
//		LOGGER.info("客户端确认死亡消息！roleId={}, shadowId={}, selfExplose={}", pRoleId, shadowId, selfExplose);
		return new KClientClaimDeadOperation(pRoleId, opTime, shadowId, selfExplose);
	}
	
	private IOperation createSelfExploseOperation(long opTime, ICombat combat, KGameMessage msg) {
		short attackerId = msg.readShort();
		long opBeginTime = msg.readInt() + combat.getCombatStartTime();
		int dmCount = msg.readByte();
		int defenderCount = msg.readByte();
		short[] defenders = new short[defenderCount];
		for (int i = 0; i < defenderCount; i++) {
			defenders[i] = msg.readShort();
		}
		return new KNormalAttackOperation(opTime, opBeginTime, attackerId, dmCount, defenders, true);
	}
	
	private IOperation createAIOperation(long opTime, KGameMessage msg) {
		short targetId = msg.readShort();
		byte aiType = msg.readByte();
		switch (aiType) {
		case IOperation.AI_CMD_BUFF:
			int buffId = msg.readShort();
			return new KAIBuffOperation(opTime, targetId, buffId);
		case IOperation.AI_CMD_ATTR_MODIFY:
			short attrType = msg.readShort();
			int value = msg.readInt();
			KGameAttrType targetAttrType = KGameAttrType.getAttrTypeEnum(attrType);
			if (targetAttrType != null) {
				return new KAIAddAttrOperation(opTime, targetId, targetAttrType, value);
			} else {
				return null;
			}
		case IOperation.AI_CMD_STATE:
			boolean add = msg.readBoolean();
			byte stateType = msg.readByte();
			return new KAIAddStateOperation(opTime, targetId, add, stateType);
		case IOperation.AI_CMD_CD_REDUCE:
			int skillId = msg.readInt();
			int cdTime = msg.readInt();
			return new KAIReduceSkillCDOperation(targetId, opTime, skillId, cdTime);
		default:
			LOGGER.error("AI指令，找不到类型：{}", aiType);
			return null;
		}
	}
	
	public static void main(String[] args) throws Exception {
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[]{38, 22, 17, -67, 0, 1, 0, 0, -89, -27, 1, 0, 1, 1, 1, 0, 23});
		DataInputStream dis = new DataInputStream(bis);
		int validationCode = dis.readInt();
		int opCount = dis.readShort();
		System.out.println("validationCode=" + validationCode + ",opCount=" + opCount);
		long currentTimeMillis = System.currentTimeMillis();
		byte opType;
		long opTime;
//		IOperation op;
		for(int i = 0; i < opCount; i++) {
			int clientOpTime = dis.readInt();
			opTime = clientOpTime + currentTimeMillis;
			opType = dis.readByte();
			switch(opType) {
			case IOperation.OPERATION_TYPE_NORMAL_ATTACK:
//				System.out.println(StringUtil.format("#### opType={}, opTime={}, opCount={}, i={} ####", opType, opTime, opCount, i));
				LOGGER.info("普通攻击");
				LOGGER.info("attackerId = {}" ,dis.readShort());
				LOGGER.info("dmCount = {}", dis.readByte());
				int defenderCount = dis.readByte();
				LOGGER.info("defenderCount = {}", defenderCount);
				short[] defenders = new short[defenderCount];
				for (int k = 0; k < defenderCount; k++) {
					defenders[k] = dis.readShort();
				}
				LOGGER.info("defenders = {}", Arrays.toString(defenders));
				break;
			case IOperation.OPERATION_TYPE_SKILL_ATTACK:
//				System.out.println(StringUtil.format("#### opType={}, opTime={}, opCount={}, i={} ####", opType, opTime, opCount, i));
				LOGGER.info("技能攻击");
				int length = dis.readInt();
				byte[] array = new byte[length];
				dis.read(array);
				String useCode = new String(array, "UTF-8");
				LOGGER.info("useCode = {}", useCode);
				short operatorId = dis.readShort();
				LOGGER.info("operatorId = {}", operatorId);
				int skillTemplateId = dis.readInt();
				LOGGER.info("skillTemplateId = {}", skillTemplateId);
				int skillDmCount = dis.readByte();
				LOGGER.info("skillDmCount = {}", skillDmCount);
				int targetCount = dis.readByte();
				LOGGER.info("targetCount = {}", targetCount);
				List<Short> targets = new ArrayList<Short>();
				for (int k = 0; k < targetCount; k++) {
					targets.add(dis.readShort());
				}
				LOGGER.info("targets = {}", targets);
				break;
			case IOperation.OPERATION_TYPE_MOUNT:
				LOGGER.info("机甲指令");
				short masterId = dis.readShort();
				LOGGER.info("masterId = {}", masterId);
				boolean upOrDown = dis.readBoolean();
				LOGGER.info("upOrDown = {}", upOrDown);
				break;
			case IOperation.OPERATION_TYPE_SWITCH_SENCE:
				LOGGER.info("切换场景");
				short switherId = dis.readShort();
				LOGGER.info("switherId = {}", switherId);
				int currentSenceId = dis.readInt();
				LOGGER.info("currentSenceId = {}", currentSenceId);
				int targetSenceId = dis.readInt();
				LOGGER.info("targetSenceId = {}", targetSenceId);
				break;
			case IOperation.OPERATION_TYPE_PICK:
				LOGGER.info("拾取");
				int dropId = dis.readInt();
				LOGGER.info("dropId = {}", dropId);
				break;
			case IOperation.OPERATION_TYPE_OBSTRUCTION_BUFF:
				LOGGER.info("buff指令");
				int buffId = dis.readInt();
				LOGGER.info("buffId = {}", buffId);
				short obstructionId = dis.readShort();
				LOGGER.info("obstructionId = {}", obstructionId);
				byte targetCounts = dis.readByte();
				List<Short> targetIds = new ArrayList<Short>();
				for (int k = 0; k < targetCounts; k++) {
					targetIds.add(dis.readShort());
				}
				LOGGER.info("targetIds = {}", targetIds);
				break;
			default:
				System.out.println(StringUtil.format("#### opType={}, opTime={}, opCount={}, i={} ####", opType, opTime, opCount, i));
//				op = null;
				break;
			}
		}
	}
}
