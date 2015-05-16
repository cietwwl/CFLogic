package com.kola.kmp.logic.npc.menu;

import java.util.Iterator;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.npc.KNPCDataStructs.KNPCTemplate;
import com.kola.kmp.logic.npc.menu.IMissionMenu.IMissionConversation;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.npc.KNpcProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KMenuService {

	public static void sendAllNpcMenusInMap(KRole role) {
		List<Integer> npcIds = KSupportFactory.getMapSupport().getAllNpcIdsInMap(role);
		//
		KGameMessage msg = KGame.newLogicMessage(KNpcProtocol.SM_GET_NPC_MENUS_RESULT);
		//
		int sizeIndex = msg.writerIndex();
		msg.writeByte(npcIds.size());
		int count = 0;
		for (Integer npcTempId : npcIds) {
			if (packNpcMenus(msg, role, npcTempId)) {
				count++;
			}
		}
		msg.setByte(sizeIndex, count);

		role.sendMsg(msg);
	}

	private static boolean packNpcMenus(KGameMessage msg, KRole role, int npcTemplateId) {

		KNPCTemplate template = KSupportFactory.getNpcModuleSupport().getNPCTemplate(npcTemplateId);
		if (template == null) {
			return false;
		}

		List<IMissionMenu> missionList = KSupportFactory.getMissionSupport().getNpcMissionsCopy(role, template);
		IMissionMenu mainMission = searchAndRemoveMainMission(missionList);
		boolean hasMainMission = mainMission != null;
		//
		msg.writeInt(template.templateId);
		msg.writeInt(template.instanceId);
		msg.writeUtf8String(template.name);
		msg.writeInt(template.talkHeadUI);
		// NPC的谈话
		msg.writeByte(template.talkabouts.size());
		for (String temp : template.talkabouts) {
			msg.writeUtf8String(temp);
		}

		// 主线任务
		msg.writeBoolean(hasMainMission);
		if (hasMainMission) {
			packMissionMenu(msg, mainMission);
		}

		// 其它任务
		msg.writeByte(missionList.size());
		for (IMissionMenu mission : missionList) {
			packMissionMenu(msg, mission);
		}
		return true;
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param role
	 * @param npcTemplateId
	 * @param addOrUpdateMissions 新增或更新的任务，不能为NULL
	 * @param deleteMissions 要删除的任务，不能为NULL
	 * @author CamusHuang
	 * @creation 2014-3-11 下午6:01:43
	 * </pre>
	 */
	public static void synNpcMenus(KRole role, int npcTemplateId, List<IMissionMenu> addOrUpdateMissions, List<Integer> deleteMissions) {
		KGameMessage msg = KGame.newLogicMessage(KNpcProtocol.SM_UPDATE_NPC_MENU);

		msg.writeInt(npcTemplateId);

		msg.writeByte(deleteMissions.size());
		for (Integer missionId : deleteMissions) {
			msg.writeInt(missionId);
		}

		// 主线任务
		IMissionMenu mainMission = searchAndRemoveMainMission(addOrUpdateMissions);
		boolean hasMainMission = mainMission != null;
		msg.writeBoolean(hasMainMission);
		if (hasMainMission) {
			packMissionMenu(msg, mainMission);
		}

		msg.writeByte(addOrUpdateMissions.size());
		for (IMissionMenu mission : addOrUpdateMissions) {
			packMissionMenu(msg, mission);
		}
		role.sendMsg(msg);
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param role
	 * @param npcTemplateId
	 * @param addOrUpdateMissions 新增或更新的任务，不能为NULL
	 * @author CamusHuang
	 * @creation 2014-3-11 下午6:01:43
	 * </pre>
	 */
	public static void synNpcAddOrUpdateMenus(KRole role, int npcTemplateId, List<IMissionMenu> addOrUpdateMissions) {
		KGameMessage msg = KGame.newLogicMessage(KNpcProtocol.SM_UPDATE_NPC_MENU);

		msg.writeInt(npcTemplateId);

		msg.writeByte(0);

		// 主线任务
		IMissionMenu mainMission = searchAndRemoveMainMission(addOrUpdateMissions);
		boolean hasMainMission = mainMission != null;
		msg.writeBoolean(hasMainMission);
		if (hasMainMission) {
			packMissionMenu(msg, mainMission);
		}

		msg.writeByte(addOrUpdateMissions.size());
		for (IMissionMenu mission : addOrUpdateMissions) {
			packMissionMenu(msg, mission);
		}

		role.sendMsg(msg);
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param role
	 * @param npcTemplateId
	 * @param deleteMissions 要删除的任务，不能为NULL
	 * @author CamusHuang
	 * @creation 2014-3-11 下午6:01:43
	 * </pre>
	 */
	public static void synNpcDeleteMenus(KRole role, int npcTemplateId, int... deleteMissions) {
		KGameMessage msg = KGame.newLogicMessage(KNpcProtocol.SM_UPDATE_NPC_MENU);

		msg.writeInt(npcTemplateId);

		msg.writeByte(deleteMissions.length);
		for (Integer missionId : deleteMissions) {
			msg.writeInt(missionId);
		}

		msg.writeBoolean(false);

		msg.writeByte(0);

		role.sendMsg(msg);
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param missions
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-11 下午5:34:05
	 * </pre>
	 */
	private static IMissionMenu searchAndRemoveMainMission(List<IMissionMenu> missions) {
		if (missions == null || missions.isEmpty()) {
			return null;
		}

		IMissionMenu mission;
		for (Iterator<IMissionMenu> itr = missions.iterator(); itr.hasNext();) {
			mission = itr.next();
			if (mission.isMainMission()) {
				itr.remove();
				return mission;
			}
		}
		return null;
	}

	private static void packMissionMenu(KGameMessage msg, IMissionMenu mission) {
		List<IMissionConversation> conversationList = mission.getConversations();
		msg.writeInt(mission.getMissionTemplateId());
		msg.writeUtf8String(mission.getMissionName());
		msg.writeByte(mission.getMissionStatus());
		msg.writeByte(conversationList.size());
		//
		IMissionConversation conversation;
		for (int j = 0; j < conversationList.size(); j++) {
			conversation = conversationList.get(j);
			msg.writeUtf8String(conversation.getConversationConten());
			msg.writeUtf8String(conversation.getMenuText());
		}
		// byte 对话结束后的行为(0=继续对话内容,1=对话结束关闭对话框,2=请求服务器)
		msg.writeByte(mission.getActionAfterTalk());
		
		// NPC对话的任务奖励提示
		boolean isHasReward = (mission.isShowMissionReward() && (mission.missionReward()!=null));
		msg.writeBoolean(isHasReward);
		if(isHasReward){
			mission.missionReward().packMsg(msg);
		}
	}
}
