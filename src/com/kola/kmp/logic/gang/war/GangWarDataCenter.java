package com.kola.kmp.logic.gang.war;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.flowdata.FlowDataModule.FamilyWarCounterType;
import com.kola.kgame.cache.flowdata.FlowDataModuleFactory;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.competition.KCompetitionBattlefield;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace.RaceGangData;
import com.kola.kmp.logic.gang.war.GangWarTaskManager.GangWarStatusTask;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangMedalDataManager.GangMedalData;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.rank.gang.GangRank;
import com.kola.kmp.logic.rank.gang.GangRankElementWarSignUp;
import com.kola.kmp.logic.rank.gang.KGangRankLogic;
import com.kola.kmp.logic.rank.gang.KGangRankTypeEnum;
import com.kola.kmp.logic.support.GangModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KXmlWriter;

/**
 * <pre>
 * 军团数据管理
 * 管理本周军团战的各项数据
 * 
 * @author CamusHuang
 * @creation 2013-8-29 下午6:29:22
 * </pre>
 */
public class GangWarDataCenter {

	private static final String saveDirPath = "./res/output/gangWar/";
	private static final String saveFileName = "gangWarDatas";
	private static final String saveFileNameSuffix = ".xml";

	// 用于PVP的战斗地图
	static final KCompetitionBattlefield PVPBattlefield = new KCompetitionBattlefield();
	// 用于PVE的战斗地图
	static KGameBattlefield PVEBattlefield;

	/** --------入围军团数据--------- */
	/* 入围的32个军团:一定有32个，不足32个报名则生成负数ID的GangData */
	private static final ArrayList<GangData> allWarGangsList = new ArrayList<GangData>();
	/* key=军团ID */
	private static final Map<Long, GangData> allWarGangsMap = new HashMap<Long, GangData>();

	/** --------各场对战数据--------- */
	/* key=场次1，2，3.. */
	private static final Map<Integer, GangWarRound> allWarRoundsMap = new HashMap<Integer, GangWarRound>();
	/* 当前是第几场 */
	private static int nowRound = 1;

