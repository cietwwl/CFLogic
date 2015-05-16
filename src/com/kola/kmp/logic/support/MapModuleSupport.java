package com.kola.kmp.logic.support;

import java.util.List;

import com.kola.kmp.logic.map.AutoSearchRoadTrack;
import com.kola.kmp.logic.role.IRoleGameSettingData;
import com.kola.kmp.logic.role.IRoleMapData;
import com.kola.kmp.logic.role.KRole;

public interface MapModuleSupport {
	
	public IRoleMapData newIRoleMapDataInstance();
	
	public IRoleGameSettingData newIRoleGameSettingDataInstance();
	
	/**
	 * 通知客户端退出战场回到主城
	 * @param role
	 */
	public void processRoleFinishBattleReturnToMap(KRole role);

	
	public List<Integer> getAllNpcIdsInMap(KRole role);

	/**
	 * <pre>
	 * 获取指定角色的周围玩家列表
	 * 附近筛选规则：当前角色所在主城所有角色，以对方的vip等级，以及角色等级进行从上往下排列。（vip等级优先角色等级）
	 * 
	 * @param role
	 * @return 不能为NULL
	 * @author CamusHuang
	 * @creation 2014-3-13 下午4:16:32
	 * </pre>
	 */
	public List<Long> getAroundRoleIds(KRole role);
	
	/**
	 * 自动寻路引导至目标NPC，获取其寻路路径
	 * 
	 * @param role
	 *            角色Id
	 * @param npcTemplateId
	 *            npc模版ID
	 * @return
	 */
	public AutoSearchRoadTrack autoDirectToNpc(KRole role,
			int npcTemplateId);

	/**
	 * 自动寻路引导至目标关卡，获取其寻路路径
	 * 
	 * @param role
	 *            橘色ID
	 * @param sccnarioId
	 *            目标关卡对应的场景ID
	 * @param levelId
	 *            目标关卡ID
	 * @return
	 */
	public AutoSearchRoadTrack autoDirectToGameLevel(KRole role,
			int sccnarioId);
	
	/**
	 * 
	 * 通知地图模块，出战宠物发生改变
	 * 
	 * @param roleId
	 */
	public void notifyRoleFightingPetChange(long roleId, long prePetId);
	
	/**
	 * 通知地图模块，角色机甲装备状态
	 * @param isMount       如果为True，表示上马，如果为false表示下马
	 * @param mountResId    如果isMount==true，则发送机甲客户端资源ID，否则填-1；
	 */
	public void notifyMountStatus(long roleId,boolean isMount,int mountResId);
	
	/**
	 * 通知地图模块，角色时装状态
	 * @param fashionData 时装变化数据
	 */
	public void notifyFashionStatus(long roleId,String fashionData);
	
	/**
	 * 处理角色完成新手引导战斗，并跳转到第一个主城场景
	 * @param role
	 */
	public void notifyFinishNoviceGuideBattleAndJumpMap(KRole role);
	
	/**
	 * 处理角色进入普通副本关卡时离开地图
	 * @param role
	 */
	public void processRoleJoinNormalGameLevelAndLeaveMap(KRole role);
	
	/**
	 * 处理角色完成普通副本关卡后返回地图
	 * @param role
	 */
	public void processRoleFinishNormalGameLevelAndReturnToMap(KRole role);

}
