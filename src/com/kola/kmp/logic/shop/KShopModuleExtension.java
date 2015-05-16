package com.kola.kmp.logic.shop;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.GameModuleExtension;
import com.kola.kmp.logic.shop.random.KRandomShopCenter;
import com.kola.kmp.logic.shop.random.KRandomShopDataManager;
import com.kola.kmp.logic.shop.timehot.KHotShopCenter;
import com.kola.kmp.logic.shop.timehot.KHotShopDataManager;

public class KShopModuleExtension implements GameModuleExtension<KShopModule> {

	public static final Logger _LOGGER = KGameLogger.getLogger(KShopModuleExtension.class);

	private KShopModule module;

	public KShopModule getModule() {
		return module;
	}

	public void init(KShopModule module, String cfgPath) throws Exception {
		this.module = module;
		//
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到初始化通知，开始加载静态数据------");
		//
		{
			Document doc = XmlUtil.openXml(cfgPath);
			Element root = doc.getRootElement();
			//
			Element logicE = root.getChild("logicConfig");
			KShopConfig.init(logicE);
			
			Element RandomShopConfig = logicE.getChild("RandomShopConfig");
			KRandomShopDataManager.initConfig(RandomShopConfig);
			
			Element HotShopConfig = logicE.getChild("TimeHotShopConfig");
			KHotShopDataManager.initConfig(HotShopConfig);
			//
			Element excelE = root.getChild("excelConfig");
			KShopDataLoader.goToLoadData(excelE);
			
			Element RandomShopExcelConfig= excelE.getChild("RandomShopExcelConfig");
			KRandomShopDataManager.goToLoadData(RandomShopExcelConfig);
			
			Element TimeHotShopExcelConfig= excelE.getChild("TimeHotShopExcelConfig");
			KHotShopDataManager.goToLoadData(TimeHotShopExcelConfig);
			
		}
		//
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成加载静态数据------");
	}

	@Override
	public void onGameWorldInitComplete() {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到游戏启动完成通知，开始验证静态的数据------");
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成静态数据验证------");
	}

	public void notifyCacheLoadComplete() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到缓存初始化完成通知，开始启动时效任务------");

		KShopDataManager.notifyCacheLoadComplete();
		
		KShopTaskManager.notifyCacheLoadComplete();
		
		KRandomShopCenter.notifyCacheLoadComplete();
		
		KHotShopCenter.notifyCacheLoadComplete();
		
		PhyPowerShopCenter.notifyCacheLoadComplete();

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
		
		KRandomShopCenter.serverShutdown();
		
		KHotShopCenter.serverShutdown();
		
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成关服------");
	}
}
