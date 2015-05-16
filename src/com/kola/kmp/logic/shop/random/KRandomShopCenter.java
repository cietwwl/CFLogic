package com.kola.kmp.logic.shop.random;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.FlowDataModuleFactory;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KShopTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.shop.KShopRoleExtCACreator;
import com.kola.kmp.logic.shop.random.KRandomShopDataManager.RandomGoodsManager;
import com.kola.kmp.logic.shop.random.KRandomShopDataManager.RandomGoodsManager.RandomGoods;
import com.kola.kmp.logic.shop.random.KRandomShopTaskManager.KRandomShopHourTask;
import com.kola.kmp.logic.shop.random.message.KPushRandomGoodsMsg;
import com.kola.kmp.logic.shop.random.message.KPushRandomShopConstanceMsg;
import com.kola.kmp.logic.shop.random.message.KRefreshRandomShopLogsMsg;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KSimpleDialyManager;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.ResultStructs.RandomShopResultExt;
import com.kola.kmp.logic.util.tips.ItemTips;
import com.kola.kmp.logic.util.tips.ShopTips;

/**
 * <pre>
 * 1.手工刷新：免费2次，钻石刷新无限
 * 2.钻石刷新动态价格、二次确认
 * 3.自动刷新：倒计时
 * 4.每种限购买一次
 * 5.购买日志
 * 
 * @author CamusHuang
 * @creation 2014-4-16 下午5:26:07
 * </pre>
 */
public class KRandomShopCenter {

	public static final Logger _LOGGER = KGameLogger.getLogger(KRandomShopCenter.class);

	// 购买日志:xxx购买了xxx x件 xx元宝
	static KSimpleDialyManager dialyManager;

	public static void notifyCacheLoadComplete() throws KGameServerException{
		dialyManager = new KSimpleDialyManager(KRandomShopDataManager.randomShopDailySaveDir, KRandomShopDataManager.randomShopDailySaveFileName);
		dialyManager.loadDialys();
		
		KRandomShopTaskManager.notifyCacheLoadComplete();
		
		KRandomShopDataManager.notifyCacheLoadComplete();
		
		//
		KRandomShopTypeEnum.checkType();
	}
	
	public static void serverShutdown() throws KGameServerException {
		dialyManager.saveDialys();
	}
	
