package com.kola.kmp.logic.item.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.item.KItemProtocol;

/**
 * <pre>
 * 合成道具
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KResolveEquipmentMsg implements GameMessageProcesser, KItemProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KResolveEquipmentMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_RESOLVE_EQUIPMENT;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long itemId = msg.readLong();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			CommonResult_Ext result = new CommonResult_Ext();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, itemId, result);
			return;
		}

		// 处理消息的过程
		CommonResult_Ext result = KItemLogic.dealMsg_resolveEquipment(role, itemId);

		// -------------
		dofinally(session, role, itemId, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, long itemId, CommonResult_Ext result) {
		KGameMessage backmsg = KGame.newLogicMessage(SM_RESOLVE_EQUIPMENT_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeLong(itemId);
		session.send(backmsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);
	}
}
