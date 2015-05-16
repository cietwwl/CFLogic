package com.kola.kmp.logic.gamble.message;

import static com.kola.kmp.protocol.gamble.KGambleProtocol.CM_WISH;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gamble.KGambleModule;
import com.kola.kmp.logic.gamble.KGambleRoleExtCACreator;
import com.kola.kmp.logic.gamble.KGambleRoleExtData;
import com.kola.kmp.logic.gamble.wish.KRoleWishData;
import com.kola.kmp.logic.gamble.wish.KWishSystemManager;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KWishMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KWishMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_WISH;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		byte wishType = msg.readByte();
		boolean isFree = false;
		if (wishType == KWishSystemManager.WISH_TYPE_POOR) {
			isFree = msg.readBoolean();
		}
		boolean isUse10Count = msg.readBoolean();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);

		KGambleRoleExtCACreator.getGambleRoleExtData(role.getId()).checkAndRestWishData();
		KGambleRoleExtData extData = KGambleRoleExtCACreator.getGambleRoleExtData(role.getId());

		KRoleWishData data = extData.getWishData();

		if (data.isGuideWish) {
			KGambleModule.getWishSystemManager().processRoleGuideWish(role);
		} else {
			KGambleModule.getWishSystemManager().processRoleWish(role, wishType, isFree, isUse10Count);
		}
	}

}
