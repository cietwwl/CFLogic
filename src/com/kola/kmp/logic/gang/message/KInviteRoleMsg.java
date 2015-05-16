package com.kola.kmp.logic.gang.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResultExt;
import com.kola.kmp.logic.util.ResultStructs.GangResult_AcceptInvite;
import com.kola.kmp.logic.util.tips.GangTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KInviteRoleMsg implements GameMessageProcesser, KGangProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KInviteRoleMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANG_INVITE_FOR_ROLE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		String oppRoleName = msg.readUtf8String();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			GangResultExt result = new GangResultExt();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, null, result);
			return;
		}
		KRole oppRole = KSupportFactory.getRoleModuleSupport().getRole(oppRoleName);
		if (oppRole == null) {
			GangResultExt result = new GangResultExt();
			result.tips = GlobalTips.角色不存在;
			dofinally(session, role, oppRole, result);
			return;
		} else if(!oppRole.isOnline()){
			GangResultExt result = new GangResultExt();
			result.tips = GlobalTips.角色不在线;
			dofinally(session, role, oppRole, result);
			return;
		}
		
		// 军团--邀请加入军团
		GangResultExt result = KGangLogic.dealMsg_inviteForGang(role, oppRole);
		
		dofinally(session, role, oppRole, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, KRole oppRole, GangResultExt result) {
		KDialogService.sendUprisingDialog(session, result.tips);
		result.doFinally(role);
		
		if(result.isSucess){
			//向对方发出邀请，二次确认
			List<KDialogButton> buttons = new ArrayList<KDialogButton>();
			buttons.add(KDialogButton.CANCEL_BUTTON);
			buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_ACCEPT_GANG_INVITE, result.gang.getId()+"", KDialogButton.CONFIRM_DISPLAY_TEXT));
			KDialogService.sendFunDialog(oppRole, "", StringUtil.format(GangTips.收到来自x级军团x的邀请是否接受, result.gang.getLevel(), result.gang.getExtName()), buttons, true, (byte) -1);
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
	public static void confirmByDialog(KGamePlayerSession session, String script) {
		long gangId = Long.parseLong(script);
		// -------------
		KRole oppRole = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (oppRole == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.服务器繁忙请稍候再试);
			return;
		}
		// -------------
		GangResult_AcceptInvite result = KGangLogic.dealMsg_acceptInvite(oppRole, gangId);
		//
		KDialogService.sendUprisingDialog(session, result.tips);
		
		if (result.isSucess) {

			// 军团频道；上浮提示；日志
			KGangLogic.addDialy(result.gang, result.extCASet, StringUtil.format(GangTips.x接受邀请加入军团, oppRole.getExName()), true, true, true);
			// 清理所有申请书
			KGangLogic.clearAppFromAllGangs(oppRole.getId());
			// 推送军团数据给新成员
			KSyncOwnGangDataMsg.sendMsg(oppRole.getId(), result.gang, result.extCASet);// 军团数据
			// 更新成员列表
			KSyncMemberListMsg.sendMsg(result.gang, Arrays.asList(result.opMember), null);
			// 同步军团信息到所有成员客户端
			KSyncGangDataMsg.sendMsg(result.gang);
			// 提示申请列表有变化
			if(result.isApp){
				KSyncAppChangeCountMsg.sendMsg(result.gang, -1);
			}
			// 通知角色刷新属性值
			KGangLogic.notifyEffectAttrChange(oppRole.getId(), null);

		}
	}	
}