	public static void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		if (role.getLevel() >= KRandomShopDataManager.RandomMinRoleLv) {
			initRandomShopForRole(role);
		}
	}
	
	public static void notifyRoleLevelUp(KRole role, int preLv) {
		if (preLv < KRandomShopDataManager.RandomMinRoleLv && KRandomShopDataManager.RandomMinRoleLv <= role.getLevel()) {
			initRandomShopForRole(role);
		}
	}
	
	/**
	 * <pre>
	 * 角色登陆时，若已达到随机商店等级，则调用本方法
	 * 角色升级时，若从未达进随机商店等级，则调用本方法
	 * 
	 * @author CamusHuang
	 * @creation 2014-4-19 上午11:29:45
	 * </pre>
	 */
	private static void initRandomShopForRole(KRole role) {
		long nowTime = System.currentTimeMillis();
		long nextSystemRefreshTime =  KRandomShopHourTask.instance.getNextRunTime();
		
		KRoleRandomData roleData = KShopRoleExtCACreator.getRoleRandomData(role.getId());
		// 若未有随机商品数据，则刷新
		if (roleData.getRandomGoodsCache().isEmpty() || //
				roleData.getLastSystemRereshTime() + KRandomShopHourTask.instance.getPeriodInMills() <= nextSystemRefreshTime) {
			// 商品数据为null，或者超过一个周期未进行系统刷新，则全刷
			KRandomShopCenter.refreshRandomGoods(role, roleData, null);
			roleData.setLastSystemRereshTime(nowTime);
		}
		//
		{
			long nextRefreshDelayTime = nextSystemRefreshTime - nowTime;
			if (nextRefreshDelayTime < Timer.ONE_MINUTE) {
				nextRefreshDelayTime = Timer.ONE_MINUTE;
			}
			KPushRandomShopConstanceMsg.sendMsg(role.getId(), nextRefreshDelayTime);
			KPushRandomGoodsMsg.pushMsg(role.getId(), null);
			KRefreshRandomShopLogsMsg.pushMsg(role, -1);
		}
	}	
	
	
	
	/**
	 * <pre>
	 * 玩家手动刷新
	 * 
	 * @param role
	 * @param nowGoodsType
	 * @param isConfirm
	 * @return
	 * @author CamusHuang
	 * @creation 2014-8-1 下午3:41:14
	 * </pre>
	 */
	public static RandomShopResultExt dealMsg_refreshRandomShop(KRole role, int nowGoodsType, boolean isConfirm) {

		RandomShopResultExt result = new RandomShopResultExt();

		KRoleRandomData roleData = KShopRoleExtCACreator.getRoleRandomData(role.getId());
		roleData.rwLock.lock();
		try {
			int nextTime = roleData.getRandomTime() + 1;
			if (nextTime > KRandomShopDataManager.RandomFreeTime) {
				// 付费模式
				int nextPayTime = nextTime - KRandomShopDataManager.RandomFreeTime;
				KCurrencyCountStruct price = ExpressionForRandomPrice(nextPayTime);
				if (!isConfirm) {
					// 要求二次确认
					result.isGoConfirm = true;
					result.goConfirmPrice = price;
					return result;
				}
				// 扣费
				if (0 > KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), price, UsePointFunctionTypeEnum.刷新随机商店, true)) {
					result.isGoMoneyUI = true;
					result.goMoneyUIType = price.currencyType;
					result.goMoneyUICount = price.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), price.currencyType);
					result.tips = StringUtil.format(ShopTips.x货币数量不足x, price.currencyType.extName, price.currencyCount);
					return result;
				}

				result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, price.currencyType.extName, price.currencyCount));

				// 全类型刷新
				refreshRandomGoods(role, roleData, null);
			} else {
				// 免费模式：只刷新指定类型的商品
				KRandomShopTypeEnum nowGoodsTypeEnum = KRandomShopTypeEnum.getEnum(nowGoodsType);
				if (nowGoodsTypeEnum == null) {
					result.tips = ShopTips.不存在此类型的商品;
					return result;
				}
				
				RandomGoodsManager manager = KRandomShopDataManager.mRandomGoodsManager.get(nowGoodsTypeEnum);
				if (manager == null) {
					result.tips = ShopTips.不存在此类型的商品;
					return result;
				}
				refreshRandomGoods(role, roleData, nowGoodsTypeEnum);
				result.freeRefreshTypeEnum = nowGoodsTypeEnum;
			}

			roleData.setRandomTime(nextTime);
			//
			result.isSucess = true;
			result.tips = ShopTips.成功刷新商品;

			return result;
		} finally {
			roleData.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 无条件刷新商品
	 * 
	 * @param role
	 * @param randomData
	 * @param nowGoodsTypeEnum
	 * @author CamusHuang
	 * @creation 2014-4-19 上午10:50:52
	 * </pre>
	 */
	public static void refreshRandomGoods(KRole role, KRoleRandomData randomData, KRandomShopTypeEnum nowGoodsTypeEnum) {
		randomData.rwLock.lock();
		try {
			if (nowGoodsTypeEnum == null) {
				// 全类型刷新
				for (Entry<KRandomShopTypeEnum, RandomGoodsManager> entry : KRandomShopDataManager.mRandomGoodsManager.entrySet()) {
					RandomGoodsManager manager = entry.getValue();
					LinkedHashMap<Integer, RandomGoods> data = manager.randomGoods(role.getLevel());
					randomData.setRandomGoods(entry.getKey(), data);
				}
			} else {
				// 免费模式：只刷新指定类型的商品
				RandomGoodsManager manager = KRandomShopDataManager.mRandomGoodsManager.get(nowGoodsTypeEnum);
				if (manager == null) {
					return;
				}
				LinkedHashMap<Integer, RandomGoods> data = manager.randomGoods(role.getLevel());
				randomData.setRandomGoods(nowGoodsTypeEnum, data);
			}
		} finally {
			randomData.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 计算第N次付费刷新的价格
	 * 
	 * @param time 不包含免费次数
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-16 下午6:26:26
	 * </pre>
	 */
	private static KCurrencyCountStruct ExpressionForRandomPrice(long time) {
		// （int(次数/5）+1）*50
//		return ((time / 5) + 1) * 50;
		
		
//		lijs(李君松) 12-23 17:01:18
//		随机商店刷新价格调整为恒定10钻
//		http://workserver/mantis/view.php?id=2009
		return KRandomShopDataManager.RandomRefreshPrice;
	}

	public static CommonResult_Ext dealMsg_buyRandomGoods(KRole role, int nowGoodsType, int goodsId) {

		CommonResult_Ext result = new CommonResult_Ext();

		KRandomShopTypeEnum nowGoodsTypeEnum = KRandomShopTypeEnum.getEnum(nowGoodsType);
		if (nowGoodsTypeEnum == null) {
			result.tips = ShopTips.不存在此类型的商品;
			return result;
		}
		
		int roleVipLv = 0;
		if(nowGoodsTypeEnum==KRandomShopTypeEnum.VIP商城){
			roleVipLv = KSupportFactory.getVIPModuleSupport().getVipLv(role.getId());
		}

		KRoleRandomData roleData = KShopRoleExtCACreator.getRoleRandomData(role.getId());
		roleData.rwLock.lock();
		try {

			LinkedHashMap<Integer, RandomGoods> map = roleData.getRandomGoodsCache().get(nowGoodsTypeEnum);
			RandomGoods goods = map.get(goodsId);
			if (goods == null) {
				result.tips = ShopTips.不存在此商品;
				return result;
			}

			if (roleData.isBuyedRandomGoods(nowGoodsTypeEnum, goodsId)) {
				result.tips = ShopTips.不能重复购买此商品请刷新后再来;
				return result;
			}
			
			if (nowGoodsTypeEnum == KRandomShopTypeEnum.VIP商城) {
				if (roleVipLv < goods.mixVIPLvl) {
					result.tips = ShopTips.VIP等级不足无法购买;
					return result;
				}
			}

			KSupportFactory.getItemModuleSupport().lockItemSet(role.getId());
			try {

				if (!KSupportFactory.getItemModuleSupport().isCanAddItemsToBag(role.getId(), Arrays.asList(goods.itemStruct))) {
					result.tips = ItemTips.背包已满;
					return result;
				}

				// 扣费
				if (0 > KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), goods.salePrice, UsePointFunctionTypeEnum.购买系统道具, true)) {
					result.isGoMoneyUI = true;
					result.goMoneyUIType = goods.salePrice.currencyType;
					result.goMoneyUICount = goods.salePrice.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), goods.salePrice.currencyType);
					result.tips = StringUtil.format(ShopTips.x货币数量不足x, goods.salePrice.currencyType.extName, goods.salePrice.currencyCount);
					return result;
				}

				ItemResult_AddItem addResult = KSupportFactory.getItemModuleSupport().addItemToBag(role, goods.itemStruct, KShopTypeEnum.随机商店.name());
				if (!addResult.isSucess) {
					// 回滚货币
					KSupportFactory.getCurrencySupport().increaseMoney(role.getId(), goods.salePrice, PresentPointTypeEnum.回滚, true);
					//
					result.tips = addResult.tips;
					return result;
				}

				// 记录流水
				if (goods.salePrice.currencyType == KCurrencyTypeEnum.DIAMOND) {
					long itemId = 0;
					if (addResult.newItemList != null && !addResult.newItemList.isEmpty()) {
						itemId = addResult.newItemList.get(0).getId();
					} else if (addResult.updateItemCountList != null && !addResult.updateItemCountList.isEmpty()) {
						itemId = addResult.updateItemCountList.get(0).getId();
					}
					FlowDataModuleFactory.getModule().recordBuyItemUsePoint(role, goods.itemStruct.itemCode, goods.itemStruct.getItemTemplate().name, itemId, (int) goods.itemStruct.itemCount,
							(int) goods.salePrice.currencyCount, KShopTypeEnum.随机商店.sign);
				}

			} finally {
				KSupportFactory.getItemModuleSupport().unlockItemSet(role.getId());
			}
			//
			roleData.notifyBuyedRandomGoods(nowGoodsTypeEnum, goodsId);
			String dialy = StringUtil.format(ShopTips.x购买了x件x, role.getExName(), goods.itemStruct.getItemTemplate().extItemName, goods.itemStruct.itemCount);
			dialyManager.addDialy(dialy);
			//
			result.isSucess = true;
			result.tips = ShopTips.购买成功;
			//
			result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, goods.salePrice.currencyType.extName, goods.salePrice.currencyCount));
			result.addDataUprisingTips(StringUtil.format(ShopTips.x加x, goods.itemStruct.getItemTemplate().extItemName, goods.itemStruct.itemCount));
			//
			// 财产日志
			String tips = StringUtil.format("模板ID:{};物品名称:{};数量:{};货币类型:{};价格:{}", goods.itemStruct.itemCode, goods.itemStruct.getItemTemplate().name,
					goods.itemStruct.itemCount, goods.salePrice.currencyType.sign, goods.salePrice.currencyCount);
			FlowManager.logOther(role.getId(), OtherFlowTypeEnum.买随机物品, tips);
			return result;
		} finally {
			roleData.rwLock.unlock();
		}
	}
}
