package com.kola.kmp.logic.mission.message;

import static com.kola.kmp.protocol.mission.KMissionProtocol.CM_SEND_NOVICE_GUIDE_FUNCTION_COMPLETED;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.mission.IMissionMenuImpl;
import com.kola.kmp.logic.mission.KMission;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.mission.KMissionModuleSupportImpl;
import com.kola.kmp.logic.mission.KMissionSet;
import com.kola.kmp.logic.mission.KMissionTemplate;
import com.kola.kmp.logic.npc.menu.IMissionMenu;
import com.kola.kmp.logic.npc.menu.KMenuService;
import com.kola.kmp.logic.other.KGameMissionFunTypeEnum;
import com.kola.kmp.logic.other.KGameMissionStatusEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class NoviceGuideFunctionCompleteMsgProcesser implements
		GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new NoviceGuideFunctionCompleteMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_SEND_NOVICE_GUIDE_FUNCTION_COMPLETED;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		short functionId = msg.readShort();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);

		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role
				.getId());

		List<KMission> allUnclosedMission = missionSet.getAllUnclosedMission();

		for (KMission unclosedMission : allUnclosedMission) {
			KMissionTemplate missionTemplate = unclosedMission
					.getMissionTemplate();
			if (missionTemplate != null
					&& missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION
					&& missionTemplate.isNewPlayerGuildMission
					&& unclosedMission.getMissionStatus() == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH
					&& missionTemplate.missionCompleteCondition
							.getUseFunctionTask().functionId == functionId) {
				if (unclosedMission
						.notifyUseFunctionMissionAndChangeMissionStatus(
								functionId,
								missionTemplate.missionCompleteCondition
										.getUseFunctionTask().useCount)) {

					// 通知客户端显示任务条件达成特效
					KMissionModuleExtension
							.getManager()
							.processMissionStatusChangeEffect(
									role,
									missionTemplate,
									KMissionTemplate.MISSION_EFFECT_TYPE_CONDITION_FINISHED);

					// 更新任务列表
					KMissionModuleExtension
							.getManager()
							.processUpdateMissionListWhileAcceptedMissionStatusChanged(
									role, unclosedMission);

					// /////////更新NPC菜单//////////////////

					int acceptMissionNPCTemplateId = missionTemplate.acceptMissionNPCTemplate.templateId;
					int submitMissionNPCTemplateId = missionTemplate.submitMissionNPCTemplate.templateId;
					if (acceptMissionNPCTemplateId == submitMissionNPCTemplateId) {
						IMissionMenuImpl menu = KMissionModuleSupportImpl
								.constructIMissionMenu(
										role,
										missionTemplate,
										missionTemplate.getMissionNameByStatusType(
												unclosedMission
														.getMissionStatus(),
												role), unclosedMission
												.getMissionStatus().statusType,
										IMissionMenu.ACTION_AFTER_TALK_SUBMIT,
										missionTemplate.getCompletedMissionDialog());
						List<IMissionMenu> menuList = new ArrayList<IMissionMenu>();
						menuList.add(menu);

						KMenuService.synNpcAddOrUpdateMenus(role,
								submitMissionNPCTemplateId, menuList);
					}
					//更新功能开启状态
					missionSet.addOrUpdateFunctionInfo(functionId, true, true);

				}
			}
		}
	}
}
