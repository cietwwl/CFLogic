package com.kola.kmp.logic.reward.garden.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.reward.garden.KGardenCenter;
import com.kola.kmp.logic.reward.garden.KGardenRoleExtCACreator;
import com.kola.kmp.logic.reward.garden.KRoleGarden;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.RelationShipTips;
import com.kola.kmp.protocol.reward.KRewardProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KGardenSpeedMsg implements GameMessageProcesser, KRewardProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGardenSpeedMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GARDEN_SPEED;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long oppRoleId = msg.readLong();
		// -------------
		CommonResult_Ext result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new CommonResult_Ext();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, oppRoleId, result);
			return;
		}

		if (oppRoleId != role.getId()) {
			if (!KSupportFactory.getRelationShipModuleSupport().isInFriendList(role.getId(), oppRoleId)) {
				result = new CommonResult_Ext();
				result.tips = RelationShipTips.请先添加对方为好友;
				dofinally(session, role, oppRoleId, result);
				return;
			}
		}

		result = KGardenCenter.dealMsg_Speed(role, oppRoleId);
		dofinally(session, role, oppRoleId, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, long oppRoleId, CommonResult_Ext result) {
		KGameMessage backmsg = KGame.newLogicMessage(SM_GARDEN_SPEED_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeLong(oppRoleId);
		session.send(backmsg);
		//
		result.doFinally(role);

		if (result.isSucess) {
			// 成功
			if (oppRoleId != role.getId()) {
				// 同步对方数据
				KRole oppRole = KSupportFactory.getRoleModuleSupport().getRole(oppRoleId);
				if (oppRole != null) {
					if (oppRole.isOnline()) {
						KGardenSynMsg.sendTreeDatas(oppRole, oppRoleId);
						KGardenSynMsg.sendNewFeets(oppRole);
					} else {
						KRoleGarden garden = KGardenRoleExtCACreator.getRoleGarden(oppRole.getId());
						garden.getAndClearNewFeetLogs();
					}
				}
				
				// 通知日常任务
				KSupportFactory.getMissionSupport().notifyUseFunction(role, KUseFunctionTypeEnum.灌溉好友庄园);
				
			} else {
				// 同步我方数据
				KGardenSynMsg.sendMyGardenData(role);
			}

			// 通知活跃度任务
			KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.浇灌庄园植物);

		} else {
			// 失败：同步我方数据
			if (oppRoleId == role.getId()) {
				KGardenSynMsg.sendMyGardenData(role);
			}
		}
	}
}
