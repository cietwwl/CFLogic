package com.kola.kmp.logic.currency.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.MoneyResult_ExchangeGold;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.currency.KCurrencyProtocol;

public class KExchangeGoldMsg implements GameMessageProcesser, KCurrencyProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KExchangeGoldMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_EXCHANGE_GOLD;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int diamond = msg.readInt();
		// -------------
		MoneyResult_ExchangeGold result = null;
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new MoneyResult_ExchangeGold();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, diamond, result);
			return;
		}
		
		result = KSupportFactory.getCurrencySupport().dealMsg_exchangeGold(role, diamond);

		doFinally(session, role, diamond, result);
	}
		
	private void doFinally(KGamePlayerSession session, KRole role, int diamond, MoneyResult_ExchangeGold result){
		KGameMessage backmsg = KGame.newLogicMessage(SM_EXCHANGE_GOLD_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeInt(diamond);
		backmsg.writeLong(result.addGold);
		session.send(backmsg);
		
		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);
		
		if(result.isSucess){
			// 通知日常任务
			KSupportFactory.getMissionSupport().notifyUseFunction(role, KUseFunctionTypeEnum.完成金币购买);
		}
	}
}
