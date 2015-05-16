package com.kola.kmp.logic.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kmp.logic.item.KItemDataStructs.KBagExtData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiAttExtData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiBuyEnchansePrice;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiInheritData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiQualitySetData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStarAttExtData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStarMaterialData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStarRateData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStarSetData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStoneSetData2;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStrongAttExtData;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStrongPriceParam;
import com.kola.kmp.logic.item.KItemDataStructs.KEquiStrongSetData;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempConsume;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempEqui;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempEquiBox;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempFixedBox;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempMaterial;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempRandomBox;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempStone;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;

public class KItemDataLoader {
	public static final Logger _LOGGER = KGameLogger.getLogger(KItemDataLoader.class);

	private KItemDataLoader() {
	}

	private static String ItemTempExcelURL;
	private static int ItemTempExcelHeaderIndex;

	static final String SheetName_道具模板_装备 = "装备";
	static final String SheetName_道具模板_材料 = "材料";
	static final String SheetName_道具模板_宝石 = "宝石";
	static final String SheetName_道具模板_消耗品 = "消耗品";
	static final String SheetName_道具模板_固定宝箱 = "固定宝箱";
	static final String SheetName_道具模板_随机宝箱 = "随机宝箱";
	static final String SheetName_道具模板_装备包 = "装备包";
	//
	static final String SheetName_背包_扩容表 = "Sheet1";
	//
	static final String SheetName_装备玩法_强化部位参数 = "强化部位参数";
	static final String SheetName_装备玩法_属性比例参数 = "属性比例参数";//
	static final String SheetName_装备玩法_强化等级属性比例 = "强化等级属性比例";//
	static final String SheetName_装备玩法_升星等级属性比例 = "升星等级属性比例";//

	static final String SheetName_装备玩法_升星材料 = "升星材料消耗";
	static final String SheetName_装备玩法_升星数据 = "升星材料成功率";//
	static final String SheetName_装备玩法_装备继承 = "装备继承";
	static final String SheetName_装备玩法_装备开孔 = "装备开孔";
	//
	static final String SheetName_装备套装_宝石套装 = "宝石套装";
	static final String SheetName_装备套装_升星套装 = "升星套装";
	static final String SheetName_装备套装_强化套装 = "强化套装";
	static final String SheetName_装备套装_品质套装 = "装备套装";

	/**
	 * <pre>
	 * 重新加载以下类型的道具模板
	 * 消耗品(3), 固定宝箱(5),随机宝箱(8), 装备包(9);
	 * 并且会与现有数据对比，保存新增的模板。
	 * 不删除现有数据，不修改现有数据。
	 * 
	 * @author CamusHuang
	 * @creation 2014-11-16 下午12:08:00
	 * </pre>
	 */
	public static String reloadNewItemTemplates() {
		// 加载数据
		try {
			KGameExcelFile file = new KGameExcelFile(ItemTempExcelURL);
			
			String[] sheetNames = new String[]{SheetName_道具模板_消耗品, SheetName_道具模板_固定宝箱, SheetName_道具模板_随机宝箱, SheetName_道具模板_装备包};
			Class[] classes = new Class[]{KItemTempConsume.class,KItemTempFixedBox.class,KItemTempRandomBox.class,KItemTempEquiBox.class};
			
			List<KItemTempAbs> allNewTemps = new ArrayList<KItemTempAbs>();
			for(int index=0;index<sheetNames.length;index++) {
				String sheetName=sheetNames[index];
				Class clazz = classes[index];
				List<KItemTempAbs> temps = reloadItemTemplateDatas(clazz, file.getTable(sheetName, ItemTempExcelHeaderIndex));
				tickExistTemp(temps);
				if(!temps.isEmpty()){
					//有新的模板
					for (KItemTempAbs temp : temps) {
						temp.dataLoadFinishedNotify();
						temp.onGameWorldInitComplete();
					}
					
					allNewTemps.addAll(temps);
				}
			}
			
			if(!allNewTemps.isEmpty()){
				KItemDataManager.mItemTemplateManager.addDatas(allNewTemps);
				return "成功加载"+allNewTemps.size()+"个新道具";
			}
			
			return "不存在新道具";
			
		} catch(Exception e){
			_LOGGER.error(e.getMessage(), e);
			return "发生异常："+e.getMessage();
		}
	}
	
