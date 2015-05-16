package com.kola.kmp.logic.shop;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.exception.KGameServerException;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kmp.logic.KGameGlobalConfig;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemDataManager;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempConsume;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.item.message.KPushItemsMsg;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KItemTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_Use;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ShopTips;
import com.kola.kmp.logic.vip.KVIPDataManager;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;

/**
 * <pre>
 * 体力值购买
 * 
 * @author CamusHuang
 * @creation 2013-6-4 下午8:21:57
 * </pre>
 */
public class PhyPowerShopCenter {

	// 体力道具ItemCode
	private static List<String> phyPowItemCodeList = new ArrayList<String>();

	public static void notifyCacheLoadComplete() throws KGameServerException {

		// 从道具模板中搜索所有加体力的消耗类道具
		for (KItemTempAbs temp : KItemDataManager.mItemTemplateManager.getItemTemplateList()) {
			if (temp.ItemType == KItemTypeEnum.消耗品) {
				KItemTempConsume temp2 = (KItemTempConsume) temp;
				if (temp2.addAtt != null && temp2.addAtt.roleAttType == KGameAttrType.PHY_POWER) {
					phyPowItemCodeList.add(temp.itemCode);
				}
			}
		}

		if (phyPowItemCodeList.isEmpty()) {
			throw new KGameServerException("不存在体力消耗类道具");
		}
	}

	/**
	 * <pre>
	 * 角色的最大体力购买次数
	 * 
	 * @deprecated 未被使用的方法
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-6-4 下午8:27:37
	 * </pre>
	 */
	public static int getMaxBuyTime(long roleId) {
		VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(roleId);
		return vipData.fatbuyrmb.length;
	}

