package com.kola.kmp.logic.reward.garden;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.reward.garden.KGardenDataManager.KGardenCommonTreeDataManager.GardenCommonRewardData;
import com.kola.kmp.logic.reward.garden.KGardenDataManager.KGardenTopTreeDataManager.GardenTopRewardData;
import com.kola.kmp.logic.reward.garden.KGardenDataManager.KTreeRipeTimeDataManager.TreeRipeTimeData;
import com.kola.kmp.logic.reward.garden.KGardenTaskManager.KGardenZombieRfreshDayTask;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;

/**
 * <pre>
 * 角色的奖励数据
 * 
 * @author CamusHuang
 * @creation 2014-2-13 下午4:53:22
 * </pre>
 */
public final class KRoleGarden extends RoleExtCABaseImpl {

	public static final Logger _LOGGER = KGameLogger.getLogger(KRoleGarden.class);

	// 读写锁
	public final ReentrantLock rwLock = new ReentrantLock();
	/** 今日的日期时间 */
	private long dataTime;
	//
	// <编号,植物数据>
	private LinkedHashMap<Integer, TreeData> treeMap = new LinkedHashMap<Integer, TreeData>();
	//
	// 新旧脚印，分别对应已读和未读
	private LinkedList<String> oldFeetLogs = new LinkedList<String>();
	private List<String> newFeetLogs = new ArrayList<String>();

	// vip存储数据
	private VIPSaveDataManager mVIPSaveDataManager = new VIPSaveDataManager();

	// =============加速相关
	private int speedTime;// 加速次数
	// <角色ID，角色CD结束时间（毫秒）> 不保存到数据库
	private Map<Long, Long> speedCDEndTime = new HashMap<Long, Long>();

	// =============僵尸相关
	private int killZomCount;// 杀僵尸的个数
	private long lastZomRefreshTime;// 僵尸最后刷新时间

	// ////////////////////////////////
	static final String JSON_VER = "0";// 版本
	static final String JSON_DAY = "Z";// 当前数据对应的日期
	//
	static final String JSON_BASE = "A";// 基础数据
	static final String JSON_BASE_SPEEDTIME = "1";// 加速次数
	static final String JSON_BASE_ZOME_KILLCOUNT = "2";// 杀僵尸的个数
	static final String JSON_BASE_ZOME_LAST_REFRESHTIME = "3";// 僵尸最后刷新时间

	static final String JSON_TREE = "B";// 植物数据
	// static final String JSON_TREE_RIPE = "1";// 成熟的时间
	static final String JSON_TREE_BINZOMBIE = "2";// 是否有僵尸
	// static final String JSON_TREE_ZOMBIETIME = "3";// 僵尸存在时长(毫秒)
	// static final String JSON_TREE_ZOMBIESTARTTIME = "4";// 僵尸开始时间
	static final String JSON_TREE_STARTTIME = "5";// 植物上次结算成熟剩余时间的时刻
	static final String JSON_TREE_RELEASETIME = "6";// 植物成熟剩余时间

	static final String JSON_OLDFEET = "C";// 旧脚印
	static final String JSON_NEWFEET = "D";// 新脚印

	static final String JSON_VIPSAVE = "E";// VIP存储数据
	static final String JSON_VIPSAVE_TIME = "1";// VIP存储时间
	static final String JSON_VIPSAVE_MONEYTYPE = "2";// 货币类型
	static final String JSON_VIPSAVE_MONEYCOUNT = "3";// 货币数量
	static final String JSON_VIPSAVE_ITEMCODE = "4";// 道具模板ID
	static final String JSON_VIPSAVE_ITEMCOUNT = "5";// 道具数量

	/**
	 * <pre>
	 * 逻辑新建对象时使用
	 * 
	 * @param dbdata
	 * @author CamusHuang
	 * @creation 2014-2-13 下午5:04:25
	 * </pre>
	 */
	protected KRoleGarden(long _roleId, int _type, boolean isFirstNew) {
		super(_roleId, _type);
		if (isFirstNew) {
			dataTime = System.currentTimeMillis();
			lastZomRefreshTime = dataTime;
		}
		//
		for (GardenCommonRewardData data : KGardenDataManager.mGardenCommonTreeDataManager.getDataCache().values()) {
			TreeData tree = new TreeData(data.type, isFirstNew, true);
			treeMap.put(tree.type, tree);
		}

		// <类型,<角色等级,数据>>
		for (Map<Integer, GardenTopRewardData> tempMap : KGardenDataManager.mGardenTopTreeDataManager.getDataCache().values()) {
			GardenTopRewardData data = tempMap.get(1);
			TreeData tree = new TreeData(data.type, isFirstNew, false);
			treeMap.put(tree.type, tree);
		}
	}

