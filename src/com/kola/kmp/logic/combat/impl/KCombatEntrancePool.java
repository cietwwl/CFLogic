package com.kola.kmp.logic.combat.impl;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.kola.kmp.logic.combat.api.ICombatObjectBase;

/**
 * 
 * entrance缓存池，由于entrance作为战斗中间对象，会经常产生，并且，它们只存
 * 在一段很短的时间后便会被销毁，因此，为了避免过多地产生内存碎片，用一个缓存
 * 池预先缓存一批对象
 * 
 * @author PERRY CHAN
 */
public class KCombatEntrancePool {

//	private static final int _ROLE_POOL_SIZE = 500; // 角色类型的entrance的容量
//	private static final int _OTHER_TYPE_POOL_SIZE = 2000; // 其他类型的entrance的容量
	private static final int _POOL_SIZE = 2500;
	
//	private static final Queue<KCombatEntrance> _roleTypeEntranceQueue = new ConcurrentLinkedQueue<KCombatEntrance>();
//	private static final Queue<KCombatEntrance> _otherTypeEntranceQueue = new ConcurrentLinkedQueue<KCombatEntrance>();
	private static final Queue<KCombatEntrance> _cacheQueue = new ConcurrentLinkedQueue<KCombatEntrance>();
	private static final Queue<KCombatEntrance> _temporaryQueue = new ConcurrentLinkedQueue<KCombatEntrance>();
	
	private static final AtomicInteger _borrowCount = new AtomicInteger(); // 借出的总数量
	private static final AtomicInteger _returnCount = new AtomicInteger(); // 归还的总数量
	
//	private static KCombatEntrance borrow(Queue<KCombatEntrance> src, boolean isRoleType) {
//		KCombatEntrance entrance = src.poll();
//		if (entrance != null) {
//			_borrowCount.incrementAndGet();
//			return entrance;
//		} else {
//			entrance = new KCombatEntrance(isRoleType ? new KRoleCombatRecorderImpl() : new KEmptyCombatRecorderImpl());
//			_temporaryQueue.add(entrance);
//			return entrance;
//		}
//	}
	
	static void init() {
//		for (int i = 0; i < _ROLE_POOL_SIZE; i++) {
//			_roleTypeEntranceQueue.add(new KCombatEntrance(new KRoleCombatRecorderImpl()));
//		}
//		for (int i = 0; i < _OTHER_TYPE_POOL_SIZE; i++) {
//			_otherTypeEntranceQueue.add(new KCombatEntrance(new KEmptyCombatRecorderImpl()));
//		}
		for(int i = 0; i < _POOL_SIZE; i++) {
			_cacheQueue.add(new KCombatEntrance());
		}
	}
	
	static KCombatEntrance borrowEntrance(ICombatObjectBase obj, KCombatEntrance mount, float x, float y, int instanceId) {
//		Queue<KCombatEntrance> srcQueue;
//		boolean isRoleType = false;
//		if (obj instanceof ICombatRole) {
//			srcQueue = _roleTypeEntranceQueue;
//			isRoleType = true;
//		} else {
//			srcQueue = _otherTypeEntranceQueue;
//		}
//		KCombatEntrance entrance = borrow(srcQueue, isRoleType);
//		entrance.init(obj, mount, x, y, instanceId);
		KCombatEntrance entrance = _cacheQueue.poll();
		if (entrance != null) {
			_borrowCount.incrementAndGet();
		} else {
			entrance = new KCombatEntrance();
			_temporaryQueue.add(entrance);
		}
		entrance.init(obj, mount, x, y, instanceId);
		return entrance;
	}
	
	static void returnEntrance(KCombatEntrance entrance) {
		if (_temporaryQueue.contains(entrance)) {
			_temporaryQueue.remove(entrance);
		} else {
			_returnCount.incrementAndGet();
			entrance.release();
//			if (entrance.getCombatRecorder() instanceof KRoleCombatRecorderImpl) {
//				_roleTypeEntranceQueue.offer(entrance);
//			} else {
//				_otherTypeEntranceQueue.offer(entrance);
//			}
			_cacheQueue.add(entrance);
		}
	}
	
	static int getBorrowCount() {
		return _borrowCount.get();
	}
	
	static int getReturnCount() {
		return _returnCount.get();
	}
	
//	static int getRoleTypeSize() {
//		return _roleTypeEntranceQueue.size();
//	}
//	
//	static int getOtherTypeSize() {
//		return _otherTypeEntranceQueue.size();
//	}
	
	static int getCacheSize() {
		return _cacheQueue.size();
	}
	
	static int getTemporarysize() {
		return _temporaryQueue.size();
	}
}
