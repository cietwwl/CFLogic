package com.kola.kmp.logic.gang.message;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResult_Exit;
import com.kola.kmp.logic.util.tips.GangTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KExitGangMsg implements GameMessageProcesser, KGangProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KExitGangMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANG_EXIT;
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
			GangResult_Exit result = new GangResult_Exit();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, result);
			return;
		}
		// 军团--退出军团
		GangResult_Exit result = KGangLogic.dealMsg_exitGang(role.getId(), false);
		if (result.isDismiss) {
			// 军团将被解散，请求二次确认
			List<KDialogButton> buttons = new ArrayList<KDialogButton>();
			buttons.add(KDialogButton.CANCEL_BUTTON);
			buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_EXIT_AND_DIMISS, "", KDialogButton.CONFIRM_DISPLAY_TEXT));
			KDialogService.sendFunDialog(session, "", GangTips.是否确认解散军团, buttons, true, (byte) -1);
			return;
		}
		
		if(result.isGoConfirm){
			// 请求二次确认
			List<KDialogButton> buttons = new ArrayList<KDialogButton>();
			buttons.add(KDialogButton.CANCEL_BUTTON);
			buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_EXIT_AND_DIMISS, "", KDialogButton.CONFIRM_DISPLAY_TEXT));
			KDialogService.sendFunDialog(session, "", GangTips.是否确认离开军团, buttons, true, (byte) -1);
			return;
		}

		//
		dofinally(session, role, result);
	}

	/**
	 * <pre>
	 * 通过菜单确认
	 * 
	 * @param session
	 * @param slotId
	 * @author CamusHuang
	 * @creation 2013-6-7 上午10:46:21
	 * </pre>
	 */
	public static void confirmByDialog(KGamePlayerSession session, String script) {
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			GangResult_Exit result = new GangResult_Exit();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, result);
			return;
		}
		// -------------
		// 军团--退出军团
		GangResult_Exit result = KGangLogic.dealMsg_exitGang(role.getId(), true);
		dofinally(session, role, result);
	}

	private static void dofinally(KGamePlayerSession session, KRole role, GangResult_Exit result) {
		KDialogService.sendNullDialog(session);//解锁
		// -------------
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_EXIT_RESULT);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		session.send(msg);
		
		if (result.isSucess) {

			if (result.isDismiss) {
				// 军团解散，不需要做任何操作
			} else {
				// 后续操作
				KGangLogic.actionAfterMemExit(result.gang, result.extCASet, result.opMember);
			}
		}
	}
}
