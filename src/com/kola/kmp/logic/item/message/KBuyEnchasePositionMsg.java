package com.kola.kmp.logic.item.message;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_BuyEnchase;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ItemTips;
import com.kola.kmp.protocol.item.KItemProtocol;

public class KBuyEnchasePositionMsg implements GameMessageProcesser, KItemProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KBuyEnchasePositionMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_BUY_ENCHASE_POSITION;
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
			ItemResult_BuyEnchase result = new ItemResult_BuyEnchase();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, itemId, result);
			return;
		}

		ItemResult_BuyEnchase result = KItemLogic.dealMsg_extendEnchanse(role, itemId, false);
		//
		doFinally(session, role, itemId, result);
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
	public static void confirmExtendEnchanse(KGamePlayerSession session, String script) {
		long itemId = Long.parseLong(script);
		if (itemId < 1) {
			return;// 非法数据，直接忽略
		}
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			ItemResult_BuyEnchase result = new ItemResult_BuyEnchase();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, itemId, result);
			return;
		}
		// -------------
		ItemResult_BuyEnchase result = KItemLogic.dealMsg_extendEnchanse(role, itemId, true);
		// -------------
		doFinally(session, role, itemId, result);
	}

	private static void doFinally(KGamePlayerSession session, KRole role, long itemId, ItemResult_BuyEnchase result) {

		if (result.priceData != null) {
			// 发送菜单，消费二次确认
			List<KDialogButton> buttons = new ArrayList<KDialogButton>();
			buttons.add(KDialogButton.CANCEL_BUTTON);
			buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_EXTEND_ENCHANSE, itemId + "", KDialogButton.CONFIRM_DISPLAY_TEXT));
			String tips = null;
			if (result.priceData.payMoney == null) {
				tips = StringUtil.format(ItemTips.是否花费x物品x数量开启此镶嵌孔, result.priceData.payItem.getItemTemplate().extItemName, result.priceData.payItem.itemCount);
			} else {
				tips = StringUtil.format(ItemTips.是否花费x物品x数量x货币x数量开启此镶嵌孔, result.priceData.payItem.getItemTemplate().extItemName, result.priceData.payItem.itemCount,
						result.priceData.payMoney.currencyType.extName, result.priceData.payMoney.currencyCount);
			}
			KDialogService.sendFunDialog(session, "", tips, buttons, true, (byte) -1);

		} else {

			KDialogService.sendNullDialog(session);// 解锁
			//
			KGameMessage msg = KGame.newLogicMessage(SM_BUY_ENCHASE_POSITION_RESULT);
			msg.writeBoolean(result.isSucess);
			msg.writeUtf8String(result.tips);
			msg.writeLong(itemId);
			session.send(msg);

			// 处理各种提示、弹开界面二次确认
			result.doFinally(role);
		}
	}
}
