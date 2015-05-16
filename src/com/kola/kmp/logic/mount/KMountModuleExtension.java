package com.kola.kmp.logic.mount;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.mount.MountModule;
import com.kola.kgame.cache.mount.impl.KAMountModuleExtension;
import com.kola.kgame.cache.mount.impl.KMountModule;
import com.kola.kmp.logic.npc.KNPCConfig;
import com.kola.kmp.logic.npc.KNPCDataLoader;

public class KMountModuleExtension extends KAMountModuleExtension {

	public void init(MountModule module, String cfgPath) throws Exception {
		super.init(module, cfgPath);
		//
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到初始化通知，开始加载静态数据------");
		{
			Document doc = XmlUtil.openXml(cfgPath);
			Element root = doc.getRootElement();
			//
			Element logicE = root.getChild("logicConfig");
			KMountConfig.init(logicE);
			//
			Element excelE = root.getChild("excelConfig");
			KMountDataLoader.goToLoadData(excelE);
		}
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成加载静态数据------");
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到游戏启动完成通知，开始验证静态的数据------");

		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成静态数据验证------");
	}

	public void notifyCacheLoadComplete() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到缓存初始化完成通知，开始启动时效任务------");

		KMountDataManager.notifyCacheLoadComplete();

		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成启动时效任务------");
	}

	@Override
	public boolean messageReceived(KGameMessageEvent msgEvent) throws KGameServerException {
		// 忽略
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

	public KMountSet newMountSet(long roleId, byte job, boolean isFirstNew) {
		return new KMountSet(roleId, job, isFirstNew);
	}

	public static KMountSet getMountSet(long roleId) {
		return (KMountSet) KMountModule.instance.getMountSet(roleId);
	}
}
