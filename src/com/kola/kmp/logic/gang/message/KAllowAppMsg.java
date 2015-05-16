package com.kola.kmp.logic.gang.message;

import java.util.Arrays;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResult_AllowApp;
import com.kola.kmp.logic.util.tips.GangTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KAllowAppMsg implements GameMessageProcesser, KGangProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KAllowAppMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANG_ALLOW_APP;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long appRoleId = msg.readLong();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			GangResult_AllowApp result = new GangResult_AllowApp();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, appRoleId, result);
			return;
		}
		KRole appRole = KSupportFactory.getRoleModuleSupport().getRole(appRoleId);
		if (appRole == null) {
			GangResult_AllowApp result = new GangResult_AllowApp();
			result.tips = GlobalTips.此角色数据暂不能访问;
			dofinally(session, role, appRoleId, result);
			return;
		}
		// 军团--批准申请
		GangResult_AllowApp result = KGangLogic.dealMsg_arrowApp(role, appRole);
		dofinally(session, role, appRoleId, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, long appRoleId, GangResult_AllowApp result) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_ALLOW_APP_RESULT);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		msg.writeLong(appRoleId);
		session.send(msg);
		
		if (result.isSucess) {

			// 军团频道；上浮提示；日志
			KGangLogic.addDialy(result.gang, result.extCASet, StringUtil.format(GangTips.x批准了x加入军团, role.getExName(), result.targetMember.getExtRoleName()), true, true, true);
			// 清理所有申请书
			KGangLogic.clearAppFromAllGangs(appRoleId);
			// 推送军团数据给新成员
			KSyncOwnGangDataMsg.sendMsg(appRoleId, result.gang, result.extCASet);// 军团数据
			// 通知新成员
			KDialogService.sendUprisingDialog(appRoleId, StringUtil.format(GangTips.恭喜你您已被批准加入x军团, result.gang.getExtName()));
			// 更新成员列表
			KSyncMemberListMsg.sendMsg(result.gang, Arrays.asList(result.targetMember), null);
			// 同步军团信息到所有成员客户端
			KSyncGangDataMsg.sendMsg(result.gang);
			// 提示申请列表有变化
			KSyncAppChangeCountMsg.sendMsg(result.gang, -1);
			
			// 通知角色刷新属性值
			KGangLogic.notifyEffectAttrChange(appRoleId, null);

		}
	}
}
