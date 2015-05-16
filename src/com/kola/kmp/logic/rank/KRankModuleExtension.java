package com.kola.kmp.logic.rank;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.GameModuleExtension;
import com.kola.kmp.logic.rank.gang.KGangRankLogic;
import com.kola.kmp.logic.rank.teampvp.KTeamPVPRankLogic;
import com.kola.kmp.logic.support.KSupportFactory;

public class KRankModuleExtension implements GameModuleExtension<KRankModule> {

	public static final Logger _LOGGER = KGameLogger.getLogger(KRankModuleExtension.class);

	private KRankModule module;

	public KRankModule getModule() {
		return module;
	}

	public void init(KRankModule module, String cfgPath) throws Exception {
		this.module = module;
		//
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到初始化通知，开始加载静态数据------");
		{
			Document doc = XmlUtil.openXml(cfgPath);
			Element root = doc.getRootElement();
			//
			Element logicE = root.getChild("logicConfig");
			KRankConfig.init(logicE);

			KGangRankLogic.init(logicE.getChild("gang"));

			KTeamPVPRankLogic.init(logicE.getChild("tempPVP"));
			//
			Element excelE = root.getChild("excelConfig");
			KRankDataLoader.goToLoadData(excelE);
		}
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成加载静态数据------");
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到游戏启动完成通知，开始验证静态的数据------");

		KSupportFactory.getVIPModuleSupport().addVipUpLvListener(new KRankVIPListener());

		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成静态数据验证------");
	}

	public void notifyCacheLoadComplete() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到缓存初始化完成通知，开始启动时效任务------");

		KRankDataManager.notifyCacheLoadComplete();

		// 开服加载排行榜
		KRankLogic.loadRankFromDB();

		RankTaskManager.notifyCacheLoadComplete();

		// 通知军团排行榜
		KGangRankLogic.notifyCacheLoadComplete();

		// 通知天梯排行榜
		KTeamPVPRankLogic.notifyCacheLoadComplete();

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

		// 关服前发布一份排行榜
		KRankLogic.onTimeSignalForPublish(true, true, true);

		// 通知军团排行榜
		KGangRankLogic.serverShutdown();

		// 通知天梯排行榜
		KTeamPVPRankLogic.serverShutdown();

		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成关服------");
	}

}
