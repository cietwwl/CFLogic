package com.kola.kmp.logic.map;

import org.slf4j.Logger;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.kola.kgame.cache.GameModule;
import com.kola.kgame.cache.level.impl.KGameLevelModule;

public class KMapModule implements GameModule{
	public static final Logger _LOGGER = KGameLogger
			.getLogger(KMapModule.class);
	
	private static KGameMapManager manager;

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
		manager = new KGameMapManager();
		manager.init(cfgPath);

		_LOGGER.warn("----- " + getModuleName() + " 完成初始化------");
		
		KMapModuleFactory.setModule(this);
	}

	@Override
	public void onGameWorldInitComplete() {
		manager.serverStartCompleted();
	}

	@Override
	public void notifyCacheLoadComplete() {
		
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
		
	}

	@Override
	public String getModuleName() {
		return _moduleName;
	}
	
	/**
	 * 获取地图管理器实例
	 * @return
	 */
	public static KGameMapManager getGameMapManager(){
		return manager;
	}

}
