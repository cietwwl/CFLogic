package com.kola.kmp.logic.gang;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGangTecTypeEnum;
import com.kola.kmp.logic.skill.KSkillDataManager;
import com.kola.kmp.logic.util.text.HyperTextTool;

/**
 * <pre>
 * 本类负责定义本模块的数据结构
 * 本类纯粹定义数据结构,而不管理数据
 * 
 * @author camus
 * @creation 2012-12-30 下午2:51:57
 * </pre>
 */
public class KGangDataStruct {

	/**
	 * <pre>
	 * 科技项目
	 * ID,名称
	 * 全局数据
	 * @author CamusHuang
	 * @creation 2013-1-11 下午3:48:18
	 * </pre>
	 */
	public static class GangTechTemplate {
		// ----------以下是EXCEL表格直导数据---------
		public int ID;// ID
		public String name;// 科技名称
		public int icon;// 科技ICON
		private int TechType;// 科技类型
		private String Techexplain;// 科技介绍
		private int[] TechLv;// 科技等级
		private int[] needLv;// 军团等级
		private int[] useGangMoney;// 消耗军团资金
		private int[] effectValue;// 增加效果值

		// ----------以下是逻辑数据---------
		public KGangTecTypeEnum type;// 科技类型
		/** 染色的名称 */
		private String extName;
		/** 各等级数据[0]表示0级 */
		private List<TechLevelData> levelDatas;

		public String getExtName() {
			return extName;
		}

		/**
		 * <pre>
		 * 获取指定的等级数据
		 * 
		 * @param lv
		 * @return
		 * @author CamusHuang
		 * @creation 2014-3-12 下午3:24:38
		 * </pre>
		 */
		public TechLevelData getLevelData(int lv) {
			if (lv >= levelDatas.size()) {
				return null;
			}
			return levelDatas.get(lv);
		}

		/**
		 * <pre>
		 * 验证数据
		 * 
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-1-11 下午5:19:13
		 * </pre>
		 */
		void notifyCacheLoadComplete() throws KGameServerException {
			extName = HyperTextTool.extColor(name, KColorFunEnum.科技名称);

			type = KGangTecTypeEnum.getEnum(TechType);
			if (type == null) {
				throw new KGameServerException(" 科技ID=" + ID + " TechType=" + TechType + " 科技类型错误");
			}

			// 验证填表数组长度
			Set<String> excuteFields = new HashSet<String>();// 排除检查的属性
			try {
				KSkillDataManager.checkArrayLenth(this, TechLv.length, excuteFields);
			} catch (Exception e) {
				throw new KGameServerException(e.getMessage() + ",科技ID=" + ID, e);
			}

			// 初始化等级数据
			levelDatas = new ArrayList<TechLevelData>();
			for (int lv = 0; lv < TechLv.length; lv++) {
				if (lv != TechLv[lv]) {
					throw new KGameServerException(" 科技ID=" + ID + " 等级错误");
				}
				TechLevelData lvData = new TechLevelData(this, lv);
				levelDatas.add(lvData);
				lvData.notifyCacheLoadComplete();
			}
			// CTODO 其它约束检查
		}

		/**
		 * <pre>
		 * 每一等级的数据
		 * 
		 * @author CamusHuang
		 * @creation 2014-3-12 下午3:01:33
		 * </pre>
		 */
		public static class TechLevelData {
			private static final DecimalFormat _DF = new DecimalFormat("0.##");
			// ----------以下是逻辑数据---------
			public final int TechLv;// 科技等级
			public final int needLv;// 军团等级
			public final int Legion_MoneyCount;// 消耗军团资金
			public final String desc;

			public final int effectValue;
			public final String effectValueStr;

			private TechLevelData(GangTechTemplate temp, int lv) {
				TechLv = temp.TechLv[lv];
				needLv = temp.needLv[lv];
				Legion_MoneyCount = temp.useGangMoney[lv];

				effectValue = temp.effectValue[lv];
				effectValueStr = _DF.format((float)effectValue/UtilTool.HUNDRED_RATIO_UNIT) + "%"; // 显示百分比，所以除以100
//				desc = StringUtil.format(temp.Techexplain, HyperTextTool.extColor(effectValue + "", KColorFunEnum.属性));
				desc = StringUtil.format(temp.Techexplain, HyperTextTool.extColor(effectValueStr, KColorFunEnum.属性));
			}

			void notifyCacheLoadComplete() throws KGameServerException {
				if (needLv < 0) {
					throw new KGameServerException("数值错误 needLv=" + needLv);
				}
				if (Legion_MoneyCount < 0) {
					throw new KGameServerException("数值错误 Legion_MoneyCount=" + Legion_MoneyCount);
				}
				if (effectValue < 0) {
					throw new KGameServerException("数值错误 effectValue=" + effectValue);
				}
			}
		}
	}

