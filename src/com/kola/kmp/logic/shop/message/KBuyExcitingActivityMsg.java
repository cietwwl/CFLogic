package com.kola.kmp.logic.shop.message;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.reward.exciting.KExcitingCenter;
import com.kola.kmp.logic.reward.exciting.message.KSynDataMsg;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.BuyExcitingActivityResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.shop.KShopProtocol;

public class KBuyExcitingActivityMsg implements GameMessageProcesser, KShopProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KBuyExcitingActivityMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_BUY_EXCITING_ACTIVITY;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int activityId = msg.readInt();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			BuyExcitingActivityResult result = new BuyExcitingActivityResult();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, result);
			return;
		}

		BuyExcitingActivityResult result = KExcitingCenter.dealMsg_buyActivity(role, activityId, false);

		if (!result.isGoConfirm) {
			dofinally(session, role, result);
			return;
		}
		// -------------
		unlock(session, role);

		// 发送菜单，消费二次确认
		List<KDialogButton> buttons = new ArrayList<KDialogButton>();
		buttons.add(KDialogButton.CANCEL_BUTTON);
		buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_PAYFOR_EXCITING, activityId+"", KDialogButton.CONFIRM_DISPLAY_TEXT));
		KDialogService.sendFunDialog(role, "", result.tips, buttons, true, (byte) -1);
	}

	public static void confirmByDialog(KGamePlayerSession session, String script) {
		
		int activityId = Integer.parseInt(script);
		
		BuyExcitingActivityResult result = null;

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new BuyExcitingActivityResult();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, result);
			return;
		}

		result = KExcitingCenter.dealMsg_buyActivity(role, activityId, true);

		//
		dofinally(session, role, result);
		return;
	}
	
	private static void unlock(KGamePlayerSession session, KRole role){
		KGameMessage backMsg = KGame.newLogicMessage(SM_BUY_EXCITING_ACTIVITY_RESULT);
		backMsg.writeBoolean(false);
		backMsg.writeUtf8String(null);
		session.send(backMsg);
	}

	private static void dofinally(KGamePlayerSession session, KRole role, BuyExcitingActivityResult result) {
		// 处理消息的过程
		KGameMessage backMsg = KGame.newLogicMessage(SM_BUY_EXCITING_ACTIVITY_RESULT);
		backMsg.writeBoolean(result.isSucess);
		backMsg.writeUtf8String(result.tips);
		session.send(backMsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);

		if (result.isSucess) {
			KSynDataMsg.sendMsgForStatus(role.getId());
		}
	}
}
