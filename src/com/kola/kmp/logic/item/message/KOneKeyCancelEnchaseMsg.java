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
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_Equi;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_InheritEquiPrice;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ItemTips;
import com.kola.kmp.logic.util.tips.ShopTips;
import com.kola.kmp.protocol.item.KItemProtocol;

/**
 * <pre>
 * 装备取消镶嵌
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:49:15
 * </pre>
 */
public class KOneKeyCancelEnchaseMsg implements GameMessageProcesser, KItemProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KOneKeyCancelEnchaseMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_ONEKEYCANCEL_ENCHASE;
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
			ItemResult_Equi result = new ItemResult_Equi();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, itemId, result);
			return;
		}

		Object resultObj = KItemLogic.dealMsg_getOneKeyCancelEnchasePrice(role, itemId);

		if (resultObj instanceof String) {
			// 获取价格有问题，对话框
			ItemResult_Equi result = new ItemResult_Equi();
			result.tips = (String) resultObj;
			doFinally(session, role, itemId, result);
			return;
		}

		List<KCurrencyCountStruct> moneys = (List<KCurrencyCountStruct>) resultObj;
		StringBuffer sbf = new StringBuffer();
		{
			for (KCurrencyCountStruct count : moneys) {
				sbf.append('\n');
				sbf.append(StringUtil.format(ShopTips.x减x, count.currencyType.extName, count.currencyCount));
			}
		}

		// 发送菜单，消费二次确认
		List<KDialogButton> buttons = new ArrayList<KDialogButton>();
		buttons.add(KDialogButton.CANCEL_BUTTON);
		buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_ONEKEY_CANCEL_ENCHANSE, itemId + "", KDialogButton.CONFIRM_DISPLAY_TEXT));
		KDialogService.sendFunDialog(session, "", StringUtil.format(ItemTips.是否支付以下货币取消镶嵌x, sbf.toString()), buttons, true, (byte) -1);

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
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			ItemResult_Equi result = new ItemResult_Equi();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, itemId, result);
			return;
		}
		// -------------
		ItemResult_Equi result = KItemLogic.dealMsg_oneKeyCancelEnchaseEquipment(role, itemId);

		doFinally(session, role, itemId, result);
	}

	private static void doFinally(KGamePlayerSession session, KRole role, long itemId, ItemResult_Equi result) {
		KDialogService.sendNullDialog(session);//解锁
		// -------------
		KGameMessage backmsg = KGame.newLogicMessage(SM_ONEKEYCANCEL_ENCHASE_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeLong(itemId);
		session.send(backmsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);
	}
}
