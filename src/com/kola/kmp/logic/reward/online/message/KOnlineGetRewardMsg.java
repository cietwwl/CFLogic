package com.kola.kmp.logic.reward.online.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.reward.online.KOnlineCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_Online;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.reward.KRewardProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KOnlineGetRewardMsg implements GameMessageProcesser, KRewardProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KOnlineGetRewardMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_ONLINE_GETREWARD;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		// -------------
		RewardResult_Online result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new RewardResult_Online();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
		} else {
			result = KOnlineCenter.dealMsg_getReward(role);
		}
		// -------------
		KDialogService.sendUprisingDialog(session, result.tips);
		//
		result.doFinally(role);
		//
		if (result.isSync) {
			KOnlineSyncMsg.sendMsg(role);
		}
	}
}
