package com.kola.kmp.logic.vip;

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
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;

public class KVIPDataLoader {
	public static final Logger _LOGGER = KGameLogger.getLogger(KVIPDataLoader.class);

	private KVIPDataLoader() {
	}

	public static void main(String[] args) throws Exception {
		String cfgPath = "./res/gsconfig/vipModule/vipLogicConfig.xml";
		//
		Document doc = XmlUtil.openXml(cfgPath);
		Element root = doc.getRootElement();
		//
		// Element logicE = root.getChild("logicConfig");
		// KSkillConfig.init(logicE);
		//
		Element excelE = root.getChild("excelConfig");
		KVIPDataLoader.goToLoadData(excelE);
		//
		KVIPDataManager.onGameWorldInitComplete();
	}

	static final String SheetName_vip功能 = "vip功能";
	static final String SheetName_vip描述 = "vip描述";

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
		Element tempE = excelE.getChild("vip");
		int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
		KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
		{
			loadVIPLevelDatas(file.getTable(SheetName_vip功能, HeaderIndex));

			loadVIPLevelDescs(file.getTable(SheetName_vip描述, HeaderIndex));
		}
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
	private static void loadVIPLevelDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<VIPLevelData> levelDatas = ReflectPaser.parseExcelData(VIPLevelData.class, table.getHeaderNames(), rows, true);
		KVIPDataManager.mVIPLevelDataManager.init(levelDatas);
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
	private static void loadVIPLevelDescs(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		for (KGameExcelRow row : rows) {
			int lvl = row.getInt("vip_lvl");
			//
			VIPLevelData vipData = KVIPDataManager.mVIPLevelDataManager.getLevelData(lvl);
			if (vipData == null) {
				throw new Exception("加载[" + table.getTableName() + "]错误：不存在对应的等级数据=" + lvl);
			}
			//
			vipData.desc =loadDesc(row, "descrip");
			vipData.newDesc =loadDesc(row, "newDescrip");
		}
	}
	
	private static String loadDesc(KGameExcelRow row, String colName){
		StringBuffer descSbf = new StringBuffer();
		for (int i = 1; i < 100; i++) {
			String key = colName + i;
			if (!row.containsCol(key)) {
				continue;
			}

			String desc = row.getData(key);
			if (desc != null && !desc.isEmpty()) {
				descSbf.append(desc).append('\n');
			}
		}
		if (descSbf.length() > 0) {
			descSbf.deleteCharAt(descSbf.length() - 1);
		}
		return descSbf.toString();
	}
}
