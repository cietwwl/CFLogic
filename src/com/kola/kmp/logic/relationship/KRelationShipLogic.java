package com.kola.kmp.logic.relationship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KRelationShipTypeEnum;
import com.kola.kmp.logic.relationship.KRelationShipDataStructs.RSPushData;
import com.kola.kmp.logic.relationship.message.KPushRSsMsg;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.ResultStructs.RSResult_AddFriend;
import com.kola.kmp.logic.util.tips.RelationShipTips;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;

/**
 * <pre>
 * // 人数限制;
 * 	// 最近联系人;
 * 	// 黑名单过滤功能;
 * 
 * @author CamusHuang
 * @creation 2014-3-12 下午5:07:42
 * </pre>
 */
public class KRelationShipLogic {
	/**
	 * <pre>
	 * 清理此角色所有失效的关系（对方被删除）
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-3-13 上午11:24:19
	 * </pre>
	 */
	public static void clearAllLoseRelationShips(KRole role) {
		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
		//
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(role.getId());
		set.rwLock.lock();
		try {
			Map<Integer, LinkedHashMap<Long, KRelationShip>> map = set.getRelationShipsCache();
			for (Entry<Integer, LinkedHashMap<Long, KRelationShip>> entry : map.entrySet()) {
				List<Long> roleList = new ArrayList<Long>(entry.getValue().keySet());
				for (Long roleId : roleList) {
					if (roleSupport.getRole(roleId) == null) {
						set.notifyElementDelete(entry.getKey(), roleId);
					}
				}
			}
		} finally {
			set.rwLock.unlock();
		}
	}

	public static void notifyPMChat(long roleId, long oppRoleId) {
		RSResult_AddFriend result = new RSResult_AddFriend();
		//
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(roleId);
		KRelationShipSet oppset = KRelationShipModuleExtension.getRelationShipSet(oppRoleId);
		//
		lockSets(set, oppset);
		try {
			addPMRS(set, roleId, oppRoleId, result);
			addPMRS(oppset, oppRoleId, roleId, result);
		} finally {
			unlockSets(set, oppset);
			//
			KPushRSsMsg.synRelationShips(result.rsSynDatas);
		}
	}

	/**
	 * <pre>
	 * 好友数量上限升VIP控制
	 * 此处返回角色当前VIP对应的好友上限
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-18 下午6:33:49
	 * </pre>
	 */
	public static int getMaxFriendCount(long roleId) {
		VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(roleId);
		return vipData.friendmaxcount;
	}

	private static void addPMRS(KRelationShipSet set, long roleId, long oppRoleId, RSResult_AddFriend result) {
		LinkedHashMap<Long, KRelationShip> map = set.getRelationShipsCache(KRelationShipTypeEnum.最近联系人.sign);
		KRelationShip ship = map.get(oppRoleId);
		if (ship == null) {
			ship = new KRelationShip(set, KRelationShipTypeEnum.最近联系人.sign, oppRoleId);
			set.addRelationShip(ship);
			result.addUpdateRoleId(roleId, KRelationShipTypeEnum.最近联系人, oppRoleId);
		}

		if (map.size() > KRelationShipTypeEnum.最近联系人.getMaxNum()) {
			// 删除最旧的
			Long removeID = map.keySet().iterator().next();
			set.notifyElementDelete(KRelationShipTypeEnum.最近联系人.sign, removeID);
			result.addDeleteRoleId(roleId, KRelationShipTypeEnum.最近联系人, removeID);
		}
	}

	/**
	 * <pre>
	 * 取消我方黑名单，已是我方好友则失败，我方满员则失败，若对方拉黑则失败，若已申请过则失败，否则加入对方申请表
	 * 
	 * @param role
	 * @param oppRole
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-13 上午10:10:20
	 * </pre>
	 */
	public static RSResult_AddFriend dealMsg_appFriend(KRole role, KRole oppRole) {
		RSResult_AddFriend result = appFriendIn(role, oppRole, null);
		return result;
	}

