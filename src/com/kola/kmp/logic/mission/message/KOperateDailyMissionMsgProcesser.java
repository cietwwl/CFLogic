package com.kola.kmp.logic.mission.message;

import static com.kola.kmp.protocol.mission.KMissionProtocol.CM_OPERATE_DAILY_MISSION;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.DailyMissionOperateTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GlobalTips;

/**
 * 角色请求操作某个日常（修行）任务的消息处理器{@link KGameMissionProtocol#CM_OPERATE_DAILY_MISSION}
 * 
 * @author zhaizl
 * 
 */
public class KOperateDailyMissionMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KOperateDailyMissionMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_OPERATE_DAILY_MISSION;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		int missionTemplateId = msg.readInt();
		byte operate = msg.readByte();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);

		DailyMissionOperateTypeEnum operateType = DailyMissionOperateTypeEnum
				.getEnum(operate);
		if (operateType != null) {
			switch (operateType) {
			case OPERATE_TYPE_SUBMIT:
				KMissionModuleExtension.getManager().getDailyMissionManager()
						.playerRoleSubmitDailyMission(role, missionTemplateId);
				KDialogService.sendNullDialog(session);
				break;
			case OPERATE_TYPE_AUTO_SUBMIT:
				KMissionModuleExtension
						.getManager()
						.getDailyMissionManager()
						.playerRoleAutoSubmitDailyMission(role,
								missionTemplateId, true);
				break;
			default:
				KDialogService.sendNullDialog(session);
				// KDialogService.sendUprisingDialog(role, "服务器繁忙，请稍后再试！");
				KDialogService.sendUprisingDialog(role,
						GlobalTips.getTipsServerBusy());
				break;
			}
		}
	}
}
