package com.kola.kmp.logic.gang.message;

import java.util.Arrays;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.gang.KGangPositionEnum;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResult_SetPosition;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.GangTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KSetPositionMsg implements GameMessageProcesser, KGangProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KSetPositionMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANG_SET_POSITION;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long targetRoleId = msg.readLong();
		byte position = msg.readByte();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			GangResult_SetPosition result = new GangResult_SetPosition();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, null, result);
			return;
		}

		KGangPositionEnum posiEnum = KGangPositionEnum.getEnum(position);
		if (posiEnum == null || posiEnum == KGangPositionEnum.军团长) {
			GangResult_SetPosition result = new GangResult_SetPosition();
			result.tips = GangTips.不能设置此职位;
			dofinally(session, role, posiEnum, result);
			return;
		}

		// 军团--任命职务
		GangResult_SetPosition result = KGangLogic.dealMsg_setPosition(role, targetRoleId, posiEnum);
		dofinally(session, role, posiEnum, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, KGangPositionEnum posiEnum, GangResult_SetPosition result) {
		KDialogService.sendUprisingDialog(session, result.tips);

		if (result.isSucess) {

			// 军团频道；上浮提示；日志
			if (posiEnum == KGangPositionEnum.成员) {
				KGangLogic.addDialy(result.gang, result.extCASet, StringUtil.format(GangTips.x被x解除了x职位, result.targetMember.getExtRoleName(), role.getExName(), posiEnum.name), true, true, true);
			} else {
				KGangLogic.addDialy(result.gang, result.extCASet, StringUtil.format(GangTips.x已将x任命为x, role.getExName(), result.targetMember.getExtRoleName(), posiEnum.name), true, true, true);
			}
			// 更新成员列表
			KSyncMemberListMsg.sendMsg(result.gang, Arrays.asList(result.targetMember), null);
			
			if (result.targetMember.getPositionEnum() != KGangPositionEnum.成员) {
				// 推送APP界面列表数量
				KSyncAppChangeCountMsg.sendMsg(result.targetMember._roleId, Byte.MIN_VALUE);
				int count = result.extCASet.getAppCache().getDataCache().size();
				if (count > 0) {
					KSyncAppChangeCountMsg.sendMsg(result.targetMember._roleId, count);
				}
			}
		}
	}
}
