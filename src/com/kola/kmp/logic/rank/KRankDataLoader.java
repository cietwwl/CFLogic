package com.kola.kmp.logic.rank;

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
import com.kola.kmp.logic.rank.KRankDataManager.KRankGoodPriceManager;
import com.kola.kmp.logic.rank.KRankDataStructs.KGangRankGoodReward;
import com.kola.kmp.logic.rank.KRankDataStructs.KRankGoodPrice;

public class KRankDataLoader {
	public static final Logger _LOGGER = KGameLogger.getLogger(KRankDataLoader.class);

	private KRankDataLoader() {
	}

	public static void main(String[] args) throws Exception {
		String cfgPath = "./res/gsconfig/rankModule/rankLogicConfig.xml";
		//
		Document doc = XmlUtil.openXml(cfgPath);
		Element root = doc.getRootElement();
		//
		Element logicE = root.getChild("logicConfig");
		//
		Element excelE = root.getChild("excelConfig");
		KRankDataLoader.goToLoadData(excelE);

		//
		KRankDataManager.notifyCacheLoadComplete();
	}

	static final String SheetName_个人排行榜点赞表 = "个人排行榜点赞表";
	static final String SheetName_军团战力榜点赞排名奖励 = "军团排行榜";
	static final String SheetName_军团点赞表 = "军团点赞表";

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
			Element tempE = excelE.getChild("good");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
			{
				loadGoodDatas(file.getTable(SheetName_个人排行榜点赞表, HeaderIndex), KRankDataManager.mRoleRankGoodPriceManager);
				
				loadGoodDatas(file.getTable(SheetName_军团点赞表, HeaderIndex), KRankDataManager.mGangRankGoodPriceManager);

				loadGangGoodDatas(file.getTable(SheetName_军团战力榜点赞排名奖励, HeaderIndex));
			}
		}
	}

	private static void loadGoodDatas(KGameExcelTable table, KRankGoodPriceManager mRankGoodPriceManager) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<KRankGoodPrice> result = ReflectPaser.parseExcelData(KRankGoodPrice.class, table.getHeaderNames(), rows, true);
		mRankGoodPriceManager.initData(result);
	}
	
	private static void loadGangGoodDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<KGangRankGoodReward> result = ReflectPaser.parseExcelData(KGangRankGoodReward.class, table.getHeaderNames(), rows, true);
		KRankDataManager.mGangGoodRewardManager.initData(result);
	}
}
