package com.kola.kmp.logic.reward.garden.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.reward.garden.KGardenCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_GardenCollect;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.reward.KRewardProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KGardenCollectMsg implements GameMessageProcesser, KRewardProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGardenCollectMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GARDEN_COLLECT;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		byte type = msg.readByte();
		// -------------
		RewardResult_GardenCollect result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new RewardResult_GardenCollect();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
		} else {
			result = KGardenCenter.dealMsg_Collect(role, type);
		}
		// -------------
		if (result.isSucess) {
			KGardenSynMsg.sendTreeData(role, role.getId(), result.treeData);
		}
		
		KGameMessage backmsg = KGame.newLogicMessage(SM_GARDEN_COLLECT_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeByte(type);
		backmsg.writeBoolean(result.diamondCount > 0);
		if (result.diamondCount > 0) {
			backmsg.writeInt(result.diamondCount);
		}
		session.send(backmsg);
		//
		result.doFinally(role);

		if (result.isSucess) {
			KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.收获庄园植物);
		}
	}
}
