package com.kola.kmp.logic.item;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.item.ItemModule;
import com.kola.kgame.cache.item.impl.KAItemModuleExtension;
import com.kola.kgame.cache.item.impl.KItemModule;
import com.kola.kmp.logic.item.listener.KItemListenerManager;
import com.kola.kmp.logic.util.EnumStrTool;

public class KItemModuleExtension extends KAItemModuleExtension {

	private static KItemModuleExtension instance;

	public static KItemModuleExtension instance() {
		return instance;
	}

	public KItemModuleExtension() {
		instance = this;
	}

	@Override
	public void init(ItemModule module, String cfgPath) throws Exception {
		super.init(module, cfgPath);
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到初始化通知，开始加载静态数据------");
		//
		{
			Document doc = XmlUtil.openXml(cfgPath);
			Element root = doc.getRootElement();
			//
			Element logicE = root.getChild("logicConfig");
			KItemConfig.init(logicE);
			//
			Element excelE = root.getChild("excelConfig");
			KItemDataLoader.goToLoadData(excelE);
		}
		//
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成加载静态数据------");
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到游戏启动完成通知，开始验证静态的数据------");
		// 验证数据
		KItemConfig.onGameWorldInitComplete();
		KItemDataManager.onGameWorldInitComplete();
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成静态数据验证------");
	}

	public void notifyCacheLoadComplete() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到缓存初始化完成通知，开始启动时效任务------");
		
		KItemListenerManager.serverStartCompleted();
		//
		ItemGlobalDataImpl.instance.load();
		//
		ItemTaskManager.notifyCacheLoadComplete();
		
		//
		EnumStrTool.notifyCacheLoadComplete();
		
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
		
		ItemGlobalDataImpl.instance.save();
		
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成关服------");
	}

	public KItemSet newItemSet(long roleId, boolean isFirstNew) {
		return new KItemSet(roleId, isFirstNew);
	}

	public static KItemSet getItemSet(long roleId) {
		return (KItemSet) KItemModule.instance.getItemSet(roleId);
	}
}
