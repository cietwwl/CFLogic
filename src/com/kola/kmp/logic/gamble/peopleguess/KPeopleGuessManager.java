package com.kola.kmp.logic.gamble.peopleguess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import jxl.read.biff.BiffException;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.XmlUtil;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.gamble.peopleguess.KPeopleGuessManager.PlayerRoleGuessData;
import com.kola.kmp.logic.gamble.peopleguess.KPeopleGuessManager.RaceHorseData;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMap;
import com.kola.kmp.logic.mission.guide.KGuideManager;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.other.KGameExtDataDBTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.CamusGlobalDataAbs;
import com.kola.kmp.logic.util.KXmlWriter;
import com.kola.kmp.logic.util.tips.PeopleGuessTips;
import com.kola.kmp.protocol.activity.KActivityProtocol;

/**
 * 全民竞猜 活动控制器 主要控制活动进行各个状态
 * 
 * @author Alex
 * @create 2015年2月28日 下午4:09:53
 */
public class KPeopleGuessManager extends CamusGlobalDataAbs implements KGameTimerTask {

	private final static Logger Log = KGameLogger.getLogger("peopleGuessLogger");

	private volatile boolean shutDownProcessing = false;

	private ReentrantLock lock = new ReentrantLock();
	
	private final static String oldDataSaveDir = "./res/output/peopleGuess/";

	private final static String oldDataSavePath = oldDataSaveDir + "peopleGuessSaveData.xml";
	
	private static AtomicLong raceId;

	// 准备开跑前在场等待时间
	public static int prepare_live_time = 60;
	// 开赛直播时长(单位：s)
	public static int live_race_time_seconds = 0;
	// 开奖时长
	public static int settle_price_time_seconds;

	public static int DELAY_TIME = 2;

	public static int[] raceTrackArea;

	// 本次赛马总投注金额
	public final static AtomicLong thisGameTotalGuess = new AtomicLong(0);

	// 本次赛马参赛马匹列表：key：参赛号码
	public static final Map<Integer, RaceHorseData> racingHorseDataMap = new LinkedHashMap<Integer, RaceHorseData>();

	// 当前角色投注数据
	public static final Map<Long, PlayerRoleGuessData> roleGuessDatas = new ConcurrentHashMap<Long, PlayerRoleGuessData>();
	// 赛马直播数据
	public static final List<HorseLiveRaceData> horseLiveRaceDataList = new ArrayList<HorseLiveRaceData>();

	// 上一次比赛冠军号
	public int lastRaceChampionID = 1;

	private boolean racing = false;

	public boolean isOpen = true;

	private int leftTime;

	private long pre_race_time;
	// 开赛直播时间点
	private long race_time;
	// 活动状态
	public KPeopleGuessStatusEnum status;

	private int horseRaceSize;

	/** 活动地图id */
	private int activityMapId;

	/** 活动副本地图 */
	private KDuplicateMap activityMap;

	private int winner_fastest_race_time_seconds;

	// 上一场系统保留金额
	private AtomicLong systemGainLastRecord = new AtomicLong(0);

	// 所有参赛备选马匹数据结构列表
	public static Map<Integer, HorseData> horseDataStructMap = new LinkedHashMap<Integer, HorseData>();

	/** 当天投注次数 */
	public static AtomicInteger currentDayVoteCount = new AtomicInteger(0);
	// 当天角色投注数据
	public static final Map<Long, PlayerRoleGuessData> currentDayRoleGuessDatas = new ConcurrentHashMap<Long, PlayerRoleGuessData>();

	// 投注押宝的钻石额度列表
	public static List<Integer> yabaoMenuList = new ArrayList<Integer>();
	
	private static boolean DATAINITFROMDB = false;

	
	private final static String KEY_RACE_ID = "1";
	
	private final static String KEY_SYSTEMGAINLASTRECORD = "2";
	
	private final static String KEY_STATUS = "3";
	
	private final static String KEY_THISGAMETOTALGAIN = "4";
	
	private final static String KEY_RACINGHORSEDATAMAP = "5";
	
	private final static String KEY_ROLEGUESSDATAS = "6";
	
	private final static String KEY_LASTCHAMPOINID = "7";
	
	private final static String KEY_DAYVOTECOUNT = "8";
	
	private final static String KEY_DAYROLEGUESSDATAS = "9";
	
	public KPeopleGuessManager() {
		super(KGameExtDataDBTypeEnum.全民竞猜数据);
	}

	@Override
	public String getName() {
		return "KPeopleGuessManager";
	}

	@Override
	public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
		if (shutDownProcessing) {
			return "ok";
		}
		if (status == KPeopleGuessStatusEnum.STATUS_YABAO) {
			leftTime -= DELAY_TIME;
			if (leftTime > 0) {
				timeSignal.getTimer().newTimeSignal(this, DELAY_TIME, TimeUnit.SECONDS);
			} else {
				startPreRace();
			}
		} else if (status == KPeopleGuessStatusEnum.STATUS_WAITING_RACE) {
			leftTime -= DELAY_TIME;
			if (leftTime > 0) {
				timeSignal.getTimer().newTimeSignal(this, DELAY_TIME, TimeUnit.SECONDS);
			} else {
				startRace();
			}
		} else if (status == KPeopleGuessStatusEnum.STATUS_RACING) {
			leftTime -= DELAY_TIME;
			if (leftTime > 0) {
				timeSignal.getTimer().newTimeSignal(this, DELAY_TIME, TimeUnit.SECONDS);
			} else {
				horesRaceEnd();
			}
		} else if (status == KPeopleGuessStatusEnum.STATUS_SETTLE_PRICE) {
			leftTime -= DELAY_TIME;
			if (leftTime > 0) {
				timeSignal.getTimer().newTimeSignal(this, DELAY_TIME, TimeUnit.SECONDS);
			} else {
				horseRaceSettlePrice();
			}
		}

