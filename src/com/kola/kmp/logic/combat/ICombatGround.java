package com.kola.kmp.logic.combat;

import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.combat.resulthandler.ICombatResult;
import com.kola.kmp.logic.combat.resulthandler.ICombatRoleResult;


/**
 * 
 * 代表一场战斗里面的某个场景的战斗信息
 * 
 * @author PERRY CHAN
 */
public interface ICombatGround {
	
	public static final AtomicInteger combatGroundIdGenerator = new AtomicInteger();
	
	/**
	 * 
	 * @return
	 */
	public boolean isFirst();
	
	/**
	 * 
	 * 获取唯一的id
	 * 
	 * @return
	 */
	public int getSerialId();
	
	/**
	 * 
	 * @return
	 */
	public int getBattleFieldTemplateId();
	
	/**
	 * 
	 * @return
	 */
	public String getMapResPath();
	
	/**
	 * 
	 * @return
	 */
	public int getBgAudioResId();
	
	/**
	 * 
	 * @return
	 */
	public int getNextBattleFieldTemplateId();
	
	/**
	 * 
	 * 获取怪物势力
	 * 
	 * @return
	 */
	public ICombatForce getMonsterForce();
	
	/**
	 * 
	 * 获取中立势力
	 * 
	 * @return
	 */
	public ICombatForce getNeutralForce();
	
	/**
	 * 
	 * 把这个场景里面的战斗信息打包到消息里面
	 * 
	 * @param msg
	 */
	public void packMemberDataToMsg(KGameMessage msg);
	
	/**
	 * 
	 * 销毁
	 * 
	 */
	public void dispose();
	
	/**
	 * 
	 * @param dropId
	 * @return
	 */
	public short getDropOwner(int dropId);
	
	/**
	 * 
	 * @param dropId
	 * @return
	 */
	public ICombatDropInfo getDropInfo(int dropId);
	
	/**
	 * 
	 * 通知开始
	 * 
	 * @param combat
	 */
	public void notifyStart(ICombat combat);
	
	/**
	 * 
	 * 通知单位死亡
	 * 
	 * @param member
	 */
	public void notifyMemberDead(ICombat combat, ICombatMember member);
	
	/**
	 * 
	 * @param combat
	 */
	public void notifyTime(ICombat combat, long currentTime);
	
	/**
	 * 
	 * @param combat
	 * @param result
	 */
	public void processCombatFinish(ICombat combat, ICombatResult result);
	
	/**
	 * 
	 * @param combat
	 * @param result
	 * @param roleResult
	 */
	public void processRoleResult(ICombat combat, ICombatResult result, ICombatRoleResult roleResult);
	
	/**
	 * 
	 * @param msg
	 */
	public void messageReceived(ICombat combat, KGameMessage msg);
	
	interface ICombatGroundBuilder {
		
		/**
		 * 
		 * @param combat
		 * @return
		 */
		ICombatGround build(ICombat combat, AtomicInteger pShadowId);
		
		/**
		 * 
		 */
		void onStartCombatFail();
	}
}
