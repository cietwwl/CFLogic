package com.kola.kmp.logic.item.message;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KItemTypeEnum;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_Compose;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.item.KItemProtocol;

/**
 * <pre>
 * 合成物品（包含材料和宝石）
 * 
 * @deprecated 合成改版:专用于合成物品（包含材料和宝石），不执行扣费和成功率
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KCompose2Msg implements GameMessageProcesser, KItemProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KCompose2Msg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_COMPOSE2;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long itemId = msg.readLong();
		boolean isComposeAll = msg.readBoolean();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			ItemResult_Compose result = new ItemResult_Compose();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, itemId, result);
			return;
		}

		ItemResult_Compose result = null;
		// 处理消息的过程
		result = KItemLogic.dealMsg_compose2(role, itemId, false, isComposeAll, null);

		if (result.isGoConfirmPay) {
			// 需要付费确认
			showConfirmPayDialog(session, result.tips, itemId, "", isComposeAll);
			return;
		}
		// -------------
		doFinally(session, role, itemId, result);
	}

	public static void doFinally(KGamePlayerSession session, KRole role, long itemId, ItemResult_Compose result) {
		KDialogService.sendUprisingDialog(session, result.tips);
		result.doFinally(role);

		if (result.isSucess) {
			if (result.itemTemp.ItemType == KItemTypeEnum.宝石) {
				KSupportFactory.getMissionSupport().notifyUseFunctionByCounts(role, KUseFunctionTypeEnum.宝石合成, result.successTime);
			}
		}
	}
	
	
	public static void showConfirmPayDialog(KGamePlayerSession session, String tips, long itemId, String selectItem, boolean isComposeAll){
		List<KDialogButton> buttons = new ArrayList<KDialogButton>();
		buttons.add(KDialogButton.CANCEL_BUTTON);
		buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_PAY_COMPOSEITEM, itemId + "," + (isComposeAll ? 1 : 0) + "," + selectItem, KDialogButton.CONFIRM_DISPLAY_TEXT));
		KDialogService.sendFunDialog(session, "", tips, buttons, true, (byte) -1);
	}

	/**
	 * <pre>
	 * 通过菜单确认付费
	 * 
	 * @param session
	 * @param slotId
	 * @author CamusHuang
	 * @creation 2013-6-7 上午10:46:21
	 * </pre>
	 */
	public static void confirmPayByDialog(KGamePlayerSession session, String script) {
		String[] scrs = script.split(",");
		long itemId = Long.parseLong(scrs[0]);
		boolean isComposeAll = Integer.parseInt(scrs[1]) == 1;
		String selectItem = scrs.length>2?scrs[2]:null;
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.服务器繁忙请稍候再试);
			return;
		}
		// -------------
		ItemResult_Compose result = null;
		// 处理消息的过程
		result = KItemLogic.dealMsg_compose2(role, itemId, true, isComposeAll, selectItem);

		// -------------
		doFinally(session, role, itemId, result);
	}
}
