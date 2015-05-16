package com.kola.kmp.logic.gang.war;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.GameModuleExtension;
import com.kola.kmp.logic.support.KSupportFactory;

public class KGangWarModuleExtension implements GameModuleExtension<KGangWarModule> {

	private KGangWarModule module;

	public KGangWarModule getModule() {
		return module;
	}

	public void init(KGangWarModule module, String cfgPath) throws Exception {
		this.module = module;
		//
		GangWarLogic.GangWarLogger.warn("----- " + getModule().getModuleName() + " extension 收到初始化通知，开始加载静态数据------");
		//
		{
			KGangWarConfig.init(cfgPath);

			Document doc = XmlUtil.openXml(cfgPath);
			Element root = doc.getRootElement();
			//
			Element excelE = root.getChild("excelConfig");
			KGangWarDataManager.goToLoadData(excelE);
		}
		//
		GangWarLogic.GangWarLogger.warn("----- " + getModule().getModuleName() + " extension 完成加载静态数据------");
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		GangWarLogic.GangWarLogger.warn("----- " + getModule().getModuleName() + " extension 收到游戏启动完成通知，开始验证静态的数据------");
		// CTODO
		GangWarLogic.GangWarLogger.warn("----- " + getModule().getModuleName() + " extension 完成静态数据验证------");
	}

	public void notifyCacheLoadComplete() throws KGameServerException {
		GangWarLogic.GangWarLogger.warn("----- " + getModule().getModuleName() + " extension 收到缓存初始化完成通知，开始启动时效任务------");

		KGangWarConfig.getInstance().notifyCacheLoadComplete();
		KGangWarDataManager.notifyCacheLoadComplete();
		GangWarDataCenter.notifyCacheLoadComplete();
		// 初始化军团战的状态
		GangWarStatusManager.notifyCacheLoadComplete();
		GangWarTaskManager.notifyCacheLoadComplete();
		
		// 注册HP血量接口
		KSupportFactory.getCombatModuleSupport().registerCombatHpUpdater(new PVPRoleSideHpUpdater());
		KSupportFactory.getCombatModuleSupport().registerCombatHpUpdater(new PVERoleSideHpUpdater());

		GangWarLogic.GangWarLogger.warn("----- " + getModule().getModuleName() + " extension 完成启动时效任务------");
	}

	@Override
	public boolean messageReceived(KGameMessageEvent msgEvent) throws KGameServerException {
		return false;
	}

	@Override
	public void exceptionCaught(KGameServerException ex) {
		GangWarLogic.GangWarLogger.error(ex.getMessage(), ex);
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		GangWarLogic.GangWarLogger.warn("----- " + getModule().getModuleName() + " extension 收到关服通知------");
		// CEND 服务器关闭,要做些什么操作??

		// 保存数据
		GangWarDataCenter.saveData("");

		GangWarLogic.GangWarLogger.warn("----- " + getModule().getModuleName() + " extension 完成关服------");
	}
}
