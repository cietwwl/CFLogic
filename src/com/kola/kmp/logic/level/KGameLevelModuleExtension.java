package com.kola.kmp.logic.level;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.kola.kgame.cache.level.GameLevelModule;
import com.kola.kgame.cache.level.impl.KAGameLevelModuleExtension;
import com.kola.kgame.cache.level.impl.KGameLevelModule;

public class KGameLevelModuleExtension extends KAGameLevelModuleExtension{
	
	private static KGameLevelManager manager;
	
	private static KGameLevelModuleExtension instance;
	
	

	public KGameLevelModuleExtension() {
		super();
		instance = this;
	}

	@Override
	public void init(GameLevelModule module, String cfgPath) throws Exception {
		super.init(module, cfgPath);
		//
		System.out.println("cfgPath::::::"+cfgPath);
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到初始化通知，开始加载静态数据------");
		manager = new KGameLevelManager();
		manager.init(cfgPath);		
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成加载静态数据------");
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException{
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到游戏启动完成通知，开始验证静态的数据------");
		// CTODO
		manager.serverStartComplete();
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成静态数据验证------");
	}

	public void notifyCacheLoadComplete() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到缓存初始化完成通知，开始启动时效任务------");
		// CTODO super.notifyCacheLoadComplete();
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成启动时效任务------");
	}

	@Override
	public boolean messageReceived(KGameMessageEvent msgEvent) throws KGameServerException {
		// CTODO Auto-generated method stub
		return false;
	}

	@Override
	public void exceptionCaught(KGameServerException ex) {
		_LOGGER.error(ex.getMessage(), ex);
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到关服通知------");
		// CEND 服务器关闭,要做些什么操作??
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成关服------");
	}
	
	@Override
	public KGameLevelSet newGameLevelSet(long roleId, boolean isFirstNew) {
		return new KGameLevelSet(roleId, isFirstNew);
	}
	
	public static KGameLevelSet getGameLevelSet(long roleId) {
		return (KGameLevelSet)(KGameLevelModule.getInstance().getGameLevelSet(roleId));
	}

	public static KGameLevelManager getManager() {
		return manager;
	}

	public static KGameLevelModuleExtension getInstance() {
		return instance;
	}
	
	

}
