package com.kola.kmp.logic.combat.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.combat.ICombatGround;
import com.kola.kmp.logic.combat.KCombatType;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatGroundPool {

	private static final int _CACHE_SIZE_COMMON = 700;
	private static final int _CACHE_SIZE_TOWER = 300;
//	private static final int _CACHE_SIZE_BARREL = 200;
	
	private static final byte COMBAT_TYPE_COMMON = 1;
	private static final byte COMBAT_TYPE_TOWER = 2;
	private static final byte COMBAT_TYPE_BARREL = 3;
	
//	private static final Queue<ICombatGround> _commonGroundCache = new ConcurrentLinkedQueue<ICombatGround>();
//	private static final Queue<ICombatGround> _towerGroundCache  = new ConcurrentLinkedQueue<ICombatGround>();
	
//	private static final Queue<ICombatGround> _commonGroundTemporary = new ConcurrentLinkedQueue<ICombatGround>();
//	private static final Queue<ICombatGround> _towerGroundTemporary = new ConcurrentLinkedQueue<ICombatGround>();
	
	private static final Map<Byte, Queue<ICombatGround>> _cache = new HashMap<Byte, Queue<ICombatGround>>();
	private static final Map<Byte, Queue<ICombatGround>> _temporary = new HashMap<Byte, Queue<ICombatGround>>();
	private static final Map<Byte, AtomicInteger> _borrowCounter = new HashMap<Byte, AtomicInteger>();
	private static final Map<Byte, AtomicInteger> _returnCounter = new HashMap<Byte, AtomicInteger>();
	
//	private static final AtomicInteger _commonBorrowCounter = new AtomicInteger();
//	private static final AtomicInteger _commonReturnCounter = new AtomicInteger();
//	
//	private static final AtomicInteger _towerBorrowCounter = new AtomicInteger();
//	private static final AtomicInteger _towerReturnCounter = new AtomicInteger();
	
	static {
		_cache.put(COMBAT_TYPE_COMMON, new ConcurrentLinkedQueue<ICombatGround>());
		_cache.put(COMBAT_TYPE_TOWER, new ConcurrentLinkedQueue<ICombatGround>());
		_cache.put(COMBAT_TYPE_BARREL, new ConcurrentLinkedQueue<ICombatGround>());
		
		_temporary.put(COMBAT_TYPE_COMMON, new ConcurrentLinkedQueue<ICombatGround>());
		_temporary.put(COMBAT_TYPE_TOWER, new ConcurrentLinkedQueue<ICombatGround>());
		_temporary.put(COMBAT_TYPE_BARREL, new ConcurrentLinkedQueue<ICombatGround>());
		
		_borrowCounter.put(COMBAT_TYPE_COMMON, new AtomicInteger());
		_borrowCounter.put(COMBAT_TYPE_TOWER, new AtomicInteger());
		_borrowCounter.put(COMBAT_TYPE_BARREL, new AtomicInteger());
		
		_returnCounter.put(COMBAT_TYPE_COMMON, new AtomicInteger());
		_returnCounter.put(COMBAT_TYPE_TOWER, new AtomicInteger());
		_returnCounter.put(COMBAT_TYPE_BARREL, new AtomicInteger());
	}
	
	private static byte getCombatGroundType(KCombatType type) {
		switch (type) {
		case BARREL:
			return COMBAT_TYPE_BARREL;
		case TOWER_COMBAT:
			return COMBAT_TYPE_TOWER;
		default:
			return COMBAT_TYPE_COMMON;
		}
	}
	
	private static ICombatGround borrow(KCombatType type) {
		byte groundType = getCombatGroundType(type);
		Queue<ICombatGround> src = _cache.get(groundType);
		ICombatGround borrow = src.poll();
		if (borrow == null) {
			Queue<ICombatGround> temporaryQueue = _temporary.get(groundType);
			temporaryQueue.add(borrow);
		}
		return borrow;
	}

	static void init() {
		Queue<ICombatGround> queue = _cache.get(COMBAT_TYPE_COMMON);
		for (int i = 0; i < _CACHE_SIZE_COMMON; i++) {
			queue.add(new KCombatGroundImpl());
		}
		queue = _cache.get(COMBAT_TYPE_TOWER);
		for (int i = 0; i < _CACHE_SIZE_TOWER; i++) {
			queue.add(new KCombatGroundTowerImpl());
		}
//		queue = _cache.get(COMBAT_TYPE_BARREL);
//		for (int i = 0; i < _CACHE_SIZE_BARREL; i++) {
//			queue.add(new KCombatGroundBarrelImpl());
//		}
	}

	static KCombatGroundImpl borrowCommon() {
		_borrowCounter.get(COMBAT_TYPE_COMMON).incrementAndGet();
		return (KCombatGroundImpl) borrow(KCombatType.GAME_LEVEL);
	}

	static KCombatGroundTowerImpl borrowTower() {
		_borrowCounter.get(COMBAT_TYPE_TOWER).incrementAndGet();
		return (KCombatGroundTowerImpl) borrow(KCombatType.TOWER_COMBAT);
	}

	static KCombatGroundBarrelImpl borrowBarrel() {
		_borrowCounter.get(COMBAT_TYPE_BARREL).incrementAndGet();
		return (KCombatGroundBarrelImpl) borrow(KCombatType.BARREL);
	}

	static void returnCombatGround(ICombatGround combatGround) {
		Queue<ICombatGround> temporaryQueue;
		byte type = 0;
		if (combatGround instanceof KCombatGroundImpl) {
			type = COMBAT_TYPE_COMMON;
		} else if (combatGround instanceof KCombatGroundTowerImpl) {
			type = COMBAT_TYPE_TOWER;
		} else if (combatGround instanceof KCombatGroundBarrelImpl) {
			type = COMBAT_TYPE_BARREL;
		}
		if (type > 0) {
			_returnCounter.get(type).incrementAndGet();
			temporaryQueue = _temporary.get(type);
			if (temporaryQueue.contains(combatGround)) {
				temporaryQueue.remove(combatGround);
			} else {
				_cache.get(type).offer(combatGround);
			}
		}
	}
	
	static String getInfo() {
		String cacheInfo = StringUtil.format("cache:{},{},{}", _cache.get(COMBAT_TYPE_COMMON).size(), _cache.get(COMBAT_TYPE_TOWER).size(), _cache.get(COMBAT_TYPE_BARREL).size());
		String tempororayInfo = StringUtil.format("temporary:{},{},{}", _temporary.get(COMBAT_TYPE_COMMON).size(), _temporary.get(COMBAT_TYPE_TOWER).size(), _temporary.get(COMBAT_TYPE_BARREL).size());
		String borrowInfo = StringUtil.format("borrow:{},{},{}", _borrowCounter.get(COMBAT_TYPE_COMMON).intValue(), _borrowCounter.get(COMBAT_TYPE_TOWER).intValue(), _borrowCounter.get(COMBAT_TYPE_BARREL).intValue());
		String returnInfo = StringUtil.format("return:{},{},{}", _returnCounter.get(COMBAT_TYPE_COMMON).intValue(), _returnCounter.get(COMBAT_TYPE_TOWER).intValue(), _returnCounter.get(COMBAT_TYPE_BARREL).intValue());
		return StringUtil.format("{};{};{};{}", cacheInfo, tempororayInfo, borrowInfo, returnInfo);
	}
}
