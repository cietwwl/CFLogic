package com.kola.kmp.logic.reward.login.message;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.reward.login.KLoginCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.AddCheckResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ShopTips;
import com.kola.kmp.protocol.reward.KRewardProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KLoginAddCheckUpMsg implements GameMessageProcesser, KRewardProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KLoginAddCheckUpMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_LOGIN_ADD_CHUCKUP;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		// -------------
		AddCheckResult result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new AddCheckResult();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, result);
			return;
		}

		result = KLoginCenter.dealMsg_addCheckUp(role, false);

		if (!result.isGoConfirm) {
			KDialogService.sendNullDialog(session);
			doFinally(session, role, result);
			return;
		}
		// -------------

		// 发送菜单，消费二次确认
		List<KDialogButton> buttons = new ArrayList<KDialogButton>();
		buttons.add(KDialogButton.CANCEL_BUTTON);
		buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_ADD_CHUCKUP, "", KDialogButton.CONFIRM_DISPLAY_TEXT));
		KDialogService.sendFunDialog(role, "", result.tips, buttons, true, (byte) -1);

	}

	public static void confirmByDialog(KGamePlayerSession session, String script) {
		AddCheckResult result = null;

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new AddCheckResult();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, result);
			return;
		}
		
		result = KLoginCenter.dealMsg_addCheckUp(role, true);
		
		//
		KDialogService.sendNullDialog(session);
		doFinally(session, role, result);
		return;
	}

	private static void doFinally(KGamePlayerSession session, KRole role, AddCheckResult result) {
		KGameMessage backmsg = KGame.newLogicMessage(SM_LOGIN_ADD_CHUCKUP_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeByte(result.day);
		session.send(backmsg);
		//
		result.doFinally(role);
		
		if(result.isSucess){
			KLoginPushMsg.syncCheckUpDataMsg(role);
		}
	}
}
