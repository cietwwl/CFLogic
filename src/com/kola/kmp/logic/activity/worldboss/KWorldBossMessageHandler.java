package com.kola.kmp.logic.activity.worldboss;

import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.activity.KActivity;
import com.kola.kmp.logic.activity.KActivityManager;
import com.kola.kmp.logic.activity.KActivityRoleExtCaCreator;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.activity.KActivityProtocol;


/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossMessageHandler {

	public static void sendWaitingAliveMsg(KWorldBossMember member) {
		// 发送等待复活消息
//		int waitingTime = (int)TimeUnit.SECONDS.convert(member.getWaitingAliveEndTime(), TimeUnit.MILLISECONDS);
		KGameMessage msg = KGame.newLogicMessage(KActivityProtocol.SM_SEND_WAITING_ALIVE_TIME);
		msg.writeInt(member.getWaitingAliveSecond());
		msg.writeShort(KWorldBossConfig.getRelivePrice());
		KSupportFactory.getRoleModuleSupport().sendMsg(member.getRoleId(), msg);
	}
	
	public static void sendInspireData(KWorldBossMember member) {
		KGameMessage msg = KGame.newLogicMessage(KActivityProtocol.SM_UPDATE_INSPIRE_DATA);
		packInspireData(msg, member);
		KSupportFactory.getRoleModuleSupport().sendMsg(member.getRoleId(), msg);
	}
	
	public static void syncAutoJoinFlagToClient(KRole role) {
		KWorldBossRoleData roleData = KActivityRoleExtCaCreator.getWorldBossRoleData(role.getId());
		if (roleData != null) {
			KGameMessage msg = KGame.newLogicMessage(KActivityProtocol.SM_SYNC_AUTO_JOIN_FLAG);
			msg.writeBoolean(roleData.isAutoJoin());
			role.sendMsg(msg);
		}
	}
	
	static void sendStartDialogToAllOnlineRoles() {
		KActivity activity = KActivityManager.getInstance().getActivity(KWorldBossActivityMonitor.getWorldBossActivityId());
		int openLv = activity.getOpenRoleLv();
//		List<KDialogButton> buttons = new ArrayList<KDialogButton>();
//		buttons.add(KDialogButton.CANCEL_BUTTON);
//		buttons.add(new KDialogButton(KActivityModuleDialogProcesser.FUN_CONFIRM_JOIN_WORLD_BOSS, "", KDialogButton.CONFIRM_DISPLAY_TEXT));
//		KGameMessage msg = KShowDialogMsg.createFunMsg("", WorldBossTips.getTipsWorldBossStartPromptUp(), false, (byte) -1, buttons);
		KGameMessage msg = KGame.newLogicMessage(KActivityProtocol.SM_NOTIFY_WORLDBOSS_START);
		msg.writeBoolean(true);
		KRole tempRole;
		List<Long> allOnlineRoleIds = KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds();
		boolean send = false;
		for (int i = 0; i < allOnlineRoleIds.size(); i++) {
			tempRole = KSupportFactory.getRoleModuleSupport().getRole(allOnlineRoleIds.get(i));
			if (tempRole == null || tempRole.getLevel() < openLv) {
				continue;
			}
			if (tempRole.isOnline() && KSupportFactory.getMissionSupport().checkFunctionIsOpen(tempRole, KFunctionTypeEnum.丧尸攻城)
					&& !KWorldBossManager.getWorldBossActivity().isInActivity(tempRole.getId())) {
				if (send) {
					msg = msg.copy();
				}
				send = tempRole.sendMsg(msg);
			}
		}
	}
	
	static void sendCombatResult(KWorldBossMember member, long dm) {
		KGameMessage msg = KGame.newLogicMessage(KActivityProtocol.SM_WORLD_BOSS_COMBAT_RESULT);
//		msg.writeInt(dm);
//		msg.writeInt(member.getTotalDm());
		msg.writeLong(dm); // 2014-09-15 从int改为long
		msg.writeLong(member.getTotalDm()); // 2014-09-15 从int改为long
		KSupportFactory.getRoleModuleSupport().sendMsg(member.getRoleId(), msg);
	}
	
	static void packInspireData(KGameMessage msg, KWorldBossMember member) {
		int nextPrice;
		byte nextPriceType;
		if (member.isInspireMax()) {
			nextPriceType = KCurrencyTypeEnum.DIAMOND.sign;
			nextPrice = 0;
		} else {
			KInspireTemplateData data = KWorldBossManager.getInspireData(member.getInspireLv() + 1);
			nextPrice = (int) data.consume.currencyCount;
			nextPriceType = data.consume.getCurrencyType();
		}
		msg.writeByte(member.getInspireLv());
		msg.writeByte(nextPriceType);
		msg.writeInt(nextPrice);
		msg.writeUtf8String(member.getAtkIncDescr());
//		msg.writeUtf8String(member.getDefIncDescr());
		msg.writeUtf8String(""); // 2014-12-05 去掉物理防御加成
		msg.writeBoolean(member.isInspireMax());
	}
}
