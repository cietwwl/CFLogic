package com.kola.kmp.logic.combat;

import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.level.ICombatAdditionalReward;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatDropInfo {

	AtomicInteger idGenerator = new AtomicInteger();
	
//	public static final Logger LOGGER = KGameLogger.getLogger(ICombatDropInfo.class);
	
	/** 掉落类型：普通 */
	public static final byte DROP_TYPE_COMMON = 1;
	/** 掉落类型：宠物 */
	public static final byte DROP_TYPE_PET = 2;
	
	/**
	 * 获取唯一的序列id，建议使用{@link #idGenerator}生成
	 * 
	 * @return
	 */
	public int getSerialId();
	
	/**
	 * 
	 * @return
	 */
	public int getResId();
	
	/**
	 * 
	 * @return
	 */
	public byte getType();
	
	/**
	 * 
	 * @return
	 */
	public String getDescr();
	
	/**
	 * 
	 * 详细信息
	 * 
	 * @return
	 */
	public String getDetail();
	
	/**
	 * 
	 */
	public void packAdditionalInfoToMsg(KGameMessage msg);
	
	/**
	 * 
	 * @param reward
	 */
	public void executeReward(ICombat combat, ICombatMember member, ICombatAdditionalReward reward, long happendTime);
	
}
