package com.kola.kmp.logic.combat.impl;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.kola.kmp.logic.combat.ICombatMember;



/**
 * 
 * @author PERRY CHAN
 */
public class KCombatMemberFactory {

	private static final int _CACHE_SIZE = 5000;
	
	private static final Queue<ICombatMember> _cache = new ConcurrentLinkedQueue<ICombatMember>();
	private static final Queue<ICombatMember> _temporary = new ConcurrentLinkedQueue<ICombatMember>();
	
	private static final AtomicInteger _borrowCounter = new AtomicInteger();
	private static final AtomicInteger _returnCounter = new AtomicInteger();
	
	static void init() {
		for (int i = 0; i < _CACHE_SIZE; i++) {
			_cache.add(new KCombatMemberImpl());
		}
	}
	
	static void returnCombatMember(ICombatMember member) {
		_returnCounter.incrementAndGet();
		if (_temporary.contains(member)) {
			_temporary.remove(member);
		} else {
			if (_cache.size() < _CACHE_SIZE) {
				_cache.offer(member);
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
	
	public static ICombatMember getCombatMemberInstance(byte pMemberType) {
		switch (pMemberType) {
		case ICombatMember.MEMBER_TYPE_ROLE:
		case ICombatMember.MEMBER_TYPE_PET:
		case ICombatMember.MEMBER_TYPE_MONSTER:
		case ICombatMember.MEMBER_TYPE_VEHICLE:
		case ICombatMember.MEMBER_TYPE_BLOCK:
		case ICombatMember.MEMBER_TYPE_TEAM_MATE_ROLE:
		case ICombatMember.MEMBER_TYPE_TEAM_MATE_PET:
		case ICombatMember.MEMBER_TYPE_BOSS_MONSTER:
		case ICombatMember.MEMBER_TYPE_ELITIST_MONSTER:
		case ICombatMember.MEMBER_TYPE_ROLE_MONSTER:
		case ICombatMember.MEMBER_TYPE_PET_MONSTER:
		case ICombatMember.MEMBER_TYPE_ASSISTANT:
		case ICombatMember.MEMBER_TYPE_MINION:
		case ICombatMember.MEMBER_TYPE_BARREL_MONSTER:
		case ICombatMember.MEMBER_TYPE_WORLD_BOSS_OTHER_ROLE:
		case ICombatMember.MEMBER_TYPE_WORLD_BOSS_OTHER_PET:
			ICombatMember member = _cache.poll();
			if(member == null) {
				member = new KCombatMemberImpl();
				_temporary.offer(member);
			}
			_borrowCounter.incrementAndGet();
			return member;
		default:
			return null;
		}
	}
}
