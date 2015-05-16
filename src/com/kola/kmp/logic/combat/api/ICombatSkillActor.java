package com.kola.kmp.logic.combat.api;

import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.skill.ICombatSkillExecution;
import com.kola.kmp.logic.other.KGameAttrType;

/**
 * 
 * 战斗技能的执行者
 * 
 * @author PERRY CHAN
 */
public interface ICombatSkillActor {
	
	/**
	 * 
	 * 获取战场对象id
	 * 
	 * @return
	 */
	public short getShadowId();

	/**
	 * 
	 * 记录技能使用记录
	 * 
	 * @param skillTemplateId 技能模板id
	 * @param useCode 客户端提供的技能使用代号（必须是战斗唯一的）
	 * @param happenTime 技能触发时间
	 */
	public void recordSkillUsed(int skillTemplateId, short targetId, String useCode, long happenTime);
	
	/**
	 * 
	 * @param skillTemplateId
	 * @param useCode
	 * @param happenTime
	 */
	public void recordSkillUse(int skillTemplateId, String useCode, long happenTime);
	
	/**
	 * 
	 * 获取useCode为标识的技能已经结算了多少次
	 * 
	 * @param skillTemplateId
	 * @param useCode
	 * @return
	 */
	public int getSkillSettleCount(int skillTemplateId, String useCode);
	
	/**
	 * 
	 * @param skillTemplateId
	 * @param useCode
	 * @return
	 */
	public boolean isTimeInSettleRecord(int skillTemplateId, String useCode, long time);
	
//	/**
//	 * 
//	 * 获取useCode的happenTime是否已经结算过
//	 * 
//	 * @param skillTemplateId
//	 * @param useCode
//	 * @param happenTime
//	 * @return
//	 */
//	public boolean isTimeHasBeenSettleOfThisTarget(int skillTemplateId, short targetId, String useCode, long happenTime);
	
	/**
	 * 
	 * 检查给定的时间，是不是当前这个技能的第一次结算的时间
	 * 
	 * @param skillTemplateId
	 * @param useCode
	 * @param happenTime
	 * @return
	 */
	public boolean isTimeFirstSettle(int skillTemplateId, String useCode, long happenTime);
	
	/**
	 * 
	 * @param skillTemplateId
	 * @param useCode
	 * @param happenTime
	 * @return
	 */
	public int getTargetSettleCount(int skillTemplateId, String useCode, short targetId, long happenTime);
	
	/**
	 * 
	 * @param skillTemplateId
	 * @param coolDownEndTime
	 */
	public void recordSkillCoolDown(int skillTemplateId, long happenTime, long cooldownMillis);
	
	/**
	 * 
	 * @param skillTemplateId
	 * @param reduceMillis
	 */
	public void reduceSkillCoolDown(int skillTemplateId, int reduceMillis);

	/**
	 * 
	 * @param skillTemplateId
	 * @return
	 */
	public boolean isCanUseSkill(int skillTemplateId, String useCode, long skillUseTime);
	
	/**
	 * 
	 * @param skillTemplateId
	 * @param useCode
	 * @return
	 */
	public boolean isNewSettle(int skillTemplateId, String useCode);
	
	/**
	 * 
	 * @param skillTemplateId
	 * @param useCode
	 * @return
	 */
	public boolean executeSkillConsume(int skillTemplateId, String useCode);
	
	/**
	 * 
	 * @param skillTemplateId
	 * @return
	 */
	public ICombatSkillExecution getSkillExecution(int skillTemplateId);
	
//	/**
//	 * 
//	 * <pre>
//	 * 增加HP
//	 * </pre>
//	 * 
//	 * @param qualtity
//	 */
//	public void increaseHp(long qualtity);
	
	/**
	 * 
	 * <pre>
	 * 扣减HP
	 * </pre>
	 * 
	 * @param quantity
	 */
	public void decreaseHp(long quantity, long happenTime);
	
	/**
	 * 
	 * <pre>
	 * 扣减怒气豆
	 * </pre>
	 * 
	 * @param count
	 * @return
	 */
	public boolean decreaseEnergyBean(int count);
	
	/**
	 * 
	 * @return
	 */
	public long getMaxHp();
	
	/**
	 * 
	 * @return
	 */
	public long getCurrentHp();
	
	/**
	 * <pre>
	 * 修改战斗属性，仅限以下属性：
	 * 生命上限：{@link KGameAttrType#MAX_HP}
	 * 攻击力：{@link KGameAttrType#ATK}
	 * 防御力：{@link KGameAttrType#DEF}
	 * 命中等级：{@link KGameAttrType#HIT_RATING}
	 * 闪避等级：{@link KGameAttrType#DODGE_RATING}
	 * 暴击等级：{@link KGameAttrType#CRIT_RATING}
	 * 抗暴等级：{@link KGameAttrType#RESILIENCE_RATING}
	 * 暴击加成：{@link KGameAttrType#CRIT_MULTIPLE}
	 * 生命上限（万分比）：{@link KGameAttrType#MAX_HP_PCT}
	 * 攻击力（万分比）：{@link KGameAttrType#ATK_PCT}
	 * 防御力（万分比）：{@link KGameAttrType#DEF_PCT}
	 * 命中等级（万分比）：{@link KGameAttrType#HIT_RATING_PCT}
	 * 闪避等级（万分比）：{@link KGameAttrType#DODGE_RATING_PCT}
	 * 暴击等级（万分比）：{@link KGameAttrType#CRIT_RATING_PCT}
	 * 抗暴等级（万分比）：{@link KGameAttrType#RESILIENCE_RATING_PCT}
	 * </pre>
	 * @param attrType 改变的数值类型
	 * @param quantity 改变的数值，正数表示增加，负数表示扣减
	 */
	public void changeCombatAttr(KGameAttrType attrType, int quantity, boolean add);
	
	/**
	 * 
	 * @param stateId
	 */
	public void addState(ICombatMember operator, int stateId, long happenTime);
	
	/**
	 * 
	 * @param stateTemplateId
	 * @param happenTime
	 */
	public void checkAndRemoveState(int stateTemplateId, long happenTime);

	/**
	 * 
	 * @param effectId
	 */
	public void addTemporaryEffec(int effectId);
	
	/**
	 * 
	 * @param effectId
	 */
	public void removeRemporaryEffect(int effectId);
	
	/**
	 * 
	 * @param add
	 */
	public void handleFaint(boolean add);
	
	/**
	 * 
	 * @param add
	 */
	public void handleFreeze(boolean add);
	
	/**
	 * 
	 * 处理霸体
	 * 
	 * @param add
	 */
	public void handleFullImmunity(boolean add);
	
	/**
	 * 
	 * 处理无敌
	 * 
	 * @param add
	 */
	public void handleInvincible(boolean add);
	
	/**
	 * 
	 * 召唤
	 * 
	 * @param templateId
	 */
	public void summon(int minionTemplateId, int count, long happenTime);
}
