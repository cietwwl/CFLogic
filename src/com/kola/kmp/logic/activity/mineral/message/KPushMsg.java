package com.kola.kmp.logic.activity.mineral.message;

import java.util.List;
import java.util.Set;

import javax.management.timer.Timer;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.mineral.KDigMineralActivityImpl;
import com.kola.kmp.logic.activity.mineral.KDigMineralActivityManager;
import com.kola.kmp.logic.activity.mineral.KDigMineralDataManager;
import com.kola.kmp.logic.activity.mineral.KDigMineralTaskManager.KMineralMinuteTask;
import com.kola.kmp.logic.activity.mineral.KRoleDiggerData;
import com.kola.kmp.logic.map.KMapEntityData;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMap;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapBornPoint;
import com.kola.kmp.logic.other.KMapEntityTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.tips.ActivityTips;
import com.kola.kmp.protocol.activity.KActivityProtocol;

public class KPushMsg implements KActivityProtocol {

	/**
	 * <pre>
	 * 服务器发送挖矿数据到客户端
	 * String 今天挖矿的历史奖励描述
	 * String 个性签名
	 * int 活动ID
	 * </pre>
	 */
	public static void pushDigActivityData(long roleId, KRoleDiggerData roleData) {

		roleData.rwLock.lock();
		try {
			KGameMessage msg = KGame.newLogicMessage(SM_SEND_DIG_ACTIVITY_DATA);
			msg.writeUtf8String(roleData.genStringForTodayReward());
			msg.writeUtf8String(roleData.getDeclare());
			msg.writeInt(KDigMineralActivityImpl.instance.getActivityId());
			msg.writeUtf8String(KDigMineralDataManager.铁镐ItemCode);

			List<String> dialys = roleData.getDialyCache();
			msg.writeByte(dialys.size());
			for (String dialy : dialys) {
				msg.writeUtf8String(dialy);
			}

			msg.writeInt((int) (KDigMineralDataManager.产出周期 / Timer.ONE_SECOND));

			KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
		} finally {
			roleData.rwLock.unlock();
		}
	}


