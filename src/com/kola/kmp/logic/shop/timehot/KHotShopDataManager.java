package com.kola.kmp.logic.shop.timehot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.jdom.Element;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.fashion.KFashionDataStructs.KFashionTemplate;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempEqui;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempFixedBox;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempRandomBox;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KItemTypeEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.pet.KPetTemplate;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.shop.timehot.KHotShopDataManager.HotShopManager.HotGoods;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.CommonActivityTime;
import com.kola.kmp.logic.util.CommonActivityTime.CATime;
import com.kola.kmp.logic.util.KGameUtilTool;

/**
 * <pre>
 * 本类负责管理本模块的公共数据 
 * 
 * @author CamusHuang
 * @creation 2013-4-8 下午2:39:56
 * </pre>
 */
public class KHotShopDataManager {
	private KHotShopDataManager() {
	}

	// //
	// /** 商品显示的数量 */
	// public static int ShowGoodsCount;

	public static void initConfig(Element logicE) throws KGameServerException {
		// ShowGoodsCount = Integer.parseInt(logicE
		// .getChildTextTrim("ShowGoodsCount"));
		// if (ShowGoodsCount < 1) {
		// throw new KGameServerException("加载模块配置文件错误：ShowGoodsCount 有误！");
		// }
	}

	// ///////////////////////////////////////////////////
	private static String excelPath;
	private static int HeaderIndex;
	//
	static final String SheetName_各服控制 = "各服控制";

