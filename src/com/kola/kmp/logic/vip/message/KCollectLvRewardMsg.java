package com.kola.kmp.logic.vip.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.vip.KVIPLogic;
import com.kola.kmp.protocol.vip.KVIPProtocol;

public class KCollectLvRewardMsg implements GameMessageProcesser, KVIPProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KCollectLvRewardMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_VIP_COLLECT_LVREWARD;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int viplv = msg.readInt();
		// -------------
		CommonResult_Ext result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new CommonResult_Ext();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
		} else {
			result = KVIPLogic.dealMsg_collectVipLvReward(role, viplv);
		}
		// -------------
		msg = KGame.newLogicMessage(SM_VIP_COLLECT_LVREWARD_RESULT);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		msg.writeInt(viplv);
		session.send(msg);
		
		result.doFinally(role);
	}
}
