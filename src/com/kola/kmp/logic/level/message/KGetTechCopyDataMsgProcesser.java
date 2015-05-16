package com.kola.kmp.logic.level.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

import static com.kola.kmp.protocol.level.KLevelProtocol.CM_GET_TECH_COPY_DATA;;

public class KGetTechCopyDataMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGetTechCopyDataMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GET_TECH_COPY_DATA;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		KGameLevelModuleExtension.getManager().getCopyManager()
				.sendCopyData(role, KGameLevelTypeEnum.技术副本关卡.levelType);
	}

}
