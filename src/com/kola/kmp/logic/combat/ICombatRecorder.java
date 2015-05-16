package com.kola.kmp.logic.combat;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatRecorder {
	
	/**
	 * 
	 * @param combat
	 */
	public void setCombat(ICombat combat);
	
	/**
	 * 
	 * @param pMemberId
	 */
	public void setCombetMemberId(short pMemberId);
	
	/**
	 * 
	 * 记录伤害
	 * 
	 * @param quantity
	 */
	public void recordDm(long quantity);
	
	/**
	 * 
	 * 记录同伴的伤害
	 * 
	 * @param quantity
	 */
	public void recordAccompanyDm(long quantity);
	
	/**
	 * 
	 * 记录击杀
	 * 
	 * @param member
	 */
	public void recordKillMember(ICombatMember member);
	
	/**
	 * 
	 * 获取上一次攻击的伤害
	 * 
	 * @return
	 */
	public long getLastDm();
	
	/**
	 * 
	 * 获取自身总伤害
	 * 
	 * @return
	 */
	public long getTotalDm();
	
	/**
	 * 获取属于这个记录器的所有伤害（包括随从、召唤物产生的伤害，都计算在内）
	 * @return
	 */
	public long getTotalDmIncludingAccompany();
	
	/**
	 * 
	 * 获取击杀的成员列表（key=成员的模板id，value=击杀的数量）
	 * 
	 * @return
	 */
	public Map<Integer, Short> getKillMemberMap();
	
	/**
	 * 
	 * 记录回血
	 * 
	 * @param quantity 回血量
	 * @param currentHp 当前的血量
	 */
	public void recordHpRecovery(int quantity, long currentHp);
	
	/**
	 * 
	 * @param qualtity
	 * @param currentEnergy
	 * @param currentEnergyBean
	 */
	public void recordEnergyRecovery(int qualtity, int currentEnergy, int currentEnergyBean);
	
	/**
	 * 
	 * 记录最后一次攻击的目标
	 * 
	 * @param pMember 最后一次攻击的目标
	 */
	public void recordLastTarget(ICombatMember pMember);
	
	/**
	 * 
	 * @param isSkillAttack
	 */
	public void recordLastAttackType(boolean isSkillAttack);
	
	/**
	 * 
	 * 
	 * 获取最后一次攻击的目标
	 * 
	 * @return
	 */
	public ICombatMember getLastTarget();
	
	/**
	 * 
	 * @return
	 */
	public boolean lastAttackTypeIsSkill();
	
	/**
	 * 
	 * @param pMember
	 */
	public void recordLastAttacker(ICombatMember pMember);
	
	/**
	 * 
	 * @return
	 */
	public ICombatMember getLastAttacker();
	
	/**
	 * 
	 * @param happenTime
	 */
	public void recordBeHit(long happenTime);
	
	/**
	 * 
	 * @return
	 */
	public int getBeHitCount();
	
	/**
	 * 
	 * @param happenTime
	 * @param times
	 */
	public void recordAttack(long happenTime);
	
	/**
	 * 
	 * @param pAttackerId
	 * @param pct
	 * @param add
	 * @param hit
	 * @param crit
	 * @param dm
	 */
	public void recordUnderAttack(short pAttackerId, int pct, int add, boolean hit, boolean crit, int dm, boolean isSkillAtk);
	
	/**
	 * 
	 * @param pAttackerId
	 * @param time
	 */
	public void recordBeNormalAttackTime(short pAttackerId, long time);
	
	/**
	 * 
	 * @param pAttackerId
	 * @param time
	 * @return
	 */
	public boolean isNormalAttackDuplicate(short pAttackerId, long time);
	
	/**
	 * 
	 * @return
	 */
	public int getMaxComboAttackCount();
	
	/**
	 * 
	 * @return
	 */
	public int getCurrentComboAttackCount();
	
	/**
	 * 
	 * @return
	 */
	public void recordTimeOfDmByDead(long time);
	
	/**
	 * 
	 * @return
	 */
	public long getTimeOfDmByDead();
	
	/**
	 * 
	 * @return
	 */
	public Map<Short, List<IRecordOfAttack>> getAttackRecord();
	
	/**
	 * 
	 * @param attackerId
	 * @return
	 */
	public List<Long> getNormalAtkTimeRecord(short attackerId);
	
	/**
	 * 
	 */
	public void release();
	
	public interface IRecordOfAttack {
		
		/**
		 * 
		 * @return
		 */
		public int getPct();
		
		/**
		 * 
		 * @return
		 */
		public int getAdd();
		
		/**
		 * 
		 * @return
		 */
		public short getAttackerId();
		
		/**
		 * 
		 * @return
		 */
		public boolean isHit();
		
		/**
		 * 
		 * @return
		 */
		public boolean isCrit();
		
		/**
		 * 
		 * @return
		 */
		public boolean isSkillAtk();
		
		/**
		 * 
		 * @return
		 */
		public int getDm();
	}
}
