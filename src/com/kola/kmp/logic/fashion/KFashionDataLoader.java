package com.kola.kmp.logic.fashion;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kmp.logic.fashion.KFashionDataStructs.KFashionTemplate;

public class KFashionDataLoader {
	public static final Logger _LOGGER = KGameLogger.getLogger(KFashionDataLoader.class);

	private KFashionDataLoader() {
	}

	public static void main(String[] args) throws Exception {
		String cfgPath = "./res/gsconfig/fashionModule/fashionLogicConfig.xml";
		//
		Document doc = XmlUtil.openXml(cfgPath);
		Element root = doc.getRootElement();
		//
		Element logicE = root.getChild("logicConfig");
		//
		Element excelE = root.getChild("excelConfig");
		KFashionDataLoader.goToLoadData(excelE);

		//
		KFashionDataManager.notifyCacheLoadComplete();
	}

	static final String SheetName_时装模板_时装 = "时装";

	/**
	 * <pre>
	 * 开始加载文件
	 * @param configPath
	 * @author CamusHuang
	 * @creation 2012-11-9 下午7:38:27
	 * </pre>
	 */
	static void goToLoadData(Element excelE) throws Exception {

		// 加载数据
		{
			Element tempE = excelE.getChild("fashionTemplate");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
			{
				loadTemplateDatas(file.getTable(SheetName_时装模板_时装, HeaderIndex));

			}
		}
		
		KFashionDataManager.dataLoadFinishedNotify();
	}

	/**
	 * <pre>
	 * 
	 * @param rows
	 * @throws Exception
	 * @throws Exception
	 * @author CamusHuang
	 * @creation 2013-1-24 下午12:14:53
	 * </pre>
	 */
	private static void loadTemplateDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<KFashionTemplate> result = ReflectPaser.parseExcelData(KFashionTemplate.class, table.getHeaderNames(), rows, true);

		KFashionDataManager.mFashionTemplateManager.initDatas(result);
	}
}
