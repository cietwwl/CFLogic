package com.kola.kmp.logic.combat.impl;

import org.slf4j.Logger;

import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatMessageChannel {

	private static final Logger _LOGGER = KGameLogger.getLogger("combatLogger");
	private long _roleId;
	private short _shadowId;
	private boolean _online;
	private long _delay; // 延迟
	private boolean _exit;
	private long _lastCommunicateTime;
	private long _checkTime;
	
	KCombatMessageChannel(long pRoleId, short pShadowId, boolean isOnline) {
		this._roleId = pRoleId;
		this._online = isOnline;
		this._shadowId = pShadowId;
	}

	void setOnline(boolean pOnline) {
		this._online = pOnline;
	}
	
	void setDelay(long pDelay) {
		this._delay = pDelay;
	}
	
	void setExit() {
		this._exit = true;
	}
	
	public long getRoleId() {
		return _roleId;
	}
	
	public short getShadowId() {
		return _shadowId;
	}
	
	public long getDelay() {
		return _delay;
	}
	
	public boolean isExit() {
		return _exit;
	}
	
	public boolean isOnline() {
		return _online;
	}
	
	public long getLastCommunicateTime() {
		return _lastCommunicateTime;
	}
	
	public void setLastCommunicateTime(long time) {
		this._lastCommunicateTime = time;
	}
	
	public long getCheckTime() {
		return _checkTime;
	}
	
	public void setCheckTime(long time) {
		this._checkTime = time;
	}

	public void sendMsg(KGameMessage msg) {
		if (_online && !_exit) {
			/*boolean success = */KSupportFactory.getRoleModuleSupport().sendMsg(_roleId, msg);
//			_LOGGER.info("发送消息：{}给{}，长度：{}，是否成功？{}", msg.getMsgID(), _roleId, msg.getPayloadLength(), success);
		}
	}
}
