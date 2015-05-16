package com.kola.kmp.logic.mount;

import java.util.List;

import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountEquiTemp;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountLv;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountLvMaxAtts;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountResetSPData;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountTemplate;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountUpBigLvData;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountUpLvData;

public class KMountDataLoader {
	public static final Logger _LOGGER = KGameLogger.getLogger(KMountDataLoader.class);

	private KMountDataLoader() {
	}

	static final String SheetName_机甲信息 = "机甲信息";
	static final String SheetName_机甲培养经验与属性比例 = "机甲培养经验与属性比例";
	static final String SheetName_机甲培养属性上限 = "机甲培养属性上限";
	static final String SheetName_机甲培养材料 = "机甲培养材料";
	static final String SheetName_机甲进阶条件 = "机甲进阶条件";
	static final String SheetName_机甲装备打造 = "机甲装备打造";
	static final String SheetName_战斗相关 = "战斗相关";
	static final String SheetName_重置sp点花费 = "重置sp点花费";

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
			Element tempE = excelE.getChild("mount");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
			{
				KMountDataManager.mMountTemplateManager.initData(loadExcelDatas(KMountTemplate.class, file.getTable(SheetName_机甲信息, HeaderIndex)));

				KMountDataManager.mMountLvDataManager.initData(loadExcelDatas(KMountLv.class, file.getTable(SheetName_机甲培养经验与属性比例, HeaderIndex)));
				
				KMountDataManager.mMountLvMaxDataManager.initData(loadExcelDatas(KMountLvMaxAtts.class, file.getTable(SheetName_机甲培养属性上限, HeaderIndex)));
				
				KMountDataManager.mMountUpLvDataManager.initData(loadExcelDatas(KMountUpLvData.class, file.getTable(SheetName_机甲培养材料, HeaderIndex)));
				
				KMountDataManager.mMountUpBigLvDataManager.initData(loadExcelDatas(KMountUpBigLvData.class, file.getTable(SheetName_机甲进阶条件, HeaderIndex)));
				
				KMountDataManager.mMountEquiDataManager.initData(loadExcelDatas(KMountEquiTemp.class, file.getTable(SheetName_机甲装备打造, HeaderIndex)));
				
				KMountDataManager.mMountResetSPDataManager.initData(loadExcelDatas(KMountResetSPData.class, file.getTable(SheetName_重置sp点花费, HeaderIndex)));
				
			}
		}
	}

	private static <T> List<T> loadExcelDatas(Class<T> clazz, KGameExcelTable table)throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		return ReflectPaser.parseExcelData(clazz, table.getHeaderNames(), rows, true);
	}
}
