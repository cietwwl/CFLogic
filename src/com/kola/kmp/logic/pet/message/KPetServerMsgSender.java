package com.kola.kmp.logic.pet.message;

import static com.kola.kmp.protocol.pet.KPetProtocol.SM_ADD_MULTIPLE_PET;
import static com.kola.kmp.protocol.pet.KPetProtocol.SM_ADD_PET;
import static com.kola.kmp.protocol.pet.KPetProtocol.SM_DELETE_PET;
import static com.kola.kmp.protocol.pet.KPetProtocol.SM_NOTIFY_PET_LV_UP;
import static com.kola.kmp.protocol.pet.KPetProtocol.SM_SEND_ALL_PETS;
import static com.kola.kmp.protocol.pet.KPetProtocol.SM_SYNCHRONIZE_PET_EXP;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.pet.Pet;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGangTecTypeEnum;
import com.kola.kmp.logic.pet.IPetSkill;
import com.kola.kmp.logic.pet.ITransferable;
import com.kola.kmp.logic.pet.KPet;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.PetTips;
import com.kola.kmp.protocol.pet.KPetProtocol;

/**
 * 
 * @author PERRY CHAN
 */
public class KPetServerMsgSender {

	/**
	 * <pre>
	 * 发送一个随从的数据，具体数据请参考：
	 * {@link com.kola.kmp.protocol.pet.KPetProtocol#PET_MSG_DATA_STRUCTURE}
	 * </pre>
	 * @param data
	 * @param msg
	 */
	public static void packSinglePetData(ITransferable data, KGameMessage msg) {
//		msg.writeLong(data.getId());
		if (data instanceof KPet) {
			msg.writeLong(((KPet) data).getId());
		} else {
			msg.writeLong(-1);
		}
		msg.writeInt(data.getTemplateId());
		msg.writeInt(data.getHeadResId());
		msg.writeInt(data.getInMapResId());
		msg.writeUtf8String(data.getName());
		msg.writeShort(data.getLevel());
		msg.writeShort(data.getMaxLevel());
		msg.writeInt(data.getCurrentExp());
		msg.writeInt(data.getUpgradeExp());
		msg.writeInt(data.getBeComposedExp());
		msg.writeInt(data.getSwallowFee());
		msg.writeBoolean(data.isCanBeAutoSelected());
		msg.writeByte(data.getQuality().sign);
		msg.writeInt(data.getQuality().getColor());
		msg.writeUtf8String(data.getQuality().getName());
		if (data.getGrowValue() < data.getMaxGrowValue()) {
			msg.writeUtf8String(String.valueOf(data.getGrowValue()));
		} else {
			msg.writeUtf8String(String.valueOf(data.getGrowValue()) + PetTips.getTipsMaxGrowValueFlag());
		}
		msg.writeShort(data.getGrowValue());
		msg.writeUtf8String(data.getPetType().getTypeName());
		msg.writeUtf8String(data.getAtkType().getName());
		msg.writeByte(KGameAttrType.DISPLAY_FOR_PET.length);
		for(int i = 0; i < KGameAttrType.DISPLAY_FOR_PET.length; i++) {
			KGameAttrType type = KGameAttrType.DISPLAY_FOR_PET[i];
			msg.writeShort(type.sign);
			msg.writeInt(data.getAttributeByType(type));
		}
		List<IPetSkill> skillList = data.getSkillList();
		msg.writeByte(skillList.size());
		IPetSkill skill;
		for(int i = 0; i < skillList.size(); i++) {
			skill = skillList.get(i);
			msg.writeInt(skill.getSkillTemplateId());
			msg.writeByte(skill.getLv());
			msg.writeByte(skill.getRate());
		}
		if (data.getStarLv() > 0) {
			Map<KGameAttrType, Integer> map = data.getAttrOfStar();
			Map.Entry<KGameAttrType, Integer> entry;
			msg.writeByte(data.getStarLv());
			msg.writeByte(map.size());
			for (Iterator<Map.Entry<KGameAttrType, Integer>> itr = map.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				msg.writeShort(entry.getKey().sign);
				msg.writeInt(entry.getValue());
			}
		} else {
			msg.writeByte(-1);
		}
		msg.writeByte(data.getStarLvUpRateHundred());
	}
	
	/**
	 * 
	 * <pre>
	 * 发送所有宠物数据到客户端
	 * 消息结构：{@link com.kola.kmp.protocol.pet.KPetProtocol#SM_SEND_ALL_PETS}
	 * </pre>
	 * 
	 * @param session
	 * @param role
	 */
	public static void sendAllPets(KGamePlayerSession session, KRole role) {
		Pet fightingPet = KSupportFactory.getPetModuleSupport().getFightingPet(role.getId());
		List<KPet> allPets = KSupportFactory.getPetModuleSupport().getAllPets(role.getId());
		String composeIncDescr = KSupportFactory.getGangSupport().getGangEffectDescr(KGangTecTypeEnum.随从合成经验加成, role.getId());
		KGameMessage msg = KGame.newLogicMessage(SM_SEND_ALL_PETS);
		msg.writeByte(allPets.size());
		if (allPets.size() > 0) {
			for (int i = 0; i < allPets.size(); i++) {
				KPet pet = allPets.get(i);
				packSinglePetData(pet, msg);
			}
		}
		if(fightingPet != null) {
			msg.writeLong(fightingPet.getId());
		} else {
			msg.writeLong(0);
		}
		msg.writeUtf8String(composeIncDescr);
		session.send(msg);
	}
	