		return "ok";
	}

	@Override
	public void done(KGameTimeSignal timeSignal) {

	}

	@Override
	public void rejected(RejectedExecutionException e) {

	}

	public void init(String str) throws Exception {

		raceId = new AtomicLong(0);
		Document doc = XmlUtil.openXml(str);

		Element root = doc.getRootElement();

		raceTrackArea = new int[4];
		raceTrackArea[0] = Integer.parseInt(root.getChild("activityCfg").getChild("raceTrackArea").getAttributeValue("x"));
		raceTrackArea[1] = Integer.parseInt(root.getChild("activityCfg").getChild("raceTrackArea").getAttributeValue("y"));
		raceTrackArea[2] = Integer.parseInt(root.getChild("activityCfg").getChild("raceTrackArea").getAttributeValue("w"));
		raceTrackArea[3] = Integer.parseInt(root.getChild("activityCfg").getChild("raceTrackArea").getAttributeValue("h"));

		activityMapId = Integer.parseInt(root.getChild("activityCfg").getChildText("race_map_id"));
		horseRaceSize = Integer.parseInt(root.getChild("activityCfg").getChildText("race_horse_size"));
		prepare_live_time = Integer.parseInt(root.getChild("activityCfg").getChildText("pre_live_race_time_seconds"));
		settle_price_time_seconds = Integer.parseInt(root.getChild("activityCfg").getChildText("settle_price_time_seconds"));
		// 创建活动副本地图
		activityMap = KSupportFactory.getDuplicateMapSupport().createDuplicateMap(activityMapId);

		winner_fastest_race_time_seconds = Integer.parseInt(root.getChild("activityCfg").getChildText("winner_fastest_race_time_seconds"));
		Integer.parseInt(root.getChild("activityCfg").getChildText("loser_slowest_race_time_seconds"));

		Element yabaoIgotE = root.getChild("activityCfg").getChild("yabaoIgot");
		for (Element igotE : (List<Element>) yabaoIgotE.getChildren("igot")) {
			int igot = Integer.parseInt(igotE.getText());
			yabaoMenuList.add(igot);
		}

		this.isOpen = root.getChildText("isOpen").equals("true");

		String timeStr = root.getChildText("racePerion");

		String xlsPath = root.getChildText("cfgPath");
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(xlsPath);
		} catch (BiffException e) {
			throw new KGameServerException("读取全民竞猜excel表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取全民竞猜excel表头发生错误！", e);
		}
		if (xlsFile != null) {
			// 初始化剧本数据
			int horseDataRowIndex = 2;
			KGameExcelTable horseDataTable = xlsFile.getTable("赛马配置信息", horseDataRowIndex);
			KGameExcelRow[] allHorseDataRows = horseDataTable.getAllDataRows();

			if (allHorseDataRows != null) {
				for (int i = 0; i < allHorseDataRows.length; i++) {
					int horseId = allHorseDataRows[i].getInt("horseId");
					String horseName = allHorseDataRows[i].getData("horseName");
					int horseResId = allHorseDataRows[i].getInt("horseResId");

					HorseData data = new HorseData(horseId, horseName, horseResId);
					horseDataStructMap.put(horseId, data);
				}
			}
		}

		//将加载旧数据的方式改为从数据库加载
		load();
		
		//如果数据库里没有数据  检查一下是否有文件数据
		if(!DATAINITFROMDB){
			checkAndReloadPeopleGuessData();
		}
		
		// 在初始化新比赛的时候 应该先检查是否有旧数据
		if (racingHorseDataMap.isEmpty()) {
			initNewRace();
		}

	}
	
	
	private boolean loadPeopleGuessData(String path) throws Exception{
		
		boolean isReload = false;
		

		Document doc = XmlUtil.openXml(path);
		if (doc != null) {
			Log.info("---------------------------------开始加载保存数据-----------------------------------------------------");
			Element root = doc.getRootElement();
			if (raceId == null) {
				raceId = new AtomicLong(1);
			}

			raceId.set(Long.parseLong(root.getChildText("raceId")));
			if (root.getChildText("systemGain") != null && !root.getChildText("systemGain").equals("")) {
				
				//检查是否为0  如果不为0  表示有初始化过 应该将总额叠加
				if(systemGainLastRecord.get() == 0){
					systemGainLastRecord.set(Long.parseLong(root.getChildText("systemGain")));
					Log.info("上次【全民竞猜】系统保留{}钻石", systemGainLastRecord.get());
				}else{
					long parseLong = Long.parseLong(root.getChildText("systemGain"));
					Log.info("发现存在其他服全民竞猜数据，当前系统保留{}钻石,增加其他服竞猜保留{}钻石", systemGainLastRecord.get(),parseLong);
					systemGainLastRecord.addAndGet(parseLong);
				}
				
			} else {
				systemGainLastRecord.set(0);
			}

			byte raceStatus = Byte.parseByte(root.getChildText("status"));
			if (KPeopleGuessStatusEnum.getEnum(raceStatus) != KPeopleGuessStatusEnum.STATUS_SETTLE_PRICE) {
				//Log.info("上次【全民竞猜】保存的状态为：{}" , KPeopleGuessStatusEnum.getEnum(raceStatus).statusName);

				isReload = true;
				
				long lastTotal = Long.parseLong(root.getChildText("thisGameTotalYaobao"));
				if(thisGameTotalGuess.get() == 0){
					Log.info("加载上次【全民竞猜】总投注{}钻石", lastTotal);
					thisGameTotalGuess.set(lastTotal);
				}else{
					Log.info("发现存在其他服全民竞猜数据，当前系统总投注{}钻石,增加其他服竞猜投注{}钻石", thisGameTotalGuess.get(),lastTotal);
					thisGameTotalGuess.addAndGet(lastTotal);
				}

				List<Element> raceHorseDataListE = root.getChild("raceHorseData").getChildren("data");
				for (Element raceHorseDataE : raceHorseDataListE) {
					int horseId = Integer.parseInt(raceHorseDataE.getAttributeValue("id"));
					int horseNumber = Integer.parseInt(raceHorseDataE.getAttributeValue("num"));
					HorseData horseData = horseDataStructMap.get(horseId);
					
					//检查缓存内是否有对应马匹数据
					RaceHorseData data = racingHorseDataMap.get(horseNumber);
					if(data == null){
						data = new RaceHorseData(horseData, horseNumber);
						data.totalYabaoRole.set(Integer.parseInt(raceHorseDataE.getAttributeValue("totalYabaoRole")));
						data.totalYabaoIgot.set(Integer.parseInt(raceHorseDataE.getAttributeValue("totalYabaoIgot")));
						racingHorseDataMap.put(horseNumber, data);
						Log.info("上场选出参赛马匹，ID：{}，参赛号码：{}，名字：{}，总押宝人数{}，总钻石数：{}", horseData.horseId, horseNumber, horseData.horseName, data.totalYabaoRole.get(), data.totalYabaoIgot.get());
					}else{
						//如果存在是直接增加
						int yabaoRoleCount = Integer.parseInt(raceHorseDataE.getAttributeValue("totalYabaoRole"));
						int yabaoIgot = Integer.parseInt(raceHorseDataE.getAttributeValue("totalYabaoIgot"));
						Log.info("发现存在其他服全民竞猜数据，上场选出参赛马匹{}总押宝人数{}，总钻石数：{}，增加其他服押宝人数{}，钻石数：{}", horseData.horseName,data.totalYabaoRole.get(),
								data.totalYabaoIgot.get(),yabaoRoleCount, yabaoIgot);
						data.totalYabaoIgot.addAndGet(yabaoIgot);
						data.totalYabaoRole.addAndGet(yabaoRoleCount);
						racingHorseDataMap.put(horseNumber, data);
					}
					
				}
				
				
				List<Element> yabaoInfoListE = root.getChild("yabaoInfo").getChildren("role");
				for (Element roleInfoE : yabaoInfoListE) {
					long roleId = Long.parseLong(roleInfoE.getAttributeValue("roleId"));
					int totalYabao = Integer.parseInt(roleInfoE.getAttributeValue("totalYabao"));
					PlayerRoleGuessData roleData = new PlayerRoleGuessData(roleId);
					roleData.totalYabao.set(totalYabao);
					List<Element> yabaoListE = roleInfoE.getChildren("inf");
					for (Element infoE : yabaoListE) {
						int horseNumber = Integer.parseInt(infoE.getAttributeValue("num"));
						int igot = Integer.parseInt(infoE.getAttributeValue("igot"));
						roleData.yabaoMap.put(horseNumber, igot);
					}
					roleGuessDatas.put(roleId, roleData);
				}

//				Log.info("上轮投注的角色数量:" + roleGuessDatas.keySet().size());
				this.lastRaceChampionID = Integer.parseInt(root.getChildText("lastRaceChampionID"));

			} else {

				isReload = false;

				this.lastRaceChampionID = Integer.parseInt(root.getChildText("lastRaceChampionID"));

			}
			status = KPeopleGuessStatusEnum.STATUS_YABAO;

		}
		return isReload;
	}

	private boolean checkAndReloadPeopleGuessData() throws Exception {
		Log.info("服务器启动，从文件加载上次【全民竞猜】保存的数据.......");

		boolean isReload = false;
		String path = oldDataSavePath.replace("\\", "/").replace("/", File.separator);
		int lastIndexOf = path.lastIndexOf(File.separator);
		path = path.substring(0, lastIndexOf);
		File file = new File(path);
		if (!file.exists()) {
			Log.info("上次【全民竞猜】保存的数据不存在，走开服初始化流程...");
			return isReload;
		} 

		
		//检查文件夹内有多少个文件   因为有可能会合服  

		File[] files = file.listFiles();
		if(files.length == 0){
			return isReload;
		}
		
		for (int i = 0; i < files.length; i++) {
			boolean data = loadPeopleGuessData(path + File.separator + files[i].getName());
			if(isReload || data){
				isReload = true;
			}
			//删除文件  避免下次重新load   因为每次shutdown 都有新文件生成
//			files[i].delete();
			Log.info("删除文件：" + files[i].getName() );
		}
		Log.info("-----加载完成，当前投注人数{},当前投注总额{},系统保留金额{},", roleGuessDatas.keySet().size(), thisGameTotalGuess.get() ,systemGainLastRecord.get());

		return isReload;
	}

	private void initNewRace() {
		chooseHorse();
		status = KPeopleGuessStatusEnum.STATUS_YABAO;

	}

	private void chooseHorse() {
		Log.info("【全民竞猜】比赛编号{},开始初始化马匹数据！",raceId.get());

		for (Integer id : horseDataStructMap.keySet()) {
			HorseData data = horseDataStructMap.get(id);
			racingHorseDataMap.put(id, new RaceHorseData(data, id));
		}

	}

	public void startYabaoLast5Minute() {
		leftTime = prepare_live_time;
		KGame.newTimeSignal(this, DELAY_TIME, TimeUnit.SECONDS);
		raceId.incrementAndGet();
		
	}

	private void startPreRace() {
		Log.info("全民竞猜开赛前5分钟状态！比赛编号{}", raceId.get());
		boolean isLock = false;
		try {
			isLock = lock.tryLock(2, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (isLock) {
			try {
				status = KPeopleGuessStatusEnum.STATUS_WAITING_RACE;
				leftTime = prepare_live_time;
				pre_race_time = System.currentTimeMillis();
				race_time = pre_race_time + prepare_live_time * 1000;
				// 计算比赛结果
				caculateRaceResult();
				// 计算直播数据
				live_race_time_seconds = caculateRaceLiveData();
				KGame.newTimeSignal(this, DELAY_TIME, TimeUnit.SECONDS);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}

			// 开跑前五分钟闪图标
			KGuideManager.sendShineIconStatusToAllOnlineRoles(KFunctionTypeEnum.全民竞猜, true);
		}
	}

	private int caculateRaceLiveData() {

		horseLiveRaceDataList.clear();
		List<Integer> raceTimeList = new ArrayList<Integer>();

		int timeSecond = winner_fastest_race_time_seconds * 1000;
		for (int i = 0; i < racingHorseDataMap.size(); i++) {
			raceTimeList.add(timeSecond);
			if (i < 2) {
				timeSecond += UtilTool.random(300, 600);
			} else {
				timeSecond += UtilTool.random(200, 400);
			}
		}

		int line = raceTrackArea[3] / racingHorseDataMap.size();

		for (RaceHorseData raceHorse : racingHorseDataMap.values()) {
			HorseLiveRaceData raceData = new HorseLiveRaceData();
			raceData.beginX = raceTrackArea[0];
			raceData.beginY = (raceTrackArea[1] + raceTrackArea[3]) - line * (raceHorse.horseNumber - 1);
			raceData.horseNumber = raceHorse.horseNumber;
			raceData.totalSeconds = raceTimeList.get((raceHorse.rankNum - 1));

			// 计算直播脚本
			caculateHorseLiveRaceScript(raceData);
			horseLiveRaceDataList.add(raceData);
		}
		timeSecond = timeSecond / 1000 + ((timeSecond % 1000) > 0 ? 1 : 0);
		return timeSecond;
	}

	private void caculateHorseLiveRaceScript(HorseLiveRaceData raceData) {

		List<Integer> trackLength = new ArrayList<Integer>();
		int length = 0;

		while (length < raceTrackArea[2]) {
			int len = 0;
			if (raceTrackArea[2] - length < 0) {
				length = raceTrackArea[2];
				trackLength.add(length);
			} else if (raceTrackArea[2] - length < 1200) {
				len = raceTrackArea[2] - length;
				length += len;
				trackLength.add(len);
			} else {
				int maxLen = 0;
				if (raceTrackArea[2] - length > 300 * 2) {
					maxLen = 300 * 2;

				} else {
					maxLen = raceTrackArea[2] - length;
				}

				len = UtilTool.random(400, maxLen);
				length += len;
				trackLength.add(len);
			}
		}

		List<Integer> timeLength = new ArrayList<Integer>();
		int perTimeSeconds = (raceData.totalSeconds / trackLength.size());
		int restTime = raceData.totalSeconds - (perTimeSeconds * trackLength.size());
		for (int i = 0, totalTime = 0; i < trackLength.size(); i++) {
			if (i != trackLength.size() - 1) {
//				boolean isAdd = false;
				int time = perTimeSeconds;
				if (restTime > 0) {
					int decTime = UtilTool.random(1, restTime);
					time += decTime;
					restTime -= decTime;
				}
				timeLength.add(time);
				totalTime += time;
			} else {
				int time = (raceData.totalSeconds - totalTime);
				timeLength.add(time);
			}
		}

//		int totalTime = 0;
//		String speedStr = "";
		for (int i = 0, x = raceData.beginX; i < trackLength.size(); i++) {
			int time = timeLength.get(i);
			x += trackLength.get(i);
			raceData.runScript += time + "," + x + "," + raceData.beginY + ";";
//			totalTime += time;
//			speedStr += "长度:" + trackLength.get(i) + ",速度:" + (float) (trackLength.get(i) / (time / 1000)) + ",";
		}
		// System.out.println("----" +raceData.horseNumber +"号的速度链：" + speedStr
		// + "总用时" + totalTime);
	}

	private void caculateRaceResult() {
		Log.warn("【全民竞猜】比赛编号：{},统计所有参赛马匹押宝数据.....", raceId.get());
		for (Integer horseNumber : racingHorseDataMap.keySet()) {
			RaceHorseData horse = racingHorseDataMap.get(horseNumber);
			Log.warn("{}号[{}],押宝人数：{}，押宝钻石总数：{}", horseNumber, horse.horseData.horseName, horse.totalYabaoRole.get(), horse.totalYabaoIgot.get());
		}

		List<Integer> resultRank = new ArrayList<Integer>();
		resultRank.addAll(racingHorseDataMap.keySet());

		UtilTool.randomList(resultRank);

		Log.warn("----------最终赛果--------------");

		lastRaceChampionID = resultRank.get(0);

		String raceResultStr = "";
		for (Integer num : resultRank) {
			raceResultStr += num + "号【"+horseDataStructMap.get(num).horseName+"】，" ;
		}
		Log.warn("【全民竞猜】比赛编号：{},比赛结果排名：" + raceResultStr, raceId.get());

		// 上一场系统保留金暂时不变 等发奖后再更新
		// systemGainLastRecord.addAndGet(caculateResult.systemGain);

		// LastRaceRecord record = new
		// LastRaceRecord(raceId.get(),UtilTool.DATE_FORMAT.format(new
		// Date(race_time)),raceResultStr1);

		for (int i = 0, rankNum = 1; i < resultRank.size(); i++, rankNum++) {
			RaceHorseData data = racingHorseDataMap.get(resultRank.get(i));
			data.rankNum = rankNum;
			// record.lastGameRankResult.add(data);
		}
		//
		// while (lastRaceRecordList.size() >= LAST_GAME_RANK_RECORD_SIZE) {
		// lastRaceRecordList.remove(0);
		// }
		// lastRaceRecordList.add(record);
		// lastRaceRecord = record;

	}

	private void startRace() {
		// System.out.println("全民竞猜---------开跑！！");
		boolean isLock = false;
		try {
			isLock = lock.tryLock(2, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (isLock) {
			try {
				status = KPeopleGuessStatusEnum.STATUS_RACING;
				racing = true;
				leftTime = live_race_time_seconds;

				KGame.newTimeSignal(this, DELAY_TIME, TimeUnit.SECONDS);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}
	}

	private void horesRaceEnd() {
		// System.out.println("全民竞猜------------------------跑马结束！");
		boolean isLock = false;
		try {
			isLock = lock.tryLock(2, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (isLock) {
			try {
				status = KPeopleGuessStatusEnum.STATUS_SETTLE_PRICE;
				leftTime = settle_price_time_seconds;
				KGame.newTimeSignal(this, DELAY_TIME, TimeUnit.SECONDS);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}

			// TODO发送消息通知跑马结束
			KGuideManager.sendShineIconStatusToAllOnlineRoles(KFunctionTypeEnum.全民竞猜, false);
		}
	}

	private void horseRaceSettlePrice() {
		boolean isLock = false;
		try {
			isLock = lock.tryLock(2, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (isLock) {
			try {

				settlePrice();
				cleanUp();
				initNewRace();

				checkNextRaceTimeAndLog();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}

	}

	/**
	 * 检查下一次赛马时间是否已经跑完当天比赛，如果是就记录当天跑马数据Log
	 */
	private void checkNextRaceTimeAndLog() {

		int time = KPeopleGuessMonitor.getMonitor().getNowDelayTime();

		String roleStr = "";
		if (time > TimeUnit.SECONDS.convert(10, TimeUnit.HOURS)) {
			int totalValue = 0;
			for (Entry<Long, PlayerRoleGuessData> entry : currentDayRoleGuessDatas.entrySet()) {
				PlayerRoleGuessData data = entry.getValue();
				totalValue += data.totalYabao.get();
				roleStr += "角色id:[" + data.roleId + "],当天比赛共投注 " + data.totalYabao.get() + " 钻石。";
				for (Integer key : data.yabaoMap.keySet()) {
					roleStr += "投注 " + key+ " 号共 " + data.yabaoMap.get(key) +" 钻石，";
				}
				roleStr += "\n";
			}

			// 记录当天数据
			Log.warn("---------------------------------------------------" + UtilTool.DATE_FORMAT4.format(new Date()) + "【全民竞猜】当天三轮比赛已经完成,投注总额:{}钻石" + "，投注总人数{}，投注总次数{}----------------------------------",
					totalValue, currentDayRoleGuessDatas.keySet().size(), currentDayVoteCount);
			Log.info(roleStr);
			// 清理当天数据
			currentDayVoteCount.set(0);
			currentDayRoleGuessDatas.clear();
		}
	}

	private void settlePrice() {
		Log.warn("【全民竞猜】比赛编号：{},进入赛后派奖状态。系统当前投注总额：{}钻石，上一轮保留金额:{}钻石,当轮"
				+ "系统操作总额：{}钻石",
				raceId.get(), thisGameTotalGuess.get(),systemGainLastRecord.get(),  thisGameTotalGuess.get()+ systemGainLastRecord.get());
		Log.warn("本轮参与投注人数：" + roleGuessDatas.keySet().size());
		
		long total = thisGameTotalGuess.get() + systemGainLastRecord.get();
		long totalUse = 0;

		for (Long roleId : roleGuessDatas.keySet()) {
			String name = KSupportFactory.getRoleModuleSupport().getRoleName(roleId);
			String tips = "";
			int[] horseVote = new int[horseRaceSize]; 
			boolean isGetPrice = false;
			int reward = 0;

			// 遍历角色所有投注的马匹
			for (Integer horseNumber : roleGuessDatas.get(roleId).yabaoMap.keySet()) {
				RaceHorseData data = racingHorseDataMap.get(horseNumber);
				int igot = roleGuessDatas.get(roleId).yabaoMap.get(horseNumber);
				horseVote[horseNumber - 1] = igot;
				
				// 只有第一名才有奖励
				if (data.rankNum == 1) {
					isGetPrice = true;
					//计算出奖励值
					float percent = (float)igot / data.totalYabaoIgot.get();
					reward = Math.round((float)((total-data.totalYabaoIgot.get())* 0.7) * percent + igot);
					//判断是否超过5倍 如果超过则回收超出部分
					reward = (reward > igot * 5) ? (int)(igot *5) : reward;
					Log.warn("角色{}投注{}号{}钻石，获得奖励{}钻石",name, horseNumber, igot, reward);
					totalUse += reward;
				}
			}
			

			if (isGetPrice && reward > 0) {
				// 发送奖励邮件
				tips = PeopleGuessTips.getRewardMailContent(lastRaceChampionID, horseVote , reward);
				List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
				KCurrencyCountStruct money = new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, reward);
				moneyList.add(money);
				KSupportFactory.getMailModuleSupport().sendMoneyMailBySystem(roleId, moneyList, PresentPointTypeEnum.全民竞猜派奖, PeopleGuessTips.getMailTitle(), tips);
			}else{
				//发送通知没有获得奖励
				tips = PeopleGuessTips.getNoRewardMailContent(lastRaceChampionID, horseVote);
				KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(roleId, PeopleGuessTips.getMailTitle(), tips);
			}
		}
		
		
		//发完奖后要设置上一场系统保留金额  如果本轮投注金额为0  则不设置  将上一场保留金放到下一轮
		if(thisGameTotalGuess.get() != 0){
			int left = Math.round((float)((total - totalUse) * 0.7));
			Log.warn("本轮发奖后剩余{}钻石，保留70%(即{}钻石)到下一轮：" , total - totalUse, left);
			systemGainLastRecord.set(left);
		}
		

		Log.warn("----------------------------------------------【全民竞猜】比赛编号：{},本轮比赛结束------------------------------------------------------", raceId.get());
	}

	/**
	 * 保存投注数据到当天缓存
	 *  
	 * @param data
	 */
	private void saveCurrentGuessData(long roleID, int horseID, int count) {
		
		PlayerRoleGuessData roleGuessData = currentDayRoleGuessDatas.get(roleID);
		if(roleGuessData == null){
			PlayerRoleGuessData data = new PlayerRoleGuessData(roleID);
			currentDayRoleGuessDatas.put(roleID, data);
		}
		PlayerRoleGuessData guessData = currentDayRoleGuessDatas.get(roleID);

		int totalIgot = 0;
		if (!guessData.yabaoMap.containsKey(horseID)) {
			guessData.yabaoMap.put(horseID, count);
			guessData.totalYabao.addAndGet(count);
		} else {
			totalIgot = guessData.yabaoMap.get(horseID);
			totalIgot += count;
			guessData.yabaoMap.put(horseID, totalIgot);
			guessData.totalYabao.addAndGet(count);
		}
	}
	
	
	private void cleanUp() {

		leftTime = 0;
		horseLiveRaceDataList.clear();
		roleGuessDatas.clear();
		thisGameTotalGuess.set(0);
		racingHorseDataMap.clear();
	}

	private int getRestTimeSeconds() {
		// int returnSecond = leftTime;
		// if(leftTime == 0){
		// //如果这个时候leftTime 为0 表示活动还没有启动 那剩余时间应该是下一个赛马时间点-准备时间
		// returnSecond = KPeopleGuessMonitor.getMonitor().getNextDelayTime() -
		// prepare_live_time;
		// }
		// return returnSecond;
		return KPeopleGuessMonitor.getMonitor().getNowDelayTime();
	}

	private int getRoleYabaoCount(KRole role, int horseNumber) {
		if (roleGuessDatas.containsKey(role.getId())) {
			if (roleGuessDatas.get(role.getId()).yabaoMap.containsKey(horseNumber)) {
				return roleGuessDatas.get(role.getId()).yabaoMap.get(horseNumber);
			}
		}
		return 0;
	}

	/**
	 * 玩家点击活动图标
	 * 
	 * @param role
	 */
	public void processRoleRequestUI(KRole role) {
		// long second = UtilTool.getNowTimeInSecond();
		// if (second >= raceTimePoint[0] && second <= raceTimePoint[1]) {

		if (status == KPeopleGuessStatusEnum.STATUS_YABAO) {
			sendYabaoUIData(role);

		} else if (status == KPeopleGuessStatusEnum.STATUS_WAITING_RACE || status == KPeopleGuessStatusEnum.STATUS_RACING) {
			KSupportFactory.getDuplicateMapSupport().playerRoleJoinDuplicateMap(role, activityMap.getDuplicateId());
			processSendRacingData(role);

		} else if (status == KPeopleGuessStatusEnum.STATUS_SETTLE_PRICE) {
			KDialogService.sendDataUprisingDialog(role, PeopleGuessTips.getTipsSettlePriceTime());
		}
		KDialogService.sendNullDialog(role);
		// }
		// KDialogService.sendUprisingDialog(role,PeopleGuessTips.getTipsNotRacingTime());
	}

	/**
	 * 服务器通知客户端进入全民竞猜界面
	 * 
	 * @param role
	 */
	public void sendYabaoUIData(KRole role) {
		KGameMessage sendMsg = KGame.newLogicMessage(KActivityProtocol.SM_ENTER_PEOPLE_GUESS);
		sendMsg.writeLong((thisGameTotalGuess.get() + systemGainLastRecord.get()));
		sendMsg.writeInt(getRestTimeSeconds());
		sendMsg.writeByte(racingHorseDataMap.size());
		for (RaceHorseData raceHorse : racingHorseDataMap.values()) {
			HorseData data = raceHorse.horseData;
			sendMsg.writeByte(data.horseId);
			sendMsg.writeUtf8String(data.horseName);
			sendMsg.writeInt(data.horseResId);
			sendMsg.writeInt(getRoleYabaoCount(role, raceHorse.horseNumber));
			sendMsg.writeBoolean(data.horseId == lastRaceChampionID);

			// 新增一个投注人数
			int i = raceHorse.totalYabaoRole.get();
			sendMsg.writeInt(i);
		}
		// sendMsg.writeBoolean(false);
		// System.out.println("距离开跑入场时间-------------"+getRestTimeSeconds()+"当前状态："
		// + status.statusName + ",剩余时间：" + leftTime);
		KSupportFactory.getRoleModuleSupport().sendMsg(role.getId(), sendMsg);
	}

	/**
	 * 服务器通知更新押宝金额
	 * 
	 * @param role
	 * @param horseNumber
	 * @param totalIgot
	 */
	public void sendUpdateYabaoInfo(KRole role, int horseNumber, int totalIgot) {

		KGameMessage sendMsg = KGame.newLogicMessage(KActivityProtocol.SM_UPDATE_YABAO_INFO);
		sendMsg.writeByte(horseRaceSize);
		PlayerRoleGuessData guessData = roleGuessDatas.get(role.getId());
		for (RaceHorseData horse : racingHorseDataMap.values()) {
			sendMsg.writeByte(horse.horseNumber);
			if (guessData.yabaoMap.containsKey(horse.horseNumber)) {
				sendMsg.writeInt(guessData.yabaoMap.get(horse.horseNumber));
			} else {
				sendMsg.writeInt(0);
			}
			sendMsg.writeInt(horse.totalYabaoRole.get());
		}
		long total =(long) (systemGainLastRecord.get()+ thisGameTotalGuess.get());
		sendMsg.writeLong(total);
		KSupportFactory.getRoleModuleSupport().sendMsg(role.getId(), sendMsg);
	}

	/**
	 * 服务器通知更新是否正在开奖直播
	 * 
	 * @param role
	 * @param isLive
	 */
	public void sendUpdateLiveRacing(KRole role, boolean isLive) {
		KGameMessage sendMsg = KGame.newLogicMessage(KActivityProtocol.SM_UPDATE_IS_LIVE_RACING);
		sendMsg.writeBoolean(isLive);
		KSupportFactory.getRoleModuleSupport().sendMsg(role.getId(), sendMsg);
	}

	/**
	 * 服务器通知跑马的选手,客户端初始化并在地图中显示
	 */
	public void sendPreRacingHorses(KRole role) {
		KGameMessage sendMsg = KGame.newLogicMessage(KActivityProtocol.SM_SEND_PEOPLE_GUESS_RACING_RUNNERS);
		sendMsg.writeByte(horseRaceSize);
		for (HorseLiveRaceData liveData : horseLiveRaceDataList) {
			RaceHorseData horseData = racingHorseDataMap.get(liveData.horseNumber);
			sendMsg.writeByte(horseData.horseNumber);
			sendMsg.writeUtf8String(horseData.horseData.horseName);
			sendMsg.writeInt(horseData.horseData.horseResId);
			sendMsg.writeFloat((float) (liveData.beginX));
			sendMsg.writeFloat((float) (liveData.beginY));
		}
		if (status == KPeopleGuessStatusEnum.STATUS_WAITING_RACE) {
			sendMsg.writeInt(leftTime);
		} else {
			sendMsg.writeInt(0);
		}

		KSupportFactory.getRoleModuleSupport().sendMsg(role.getId(), sendMsg);
	}

	/**
	 * 服务器通知跑马开始和结果
	 * 
	 * @param role
	 */
	public void sendRacingResult(KRole role) {
		KGameMessage sendMsg = KGame.newLogicMessage(KActivityProtocol.SM_SEND_PEOPLE_GUESS_RACING_RESULT);
		sendMsg.writeByte(horseLiveRaceDataList.size());
		for (HorseLiveRaceData liveData : horseLiveRaceDataList) {
			RaceHorseData horseData = racingHorseDataMap.get(liveData.horseNumber);
			sendMsg.writeByte(horseData.rankNum);
			sendMsg.writeByte(liveData.horseNumber);
			sendMsg.writeUtf8String(liveData.runScript);
		}

		KSupportFactory.getRoleModuleSupport().sendMsg(role.getId(), sendMsg);
	}

	/**
	 * 客户端请求下注
	 * 
	 * @param role
	 * @param horseId
	 * @param count
	 */
	public void clientYabao(KRole role, int horseId, int count) {

		try {

			if (status != KPeopleGuessStatusEnum.STATUS_YABAO) {
				KDialogService.sendUprisingDialog(role,PeopleGuessTips.getTipsNotYabaoTime());
				return;
			}

			if (!racingHorseDataMap.containsKey(horseId)) {
				KDialogService.sendDataUprisingDialog(role, PeopleGuessTips.getTipsNotFoundHorse(horseId));
				return;
			}

			boolean checkIgot = false;
			for (Integer igot : yabaoMenuList) {
				if (count == igot) {
					checkIgot = true;
					break;
				}
			}
			if (!checkIgot) {
				KDialogService.sendUprisingDialog(role,
						PeopleGuessTips.getTipsYabaoFieldByIgotError());
				return;
			}

			long result = KSupportFactory.getCurrencySupport().decreaseMoney(
					role.getId(),
					new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, count),
					UsePointFunctionTypeEnum.全民竞猜押宝, true);

			if (result == -1) {
				KDialogService.sendDataUprisingDialog(role,
						PeopleGuessTips.getTipsNotEnoughMoney());
				return;
			}

			RaceHorseData horseData = racingHorseDataMap.get(horseId);

			horseData.totalYabaoIgot.addAndGet(count);

			if (!roleGuessDatas.containsKey(role.getId())) {
				roleGuessDatas.put(role.getId(),
						new PlayerRoleGuessData(role.getId()));
			}

			PlayerRoleGuessData guessData = roleGuessDatas.get(role.getId());

			int totalIgot = 0;
//			int roleTotalYabao = 0;
			if (!guessData.yabaoMap.containsKey(horseId)) {
				guessData.yabaoMap.put(horseId, count);
				guessData.totalYabao.addAndGet(count);
				totalIgot = count;
				horseData.totalYabaoRole.incrementAndGet();
			} else {
				totalIgot = guessData.yabaoMap.get(horseId);
				totalIgot += count;
				guessData.yabaoMap.put(horseId, totalIgot);
				guessData.totalYabao.addAndGet(count);
			}

			long systemTotalYabao = thisGameTotalGuess.addAndGet(count);

			// 更新界面
			sendUpdateYabaoInfo(role, horseId, totalIgot);

			KDialogService.sendUprisingDialog(role, PeopleGuessTips
					.getTipsYabaoSuccess(horseId,
							horseData.horseData.horseName, count));

			currentDayVoteCount.incrementAndGet();
			saveCurrentGuessData(role.getId(), horseId, count);
			
			Log.warn("玩家：{}投注{}号{}{}钻石,系统当前投注总额{}钻石", role.getName(), horseId,
					horseData.horseData.horseName, count, systemTotalYabao);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 客户端请求进入赛马地图
	 * 
	 * @param role
	 */
	public void processRoleEnterRaceMap(KRole role) {

		if (status == KPeopleGuessStatusEnum.STATUS_WAITING_RACE || status == KPeopleGuessStatusEnum.STATUS_RACING) {
			KSupportFactory.getDuplicateMapSupport().playerRoleJoinDuplicateMap(role, activityMap.getDuplicateId());
			processSendRacingData(role);
		} else if (status == KPeopleGuessStatusEnum.STATUS_SETTLE_PRICE) {
			KDialogService.sendUprisingDialog(role, PeopleGuessTips.getTipsSettlePriceTime());
		} else {
			KDialogService.sendUprisingDialog(role, PeopleGuessTips.getTipsNotRacingTime());
		}
	}

	public void processSendRacingData(KRole role) {
		if (status == KPeopleGuessStatusEnum.STATUS_WAITING_RACE || status == KPeopleGuessStatusEnum.STATUS_RACING) {
			sendPreRacingHorses(role);
			sendRacingResult(role);
		}
	}

	/**
	 * 玩家请求退出比赛地图
	 * 
	 * @param role
	 */
	public void processRoleExitRaceMap(KRole role) {
		KSupportFactory.getDuplicateMapSupport().playerRoleLeaveDuplicateMap(role, activityMap.getDuplicateId());
		KDialogService.sendNullDialog(role);
	}

	public void processServerShutDown() {
		shutDownProcessing = true;

		try {
			//checkAndSavePeopleGuessData();
			
			//不将数据保存回文件  修改为保存入数据库
			save();
		} catch (Exception e) {
			Log.error("服务器关闭时保存【全民竞猜】数据出错！", e);
		}

	}

	
	/**
	 * 保存xml
	 * 
	 * @throws Exception
	 */
	public void checkAndSavePeopleGuessData() throws Exception {
		String path = oldDataSavePath.replace("\\", "/").replace("/", File.separator);
		File file = new File(oldDataSaveDir);
		if (!file.exists()) {
			file.mkdir();
		}
		KXmlWriter writer = new KXmlWriter(path, true);
		boolean isLock = false;
		try {
			isLock = lock.tryLock(2l, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (isLock) {
			try {
				if (status != KPeopleGuessStatusEnum.STATUS_SETTLE_PRICE) {

					Element timeE = new Element("shutdownTime");
					timeE.setText(new Date().toString());
					writer.addElement(timeE);

					Element raceIdE = new Element("raceId");
					raceIdE.setText("" + raceId.get());
					writer.addElement(raceIdE);

					Element statusE = new Element("status");
					statusE.setText("" + KPeopleGuessStatusEnum.STATUS_YABAO.status);
					writer.addElement(statusE);

					Element thisGameTotalYaobaoE = new Element("thisGameTotalYaobao");
					thisGameTotalYaobaoE.setText("" + thisGameTotalGuess.get());
					writer.addElement(thisGameTotalYaobaoE);

					Element systemGainE = new Element("systemGain");
					systemGainE.setText("" + systemGainLastRecord.get());
					writer.addElement(systemGainE);

					Element raceHorseDataListE = new Element("raceHorseData");
					Element raceHorseDataE;
					for (Iterator<RaceHorseData> itr = racingHorseDataMap.values().iterator(); itr.hasNext();) {
						RaceHorseData data = itr.next();
						raceHorseDataE = new Element("data");
						raceHorseDataE.setAttribute("id", String.valueOf(data.horseData.horseId));
						raceHorseDataE.setAttribute("num", String.valueOf(data.horseNumber));
						raceHorseDataE.setAttribute("totalYabaoRole", String.valueOf(data.totalYabaoRole.get()));
						raceHorseDataE.setAttribute("totalYabaoIgot", String.valueOf(data.totalYabaoIgot.get()));
						raceHorseDataListE.addContent(raceHorseDataE);
					}
					writer.addElement(raceHorseDataListE);

					Element yabaoInfoListE = new Element("yabaoInfo");
					Element roleInfoE, yabaoInfoE;
					for (Iterator<PlayerRoleGuessData> itr = roleGuessDatas.values().iterator(); itr.hasNext();) {
						roleInfoE = new Element("role");
						PlayerRoleGuessData roleData = itr.next();
						roleInfoE.setAttribute("roleId", String.valueOf(roleData.roleId));
						roleInfoE.setAttribute("totalYabao", String.valueOf(roleData.totalYabao.get()));
						for (Integer horseNumber : roleData.yabaoMap.keySet()) {
							int igot = roleData.yabaoMap.get(horseNumber);
							yabaoInfoE = new Element("inf");
							yabaoInfoE.setAttribute("num", String.valueOf(horseNumber));
							yabaoInfoE.setAttribute("igot", String.valueOf(igot));
							roleInfoE.addContent(yabaoInfoE);
						}
						yabaoInfoListE.addContent(roleInfoE);
					}
					writer.addElement(yabaoInfoListE);

					Element championID = new Element("lastRaceChampionID");
					championID.setText("" + lastRaceChampionID);
					writer.addElement(championID);

				} else {
					Element raceIdE = new Element("raceId");
					raceIdE.setText("" + raceId.get());
					writer.addElement(raceIdE);

					Element statusE = new Element("status");
					statusE.setText("" + status.status);
					writer.addElement(statusE);

					Element systemGainE = new Element("systemGain");
					raceIdE.setText("" + systemGainLastRecord.get());
					writer.addElement(systemGainE);

					Element championID = new Element("lastRaceChampionID");
					championID.setText("" + lastRaceChampionID);
					writer.addElement(championID);
				}
				writer.output();
			} finally {
				lock.unlock();
			}
		}


		Log.warn("**************************************");
		Log.warn("******************************************服务器保存【全民竞猜】数据*****************************");

	}

	public void checkRoleJoinGame(KRole role) {
		// 角色登录 检查是否要发送跑马图标闪烁
		if (status == KPeopleGuessStatusEnum.STATUS_WAITING_RACE || status == KPeopleGuessStatusEnum.STATUS_RACING) {
			KGuideManager.sendShineIconStatusToAllOnlineRoles(KFunctionTypeEnum.全民竞猜, true);
		}

	}

	
	
	
	@Override
	protected void decode(JSONObject json) throws JSONException {
		if(json == null){
			return;
		}
		Log.info("---------------------------------开始加载保存数据-----------------------------------------------------");
		raceId.set(json.optLong(KEY_RACE_ID,1));
		systemGainLastRecord.set(json.optLong(KEY_SYSTEMGAINLASTRECORD, 0));
		Log.info("上次【全民竞猜】系统保留{}钻石", systemGainLastRecord.get());

		byte raceStatus = json.getByte(KEY_STATUS);
		if(raceStatus != KPeopleGuessStatusEnum.STATUS_SETTLE_PRICE.status){
			thisGameTotalGuess.set(json.optLong(KEY_THISGAMETOTALGAIN, 0));
			Log.info("加载上次【全民竞猜】总投注{}钻石", thisGameTotalGuess.get());
			
			
			JSONObject racingDataObj = json.optJSONObject(KEY_RACINGHORSEDATAMAP);
			String key;
			RaceHorseData horseData;
			if(racingDataObj != null){
				for (Iterator<String> itr = racingDataObj.keys(); itr.hasNext();) {
					key = itr.next();
					horseData = new RaceHorseData();
					horseData.init(racingDataObj.getString(key));
					racingHorseDataMap.put(Integer.parseInt(key), horseData);
				}
			}
			
			
			JSONObject roleGuessObj = json.optJSONObject(KEY_ROLEGUESSDATAS);
			PlayerRoleGuessData guessData;
			
			if(roleGuessObj != null){
				for (Iterator<String> itr = roleGuessObj.keys(); itr.hasNext();) {
					key = itr.next();
					guessData = new PlayerRoleGuessData(Long.parseLong(key));
					guessData.init(roleGuessObj.getString(key));
					roleGuessDatas.put(Long.parseLong(key), guessData);
				}
			}
			
			
		}
		
		int lastID = json.getInt(KEY_LASTCHAMPOINID);
		if(lastID != 0){
			
			this.lastRaceChampionID = lastID; 
		}
		
		this.status = KPeopleGuessStatusEnum.STATUS_YABAO;
		
		//保存当天数据记录
		currentDayVoteCount.set(json.optInt(KEY_DAYVOTECOUNT, 0));
		
		JSONObject dayObj = json.optJSONObject(KEY_DAYROLEGUESSDATAS);
		PlayerRoleGuessData guessData;
		if(dayObj != null){
			for (Iterator<String> itr = dayObj.keys(); itr.hasNext();) {
				String key = itr.next();
				guessData = new PlayerRoleGuessData(Long.parseLong(key));
				guessData.init(dayObj.getString(key));
				currentDayRoleGuessDatas.put(Long.parseLong(key), guessData);
			}
		}
		
		DATAINITFROMDB = true;
	}

	@Override
	protected JSONObject encode() throws JSONException {
		
		boolean isLock = false;
		try {
			isLock = lock.tryLock(2l, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Log.info("-------------------------"+new Date().toString()+"停服开始保存【全民竞猜】数据");
		
		
		JSONObject obj = new JSONObject();
		
		if(isLock){
		
			try {
				
				obj.put(KEY_RACE_ID, raceId.get());
				obj.put(KEY_STATUS, this.status.status);
				obj.put(KEY_SYSTEMGAINLASTRECORD, systemGainLastRecord.get());
				obj.put(KEY_LASTCHAMPOINID, lastRaceChampionID);
				if(status != KPeopleGuessStatusEnum.STATUS_SETTLE_PRICE){
					obj.put(KEY_THISGAMETOTALGAIN, thisGameTotalGuess.get());
					
					JSONObject racingHorseJson = new JSONObject();
					for (Iterator<RaceHorseData> itr = racingHorseDataMap.values().iterator(); itr.hasNext();) {
						RaceHorseData data = itr.next();
						racingHorseJson.put(String.valueOf(data.horseNumber), data.saveAttribute());
					}
					
					obj.put(KEY_RACINGHORSEDATAMAP, racingHorseJson);
					
					
					JSONObject roleGuessDatasObj = new JSONObject();
					
					for (Iterator<PlayerRoleGuessData> itr = roleGuessDatas.values().iterator(); itr.hasNext();) {
						PlayerRoleGuessData next = itr.next();
						roleGuessDatasObj.put(String.valueOf(next.roleId), next.saveAttribute());
					}
					obj.put(KEY_ROLEGUESSDATAS, roleGuessDatasObj);
					
					obj.put(KEY_DAYVOTECOUNT, currentDayVoteCount.get());
					
					JSONObject dayObj = new JSONObject();
					
					for (Iterator<PlayerRoleGuessData> itr = currentDayRoleGuessDatas.values().iterator(); itr.hasNext();) {
						PlayerRoleGuessData next = itr.next();
						dayObj.put(String.valueOf(next.roleId), next.saveAttribute());
					}
					
					obj.put(KEY_DAYROLEGUESSDATAS, dayObj);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				lock.unlock();
			}
			
		}
		Log.warn("******************************************服务器保存【全民竞猜】数据*****************************");
		return obj;
	}

	
	
	
	class HorseData{
		public int horseId;

		public String horseName;

		public int horseResId;


		private final static String KEY_HORSEID = "1";
		private final static String KEY_HORSENAME = "2";
		private final static String KEY_HORSERESID = "3";
		
		public HorseData(int horseId, String horseName, int horseResId) {
			this.horseId = horseId;
			this.horseName = horseName;
			this.horseResId = horseResId;
		}
		
		public HorseData(String dataStr){
			JSONObject obj;
			try {
				obj = new JSONObject(dataStr);
				this.horseId = obj.getInt(KEY_HORSEID);
				this.horseName = obj.getString(KEY_HORSENAME);
				this.horseResId = obj.getInt(KEY_HORSERESID);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		public String saveAttribute(){
			JSONObject obj = null;
			try {
				obj = new JSONObject();
				obj.put(KEY_HORSEID, this.horseId);
				obj.put(KEY_HORSENAME, this.horseName);
				obj.put(KEY_HORSERESID, this.horseResId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return obj.toString();
		}

		@Override
		public String toString() {
			return "HorseData [horseId=" + horseId + ", horseName=" + horseName
					+ ", horseResId=" + horseResId + "]";
		}
	}

	class RaceHorseData {
		
		public HorseData horseData;
		public final AtomicInteger totalYabaoIgot = new AtomicInteger(0);
		public final AtomicInteger totalYabaoRole = new AtomicInteger(0);
		public int horseNumber;
		public int rankNum;

		private final static String KEY_HORSEDATA = "1";
		private final static String KEY_IGOT = "2";
		private final static String KEY_ROLENUM = "3";
		private final static String KEY_HORSENUM = "4";
		private final static String KEY_RANKNUM = "5";
		
		
		public RaceHorseData() {
		}

		public RaceHorseData(HorseData horseData, int horseNumber) {
			this.horseData = horseData;
			this.horseNumber = horseNumber;
		}
		
		public void init(String dataSoure){
			if(dataSoure != null){
				JSONObject obj;
				try {
					obj = new JSONObject(dataSoure);
					String horseDataStr = obj.getString(KEY_HORSEDATA);
					this.horseData = new HorseData(horseDataStr);
					this.totalYabaoIgot.set(obj.optInt(KEY_IGOT, 0));
					this.totalYabaoRole.set(obj.optInt(KEY_ROLENUM, 0));
					this.horseNumber = obj.getInt(KEY_HORSENUM);
					this.rankNum = obj.getInt(KEY_RANKNUM);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		public String saveAttribute() throws Exception{
			JSONObject obj = new JSONObject();
			obj.put(KEY_HORSEDATA, this.horseData.saveAttribute());
			obj.put(KEY_IGOT, this.totalYabaoIgot.get());
			obj.put(KEY_ROLENUM, this.totalYabaoRole.get());
			obj.put(KEY_HORSENUM, this.horseNumber);
			obj.put(KEY_RANKNUM, this.rankNum);
			return obj.toString();
		}

		@Override
		public String toString() {
			return "RaceHorseData [horseData=" + horseData
					+ ", totalYabaoIgot=" + totalYabaoIgot
					+ ", totalYabaoRole=" + totalYabaoRole + ", horseNumber="
					+ horseNumber + ", rankNum=" + rankNum + "]";
		}
		
		
	}

	class PlayerRoleGuessData {
		public long roleId;

		public final Map<Integer, Integer> yabaoMap = new HashMap<Integer, Integer>();

		public final AtomicInteger totalYabao;

		private final static String KEY_ROLEID = "1";
		private final static String KEY_YABAOMAP = "2";
		private final static String KEY_TOTALYABAO = "3";
		
		

		public PlayerRoleGuessData(long roleId) {
			this.roleId = roleId;
			totalYabao = new AtomicInteger(0);
		}
		
		public void init(String dataSource){
			if(dataSource != null){
				JSONObject obj;
				try {
					obj = new JSONObject(dataSource);
					this.roleId = obj.getLong(KEY_ROLEID);
					this.totalYabao.set(obj.optInt(KEY_TOTALYABAO, 0));
					JSONObject mapObj = obj.optJSONObject(KEY_YABAOMAP);
					String key;
					for (Iterator<String> itr = mapObj.keys(); itr.hasNext();) {
						key = itr.next();
						int value = mapObj.getInt(key);
						yabaoMap.put(Integer.parseInt(key), value);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		
		String saveAttribute(){
			JSONObject obj = null;
			try {
				obj = new JSONObject();
				obj.put(KEY_ROLEID, this.roleId);
				obj.put(KEY_TOTALYABAO, this.totalYabao.get());
				
				JSONObject mapObj = new JSONObject();
				
				for (Entry<Integer, Integer> entry : yabaoMap.entrySet()) {
					mapObj.put(String.valueOf(entry.getKey()), entry.getValue());
				}
				
				obj.put(KEY_YABAOMAP, mapObj);
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return obj.toString();
		}
	}

	class HorseLiveRaceData {
		public int beginX, beginY;
		public int horseNumber;
		public int totalSeconds;
		public String runScript = "";
	}


}