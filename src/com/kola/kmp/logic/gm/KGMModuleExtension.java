package com.kola.kmp.logic.gm;

import javax.management.timer.Timer;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayer;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.GameModuleExtension;
import com.kola.kmp.protocol.gm.ProtocolGs;

public class KGMModuleExtension implements GameModuleExtension<KGMModule>, ProtocolGs {

	public static final Logger _LOGGER = KGameLogger.getLogger(KGMModuleExtension.class);

	private KGMModule module;

	public KGMModule getModule() {
		return module;
	}

	public void init(KGMModule module, String cfgPath) throws Exception {
		this.module = module;
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到初始化通知，开始加载静态数据------");
		//
		Document doc = XmlUtil.openXml(cfgPath);
		Element root = doc.getRootElement();
		//
		Element logicE = root.getChild("logicConfig");
		KGMConfig.init(logicE);

		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成加载静态数据------");
	}

	@Override
	public void onGameWorldInitComplete() {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到游戏启动完成通知，开始验证静态的数据------");
		// CTODO
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成静态数据验证------");
	}

	public void notifyCacheLoadComplete() {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到缓存初始化完成通知，开始启动时效任务------");

		GMTaskManager.notifyCacheLoadComplete();

		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成启动时效任务------");
	}

	@Override
	public boolean messageReceived(KGameMessageEvent msgEvent) throws KGameServerException {
		KGamePlayerSession playerSession = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		KGamePlayer player = playerSession.getBoundPlayer();
		if (player == null || player.getID() != KGMConfig.getInstance().gmPlayerId) {
			_LOGGER.error("GM消息异常，非法的GM帐号 playerId=" + (player == null ? "?" : player.getID()) + " Add=" + playerSession.getChannel().getRemoteAddress());
			playerSession.close();
			return false;
		}

		int msgId = msg.getMsgID();
		switch (msgId) {
		case GMS_GS_TCP_ADD_AFFINE:
			KGMLogic.dealMsg_processAffine(msg);
			break;
		case GMS_GS_ORDER:// GM指令
			KGMLogic.dealMsg_processOrder(playerSession, msg);
			break;
		case GMS_GS_TCP_RELAY_EMAIL:
			KGMLogic.dealMsg_processGMCMail(playerSession, msg);
			break;
		case GMS_GS_GM_IN_MAIL_SERVICE:
			KGMLogic.dealMsg_processGMInMailCount(msg);
			break;
		case GMS_GS_RESPONSE_IS_MIAL_SUCCESS:
			KGMLogic.dealMsg_processGMMailConfirm(msg);
			break;
		case GMS_GS_TCP_QUERY_UNION:
			KGMLogic.dealMsg_processDataQuery(playerSession, msg);
			break;
		}
		return true;
	}

	@Override
	public void exceptionCaught(KGameServerException ex) {
		_LOGGER.error(ex.getMessage(), ex);
	}

	public void playerLogouted(KGamePlayerSession playerSession) {
	}

	public void playerLogined(KGamePlayerSession playerSession) {
		if (playerSession.getBoundPlayer().getID() != KGMConfig.getInstance().gmPlayerId) {
			return;
		}
		GMTaskManager.GMMailCacheClearTask.submitTast(5 * Timer.ONE_SECOND);
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 收到关服通知------");
		// CEND 服务器关闭,要做些什么操作??
		_LOGGER.warn("----- " + getModule().getModuleName() + " extension 完成关服------");
	}
}
