package com.kola.kmp.logic.activity.mineral;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.KActivity;
import com.kola.kmp.logic.activity.KActivityRoleExtCaCreator;
import com.kola.kmp.logic.activity.KActivityRoleExtData;
import com.kola.kmp.logic.activity.mineral.KDigMineralDataManager.GoldBaseDataManger.KMineGoldBaseData;
import com.kola.kmp.logic.activity.mineral.KDigMineralDataManager.MineralDataManger.KMineral;
import com.kola.kmp.logic.activity.mineral.KDigMineralDataManager.MineralTempDataManger.KMineralTemplate;
import com.kola.kmp.logic.activity.mineral.KDigMineralTaskManager.KMineralSyncTask;
import com.kola.kmp.logic.activity.mineral.message.KPushMsg;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.combat.ICombatCommonResult;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.map.KMapEntityData;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMap;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapBornPoint;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.other.KItemTypeEnum;
import com.kola.kmp.logic.other.KMapEntityTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.CurrencyModuleSupport;
import com.kola.kmp.logic.support.ItemModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.ResultStructs.DigRequestResult;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.tips.ActivityTips;
import com.kola.kmp.logic.util.tips.GangWarTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ShopTips;

public class KDigMineralActivityManager {

	public static final Logger _LOGGER = KGameLogger.getLogger(KDigMineralActivityManager.class);

	public static KRoleDiggerData getRoleDiggerData(long roleId) {
		KActivityRoleExtData roleActivityData = KActivityRoleExtCaCreator.getActivityRoleExtData(roleId);
		if (roleActivityData == null) {
			return null;
		}
		return roleActivityData.getMineDiggerData();
	}

	public static CommonResult dealMsg_joinMineral(KRole role) {

		CommonResult result = new CommonResult();

		KActivity activity = KDigMineralActivityImpl.instance;
		if (activity == null) {
			_LOGGER.error("###error：角色请求进入挖矿活动发生错误，找不到对应的活动数据。");
			result.tips = ActivityTips.此活动未开放;
			return result;
		}

		if (!KSupportFactory.getMissionSupport().checkFunctionIsOpen(role, KFunctionTypeEnum.挖矿副本)) {
			result.tips = ActivityTips.此活动未开放;
			return result;
		}

		KActionResult tempResult = activity.playerRoleJoinActivity(role);
		if (tempResult.success) {
			return result;
		}
		result.tips = tempResult.tips;
		return result;
	}

	public static CommonResult dealMsg_updateDeclare(KRole role, String data) {
		CommonResult result = new CommonResult();

		KRoleDiggerData roleData = getRoleDiggerData(role.getId());
		if (roleData == null) {
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			return result;
		}

		if (data.length() > KDigMineralDataManager.DeclareMaxSize) {
			data = data.substring(0, KDigMineralDataManager.DeclareMaxSize);
		}

		data = KSupportFactory.getDirtyWordSupport().clearDirtyWords(data, true);

		roleData.setDeclare(data);

		result.tips = ActivityTips.更新成功;
		return result;
	}

	public static DigRequestResult dealMsg_requestDigMineral(KRole role, int mineId) {
		DigRequestResult result = new DigRequestResult();

		KRoleDiggerData roleData = getRoleDiggerData(role.getId());

		if (sendRevengeDialog(role, roleData) == 2) {
			return result;
		}

		KMineralTemplate temp = KDigMineralDataManager.mMineralTempDataManger.getTemplate(mineId);
		if (temp == null) {
			result.tips = ActivityTips.不存在此矿区;
			return result;
		}

		KMineral mineral = KDigMineralDataManager.mMineralDataManger.getMineral(mineId);
		if (mineral == null) {
			result.tips = ActivityTips.不存在此矿区;
			return result;
		}

		mineral.rwLock.lock();
		try {
			roleData.rwLock.lock();
			try {

				if (roleData.getMineId() > 0) {
					result.tips = ActivityTips.请先停止挖矿;
					return result;
				}

				if (mineral.containDigger(role.getId())) {
					result.isSucess = true;
					result.tips = ActivityTips.开始挖矿;
					return result;
				}

				// 保护：离开所有矿区
				KDigMineralDataManager.mMineralDataManger.removeDiggerFromAllMineral(role.getId());

				if (mineral.getDiggerSize() >= temp.maxDiggerCount) {
					// 满员，发送驱赶列表，要求驱赶
					result.isShowNameList = true;
					return result;
				}

				// 是否有工具
				if (!roleData.hashTool()) {
					result.tips = ActivityTips.缺少工具;
					return result;
				}

				// 成功开始挖矿
				mineral.addDigger(role.getId());
				roleData.setMineData(mineId);
				//
				result.isSucess = true;
				result.tips = ActivityTips.开始挖矿;
				return result;
			} finally {
				roleData.rwLock.unlock();
			}
		} finally {
			mineral.rwLock.unlock();

			if (result.isSucess) {
				// 同步倒计时
				KPushMsg.pushCountDown(role.getId(), roleData);

				// 同步周围玩家
				KPushMsg.synMineStatusToArround(role, roleData);
			}
		}
	}

