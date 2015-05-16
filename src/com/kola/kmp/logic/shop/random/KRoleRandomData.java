package com.kola.kmp.logic.shop.random;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.kola.kmp.logic.shop.KRoleShop;
import com.kola.kmp.logic.shop.random.KRandomShopDataManager.RandomGoodsManager.RandomGoods;

/**
 * <pre>
 * 随机商店相关数据
 * 
 * @author CamusHuang
 * @creation 2014-11-10 下午3:18:24
 * </pre>
 */
public class KRoleRandomData {
	public final KRoleShop owner;
	// 读写锁
	public final ReentrantLock rwLock;

	// 不永久保存<类型,<商品ID,商品>>
	private Map<KRandomShopTypeEnum, LinkedHashMap<Integer, RandomGoods>> randomGoodsMap = new HashMap<KRandomShopTypeEnum, LinkedHashMap<Integer, RandomGoods>>();
	// 不永久保存 <KRandomShopTypeEnum, Set<随机商品ID>>
	private Map<KRandomShopTypeEnum, Set<Integer>> randomGoodsBuyMap = new HashMap<KRandomShopTypeEnum, Set<Integer>>();
	// 当天随机刷新次数
	private int randomTime;
	// 不永久保存-最后一次系统刷新时间
	private long lastSystemRereshTime;

	//

	public KRoleRandomData(KRoleShop owner) {
		this.owner = owner;
		this.rwLock = owner.rwLock;
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 
	 * @param newDayStartTime
	 * @author CamusHuang
	 * @creation 2014-4-10 上午10:25:35
	 * </pre>
	 */
	public void notifyForDayChange(long nowTime) {
		if (randomTime > 0) {
			randomTime = 0;
			owner.notifyUpdate();
		}
	}

	public int getRandomTime() {
		return randomTime;
	}

	long getLastSystemRereshTime() {
		return lastSystemRereshTime;
	}

	public void setRandomTime(int randomTime) {
		owner.rwLock.lock();
		try {
			if (this.randomTime != randomTime) {
				this.randomTime = randomTime;
				owner.notifyUpdate();
			}
		} finally {
			owner.rwLock.unlock();
		}
	}

	void setLastSystemRereshTime(long newTime) {
		owner.rwLock.lock();
		try {
			this.lastSystemRereshTime = newTime;
		} finally {
			owner.rwLock.unlock();
		}
	}

	void setRandomGoods(KRandomShopTypeEnum key, LinkedHashMap<Integer, RandomGoods> goodsMap) {
		owner.rwLock.lock();
		try {
			randomGoodsMap.put(key, goodsMap);

			// 清理所有随机商品以及购买信息
			Set<Integer> set = randomGoodsBuyMap.get(key);
			if (set != null) {
				set.clear();
			}

			owner.notifyUpdate();
		} finally {
			owner.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 直接获取缓存，谨慎使用
	 * @return <类型,<商品ID,商品>>
	 * @author CamusHuang
	 * @creation 2014-4-17 上午11:55:12
	 * </pre>
	 */
	Map<KRandomShopTypeEnum, LinkedHashMap<Integer, RandomGoods>> getRandomGoodsCache() {
		return randomGoodsMap;
	}

	boolean isBuyedRandomGoods(KRandomShopTypeEnum type, int goodsID) {
		owner.rwLock.lock();
		try {
			Set<Integer> set = randomGoodsBuyMap.get(type);
			if (set == null) {
				return false;
			}
			return set.contains(goodsID);
		} finally {
			owner.rwLock.unlock();
		}
	}

	void notifyBuyedRandomGoods(KRandomShopTypeEnum type, int goodsID) {
		owner.rwLock.lock();
		try {
			Set<Integer> set = randomGoodsBuyMap.get(type);
			if (set == null) {
				set = new HashSet<Integer>();
				randomGoodsBuyMap.put(type, set);
			}
			set.add(goodsID);
			owner.notifyUpdate();
		} finally {
			owner.rwLock.unlock();
		}
	}
}