	@Override
	protected void decode(String jsonCA) {
		// 由底层调用,解释出逻辑层数据
		try {
			JSONObject json = new JSONObject(jsonCA);
			int ver = json.getInt(JSON_VER);// 默认版本
			dataTime = json.getLong(JSON_DAY) * Timer.ONE_HOUR;
			// CEND 暂时只有版本0
			switch (ver) {
			case 0: {
				{
					JSONObject temp = json.getJSONObject(JSON_BASE);
					speedTime = temp.getInt(JSON_BASE_SPEEDTIME);
					killZomCount = temp.getInt(JSON_BASE_ZOME_KILLCOUNT);
					lastZomRefreshTime = temp.getLong(JSON_BASE_ZOME_LAST_REFRESHTIME) * Timer.ONE_SECOND + dataTime;
				}

				{
					JSONObject temp = json.getJSONObject(JSON_TREE);
					long nowTime = System.currentTimeMillis();
					for (Iterator<String> it = temp.keys(); it.hasNext();) {
						String key = it.next();
						JSONObject temp2 = temp.getJSONObject(key);
						TreeData tree = treeMap.get(Integer.parseInt(key));
						{
							// tree.ripeTime = temp2.getInt(JSON_TREE_RIPE) *
							// Timer.ONE_MINUTE;
							// tree.zombieTime =
							// temp2.getInt(JSON_TREE_ZOMBIETIME) *
							// Timer.ONE_MINUTE;
							tree.isBinZombie = temp2.getBoolean(JSON_TREE_BINZOMBIE);
							// long tempTime =
							// temp2.optLong(JSON_TREE_ZOMBIESTARTTIME);
							// if (tempTime < 1) {
							// tree.zombieStartTime = nowTime;
							// } else {
							// tree.zombieStartTime = tempTime *
							// Timer.ONE_MINUTE;
							// }
							tree.treeStartTime = temp2.optLong(JSON_TREE_STARTTIME);
							if (tree.treeStartTime < 1) {
								tree.treeStartTime = nowTime;
							} else {
								tree.treeStartTime *= Timer.ONE_MINUTE;
							}
							tree.ripeReleaseTime = temp2.optLong(JSON_TREE_RELEASETIME) * Timer.ONE_MINUTE;
						}
					}
				}
				//
				{
					JSONArray temp = json.optJSONArray(JSON_OLDFEET);
					if (temp != null) {
						int len = temp.length();
						for (int i = 0; i < len; i++) {
							oldFeetLogs.add(temp.getString(i));
						}
					}
				}
				//
				{
					JSONArray temp = json.optJSONArray(JSON_NEWFEET);
					if (temp != null) {
						int len = temp.length();
						for (int i = 0; i < len; i++) {
							newFeetLogs.add(temp.getString(i));
						}
					}
				}

				mVIPSaveDataManager.decode(dataTime, json.optJSONArray(JSON_VIPSAVE));
			}

				break;
			}

		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + getRoleId() + " ----丢失数据，存在运行隐患！", ex);
		}
	}

	@Override
	protected String encode() {
		boolean isLock = false;
		try {
			isLock = rwLock.tryLock(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			_LOGGER.error(e.getMessage(), e);
		}
		if (!isLock) {
			return null;
		}
		try {
			JSONObject json = new JSONObject();
			json.put(JSON_VER, 0);
			json.put(JSON_DAY, dataTime / Timer.ONE_HOUR);
			{
				JSONObject temp = new JSONObject();
				json.put(JSON_BASE, temp);
				temp.put(JSON_BASE_SPEEDTIME, speedTime);
				temp.put(JSON_BASE_ZOME_KILLCOUNT, killZomCount);
				temp.put(JSON_BASE_ZOME_LAST_REFRESHTIME, (lastZomRefreshTime - (dataTime / Timer.ONE_HOUR * Timer.ONE_HOUR)) / Timer.ONE_SECOND);
			}

			{
				JSONObject temp = new JSONObject();
				json.put(JSON_TREE, temp);
				for (TreeData tree : treeMap.values()) {
					JSONObject temp2 = new JSONObject();
					temp.put(tree.type + "", temp2);
					{
						// temp2.put(JSON_TREE_RIPE, tree.ripeTime /
						// Timer.ONE_MINUTE);
						// temp2.put(JSON_TREE_ZOMBIETIME, tree.zombieTime /
						// Timer.ONE_MINUTE);
						temp2.put(JSON_TREE_BINZOMBIE, tree.isBinZombie);
						// temp2.put(JSON_TREE_ZOMBIESTARTTIME,
						// tree.zombieStartTime / Timer.ONE_MINUTE);
						temp2.put(JSON_TREE_STARTTIME, tree.treeStartTime / Timer.ONE_MINUTE);
						temp2.put(JSON_TREE_RELEASETIME, tree.ripeReleaseTime / Timer.ONE_MINUTE);
					}
				}
			}
			//
			{
				if (!oldFeetLogs.isEmpty()) {
					JSONArray temp = new JSONArray();
					json.put(JSON_OLDFEET, temp);
					for (String log : oldFeetLogs) {
						temp.put(log);
					}
				}
			}
			//
			{
				if (!newFeetLogs.isEmpty()) {
					JSONArray temp = new JSONArray();
					json.put(JSON_NEWFEET, temp);
					for (String log : newFeetLogs) {
						temp.put(log);
					}
				}
			}

			json.put(JSON_VIPSAVE, mVIPSaveDataManager.encode(dataTime / Timer.ONE_DAY * Timer.ONE_DAY));

			return json.toString();
		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + getRoleId() + " ----丢失数据，存在运行隐患！", ex);
			return null;
		} finally {
			rwLock.unlock();
		}
	}

	public void notifyUpdate() {
		super.notifyUpdate();
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 
	 * @param newDayStartTime
	 * @author CamusHuang
	 * @creation 2014-4-10 上午10:25:35
	 * </pre>
	 */
	boolean notifyForLogin(long nowTime) {
		rwLock.lock();
		try {
			if (UtilTool.isBetweenDay(dataTime, nowTime)) {
				return notifyForDayChange(nowTime);
			}
			return false;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 
	 * @param newDayStartTime
	 * @author CamusHuang
	 * @creation 2014-4-10 上午10:25:35
	 * </pre>
	 */
	boolean notifyForDayChange(long nowTime) {
		rwLock.lock();
		try {
			dataTime = nowTime;
			if (speedTime > 0 || killZomCount > 0) {
				speedTime = 0;
				speedCDEndTime.clear();
				killZomCount = 0;
				notifyUpdate();
				return true;
			}
			return false;
		} finally {
			rwLock.unlock();
		}
	}

	public int getSpeedTime() {
		return speedTime;
	}

	public void increaseSpeedTime() {
		rwLock.lock();
		try {
			speedTime++;
			notifyUpdate();
		} finally {
			rwLock.unlock();
		}
	}

	public int getReleaseSpeedTime() {
		rwLock.lock();
		try {
			int time = KGardenDataManager.SpeedTimePerDay - speedTime;
			return Math.max(0, time);
		} finally {
			rwLock.unlock();
		}
	}

	public void recordSpeedCDEndTime(long roleId, long cdEndTime) {
		speedCDEndTime.put(roleId, cdEndTime);
	}

	public long getSpeedCDEndTime(long roleId) {
		Long time = speedCDEndTime.get(roleId);
		if (time == null) {
			return 0;
		}
		return time;
	}

	public long getSpeedCDReleaseTime(long roleId) {
		long nowTime = System.currentTimeMillis();
		Long time = speedCDEndTime.get(roleId);
		if (time == null || time <= nowTime) {
			return 0;
		}
		return time - nowTime;
	}

	/**
	 * <pre>
	 * 找出可以被浇灌的植物
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-8-28 下午2:58:48
	 * </pre>
	 */
	List<TreeData> searchCouldSpeedTrees(boolean isSenstiveForZombie) {
		rwLock.lock();
		try {
			// 找出可浇灌的植物
			List<TreeData> speedTrees = new ArrayList<TreeData>();

			// 只遍历低级植物
			LinkedHashMap<Integer, GardenCommonRewardData> dataMap = KGardenDataManager.mGardenCommonTreeDataManager.getDataCache();
			for (GardenCommonRewardData commonReward : dataMap.values()) {
				// 普通植物
				TreeData tree = treeMap.get(commonReward.type);
				tree.countReleaseTimeIn();

				int releaseTime = tree.getReleaseTime();
				if (releaseTime < 1) {
					// 成熟
					continue;
				}

				if (isSenstiveForZombie && tree.isBinZombie()) {
					// 有僵尸，不能浇灌
					continue;
				}

				TreeRipeTimeData data = KGardenDataManager.mTreeRipeTimeDataManager.getData(commonReward.type);
				if (data.speedTime < 1) {
					// 不能浇灌
					continue;
				}
				// 可浇灌
				speedTrees.add(tree);
			}

			return speedTrees;
		} finally {
			rwLock.unlock();
		}
	}

	public TreeData getTreeData(int type) {
		rwLock.lock();
		try {
			return treeMap.get(type);
		} finally {
			rwLock.unlock();
		}
	}

	public Collection<TreeData> getTreeDataCache() {
		rwLock.lock();
		try {
			return treeMap.values();
		} finally {
			rwLock.unlock();
		}
	}

	public void addFeet(String feet) {
		rwLock.lock();
		try {
			// 如果与上次的人是同一个，则忽略
			{
				String lastFeet = null;
				if (!newFeetLogs.isEmpty()) {
					lastFeet = newFeetLogs.get(newFeetLogs.size() - 1);
				} else if (!oldFeetLogs.isEmpty()) {
					lastFeet = oldFeetLogs.get(oldFeetLogs.size() - 1);
				}
				if (lastFeet != null && lastFeet.equals(feet)) {
					return;
				}
			}
			newFeetLogs.add(feet);
			notifyUpdate();
		} finally {
			rwLock.unlock();
		}
	}

	public List<String> getOldFeetLogCache() {
		return oldFeetLogs;
	}

	public List<String> getAndClearNewFeetLogs() {
		rwLock.lock();
		try {
			if (newFeetLogs.isEmpty()) {
				return Collections.emptyList();
			}
			List<String> temp = new ArrayList<String>(newFeetLogs);
			oldFeetLogs.addAll(newFeetLogs);
			while (oldFeetLogs.size() > KGardenDataManager.FeetLogMaxCount) {
				oldFeetLogs.removeFirst();
			}
			newFeetLogs.clear();
			notifyUpdate();
			return temp;
		} finally {
			rwLock.unlock();
		}
	}

	public VIPSaveDataManager getVIPSaveDataManager() {
		return mVIPSaveDataManager;
	}

	public void increaseKillZomCount() {
		rwLock.lock();
		try {
			killZomCount++;
			notifyUpdate();
		} finally {
			rwLock.unlock();
		}
	}

	public int getKillZomCount() {
		return killZomCount;
	}

	void notifyForZombieRefresh(List<TreeData> synDatas, long nowTime) {
		// 进行僵尸数据刷新
		rwLock.lock();
		try {
			// 更新僵尸最后刷新时间
			lastZomRefreshTime = nowTime;
			//
			for (GardenCommonRewardData data : KGardenDataManager.mGardenCommonTreeDataManager.getDataCache().values()) {
				TreeData tree = treeMap.get(data.type);
				if (tree.isBinZombie()) {
					continue;
				}
				boolean isBin = UtilTool.random(1, 10000) <= data.Corpsechance;
				if (isBin) {
					tree.setBinZombie(true);
					if (synDatas != null) {
						synDatas.add(tree);
					}
				}
			}
		} finally {
			rwLock.unlock();
		}
	}

	boolean isContainZombies() {
		rwLock.lock();
		try {
			for (TreeData tree : treeMap.values()) {
				if (tree.isBinZombie()) {
					return true;
				}
			}
			return false;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 一。【模拟刷新僵尸】
	 * 由于时效任务只针对在线玩家刷新僵尸，玩家离线后则被时效任务忽略
	 * 因此需要补充【模拟刷新僵尸】
	 * 玩家离线后：一但庄园被访问、玩家重新上线，则执行【模拟刷新僵尸】
	 * 刷新规则：按上次僵尸记录时间到当前为止，时效任务一共执行了N次刷新，则循环执行N次【模拟刷新僵尸】
	 * 与VIP无关、不需要同步数据给玩家
	 * 
	 * 二。VIP【自动收获】
	 * 在线时由玩家手工收获，离线后如果是VIP玩家则自动收获
	 * 玩家离线后：一但庄园被访问、玩家重新上线，则执行【自动收获】
	 * 【自动收获】规则：按植物开始种植到当前为止，根据植物成熟周期，一共有N次成熟时间点，则循环执行N次【自动收获】
	 * 与VIP有关、不需要同步数据给玩家
	 * 
	 * 三。以上两种规则，必须在同一个时间线中交错执行才能保证僵尸时效对产量的正确影响
	 * 
	 * 四。具体算法逻辑
	 * 按两种规则，找到相应的时间点（僵尸刷新和各植物成熟期），将时间点按从小到大排序，按时间点执行相应的逻辑
	 * 
	 * @param roleLv
	 * @author CamusHuang
	 * @creation 2014-5-1 下午4:51:24
	 * </pre>
	 */
	public void autoRefreshZombieAndCollectForVIP(KRole role, long nowTime) {
		rwLock.lock();
		try {
			VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(getRoleId());
			long vipSaveTime = vipData.gardensavetimeInMills;

			// 找出最后僵尸刷新时间到目前的所有僵尸刷新时间点
			List<Long> timeList = new ArrayList<Long>();
			{
				long lastTime = lastZomRefreshTime;
				KGardenZombieRfreshDayTask.instance.countRefreshTimes(lastTime, nowTime, timeList);
			}

			if (timeList.isEmpty()) {
				// 将在此时间点前已经成熟的植物自动收获
				if (vipSaveTime > 0) {
					// 结算植物成熟时间
					countReleaseTimeIn2(nowTime);
					// 自动收获
					simulateCollectForVIP(role, nowTime);
				}

			} else {
				// 按时间点执行
				for (Long refreshTime : timeList) {
					// 结算植物成熟时间
					countReleaseTimeIn2(refreshTime);

					// 将在此时间点前已经成熟的植物自动收获
					if (vipSaveTime > 0) {
						// 自动收获
						simulateCollectForVIP(role, refreshTime);
					}

					// 僵尸刷新
					notifyForZombieRefresh(null, refreshTime);
				}
			}

			// 清理溢出的VIP存储数据
			mVIPSaveDataManager.clearOutTimeDatas(nowTime - vipSaveTime);
		} finally {
			rwLock.unlock();
		}
	}

	private void simulateCollectForVIP(KRole role, long nowTime) {
		int roleLv = role.getLevel();
		// 自动收获
		for (TreeData tree : treeMap.values()) {
			if (tree.ripeReleaseTime > Timer.ONE_SECOND) {
				continue;// 未成熟
			}

			VIPSaveData data = null;
			if (tree.type < KGardenDataManager.TYPE_TOP_MIN) {
				// 普通奖励
				GardenCommonRewardData commonReward = KGardenDataManager.mGardenCommonTreeDataManager.getData(tree.type);
				//
				long resultMoney = KGardenCenter.ExpressionForCommonReward(roleLv, commonReward.addMoney);
				data = new VIPSaveData(nowTime, new KCurrencyCountStruct(commonReward.addMoney.currencyType, resultMoney));
			} else {
				// 特殊奖励
				GardenTopRewardData topReward = KGardenDataManager.mGardenTopTreeDataManager.getData(tree.type, roleLv);
				data = new VIPSaveData(nowTime, ItemCountStruct.randomItem(topReward.addItems, topReward.addItemRates, topReward.allRate));
			}

			// 加入VIP存储
			mVIPSaveDataManager.addData(data);
			// 收获后重生
			tree.rebornIn(tree.isBinZombie, nowTime);
//			// 通知活跃度
//			KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.收获庄园植物);
		}
	}

	/**
	 * <pre>
	 * 结算植物成熟时间
	 * 
	 * @param nowTime
	 * @author CamusHuang
	 * @creation 2014-8-27 下午3:50:16
	 * </pre>
	 */
	private void countReleaseTimeIn2(long nowTime) {
		for (TreeData tree : treeMap.values()) {
			tree.countReleaseTimeIn2(nowTime);
		}
	}

	// /**
	// * <pre>
	// * 找出各编号的植物所有成熟时间点
	// * 前提：VIP用户
	// *
	// * @param timeList
	// * @author CamusHuang
	// * @creation 2014-5-2 下午12:17:47
	// * </pre>
	// */
	// private void countRipeTimesForVip(List<AutoData> timeList, long nowTime)
	// {
	// for (TreeData tree : treeMap.values()) {
	// long startTime = tree.ripeTime;
	// long periodTime =
	// KGardenDataManager.mTreeRipeTimeDataManager.getData(tree.type).ripeTime;
	// while (startTime < nowTime) {
	// timeList.add(new AutoData(startTime, false));
	// startTime += periodTime;
	// }
	// }
	// }

	CommonResult speedByGM(int treeId, int minute) {
		CommonResult result = new CommonResult();
		rwLock.lock();
		try {

			for (TreeData tree : treeMap.values()) {
				int releaseTime = tree.getReleaseTime();
				if (releaseTime < 1) {
					continue;
				}

				if (tree.type == treeId || treeId == 0) {
					tree.speedRipeByGM(minute * Timer.ONE_MINUTE);
				}
			}

			result.isSucess = true;
			result.tips = RewardTips.成功浇灌;
			return result;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 每一笔VIP自动收获和存储的数据
	 * 货币和物品不会同时存在
	 * 
	 * @author CamusHuang
	 * @creation 2014-4-30 下午3:37:39
	 * </pre>
	 */
	public class VIPSaveData {
		private final long saveTime;
		public final KCurrencyCountStruct addMoney;
		public final ItemCountStruct addItem;

		private VIPSaveData(long saveTime, KCurrencyCountStruct addMoney) {
			this.saveTime = saveTime;
			this.addMoney = addMoney;
			this.addItem = null;
		}

		private VIPSaveData(long saveTime, ItemCountStruct addItem) {
			this.saveTime = saveTime;
			this.addItem = addItem;
			this.addMoney = null;
		}
	}

	public class VIPSaveDataManager {
		private List<VIPSaveData> datas = new ArrayList<KRoleGarden.VIPSaveData>();

		JSONArray encode(long dateTime) throws JSONException {
			JSONArray array = new JSONArray();
			for (VIPSaveData data : datas) {
				JSONObject json = new JSONObject();
				array.put(json);
				json.put(JSON_VIPSAVE_TIME, (data.saveTime - dateTime) / Timer.ONE_SECOND);
				if (data.addMoney != null) {
					json.put(JSON_VIPSAVE_MONEYTYPE, data.addMoney.currencyType.sign);
					json.put(JSON_VIPSAVE_MONEYCOUNT, data.addMoney.currencyCount);
				} else {
					json.put(JSON_VIPSAVE_ITEMCODE, data.addItem.itemCode);
					json.put(JSON_VIPSAVE_ITEMCOUNT, data.addItem.itemCount);
				}
			}
			return array;
		}

		void decode(long dateTime, JSONArray array) throws JSONException {
			if (array == null) {
				return;
			}
			int len = array.length();
			for (int i = 0; i < len; i++) {
				JSONObject json = array.getJSONObject(i);
				long saveTime = json.getLong(JSON_VIPSAVE_TIME) * Timer.ONE_SECOND + dateTime;
				KCurrencyCountStruct addMoney = null;
				ItemCountStruct addItem = null;
				if (json.isNull(JSON_VIPSAVE_ITEMCODE)) {
					addMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.getEnum(json.getInt(JSON_VIPSAVE_MONEYTYPE)), json.getLong(JSON_VIPSAVE_MONEYCOUNT));
				} else {
					addItem = new ItemCountStruct(json.getString(JSON_VIPSAVE_ITEMCODE), json.getLong(JSON_VIPSAVE_ITEMCOUNT));
				}
				if (addMoney != null && addMoney.currencyType != null) {
					datas.add(new VIPSaveData(saveTime, addMoney));
				} else if (addItem != null && addItem.getItemTemplate() != null) {
					datas.add(new VIPSaveData(saveTime, addItem));
				}
			}
		}

		void addData(VIPSaveData data) {
			this.datas.add(data);
			notifyUpdate();
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 直接获取缓存，请谨慎使用
		 * @return
		 * @author CamusHuang
		 * @creation 2014-4-30 下午5:23:55
		 * </pre>
		 */
		List<VIPSaveData> getVIPSaveDataCache() {
			return mVIPSaveDataManager.datas;
		}

		public boolean isContainVipSaveData() {
			return !mVIPSaveDataManager.datas.isEmpty();
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @param startTime
		 * @return 是否清理了某些数据
		 * @author CamusHuang
		 * @creation 2014-4-30 下午3:53:26
		 * </pre>
		 */
		void clearOutTimeDatas(long startTime) {
			if (datas.isEmpty()) {
				return;
			}
			//
			boolean isClear = false;
			VIPSaveData data = null;
			for (Iterator<VIPSaveData> it = datas.iterator(); it.hasNext();) {
				data = it.next();
				if (data.saveTime < startTime) {
					it.remove();
					isClear = true;
				}
			}
			if (isClear) {
				notifyUpdate();
			}
		}
	}

	public class TreeData {
		public final int type;
		private long treeStartTime;// 植物上次结算成熟剩余时间的时刻
		private long ripeReleaseTime;// 植物成熟剩余时间
		private boolean isBinZombie;// 是否有僵尸

		TreeData(int type, boolean isFirstNew, boolean isBinZombie) {
			this.type = type;
			this.rebornIn(isBinZombie, System.currentTimeMillis());
			if (isFirstNew) {
				notifyUpdate();
			}
		}

		void reborn() {
			rebornIn(isBinZombie, System.currentTimeMillis());
			notifyUpdate();
		}

		private void rebornIn(boolean isBinZombie, long nowTime) {
			rwLock.lock();
			try {
				this.treeStartTime = nowTime;
				TreeRipeTimeData data = KGardenDataManager.mTreeRipeTimeDataManager.getData(type);
				this.ripeReleaseTime = data.ripeTime;
				//
				this.isBinZombie = isBinZombie;
			} finally {
				rwLock.unlock();
			}
		}

		/**
		 * <pre>
		 * 剩余多少秒可成熟
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2014-4-29 下午4:21:16
		 * </pre>
		 */
		public int getReleaseTime() {
			// 结算剩余时长
			countReleaseTimeIn();
			return (int) (ripeReleaseTime / Timer.ONE_SECOND);
		}

		/**
		 * <pre>
		 * 结算剩余时长
		 * 
		 * @author CamusHuang
		 * @creation 2014-8-27 下午3:11:14
		 * </pre>
		 */
		void countReleaseTimeIn() {
			long nowTime = System.currentTimeMillis();
			countReleaseTimeIn2(nowTime);
		}

		private void countReleaseTimeIn2(long nowTime) {
			if (!isBinZombie) {
				ripeReleaseTime -= (nowTime - treeStartTime);
				ripeReleaseTime = Math.max(0, ripeReleaseTime);
				notifyUpdate();
			}
			this.treeStartTime = nowTime;
		}

		void speedRipe(long decreaseTime) {
			rwLock.lock();
			try {
				// 结算剩余时长
				countReleaseTimeIn();

				if (isBinZombie) {
					return;
				} else {
					// 没有僵尸，直接加速
					long nowTime = System.currentTimeMillis();
					ripeReleaseTime -= decreaseTime;
					ripeReleaseTime = Math.max(0, ripeReleaseTime);
					treeStartTime = nowTime;
					notifyUpdate();
				}
			} finally {
				rwLock.unlock();
			}
		}

		void speedRipeByGM(long decreaseTime) {
			rwLock.lock();
			try {
				// 不管僵尸，直接加速
				long nowTime = System.currentTimeMillis();
				ripeReleaseTime -= decreaseTime;
				ripeReleaseTime = Math.max(0, ripeReleaseTime);
				treeStartTime = nowTime;
				if (ripeReleaseTime < Timer.ONE_MINUTE) {
					if(KGardenDataManager.mGardenCommonTreeDataManager.getData(type)!=null){
						isBinZombie = true;
					}
				}
				notifyUpdate();
			} finally {
				rwLock.unlock();
			}
		}

		void setBinZombie(boolean isBinZombieNew) {
			rwLock.lock();
			try {
				if (isBinZombie == isBinZombieNew) {
					return;
				}
				if (isBinZombie) {
					// 结算剩余时长
					countReleaseTimeIn();
				}
				isBinZombie = isBinZombieNew;
				notifyUpdate();
			} finally {
				rwLock.unlock();
			}
		}

		/**
		 * <pre>
		 * 是否有僵尸
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2014-4-29 下午4:21:27
		 * </pre>
		 */
		public boolean isBinZombie() {
			return isBinZombie;
		}
	}
}
