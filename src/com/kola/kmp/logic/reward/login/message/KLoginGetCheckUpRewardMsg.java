package com.kola.kmp.logic.reward.login.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.reward.login.KLoginCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.reward.KRewardProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KLoginGetCheckUpRewardMsg implements GameMessageProcesser, KRewardProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KLoginGetCheckUpRewardMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_LOGIN_GETREWARD;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int day = msg.readInt();
		// -------------
		CommonResult_Ext result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new CommonResult_Ext();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
		} else {
			result = KLoginCenter.dealMsg_getCheckUpReward(role, day);
		}
		// -------------
		KGameMessage backmsg = KGame.newLogicMessage(SM_LOGIN_GET_REWARD_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeInt(day);
		session.send(backmsg);
		//
		result.doFinally(role);
	}
}
