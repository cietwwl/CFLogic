package com.kola.kmp.logic.combat.impl;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.kola.kmp.logic.combat.ICombatRecorder;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatRecorderPool {

	private static final int _ROLE_RECORDER_SIZE = 1000;
	private static final int _EMPTY_RECORDER_SIZE = 4000;
	
	private static final Queue<ICombatRecorder> _roleRecorderCache = new ConcurrentLinkedQueue<ICombatRecorder>();
	private static final Queue<ICombatRecorder> _emptyRecorderCache = new ConcurrentLinkedQueue<ICombatRecorder>();
	private static final Queue<ICombatRecorder> _temporary = new ConcurrentLinkedQueue<ICombatRecorder>();
	private static final AtomicInteger _borrowCount = new AtomicInteger();
	private static final AtomicInteger _returnCount = new AtomicInteger();
	
	static void init() {
		for (int i = 0; i < _ROLE_RECORDER_SIZE; i++) {
			_roleRecorderCache.add(new KRoleCombatRecorderImpl());
		}
		for (int i = 0; i < _EMPTY_RECORDER_SIZE; i++) {
			_emptyRecorderCache.add(new KEmptyCombatRecorderImpl());
		}
	}
	
	static ICombatRecorder borrowRecorder(boolean roleType) {
		Queue<ICombatRecorder> src;
		if (roleType) {
			src = _roleRecorderCache;
		} else {
			src = _emptyRecorderCache;
		}
		ICombatRecorder recorder = src.poll();
		if (recorder == null) {
			recorder = roleType ? new KRoleCombatRecorderImpl() : new KEmptyCombatRecorderImpl();
			_temporary.add(recorder);
		} else {
			_borrowCount.incrementAndGet();
		}
		return recorder;
	}
	
	static void returnRecorder(ICombatRecorder recorder) {
		if (_temporary.contains(recorder)) {
			_temporary.remove(recorder);
		} else {
//			_returnCount.incrementAndGet();
			recorder.release();
			Queue<ICombatRecorder> queue;
			if (recorder instanceof KRoleCombatRecorderImpl) {
				queue = _roleRecorderCache;
			} else {
				queue = _emptyRecorderCache;
			}
			if(!queue.contains(recorder)) {
				_returnCount.incrementAndGet();
				queue.offer(recorder);
			}
		}
	}
	
	static int getBorrowCount() {
		return _borrowCount.get();
	}
	
	static int getReturnCount() {
		return _returnCount.get();
	}
	
	static int getRoleTypeSize() {
		return _roleRecorderCache.size();
	}
	
	static int getOtherTypeSize() {
		return _emptyRecorderCache.size();
	}
	
	static int getTemporarysize() {
		return _temporary.size();
	}
}
