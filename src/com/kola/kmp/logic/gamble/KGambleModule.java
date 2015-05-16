package com.kola.kmp.logic.gamble;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.GameModule;
import com.kola.kmp.logic.gamble.peopleguess.KPeopleGuessMonitor;
import com.kola.kmp.logic.gamble.wish.KWishSystemManager;
import com.kola.kmp.logic.gamble.wish2.KWish2Manager;

public class KGambleModule implements GameModule {
	public static final Logger _LOGGER = KGameLogger
			.getLogger(KGambleModule.class);

	private static KWishSystemManager wishManager;
	private static KWish2Manager wish2Manager;

	private String _moduleName;
	private int _msgLowerId;
	private int _msgUpperId;

	private String moduleConfigPath;

	@Override
	public void init(String cfgPath, String pModuleName, int pMsgLowerId,
			int pMsgUpperId) throws Exception {
		this._moduleName = pModuleName;
		this._msgLowerId = pMsgLowerId;
		this._msgUpperId = pMsgUpperId;

		moduleConfigPath = cfgPath;
		//
		_LOGGER.warn("----- " + getModuleName() + " 收到初始化通知------");
		//
		_LOGGER.info("！！！博彩模块加载开始！！！");
		Document doc = XmlUtil.openXml(cfgPath);
		if (doc != null) {
			Element root = doc.getRootElement();
			String wishXmlFilePath = root.getChildText("wishConfigPath");
			wishManager = new KWishSystemManager();
			wishManager.init(wishXmlFilePath);
			String wish2XmlFilePath = root.getChildText("wish2ConfigPath");
			wish2Manager = new KWish2Manager();
			wish2Manager.init(wish2XmlFilePath);
			
			String peopleGuessFilePath = root.getChildText("peopleGuessConfigPath");
			KPeopleGuessMonitor.getMonitor().init(peopleGuessFilePath);
		} else {
			throw new NullPointerException("博彩模块配置不存在！！");
		}

		_LOGGER.warn("----- " + getModuleName() + " 完成初始化------");

	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		wishManager.serverStartCompleted();
		KPeopleGuessMonitor.getMonitor().initManager();
		KPeopleGuessMonitor.getMonitor().onGameWorldInitComplete();
	}

	@Override
	public void notifyCacheLoadComplete() throws KGameServerException {

	}

	@Override
	public boolean messageReceived(KGameMessageEvent msgEvent)
			throws KGameServerException {
		return false;
	}

	@Override
	public void exceptionCaught(KGameServerException ex) {

	}

	@Override
	public void serverShutdown() throws KGameServerException {
		wishManager.serverShutdown();
		KPeopleGuessMonitor.getMonitor().maneger.processServerShutDown();
	}

	@Override
	public String getModuleName() {
		return _moduleName;
	}

	/**
	 * 获取许愿系统管理器实例
	 * 
	 * @return
	 */
	public static KWishSystemManager getWishSystemManager() {
		return wishManager;
	}

	/**
	 * 
	 * @return
	 */
	public static KWish2Manager getWish2Manager() {
		return wish2Manager;
	}
	
	

}
