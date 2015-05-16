package com.kola.kmp.logic.relationship;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.other.KRelationShipTypeEnum;

/**
 * <pre>
 * 模块配置表
 * 颜色值请参考{@link KColorFunEnum}
 * 
 * @author CamusHuang
 * @creation 2012-11-14 下午4:10:27
 * </pre>
 */
public class KRelationShipConfig {
	private static KRelationShipConfig instance;

	// 亲密度最大值
	public final int ClosenessMaxValue;

	// 好友副本亲密度增值
	public final int ClosenessForWarWin;
	public final int ClosenessForWarLose;
	public final int ClosenessForPMChat;
	
	final String 切磋PVP地图文件名;
	final int 切磋PVP地图背景音乐;
	final int 切磋名单最大数量;

	private KRelationShipConfig(Element logicE) throws KGameServerException {
		{
			Element e_enum = logicE.getChild("TypeEnum");
			for (KRelationShipTypeEnum type : KRelationShipTypeEnum.values()) {
				int maxNum = Integer.parseInt(e_enum.getChildTextTrim(type.name()));
				type.setMaxNum(maxNum);
				if (maxNum < 10) {
					throw new KGameServerException("maxNum 数据错误，关系类型=" + type.name());
				}
			}
		}

		ClosenessMaxValue = Integer.parseInt(logicE.getChildTextTrim("ClosenessMaxValue"));
		if (ClosenessMaxValue < 1) {
			throw new KGameServerException("ClosenessMaxValue 数据错误");
		}

		ClosenessForWarWin = Integer.parseInt(logicE.getChildTextTrim("ClosenessForWarWin"));
		if (ClosenessForWarWin < 0) {
			throw new KGameServerException("ClosenessForWarWin 数据错误");
		}

		ClosenessForWarLose = Integer.parseInt(logicE.getChildTextTrim("ClosenessForWarLose"));
		if (ClosenessForWarLose < 0) {
			throw new KGameServerException("ClosenessForWarLose 数据错误");
		}

		ClosenessForPMChat = Integer.parseInt(logicE.getChildTextTrim("ClosenessForPMChat"));
		if (ClosenessForPMChat < 0) {
			throw new KGameServerException("ClosenessForPMChat 数据错误");
		}
		
		切磋PVP地图文件名 = logicE.getChildTextTrim("切磋PVP地图文件名");
		切磋PVP地图背景音乐 = Integer.parseInt(logicE.getChildTextTrim("切磋PVP地图背景音乐"));
		
		切磋名单最大数量 = Integer.parseInt(logicE.getChildTextTrim("切磋名单最大数量"));
		if (切磋名单最大数量 > Byte.MAX_VALUE) {
			throw new KGameServerException("切磋名单最大数量 不能大于" + Byte.MAX_VALUE);
		}
	}

	public static void init(Element logicE) throws KGameServerException {
		instance = new KRelationShipConfig(logicE);
	}

	public static KRelationShipConfig getInstance() {
		return instance;
	}
}
