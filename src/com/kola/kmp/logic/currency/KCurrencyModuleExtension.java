package com.kola.kmp.logic.currency;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.currency.CurrencyModule;
import com.kola.kgame.cache.currency.impl.KACurrencyModuleExtension;
import com.kola.kgame.cache.currency.impl.KCurrencyModule;
import com.kola.kmp.logic.item.KItemConfig;
import com.kola.kmp.logic.item.KItemDataLoader;

public class KCurrencyModuleExtension extends KACurrencyModuleExtension {

	private static KCurrencyModuleExtension instance;

	public static KCurrencyModuleExtension instance() {
		return instance;
	}

	public KCurrencyModuleExtension() {
		instance = this;
	}

	public void init(CurrencyModule module, String cfgPath) throws Exception {
		super.init(module, cfgPath);
		//
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到初始化通知，开始加载静态数据------");
		//
		{
			Document doc = XmlUtil.openXml(cfgPath);
			Element root = doc.getRootElement();
			//
			Element logicE = root.getChild("logicConfig");
			KCurrencyConfig.init(logicE);
			//
			Element excelE = root.getChild("excelConfig");
			KCurrencyDataManager.goToLoadData(excelE);
		}
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成加载静态数据------");
	}

	@Override
	public void onGameWorldInitComplete() {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到游戏启动完成通知，开始验证静态的数据------");
		// CTODO
		// KGameBoundaryDataManager.mBoundaryDataManager.serverStartCompleted();
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成静态数据验证------");
	}

	public void notifyCacheLoadComplete() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到缓存初始化完成通知，开始启动时效任务------");

		KCurrencyDataManager.notifyCacheLoadComplete();

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

	public KCurrencyAccountSet newCurrencyAccountSet(long roleId, boolean isFirstNew) {
		return new KCurrencyAccountSet(roleId, isFirstNew);
	}

	public static KCurrencyAccountSet getCurrencyAccountSet(long roleId) {
		return (KCurrencyAccountSet) KCurrencyModule.instance.getCurrencyAccountSet(roleId);
	}

}