	/**
	 * <pre>
	 * 将已存在于内存中的模板剔除，保留不存在的模板
	 * 
	 * @param temps
	 * @author CamusHuang
	 * @creation 2014-11-16 下午12:17:44
	 * </pre>
	 */
	private static void tickExistTemp(List<KItemTempAbs> temps){ 
		for(Iterator<KItemTempAbs> it=temps.iterator();it.hasNext();){
			KItemTempAbs temp = it.next();
			if(KItemDataManager.mItemTemplateManager.containTemplate(temp.id)){
				it.remove();
			}
		}
	}

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
			Element tempE = excelE.getChild("itemTemplate");
			ItemTempExcelURL = tempE.getChildTextTrim("path");
			ItemTempExcelHeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));

			KGameExcelFile file = new KGameExcelFile(ItemTempExcelURL);
			{
				loadItemTemplateDatas(KItemTempEqui.class, file.getTable(SheetName_道具模板_装备, ItemTempExcelHeaderIndex));

				loadItemTemplateDatas(KItemTempMaterial.class, file.getTable(SheetName_道具模板_材料, ItemTempExcelHeaderIndex));

				loadItemTemplateDatas(KItemTempStone.class, file.getTable(SheetName_道具模板_宝石, ItemTempExcelHeaderIndex));

				loadItemTemplateDatas(KItemTempConsume.class, file.getTable(SheetName_道具模板_消耗品, ItemTempExcelHeaderIndex));

				loadItemTemplateDatas(KItemTempFixedBox.class, file.getTable(SheetName_道具模板_固定宝箱, ItemTempExcelHeaderIndex));

				loadItemTemplateDatas(KItemTempRandomBox.class, file.getTable(SheetName_道具模板_随机宝箱, ItemTempExcelHeaderIndex));

				loadItemTemplateDatas(KItemTempEquiBox.class, file.getTable(SheetName_道具模板_装备包, ItemTempExcelHeaderIndex));
			}
		}
		{
			Element tempE = excelE.getChild("bagExt");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
			{
				loadBagExtDatas(file.getTable(SheetName_背包_扩容表, HeaderIndex));
			}
		}
		{
			Element tempE = excelE.getChild("equiSystem");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
			{

				loadEquiStrongPriceParamData(file.getTable(SheetName_装备玩法_强化部位参数, HeaderIndex));

				loadEquiStrongAttExtData(file.getTable(SheetName_装备玩法_强化等级属性比例, HeaderIndex));

				loadEquiStarMetrialData(file.getTable(SheetName_装备玩法_升星材料, HeaderIndex));

				loadEquiStarData(file.getTable(SheetName_装备玩法_升星数据, HeaderIndex));

				loadEquiAttExtData(file.getTable(SheetName_装备玩法_属性比例参数, HeaderIndex));

				loadEquiStarAttExtData(file.getTable(SheetName_装备玩法_升星等级属性比例, HeaderIndex));

				loadEquiInheritata(file.getTable(SheetName_装备玩法_装备继承, HeaderIndex));
				
				loadEquiBuyEnchanseData(file.getTable(SheetName_装备玩法_装备开孔, HeaderIndex));
			}
		}
		{
			Element tempE = excelE.getChild("equiSetSystem");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
			{
				loadEquiStoneSetData(file.getTable(SheetName_装备套装_宝石套装, HeaderIndex));

				loadEquiStarSetData(file.getTable(SheetName_装备套装_升星套装, HeaderIndex));

				loadEquiStrongSetData(file.getTable(SheetName_装备套装_强化套装, HeaderIndex));

				loadEquiQualitySetData(file.getTable(SheetName_装备套装_品质套装, HeaderIndex));
			}
		}

		//
		KItemDataManager.dataLoadFinishedNotify();
	}

	private static void loadBagExtDatas(KGameExcelTable table) throws KGameServerException {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new KGameServerException("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		try {
			List<KBagExtData> result = ReflectPaser.parseExcelData(KBagExtData.class, table.getHeaderNames(), rows, true);

			KItemDataManager.mBagExtDataManager.initDatas(result);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
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
	private static void loadItemTemplateDatas(Class tempClazz, KGameExcelTable table) throws KGameServerException {
		List<KItemTempAbs> result = reloadItemTemplateDatas(tempClazz, table);

		try {
			KItemDataManager.mItemTemplateManager.addDatas(result);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
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
	private static List<KItemTempAbs> reloadItemTemplateDatas(Class tempClazz, KGameExcelTable table) throws KGameServerException {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new KGameServerException("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		try {
			return ReflectPaser.parseExcelData(tempClazz, table.getHeaderNames(), rows, true);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
	}

	private static void loadEquiStrongPriceParamData(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}
		try {
			List<KEquiStrongPriceParam> result = ReflectPaser.parseExcelData(KEquiStrongPriceParam.class, table.getHeaderNames(), rows, true);

			KItemDataManager.mEquiStrongPriceParamManager.initDatas(result);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
	}
	
	private static void loadEquiBuyEnchanseData(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}
		try {
			List<KEquiBuyEnchansePrice> datas = new ArrayList<KEquiBuyEnchansePrice>();
			for(KGameExcelRow row:rows){
				KEquiBuyEnchansePrice price = new KEquiBuyEnchansePrice();
				price.HoleID = row.getInt("HoleID");
				BaseRewardData reward = BaseRewardData.loadData(row, false);
				if(reward.moneyList.size()>1){
					throw new Exception("价格只能填一个");
				}
				if(reward.itemStructs.size()>1){
					throw new Exception("消费物品只能填一个");
				}
				if(!reward.itemStructs.isEmpty()){
					price.payItem = reward.itemStructs.get(0);
				}
				if(!reward.moneyList.isEmpty()){
					price.payMoney = reward.moneyList.get(0);
				}
				datas.add(price);
			}

			KItemDataManager.mEquiBuyEnchanseDataManager.initDatas(datas);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
	}

	private static void loadEquiStrongAttExtData(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}
		try {
			List<KEquiStrongAttExtData> result = ReflectPaser.parseExcelData(KEquiStrongAttExtData.class, table.getHeaderNames(), rows, true);

			KItemDataManager.mEquiStrongAttExtDataManager.initDatas(result);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
	}

	private static void loadEquiStarMetrialData(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}
		try {
			List<KEquiStarMaterialData> result = ReflectPaser.parseExcelData(KEquiStarMaterialData.class, table.getHeaderNames(), rows, true);

			KItemDataManager.mEquiStarMetrialDataManager.initDatas(result);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
	}

	private static void loadEquiStarData(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}
		try {
			List<KEquiStarRateData> result = ReflectPaser.parseExcelData(KEquiStarRateData.class, table.getHeaderNames(), rows, true);

			KItemDataManager.mEquiStarRateManager.initDatas(result);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
	}

	private static void loadEquiAttExtData(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}
		try {
			List<KEquiAttExtData> result = ReflectPaser.parseExcelData(KEquiAttExtData.class, table.getHeaderNames(), rows, true);

			KItemDataManager.mEquiAttExtDataManager.initDatas(result);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
	}

	private static void loadEquiStarAttExtData(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}
		try {
			List<KEquiStarAttExtData> result = ReflectPaser.parseExcelData(KEquiStarAttExtData.class, table.getHeaderNames(), rows, true);

			KItemDataManager.mEquiStarAttExtManager.initDatas(result);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
	}

	private static void loadEquiInheritata(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}
		try {
			List<KEquiInheritData> result = ReflectPaser.parseExcelData(KEquiInheritData.class, table.getHeaderNames(), rows, true);

			KItemDataManager.mEquiInheritDataManager.initDatas(result);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
	}

	private static void loadEquiStarSetData(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}
		try {
			List<KEquiStarSetData> result = ReflectPaser.parseExcelData(KEquiStarSetData.class, table.getHeaderNames(), rows, true);

			KItemDataManager.mEquiStarSetDataManager.initDatas(result);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
	}

	private static void loadEquiStoneSetData(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}
		try {
			List<KEquiStoneSetData2> result = ReflectPaser.parseExcelData(KEquiStoneSetData2.class, table.getHeaderNames(), rows, true);

			KItemDataManager.mEquiStoneSetDataManager2.initDatas(result);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
	}

	private static void loadEquiStrongSetData(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}
		try {
			List<KEquiStrongSetData> result = ReflectPaser.parseExcelData(KEquiStrongSetData.class, table.getHeaderNames(), rows, true);

			KItemDataManager.mEquiStrongSetDataManager.initDatas(result);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
	}

	private static void loadEquiQualitySetData(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}
		try {
			List<KEquiQualitySetData> result = ReflectPaser.parseExcelData(KEquiQualitySetData.class, table.getHeaderNames(), rows, true);

			KItemDataManager.mEquiQualitySetDataManager.initDatas(result);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
	}
}
