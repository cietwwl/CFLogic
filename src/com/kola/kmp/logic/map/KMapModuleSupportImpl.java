package com.kola.kmp.logic.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.map.KGameMapEntity.PetMapEntityShowData;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMap;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapImpl;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapManager;
import com.kola.kmp.logic.mission.KMissionCompleteRecordSet;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KMapEntityTypeEnum;
import com.kola.kmp.logic.other.KNoviceGuideStepEnum;
import com.kola.kmp.logic.role.IRoleGameSettingData;
import com.kola.kmp.logic.role.IRoleMapData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.MapModuleSupport;

public class KMapModuleSupportImpl implements MapModuleSupport {
	private static final KGameLogger _LOGGER = KGameLogger.getLogger(KMapModuleSupportImpl.class);

	@Override
	public IRoleMapData newIRoleMapDataInstance() {
		return new KRoleMapDataImpl();
	}

	@Override
	public IRoleGameSettingData newIRoleGameSettingDataInstance() {
		return new KRoleGameSettingDataImpl();
	}

	@Override
	public void processRoleFinishBattleReturnToMap(KRole role) {
		if (role.getRoleMapData().isInDuplicateMap()) {
			KDuplicateMap targetMap = KDuplicateMapManager.getInstace().getDuplicateMap(role.getRoleMapData().getCurrentDuplicateMapId());
			if (targetMap != null) {
				KDuplicateMapManager.getInstace().processRoleFinishBattleReturnToMap(role);
			} else {
				KSupportFactory.getDuplicateMapSupport().playerRoleLeaveDuplicateMap(role, role.getRoleMapData().getCurrentDuplicateMapId());
			}
		} else {
			KMapModule.getGameMapManager().processRoleFinishBattleReturnToMap(role);
		}
		KDialogService.sendNullDialog(role);
	}

	@Override
	public List<Integer> getAllNpcIdsInMap(KRole role) {
		List<Integer> list = new ArrayList<Integer>();
		KMap map;
		if (role.getRoleMapData().isInDuplicateMap()) {
			int duplicateMapId = role.getRoleMapData().getCurrentDuplicateMapId();

			map = KDuplicateMapManager.getInstace().getDuplicateMap(duplicateMapId);
		} else {
			int mapId = role.getRoleMapData().getCurrentMapId();
			map = KMapModule.getGameMapManager().getGameMap(mapId);

		}
		if (map != null) {
			List<KGameMapEntity> entityList = map.getEntities(KMapEntityTypeEnum.ENTITY_TYPE_NPC);
			if (entityList != null) {
				for (KGameMapEntity entity : entityList) {
					list.add((int) entity.getSourceObjectID());
				}
			}
		}
		return list;
	}

	@Override
	public AutoSearchRoadTrack autoDirectToNpc(KRole role, int npcTemplateId) {
		return KMapModule.getGameMapManager().autoDirectToNpc(role, npcTemplateId);
	}

	@Override
	public AutoSearchRoadTrack autoDirectToGameLevel(KRole role, int sccnarioId) {
		return KMapModule.getGameMapManager().autoDirectToGameLevel(role, sccnarioId);
	}

	@Override
	public List<Long> getAroundRoleIds(KRole role) {

		if (role == null) {
			// _LOGGER.error("角色更新地图坐标时发生错误，角色为null。");
			return Collections.emptyList();
		}

		// _LOGGER.debug("角色更新地图坐标，角色：" + role.getRoleId() +
		// "，地图:"+mapId+"，坐标：(" + x + "," + y
		// + ")。");

		KGameNormalMap map = KMapModule.getGameMapManager().getGameMap(role.getRoleMapData().getCurrentMapId());

		if (map == null) {
			// _LOGGER.error("角色更新地图坐标时发生错误，源地图数据为null，角色ID：" + role.getId() +
			// "。");
			return Collections.emptyList();
		}

		return map.getEntitieIds(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE);
	}