	/**
	 * <pre>
	 * 军团等级数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:25:48
	 * </pre>
	 */
	public static class GangLevelData {
		// ----------以下是EXCEL表格直导数据---------
		public int LegionLv;// 军团等级
		public int exp;// 升级经验
		public int maxuser;// 军团最大人数
		public int donatetime;// 捐献总次数

		// ----------以下是逻辑数据---------

		void notifyCacheLoadComplete() throws KGameServerException {
			if (exp < 1) {
				throw new KGameServerException("数值错误 exp=" + exp);
			}
			if (maxuser < 1) {
				throw new KGameServerException("数值错误 maxuser=" + maxuser);
			}
			if (donatetime < 1) {
				throw new KGameServerException("数值错误 donatetime=" + donatetime);
			}
		}
	}

	/**
	 * <pre>
	 * 军团商品数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:25:48
	 * </pre>
	 */
	public static class GangGoodsData {
		// ----------以下是EXCEL表格直导数据---------
		public int ID;// ID
		private String ItemID;// 道具ID
		private int Itemamount;// 道具数量
		private int moneyType;// 货币类型
		private int moneyCount;// 货币数量
		public int LegionLv;// 军团等级

		// ----------以下是逻辑数据---------
		public ItemCountStruct item;
		public KCurrencyCountStruct price;

		void notifyCacheLoadComplete() throws KGameServerException {

			if (LegionLv < 1) {
				throw new KGameServerException("数值错误 LegionLv=" + LegionLv);
			}
			if (moneyCount <= 0) {
				throw new KGameServerException("数据错误 moneyCount = " + moneyCount);
			}
			price = new KCurrencyCountStruct(KCurrencyTypeEnum.getEnum(moneyType), moneyCount);
			if (price.currencyType == null) {
				throw new KGameServerException("数据错误 moneyType = " + moneyType);
			}
			//
			if (Itemamount <= 0) {
				throw new KGameServerException("数据错误 Itemamount = " + Itemamount);
			}
			item = new ItemCountStruct(ItemID, Itemamount);
			if (item.getItemTemplate() == null) {
				throw new KGameServerException("物品不存在 ItemID = " + ItemID);
			}
		}
		// CTODO 其它约束检查
	}

	/**
	 * <pre>
	 * 军团捐献数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:25:48
	 * </pre>
	 */
	public static class GangContributionData {
		// ----------以下是EXCEL表格直导数据---------
		public int DonateType;// 捐献类型
		public int time;// 捐献次数
		private int quantity;// 捐献数量
		public int GainContribution;// 获得贡献
		public int exp;// 军团经验系数

		// ----------以下是逻辑数据---------
		public KCurrencyCountStruct price;

		void notifyCacheLoadComplete() throws KGameServerException {
			if (time < 0) {
				throw new KGameServerException("数值错误 time=" + time);
			}
			if (quantity < 1) {
				throw new KGameServerException("数据错误 quantity = " + quantity);
			}
			if (GainContribution < 1) {
				throw new KGameServerException("数据错误 GainContribution = " + GainContribution);
			}
			if (exp < 1) {
				throw new KGameServerException("数据错误 exp = " + exp);
			}
			price = new KCurrencyCountStruct(KCurrencyTypeEnum.getEnum(DonateType), quantity);
			if (price.currencyType == null) {
				throw new KGameServerException("数据错误 DonateType = " + DonateType);
			}
			// CTODO 其它约束检查
		}
	}

	/**
	 * <pre>
	 * 繁荣度基数
	 * 
	 * @author CamusHuang
	 * @creation 2013-1-24 上午11:25:48
	 * </pre>
	 */
	public static class GangProsperityData {
		// ----------以下是EXCEL表格直导数据---------
		private int gold_boom;// 金币繁荣度基数
		private int battlepoint_boom;// 潜能繁荣度基数
		private int diamond_boom;// 钻石繁荣度基数
		public int liveness_boom;// 活跃度繁荣基数

		// ----------以下是逻辑数据---------
		public Map<KCurrencyTypeEnum, Integer> moneyBase = new HashMap<KCurrencyTypeEnum, Integer>();

		void notifyCacheLoadComplete() throws KGameServerException {

			if (gold_boom < 1) {
				throw new KGameServerException("数值错误 gold_boom=" + gold_boom);
			}
			if (battlepoint_boom <= 0) {
				throw new KGameServerException("数据错误 battlepoint_boom = " + battlepoint_boom);
			}
			if (diamond_boom <= 0) {
				throw new KGameServerException("数据错误 diamond_boom = " + diamond_boom);
			}

			if (liveness_boom <= 0) {
				throw new KGameServerException("数据错误 liveness_boom = " + liveness_boom);
			}

			moneyBase.put(KCurrencyTypeEnum.GOLD, gold_boom);
			moneyBase.put(KCurrencyTypeEnum.POTENTIAL, battlepoint_boom);
			moneyBase.put(KCurrencyTypeEnum.DIAMOND, diamond_boom);

		}
		// CTODO 其它约束检查
	}
}