	/**
	 * <pre>
	 * 同步挖矿工作状态
	 * 
	 * </pre>
	 */
	public static void synMineJob(long roleId, int mineId) {

		KGameMessage msg = KGame.newLogicMessage(SM_SYN_MINE_JOB);
		// int 矿石id（暂定为instanceId）-1表示停止挖矿
		msg.writeInt(mineId);

		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	/**
	 * <pre>
	 * 服务器发送驱逐者信息到客户端
	 * String 标题
	 * String 内容
	 * long 驱逐者id
	 * String 驱逐者名字
	 * int 驱逐者头像id
	 * short 驱逐者等级
	 * int 驱逐者战斗力
	 * </pre>
	 */
	public static void pushBanishDialog(long roleId, String title, String content, int mineId, KRole oppRole) {

		KGameMessage msg = KGame.newLogicMessage(SM_SEND_BANISH_DIALOG);
		msg.writeUtf8String(title);
		msg.writeUtf8String(content);
		msg.writeInt(mineId);
		msg.writeLong(oppRole.getId());
		msg.writeUtf8String(oppRole.getName());
		msg.writeInt(oppRole.getHeadResId());
		msg.writeShort(oppRole.getLevel());
		msg.writeInt(oppRole.getBattlePower());

		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	public static void pushRolesInMineral(long roleId, int mineId) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_ROLES_IN_MINERAL);
		msg.writeInt(mineId);
		KDigMineralActivityManager.packRoleInMineral(msg, mineId);

		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	public static void pushPVPResult(KRole role, boolean isWin, String tips) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_BANISH_RESULT);
		msg.writeBoolean(isWin);
		msg.writeUtf8String(tips);
		role.sendMsg(msg);
	}

	public static void pushCountDown(long roleId, KRoleDiggerData roleData) {

		long nowTime = System.currentTimeMillis();

		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_NEW_COUNTDOWN);
		msg.writeInt(Math.max(2, (int) ((roleData.nextProduceTime - nowTime) / Timer.ONE_SECOND)));
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	public static void pushCountDown(Set<Long> roleIds) {
		if (roleIds.isEmpty()) {
			return;
		}

		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_NEW_COUNTDOWN);
		msg.writeInt(Math.max(2, (int) (KDigMineralDataManager.产出周期 / Timer.ONE_SECOND)));

		RoleModuleSupport mRoleModuleSupport = KSupportFactory.getRoleModuleSupport();
		for (long roleId : roleIds) {
			mRoleModuleSupport.sendMsg(roleId, msg.duplicate());
		}
	}

	public static void pushNewDialy(long roleId, String dialy) {

		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_NEW_DIALY);
		msg.writeUtf8String(dialy);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	public static void pushTodayRewardClear(Set<Long> changeRoleIds) {
		if (changeRoleIds.isEmpty()) {
			return;
		}

		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_HISTORY_REWARD_DESC);
		String tips = StringUtil.format(ActivityTips.今天已挖矿x时间获得奖励x, UtilTool.genReleaseCDTimeString(0), "");
		msg.writeUtf8String(tips);

		RoleModuleSupport mRoleModuleSupport = KSupportFactory.getRoleModuleSupport();
		for (long roleId : changeRoleIds) {
			mRoleModuleSupport.sendMsg(roleId, msg);
		}
	}

	public static void pushTodayReward(KRole role, KRoleDiggerData roleData) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_HISTORY_REWARD_DESC);
		msg.writeUtf8String(roleData.genStringForTodayReward());
		role.sendMsg(msg);
	}

	/**
	 * <pre>
	 * 角色A开始、停止挖矿时，如果角色A在线，则通知所有能看到本人的其它角色更新其状态
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-12-10 下午12:17:54
	 * </pre>
	 */
	public static void synMineStatusToArround(KRole role, KRoleDiggerData roleData) {
		// * long 角色id
		// * int 当前挖矿id
		// * if (当前挖矿id > 0) {
		// * float 挖矿时的x坐标
		// * float 挖矿时的y坐标
		// * }
		if (!role.isOnline()) {
			return;
		}
		int dupMapId = KDigMineralDataManager.mineMap.getDuplicateId();
		KDuplicateMap<KMapEntityData> map = KSupportFactory.getDuplicateMapSupport().getDuplicateMap(dupMapId);
		if (map == null) {
			return;
		}
		KMapEntityData<KRole> mapEntity = map.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, role.getId());
		if (mapEntity == null) {
			return;
		}

		List<Long> oppRoleIds = mapEntity.getMyHolderRoleIds();
		if (oppRoleIds.isEmpty()) {
			return;
		}

		KGameMessage msg = KGame.newLogicMessage(SM_SYNC_MINING_STATE);
		roleData.rwLock.lock();
		try {
			msg.writeByte(1);
//			System.err.println(role.getId()+":"+roleData.getMineId());
			msg.writeLong(role.getId());
			msg.writeInt(roleData.getMineId());
			if (roleData.getMineId() > 0) {
				KDuplicateMapBornPoint point = roleData.getDigPoint();
				msg.writeFloat(point._corX);
				msg.writeFloat(point._corY);
			}
//			if(role.getName().equals("布兰妮鲍林") && roleData.getMineId()<1){
//				System.err.println(role.getName()+"："+roleData.getMineId());
//			}
		} finally {
			roleData.rwLock.unlock();
		}

		RoleModuleSupport mRoleModuleSupport = KSupportFactory.getRoleModuleSupport();
		for (long oppRoleId : oppRoleIds) {
			mRoleModuleSupport.sendMsg(oppRoleId, msg.duplicate());
		}
	}

	/**
	 * <pre>
	 * 角色进入时，刷新所有本人能看到的其它角色的状态
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-12-10 下午12:17:54
	 * </pre>
	 */
	public static void synMineStatusToMe(long roleId) {
		// * long 角色id
		// * int 当前挖矿id
		// * if (当前挖矿id > 0) {
		// * float 挖矿时的x坐标
		// * float 挖矿时的y坐标
		// * }
		int dupMapId = KDigMineralDataManager.mineMap.getDuplicateId();
		KDuplicateMap<KMapEntityData> map = KSupportFactory.getDuplicateMapSupport().getDuplicateMap(dupMapId);
		if (map == null) {
			return;
		}
		KMapEntityData<KRole> mapEntity = map.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, roleId);
		if (mapEntity == null) {
			return;
		}

		List<Long> oppRoleIds = mapEntity.getMyShowRoleIds();
		if (oppRoleIds.isEmpty()) {
			return;
		}

		KGameMessage msg = KGame.newLogicMessage(SM_SYNC_MINING_STATE);
		int writeIndex = msg.writerIndex();
		msg.writeByte(0);
		int count = 0;
		for (long oppRoleId : oppRoleIds) {
			KRoleDiggerData roleData = KDigMineralActivityManager.getRoleDiggerData(oppRoleId);
			if (roleData == null) {
				continue;
			}

			roleData.rwLock.lock();
			try {
				msg.writeLong(oppRoleId);
//				System.err.println(oppRoleId+":"+roleData.getMineId());
				msg.writeInt(roleData.getMineId());
				if (roleData.getMineId() > 0) {
					KDuplicateMapBornPoint point = roleData.getDigPoint();
					msg.writeFloat(point._corX);
					msg.writeFloat(point._corY);
				}
			} finally {
				roleData.rwLock.unlock();
			}
			count++;
		}
		msg.setByte(writeIndex, count);
		//
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
}
