package com.kola.kmp.logic.gang.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.KGangConfig;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResult_SetPosition;
import com.kola.kmp.logic.util.tips.GangTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KMakeOverSirMsg implements GameMessageProcesser, KGangProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KMakeOverSirMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANG_MAKEOVER_SIR;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long nextSirRoleId = msg.readLong();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			GangResult_SetPosition result = new GangResult_SetPosition();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, result);
			return;
		}

		KRole nextSirRole = KSupportFactory.getRoleModuleSupport().getRole(nextSirRoleId);
		if (nextSirRole == null) {
			GangResult_SetPosition result = new GangResult_SetPosition();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, result);
			return;
		}

		// 角色等级
		if (nextSirRole.getLevel() < KGangConfig.getInstance().CreateGangMinRoleLevel) {
			GangResult_SetPosition result = new GangResult_SetPosition();
			result.tips = StringUtil.format(GangTips.等级必须达到x级才能接任军团长, KGangConfig.getInstance().CreateGangMinRoleLevel);
			dofinally(session, role, result);
			return;
		}

		// 发送菜单，消费二次确认
		List<KDialogButton> buttons = new ArrayList<KDialogButton>();
		buttons.add(KDialogButton.CANCEL_BUTTON);
		buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_MAKEOVER_SIR, nextSirRoleId + "", KDialogButton.CONFIRM_DISPLAY_TEXT));
		KDialogService.sendFunDialog(session, "", StringUtil.format(GangTips.确认是否将团长转让给x, nextSirRole.getExName()), buttons, true, (byte) -1);
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
		long nextSirRoleId = Long.parseLong(script);
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			GangResult_SetPosition result = new GangResult_SetPosition();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, result);
			return;
		}

		KRole nextSirRole = KSupportFactory.getRoleModuleSupport().getRole(nextSirRoleId);
		if (nextSirRole == null) {
			GangResult_SetPosition result = new GangResult_SetPosition();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, result);
			return;
		}
		// -------------
		// 军团--转让团长
		GangResult_SetPosition result = KGangLogic.dealMsg_makeOverSir(role, nextSirRole);
		dofinally(session, role, result);
	}

	private static void dofinally(KGamePlayerSession session, KRole role, GangResult_SetPosition result) {
		KDialogService.sendUprisingDialog(session, result.tips);

		if (result.isSucess) {

			// 军团频道；上浮提示；日志
			KGangLogic.addDialy(result.gang, result.extCASet, StringUtil.format(GangTips.x将团长禅让给了x, role.getExName(), result.targetMember.getExtRoleName()), true, true, true);
			// 更新成员列表
			KSyncMemberListMsg.sendMsg(result.gang, Arrays.asList(result.opMember, result.targetMember), null);

			// 推送APP界面列表数量
			KSyncAppChangeCountMsg.sendMsg(result.targetMember._roleId, Byte.MIN_VALUE);
			int count = result.extCASet.getAppCache().getDataCache().size();
			if (count > 0) {
				KSyncAppChangeCountMsg.sendMsg(result.targetMember._roleId, count);
			}

			// // 通知称号模块
			// KSupportFactory.getGameTitleSupport().notifyGangChangeSir(result.gang.getId(),
			// roleId, nextSirRoleId);

		}
	}
}