	static void notifyCacheLoadComplete() throws KGameServerException {
		PVPBattlefield.initBattlefield(KGangWarConfig.getInstance().军团战PVP地图文件名, KGangWarConfig.getInstance().军团战PVP地图背景音乐);
		try {
			if (PVPBattlefield == null) {
				throw new KGameServerException("军团战PVP 战斗场景不存在");
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		PVEBattlefield = KSupportFactory.getLevelSupport().getFamilyWarBattlefield(KGangWarConfig.getInstance().军团战PVE地图关卡ID);
		try {
			if (PVEBattlefield == null) {
				throw new KGameServerException("军团战PVE 战斗场景不存在");
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * <pre>
	 * 审核报名榜，选出入围军团战的军团，同时初始化各场对战数据
	 * 
	 * @return 是否顺利开启活动
	 * @author CamusHuang
	 * @creation 2014-5-20 下午2:55:15
	 * </pre>
	 */
	static boolean checkUpSignUpRanksAndInitRounds() {

		// 报名榜前32的军团可以入围军团战
		GangRank<GangRankElementWarSignUp> rank = KGangRankLogic.getRank(KGangRankTypeEnum.军团战报名);
		rank.onTimeSignalForPublish(true, true, true);
		//
		Set<Long> joinGangIdSet = new HashSet<Long>();
		int minJoinFlourish = 0;// 最低入围繁荣度
		//
		boolean isSuccess = false;
		GangWarLogic.lock.lock();
		try {
			List<GangRankElementWarSignUp> rankElementList = rank.getPublishData().getUnmodifiableElementList();
			if (rankElementList.size() < 1) {
				GangWarLogic.GangWarLogger.warn("军团战：开启失败，报名军团数量不足1");
				return isSuccess;
			}

			// 记录流水
			FlowDataModuleFactory.getModule().recordFamilyWarCounter(0, FamilyWarCounterType.报名军团数量, rankElementList.size());

			// 清理所有数据
			clearData();

			// 初始化入围军团，不足则补空
			for (int i = 0; i < KGangWarConfig.getInstance().MaxGangCount; i++) {
				GangData temp = null;

				if (rankElementList.size() > i) {
					GangRankElementWarSignUp gangE = rankElementList.get(i);
					KGang gang = KSupportFactory.getGangSupport().getGang(gangE.elementId);
					temp = new GangData(gangE.elementId, gang.getLevel(), gangE.elementName);
					//
					joinGangIdSet.add(gangE.elementId);
					if (gangE.getFlourish() > minJoinFlourish) {
						minJoinFlourish = gangE.getFlourish();
					}
				} else {
					temp = new GangData(-(i + 1), 0, "--");
				}
				allWarGangsList.add(temp);
				allWarGangsMap.put(temp.gangId, temp);
			}

			// 按军团战力对入围军团进行排序，战力强则排前
			resetGangBattlePowersAndResort(allWarGangsList);

			// 各场数据
			for (int round = 1; round <= KGangWarConfig.getInstance().MaxRound; round++) {
				GangWarRound roundData = new GangWarRound(round);
				allWarRoundsMap.put(roundData.round, roundData);
			}

			GangWarLogic.GangWarLogger.warn("军团战：第1场：分组成功");
			isSuccess = true;
			return isSuccess;
		} finally {
			GangWarLogic.lock.unlock();

			// 清空报名榜、通报到各军团
			clearSignUpRankAndNotifyGangs(isSuccess, minJoinFlourish, rank, joinGangIdSet);
		}

	}

	/**
	 * <pre>
	 * 将军团数据的总战力重新计算，并重排
	 * 
	 * @param allWarGangsList
	 * @author CamusHuang
	 * @creation 2014-5-20 上午10:41:26
	 * </pre>
	 */
	private static void resetGangBattlePowersAndResort(ArrayList<GangData> allWarGangsList) {
		GangModuleSupport support = KSupportFactory.getGangSupport();
		for (GangData data : allWarGangsList) {
			KGang gang = support.getGang(data.gangId);
			if (gang != null) {
				data.setBattlePower(gang.countGangBattlePower());
			}
		}

		Collections.sort(allWarGangsList);
	}

	/**
	 * <pre>
	 * 清空排行榜
	 * 通知入围和未入围的军团
	 * 
	 * @param rank 报名排行榜
	 * @param joinGangIdSet 入围军团名单
	 * @author CamusHuang
	 * @creation 2013-8-28 上午10:08:21
	 * </pre>
	 */
	static void clearSignUpRankAndNotifyGangs(boolean isSuccess, int minJoinFlourish, GangRank<GangRankElementWarSignUp> rank, Set<Long> joinGangIdSet) {
		GangWarLogic.GangWarLogger.warn("军团战：入围军团通告");
		//
		List<GangRankElementWarSignUp> list = rank.getPublishData().getUnmodifiableElementList();
		for (GangRankElementWarSignUp element : list) {

			if (joinGangIdSet.contains(element.elementId)) {
				// 入围通报
				GangWarSystemBrocast.onSignupEnd_JoinList(element.elementId, minJoinFlourish, element.getFlourish());
			} else {
				// 系统通知
				GangWarSystemBrocast.onSignupEnd_OutList(isSuccess, element.elementId, minJoinFlourish, element.getFlourish());
			}
		}

		if(isSuccess){
			GangWarLogic.GangWarLogger.warn("军团战：清空报名表");
			// 清空排行榜
			rank.clear();
			// 清空所有军团的繁荣度
			KGangLogic.clearAllGangFlourish();
		}
	}

	/**
	 * <pre>
	 * 清理所有数据
	 * 
	 * @param isForce
	 * @author CamusHuang
	 * @creation 2013-10-11 下午11:18:12
	 * </pre>
	 */
	static void clearData() {
		// PS:不作数据备份
		allWarGangsList.clear();
		allWarGangsMap.clear();
		for (GangWarRound round : allWarRoundsMap.values()) {
			round.clearData();
		}
		allWarRoundsMap.clear();
	}

	static GangData getWarGang(long gangId) {
		return allWarGangsMap.get(gangId);
	}

	static List<GangData> getUnmodifyWarGangs() {
		return Collections.unmodifiableList(allWarGangsList);
	}

	static GangWarRound getRoundData(int roundId) {
		return allWarRoundsMap.get(roundId);
	}

	static void setNowRoundId(int roundId) {
		nowRound = roundId;
	}

	static int getNowRoundId() {
		return nowRound;
	}

	static GangWarRound getNowRoundData() {
		return allWarRoundsMap.get(nowRound);
	}

	/**
	 * <pre>
	 * 获取最近结束的一场
	 * 当前第2，则返回第1，
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2013-10-13 下午9:16:36
	 * </pre>
	 */
	static GangWarRound getNearRoundData() {
		if (GangWarStatusManager.getNowStatus() == GangWarStatusEnum.WAR_ROUND_START_NOW) {
			return getNowRoundData();
		}
		switch (nowRound) {
		case 1:
			return allWarRoundsMap.get(1);
		case 2:
			return allWarRoundsMap.get(1);
		default:
			GangWarRound round = allWarRoundsMap.get(nowRound);
			if (round != null && round.isEnd()) {
				return round;
			}
			return allWarRoundsMap.get(nowRound - 1);
		}
	}

	/**
	 * <pre>
	 * 按场次将军团分组
	 * 
	 * @param round
	 * @author CamusHuang
	 * @creation 2013-9-17 下午7:44:53
	 * </pre>
	 */
	static void groupRound(int round) {
		nowRound = round;
		//
		if (round == 1) {
			groupRound1(allWarGangsList, allWarRoundsMap.get(round));
			return;
		}
		if (round == KGangWarConfig.getInstance().MaxRound) {
			groupRoundLast(allWarRoundsMap.get(round - 1).searchAllWinGangDatas(), allWarRoundsMap.get(round));
			return;
		}
		{
			groupRoundX(allWarRoundsMap.get(round - 1), allWarRoundsMap.get(round));
		}
	}
	
	/**
	 * <pre>
	 * 初赛对战组划分规则：将参赛队伍按战斗力优先将战斗力强的一半军团平均放置到A、B两组，按该顺序分配：战力第一军团放置到A组，第二放置到B组，第三军团放置到A组；以此类推直至分配完成所有参赛军团的一半X、若参赛军团为单数时则为X+1
	 * 初赛对战匹配规则：前一半参赛军团被划分完成后，会给已分配的每个军团随机匹配后剩余参赛军团作为第一场比赛的竞争对手，最后一个单数的军团无对手匹配时该军团本次轮空
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 上午11:18:49
	 * </pre>
	 */
	private static void groupRound1(List<GangData> lastWinGangList, GangWarRound thisRoundData) {
		// 强弱分界线
		int divideIndex = lastWinGangList.size() / 2;

		ArrayList<GangData> topWarGangsList = new ArrayList<GangData>(lastWinGangList.subList(0, divideIndex));
		ArrayList<GangData> lowWarGangsList = new ArrayList<GangData>(lastWinGangList.subList(divideIndex, lastWinGangList.size()));

		List<GangRace> groupAList = new ArrayList<GangRace>();
		List<GangRace> groupBList = new ArrayList<GangRace>();

		// ===================================本场分组
		for (int index = 0; index < topWarGangsList.size(); index++) {
			GangData topData = topWarGangsList.get(index);
			GangData lowData = lowWarGangsList.remove(UtilTool.random(lowWarGangsList.size()));

			boolean isGroupA = index % 2 == 0;

			GangRace race = new GangRace(thisRoundData.round, isGroupA, topData, lowData);
			if (isGroupA) {
				groupAList.add(race);
			} else {
				groupBList.add(race);
			}
			GangWarLogic.GangWarLogger.warn("军团争霸第{}场分组：{}：{{}-{}} vs {{}-{}}", thisRoundData.round, isGroupA ? "A组" : "B组", race.gangDataA.gangId, race.gangDataA.gangData.gangName,
					race.gangDataB.gangId, race.gangDataB.gangData.gangName);
		}
		//
		thisRoundData.initData(groupAList, groupBList, null);
	}	

	/**
	 * <pre>
	 * 将上一场A\B组的胜出军团挑出，进行强弱匹配
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 上午11:18:49
	 * </pre>
	 */
	private static void groupRoundX(GangWarRound lastRoundData, GangWarRound thisRoundData) {
		
		List<GangRace> groupAList = groupRoundIn(lastRoundData, true, thisRoundData);
		List<GangRace> groupBList = groupRoundIn(lastRoundData, false, thisRoundData);
		//
		thisRoundData.initData(groupAList, groupBList, null);
	}
	
	/**
	 * <pre>
	 * N从1开始算的奇数场
	 * 取第N场的胜者，与第N+1场的胜者匹配
	 * 
	 * @param lastRoundData
	 * @param isGroupA
	 * @param thisRoundData
	 * @return
	 * @author CamusHuang
	 * @creation 2014-11-6 下午9:28:35
	 * </pre>
	 */
	private static List<GangRace> groupRoundIn(GangWarRound lastRoundData, boolean isGroupA, GangWarRound thisRoundData) {
		
		List<GangData> lastWinGangList = lastRoundData.searchAllWinGangDatas(isGroupA);
		
		List<GangRace> raceList = new ArrayList<GangRace>();

		// ===================================本场分组
		for (int index = 0; index < lastWinGangList.size(); ) {
			GangData topData = lastWinGangList.get(index);
			GangData lowData = lastWinGangList.get(index+1);

			GangRace race = new GangRace(thisRoundData.round, isGroupA, topData, lowData);
			raceList.add(race);
			
			GangWarLogic.GangWarLogger.warn("军团争霸第{}场分组：{}：{{}-{}} vs {{}-{}}", thisRoundData.round, isGroupA ? "A组" : "B组", race.gangDataA.gangId, race.gangDataA.gangData.gangName,
					race.gangDataB.gangId, race.gangDataB.gangData.gangName);
			
			index+=2;
		}
		
		return raceList;
	}	

	/**
	 * <pre>
	 * 找出两个胜出军团，进行冠军PK
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 上午11:18:49
	 * </pre>
	 */
	private static void groupRoundLast(List<GangData> lastWinGangList, GangWarRound thisRoundData) {
		GangData topData = lastWinGangList.get(0);
		GangData lowData = lastWinGangList.get(1);
		boolean isGroupA = true;
		GangRace race = new GangRace(thisRoundData.round, isGroupA, topData, lowData);
		GangWarLogic.GangWarLogger.warn("军团争霸第{}场分组：{}：{{}-{}} vs {{}-{}}", thisRoundData.round, "--", race.gangDataA.gangId, race.gangDataA.gangData.gangName, race.gangDataB.gangId,
				race.gangDataB.gangData.gangName);
		//
		thisRoundData.initData(Collections.<GangRace> emptyList(), Collections.<GangRace> emptyList(), race);
	}

	/**
	 * <pre>
	 * 发送最终奖励
	 * 	军团战结束后，会将团战勋章赋予军团战结束时的前4强军团的【团长】与【副团长】
	 * 	颁发军团有：1个冠军军团，1个亚军军团，2个季军军团
	 * 	亚军为争夺冠军失败的那个军团，季军为争夺亚军失败的两个军团
	 * 
	 * 	对应的玩家获得勋章奖励时，会弹出获得勋章奖励界面
	 * 	若对应玩家不在线时，则会在其上线后打开军团功能界面时再弹出：
	 * 
	 * 	每次军团报名结束时，系统会自动回收上一届军团战所发放的军团勋章
	 * 	在勋章回收时，系统会发送文件给被回收的玩家:
	 * 	邮件标题：勋章回收通知
	 * 	邮件内容：新一届的军团争霸已经开始，您在上届军团战中所获得的【XX勋章】已被回收，请在本届军团战中再接再厉、再创辉煌！
	 * 	获得勋章的成员，进行职位禅让、解除职位，其勋章保留
	 * 	军团成员离开军团、军团解散时，系统回收拥有的勋章
	 * 
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-29 下午6:51:13
	 * </pre>
	 */
	static void sendFinallyRewardToAllWarGangs() {

		// 将所有军团进行排名
		List<GangData> allGangs = sortAllGangs();

		GangData NO1 = allGangs.get(0);
		GangData NO2 = allGangs.get(1);
		GangData NO3_1 = allGangs.get(2);
		GangData NO3_2 = allGangs.get(3);

		sendFinallyReward(NO1, 1);
		sendFinallyReward(NO2, 2);
		sendFinallyReward(NO3_1, 3);
		sendFinallyReward(NO3_2, 3);

		// 系统通知
		GangWarSystemBrocast.onWarEnd_Win(NO1);
	}

	private static void sendFinallyReward(GangData tempWarGang, int rank) {
		if (tempWarGang == null || tempWarGang.gangId < 0) {
			return;
		}
		//
		GangMedalData medal = KGangWarDataManager.mGangMedalDataManager.getDataByRank(rank);
		KGang gang = KGangLogic.sendGangWarFinalReward(tempWarGang.gangId, medal);
		if (gang == null) {
			GangWarLogic.GangWarLogger.error("警告：军团战排行{}，军团ID={}，军团名称={}，发送最终奖励时找不到此军团！", GangWarLogic.getTitle(rank), tempWarGang.gangId, tempWarGang.gangName);
			return;
		}
	}

	static void saveData(String tips) {

		String saveFileName = GangWarDataCenter.saveFileName + tips;

		String url = saveDirPath + saveFileName + saveFileNameSuffix;
		KXmlWriter writer = null;
		try {
			File file = new File(saveDirPath);
			file.mkdirs();

			writer = new KXmlWriter(url, true);

			Element root = writer.getRoot();
			root.setAttribute("保存时间", UtilTool.DATE_FORMAT.format(new Date()));
			root = new Element("停服缓存数据");
			writer.addElement(root);

			{
				GangWarStatusEnum status = GangWarStatusManager.getNowStatus();
				Element elementA = new Element("当前状态");
				root.addContent(elementA);
				elementA.setAttribute("id", status.sign + "");
				elementA.setAttribute("name", status.name);
				//
				status = GangWarStatusTask.getNextWarStatusEnum();
				elementA = new Element("下一状态");
				root.addContent(elementA);
				elementA.setAttribute("id", status == null ? "0" : (status.sign + ""));
				elementA.setAttribute("name", status == null ? "0" : (status.name));
			}
			{
				Element elementA = new Element("入围军团数据");
				root.addContent(elementA);
				for (GangData mWarGang : allWarGangsList) {
					Element elementB = new Element("军团");
					elementA.addContent(elementB);
					//
					elementB.setAttribute("军团ID", mWarGang.gangId + "");
					elementB.setAttribute("军团名称", mWarGang.gangName + "");
					elementB.setAttribute("军团等级", mWarGang.gangLv + "");
					elementB.setAttribute("入围战力", mWarGang.getBattlePower() + "");
				}
			}
			{
				Element elementA = new Element("各场数据");
				root.addContent(elementA);
				elementA.setAttribute("当前场次", nowRound + "");

				for (int round = 1; round <= KGangWarConfig.getInstance().MaxRound; round++) {

					GangWarRound warRound = allWarRoundsMap.get(round);
					if (warRound == null) {
						continue;
					}

					Element elementB = new Element("第" + round + "场");
					elementA.addContent(elementB);
					elementB.setAttribute("是否已结束", Boolean.toString(warRound.isEnd()));

					if(round < KGangWarConfig.getInstance().MaxRound){
						{
							Element groupE = new Element("A组");
							elementB.addContent(groupE);
							List<GangRace> groupList = warRound.getUnmodifyRaceList(true);
							saveRaces(groupE, groupList);
						}

						{
							Element groupE = new Element("B组");
							elementB.addContent(groupE);
							List<GangRace> groupList = warRound.getUnmodifyRaceList(false);
							saveRaces(groupE, groupList);
						}
					} else {
						Element groupE = new Element("总决赛");
						elementB.addContent(groupE);
						List<GangRace> groupList = warRound.getUnmodifyRaceList();
						saveRaces(groupE, groupList);
					}
				}
			}
			writer.output();
			// COPY一份备份
			UtilTool.copyFile(url, url + ".bak");

			GangWarLogic.GangWarLogger.error("保存成功！路径={}", url);

		} catch (Exception e) {
			GangWarLogic.GangWarLogger.error("保存出错！路径={}", url);
			GangWarLogic.GangWarLogger.error(e.getMessage(), e);
			return;
		}
	}

	private static void saveRaces(Element groupE, List<GangRace> groupList) {
		for (GangRace race : groupList) {
			Element elementC = new Element("对战");
			elementC.setAttribute("平均等级", race.avgRoleLv + "");
			elementC.setAttribute("胜出军团ID", race.getWinner() == null ? "0" : race.getWinner().gangId + "");
			groupE.addContent(elementC);

			{
				Element elementD = new Element("A军团");
				elementC.addContent(elementD);
				elementD.setAttribute("gangId", race.gangDataA.gangId + "");
				elementD.setAttribute("积分", race.gangDataA.getScore() + "");
			}
			{
				Element elementD = new Element("B军团");
				elementC.addContent(elementD);
				elementD.setAttribute("gangId", race.gangDataB.gangId + "");
				elementD.setAttribute("积分", race.gangDataB.getScore() + "");
			}
		}
	}

	/**
	 * <pre>
	 * 开服加载数据
	 * 
	 * @param cacheData
	 * @author CamusHuang
	 * @creation 2013-10-30 上午10:29:57
	 * </pre>
	 */
	static void resetFromCacheData(CacheData cacheData) {

		nowRound = cacheData.nowRound;
		allWarGangsList.addAll(cacheData.allWarGangsList);
		allWarGangsMap.putAll(cacheData.allWarGangsMap);
		//
		allWarRoundsMap.putAll(cacheData.allWarRoundsMap);
	}

	static CacheData loadData() {

		CacheData cacheData = null;
		try {

			String url = saveDirPath + saveFileName + saveFileNameSuffix;
			File file = new File(url);
			if (file.exists()) {
				if (file.isDirectory()) {
					throw new Exception("文件路径不能是目录！path=" + url);
				}
			} else {
				GangWarLogic.GangWarLogger.warn("不存在数据文件！");
				return cacheData;
			}
			Document doc = XmlUtil.openXml(file);
			Element root = doc.getRootElement();

			cacheData = new CacheData(root.getChild("停服缓存数据"));

		} catch (Exception e) {
			GangWarLogic.GangWarLogger.error(e.getMessage(), e);
		}

		return cacheData;
	}

	/**
	 * <pre>
	 * 排序所有军团
	 * 
	 * @deprecated 只能在第五场结束后调用
	 * @return
	 * @author CamusHuang
	 * @creation 2014-6-16 下午8:01:05
	 * </pre>
	 */
	private static List<GangData> sortAllGangs() {
		List<GangData> allGangs = new ArrayList<GangData>();
		Set<Long> ids = new HashSet<Long>();

		for (int roundId = KGangWarConfig.getInstance().MaxRound; roundId > 0; roundId--) {
			GangWarRound round = allWarRoundsMap.get(roundId);
			for (GangRace race : round.getUnmodifyRaceList()) {
				RaceGangData data = race.getWinner();
				if (ids.add(data.gangId)) {
					allGangs.add(data.gangData);
				}
			}
			for (GangRace race : round.getUnmodifyRaceList()) {
				RaceGangData data = race.getWinner();
				data = race.getRaceGang(data.gangId);
				if (ids.add(data.gangId)) {
					allGangs.add(data.gangData);
				}
			}
		}

		// 记录流水
		List<String> names = new ArrayList<String>();
		for (GangData data : allGangs) {
			names.add(data.gangName);
		}
		FlowDataModuleFactory.getModule().recordFamilyWarRank(0, names);

		return allGangs;
	}

	/**
	 * <pre>
	 * 从外部文件加载的数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-10-29 下午12:16:16
	 * </pre>
	 */
	static class CacheData {
		GangWarStatusEnum nowStatus;
		GangWarStatusEnum nextStatus;
		/* 当前是第几场 */
		int nowRound = 1;

		ArrayList<GangData> allWarGangsList = new ArrayList<GangData>();
		Map<Long, GangData> allWarGangsMap = new HashMap<Long, GangData>();
		/* key=场次1，2，3 */
		Map<Integer, GangWarRound> allWarRoundsMap = new HashMap<Integer, GangWarRound>();

		CacheData(Element root) {
			{
				nowStatus = GangWarStatusEnum.getEnum(Byte.parseByte(root.getChild("当前状态").getAttributeValue("id")));
				nextStatus = GangWarStatusEnum.getEnum(Byte.parseByte(root.getChild("下一状态").getAttributeValue("id")));
			}
			{
				List<Element> tempEs = root.getChild("入围军团数据").getChildren("军团");
				for (Element temp : tempEs) {
					long gangId = Long.parseLong(temp.getAttributeValue("军团ID"));
					String gangName = temp.getAttributeValue("军团名称");
					int gangLv = Integer.parseInt(temp.getAttributeValue("军团等级"));
					int battlePow = Integer.parseInt(temp.getAttributeValue("入围战力"));

					GangData mWarGang = new GangData(gangId, gangLv, gangName);
					mWarGang.setBattlePower(battlePow);
					allWarGangsList.add(mWarGang);
					allWarGangsMap.put(mWarGang.gangId, mWarGang);
				}
			}
			{
				Element tempE = root.getChild("各场数据");
				nowRound = Integer.parseInt(tempE.getAttributeValue("当前场次"));

				for (int round = 1; round <= KGangWarConfig.getInstance().MaxRound; round++) {

					Element tempERound = tempE.getChild("第" + round + "场");
					if (tempERound == null) {
						continue;
					}

					GangWarRound warRound = new GangWarRound(round);
					boolean isEnd = Boolean.parseBoolean(tempERound.getAttributeValue("是否已结束"));
					
					List<GangRace> alist = Collections.emptyList();
					List<GangRace> blist = Collections.emptyList();
					List<GangRace> clist = Collections.emptyList();
					if(round < KGangWarConfig.getInstance().MaxRound){

						Element groupA = tempERound.getChild("A组");
						alist = readGroup(groupA, round, true, isEnd);
	
						Element groupB = tempERound.getChild("B组");
						blist = readGroup(groupB, round, false, isEnd);
					} else {

						Element groupC = tempERound.getChild("总决赛");
						clist = readGroup(groupC, round, true, isEnd);
					}

					warRound.resetData(isEnd, alist, blist, clist.isEmpty() ? null : clist.get(0));
					allWarRoundsMap.put(round, warRound);
				}
			}
		}

		private List<GangRace> readGroup(Element group, int roundId, boolean isGroupA, boolean isRoundEnd) {
			List<GangRace> groupList = new ArrayList<GangRace>();
			{
				List<Element> tempRaceList = group.getChildren("对战");
				for (Element raceE : tempRaceList) {
					RaceGangData warGangA, warGangB, winGangData;

					int avgRoleLv = Integer.parseInt(raceE.getAttributeValue("平均等级"));
					long winGangId = Long.parseLong(raceE.getAttributeValue("胜出军团ID"));

					{
						Element groupFE = raceE.getChild("A军团");
						long gangId = Long.parseLong(groupFE.getAttributeValue("gangId"));
						int score = Integer.parseInt(groupFE.getAttributeValue("积分"));
						warGangA = new RaceGangData(allWarGangsMap.get(gangId));
						warGangA.setScore(score);
					}
					{
						Element groupFE = raceE.getChild("B军团");
						long gangId = Long.parseLong(groupFE.getAttributeValue("gangId"));
						int score = Integer.parseInt(groupFE.getAttributeValue("积分"));
						warGangB = new RaceGangData(allWarGangsMap.get(gangId));
						warGangB.setScore(score);
					}

					if (isRoundEnd) {
						if (winGangId == warGangA.gangId) {
							winGangData = warGangA;
						} else if (winGangId == warGangB.gangId) {
							winGangData = warGangB;
						} else {
							winGangData = warGangA;
						}
					} else {
						winGangData = null;
					}

					GangRace race = new GangRace(roundId, isGroupA, warGangA, warGangB, winGangId, avgRoleLv);
					groupList.add(race);
				}
			}
			return groupList;
		}
	}

}
