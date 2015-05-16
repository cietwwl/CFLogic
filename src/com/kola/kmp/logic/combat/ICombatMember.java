package com.kola.kmp.logic.combat;

import java.util.List;
import java.util.Map;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.combat.api.ICombatSkillActor;
import com.kola.kmp.logic.combat.impl.KCombatEntrance;
import com.kola.kmp.logic.combat.operation.IOperation;

/**
 * 
 * 战斗成员
 * 
 * @author PERRY CHAN
 */
public interface ICombatMember {

	/** 成员类型：角色 */
	public static byte MEMBER_TYPE_ROLE = 1;
	/** 成员类型：宠物 */
	public static byte MEMBER_TYPE_PET = 2;
	/** 成员类型：怪物 */
	public static byte MEMBER_TYPE_MONSTER = 3;
	/** 成员类型：载具 */
	public static byte MEMBER_TYPE_VEHICLE = 4;
	/** 成员类型：障碍物 */
	public static byte MEMBER_TYPE_BLOCK = 5;
	/** 成员类型：队友主角 */
	public static byte MEMBER_TYPE_TEAM_MATE_ROLE = 6;
	/** 成员类型：队友宠物 */
	public static byte MEMBER_TYPE_TEAM_MATE_PET = 7;
	/** 成员类型：召唤物 */
	public static byte MEMBER_TYPE_MINION = 8;
	/** 成员类型：精英怪物 */
	public static byte MEMBER_TYPE_ELITIST_MONSTER = 9;
	/** 成员类型：boss怪物 */
	public static byte MEMBER_TYPE_BOSS_MONSTER = 10;
	/** 成员类型：角色（AI）*/
	public static byte MEMBER_TYPE_ROLE_MONSTER = 11;
	/** 成员类型：随从（AI）*/
	public static byte MEMBER_TYPE_PET_MONSTER = 12;
	/** 成员类型：小助手 */
	public static byte MEMBER_TYPE_ASSISTANT = 13;
	/** 成员类型：产金活动怪物（油桶）*/
	public static byte MEMBER_TYPE_BARREL_MONSTER = 14;
	/** 成员类型：世界boss其他玩家 */
	public static byte MEMBER_TYPE_WORLD_BOSS_OTHER_ROLE = 15;
	/** 成员类型：世界boss其他随从 */
	public static byte MEMBER_TYPE_WORLD_BOSS_OTHER_PET = 16;
	
	/**
	 * 
	 * @param pShadowId
	 * @param entrance
	 */
	public void init(byte forceType, short pShadowId, long pCreateTime, KCombatEntrance entrance, ICombat combat);
	
	/**
	 * 
	 */
	public void release();
	
	/**
	 * 
	 * @return
	 */
	public ICombatSkillActor getSkillActor();
	
	/**
	 * 
	 * @return
	 */
	public ICombatRecorder getCombatRecorder();
	
	/**
	 * 
	 * @return
	 */
	public Map<Integer, ICombatMinion> getCombatMinions();
	
//	/**
//	 * 
//	 * @param start
//	 * @param end
//	 * @return
//	 */
//	public List<IOperation> getPeriodOperation(long start, long end);
	
	/**
	 * 
	 * @param eventId
	 */
	public void combatEventNotify(int eventId, long happenTime);
	
	/**
	 * 
	 * 把自身的数据填充到消息
	 * 
	 * @param msg
	 */
	public void packDataToMsg(KGameMessage msg);
	
	/**
	 * 
	 * 切换武器
	 * 
	 * @param switchToSecond 是否切换到副武器
	 */
	public void switchWeapon(boolean switchToSecond);
	
	/**
	 * 
	 * 处理聚力
	 * 
	 * @param cohensionTime 聚力的时间（毫秒）
	 * @param clientResult 客户端计算出来的结果
	 */
	public void processCohension(int cohensionTime, int clientResult);
	
	/**
	 * 
	 * 切换格挡状态
	 * 
	 * @param start
	 */
	public void switchBlockStatus(boolean start);
	
	/**
	 * 
	 * @param pEscape
	 */
	public void setEscape(boolean pEscape);
	
	/**
	 * 
	 * @return
	 */
	public boolean isEscape();
	
	/**
	 * 
	 * 获取源对象id
	 * 
	 * @return
	 */
	public long getSrcObjId();
	
	/**
	 * 
	 * 获取源对象类型
	 * 
	 * @return
	 */
	public byte getSrcObjType();
	
	/**
	 * 
	 * @return
	 */
	public int getSrcObjTemplateId();
	
	/**
	 * 
	 * 获取战斗对象类型
	 * 
	 * @return
	 */
	public byte getMemberType();
	
	/**
	 * 
	 * 是否广义上的怪物，以下类型会返回true
	 * {@link #MEMBER_TYPE_BOSS_MONSTER}
	 * {@link #MEMBER_TYPE_MONSTER}
	 * {@link #MEMBER_TYPE_ELITIST_MONSTER}
	 * {@link #MEMBER_TYPE_BARREL_MONSTER}
	 * {@link #MEMBER_TYPE_ROLE_MONSTER}
	 * {@link #MEMBER_TYPE_PET_MONSTER}
	 * @return
	 */
	public boolean isGeneralMonster();
	
	/**
	 * 
	 * 是否狭义上的怪物，以下类型会返回true
	 * {@link #MEMBER_TYPE_BOSS_MONSTER}
	 * {@link #MEMBER_TYPE_MONSTER}
	 * {@link #MEMBER_TYPE_ELITIST_MONSTER}
	 * {@link #MEMBER_TYPE_BARREL_MONSTER}
	 * @return
	 */
	public boolean isNarrowMonster();
	
