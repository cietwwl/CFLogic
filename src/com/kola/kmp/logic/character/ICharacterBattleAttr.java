package com.kola.kmp.logic.character;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICharacterBattleAttr {

	/**
	 * 
	 * 获取角色的生命上限
	 * 
	 * @return
	 */
	public long getMaxHp();
	
	/**
	 * 
	 * 获取角色的怒气上限
	 * 
	 * @return
	 */
	public int getMaxEnergy();
	
	/**
	 * 
	 * 获取角色的生命恢复速度
	 * 
	 * @return
	 */
	public int getHpRecovery();
	
	/**
	 * 
	 * 获取攻击力
	 * 
	 * @return
	 */
	public int getAtk();
	
	/**
	 * 
	 * 获取防御力
	 * 
	 * @return
	 */
	public int getDef();
	
	/**
	 * 
	 * 获取暴击等级
	 * 
	 * @return
	 */
	public int getCritRating();
	
	/**
	 * 
	 * 获取闪避等级
	 * 
	 * @return
	 */
	public int getDodgeRating();
	
	/**
	 * 
	 * 获取命中等级
	 * 
	 * @return
	 */
	public int getHitRating();
	
	/**
	 * 
	 * 获取抗爆等级
	 * 
	 * @return
	 */
	public int getResilienceRating();
	
	/**
	 * 
	 * @return
	 */
	public int getFaintResistRating();
	
	/**
	 * 
	 * 获取暴击伤害加上
	 * 
	 * @return
	 */
	public int getCritMultiple();
	
	/**
	 * 
	 * 获取冷却加成
	 * 
	 * @return
	 */
	public int getCdReduce();
	
	/**
	 * 
	 * 获取生命吸收的比例
	 * 
	 * @return
	 */
	public int getHpAbsorb();
	
	/**
	 * 
	 * 获取无视防御的值
	 * 
	 * @return
	 */
	public int getDefIgnore();
	
	/**
	 * 
	 * 获取近程攻击速度
	 * 
	 * @return
	 */
	public int getShortRaAtkItr();
	
	/**
	 * 
	 * 获取远程攻击速度
	 * 
	 * @return
	 */
	public int getLongRaAtkItr();
	
	/**
	 * 
	 * 获取近程攻击距离
	 * 
	 * @return
	 */
	public int getShortRaAtkDist();
	
	/**
	 * 
	 * 获取远程攻击距离
	 * 
	 * @return
	 */
	public int getLongRaAtkDist();
	
	/**
	 * 
	 * 获取移动速度
	 * 
	 * @return
	 */
	public int getMoveSpeedX();
	
	/**
	 * 
	 * 获取移动速度y
	 * 
	 * @return
	 */
	public int getMoveSpeedY();
	
	/**
	 * 
	 * @return
	 */
	public int getBattleMoveSpeedX();
	
	/**
	 * 
	 * @return
	 */
	public int getBattleMoveSpeedY();
	
	/**
	 * 
	 * 获取格挡伤害
	 * 
	 * @return
	 */
	public int getBlock();
	
	/**
	 * 
	 * 获取聚力伤害
	 * 
	 * @return
	 */
	public int getCohesionDm();
	
	/**
	 * 
	 * 获取子弹伤害
	 * 
	 * @return
	 */
	public int getBulletDm();
	
}