	/**
	 * <pre>
	 * 剩余可购买次数
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-6-5 下午5:18:35
	 * </pre>
	 */
	public static int getBuyReleaseTime(long roleId) {
		KRoleShop shop = KShopRoleExtCACreator.getRoleShop(roleId);
		shop.rwLock.lock();
		try {
			// 当天购买次数检查
			int buyTime = shop.getPhyPowerBuyTime();

			// 剩余次数
			VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(roleId);

			return Math.max(0, vipData.fatbuyrmb.length - buyTime);
		} finally {
			shop.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 显示购买体力二次确认界面
	 * 玩家确认后执行{@link #confirmBuyPhyPower(KGamePlayerSession)}
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2013-6-27 下午3:23:50
	 * </pre>
	 */
	public static void showBuyPhyPowerDialog(KRole role) {
		long roleId = role.getId();
		KRoleShop shop = KShopRoleExtCACreator.getRoleShop(roleId);
		shop.rwLock.lock();
		try {
			// 当天购买次数检查
			int buyTime = shop.getPhyPowerBuyTime();

			// 剩余次数
			VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(roleId);
			int releaseTime = vipData.fatbuyrmb.length - buyTime;

			// 检查体力是否已满、购买次数是否用光
			if (!checkCanGoBuyPhyPower(role, vipData.lvl, releaseTime)) {
				return;
			}
			
			int ingotCount = vipData.fatbuyrmb[buyTime];
			int powfull = KSupportFactory.getRoleModuleSupport().getPhyPowerFullSize();
			// 发送菜单，消费二次确认
			List<KDialogButton> buttons = new ArrayList<KDialogButton>();
			buttons.add(KDialogButton.CANCEL_BUTTON);
			buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_BUY_PHYPOW, "", KDialogButton.CONFIRM_DISPLAY_TEXT));
			KDialogService.sendFunDialog(role, "", StringUtil.format(ShopTips.是否花费x数量x货币增加x体力, ingotCount, KCurrencyTypeEnum.DIAMOND.extName, powfull), buttons, true, (byte) -1);

		} finally {
			shop.rwLock.unlock();
		}
	}

	private static boolean checkCanGoBuyPhyPower(KRole role, int vipLv, int releaseTime) {

		boolean isFull = KSupportFactory.getRoleModuleSupport().isPhyPowerFull(role);
		if (isFull) {
			KDialogService.sendSimpleDialog(role, "", ShopTips.体力值已满无需补充);
			return false;
		}

		{
			if (releaseTime < 1) {
				// 次数已达极限且VIP已达极限
				if (vipLv >= KVIPDataManager.mVIPLevelDataManager.getMaxLevel().lvl || !KGameGlobalConfig.isVipEnable()) {
					KDialogService.sendSimpleDialog(role, "", ShopTips.体力值购买次数已达极限);
				} else {
					// 询问跳转到VIP界面
					KDialogService.showVIPDialog(role.getId(), ShopTips.今日次数已用完提升VIP等级可以增加次数是否前去提升);
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * <pre>
	 * 先显示体力购买二次确认对话框 {@link #showBuyPhyPowerDialog(KRole)}
	 * 玩家二次确认后执行本方法进行购买
	 * 
	 * 购买体力流程：
	 * CM请求-->次数不足-->提示次数不足-->确认则跳转到VIP界面
	 * CM请求-->元宝不足-->提示元宝不足-->确认则跳转到充值界面
	 * CM请求-->购买二次确认-->确认则购买-->结果提示
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2013-6-27 下午3:23:50
	 * </pre>
	 */
	public static void confirmBuyPhyPower(KGamePlayerSession session) {

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.服务器繁忙请稍候再试);
			return;
		}

		long roleId = role.getId();
		KRoleShop shop = KShopRoleExtCACreator.getRoleShop(roleId);
		shop.rwLock.lock();
		try {
			// 当天购买次数检查
			int buyTime = shop.getPhyPowerBuyTime();

			// 剩余次数
			VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(roleId);
			int releaseTime = vipData.fatbuyrmb.length - buyTime;

			// 检查体力是否已满、购买次数是否用光
			if (!checkCanGoBuyPhyPower(role, vipData.lvl, releaseTime)) {
				return;
			}

			// 扣货币
			int ingotCount = vipData.fatbuyrmb[buyTime]; // index需要-1，所以如果是获取index的话，应该用buyTime（代表buyTime+1的价格）
			if (KSupportFactory.getCurrencySupport().decreaseMoney(roleId, KCurrencyTypeEnum.DIAMOND, ingotCount, UsePointFunctionTypeEnum.购买体力, true) < 0) {
				// 询问跳转到充值界面
				KDialogService.showChargeDialog(roleId, ShopTips.您的钻石不足是否前去充值);
				return;
			}

			// 加体力，加购买次数
			int powfull = KSupportFactory.getRoleModuleSupport().getPhyPowerFullSize();
			KSupportFactory.getRoleModuleSupport().addPhyPower(roleId, powfull, true, "购买");
			shop.setPhyPowerBuyTime(buyTime + 1);

			KDialogService.sendSimpleDialog(roleId, ShopTips.购买成功, StringUtil.format(ShopTips.成功使用x数量x货币增加x体力, ingotCount, KCurrencyTypeEnum.DIAMOND.extName, powfull));

//			KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.体力购买);

		} finally {
			shop.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 执行购买流程
	 * 
	 * @deprecated 未被使用的方法
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-6-27 下午4:17:50
	 * </pre>
	 */
	public static CommonResult buyPhyPowerForScenario(long roleId) {
		CommonResult result = new CommonResult();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role == null) {
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			return result;
		}

		KRoleShop shop = KShopRoleExtCACreator.getRoleShop(roleId);
		shop.rwLock.lock();
		try {
			// 当天购买次数检查
			int buyTime = shop.getPhyPowerBuyTime();

			// 剩余次数
			VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(roleId);
			int releaseTime = vipData.fatbuyrmb.length - buyTime;

			// 检查体力是否已满、购买次数是否用光
			if (KSupportFactory.getRoleModuleSupport().isPhyPowerFull(role)) {
				result.tips = ShopTips.体力值已满无需补充;
				return result;
			}

			if (releaseTime < 1) {
				// 次数已达极限且VIP已达极限
				if (vipData.lvl >= KVIPDataManager.mVIPLevelDataManager.getMaxLevel().lvl) {
					result.tips = ShopTips.体力值购买次数已达极限;
					return result;
				}
				result.tips = ShopTips.体力值购买次数已达极限请提升VIP等级再来;
				return result;
			}

			// 扣货币
			int ingotCount = vipData.fatbuyrmb[buyTime + 1];
			if (KSupportFactory.getCurrencySupport().decreaseMoney(roleId, KCurrencyTypeEnum.DIAMOND, ingotCount, UsePointFunctionTypeEnum.购买体力, true) < 0) {
				// 询问跳转到充值界面
				result.tips = StringUtil.format(ShopTips.x货币数量不足x, KCurrencyTypeEnum.DIAMOND.extName, ingotCount);
				return result;
			}

			// 加体力，加购买次数
			int powfull = KSupportFactory.getRoleModuleSupport().getPhyPowerFullSize();
			KSupportFactory.getRoleModuleSupport().addPhyPower(roleId, powfull, true, "购买");
			shop.setPhyPowerBuyTime(buyTime + 1);

//			KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.体力购买);

			result.tips = StringUtil.format(StringUtil.format(ShopTips.成功使用x数量x货币增加x体力, ingotCount, KCurrencyTypeEnum.DIAMOND.extName, powfull));
			return result;
		} finally {
			shop.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 搜索角色的体力药水道具
	 * 1.null表示不存在体力道具，调用者应该进入【购买】体力流程
	 * 2.非null时，调用者应该将道具ID放入Dialog脚本，并要求角色确认使用
	 * 3.角色确认使用时，请调用{@link #usePhyPowerItemForScenario(KGamePlayerSession, long, long)}使用道具
	 * 
	 * @param roleId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-12-14 下午3:36:16
	 * </pre>
	 */
	public static KItem searchPhyPowerItem(long roleId) {
		for (String itemCode : phyPowItemCodeList) {
			KItem item = KItemLogic.searchItemFromBag(roleId, itemCode);
			if (item != null) {
				return item;
			}
		}
		return null;
	}
	
	public static List<KItem> searchPhyPowerItemCount(long roleId) {
		List<KItem> result = new ArrayList<KItem>();
		for (String itemCode : phyPowItemCodeList) {
			KItem item = KItemLogic.searchItemFromBag(roleId, itemCode);
			if (item != null) {
				result.add(item);
			}
		}
		return result;
	}

	/**
	 * <pre>
	 * 使用角色的指定道具
	 * 
	 * @deprecated 未被使用的方法
	 * @param roleId
	 * @param itemId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-12-14 下午3:39:08
	 * </pre>
	 */
	public static ItemResult_Use usePhyPowerItemForScenario(KGamePlayerSession session, KRole role, long itemId) {
		ItemResult_Use result = KItemLogic.dealMsg_useBagItem(session, role, itemId, KItemTypeEnum.消耗品, false);
		if (result.isSucess) {
			KPushItemsMsg.pushItemCount(role.getId(), itemId, result.item.getCount());
		}

		result.doFinally(role);
		return result;
	}
}
