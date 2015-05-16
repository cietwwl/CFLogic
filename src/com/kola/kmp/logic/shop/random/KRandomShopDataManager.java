package com.kola.kmp.logic.shop.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.timer.Timer;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.shop.random.KRandomShopDataManager.RandomGoodsManager.RandomGoods;

/**
 * <pre>
 * 本类负责管理本模块的公共数据 
 * 
 * @author CamusHuang
 * @creation 2013-4-8 下午2:39:56
 * </pre>
 */
public class KRandomShopDataManager {
	private KRandomShopDataManager() {
	}

	//
	public static final String randomShopDailySaveDir = "./res/output/randomShopDialy/";
	public static final String randomShopDailySaveFileName = "daily.xml";
	//
	/** 随机商品的数量 */
	public static int RandomGoodsCount;
	/** 随机商品付费刷新价格 */
	public static KCurrencyCountStruct RandomRefreshPrice;
	/** 随机商品免费刷新次数 */
	public static int RandomFreeTime;
	/** 随机商品免费刷新周期(小时) */
	public static int RandomPeriod;
	/** 随机商品的覆盖的最小角色等级 */
	public static int RandomMinRoleLv;
	/** 随机商品购买日志的最大数量 */
	public static int RandomLogMaxCount;

	public static void initConfig(Element logicE) throws KGameServerException {
		RandomGoodsCount = Integer.parseInt(logicE.getChildTextTrim("RandomGoodsCount"));
		if (RandomGoodsCount < 1) {
			throw new KGameServerException("加载模块配置文件错误：RandomGoodsCount 有误！");
		}
		
		{
			Element priceE = logicE.getChild("RandomGoodsRefreshPrice");
			KCurrencyTypeEnum typeEnum = KCurrencyTypeEnum.getEnum(Integer.parseInt(priceE.getAttributeValue("type")));
			long price = Long.parseLong(priceE.getTextTrim());
			if(typeEnum == null || price < 1){
				throw new KGameServerException("加载模块配置文件错误：RandomGoodsRefreshPrice 有误！");
			}
			RandomRefreshPrice = new KCurrencyCountStruct(typeEnum, price);
		}

		RandomFreeTime = Integer.parseInt(logicE.getChildTextTrim("RandomFreeTime"));
		if (RandomFreeTime < 1) {
			throw new KGameServerException("加载模块配置文件错误：RandomFreeTime 有误！");
		}

		RandomPeriod = Integer.parseInt(logicE.getChildTextTrim("RandomPeriod"));
		long DayHours = Timer.ONE_DAY / Timer.ONE_HOUR;// 24小时
		if (RandomPeriod < 1 || RandomPeriod >= DayHours || DayHours % RandomPeriod != 0) {
			throw new KGameServerException("加载模块配置文件错误：RandomPeriod 有误！");
		}

		RandomMinRoleLv = Integer.parseInt(logicE.getChildTextTrim("RandomMinRoleLv"));
		if (RandomMinRoleLv < 1) {
			throw new KGameServerException("加载模块配置文件错误：RandomMinRoleLv 有误！");
		}

		RandomLogMaxCount = Integer.parseInt(logicE.getChildTextTrim("RandomLogMaxCount"));
		if (RandomLogMaxCount < 1) {
			throw new KGameServerException("加载模块配置文件错误：RandomLogMaxCount 有误！");
		}
	}

	// ///////////////////////////////////////////////////

	static final String SheetName_金币物品 = "金币物品";
	static final String SheetName_钻石物品 = "钻石物品";
	static final String SheetName_随从物品 = "随从物品";
	static final String SheetName_VIP物品 = "VIP物品";

