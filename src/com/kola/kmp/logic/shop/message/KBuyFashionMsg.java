package com.kola.kmp.logic.shop.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.fashion.KFashionLogic;
import com.kola.kmp.logic.fashion.message.KPushFashionsMsg;
import com.kola.kmp.logic.fashion.message.KSelectFashionMsg;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KRankTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.rank.KRankLogic;
import com.kola.kmp.logic.rank.message.KGetTopRankMsg;
import com.kola.kmp.logic.rank.teampvp.KTeamPVPRankLogic;
import com.kola.kmp.logic.rank.teampvp.KTeamPVPRankTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.ResultStructs.FashionResult_Buy;
import com.kola.kmp.logic.util.ResultStructs.RankResult_GoodJob;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.shop.KShopProtocol;

public class KBuyFashionMsg implements GameMessageProcesser, KShopProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KBuyFashionMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_BUY_FASHION;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int fashionId = msg.readInt();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			FashionResult_Buy result = new FashionResult_Buy();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, result, fashionId);
			return;
		}
		
		
		FashionResult_Buy result = KFashionLogic.dealMsg_buyFashion(role, fashionId, false);
		
		// -------------
		if (result.isGoConfirm) {
			// 解锁
			FashionResult_Buy tempresult = new FashionResult_Buy();
			tempresult.tips = UtilTool.getNotNullString(null);
			dofinally(session, role, tempresult, fashionId);
			
			// 需要二次确认
			List<KDialogButton> buttons = new ArrayList<KDialogButton>();
			buttons.add(KDialogButton.CANCEL_BUTTON);
			buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_BUY_FASHION, fashionId + "", KDialogButton.CONFIRM_DISPLAY_TEXT));
			KDialogService.sendFunDialog(session, "", result.tips, buttons, true, (byte) -1);
			return;
		}

		// 不需要二次确认
		dofinally(session, role, result, fashionId);
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
		int fashionId = Integer.parseInt(script);
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			FashionResult_Buy result = new FashionResult_Buy();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, result, fashionId);
			return;
		}
		// -------------
		FashionResult_Buy result = KFashionLogic.dealMsg_buyFashion(role, fashionId, true);
		//
		dofinally(session, role, result, fashionId);
	}	

	private static void dofinally(KGamePlayerSession session, KRole role, FashionResult_Buy result, int fashionId) {
		// 处理消息的过程
		KGameMessage backMsg = KGame.newLogicMessage(SM_BUY_FASHION_RESULT);
		backMsg.writeBoolean(result.isSucess);
		backMsg.writeUtf8String(result.tips);
		backMsg.writeInt(fashionId);
		backMsg.writeLong(result.effectTime);
		session.send(backMsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);
		
		if (result.isSucess) {
			// 同步全部时装
			KPushFashionsMsg.pushAllFashions(role);
			// 自动穿戴
			KFashionLogic.autoSelectFashionForBuy(role, fashionId, true);
		}
	}
}
