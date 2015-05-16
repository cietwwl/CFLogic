package com.kola.kmp.logic.level.message;

import static com.kola.kmp.protocol.level.KLevelProtocol.CM_REQUEST_START_SAODANG;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.level.KLevelProtocol;

public class RequestStartSaoDangMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new RequestStartSaoDangMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REQUEST_START_SAODANG;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();

		byte levelType = msg.readByte();
		int levelId = msg.readInt();
		byte saodangCount = msg.readByte();
		if (levelType == KGameLevelTypeEnum.普通关卡.levelType) {

			KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);

			KGameLevelModuleExtension.getManager().processNormalLevelSaodang(role, levelId, saodangCount);
		} else if (levelType == KGameLevelTypeEnum.精英副本关卡.levelType || levelType == KGameLevelTypeEnum.技术副本关卡.levelType) {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);

			KGameLevelModuleExtension.getManager().getCopyManager().processCopyLevelSaodang(role, levelId, KGameLevelTypeEnum.getEnum(levelType), saodangCount, true);
		}
	}

}
