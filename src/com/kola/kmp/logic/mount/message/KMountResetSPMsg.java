package com.kola.kmp.logic.mount.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.mount.KMountLogic;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.reward.login.KLoginCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.AddCheckResult;
import com.kola.kmp.logic.util.ResultStructs.MountResult_ResetSP;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.mount.KMountProtocol;

/**
 * <pre>
 * 装备强化消息
 * 
 * @author CamusHuang
 * @creation 2012-12-10 下午4:35:52
 * </pre>
 */
public class KMountResetSPMsg implements GameMessageProcesser, KMountProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KMountResetSPMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_MOUNT_RESET_SP;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int modelId = msg.readInt();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			MountResult_ResetSP result = new MountResult_ResetSP();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, modelId, result);
			return;
		}
		MountResult_ResetSP result = KMountLogic.dealMsg_resetSP(role, modelId, false);

		// -------------

		if (!result.isGoConfirm) {
			KDialogService.sendNullDialog(session);
			dofinally(session, role, modelId, result);
			return;
		}
		// -------------

		// 发送菜单，消费二次确认
		List<KDialogButton> buttons = new ArrayList<KDialogButton>();
		buttons.add(KDialogButton.CANCEL_BUTTON);
		buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_PAY_RESET_SP, modelId+"", KDialogButton.CONFIRM_DISPLAY_TEXT));
		KDialogService.sendFunDialog(role, "", result.tips, buttons, true, (byte) -1);

	}

	public static void confirmByDialog(KGamePlayerSession session, String script) {
		MountResult_ResetSP result = null;

		int modelId = Integer.parseInt(script);

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new MountResult_ResetSP();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, modelId, result);
			return;
		}

		result = KMountLogic.dealMsg_resetSP(role, modelId, true);

		//
		KDialogService.sendNullDialog(session);
		dofinally(session, role, modelId, result);
		return;
	}

	private static void dofinally(KGamePlayerSession session, KRole role, int modelId, MountResult_ResetSP result) {

		KGameMessage backmsg = KGame.newLogicMessage(SM_MOUNT_RESET_SP_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeInt(modelId);
		backmsg.writeInt(result.releaseSP);
		if (result.isSucess) {
			Collection<Integer> skillIds = result.mountTemplate.skillIdList;
			backmsg.writeByte(skillIds.size());
			for (int skillId : skillIds) {
				backmsg.writeInt(skillId);
			}
		}
		session.send(backmsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);
	}
}
