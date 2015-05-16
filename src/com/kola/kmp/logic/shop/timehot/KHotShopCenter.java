package com.kola.kmp.logic.shop.timehot;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.FlowDataModuleFactory;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KShopTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.shop.KShopRoleExtCACreator;
import com.kola.kmp.logic.shop.timehot.KHotShopDataManager.HotShopManager.HotGoods;
import com.kola.kmp.logic.shop.timehot.KHotShopDataManager.HotShopManager.HotShop;
import com.kola.kmp.logic.shop.timehot.KHotShopTaskManager.TimeLimitActivityTaskDataManager;
import com.kola.kmp.logic.shop.timehot.message.KPushHotGoodsMsg;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.ResultStructs.HotShopResultExt;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.tips.ItemTips;
import com.kola.kmp.logic.util.tips.ShopTips;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2014-4-16 下午5:26:07
 * </pre>
 */
public class KHotShopCenter {

	public static final Logger _LOGGER = KGameLogger.getLogger(KHotShopCenter.class);

	public static void notifyCacheLoadComplete() throws KGameServerException {
		HotShopGlobalDataImpl.instance.load();
		//
		KHotShopDataManager.notifyCacheLoadComplete();
		//
		KHotShopTaskManager.notifyCacheLoadComplete();
	}
	
	public static void serverShutdown() throws KGameServerException {
		HotShopGlobalDataImpl.instance.save();
	}

	/**
	 * <pre>
	 * 仅在跨天时调用，处理在线角色
	 * 
	 * @author CamusHuang
	 * @creation 2014-12-31 上午9:53:17
	 * </pre>
	 */
	static void notifyForDayChange() {

		long nowTime = System.currentTimeMillis();

		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
		for (long roleId : roleSupport.getAllOnLineRoleIds()) {
			TimeHotShopData roleData = KShopRoleExtCACreator.getRoleTimeHotShopData(roleId);
			if (roleData == null) {
				continue;
			}
			//
			roleData.rwLock.lock();
			try {
				// 跨天
				roleData.notifyForDayChange(nowTime);
				//
				correctGoodsBuyTimesForRole(roleData, false);
			} finally {
				roleData.rwLock.unlock();
			}
		}
	}