	/**
	 * 
	 * 获取战场对象id
	 * 
	 * @return
	 */
	public short getShadowId();
	
	/**
	 * 
	 * 获取属于的战场“势力”，只能是以下比常量：
	 * {@link ICombatForce#FORCE_TYPE_ROLE_SIDE}
	 * {@link ICombatForce#FORCE_TYPE_MONSTER_SIDE}
	 * {@link ICombatForce#FORCE_TYPE_NEUTRAL}
	 * 
	 * @return
	 */
	public byte getForceType();
	
	/**
	 * 
	 * 获取名字
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * 
	 * @return
	 */
	public int getLv();
	
	/**
	 * 
	 * 是否能被攻击
	 * 
	 * @return
	 */
	public boolean canBeAttacked();
	
	/**
	 * 
	 * @return
	 */
	public boolean isHang();
	
	
	/**
	 * 
	 * 是否能操作
	 * 
	 * @return
	 */
	public boolean canOperate();
	
	/**
	 * 
	 * 是否在同一个阵营
	 * 
	 * @return
	 */
	public boolean isInDiffForce(ICombatMember member);
	
	/**
	 * 
	 */
	public void resume();
	
	/**
	 * 
	 * <pre>
	 * 获取x坐标
	 * </pre>
	 * 
	 * @return
	 */
	public float getX();
	
	/**
	 * 
	 * <pre>
	 * 获取y坐标
	 * </pre>
	 * 
	 * @return
	 */
	public float getY();
	
	/**
	 * 
	 * 获取当前HP
	 * 
	 * @return
	 */
	public long getCurrentHp();
	
	/**
	 * 
	 * 获取原始的生命上限
	 * 
	 * @return
	 */
	public long getSrcMaxHp();
	
	/**
	 * 
	 * 获取生命上限
	 * 
	 * @return
	 */
	public long getMaxHp();
	
	/**
	 * 
	 * 获取当前怒气值
	 * 
	 * @return
	 */
	public int getCurrentEnergy();
	
	/**
	 * 
	 * 获取怒气豆数量
	 * 
	 * @return
	 */
	public int getCurrentEnergyBean();
	
	/**
	 * 
	 * 获取HP恢复速度
	 * 
	 * @return
	 */
	public int getHpRecovery();
	
	/**
	 * 
	 * @return
	 */
	public int getAtkCountPerTime();
	
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
	 * 获取命中率（万分比）
	 * 
	 * @return
	 */
	public int getHitRating();
	
	/**
	 * 
	 * 获取闪避率（万分比）
	 * 
	 * @return
	 */
	public int getDodgeRating();
	
	/**
	 * 
	 * 获取暴击率（万分比）
	 * 
	 * @return
	 */
	public int getCritRating();
	
	/**
	 * 
	 * 获取抗暴率（万分比）
	 * 
	 * @return
	 */
	public int getResilienceRating();
	
	/**
	 * 
	 * 获取暴击伤害加成的比例（万分比）
	 * 
	 * @return
	 */
	public int getCritMultiple();
	
	/**
	 * 
	 * 获取每次伤害转化为自身HP的血量百分比
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
	 * 获取技能伤害加成比例
	 * 
	 * @return
	 */
	public int getSkillDmPctInc();
	
	/**
	 * 
	 * 获取伤害减免比例
	 * 
	 * @return
	 */
	public int getDmReducePct();
	
	/**
	 * 
	 * @return
	 */
	public int getKilledEnergy();
	
	/**
	 * 
	 * @return
	 */
	public long getDeadTime();
	
	/**
	 * 
	 * 是否生还的
	 * 
	 * @return
	 */
	public boolean isAlive();
	
	/**
	 * <pre>
	 * 强制设为死亡
	 * </pre>
	 */
	public void sentenceToDead(long happenTime);
	
	/**
	 * 
	 * 增加HP
	 * 
	 * @param quantity
	 */
	public void increaseHp(long quantity);
	
	/**
	 * 
	 * 扣减HP
	 * 
	 * @param quantity
	 */
	public void decreaseHp(long quantity, long happenTime);
	
	/**
	 * 
	 * 增加怒气
	 * 
	 * @param quantity
	 */
	public void increaseEnergy(int quantity);
	
	/**
	 * 
	 * @param effect
	 */
	public void registPermanentEffect(ICombatEventListener effect);

	/**
	 * 
	 * 召唤机甲
	 * 
	 * @param templateId
	 * @param happenTime
	 */
	public void summonMount(long happenTime);
	
	/**
	 * 
	 * 减低生存时间
	 * 
	 * @param reduceCount
	 */
	public void reduceSurviveTime(long reduceCount);
	
	/**
	 * 
	 * 获取终结的时间
	 * 
	 * @return
	 */
	public long getTerminateTime();
	
	/**
	 * 
	 * 是否随着时间而终结
	 * 
	 * @return
	 */
	public boolean isTerminateByTime();
	
	/**
	 * 
	 * 每次攻击是否扣取特定比例的血量
	 * 
	 * @return
	 */
	public boolean isAtkByPercentage();
	
	/**
	 * 
	 * 是否霸体
	 * 
	 * @return
	 */
	public boolean isFullImmunity();
	
	/**
	 * 
	 * 获取伤害格挡值
	 * 
	 * @return
	 */
	public int getDmBlock();
	
	/**
	 * 获取副武器伤害加成百分比
	 * 
	 * @return
	 */
	public int getSecondWeaponDmPct();
	
	/**
	 * 
	 * 获取副武器固定伤害加成
	 * 
	 * @return
	 */
	public int getSecondWeaponFixedDm();
	
	/**
	 * 屏蔽超杀技能
	 */
	public void blockSuperSkill();
	
	/**
	 * 屏蔽机甲
	 */
	public void blockMount();
}
