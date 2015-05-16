package com.kola.kmp.logic.item.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.other.KItemTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_Use;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.item.KItemProtocol;

public class KUseItemMsg implements GameMessageProcesser, KItemProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KUseItemMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_USE_ITEM;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long itemId = msg.readLong();
		byte type = msg.readByte();
		KItemTypeEnum itemType = KItemTypeEnum.getEnum(type);
		boolean isUseAll = msg.readBoolean();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			ItemResult_Use result = new ItemResult_Use();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, itemId, result);
			return;
		}
		// if(KSupportFactory.getRoleModuleSupport().isPhyPowerFull(role)){
		// KSupportFactory.getRoleModuleSupport().decreasePhyPower(role, -50);
		// }
		// -------------
		ItemResult_Use result = KItemLogic.dealMsg_useBagItem(session, role, itemId, itemType, isUseAll);

		doFinally(session, role, itemId, result);
	}

	private static void doFinally(KGamePlayerSession session, KRole role, long itemId, CommonResult_Ext result) {
		KGameMessage backmsg = KGame.newLogicMessage(SM_USE_ITEM_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeLong(itemId);
		session.send(backmsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);
	}
}
