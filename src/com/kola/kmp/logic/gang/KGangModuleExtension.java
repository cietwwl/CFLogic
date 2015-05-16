package com.kola.kmp.logic.gang;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.CreateGangData;
import com.kola.kgame.cache.gang.Gang;
import com.kola.kgame.cache.gang.GangExtCASet;
import com.kola.kgame.cache.gang.GangIntegrateData;
import com.kola.kgame.cache.gang.GangModule;
import com.kola.kgame.cache.gang.impl.KAGangModuleExtension;
import com.kola.kgame.cache.gang.impl.KGangModule;
import com.kola.kmp.logic.rank.gang.KGangRankLogic;

public class KGangModuleExtension extends KAGangModuleExtension {

	private static KGangModuleExtension instance;

	public static KGangModuleExtension instance() {
		return instance;
	}

	public KGangModuleExtension() {
		instance = this;
	}

	public void init(GangModule module, String cfgPath) throws Exception {
		super.init(module, cfgPath);
		//
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到初始化通知，开始加载静态数据------");

		{
			Document doc = XmlUtil.openXml(cfgPath);
			Element root = doc.getRootElement();
			//
			Element logicE = root.getChild("logicConfig");
			KGangConfig.init(logicE);
			//
			Element excelE = root.getChild("excelConfig");
			KGangDataLoader.goToLoadData(excelE);
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
		
		// 验证数据
		KGangDataManager.notifyCacheLoadComplete();
		
		// 重置一次跨天数据
		//KGangLogic.notifyForDayTask();

		// 启动时效
		KGangTaskManager.notifyCacheLoadComplete();

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

		

		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成关服------");
	}

	@Override
	public Gang newGang() {
		return new KGang();
	}

	public GangExtCASet newGangExtCASet() {
		return new KGangExtCASet();
	}

	public static KGang getGang(long gangId) {
		return (KGang) KGangModule.instance.getGang(gangId);
	}

	public static KGangExtCASet getGangExtCASet(long gangId) {
		return (KGangExtCASet) KGangModule.instance.getGangExtCASet(gangId);
	}

	public static GangIntegrateData getGangAndSet(long gangId) {
		return KGangModule.instance.getGangAndSet(gangId);
	}

	static class CreateGangDataImpl implements CreateGangData {

		private Gang gang;
		private GangExtCASet gangExtCASet;

		CreateGangDataImpl(Gang gang, GangExtCASet gangExtCASet) {
			this.gang = gang;
			this.gangExtCASet = gangExtCASet;
		}

		@Override
		public Gang getGang() {
			return gang;
		}

		@Override
		public GangExtCASet getGangExtCASet() {
			return gangExtCASet;
		}

	}
}
