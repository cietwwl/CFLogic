package com.kola.kmp.logic.gm;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.KGamePlayerSessionListener;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.GameModule;

public final class KGMModule implements GameModule, KGamePlayerSessionListener {

	public static final Logger _LOGGER = KGameLogger.getLogger(KGMModule.class);

	public static KGMModule instance;

	private String _moduleName;
	private int _msgLowerId;
	private int _msgUpperId;
	private KGMModuleExtension extension;

	public KGMModule() {
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
		extension = (KGMModuleExtension) Class.forName(className).newInstance();
		//
		String logicCfgPath = extensionE.getChildTextTrim("configPath");
		extension.init(this, logicCfgPath);

		_LOGGER.warn("----- " + getModuleName() + " 完成初始化------");
	}

	public KGMModuleExtension getExtension() {
		return extension;
	}

	@Override
	public void notifyCacheLoadComplete() {
		extension.notifyCacheLoadComplete();
	}

	@Override
	public void onGameWorldInitComplete() {
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

	@Override
	public void playerLogined(KGamePlayerSession playerSession) {
		// CTODO Auto-generated method stub
		extension.playerLogined(playerSession);
	}

	@Override
	public void playerSessionDisconnected(KGamePlayerSession playerSession, int cause) {
		// CTODO Auto-generated method stub
	}

	@Override
	public void playerLogouted(KGamePlayerSession playerSession, int cause) {
		extension.playerLogouted(playerSession);
	}

	@Override
	public void playerSessionReconnected(KGamePlayerSession playerSession) {
	}
}
