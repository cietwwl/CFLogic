package com.kola.kmp.logic.gamble.message;


import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gamble.peopleguess.KPeopleGuessMonitor;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GlobalTips;

import static com.kola.kmp.protocol.activity.KActivityProtocol.CM_REQUEST_YABAO;

/**
 * 全民竞猜客户端请求下注
 * @author Alex
 * @create 2015年3月6日 下午3:47:43
 */
public class KPeopleGuessRequestYabaoProcesser implements GameMessageProcesser{

	@Override
	public GameMessageProcesser newInstance() {
		return new KPeopleGuessRequestYabaoProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		
		return CM_REQUEST_YABAO;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {

		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		
		byte horseId = msg.readByte();
		int count = msg.readInt();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if(role == null){
			KDialogService.sendSimpleDialog(session, "", GlobalTips.服务器繁忙请稍候再试);
			return;
		}
		KPeopleGuessMonitor.getMonitor().maneger.clientYabao(role, horseId, count);
	}

}
