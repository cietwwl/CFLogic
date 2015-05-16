package com.kola.kmp.logic.reward.vitality;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.vitality.KVitalityDataManager.FunTypeDataManager.FunTypeData;
import com.kola.kmp.logic.reward.vitality.KVitalityDataManager.VitalityRewardDataManager.VitalityRewardData;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.util.tips.RewardTips;

/**
 * <pre>
 * 本类负责管理本模块的公共数据 
 * 
 * @author CamusHuang
 * @creation 2013-4-8 下午2:39:56
 * </pre>
 */
public class KVitalityDataManager {

	static final String VitalityDailySaveDir = "./res/output/VitalityDialy/";
	static final String VitalityDailySaveFileName = "daily.xml";


	static void loadConfig(Element e) {
	}

	//
	static final String SheetName_活跃度功能配置 = "活跃度功能配置";
	static final String SheetName_活跃度奖励 = "活跃度奖励";

	/**
	 * <pre>
	 * 开始加载文件
	 * @param configPath
	 * @author CamusHuang
	 * @creation 2012-11-9 下午7:38:27
	 * </pre>
	 */
	static void goToLoadData(KGameExcelFile file, int HeaderIndex) throws Exception {

		// 加载数据
		{
			loadFunDatas(file.getTable(SheetName_活跃度功能配置, HeaderIndex));

			loadRewardDatas(file.getTable(SheetName_活跃度奖励, HeaderIndex));
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
	private static void loadFunDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}

		List<FunTypeData> datas = ReflectPaser.parseExcelData(FunTypeData.class, table.getHeaderNames(), rows, true);
		mFunTypeDataManager.init(datas);
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
	private static void loadRewardDatas(KGameExcelTable table) throws Exception {
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
		}
		
		try {
			List<VitalityRewardData> datas = new ArrayList<VitalityRewardData>();
			//
			List<String> headerNames = table.getHeaderNames();
			Class<VitalityRewardData> clazz = VitalityRewardData.class;
			for (KGameExcelRow row : rows) {
				VitalityRewardData obj = clazz.newInstance();
				ReflectPaser.parseExcelData(obj, headerNames, row, false, false);
				obj.baseReward = BaseRewardData.loadData(row, false);
				datas.add(obj);
			}
			mVitalityRewardDataManager.init(datas);
		} catch (Exception e) {
			throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
		}
	}

	// /////////////////////////////////////////

	// 功能类型奖励数据管理器
	static FunTypeDataManager mFunTypeDataManager = new FunTypeDataManager();
	// 活跃度奖励数据管理器
	static VitalityRewardDataManager mVitalityRewardDataManager = new VitalityRewardDataManager();

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 功能类型数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2013-5-17 上午11:53:20
	 * </pre>
	 */
	static class FunTypeDataManager {
		/**
		 * <pre>
		 * <类型,数据>
		 * </pre>
		 */
		private LinkedHashMap<Integer, FunTypeData> dataMap = new LinkedHashMap<Integer, FunTypeData>();

		// // 开放新功能类型的等级
		// private Set<Integer> openTypeLvSet = new HashSet<Integer>();

		private FunTypeDataManager() {
		}

		void init(List<FunTypeData> datas) throws Exception {
			for (FunTypeData data : datas) {
				dataMap.put(data.ID, data);
				// openTypeLvSet.add(data.mixlv);
			}
		}

		FunTypeData getData(int type) {
			return dataMap.get(type);
		}

		/**
		 * <pre>
		 * <类型,数据>
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2014-4-25 下午7:41:47
		 * </pre>
		 */
		LinkedHashMap<Integer, FunTypeData> getDataCache() {
			return dataMap;
		}

		// boolean checkOpenType(int preRoleLv, int nowRoleLv) {
		// for (int lv = preRoleLv + 1; lv <= nowRoleLv; lv++) {
		// if (openTypeLvSet.contains(lv)) {
		// return true;
		// }
		// }
		// return false;
		// }

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

			// if (KVitalityTypeEnum.values().length != dataMap.size()) {
			// throw new KGameServerException("功能类型 数量与 枚举不一致");
			// }
			// 是否包含所有功能类型
			for (KVitalityTypeEnum type : KVitalityTypeEnum.values()) {
				if (!type.isCheckLoad) {
					continue;
				}
				FunTypeData data = dataMap.get(type.sign);
				if (data == null) {
					throw new KGameServerException("缺少功能类型=" + type.name);
				}
				if (!data.name.equals(type.name)) {
					throw new KGameServerException("功能类型名称与枚举不一致 枚举名称=" + type.name);
				}
				data.notifyCacheLoadComplete();
			}
		}

		// //////////////////////////
		/**
		 * <pre>
		 * 功能类型奖励
		 * 
		 * @author CamusHuang
		 * @creation 2013-5-30 上午9:40:13
		 * </pre>
		 */
		public static class FunTypeData {

			// ----------以下是EXCEL表格直导数据---------
			public int ID;// 功能类型
			public String name;// 功能名称
			public int icon;// 图标
			public int time;// 完成次数
			public int vitalityValue;// 获得活跃度
			public int orderId;// 打开界面指令ID

			// ----------以下是逻辑数据---------
			public KVitalityTypeEnum typeEnum;

