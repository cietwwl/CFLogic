package com.kola.kmp.logic.relationship.message;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.other.KRelationShipTypeEnum;
import com.kola.kmp.logic.relationship.KRelationShip;
import com.kola.kmp.logic.relationship.KRelationShipModuleExtension;
import com.kola.kmp.logic.relationship.KRelationShipSet;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.ResultStructs.RSResult_AddFriend.RSSynStruct;
import com.kola.kmp.protocol.relationship.KRelationShipProtocol;

public class KPushRSsMsg implements KRelationShipProtocol {

	/**
	 * <pre>
	 * 推送角色全部关系
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-3-13 上午11:11:21
	 * </pre>
	 */
	public static void pushAllRelationShips(KRole role) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_ALL_RSS);
		//
		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
		//
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(role.getId());
		set.rwLock.lock();
		try {
			Map<Integer, LinkedHashMap<Long, KRelationShip>> map = set.getRelationShipsCache();
			msg.writeByte(map.size());
			for (Entry<Integer, LinkedHashMap<Long, KRelationShip>> entry : map.entrySet()) {
				msg.writeByte(entry.getKey());
				//
				Map<Long, KRelationShip> tempMap = entry.getValue();
				boolean isFriend = entry.getKey() == KRelationShipTypeEnum.好友.sign;
//				int type = entry.getKey();
				//
				int writeIndex = msg.writerIndex();
				msg.writeShort(tempMap.size());
				int count = 0;
				for (KRelationShip ship : tempMap.values()) {
					KRole oppRole = roleSupport.getRole(ship._guestRoleId);
					if (oppRole != null) {
						int closeness = 0;
						int pvpScore = 0;
						if (isFriend) {
							closeness = ship.getCloseness();
							KRelationShipSet oppSet = KRelationShipModuleExtension.getRelationShipSet(oppRole.getId());
							if(oppSet!=null){
								pvpScore = oppSet.getPVPScore();
							}
						}
						packRelationShip(msg, oppRole, closeness, pvpScore);
						count++;
					}
				}
				msg.setShort(writeIndex, count);
			}
			
			// 我的切磋积分
			msg.writeInt(set.getPVPScore());
		} finally {
			set.rwLock.unlock();
		}
		//
		role.sendMsg(msg);
	}

	static void packRelationShip(KGameMessage msg, KRole role) {
		packRelationShip(msg, role, 0, 0);
	}

	/**
	 * <pre>
	 * 打包一个关系数据
	 * 
	 * @param msg
	 * @param ship
	 * @author CamusHuang
	 * @creation 2014-3-13 上午11:18:28
	 * </pre>
	 */
	private static void packRelationShip(KGameMessage msg, KRole role, int closeness, int pvpScore) {
		// * long 角色ID
		// * String　角色名
		// * byte 职业
		// * short 角色等级
		// * int　战力
		// * short VIP等级
		// * int 亲密度
		msg.writeLong(role.getId());
		msg.writeUtf8String(role.getName());
		msg.writeByte(role.getJob());
		msg.writeShort(role.getLevel());
		msg.writeInt(role.getBattlePower());
		msg.writeShort(KSupportFactory.getVIPModuleSupport().getVipLv(role.getId()));
		msg.writeInt(closeness);
		msg.writeInt(pvpScore);
	}

	public static void synRelationShips(Map<Long, RSSynStruct> rsSynDatas) {
		if (rsSynDatas == null || rsSynDatas.isEmpty()) {
			return;
		}

		for (RSSynStruct data : rsSynDatas.values()) {
			synRelationShips(data);
		}
	}

	/**
	 * <pre>
	 * 关系同步
	 * 
	 * @param role
	 * @param addOrUpdates
	 * @param deletes
	 * @author CamusHuang
	 * @creation 2014-3-13 上午11:11:50
	 * </pre>
	 */
	public static void synRelationShips(RSSynStruct rsSynStruct) {
		if (rsSynStruct == null) {
			return;
		}
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(rsSynStruct.roleId);
		if (!role.isOnline()) {
			return;
		}
		KGameMessage msg = KGame.newLogicMessage(SM_SYN_RSS);
		packDeleteRoleIds(msg, rsSynStruct.deletes);
		packAddOrUpdateRSs(msg, role.getId(), rsSynStruct.addOrUpdates);
		role.sendMsg(msg);
	}

	private static void packAddOrUpdateRSs(KGameMessage msg, long roleId, Map<KRelationShipTypeEnum, List<Long>> addOrUpdateRoles) {
		if (addOrUpdateRoles == null) {
			msg.writeByte(0);
			return;
		}

		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(roleId);
		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
//		好友(1), //
//		好友申请(2), //
//		最近联系人(3), //
//		黑名单(4), //
//		附近的人(5), //
		msg.writeByte(addOrUpdateRoles.size());
		for (Entry<KRelationShipTypeEnum, List<Long>> entry : addOrUpdateRoles.entrySet()) {
			msg.writeByte(entry.getKey().sign);
			//
			List<Long> temp = entry.getValue();
			boolean isFriend = entry.getKey() == KRelationShipTypeEnum.好友;
			int type = entry.getKey().sign;
			//
			int writeIndex = msg.writerIndex();
			msg.writeShort(temp.size());
			int count = 0;
			for (Long oppRoleId : temp) {
				KRole oppRole = roleSupport.getRole(oppRoleId);
				if (oppRole != null) {
					int closeness = 0;
					int pvpScore = 0;
					if (isFriend) {
						KRelationShip ship = set.getRelationShip(type, oppRoleId);
						if (ship != null) {
							closeness = ship.getCloseness();
						}
						KRelationShipSet oppSet = KRelationShipModuleExtension.getRelationShipSet(oppRole.getId());
						if(oppSet!=null){
							pvpScore = oppSet.getPVPScore();
						}
					}
					packRelationShip(msg, oppRole, closeness, pvpScore);
					count++;
				}
			}
			msg.setShort(writeIndex, count);
		}
	}

	private static void packDeleteRoleIds(KGameMessage msg, Map<KRelationShipTypeEnum, List<Long>> deleteRoles) {
		if (deleteRoles == null) {
			msg.writeByte(0);
			return;
		}
		msg.writeByte(deleteRoles.size());
		for (Entry<KRelationShipTypeEnum, List<Long>> entry : deleteRoles.entrySet()) {
			msg.writeByte(entry.getKey().sign);
			List<Long> temp = entry.getValue();
			msg.writeShort(temp.size());
			for (Long roleId : temp) {
				msg.writeLong(roleId);
			}
		}
	}

	public static void pushRecommendFriends(KRole role, List<KRole> result) {
		if (result.size() > 0) {
			KGameMessage msg = KGame.newLogicMessage(SM_PUSH_RECOMMENT_FRIENDS);
			msg.writeByte(result.size());
			for (KRole tempRole : result) {
				packRelationShip(msg, tempRole, 0, 0);
			}
			role.sendMsg(msg);
		}
	}
}
