package com.kola.kmp.logic.gang.message;

import java.util.Arrays;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResult_DoContribution;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.GangTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.KGangProtocol;

public class KDoContributionMsg implements GameMessageProcesser, KGangProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KDoContributionMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANG_DO_CONTRIBUTION;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int moneyType = msg.readInt();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			GangResult_DoContribution result = new GangResult_DoContribution();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, null, result);
			return;
		}

		KCurrencyTypeEnum moneyEnum = KCurrencyTypeEnum.getEnum(moneyType);
		if (moneyEnum == null) {
			GangResult_DoContribution result = new GangResult_DoContribution();
			result.tips = GangTips.不能捐献此类型的货币;
			dofinally(session, role, moneyEnum, result);
			return;
		}

		// 军团--捐献
		GangResult_DoContribution result = KGangLogic.dealMsg_doContribution(role, moneyEnum);
		dofinally(session, role, moneyEnum, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, KCurrencyTypeEnum moneyEnum, GangResult_DoContribution result) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANG_DO_CONTRIBUTION_RESULT);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		if(result.isSucess){
			KGangMsgPackCenter.packContributionDatas(role.getId(), result.gang, null, msg);
		}
		session.send(msg);

		result.doFinally(role);

		if (result.isSucess) {

			// 更新成员列表
			KSyncMemberListMsg.sendMsg(result.gang, Arrays.asList(result.member), null);
			// 提示全体成员：捐献列表有变化
			KSyncContributionChangeCountMsg.sendMsg(result.gang, 1);

			// 通知现存成员：军团频道
			String tips = StringUtil.format(GangTips.x捐献x数量x货币, role.getExName(), moneyEnum.extName, result.price.currencyCount);
			KSupportFactory.getChatSupport().sendChatToAnyChannel(KChatChannelTypeEnum.军团, tips, result.gang.getId());

			// 尝试进行升级
			KGangLogic.tryToUplvGang(result.gang);

			// 更新全体成员：军团基础信息
			KSyncGangDataMsg.sendMsg(result.gang);
			
			// 通知日常任务
			KSupportFactory.getMissionSupport().notifyUseFunction(role, KUseFunctionTypeEnum.军团捐献);
		}
	}
}
