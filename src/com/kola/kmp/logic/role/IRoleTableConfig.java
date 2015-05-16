package com.kola.kmp.logic.role;

/**
 * 
 * @author PERRY CHAN
 */
public interface IRoleTableConfig {

	/** 角色模板表 */
	public static final byte TABLE_ROLE_TEMPLATE_DATA = 1;
	/** 属性模板表 */
	public static final byte TABLE_ROLE_ATTRIBUTE_DATA = 2;
	/** 升级经验表 */
	public static final byte TABLE_ROLE_EXP_DATA = 3;
	/** 角色模块逻辑配置 */
	public static final byte TABLE_ROLE_MODULE_LOGIC_CONFIG = 4;
	/** 突击战士成长属性 */
	public static final byte TABLE_WARRIOR_UP_LV_ATTR = 5;
	/** 暗影特工成长属性 */
	public static final byte TABLE_MAGICIAN_UP_LV_ATTR = 6;
	/** 枪械师成长属性 */
	public static final byte TABLE_BOWMAN_UP_LV_ATTR = 7;
	/** 战斗力参数表 */
	public static final byte TABLE_BATTLE_POWER_PARA = 8;
	/** PVP血量加成 */
	public static final byte TABLE_PVP_HP_MULTIPLE = 9;
}