	/**
	 * <pre>
	 * 开始加载文件
	 * @param configPath
	 * @author CamusHuang
	 * @creation 2012-11-9 下午7:38:27
	 * </pre>
	 */
	public static void goToLoadData(Element excelE) throws Exception {

		// 加载数据
		Element tempE = excelE.getChild("randomGoods");
		int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
		KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
		{

			loadGoodsDatas(KRandomShopTypeEnum.金币商城, file.getTable(SheetName_金币物品, HeaderIndex));

			loadGoodsDatas(KRandomShopTypeEnum.钻石商城, file.getTable(SheetName_钻石物品, HeaderIndex));
			
			loadGoodsDatas(KRandomShopTypeEnum.随从商城, file.getTable(SheetName_随从物品, HeaderIndex));
			
			loadGoodsDatas(KRandomShopTypeEnum.VIP商城, file.getTable(SheetName_VIP物品, HeaderIndex));
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
	private static void loadGoodsDatas(KRandomShopTypeEnum shopType, KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		try {
			RandomGoodsManager manager = new RandomGoodsManager(shopType);
			List<RandomGoods> datas = ReflectPaser.parseExcelData(RandomGoods.class, table.getHeaderNames(), rows, true);
			manager.init(datas);
			mRandomGoodsManager.put(shopType, manager);
		} catch (Exception e) {
			throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
	}

	// ///////////////////////////////////////////////////
	// ///////////////////////////////////////////////////
	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 随机商品数据管理器
	 * </pre>
	 */
	public static Map<KRandomShopTypeEnum, RandomGoodsManager> mRandomGoodsManager = new HashMap<KRandomShopTypeEnum, RandomGoodsManager>();

	// ///////////////////////////////////////////////////
	// ///////////////////////////////////////////////////
	// ///////////////////////////////////////////////////
	public static class RandomGoodsManager {
		public final KRandomShopTypeEnum shopType;
		/**
		 * <pre>
		 * KEY = 商品ID
		 * </pre>
		 */
		private Map<Integer, RandomGoods> dataMap = new HashMap<Integer, RandomGoods>();
		/**
		 * <pre>
		 * KEY = 角色等级
		 * </pre>
		 */
		private Map<Integer, RandomGoodsForRoleLv> dataMapForLv = new HashMap<Integer, RandomGoodsForRoleLv>();

		public RandomGoodsManager(KRandomShopTypeEnum shopType) {
			this.shopType = shopType;
		}

		public void init(List<RandomGoods> datas) throws KGameServerException {
			for (RandomGoods data : datas) {
				if (shopType == KRandomShopTypeEnum.VIP商城) {
					if (data.mixVIPLvl < 1) {
						throw new KGameServerException("mixVIPLvl 错误, Id = " + data.id);
					}
				} else {
					if (data.mixVIPLvl > 0) {
						throw new KGameServerException("mixVIPLvl 错误, Id = " + data.id);
					}
				}
				dataMap.put(data.index, data);
			}

			// 必须保证每个等级均有N件可选商品
			int maxLv = KRoleModuleConfig.getRoleMaxLv();
			for (int lv = RandomMinRoleLv; lv <= maxLv; lv++) {
				List<RandomGoods> list = searchAllGoods(lv);
				if (list.size() < RandomGoodsCount) {
					throw new KGameServerException("商品数量不足 角色等级=" + lv);
				}
				dataMapForLv.put(lv, new RandomGoodsForRoleLv(lv, list));
			}
			
			
		}

		private List<RandomGoods> searchAllGoods(int roleLv) {
			List<RandomGoods> result = new ArrayList<RandomGoods>();
			for (RandomGoods data : dataMap.values()) {
				if (data.mixlvl <= roleLv && roleLv <= data.maxlvl) {
					result.add(data);
				}
			}
			return result;
		}

		/**
		 * <pre>
		 * 随机出商品列表
		 * 
		 * @param roleLv
		 * @return
		 * @author CamusHuang
		 * @creation 2014-4-16 下午5:50:43
		 * </pre>
		 */
		public LinkedHashMap<Integer, RandomGoods> randomGoods(int roleLv) {

			roleLv = Math.min(roleLv, KRoleModuleConfig.getRoleMaxLv());
			roleLv = Math.max(roleLv, RandomMinRoleLv);

			RandomGoodsForRoleLv data = dataMapForLv.get(roleLv);
			return data.randomGoods();
		}

		/**
		 * <pre>
		 * 验证数据/跨模块操作
		 * 
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-4-25 下午4:26:38
		 * </pre>
		 */
		public void notifyCacheLoadComplete() throws KGameServerException {
			for (RandomGoods data : dataMap.values()) {
				data.notifyCacheLoadComplete(shopType);
			}
		}

		/**
		 * <pre>
		 * 某个角色等级对应的随机商店商品
		 * 
		 * @author CamusHuang
		 * @creation 2014-11-12 下午8:47:29
		 * </pre>
		 */
		private static class RandomGoodsForRoleLv {
			private int roleLv;
			private List<RandomGoods> goodsList;
			private int allRate;

			private RandomGoodsForRoleLv(int roleLv, List<RandomGoods> goodsList) {
				this.roleLv = roleLv;
				this.goodsList = goodsList;
				for (RandomGoods goods : goodsList) {
					allRate += goods.pro;
				}
			}

			private LinkedHashMap<Integer, RandomGoods> randomGoods() {
				LinkedHashMap<Integer, RandomGoods> result = new LinkedHashMap<Integer, RandomGoods>();

				int tempRate = allRate;
				for (int i = 0; i < RandomGoodsCount; i++) {
					int rate = UtilTool.random(1, tempRate);
					for (RandomGoods goods : goodsList) {
						if (result.containsKey(goods.index)) {
							// 已经被选出
							continue;
						}
						rate -= goods.pro;
						if (rate < 1) {
							result.put(goods.index, goods);
							tempRate -= goods.pro;
							break;
						}
					}
				}
				if(result.isEmpty()){
					return result;
				}
				
				List<RandomGoods> resultList = new ArrayList<RandomGoods>(result.values());
				result.clear();
				Collections.sort(resultList);
				for(RandomGoods goods:resultList){
					result.put(goods.index, goods);
				}
				return result;
			}
		}

		/**
		 * <pre>
		 * 
		 * @author CamusHuang
		 * @creation 2014-2-21 下午3:10:02
		 * </pre>
		 */
		public static class RandomGoods implements Comparable<RandomGoods>{
			// ----------以下是EXCEL表格直导数据---------
			public int index;// 序号
			private int id;// 物品ID
			private int quantity;// 物品数量
			public int mixlvl;// 最低等级
			public int maxlvl;// 最高等级
			public int pro;// 权重
			private int type;// 货币类型--无视
			private int money;// 原价
			private int sale;// 折后价
			private int news;// 购买信息显示
			public int mixVIPLvl;// 最低购买VIP等级

			// ----------以下是逻辑数据---------
			public boolean isLog;// 是否记录
			public ItemCountStruct itemStruct;
			public KCurrencyCountStruct orgPrice;
			public KCurrencyCountStruct salePrice;

			public String desc;

			private void notifyCacheLoadComplete(KRandomShopTypeEnum shopType) throws KGameServerException {
				if (news == 1) {
					isLog = true;
				}
				KCurrencyTypeEnum moneyType = KCurrencyTypeEnum.getEnum(type);
				if (shopType.moneyType != null && moneyType != shopType.moneyType) {
					throw new KGameServerException("数值错误 type = " + type);
				}
				if (mixlvl < 1) {
					throw new KGameServerException("数值错误 mixlvl = " + mixlvl);
				}
				if (maxlvl < 1) {
					throw new KGameServerException("数值错误 maxlvl = " + maxlvl);
				}
				if (pro < 1) {
					throw new KGameServerException("数值错误 pro = " + pro);
				}
				if (mixVIPLvl < 0) {
					throw new KGameServerException("数值错误 mixVIPLvl = " + mixVIPLvl);
				}
				{
					if (quantity < 1) {
						throw new KGameServerException("数值错误 quantity = " + quantity);
					}
					itemStruct = new ItemCountStruct(id + "", quantity);
					if (itemStruct.getItemTemplate() == null) {
						throw new KGameServerException("数值错误 id = " + id);
					}
				}
				{
					if (money < 1) {
						throw new KGameServerException("数值错误 money = " + money);
					}
					orgPrice = new KCurrencyCountStruct(moneyType, money);
					if (sale < 1) {
						throw new KGameServerException("数值错误 sale = " + sale);
					}
					salePrice = new KCurrencyCountStruct(moneyType, sale);

					if (sale >= money) {
						throw new KGameServerException("数值错误 money=" + money + " sale=" + sale);
					}
				}
				// CTODO 其它约束检查
			}

			@Override
			public int compareTo(RandomGoods o) {
				if (index < o.index) {
					return -1;
				}
				if (index > o.index) {
					return 1;
				}
				return 0;
			}

		}
	}

	/**
	 * <pre>
	 * 服务器启动完成
	 * 
	 * @author CamusHuang
	 * @creation 2012-12-3 下午8:41:04
	 * </pre>
	 */
	static void notifyCacheLoadComplete() throws KGameServerException {
		try {
			for (RandomGoodsManager data : mRandomGoodsManager.values()) {
				data.notifyCacheLoadComplete();
			}
		} catch (Exception e) {
			throw new KGameServerException("加载[随机商品]错误：" + e.getMessage(), e);
		}
	}
}
