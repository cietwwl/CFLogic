package com.kola.kmp.logic.mount;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;

/**
 * 模块配置表
 * 
 * @author Administrator
 */
public class KMountConfig {
	private static KMountConfig instance;
	
	// 装备固定数量
	public static final int EQUI_FIXED_COUNT = 4;

	/** 金币培养属性次数加值 */
//	public final int GoldTrainAttTimeAddTime;// = 1;

	private KMountConfig(Element logicE) throws KGameServerException {
//		GoldTrainAttTimeAddTime = Integer.parseInt(logicE.getChildTextTrim("GoldTrainAttTimeAddTime"));
//		if (GoldTrainAttTimeAddTime <= 0) {
//			throw new KGameServerException("加载模块配置文件错误：GoldTrainAttTimeAddTime 有误！");
//		}
	}

	public static void init(Element logicE) throws KGameServerException {
		instance = new KMountConfig(logicE);
	}

	public static KMountConfig getInstance() {
		return instance;
	}
}
