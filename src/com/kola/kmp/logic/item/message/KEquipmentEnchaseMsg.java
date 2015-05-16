package com.kola.kmp.logic.item.message;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_Enchase;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ItemTips;
import com.kola.kmp.protocol.item.KItemProtocol;

/**
 * <pre>
 * 装备镶嵌消息
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:49:30
 * </pre>
 */
public class KEquipmentEnchaseMsg implements GameMessageProcesser, KItemProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KEquipmentEnchaseMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_EQUIPMENT_ENCHASE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long itemId = msg.readLong();
		long stoneId = msg.readLong();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			ItemResult_Enchase result = new ItemResult_Enchase();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, itemId, stoneId, result);
			return;
		}

		ItemResult_Enchase result = KItemLogic.dealMsg_enchaseEquipment(role, itemId, stoneId, false);
		if (result.isGoConfirmMuil) {
			// 发送菜单，消费二次确认
			List<KDialogButton> buttons = new ArrayList<KDialogButton>();
			buttons.add(KDialogButton.CANCEL_BUTTON);
			buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_ENCHANSE, itemId + "," + stoneId, KDialogButton.CONFIRM_DISPLAY_TEXT));
			KDialogService.sendFunDialog(session, "", ItemTips.镶嵌二次确认提示, buttons, true, (byte) -1);
			return;
		}

		// -------------
		doFinally(session, role, itemId, stoneId, result);
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
	public static void confirmCancelEnchanse(KGamePlayerSession session, String script) {
		String[] temps = script.split(",");
		long itemId = Long.parseLong(temps[0]);
		long stoneId = Long.parseLong(temps[1]);
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			ItemResult_Enchase result = new ItemResult_Enchase();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, itemId, stoneId, result);
			return;
		}
		// -------------
		ItemResult_Enchase result = KItemLogic.dealMsg_enchaseEquipment(role, itemId, stoneId, true);

		// -------------
		doFinally(session, role, itemId, stoneId, result);
	}

	private static void doFinally(KGamePlayerSession session, KRole role, long itemId, long stoneId, ItemResult_Enchase result) {
		KDialogService.sendNullDialog(session);//解锁
		// -------------
		KGameMessage msg = KGame.newLogicMessage(SM_EQUIPMENT_ENCHASE_RESULT);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(UtilTool.getNotNullString(result.tips));
		msg.writeLong(itemId);
		msg.writeLong(stoneId);
		session.send(msg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);

		if (result.isSucess) {
			// 通知日常任务
			KSupportFactory.getMissionSupport().notifyUseFunction(role, KUseFunctionTypeEnum.装备镶嵌);
		}
	}
}
