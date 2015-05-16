package com.kola.kmp.logic.fashion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.timer.Timer;

import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.item.impl.KAItemPack;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.fashion.KFashionDataStructs.KFashionTemplate;
import com.kola.kmp.logic.fashion.KRoleFashion.FashionData;
import com.kola.kmp.logic.fashion.message.KPushFashionsMsg;
import com.kola.kmp.logic.fashion.message.KSelectFashionMsg;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.PropertyTypeEnum;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemConfig;
import com.kola.kmp.logic.item.KItemModuleExtension;
import com.kola.kmp.logic.item.KItemSet;
import com.kola.kmp.logic.item.KItem.KItemCA.KItem_EquipmentData;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempEqui;
import com.kola.kmp.logic.other.KItemPackTypeEnum;
import com.kola.kmp.logic.other.KItemTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.ResultStructs.FashionResult_Buy;
import com.kola.kmp.logic.util.ResultStructs.FashionResult_Buys;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_Item;
import com.kola.kmp.logic.util.tips.FashionTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ItemTips;
import com.kola.kmp.logic.util.tips.ShopTips;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2014-3-15 上午11:58:28
 * </pre>
 */
public class KFashionLogic {

	// private static final KGameLogger _LOGGER =
	// KGameLogger.getLogger(KFashionLogic.class);

	static CommonResult addFashions(KRole role, List<Integer> fashionTempIds, String sourceTips) {
		CommonResult result = new CommonResult();
		//
		fashionTempIds = new ArrayList<Integer>(fashionTempIds);

		boolean isAdd = false;
		KRoleFashion set = KFasionRoleExtCACreator.getRoleFashion(role.getId());
		set.rwLock.lock();
		try {

			for (Iterator<Integer> it = fashionTempIds.iterator(); it.hasNext();) {
				int tempId = it.next();

				KFashionTemplate temp = KFashionDataManager.mFashionTemplateManager.getFashionTemplate(tempId);
				if (temp == null) {
					it.remove();
					continue;
				}
				
				if (temp.jobEnum != null && temp.job != role.getJob()) {
					continue;
				}

				FashionData fashionData = set.getFashionData(tempId);
				if (fashionData != null) {
					if (temp.effectTime <= 0) {
						continue;
					}
				}

				// 加时装或加时长
				fashionData = set.addFashion(temp);

				isAdd = true;

				// 财产日志
				FlowManager.logPropertyAddOrDelete(role.getId(), PropertyTypeEnum.时装, fashionData.getUUID(), temp.id, temp.name, true, sourceTips);
			}

			result.isSucess = true;
			return result;
		} finally {
			set.rwLock.unlock();

			if (isAdd) {
				// 同步全部时装
				KPushFashionsMsg.pushAllFashions(role);
				// 自动穿戴
				autoSelectFashionForMutil(role, fashionTempIds, true);
			}
		}
	}

	public static FashionResult_Buy dealMsg_buyFashion(KRole role, int fashionId, boolean isConfirm) {
		FashionResult_Buy result = new FashionResult_Buy();
		//
		KFashionTemplate temp = KFashionDataManager.mFashionTemplateManager.getFashionTemplate(fashionId);
		if (temp == null) {
			result.tips = FashionTips.时装不存在;
			return result;
		}

		if (temp.buyMoney==null) {
			result.tips = FashionTips.此时装不允许购买;
			return result;
		}

		if (temp.jobEnum != null && temp.job != role.getJob()) {
			result.tips = FashionTips.你的职业不适合购买此时装;
			return result;
		}

		FashionData fashionData = null;

		KRoleFashion set = KFasionRoleExtCACreator.getRoleFashion(role.getId());
		set.rwLock.lock();
		try {

			fashionData = set.getFashionData(fashionId);
			if (fashionData != null) {
				if (temp.effectTime <= 0) {
					result.tips = FashionTips.你已拥有此时装无须重复购买;
					return result;
				}
			}

			if (!isConfirm) {
				result.isGoConfirm = true;
				result.tips = StringUtil.format(FashionTips.是否花费x数量x货币购买x时装, temp.buyMoney.currencyCount, temp.buyMoney.currencyType.extName, temp.extName);
				return result;
			}

			// 扣货币
			long moneyResult = KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), temp.buyMoney, UsePointFunctionTypeEnum.购买时装, true);
			if (moneyResult < 0) {
				result.isGoMoneyUI = true;
				result.goMoneyUIType = temp.buyMoney.currencyType;
				result.goMoneyUICount = temp.buyMoney.currencyCount-KSupportFactory.getCurrencySupport().getMoney(role.getId(), temp.buyMoney.currencyType);
				result.tips = StringUtil.format(ShopTips.x货币数量不足x, temp.buyMoney.currencyType.extName, temp.buyMoney.currencyCount);
				return result;
			}

