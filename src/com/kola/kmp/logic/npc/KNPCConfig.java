package com.kola.kmp.logic.npc;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;

/**
 * <pre>
 * 技能--模块配置表
 * 
 * @author CamusHuang
 * @creation 2012-11-14 下午4:10:27
 * </pre>
 */
public class KNPCConfig {
	private static KNPCConfig instance;

	private KNPCConfig(Element logicE) throws KGameServerException {
	}

	public static void init(Element logicE) throws KGameServerException {
		instance = new KNPCConfig(logicE);
	}

	public static KNPCConfig getInstance() {
		return instance;
	}
}
