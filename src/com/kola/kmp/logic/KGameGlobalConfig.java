package com.kola.kmp.logic;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.mission.guide.KGuideManager;
import com.kola.kmp.logic.mission.guide.MainMenuFunction;
import com.kola.kmp.logic.other.KColorFunEnum.KColorManager;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.other.KPetQuality;
import com.kola.kmp.logic.util.text.ColorSignFilter;

/**
 * 
 * @author PERRY CHAN
 */
public class KGameGlobalConfig {
	
	private static boolean _openCheatCmd = true;
	private static boolean _vipEnable = true;
	private static int _basicHitRating;

	public static void init(String configPath) throws KGameServerException {
		Document doc = XmlUtil.openXml(configPath);
		Element root = doc.getRootElement();

		Element excelE = root.getChild("excelConfig");
		goToLoadData(excelE);
		
		@SuppressWarnings("unchecked")
		List<Element> children = root.getChild("gameGlobalConfig").getChildren("config");
		ReflectPaser.parse(KGameGlobalConfig.class, children);
	}
	
	public static void loadGlobalConfig(Element element) {
		@SuppressWarnings("unchecked")
		List<Element> children = element.getChildren();
		ReflectPaser.parse(KGameGlobalConfig.class, children);
	}

	public static void onGameWorldInitComplete() {
		KPetQuality.initColor();
		MainMenuFunction function = KGuideManager.getMainMenuFunctionInfoMap().get(KFunctionTypeEnum.VIP特权.functionId);
		_vipEnable = (function.getOpenRoleLevelLimit() < 999);
	}

	private static final String SheetName_颜色表 = "颜色表";
	private static final String SheetName_跑马灯 = "跑马灯";

	private static void goToLoadData(Element excelE) throws KGameServerException {

		try {
			// 加载颜色定义
			Element tempE = excelE.getChild("colorExcel");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
			{
				KColorManager.loadColors(file.getTable(SheetName_颜色表, HeaderIndex));
				KGameExcelTable.setColorSignFilter(new ColorSignFilter());
				ReflectPaser.setColorSignFilter(new ColorSignFilter());
			}

			//
			// 加载跑马灯
			tempE = excelE.getChild("broadcastExcel");
			HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			file = new KGameExcelFile(tempE.getChildTextTrim("path"));
			{
				KWordBroadcastType.init(file.getTable(SheetName_跑马灯, HeaderIndex));
			}
		} catch (KGameServerException e) {
			throw e;
		} catch (Exception e) {
			throw new KGameServerException(e.getMessage(), e);
		}
	}

	public static boolean isOpenCheatCmd() {
		return _openCheatCmd;
	}

	public static int getBasicHitRating() {
		return _basicHitRating;
	}
	
	public static boolean isVipEnable() {
		return _vipEnable;
	}
}