			void notifyCacheLoadComplete() throws KGameServerException {
				if (time < 1) {
					throw new KGameServerException("数值错误 time = " + time);
				}
				if (vitalityValue < 1) {
					throw new KGameServerException("数值错误 vitalityValue = " + vitalityValue);
				}
				typeEnum = KVitalityTypeEnum.getEnum(ID);
				if (typeEnum == null) {
					throw new KGameServerException("数值错误 ID = " + ID);
				}
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 角色等级数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2013-5-17 上午11:53:20
	 * </pre>
	 */
	static class VitalityRewardDataManager {
		// <角色等级,List<数据>>
		private Map<Integer, List<VitalityRewardData>> dataMap = new HashMap<Integer, List<VitalityRewardData>>();
		// <角色等级,LinkedHashMap<积分,数据>>
		private Map<Integer, Map<Integer, VitalityRewardData>> dataMap2 = new HashMap<Integer, Map<Integer, VitalityRewardData>>();
		// 全部数据
		private List<VitalityRewardData> dataList = new ArrayList<VitalityRewardData>();

		private VitalityRewardDataManager() {
		}

		void init(List<VitalityRewardData> datas) throws KGameServerException {
			dataList.addAll(datas);

			for (VitalityRewardData data : datas) {
				for (int lv = data.mixlv; lv <= data.maxlv; lv++) {
					{
						List<VitalityRewardData> list = dataMap.get(lv);
						if (list == null) {
							list = new ArrayList<VitalityRewardData>();
							dataMap.put(lv, list);
						}
						list.add(data);
					}
					{
						Map<Integer, VitalityRewardData> map = dataMap2.get(lv);
						if (map == null) {
							map = new HashMap<Integer, VitalityRewardData>();
							dataMap2.put(lv, map);
						}
						if (map.put(data.score, data) != null) {
							throw new KGameServerException("重复的数据 lv=" + lv + " score=" + data.score);
						}
					}

				}
			}
			int maxLv = KRoleModuleConfig.getRoleMaxLv();
			for (int lv = 1; lv <= maxLv; lv++) {
				List<VitalityRewardData> temp = dataMap.get(lv);
				if (temp == null) {
					throw new KGameServerException("缺少的奖励等级=" + lv);
				}
				Collections.sort(temp);
				for (int i = 0; i < temp.size(); i++) {
					temp.get(i).rewardLv = i + 1;
				}
			}
		}

		/**
		 * <pre>
		 * 获取指定奖励项
		 * 
		 * @param roleLv
		 * @return
		 * @author CamusHuang
		 * @creation 2013-5-30 上午11:08:59
		 * </pre>
		 */
		List<VitalityRewardData> getData(int roleLv) {
			return dataMap.get(roleLv);
		}

		/**
		 * <pre>
		 * 获取指定奖励项
		 * 
		 * @param roleLv
		 * @param score
		 * @return
		 * @author CamusHuang
		 * @creation 2014-10-31 上午11:41:57
		 * </pre>
		 */
		VitalityRewardData getData(int roleLv, int score) {
			Map<Integer, VitalityRewardData> map = dataMap2.get(roleLv);
			if (map == null) {
				return null;
			}
			return map.get(score);
		}

		/**
		 * <pre>
		 * 检查这两个等级对应的阶级性奖励是否完全相同
		 * 
		 * @param preRoleLv
		 * @param nowRoleLv
		 * @return
		 * @author CamusHuang
		 * @creation 2014-10-31 上午11:29:39
		 * </pre>
		 */
		boolean checkChangeReward(int preRoleLv, int nowRoleLv) {
			Map<Integer, VitalityRewardData> a = dataMap2.get(preRoleLv);
			Map<Integer, VitalityRewardData> b = dataMap2.get(nowRoleLv);
			if (a.size() != b.size()) {
				return false;
			}
			for (Entry<Integer, VitalityRewardData> e : a.entrySet()) {
				if (!b.containsKey(e.getKey())) {
					return false;
				}
				VitalityRewardData datab = b.get(e.getKey());
				if (!datab.equals(e.getValue())) {
					return false;
				}
			}
			return true;
		}

		void notifyCacheLoadComplete() throws Exception {
			for (VitalityRewardData data : dataList) {
				data.notifyCacheLoadComplete();
			}
		}

		/**
		 * <pre>
		 * 某角色等级道具果实掉落数据
		 * 
		 * @author CamusHuang
		 * @creation 2013-5-24 上午11:09:46
		 * </pre>
		 */
		public static class VitalityRewardData implements Comparable<VitalityRewardData> {

			// ----------以下是EXCEL表格直导数据---------
			public int mixlv;// 最低等级
			public int maxlv;// 最高等级
			public int score;// 所需积分
			private BaseRewardData baseReward;// 奖励

			// ----------以下是逻辑数据---------
			public int rewardLv;// 是第几档奖励
			public BaseMailRewardData baseMailReward;

			void notifyCacheLoadComplete() throws Exception {
				if (mixlv < 1) {
					throw new KGameServerException("数值错误 mixlv = " + mixlv);
				}

				if (maxlv < mixlv) {
					throw new KGameServerException("数值错误 mixlv = " + mixlv);
				}

				if (score < 1) {
					throw new KGameServerException("数值错误 score = " + score);
				}

				baseReward.notifyCacheLoadComplete();

				// 检查奖励内容是否有效
				if (!baseReward.checkIsEffect()) {
					throw new KGameServerException("所有奖励项无效");
				}

				BaseMailContent mailContent = new BaseMailContent(RewardTips.活跃奖励邮件标题, RewardTips.背包已满通用奖励邮件内容, null, null);
				baseMailReward = new BaseMailRewardData(1, mailContent, baseReward);

				// CTODO 其它约束检查
			}

			@Override
			public int compareTo(VitalityRewardData o) {
				if (score < o.score) {
					return -1;
				}
				if (score > o.score) {
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
			mFunTypeDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_活跃度功能配置 + "]错误：" + e.getMessage(), e);
		}

		try {
			mVitalityRewardDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_活跃度奖励 + "]错误：" + e.getMessage(), e);
		}
	}
}
