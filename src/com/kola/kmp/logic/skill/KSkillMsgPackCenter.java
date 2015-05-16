package com.kola.kmp.logic.skill;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleSkillTempAbs;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.skill.KSkillProtocol;

/**
 * <pre>
 * 由于在打包模块消息的过程中，涉及到访问及复杂的逻辑结构
 * 因此设立此类，用于打包消息
 * 
 * @author CamusHuang
 * @creation 2013-1-5 下午12:03:37
 * </pre>
 */
public class KSkillMsgPackCenter {

	public static final Logger _LOGGER = KGameLogger.getLogger(KSkillMsgPackCenter.class);

	public static void packAllSkills(KGameMessage msg, KRole role) {
		KSkillSet set = KSkillModuleExtension.getSkillSet(role.getId());
		set.rwLock.lock();
		try {
			int writeIndex = msg.writerIndex();
			msg.writeByte(0);
			int count = 0;
			{
				// 主动技能：本职业和不限职业
				Map<Integer, KSkill> iniSkills = set.searchAllSkills(true);
				Map<Integer, KRoleIniSkillTemp> allSkillTempMap = KSkillDataManager.mRoleIniSkillTempManager.getCache();
				for (KRoleIniSkillTemp temp : allSkillTempMap.values()) {
					if (temp.job == 0 || temp.job == role.getJob()) {
						KSkill skill = iniSkills.get(temp.id);
						if (skill != null) {
							packSkill(msg, skill, role.getLevel());
							count++;
						}
					}
				}
				for (KRoleIniSkillTemp temp : allSkillTempMap.values()) {
					if (temp.job == 0 || temp.job == role.getJob()) {
						KSkill skill = iniSkills.get(temp.id);
						if (skill == null) {
							if (temp.isAddForRoleUpLv()) {
								packSkill(msg, temp, role.getLevel());
								count++;
							}
						}
					}
				}
			}
			{
				// 被动技能：本职业和不限职业，且是按等级开放的
				Map<Integer, KSkill> pasSkills = set.searchAllSkills(false);
				Map<Integer, KRolePasSkillTemp> allSkillTempMap = KSkillDataManager.mRolePasSkillTempManager.getCache();
				for (KRolePasSkillTemp temp : allSkillTempMap.values()) {
					if (temp.job == 0 || temp.job == role.getJob()) {
						KSkill skill = pasSkills.get(temp.id);
						if (skill != null) {
							packSkill(msg, skill, role.getLevel());
							count++;
						}
					}
				}
				for (KRolePasSkillTemp temp : allSkillTempMap.values()) {
					if (temp.job == 0 || temp.job == role.getJob()) {
						KSkill skill = pasSkills.get(temp.id);
						if (skill == null) {
							if (temp.isAddForRoleUpLv()) {
								packSkill(msg, temp, role.getLevel());
								count++;
							}
						}
					}
				}
			}
			msg.setByte(writeIndex, count);
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 一个技能的消息结构
	 * 参考{@link KSkillProtocol#MSG_STRUCT_SKILL_DETAILS}
	 * </pre>
	 */
	private static void packSkill(KGameMessage msg, KRoleSkillTempAbs temp, int roleLv) {
		msg.writeInt(temp.id);
		msg.writeBoolean(false);
		msg.writeBoolean(temp.isIniSkill);
		msg.writeInt(1);

		// 战斗力
		int power = KSkillLogic.ExpressionForPower(temp, 1, roleLv);
		msg.writeInt(power);
	}

	/**
	 * <pre>
	 * 一个技能的消息结构
	 * 参考{@link KSkillProtocol#MSG_STRUCT_SKILL_DETAILS}
	 * </pre>
	 */
	private static void packSkill(KGameMessage msg, KSkill skill, int roleLv) {
		msg.writeInt(skill._templateId);
		msg.writeBoolean(true);
		msg.writeBoolean(skill.isInitiative());
		msg.writeInt(skill.getLv());
		// 战斗力
		KRoleSkillTempAbs temp = null;
		if (skill.isInitiative()) {
			temp = KSkillDataManager.mRoleIniSkillTempManager.getTemplate(skill._templateId);
		} else {
			temp = KSkillDataManager.mRolePasSkillTempManager.getTemplate(skill._templateId);
		}
		int power = KSkillLogic.ExpressionForPower(temp, skill.getLv(), roleLv);
		msg.writeInt(power);
	}

	public static void packAllSelectedSkills(KGameMessage msg, long roleId) {
		KSkillSet set = KSkillModuleExtension.getSkillSet(roleId);
		set.rwLock.lock();
		try {
			int[] skillTempSlot = set.getSkillSlotDataCache();
			msg.writeByte(skillTempSlot.length);
			for (int index = 0; index < skillTempSlot.length; index++) {
				msg.writeInt(index + 1);
				msg.writeInt(skillTempSlot[index]);
			}
		} finally {
			set.rwLock.unlock();
		}
	}

	public static void packSkills(KGameMessage msg, KRole receiver, List<KSkill> newSkills) {
		msg.writeByte(newSkills.size());
		for (KSkill skill : newSkills) {
			packSkill(msg, skill, receiver.getLevel());
		}
	}

}
