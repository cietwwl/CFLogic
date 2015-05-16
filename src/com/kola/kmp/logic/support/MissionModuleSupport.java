package com.kola.kmp.logic.support;

import java.util.Collections;
import java.util.List;

import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.npc.KNPCDataStructs.KNPCTemplate;
import com.kola.kmp.logic.npc.menu.IMissionMenu;
import com.kola.kmp.logic.other.KEquipmentTypeEnum;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;

public interface MissionModuleSupport {

	/**
	 * <pre>
	 * 获取角色在指定NPC身上的所有任务
	 * 返回一个可被修改但不影响内部数据的 List
	 * 如果没有任务，请返回 {@link Collections#emptyList()}
	 * 
	 * @param role
	 * @param template
	 * @return 
	 * @author CamusHuang
	 * @creation 2014-3-11 下午5:37:41
	 * </pre>
	 */
	public List<IMissionMenu> getNpcMissionsCopy(KRole role, KNPCTemplate template);

	/**
	 * <pre>
	 * 玩家点击NPC上的任务项时调用
	 * 
	 * @param role
	 * @param missionTempId
	 * @author CamusHuang
	 * @creation 2014-3-11 下午5:18:54
	 * </pre>
	 */
	public void nofityForRoleSelectedMission(KRole role, int missionTempId);
	
	/**
	 * <pre>
	 * 根据任务模版ID，检测任务是否为已接或已完成。如果任务处于已接未完成、完成未提交、完成且已提交状态
	 * 都视为已开放状态，返回true。否则（任务为不可接、可接状态，视为未开放）返回false。
	 * 
	 * @param missionTemplateId
	 * @param role
	 * @return
	 * </pre>
	 */
	public boolean checkMissionIsAcceptedOrCompleted(KRole role,
			int missionTemplateId);

	/**
	 * <pre>
	 * 通知任务系统某项功能使用一次
	 * （注意：是当使用成功才调用该方法）
	 * @param role
	 * @param funType
	 * </pre>
	 */
	public void notifyUseFunction(KRole role, KUseFunctionTypeEnum funType);

	/**
	 * <pre>
	 * 通知任务系统某项功能使用一次
	 * （注意：是当使用成功才调用该方法）
	 * @param roleId
	 * @param funType
	 * </pre>
	 */
	public void notifyUseFunction(long roleId, KUseFunctionTypeEnum funType);

	/**
	 * <pre>
	 * 通知任务系统某项功能使用N次
	 * （注意：是当使用成功才调用该方法）
	 * @param role
	 * @param funType
	 * @param count
	 * </pre>
	 */
	public void notifyUseFunctionByCounts(KRole role,
			KUseFunctionTypeEnum funType, int count);

	/**
	 * <pre>
	 * 通知任务系统某项功能使用N次
	 * （注意：是当使用成功才调用该方法）
	 * @param roleId
	 * @param funType
	 * @param count
	 * </pre>
	 */
	public void notifyUseFunctionByCounts(long roleId,
			KUseFunctionTypeEnum funType, int count);

	/**
	 * <pre>
	 * 通知任务系统某项功能使用一次
	 * （注意：是当使用成功才调用该方法）
	 * @param roleId
	 * @param funType
	 * </pre>
	 */
	public void notifyUseItemFunction(long roleId, KItemTempAbs template,
			int count, KUseFunctionTypeEnum funType);
	
	/**
	 * 通知任务模块主角技能升级成功
	 * 
	 * @param roleId
	 * @param newLv
	 *            最新技能等级
	 */
	public void notifyRoleSkillLv(long roleId, int newLv);
	
	/**
	 * 通知任务模块装备强化成功
	 * 
	 * @param roleId
	 * @param equipType
	 * @param newLv
	 */
	public void notifyStrongEquip(long roleId,
			KEquipmentTypeEnum equipType, int newLv);

	/**
	 * 通知任务模块装备升级成功
	 * 
	 * @param roleId
	 * @param equipType
	 * @param newLv
	 */
	public void notifyUpgrageEquipLv(long roleId,
			KEquipmentTypeEnum equipType, int newLv);

	/**
	 * <pre>
	 * 通知角色VIP等级提升
	 * 
	 * @param roleId  
	 * @param preLv  原来的vip等级
	 * @param newLv  新的vip等级
	 * </pre>
	 */
	public void notifyVipLevelUp(long roleId, int preLv, int newLv);
	

	/**
	 * 通知角色完成第一次新手引导战场
	 * @param role
	 */
	public void notifyCompleteNoviceGuideBattlefield(KRole role);
	
	/**
	 * <pre>
	 * 获取preLv（不包括）与nowLv（包括）等级之间开放的功能id
	 * </pre>
	 * @param preLv
	 * @param nowLv
	 * @return
	 */
	public List<Short> getOpenFuncIds(int preLv, int nowLv);
	

	/**
	 * 检测角色的指定某个功能是否开放
	 * @param role
	 * @param funType
	 * @return
	 */
	public boolean checkFunctionIsOpen(KRole role,KFunctionTypeEnum funType);
}
