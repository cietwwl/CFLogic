package com.kola.kmp.logic.talent.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.talent.KTalentEntireData;
import static com.kola.kmp.protocol.talent.KTalentProtocol.SM_SEND_TALENT_DATA;

/**
 * 
 * @author PERRY CHAN
 */
public class KTalentServerMsgSender {

	public static void sendAllTalentData(KRole role, KGamePlayerSession session) {
		KTalentEntireData talentEntireData = KSupportFactory.getTalentSupport().getTalentData(role.getId());
		if (talentEntireData != null) {
			KGameMessage msg = KGame.newLogicMessage(SM_SEND_TALENT_DATA);
			talentEntireData.packDataToMsg(msg);
			session.send(msg);
		}
	}
}
