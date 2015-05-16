package com.kola.kmp.logic.role;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kgame.cache.role.RoleModuleFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KRoleJobCounterTask implements KGameTimerTask {

	private static final int _DELAY = 30;
	
	private Map<Integer, AtomicInteger> _counterMap = new HashMap<Integer, AtomicInteger>();
	private int _defaultTemplateId;
	
	KRoleJobCounterTask(List<Integer> allTemplateIds) {
		for (int i = 0; i < allTemplateIds.size(); i++) {
			int count = RoleModuleFactory.getRoleModule().getTotalCountByType(allTemplateIds.get(i));
			_counterMap.put(allTemplateIds.get(i), new AtomicInteger(count));
		}
		compareJobs();
	}

	private void compareJobs() {
		int tempType = 0;
		int tempCount = 0;
		int lastCount = -1;
		Map.Entry<Integer, AtomicInteger> entry;
		for (Iterator<Map.Entry<Integer, AtomicInteger>> itr = _counterMap.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			tempCount = entry.getValue().get();
			if (lastCount == -1 || lastCount > tempCount) {
				lastCount = tempCount;
				tempType = entry.getKey();
			}
		}
		_defaultTemplateId = tempType;
	}
	
	void start() {
		KGame.getTimer().newTimeSignal(this, _DELAY, TimeUnit.SECONDS);
	}
	
	int getDefaultTemplateId() {
		return _defaultTemplateId;
	}
	
	void notifyRoleCreated(int templateId) {
		AtomicInteger counter = _counterMap.get(templateId);
		counter.incrementAndGet();
	}
	
	void notifyRoleDeleted(int templateId) {
		AtomicInteger counter = _counterMap.get(templateId);
		counter.decrementAndGet();
	}
	
	@Override
	public String getName() {
		return "KRoleJobCounterTask";
	}

	@Override
	public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
		compareJobs();
		timeSignal.getTimer().newTimeSignal(this, _DELAY, TimeUnit.SECONDS);
		return "SUCCESS";
	}

	@Override
	public void done(KGameTimeSignal timeSignal) {
		
	}

	@Override
	public void rejected(RejectedExecutionException e) {
		
	}

}
