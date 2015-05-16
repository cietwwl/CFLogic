package com.kola.kmp.logic.map.duplicatemap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import jxl.read.biff.BiffException;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.map.BroadcastMapEntityStateTimerTask;
import com.kola.kmp.logic.map.KCoordinate;
import com.kola.kmp.logic.map.KGameMapData;
import com.kola.kmp.logic.map.KGameMapEntity;
import com.kola.kmp.logic.map.KGameMapManager;
import com.kola.kmp.logic.map.KGameMapModuleException;
import com.kola.kmp.logic.map.KGameNormalMap;
import com.kola.kmp.logic.map.KMapEntityData;
import com.kola.kmp.logic.map.KMapModule;
import com.kola.kmp.logic.map.PlayerRoleJoinLeaveMapEventListener;
import com.kola.kmp.logic.map.PlayerRolePrivateEntityList;
import com.kola.kmp.logic.map.KGameMapEntity.PetMapEntityShowData;
import com.kola.kmp.logic.map.KGameMapEntity.RoleMapEntityShowData;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KMapDuplicateTypeEnum;
import com.kola.kmp.logic.other.KMapEntityTypeEnum;
import com.kola.kmp.logic.other.KMapTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.map.KMapProtocol;

public class KDuplicateMapManager {
	private static final KGameLogger _LOGGER = KGameLogger.getLogger(KDuplicateMapManager.class);

	private static KDuplicateMapManager instace;

	private ReentrantLock lock = new ReentrantLock();
	/**
	 * 副本地图数据结构源对象Map，Key：地图编号（配置表编号），
	 */
	private static Map<Integer, KDuplicateMapImpl> structMaps = new HashMap<Integer, KDuplicateMapImpl>();

	private static Map<KMapDuplicateTypeEnum, Map<Integer, KDuplicateMapImpl>> structMapsByType = new HashMap<KMapDuplicateTypeEnum, Map<Integer, KDuplicateMapImpl>>();

	private static Map<Integer, Map<Integer, KDuplicateMapImpl>> duplicateMapsByIdType = new ConcurrentHashMap<Integer, Map<Integer, KDuplicateMapImpl>>();

	private final ConcurrentHashMap<Integer, KDuplicateMapImpl> allDuplicateMaps = new ConcurrentHashMap<Integer, KDuplicateMapImpl>();

	private static DuplcateMapIdGenerator duplcateMapIdGenerator = new DuplcateMapIdGenerator(1);

	public KDuplicateMapManager() {
		instace = this;
	}

	public static KDuplicateMapManager getInstace() {
		if (instace == null) {
			instace = new KDuplicateMapManager();
		}
		return instace;
	}

	public void init(String configPath) throws Exception {
		for (KMapDuplicateTypeEnum type : KMapDuplicateTypeEnum.values()) {
			structMapsByType.put(type, new ConcurrentHashMap<Integer, KDuplicateMapImpl>());
		}
		loadMapDataExcelFile(configPath);
		// debugTest();
		KGame.newTimeSignal(new DuplicateBroadcastMapEntityStateTimerTask(), KGameMapManager.broadcastEntityStateChangeMsgPackTimeSeconds, TimeUnit.SECONDS);
	}

	private void loadMapDataExcelFile(String path) throws Exception {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(path);
		} catch (BiffException e) {
			throw new KGameServerException("读取地图excel表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取地图excel表头发生错误！", e);
		}

