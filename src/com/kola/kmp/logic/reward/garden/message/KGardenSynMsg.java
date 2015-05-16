package com.kola.kmp.logic.reward.garden.message;

import java.util.Collection;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.reward.garden.KGardenCenter;
import com.kola.kmp.logic.reward.garden.KGardenRoleExtCACreator;
import com.kola.kmp.logic.reward.garden.KRoleGarden;
import com.kola.kmp.logic.reward.garden.KRoleGarden.TreeData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.logic.vip.KRoleVIP;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;
import com.kola.kmp.protocol.reward.KRewardProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KGardenSynMsg implements KRewardProtocol {

	public static void sendNewFeets(KRole role) {
		KRoleGarden garden = KGardenRoleExtCACreator.getRoleGarden(role.getId());
		List<String> logs = garden.getAndClearNewFeetLogs();
		if (logs.isEmpty()) {
			return;
		}

		KGameMessage msg = KGame.newLogicMessage(SM_GARDEN_SYN_FEET);
		msg.writeByte(logs.size());
		for (String log : logs) {
			msg.writeUtf8String(log);
		}
		role.sendMsg(msg);
	}

	public static void sendTreeDatas(KRole role, long treeRoleId, List<TreeData> trees) {
		KGameMessage msg = KGame.newLogicMessage(SM_GARDEN_SYN_TREE);
		msg.writeLong(treeRoleId);
		msg.writeByte(trees.size());
		for (TreeData tree : trees) {
			KGardenCenter.packTree(msg, tree);
		}
		role.sendMsg(msg);
	}

	public static void sendTreeDatas(KRole role, long treeRoleId) {
		KGameMessage msg = KGame.newLogicMessage(SM_GARDEN_SYN_TREE);
		msg.writeLong(treeRoleId);

		KRoleGarden garden = KGardenRoleExtCACreator.getRoleGarden(treeRoleId);
		garden.rwLock.lock();
		try {
			Collection<TreeData> coll = garden.getTreeDataCache();
			msg.writeByte(coll.size());
			for (TreeData tree : coll) {
				KGardenCenter.packTree(msg, tree);
			}
		} finally {
			garden.rwLock.unlock();
		}
		role.sendMsg(msg);
	}

	public static void sendTreeData(KRole role, long treeRoleId, TreeData tree) {
		KGameMessage msg = KGame.newLogicMessage(SM_GARDEN_SYN_TREE);
		msg.writeLong(treeRoleId);
		msg.writeByte(1);
		KGardenCenter.packTree(msg, tree);
		role.sendMsg(msg);
	}

	public static void sendMyGardenData(KRole role) {
		if (role == null) {
			return;
		}
		KGameMessage msg = KGame.newLogicMessage(SM_GARDEN_PUSH_DATA);
		KRoleGarden garden = KGardenRoleExtCACreator.getRoleGarden(role.getId());
		garden.rwLock.lock();
		try {
			msg.writeInt(garden.getReleaseSpeedTime());
			KGardenCenter.packGardenData(msg, role, garden, garden.getSpeedCDReleaseTime(role.getId()));
		} finally {
			garden.rwLock.unlock();
		}
		role.sendMsg(msg);
	}

	public static void sendGardenConstance(KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_GARDEN_PUSH_CONSTANCE);
		KGardenCenter.packGardenConstance(role, msg);
		role.sendMsg(msg);
	}

	public static void sendVipSaveLogo(KRoleVIP vip) {
		sendVipSaveLogo(vip.getRoleId(), null, null);
	}

	public static void sendVipSaveLogo(KRole role, KRoleGarden garden) {
		sendVipSaveLogo(role.getId(), garden, null);
	}

	private static void sendVipSaveLogo(long roleId, KRoleGarden garden, VIPLevelData vip) {

		if (garden == null) {
			garden = KGardenRoleExtCACreator.getRoleGarden(roleId);
		}

		if (vip == null) {
			vip = KSupportFactory.getVIPModuleSupport().getVIPLevelData(roleId);
		}

		KGameMessage msg = KGame.newLogicMessage(SM_GARDEN_SYN_VIPLOGO);
		boolean isSave = garden.getVIPSaveDataManager().isContainVipSaveData();
		msg.writeBoolean(isSave || vip.gardensavetimeInMills > 0);
		msg.writeBoolean(isSave);
		msg.writeInt(garden.getReleaseSpeedTime());
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
}
