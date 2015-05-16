package com.kola.kmp.logic.combat.api;

import com.kola.kmp.logic.combat.ICombatMember;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatObjectBase {

	/** 对象类型：角色 */
	public static final byte OBJECT_TYPE_ROLE = ICombatMember.MEMBER_TYPE_ROLE;
	/** 对象类型：角色怪物 */
	public static final byte OBJECT_TYPE_ROLE_MONSTER = ICombatMember.MEMBER_TYPE_ROLE_MONSTER;
	/** 对象类型：宠物 */
	public static final byte OBJECT_TYPE_PET = ICombatMember.MEMBER_TYPE_PET;
	/** 对象类型：怪物 */
	public static final byte OBJECT_TYPE_MONSTER = ICombatMember.MEMBER_TYPE_MONSTER;
	/** 对象类型：载具 */
	public static final byte OBJECT_TYPE_VEHICLE = ICombatMember.MEMBER_TYPE_VEHICLE;
	/** 对象类型：障碍物 */
	public static final byte OBJECT_TYPE_BLOCK = ICombatMember.MEMBER_TYPE_BLOCK;
	/** 对象类型：队友主角 */
	public static final byte OBJECT_TYPE_MATE_ROLE = ICombatMember.MEMBER_TYPE_TEAM_MATE_ROLE;
	/** 对象类型：队友宠物 */
	public static final byte OBJECT_TYPE_MATE_PET = ICombatMember.MEMBER_TYPE_TEAM_MATE_PET;
	/** 对象类型：召唤物 */
	public static final byte OBJECT_TYPE_MINION = ICombatMember.MEMBER_TYPE_MINION;
	/** 对象类型：精英怪物 */
	public static final byte OBJECT_TYPE_MONSTER_ELITIST = ICombatMember.MEMBER_TYPE_ELITIST_MONSTER;
	/** 对象类型：boss怪物 */
	public static final byte OBJECT_TYPE_MONSTER_BOSS = ICombatMember.MEMBER_TYPE_BOSS_MONSTER;
	
	/** 目标类型：没有目标 */
	public static final byte TARGET_TYPE_NONE = 0;
	/** 目标类型：主角 */
	public static final byte TARGET_TYPE_ROLE = 1;
	/** 目标类型：怪物 */
	public static final byte TARGET_TYPE_MONSTER = 2;
	/** 目标类型：所有目标 */
	public static final byte TARGET_TYPE_ALL = 3;
	/**
	 * <pre>
	 * 对象是否能被攻击
	 * </pre>
	 * 
	 * @return
	 */
	public boolean canBeAttack();
	
	/**
	 * 
	 * 返回对象的类型
	 * 
	 * @return
	 */
	public byte getObjectType();
	
	/**
	 * 
	 * 获取模板id
	 * 
	 * @return
	 */
	public int getTemplateId();
	
	/**
	 * 
	 * 获取唯一id
	 * 
	 * @return
	 */
	public long getId();
	
	/**
	 * 
	 * 获取名字
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * 
	 * 获取头像资源id
	 * 
	 * @return
	 */
	public int getHeadResId();
	
	/**
	 * 
	 * 获取场景资源id
	 * 
	 * @return
	 */
	public int getInMapResId();
	
	/**
	 * 
	 * 获取等级
	 * 
	 * @return
	 */
	public int getLevel();
	
	/**
	 * 
	 * 获取当前的HP
	 * 
	 * @return
	 */
	public long getCurrentHp();
	
	/**
	 * 
	 * 获取HP上限
	 * 
	 * @return
	 */
	public long getMaxHp();
	
	/**
	 * 
	 * 获取x移动速度
	 * 
	 * @return
	 */
	public int getBattleMoveSpeedX();
	
	/**
	 * 
	 * 获取y移动速度
	 * 
	 * @return
	 */
	public int getBattleMoveSpeedY();
	
	/**
	 * 
	 * 获取视野
	 * 
	 * @return
	 */
	public int getVision();
}
