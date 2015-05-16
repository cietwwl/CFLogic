package com.kola.kmp.logic.shop.timehot;

import javax.management.timer.Timer;

import com.koala.game.KGameMessage;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.fashion.KFashionLogic;
import com.kola.kmp.logic.fashion.KFashionMsgPackCenter;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.shop.KShopRoleExtCACreator;
import com.kola.kmp.logic.shop.timehot.KHotShopDataManager.HotShopManager.HotGoods;
import com.kola.kmp.logic.shop.timehot.KHotShopDataManager.HotShopManager.HotGoodsForRoleLv;
import com.kola.kmp.logic.shop.timehot.KHotShopDataManager.HotShopManager.HotShop;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.ShopTips;
import com.kola.kmp.protocol.fashion.KFashionProtocol;
import com.kola.kmp.protocol.item.KItemProtocol;
import com.kola.kmp.protocol.shop.KShopProtocol;

/**
 * <pre>
 * 由于在打包模块消息的过程中，涉及到访问及复杂的逻辑结构
 * 因此设立此类，用于打包消息
 * 
 * @author CamusHuang
 * @creation 2013-1-5 下午12:03:37
 * </pre>
 */
public class KHotShopMsgPackCenter {
	public static KHotShopMsgPackCenter instance = new KHotShopMsgPackCenter();

	/**
	 * <pre>
	 * 参考{@link KShopProtocol#SM_PUSH_HOTGOODS}
	 * 
	 * @param msg
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-4-18 下午4:27:44
	 * </pre>
	 */
	public void packAllGoods(KGameMessage msg, long roleId) {
		// * int 所有商品数量（>0时显示主界面入口）
		// * byte 标签数量M
		// * for(0~M){
		// * byte 标签类型( 时装(1), 装备(2),随从(3),材料(4))
		// * String 标签名称
		// * byte 商品数量M（商品为0时，请不要显示此标签）
		// * for(0~M){
		// * 参考{@link KShopProtocol#MSG_STRUCT_HOTGOODS}
		// * }
		// * }
		if (!KHotShopDataManager.mHotShopManager.isActivityOpen) {
			msg.writeInt(0);
			msg.writeByte(0);
			return;
		}

		long nowTime = System.currentTimeMillis();
		long todayTime = UtilTool.getTodayStart().getTimeInMillis();
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		int roleLv = role.getLevel();
		int job = role.getJob();
		//
		TimeHotShopData roleData = KShopRoleExtCACreator.getRoleTimeHotShopData(roleId);
		//
		int writeIndex = msg.writerIndex();
		int totalCount = 0;
		msg.writeInt(totalCount);
		{
			msg.writeByte(KHotShopTypeEnum.values().length);
			for (KHotShopTypeEnum typeA : KHotShopTypeEnum.values()) {
				msg.writeByte(typeA.sign);
				msg.writeUtf8String(typeA.name);
				int goosCountWriteIndex = msg.writerIndex();
				int goodsCount = 0;
				msg.writeByte(goodsCount);
				{
					HotShop hotShop = KHotShopDataManager.mHotShopManager.allShopMap.get(typeA);
					HotGoodsForRoleLv goodsForLv = hotShop.getHotGoodsForRoleLv(roleLv);
					for (HotGoods goods : goodsForLv.dataList) {
						// 等级、职业、时间
						if (goods.job == null || goods.job.getJobType() == job) {
							if (goods.isActivityTakeEffectNow(nowTime, todayTime)) {
								packGoods(msg, roleId, typeA, goods, roleData.getGoodsBuyedTime(typeA, goods.index));
								goodsCount++;
							} else {
//								System.err.println("时间无效 type=" + typeA.name() + " id=" + goods.index);
							}
						} else {
//							System.err.println("职业无效 job=" + goods.job + " type=" + typeA.name() + " id=" + goods.index);
						}
					}
				}
				msg.setByte(goosCountWriteIndex, goodsCount);
				totalCount += goodsCount;
			}
		}
		msg.setInt(writeIndex, totalCount);
	}

	/**
	 * <pre>
	 * 参考{@link KShopProtocol#MSG_STRUCT_HOTGOODS}
	 * 
	 * @param msg
	 * @param goods
	 * @author CamusHuang
	 * @creation 2014-4-17 上午11:32:04
	 * </pre>
	 */
	private void packGoods(KGameMessage msg, long roleId, KHotShopTypeEnum typeA, HotGoods goods, int buyedTime) {

		// * int 商品ID
		// * if(标签类型==时装(1)){
		// * 参考{@link KItemProtocol#MSG_STRUCT_ITEM_DETAILS}
		// * 参考{@link KFashionProtocol#MSG_STRUCT_FASHION_DETAILS}
		// * } else if(标签类型==随从(3)){
		// * 参考{@link KItemProtocol#MSG_STRUCT_ITEM_DETAILS}
		// * 参考{@link
		// com.kola.kmp.protocol.pet.KPetProtocol#PET_MSG_DATA_STRUCTURE}
		// * } else {
		// * 参考{@link KItemProtocol#MSG_STRUCT_ITEM_DETAILS}
		// * }
		// *
		// * short 剩余购买次数
		// * short 总购买次数
		// * boolean 总购买次数是否每日重置
		// * int 剩余时间（秒）
		// * int 全局剩余数量（-1表示无限，0表示售磬）

		msg.writeInt(goods.index);
		//
		KItemTempAbs temp = goods.itemStruct.getItemTemplate();
		KItemMsgPackCenter.packItem(msg, temp, goods.itemStruct.itemCount, goods.price);
		if (typeA == KHotShopTypeEnum.时装) {
			KFashionMsgPackCenter.packFashion(msg, roleId, goods.getFashionTemp().id);
		} else if (typeA == KHotShopTypeEnum.随从) {
			KSupportFactory.getPetModuleSupport().packPetTemplateMsg(msg, goods.getPetTemp().getTemplateId());
		}
		//
		msg.writeShort(Math.max(0, goods.buyTimeForRole - buyedTime));
		msg.writeShort(goods.buyTimeForRole);
		msg.writeBoolean(goods.buyTimeForRoleIsDay);

		//
		long releaseTime = goods.getActivityReleaseTime();
		msg.writeInt((int) (releaseTime / Timer.ONE_SECOND));

		// 全服购买次数检查
		if (goods.buyTimeForWorld > 0) {
			// 有限次数
			int globalCount = HotShopGlobalDataImpl.instance.getCount(goods.index);
			if (globalCount >= goods.buyTimeForWorld) {
				msg.writeInt(0);
			} else {
				msg.writeInt(goods.buyTimeForWorld - globalCount);
			}
		} else {
			msg.writeInt(-1);
		}
	}

}
