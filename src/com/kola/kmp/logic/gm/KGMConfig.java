package com.kola.kmp.logic.gm;

import org.jdom.Element;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.player.KGamePlayer;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KColorFunEnum;

/**
 * <pre>
 * 模块配置表
 * 颜色值请参考{@link KColorFunEnum}
 * 
 * @author CamusHuang
 * @creation 2012-11-14 下午4:10:27
 * </pre>
 */
public class KGMConfig {
	private static KGMConfig instance;
	//
	public final String gmPlayerName;
	public final long gmPlayerId;
	//
	public final long GSStateTaskPeroid;
	

	private KGMConfig(Element logicE) throws KGameServerException {
		// CTODO 技能--模块配置表初始化
		gmPlayerName = logicE.getChildTextTrim("GMPlayerName");
		// gmUserId = Long.parseLong(e.getChildTextTrim("GMAccountId"));
		KGamePlayer player = KGame.getPlayerManager().loadPlayerData(gmPlayerName);
		if (player == null) {
			throw new KGameServerException("GM帐号不存在=" + gmPlayerName);
		}
		gmPlayerId = player.getID();
		//
		GSStateTaskPeroid = UtilTool.parseDHMS(logicE.getChild("Task").getChildTextTrim("GSStateTaskPeroid"));
	}

	public static void init(Element logicE) throws KGameServerException {
		instance = new KGMConfig(logicE);
	}

	public static KGMConfig getInstance() {
		return instance;
	}
}
