package com.kola.kmp.logic.gang.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.rank.gang.KGangRankLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResult_UplvTech;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KCreateGangMsg implements GameMessageProcesser, KGangProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KCreateGangMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANG_CREATE_GANG;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		String gangName = msg.readUtf8String();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			GangResult_UplvTech result = new GangResult_UplvTech();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, result);
			return;
		}

		GangResult_UplvTech result = KGangLogic.dealMsg_createGang(role, gangName);
		dofinally(session, role, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, GangResult_UplvTech result) {

		if (result.isSucess) {
			// 推送军团数据给新成员---客户端要求先发此消息，再发其它消息
			KSyncOwnGangDataMsg.sendMsg(role.getId(), result.gang, result.extCASet);// 军团数据
		}

		KGameMessage backmsg = KGame.newLogicMessage(SM_GANG_CREATE_GANG_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		session.send(backmsg);

		result.doFinally(role);

		if (result.isSucess) {

			// 刷新军团排行榜
			KGangRankLogic.notifyGangCreate(result.gang);
			// 清理创建者的所有申请书
			KGangLogic.clearAppFromAllGangs(role.getId());

			//
			// KSupportFactory.getMissionSupport().notifyUseFunction(role.getRoleId(),
			// FunctionTypeEnum.创建军团);
			KWordBroadcastType _boradcastType = KWordBroadcastType.军团_x角色创建了军团x;
			KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(_boradcastType.content, role.getExName(), result.gang.getExtName()), _boradcastType);
			
			// 通知角色刷新属性值
			KGangLogic.notifyEffectAttrChange(role.getId(), null);

		}
	}
}
