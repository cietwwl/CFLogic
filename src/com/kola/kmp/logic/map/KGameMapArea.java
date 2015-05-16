package com.kola.kmp.logic.map;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class KGameMapArea {

	private int areaId;

	private String areaName;

	//主城开放角色等级限制
	private int openRoleLevel;

	//主城开放前置任务限制
	private int openFrontMissionTemplateId;

	private ConcurrentHashMap<Integer, KGameNormalMap> areaMaps = new ConcurrentHashMap<Integer, KGameNormalMap>();

	private KGameNormalMap firstLevelMap;

	/**
	 * 取得大区ID（游戏中的主城）
	 * 
	 * @return
	 */
	public int getAreaId() {
		return areaId;
	}

	public void setAreaId(int areaId) {
		this.areaId = areaId;
	}

	/**
	 * 大区名称（主城名称）
	 * 
	 * @return
	 */
	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	/**
	 * 主城的层数（其实为该区域地图数量）
	 * 
	 * @return
	 */
	public int getLevel() {
		return areaMaps.size();
	}

	/**
	 * 取得该区域所有地图
	 * 
	 * @return
	 */
	public List<KGameNormalMap> getAreaMaps() {
		return new ArrayList<KGameNormalMap>(areaMaps.values());
	}

	/**
	 * 取得区域中某个ID的KGameMap
	 * 
	 * @param mapId
	 * @return
	 */
	public KGameNormalMap getMap(int mapId) {
		return areaMaps.get(mapId);
	}

	/**
	 * 增加区域一个KGameMap地图
	 * 
	 * @param map
	 */
	public void addMap(KGameNormalMap map) {
		areaMaps.put(map.getMapId(), map);
	}

	/**
	 * 取得区域第一层的地图
	 * 
	 * @return
	 */
	public KGameNormalMap getFirstLevelMap() {
		return firstLevelMap;
	}

	/**
	 * 设置第一层地图
	 * 
	 * @param map
	 */
	public void setFirstLevelMap(KGameNormalMap map) {
		this.firstLevelMap = map;
	}
	
	

	public int getOpenRoleLevel() {
		return openRoleLevel;
	}

	public void setOpenRoleLevel(int openRoleLevel) {
		this.openRoleLevel = openRoleLevel;
	}

	public int getOpenFrontMissionTemplateId() {
		return openFrontMissionTemplateId;
	}

	public void setOpenFrontMissionTemplateId(int openFrontMissionTemplateId) {
		this.openFrontMissionTemplateId = openFrontMissionTemplateId;
	}

//	/**
//	 * 检测地图主城是否开放
//	 * @param role
//	 * @return
//	 */
//	public boolean checkAreaIsOpen(KRole role){
//		if(role.getLevel()<openRoleLevel){
//			return false;
//		}
//		KMissionContainer container = KSupportFactory.getDataCacheSupport().getMissionContainer(role.getId());
//		if(container!=null){
//			if(openFrontMissionTemplateId!=0&&!container.checkMissionIsCompleted(openFrontMissionTemplateId)){
//				return false;
//			}
//		}
//		return true;
//	}
}
