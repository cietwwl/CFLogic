package com.kola.kmp.logic.reward.garden.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.reward.garden.KGardenCenter;
import com.kola.kmp.logic.reward.garden.KGardenDataManager;
import com.kola.kmp.logic.reward.garden.KGardenDataManager.KGardenCommonTreeDataManager.GardenCommonRewardData;
import com.kola.kmp.logic.reward.garden.KGardenRoleExtCACreator;
import com.kola.kmp.logic.reward.garden.KRoleGarden;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_Garden;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.RelationShipTips;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.protocol.reward.KRewardProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KGardenKillZombieMsg implements GameMessageProcesser, KRewardProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGardenKillZombieMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GARDEN_KILLZOMBIE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long oppRoleId = msg.readLong();
		byte type = msg.readByte();
		// -------------
		RewardResult_Garden result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new RewardResult_Garden();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			doFinally(session, role, oppRoleId, type, result);
			return;
		}
		
		if (oppRoleId != role.getId()) {
			if(!KSupportFactory.getRelationShipModuleSupport().isInFriendList(role.getId(), oppRoleId)){
				result = new RewardResult_Garden();
				result.tips = RelationShipTips.请先添加对方为好友;
				doFinally(session, role, oppRoleId, type, result);
				return;
			}
		}
			
		result = KGardenCenter.dealMsg_KillZombie(role, oppRoleId, type);
		doFinally(session, role, oppRoleId, type, result);
	}

	private void doFinally(KGamePlayerSession session, KRole role, long oppRoleId, byte type, RewardResult_Garden result) {
		// -------------
		KGameMessage backmsg = KGame.newLogicMessage(SM_GARDEN_KILLZOMBIE_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeLong(oppRoleId);
		backmsg.writeByte(type);
		session.send(backmsg);
		//
		result.doFinally(role);
		//
		if (result.isSucess) {
			// 成功：同步对方数据
			if (oppRoleId != role.getId()) {

				KRole oppRole = KSupportFactory.getRoleModuleSupport().getRole(oppRoleId);
				if (oppRole != null) {
					if (oppRole.isOnline()) {
						KGardenSynMsg.sendTreeData(oppRole, oppRoleId, result.treeData);
						KGardenSynMsg.sendNewFeets(oppRole);
						GardenCommonRewardData data = KGardenDataManager.mGardenCommonTreeDataManager.getData(result.treeData.type);
						KDialogService.sendUprisingDialog(oppRole, StringUtil.format(RewardTips.植物x已恢复正常生长, data.name));
					} else {
						KRoleGarden garden = KGardenRoleExtCACreator.getRoleGarden(oppRole.getId());
						garden.getAndClearNewFeetLogs();
					}
				}
			}
			// 通知日常任务
			KSupportFactory.getMissionSupport().notifyUseFunction(role, KUseFunctionTypeEnum.清理僵尸);
			KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.清理庄园僵尸);
		} else {
			// 失败：更新己方数据
			if (oppRoleId == role.getId()) {
				if (result.treeData != null) {
					KGardenSynMsg.sendTreeData(role, role.getId(), result.treeData);
				}
			}
		}
		
		// 打僵尸掉宝石世界广播
		if (result.specialRwardItem != null) {
			KWordBroadcastType btype = KWordBroadcastType.庄园_xx在庄园中暴打僵尸僵尸突然从口中掉出了X个X;
			String chatString = StringUtil.format(btype.content, role.getExName(), result.specialRwardItem.itemCount, result.specialRwardItem.getItemTemplate().extItemName);
			KSupportFactory.getChatSupport().sendSystemChat(chatString, btype);
		}
	}
}