	/**
	 * <pre>
	 * 取消我方黑名单，已是我方好友则失败，我方满员则失败，若对方拉黑则失败，若已申请过则失败，否则加入对方申请表
	 * 
	 * @param role
	 * @param oppRole
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-13 上午10:10:20
	 * </pre>
	 */
	public static RSResult_AddFriend dealMsg_appFriends(KRole role, Set<KRole> oppRoles) {
		RSResult_AddFriend result = new RSResult_AddFriend();

		int success = 0;
		for (KRole oppRole : oppRoles) {
			result = appFriendIn(role, oppRole, result);
			if (result.isSucess) {
				success++;
			}
		}

		if (success > 0) {
			result.isSucess = true;
			result.tips = StringUtil.format(RelationShipTips.已向x名角色发出好友请求, success);
		} else {
			result.isSucess = false;
			result.tips = RelationShipTips.没有符合条件的角色;
		}
		return result;
	}

	private static RSResult_AddFriend appFriendIn(KRole role, KRole oppRole, RSResult_AddFriend result) {
		if (result == null) {
			result = new RSResult_AddFriend();
		}
		result.isSucess = false;

		if (role.getId() == oppRole.getId()) {
			result.tips = RelationShipTips.不能对自己进行操作;
			return result;
		}
		//
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(role.getId());
		KRelationShipSet oppset = KRelationShipModuleExtension.getRelationShipSet(oppRole.getId());
		//
		lockSets(set, oppset);
		try {
			{
				// 取消拉黑对方
				KRelationShip ship = set.notifyElementDelete(KRelationShipTypeEnum.黑名单.sign, oppRole.getId());
				if (ship != null) {
					// 同步
					result.addDeleteRoleId(role.getId(), KRelationShipTypeEnum.黑名单, oppRole.getId());
				}

				ship = set.getRelationShip(KRelationShipTypeEnum.好友.sign, oppRole.getId());
				if (ship != null) {
					result.tips = RelationShipTips.对方已是你的好友;
					return result;
				}

				if (set.isRelationShipFull(KRelationShipTypeEnum.好友)) {
					result.tips = StringUtil.format(RelationShipTips.你的x关系数量已满员, KRelationShipTypeEnum.好友.name);
					return result;
				}
			}
			//
			{
				KRelationShip ship = oppset.getRelationShip(KRelationShipTypeEnum.黑名单.sign, role.getId());
				if (ship != null) {
					result.tips = RelationShipTips.你已经在对方黑名单中无法加对方好友;
					return result;
				}

				ship = oppset.getRelationShip(KRelationShipTypeEnum.好友申请.sign, role.getId());
				if (ship != null) {
					result.tips = RelationShipTips.你已在对方邀请好友列表中;
					return result;
				}

				ship = new KRelationShip(oppset, KRelationShipTypeEnum.好友申请.sign, role.getId());
				oppset.addRelationShip(ship);
				result.addUpdateRoleId(oppRole.getId(), KRelationShipTypeEnum.好友申请, role.getId());
			}

			result.isSucess = true;
			result.tips = RelationShipTips.系统已将你的请求通知对方;
			return result;
		} finally {
			unlockSets(set, oppset);
		}
	}

	/**
	 * <pre>
	 * 已在我方黑名单则失败，否则清除双方好友，加入我方黑名单
	 * 
	 * @param role
	 * @param oppRole
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-13 上午10:14:37
	 * </pre>
	 */
	public static RSResult_AddFriend dealMsg_addBlackList(KRole role, KRole oppRole) {
		RSResult_AddFriend result = new RSResult_AddFriend();

		if (role.getId() == oppRole.getId()) {
			result.tips = RelationShipTips.不能对自己进行操作;
			return result;
		}
		//
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(role.getId());
		KRelationShipSet oppset = KRelationShipModuleExtension.getRelationShipSet(oppRole.getId());
		//
		lockSets(set, oppset);
		try {
			{
				KRelationShip ship = set.getRelationShip(KRelationShipTypeEnum.黑名单.sign, oppRole.getId());
				if (ship != null) {
					result.tips = RelationShipTips.对方已在你的黑名单中;
					return result;
				}

				if (set.countRelationShipSize(KRelationShipTypeEnum.黑名单.sign) >= KRelationShipTypeEnum.黑名单.getMaxNum()) {
					result.tips = StringUtil.format(RelationShipTips.你的x关系数量已满员, KRelationShipTypeEnum.黑名单.name);
					return result;
				}

				ship = set.getRelationShip(KRelationShipTypeEnum.好友.sign, oppRole.getId());
				if (ship == null) {
					// 不是好友，直接加入黑名单
					ship = new KRelationShip(set, KRelationShipTypeEnum.黑名单.sign, oppRole.getId());
					set.addRelationShip(ship);
				} else {
					// 是好友，修改类型为黑名单
					ship.changeType(KRelationShipTypeEnum.黑名单.sign);
					set.notifyRelationShipChange(ship, KRelationShipTypeEnum.好友.sign);
					result.addDeleteRoleId(role.getId(), KRelationShipTypeEnum.好友, oppRole.getId());
				}
				result.addUpdateRoleId(role.getId(), KRelationShipTypeEnum.黑名单, oppRole.getId());
			}
			//
			{
				KRelationShip ship = oppset.notifyElementDelete(KRelationShipTypeEnum.好友.sign, role.getId());
				if (ship != null) {
					result.addDeleteRoleId(oppRole.getId(), KRelationShipTypeEnum.好友, role.getId());
				}
			}

			result.isSucess = true;
			result.tips = RelationShipTips.拉黑成功;
			return result;
		} finally {
			unlockSets(set, oppset);
		}
	}

