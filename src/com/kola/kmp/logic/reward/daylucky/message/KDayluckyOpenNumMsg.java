package com.kola.kmp.logic.reward.daylucky.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.reward.daylucky.KDayluckyCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_DayluckOpenNum;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_GoodLuck;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.reward.KRewardProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KDayluckyOpenNumMsg implements GameMessageProcesser, KRewardProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KDayluckyOpenNumMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_DAYLUCK_OPEN;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		// -------------
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			RewardResult_DayluckOpenNum result = new RewardResult_DayluckOpenNum();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, result);
			return;
		}

		RewardResult_DayluckOpenNum result = KDayluckyCenter.dealMsg_openNum(role);
		doFinally(session, role, result);
	}

	private void doFinally(KGamePlayerSession session, KRole role, RewardResult_DayluckOpenNum result) {
		KGameMessage backmsg = KGame.newLogicMessage(SM_DAYLUCK_OPEN_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeBoolean(result.isSynLogs);
		session.send(backmsg);

		result.doFinally(role);
	}
}
