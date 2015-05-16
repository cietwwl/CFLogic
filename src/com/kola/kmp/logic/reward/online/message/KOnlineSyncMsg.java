package com.kola.kmp.logic.reward.online.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.reward.online.KOnlineCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.protocol.reward.KRewardProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KOnlineSyncMsg implements KRewardProtocol {

	/**
	 * <pre>
	 * 推送LOGO
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-4-25 上午10:26:37
	 * </pre>
	 */
	public static void sendMsg(KRole role) {
		if(role==null){
			return;
		}
		KGameMessage msg = KGame.newLogicMessage(SM_ONLINE_SYN_LOGO);
		KOnlineCenter.packLogo(role, msg);
		role.sendMsg(msg);
	}
}