	/**
	 * <pre>
	 * 开始加载文件
	 * @param configPath
	 * @author CamusHuang
	 * @creation 2012-11-9 下午7:38:27
	 * </pre>
	 */
	public static void goToLoadData(Element excelE) throws Exception {
		Element tempE = excelE.getChild("goods");
		// 加载数据
		excelPath = tempE.getChildTextTrim("path");
		HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));

		reloadData(false);
	}

	static void reloadData(boolean isReload) throws Exception {

		HotShopManager tempShopManager = new HotShopManager();

		// 开服当天的0点
		final long ServerStartDay = UtilTool.getNextNDaysStart(KGame.getGSFirstStartTime(), 0).getTimeInMillis();
		final long nowTime = System.currentTimeMillis();

		KGameExcelFile file = new KGameExcelFile(excelPath);
		{
			KGameExcelTable table = file.getTable(SheetName_各服控制, HeaderIndex);
			KGameExcelRow[] rows = KGameUtilTool.filterRowsByGSID(table);
			if (rows.length != 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数必须为1！");
			}
			//
			{
				List<String> headerNames = table.getHeaderNames();
				ReflectPaser.parseExcelData(tempShopManager, headerNames, rows[0], false, false);
			}
		}
		{
			Map<KHotShopTypeEnum, Map<Integer, HotGoods>> allGoods = new HashMap<KHotShopTypeEnum, Map<Integer, HotGoods>>();

			for (KHotShopTypeEnum type : KHotShopTypeEnum.values()) {
				KGameExcelTable table = file.getTable(type.name(), HeaderIndex);
				List<String> headerNames = table.getHeaderNames();
				KGameExcelRow[] rows = table.getAllDataRows();

				Map<Integer, HotGoods> goodsMap = new HashMap<Integer, HotGoods>();
				for (KGameExcelRow row : rows) {
					HotGoods goods = new HotGoods();
					ReflectPaser.parseExcelData(goods, headerNames, row, false, false);
					CommonActivityTime activityTime = null;
					try {
						activityTime = CommonActivityTime.load(ServerStartDay, nowTime, row);
						
						if (activityTime.getAllTime().size() != 1) {
							throw new Exception("加载[" + table.getTableName() + "]错误:" + "必须填且只填一组有效时间参数");
						}
						if(activityTime.effectTimes.isEmpty()){
							goods.caTime = activityTime.getAllTime().get(0);
						} else {
							goods.caTime = activityTime.effectTimes.get(0);
						}
					} catch (Exception e) {
						throw new Exception("加载[" + table.getTableName() + "]错误:" + e.getMessage() + ", row=" + row.getIndexInFile(), e);
					}
					//
					if (goodsMap.put(goods.index, goods) != null) {
						throw new KGameServerException("加载[" + table.getTableName() + "]错误:" + ">商品重复 index=" + goods.index);
					}
				}

				allGoods.put(type, goodsMap);
			}

			tempShopManager.initDatas(allGoods);
		}

		if (isReload) {
			notifyCacheLoadComplete(tempShopManager);
		}

		mHotShopManager = tempShopManager;
	}

	// ///////////////////////////////////////////////////
	// ///////////////////////////////////////////////////
	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 商城数据管理器
	 * </pre>
	 */
	public static HotShopManager mHotShopManager;

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 服务器启动完成
	 * 
	 * @author CamusHuang
	 * @creation 2012-12-3 下午8:41:04
	 * </pre>
	 */
	static void notifyCacheLoadComplete() throws KGameServerException {
		notifyCacheLoadComplete(mHotShopManager);
	}

	/**
	 * <pre>
	 * 服务器启动完成
	 * 
	 * @author CamusHuang
	 * @creation 2012-12-3 下午8:41:04
	 * </pre>
	 */
	private static void notifyCacheLoadComplete(HotShopManager tempShopManager) throws KGameServerException {
		try {
			tempShopManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载限时热购数据表错误：" + e.getMessage(), e);
		}
	}

	public static class HotShopManager {
		// ----------以下是EXCEL表格直导数据---------
		private int activityopen;// 活动开启
		private int[] goodsIndexs;// 页签显示序号
		private int[] fashionGoodsIndexs;// 时装商品序号
		private int[] equipGoodsIndexs;// 装备商品序号
		private int[] petGoodsIndexs;// 随从商品序号
		private int[] itemGoodsIndexs;// 材料商品序号

		// ----------以下是逻辑数据---------
		public boolean isActivityOpen;
		public final Map<KHotShopTypeEnum, HotShop> allShopMap = new HashMap<KHotShopTypeEnum, HotShop>();
		public final List<HotShop> allShopList = new ArrayList<HotShop>();
		private Set<Integer> goodsChangeForRoleLv = new HashSet<Integer>();

		void initDatas(Map<KHotShopTypeEnum, Map<Integer, HotGoods>> datas) throws KGameServerException {
			allShopMap.clear();
			allShopList.clear();

			long nowTime = System.currentTimeMillis();
			for (int type : goodsIndexs) {
				KHotShopTypeEnum typeEnum = KHotShopTypeEnum.getEnum(type);
				if (typeEnum == null) {
					throw new KGameServerException("加载[" + SheetName_各服控制 + "]错误:" + "标签类型不存在  = " + type);
				}
				Map<Integer, HotGoods> map = datas.get(typeEnum);
				//
				int[] GoodsIndexs = null;
				switch (typeEnum) {
				case 时装:
					GoodsIndexs = fashionGoodsIndexs;
					break;
				case 材料:
					GoodsIndexs = itemGoodsIndexs;
					break;
				case 装备:
					GoodsIndexs = equipGoodsIndexs;
					break;
				case 随从:
					GoodsIndexs = petGoodsIndexs;
					break;
				}
				//
				Map<Integer, HotGoods> goodsMap = new HashMap<Integer, HotGoods>();
				for (int index : GoodsIndexs) {
					HotGoods goods = map.get(index);
					if (goods == null) {
						throw new KGameServerException("加载[" + SheetName_各服控制 + "]错误: 商品不存在 index = " + index + " type = " + type);
					}
					if(goods.caTime.endTime<=nowTime){
						continue;
					}
					if (goodsMap.put(index, goods) != null) {
						throw new KGameServerException("加载[" + SheetName_各服控制 + "]错误: 商品重复 index = " + index + " type = " + type);
					}
					
					goodsChangeForRoleLv.add(goods.mixlvl);
					goodsChangeForRoleLv.add(goods.maxlvl);
				}
				//
				HotShop shop = new HotShop(typeEnum);
				shop.init(goodsMap);
				allShopMap.put(typeEnum, shop);
				allShopList.add(shop);
			}
		}

		public boolean isGoodsChangeForRoleLv(int level) {
			// CTODO Auto-generated method stub
			return false;
		}

		private void notifyCacheLoadComplete() throws Exception {

			isActivityOpen = activityopen == 1;

			for (HotShop shop : allShopList) {
				shop.notifyCacheLoadComplete();
			}
		}

		public static class HotGoodsForRoleLv {
			private int roleLv;
			public List<HotGoods> dataList;
			public Map<Integer, HotGoods> dataMap;

			private HotGoodsForRoleLv(int roleLv, Map<Integer, HotGoods> dataMap) {
				this.roleLv = roleLv;
				this.dataMap = new HashMap<Integer, HotGoods>(dataMap);
				this.dataList = new ArrayList<HotGoods>(dataMap.values());
			}
		}

		public static class HotShop {
			final KHotShopTypeEnum type;
			/**
			 * <pre>
			 * KEY = 商品ID
			 * </pre>
			 */
			private Map<Integer, HotGoods> dataMap = new LinkedHashMap<Integer, HotGoods>();
			List<HotGoods> dataList = new ArrayList<HotGoods>();

			/**
			 * <pre>
			 * KEY = 角色等级
			 * </pre>
			 */
			private Map<Integer, HotGoodsForRoleLv> dataForLv = new HashMap<Integer, HotGoodsForRoleLv>();

			HotShop(KHotShopTypeEnum type) {
				this.type = type;
			}

			public void init(Map<Integer, HotGoods> datas) throws KGameServerException {
				dataMap.clear();
				dataList.clear();
				//
				dataMap.putAll(datas);
				dataList.addAll(datas.values());
			}

			HotGoods getGoods(int goodsId) {
				return dataMap.get(goodsId);
			}

			HotGoodsForRoleLv getHotGoodsForRoleLv(int roleLv) {
				return dataForLv.get(roleLv);
			}

			private LinkedHashMap<Integer, HotGoods> searchGoodsForRoleLv(int roleLv) {
				LinkedHashMap<Integer, HotGoods> result = new LinkedHashMap<Integer, HotGoods>();
				for (HotGoods data : dataList) {
					if (data.mixlvl <= roleLv && roleLv <= data.maxlvl) {
						result.put(data.index, data);
					}
				}
				return result;
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
			void notifyCacheLoadComplete() throws KGameServerException {
				for (HotGoods data : dataMap.values()) {
					data.notifyCacheLoadComplete(type);
				}

				// 将所有商品按等级划分
				int maxLv = KRoleModuleConfig.getRoleMaxLv();
				for (int lv = 1; lv <= maxLv; lv++) {
					LinkedHashMap<Integer, HotGoods> map = searchGoodsForRoleLv(lv);
					dataForLv.put(lv, new HotGoodsForRoleLv(lv, map));
				}
			}
		}

		/**
		 * <pre>
		 * 
		 * @author CamusHuang
		 * @creation 2014-2-21 下午3:10:02
		 * </pre>
		 */
		public static class HotGoods {
			// ----------以下是EXCEL表格直导数据---------
			public int index;// 序号
			private int id;// 物品ID
			private int quantity;// 物品数量
			public int mixlvl;// 最低等级
			public int maxlvl;// 最高等级
			private int type;// 货币类型
			private int money;// 价格
			public int buyTimeForRole;// 个人限购次数
			public boolean buyTimeForRoleIsDay;// 个人限购次数是否针对每日
			public int buyTimeForWorld;// 全区限购总次数(-1表示无限)

			// ----------以下是逻辑数据---------
			public ItemCountStruct itemStruct;
			//
			public KCurrencyCountStruct price;
			public CATime caTime;// 生效时间参数
			//
			public KJobTypeEnum job;// null表示所有职业

			/**
			 * <pre>
			 * 当前是否在活动开启过程中
			 * 
			 * @return
			 * @author CamusHuang
			 * @creation 2014-11-12 下午12:32:12
			 * </pre>
			 */
			boolean isActivityTakeEffectNow(long nowTime, long todayTime) {
				return caTime.isInEffectTime(nowTime, todayTime);
			}

			long getActivityReleaseTime() {
				long nowTime = System.currentTimeMillis();
				long todayTime = UtilTool.getNowDayInMilliseconds();
				return getActivityReleaseTime(nowTime, todayTime);
			}

			/**
			 * <pre>
			 * 当前离活动结束还有多久
			 * 
			 * @return -1表示未开启
			 * @author CamusHuang
			 * @creation 2014-11-12 下午12:32:41
			 * </pre>
			 */
			long getActivityReleaseTime(long nowTime, long todayTime) {

				return caTime.getReleaseEffectTime(nowTime);
			}

			/**
			 * <pre>
			 * 
			 * @deprecated 只有时装类型宝箱才有效
			 * @return
			 * @author CamusHuang
			 * @creation 2015-1-22 上午11:47:06
			 * </pre>
			 */
			KFashionTemplate getFashionTemp() {
				if (itemStruct.getItemTemplate().ItemType != KItemTypeEnum.固定宝箱) {
					return null;
				}
				KItemTempFixedBox boxTemp = ((KItemTempFixedBox) itemStruct.getItemTemplate());
				if (boxTemp.addFashionTempMap.isEmpty()) {
					return null;
				}
				return boxTemp.addFashionTempMap.keySet().iterator().next();
			}

			/**
			 * <pre>
			 * 
			 * @deprecated 只有随从宝箱才有效
			 * @return
			 * @author CamusHuang
			 * @creation 2015-1-22 上午11:48:51
			 * </pre>
			 */
			KPetTemplate getPetTemp() {
				if (itemStruct.getItemTemplate().ItemType != KItemTypeEnum.随机宝箱) {
					return null;
				}
				KItemTempRandomBox boxTemp = ((KItemTempRandomBox) itemStruct.getItemTemplate());
				if (boxTemp.addPets.isEmpty()) {
					return null;
				}
				return KSupportFactory.getPetModuleSupport().getPetTemplate(boxTemp.addPets.get(0));
			}

			private void notifyCacheLoadComplete(KHotShopTypeEnum typeEnum) throws KGameServerException {
				if (mixlvl < 1) {
					throw new KGameServerException("数值错误 mixlvl = " + mixlvl);
				}
				if (maxlvl < 1) {
					throw new KGameServerException("数值错误 maxlvl = " + maxlvl);
				}
				{
					itemStruct = new ItemCountStruct(id + "", quantity);
					if (itemStruct.getItemTemplate() == null) {
						throw new KGameServerException("物品不存在 id = " + id);
					}
					if (quantity < 1) {
						throw new KGameServerException("数值错误 quantity = " + quantity);
					}

					if (typeEnum == KHotShopTypeEnum.时装) {
						if (quantity != 1) {
							throw new KGameServerException("数值错误 quantity = " + quantity);
						}

						if (itemStruct.getItemTemplate().ItemType != KItemTypeEnum.固定宝箱) {
							throw new KGameServerException("物品必须是固定宝箱 id = " + id);
						}
						KItemTempFixedBox boxTemp = ((KItemTempFixedBox) itemStruct.getItemTemplate());
						if (!boxTemp.addMoneys.isEmpty() || !boxTemp.addItems.isEmpty()) {
							throw new KGameServerException("物品必须是时装宝箱 id = " + id);
						}
						if (boxTemp.addFashionTempList.isEmpty()) {
							throw new KGameServerException("物品必须是时装宝箱 id = " + id);
						}
						if (boxTemp.addFashionTempList.size() > 1) {
							throw new KGameServerException("时装宝箱只能配置一件时装 id = " + id);
						}
						job = boxTemp.addFashionTempMap.keySet().iterator().next().jobEnum;
					} else if (typeEnum == KHotShopTypeEnum.随从) {
						if (itemStruct.getItemTemplate().ItemType != KItemTypeEnum.随机宝箱) {
							throw new KGameServerException("物品必须是固定宝箱 id = " + id);
						}
						KItemTempRandomBox boxTemp = ((KItemTempRandomBox) itemStruct.getItemTemplate());
						if (!boxTemp.isPetBox()) {
							throw new KGameServerException("物品必须是随从宝箱 id = " + id);
						}
						if (boxTemp.getPets().size() > 1) {
							throw new KGameServerException("随从宝箱只能配置一个随从 id = " + id);
						}
					} else if (typeEnum == KHotShopTypeEnum.装备) {
						{
							if (itemStruct.getItemTemplate().ItemType == KItemTypeEnum.装备) {
								job = ((KItemTempEqui) itemStruct.getItemTemplate()).jobEnum;
							}
						}
					}
					if (buyTimeForRole < 1 || buyTimeForRole > Byte.MAX_VALUE) {
						throw new KGameServerException("数值错误 buyTimeForRole = " + buyTimeForRole);
					}
					if (buyTimeForWorld != -1 && buyTimeForWorld < 1) {
						throw new KGameServerException("数值错误 buyTimeForWorld = " + buyTimeForWorld);
					}
					{
						KCurrencyTypeEnum moneyEnum = KCurrencyTypeEnum.getEnum(type);
						if (moneyEnum == null) {
							throw new KGameServerException("数值错误 type = " + type);
						}
						if (money < 1) {
							throw new KGameServerException("数值错误 money = " + money);
						}
						price = new KCurrencyCountStruct(moneyEnum, money);
					}
					// CTODO 其它约束检查
				}
			}
		}
	}
}
