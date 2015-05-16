package com.kola.kmp.logic.map.duplicatemap;

import java.util.List;

import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.map.KMapEntityData;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KMapDuplicateTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KDuplicateMapSupport;
import com.kola.kmp.logic.util.tips.GlobalTips;

public class KDuplicateSupportImpl implements KDuplicateMapSupport {
	private static final KGameLogger _LOGGER = KGameLogger
			.getLogger(KDuplicateSupportImpl.class);

	@Override
	public KDuplicateMap createDuplicateMap(int structMapId) {
		return KDuplicateMapManager.getInstace()
				.createDuplicateMap(structMapId);
	}

	@Override
	public List<KDuplicateMap> createDuplicateMapByCounts(int structMapId,
			int count) {
		return KDuplicateMapManager.getInstace().createDuplicateMapByCounts(
				structMapId, count);
	}

	@Override
	public KActionResult<KDuplicateMap> removeDuplicateMap(int duplicateMapId) {
		return KDuplicateMapManager.getInstace().removeDuplicateMap(
				duplicateMapId);
	}

	@Override
	public boolean removeDuplicateMapsByType(int structMapId) {
		return KDuplicateMapManager.getInstace().removeDuplicateMapsByType(
				structMapId);
	}

	@Override
	public KDuplicateMap getDuplicateMap(int duplicateMapId) {
		return KDuplicateMapManager.getInstace()
				.getDuplicateMap(duplicateMapId);
	}

	@Override
	public List<KDuplicateMap> getDuplicateMaps(int structMapId) {
		return KDuplicateMapManager.getInstace().getDuplicateMaps(structMapId);
	}

	@Override
	public KActionResult playerRoleJoinDuplicateMap(KRole role,
			int duplicateMapId) {

		if (role == null && role.getRoleMapData() != null) {
			_LOGGER.warn("跳转地图时发生错误，角色为null。");
			return new KActionResult(false, GlobalTips.getTipsServerBusy());
		}

		if (role.getRoleMapData().isInDuplicateMap()) {
			return KDuplicateMapManager.getInstace()
					.playerRoleJumpDuplicateMap(role,
							role.getRoleMapData().getCurrentDuplicateMapId(),
							duplicateMapId);
		} else {
			return KDuplicateMapManager.getInstace()
					.playerRoleJoinDuplicateMap(role, duplicateMapId);
		}

	}

	@Override
	public KActionResult playerRoleLeaveDuplicateMap(KRole role,
			int duplicateMapId) {
		return KDuplicateMapManager.getInstace().playerRoleLeaveDuplicateMap(
				role, duplicateMapId);
	}

	@Override
	public KActionResult playerRoleJoinDuplicateMap(KRole role,
			int duplicateMapId, float corX, float corY) {
		return KDuplicateMapManager.getInstace().playerRoleJoinDuplicateMap(
				role, duplicateMapId, corX, corY);
	}

	@Override
	public KDuplicateMap getDuplicateMapStruct(int structMapId) {
		return KDuplicateMapManager.getStructMap(structMapId);
	}

	@Override
	public KActionResult resetPlayerRoleToBornPoint(KRole role,
			KDuplicateMapBornPoint bornPoint) {
		return KDuplicateMapManager.getInstace().resetPlayerRoleToBornPoint(
				role, bornPoint);
	}

	public void notifyPlayerRoleFightStatus(KRole role, boolean isFight) {
		if (role.getRoleMapData().isInDuplicateMap()) {
			KDuplicateMapManager.getInstace().notifyPlayerRoleFightStatus(role,
					isFight);
		}
	}

}