	/**
	 * <pre>
	 * 若对方未申请则失败，若对方拉黑则失败,否则删除我方黑名单,双向加好友
	 * 
	 * @param role
	 * @param oppRoleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-13 上午10:21:14
	 * </pre>
	 */
	public static RSResult_AddFriend dealMsg_agreeForFriend(KRole role, KRole oppRole) {
		RSResult_AddFriend result = new RSResult_AddFriend();

		if (role.getId() == oppRole.getId()) {
			result.tips = RelationShipTips.不能对自己进行操作;
			return result;
		}
		//
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(role.getId());
		KRelationShipSet oppset = KRelationShipModuleExtension.getRelationShipSet(oppRole.getId());
		//
		lockSets(set, oppset);
		try {
			{
				// 检查我方申请
				KRelationShip ship = set.getRelationShip(KRelationShipTypeEnum.好友申请.sign, oppRole.getId());
				if (ship == null) {
					result.tips = RelationShipTips.对方未曾请求交友;
					return result;
				}

				if (set.isRelationShipFull(KRelationShipTypeEnum.好友)) {
					result.tips = StringUtil.format(RelationShipTips.你的x关系数量已满员, KRelationShipTypeEnum.好友.name);
					return result;
				}
			}
			//
			{
				KRelationShip ship = oppset.getRelationShip(KRelationShipTypeEnum.黑名单.sign, role.getId());
				if (ship != null) {
					result.tips = RelationShipTips.你已经在对方黑名单中无法加对方好友;
					return result;
				}

				if (oppset.isRelationShipFull(KRelationShipTypeEnum.好友)) {
					result.tips = StringUtil.format(RelationShipTips.对方的x关系数量已满员, KRelationShipTypeEnum.好友.name);
					return result;
				}

				if (oppset.getRelationShip(KRelationShipTypeEnum.好友.sign, role.getId()) == null) {
					// 添加自己为对方好友
					ship = new KRelationShip(oppset, KRelationShipTypeEnum.好友.sign, role.getId());
					oppset.addRelationShip(ship);
					result.addUpdateRoleId(oppRole.getId(), KRelationShipTypeEnum.好友, role.getId());
				}
			}
			//
			{
				// 删除我方申请
				KRelationShip ship = set.notifyElementDelete(KRelationShipTypeEnum.好友申请.sign, oppRole.getId());
				if (ship != null) {
					result.addDeleteRoleId(role.getId(), KRelationShipTypeEnum.好友申请, oppRole.getId());
				}

				// 解除我方黑名单
				ship = set.getRelationShip(KRelationShipTypeEnum.黑名单.sign, oppRole.getId());
				if (ship == null) {
					// 不是黑名单，直接添加对方为自己好友
					ship = new KRelationShip(set, KRelationShipTypeEnum.好友.sign, oppRole.getId());
					set.addRelationShip(ship);
				} else {
					// 是黑名单，修改类型为好友
					ship.changeType(KRelationShipTypeEnum.好友.sign);
					set.notifyRelationShipChange(ship, KRelationShipTypeEnum.黑名单.sign);
					result.addDeleteRoleId(role.getId(), KRelationShipTypeEnum.黑名单, oppRole.getId());
				}
				result.addUpdateRoleId(role.getId(), KRelationShipTypeEnum.好友, oppRole.getId());
			}

			result.isSucess = true;
			return result;
		} finally {
			unlockSets(set, oppset);
		}
	}

