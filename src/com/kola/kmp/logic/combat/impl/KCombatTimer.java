package com.kola.kmp.logic.combat.impl;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.cmd.ICombatCommand;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatTimer implements KGameTimerTask {

	private static final Logger _LOGGER = KGameLogger.getLogger("combatLogger");
	
	private static final int _DELAY_MILLIS = 200;
	
	private final Queue<ICombatCommand> _cmds = new ConcurrentLinkedQueue<ICombatCommand>(); // 指令队伍
	private final Queue<ICombatCommand> _copys = new LinkedList<ICombatCommand>();
	private final ReadWriteLock _lock = new ReentrantReadWriteLock();
	
	private ICombat _combat;
	private String _name;
	private boolean _on = true;
	
	static KCombatTimer createAndSubmitNewTask(ICombat pCombat) {
		KCombatTimer timer = new KCombatTimer(pCombat);
		KGame.newTimeSignal(timer, _DELAY_MILLIS, TimeUnit.MILLISECONDS);
		return timer;
	}
	
	KCombatTimer(ICombat pCombat) {
		this._combat = pCombat;
		this._name = "KCombatTimer-" + this._combat.getSerialId();
	}
	
	void start() {
		this._on = true;
		KGame.newTimeSignal(this, _DELAY_MILLIS, TimeUnit.MILLISECONDS);
	}
	
	void addCmd(ICombatCommand cmd) {
		_lock.readLock().lock();
		try {
			_cmds.add(cmd);
		} finally {
			_lock.readLock().unlock();
		}
	}
	
	@Override
	public String getName() {
		return _name;
	}

	@Override
	public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
		try {
			if (_cmds.size() > 0) {
				_lock.writeLock().lock();
				try {
					_copys.addAll(_cmds);
					_cmds.clear();
				} finally {
					_lock.writeLock().unlock();
				}
				ICombatCommand cmd;
				while ((cmd = _copys.poll()) != null) {
					cmd.execute();
				}
			}
			_combat.onTimeSignal();
		} catch (Exception e) {
			_LOGGER.error("战场时效任务执行出现异常！战场id是：{}", _combat.getSerialId(), e);
		} catch (Throwable t) {
			_LOGGER.error("战场时效任务执行出现错误！战场id是：{}", _combat.getSerialId(), t);
		}
		if (_on) {
			timeSignal.getTimer().newTimeSignal(this, _DELAY_MILLIS, TimeUnit.MILLISECONDS);
		}
		return "success";
	}

	@Override
	public void done(KGameTimeSignal timeSignal) {
		
	}

	@Override
	public void rejected(RejectedExecutionException e) {
		
	}
	
	void terminateTimer() {
		_LOGGER.debug("战斗终结通知！战场id：{}",  _combat.getSerialId());
		this._on = false;
		this._cmds.clear();
		this._copys.clear();
	}

}
