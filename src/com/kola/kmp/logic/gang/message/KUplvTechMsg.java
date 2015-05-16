package com.kola.kmp.logic.gang.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.other.KGangTecTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResult_UplvTech;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KUplvTechMsg implements GameMessageProcesser, KGangProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KUplvTechMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANG_UPLV_TECH;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int sciId = msg.readShort();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			GangResult_UplvTech result = new GangResult_UplvTech();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, sciId, result);
			return;
		}
		
		// 军团--升级科技
		GangResult_UplvTech result = KGangLogic.dealMsg_uplvGangTech(role, sciId);
		dofinally(session, role, sciId, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, int sciId, GangResult_UplvTech result) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_UPLV_TECH_RESULT);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		session.send(msg);
		
		result.doFinally(role);

		if (result.isSucess) {
			// 通知在线军团成员刷新属性值
			KGangLogic.notifyEffectAttrChange(result.gang, result.tecType);
			// 同步科技数据给全体成员
			KSyncTechMsg.sendMsg(result.gang, result.extCASet, sciId);
			// CTODO 通知任务模块
			// KSupportFactory.getMissionSupport().notifyUseFunction(role.getRoleId(),
			// FunctionTypeEnum.军团科技);
			KSyncGangDataMsg.sendMsg(result.gang);
		}
	}
}
