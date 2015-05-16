package com.kola.kmp.logic.npc.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.protocol.npc.KNpcProtocol;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-26 下午6:07:37
 * </pre>
 */
public class KExecuteDialogFunMsg implements GameMessageProcesser, KNpcProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KExecuteDialogFunMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_EXECUTE_DIALOG_FUN;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		byte _dialogType = msg.readByte();
		short _funId = msg.readShort();
		String _script = msg.readUtf8String();
		// -------------
		KDialogService.processDialogFun(session, _funId, _script);
	}

}
