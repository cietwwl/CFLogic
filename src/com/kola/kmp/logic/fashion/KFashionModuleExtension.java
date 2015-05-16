package com.kola.kmp.logic.fashion;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.GameModuleExtension;

public class KFashionModuleExtension implements GameModuleExtension<KFashionModule> {

	public static final Logger _LOGGER = KGameLogger.getLogger(KFashionModuleExtension.class);

	private KFashionModule module;
	
	public KFashionModule getModule() {
		return module;
	}

	public void init(KFashionModule module, String cfgPath) throws Exception{
		this.module = module;
		//
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到初始化通知，开始加载静态数据------");
		//
		{
			Document doc = XmlUtil.openXml(cfgPath);
			Element root = doc.getRootElement();
			//
			Element logicE = root.getChild("logicConfig");
			KFashionConfig.init(logicE);
			//
			Element excelE = root.getChild("excelConfig");
			KFashionDataLoader.goToLoadData(excelE);
		}
		//
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成加载静态数据------");
	}

	@Override
	public void onGameWorldInitComplete() {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到游戏启动完成通知，开始验证静态的数据------");
		// CTODO
		// KGameBoundaryDataManager.mBoundaryDataManager.serverStartCompleted();
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成静态数据验证------");
	}

	public void notifyCacheLoadComplete() throws KGameServerException{
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到缓存初始化完成通知，开始启动时效任务------");
		// 验证数据
		KFashionDataManager.notifyCacheLoadComplete();
		// 启动时效任务
		FashionTaskManager.notifyCacheLoadComplete();
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
}