			// 加时装或加时长
			fashionData = set.addFashion(temp);

			//
			result.effectTime = ExpressionForFashionReleaseTime(fashionData, temp);
			//
			result.isSucess = true;
			result.tips = FashionTips.购买时装成功;
			result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, temp.buyMoney.currencyType.extName, temp.buyMoney.currencyCount));
			return result;
		} finally {
			set.rwLock.unlock();

			if (result.isSucess) {
				// 财产日志
				FlowManager.logPropertyAddOrDelete(role.getId(), PropertyTypeEnum.时装, fashionData.getUUID(), temp.id, temp.name, true, "直接购买");
			}
		}
	}

	public static FashionResult_Buys dealMsg_buyFashions(KRole role, List<Integer> fashionIds) {
		FashionResult_Buys result = new FashionResult_Buys();

		if (fashionIds.size() < 1) {
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			return result;
		}

		boolean isAdd = false;
		for (int fashionId : fashionIds) {

			KFashionTemplate temp = KFashionDataManager.mFashionTemplateManager.getFashionTemplate(fashionId);
			if (temp == null) {
				result.addUprisingTips(FashionTips.时装不存在);
				continue;
			}

			FashionResult_Buy tempResult = dealMsg_buyFashion(role, fashionId, true);
			if (tempResult.isGoMoneyUI) {
				result.isGoMoneyUI = tempResult.isGoMoneyUI;
				result.goMoneyUIType = tempResult.goMoneyUIType;
				result.goMoneyUICount = tempResult.goMoneyUICount;
			}
			if (tempResult.isGoVip) {
				result.isGoVip = tempResult.isGoVip;
				result.showVipTips = tempResult.showVipTips;
			}
			result.addDataUprisingTips(tempResult.getDataUprisingTips());
			result.addUprisingTips(tempResult.getUprisingTips());
			result.effectTimes.put(fashionId, tempResult.effectTime);

			if (tempResult.isSucess) {
				// 成功
				isAdd = true;
				result.addUprisingTips(StringUtil.format(FashionTips.购买时装x成功, temp.extName));
			} else {
				// 失败
				result.addUprisingTips(StringUtil.format(FashionTips.购买时装x失败原因x, temp.extName, tempResult.tips));
			}
		}

		result.isSucess = isAdd;
		return result;
	}

	/**
	 * <pre>
	 * 计算时装的剩余时长
	 * 
	 * @param fashionData
	 * @param temp
	 * @return （0表示永久,-1表示未购买）
	 * @author CamusHuang
	 * @creation 2014-6-17 上午10:01:14
	 * </pre>
	 */
	static long ExpressionForFashionReleaseTime(FashionData fashionData, KFashionTemplate temp) {
		if (fashionData == null) {
			return -1;
		}

		if (fashionData.getEndTime() <= 0) {
			return 0;
		}

		long releaseTime = 0;
		long nowTime = System.currentTimeMillis();
		if (fashionData.getEndTime() < nowTime) {
			releaseTime = Timer.ONE_MINUTE;
		} else {
			releaseTime = fashionData.getEndTime() - nowTime;
			if (releaseTime < Timer.ONE_MINUTE) {
				releaseTime = Timer.ONE_MINUTE;
			}
		}
		return releaseTime;
	}

	/**
	 * <pre>
	 * 
	 * @param role
	 * @param fashionId 必须为实际ID
	 * @param isSelected true穿戴，false卸载
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-15 下午3:22:48
	 * </pre>
	 */
	public static CommonResult_Ext dealMsg_selecteFashion(KRole role, int fashionId, boolean isSelected) {
		CommonResult_Ext result = new CommonResult_Ext();
		//
		KFashionTemplate temp = KFashionDataManager.mFashionTemplateManager.getFashionTemplate(fashionId);
		if (temp == null) {
			result.tips = FashionTips.时装不存在;
			return result;
		}

		if (temp.jobEnum != null && temp.job != role.getJob()) {
			result.tips = FashionTips.你的职业不适合穿着此时装;
			return result;
		}

		KRoleFashion set = KFasionRoleExtCACreator.getRoleFashion(role.getId());
		set.rwLock.lock();
		try {

			FashionData fashionData = set.getFashionData(fashionId);
			if (fashionData == null) {
				if(temp.buyMoney==null){
					result.tips = temp.catchDesc2;
					return result;
				} else {
					result.tips = FashionTips.你还没有购买此时装;
					return result;
				}
			}

			// set.setSelectedFashionId(fashionId);
			// Perry 修复，如果不是选取，需要把fashionId设为0，表示卸下
			if (isSelected) {
				set.setSelectedFashionId(fashionId);
			} else {
				if (set.getSelectedFashionId() == fashionId) {
					set.setSelectedFashionId(0);
				}
			}

			result.isSucess = true;
			if (isSelected) {
				result.tips = FashionTips.穿戴时装成功;
			} else {
				result.tips = FashionTips.卸载时装成功;
			}
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 本方法无调用，但仍然有效，可以随时使用
	 * 
	 * @param role
	 * @param fashionId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-6-23 上午10:14:57
	 * </pre>
	 */
	static CommonResult_Ext addFashion(KRole role, int fashionId, String sourceTips) {
		CommonResult_Ext result = new CommonResult_Ext();
		//
		KFashionTemplate temp = KFashionDataManager.mFashionTemplateManager.getFashionTemplate(fashionId);
		if (temp == null) {
			result = new ItemResult_Item();
			result.tips = FashionTips.时装不存在;
			return result;
		}

		FashionData fashionData = null;

		boolean isAdd = false;
		KRoleFashion set = KFasionRoleExtCACreator.getRoleFashion(role.getId());
		set.rwLock.lock();
		try {

			fashionData = set.getFashionData(fashionId);
			if (fashionData != null) {
				if (temp.effectTime <= 0) {
					result.isSucess = true;
					result.tips = FashionTips.添加时装成功;
					return result;
				}
			}

			// 加时装或加时长
			fashionData = set.addFashion(temp);

			isAdd = true;
			result.isSucess = true;
			result.tips = FashionTips.添加时装成功;
			return result;
		} finally {
			set.rwLock.unlock();

			if (isAdd) {
				// 同步全部时装
				KPushFashionsMsg.pushAllFashions(role);
				// 自动穿戴
				autoSelectFashionForMutil(role, Arrays.asList(fashionId), true);
				// 财产日志
				FlowManager.logPropertyAddOrDelete(role.getId(), PropertyTypeEnum.时装, fashionData.getUUID(), temp.id, temp.name, true, sourceTips);
			}
		}
	}

	/**
	 * <pre>
	 * 处理超时失效的时装
	 * 
	 * @param roleIdsCopy
	 * @author CamusHuang
	 * @creation 2014-7-1 下午12:39:54
	 * </pre>
	 */
	static void clearTimeOutFashion(KRole role, boolean isSynToClient) {
		KRoleFashion set = KFasionRoleExtCACreator.getRoleFashion(role.getId());
		if (set == null) {
			return;
		}
		int selectFashionId = set.getSelectedFashionId();
		List<FashionData> clearFashions = set.clearTimeOutFashion();
		boolean isSelectFahionIdChange = set.getSelectedFashionId() != selectFashionId;

		if (!clearFashions.isEmpty()) {
			// ====注意，本块代码的顺序不能随意改变======

			// 发邮件通知
			for (FashionData data : clearFashions) {
				KFashionTemplate temp = KFashionDataManager.mFashionTemplateManager.getFashionTemplate(data.tempId);
				if (temp == null) {
					continue;
				}
				KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(role.getId(), FashionTips.时装失效通知,
						StringUtil.format(FashionTips.时装x已过了有效期x, temp.extName, UtilTool.DATE_FORMAT.format(new Date(data.getEndTime()))));

				// 财产日志
				FlowManager.logPropertyAddOrDelete(role.getId(), PropertyTypeEnum.时装, data.getUUID(), temp.id, temp.name, false, "到期失效");
			}

			// 需要通知角色模块时装参数变更
			if (isSelectFahionIdChange) {
				updateForSelectFashionChange(role);
			}

			if (isSynToClient) {
				// 时装失效通知
				if (isSelectFahionIdChange) {
					KSelectFashionMsg.synSelectFashionToClient(role, selectFashionId, false);
				}

				// 同步全部时装
				KPushFashionsMsg.pushAllFashions(role);
			}

			// 自动穿戴
			if (isSelectFahionIdChange) {
				int targetTempId = set.searchMaxPowerTempId();
				if (targetTempId > 0) {
					autoSelectFashionForBuy(role, targetTempId, isSynToClient);
				}
			}

			// 筛选出战力比当前时装高的过期时装，提示续费：单个购买并自动穿戴（已经实现）；批量购买并自动穿戴？
			if (isSynToClient) {
				notifyForPay(role, set, clearFashions);
			}
		}
	}

	/**
	 * <pre>
	 * 筛选出战力比当前时装高的过期时装，提示续费：单个购买并自动穿戴（已经实现）；批量购买并自动穿戴？
	 * 
	 * @param role
	 * @param set
	 * @param clearFashions
	 * @author CamusHuang
	 * @creation 2014-10-28 下午12:00:24
	 * </pre>
	 */
	private static void notifyForPay(KRole role, KRoleFashion set, List<FashionData> clearFashions) {
		Set<Integer> fashionTempIdSet = new HashSet<Integer>();
		// 剔除不存在的，不符合职业要求的，不能购买的
		for (FashionData data : clearFashions) {
			KFashionTemplate temp = KFashionDataManager.mFashionTemplateManager.getFashionTemplate(data.tempId);
			if (temp == null) {
				continue;
			}
			if (temp.jobEnum != null && temp.job != role.getJob()) {
				continue;
			}
			if (temp.buyMoney==null) {
				continue;
			}
			fashionTempIdSet.add(temp.id);
		}

		if (fashionTempIdSet.isEmpty()) {
			return;
		}

		// 剔除比当前正在穿戴的时装战力要低的
		set.removeLowPowerTempIds(fashionTempIdSet);
		if (fashionTempIdSet.isEmpty()) {
			return;
		}

		//
		KPushFashionsMsg.pushFashionBuyDialog(role, fashionTempIdSet);
	}

	/**
	 * <pre>
	 * 尝试自动穿戴时装
	 * 指定了一些时装，如果当前时装没有指定时装战力高，则执行自动穿戴
	 * 
	 * @param role
	 * @param fashionTempIds
	 * @param isSynToClient
	 * @author CamusHuang
	 * @creation 2014-10-28 上午9:50:33
	 * </pre>
	 */
	public static void autoSelectFashionForMutil(KRole role, List<Integer> fashionTempIds, boolean isSynToClient) {
		
		if(role.getLevel() < KFashionConfig.getInstance().FashionAutoSelectedMinRoleLv){
			return;
		}
		
		Set<Integer> fashionTempIdSet = new HashSet<Integer>();
		for (int fashionTempId : fashionTempIds) {
			KFashionTemplate temp = KFashionDataManager.mFashionTemplateManager.getFashionTemplate(fashionTempId);
			if (temp == null) {
				continue;
			}
			if (temp.jobEnum != null && temp.job != role.getJob()) {
				continue;
			}
			fashionTempIdSet.add(fashionTempId);
		}

		int fashionTempId = 0;
		boolean isChange = false;
		KRoleFashion set = KFasionRoleExtCACreator.getRoleFashion(role.getId());
		set.rwLock.lock();
		try {
			if (fashionTempIdSet.contains(set.getSelectedFashionId())) {
				// 正在穿戴的时装
				return;
			}

			fashionTempId = set.searchTopPowerTempId(fashionTempIdSet);
			if (fashionTempId > 0) {
				set.setSelectedFashionId(fashionTempId);
				isChange = true;
			}
			return;
		} finally {
			set.rwLock.unlock();
			if (isChange) {
				// 时装变更后通知所有相关数值、显示更新
				updateForSelectFashionChange(role);
				// 同步给客户端
				if (isSynToClient) {
					KSelectFashionMsg.synSelectFashionToClient(role, fashionTempId, true);
				}
			}
		}
	}

	/**
	 * <pre>
	 * 购买时装，无视战力，直接穿戴
	 * 
	 * @param role
	 * @param fashionTempId
	 * @param isSynToClient
	 * @author CamusHuang
	 * @creation 2014-10-28 上午10:01:55
	 * </pre>
	 */
	public static void autoSelectFashionForBuy(KRole role, int fashionTempId, boolean isSynToClient) {
		
		if(role.getLevel() < KFashionConfig.getInstance().FashionAutoSelectedMinRoleLv){
			return;
		}

		KFashionTemplate temp = KFashionDataManager.mFashionTemplateManager.getFashionTemplate(fashionTempId);
		if (temp == null) {
			return;
		}
		if (temp.jobEnum != null && temp.job != role.getJob()) {
			return;
		}

		boolean isChange = false;
		KRoleFashion set = KFasionRoleExtCACreator.getRoleFashion(role.getId());
		set.rwLock.lock();
		try {
			if (set.getSelectedFashionId() == fashionTempId) {
				return;
			}

			FashionData fashionData = set.getFashionData(fashionTempId);
			if (fashionData == null) {
				return;
			}

			set.setSelectedFashionId(fashionTempId);
			isChange = true;
			return;
		} finally {
			set.rwLock.unlock();
			if (isChange) {
				// 时装变更后通知所有相关数值、显示更新
				updateForSelectFashionChange(role);
				// 同步给客户端
				if (isSynToClient) {
					KSelectFashionMsg.synSelectFashionToClient(role, fashionTempId, true);
				}
			}
		}
	}

	/**
	 * <pre>
	 * 时装变更后通知所有相关数值、显示更新
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-10-24 下午3:21:23
	 * </pre>
	 */
	public static void updateForSelectFashionChange(KRole role) {
		// 刷新角色属性
		KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(role.getId(), KFashionAttributeProvider.getType());
		// 刷新UI
		KSupportFactory.getMapSupport().notifyFashionStatus(role.getId(), KSupportFactory.getFashionModuleSupport().getFashingResId(role.getId()));
		// 刷新UI
		KSupportFactory.getRoleModuleSupport().updateFashionRes(role.getId());
		KSupportFactory.getTeamPVPSupport().notifyRoleFashionUpdate(role.getId());
	}

	/**
	 * <pre>
	 * 清理角色的指定时装
	 * 
	 * @param role
	 * @param keySet 指定的时装
	 * @return 清理的时装
	 * @author CamusHuang
	 * @creation 2014-11-19 上午12:44:39
	 * </pre>
	 */
	public static Set<Integer> clearIllegalFashion(KRole role, Set<Integer> keySet) {

		KRoleFashion set = KFasionRoleExtCACreator.getRoleFashion(role.getId());
		if (set == null) {
			return Collections.emptySet();
		}

		int selectFashionId = set.getSelectedFashionId();
		boolean isSelectFahionIdChange = false;

		set.rwLock.lock();
		try {
			Set<Integer> result = new HashSet<Integer>();
			for (Iterator<FashionData> it = set.getAllFashionsCacha().values().iterator(); it.hasNext();) {
				FashionData data = it.next();
				if (!keySet.contains(data.tempId)) {
					continue;
				}
				
				it.remove();
				result.add(data.tempId);
				if (selectFashionId == data.tempId) {
					set.setSelectedFashionId(0);
					isSelectFahionIdChange = true;
				}
				
				// 财产日志
				FlowManager.logPropertyAddOrDelete(role.getId(), PropertyTypeEnum.时装, data.getUUID(), data.tempId, "", false, "改版清除");
			}
			if (!result.isEmpty()) {
				set.notifyDB();
			}
			return result;
		} finally {
			set.rwLock.unlock();

			if (isSelectFahionIdChange) {
				// 需要通知角色模块时装参数变更
				updateForSelectFashionChange(role);

				// 自动穿戴
				int targetTempId = set.searchMaxPowerTempId();
				if (targetTempId > 0) {
					autoSelectFashionForBuy(role, targetTempId, false);
				}
			}
		}
	}
}