	/**
	 * <pre>
	 * 驱赶
	 * 
	 * @param role
	 * @param mineId
	 * @param oppRoleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-12-8 上午10:31:27
	 * </pre>
	 */
	public static DigRequestResult dealMsg_requestBanish(KRole role, int mineId, long oppRoleId) {

		DigRequestResult result = new DigRequestResult();

		if (role.isFighting()) {
			result.tips = GangWarTips.状态有误请重新登陆;
			return result;
		}

		KMineralTemplate temp = KDigMineralDataManager.mMineralTempDataManger.getTemplate(mineId);
		if (temp == null) {
			result.tips = ActivityTips.不存在此矿区;
			return result;
		}

		KMineral mineral = KDigMineralDataManager.mMineralDataManger.getMineral(mineId);
		if (mineral == null) {
			result.tips = ActivityTips.不存在此矿区;
			return result;
		}

		KRole oppRole = KSupportFactory.getRoleModuleSupport().getRole(oppRoleId);
		if (oppRole == null) {
			result.tips = ActivityTips.该玩家已被驱赶出矿区;
			return result;
		}

		KRoleDiggerData oppRoleData = getRoleDiggerData(oppRoleId);
		if (oppRoleData == null) {
			result.isShowNameList = true;
			result.tips = ActivityTips.该玩家已被驱赶出矿区;
			return result;
		}

		KRoleDiggerData roleData = getRoleDiggerData(role.getId());
		//
		mineral.rwLock.lock();
		try {
			lock(roleData, oppRoleData);
			try {
				if (roleData.getMineId() > 0) {
					result.tips = ActivityTips.请先停止挖矿;
					return result;
				}

				if (!mineral.containDigger(oppRoleId)) {
					result.isShowNameList = true;
					result.tips = ActivityTips.该玩家已被驱赶出矿区;
					return result;
				}

				// 记录参战
				if (!oppRoleData.isInWar.compareAndSet(false, true)) {
					result.isShowNameList = true;
					result.tips = ActivityTips.该玩家正在被挑战;
					return result;
				}

				try {// 本人打对方AI
					KActionResult<Integer> pvpPesult = KSupportFactory.getCombatModuleSupport().fightWithAI(role, oppRoleId, KCombatType.MINERAL_PVP, KDigMineralDataManager.PVPBattlefield,
							new DigPVPAttachment(oppRoleId, mineId, false));
					if (!pvpPesult.success) {
						oppRoleData.isInWar.set(false);
						result.tips = pvpPesult.tips;
						return result;
					}
					// PVP
					_LOGGER.warn("挖矿PVP：角色{} vs 角色{}！", role.getName(), oppRole.getName());

					result.isSucess = true;
					return result;
				} catch (Exception e) {
					_LOGGER.error(e.getMessage(), e);

					oppRoleData.isInWar.set(false);
					result.tips = GlobalTips.服务器繁忙请稍候再试;
					return result;
				}
			} finally {
				unlock(roleData, oppRoleData);
			}
		} finally {
			mineral.rwLock.unlock();

			if (result.isSucess) {
				// 保护：离开所有矿区
				KDigMineralDataManager.mMineralDataManger.removeDiggerFromAllMineral(role.getId());
			}
		}
	}

	private static void lock(KRoleDiggerData roleData, KRoleDiggerData oppRoleData) {
		if (roleData._roleId > oppRoleData._roleId) {
			roleData.rwLock.lock();
			oppRoleData.rwLock.lock();
		} else {
			oppRoleData.rwLock.lock();
			roleData.rwLock.lock();
		}
	}