	public static void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		//
		try {
			TimeHotShopData roleData = KShopRoleExtCACreator.getRoleTimeHotShopData(role.getId());
			if (roleData == null) {
				return;
			}

			roleData.rwLock.lock();
			try {
				// 是否跨天（内部会记录当前时间）
				boolean isAnotherDay = roleData.isAnotherDay();
				//
				if(isAnotherDay){
					correctGoodsBuyTimesForRole(roleData, true);
				}
			} finally {
				roleData.rwLock.unlock();
			}
		} finally {
			// 推送商品列表
			KPushHotGoodsMsg.instance.pushMsg(role);
		}
	}

	public static void notifyRoleLevelUp(KRole role, int preLv) {
		//
		TimeHotShopData roleData = KShopRoleExtCACreator.getRoleTimeHotShopData(role.getId());
		if (roleData == null) {
			return;
		}
		
		boolean isChange = false;
		for (int lv = preLv; lv <= role.getLevel(); lv++) {
			if (KHotShopDataManager.mHotShopManager.isGoodsChangeForRoleLv(lv)) {
				isChange = true;
				break;
			}
		}
		if (isChange) {
			KPushHotGoodsMsg.instance.pushMsg(role);
		}
	}

	public static String reloadData() {
		try {
			KHotShopDataManager.reloadData(true);
			//
			TimeLimitActivityTaskDataManager.restartAllActivityTast();
			//
			KPushHotGoodsMsg.instance.pushToAllOnlineRole();
			return "加载完成";
		} catch (Exception e) {
			KHotShopCenter._LOGGER.error(e.getMessage(), e);
			return "发生异常：" + e.getMessage();
		}
	}

	static void nofityGoodsStartOrEnd() {
		_LOGGER.warn("限时热购商品起始或结束时间生效");
		//
		KPushHotGoodsMsg.instance.pushToAllOnlineRole();
	}

	/**
	 * <pre>
	 * 登陆、跨天、升级、商品开始、商品结束、重加载
	 * 
	 * @param roleData
	 * @param roleLv
	 * @param isAnotherDay
	 * @param isForLogin
	 * @author CamusHuang
	 * @creation 2014-12-31 上午10:47:07
	 * </pre>
	 */
	private static void correctGoodsBuyTimesForRole(TimeHotShopData roleData, boolean isForLogin) {
		// 重置每日次数
		boolean hasDoSomeThing = false;
		for(KHotShopTypeEnum type:KHotShopTypeEnum.values()){
			HotShop shop = KHotShopDataManager.mHotShopManager.allShopMap.get(type);
			Map<Integer, AtomicInteger> goodsCountMap = roleData.getGoodsBuyTimesCache(type);
			for (Iterator<Entry<Integer, AtomicInteger>> it = goodsCountMap.entrySet().iterator(); it.hasNext();) {
				Entry<Integer, AtomicInteger> e = it.next();
				HotGoods goods = shop.getGoods(e.getKey());
				if (goods == null) {
					it.remove();
					hasDoSomeThing = true;
					continue;
				}
				if (goods.buyTimeForRoleIsDay) {
					e.getValue().set(0);
					hasDoSomeThing = true;
				}
			}
		}
		
		if (hasDoSomeThing) {
			// 保存
			roleData.owner.notifyUpdate();
			if (!isForLogin) {
				KPushHotGoodsMsg.instance.pushMsg(roleData.owner.getRoleId());
			}
		}
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param role
	 * @param type
	 * @param goodsId
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-16 下午3:13:07
	 * </pre>
	 */
	public static HotShopResultExt dealMsg_buyHotGoods(KRole role, byte type, int goodsId) {

		HotShopResultExt result = new HotShopResultExt();

		if (!KHotShopDataManager.mHotShopManager.isActivityOpen) {
			result.tips = ShopTips.此活动未开放;
			return result;
		}
		
		KHotShopTypeEnum typeEnum = KHotShopTypeEnum.getEnum(type);
		if(typeEnum==null){
			result.tips = ShopTips.不存在此商品;
			return result;
		}
		
		HotShop hotShop = KHotShopDataManager.mHotShopManager.allShopMap.get(typeEnum);
		if(hotShop==null){
			result.tips = ShopTips.不存在此商品;
			return result;
		}
		//
		HotGoods goods = hotShop.getGoods(goodsId);
		if(goods==null){
			result.tips = ShopTips.不存在此商品;
			return result;
		}
		
		long nowTime = System.currentTimeMillis();
		long todayTime = UtilTool.getTodayStart().getTimeInMillis();
		
		if(!goods.isActivityTakeEffectNow(nowTime, todayTime)){
			result.tips = ShopTips.此商品已下架;
			return result;
		}
		
		if (role.getLevel() < goods.mixlvl || role.getLevel() > goods.maxlvl) {
			result.tips = ShopTips.此商品已下架;
			return result;
		}

		TimeHotShopData roleData = KShopRoleExtCACreator.getRoleTimeHotShopData(role.getId());
		roleData.rwLock.lock();
		try {
			{
				result.isFind = true;
				// 个人次数检查
				int buyedTime = roleData.getGoodsBuyedTime(typeEnum, goodsId);
				if (buyedTime < goods.buyTimeForRole) {
					result.releaseTime = goods.buyTimeForRole - buyedTime;
				}
				// 全服购买次数检查
				if (goods.buyTimeForWorld > 0) {
					// 有限次数
					int globalCount = HotShopGlobalDataImpl.instance.getCount(goodsId);
					if (globalCount < goods.buyTimeForWorld) {
						result.releaseWorldTime = goods.buyTimeForWorld - globalCount;
					}
				} else {
					result.releaseWorldTime = -1;
				}

				if (result.releaseTime < 1) {
					if (goods.buyTimeForRoleIsDay) {
						result.tips = ShopTips.此商品今天购买次数已达极限;
					} else {
						result.tips = ShopTips.此商品购买次数已达极限;
					}
					return result;
				}

				if (result.releaseWorldTime == 0) {
					result.tips = ShopTips.此商品已售磬;
					return result;
				}
			}

			KSupportFactory.getItemModuleSupport().lockItemSet(role.getId());
			try {

				if (!KSupportFactory.getItemModuleSupport().isCanAddItemsToBag(role.getId(), Arrays.asList(goods.itemStruct))) {
					result.tips = ItemTips.背包容量不足;
					return result;
				}

				// 扣费
				if (0 > KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), goods.price, UsePointFunctionTypeEnum.购买系统道具, true)) {
					result.isGoMoneyUI = true;
					result.goMoneyUIType = goods.price.currencyType;
					result.goMoneyUICount = goods.price.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), goods.price.currencyType);
					result.tips = StringUtil.format(ShopTips.x货币数量不足x, goods.price.currencyType.extName, goods.price.currencyCount);
					return result;
				}

				ItemResult_AddItem addResult = KSupportFactory.getItemModuleSupport().addItemToBag(role, goods.itemStruct, KShopTypeEnum.热购商店.name());
				if (!addResult.isSucess) {
					// 回滚货币
					KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), goods.price, PresentPointTypeEnum.回滚, true);
					//
					result.tips = addResult.tips;
					return result;
				}

				// 记录流水
				if (goods.price.currencyType == KCurrencyTypeEnum.DIAMOND) {
					long itemId = 0;
					if (addResult.newItemList != null && !addResult.newItemList.isEmpty()) {
						itemId = addResult.newItemList.get(0).getId();
					} else if (addResult.updateItemCountList != null && !addResult.updateItemCountList.isEmpty()) {
						itemId = addResult.updateItemCountList.get(0).getId();
					}
					FlowDataModuleFactory.getModule().recordBuyItemUsePoint(role, goods.itemStruct.itemCode, goods.itemStruct.getItemTemplate().name, itemId, (int) goods.itemStruct.itemCount,
							(int) goods.price.currencyCount, KShopTypeEnum.热购商店.sign);
				}

			} finally {
				KSupportFactory.getItemModuleSupport().unlockItemSet(role.getId());
			}
			//
			// 全服购买次数记录
			if (goods.buyTimeForWorld > 0) {
				HotShopGlobalDataImpl.instance.increaseCount(goodsId);
			}
			//
			roleData.notifyBuyedGoods(typeEnum, goodsId);
			result.releaseTime = goods.buyTimeForRole - roleData.getGoodsBuyedTime(typeEnum, goodsId);
			//
			result.isSucess = true;
			result.tips = ShopTips.购买成功;
			//
			result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, goods.price.currencyType.extName, goods.price.currencyCount));
			result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, goods.itemStruct.getItemTemplate().extItemName, goods.itemStruct.itemCount));
			//
			// 财产日志
			String tips = StringUtil.format("模板ID:{};物品名称:{};数量:{};货币类型:{};价格:{}", goods.itemStruct.itemCode, goods.itemStruct.getItemTemplate().name, goods.itemStruct.itemCount,
					goods.price.currencyType.sign, goods.price.currencyCount);
			FlowManager.logOther(role.getId(), OtherFlowTypeEnum.买热购商品, tips);
			return result;
		} finally {
			roleData.rwLock.unlock();
		}
	}
}
