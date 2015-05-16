package com.kola.kmp.logic.skill.message;

import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.KSkill;
import com.kola.kmp.logic.skill.KSkillMsgPackCenter;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.skill.KSkillProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KPushSkillsMsg implements KSkillProtocol {
	/**
	 * <pre>
	 * 发送技能列表给客户端
	 * 
	 * @param roleId
	 * @param skills
	 * @author CamusHuang
	 * @creation 2013-1-12 下午3:59:45
	 * </pre>
	 */
	public static void pushAllSkills(long roleId) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if(role!=null){
			pushAllSkills(role);
		}
	}
	
	public static void pushAllSkills(KRole receiver) {
		if (receiver.isOnline()) {
			KGameMessage msg = KGame.newLogicMessage(SM_PUSH_ALLSKILL_LIST);
			KSkillMsgPackCenter.packAllSkills(msg, receiver);
			receiver.sendMsg(msg);
		}
	}
	
	public static void pushNewSkills(KRole receiver, List<KSkill> newSkills) {
		if (receiver.isOnline()) {
			KGameMessage msg = KGame.newLogicMessage(SM_PUSH_NEWSKILL_LIST);
			KSkillMsgPackCenter.packSkills(msg, receiver, newSkills);
			receiver.sendMsg(msg);
		}
	}

	public static void pushSelectedSkills(KRole receiver) {
		if (receiver.isOnline()) {
			KGameMessage msg = KGame.newLogicMessage(SM_PUSH_INSTALLED_SKILLS);
			KSkillMsgPackCenter.packAllSelectedSkills(msg, receiver.getId());
			receiver.sendMsg(msg);
		}
	}

	// public static void sendSelectedSkillsToClient(long roleId, Map<Integer,
	// ISkill> skills) {
	// KGameMessage msg = KGame.newLogicMessage(SM_SYNC_SKILL_SLOTS);
	// msg.writeByte(KGameSkillModuleConfig.getInstance().MaxSkillCount);
	// for (int i=0;i<KGameSkillModuleConfig.getInstance().MaxSkillCount;i++) {
	// KGameSkill skill = (KGameSkill)skills.get(i);
	// if (skill == null) {
	// msg.writeInt(-1);
	// } else {
	// msg.writeInt(skill.getSkillTemplateId());
	// }
	// }
	// KSupportFactory.getRoleSupport().pushItem(roleId, msg);
	// }
}