	private static void unlock(KRoleDiggerData roleData, KRoleDiggerData oppRoleData) {
		if (roleData._roleId > oppRoleData._roleId) {
			oppRoleData.rwLock.unlock();
			roleData.rwLock.unlock();
		} else {
			roleData.rwLock.unlock();
			oppRoleData.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 复仇
	 * 
	 * @param role
	 * @param mineId
	 * @param oppRoleId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-12-8 上午10:31:36
	 * </pre>
	 */
	public static CommonResult dealMsg_requestRevenge(KRole role) {

		CommonResult result = new CommonResult();

		if (role.isFighting()) {
			result.tips = GangWarTips.状态有误请重新登陆;
			return result;
		}

		KRoleDiggerData roleData = getRoleDiggerData(role.getId());

		long oppRoleId = roleData.getRevengeRoleId();
		KRole oppRole = KSupportFactory.getRoleModuleSupport().getRole(oppRoleId);
		if (oppRole == null) {
			result.tips = ActivityTips.此玩家不处于挖矿状态中无法复仇;
			return result;
		}
		KRoleDiggerData oppRoleData = getRoleDiggerData(oppRoleId);
		if (oppRoleData == null) {
			result.tips = ActivityTips.此玩家不处于挖矿状态中无法复仇;
			return result;
		}

		int mineId = oppRoleData.getMineId();
		if (mineId < 1) {
			result.tips = ActivityTips.此玩家不处于挖矿状态中无法复仇;
			return result;
		}

		KMineralTemplate temp = KDigMineralDataManager.mMineralTempDataManger.getTemplate(mineId);
		if (temp == null) {
			result.tips = ActivityTips.此玩家不处于挖矿状态中无法复仇;
			return result;
		}

		KMineral mineral = KDigMineralDataManager.mMineralDataManger.getMineral(mineId);
		if (mineral == null) {
			result.tips = ActivityTips.此玩家不处于挖矿状态中无法复仇;
			return result;
		}

		mineral.rwLock.lock();
		try {
			lock(roleData, oppRoleData);
			try {

				// 记录参战
				if (!oppRoleData.isInWar.compareAndSet(false, true)) {
					result.tips = ActivityTips.该玩家正在被挑战;
					return result;
				}

				if (!mineral.containDigger(oppRoleId)) {
					result.tips = ActivityTips.此玩家不处于挖矿状态中无法复仇;
					return result;
				}

				try {// 本人打对方AI
					KActionResult<Integer> pvpPesult = KSupportFactory.getCombatModuleSupport().fightWithAI(role, oppRoleId, KCombatType.MINERAL_PVP, KDigMineralDataManager.PVPBattlefield,
							new DigPVPAttachment(oppRoleId, mineId, true));
					if (!pvpPesult.success) {
						oppRoleData.isInWar.set(false);
						result.tips = pvpPesult.tips;
						return result;
					}
					// PVP
					_LOGGER.warn("复仇PVP：角色{} vs 角色{}！", role.getName(), oppRole.getName());

					// 自动停止挖矿
					roleData.setMineData(-1);
					result.isSucess = true;
					return result;
				} catch (Exception e) {
					_LOGGER.error(e.getMessage(), e);

					oppRoleData.isInWar.set(false);
					result.tips = GlobalTips.服务器繁忙请稍候再试;
					return result;
				}

			} finally {
				unlock(roleData, oppRoleData);
			}
		} finally {
			mineral.rwLock.unlock();

			if (result.isSucess) {
				// 离开所有矿区
				KDigMineralDataManager.mMineralDataManger.removeDiggerFromAllMineral(role.getId());
				// 同步周围玩家
				KPushMsg.synMineStatusToArround(role, roleData);
			}
		}
	}

	public static CommonResult dealMsg_cancelDigMine(KRole role) {
		CommonResult result = new CommonResult();

		// 离开所有矿区
		KDigMineralDataManager.mMineralDataManger.removeDiggerFromAllMineral(role.getId());

		KRoleDiggerData roleData = getRoleDiggerData(role.getId());

		try {
			roleData.rwLock.lock();
			try {
				roleData.setMineData(-1);
			} finally {
				roleData.rwLock.unlock();
			}

			result.isSucess = true;
			result.tips = ActivityTips.取消挖矿成功;
			return result;
		} finally {
			if (result.isSucess) {
				// 同步周围玩家
				KPushMsg.synMineStatusToArround(role, roleData);
			}
		}
	}

	/**
	 * <pre>
	 * 打包矿内玩家列表
	 * 
	 * @param msg
	 * @param mineId
	 * @author CamusHuang
	 * @creation 2014-12-5 下午4:18:47
	 * </pre>
	 */
	public static void packRoleInMineral(KGameMessage msg, int mineId) {
		KMineral mineral = KDigMineralDataManager.mMineralDataManger.getMineral(mineId);
		if (mineral == null) {
			msg.writeByte(0);
			return;
		}
		mineral.rwLock.lock();
		try {

			RoleModuleSupport mRoleModuleSupport = KSupportFactory.getRoleModuleSupport();

			List<Long> list = new ArrayList<Long>(mineral.getAllDiggerCache());
			UtilTool.randomPartOfList(list, KDigMineralDataManager.驱赶名单最大数量, false);
			//
			int writeIndex = msg.writerIndex();
			msg.writeByte(0);
			int count = 0;
			for (long roleId : list) {
				KRole role = mRoleModuleSupport.getRole(roleId);
				if(role==null){
					continue;
				}
				KRoleDiggerData roleData = getRoleDiggerData(role.getId());
				if(roleData==null){
					continue;
				}
				//
				msg.writeLong(role.getId());
				msg.writeUtf8String(role.getName());
				msg.writeInt(role.getHeadResId());
				msg.writeShort(role.getLevel());
				msg.writeInt(role.getBattlePower());
				msg.writeUtf8String(roleData.getDeclare());
				//
				count++;
			}
			msg.setByte(writeIndex, count);
		} finally {
			mineral.rwLock.unlock();
		}
	}

	/**
	 * 外部通知：PVP战斗结果，出结果即时调用
	 * 
	 * @param role
	 * @param result
	 */
	public static void notifyPVPBattleFinished(KRole role, ICombatCommonResult result) {

		DigPVPAttachment att = (DigPVPAttachment) result.getAttachment();

		if (att.isRevenge) {
			// 复仇
			notifyPVPResultForRevenge(result.isWin(), role, att.oppRoleId);
		} else {
			// 驱赶
			notifyPVPResultForBanish(result.isWin(), role, att.oppRoleId);
		}
	}

	private static void notifyPVPResultForRevenge(boolean isWin, KRole role, long oppRoleId) {
		KRole oppRole = KSupportFactory.getRoleModuleSupport().getRole(oppRoleId);
		KRoleDiggerData oppRoleData = getRoleDiggerData(oppRole.getId());
		if (oppRole == null || oppRoleData == null){
			notifyPVPResultForRevengeWithNull(isWin, role);
			return;
		}
		
		oppRoleData.isInWar.set(false);
		//
		KRoleDiggerData roleData = getRoleDiggerData(role.getId());
		roleData.isInWar.set(false);

		_LOGGER.warn("复仇PVP：结果({})通知：角色{} vs 角色{}！", (isWin ? "胜出" : "战败"), role.getName(), oppRole.getName());

		int mineId = oppRoleData.getMineId();
		String tips = null;
		try {
			if (!isWin) {
				// 战斗失败
				tips = ActivityTips.复仇失败;
				return;
			}

			// 战斗胜利
			KMineral mineral = KDigMineralDataManager.mMineralDataManger.getMineral(mineId);
			if (mineral == null) {
				tips = ActivityTips.战斗胜利矿区已满;
				return;
			}

			// ///////////////
			mineral.rwLock.lock();
			try {
				lock(roleData, oppRoleData);
				try {

					// 退出矿区
					mineral.removeDigger(oppRoleId);
					oppRoleData.setMineData(-1);

					// 是否有工具
					if (!roleData.hashTool()) {
						tips = ActivityTips.复仇成功缺少工具;
						return;
					}

					{
						tips = ActivityTips.复仇成功开始挖矿;
						//
						mineral.addDigger(role.getId());
						roleData.setMineData(mineId);
					}

					// ///////////////////////////////////////////////
				} finally {
					unlock(roleData, oppRoleData);
				}
			} finally {
				mineral.rwLock.unlock();
			}
		} finally {

			// 同步挖矿状态
			KPushMsg.synMineJob(role.getId(), roleData.getMineId());

			if (roleData.getMineId() > 0) {
				// 同步倒计时
				KPushMsg.pushCountDown(role.getId(), roleData);
			}
			// 同步周围玩家
			KPushMsg.synMineStatusToArround(role, roleData);

			// 同步挖矿状态
			{
				KPushMsg.synMineJob(oppRoleId, oppRoleData.getMineId());
				// 同步周围玩家
				KPushMsg.synMineStatusToArround(oppRole, oppRoleData);
			}

			// 战斗结算奖励
			KPushMsg.pushPVPResult(role, isWin, tips);
		}
	}
	
	private static void notifyPVPResultForRevengeWithNull(boolean isWin, KRole role) {
		KRoleDiggerData roleData = getRoleDiggerData(role.getId());
		roleData.isInWar.set(false);
		//
		_LOGGER.warn("复仇PVP：结果({})通知：角色{} vs 角色{}！", (isWin ? "胜出" : "战败"), role.getName(), "--");

		String tips = ActivityTips.复仇失败;
		{

			// 同步挖矿状态
			KPushMsg.synMineJob(role.getId(), roleData.getMineId());

			if (roleData.getMineId() > 0) {
				// 同步倒计时
				KPushMsg.pushCountDown(role.getId(), roleData);
			}
			// 同步周围玩家
			KPushMsg.synMineStatusToArround(role, roleData);

			// 战斗结算奖励
			KPushMsg.pushPVPResult(role, isWin, tips);
		}
	}	

	private static void notifyPVPResultForBanish(boolean isWin, KRole role, long oppRoleId) {
		KRole oppRole = KSupportFactory.getRoleModuleSupport().getRole(oppRoleId);
		KRoleDiggerData oppRoleData = getRoleDiggerData(oppRoleId);
		if(oppRole==null || oppRoleData==null){
			notifyPVPResultForBanishWithNull(isWin, role);
			return;
		}
		
		oppRoleData.isInWar.set(false);
		//
		KRoleDiggerData roleData = getRoleDiggerData(role.getId());
		roleData.isInWar.set(false);

		_LOGGER.warn("驱赶PVP：结果({})通知：角色{} vs 角色{}！", (isWin ? "胜出" : "战败"), role.getName(), oppRole.getName());

		boolean isBanishSuccess = false;//是否驱赶成功
		int mineId = oppRoleData.getMineId();
		KMineral mineral = KDigMineralDataManager.mMineralDataManger.getMineral(mineId);
		String tips = null;
		try {
			if (!isWin) {
				// 战斗失败
				tips = ActivityTips.驱赶失败;
				return;
			}

			// 战斗胜利
			if (mineral == null) {
				tips = ActivityTips.战斗胜利矿区已满;
				return;
			}

			// ///////////////
			mineral.rwLock.lock();
			try {
				lock(roleData, oppRoleData);
				try {

					// 退出矿区
					mineral.removeDigger(oppRoleId);
					oppRoleData.setMineData(-1);
					isBanishSuccess = true;
					// 复仇数据记录
					oppRoleData.setRevengeRoleId(role.getId(), mineId);
					roleData.setBanishRoleId(oppRoleId);

					// 是否有工具
					if (!roleData.hashTool()) {
						tips = ActivityTips.驱赶成功缺少工具;
					} else {
						tips = ActivityTips.驱赶成功开始挖矿;
						//
						mineral.addDigger(role.getId());
						roleData.setMineData(mineId);
					}
					// ///////////////////////////////////////////////
				} finally {
					unlock(roleData, oppRoleData);
				}
			} finally {
				mineral.rwLock.unlock();
			}
		} finally {

			// 同步挖矿状态
			KPushMsg.synMineJob(role.getId(), roleData.getMineId());

			if (roleData.getMineId() > 0) {
				// 同步倒计时
				KPushMsg.pushCountDown(role.getId(), roleData);
			}
			// 同步周围玩家
			KPushMsg.synMineStatusToArround(role, roleData);

			// 同步挖矿状态
			{
				KPushMsg.synMineJob(oppRoleId, oppRoleData.getMineId());
				// 同步周围玩家
				KPushMsg.synMineStatusToArround(oppRole, oppRoleData);
				
				if(isBanishSuccess){
					String dialy = StringUtil.format(ActivityTips.x时间被x玩家驱动出x矿区, UtilTool.DATE_FORMAT11.format(new Date()), 
							role.getExName(), mineral.template.mineName);
					oppRoleData.addDialy(dialy);
					// 日志同步
					if (oppRole.isOnline()) {
						KPushMsg.pushNewDialy(oppRole.getId(), dialy);
						KDialogService.sendUprisingDialog(oppRole, dialy);
					}
				}
			}

			// 战斗结算奖励
			KPushMsg.pushPVPResult(role, isWin, tips);
		}
	}
	
	private static void notifyPVPResultForBanishWithNull(boolean isWin, KRole role) {
		KRoleDiggerData roleData = getRoleDiggerData(role.getId());
		roleData.isInWar.set(false);

		_LOGGER.warn("驱赶PVP：结果({})通知：角色{} vs 角色{}！", (isWin ? "胜出" : "战败"), role.getName(), "--");

		String tips = ActivityTips.驱赶失败;
		{

			// 同步挖矿状态
			KPushMsg.synMineJob(role.getId(), roleData.getMineId());

			if (roleData.getMineId() > 0) {
				// 同步倒计时
				KPushMsg.pushCountDown(role.getId(), roleData);
			}
			// 同步周围玩家
			KPushMsg.synMineStatusToArround(role, roleData);

			// 战斗结算奖励
			KPushMsg.pushPVPResult(role, isWin, tips);
		}
	}	

	/**
	 * <pre>
	 * 
	 * 
	 * @param role
	 * @param roleData
	 * @return -1表示条件不成熟，暂不发；1表示已经处理，但无需推送；2表示已经处理，且已推送
	 * @author CamusHuang
	 * @creation 2014-12-10 下午11:36:11
	 * </pre>
	 */
	static int sendRevengeDialog(KRole role, KRoleDiggerData roleData) {
		if (role == null || roleData == null) {
			return 1;
		}

		if (!role.isOnline() || !roleData.isInMap.get() || roleData.isInWar.get()) {
			return -1;
		}

		{
			long oppRoleId = roleData.getRevengeRoleId();
			KRole oppRole = KSupportFactory.getRoleModuleSupport().getRole(oppRoleId);
			if (oppRole == null) {
				return 1;
			}
			KRoleDiggerData oppRoleData = getRoleDiggerData(oppRoleId);
			if (oppRoleData == null) {
				return 1;
			}
			if (oppRoleData.getMineId() < 1) {
				// 对方非挖矿中
				return -1;
			}

			KMineral mineral = KDigMineralDataManager.mMineralDataManger.getMineral(roleData.getRevengeMineId());
			if (mineral == null) {
				return 1;
			}

			String digtips = StringUtil.format(ActivityTips.x在x矿驱赶了你是否复仇, oppRole.getExName(), mineral.template.mineName);
			KPushMsg.pushBanishDialog(role.getId(), ActivityTips.被驱赶, digtips, oppRoleData.getMineId(), oppRole);

			// 下次不再弹框
			roleData.setRevengeRoleId(oppRoleId, -1);
			return 2;
		}
	}

	public static void dealMsg_confrimPKResult(KRole role) {
		_LOGGER.info("挖矿：角色（{}:{}）确认战斗结算", role.getId(), role.getName());

		KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(role);

		// 推送复仇弹框
		KRoleDiggerData roleData = KDigMineralActivityManager.getRoleDiggerData(role.getId());
		//
		long oppRoleId = roleData.getBanishRoleId();
		if (oppRoleId > 0) {
			KRole oppRole = KSupportFactory.getRoleModuleSupport().getRole(oppRoleId);
			KRoleDiggerData oppRoleData = KDigMineralActivityManager.getRoleDiggerData(oppRoleId);

			if (oppRole == null || oppRoleData == null) {
				roleData.setBanishRoleId(-1);
			} else {
				if (sendRevengeDialog(oppRole, oppRoleData) > 0) {
					roleData.setBanishRoleId(-1);
				}
			}
		}

		// 同步周围玩家状态
		KMineralSyncTask.submit(role);
	}
	
	public static void notifyRoleDeleted(long roleId) {
		// 离开所有矿区
		KDigMineralDataManager.mMineralDataManger.removeDiggerFromAllMineral(roleId);
	}

	static void notifyForDayChange() {
		// 针对在线玩家，进行奖励统计清空，并同步客户端

		long nowTime = System.currentTimeMillis();

		Set<Long> changeRoleIds = new HashSet<Long>();
		for (long roleId : KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds()) {

			KRoleDiggerData roleData = KDigMineralActivityManager.getRoleDiggerData(roleId);

			if (roleData!=null && roleData.notifyForDayChange(nowTime)) {
				changeRoleIds.add(roleId);
			}
		}
		//
		KPushMsg.pushTodayRewardClear(changeRoleIds);
	}

	/**
	 * <pre>
	 * 时效任务，出产
	 * 
	 * @author CamusHuang
	 * @creation 2014-12-8 下午4:50:18
	 * </pre>
	 */
	static void notifyForProduceTask() {
		// 针对所有挖矿中的玩家，进行每周期产出，离线则汇总上线时发邮件

		CurrencyModuleSupport mCurrencyModuleSupport = KSupportFactory.getCurrencySupport();
		ItemModuleSupport mItemModuleSupport = KSupportFactory.getItemModuleSupport();
		RoleModuleSupport mRoleModuleSupport = KSupportFactory.getRoleModuleSupport();

		long nowTime = System.currentTimeMillis();
		long nextProduceTime = nowTime + KDigMineralDataManager.产出周期;

		Set<Long> allPushCountDownRoleids = new HashSet<Long>();//所有需要同步倒计时的角色
		List<ItemCountStruct> tempAdditems = new ArrayList<ItemCountStruct>();
		for (KMineral mineral : KDigMineralDataManager.mMineralDataManger.getDataCache().values()) {
			mineral.rwLock.lock();
			try {
				for (long roleId : mineral.getAllDiggerCache()) {
					tempAdditems.clear();
					KRole role = mRoleModuleSupport.getRole(roleId);
					if (role == null) {
						continue;
					}
					KRoleDiggerData roleData = getRoleDiggerData(roleId);
					if (roleData == null) {
						continue;
					}

					// [0]挖矿是否成功，[1]表示是否停止挖矿
					boolean[] result = new boolean[]{false, false};
					
					roleData.rwLock.lock();
					try {
						if (!mineral.containDigger(roleId)) {
							continue;
						}

						if (roleData.nextProduceTime > nowTime) {
							continue;
						}
						
						// 处理消耗、停止、产出、日志、提示
						result = tryToProduce(mCurrencyModuleSupport, mItemModuleSupport, role, mineral, roleData, nextProduceTime, tempAdditems);
						
						if (role.isOnline() && result[0] && !result[1]) {
							// 在线，稍后推送新的倒计时
							allPushCountDownRoleids.add(roleId);
						}

					} finally {
						roleData.rwLock.unlock();

						if (result[1]) {
							// 停止挖矿
							KPushMsg.synMineJob(roleId, -1);

							// 同步周围玩家
							KPushMsg.synMineStatusToArround(role, roleData);

						}

						if (result[0]) {
							// 成功挖矿
							// 活跃度
							KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.挖矿);

							// 世界广播
							for (ItemCountStruct item : tempAdditems) {
								KWordBroadcastType btype = KWordBroadcastType.挖矿_xx在挖矿中突然发现X个闪闪发光的XXXX;
								if (item.getItemTemplate().ItemType != KItemTypeEnum.改造材料) {
									String chatString = StringUtil.format(btype.content, role.getExName(), item.itemCount, item.getItemTemplate().extItemName);
									KSupportFactory.getChatSupport().sendSystemChat(chatString, btype);
								}
							}
						}
					}
				}
			} finally {
				mineral.rwLock.unlock();
			}
		}

		// 推送新的倒计时
		KPushMsg.pushCountDown(allPushCountDownRoleids);
	}
	
	/**
	 * <pre>
	 * 尝试挖矿
	 * 
	 * @param mCurrencyModuleSupport
	 * @param mItemModuleSupport
	 * @param role
	 * @param mineral
	 * @param roleData
	 * @param items
	 * @return [0]挖矿是否成功，[1]表示是否停止挖矿
	 * @author CamusHuang
	 * @creation 2014-12-19 上午10:26:29
	 * </pre>
	 */
	private static boolean[] tryToProduce(CurrencyModuleSupport mCurrencyModuleSupport,ItemModuleSupport mItemModuleSupport,
			KRole role, KMineral mineral, KRoleDiggerData roleData, long nextProduceTime, List<ItemCountStruct> tempAdditems){
		
		// 检查工具
		KItem tool = KItemLogic.searchItemFromBag(role.getId(), KDigMineralDataManager.铁镐ItemCode);
		if (tool == null || tool.getCount() < 1) {
			// 退出矿区
			getOutOfMineral(mineral, role, roleData, ActivityTips.x时间由于工具用完停止挖矿);
			return new boolean[]{false, true};
		}
		
		KMineGoldBaseData data = KDigMineralDataManager.mGoldBaseDataManger.getData(role.getLevel());
		long gold = mineral.template.getGold(data);
		tempAdditems = mineral.template.randomItem(data);

		{
			// 发奖
			// 背包满则停止
			if (!tempAdditems.isEmpty()) {
				ItemResult_AddItem addItemResult = mItemModuleSupport.addItemsToBag(role, tempAdditems, "挖矿");
				if (!addItemResult.isSucess) {
					// 退出矿区
					getOutOfMineral(mineral, role, roleData, ActivityTips.x时间由于背包已满停止挖矿);
					return new boolean[]{false, true};
				}
			}
	
			if (gold > 0) {
				mCurrencyModuleSupport.increaseMoney(role.getId(), KCurrencyTypeEnum.GOLD, gold, PresentPointTypeEnum.挖矿, true);
			}
		}

		// 下次产出时间
		roleData.nextProduceTime = nextProduceTime;

		// 扣工具
		mItemModuleSupport.removeItemFromBag(role.getId(), KDigMineralDataManager.铁镐ItemCode, 1);

		// 统计奖励
		roleData.recordForReward(role.isOnline(), gold, tempAdditems);

		{
			// 奖励日志，活动提示
			StringBuffer sbf = new StringBuffer();
			if (!tempAdditems.isEmpty()) {
				for (ItemCountStruct struct : tempAdditems) {
					String temp = StringUtil.format(ShopTips.xxx, struct.getItemTemplate().extItemName, struct.itemCount);
					sbf.append(temp).append(GlobalTips.顿号);
					KDialogService.sendUprisingDialog(role, StringUtil.format(ActivityTips.挖矿获得物品x, temp));
				}
			}
			if (gold > 0) {
				String temp = StringUtil.format(ShopTips.xxx, KCurrencyTypeEnum.GOLD.extName, gold);
				sbf.append(temp).append(GlobalTips.顿号);
				KDialogService.sendUprisingDialog(role, StringUtil.format(ActivityTips.挖矿获得货币x, temp));
			}

			KDialogService.sendUprisingDialog(role, StringUtil.format(ShopTips.x减x, tool.getItemTemplate().extItemName, 1));

			if (sbf.length() > 0) {
				// x:00挖矿获得了xxx;
				sbf.deleteCharAt(sbf.length() - 1);
				String dialy = StringUtil.format(ActivityTips.x时间挖矿获得了x剩余xxx, UtilTool.DATE_FORMAT11.format(new Date()), sbf.toString(), tool.getItemTemplate().extItemName,
						tool.getCount());
				roleData.addDialy(dialy);
				// 日志同步
				if (role.isOnline()) {
					KPushMsg.pushNewDialy(role.getId(), dialy);
				}
				// 今天奖励同步
				KPushMsg.pushTodayReward(role, roleData);
			}
		}
		
		if (tool.getCount() < 1) {
			// 退出矿区
			getOutOfMineral(mineral, role, roleData, ActivityTips.x时间由于工具用完停止挖矿);
			return new boolean[]{true, true};
		}
		
		return new boolean[]{true, false};
	}
	
	/**
	 * <pre>
	 * 退出矿区
	 * 
	 * @param mineral
	 * @param roleData
	 * @param reasonTips
	 * @author CamusHuang
	 * @creation 2014-12-19 上午10:14:15
	 * </pre>
	 */
	private static void getOutOfMineral(KMineral mineral, KRole role, KRoleDiggerData roleData, String reasonTips){
		// 退出矿区
		String dialy = StringUtil.format(reasonTips, UtilTool.DATE_FORMAT11.format(new Date()));
		roleData.addDialy(dialy);
		// 日志同步
		if (role.isOnline()) {
			KPushMsg.pushNewDialy(role.getId(), dialy);
			KDialogService.sendUprisingDialog(role, dialy);
		}

		mineral.removeDigger(roleData._roleId);
		roleData.setMineData(-1);
	}

	static class DigPVPAttachment {
		final long oppRoleId;
		final int mineId;
		final boolean isRevenge;

		DigPVPAttachment(long oppRoleId, int mineId, boolean isRevenge) {
			this.oppRoleId = oppRoleId;
			this.mineId = mineId;
			this.isRevenge = isRevenge;
		}
	}

	public static CommonResult dealMsg_gobackTown(KRole role) {
		CommonResult result = new CommonResult();
		//
		result.tips = rebackToGameWorld(role);
		return result;
	}

	private static String rebackToGameWorld(KRole role) {
		// 离开场景，返回游戏地图
		int dupMapId = KDigMineralDataManager.mineMap.getDuplicateId();
		KDuplicateMapBornPoint orgPoint = getDigPoint(role.getId());
		//
		KActionResult mapResult = KSupportFactory.getDuplicateMapSupport().playerRoleLeaveDuplicateMap(role, dupMapId);
		if (mapResult.success) {
			KRoleDiggerData roleData = KDigMineralActivityManager.getRoleDiggerData(role.getId());
			roleData.isInMap.set(false);
			// 角色在副本中的坐标
			roleData.digPoint.set(orgPoint);
			return null;
		} else {
			return mapResult.tips;
		}
	}

	static KDuplicateMapBornPoint getDigPoint(long roleId) {
		int dupMapId = KDigMineralDataManager.mineMap.getDuplicateId();
		KDuplicateMap<KMapEntityData> map = KSupportFactory.getDuplicateMapSupport().getDuplicateMap(dupMapId);
		if (map != null) {
			KMapEntityData<KRole> mapEntity = map.getEntity(KMapEntityTypeEnum.ENTITY_TYPE_PLAYERROLE, roleId);
			if (mapEntity != null) {
				return new KDuplicateMapBornPoint(0, 0, mapEntity.getCoordinate().getX(), mapEntity.getCoordinate().getY());
			}
		}
		return null;
	}

	/**
	 * <pre>
	 * 发送离线奖励统计邮件
	 * 
	 * @param role
	 * @param roleData
	 * @author CamusHuang
	 * @creation 2014-12-9 上午9:35:00
	 * </pre>
	 */
	static void sendOfflineRewardMail(KRole role, KRoleDiggerData roleData) {
		String mail = roleData.genAndClearOfflineRewardMail();
		if (mail != null) {
			KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(role.getId(), ActivityTips.离线挖矿结果邮件标题, mail);
		}
	}
}
