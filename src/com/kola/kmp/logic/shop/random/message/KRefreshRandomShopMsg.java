package com.kola.kmp.logic.shop.random.message;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.shop.random.KRandomShopCenter;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResult_SetPosition;
import com.kola.kmp.logic.util.ResultStructs.RandomShopResultExt;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.GangTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ShopTips;
import com.kola.kmp.protocol.shop.KShopProtocol;

public class KRefreshRandomShopMsg implements GameMessageProcesser, KShopProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KRefreshRandomShopMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REFRESH_RANDOMSHOP;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		byte nowGoodsType = msg.readByte();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			RandomShopResultExt result = new RandomShopResultExt();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			//
			dofinally(session, role, result);
			return;
		}

		RandomShopResultExt result = KRandomShopCenter.dealMsg_refreshRandomShop(role, nowGoodsType, false);
		// -------------
		if (result.isGoConfirm) {
			// 发送菜单，消费二次确认
			List<KDialogButton> buttons = new ArrayList<KDialogButton>();
			buttons.add(KDialogButton.CANCEL_BUTTON);
			buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_PAY_FOR_REFRESH_RANDOMSHOP, nowGoodsType + "", KDialogButton.CONFIRM_DISPLAY_TEXT));
			KDialogService.sendFunDialog(session, "", StringUtil.format(ShopTips.是否消耗x数量x货币进行商品刷新, result.goConfirmPrice.currencyCount, result.goConfirmPrice.currencyType.extName), buttons, true, (byte) -1);
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
		int nowGoodsType = Integer.parseInt(script);
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			RandomShopResultExt result = new RandomShopResultExt();
			result.tips = GlobalTips.服务器繁忙请稍候再试;

			dofinally(session, role, result);
			return;
		}
		// -------------
		RandomShopResultExt result = KRandomShopCenter.dealMsg_refreshRandomShop(role, nowGoodsType, true);
		//
		dofinally(session, role, result);
	}

	private static void dofinally(KGamePlayerSession session, KRole role, RandomShopResultExt result) {
		KDialogService.sendNullDialog(session);//解锁
		// -------------
		// 处理消息的过程
		KGameMessage backMsg = KGame.newLogicMessage(SM_REFRESH_RANDOMSHOP_RESULT);
		backMsg.writeBoolean(result.isSucess);
		backMsg.writeUtf8String(result.tips);
		session.send(backMsg);
		
		result.doFinally(role);

		if (result.isSucess) {
			KPushRandomGoodsMsg.pushMsg(role, result.freeRefreshTypeEnum);
		}
	}
}
