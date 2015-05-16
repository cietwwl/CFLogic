package com.kola.kmp.logic.competition;

import org.slf4j.Logger;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.kola.kgame.cache.GameModule;

public class KCompetitionModule implements GameModule{
	static final Logger _LOGGER = KGameLogger.getLogger(KCompetitionModule.class);
	
	private static KCompetitionManager manager;

	private String _moduleName;
	private int _msgLowerId;
	private int _msgUpperId;
	
	private String  moduleConfigPath;
	@Override
	public void init(String cfgPath, String pModuleName, int pMsgLowerId,
			int pMsgUpperId) throws Exception {
		this._moduleName = pModuleName;
		this._msgLowerId = pMsgLowerId;
		this._msgUpperId = pMsgUpperId;
		
		moduleConfigPath = cfgPath;
		//
		_LOGGER.warn("----- " + getModuleName() + " 收到初始化通知------");
		//
		manager = new KCompetitionManager();
		manager.init(cfgPath);

		_LOGGER.warn("----- " + getModuleName() + " 完成初始化------");
		
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		manager.serverStartCompleted();
		
	}

	@Override
	public void notifyCacheLoadComplete() throws KGameServerException {
		manager.notifyCacheLoadComplete();
	}

	@Override
	public boolean messageReceived(KGameMessageEvent msgEvent)
			throws KGameServerException {
		return false;
	}

	@Override
	public void exceptionCaught(KGameServerException ex) {
		
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		manager.serverShutDown();
	}

	@Override
	public String getModuleName() {
		return _moduleName;
	}
	
	/**
	 * 获取竞技场管理器实例
	 * @return
	 */
	public static KCompetitionManager getCompetitionManager(){
		return manager;
	}

}
