package com.kola.kmp.logic.level.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

import static com.kola.kmp.protocol.level.KLevelProtocol.CM_RESET_COPY_LEVEL;

public class KResetEliteCopyDataMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KResetEliteCopyDataMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_RESET_COPY_LEVEL;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		byte levelType = msg.readByte();
		int levelId = msg.readInt();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		/******  20141129注释，精英副本不需要消耗钻石********/
//		KGameLevelModuleExtension.getManager().getCopyManager()
//				.processResetCopyGameLevel(role, levelId, KGameLevelTypeEnum.getEnum(levelType), true);
	}

}
