package com.kola.kmp.logic.npc;

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
import com.kola.kmp.logic.npc.KNPCDataStructs.KDropInfoTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.KNPCTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.MonstUIData;
import com.kola.kmp.logic.npc.KNPCDataStructs.ObstructionTemplate;

public class KNPCDataLoader {
	public static final Logger _LOGGER = KGameLogger.getLogger(KNPCDataLoader.class);

	private KNPCDataLoader() {
	}

	public static void main(String[] args) throws Exception {
		String cfgPath = "./res/gsconfig/npcModule/npcLogicConfig.xml";
		//
		Document doc = XmlUtil.openXml(cfgPath);
		Element root = doc.getRootElement();
		//
		Element logicE = root.getChild("logicConfig");
		KNPCConfig.init(logicE);
		//
		Element excelE = root.getChild("excelConfig");
		KNPCDataLoader.goToLoadData(excelE);

		//
		KNPCDataManager.onGameWorldInitComplete();
	}

	static final String SheetName_NPC模板 = "NPC模板表";
	static final String SheetName_怪物模板 = "怪物模板";
	static final String SheetName_怪物形象表 = "怪物形象表";
	static final String SheetName_战场障碍物数据表 = "陷阱及障碍物表";
	static final String SheetName_掉落数据表 = "掉落数据表";

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
			Element tempE = excelE.getChild("npcTemplate");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
			{
				loadNPCTemplateDatas(file.getTable(SheetName_NPC模板, HeaderIndex));
			}
		}
		{
			Element tempE = excelE.getChild("monstTemplate");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
			{
				loadMonstTemplateDatas(file.getTable(SheetName_怪物模板, HeaderIndex));
			}
		}
		{
			Element tempE = excelE.getChild("monstUITemplate");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
			{
				loadMonstUIDatas(file.getTable(SheetName_怪物形象表, HeaderIndex));
			}
		}
		{
			Element tempE = excelE.getChild("ObstructionTemp");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
			{
				loadObstructionTempDatas(file.getTable(SheetName_战场障碍物数据表, HeaderIndex));
			}
		}
		{
			Element tempE = excelE.getChild("dropInfoTemplate");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
			{
				loadDropInfoTemplateDatas(file.getTable(SheetName_掉落数据表, HeaderIndex));
			}
		}
	}

	private static void loadNPCTemplateDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<KNPCTemplate> result = ReflectPaser.parseExcelData(KNPCTemplate.class, table.getHeaderNames(), rows, true);
		KNPCDataManager.mNPCTemplateManager.initData(result);
	}

	private static void loadMonstTemplateDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<KMonstTemplate> result = ReflectPaser.parseExcelData(KMonstTemplate.class, table.getHeaderNames(), rows, true);
		KNPCDataManager.mMonstTemplateManager.initData(result);
	}

	private static void loadMonstUIDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<MonstUIData> result = ReflectPaser.parseExcelData(MonstUIData.class, table.getHeaderNames(), rows, true);
		KNPCDataManager.mMonstUIDataManager.initData(result);
	}

	private static void loadObstructionTempDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<ObstructionTemplate> result = ReflectPaser.parseExcelData(ObstructionTemplate.class, table.getHeaderNames(), rows, true);
		KNPCDataManager.mObstructionTempDataManager.initData(result);
	}
	
	private static void loadDropInfoTemplateDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}
		
		List<KDropInfoTemplate> result = ReflectPaser.parseExcelData(KDropInfoTemplate.class, table.getHeaderNames(), table.getAllDataRows(), false);
		KNPCDataManager.mDropInfoTempDataMamanger.initData(result);
	}
}