	@Override
	public void notifyRoleFightingPetChange(long roleId, long prePetId) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);

		if (role == null) {
			// _LOGGER.error("角色更新地图坐标时发生错误，角色为null。");
			return;
		}

		// _LOGGER.debug("角色更新地图坐标，角色：" + role.getRoleId() +
		// "，地图:"+mapId+"，坐标：(" + x + "," + y
		// + ")。");

		KGameNormalMap map = KMapModule.getGameMapManager().getGameMap(role.getRoleMapData().getCurrentMapId());

		if (map == null) {
			// _LOGGER.error("角色更新地图坐标时发生错误，源地图数据为null，角色ID：" + role.getId() +
			// "。");
			return;
		}
		KGameMapEntity roleEntity = map.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());

		if (roleEntity == null) {
			// _LOGGER.error("角色更新地图坐标时发生错误，角色对应的地图实体为null。角色ID：" +
			// role.getId());
			return;
		}

		PetMapEntityShowData petData = KMapModule.getGameMapManager().getPetMapEntityShowData(roleId);
		if (petData != null) {
			if (prePetId > 0) {
				roleEntity.sendOtherEntityPetLeaveMapData(prePetId);
			}
			roleEntity.sendOtherEntityPetBornIntoMapData(petData);
		} else {
			if (prePetId > 0) {
				roleEntity.sendOtherEntityPetLeaveMapData(prePetId);
			}
		}

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

						} else {
							if (petData != null) {
								if (prePetId > 0) {
									holderEntity.sendOtherEntityPetLeaveMapData(prePetId);
								}
								holderEntity.sendOtherEntityPetBornIntoMapData(petData);
							} else {
								if (prePetId > 0) {
									holderEntity.sendOtherEntityPetLeaveMapData(prePetId);
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void notifyMountStatus(long roleId, boolean isMount, int mountResId) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);

		if (role == null) {
			return;
		}

		KGameNormalMap map = KMapModule.getGameMapManager().getGameMap(role.getRoleMapData().getCurrentMapId());

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
					holderEntity.sendOtherEntityMountStatus(roleId, (isMount ? mountResId : -1));
				}
			}
		}
	}

	@Override
	public void notifyFashionStatus(long roleId, String fashionData) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);

		if (role == null) {
			return;
		}

		KGameNormalMap map = KMapModule.getGameMapManager().getGameMap(role.getRoleMapData().getCurrentMapId());

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
					holderEntity.sendOtherEntityFashionStatus(roleId, fashionData);
				}
			}
		}
	}

	@Override
	public void notifyFinishNoviceGuideBattleAndJumpMap(KRole role) {
		KGameNormalMap map = KMapModule.getGameMapManager().firstMap;
		int srcMapID = role.getRoleMapData().getCurrentMapId();

		KCoordinate coor = map.getGameMapData().getPlayerRoleJumpMapCoordinateWithNoDirection();

		KMapModule.getGameMapManager().playerRoleJumpMap(role, srcMapID, map.getMapId(), coor.getX(), coor.getY(), true);

		KMissionCompleteRecordSet set = KMissionModuleExtension.getMissionCompleteRecordSet(role.getId());
		if (set != null) {
			set.finishNoviceGuide();
		}

	}

	@Override
	public void processRoleJoinNormalGameLevelAndLeaveMap(KRole role) {
//		if (role == null) {
//			_LOGGER.error("跳转地图时发生错误，角色为null。");
//			return;
//		}

		KMapModule.getGameMapManager().processRoleJoinNormalGameLevelAndLeaveMap(role);

	}

	@Override
	public void processRoleFinishNormalGameLevelAndReturnToMap(KRole role) {
		if (role == null) {
			_LOGGER.error("跳转地图时发生错误，角色为null。");
			return;
		}
		KMapModule.getGameMapManager().processRoleFinishNormalGameLevelAndReturnToMap(role);
	}

}
