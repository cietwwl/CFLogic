package com.kola.kmp.logic.reward.exciting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.timer.Timer;

import org.jdom.Element;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.KGameGlobalConfig;
import com.kola.kmp.logic.activity.transport.KTransportManager;
import com.kola.kmp.logic.gamble.wish.KWishItemPool;
import com.kola.kmp.logic.gang.KGangPositionEnum;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempFixedBox;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempRandomBox;
import com.kola.kmp.logic.level.KGameLevelDropPool.KDropGroup;
import com.kola.kmp.logic.level.KLevelReward.NormalItemRewardTemplate;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KItemTypeEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.rank.gang.KGangRankTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.ExcitingDataManager.ExcitionActivity;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.ExcitingRuleManager.RewardRule;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.ExcitingRuleManager.RewardRuleBoolanData;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.RankTimeLimitRewardDataManager.RankTimeLimitReward;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.CommonActivityTime;
import com.kola.kmp.logic.util.CommonActivityTime.CATime;
import com.kola.kmp.logic.util.tips.GlobalTips;

public class KExcitingDataManager {

	static void loadConfig(Element e) throws KGameServerException {
	}

	private static String excelPath;
	private static int HeaderIndex;
	//
	private static final String SheetName_精彩活动 = "精彩活动";
	private static final String SheetName_奖励规则 = "奖励规则";
	private static final String SheetName_网址及邮箱 = "网址及邮箱";
	private static final String SheetName_限时产出活动 = "限时产出活动";
	private static final String SheetName_排行榜定时奖励 = "排行榜定时奖励";
	private static final String SheetName_普通关卡活动掉落 = "普通关卡活动掉落";
	private static final String SheetName_精英关卡活动掉落 = "精英关卡活动掉落";
	private static final String SheetName_掉落方案配置 = "掉落方案配置";

	/**
	 * <pre>
	 * 开始加载文件
	 * 
	 * @deprecated 模块内调用
	 * @param configPath
	 * @author CamusHuang
	 * @creation 2012-11-9 下午7:38:27
	 * </pre>
	 */
	static void goToLoadData(String fileUrl, int headerIndex) throws Exception {
		// 加载数据
		excelPath = fileUrl;
		HeaderIndex = headerIndex;

		reloadData(false);
	}

	static void reloadData(boolean isReload) throws Exception {

		// 精彩活动数据管理器
		ExcitingDataManager mExcitingDataManager = new ExcitingDataManager();
		// 奖励规则数据管理器
		ExcitingRuleManager mExcitingRuleManager = new ExcitingRuleManager();
		// 限时产出活动数据管理器
		TimeLimitActivityDataManager mTimeLimitActivityDataManager = new TimeLimitActivityDataManager();
		// 排行榜定时奖励活动
		RankTimeLimitRewardDataManager mRankTimeLimitRewardDataManager = new RankTimeLimitRewardDataManager();

		KGameExcelFile file = new KGameExcelFile(excelPath);
		{
			KGameExcelTable table = file.getTable(SheetName_奖励规则, HeaderIndex);
			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length < 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
			}

			Map<Integer, RewardRule> datas = new HashMap<Integer, RewardRule>();
			{
				List<String> headerNames = table.getHeaderNames();
				Class<RewardRule> clazz = RewardRule.class;
				for (KGameExcelRow row : rows) {
					RewardRule obj = clazz.newInstance();
					ReflectPaser.parseExcelData(obj, headerNames, row, false, false);
					obj.rewardMail = BaseMailRewardData.loadData(row, false);
					if (datas.put(obj.ruleId, obj) != null) {
						throw new Exception("重复的规则 ruleId=" + obj.ruleId);
					}
				}
			}
			mExcitingRuleManager.initDatas(datas);
		}
		
		// 开服当天的0点
		final long ServerStartDay = UtilTool.getNextNDaysStart(KGame.getGSFirstStartTime(), 0).getTimeInMillis();
		final long nowTime = System.currentTimeMillis();
		
