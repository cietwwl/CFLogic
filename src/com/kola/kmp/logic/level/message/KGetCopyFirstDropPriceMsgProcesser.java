package com.kola.kmp.logic.level.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

import static com.kola.kmp.protocol.level.KLevelProtocol.CM_GET_COPY_LEVEL_FIRST_ITEM;

public class KGetCopyFirstDropPriceMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGetCopyFirstDropPriceMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GET_COPY_LEVEL_FIRST_ITEM;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		byte levelType = msg.readByte();
		int levelId = msg.readInt();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		KGameLevelModuleExtension.getManager().getCopyManager()
				.processGetCopyFirstDropItem(role, levelId, KGameLevelTypeEnum.getEnum(levelType));
	}

}
