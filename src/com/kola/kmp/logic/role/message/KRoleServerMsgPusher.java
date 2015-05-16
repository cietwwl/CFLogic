package com.kola.kmp.logic.role.message;

import static com.kola.kmp.protocol.role.KRoleProtocol.SM_PLAYERROLE_ATTRIBUTE_REFURBISH;
import static com.kola.kmp.protocol.role.KRoleProtocol.SM_PLAYERROLE_UPGRADE;
import static com.kola.kmp.protocol.role.KRoleProtocol.SM_SYNCHRONIZE_ATTRIBUTE;
import static com.kola.kmp.protocol.role.KRoleProtocol.SM_SYNC_GET_PLAYERROLE_LIST;
import static com.kola.kmp.protocol.role.KRoleProtocol.SM_SYNC_PLAYERROLE_JOIN_GAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.player.KGamePlayer;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.other.KEquipmentTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KItemQualityEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.role.IRoleBaseInfo;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.role.KRoleModuleManager;
import com.kola.kmp.logic.role.KRoleTemplate;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.IRoleEquipShowData;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.protocol.role.KRoleProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KRoleServerMsgPusher {

	private static final String _attrIncFromatter = "{}+{}";
	private static final String _attrDecFromatter = "{}{}";
	
	private static final List<KGameAttrType> _lvUpAttrSeqList = Arrays.asList(KGameAttrType.MAX_HP, KGameAttrType.ATK, KGameAttrType.DEF, KGameAttrType.HIT_RATING, KGameAttrType.CRIT_RATING, KGameAttrType.RESILIENCE_RATING);
	
	static void sendRoleList(KGamePlayerSession session) {
		KGamePlayer player = session.getBoundPlayer();
		List<IRoleBaseInfo> list;
		if (player != null) {
			list = KSupportFactory.getRoleModuleSupport().getRoleList(session.getBoundPlayer().getID());
		} else {
			list = new ArrayList<IRoleBaseInfo>();
		}
		KRoleTemplate template = null;
		KJobTypeEnum jobType = null;
		IRoleEquipShowData temp = null;
		int endSize = list.size();
		if(endSize > KRoleModuleConfig.getMaxRoleShowCountOfPlayer()) {
			endSize = KRoleModuleConfig.getMaxRoleShowCountOfPlayer();
		}
		KGameMessage msg = KGame.newLogicMessage(SM_SYNC_GET_PLAYERROLE_LIST);
		msg.writeByte(endSize);
		boolean hasRedWeapon = false;
		for (int i = 0; i < endSize; i++) {
			hasRedWeapon = false;
			IRoleBaseInfo info = list.get(i);
			template = KRoleModuleManager.getRoleTemplate(info.getType());
			jobType = KJobTypeEnum.getJob(template.job);
			msg.writeLong(info.getId());
			msg.writeInt(template.inMapResId);
			msg.writeInt(template.job);
			msg.writeUtf8String(jobType.getJobName());
			msg.writeUtf8String(info.getName());
			msg.writeShort(info.getLevel());
			msg.writeByte(info.getEquipmentRes().size());
			for(Iterator<IRoleEquipShowData> itr = info.getEquipmentRes().iterator(); itr.hasNext();) {
				temp = itr.next();
				msg.writeByte(temp.getPart());
				msg.writeUtf8String(temp.getRes());
				if(temp.getPart() == KEquipmentTypeEnum.主武器.sign) {
					hasRedWeapon = temp.getQuality() == KItemQualityEnum.无敌的;
				}
			}
			msg.writeUtf8String(info.getFashionRes());
			msg.writeInt(info.getEquipSetRes()[0]);
			msg.writeInt(info.getEquipSetRes()[1]);
			msg.writeBoolean(hasRedWeapon);
		}
		msg.writeInt(KRoleModuleManager.getDefaultTemplateId());
		session.send(msg);
	}
	
	static void packRoleAttributeToMsg(KRole role, KGameMessage msg) {
		msg.writeLong(role.getId());
		msg.writeInt(role.getHeadResId());
		msg.writeInt(role.getAnimationHeadResId());
		msg.writeInt(role.getInMapResId());
		msg.writeUtf8String(role.getName());
		msg.writeByte(role.getJob());
		msg.writeUtf8String(KJobTypeEnum.getJobName(role.getJob()));
		msg.writeInt(role.getLevel());
		msg.writeInt(role.getCurrentExp());
		msg.writeInt(role.getUpgradeExp());
		msg.writeInt(role.getPhyPower());
		msg.writeInt(role.getMaxPhyPower());
		KGameAttrType[] displays = KGameAttrType.DISPLAY_FOR_ROLE;
		KGameAttrType type;
		msg.writeByte(displays.length);
		for (int i = 0; i < displays.length; i++) {
			type = displays[i];
			msg.writeShort(type.sign);
			msg.writeInt(role.getAttributeByType(type));
		}
	}
	
	public static void sendLevelUpDialog(KRole role, Map<KGameAttrType, Integer> upgradeAttrs, int preLv) {
		List<Short> openList = KSupportFactory.getMissionSupport().getOpenFuncIds(preLv, role.getLevel());
		KGameMessage msg = KGame.newLogicMessage(KRoleProtocol.SM_ROLE_UPGRADE_SHOW_DIALOG);
		msg.writeByte(upgradeAttrs.size());
		if (upgradeAttrs.size() > 0) {
			Map.Entry<KGameAttrType, Integer> entry;
			KGameAttrType attr;
			for (int i = 0; i < _lvUpAttrSeqList.size(); i++) {
				attr = _lvUpAttrSeqList.get(i);
				Integer value = upgradeAttrs.get(attr);
				if (value != null) {
					msg.writeUtf8String(attr.getName());
					msg.writeInt(role.getAttributeByType(attr));
					msg.writeInt(value);
				}
			}
			for (Iterator<Map.Entry<KGameAttrType, Integer>> itr = upgradeAttrs.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				if(upgradeAttrs.containsKey(entry.getKey())) {
					continue;
				}
				msg.writeUtf8String(entry.getKey().getName());
				msg.writeInt(role.getAttributeByType(entry.getKey()));
				msg.writeInt(entry.getValue());
			}
		}
		msg.writeByte(openList.size());
		for(int i = 0; i < openList.size(); i++) {
			msg.writeShort(openList.get(i));
		}
		role.sendMsg(msg);
	}
	
	public static void sendInitRoleData(KGamePlayerSession session, KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_SYNC_PLAYERROLE_JOIN_GAME);
		packRoleAttributeToMsg(role, msg);
		List<Integer> allSkills = KSupportFactory.getSkillModuleSupport().getRoleAllIniSkills(role.getId());
		List<Integer> inUseSkills = KSupportFactory.getSkillModuleSupport().getRoleInUseIniSkills(role.getId());
		msg.writeByte(allSkills.size());
		for(int i = 0; i < allSkills.size(); i++) {
			msg.writeInt(allSkills.get(i));
		}
		msg.writeByte(inUseSkills.size());
		for(int i = 0; i < inUseSkills.size(); i++) {
			msg.writeInt(inUseSkills.get(i));
		}
		session.send(msg);
	}
	
	public static void sendRefurbishAttribute(KRole role, Map<KGameAttrType, Integer> map) {
		KGameMessage msg = KGame.newLogicMessage(SM_PLAYERROLE_ATTRIBUTE_REFURBISH);
		msg.writeBoolean(true);
		msg.writeLong(role.getId());
		msg.writeByte(map.size());
		Map.Entry<KGameAttrType, Integer> current;
		for(Iterator<Map.Entry<KGameAttrType, Integer>> itr = map.entrySet().iterator(); itr.hasNext();) {
			current = itr.next();
			msg.writeShort(current.getKey().sign);
			msg.writeInt(current.getValue().intValue());
		}
		role.sendMsg(msg);
	}
	
	/**
	 * <pre>
	 * 这条消息原则上和 {@link KRoleProtocol#SM_PLAYERROLE_ATTRIBUTE_REFURBISH}区别不大
	 * 但是客户端需求独立一条消息发送以便播放动画的时候能够区分
	 * </pre>
	 * @param role
	 * @param preBattlePower
	 * @param nowBattlePower
	 * @param map
	 */
	public static void sendAttributeChgMsg(KRole role, int preBattlePower, int nowBattlePower, Map<KGameAttrType, Integer> map) {
		map.remove(KGameAttrType.BATTLE_POWER);
		KGameMessage msg = KGame.newLogicMessage(KRoleProtocol.SM_NOTIFY_ROLE_ATTR_UPDATE);
		msg.writeInt(preBattlePower);
		msg.writeInt(nowBattlePower);
		msg.writeByte(map.size());
		Map.Entry<KGameAttrType, Integer> entry;
		String text;
		String value;
		for (Iterator<Map.Entry<KGameAttrType, Integer>> itr = map.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			switch (entry.getKey()) {
			case SHOT_DM_PCT:
			case COHESION_DM:
				value = ((float) entry.getValue() / UtilTool.HUNDRED_RATIO_UNIT) + "%";
				break;
			default:
				value = entry.getValue().toString();
				break;
			}
			if (entry.getValue() > 0) {
				text = HyperTextTool.extColor(StringUtil.format(_attrIncFromatter, entry.getKey().getName(), value), KColorFunEnum.绿色);
			} else {
				text = HyperTextTool.extColor(StringUtil.format(_attrDecFromatter, entry.getKey().getName(), value), KColorFunEnum.红色);
			}
			msg.writeUtf8String(text);
		}
		role.sendMsg(msg);
	}
	
	/**
	 * 发送消息{@link KRoleProtocol#SM_SYNCHRONIZE_ATTRIBUTE}到客户端
	 * @param role
	 * @param exp
	 */
	public static void syncAttributeToClient(KRole role, KGameAttrType type, int value) {
		KGameMessage msg = KGame.newLogicMessage(SM_SYNCHRONIZE_ATTRIBUTE);
		msg.writeShort(type.sign);
		msg.writeInt(value);
		role.sendMsg(msg);
	}
	
	/**
	 * 
	 * 发送消息{@link KRoleProtocol#SM_PLAYERROLE_UPGRADE}到客户端
	 * 
	 * @param role
	 */
	public static void sendRoleLvUpMsg(KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_PLAYERROLE_UPGRADE);
		msg.writeInt(role.getLevel());
		msg.writeInt((int)role.getCurrentHp());
		msg.writeInt(role.getUpgradeExp());
		msg.writeInt((int)role.getCurrentHp());
		msg.writeInt(role.getInMapResId());
//		msg.writeUtf8String(RoleTips.getTipsRoleLvUp(role.getLevel()));
		role.sendMsg(msg);
	}
}