		{
			KGameExcelTable table = file.getTable(SheetName_精彩活动, HeaderIndex);
			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length < 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
			}
			//
			List<ExcitionActivity> dataList = new ArrayList<ExcitionActivity>();
			{
				List<String> headerNames = table.getHeaderNames();
				Class<ExcitionActivity> clazz = ExcitionActivity.class;
				for (KGameExcelRow row : rows) {
					ExcitionActivity obj = clazz.newInstance();
					ReflectPaser.parseExcelData(obj, headerNames, row, false, false);
					CommonActivityTime activityTime = null;
					try{
						activityTime = CommonActivityTime.load(ServerStartDay, nowTime, row);
						if(activityTime.effectTimes.isEmpty()){
							continue;
						}
						if(activityTime.effectTimes.size()>1){
							throw new Exception("只能填一组有效时间参数");
						}
						obj.caTime=activityTime.effectTimes.get(0);
						if(!obj.caTime.isFullTime){
							throw new Exception("不允许填每日时间段");
						}
					} catch(Exception e){
						throw new Exception("加载[" + table.getTableName() + "]错误:"+e.getMessage()+", row="+row.getIndexInFile(), e);
					}
					dataList.add(obj);
				}
			}
			mExcitingDataManager.initDatas(dataList);
		}
		{
			//
			KGameExcelTable table = file.getTable(SheetName_网址及邮箱, HeaderIndex);
			load网址及邮箱(SheetName_网址及邮箱, table.getAllDataRows(), mExcitingDataManager);
		}

		{
			KGameExcelTable table = null;
			KGameExcelRow[] rows = null;
			{
				table = file.getTable(SheetName_限时产出活动, HeaderIndex);
				rows = table.getAllDataRows();
				if (rows.length < 1) {
					throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
				}
				//
				List<TimeLimieProduceActivity> dataList = new ArrayList<TimeLimieProduceActivity>();
				{
					List<String> headerNames = table.getHeaderNames();
					Class<TimeLimieProduceActivity> clazz = TimeLimieProduceActivity.class;
					for (KGameExcelRow row : rows) {
						TimeLimieProduceActivity obj = clazz.newInstance();
						ReflectPaser.parseExcelData(obj, headerNames, row, false, false);
						try{
							obj.mCommonActivityTime = CommonActivityTime.load(ServerStartDay, nowTime, row);
						} catch(Exception e){
							throw new Exception("加载[" + table.getTableName() + "]错误:"+e.getMessage()+", row="+row.getIndexInFile(), e);
						}
						dataList.add(obj);
					}
				}
				
				mTimeLimitActivityDataManager.initDatas(dataList);
			}
			{
				List<HolidayCopySpecialReward> normalRewardDataList;
				{
					table = file.getTable(SheetName_普通关卡活动掉落, HeaderIndex);
					rows = table.getAllDataRows();
					if (rows.length < 1) {
						normalRewardDataList = new ArrayList<KExcitingDataManager.HolidayCopySpecialReward>();
					} else {
						normalRewardDataList = ReflectPaser.parseExcelData(HolidayCopySpecialReward.class, table.getHeaderNames(), rows, true);
					}
				}
				List<HolidayCopySpecialReward> eliteRewardDataList;
				{
					table = file.getTable(SheetName_精英关卡活动掉落, HeaderIndex);
					rows = table.getAllDataRows();
					if (rows.length < 1) {
						throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
					}

					if (rows.length < 1) {
						eliteRewardDataList = new ArrayList<KExcitingDataManager.HolidayCopySpecialReward>();
					} else {
						eliteRewardDataList = ReflectPaser.parseExcelData(HolidayCopySpecialReward.class, table.getHeaderNames(), rows, true);
					}
				}

				List<KDropGroup> dropGroupList = new ArrayList<KDropGroup>();
				{
					table = file.getTable(SheetName_掉落方案配置, HeaderIndex);
					rows = table.getAllDataRows();

					if (rows.length > 0) {
						for (int i = 0; i < rows.length; i++) {
							KDropGroup group = new KDropGroup();
							group.init(rows[i]);
							dropGroupList.add(group);
						}
					}
				}

				mTimeLimitActivityDataManager.initHolidayRewardDatas(normalRewardDataList, eliteRewardDataList, dropGroupList);
			}
		}

		{
			KGameExcelTable table = file.getTable(SheetName_排行榜定时奖励, HeaderIndex);
			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length < 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
			}
			//
			List<RankTimeLimitReward> dataList = new ArrayList<RankTimeLimitReward>();
			{
				List<String> headerNames = table.getHeaderNames();
				Class<RankTimeLimitReward> clazz = RankTimeLimitReward.class;
				for (KGameExcelRow row : rows) {
					RankTimeLimitReward obj = clazz.newInstance();
					ReflectPaser.parseExcelData(obj, headerNames, row, false, false);
					obj.rewardMail = BaseMailRewardData.loadData(row, false);
					CommonActivityTime activityTime = null;
					try{
						activityTime = CommonActivityTime.load(ServerStartDay, nowTime, row);
						if(activityTime.effectTimes.isEmpty()){
							continue;
						}
						if(activityTime.effectTimes.size()>1){
							throw new Exception("只能填一组有效时间参数");
						}
						obj.caTime=activityTime.effectTimes.get(0);
						if(!obj.caTime.isFullTime){
							throw new Exception("不允许填每日时间段");
						}
					} catch(Exception e){
						throw new Exception("加载[" + table.getTableName() + "]错误:"+e.getMessage()+", row="+row.getIndexInFile(), e);
					}
					dataList.add(obj);
				}
			}

			mRankTimeLimitRewardDataManager.initDatas(dataList);
		}

		if (isReload) {
			notifyCacheLoadComplete(mExcitingDataManager, mExcitingRuleManager, mTimeLimitActivityDataManager, mRankTimeLimitRewardDataManager);
		}

		KExcitingDataManager.mExcitingDataManager = mExcitingDataManager;
		KExcitingDataManager.mExcitingRuleManager = mExcitingRuleManager;
		KExcitingDataManager.mTimeLimitActivityDataManager = mTimeLimitActivityDataManager;
		KExcitingDataManager.mRankTimeLimitRewardDataManager = mRankTimeLimitRewardDataManager;
	}

	private static void load网址及邮箱(String sheetName, KGameExcelRow[] rows, ExcitingDataManager mExcitingDataManager) throws KGameServerException {
		if (rows.length < 1) {
			return;
		}

		KGameExcelRow row = rows[0];
		try {
			mExcitingDataManager.统一显示的网址标题 = row.getData("统一显示的网址标题");// 奖励说明
			mExcitingDataManager.统一显示的网址 = row.getData("统一显示的网址");// 奖励说明
			if (mExcitingDataManager.统一显示的网址标题.isEmpty()) {
				if (!mExcitingDataManager.统一显示的网址.isEmpty()) {
					throw new KGameServerException("加载" + sheetName + "错误：网址信息 错误 ,Row=" + row.getIndexInFile());
				}
			}
			if (!mExcitingDataManager.统一显示的网址标题.isEmpty()) {
				if (mExcitingDataManager.统一显示的网址.isEmpty()) {
					throw new KGameServerException("加载" + sheetName + "错误：网址信息 错误 ,Row=" + row.getIndexInFile());
				}
			}
			mExcitingDataManager.统一显示的邮箱标题 = row.getData("统一显示的邮箱标题");// 奖励说明
			mExcitingDataManager.统一显示的邮箱 = row.getData("统一显示的邮箱");// 奖励说明
			if (mExcitingDataManager.统一显示的邮箱标题.isEmpty()) {
				if (!mExcitingDataManager.统一显示的邮箱.isEmpty()) {
					throw new KGameServerException("加载" + sheetName + "错误：邮箱信息 错误 ,Row=" + row.getIndexInFile());
				}
			}
			if (!mExcitingDataManager.统一显示的邮箱标题.isEmpty()) {
				if (mExcitingDataManager.统一显示的邮箱.isEmpty()) {
					throw new KGameServerException("加载" + sheetName + "错误：邮箱信息 错误 ,Row=" + row.getIndexInFile());
				}
			}

		} catch (KGameServerException e) {
			throw e;
		} catch (Exception e) {
			throw new KGameServerException("加载" + sheetName + "错误：Row=" + row.getIndexInFile(), e);
		}
	}

	// 精彩活动数据管理器
	public static ExcitingDataManager mExcitingDataManager = new ExcitingDataManager();
	// 奖励规则数据管理器
	public static ExcitingRuleManager mExcitingRuleManager = new ExcitingRuleManager();
	// 限时产出活动数据管理器
	public static TimeLimitActivityDataManager mTimeLimitActivityDataManager = new TimeLimitActivityDataManager();
	// 排行榜定时奖励活动
	public static RankTimeLimitRewardDataManager mRankTimeLimitRewardDataManager = new RankTimeLimitRewardDataManager();

	// ///////////////////////////////////////////////////
	public static class ExcitingRuleManager {
		private Map<Integer, RewardRule> datasMap = new HashMap<Integer, RewardRule>();

		void initDatas(Map<Integer, RewardRule> datas) throws Exception {
			datasMap.putAll(datas);
		}

		RewardRule getData(int ruleId) {
			return datasMap.get(ruleId);
		}

		void notifyCacheLoadComplete() throws Exception {
			for (RewardRule rule : datasMap.values()) {
				try {
					rule.notifyCacheLoadComplete();
				} catch (Exception e) {
					throw new Exception(e.getMessage() + "，规则ID=" + rule.ruleId, e);
				}
			}
		}

		/**
		 * <pre>
		 * 奖励规则
		 * 
		 * @author CamusHuang
		 * @creation 2013-5-24 上午11:09:46
		 * </pre>
		 */
		static class RewardRule extends RewardRuleBoolanData {
			// ----------以下是EXCEL表格直导数据---------
			public int ruleId;// 规则ID
			public String desc;// 奖励说明
			// 【硬条件】，可以同时使用多个，时效任务可以直接在扫描时获取数据核算发奖，不需要任何显式通知
			public int minLv;// 当前最小角色等级
			public int minBattlePow;// 当前角色总战力
			public int minMountLv;// 当前座驾最小等级
			public int minEquiStrongSetLv;// 当前装备强化套装等级
			public int minEquiStoneSetLv;// 当前装备宝石套装等级
			public int minEquiStarSetLv;// 当前装备升星套装等级
			public int hisTotalCharge;// 历史累计充值金额
			// 【单次条件】，一条规则只允许使用其中之一，且不能与【累计条件】同时使用，但可以搭配任意多个【硬条件】，需要显式通知时发奖
			public int chargeInOneTimeMin;// 期间单次充值元宝下限
			public int chargeInOneTimeMax;// 期间单次充值元宝上限
			public int expTaskRewardLv;// 期间领取经验任务第几阶段奖励
			public int vitalityTaskRewardLv;// 期间领取活跃度任务第几阶段奖励
			// 【累计条件】，一条规则只允许使用其中之一，且不能与【单次条件】同时使用，但可以搭配任意多个【硬条件】，需要显式通知进行累计，时效任务核算发奖
			public int totalCharge;// 期间累计充值元宝
			public int totalUsePhyPow;// 期间消耗体力
			public int totalPay;// 期间累计消费元宝
			private int totalOnline;// 期间累计在线时长（分钟）
			public int totalLoginDay;// 期间连续登录天数
			// 其它配合参数
			public int presentIngotRate;// 元宝返还百分比(支持[期间单次充值]、[期间累计充值]、[期间累计消费])
			private boolean isExcuteFirstCharge;// 是否排除首充(支持[期间单次充值]、[期间累计充值])
			public int maxTimeForWorld;// 全服限量，-1表示不限量
			// 单笔充值限量参数
			public int chargeInOneTimeMaxTimeForRole=-1;// 单笔充值个人限量
			public boolean isChargeInOneTimeMaxTimeForRolePerDay;// 单笔充值个人限量是否每日重置
			public boolean isTotalChargePerDay;// 期间累计充值金额是否每日重置

			//
			public String worldChat;// 领奖世界播报

			// ----------以下是逻辑数据---------
			BaseMailRewardData rewardMail;
			public long totalOnlineMills;// 期间累计在线时长（毫秒）

			public int getTotalOnlineInMinu() {
				return totalOnline;
			}

			/**
			 * <pre>
			 * 验证所有数据
			 * 
			 * @throws KGameServerException
			 * @author CamusHuang
			 * @creation 2013-1-4 下午8:26:02
			 * </pre>
			 */
			void notifyCacheLoadComplete() throws Exception {

				rewardMail.notifyCacheLoadComplete();

				//
				super.initForRewardRule();

				//
				if (isSenstiveForTotalOnline()) {
					totalOnlineMills = totalOnline * Timer.ONE_MINUTE;
				}

				if (chargeInOneTimeMin > chargeInOneTimeMax) {
					throw new Exception("chargeInOneTimeMin、chargeInOneTimeMax 数值错误");
				}

				if (chargeInOneTimeMin < 1 && chargeInOneTimeMax >= 1) {
					throw new Exception("chargeInOneTimeMin、chargeInOneTimeMax 数值错误");
				}

				if (presentIngotRate > 0 && !isSenstiveForChargeInOneTime() && !isSenstiveForTotalCharge() && !isSenstiveForTotalPay()) {
					throw new Exception("presentIngotRate 必须与 chargeInOneTimeMin 或 totalCharge 或 totalPay 条件搭配");
				}
				
				if (isExcuteFirstCharge && !isSenstiveForChargeInOneTime() && !isSenstiveForTotalCharge()){
					throw new Exception("isExcuteFirstCharge 必须与 chargeInOneTimeMin 或 totalCharge 条件搭配");
				}
				
				if (chargeInOneTimeMaxTimeForRole > 0 && !isSenstiveForChargeInOneTime()){
					throw new Exception("chargeInOneTimeMaxTimeForRole 必须与 chargeInOneTimeMin 条件搭配");
				}
				
				if(isChargeInOneTimeMaxTimeForRolePerDay && chargeInOneTimeMaxTimeForRole < 1){
					throw new Exception("isChargeInOneTimeMaxTimeForRolePerDay 必须与 chargeInOneTimeMaxTimeForRole 条件搭配");
				}
				
				if(isTotalChargePerDay && totalCharge < 1){
					throw new Exception("isTotalChargePerDay 必须与 totalCharge 条件搭配");
				}
				

				// 检查奖励内容是否有效
				if (presentIngotRate < 1 && !rewardMail.checkIsEffect()) {
					throw new Exception("所有奖励项无效");
				}

				// 检查领奖条件是否有效
				if (minLv < 1 && //
						minBattlePow < 1 && //
						minMountLv < 1 && //
						minEquiStrongSetLv < 1 && //
						minEquiStoneSetLv < 1 && //
						minEquiStarSetLv < 1 && //
						hisTotalCharge < 1 && //
						// ///////////////////////////
						chargeInOneTimeMin < 1 && //
						chargeInOneTimeMax < 1 && //
						expTaskRewardLv < 1 && //
						vitalityTaskRewardLv < 1 && //
						// /////////////////////////
						totalCharge < 1 && //
						totalUsePhyPow < 1 && //
						totalPay < 1 && //
						totalOnline < 1 && //
						totalLoginDay < 1) {
					throw new Exception("缺少领奖条件");
				}

				// 检查单次条件和累计条件是否重叠
				{
					LinkedHashSet<String> tempSet = new LinkedHashSet<String>();
					if (chargeInOneTimeMin > 0) {
						tempSet.add("chargeInOneTimeMin");
					}
					if (expTaskRewardLv > 0) {
						tempSet.add("expTaskRewardLv");
					}
					if (vitalityTaskRewardLv > 0) {
						tempSet.add("vitalityTaskRewardLv");
					}
					if (totalCharge > 0) {
						tempSet.add("totalCharge");
					}
					if (totalUsePhyPow > 0) {
						tempSet.add("totalUsePhyPow");
					}
					if (totalPay > 0) {
						tempSet.add("totalPay");
					}
					if (totalOnline > 0) {
						tempSet.add("totalOnline");
					}
					if (totalLoginDay > 0) {
						tempSet.add("totalLoginDay");
					}
					if (tempSet.size() > 1) {
						StringBuffer sbf = new StringBuffer();
						for (String temp : tempSet) {
							sbf.append(temp).append(GlobalTips.顿号);
						}
						throw new Exception("单次条件和累计条件 不可叠加 ：" + sbf.toString());
					}
				}

				if (worldChat.length() < 1) {
					worldChat = null;
				}

				if (desc.length() < 1) {
					throw new KGameServerException("desc 不能为空 ");
				}
			}
		}

		static class RewardRuleBoolanData {
			private boolean isSenstiveForLv;
			private boolean isSenstiveForBattlePow;
			private boolean isSenstiveForMountLv;
			private boolean isSenstiveForEquiStrongSetLv;
			private boolean isSenstiveForEquiStoneSetLv;
			private boolean isSenstiveForEquiStarSetLv;
			private boolean isSenstiveForHisTotalCharge;
			//
			private boolean isSenstiveForChargeInOneTime;
			private boolean isSenstiveForExpTaskRewardLv;
			private boolean isSenstiveForVitalityRewardLv;
			//
			private boolean isSenstiveForTotalCharge;
			private boolean isSenstiveForTotalUsePhyPow;
			private boolean isSenstiveForTotalPay;
			private boolean isSenstiveForTotalOnline;
			private boolean isSenstiveForLoginDay;
			//
			private boolean isExcuteFirstCharge;// 是否排除首充(支持[期间单次充值]、[期间累计充值])

			void copyTrue(RewardRuleBoolanData data) throws Exception {
				Class clazz = RewardRuleBoolanData.class;
				Field[] fields = clazz.getDeclaredFields();

				for (Field field : fields) {
					boolean isTrue = field.getBoolean(data);
					if (isTrue) {
						field.setBoolean(this, true);
					}
				}
			}

			void initForRewardRule() {
				RewardRule rule = (RewardRule) this;
				if (rule.minLv > 0) {
					isSenstiveForLv = true;
				}
				if (rule.minBattlePow > 0) {
					isSenstiveForBattlePow = true;
				}
				if (rule.minMountLv > 0) {
					isSenstiveForMountLv = true;
				}
				if (rule.minEquiStrongSetLv > 0) {
					isSenstiveForEquiStrongSetLv = true;
				}
				if (rule.minEquiStoneSetLv > 0) {
					isSenstiveForEquiStoneSetLv = true;
				}
				if (rule.minEquiStarSetLv > 0) {
					isSenstiveForEquiStarSetLv = true;
				}
				if (rule.hisTotalCharge > 0) {
					isSenstiveForHisTotalCharge = true;
				}
				//
				if (rule.chargeInOneTimeMin > 0) {
					isSenstiveForChargeInOneTime = true;
				}
				if (rule.expTaskRewardLv > 0) {
					isSenstiveForExpTaskRewardLv = true;
				}
				if (rule.vitalityTaskRewardLv > 0) {
					isSenstiveForVitalityRewardLv = true;
				}
				//
				if (rule.totalCharge > 0) {
					isSenstiveForTotalCharge = true;
				}
				if (rule.totalUsePhyPow > 0) {
					isSenstiveForTotalUsePhyPow = true;
				}
				if (rule.totalPay > 0) {
					isSenstiveForTotalPay = true;
				}
				if (rule.totalOnline > 0) {
					isSenstiveForTotalOnline = true;
				}
				if (rule.totalLoginDay > 0) {
					isSenstiveForLoginDay = true;
				}
				//
				isExcuteFirstCharge = rule.isExcuteFirstCharge;
			}

			public boolean isSenstiveForLv() {
				return isSenstiveForLv;
			}

			public boolean isSenstiveForBattlePow() {
				return isSenstiveForBattlePow;
			}

			public boolean isSenstiveForMountLv() {
				return isSenstiveForMountLv;
			}

			public boolean isSenstiveForEquiStrongSetLv() {
				return isSenstiveForEquiStrongSetLv;
			}

			public boolean isSenstiveForEquiStoneSetLv() {
				return isSenstiveForEquiStoneSetLv;
			}

			public boolean isSenstiveForEquiStarSetLv() {
				return isSenstiveForEquiStarSetLv;
			}

			public boolean isSenstiveForHisTotalCharge() {
				return isSenstiveForHisTotalCharge;
			}

			public boolean isSenstiveForChargeInOneTime() {
				return isSenstiveForChargeInOneTime;
			}

			public boolean isSenstiveForExpTaskRewardLv() {
				return isSenstiveForExpTaskRewardLv;
			}

			public boolean isSenstiveForVitalityRewardLv() {
				return isSenstiveForVitalityRewardLv;
			}

			public boolean isSenstiveForTotalCharge() {
				return isSenstiveForTotalCharge;
			}

			public boolean isSenstiveForTotalUsePhyPow() {
				return isSenstiveForTotalUsePhyPow;
			}

			public boolean isSenstiveForTotalPay() {
				return isSenstiveForTotalPay;
			}

			public boolean isSenstiveForTotalOnline() {
				return isSenstiveForTotalOnline;
			}

			public boolean isSenstiveForLoginDay() {
				return isSenstiveForLoginDay;
			}

			public boolean isExcuteFirstCharge() {
				return isExcuteFirstCharge;
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 
	 * @author CamusHuang
	 * @creation 2013-5-17 上午11:53:20
	 * </pre>
	 */
	public static class ExcitingDataManager {
		public String 统一显示的网址标题 = "";// （例如："官方粉丝页："，""表示无需显示）
		public String 统一显示的网址 = "";// （例如："https://www.facebook.com/kimi.lwk"，""表示无需显示）
		public String 统一显示的邮箱标题 = "";// （例如："官方邮箱："，""表示无需显示）
		public String 统一显示的邮箱 = "";// （例如："lwk@kimi.com.tw"，""表示无需显示）

		private List<ExcitionActivity> datasList = new ArrayList<ExcitionActivity>();
		private Map<Integer, ExcitionActivity> datasMap = new HashMap<Integer, ExcitionActivity>();

		private ExcitingDataManager() {
		}

		void initDatas(List<ExcitionActivity> datas) throws Exception {
			for (ExcitionActivity data : datas) {
				if (datasMap.put(data.id, data) != null) {
					throw new Exception("重复的活动 ID=" + data.id);
				}
				datasList.add(data);
			}
		}

		public ExcitionActivity getData(int id) {
			return datasMap.get(id);
		}

		List<ExcitionActivity> getDatas() {
			return datasList;
		}

		void notifyCacheLoadComplete(ExcitingRuleManager mExcitingRuleManager) throws Exception {
			for (ExcitionActivity data : datasList) {
				data.notifyCacheLoadComplete(mExcitingRuleManager);
			}
		}

		/**
		 * <pre>
		 * 精彩活动
		 * 
		 * @author CamusHuang
		 * @creation 2013-6-11 下午4:45:34
		 * </pre>
		 */
		public static class ExcitionActivity extends RewardRuleBoolanData {
			// ----------以下是EXCEL表格直导数据---------
			public int id;// 活动ID
			public String label;// 标签
			public boolean isAutoSendReward;// 是否自动发奖
			public String title;// 标题
			public String content;// 更新内容

			public int uiLink;// 功能或者玩法界面
			private int[] ruleIds;// 规则ID

			private String remark;// 备注-1;2;1:1000
			private String remarkdesc;// 备注说明-备注类型1;VIP2;钻石:1000

			// ----------以下是逻辑数据---------
			public Map<Integer, RewardRule> ruleMap = new LinkedHashMap<Integer, RewardRule>();
			public CATime caTime;//活动时间
			
			// 购买信息
			public KCurrencyCountStruct buyPrice;// 支付价格
			public int buyVipLv;// 要求的VIP最小等级

			void notifyCacheLoadComplete(ExcitingRuleManager mExcitingRuleManager) throws Exception {
				// 替换字符串
				{
					content = content.replaceAll("【startTime】", caTime.startTimeStr).replaceAll("【endTime】", caTime.endTimeStr);
				}

				// 备注解释
				{
					if (!remark.isEmpty()) {
						String[] rs = remark.split(";");
						if (rs[0].equals("1")) {
							// 购买信息
							buyVipLv = Integer.parseInt(rs[1]);
							if (buyVipLv < 0) {
								throw new Exception("活动VIP门槛错误=" + buyVipLv);
							}
							rs = rs[2].split(":");
							KCurrencyTypeEnum type = KCurrencyTypeEnum.getEnum(Integer.parseInt(rs[0]));
							long moneyCount = Long.parseLong(rs[1]);
							buyPrice = new KCurrencyCountStruct(type, moneyCount);

							if (moneyCount < 1 || type == null) {
								throw new Exception("活动支付门槛错误=" + remark);
							}
						}
					}
				}

				//
				int isExcuteFirstChargeCount = 0;
				for (Integer ruleId : ruleIds) {
					RewardRule rule = mExcitingRuleManager.getData(ruleId);

					if (rule == null) {
						throw new Exception("规则不存在 规则ID=" + ruleId);
					}
					if (ruleMap.put(rule.ruleId, rule) != null) {
						throw new Exception("重复的规则 规则ID=" + ruleId);
					}

					super.copyTrue(rule);

					if (rule.isSenstiveForChargeInOneTime() && !isAutoSendReward) {
						throw new Exception("单次充值活动必须自动发奖");
					}
					if (rule.isSenstiveForExpTaskRewardLv() && !isAutoSendReward) {
						throw new Exception("经验任务活动必须自动发奖");
					}
					if (rule.isSenstiveForVitalityRewardLv() && !isAutoSendReward) {
						throw new Exception("活跃度活动必须自动发奖");
					}

					if (rule.isExcuteFirstCharge()) {
						isExcuteFirstChargeCount++;
					}
				}

				if (isExcuteFirstChargeCount > 0 && isExcuteFirstChargeCount != ruleIds.length) {
					throw new Exception("同一活动的所有规则的参数isExcuteFirstCharge必须统一");
				}
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 
	 * 
	 * @author CamusHuang
	 * @creation 2014-11-17 下午1:08:03
	 * </pre>
	 */
	public static class TimeLimitActivityDataManager {
		private List<TimeLimieProduceActivity> timeLimieProduceActivityList = new ArrayList<TimeLimieProduceActivity>();
		// <枚举类型,数据>
		private Map<KLimitTimeProduceActivityTypeEnum, TimeLimieProduceActivity> timeLimieProduceActivityMap = new HashMap<KLimitTimeProduceActivityTypeEnum, TimeLimieProduceActivity>();

		/**
		 * 节假日普通副本特殊掉落奖励表
		 */
		public Map<Integer, HolidayCopySpecialReward> normalCopyRewardMap = new LinkedHashMap<Integer, HolidayCopySpecialReward>();
		/**
		 * 节假日经验副本特殊掉落奖励表
		 */
		public Map<Integer, HolidayCopySpecialReward> eliteCopyRewardMap = new LinkedHashMap<Integer, HolidayCopySpecialReward>();
		/**
		 * 节假日特殊奖励掉落池
		 */
		public Map<Integer, KDropGroup> dropGroupMap = new LinkedHashMap<Integer, KDropGroup>();

		private TimeLimitActivityDataManager() {
		}

		void initDatas(List<TimeLimieProduceActivity> timeLimieProduceList) throws Exception {
			for (TimeLimieProduceActivity data : timeLimieProduceList) {
				data.acitvityType = KLimitTimeProduceActivityTypeEnum.getEnum(data.type);
				if (data.acitvityType == null) {
					throw new Exception("未定义的产出活动 ID=" + data.type + " name=" + data.name);
				}
				if (timeLimieProduceActivityMap.put(data.acitvityType, data) != null) {
					throw new Exception("重复的产出活动 ID=" + data.type + " name=" + data.name);
				}
				this.timeLimieProduceActivityList.add(data);
			}
		}

		void initHolidayRewardDatas(List<HolidayCopySpecialReward> normalCopyReward, List<HolidayCopySpecialReward> eliteCopyReward, List<KDropGroup> dropDroup) {
			for (HolidayCopySpecialReward data : normalCopyReward) {
				this.normalCopyRewardMap.put(data.levelId, data);
			}
			for (HolidayCopySpecialReward data : eliteCopyReward) {
				this.eliteCopyRewardMap.put(data.levelId, data);
			}
			for (KDropGroup data : dropDroup) {
				this.dropGroupMap.put(data.groupId, data);
			}
		}

		TimeLimieProduceActivity getTimeLimieProduceActivityData(KLimitTimeProduceActivityTypeEnum type) {
			return timeLimieProduceActivityMap.get(type);
		}

		List<TimeLimieProduceActivity> getDataCache() {
			return timeLimieProduceActivityList;
		}

		void notifyCacheLoadComplete(ExcitingRuleManager mExcitingRuleManager) throws Exception {
			for (Iterator<TimeLimieProduceActivity> it = timeLimieProduceActivityList.iterator(); it.hasNext();) {
				TimeLimieProduceActivity data = it.next();
				data.notifyCacheLoadComplete();
			}
			for (HolidayCopySpecialReward data : normalCopyRewardMap.values()) {
				data.notifyCacheLoadComplete();
			}
			for (HolidayCopySpecialReward data : eliteCopyRewardMap.values()) {
				data.notifyCacheLoadComplete();
			}
		}

		public List<ItemCountStruct> caculateHolidayActivityReward(KRole role, KGameLevelTypeEnum levelType, int levelId) {
			List<ItemCountStruct> itemList = new ArrayList<ItemCountStruct>();

			HolidayCopySpecialReward reward = null;
			if (levelType == KGameLevelTypeEnum.普通关卡) {
				reward = normalCopyRewardMap.get(levelId);
			} else if (levelType == KGameLevelTypeEnum.精英副本关卡) {
				reward = eliteCopyRewardMap.get(levelId);
			}

			if (reward != null) {
				itemList.addAll(reward.caculateItemReward(role.getJob(), 1));
				itemList.addAll(reward.caculateDropPoolItems(1));

				itemList = ItemCountStruct.mergeItemCountStructs(itemList);
			}

			return itemList;
		}

		/**
		 * 限时产出（或神秘商人打折）活动数据
		 * 
		 * @author Administrator
		 * 
		 */
		public static class TimeLimieProduceActivity {

			public final static byte NIUDAN_ACT_TYPE_NORMAL = 0;// 扭蛋活动的类型：屌丝扭蛋
			public final static byte NIUDAN_ACT_TYPE_RICH = 1;// 扭蛋活动的类型：高富帅扭蛋

			public int type;
			public String name;// 标题
			public String tips;
			public String desc;
			/**
			 * 表示当前版本，若需要第二次开启活动，则需要升级为V2，以此类推，高版本的数据将会覆盖低版本；
			 * 
			 * 版本仅针对有数据保存的活动有效： 例如【9金币兑换】有兑换限额，更换版本号即可重新计算限额
			 * 例如【12初级扭蛋10连抽打折】有打折次数，更换版本号即可重新计算次数
			 * 例如【17限时充值优惠】有时效计算，更换版本号即可重新计算时效
			 */
			public int version;// 活动版本
			public float expRate;// 经验倍数
			public float goldRate;// 金币倍数（也适用于金币兑换的倍数）
			public float potentialRate;// 潜能倍数
			public int itemMultiple;// 道具掉落倍数
			public int itemMultipleRate;// 道具掉落倍数的出现概率
			public int discount;// 商店打折数具（百分比）
			public int discountTimes;// 允许使用打折次数(如 扭蛋打折，允许前5次打折)
			public String[] discountItemCode;// 打折物品code
			public Set<String> discountItemCodeSet;// 打折物品code
			public float probabilityRatio;// 通用几率翻倍的倍数

			public byte niudanType;// 扭蛋类型，参考NIUDAN_ACT_TYPE_NORMAL与NIUDAN_ACT_TYPE_RICH
			public String niudanIndexInfo;// 参与扭蛋几率翻倍的序号ID（参考扭蛋表序号）
			public Set<Integer> niudanSet = new HashSet<Integer>();// 参与扭蛋几率翻倍的序号ID的Set

			public int[] carrierId;// 参与物资运输活动的奖励翻倍指定载具ID
			public float[] goldActivityCardId;// 参与赚金币活动卡牌翻倍的指定卡牌序号

			public boolean isSendMail;// 是否需要发送邮件
			public String mailTitle;
			public String mailContent;

			public KLimitTimeProduceActivityTypeEnum acitvityType;

			public long activity9_MAX;// 活动9：金币兑换额外奖励，最大兑换上限
			public int activity15_MIN;// 活动15：机甲进阶经验倍率，进阶次数要求
			public long activity17_PERIOD;// 活动17：充值还现有效时长
			public Set<Integer> activity21_starLvSet;// 活动21：参与活动的星阶

			public boolean isExpDouble = false;// 是否经验产出双倍活动
			public boolean isGoldDouble = false;// 是否金币产出双倍活动
			public boolean isPotentialDouble = false;// 是否潜能产出双倍活动
			public boolean isDropItemDouble = false;// 是否道具掉落双倍活动
			//
			public KItemTempFixedBox dreamGiftItemTemp;// 梦想话费礼包
			public KItemTempFixedBox commonGiftItemTemp;// 梦想非话费礼包
			//
			public KItemTempRandomBox mutiMapItemTemp;// 副本实物礼包
			public Map<String, Integer> mutiMapReward;// 副本实物奖励及数量
			
			public byte[] wish2PoolType;//幸运转盘抽奖池类型
			public String wish2PresentItemCode;//幸运转盘10连抽赠送道具信息
			public Map<Byte,List<ItemCountStruct>> wish2PresentItemList = new LinkedHashMap<Byte, List<ItemCountStruct>>();////幸运转盘10连抽赠送道具表

			/**
			 * 普通扭蛋10连抽赠送道具表
			 */
			public List<ItemCountStruct> poorWish10PresentItems = new ArrayList<ItemCountStruct>();
			/**
			 * 高级扭蛋10连抽赠送道具表
			 */
			public List<ItemCountStruct> richWish10PresentItems = new ArrayList<ItemCountStruct>();

			// 活动时间
			public CommonActivityTime mCommonActivityTime;
			
			/**
			 * <pre>
			 * 当前是否在活动开启过程中
			 * 
			 * @return
			 * @author CamusHuang
			 * @creation 2014-11-12 下午12:32:12
			 * </pre>
			 */
			public boolean isActivityTakeEffectNow() {
				long nowTime = System.currentTimeMillis();
				return mCommonActivityTime.isInEffectTime(nowTime);
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
			public long getActivityReleaseTime() {
				long nowTime = System.currentTimeMillis();
				return mCommonActivityTime.getReleaseEffectTime(nowTime);
			}

			void notifyCacheLoadComplete() throws Exception {
				if (isSendMail) {
					if (mailTitle == null || mailTitle.isEmpty()) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">mailTitle出错 =" + mailTitle);
					}
					if (mailContent == null || mailContent.isEmpty()) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">mailContent出错 =" + mailContent);
					}
				}

				if (acitvityType == KLimitTimeProduceActivityTypeEnum.话费礼包活动) {
					if (discountItemCode == null || discountItemCode.length != 2) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode错误，该值必须生效");
					}

					for (String itemCode : discountItemCode) {
						KItemTempAbs temp = KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode);
						if (temp == null) {
							throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode错误，物品不存在=" + itemCode);
						}
						// 涉及到激活码保存，不允许随意修改限制
						if (temp.ItemType != KItemTypeEnum.固定宝箱) {
							throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode错误，物品必须为" + KItemTypeEnum.固定宝箱.name());
						}
					}

					commonGiftItemTemp = (KItemTempFixedBox) KSupportFactory.getItemModuleSupport().getItemTemplate(discountItemCode[0]);
					dreamGiftItemTemp = (KItemTempFixedBox) KSupportFactory.getItemModuleSupport().getItemTemplate(discountItemCode[1]);
				}

				if (acitvityType == KLimitTimeProduceActivityTypeEnum.节假副本特殊掉落) {
					if (discountItemCode == null || discountItemCode.length != 1) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode错误，该值必须指定1种物品");
					}

					{
						String itemCode = discountItemCode[0];
						KItemTempAbs temp = KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode);
						if (temp == null) {
							throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode错误，物品不存在=" + itemCode);
						}
						// 涉及到激活码保存，不允许随意修改限制
						if (temp.ItemType != KItemTypeEnum.随机宝箱) {
							throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode错误，物品必须为" + KItemTypeEnum.随机宝箱.name());
						}
					}

					mutiMapItemTemp = (KItemTempRandomBox) KSupportFactory.getItemModuleSupport().getItemTemplate(discountItemCode[0]);

					{
						mutiMapReward = new HashMap<String, Integer>();
						String[] datas = tips.split(";")[0].split(",");
						for (String data : datas) {
							if (data.isEmpty()) {
								continue;
							}
							String[] temps = data.split("x");
							mutiMapReward.put(temps[0], Integer.parseInt(temps[1]));
							if (Integer.parseInt(temps[1]) < 1) {
								throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">tips错误=" + tips);
							}
						}
					}
				}

				if (acitvityType == KLimitTimeProduceActivityTypeEnum.材料打折) {
					if (discount < 1 || discount >= 100) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discount错误=" + discount);
					}

					if (discountItemCode == null || discountItemCode.length == 0) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode错误，当活动类型为升星石打折时，该值必须生效");
					}

					discountItemCodeSet = new HashSet<String>();
					for (String itemCode : discountItemCode) {
						if (KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode) == null) {
							throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode错误，物品不存在=" + itemCode);
						}
						discountItemCodeSet.add(itemCode);
					}
				}

				if (acitvityType == KLimitTimeProduceActivityTypeEnum.金币兑换额外奖励) {
					if (goldRate <= 1) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " goldRate错误=" + goldRate);
					}

					activity9_MAX = Long.parseLong(tips.split(";")[0]);
					if (activity9_MAX < 1) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " tips错误=" + tips);
					}
				}
				
				if (acitvityType == KLimitTimeProduceActivityTypeEnum.装备升星成功率) {
					if (probabilityRatio <= 1) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " probabilityRatio错误=" + probabilityRatio);
					}

					{
						activity21_starLvSet = new HashSet<Integer>();
						String[] datas = tips.split(";")[0].split(",");
						for (String data : datas) {
							int starLv = Integer.parseInt(data);
							if (starLv < 1) {
								throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">tips错误=" + tips);
							}
							activity21_starLvSet.add(starLv);
						}
						
						if (activity21_starLvSet.isEmpty()) {
							throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " tips错误=" + tips);
						}
					}
				}

				if (acitvityType == KLimitTimeProduceActivityTypeEnum.机甲升级经验倍率) {
					if (expRate <= 1) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">expRate错误=" + expRate);
					}

					activity15_MIN = Integer.parseInt(tips.split(";")[0]);
					if (activity15_MIN < 1) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">tips错误=" + tips);
					}
				}

				if (acitvityType == KLimitTimeProduceActivityTypeEnum.限时充值优惠) {
					if (goldRate <= 0) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">goldRate错误=" + goldRate);
					}

					activity17_PERIOD = UtilTool.parseDHMS(tips.split(";")[0]);
					if (activity17_PERIOD < Timer.ONE_MINUTE) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">tips错误=" + tips);
					}
				}

				if (acitvityType == KLimitTimeProduceActivityTypeEnum.初级扭蛋10连抽概率翻倍) {
					if (niudanType < 0 || niudanType > 1) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">的niudanType=" + niudanType + "格式错误，当活动类型为初级扭蛋10连抽打折时，该值必须为0或1");
					}
					if (niudanIndexInfo == null || niudanIndexInfo.length() == 0 || niudanIndexInfo.equals("0")) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">的niudanIndexInfo格式错误，当活动类型为初级扭蛋10连抽概率翻倍时，该值必须生效，值=" + niudanIndexInfo);
					}
					String[] info = niudanIndexInfo.split(",");
					if (info == null || info.length <= 0) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">的niudanIndexInfo格式错误，当活动类型为初级扭蛋10连抽概率翻倍时，该值必须生效，值=" + niudanIndexInfo);
					}
					for (int i = 0; i < info.length; i++) {
						int index = Integer.parseInt(info[i]);
						if (!KWishItemPool.constainsItem(index)) {
							throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">的niudanIndexInfo=" + niudanIndexInfo + "格式错误，当活动类型为初级扭蛋10连抽概率翻倍时，找不到该掉落ID的物品，ID=" + index);
						}
						niudanSet.add(index);
					}
				}

				if (acitvityType == KLimitTimeProduceActivityTypeEnum.初级扭蛋10连抽打折 || acitvityType == KLimitTimeProduceActivityTypeEnum.高级扭蛋10连抽打折) {
					if (this.discount <= 0) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">的discount=" + discount + "格式错误，当活动类型为初级扭蛋10连抽打折和高级扭蛋10连抽打折时，该值必须大于0");
					}
					if (niudanType < 0 || niudanType > 1) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">的niudanType=" + niudanType + "格式错误，当活动类型为初级扭蛋10连抽打折和高级扭蛋10连抽打折时，该值必须为0或1");
					}
				}

				if (acitvityType == KLimitTimeProduceActivityTypeEnum.物资运输活动) {
					if (this.carrierId == null || this.carrierId.length == 0) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">的carrierId格式错误，当活动类型为物资运输活动时，该值不能留空");
					}
					for (int i = 0; i < this.carrierId.length; i++) {
						if (KTransportManager.getCarrierData(this.carrierId[i]) == null) {
							throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">的carrierId格式错误，找不到对应的载具ID=" + this.carrierId[i]);
						}
					}
				}

				if (acitvityType == KLimitTimeProduceActivityTypeEnum.赚金币结算卡牌奖励翻倍) {
					if (this.goldActivityCardId == null || this.goldActivityCardId.length != 5) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">的goldActivityCardId格式错误，当活动类型为物资运输活动时，该值不能留空或数组长度必须为5");
					}
				}
				
				if (acitvityType == KLimitTimeProduceActivityTypeEnum.初级扭蛋10连抽送道具) {
					if (discountItemCode == null || discountItemCode.length == 0) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode错误，当活动类型为初级扭蛋10连抽送道具时，该值必须生效");
					}
					
					for (int i = 0; i < discountItemCode.length; i++) {
						String[] itemData = discountItemCode[i].split("\\*");
						if(itemData == null || itemData.length!=2){
							throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode格式错误，当活动类型为初级扭蛋10连抽送道具时，该值格式必须为：code*数量");
						}
						if (KSupportFactory.getItemModuleSupport().getItemTemplate(itemData[0]) == null) {
							throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode错误，找不到道具类型："+itemData[0]);
						}
						poorWish10PresentItems.add(new ItemCountStruct(itemData[0], Integer.parseInt(itemData[1])));
					}
				}
				
				if (acitvityType == KLimitTimeProduceActivityTypeEnum.高级扭蛋10连抽送道具) {
					if (discountItemCode == null || discountItemCode.length == 0) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode错误，当活动类型为高级扭蛋10连抽送道具时，该值必须生效");
					}
					
					for (int i = 0; i < discountItemCode.length; i++) {
						String[] itemData = discountItemCode[i].split("\\*");
						if(itemData == null || itemData.length!=2){
							throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode格式错误，当活动类型为高级级扭蛋10连抽送道具时，该值格式必须为：code*数量");
						}
						if (KSupportFactory.getItemModuleSupport().getItemTemplate(itemData[0]) == null) {
							throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode错误，找不到道具类型："+itemData[0]);
						}
						richWish10PresentItems.add(new ItemCountStruct(itemData[0], Integer.parseInt(itemData[1])));
					}
				}
				
				if (acitvityType == KLimitTimeProduceActivityTypeEnum.精英副本消耗体力减半) {
					if (this.discount <= 0) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">的discount=" + discount + "格式错误，当活动类型为精英副本消耗体力减半时，该值必须大于0");
					}
				}
				
				if (acitvityType == KLimitTimeProduceActivityTypeEnum.赚金币增加挑战次数) {
					if (this.discountTimes <= 0) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">的discountTimes=" + discountTimes + "格式错误，当活动类型为赚金币增加挑战次数时，该值必须大于0");
					}
				}
				
				if (acitvityType == KLimitTimeProduceActivityTypeEnum.幸运转盘打折) {
					if (this.discount <= 0) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">的discount=" + discount + "格式错误，当活动类型为幸运转盘打折时，该值必须大于0");
					}
					if(wish2PoolType == null || wish2PoolType.length == 0){
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " wish2PoolType错误，当活动类型为幸运转盘打折时，该值必须生效");
					}
					for (int i = 0; i < wish2PoolType.length; i++) {
						if(wish2PoolType[i]<1||wish2PoolType[i]>4){
							throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " wish2PoolType错误，当活动类型为幸运转盘打折时，该值必须范围必须为(1-4)");
						}
					}
				}
				
				if (acitvityType == KLimitTimeProduceActivityTypeEnum.幸运转盘10连抽送道具) {
					if(wish2PoolType == null || wish2PoolType.length == 0){
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " wish2PoolType错误，当活动类型为幸运转盘10连抽送道具时，该值必须生效");
					}
					for (int i = 0; i < wish2PoolType.length; i++) {
						if(wish2PoolType[i]<1||wish2PoolType[i]>4){
							throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " wish2PoolType错误，当活动类型为幸运转盘10连抽送道具时，该值必须范围必须为(1-4)");
						}
					}
					
					if (wish2PresentItemCode == null || wish2PresentItemCode.length() == 0) {
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " wish2PresentItemCode错误，当活动类型为幸运转盘10连抽送道具时，该值必须生效");
					}
					String[] poolTypeItemInfo = wish2PresentItemCode.split(";");
					if(poolTypeItemInfo == null){
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " wish2PresentItemCode格式错误，当活动类型为幸运转盘10连抽送道具时，请参考表注释的格式");
					}
					if(poolTypeItemInfo.length != wish2PoolType.length){
						throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " wish2PresentItemCode格式错误，当活动类型为幸运转盘10连抽送道具时，分割;长度必须与wish2PoolType相同");
					}
					for (int i = 0; i < poolTypeItemInfo.length; i++) {
						String[] itemDataInfo = poolTypeItemInfo[i].split(",");
						if(itemDataInfo == null){
							throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode格式错误，当活动类型为幸运转盘10连抽送道具时，请参考表注释的格式");
						}
						for (int j = 0; j < itemDataInfo.length; j++) {
							String[] itemData = itemDataInfo[j].split("\\*");
							if(itemData == null || itemData.length!=2){
								throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode格式错误，当活动类型为初级扭蛋10连抽送道具时，该值格式必须为：code*数量");
							}
							if (KSupportFactory.getItemModuleSupport().getItemTemplate(itemData[0]) == null) {
								throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode错误，找不到道具类型："+itemData[0]);
							}
							int count = Integer.parseInt(itemData[1]);
							if(count<1){
								throw new KGameServerException("初始化表<" + SheetName_限时产出活动 + ">" + acitvityType.name() + " discountItemCode错误，道具类型："+itemData[0]+"的数量必须大于0");
							}
							if(!wish2PresentItemList.containsKey(wish2PoolType[i])){
								wish2PresentItemList.put(wish2PoolType[i], new ArrayList<ItemCountStruct>());
							}
							wish2PresentItemList.get(wish2PoolType[i]).add(new ItemCountStruct(itemData[0], count));
						}						
					}
				}

				if (expRate > 1) {
					this.isExpDouble = true;
				}
				if (goldRate > 1) {
					this.isGoldDouble = true;
				}
				if (potentialRate > 1) {
					this.isPotentialDouble = true;
				}
				if (itemMultiple > 1 && itemMultipleRate > 0) {
					this.isDropItemDouble = true;
				}

				// 提交任务

			}
		}
	}

	/**
	 * 限时活动中的节假日副本特殊奖励数据结构
	 * 
	 * @author Administrator
	 * 
	 */
	public static class HolidayCopySpecialReward {
		public KGameLevelTypeEnum levelType;
		public int levelId;
		private String soldier_item;
		private String ninja_item;
		private String gun_item;
		private int drop_ID1;
		private int weight1;
		private int drop_ID2;
		private int weight2;

		// 掉落方案表
		public Map<Integer, Integer> dropPoolMap = new LinkedHashMap<Integer, Integer>();

		// 需要计算掉落概率的道具列表
		private Map<Byte, List<NormalItemRewardTemplate>> caculateItemRewardMap = new HashMap<Byte, List<NormalItemRewardTemplate>>();
		private Map<Byte, Integer> totalDropWeight = new HashMap<Byte, Integer>();

		public void notifyCacheLoadComplete() throws Exception {
			for (KJobTypeEnum job : KJobTypeEnum.values()) {
				caculateItemRewardMap.put(job.getJobType(), new ArrayList<NormalItemRewardTemplate>());
				totalDropWeight.put(job.getJobType(), 0);
			}
			String tableName = "普通关卡活动掉落";
			if (levelType == KGameLevelTypeEnum.精英副本关卡) {
				tableName = "精英关卡活动掉落";
			}
			initNormalItemReward(tableName, "soldier_item", soldier_item, KJobTypeEnum.WARRIOR.getJobType(), levelId);
			initNormalItemReward(tableName, "ninja_item", ninja_item, KJobTypeEnum.SHADOW.getJobType(), levelId);
			initNormalItemReward(tableName, "gun_item", gun_item, KJobTypeEnum.GUNMAN.getJobType(), levelId);
			if (drop_ID1 != 0) {
				if (KExcitingDataManager.mTimeLimitActivityDataManager.dropGroupMap.containsKey(drop_ID1)) {
					dropPoolMap.put(drop_ID1, weight1);
				} else {
					throw new KGameServerException("初始化表<" + tableName + ">的掉落表错误，找不到掉落组ID：" + drop_ID1 + "，字段：drop_ID1，关卡ID：" + levelId);
				}
			}
			if (drop_ID2 != 0) {
				if (KExcitingDataManager.mTimeLimitActivityDataManager.dropGroupMap.containsKey(drop_ID2)) {
					dropPoolMap.put(drop_ID2, weight2);
				} else {
					throw new KGameServerException("初始化表<" + tableName + ">的掉落表错误，找不到掉落组ID：" + drop_ID2 + "，字段：drop_ID2，关卡ID：" + levelId);
				}
			}
		}

		private List<ItemCountStruct> initNormalItemReward(String tableName, String fieldName, String dropData, byte jobType, int levelId) throws KGameServerException {
			List<ItemCountStruct> list = new ArrayList<ItemCountStruct>();
			if (dropData == null || dropData.equals("0") || dropData.length() == 0) {
				return list;
			}
			if (dropData != null) {
				String[] itemInfoStr = dropData.split(",");
				if (itemInfoStr != null && itemInfoStr.length > 0) {
					for (int i = 0; i < itemInfoStr.length; i++) {
						String[] itemData = itemInfoStr[i].split("\\*");
						if (itemData != null && itemData.length == 3) {
							NormalItemRewardTemplate itemTemplate = null;
							String itemCode = itemData[0];
							int count = Integer.parseInt(itemData[1]);
							boolean isNoneDrop = false;
							int dropWeight = Integer.parseInt(itemData[2]);
							if (itemCode.equals("0")) {
								itemTemplate = new NormalItemRewardTemplate(dropWeight);
								isNoneDrop = true;
							} else {
								itemTemplate = new NormalItemRewardTemplate(itemCode, count, dropWeight);
							}
							if (!itemTemplate.isNoneDrop && KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode) == null) {
								throw new KGameServerException("初始化表<" + tableName + ">的道具错误，找不到道具类型：" + itemCode + "，字段：" + fieldName + "=" + dropData + "，关卡ID：" + levelId);
							}
							this.caculateItemRewardMap.get(jobType).add(itemTemplate);
							int weight = 0;
							if (totalDropWeight.containsKey(jobType)) {
								weight = totalDropWeight.get(jobType);
							}
							this.totalDropWeight.put(jobType, (weight + dropWeight));

							if (!isNoneDrop) {
								list.add(new ItemCountStruct(KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode), count));
							}
						} else {
							throw new KGameServerException("初始化表<" + tableName + ">的道具格式错误：：" + dropData + "，关卡ID：" + levelId);
						}
					}
				}
			}
			return list;
		}

		public List<ItemCountStruct> caculateItemReward(byte jobType, int multiple) {
			List<ItemCountStruct> list = new ArrayList<ItemCountStruct>();
			KItemTempAbs itemTemplate;
			int totalWeight = totalDropWeight.get(jobType);
			if (totalWeight > 0) {
				int weight = UtilTool.random(0, totalWeight);
				int tempRate = 0;
				for (NormalItemRewardTemplate template : caculateItemRewardMap.get(jobType)) {
					if (tempRate < weight && weight <= (tempRate + template.dropWeight)) {
						if (!template.isNoneDrop) {
							itemTemplate = KSupportFactory.getItemModuleSupport().getItemTemplate(template.getItemCode());
							list.add(new ItemCountStruct(itemTemplate, template.rewardCount * multiple));
						}
						break;
					} else {
						tempRate += template.dropWeight;
					}
				}
			}

			return list;
		}

		public List<ItemCountStruct> caculateDropPoolItems(int multiple) {
			List<ItemCountStruct> list = new ArrayList<ItemCountStruct>();

			for (Integer dropGroupId : dropPoolMap.keySet()) {
				int weight = dropPoolMap.get(dropGroupId);
				KDropGroup group = mTimeLimitActivityDataManager.dropGroupMap.get(dropGroupId);
				if (group != null) {
					int rate = UtilTool.random(UtilTool.TEN_THOUSAND_RATIO_UNIT);
					if (weight >= rate) {
						list.add(group.caculateDropItem().getItemCountStruct(multiple));
					}
				}
			}

			return list;
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 排行榜定时奖励活动
	 * 
	 * @author CamusHuang
	 * @creation 2014-11-17 下午1:38:02
	 * </pre>
	 */
	public static class RankTimeLimitRewardDataManager {

		private Map<Integer, RankTimeLimitReward> datasMap = new HashMap<Integer, RankTimeLimitReward>();
		private List<RankTimeLimitReward> datasList = new ArrayList<RankTimeLimitReward>();

		private RankTimeLimitRewardDataManager() {
		}

		void initDatas(List<RankTimeLimitReward> datas) throws Exception {
			for (RankTimeLimitReward data : datas) {
				if (datasMap.put(data.id, data) != null) {
					throw new Exception("重复的id = " + data.id);
				}
				datasList.add(data);
			}
		}

		List<RankTimeLimitReward> getDataCache() {
			return datasList;
		}

		RankTimeLimitReward getData(int rewardId) {
			return datasMap.get(rewardId);
		}

		void notifyCacheLoadComplete() throws Exception {
			long nowTime = System.currentTimeMillis();

			for (Iterator<RankTimeLimitReward> it = datasList.iterator(); it.hasNext();) {
				RankTimeLimitReward reward = it.next();
				reward.notifyCacheLoadComplete();
				if (reward.caTime.endTime < nowTime) {
					it.remove();
				}
			}
		}

		public static class RankTimeLimitReward {

			public static final int MODE_DAY = 1;// 1表示每日指定时间发奖
			public static final int MODE_END = 2;// 2表示结束当天指定时间发奖

			// ----------以下是EXCEL表格直导数据---------
			public int id;
			public String name;// 活动名称
			public int rankType;// 排行榜类型
			public int mode;// 发奖模式
			private String sendTimeStr;// 发奖时刻
			public int startRank;// 发奖起始名次(包含）
			public int endRank;// 发奖结束名次(包含）
			private String otherParams;// 其它参数

			// ----------以下是逻辑数据---------
			public CATime caTime;// 活动时间
			public long sendTime;// 发奖时刻(偏移当天00:00的毫秒数)
			public Set<KGangPositionEnum> gangPositionSet = Collections.emptySet();// 配合军团榜使用，发奖的职位
			BaseMailRewardData rewardMail;

			void notifyCacheLoadComplete() throws Exception {

				if (mode != MODE_DAY && mode != MODE_END) {
					throw new Exception("数据错误 mode=" + mode +" id=" + id);
				}

				if (startRank > endRank) {
					throw new Exception("startRank不能>endRank id=" + id);
				}

				if (KGangRankTypeEnum.getMinTypeSign() <= rankType && rankType <= KGangRankTypeEnum.getMaxTypeSign()) {
					//
					if (otherParams != null && !otherParams.isEmpty()) {
						gangPositionSet = new HashSet<KGangPositionEnum>();
						for (String temp : otherParams.split(",")) {
							gangPositionSet.add(KGangPositionEnum.getEnum(Integer.parseInt(temp)));
						}
					}
				}

				sendTime = UtilTool.parseHHmmToMillis(sendTimeStr);

				Calendar cal = UtilTool.getNextNDaysStart(caTime.endTime, 0);
				long lastSendTime = cal.getTimeInMillis() + sendTime;
				if (caTime.endTime < lastSendTime) {
					throw new Exception("最后一天的结束时间不能小于发奖时间 id=" + id);
				}

				rewardMail.notifyCacheLoadComplete();

				// 检查奖励内容是否有效
				if (!rewardMail.checkIsEffect()) {
					throw new Exception("所有奖励项无效 id=" + id);
				}
			}

			/**
			 * <pre>
			 * 计算指定时间之后的下次发奖时间，没有则返回-1
			 * 
			 * @param nowTime
			 * @return
			 * @author CamusHuang
			 * @creation 2014-12-2 下午10:03:41
			 * </pre>
			 */
			long getNextRewardCollectTime(long nowTime) {
				if (mode == MODE_DAY) {
					// 每日发奖模式
					long todaySendTime = UtilTool.getNextNDaysStart(nowTime, 0).getTimeInMillis() + sendTime;
					if (nowTime < todaySendTime) {
						// 今天的发送时间未过
						return todaySendTime;
					}

					long nextSendTime = todaySendTime + Timer.ONE_DAY;
					if (nextSendTime > caTime.endTime) {
						// 明天的发送时间活动已结束
						return -1;
					}

					return nextSendTime;
				} else {
					// 结束日发奖模式
					long lastSendTime = UtilTool.getNextNDaysStart(caTime.endTime, 0).getTimeInMillis() + sendTime;
					if (nowTime < lastSendTime) {
						// 发送时间未过
						return lastSendTime;
					}
					return -1;
				}
			}
		}
	}

	static void notifyCacheLoadComplete() throws KGameServerException {
		notifyCacheLoadComplete(mExcitingDataManager, mExcitingRuleManager, mTimeLimitActivityDataManager, mRankTimeLimitRewardDataManager);
	}

	private static void notifyCacheLoadComplete(ExcitingDataManager mExcitingDataManager, ExcitingRuleManager mExcitingRuleManager, TimeLimitActivityDataManager mTimeLimitActivityDataManager,
			RankTimeLimitRewardDataManager mRankTimeLimitRewardDataManager) throws KGameServerException {
		try {
			mExcitingRuleManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载【" + SheetName_奖励规则 + "】错误：" + e.getMessage(), e);
		}

		try {
			mExcitingDataManager.notifyCacheLoadComplete(mExcitingRuleManager);
		} catch (Exception e) {
			throw new KGameServerException("加载【" + SheetName_精彩活动 + "】错误：" + e.getMessage(), e);
		}

		try {
			mTimeLimitActivityDataManager.notifyCacheLoadComplete(mExcitingRuleManager);
		} catch (Exception e) {
			throw new KGameServerException("加载【" + SheetName_限时产出活动 + "】错误：" + e.getMessage(), e);
		}

		try {
			mRankTimeLimitRewardDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载【" + SheetName_排行榜定时奖励 + "】错误：" + e.getMessage(), e);
		}
	}

}
