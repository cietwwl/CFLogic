package com.kola.kmp.logic.reward.message;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.reward.KRewardLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.reward.KRewardProtocol;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2015-1-26 下午4:29:02
 * </pre>
 */
public class KPhyPowGetMsg implements GameMessageProcesser, KRewardProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KPhyPowGetMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_REWARD_GETPHYPOWER;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		// -------------
		CommonResult result = null;
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new CommonResult();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
		} else {
			result = KRewardLogic.dealMsg_getPhyPower(role);
		}
		// -------------
		KDialogService.sendUprisingDialog(session, result.tips);
		
		KPhyPowSyncIconMsg.instance.sendSynMsg(role, KRewardLogic.isShowPhyPowerIcon(role));
	}
}
