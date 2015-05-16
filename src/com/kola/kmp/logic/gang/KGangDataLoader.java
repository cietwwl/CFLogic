package com.kola.kmp.logic.gang;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kmp.logic.gang.KGangDataStruct.GangContributionData;
import com.kola.kmp.logic.gang.KGangDataStruct.GangGoodsData;
import com.kola.kmp.logic.gang.KGangDataStruct.GangLevelData;
import com.kola.kmp.logic.gang.KGangDataStruct.GangProsperityData;
import com.kola.kmp.logic.gang.KGangDataStruct.GangTechTemplate;
import com.kola.kmp.logic.skill.KSkillConfig;

/**
 * <pre>
 * 本类负责加载数据
 * 
 * @author CamusHuang
 * @creation 2013-1-11 上午11:03:40
 * </pre>
 */
public class KGangDataLoader {
	private KGangDataLoader() {
	}

	public static void main(String[] args) throws Exception {
		String cfgPath = "./res/gsconfig/gangModule/gangLogicConfig.xml";
		//
		Document doc = XmlUtil.openXml(cfgPath);
		Element root = doc.getRootElement();
		//
		Element logicE = root.getChild("logicConfig");
		KSkillConfig.init(logicE);
		//
		Element excelE = root.getChild("excelConfig");
		KGangDataLoader.goToLoadData(excelE);

		//
		KGangDataManager.notifyCacheLoadComplete();
	}

	static final String SheetName_军团等级 = "军团等级";
	static final String SheetName_军团商店 = "军团商店";
	static final String SheetName_军团科技 = "军团科技";
	static final String SheetName_军团捐献 = "军团捐献";
	static final String SheetName_军团繁荣度 = "军团繁荣度";

	/**
	 * <pre>
	 * 开始加载文件
	 * 
	 * @param excelE
	 * @throws Exception
	 * @author CamusHuang
	 * @creation 2013-1-12 上午11:03:52
	 * </pre>
	 */
	public static void goToLoadData(Element excelE) throws Exception {

		// 加载数据
		Element tempE = excelE.getChild("gang");
		int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
		KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
		{
			loadGangLevelDatas(file.getTable(SheetName_军团等级, HeaderIndex));

			loadGangTechDatas(file.getTable(SheetName_军团科技, HeaderIndex));

			loadGangGoodsDatas(file.getTable(SheetName_军团商店, HeaderIndex));

			loadGangContributionDatas(file.getTable(SheetName_军团捐献, HeaderIndex));
			
			loadGangProsperityData(file.getTable(SheetName_军团繁荣度, HeaderIndex));
		}
	}

	private static void loadGangLevelDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<GangLevelData> result = ReflectPaser.parseExcelData(GangLevelData.class, table.getHeaderNames(), rows, true);
		KGangDataManager.mGangLevelDataManager.initData(result);
	}

	private static void loadGangTechDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<GangTechTemplate> result = ReflectPaser.parseExcelData(GangTechTemplate.class, table.getHeaderNames(), rows, true);
		KGangDataManager.mGangTechDataManager.initData(result);
	}

	private static void loadGangGoodsDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<GangGoodsData> result = ReflectPaser.parseExcelData(GangGoodsData.class, table.getHeaderNames(), rows, true);
		KGangDataManager.mGangGoodsDataManager.initData(result);
	}

	private static void loadGangContributionDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<GangContributionData> result = ReflectPaser.parseExcelData(GangContributionData.class, table.getHeaderNames(), rows, true);
		KGangDataManager.mGangContributionDataManager.initData(result);
	}
	
	private static void loadGangProsperityData(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length != 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数只能为1！");
		}

		List<GangProsperityData> result = ReflectPaser.parseExcelData(GangProsperityData.class, table.getHeaderNames(), rows, true);
		KGangDataManager.mGangProsperityData=result.get(0);
	}
}
