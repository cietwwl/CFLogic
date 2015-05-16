package com.kola.kmp.logic.gang.reswar;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.GameModuleExtension;

public class KResWarModuleExtension implements GameModuleExtension<KResWarModule> {

	public static final Logger _LOGGER = KGameLogger.getLogger(KResWarModuleExtension.class);

	private KResWarModule module;

	public KResWarModule getModule() {
		return module;
	}

	public void init(KResWarModule module, String cfgPath) throws Exception {
		this.module = module;
		//
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到初始化通知，开始加载静态数据------");
		//
		{
			KResWarConfig.init(cfgPath);
			
			Document doc = XmlUtil.openXml(cfgPath);
			Element root = doc.getRootElement();
			//
			Element excelE = root.getChild("excelConfig");
			KResWarDataManager.goToLoadData(excelE);
		}
		//
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成加载静态数据------");
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到游戏启动完成通知，开始验证静态的数据------");
		// CTODO
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成静态数据验证------");
	}

	public void notifyCacheLoadComplete() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到缓存初始化完成通知，开始启动时效任务------");
		// CTODO 未测试，暂时屏蔽
		KResWarDataManager.notifyCacheLoadComplete();
		//
		ResWarDataCenter.notifyCacheLoadComplete();
		//
		ResWarStatusManager.notifyCacheLoadComplete();
		//
		ResWarTaskManager.notifyCacheLoadComplete();

		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成启动时效任务------");
	}

	@Override
	public boolean messageReceived(KGameMessageEvent msgEvent) throws KGameServerException {
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
		// CTODO 未测试，暂时屏蔽
		// 保存数据
		ResWarDataCenter.saveData("");

		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成关服------");
	}
}
