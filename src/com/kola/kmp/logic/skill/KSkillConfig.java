package com.kola.kmp.logic.skill;

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
public class KSkillConfig {
	private static KSkillConfig instance;
	//
	/** 角色能带进战斗的技能数量 */
	public final int MaxSkillForWar;

	private KSkillConfig(Element logicE) throws KGameServerException {
		// 技能--模块配置表初始化
		MaxSkillForWar = Integer.parseInt(logicE.getChildTextTrim("MaxSkillForWar"));
		if (MaxSkillForWar <= 0) {
			throw new KGameServerException("加载模块配置文件错误：MaxSkillForWar 有误！");
		}
	}

	public static void init(Element logicE) throws KGameServerException {
		instance = new KSkillConfig(logicE);
	}

	public static KSkillConfig getInstance() {
		return instance;
	}
}
