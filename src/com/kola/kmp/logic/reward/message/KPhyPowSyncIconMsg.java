package com.kola.kmp.logic.reward.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.protocol.reward.KRewardProtocol;

public class KPhyPowSyncIconMsg implements KRewardProtocol {

	public final static KPhyPowSyncIconMsg instance = new KPhyPowSyncIconMsg();

	/**
	 * <pre>
	 * 一天之内，只有最后一次体力领取后或者最后一次时间段过期后才不显示ICON，其它时间显示ICON
	 * 
	 * @param role
	 * @param isOpen
	 * @author CamusHuang
	 * @creation 2015-1-29 下午3:38:07
	 * </pre>
	 */
	public void sendSynMsg(KRole role, boolean isOpen) {
		if (role == null) {
			return;
		}
		KGameMessage msg = KGame.newLogicMessage(CM_REWARD_SYNC_GETPHYPOWER_ICON);
		msg.writeBoolean(isOpen);
		role.sendMsg(msg);
	}
}