	public static RSResult_AddFriend dealMsg_agreeForFriends(KRole role) {
		RSResult_AddFriend result = new RSResult_AddFriend();
		//
		List<Long> allAppRoleIds = null;// 所有的申请书
		//
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(role.getId());
		set.rwLock.lock();
		try {

			if (set.isRelationShipFull(KRelationShipTypeEnum.好友)) {
				result.tips = StringUtil.format(RelationShipTips.你的x关系数量已满员, KRelationShipTypeEnum.好友.name);
				return result;
			}

			allAppRoleIds = new ArrayList<Long>(set.getRelationShipsCache(KRelationShipTypeEnum.好友申请.sign).keySet());
		} finally {
			set.rwLock.unlock();
		}
		//
		int addCount = 0;
		int failCount = 0;
		for (Long oppRoleId : allAppRoleIds) {
			KRole oppRole = KSupportFactory.getRoleModuleSupport().getRole(oppRoleId);
			if (oppRole == null) {
				// 删除我方申请
				KRelationShip ship = set.notifyElementDelete(KRelationShipTypeEnum.好友申请.sign, oppRoleId);
				if (ship != null) {
					failCount++;
					result.addDeleteRoleId(role.getId(), KRelationShipTypeEnum.好友申请, oppRoleId);
				}
				continue;
			}
			KRelationShipSet oppset = KRelationShipModuleExtension.getRelationShipSet(oppRoleId);
			//
			lockSets(set, oppset);
			try {
				{
					// 检查我方申请
					KRelationShip ship = set.getRelationShip(KRelationShipTypeEnum.好友申请.sign, oppRoleId);
					if (ship == null) {
						continue;
					}

					if (set.isRelationShipFull(KRelationShipTypeEnum.好友)) {
						result.addUprisingTips(StringUtil.format(RelationShipTips.你的x关系数量已满员, KRelationShipTypeEnum.好友.name));
						break;
					}
				}
				//
				{
					KRelationShip oppship = oppset.getRelationShip(KRelationShipTypeEnum.黑名单.sign, role.getId());
					if (oppship != null) {
						// 删除我方申请
						KRelationShip ship = set.notifyElementDelete(KRelationShipTypeEnum.好友申请.sign, oppRoleId);
						if (ship != null) {
							failCount++;
							result.addDeleteRoleId(role.getId(), KRelationShipTypeEnum.好友申请, oppRoleId);
						}
						continue;
					}

					if (oppset.isRelationShipFull(KRelationShipTypeEnum.好友)) {
						// 删除我方申请
						KRelationShip ship = set.notifyElementDelete(KRelationShipTypeEnum.好友申请.sign, oppRoleId);
						if (ship != null) {
							failCount++;
							result.addDeleteRoleId(role.getId(), KRelationShipTypeEnum.好友申请, oppRoleId);
						}
						continue;
					}

					if (oppset.getRelationShip(KRelationShipTypeEnum.好友.sign, role.getId()) == null) {
						// 添加自己为对方好友
						oppship = new KRelationShip(oppset, KRelationShipTypeEnum.好友.sign, role.getId());
						oppset.addRelationShip(oppship);
						result.addUpdateRoleId(oppRoleId, KRelationShipTypeEnum.好友, role.getId());
					}
				}
				//
				{
					// 删除我方申请
					KRelationShip ship = set.notifyElementDelete(KRelationShipTypeEnum.好友申请.sign, oppRoleId);
					if (ship != null) {
						result.addDeleteRoleId(role.getId(), KRelationShipTypeEnum.好友申请, oppRoleId);
					}

					// 解除我方黑名单
					ship = set.getRelationShip(KRelationShipTypeEnum.黑名单.sign, oppRoleId);
					if (ship == null) {
						// 不是黑名单，直接添加对方为自己好友
						ship = new KRelationShip(set, KRelationShipTypeEnum.好友.sign, oppRoleId);
						set.addRelationShip(ship);
					} else {
						// 是黑名单，修改类型为好友
						ship.changeType(KRelationShipTypeEnum.好友.sign);
						set.notifyRelationShipChange(ship, KRelationShipTypeEnum.黑名单.sign);
						result.addDeleteRoleId(role.getId(), KRelationShipTypeEnum.黑名单, oppRoleId);
					}
					result.addUpdateRoleId(role.getId(), KRelationShipTypeEnum.好友, oppRoleId);
				}

				addCount++;
			} finally {
				unlockSets(set, oppset);
			}
		}
		
		if(failCount > 0){
			result.addUprisingTips(StringUtil.format(RelationShipTips.有x个好友申请已失效已经为您清空, failCount));
		}

		if (addCount < 1) {
			result.tips = RelationShipTips.没有可以接受的邀请;
			return result;
		}

		result.isSucess = true;
		result.tips = StringUtil.format(RelationShipTips.接受了x个好友邀请, addCount);
		return result;
	}

