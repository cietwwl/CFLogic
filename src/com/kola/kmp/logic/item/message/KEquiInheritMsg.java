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
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_Equi;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_InheritEquiPrice;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ItemTips;
import com.kola.kmp.protocol.item.KItemProtocol;

/**
 * <pre>
 * 装备强化消息
 * 
 * @author CamusHuang
 * @creation 2012-12-10 下午4:35:52
 * </pre>
 */
public class KEquiInheritMsg implements GameMessageProcesser, KItemProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KEquiInheritMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_INHERIT_EQUI;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long srcItemId = msg.readLong();
		long tarItemId = msg.readLong();
		boolean isOneKey = msg.readBoolean();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			ItemResult_Equi result = new ItemResult_Equi();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, srcItemId, tarItemId, result);
			return;
		}

		if (isOneKey) {
			// 一键继承
			ItemResult_InheritEquiPrice priceResult = KItemLogic.dealMsg_getInheritEquiPrice(role, srcItemId, tarItemId);
			if (priceResult.isSucess) {
				// 发送菜单，消费二次确认
				List<KDialogButton> buttons = new ArrayList<KDialogButton>();
				buttons.add(KDialogButton.CANCEL_BUTTON);
				buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_ONEKEY_INHERIT, srcItemId + "," + tarItemId, KDialogButton.CONFIRM_DISPLAY_TEXT));
				KDialogService.sendFunDialog(session, "", StringUtil.format(ItemTips.是否支付x货币类型x数量继承旧装备的属性, priceResult.commonPayGold.currencyType.extName, priceResult.commonPayGold.currencyCount),
						buttons, true, (byte) -1);
			} else {
				ItemResult_Equi result = new ItemResult_Equi();
				result.tips = priceResult.tips;
				doFinally(session, role, srcItemId, tarItemId, result);
			}
		} else {
			ItemResult_Equi result = KItemLogic.dealMsg_inheritEquipment(role, srcItemId, tarItemId);

			doFinally(session, role, srcItemId, tarItemId, result);
		}
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
	public static void confirmOneKeyInherit(KGamePlayerSession session, String script) {
		String[] temps = script.split(",");
		long srcItemId = Long.parseLong(temps[0]);
		long tarItemId = Long.parseLong(temps[1]);
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			ItemResult_Equi result = new ItemResult_Equi();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, srcItemId, tarItemId, result);
			return;
		}

		ItemResult_Equi result = KItemLogic.dealMsg_inheritEquipment(role, srcItemId, tarItemId);

		doFinally(session, role, srcItemId, tarItemId, result);
	}

	private static void doFinally(KGamePlayerSession session, KRole role, long srcItemId, long tarItemId, ItemResult_Equi result) {
		KDialogService.sendNullDialog(session);//解锁
		// -------------
		KGameMessage backmsg = KGame.newLogicMessage(SM_INHERIT_EQUI_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeLong(srcItemId);
		backmsg.writeLong(tarItemId);
		session.send(backmsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);

		if (result.isSucess) {
			// 通知日常任务
			KSupportFactory.getMissionSupport().notifyUseFunction(role, KUseFunctionTypeEnum.装备继承);
		}
	}
}
