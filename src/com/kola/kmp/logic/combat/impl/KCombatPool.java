package com.kola.kmp.logic.combat.impl;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatPool {
	
	private static final int _CACHE_SIZE = 1000;
	private static final Queue<KCombatImpl> _cache = new ConcurrentLinkedQueue<KCombatImpl>();
	private static final Queue<KCombatImpl> _temporary = new ConcurrentLinkedQueue<KCombatImpl>();
	
	private static final AtomicInteger _borrowCounter = new AtomicInteger();
	private static final AtomicInteger _returnCounter = new AtomicInteger();
	
	static void init() {
		for(int i = 0; i < _CACHE_SIZE; i++) {
			_cache.add(new KCombatImpl());
		}
	}
	
	static KCombatImpl borrowCombat() {
		_borrowCounter.incrementAndGet();
		KCombatImpl entity = _cache.poll();
		if (entity == null) {
			entity = new KCombatImpl();
			_temporary.add(entity);
		}
		return entity;
	}
	
	static void returnCombat(KCombatImpl entity) {
		_returnCounter.incrementAndGet();
		if (_temporary.contains(entity)) {
			_temporary.remove(entity);
		} else {
			if (_cache.size() < _CACHE_SIZE) {
				_cache.add(entity);
			}
		}
	}
	
	static int getBorrowCount() {
		return _borrowCounter.get();
	}

	static int getReturnCount() {
		return _returnCounter.get();
	}

	static int getCacheSize() {
		return _cache.size();
	}

	static int getTemporarySize() {
		return _temporary.size();
	}
}
