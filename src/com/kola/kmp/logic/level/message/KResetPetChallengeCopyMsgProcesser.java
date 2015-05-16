package com.kola.kmp.logic.level.message;

import static com.kola.kmp.protocol.level.KLevelProtocol.CM_RESET_PET_CHALLENGE_COPY_AND_GET_PRICE;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KResetPetChallengeCopyMsgProcesser  implements GameMessageProcesser {
		@Override
		public GameMessageProcesser newInstance() {
			return new KResetPetChallengeCopyMsgProcesser();
		}

		@Override
		public int getMsgIdHandled() {
			return CM_RESET_PET_CHALLENGE_COPY_AND_GET_PRICE;
		}

		@Override
		public void processMessage(KGameMessageEvent msgEvent) throws Exception {
			KGamePlayerSession session = msgEvent.getPlayerSession();

			KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
			KGameLevelModuleExtension.getManager().getPetChallengeCopyManager().resetCopyAndGetReward(role);
		}

	}
