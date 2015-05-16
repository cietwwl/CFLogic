package com.kola.kmp.logic.fashion;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.kola.kgame.cache.util.UtilTool;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-11-14 下午4:10:27
 * </pre>
 */
public class KFashionConfig {

	private static KFashionConfig instance;
	
	// 时装超时失效扫描周期
	public final long TimeOutScanTaskPeriod;

	// 时装自动穿戴的最小角色等级
	public final int FashionAutoSelectedMinRoleLv;
	
	private KFashionConfig(Element logicE) throws KGameServerException {
		
		TimeOutScanTaskPeriod = UtilTool.parseDHMS(logicE.getChildTextTrim("TimeOutScanTaskPeriod"));
		
		FashionAutoSelectedMinRoleLv = Integer.parseInt(logicE.getChildTextTrim("FashionAutoSelectedMinRoleLv"));
	}

	public static void init(Element logicE) throws KGameServerException {
		instance = new KFashionConfig(logicE);
	}

	public static KFashionConfig getInstance() {
		return instance;
	}
}
