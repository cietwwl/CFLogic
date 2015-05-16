package com.kola.kmp.logic.role.message;

import static com.kola.kmp.protocol.role.KRoleProtocol.CM_SYNC_GET_OTHER_ROLE_INFO;
import static com.kola.kmp.protocol.role.KRoleProtocol.SM_SYNC_GET_OTHER_ROLE_INFO;

import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.pet.KPet;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.RoleTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KGetOtherRoleInfoMsgProcesser implements GameMessageProcesser {

	private static final byte CONDITION_TYPE_ROLE_NAME = 1;
	private static final byte CONDITION_TYPE_ROLE_ID = 2;
	
	@Override
	public GameMessageProcesser newInstance() {
		return new KGetOtherRoleInfoMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_SYNC_GET_OTHER_ROLE_INFO;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KRole role = null;
		byte condition = msgEvent.getMessage().readByte();
		switch (condition) {
		case CONDITION_TYPE_ROLE_ID:
			long roleId = msgEvent.getMessage().readLong();
			role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			break;
		case CONDITION_TYPE_ROLE_NAME:
			String roleName = msgEvent.getMessage().readUtf8String();
			role = KSupportFactory.getRoleModuleSupport().getRole(roleName);
			break;
		}
		if (role != null) {
			List<KItem> equipmentList = KSupportFactory.getItemModuleSupport().getRoleEquipmentList(role.getId());
			KGameMessage msg = KGame.newLogicMessage(SM_SYNC_GET_OTHER_ROLE_INFO);
			KRoleServerMsgPusher.packRoleAttributeToMsg(role, msg); // 基础属性
			boolean hasMount = KSupportFactory.getMountModuleSupport().getMountCanWarOfRole(role.getId()) != null;
			msg.writeUtf8String(KSupportFactory.getGangSupport().getGangNameByRoleId(role.getId())); // 公会名字
			msg.writeUtf8String(""); // 称号
			msg.writeUtf8String(KSupportFactory.getFashionModuleSupport().getFashingResId(role.getId()));
			msg.writeByte(equipmentList.size()); // 装备的数量
			for (int i = 0; i < equipmentList.size(); i++) {
				KSupportFactory.getItemModuleSupport().packItemDataToMsg(equipmentList.get(i), role.getLevel(), msg);
			}
			KSupportFactory.getMountModuleSupport().packMountDataToMsgForOtherRole(msg, role);
			KSupportFactory.getItemModuleSupport().packEquiSetDataToMsg(role, msg);
			KPet pet = KSupportFactory.getPetModuleSupport().getFightingPet(role.getId());
			if (pet != null) {
				msg.writeBoolean(true);
				KSupportFactory.getPetModuleSupport().packPetDataToMsg(msg, pet);
			} else {
				msg.writeBoolean(false);
			}
			msgEvent.getPlayerSession().send(msg);
			KDialogService.sendNullDialog(msgEvent.getPlayerSession());
		} else {
			KDialogService.sendUprisingDialog(msgEvent.getPlayerSession(), RoleTips.getTipsNoSuchRole());
		}
	}

}