	/**
	 * 
	 * <pre>
	 * 服务器通知客户端添加宠物
	 * 消息结构：{@link com.kola.kmp.protocol.pet.KPetProtocol#SM_ADD_PET}
	 * </pre>
	 * 
	 * @param roleId
	 * @param petId
	 * @param templateId
	 */
	public static void sendAddPetToClient(long roleId, KPet pet) {
		KGameMessage msg = KGame.newLogicMessage(SM_ADD_PET);
//		msg.writeLong(pet.getId());
//		msg.writeInt(pet.getTemplateId());
		packSinglePetData(pet, msg);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
	
	/**
	 * 
	 * <pre>
	 * 服务器通知客户端添加宠物
	 * 消息结构：{@link com.kola.kmp.protocol.pet.KPetProtocol#SM_ADD_MULTIPLE_PET}
	 * </pre>
	 * 
	 * @param roleId
	 * @param petId
	 * @param templateId
	 */
	public static void sendAddMultiplePetsToClient(long roleId, List<KPet> pets) {
		KPet pet;
		KGameMessage msg = KGame.newLogicMessage(SM_ADD_MULTIPLE_PET);
		msg.writeByte(pets.size());
		for (int i = 0; i < pets.size(); i++) {
			pet = pets.get(i);
			packSinglePetData(pet, msg);
		}
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
	
	/**
	 * 
	 * <pre>
	 * 服务器通知客户端删除宠物
	 * 消息结构：{@link com.kola.kmp.protocol.pet.KPetProtocol#SM_DELETE_PET}
	 * </pre>
	 * 
	 * @param roleId
	 * @param petId
	 * @param petTemplateId
	 */
	public static void sendDeletePetToClient(long roleId, long petId, int petTemplateId) {
		KGameMessage msg = KGame.newLogicMessage(SM_DELETE_PET);
		msg.writeLong(petId);
//		msg.writeInt(petTemplateId);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
	
	/**
	 * 
	 * <pre>
	 * 服务器通知宠物升级
	 * 消息结构：{@link com.kola.kmp.protocol.pet.KPetProtocol#SM_NOTIFY_PET_LV_UP}
	 * </pre>
	 * 
	 * @param roleId
	 * @param pet
	 */
	public static void sendPetLevelUpMsg(long roleId, KPet pet) {
		KGameMessage msg = KGame.newLogicMessage(SM_NOTIFY_PET_LV_UP);
		msg.writeLong(pet.getId());
		msg.writeShort(pet.getLevel());
		msg.writeInt(pet.getCurrentExp());
		msg.writeInt(pet.getUpgradeExp());
		msg.writeInt(pet.getBeComposedExp());
		msg.writeInt(pet.getSwallowFee());
		msg.writeUtf8String("");
		msg.writeByte(KGameAttrType.DISPLAY_FOR_PET.length);
		for (int i = 0; i < KGameAttrType.DISPLAY_FOR_PET.length; i++) {
			KGameAttrType type = KGameAttrType.DISPLAY_FOR_PET[i];
			msg.writeShort(type.sign);
			msg.writeInt(pet.getAttributeByType(type));
		}
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
	
	/**
	 * 
	 * <pre>
	 * 服务器同步宠物经验值到客户端
	 * 消息结构：{@link com.kola.kmp.protocol.pet.KPetProtocol#SM_SYNCHRONIZE_PET_EXP}
	 * </pre>
	 * 
	 * @param roleId
	 * @param pet
	 */
	public static void sendSyncExpToClient(long roleId, KPet pet) {
		KGameMessage msg = KGame.newLogicMessage(SM_SYNCHRONIZE_PET_EXP);
		msg.writeLong(pet.getId());
		msg.writeInt(pet.getCurrentExp());
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
	
	/**
	 * <pre>
	 * 服务器同步宠物星级到客户端
	 * 消息结构：{@link com.kola.kmp.protocol.pet.KPetProtocol#SM_SYNC_PET_STAR_LV}
	 * </pre>
	 * @param roleId
	 * @param pet
	 */
	public static void sendSyncStarLvToClient(long roleId, KPet pet) {
		KGameMessage msg = KGame.newLogicMessage(KPetProtocol.SM_SYNC_PET_STAR_LV);
		Map<KGameAttrType, Integer> attrMap = pet.getAttrOfStar();
		msg.writeLong(pet.getId());
		msg.writeByte(pet.getStarLv());
		if (attrMap.size() > 0) {
			Map.Entry<KGameAttrType, Integer> entry;
			msg.writeByte(attrMap.size());
			for (Iterator<Map.Entry<KGameAttrType, Integer>> itr = attrMap.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				msg.writeShort(entry.getKey().sign);
				msg.writeInt(entry.getValue());
			}
		} else {
			msg.writeByte(0);
		}
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
	
	public static void sendDeletePets(KRole role, List<Long> petIds) {
		KGameMessage msg = KGame.newLogicMessage(KPetProtocol.SM_SEND_DELETE_PETS);
		msg.writeByte(petIds.size());
		for(int i = 0; i < petIds.size(); i++) {
			msg.writeLong(petIds.get(i));
		}
		role.sendMsg(msg);
	}
}
