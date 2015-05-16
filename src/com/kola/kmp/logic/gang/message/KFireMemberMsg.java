package com.kola.kmp.logic.gang.message;

import java.util.Arrays;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResult_SetPosition;
import com.kola.kmp.logic.util.tips.GangTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KFireMemberMsg implements GameMessageProcesser, KGangProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KFireMemberMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANG_FIRE_MEMBER;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long targetRoleId = msg.readLong();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			GangResult_SetPosition result = new GangResult_SetPosition();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, result);
			return;
		}
		// 军团--开除成员
		GangResult_SetPosition result = KGangLogic.dealMsg_fireMember(role, targetRoleId);
		dofinally(session, role, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, GangResult_SetPosition result) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_FIRE_MEMBER_RESULT);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		session.send(msg);

		if (result.isSucess) {

			// 军团频道；上浮提示；日志
			KGangLogic.addDialy(result.gang, result.extCASet, StringUtil.format(GangTips.x将x开除出军团, role.getExName(), result.targetMember.getExtRoleName()), true, true, true);

			// 通知被开除者
			KSyncDismissMsg.sendMsg(result.targetMember._roleId, GangTips.你已被开除出军团);
			// 更新成员列表
			KSyncMemberListMsg.sendMsg(result.gang, null, Arrays.asList(result.targetMember._roleId));
			KSyncGangDataMsg.sendMsg(result.gang);
			
			// 通知角色刷新属性值
			KGangLogic.notifyEffectAttrChange(result.targetMember._roleId, null);
			// // 通知称号模块
			// KSupportFactory.getGameTitleSupport().notifyGangLeaveMember(result.gang.getId(),
			// result.member._roleId, false);
		}
	}
}
