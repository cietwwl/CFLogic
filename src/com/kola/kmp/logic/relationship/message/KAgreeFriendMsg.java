package com.kola.kmp.logic.relationship.message;

import java.util.List;
import java.util.Map.Entry;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KRelationShipTypeEnum;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.relationship.KRelationShipLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.RSResult_AddFriend;
import com.kola.kmp.logic.util.ResultStructs.RSResult_AddFriend.RSSynStruct;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.relationship.KRelationShipProtocol;

public class KAgreeFriendMsg implements GameMessageProcesser, KRelationShipProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KAgreeFriendMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_AGREE_FRIEND;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		boolean isAll = msg.readBoolean();
		long oppRoleId = 0;
		if (!isAll) {
			oppRoleId = msg.readLong();
		}
		// -------------
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.服务器繁忙请稍候再试);
			return;
		} 
		
		RSResult_AddFriend result = null;
		if (isAll) {
			result = KRelationShipLogic.dealMsg_agreeForFriends(role);
		} else {
			KRole oppRole = KSupportFactory.getRoleModuleSupport().getRole(oppRoleId);
			if (oppRole == null) {
				result = new RSResult_AddFriend();
				result.tips = GlobalTips.角色不存在;
			} else {
				result = KRelationShipLogic.dealMsg_agreeForFriend(role, oppRole);
			}
		}
		// -------------
		KDialogService.sendUprisingDialog(session, result.tips);
		result.doFinally(role);
		
		KPushRSsMsg.synRelationShips(result.rsSynDatas);

		if (result.rsSynDatas != null) {
			for (RSSynStruct data : result.rsSynDatas.values()) {
				for (Entry<KRelationShipTypeEnum, List<Long>> e : data.addOrUpdates.entrySet()) {
					if (e.getKey() == KRelationShipTypeEnum.好友) {
						// 通知任务
						KSupportFactory.getMissionSupport().notifyUseFunctionByCounts(data.roleId, KUseFunctionTypeEnum.添加好友, e.getValue().size());
						// 通知队伍
						for (Long tempOppRoleId : e.getValue()) {
							KSupportFactory.getTeamPVPSupport().notifyFriendAdded(data.roleId, tempOppRoleId);
						}
					}
				}
			}
		}
	}
}