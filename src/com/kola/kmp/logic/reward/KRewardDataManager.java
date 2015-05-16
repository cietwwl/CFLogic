package com.kola.kmp.logic.reward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jdom.Element;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.PhyPowerRewardStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.DialyMailData;
import com.kola.kmp.logic.reward.KRewardDataStruct.FengceCompetitionRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.FengceFriendFubenRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.FengceRoleUplvRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.FengceSaveOnlineRoleData;
import com.kola.kmp.logic.reward.KRewardDataStruct.KAJIRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.PhyPowerRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.ShutdownRewardData;
import com.kola.kmp.logic.reward.KRewardTaskManager.OlineRoleToXMLTask;
import com.kola.kmp.logic.util.CommonActivityTime;
import com.kola.kmp.logic.util.KGameUtilTool;

/**
 * <pre>
 * 本类负责管理本模块的公共数据 
 * 
 * @author CamusHuang
 * @creation 2013-4-8 下午2:39:56
 * </pre>
 */
public class KRewardDataManager {

	static final String SheetName_每日邮件 = "每日邮件";
	static final String SheetName_体力奖励 = "体力定时领取";
	static final String SheetName_维护奖励数据表 = "维护奖励数据表";
	static final String SheetName_封测角色升级奖励表 = "角色升级奖励";
	static final String SheetName_封测好友副本奖励表 = "好友副本奖励";
	static final String SheetName_封测竞技场排名奖励表 = "竞技场排名奖励";
	static final String SheetName_定时导出在线角色数据 = "定时导出在线角色数据";
	static final String SheetName_卡机补偿 = "卡机补偿";

	// 每日邮件
	public static DialyMailDataManager mDialyMailDataManager = null;
	// 维护补偿
	public static ShutdownRewardManager mShutdownRewardManager = new ShutdownRewardManager();
	// 封测期间角色升级奖励
	public static FengceRoleUpLvRewardManager mFengceRoleUpLvRewardManager = new FengceRoleUpLvRewardManager();
	// 封测期间角色好友副本波数奖励
	public static FengceFriendFubenRewardManager mFengceFriendFubenRewardManager = new FengceFriendFubenRewardManager();
	// 封测期间竞技排名奖励
	public static FengceCompetitionRewardManager mFengceCompetitionRewardManager = new FengceCompetitionRewardManager();
	// 定时导出在线角色数据
	public static FengceSaveOnlineRoleManager mFengceSaveOnlineRoleManager = new FengceSaveOnlineRoleManager();
	// 免费领取体力数据
	public static PhyPowerDataManager mPhyPowerDataManager = new PhyPowerDataManager();
	// 卡机补偿管理器
	public static KAJIRewardManager mKAJIRewardManager = new KAJIRewardManager();

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 每日邮件数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2014-1-6 下午8:46:01
	 * </pre>
	 */
	public static class DialyMailDataManager {
		private static String url;
		private static int headerIndex;

		/**
		 * <pre>
		 * <等级, List<奖励数据>>
		 * unmodifiable
		 * </pre>
		 */
		private final Map<Integer, List<DialyMailData>> dataMap = new HashMap<Integer, List<DialyMailData>>();

		private final List<DialyMailData> allData = new ArrayList<DialyMailData>();

		private DialyMailDataManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param data
		 * @return
		 * @author CamusHuang
		 * @creation 2013-12-24 下午6:05:54
		 * </pre>
		 */
		private void addData(DialyMailData data) {
			for (int roleLv = data.minRoleLv; roleLv <= data.maxRoleLv; roleLv++) {
				List<DialyMailData> temp = dataMap.get(roleLv);
				if (temp == null) {
					temp = new ArrayList<DialyMailData>();
					dataMap.put(roleLv, temp);
				}
				temp.add(data);
			}

			allData.add(data);
		}

		/**
		 * <pre>
		 * 获取指定奖励
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2013-5-30 上午11:08:59
		 * </pre>
		 */
		public List<DialyMailData> getDatas(int roleLv) {
			List<DialyMailData> temp = dataMap.get(roleLv);
			if (temp == null) {
				return Collections.emptyList();
			}
			return temp;
		}

