package com.kola.kmp.logic.shop;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.GameModule;

public final class KShopModule implements GameModule {

	public static final Logger _LOGGER = KGameLogger.getLogger(KShopModule.class);

	public static KShopModule instance;

	private String _moduleName;
	private int _msgLowerId;
	private int _msgUpperId;
	private KShopModuleExtension extension;

	public KShopModule() {
		instance = this;
	}

	@Override
	public void init(String cfgPath, String pModuleName, int pMsgLowerId, int pMsgUpperId) throws Exception {
		this._moduleName = pModuleName;
		this._msgLowerId = pMsgLowerId;
		this._msgUpperId = pMsgUpperId;
		//
		_LOGGER.warn("----- " + getModuleName() + " 收到初始化通知------");
		//
		Document doc = XmlUtil.openXml(cfgPath);
		Element root = doc.getRootElement();
		//
		Element extensionE = root.getChild("extension");
		String className = extensionE.getAttributeValue("classPath");
		extension = (KShopModuleExtension) Class.forName(className).newInstance();
		//
		String logicCfgPath = extensionE.getChildTextTrim("configPath");
		extension.init(this, logicCfgPath);

		_LOGGER.warn("----- " + getModuleName() + " 完成初始化------");
	}

	public KShopModuleExtension getExtension() {
		return extension;
	}

	@Override
	public void notifyCacheLoadComplete() throws KGameServerException {
		extension.notifyCacheLoadComplete();
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		extension.onGameWorldInitComplete();
	}

	@Override
	public boolean messageReceived(KGameMessageEvent msgEvent) throws KGameServerException {
		if (msgEvent.getMessage().getMsgID() < _msgLowerId || msgEvent.getMessage().getMsgID() > _msgUpperId) {
			return false;
		}
		return extension.messageReceived(msgEvent);
	}

	@Override
	public void exceptionCaught(KGameServerException ex) {
		extension.exceptionCaught(ex);
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		extension.serverShutdown();
	}

	public String getModuleName() {
		return _moduleName;
	}
}
