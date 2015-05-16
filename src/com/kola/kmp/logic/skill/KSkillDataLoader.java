package com.kola.kmp.logic.skill;

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
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.SkillStatusData;

public class KSkillDataLoader {
	public static final Logger _LOGGER = KGameLogger.getLogger(KSkillDataLoader.class);

	private KSkillDataLoader() {
	}

	public static void main(String[] args) throws Exception {
		String cfgPath = "./res/gsconfig/skillModule/skillLogicConfig.xml";
		//
		Document doc = XmlUtil.openXml(cfgPath);
		Element root = doc.getRootElement();
		//
		Element logicE = root.getChild("logicConfig");
		KSkillConfig.init(logicE);
		//
		Element excelE = root.getChild("excelConfig");
		KSkillDataLoader.goToLoadData(excelE);

		//
		KSkillDataManager.onGameWorldInitComplete();
	}

	static final String SheetName_角色主动技能模板 = "主动技能";
	static final String SheetName_角色被动技能模板 = "被动技能";
	static final String SheetName_机甲技能模板 = "坐骑技能";
	static final String SheetName_宠物技能模板 = "随从技能";
	static final String SheetName_宠物被动技能模板 = "随从被动技能";
	static final String SheetName_怪物技能模板 = "怪物技能";
	static final String SheetName_技能状态数据 = "状态数据表";
	static final String SheetName_技能召唤物数据 = "召唤物表";

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
		Element tempE = excelE.getChild("skillTemplate");
		int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
		KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
		{
			loadRoleIniSkillTemplateDatas(file.getTable(SheetName_角色主动技能模板, HeaderIndex));

			loadRolePasSkillTemplateDatas(file.getTable(SheetName_角色被动技能模板, HeaderIndex));

			loadMountSkillTemplateDatas(file.getTable(SheetName_机甲技能模板, HeaderIndex));
			
			loadPetSkillTemplateDatas(file.getTable(SheetName_宠物技能模板, HeaderIndex));
			
			loadPetPasSkillTemplateDatas(file.getTable(SheetName_宠物被动技能模板, HeaderIndex));
			
			loadMonsterSkillTemplateDatas(file.getTable(SheetName_怪物技能模板, HeaderIndex));

			loadSkillStatusDatas(file.getTable(SheetName_技能状态数据, HeaderIndex));
		}
		
		tempE = excelE.getChild("minionTemplate");
		HeaderIndex = Integer.parseInt(tempE.getChildText("HeaderIndex"));
		file = new KGameExcelFile(tempE.getChildTextTrim("path"));
		{
			loadMinionData(file.getTable(SheetName_技能召唤物数据, HeaderIndex));
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
	private static void loadRoleIniSkillTemplateDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<KRoleIniSkillTemp> result = ReflectPaser.parseExcelData(KRoleIniSkillTemp.class, table.getHeaderNames(), rows, true);
		KSkillDataManager.mRoleIniSkillTempManager.initData(result);
	}

	private static void loadRolePasSkillTemplateDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<KRolePasSkillTemp> result = ReflectPaser.parseExcelData(KRolePasSkillTemp.class, table.getHeaderNames(), rows, true);
		KSkillDataManager.mRolePasSkillTempManager.initData(result);
	}

	private static void loadMountSkillTemplateDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<KRoleIniSkillTemp> result = ReflectPaser.parseExcelData(KRoleIniSkillTemp.class, table.getHeaderNames(), rows, true);
		KSkillDataManager.mMountSkillTempManager.initData(result);
	}

	private static void loadPetSkillTemplateDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<KRoleIniSkillTemp> result = ReflectPaser.parseExcelData(KRoleIniSkillTemp.class, table.getHeaderNames(), rows, true);
		KSkillDataManager.mPetSkillTempManager.initData(result);
	}
	
	private static void loadPetPasSkillTemplateDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<KRolePasSkillTemp> result = ReflectPaser.parseExcelData(KRolePasSkillTemp.class, table.getHeaderNames(), rows, true);
		KSkillDataManager.mPetPasSkillTempManager.initData(result);
	}
	
	private static void loadMonsterSkillTemplateDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<KRoleIniSkillTemp> result = ReflectPaser.parseExcelData(KRoleIniSkillTemp.class, table.getHeaderNames(), rows, true);
		KSkillDataManager.mMonsterSkillTempManager.initData(result);
	}

	private static void loadSkillStatusDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<SkillStatusData> result = ReflectPaser.parseExcelData(SkillStatusData.class, table.getHeaderNames(), rows, true);
		KSkillDataManager.mSkillStatusManager.initData(result);
	}
	
	private static void loadMinionData(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<KMinionTemplateData> result = ReflectPaser.parseExcelData(KMinionTemplateData.class, table.getHeaderNames(), rows, true);
		KSkillDataManager.mSkillMinionManager.initData(result);
	}
}
