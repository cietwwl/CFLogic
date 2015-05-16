package com.kola.kmp.logic.support;

import java.util.List;

import com.kola.kmp.logic.map.KMap;
import com.kola.kmp.logic.map.KMapEntityData;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMap;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapBornPoint;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KMapDuplicateTypeEnum;
import com.kola.kmp.logic.role.KRole;

public interface KDuplicateMapSupport {

	/**
	 * 根据副本地图结构体ID获取一张副本地图模版
	 * 
	 * @param structMapId
	 *            结构ID
	 * @return
	 */
	public KDuplicateMap getDuplicateMapStruct(int structMapId);

	/**
	 * 创建一张指定副本地图结构体ID的副本地图
	 * 
	 * @param structMapId
	 *            结构ID
	 * @return
	 */
	public KDuplicateMap createDuplicateMap(int structMapId);

	/**
	 * 创建指定数量的副本地图
	 * 
	 * @param structMapId
	 *            结构ID
	 * @return
	 */
	public List<KDuplicateMap> createDuplicateMapByCounts(int structMapId,
			int count);

	/**
	 * 根据副本ID删除一张副本地图
	 * 
	 * @param duplicateMapId
	 *            副本ID
	 * @return
	 */
	public KActionResult<KDuplicateMap> removeDuplicateMap(int duplicateMapId);

	/**
	 * 根据指定副本地图类型删除所有该类型的副本地图
	 * 
	 * @param duplicateType
	 * @return
	 */
	public boolean removeDuplicateMapsByType(int structMapId);

	/**
	 * 根据副本ID获取一张副本地图
	 * 
	 * @param duplicateMapId
	 * @return
	 */
	public KDuplicateMap getDuplicateMap(int duplicateMapId);

	/**
	 * 根据指定副本地图类型获取所有该类型的副本地图
	 * 
	 * @param duplicateType
	 * @return
	 */
	public List<KDuplicateMap> getDuplicateMaps(int structMapId);

	/**
	 * 处理角色进入副本地图
	 * 
	 * @param role
	 * @param duplicateMapId
	 * @return
	 */
	public KActionResult playerRoleJoinDuplicateMap(KRole role,
			int duplicateMapId);

	/**
	 * 处理角色跳转进入副本地图。 （同时支持角色：主城->副本地图、副本地图间的跳转，）
	 * 
	 * @param role
	 * @param duplicateMapId
	 * @return
	 */
	public KActionResult playerRoleJoinDuplicateMap(KRole role,
			int duplicateMapId, float corX, float corY);

	/**
	 * 处理角色离开副本地图，并返回到角色原来的主城地图
	 * 
	 * @param role
	 * @param duplicateMapId,副本地图的副本ID
	 * @return
	 */
	public KActionResult playerRoleLeaveDuplicateMap(KRole role,
			int duplicateMapId);

	/**
	 * 重置角色坐标，回到地图出生点
	 * 
	 * @param role
	 * @param bornPoint
	 * @return
	 */
	public KActionResult resetPlayerRoleToBornPoint(KRole role,
			KDuplicateMapBornPoint bornPoint);

	/**
	 * 通知副本地图中角色战斗状态改变
	 * 
	 * @param role
	 * @param isFight
	 */
	public void notifyPlayerRoleFightStatus(KRole role, boolean isFight);
}
