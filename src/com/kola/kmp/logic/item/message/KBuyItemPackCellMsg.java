package com.kola.kmp.logic.item.message;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.item.KItemAttributeProvider;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_ExtPack;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_GetExtBagPrice;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ShopTips;
import com.kola.kmp.protocol.item.KItemProtocol;

public class KBuyItemPackCellMsg implements GameMessageProcesser, KItemProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KBuyItemPackCellMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_BUY_ITEMPACK_CELL;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		byte buyCell = msg.readByte();
		if (buyCell < 1) {
			buyCell = 1;
		}
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			ItemResult_ExtPack result = new ItemResult_ExtPack();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, result);
			return;
		}

		ItemResult_GetExtBagPrice resultPrice = KItemLogic.dealMsg_getExtendPackPrice(role.getId(), buyCell);

		if (!resultPrice.isSucess) {
			// 获取价格有问题，对话框
			ItemResult_ExtPack result = new ItemResult_ExtPack();
			result.tips = result.tips;
			doFinally(session, role, result);
			return;
		}

		if (resultPrice.price.isEmpty()) {
			// 免费
			ItemResult_ExtPack result = KItemLogic.dealMsg_extendBagVolume(role, buyCell);
			doFinally(session, role, result);
			return;
		}

//		// 如果没钱，直接提示
//		KCurrencyCountStruct ePrice = KSupportFactory.getCurrencySupport().checkMoneysEnought(role.getId(), resultPrice.price);
//		if (ePrice != null) {
//			// 获取价格有问题，对话框
//			if (ePrice.currencyType == KCurrencyTypeEnum.DIAMOND) {
//				KDialogService.showChargeDialog(role.getId(), ShopTips.您的钻石不足是否前去充值);
//			} else if (ePrice.currencyType == KCurrencyTypeEnum.GOLD) {
//				KDialogService.showExchangeDialog(role.getId(), ShopTips.您的金币不足是否前去兑换);
//			} else {
//				ItemResult_ExtPack result = new ItemResult_ExtPack();
//				result.tips = StringUtil.format(ShopTips.x货币数量不足x, ePrice.currencyType.extName, ePrice.currencyCount);
//				doFinally(session, role, result);
//			}
//			return;
//		}

		// 发送菜单，消费二次确认
		List<KDialogButton> buttons = new ArrayList<KDialogButton>();
		buttons.add(KDialogButton.CANCEL_BUTTON);
		buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_EXTEND_PACK, buyCell + "", KDialogButton.CONFIRM_DISPLAY_TEXT));
		KDialogService.sendFunDialog(session, "", resultPrice.tips, buttons, true, (byte) -1);
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
	public static void confirmExtendPack(KGamePlayerSession session, String script) {
		byte buyCell = Byte.parseByte(script);
		if (buyCell < 1) {
			buyCell = 1;
		}
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			ItemResult_ExtPack result = new ItemResult_ExtPack();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, result);
			return;
		}
		// -------------
		ItemResult_ExtPack result = KItemLogic.dealMsg_extendBagVolume(role, buyCell);
		// -------------
		doFinally(session, role, result);
	}

	private static void doFinally(KGamePlayerSession session, KRole role, ItemResult_ExtPack result) {
		KDialogService.sendNullDialog(session);//解锁
		// -------------
		KGameMessage msg = KGame.newLogicMessage(SM_BUY_ITEMPACK_CELL_RESULT);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		if (result.isSucess) {
			msg.writeShort(result.newVolume);
		}
		session.send(msg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);

		if (result.isSucess) {
			// 刷新角色属性
			KItemAttributeProvider.notifyEffectAttrChange(role);
		}
	}
}
