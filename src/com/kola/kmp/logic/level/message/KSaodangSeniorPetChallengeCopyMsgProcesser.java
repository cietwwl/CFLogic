package com.kola.kmp.logic.level.message;
import static com.kola.kmp.protocol.level.KLevelProtocol.CM_VIP_SAODANG_SENIOR_PET_CHALLENGE_COPY;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KSaodangSeniorPetChallengeCopyMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KSaodangSeniorPetChallengeCopyMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_VIP_SAODANG_SENIOR_PET_CHALLENGE_COPY;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		KGameLevelModuleExtension.getManager().getKSeniorPetChallengeCopyManager().processSaodangCopy(role);
	}

}