		if (xlsFile != null) {
			// 读取副本地图结构工作表
			int areaDataRowIndex = 3;
			KGameExcelTable areaDataTable = xlsFile.getTable("副本地图结构", areaDataRowIndex);
			KGameExcelRow[] allRows = areaDataTable.getAllDataRows();
			if (allRows != null) {
				for (int i = 0; i < allRows.length; i++) {
					// 先加载地图基础数据
					int mapId = allRows[i].getInt("mapId");
					_LOGGER.info("加载副本地图数据，地图ID" + mapId);
					String mapName = allRows[i].getData("mapName");
					byte duplicateType = allRows[i].getByte("mapType");
					int mapResId = allRows[i].getInt("map_res_id");
					String mapDataFileName = allRows[i].getData("map_data_file_name");
					if (mapDataFileName == null) {
						throw new KGameServerException("读取副本地图的map_data_file_name=" + mapDataFileName + "错误，这两个值不能为空！！，Row=" + allRows[i].getIndexInFile());
					}
					String mapDesc = allRows[i].getData("description");
					int mapGroupId = allRows[i].getInt("mapGroupId");
					int bgMusicResId = allRows[i].getInt("musicId");
					int joinMapEfficiencyId = allRows[i].getInt("joinMapEfficiencyId");
					int unitLimit = allRows[i].getInt("showCount");
					byte unitLimitType;
					if (unitLimit < 0) {
						unitLimitType = KDuplicateMapImpl.UNIT_LIMIT_TYPE_UNLIMIT;
					} else if (unitLimit > 0) {
						unitLimitType = KDuplicateMapImpl.UNIT_LIMIT_TYPE_LIMIT;
					} else {
						unitLimitType = KDuplicateMapImpl.UNIT_LIMIT_TYPE_NOT_SHOW;
					}
					boolean isShowPet = allRows[i].getBoolean("showPet");

					KDuplicateMapImpl map = new KDuplicateMapImpl(mapId, mapGroupId, KMapTypeEnum.副本地图.type, duplicateType, unitLimitType, unitLimit, isShowPet, mapName, mapDesc);

					map.setMapResId(mapResId);
					map.setBgMusicResId(bgMusicResId);
					map.setMapDataFileName(mapDataFileName);
					map.setJoinMapEfficiencyId(joinMapEfficiencyId);
					String filePath = KGameMapManager.mapNpcXmlFilePath + mapDataFileName + ".assetbundle";
					map.init(filePath, allRows[i]);

					structMaps.put(mapId, map);
					structMapsByType.get(map.duplicateType).put(map.getMapId(), map);
				}
			}
		}
	}

	public static DuplcateMapIdGenerator getDuplcateMapIdGenerator() {
		return duplcateMapIdGenerator;
	}

	public static KDuplicateMap getStructMap(int structMapId) {
		return structMaps.get(structMapId);
	}

	public KDuplicateMap createDuplicateMap(int structMapId) {

		if (structMaps.containsKey(structMapId)) {
			if (lock.tryLock()) {
				try {
					KDuplicateMapImpl duplicateMap = (KDuplicateMapImpl) structMaps.get(structMapId).clone();
					if (duplicateMap != null) {
						allDuplicateMaps.put(duplicateMap.getDuplicateId(), duplicateMap);
						if (!duplicateMapsByIdType.containsKey(duplicateMap.getMapId())) {
							duplicateMapsByIdType.put(duplicateMap.getMapId(), new ConcurrentHashMap<Integer, KDuplicateMapImpl>());
						}
						duplicateMapsByIdType.get(duplicateMap.getMapId()).put(duplicateMap.getDuplicateId(), duplicateMap);
						return duplicateMap;
					}
				} finally {
					lock.unlock();
				}
			}
		}

		return null;
	}

	public List<KDuplicateMap> createDuplicateMapByCounts(int structMapId, int count) {
		List<KDuplicateMap> list = new ArrayList<KDuplicateMap>();
		if (structMaps.containsKey(structMapId)) {
			if (lock.tryLock()) {
				try {
					for (int i = 0; i < count; i++) {
						KDuplicateMapImpl duplicateMap = (KDuplicateMapImpl) structMaps.get(structMapId).clone();
						if (duplicateMap != null) {
							allDuplicateMaps.put(duplicateMap.getDuplicateId(), duplicateMap);
							if (!duplicateMapsByIdType.containsKey(duplicateMap.getMapId())) {
								duplicateMapsByIdType.put(duplicateMap.getMapId(), new ConcurrentHashMap<Integer, KDuplicateMapImpl>());
							}
							duplicateMapsByIdType.get(duplicateMap.getMapId()).put(duplicateMap.getDuplicateId(), duplicateMap);
							list.add(duplicateMap);
						}
					}
				} finally {
					lock.unlock();
				}
			}
		}
		return list;
	}

	public KActionResult<KDuplicateMap> removeDuplicateMap(int duplicateMapId) {
		if (allDuplicateMaps.containsKey(duplicateMapId)) {
			if (lock.tryLock()) {
				try {
					KDuplicateMapImpl duplicateMap = allDuplicateMaps.remove(duplicateMapId);
					if (duplicateMap != null) {
						if (duplicateMapsByIdType.containsKey(duplicateMap.getMapId())) {
							duplicateMapsByIdType.get(duplicateMap.getMapId()).remove(duplicateMap.getDuplicateId());
						}
						duplicateMap.dispose();
						return new KActionResult<KDuplicateMap>(true, "", duplicateMap);
					}
				} finally {
					lock.unlock();
				}
			}
		}
		return new KActionResult<KDuplicateMap>(false, "", null);
	}

	public boolean removeDuplicateMapsByType(int structMapId) {
		boolean result = false;
		if (lock.tryLock()) {
			try {
				if (duplicateMapsByIdType.remove(structMapId) != null) {
					result = true;
				}
				List<Integer> removeDuplicateIdList = new ArrayList<Integer>();
				for (Integer duplicateId : allDuplicateMaps.keySet()) {
					KDuplicateMapImpl duplicateMap = allDuplicateMaps.get(duplicateId);
					if (duplicateMap.getMapId() == structMapId) {
						removeDuplicateIdList.add(duplicateId);
					}
				}
				if (!removeDuplicateIdList.isEmpty()) {
					result = true;
					for (Integer duplicateId : removeDuplicateIdList) {
						KDuplicateMapImpl duplicateMap = allDuplicateMaps.remove(duplicateId);
						if (duplicateMap != null) {
							duplicateMap.dispose();
						}
					}
				}
			} finally {
				lock.unlock();
			}
		}
		return result;
	}

	public Map<Integer, KDuplicateMapImpl> getAllDuplicateMaps() {
		return allDuplicateMaps;
	}

	public KDuplicateMap getDuplicateMap(int duplicateMapId) {
		return allDuplicateMaps.get(duplicateMapId);
	}

	public List<KDuplicateMap> getDuplicateMaps(int structMapId) {
		List<KDuplicateMap> list = new ArrayList<KDuplicateMap>();
		if (duplicateMapsByIdType.containsKey(structMapId)) {
			list.addAll(duplicateMapsByIdType.get(structMapId).values());
		}
		return list;
	}

	public KActionResult playerRoleJoinDuplicateMap(KRole role, int duplicateMapId) {
		KGameNormalMap srcMap = KMapModule.getGameMapManager().getGameMap(role.getRoleMapData().getCurrentMapId());

		if (srcMap == null) {
			_LOGGER.error("跳转地图时发生错误，源地图数据为null，角色ID：" + role.getId() + "。");
			return new KActionResult(false, GlobalTips.getTipsServerBusy());
		}

		KDuplicateMapImpl dupMap = allDuplicateMaps.get(duplicateMapId);

		if (dupMap == null) {
			_LOGGER.error("跳转地图时发生错误，源地图数据为null，角色ID：" + role.getId() + "。");
			return new KActionResult(false, GlobalTips.getTipsServerBusy());
		}

		KGameMapEntity playerEntity = null;

		if ((playerEntity = srcMap.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId())) != null) {
			// 处理玩家离开源地图
			KMapModule.getGameMapManager().playerRoleEntityLeaveMap(srcMap, playerEntity);
			playerEntity.dispose();
		} else {
			_LOGGER.warn("地图系统：jumpMap 过地图的时候，在地图里找不到玩家角色：" + role.getId() + ", 角色名：" + role.getName() + ",地图ID： " + srcMap.getMapId());

		}
		// 通知所有地图监听器角色离开地图成功
		KMapModule.getGameMapManager().notifyPlayerRoleLeavedMapEvent(role, srcMap);

		playerEntity = new KGameMapEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId(), role);

		List<KDuplicateMapBornPoint> bornPointList = dupMap.getAllBornPointEntity();
		float cor_x = 0f, cor_y = 0f;
		if (!bornPointList.isEmpty()) {
			cor_x = bornPointList.get(0)._corX;
			cor_y = bornPointList.get(0)._corY;
		}

		boolean joinResult = playerRoleJoinInMap(role, playerEntity, dupMap, cor_x, cor_y);

		return new KActionResult(joinResult, "");
	}

	public KActionResult playerRoleJoinDuplicateMap(KRole role, int duplicateMapId, float corX, float corY) {
		if (role == null || role.getRoleMapData() == null) {
			_LOGGER.error("跳转地图时发生错误，角色为null。");
			return new KActionResult(false, GlobalTips.getTipsServerBusy());
		}

		KGameNormalMap srcMap = KMapModule.getGameMapManager().getGameMap(role.getRoleMapData().getCurrentMapId());

		if (srcMap == null) {
			_LOGGER.error("跳转地图时发生错误，源地图数据为null，角色ID：" + role.getId() + "。");
			return new KActionResult(false, GlobalTips.getTipsServerBusy());
		}

		KDuplicateMapImpl dupMap = allDuplicateMaps.get(duplicateMapId);

		if (dupMap == null) {
			_LOGGER.error("跳转地图时发生错误，源地图数据为null，角色ID：" + role.getId() + "。");
			return new KActionResult(false, GlobalTips.getTipsServerBusy());
		}

		KGameMapEntity playerEntity = null;

		if ((playerEntity = srcMap.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId())) != null) {
			// 处理玩家离开源地图
			KMapModule.getGameMapManager().playerRoleEntityLeaveMap(srcMap, playerEntity);
			playerEntity.dispose();
		} else {
			_LOGGER.warn("地图系统：jumpMap 过地图的时候，在地图里找不到玩家角色：" + role.getId() + ", 角色名：" + role.getName() + ",地图ID： " + srcMap.getMapId());

		}
		// 通知所有地图监听器角色离开地图成功
		KMapModule.getGameMapManager().notifyPlayerRoleLeavedMapEvent(role, srcMap);

		playerEntity = new KGameMapEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId(), role);

		boolean joinResult = playerRoleJoinInMap(role, playerEntity, dupMap, corX, corY);

		return new KActionResult(joinResult, "");
	}

	/**
	 * 角色进入地图处理
	 * 
	 * @param targetMap
	 * @param roleEntity
	 * @param target_x
	 * @param target_y
	 * @param srcMapId
	 * @return
	 */
	private boolean playerRoleJoinInMap(KRole myRole, KGameMapEntity roleEntity, KDuplicateMapImpl targetMap, float target_x, float target_y) {

		// 将角色放入地图
		try {
			targetMap.putEntityTo(roleEntity, target_x, target_y);
		} catch (KGameMapModuleException e) {
			// TODO 处理将玩家角色放入地图失败的情况，需要通知客户端
			_LOGGER.error("地图系统：jumpMap 过地图的时候，将角色放入地图失败，角色ID：" + myRole.getId() + ", 角色名：" + myRole.getName() + ",地图ID： " + targetMap.getMapId(), e);
			return false;
		}

		myRole.getRoleMapData().setCorDuplicateX(target_x);
		myRole.getRoleMapData().setCorDuplicateY(target_y);

		myRole.getRoleMapData().setInDuplicateMap(true);
		myRole.getRoleMapData().setCurrentDuplicateMapId(targetMap.getDuplicateId());

		// 发送地图数据给客户端
		sendGameMapDataToClient(roleEntity, targetMap);

		// 以下是处理发送周围角色数据

		if (targetMap.unitLimitType == KDuplicateMapImpl.UNIT_LIMIT_TYPE_LIMIT) {
			// 获取角色周围地图实体私有列表
			PlayerRolePrivateEntityList privateList = roleEntity.getPrivateEntityList();
			try {
				if (privateList != null) {
					// 取得我私有列表中的地图实体
					List<KGameMapEntity> myEntityList = privateList.getMyListEntities();

					if (myEntityList != null) {
						for (KGameMapEntity myOtherEntity : myEntityList) {
							if (myOtherEntity != null && privateList.isShowEntity(myOtherEntity)) {
								// 向持有自己实体的其他角色发送自己在地图中生成的消息数据（这些角色存在于我的私有列表）
								KRole holderEntityRole = (KRole) myOtherEntity.getSourceObject();

								RoleMapEntityShowData data = KGameMapManager.getRoleMapEntityShowData(holderEntityRole, true);

								if (data != null) {
									PetMapEntityShowData petData = null;
									if (targetMap.isShowPet) {
										petData = KGameMapManager.getPetMapEntityShowData(holderEntityRole.getId());
									}
									boolean result = roleEntity.sendOtherEntityBornIntoMapData(myOtherEntity.getEntityType(), myOtherEntity.getSourceObjectID(), myOtherEntity.getCoordinate().getX(),
											myOtherEntity.getCoordinate().getY(), data, petData);
								}
							}
							// 向自己添加我的私有列表的其他角色在地图出生的

						}
					}

					// 取得我的地图实体对象持有者（持有我地图实体对象的其他角色地图实体对象集合）
					List<KGameMapEntity> holderList = privateList.getMyHolderEntities();
					RoleMapEntityShowData data = KGameMapManager.getRoleMapEntityShowData(myRole, true);
					PetMapEntityShowData petData = null;
					if (targetMap.isShowPet) {
						petData = KGameMapManager.getPetMapEntityShowData(myRole.getId());
					}
					if (petData != null) {
						roleEntity.sendOtherEntityPetBornIntoMapData(petData);
					}
					if (holderList != null) {
						// 向其他对象持有者打包组装我的角色进入地图状态消息（这里并不是真正发送消息，该消息由时效任务发送）
						for (KGameMapEntity holderEntity : holderList) {
							if (holderEntity != null && holderEntity.getPrivateEntityList() != null && holderEntity.getPrivateEntityList().isShowEntity(roleEntity)) {

								if (data != null) {
									holderEntity.sendOtherEntityBornIntoMapData(roleEntity.getEntityType(), roleEntity.getSourceObjectID(), target_x, target_y, data, petData);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				_LOGGER.error("角色：" + myRole.getId() + "跳转地图时处理通知周围角色时发生异常。", e);
			}
		} else if (targetMap.unitLimitType == KDuplicateMapImpl.UNIT_LIMIT_TYPE_UNLIMIT) {
			try {
				RoleMapEntityShowData myData = KGameMapManager.getRoleMapEntityShowData(myRole, true);
				PetMapEntityShowData myPetData = null;
				if (targetMap.isShowPet) {
					myPetData = KGameMapManager.getPetMapEntityShowData(myRole.getId());
				}
				if (myPetData != null) {
					roleEntity.sendOtherEntityPetBornIntoMapData(myPetData);
				}

				for (KGameMapEntity myOtherEntity : targetMap.getEntities(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE)) {
					if (myOtherEntity != null && myOtherEntity.getSourceObjectID() != myRole.getId()) {
						// 向持有自己实体的其他角色发送自己在地图中生成的消息数据（这些角色存在于我的私有列表）
						KRole holderEntityRole = (KRole) myOtherEntity.getSourceObject();

						RoleMapEntityShowData data = KGameMapManager.getRoleMapEntityShowData(holderEntityRole, true);

						if (data != null) {
							PetMapEntityShowData petData = null;
							if (targetMap.isShowPet) {
								petData = KGameMapManager.getPetMapEntityShowData(holderEntityRole.getId());
							}
							boolean result = roleEntity.sendOtherEntityBornIntoMapData(myOtherEntity.getEntityType(), myOtherEntity.getSourceObjectID(), myOtherEntity.getCoordinate().getX(),
									myOtherEntity.getCoordinate().getY(), data, petData);
						}

						if (myData != null) {
							myOtherEntity.sendOtherEntityBornIntoMapData(roleEntity.getEntityType(), roleEntity.getSourceObjectID(), target_x, target_y, myData, myPetData);
						}
					}
				}
			} catch (Exception e) {
				_LOGGER.error("角色：" + myRole.getId() + "跳转地图时处理通知周围角色时发生异常。", e);
			}
		}

		return true;
	}

	public KActionResult playerRoleLeaveDuplicateMap(KRole role, int duplicateMapId) {

		if (role == null) {
			_LOGGER.error("角色退出副本地图时发生错误，角色为null。地图ID：{}", duplicateMapId);
			return new KActionResult(false, GlobalTips.getTipsServerBusy());
		}

		// 处理离开副本地图
		KDuplicateMapImpl dupMap = allDuplicateMaps.get(duplicateMapId);
		if (dupMap != null) {
			KGameMapEntity playerEntity = dupMap.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());
			if (playerEntity != null) {
				// 处理角色离开地图流程，以及广播周围其他角色该玩家角色离开地图
				if (playerEntity != null) {
					playerRoleEntityLeaveDuplicateMap(dupMap, playerEntity);
					// 释放entity数据
					if (playerEntity != null)
						playerEntity.dispose();
				}
			}
		}
		role.getRoleMapData().setInDuplicateMap(false);
		role.getRoleMapData().setCurrentDuplicateMapId(0);

		// 处理角色返回普通主城地图
		int currentNormalMapId = role.getRoleMapData().getCurrentMapId();
		KGameNormalMap normalMap = KMapModule.getGameMapManager().getGameMap(currentNormalMapId);
		if (normalMap == null) {
			normalMap = KMapModule.getGameMapManager().firstMap;
		}

		boolean joinResult = KMapModule.getGameMapManager().playerRoleLoginJumpMap(role);

		return new KActionResult(joinResult, "");
	}

	public void playerRoleLogoutLeaveDuplicateMap(KRole role, int duplicateMapId) {
		if (role == null) {
			_LOGGER.error("角色退出副本地图时发生错误，角色为null。地图ID：{}", duplicateMapId);
			return;
		}

		// 处理离开副本地图
		KDuplicateMapImpl dupMap = allDuplicateMaps.get(duplicateMapId);
		if (dupMap != null) {
			KGameMapEntity playerEntity = dupMap.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());
			if (playerEntity != null) {
				// 处理角色离开地图流程，以及广播周围其他角色该玩家角色离开地图
				if (playerEntity != null) {
					playerRoleEntityLeaveDuplicateMap(dupMap, playerEntity);
					// 释放entity数据
					if (playerEntity != null)
						playerEntity.dispose();
				}
			}
		}
		role.getRoleMapData().setInDuplicateMap(false);
		role.getRoleMapData().setCurrentDuplicateMapId(0);

		// 处理角色返回普通主城地图
		int currentNormalMapId = role.getRoleMapData().getCurrentMapId();
		KGameNormalMap normalMap = KMapModule.getGameMapManager().getGameMap(currentNormalMapId);
		if (normalMap == null) {
			normalMap = KMapModule.getGameMapManager().firstMap;
		}
	}

	/**
	 * 角色离开地图处理
	 * 
	 * @param map
	 * @param roleEntity
	 */
	public void playerRoleEntityLeaveDuplicateMap(KDuplicateMapImpl map, KGameMapEntity roleEntity) {
		if (map.unitLimitType == KDuplicateMapImpl.UNIT_LIMIT_TYPE_LIMIT) {
			// 获取角色周围地图实体列表
			PlayerRolePrivateEntityList privateList = roleEntity.getPrivateEntityList();
			if (privateList != null) {
				// 取得我的实体对象持有者
				List<KGameMapEntity> holderList = privateList.getMyHolderEntities();
				boolean isHasPet = false;
				long petId = 0;
				if (map.isShowPet) {
					PetMapEntityShowData petData = KGameMapManager.getPetMapEntityShowData(roleEntity.getSourceObjectID());
					if (petData != null) {
						isHasPet = true;
						petId = petData.petId;
					}
				}

				if (holderList != null) {
					// 向其他对象持有者打包组装角色离开地图状态消息（这里并不是真正发送消息，该消息由时效任务发送）
					for (KGameMapEntity holderEntity : holderList) {
						if (holderEntity != null && holderEntity.getPrivateEntityList() != null && holderEntity.getPrivateEntityList().isShowEntity(roleEntity)) {

							holderEntity.sendOtherEntityLeaveMapData(roleEntity.getEntityType(), roleEntity.getSourceObjectID(), isHasPet, petId);
						}
					}
				}
			}
		} else if (map.unitLimitType == KDuplicateMapImpl.UNIT_LIMIT_TYPE_UNLIMIT) {
			boolean isHasPet = false;
			long petId = 0;
			if (map.isShowPet) {
				PetMapEntityShowData petData = KGameMapManager.getPetMapEntityShowData(roleEntity.getSourceObjectID());
				if (petData != null) {
					isHasPet = true;
					petId = petData.petId;
				}
			}
			for (KGameMapEntity myOtherEntity : map.getEntities(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE)) {
				if (myOtherEntity != null && myOtherEntity.getSourceObjectID() != roleEntity.getSourceObjectID()) {
					myOtherEntity.sendOtherEntityLeaveMapData(roleEntity.getEntityType(), roleEntity.getSourceObjectID(), isHasPet, petId);
				}
			}
		}

		// 移除地图角色实体
		map.removeEntity(roleEntity);

		roleEntity.clearOtherRoleStateChangedMsgs();

		// TODO 以后可能会有更多角色离开地图事情要做，待定
	}

	/**
	 * 处理角色在地图中更新坐标位置
	 * 
	 * @param role
	 *            更新坐标的角色
	 * @param mapId
	 *            角色所在地图ID
	 * @param x
	 *            要更新的X坐标
	 * @param y
	 *            要更新的Y坐标
	 */
	public void processRoleUpdateCoordinate(KRole role, int duplicateMapId, float x, float y) {
		if (role == null) {
			_LOGGER.error("角色更新地图坐标时发生错误，角色为null。");
			return;
		}

		// _LOGGER.debug("角色更新地图坐标，角色：" + role.getRoleId() +
		// "，地图:"+mapId+"，坐标：(" + x + "," + y
		// + ")。");

		KDuplicateMapImpl map = allDuplicateMaps.get(duplicateMapId);

		if (map == null) {
			_LOGGER.error("角色更新地图坐标时发生错误，源地图数据为null，角色ID：" + role.getId() + "。");
			return;
		}
		KGameMapEntity roleEntity = map.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());

		if (roleEntity == null) {
			_LOGGER.error("角色更新地图坐标时发生错误，角色对应的地图实体为null。角色ID：" + role.getId());
			return;
		}

		if (map.unitLimitType == KDuplicateMapImpl.UNIT_LIMIT_TYPE_LIMIT) {
			// 以下是处理地图中本角色实体的持有者，对其通知本角色坐标的改变
			// 获取角色周围地图实体私有列表
			PlayerRolePrivateEntityList privateList = roleEntity.getPrivateEntityList();
			if (privateList != null) {
				// 取得我的地图实体对象持有者（持有我地图实体对象的其他角色地图实体对象集合）
				List<KGameMapEntity> holderList = privateList.getMyHolderEntities();

				if (holderList != null) {
					// 向其他对象持有者打包组装我的角色更新坐标消息（这里并不是真正发送消息，该消息由时效任务发送）

					for (KGameMapEntity holderEntity : holderList) {
						if (holderEntity != null && holderEntity.getPrivateEntityList() != null && holderEntity.getPrivateEntityList().isShowEntity(roleEntity)) {
							if (holderEntity.getSourceObjectID() == roleEntity.getSourceObjectID()) {
								_LOGGER.debug("***************** 自己的Holder中存在自己角色。角色ID：" + role.getId());
								continue;
							} else {
								holderEntity.sendOtherEntityWalkStateData(roleEntity.getEntityType(), roleEntity.getSourceObjectID(), x, y);
							}
						}
					}
				}
			}
		} else if (map.unitLimitType == KDuplicateMapImpl.UNIT_LIMIT_TYPE_UNLIMIT) {
			for (KGameMapEntity myOtherEntity : map.getEntities(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE)) {
				if (myOtherEntity != null && myOtherEntity.getSourceObjectID() != roleEntity.getSourceObjectID()) {
					myOtherEntity.sendOtherEntityWalkStateData(roleEntity.getEntityType(), roleEntity.getSourceObjectID(), x, y);
				}
			}
		}

		// 设置角色属性的坐标信息
		role.getRoleMapData().setCorDuplicateX(x);
		role.getRoleMapData().setCorDuplicateY(y);
		roleEntity.getCoordinate().setX(x);
		roleEntity.getCoordinate().setY(y);
	}

	public KActionResult processPlayerRoleResetCoordinate(KRole role, float x, float y) {
		if (role == null) {
			_LOGGER.error("角色更新地图坐标时发生错误，角色为null。");
			return new KActionResult(false, "");
		}

		// _LOGGER.debug("角色更新地图坐标，角色：" + role.getRoleId() +
		// "，地图:"+mapId+"，坐标：(" + x + "," + y
		// + ")。");

		KDuplicateMapImpl map = allDuplicateMaps.get(role.getRoleMapData().getCurrentDuplicateMapId());
		if (map == null) {
			_LOGGER.error("角色更新地图坐标时发生错误，源地图数据为null，角色ID：" + role.getId() + "。");
			return new KActionResult(false, "");
		}

		KGameMapEntity roleEntity = map.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());

		if (roleEntity == null) {
			_LOGGER.error("角色更新地图坐标时发生错误，角色对应的地图实体为null。角色ID：" + role.getId());
			return new KActionResult(false, "");
		}

		// 设置角色属性的坐标信息
		role.getRoleMapData().setCorDuplicateX(x);
		role.getRoleMapData().setCorDuplicateY(y);
		roleEntity.getCoordinate().setX(x);
		roleEntity.getCoordinate().setY(y);

		KGameMessage sendMsg = KGame.newLogicMessage(KMapProtocol.SM_RESET_SPRITE_XY);
		sendMsg.writeFloat(x);
		sendMsg.writeFloat(y);

		role.sendMsg(sendMsg);

		if (map.unitLimitType == KDuplicateMapImpl.UNIT_LIMIT_TYPE_LIMIT) {
			// 获取角色周围地图实体私有列表
			PlayerRolePrivateEntityList privateList = roleEntity.getPrivateEntityList();
			if (privateList != null) {
				// 取得我的地图实体对象持有者（持有我地图实体对象的其他角色地图实体对象集合）
				List<KGameMapEntity> holderList = privateList.getMyHolderEntities();

				if (holderList != null) {
					// 向其他对象持有者打包组装我的角色更新坐标消息（这里并不是真正发送消息，该消息由时效任务发送）

					for (KGameMapEntity holderEntity : holderList) {
						if (holderEntity != null && holderEntity.getPrivateEntityList() != null && holderEntity.getPrivateEntityList().isShowEntity(roleEntity)) {
							if (holderEntity.getSourceObjectID() == roleEntity.getSourceObjectID()) {
								_LOGGER.debug("***************** 自己的Holder中存在自己角色。角色ID：" + role.getId());
							}
							holderEntity.sendOtherEntityResetStateData(roleEntity.getEntityType(), roleEntity.getSourceObjectID(), x, y);
						}
					}
				}
			}
		} else if (map.unitLimitType == KDuplicateMapImpl.UNIT_LIMIT_TYPE_UNLIMIT) {
			for (KGameMapEntity myOtherEntity : map.getEntities(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE)) {
				if (myOtherEntity != null && myOtherEntity.getSourceObjectID() != roleEntity.getSourceObjectID()) {
					myOtherEntity.sendOtherEntityResetStateData(roleEntity.getEntityType(), roleEntity.getSourceObjectID(), x, y);
				}
			}
		}

		return new KActionResult(true, "");
	}

	public KActionResult resetPlayerRoleToBornPoint(KRole role, KDuplicateMapBornPoint bornPoint) {
		if (role == null) {
			_LOGGER.error("角色更新地图坐标时发生错误，角色为null。");
			return new KActionResult(false, "");
		}

		// _LOGGER.debug("角色更新地图坐标，角色：" + role.getRoleId() +
		// "，地图:"+mapId+"，坐标：(" + x + "," + y
		// + ")。");

		KDuplicateMapImpl map = allDuplicateMaps.get(role.getRoleMapData().getCurrentDuplicateMapId());
		if (map == null) {
			_LOGGER.error("角色更新地图坐标时发生错误，源地图数据为null，角色ID：" + role.getId() + "。");
			return new KActionResult(false, "");
		}

		KGameMapEntity roleEntity = map.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());

		if (roleEntity == null) {
			_LOGGER.error("角色更新地图坐标时发生错误，角色对应的地图实体为null。角色ID：" + role.getId());
			return new KActionResult(false, "");
		}

		// 设置角色属性的坐标信息
		role.getRoleMapData().setCorDuplicateX(bornPoint._corX);
		role.getRoleMapData().setCorDuplicateY(bornPoint._corY);
		roleEntity.getCoordinate().setX(bornPoint._corX);
		roleEntity.getCoordinate().setY(bornPoint._corY);

		return new KActionResult(true, "");
	}

	public void processRoleFinishBattleReturnToMap(KRole role) {
		KDuplicateMapImpl targetMap = allDuplicateMaps.get(role.getRoleMapData().getCurrentDuplicateMapId());

		KGameMapEntity roleEntity = targetMap.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());

		if (roleEntity == null) {
			_LOGGER.error("角色：" + role.getId() + "完成战斗返回地图时处理通知周围角色时发生错误。找不到地图实体。重新创建新地图实体。");
			roleEntity = new KGameMapEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId(), role);
		}

		// 发送地图数据给客户端
		sendGameMapDataToClient(roleEntity, targetMap);

		if (targetMap.unitLimitType == KDuplicateMapImpl.UNIT_LIMIT_TYPE_LIMIT) {
			// 获取角色周围地图实体私有列表
			PlayerRolePrivateEntityList privateList = roleEntity.getPrivateEntityList();
			try {
				if (privateList != null) {
					// 取得我私有列表中的地图实体
					List<KGameMapEntity> myEntityList = privateList.getMyListEntities();

					if (myEntityList != null) {
						for (KGameMapEntity myOtherEntity : myEntityList) {
							if (myOtherEntity != null && privateList.isShowEntity(myOtherEntity)) {
								// 向持有自己实体的其他角色发送自己在地图中生成的消息数据（这些角色存在于我的私有列表）
								KRole holderEntityRole = (KRole) myOtherEntity.getSourceObject();

								RoleMapEntityShowData data = KGameMapManager.getRoleMapEntityShowData(holderEntityRole, true);

								if (data != null) {
									PetMapEntityShowData petData = null;
									if (targetMap.isShowPet) {
										petData = KGameMapManager.getPetMapEntityShowData(holderEntityRole.getId());
									}
									boolean result = roleEntity.sendOtherEntityBornIntoMapData(myOtherEntity.getEntityType(), myOtherEntity.getSourceObjectID(), myOtherEntity.getCoordinate().getX(),
											myOtherEntity.getCoordinate().getY(), data, petData);
								}
							}
							// 向自己添加我的私有列表的其他角色在地图出生的

						}
					}

					// 取得我的地图实体对象持有者（持有我地图实体对象的其他角色地图实体对象集合）
					List<KGameMapEntity> holderList = privateList.getMyHolderEntities();
					RoleMapEntityShowData data = KGameMapManager.getRoleMapEntityShowData(role, true);
					PetMapEntityShowData petData = null;
					if (targetMap.isShowPet) {
						petData = KGameMapManager.getPetMapEntityShowData(role.getId());
					}
					if (petData != null) {
						roleEntity.sendOtherEntityPetBornIntoMapData(petData);
					}
					if (holderList != null) {
						// 向其他对象持有者打包组装我的角色进入地图状态消息（这里并不是真正发送消息，该消息由时效任务发送）
						for (KGameMapEntity holderEntity : holderList) {
							if (holderEntity != null && holderEntity.getPrivateEntityList() != null && holderEntity.getPrivateEntityList().isShowEntity(roleEntity)) {

								if (data != null) {
									holderEntity.sendOtherEntityBornIntoMapData(roleEntity.getEntityType(), roleEntity.getSourceObjectID(), role.getRoleMapData().getCorDuplicateX(), role
											.getRoleMapData().getCorDuplicateY(), data, petData);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				_LOGGER.error("角色：" + role.getId() + "战斗完返回副本地图时处理通知周围角色时发生异常。", e);
			}
		} else if (targetMap.unitLimitType == KDuplicateMapImpl.UNIT_LIMIT_TYPE_UNLIMIT) {
			try {
				RoleMapEntityShowData myData = KGameMapManager.getRoleMapEntityShowData(role, true);
				PetMapEntityShowData myPetData = null;
				if (targetMap.isShowPet) {
					myPetData = KGameMapManager.getPetMapEntityShowData(role.getId());
				}
				if (myPetData != null) {
					roleEntity.sendOtherEntityPetBornIntoMapData(myPetData);
				}

				for (KGameMapEntity myOtherEntity : targetMap.getEntities(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE)) {
					if (myOtherEntity != null && myOtherEntity.getSourceObjectID() != role.getId()) {
						// 向持有自己实体的其他角色发送自己在地图中生成的消息数据（这些角色存在于我的私有列表）
						KRole holderEntityRole = (KRole) myOtherEntity.getSourceObject();

						RoleMapEntityShowData data = KGameMapManager.getRoleMapEntityShowData(holderEntityRole, true);

						if (data != null) {
							PetMapEntityShowData petData = null;
							if (targetMap.isShowPet) {
								petData = KGameMapManager.getPetMapEntityShowData(holderEntityRole.getId());
							}
							boolean result = roleEntity.sendOtherEntityBornIntoMapData(myOtherEntity.getEntityType(), myOtherEntity.getSourceObjectID(), myOtherEntity.getCoordinate().getX(),
									myOtherEntity.getCoordinate().getY(), data, petData);
						}

						if (myData != null) {
							myOtherEntity.sendOtherEntityBornIntoMapData(roleEntity.getEntityType(), roleEntity.getSourceObjectID(), role.getRoleMapData().getCorDuplicateX(), role.getRoleMapData()
									.getCorDuplicateY(), myData, myPetData);
						}
					}
				}
			} catch (Exception e) {
				_LOGGER.error("角色：" + role.getId() + "战斗完返回副本地图处理通知周围角色时发生异常。", e);
			}
		}
	}

	public KActionResult playerRoleJumpDuplicateMap(KRole role, int srcDuplicateMapId, int targetDuplicateMapId) {
		if (role == null || role.getRoleMapData() == null) {
			_LOGGER.error("跳转地图时发生错误，角色为null。");
			return new KActionResult(false, GlobalTips.getTipsServerBusy());
		}

		KDuplicateMapImpl srcMap = allDuplicateMaps.get(srcDuplicateMapId);

		if (srcMap == null) {
			_LOGGER.error("副本地图之间跳转时发生错误，源地图数据为null，角色ID：" + role.getId() + "。");
			return new KActionResult(false, GlobalTips.getTipsServerBusy());
		}

		KDuplicateMapImpl tarMap = allDuplicateMaps.get(targetDuplicateMapId);

		if (tarMap == null) {
			_LOGGER.error("副本地图之间跳转时发生错误，目标地图数据为null，角色ID：" + role.getId() + "。");
			return new KActionResult(false, GlobalTips.getTipsServerBusy());
		}

		KGameMapEntity playerEntity = null;

		if ((playerEntity = srcMap.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId())) != null) {
			// 处理玩家离开源地图
			playerRoleEntityLeaveDuplicateMap(srcMap, playerEntity);
		} else {
			_LOGGER.warn("副本地图系统：jumpMap 过地图的时候，在地图里找不到玩家角色：" + role.getId() + ", 角色名：" + role.getName() + ",地图ID： " + srcMap.getMapId());
			playerEntity = new KGameMapEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId(), role);
		}

		List<KDuplicateMapBornPoint> bornPointList = tarMap.getAllBornPointEntity();
		float cor_x = 0f, cor_y = 0f;
		if (!bornPointList.isEmpty()) {
			cor_x = bornPointList.get(0)._corX;
			cor_y = bornPointList.get(0)._corY;
		}

		boolean joinResult = playerRoleJoinInMap(role, playerEntity, tarMap, cor_x, cor_y);

		return new KActionResult(joinResult, "");
	}

	public KActionResult playerRoleJumpDuplicateMap(KRole role, int srcDuplicateMapId, int targetDuplicateMapId, float corX, float corY) {
		if (role == null || role.getRoleMapData() == null) {
			_LOGGER.error("跳转地图时发生错误，角色为null。");
			return new KActionResult(false, GlobalTips.getTipsServerBusy());
		}

		KDuplicateMapImpl srcMap = allDuplicateMaps.get(srcDuplicateMapId);

		if (srcMap == null) {
			_LOGGER.error("副本地图之间跳转时发生错误，源地图数据为null，角色ID：" + role.getId() + "。");
			return new KActionResult(false, GlobalTips.getTipsServerBusy());
		}

		KDuplicateMapImpl tarMap = allDuplicateMaps.get(targetDuplicateMapId);

		if (tarMap == null) {
			_LOGGER.error("副本地图之间跳转时发生错误，目标地图数据为null，角色ID：" + role.getId() + "。");
			return new KActionResult(false, GlobalTips.getTipsServerBusy());
		}

		KGameMapEntity playerEntity = null;

		if ((playerEntity = srcMap.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId())) != null) {
			// 处理玩家离开源地图
			playerRoleEntityLeaveDuplicateMap(srcMap, playerEntity);
		} else {
			_LOGGER.warn("副本地图系统：jumpMap 过地图的时候，在地图里找不到玩家角色：" + role.getId() + ", 角色名：" + role.getName() + ",地图ID： " + srcMap.getMapId());
			playerEntity = new KGameMapEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId(), role);
		}

		boolean joinResult = playerRoleJoinInMap(role, playerEntity, tarMap, corX, corY);

		return new KActionResult(joinResult, "");
	}

	/**
	 * 处理发送地图数据消息给客户端
	 */
	public void sendGameMapDataToClient(KGameMapEntity playerEntity, KDuplicateMapImpl targetMap) {
		if (playerEntity == null || targetMap == null) {
			return;
		}
		if (!playerEntity.isPlayerRoleType()) {
			return;
		}

		KRole role = (KRole) playerEntity.getSourceObject();

		KGameMessage sendMsg = KGame.newLogicMessage(KMapProtocol.SM_SEND_DUPLICATE_MAP_DATA);

		sendMsg.writeByte(targetMap.mapType.type);
		sendMsg.writeByte(targetMap.duplicateType.type);

		// 发送地图基本数据，及主角色出生点坐标
		sendMsg.writeInt(targetMap.getDuplicateId());
		sendMsg.writeUtf8String(targetMap.getName());
		sendMsg.writeUtf8String(targetMap.getMapDataFileName());
		sendMsg.writeInt(targetMap.getDuplicateId());
		sendMsg.writeInt(targetMap.getMapResId());
		sendMsg.writeInt(targetMap.getBgMusicResId());
		sendMsg.writeFloat(playerEntity.getCoordinate().getX());
		sendMsg.writeFloat(playerEntity.getCoordinate().getY());

		role.sendMsg(sendMsg);

		packCollisionData(playerEntity, targetMap);

		// _LOGGER.info("发送地图模块数据消息！角色id是：" + role.getRoleId());
	}

	private void packCollisionData(KGameMapEntity roleEntity, KDuplicateMap map) {
		List<CollisionEventObjectData> list = map.getAllCollisionEventObject();
		if (list != null) {
			for (CollisionEventObjectData data : list) {
				roleEntity.sendOtherEntityBornIntoMapData(KMapEntityTypeEnum.ENTITY_TYPE_MONSTER, (long) (data.getMapInstanceId()), data.getX(), data.getY(), null, null);
			}
		}
	}

	/**
	 * 通知副本地图中角色战斗状态改变
	 * 
	 * @param role
	 * @param isFight
	 */
	public void notifyPlayerRoleFightStatus(KRole role, boolean isFight) {

		KDuplicateMapImpl map = allDuplicateMaps.get(role.getRoleMapData().getCurrentDuplicateMapId());

		if (map == null) {
			return;
		}
		KGameMapEntity roleEntity = map.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());

		if (roleEntity == null) {
			return;
		}

		// 获取角色周围地图实体私有列表
		PlayerRolePrivateEntityList privateList = roleEntity.getPrivateEntityList();
		if (privateList != null) {
			// 取得我的地图实体对象持有者（持有我地图实体对象的其他角色地图实体对象集合）
			List<KGameMapEntity> holderList = privateList.getMyHolderEntities();

			if (holderList != null) {
				// 向其他对象持有者打包组装我的角色更新坐标消息（这里并不是真正发送消息，该消息由时效任务发送）
				for (KGameMapEntity holderEntity : holderList) {
					holderEntity.sendOtherEntityFightStatus(role.getId(), isFight);
				}
			}
		}
	}

	/**
	 * 地图事件ID生成器
	 * 
	 * @author Administrator
	 * 
	 */
	public static class DuplcateMapIdGenerator {
		private AtomicInteger id;

		public DuplcateMapIdGenerator(int initialValue) {
			id = new AtomicInteger(initialValue);
		}

		public int currentDuplcateMapId() {
			return id.get();
		}

		public int nextDuplcateMapId() {
			return id.incrementAndGet();
		}
	}

	private void debugTest() {
		// for (Integer mapId : structMaps.keySet()) {
		// KDuplicateMapManager.getInstace().createDuplicateMap(mapId);
		// }
		for (int i = 0; i < 2; i++) {
			KDuplicateMapManager.getInstace().createDuplicateMap(90002);
		}
	}
}
