package com.kola.kmp.logic.vip.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.vip.KVIPLogic;
import com.kola.kmp.protocol.vip.KVIPProtocol;

public class KCollectDayRewardMsg implements GameMessageProcesser, KVIPProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KCollectDayRewardMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_VIP_COLLECT_DAYREWARD;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		// -------------
		CommonResult_Ext result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new CommonResult_Ext();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
		} else {
			result = KVIPLogic.dealMsg_collectVipDayReward(role);
		}
		// -------------
		msg = KGame.newLogicMessage(SM_VIP_COLLECT_DAYREWARD_RESULT);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		session.send(msg);
		
		result.doFinally(role);
	}
}
