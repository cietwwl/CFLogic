package com.kola.kmp.logic.relationship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KRelationShipTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RelationShipModuleSupport;
import com.kola.kmp.logic.util.ResultStructs.RSResult_AddFriend;
import com.kola.kmp.logic.util.tips.RelationShipTips;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-21 下午4:59:04
 * </pre>
 */
public class KRelationShipSupportImpl implements RelationShipModuleSupport {

	@Override
	public void notifyPMChat(long roleId, long oppRoleId) {
		KRelationShipLogic.notifyPMChat(roleId, oppRoleId);
	}

	public void notifyCloseAction_WAR(long roleId, long oppRoleId, boolean isWin) {
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(roleId);
		if (set == null) {
			return;
		}
		set.rwLock.lock();
		try {
			KRelationShip ship = set.getRelationShip(KRelationShipTypeEnum.好友.sign, oppRoleId);
			if (ship == null) {
				return;
			}
			if (isWin) {
				ship.addCloseness(KRelationShipConfig.getInstance().ClosenessForWarWin);
			} else {
				ship.addCloseness(KRelationShipConfig.getInstance().ClosenessForWarLose);
			}

		} finally {
			set.rwLock.unlock();
		}
	}

	public void notifyCloseAction_PMChat(long roleId, long oppRoleId) {
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(roleId);
		if (set == null) {
			return;
		}
		set.rwLock.lock();
		try {
			KRelationShip ship = set.getRelationShip(KRelationShipTypeEnum.好友.sign, oppRoleId);
			if (ship == null) {
				return;
			}
			if (!ship.isDonePMChatToday()) {
				ship.addCloseness(KRelationShipConfig.getInstance().ClosenessForPMChat);
			}
		} finally {
			set.rwLock.unlock();
		}
	}

	public boolean isInBlackList(long roleId, long oppRoleId) {
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(roleId);
		if (set != null && set.getRelationShip(KRelationShipTypeEnum.黑名单.sign, oppRoleId) != null) {
			return true;
		}
		return false;
	}

	@Override
	public int getCloseness(long roleId, long oppRoleId) {
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(roleId);
		if (set != null) {
			KRelationShip ship = set.getRelationShip(KRelationShipTypeEnum.好友.sign, oppRoleId);
			if (ship != null) {
				return ship.getCloseness();
			}
		}
		return 0;
	}

	@Override
	public boolean isInFriendList(long roleId, long oppRoleId) {
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(roleId);
		if (set != null && set.getRelationShip(KRelationShipTypeEnum.好友.sign, oppRoleId) != null) {
			return true;
		}
		return false;
	}

	public List<Long> getAllFriends(long roleId) {
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(roleId);
		if (set == null) {
			return Collections.emptyList();
		}
		set.rwLock.lock();
		try {
			return new ArrayList<Long>(set.getRelationShipsCache(KRelationShipTypeEnum.好友.sign).keySet());
		} finally {
			set.rwLock.unlock();
		}
	}

	public boolean isRelationShipFull(long roleId, KRelationShipTypeEnum type) {
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(roleId);
		if (set == null) {
			return true;
		}
		return set.isRelationShipFull(KRelationShipTypeEnum.好友);
	}
	
	public void autoAppFriend(KRole role, KRole oppRole){
		RSResult_AddFriend result = KRelationShipLogic.dealMsg_appFriend(role, oppRole);
		if(result.isSucess){
			KDialogService.sendUprisingDialog(role, StringUtil.format(RelationShipTips.系统自动向x发送好友请求, oppRole.getName()));
		}
	}
}
