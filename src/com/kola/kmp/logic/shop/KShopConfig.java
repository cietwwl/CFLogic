package com.kola.kmp.logic.shop;

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
public class KShopConfig {
	private static KShopConfig instance;
	//

	private KShopConfig(Element logicE) throws KGameServerException {
		// CTODO 技能--模块配置表初始化
		
	}

	public static void init(Element logicE) throws KGameServerException {
		instance = new KShopConfig(logicE);
	}

	public static KShopConfig getInstance() {
		return instance;
	}
}
