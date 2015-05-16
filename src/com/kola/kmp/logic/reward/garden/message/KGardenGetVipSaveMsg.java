package com.kola.kmp.logic.reward.garden.message;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.reward.garden.KGardenCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.reward.KRewardProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KGardenGetVipSaveMsg implements GameMessageProcesser, KRewardProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGardenGetVipSaveMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GARDEN_GET_VIPSAVE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.服务器繁忙请稍候再试);
			return;
		}

		CommonResult_Ext result = KGardenCenter.dealMsg_GetVipSaveDesc(role);
		if (!result.isSucess) {
			KDialogService.sendUprisingDialog(session, result.tips);
			return;
		}

		// 发送菜单，消费二次确认
		List<KDialogButton> buttons = new ArrayList<KDialogButton>();
		buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_GET_GARDEN_VIPSAVE, "", KDialogButton.CONFIRM_DISPLAY_TEXT));
		KDialogService.sendFunDialog(session, "", result.tips, buttons, true, (byte) -1);
		return;
	}

	public static void confirmToGetVipSave(KGamePlayerSession session, String script) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.服务器繁忙请稍候再试);
			return;
		}

		CommonResult_Ext result = KGardenCenter.dealMsg_GetVipSave(role);

		KDialogService.sendUprisingDialog(session, result.tips);
		//
		result.doFinally(role);
		if (result.isSucess) {
			KGardenSynMsg.sendVipSaveLogo(role, null);
			
			// 通知活跃度
			KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.收获庄园植物);
		}
	}
}
