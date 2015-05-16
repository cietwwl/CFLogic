package com.kola.kmp.logic.combat;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;

import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.combat.cmd.ICombatCommand;
import com.kola.kmp.logic.combat.impl.KCombatEntrance;
import com.kola.kmp.logic.combat.operation.IOperation;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombat {
	
	Logger LOGGER = KGameLogger.getLogger("combatLogger");
	
	/** 战斗状态：开始 */
	byte STATUS_START = 1;
	/** 战斗状态：暂停 */
	byte STATUS_PAUSE = 2;
	/** 战斗状态：继续 */
	byte STATUS_CONTINUE = 3;
	/** 战斗状态：资源加载完毕 */
	byte STATUS_LOAD_FINISH = 4;
	/** 战斗状态：退出完毕 */
	byte STATUS_EXIT_FINISH = 5;
	/** 战斗状态：战斗结束*/
	byte STATUS_FINISH = 6;
	/** 战斗状态：客户端战斗结束 */
	byte STATUS_CLIENT_END = 7;
	
	/**
	 * 
	 * @param msg
	 */
	public void sendMsgToAll(KGameMessage msg);
	
	/**
	 * 
	 * @param cmd
	 */
	public void submitCommand(ICombatCommand pCmd);
	
	/**
	 * 开始战斗
	 */
	public void startCombat();
	
	/**
	 * 
	 * @return
	 */
	public KCombatType getCombatType();

	/**
	 * 
	 * 根据战场势力类型，获取战场“势力”
	 * 
	 * @return
	 */
	public ICombatForce getForce(byte pForceType);
	
	/**
	 * 
	 * 获取所有的敌对势力（相对于角色）
	 * 
	 * @return
	 */
	public List<ICombatForce> getAllEnermyForces();
	
	/**
	 * 
	 * 获取战场成员
	 * 
	 * @param shadowId 战场分配的id
	 * @return
	 */
	public ICombatMember getCombatMember(short shadowId);
	
	/**
	 * 
	 * @param objId
	 * @return
	 */
	public ICombatMember getRoleTypeMemberBySrcId(long objId);
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public List<ICombatMember> getCombatMemberByType(byte type);
	
	/**
	 * 
	 * 获取这个战场的随机器
	 * 
	 * @return
	 */
	public Random getCombatRandomInstance();
	
	/**
	 * 
	 * @return
	 */
	public int getSerialId();
	
	/**
	 * 
	 * @return
	 */
	public long getCombatStartTime();
	
	/**
	 * 
	 * @return
	 */
	public int getCurrentUseTime();
	
	/**
	 * 
	 * @return
	 */
	public int getTimeOutMillis();
	
	/**
	 * 
	 * @return
	 */
	public boolean isTerminal();
	
	/**
	 * 
	 * 临时添加额外的operation到战场去执行
	 * 
	 * @param op
	 */
	public void addExtractOperation(IOperation op);
	
	/**
	 * 
	 * @param opResult
	 */
	public void addOperationResult(IOperationResult opResult);
	
	/**
	 * 
	 * @param member
	 */
	public void notifyMemberDead(ICombatMember member);
	
	/**
	 * 
	 * @param shadowId
	 */
	public void addRoundDeadMemberAgain(ICombatMember member);
	
	/**
	 * 
	 * 时效任务的通知
	 * 
	 */
	public void onTimeSignal();
	
	/**
	 * 
	 * @param msg
	 */
	public void msgReceived(long roleId, KGameMessage msg);
	
	/**
	 * 
	 * @param roleId
	 */
	public void handleRoleLeaveGame(long roleId);
	
	/**
	 * 
	 * @param masterSrcId
	 * @param petShadowId
	 */
	public void recordMasterIdOfPet(long masterSrcId, short petShadowId);
	
	/**
	 * 
	 * @param pInstanceId
	 */
	public void recordKillInstanceId(int pInstanceId);
	
	/**
	 * 
	 * @param shadowIdOfPet
	 * @return
	 */
	public short getMasterShadowIdOfPet(short shadowIdOfPet);
	
	/**
	 * 
	 * @param minion
	 */
	public ICombatMember addMinion(ICombatMember master, ICombatMinion minion, long createTime);
	
	/**
	 * 
	 * @param master
	 * @param mount
	 * @param createTime
	 */
	public ICombatMember addMount(ICombatMember master, KCombatEntrance mount, long createTime);
	
	/**
	 * 
	 * @param master
	 */
	public void releaseMount(ICombatMember master, long happenTime);
	
	/**
	 * 
	 * @param master
	 * @return
	 */
	public ICombatMember getInUseMount(ICombatMember master);
	
	/**
	 * 
	 * @param mount
	 */
	public ICombatMember getMasterOfMount(ICombatMember mount);
	
	/**
	 * 
	 * @param minion
	 * @return
	 */
	public ICombatMember getMasterOfMinion(ICombatMember minion);
	
	/**
	 * 
	 * @param memberMap
	 */
	public void notifyMemberAddToCombatGround(List<ICombatMember> memberList);
	
	/**
	 * 
	 * @param dropId
	 */
	public void pickUp(long roleId, int dropId, long happenTime);
	
	/**
	 * 
	 * 一个operation执行完之后的回调
	 * 
	 * @param happenTime
	 */
	public void afterOneOperationExecuted(long happenTime);
	
	/**
	 * 
	 * @param current
	 * @param target
	 */
	public void switchSence(int current, int target);
	
	/**
	 * 
	 * @param shadowId
	 */
	public void addSyncHpShadowId(short shadowId);
	
	/**
	 * 
	 * @param pNowTime
	 */
	public void nowTimeNotify(long pNowTime);
	
	/**
	 * 
	 * @param roleId
	 */
	public void handleClientExitFinished(long roleId);
}
