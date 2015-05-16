package com.kola.kmp.logic.shop.random;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.shop.KShopRoleExtCACreator;
import com.kola.kmp.logic.shop.random.KRandomShopDataManager.RandomGoodsManager.RandomGoods;
import com.kola.kmp.logic.util.KSimpleDialyManager.Dialy;
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
public class KRandomShopMsgPackCenter {

	public static final Logger _LOGGER = KGameLogger.getLogger(KRandomShopMsgPackCenter.class);

	/**
	 * <pre>
	 * 参考{@link KShopProtocol#SM_PUSH_RANDOMGOODS}
	 * 
	 * @param msg
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-4-18 下午4:27:44
	 * </pre>
	 */
	public static void packAllRandomGoods(KGameMessage msg, long roleId) {
		KRoleRandomData roleData = KShopRoleExtCACreator.getRoleRandomData(roleId);
		roleData.rwLock.lock();
		try {
			Map<KRandomShopTypeEnum, LinkedHashMap<Integer, RandomGoods>> dataMap = roleData.getRandomGoodsCache();
			msg.writeByte(dataMap.size());
			for (KRandomShopTypeEnum type : KRandomShopTypeEnum.values()) {
				LinkedHashMap<Integer, RandomGoods> map = dataMap.get(type);
				if (map == null) {
					continue;
				}
				msg.writeByte(type.sign);
				msg.writeUtf8String(type.name);
				msg.writeByte(map.size());
				for (RandomGoods goods : map.values()) {
					packRandomGoods(msg, goods, !roleData.isBuyedRandomGoods(type, goods.index));
				}
			}
		} finally {
			roleData.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 参考{@link KShopProtocol#SM_PUSH_RANDOMGOODS}
	 * 
	 * @param msg
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-4-18 下午4:27:44
	 * </pre>
	 */
	public static void packRandomGoods(KGameMessage msg, long roleId, KRandomShopTypeEnum nowGoodsType) {
		KRoleRandomData roleData = KShopRoleExtCACreator.getRoleRandomData(roleId);
		roleData.rwLock.lock();
		try {
			msg.writeByte(1);
			{
				LinkedHashMap<Integer, RandomGoods> map = roleData.getRandomGoodsCache().get(nowGoodsType);
				msg.writeByte(nowGoodsType.sign);
				msg.writeUtf8String(nowGoodsType.name);
				msg.writeByte(map.size());
				for (RandomGoods goods : map.values()) {
					packRandomGoods(msg, goods, !roleData.isBuyedRandomGoods(nowGoodsType, goods.index));
				}
			}
		} finally {
			roleData.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 参考{@link KShopProtocol#MSG_STRUCT_RANDOMGOODS}
	 * 
	 * @param msg
	 * @param goods
	 * @author CamusHuang
	 * @creation 2014-4-17 上午11:32:04
	 * </pre>
	 */
	private static void packRandomGoods(KGameMessage msg, RandomGoods goods, boolean isCanBuy) {
		/**
		 * <pre>
		 * 一件随机商品的列表数据消息结构
		 * int 商品ID
		 * boolean 是否能购买（主要用于随机商店）
		 * 参考{@link KItemProtocol#MSG_STRUCT_ITEM_DETAILS}
		 * String 现价货币类型
		 * int 现价
		 * String 商品特征标签（例如"热卖"标签，""表示无标签，主要用于商城）
		 * </pre>
		 */
		msg.writeInt(goods.index);
		msg.writeBoolean(isCanBuy);
		
		KItemTempAbs temp = goods.itemStruct.getItemTemplate();
		KItemMsgPackCenter.packItem(msg, temp, goods.itemStruct.itemCount, goods.orgPrice);
		msg.writeByte(goods.salePrice.currencyType.sign);
		msg.writeInt((int) goods.salePrice.currencyCount);
		msg.writeUtf8String("");
		msg.writeByte(goods.mixVIPLvl);
	}

	public static KGameMessage packRandomShopLogs(int msgId, int maxLogsId) {
		int maxId = KRandomShopCenter.dialyManager.getMaxDialyId();
		if (maxLogsId >= maxId) {
			return null;
		}

		List<Dialy> dialys = KRandomShopCenter.dialyManager.getAllDialysCopy();
		KGameMessage backMsg = KGame.newLogicMessage(msgId);
		int writeIndex = backMsg.writerIndex();
		backMsg.writeByte(0);
		int count = 0;
		for (Dialy dialy : dialys) {
			if (dialy.id > maxLogsId) {
				backMsg.writeInt(dialy.id);
				backMsg.writeUtf8String(dialy.dialy);
				count++;
			}
		}
		backMsg.setByte(writeIndex, count);
		return backMsg;
	}

}
