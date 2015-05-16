package com.kola.kmp.logic.activity.message;
import static com.kola.kmp.protocol.activity.KActivityProtocol.CM_REQUEST_ENTER_ACTIVITY;
import static com.kola.kmp.protocol.activity.KActivityProtocol.SM_REPONSE_ENTER_ACTIVITY;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.activity.KActivityManager;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KEnterActivityMsgProcesser implements GameMessageProcesser{

	@Override
	public GameMessageProcesser newInstance() {
		return new KEnterActivityMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		
		return CM_REQUEST_ENTER_ACTIVITY;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		if (role != null) {
			int activityId = msgEvent.getMessage().readInt();
			@SuppressWarnings("rawtypes")
			KActionResult result = KActivityManager.getInstance().processPlayerRoleEnterActivity(role, activityId);
			KGameMessage msg = KGame.newLogicMessage(SM_REPONSE_ENTER_ACTIVITY);
			msg.writeBoolean(result.success);
			if(!result.success) {
				msg.writeUtf8String(result.tips);
			}
			msgEvent.getPlayerSession().send(msg);
		}
	}

}
