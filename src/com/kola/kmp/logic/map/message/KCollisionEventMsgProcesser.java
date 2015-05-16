package com.kola.kmp.logic.map.message;

import static com.kola.kmp.protocol.map.KMapProtocol.CM_SEND_COLLISION_EVENT;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.map.KGameMapEntity;
import com.kola.kmp.logic.map.duplicatemap.CollisionEventObjectData;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMap;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapImpl;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapManager;
import com.kola.kmp.logic.other.KMapEntityTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KCollisionEventMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KCollisionEventMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_SEND_COLLISION_EVENT;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();

		int duplicateMapId = msg.readInt();
		int entity_type = msg.readInt();
		KMapEntityTypeEnum entityTypeEnum = KMapEntityTypeEnum
				.getEnum(entity_type);
		int map_instanceId = 0;
		long otherRoleId = 0;
		CollisionEventObjectData data = null;
		if (entityTypeEnum == KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE) {
			otherRoleId = msg.readLong();
		} else {
			map_instanceId = msg.readInt();
		}
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if(role == null){
			return;
		}
		KDuplicateMapImpl map = (KDuplicateMapImpl) (KDuplicateMapManager
				.getInstace().getDuplicateMap(duplicateMapId));
		if (map == null) {
			return;
		}

		KGameMapEntity roleE = map.getEntity(
				KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());
		if (roleE == null) {
			return;
		}

		if (map.getCollisionEventListener() != null) {
			if (entityTypeEnum == KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE) {
				KRole otherRole = KSupportFactory.getRoleModuleSupport()
						.getRole(otherRoleId);
				if (otherRole != null) {
					KGameMapEntity otherRoleE = map.getEntity(
							KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE,
							otherRole.getId());
					if (otherRoleE != null) {
						map.getCollisionEventListener()
								.notifyPlayerRoleCollisionOtherRole(role,
										otherRole);
					}
				}
			} else {
				data = map.getCollisionEventObject(map_instanceId);
				if (data != null) {
					map.getCollisionEventListener()
							.notifyPlayerRoleCollisionEvent(role, data);
				}
			}
		}
	}

}
