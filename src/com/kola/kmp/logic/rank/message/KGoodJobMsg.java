package com.kola.kmp.logic.rank.message;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KRankTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.rank.KRankLogic;
import com.kola.kmp.logic.rank.gang.KGangRankTypeEnum;
import com.kola.kmp.logic.rank.teampvp.KTeamPVPRankLogic;
import com.kola.kmp.logic.rank.teampvp.KTeamPVPRankTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.ResultStructs.RankResult_GoodJob;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.RankTips;
import com.kola.kmp.protocol.rank.KRankProtocol;

public class KGoodJobMsg implements GameMessageProcesser, KRankProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGoodJobMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GOOD_JOB;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		byte rankType = msg.readByte();
		long beGoodElementId = msg.readLong();
		byte goodTime = msg.readByte();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			CommonResult_Ext result = new CommonResult_Ext();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, result, rankType, beGoodElementId, goodTime);
			return;
		}

		RankResult_GoodJob result = dealMsg_goodJob(role, rankType, beGoodElementId, goodTime, false);

		if (result.isGoConfirm) {
			// 需要二次确认
			List<KDialogButton> buttons = new ArrayList<KDialogButton>();
			buttons.add(KDialogButton.CANCEL_BUTTON);
			buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_RANKGOODJOBPAY, rankType + "," + beGoodElementId + "," + goodTime, KDialogButton.CONFIRM_DISPLAY_TEXT));
			KDialogService.sendFunDialog(session, "", result.tips, buttons, true, (byte) -1);
			return;
		}

		// 不需要二次确认
		dofinally(session, role, result, rankType, beGoodElementId, goodTime);
	}
	
	private static RankResult_GoodJob dealMsg_goodJob(KRole role, byte rankType,long beGoodElementId,byte goodTime, boolean isConfirm){
		RankResult_GoodJob result=null;
		{
			KRankTypeEnum type = KRankTypeEnum.getEnum(rankType);
			if (type != null) {
				result = KRankLogic.dealMsg_goodJob(role, type, beGoodElementId, goodTime, isConfirm);
				return result;
			}
		}
		{
			KTeamPVPRankTypeEnum type = KTeamPVPRankTypeEnum.getEnum(rankType);
			if (type != null) {
				result = KTeamPVPRankLogic.dealMsg_goodJob(role, type, beGoodElementId, goodTime, isConfirm);
				return result;
			}
		}
		{
			KGangRankTypeEnum type = KGangRankTypeEnum.getEnum(rankType);
			if (type != null) {
				result = KRankLogic.dealMsg_goodJob(role, type, beGoodElementId, goodTime, isConfirm);
				return result;
			}
		}
		
		result = new RankResult_GoodJob();
		result.tips = RankTips.此排行榜未开放;
		return result;		
	}

	/**
	 * <pre>
	 * 通过菜单确认
	 * 
	 * @param session
	 * @param slotId
	 * @author CamusHuang
	 * @creation 2013-6-7 上午10:46:21
	 * </pre>
	 */
	public static void confirmByDialog(KGamePlayerSession session, String script) {
		String[] scrs = script.split(",");
		byte rankType = Byte.parseByte(scrs[0]);
		long beGoodElementId = Long.parseLong(scrs[1]);
		byte goodTime = Byte.parseByte(scrs[2]);
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			CommonResult_Ext result = new CommonResult_Ext();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, result, rankType, beGoodElementId, goodTime);
			return;
		}
		// -------------
		RankResult_GoodJob result = dealMsg_goodJob(role, rankType, beGoodElementId, goodTime, true);
			
		// 不需要二次确认
		dofinally(session, role, result, rankType, beGoodElementId, goodTime);
	}

	private static void dofinally(KGamePlayerSession session, KRole role, CommonResult_Ext result, byte rankType, long elementId, byte goodTime) {
		KDialogService.sendNullDialog(session);// 解锁
		// -------------
		KGameMessage backmsg = KGame.newLogicMessage(SM_GOOD_JOB_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeByte(rankType);
		backmsg.writeLong(elementId);
		backmsg.writeByte(goodTime);
		session.send(backmsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);

		// 重新推送冠军榜
		KGetTopRankMsg.pushTopRank(session);

		if (result.isSucess) {
			KSupportFactory.getRewardModuleSupport().recordFuns(role, KVitalityTypeEnum.排行榜点赞, goodTime);
		}
	}
}