package com.kola.kmp.logic.relationship;

import java.util.List;

import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kmp.logic.relationship.KRelationShipDataStructs.RSPushData;

public class KRelationShipDataLoader {
	public static final Logger _LOGGER = KGameLogger.getLogger(KRelationShipDataLoader.class);

	private KRelationShipDataLoader() {
	}

	static final String SheetName_好友推送 = "好友推送";

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
		Element tempE = excelE.getChild("friendPush");
		int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
		KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
		{
			loadRSPushDatas(file.getTable(SheetName_好友推送, HeaderIndex));
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
	private static void loadRSPushDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<RSPushData> rsPushDatas = ReflectPaser.parseExcelData(RSPushData.class, table.getHeaderNames(), rows, true);
		KRelationShipDataManager.mRSPushDataManager.init(rsPushDatas);
	}
}
