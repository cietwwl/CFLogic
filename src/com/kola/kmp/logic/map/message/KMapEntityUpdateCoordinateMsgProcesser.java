package com.kola.kmp.logic.map.message;

import static com.kola.kmp.protocol.map.KMapProtocol.CM_UPDATE_SPRITE_XY;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.map.KGameMapEntity;
import com.kola.kmp.logic.map.KGameNormalMap;
import com.kola.kmp.logic.map.KMapModule;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMap;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapImpl;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapManager;
import com.kola.kmp.logic.other.KMapEntityTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 客户端发送的更新角色地图坐标的消息处理器，参考对应消息:
 * {@link KGameMapProtocol#CM_UPDATE_SPRITE_XY}
 * @author Administrator
 * </pre>
 */
public class KMapEntityUpdateCoordinateMsgProcesser implements GameMessageProcesser {
	int mapId;
	float x;
	float y;

	@Override
	public GameMessageProcesser newInstance() {
		return new KMapEntityUpdateCoordinateMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_UPDATE_SPRITE_XY;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		this.mapId = msg.readInt();
		this.x = msg.readFloat();
		this.y = msg.readFloat();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role != null && role.getRoleMapData() != null) {
			if (role.getRoleMapData().isInDuplicateMap()) {
				KDuplicateMapImpl map = (KDuplicateMapImpl) KDuplicateMapManager.getInstace().getDuplicateMap(role.getRoleMapData().getCurrentDuplicateMapId());
				if (map == null) {
					return;
				}
				KGameMapEntity roleEntity = map.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());

				if (roleEntity == null) {
					return;
				}
				KDuplicateMapManager.getInstace().processRoleUpdateCoordinate(role, role.getRoleMapData().getCurrentDuplicateMapId(), this.x, this.y);
			} else {
				int currentMapId = role.getRoleMapData().getCurrentMapId();
				if (currentMapId == this.mapId) {

					KGameNormalMap map = KMapModule.getGameMapManager().getGameMap(this.mapId);
					if (map == null) {
						return;
					}
					KGameMapEntity roleEntity = map.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());
					if (roleEntity == null) {
						return;
					}
					KMapModule.getGameMapManager().processRoleUpdateCoordinate(role, this.mapId, this.x, this.y);
				}
			}

		}
	}

}
