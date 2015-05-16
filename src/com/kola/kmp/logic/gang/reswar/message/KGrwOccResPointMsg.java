package com.kola.kmp.logic.gang.reswar.message;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.gang.reswar.ResWarLogic;
import com.kola.kmp.logic.gang.reswar.KResWarMsgPackCenter;
import com.kola.kmp.logic.gang.reswar.ResWarCity;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResWarResult_Occ;
import com.kola.kmp.logic.util.tips.GangResWarTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.gang.reswar.KGangResWarProtocol;

public class KGrwOccResPointMsg implements GameMessageProcesser, KGangResWarProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KGrwOccResPointMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GANGRW_OCC_RESPOINT;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int cityId = msg.readByte();
		int resPointId = msg.readByte();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			GangResWarResult_Occ result = new GangResWarResult_Occ();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, cityId, resPointId, result);
			return;
		}
		GangResWarResult_Occ result = ResWarLogic.dealMsg_occResPoint(role, cityId, resPointId, false);
		if (result.confirmOccCity != null) {
			// 需要二次确认
			List<KDialogButton> buttons = new ArrayList<KDialogButton>();
			buttons.add(KDialogButton.CANCEL_BUTTON);
			buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_GIVEUP_OCCRESPOINT, cityId + "," + resPointId, KDialogButton.CONFIRM_DISPLAY_TEXT));
			KDialogService.sendFunDialog(session, "", StringUtil.format(GangResWarTips.是否放弃当前占领的x城市x资源点对此处进行占领, result.confirmOccCity.getTemplate().cityname, result.confirmOccPoint.getTemplate().name), buttons,
					true, (byte) -1);
			return;
		}

		dofinally(session, role, cityId, resPointId, result);
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
		int cityId = Integer.parseInt(scrs[0]);
		int resPointId = Integer.parseInt(scrs[1]);
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			GangResWarResult_Occ result = new GangResWarResult_Occ();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, cityId, resPointId, result);
			return;
		}
		// -------------
		GangResWarResult_Occ result = ResWarLogic.dealMsg_occResPoint(role, cityId, resPointId, true);
		dofinally(session, role, cityId, resPointId, result);
	}

	private static void dofinally(KGamePlayerSession session, KRole role, int cityId, int resPointId, GangResWarResult_Occ result) {
		KDialogService.sendNullDialog(session);//解锁
		// -------------
		KGameMessage msg = KGame.newLogicMessage(SM_GANGRW_OCC_RESPOINT_RESULT);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		msg.writeByte(cityId);
		msg.writeByte(resPointId);
		if (result.isSucess) {
			KResWarMsgPackCenter.packResPoint(msg, result.resPoint);
		}
		session.send(msg);

		result.doFinally(role);

		if (result.isGoPVP) {
			ResWarLogic.gotoPVPMap(role, cityId, result.resPoint);
		}

		if (result.syncCitys != null) {
			for (ResWarCity city : result.syncCitys) {
				// 同步资源点数据
				KGrwSynResPointsMsg.pushMsg(city);
			}
		}

	}
}