		private void notifyCacheLoadComplete() throws Exception {
			for (DialyMailData data : allData) {
				data.notifyCacheLoadComplete();
			}
			//
			HashMap<Integer, List<DialyMailData>> tempMap = new HashMap<Integer, List<DialyMailData>>(dataMap);
			for (Entry<Integer, List<DialyMailData>> e : tempMap.entrySet()) {
				dataMap.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 封测期间角色升级奖励
	 * 
	 * @author CamusHuang
	 * @creation 2014-8-11 上午11:39:33
	 * </pre>
	 */
	public static class FengceRoleUpLvRewardManager {
		/**
		 * <pre>
		 * <等级, 奖励数据>
		 * unmodifiable
		 * </pre>
		 */
		private final Map<Integer, BaseMailRewardData> orgDataMap = new HashMap<Integer, BaseMailRewardData>();

		private final Map<Integer, FengceRoleUplvRewardData> dataMap = new HashMap<Integer, FengceRoleUplvRewardData>();

		private FengceRoleUpLvRewardManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param data
		 * @return
		 * @author CamusHuang
		 * @creation 2013-12-24 下午6:05:54
		 * </pre>
		 */
		private void addData(int lv, BaseMailRewardData data) throws Exception {
			if (orgDataMap.put(lv, data) != null) {
				throw new Exception("等级重复 lv=" + lv);
			}
		}

		/**
		 * <pre>
		 * 获取指定奖励
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2013-5-30 上午11:08:59
		 * </pre>
		 */
		public FengceRoleUplvRewardData getData(int roleLv) {
			return dataMap.get(roleLv);
		}

		private void notifyCacheLoadComplete() throws Exception {
			for (Entry<Integer, BaseMailRewardData> data : orgDataMap.entrySet()) {
				int roleLv = data.getKey();
				BaseMailRewardData baseMailReward = data.getValue();
				//
				BaseMailContent baseMail = new BaseMailContent(StringUtil.format(baseMailReward.baseMail.getMailTitle(), roleLv), StringUtil.format(baseMailReward.baseMail.getMailContent(), roleLv),
						null, null);
				baseMailReward = new BaseMailRewardData(roleLv, baseMail, baseMailReward.baseRewardData);
				dataMap.put(roleLv, new FengceRoleUplvRewardData(roleLv, baseMailReward));
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 封测期间角色好友副本波数奖励
	 * 
	 * @author CamusHuang
	 * @creation 2014-8-11 上午11:39:33
	 * </pre>
	 */
	public static class FengceFriendFubenRewardManager {
		/**
		 * <pre>
		 * <波数, 奖励数据>
		 * unmodifiable
		 * </pre>
		 */
		private final Map<Integer, BaseMailRewardData> orgDataMap = new LinkedHashMap<Integer, BaseMailRewardData>();

		private final Map<Integer, FengceFriendFubenRewardData> dataMap = new HashMap<Integer, FengceFriendFubenRewardData>();

		private FengceFriendFubenRewardManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param data
		 * @return
		 * @author CamusHuang
		 * @creation 2013-12-24 下午6:05:54
		 * </pre>
		 */
		private void addData(int GetQuantity, BaseMailRewardData data) throws Exception {
			if (orgDataMap.put(GetQuantity, data) != null) {
				throw new Exception("波数重复 GetQuantity=" + GetQuantity);
			}
		}

		/**
		 * <pre>
		 * 获取指定奖励
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2013-5-30 上午11:08:59
		 * </pre>
		 */
		public FengceFriendFubenRewardData getData(int GetQuantity) {
			return dataMap.get(GetQuantity);
		}

		private void notifyCacheLoadComplete() throws Exception {
			for (Entry<Integer, BaseMailRewardData> data : orgDataMap.entrySet()) {
				int GetQuantity = data.getKey();
				BaseMailRewardData mailReward = data.getValue();
				BaseMailContent baseMail = mailReward.baseMail;

				baseMail = new BaseMailContent(StringUtil.format(baseMail.getMailTitle(), GetQuantity), StringUtil.format(baseMail.getMailContent(), GetQuantity), null, null);
				mailReward = new BaseMailRewardData(GetQuantity, baseMail, mailReward.baseRewardData);

				FengceFriendFubenRewardData rewardData = new FengceFriendFubenRewardData(GetQuantity, mailReward);
				dataMap.put(GetQuantity, rewardData);
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 封测期间竞技场排名奖励
	 * 
	 * @author CamusHuang
	 * @creation 2014-8-11 上午11:39:33
	 * </pre>
	 */
	public static class FengceCompetitionRewardManager {
		/**
		 * <pre>
		 * <排名, 奖励数据>
		 * unmodifiable
		 * </pre>
		 */
		private final Map<Integer, BaseMailRewardData> orgDataMap = new LinkedHashMap<Integer, BaseMailRewardData>();

		private final Map<Integer, FengceCompetitionRewardData> dataMap = new HashMap<Integer, FengceCompetitionRewardData>();

		private int maxRanking;
		private FengceCompetitionRewardData otherRankingReward;

		private FengceCompetitionRewardManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param data
		 * @return
		 * @author CamusHuang
		 * @creation 2013-12-24 下午6:05:54
		 * </pre>
		 */
		private void addData(int ranking, BaseMailRewardData data) throws Exception {
			if (orgDataMap.put(ranking, data) != null) {
				throw new Exception("排名重复 ranking=" + ranking);
			}
		}

		/**
		 * <pre>
		 * 获取指定奖励
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2013-5-30 上午11:08:59
		 * </pre>
		 */
		public FengceCompetitionRewardData getData(int ranking) {
			if (maxRanking > 0) {
				if (ranking > maxRanking) {
					return otherRankingReward;
				}
			}
			return dataMap.get(ranking);
		}

		private void notifyCacheLoadComplete() throws Exception {
			// 保证按顺序填表
			{
				int lastRanking = 0;
				for (Entry<Integer, BaseMailRewardData> data : orgDataMap.entrySet()) {
					int ranking = data.getKey();
					if (ranking == 0) {
						continue;
					}
					if (ranking <= lastRanking) {
						throw new Exception("请按顺序填写排名奖励");
					}
					lastRanking = ranking;
					data.getValue().notifyCacheLoadComplete();
				}
			}

			//
			{
				int maxRanking = 0;
				FengceCompetitionRewardData otherReward = null;
				for (Entry<Integer, BaseMailRewardData> data : orgDataMap.entrySet()) {
					int ranking = data.getKey();
					BaseMailRewardData mailReward = data.getValue();
					FengceCompetitionRewardData tempReward = new FengceCompetitionRewardData(ranking, mailReward.baseMail, mailReward.baseRewardData);
					if (ranking == 0) {
						otherReward = tempReward;
						continue;
					}

					for (int temp = maxRanking + 1; temp <= ranking; temp++) {
						dataMap.put(temp, tempReward);
					}
					maxRanking = ranking;
				}
				if (otherReward != null) {
					this.maxRanking = maxRanking;
					otherRankingReward = otherReward;
				}
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 定时导出在线角色数据
	 * 
	 * @author CamusHuang
	 * @creation 2014-9-20 下午3:21:25
	 * </pre>
	 */
	public static class FengceSaveOnlineRoleManager {
		/**
		 * <pre>
		 * <数据>
		 * </pre>
		 */
		private final List<FengceSaveOnlineRoleData> dataList = new ArrayList<FengceSaveOnlineRoleData>();

		private FengceSaveOnlineRoleManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param data
		 * @return
		 * @author CamusHuang
		 * @creation 2013-12-24 下午6:05:54
		 * </pre>
		 */
		private void init(List<FengceSaveOnlineRoleData> datas) {
			dataList.addAll(datas);
		}

		/**
		 * <pre>
		 * 获取指定奖励
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2013-5-30 上午11:08:59
		 * </pre>
		 */
		public List<FengceSaveOnlineRoleData> getDataCache() {
			return dataList;
		}

		private void notifyCacheLoadComplete() throws Exception {
			Set<Long> times = new HashSet<Long>();
			for (FengceSaveOnlineRoleData data : dataList) {
				data.notifyCacheLoadComplete();
				if(!times.add(data.dateTimeInMills)){
					throw new Exception("重复的导出时间");
				}
			}

			// 提交时效任务
			for (FengceSaveOnlineRoleData data : dataList) {
				new OlineRoleToXMLTask(data);
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 维护奖励管理器
	 * 
	 * @author CamusHuang
	 * @creation 2014-1-6 下午8:46:01
	 * </pre>
	 */
	public static class ShutdownRewardManager {
		/**
		 * <pre>
		 * <等级, List<奖励数据>>
		 * unmodifiable
		 * </pre>
		 */
		private final Map<Integer, List<ShutdownRewardData>> dataMap = new HashMap<Integer, List<ShutdownRewardData>>();

		private final List<ShutdownRewardData> allData = new ArrayList<ShutdownRewardData>();

		private ShutdownRewardManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param data
		 * @return
		 * @author CamusHuang
		 * @creation 2013-12-24 下午6:05:54
		 * </pre>
		 */
		private void addData(ShutdownRewardData data) {
			for (int roleLv = data.minRoleLv; roleLv <= data.maxRoleLv; roleLv++) {
				List<ShutdownRewardData> temp = dataMap.get(roleLv);
				if (temp == null) {
					temp = new ArrayList<ShutdownRewardData>();
					dataMap.put(roleLv, temp);
				}
				temp.add(data);
			}

			allData.add(data);
		}

		/**
		 * <pre>
		 * 获取指定奖励
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2013-5-30 上午11:08:59
		 * </pre>
		 */
		public List<ShutdownRewardData> getDatas(int roleLv) {
			List<ShutdownRewardData> temp = dataMap.get(roleLv);
			if (temp == null) {
				return Collections.emptyList();
			}
			return temp;
		}

		private void notifyCacheLoadComplete() throws Exception {
			for (ShutdownRewardData data : allData) {
				data.notifyCacheLoadComplete();
			}
			//
			HashMap<Integer, List<ShutdownRewardData>> tempMap = new HashMap<Integer, List<ShutdownRewardData>>(dataMap);
			for (Entry<Integer, List<ShutdownRewardData>> e : tempMap.entrySet()) {
				dataMap.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
			}
		}
	}
	
	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 卡机补偿管理器
	 * 
	 * @author CamusHuang
	 * @creation 2015-2-3 下午3:15:40
	 * </pre>
	 */
	public static class KAJIRewardManager {
		private static String url;
		private static int headerIndex;
		/**
		 * <pre>
		 * <等级, List<奖励数据>>
		 * unmodifiable
		 * </pre>
		 */
		private final Map<Integer, List<KAJIRewardData>> dataMap = new HashMap<Integer, List<KAJIRewardData>>();

		private final List<KAJIRewardData> allData = new ArrayList<KAJIRewardData>();

		private KAJIRewardManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param data
		 * @return
		 * @author CamusHuang
		 * @creation 2013-12-24 下午6:05:54
		 * </pre>
		 */
		private void addData(KAJIRewardData data) {
			for (int roleLv = data.minRoleLv; roleLv <= data.maxRoleLv; roleLv++) {
				List<KAJIRewardData> temp = dataMap.get(roleLv);
				if (temp == null) {
					temp = new ArrayList<KAJIRewardData>();
					dataMap.put(roleLv, temp);
				}
				temp.add(data);
			}

			allData.add(data);
		}

		/**
		 * <pre>
		 * 获取指定奖励
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2013-5-30 上午11:08:59
		 * </pre>
		 */
		public List<KAJIRewardData> getDatas(int roleLv) {
			List<KAJIRewardData> temp = dataMap.get(roleLv);
			if (temp == null) {
				return Collections.emptyList();
			}
			return temp;
		}

		private void notifyCacheLoadComplete() throws Exception {
			for (KAJIRewardData data : allData) {
				data.notifyCacheLoadComplete();
			}
		}
	}	

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 免费领取体力数据
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-26 下午4:50:41
	 * </pre>
	 */
	public static class PhyPowerDataManager {
		private static String url;
		private static int headerIndex;

		private final List<PhyPowerRewardData> allData = new ArrayList<PhyPowerRewardData>();
		private PhyPowerRewardData lastData;

		private PhyPowerDataManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param data
		 * @return
		 * @author CamusHuang
		 * @creation 2013-12-24 下午6:05:54
		 * </pre>
		 */
		private void initDatas(List<PhyPowerRewardData> data) {
			allData.clear();
			allData.addAll(data);
		}

		/**
		 * <pre>
		 * 
		 * @deprecated
		 * @return
		 * @author CamusHuang
		 * @creation 2015-1-26 下午6:05:43
		 * </pre>
		 */
		List<PhyPowerRewardData> getAllDataCache() {
			return allData;
		}
		
		PhyPowerRewardData getLastStruct(){
			return lastData;
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @deprecated 注意传入的参数
		 * @param time 现在相对于今天00:00的偏移值
		 * @return
		 * @author CamusHuang
		 * @creation 2015-1-29 下午3:53:35
		 * </pre>
		 */
		PhyPowerRewardStruct getEffectTimeStruct(long time) {
			if(allData.isEmpty()){
				return null;
			}
			
			PhyPowerRewardStruct result = new PhyPowerRewardStruct();

			for (int i = 0; i < allData.size(); i++) {
				PhyPowerRewardData data = allData.get(i);
				//
				if(time < data.timeInterval.getBeginTime()){
					result.nextTime = data;
					return result;
				}
				
				if (time < data.timeInterval.getEndTime()) {
					result.nowTime = data;
					if (i+1 < allData.size()) {
						result.nextTime = allData.get(i + 1);
					} else {
						result.nextTime = allData.get(0);
					}
					return result;
				}
			}
			
			result.nextTime = allData.get(0);
			return result;
		}

		/**
		 * <pre>
		 * 当前时间存在的数据
		 * 
		 * @deprecated 注意传入的参数
		 * @param time 现在相对于今天00:00的偏移值
		 * @return
		 * @author CamusHuang
		 * @creation 2015-1-29 下午3:54:25
		 * </pre>
		 */
		PhyPowerRewardData getEffectTime(long time) {
			if(allData.isEmpty()){
				return null;
			}
			for (int i = 0; i < allData.size(); i++) {
				PhyPowerRewardData data = allData.get(i);
				//
				if(time < data.timeInterval.getBeginTime()){
					return null;
				}
				
				if (time < data.timeInterval.getEndTime()) {
					return data;
				}
			}
			return null;
		}

		private void notifyCacheLoadComplete() throws Exception {
			if(allData.isEmpty()){
				return;
			}
			for (PhyPowerRewardData data : allData) {
				data.notifyCacheLoadComplete();
			}
			//
			Collections.sort(allData);
			lastData = allData.get(allData.size()-1);
			//
			PhyPowerRewardData minOne = null;
			for (PhyPowerRewardData data : allData) {
				if(minOne!=null){
					if (data.timeInterval.getBeginTime() <= minOne.timeInterval.getEndTime()) {
						throw new Exception("时间重叠");
					}
				}
				minOne=data;
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

		// 加载奖励总表
		{
			Element tempE = excelE.getChild("reward");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
			{
				// 通知子模块
				for (KRewardSonModuleAbs impl : KRewardModuleExtension.rewardSonImplMap.values()) {
					impl.goToParamsRewardData(file, HeaderIndex);
				}

				// 维护奖励
				loadShutdownRewardDatas(file.getTable(SheetName_维护奖励数据表, HeaderIndex));
			}
		}
		
		// 加载封测奖励
		{
			Element tempE = excelE.getChild("fengceReward");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
			{
				loadFengceRoleUplvRewardDatas(file.getTable(SheetName_封测角色升级奖励表, HeaderIndex));

				loadFengceFriendFubenRewardDatas(file.getTable(SheetName_封测好友副本奖励表, HeaderIndex));

				loadFengceCompetionRewardDatas(file.getTable(SheetName_封测竞技场排名奖励表, HeaderIndex));
				
				loadFengceSaveOnlineRoleDatas(file.getTable(SheetName_定时导出在线角色数据, HeaderIndex));
			}
		}
		
		// 加载每日邮件数据
		{
			Element tempE = excelE.getChild("dialyMail");
			String url = tempE.getChildTextTrim("path");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			{
				DialyMailDataManager.url = url;
				DialyMailDataManager.headerIndex = HeaderIndex;
				//
				mDialyMailDataManager = loadDialyMailRewardDatas();
			}
		}

		// 加载免费体力数据
		{
			Element tempE = excelE.getChild("phyPowerReward");
			String url = tempE.getChildTextTrim("path");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			{
				PhyPowerDataManager.url = url;
				PhyPowerDataManager.headerIndex = HeaderIndex;
				//
				mPhyPowerDataManager = loadPhyPowerRewardDatas();
			}
		}
		
		// 加载卡机补偿
		{
			Element tempE = excelE.getChild("kajiReward");
			String url = tempE.getChildTextTrim("path");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			{
				KAJIRewardManager.url = url;
				KAJIRewardManager.headerIndex = HeaderIndex;
				//
				mKAJIRewardManager = loadKAJIRewardDatas();
			}
		}

		// 加载各子模块独立的数据表
		for (KRewardSonModuleAbs impl : KRewardModuleExtension.rewardSonImplMap.values()) {
			impl.goToLoadData(excelE);
		}
	}

	private static void loadShutdownRewardDatas(KGameExcelTable table) throws Exception {
		// 不填数据即没有维护奖励
		KGameExcelRow[] rows = table.getAllDataRows();

		for (KGameExcelRow row : rows) {
			try {
				int minRoleLv = row.getInt("minRoleLv");
				if (minRoleLv < 0) {
					throw new Exception("minRoleLv=" + minRoleLv);
				}

				int maxRoleLv = row.getInt("maxRoleLv");
				if (maxRoleLv < 0) {
					throw new Exception("maxRoleLv=" + maxRoleLv);
				}

				BaseMailRewardData baseMailRewardData = BaseMailRewardData.loadData(row, false);

				ShutdownRewardData data = new ShutdownRewardData(minRoleLv, maxRoleLv, baseMailRewardData);

				mShutdownRewardManager.addData(data);

			} catch (Exception e) {
				throw new KGameServerException("加载" + table.getTableName() + "错误：" + e.getMessage() + "，Row=" + row.getIndexInFile(), e);
			}
		}

	}

	private static void loadFengceRoleUplvRewardDatas(KGameExcelTable table) throws Exception {
		// 不填数据即没有奖励
		KGameExcelRow[] rows = table.getAllDataRows();

		for (KGameExcelRow row : rows) {
			try {
				int lv = row.getInt("lv");
				if (lv < 1) {
					throw new Exception("lv=" + lv);
				}

				BaseMailRewardData baseRewardData = BaseMailRewardData.loadData(row, false);

				mFengceRoleUpLvRewardManager.addData(lv, baseRewardData);

			} catch (Exception e) {
				throw new KGameServerException("加载" + table.getTableName() + "错误：" + e.getMessage() + "，Row=" + row.getIndexInFile(), e);
			}
		}

	}

	private static void loadFengceFriendFubenRewardDatas(KGameExcelTable table) throws Exception {
		// 不填数据即没有奖励
		KGameExcelRow[] rows = table.getAllDataRows();

		for (KGameExcelRow row : rows) {
			try {
				int GetQuantity = row.getInt("GetQuantity");
				if (GetQuantity < 1) {
					throw new Exception("GetQuantity=" + GetQuantity);
				}

				BaseMailRewardData baseRewardData = BaseMailRewardData.loadData(row, false);

				mFengceFriendFubenRewardManager.addData(GetQuantity, baseRewardData);

			} catch (Exception e) {
				throw new KGameServerException("加载" + table.getTableName() + "错误：" + e.getMessage() + "，Row=" + row.getIndexInFile(), e);
			}
		}

	}

	private static void loadFengceCompetionRewardDatas(KGameExcelTable table) throws Exception {
		// 不填数据即没有奖励
		KGameExcelRow[] rows = table.getAllDataRows();

		for (KGameExcelRow row : rows) {
			try {
				int ranking = row.getInt("ranking");
				if (ranking < 0) {
					throw new Exception("ranking=" + ranking);
				}

				BaseMailRewardData baseRewardData = BaseMailRewardData.loadData(row, false);

				mFengceCompetitionRewardManager.addData(ranking, baseRewardData);

			} catch (Exception e) {
				throw new KGameServerException("加载" + table.getTableName() + "错误：" + e.getMessage() + "，Row=" + row.getIndexInFile(), e);
			}
		}

	}

	private static void loadFengceSaveOnlineRoleDatas(KGameExcelTable table) throws Exception {
		// 不填数据即没有奖励
		KGameExcelRow[] rows = table.getAllDataRows();
		if (rows.length < 1) {
			return;
		}

		List<FengceSaveOnlineRoleData> datas = ReflectPaser.parseExcelData(FengceSaveOnlineRoleData.class, table.getHeaderNames(), rows, true);
		mFengceSaveOnlineRoleManager.init(datas);
	}

	public static String reloadDialyMailRewardDatas() {
		try {
			DialyMailDataManager mDialyMailDataManager = loadDialyMailRewardDatas();
			mDialyMailDataManager.notifyCacheLoadComplete();

			KRewardDataManager.mDialyMailDataManager = mDialyMailDataManager;
			return null;
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private static DialyMailDataManager loadDialyMailRewardDatas() throws KGameServerException, Exception {
		
		// 开服当天的0点
		final long ServerStartDay = UtilTool.getNextNDaysStart(KGame.getGSFirstStartTime(), 0).getTimeInMillis();
		final long nowTime = System.currentTimeMillis();
		
		// 不填数据即没有每日邮件
		KGameExcelFile file = new KGameExcelFile(DialyMailDataManager.url);
		KGameExcelTable table = file.getTable(SheetName_每日邮件, DialyMailDataManager.headerIndex);
		String sheetName = table.getTableName();
		KGameExcelRow[] rows = KGameUtilTool.filterRowsByGSID(table);

		DialyMailDataManager mDialyMailDataManager = new DialyMailDataManager();

		for (KGameExcelRow row : rows) {
			try {
				
				int minRoleLv = row.getInt("minRoleLv");
				if (minRoleLv < 0) {
					throw new Exception("minRoleLv=" + minRoleLv);
				}

				int maxRoleLv = row.getInt("maxRoleLv");
				if (maxRoleLv < 0) {
					throw new Exception("maxRoleLv=" + maxRoleLv);
				}
				
				if (minRoleLv > maxRoleLv) {
					throw new Exception("最小等级 不能 > 最大等级");
				}

				CommonActivityTime time  = CommonActivityTime.load(ServerStartDay, nowTime, row);

				BaseMailRewardData baseMailRewardData = BaseMailRewardData.loadData(row, false);

				DialyMailData data = new DialyMailData(time, minRoleLv, maxRoleLv, baseMailRewardData);

				mDialyMailDataManager.addData(data);

			} catch (Exception e) {
				throw new KGameServerException("加载" + sheetName + "错误：" + e.getMessage() + "，Row=" + row.getIndexInFile(), e);
			}
		}

		return mDialyMailDataManager;
	}

	public static String reloadPhyPowerRewardDatas() {
		try {
			PhyPowerDataManager mPhyPowerDataManager = loadPhyPowerRewardDatas();
			mPhyPowerDataManager.notifyCacheLoadComplete();

			KRewardDataManager.mPhyPowerDataManager = mPhyPowerDataManager;
			return null;
		} catch (Exception e) {
			return "加载" + SheetName_体力奖励 + "错误：" + e.getMessage();
		}
	}

	private static PhyPowerDataManager loadPhyPowerRewardDatas() throws KGameServerException, Exception {

		// 不填数据即没有体力奖励
		KGameExcelFile file = new KGameExcelFile(PhyPowerDataManager.url);
		KGameExcelTable table = file.getTable(SheetName_体力奖励, PhyPowerDataManager.headerIndex);
		KGameExcelRow[] rows = KGameUtilTool.filterRowsByGSID(table);

		PhyPowerDataManager mPhyPowerDataManager = new PhyPowerDataManager();

		List<PhyPowerRewardData> datas = ReflectPaser.parseExcelData(PhyPowerRewardData.class, table.getHeaderNames(), rows, true);
		mPhyPowerDataManager.initDatas(datas);

		return mPhyPowerDataManager;
	}
	
	public static String reloadKAJIRewardDatas() {
		try {
			KAJIRewardManager mKAJIRewardManager = loadKAJIRewardDatas();
			mKAJIRewardManager.notifyCacheLoadComplete();

			KRewardDataManager.mKAJIRewardManager = mKAJIRewardManager;
			return null;
		} catch (Exception e) {
			return "加载" + SheetName_卡机补偿 + "错误：" + e.getMessage();
		}
	}
	
	private static KAJIRewardManager loadKAJIRewardDatas() throws Exception {
		final long nowTime = System.currentTimeMillis();

		// 不填数据即没有卡机补偿
		KGameExcelFile file = new KGameExcelFile(KAJIRewardManager.url);
		KGameExcelTable table = file.getTable(SheetName_卡机补偿, KAJIRewardManager.headerIndex);
		KGameExcelRow[] rows = KGameUtilTool.filterRowsByGSID(table);

		KAJIRewardManager mKAJIRewardManager = new KAJIRewardManager();

		for (KGameExcelRow row : rows) {
			try {
				int minRoleLv = row.getInt("minRoleLv");
				if (minRoleLv < 0) {
					throw new Exception("minRoleLv=" + minRoleLv);
				}

				int maxRoleLv = row.getInt("maxRoleLv");
				if (maxRoleLv < 0) {
					throw new Exception("maxRoleLv=" + maxRoleLv);
				}

				//
				long createStartTime = UtilTool.DATE_FORMAT.parse(row.getData("createStartTime")).getTime();
				long createEndTime = nowTime;
				String createEndTimeStr = row.getData("createEndTime");
				if (!createEndTimeStr.equals("loadTime")) {
					createEndTime = UtilTool.DATE_FORMAT.parse(createEndTimeStr).getTime();
				}
				if (createEndTime <= createStartTime) {
					throw new Exception("角色创建结束时间<=起始时间");
				}
				//
				long effectEndTime = UtilTool.DATE_FORMAT.parse(row.getData("effectEndTime")).getTime();
				long effectStartTime = nowTime;
				String effectStartTimeStr = row.getData("effectStartTime");
				if (!effectStartTimeStr.equals("loadTime")) {
					effectStartTime = UtilTool.DATE_FORMAT.parse(effectStartTimeStr).getTime();
				}
				if (effectEndTime <= effectStartTime) {
					throw new Exception("补偿生效结束时间<=起始时间");
				}

				BaseMailRewardData baseMailRewardData = BaseMailRewardData.loadData(row, false);

				KAJIRewardData data = new KAJIRewardData(minRoleLv, maxRoleLv, createStartTime, createEndTime, effectStartTime, effectEndTime, baseMailRewardData);

				mKAJIRewardManager.addData(data);

			} catch (Exception e) {
				throw new KGameServerException("加载" + table.getTableName() + "错误：" + e.getMessage() + "，Row=" + row.getIndexInFile(), e);
			}
		}

		return mKAJIRewardManager;
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
			mDialyMailDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_每日邮件 + "]错误：" + e.getMessage(), e);
		}

		try {
			mFengceRoleUpLvRewardManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_封测角色升级奖励表 + "]错误：" + e.getMessage(), e);
		}

		try {
			mFengceFriendFubenRewardManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_封测好友副本奖励表 + "]错误：" + e.getMessage(), e);
		}

		try {
			mFengceCompetitionRewardManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_封测竞技场排名奖励表 + "]错误：" + e.getMessage(), e);
		}

		try {
			mShutdownRewardManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_维护奖励数据表 + "]错误：" + e.getMessage(), e);
		}

		try {
			mFengceSaveOnlineRoleManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_定时导出在线角色数据 + "]错误：" + e.getMessage(), e);
		}

		try {
			mPhyPowerDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_体力奖励 + "]错误：" + e.getMessage(), e);
		}
	}
}