	/**
	 * <pre>
	 * 删除我方申请表，通知对方
	 * 默认成功
	 * 
	 * @param role
	 * @param oppRole
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-13 上午10:29:57
	 * </pre>
	 */
	public static RSResult_AddFriend dealMsg_refuseForFriend(KRole role, long oppRoleId) {
		RSResult_AddFriend result = new RSResult_AddFriend();
		//
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(role.getId());
		//
		set.rwLock.lock();
		try {
			// 默认成功
			KRelationShip ship = set.notifyElementDelete(KRelationShipTypeEnum.好友申请.sign, oppRoleId);
			if (ship != null) {
				result.addDeleteRoleId(role.getId(), KRelationShipTypeEnum.好友申请, oppRoleId);
			}

			result.isSucess = true;
			result.tips = StringUtil.format(RelationShipTips.拒绝了x个好友邀请, 1);
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 删除我方申请表，通知对方
	 * 默认成功
	 * 
	 * @param role
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-13 上午11:02:09
	 * </pre>
	 */
	public static RSResult_AddFriend dealMsg_refuseForFriends(KRole role) {
		RSResult_AddFriend result = new RSResult_AddFriend();
		//
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(role.getId());
		//
		set.rwLock.lock();
		try {
			// 所有申请书
			List<Long> allAppRoleIds = new ArrayList<Long>(set.getRelationShipsCache(KRelationShipTypeEnum.好友申请.sign).keySet());

			for (Long oppRoleId : allAppRoleIds) {
				// 默认成功
				KRelationShip ship = set.notifyElementDelete(KRelationShipTypeEnum.好友申请.sign, oppRoleId);
				if (ship != null) {
					result.addDeleteRoleId(role.getId(), KRelationShipTypeEnum.好友申请, oppRoleId);
				}
			}

			result.isSucess = true;
			result.tips = StringUtil.format(RelationShipTips.拒绝了x个好友邀请, allAppRoleIds.size());
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 若不是我方好友则失败，否则双向删除好友，邮件通知对方
	 * 
	 * @param role
	 * @param oppRoleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-13 上午10:38:59
	 * </pre>
	 */
	public static RSResult_AddFriend dealMsg_deleteFriend(KRole role, long oppRoleId) {
		RSResult_AddFriend result = new RSResult_AddFriend();
		//
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(role.getId());
		KRelationShipSet oppset = KRelationShipModuleExtension.getRelationShipSet(oppRoleId);
		if (oppset == null) {
			set.rwLock.lock();
			try {
				// 删除我方好友
				KRelationShip ship = set.notifyElementDelete(KRelationShipTypeEnum.好友.sign, oppRoleId);
				if (ship != null) {
					// 同步
					result.addDeleteRoleId(role.getId(), KRelationShipTypeEnum.好友, oppRoleId);
				}

				result.isSucess = true;
				result.tips = RelationShipTips.删除好友成功;
				return result;
			} finally {
				set.rwLock.unlock();
			}
		} else {
			lockSets(set, oppset);
			try {
				// 删除我方好友
				KRelationShip ship = set.notifyElementDelete(KRelationShipTypeEnum.好友.sign, oppRoleId);
				if (ship != null) {
					// 同步
					result.addDeleteRoleId(role.getId(), KRelationShipTypeEnum.好友, oppRoleId);
					// 是我的好友，则同时删除对方好友，通知对方
					ship = oppset.notifyElementDelete(KRelationShipTypeEnum.好友.sign, role.getId());
					if (ship != null) {
						// 同步
						result.addDeleteRoleId(oppRoleId, KRelationShipTypeEnum.好友, role.getId());
					}
				}

				result.isSucess = true;
				result.tips = RelationShipTips.删除好友成功;
				return result;
			} finally {
				unlockSets(set, oppset);
			}
		}
	}

	private static void lockSets(KRelationShipSet set1, KRelationShipSet set2) {
		KRelationShipSet setA = null;
		KRelationShipSet setB = null;
		if (set1._roleId > set2._roleId) {
			setA = set1;
			setB = set2;
		} else {
			setA = set2;
			setB = set1;
		}

		setA.rwLock.lock();
		setB.rwLock.lock();
	}

	private static void unlockSets(KRelationShipSet set1, KRelationShipSet set2) {
		KRelationShipSet setA = null;
		KRelationShipSet setB = null;
		if (set1._roleId > set2._roleId) {
			setA = set1;
			setB = set2;
		} else {
			setA = set2;
			setB = set1;
		}

		setA.rwLock.unlock();
		setB.rwLock.unlock();
	}

	/**
	 * <pre>
	 * 若不是我方黑名单则失败，否则删除我方黑名单
	 * 
	 * @param role
	 * @param oppRole
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-13 上午10:58:17
	 * </pre>
	 */
	public static RSResult_AddFriend dealMsg_deleteBlackList(KRole role, long oppRoleId) {
		RSResult_AddFriend result = new RSResult_AddFriend();
		//
		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(role.getId());
		//
		set.rwLock.lock();
		try {

			KRelationShip ship = set.notifyElementDelete(KRelationShipTypeEnum.黑名单.sign, oppRoleId);
			if (ship == null) {
				result.tips = RelationShipTips.对方不在你的黑名单中;
				return result;
			}
			result.addDeleteRoleId(role.getId(), KRelationShipTypeEnum.黑名单, oppRoleId);

			result.isSucess = true;
			result.tips = RelationShipTips.取消拉黑成功;
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	public static List<Long> dealMsg_getAroundList(KRole role) {
		// 如若附近的人，已经是玩家好友或者你已经将对方加入黑名单，则该玩家不出现在附近的列表中。
		List<Long> result = KSupportFactory.getMapSupport().getAroundRoleIds(role);

		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(role.getId());
		//
		set.rwLock.lock();
		try {
			Map<Long, KRelationShip> friends = set.getRelationShipsCache(KRelationShipTypeEnum.好友.sign);
			Map<Long, KRelationShip> blacks = set.getRelationShipsCache(KRelationShipTypeEnum.黑名单.sign);

			//
			long myId = role.getId();
			for (Iterator<Long> it = result.iterator(); it.hasNext();) {
				long roleId = it.next();
				if (roleId == myId || friends.containsKey(roleId) || blacks.containsKey(roleId)) {
					it.remove();
				}
			}
		} finally {
			set.rwLock.unlock();
		}

		int toIndex = Math.min(result.size(), KRelationShipTypeEnum.附近的人.getMaxNum());
		return result.subList(0, toIndex);
	}

	/**
	 * <pre>
	 * 推荐好友
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-8-29 下午3:13:18
	 * </pre>
	 */
	static void recommondFriends(KRole role, int preLv) {
		int nowLv = role.getLevel();
		if(preLv>=nowLv){
			preLv = nowLv-1;
		}
		RSPushData pushData = null;
		for (int lv = preLv + 1; lv <= nowLv; lv++) {
			RSPushData tempPushData = KRelationShipDataManager.mRSPushDataManager.getData(lv);
			if (tempPushData != null) {
				pushData = tempPushData;
			}
		}
		if (pushData == null) {
			return;
		}

		KRelationShipSet set = KRelationShipModuleExtension.getRelationShipSet(role.getId());
		int releaseQuota = getMaxFriendCount(role.getId()) - set.countRelationShipSize(KRelationShipTypeEnum.好友.sign);
		releaseQuota = Math.max(0, releaseQuota);
		releaseQuota = Math.min(pushData.amount, releaseQuota);
		if (releaseQuota < 1) {
			// 已满员
			return;
		}

		// 符合基础范围且未满员的角色
		List<KRole> baseRangeRoles = new ArrayList<KRole>();
		// 符合备选范围的角色
		List<KRole> nextRangeRoles = new ArrayList<KRole>();
		{
			// 选出符合基础范围的角色（互不拉黑，不是我的好友，我未申请，对方未满员）
			// 选出符合备选范围的角色
			Collection<Long> allCacheRoleIds = KSupportFactory.getRoleModuleSupport().getRoleIdCache();
			KRole tempRole = null;
			KRelationShipSet tempSet = null;
			for (Long tempRoleId : allCacheRoleIds) {
				if (tempRoleId == role.getId()) {
					continue;
				}
				tempRole = KSupportFactory.getRoleModuleSupport().getRole(tempRoleId);
				if (tempRole == null) {
					continue;
				}
				int tempLv = tempRole.getLevel();
				// 基础范围
				if (pushData.minlevel <= tempLv && tempLv <= pushData.maxlevel) {
					tempSet = KRelationShipModuleExtension.getRelationShipSet(tempRoleId);
					if (checkRecommondFriend(set, tempSet)) {
						baseRangeRoles.add(tempRole);
					}
					continue;
				}
				// 备选范围
				if (pushData.nextMinlevel <= tempLv && tempLv <= pushData.nextMaxlevel) {
					// 未判断是否已满员，有需要再进一步过滤
					nextRangeRoles.add(tempRole);
					continue;
				}
			}
		}

		List<KRole> result = new ArrayList<KRole>();
		// 基础范围内选择
		{
			if (baseRangeRoles.size() > releaseQuota) {
				// 人数充足，随机选5个
				UtilTool.randomPartOfList(baseRangeRoles, releaseQuota, false);
			}
			result.addAll(baseRangeRoles);
		}
		// 备选范围内选择（互不拉黑，不是我的好友，我未申请）
		if (result.size() < releaseQuota && !nextRangeRoles.isEmpty()) {
			// 按等级排序
			Collections.sort(nextRangeRoles, pushData.isNextRangeAdd ? ComparatorLH.instance : ComparatorHL.instance);
			//
			KRelationShipSet tempSet = null;
			for (KRole tempRole : nextRangeRoles) {
				tempSet = KRelationShipModuleExtension.getRelationShipSet(tempRole.getId());
				if (checkRecommondFriend(set, tempSet)) {
					result.add(tempRole);
					if (result.size() >= releaseQuota) {
						break;
					}
				}
			}
		}

		// 发送消息
		KPushRSsMsg.pushRecommendFriends(role, result);
	}

	/**
	 * <pre>
	 * 检查是否符合推送要求
	 * 
	 * 互不拉黑，不是我的好友，我未申请，对方未满员
	 * 
	 * @param mySet
	 * @param oppSet
	 * @return
	 * @author CamusHuang
	 * @creation 2014-9-10 下午3:46:55
	 * </pre>
	 */
	private static boolean checkRecommondFriend(KRelationShipSet mySet, KRelationShipSet oppSet) {
		if (oppSet == null) {
			return false;
		}
		// 对方未满员
		if (oppSet.isRelationShipFull(KRelationShipTypeEnum.好友)) {
			return false;
		}

		// 不是我的好友
		KRelationShip tempShip = mySet.getRelationShip(KRelationShipTypeEnum.好友.sign, oppSet._roleId);
		if (tempShip != null) {
			return false;
		}

		// 互不拉黑
		tempShip = mySet.getRelationShip(KRelationShipTypeEnum.黑名单.sign, oppSet._roleId);
		if (tempShip != null) {
			return false;
		}
		tempShip = oppSet.getRelationShip(KRelationShipTypeEnum.黑名单.sign, mySet._roleId);
		if (tempShip != null) {
			return false;
		}

		// 我未申请
		tempShip = oppSet.getRelationShip(KRelationShipTypeEnum.好友申请.sign, mySet._roleId);
		if (tempShip != null) {
			return false;
		}

		return true;
	}

	/**
	 * <pre>
	 * 将角色按等级从高到低排序
	 * 
	 * @param <Role>
	 * @author CamusHuang
	 * @creation 2014-8-29 下午4:48:03
	 * </pre>
	 */
	private static class ComparatorHL implements Comparator<KRole> {

		final static ComparatorHL instance = new ComparatorHL();

		@Override
		public int compare(KRole o1, KRole o2) {
			int temp = o1.getLevel() - o2.getLevel();
			if (temp > 0) {
				return -1;
			}
			if (temp < 0) {
				return 1;
			}
			return 0;
		}

	}

	/**
	 * <pre>
	 * 将角色按等级从低到高排序
	 * 
	 * @param <Role>
	 * @author CamusHuang
	 * @creation 2014-8-29 下午4:48:18
	 * </pre>
	 */
	private static class ComparatorLH implements Comparator<KRole> {

		final static ComparatorLH instance = new ComparatorLH();

		@Override
		public int compare(KRole o1, KRole o2) {
			int temp = o1.getLevel() - o2.getLevel();
			if (temp > 0) {
				return 1;
			}
			if (temp < 0) {
				return -1;
			}
			return 0;
		}
	}
}
