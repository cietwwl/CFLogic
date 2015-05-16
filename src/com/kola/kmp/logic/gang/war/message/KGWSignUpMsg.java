package com.kola.kmp.logic.gang.war.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.war.GangWarLogic;
import com.kola.kmp.logic.rank.gang.GangRank;
import com.kola.kmp.logic.rank.gang.GangRankElementWarSignUp;
import com.kola.kmp.logic.rank.gang.KGangRankLogic;
import com.kola.kmp.logic.rank.gang.KGangRankTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.war.KGangWarProtocol;

public class KGWSignUpMsg implements GameMessageProcesser, KGangWarProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGWSignUpMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GW_SIGNUP;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			CommonResult result = new CommonResult();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, result);
			return;
		}
		CommonResult result = GangWarLogic.dealMsg_signUp(role);
		dofinally(session, role, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, CommonResult result) {
		KGameMessage msg = KGame.newLogicMessage(SM_GW_SIGNUP_RESULT);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		{
			GangRank<GangRankElementWarSignUp> rank = KGangRankLogic.getRank(KGangRankTypeEnum.军团战报名);
			msg.writeInt(rank.getTempCacheData().size());
		}
		session.send(msg);
	}
}
