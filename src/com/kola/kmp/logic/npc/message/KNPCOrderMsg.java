package com.kola.kmp.logic.npc.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.other.KNPCOrderEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.protocol.npc.KNpcProtocol;

public class KNPCOrderMsg implements KNpcProtocol {

	/**
	 * <pre>
	 * 
	 * @param session
	 * @param order
	 * @param script 请参考{@link #SM_NPC_ORDER}，与客户端协商
	 * @author CamusHuang
	 * @creation 2014-2-26 下午5:31:05
	 * </pre>
	 */
	public static void sendNPCMenuOrder(KGamePlayerSession session, KNPCOrderEnum order, String script) {
		KGameMessage msg = KGame.newLogicMessage(SM_NPC_ORDER);
		msg.writeInt(order.sign);
		msg.writeUtf8String(script);
		session.send(msg);
	}
	/**
	 * <pre>
	 * 
	 * 
	 * @param role
	 * @param order
	 * @param script 请参考{@link #SM_NPC_ORDER}，与客户端协商
	 * @author CamusHuang
	 * @creation 2014-2-26 下午5:31:57
	 * </pre>
	 */
	public static void sendNPCMenuOrder(KRole role, KNPCOrderEnum order, String script) {
		KGameMessage msg = KGame.newLogicMessage(SM_NPC_ORDER);
		msg.writeInt(order.sign);
		msg.writeUtf8String(script);
		role.sendMsg(msg);
	}
}
