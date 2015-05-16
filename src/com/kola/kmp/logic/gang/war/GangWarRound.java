package com.kola.kmp.logic.gang.war;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.timer.Timer;

import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.FlowDataModule.FamilyWarCounterType;
import com.kola.kgame.cache.flowdata.FlowDataModuleFactory;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.gang.GangIntegrateData;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangExtCASet;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.gang.KGangModuleExtension;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace.RaceGangData;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace.RaceGangData.RaceMemberData;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace.RaceGangData.RaceMemberData.MemStatusEnum;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace.RaceScoreRank.BPElement;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangWarRewardDataManager.WarRankRewardData;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangWarRewardRatioDataManager.RewardRatioData;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KRoleScoreRankRewardManager.ScoreRankReward;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GangWarTips;
import com.kola.kmp.logic.util.tips.RewardTips;

/**
 * <pre>
 * 第几场
 * 
 * @author CamusHuang
 * @creation 2013-9-17 下午7:34:39
 * </pre>
 */
public class GangWarRound {
	final int round;

	// 本场战斗是否结束
	private boolean isEnd;

	/** A组:1,2,3,4场有数据 */
	/* key=军团ID 本组所有对战 */
	private final Map<Long, GangRace> groupARacesMap = new HashMap<Long, GangRace>();
	private final List<GangRace> groupARacesList = new ArrayList<GangRace>();

	/** B组:1,2,3,4场有数据 */
	/* key=军团ID 本组所有对战 */
	private final Map<Long, GangRace> groupBRacesMap = new HashMap<Long, GangRace>();
	private final List<GangRace> groupBRacesList = new ArrayList<GangRace>();

	/** 决赛:5场有数据 */
	private GangRace finalRace;

	/** 全部对战 */
	private List<GangRace> totalRacesList = new ArrayList<GangRace>();
	/* key=军团ID 所有对战 */
	private Map<Long, GangRace> totalRacesMap = new HashMap<Long, GangRace>();

	GangWarRound(int round) {
		this.round = round;
	}

	/**
	 * <pre>
	 * 重设本场分组数据
	 * 
	 * @param groupList
	 * @author CamusHuang
	 * @creation 2013-9-25 下午2:57:10
	 * </pre>
	 */
	void initData(List<GangRace> groupAList, List<GangRace> groupBList, GangRace finalRace) {
		resetData(false, groupAList, groupBList, finalRace);
	}

	/**
	 * <pre>
	 * 
	 * @param isEnd
	 * @param groupList
	 * @author CamusHuang
	 * @creation 2013-10-29 下午3:57:36
	 * </pre>
	 */
	void resetData(boolean isEnd, List<GangRace> groupAList, List<GangRace> groupBList, GangRace finalRace) {
		this.isEnd = isEnd;
		{
			this.groupARacesList.clear();
			this.groupARacesList.addAll(groupAList);
			//
			this.groupARacesMap.clear();
			for (GangRace tempRace : groupAList) {
				this.groupARacesMap.put(tempRace.gangDataA.gangId, tempRace);
				this.groupARacesMap.put(tempRace.gangDataB.gangId, tempRace);
				// 生成地图，分配游戏主场景，初始化BOSS
				tempRace.MapAndPKCenter.initMapAndBoss(tempRace);
			}
		}
		{
			this.groupBRacesList.clear();
			this.groupBRacesList.addAll(groupBList);
			//
			this.groupBRacesMap.clear();
			for (GangRace tempRace : groupBList) {
				this.groupBRacesMap.put(tempRace.gangDataA.gangId, tempRace);
				this.groupBRacesMap.put(tempRace.gangDataB.gangId, tempRace);
				// 生成地图，分配游戏主场景
				tempRace.MapAndPKCenter.initMapAndBoss(tempRace);
			}

		}
		if (finalRace != null) {
			this.finalRace = finalRace;
			this.finalRace.MapAndPKCenter.initMapAndBoss(this.finalRace);
		}

		{
			totalRacesList.addAll(groupAList);
			totalRacesList.addAll(groupBList);
			if (finalRace != null) {
				totalRacesList.add(finalRace);
			}
			for (GangRace tempRace : totalRacesList) {
				this.totalRacesMap.put(tempRace.gangDataA.gangId, tempRace);
				this.totalRacesMap.put(tempRace.gangDataB.gangId, tempRace);
			}
		}
	}

	// List<GangRace> getUnmodifyRaceList(boolean isGroupA) {
	// if (isGroupA) {
	// return Collections.unmodifiableList(groupARacesList);
	// } else {
	// return Collections.unmodifiableList(groupBRacesList);
	// }
	// }

	List<GangRace> getUnmodifyRaceList() {
		return Collections.unmodifiableList(totalRacesList);
	}

	List<GangRace> getUnmodifyRaceList(boolean isGroupA) {
		if (isGroupA) {
			return Collections.unmodifiableList(groupARacesList);
		} else {
			return Collections.unmodifiableList(groupBRacesList);
		}
	}

	/**
	 * <pre>
	 * 搜索所有胜出的军团
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-21 上午10:13:27
	 * </pre>
	 */
	List<GangData> searchAllWinGangDatas() {
		List<GangData> list = new ArrayList<GangData>();
		for (GangRace race : totalRacesList) {
			RaceGangData data = race.getWinner();
			if (data != null) {
				list.add(data.gangData);
			}
		}
		return list;
	}
	
	/**
	 * <pre>
	 * 搜索所有胜出的军团
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-21 上午10:13:27
	 * </pre>
	 */
	List<GangData> searchAllWinGangDatas(boolean isGroupA) {
		List<GangRace> tempRacesList = isGroupA ? groupARacesList : groupBRacesList;
		
		List<GangData> list = new ArrayList<GangData>();
		for (GangRace race : tempRacesList) {
			RaceGangData data = race.getWinner();
			if (data != null) {
				list.add(data.gangData);
			}
		}
		return list;
	}	

	// GangRace getRace(boolean isGroupA, long gangId) {
	// if (isGroupA) {
	// return groupARacesMap.get(gangId);
	// } else {
	// return groupBRacesMap.get(gangId);
	// }
	// }

	GangRace getRaceByGangId(long gangId) {
		return totalRacesMap.get(gangId);
	}

//	GangRace getRaceById(boolean isGroupA, int raceId) {
//		if (finalRace != null && finalRace.raceId == raceId) {
//			return finalRace;
//		}
//		List<GangRace> list = null;
//		if (isGroupA) {
//			list = groupARacesList;
//		} else {
//			list = groupBRacesList;
//		}
//		for (GangRace race : list) {
//			if (race.raceId == raceId) {
//				return race;
//			}
//		}
//		return null;
//	}

	void setEnd() {
		isEnd = true;
	}

	boolean isEnd() {
		return isEnd;
	}

	/**
	 * <pre>
	 * 对战中，执行裁判
	 * 
	 * @param round
	 * @return 是否所有分组均已决出胜负
	 * @author CamusHuang
	 * @creation 2013-8-30 下午3:46:09
	 * </pre>
	 */
	boolean judgeInRound() {

		boolean isAllGroupEnd = true;// 是否所有分组均已决出胜负
		
		// 遍历所有分组
		for (GangRace race : totalRacesList) {
			if (race.getWinner() != null) {
				// 已决出胜负，忽略
				continue;
			} else {
				isAllGroupEnd = false;
				// 裁决
				if (race.judgeInRound()) {
					clearForRaceEnd(race);
				}
			}
		}

		return isAllGroupEnd;
	}

	/**
	 * <pre>
	 * 对战结束时，执行裁判
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-30 下午3:46:09
	 * </pre>
	 */
	void judgeForRoundEnd() {
		int totalRole = 0;
		int effectRaceNum = 0;
		// 遍历所有分组
		for (GangRace race : totalRacesList) {

			if (race.gangDataA.gangId > 0 || race.gangDataB.gangId > 0) {
				totalRole += race.gangDataA.getAllMems().size();
				totalRole += race.gangDataB.getAllMems().size();
				effectRaceNum++;
			}

			if (race.judgeForRoundEnd()) {
				clearForRaceEnd(race);
			}
		}

		// 此场结束
		setEnd();

		// 记录流水
		FlowDataModuleFactory.getModule().recordFamilyWarCounter(round, FamilyWarCounterType.getEnum(round), totalRole/effectRaceNum);
	}
	
	private void clearForRaceEnd(GangRace race){
		GangWarSystemBrocast.onRaceEnd(round, race);
		// 将场景中的所有成员转到主城
		GangWarLogic.clearMemsAfterJudge(round, race, race.gangDataA);
		GangWarLogic.clearMemsAfterJudge(round, race, race.gangDataB);
		// 移除地图
		KSupportFactory.getDuplicateMapSupport().removeDuplicateMap(race.MapAndPKCenter.getWarMap().getDuplicateId());
	}

	/**
	 * <pre>
	 * 发放单场奖励
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-29 下午6:51:13
	 * </pre>
	 */
	void sendRoundReward() {
		// 遍历所有分组
		for (GangRace group : totalRacesList) {
			group.sendRoundReward(round);
		}
	}

	void clearData() {
		groupARacesList.clear();
		groupARacesMap.clear();
		groupBRacesList.clear();
		groupBRacesMap.clear();
		finalRace = null;
		totalRacesMap.clear();
		for (GangRace race : totalRacesList) {
			race.clearData();
		}
		totalRacesList.clear();
	}

	/**
	 * <pre>
	 * 一场军团对战比赛
	 * 
	 * @author CamusHuang
	 * @creation 2013-9-17 下午7:27:02
	 * </pre>
	 */
	public static class GangRace {
		public final int roundId;
		public final boolean isGroupA;
		//
		final GangWarRaceMapAndPKCenter MapAndPKCenter = new GangWarRaceMapAndPKCenter();// 场景地图和PK中心
		//
		public final RaceGangData gangDataA;// 一定不为NULL
		public final RaceGangData gangDataB;// 一定不为NULL
		public final int avgRoleLv;// 双方参战军团所有成员中等级前10名玩家的平均等级
		private AtomicLong winerId = new AtomicLong(0);// 胜出者
		//
		final RaceScoreRank scoreRank = new RaceScoreRank();// 积分榜
		//
		final RaceScoreRank keepKillRank = new RaceScoreRank();// 最高连杀榜

		GangRace(int roundId, boolean isGroupA, GangData warGangA, GangData warGangB) {
			this.roundId = roundId;
			this.isGroupA = isGroupA;
			//
			this.gangDataA = new RaceGangData(warGangA);
			this.gangDataB = new RaceGangData(warGangB);
			//
			this.avgRoleLv = GangWarLogic.avgGangMemRoleLv(warGangA.gangId, warGangB.gangId);
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 仅供开服加载数据使用
		 * @param warGangA
		 * @param warGangB
		 * @param winGangData
		 * @author CamusHuang
		 * @creation 2013-10-29 下午4:02:52
		 * </pre>
		 */
		GangRace(int roundId, boolean isGroupA, RaceGangData warGangA, RaceGangData warGangB, long winerId, int avgRoleLv) {
			this.roundId = roundId;
			this.isGroupA = isGroupA;
			//
			this.gangDataA = warGangA;
			this.gangDataB = warGangB;
			//
			this.winerId.set(winerId);
			this.avgRoleLv = avgRoleLv;
		}

		RaceGangData getRaceGang(long gangId) {
			if (gangDataA.gangId == gangId) {
				return gangDataA;
			}
			return gangDataB;
		}

		RaceGangData getOppRaceGang(long gangId) {
			if (gangDataA.gangId == gangId) {
				return gangDataB;
			}
			return this.gangDataA;
		}

		private boolean setWinner(long winerId) {
			return this.winerId.compareAndSet(0, winerId);
		}

		RaceGangData getWinner() {
			if(winerId.get()==0){
				return null;
			}
			return getRaceGang(winerId.get());
		}

		RaceGangData getLoser() {
			if(winerId.get()==0){
				return null;
			}
			return getOppRaceGang(winerId.get());
		}

		NotifyPKResult notifyPVPResult(long gangId, long roleId, long oppRoleId, boolean isWin, int addScore) {

			NotifyPKResult notifyResult  = new NotifyPKResult();
			
			if (winerId.get() > 0) {
				// 已决出胜负，忽略
				return notifyResult;
			}

			RaceGangData gangData = getRaceGang(gangId);
			if (gangData == null) {
				return notifyResult;
			}

			notifyResult.isSuccess = gangData.notifyPVPResult(roleId, oppRoleId, isWin, addScore);
			if (notifyResult.isSuccess) {
				notifyRaceRankForPKResult(gangData, roleId, isWin, addScore, notifyResult);
			}
			return notifyResult;
		}

		NotifyPKResult notifyPVEResult(long gangId, long roleId, boolean isWin, int addScore) {
			NotifyPKResult notifyResult  = new NotifyPKResult();
			
			if (winerId.get() > 0) {
				// 已决出胜负，忽略
				return notifyResult;
			}
			RaceGangData gangData = getRaceGang(gangId);
			if (gangData == null) {
				return notifyResult;
			}

			notifyResult.isSuccess = gangData.notifyPVEResult(roleId, isWin, addScore);
			if (notifyResult.isSuccess) {
				notifyRaceRankForPKResult(gangData, roleId, isWin, addScore, notifyResult);
			}
			return notifyResult;
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @param gangData
		 * @param roleId
		 * @param isWin
		 * @param addScore
		 * @return []{积分榜前三名是否发生变化,连杀榜前三名是否发生变化}
		 * @author CamusHuang
		 * @creation 2014-11-6 下午11:46:11
		 * </pre>
		 */
		private void notifyRaceRankForPKResult(RaceGangData gangData, long roleId, boolean isWin, int addScore, NotifyPKResult notifyResult) {
			RaceMemberData mem = gangData.getMem(roleId);
			if (mem != null) {
				// 通知积分榜
				if (addScore > 0) {
					notifyResult.isScoreRankChange=scoreRank.notifyChange(mem, gangData.gangData.gangName, mem.score);
				}
				// 通知连杀榜
				if (isWin) {
					notifyResult.isKillRankChange=keepKillRank.notifyChange(mem, gangData.gangData.gangName, mem.maxWinCount);
				}
			}
		}

		/**
		 * <pre>
		 * 对战结束时，执行裁判
		 * 
		 * 			 --胜负判断规则：
		 * 			 ----某方BOSS先死则另一方方胜出
		 * 			 ----军团战结束时，积分高的胜出；积分相同时，BOSS血量高的胜出；BOSS血量相同时，A方胜出
		 * 
		 * @author CamusHuang
		 * @creation 2013-8-30 下午3:46:09
		 * </pre>
		 */
		private boolean judgeForRoundEnd() {

			if (winerId.get() > 0) {
				// 已决出胜负，忽略
				return false;
			}

			// 未有裁决，执行裁决
			if (gangDataB.gangId < 0) {
				// 轮空
				setWinner(gangDataA.gangId);
				GangWarLogic.GangWarLogger.warn("军团战：{} 单场结束胜负 军团（{}-{}，积分{}，boss{}），轮空，胜出）", isGroupA ? "A组" : "B组", gangDataA.gangId, gangDataA.gangData.gangName, gangDataA.getScore(), MapAndPKCenter.getBossData().bossTemp1.name);
				return true;
			} else {
				if (gangDataA.getScore() > gangDataB.getScore()) {
					setWinner(gangDataA.gangId);
				} else if (gangDataA.getScore() < gangDataB.getScore()) {
					setWinner(gangDataB.gangId);
				} else {
					// BOSS血量高的胜出
					long hpA=MapAndPKCenter.getRacePVEBoss(gangDataA.gangId).getCurrentHp();
					long hpB=MapAndPKCenter.getRacePVEBoss(gangDataA.gangId).getCurrentHp();
					if(hpA>=hpB){
						// BOSS血量相同时默认左边为胜
						setWinner(gangDataA.gangId);
					} else {
						setWinner(gangDataB.gangId);
					}
				}

				GangWarLogic.GangWarLogger.warn("军团战：{} 单场结束胜负 军团（{}-{}，积分{}，boss{}），军团（{}-{}，积分{}，boss{}）， 军团（{}-{}胜出）", isGroupA ? "A组" : "B组", gangDataA.gangId, gangDataA.gangData.gangName, gangDataA.getScore(),
						MapAndPKCenter.getBossData().bossTemp1.name, gangDataB.gangId, gangDataB.gangData.gangName, gangDataB.getScore(), MapAndPKCenter.getBossData().bossTemp2.name, winerId,
						getWinner().gangData.gangName);
				return true;
			}
		}

		/**
		 * <pre>
		 * 对战中，执行裁判
		 * 			 --胜负判断规则：
		 * 			 ----某方BOSS先死则另一方方胜出
		 * 			 ----军团战结束时，积分高的胜出；积分相同时，BOSS血量高的胜出；BOSS血量相同时，A方胜出
		 * 
		 * @param group
		 * @author CamusHuang
		 * @creation 2013-8-31 上午11:18:01
		 * </pre>
		 */
		private boolean judgeInRound() {
			if (winerId.get() > 0) {
				// 胜负已分
				return false;
			}

			// 未有裁决，执行裁决
			if (gangDataB.gangId < 0) {
				// 轮空
				setWinner(gangDataA.gangId);
				GangWarLogic.GangWarLogger.warn("军团战：{} 单场结束胜负 军团（{}-{}，积分{}，boss{}），轮空，胜出）", isGroupA ? "A组" : "B组", gangDataA.gangId, gangDataA.gangData.gangName, gangDataA.getScore(), MapAndPKCenter.getBossData().bossTemp1.name);
				return true;
			} else {
				// 某方BOSS先死则另一方方胜出
				
				if(!MapAndPKCenter.getRacePVEBoss(gangDataB.gangId).isAlive()){
					//B方BOSS死掉，A方赢
					setWinner(gangDataA.gangId);
				} else if(!MapAndPKCenter.getRacePVEBoss(gangDataA.gangId).isAlive()){
					setWinner(gangDataB.gangId);
				}

				if (winerId.get() > 0) {
					GangWarLogic.GangWarLogger.warn("军团战：{} 单场中途胜负 军团（{}-{}，积分{}，boss{}），军团（{}-{}，积分{}，boss{}）， 军团（{}-{}胜出）", isGroupA ? "A组" : "B组", gangDataA.gangId, gangDataA.gangData.gangName, gangDataA.getScore(),
							MapAndPKCenter.getBossData().bossTemp1.name, gangDataB.gangId, gangDataB.gangData.gangName, gangDataB.getScore(), MapAndPKCenter.getBossData().bossTemp2.name, winerId,
							getWinner().gangData.gangName);
					// 胜负已分
					return true;
				}
				return false;
			}
		}

		/**
		 * <pre>
		 * 发放单场奖励
		 * 
		 * @author CamusHuang
		 * @creation 2014-5-20 下午7:27:42
		 * </pre>
		 */
		void sendRoundReward(int round) {
			// 发放积分榜奖励
			{
				// 军团内积分排名前10的玩家无论本次军团战输赢都会在军团战结束后获得积分排名奖励
				List<BPElement> list = scoreRank.getCopyElementList();
				for (BPElement e:list) {
					ScoreRankReward reward = KGangWarDataManager.mRoleScoreRankRewardManager.getData(e.rank);
					if(reward != null) {
						String title = StringUtil.format(GangWarTips.军团战第x场积分排名奖励, round);
						String content = StringUtil.format(GangWarTips.恭喜您在本场军团战中获得x积分排名x获得额外的积分排名奖励, e.score, e.rank);
						KSupportFactory.getMailModuleSupport().sendAttMailBySystem(e.elementId, reward.addItem, title, content);
					}
				}
			}
			// 发放军团和成员奖励
			{
				RaceGangData winFD = getWinner();
				winFD.sendRoundRewardToAllMems(round, true);// 胜出军团

				RaceGangData loseFD = getLoser();
				if (loseFD != null) {
					loseFD.sendRoundRewardToAllMems(round, false);// 战败军团
				}
			}
		}

		private void clearData() {
			scoreRank.clear();
			keepKillRank.clear();
			gangDataA.clearData();
			gangDataB.clearData();
			MapAndPKCenter.clearData();
		}
		
		static class NotifyPKResult{
			boolean isScoreRankChange = false;
			boolean isKillRankChange = false;
			boolean isSuccess = false;
		}

		/**
		 * <pre>
		 * 对战场景内的某个军团的数据
		 * 即某军团在此对战内的数据
		 * 
		 * @author CamusHuang
		 * @creation 2013-9-25 下午3:45:15
		 * </pre>
		 */
		static class RaceGangData {
			public final long gangId;
			public final GangData gangData;
			// -------当场对战的数据
			private int score;// 积分
			// 所有参战的成员，key=角色ID
			private Map<Long, RaceMemberData> allMembersMap = new HashMap<Long, RaceMemberData>();

			RaceGangData(GangData gangData) {
				this.gangId = gangData.gangId;
				this.gangData = gangData;
			}

			RaceMemberData getMemOrNew(long roleId) {
				RaceMemberData mem = allMembersMap.get(roleId);
				if (mem == null) {
					KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
					mem = new RaceMemberData(roleId, role.getName(), role.getJob(), gangId);
					allMembersMap.put(roleId, mem);
				}
				return mem;
			}

			Collection<RaceMemberData> getAllMems() {
				return allMembersMap.values();
			}

			RaceMemberData getMem(long roleId) {
				return allMembersMap.get(roleId);
			}

			/**
			 * <pre>
			 * 设置成员状态
			 * 
			 * @param roleId
			 * @param newStatus
			 * @author CamusHuang
			 * @creation 2013-9-24 下午5:37:54
			 * </pre>
			 */
			void setMemStatus(long roleId, MemStatusEnum newStatus) {
				RaceMemberData mem = getMem(roleId);
				if (mem != null) {
					mem.setStatus(newStatus);
					GangWarLogic.GangWarLogger.info("角色="+roleId+" 状态-》"+newStatus.name);
				}
			}

			MemStatusEnum getMemStatus(long roleId) {
				RaceMemberData mem = getMem(roleId);
				if (mem == null) {
					return MemStatusEnum.OUT;
				}
				return mem.getStatus();
			}

			/**
			 * <pre>
			 * 统计在军团战主场及战斗中的人数
			 * 
			 * @return
			 * @author CamusHuang
			 * @creation 2013-8-30 下午4:28:13
			 * </pre>
			 */
			int countMemsInRace() {
				int count = 0;
				for (RaceMemberData mem : allMembersMap.values()) {
					if (mem.getStatus() == MemStatusEnum.OUT) {
						continue;
					}
					count++;
				}
				return count;
			}

			/**
			 * <pre>
			 * 
			 * @return
			 * @author CamusHuang
			 * @creation 2013-8-30 上午10:51:48
			 * </pre>
			 */
			int getScore() {
				return score;
			}

			void setScore(int score) {
				this.score = score;
			}

			/**
			 * <pre>
			 * 搜索复活超时的玩家
			 * 
			 * @param wf
			 * @author CamusHuang
			 * @creation 2013-9-24 下午8:45:57
			 * </pre>
			 */
			List<Long> searchReviveOutTimeMems() {
				List<Long> list = new ArrayList<Long>();
				long nowTime = System.currentTimeMillis();
				for (RaceMemberData mem : allMembersMap.values()) {
					if (nowTime >= mem.getReviveTime()) {
						list.add(mem.roleId);
					}
				}
				return list;
			}

			long getReviveTime(long roleId) {
				RaceMemberData mem = getMem(roleId);
				if (mem == null) {
					return 0;
				}
				return mem.getReviveTime();
			}

			boolean notifyPVPResult(long roleId, long oppRoleId, boolean isWin, int addScore) {
				RaceMemberData mem = getMem(roleId);
				if (mem == null) {
					return false;
				}

				boolean isSuccess = mem.notifyPVPResult(isWin, oppRoleId, addScore);
				if (isSuccess) {
					score += addScore;
				}
				return isSuccess;
			}

			boolean notifyPVEResult(long roleId, boolean isWin, int addScore) {
				RaceMemberData mem = getMem(roleId);
				if (mem == null) {
					return false;
				}

				boolean isSuccess = mem.notifyPVEResult(isWin, addScore);
				if (isSuccess) {
					score += addScore;
				}
				return isSuccess;
			}

			/**
			 * <pre>
			 * 获取指定成员的PVP CD剩余时间
			 * 
			 * @param roleId
			 * @param oppRoleId 对方角色
			 * @return 剩余CD时间，0表示无CD时间
			 * @deprecated 不再使用的方法
			 * @author CamusHuang
			 * @creation 2014-5-19 下午7:49:14
			 * </pre>
			 */
			private long getPVPCDReleaseTime(long roleId, long oppRoleId) {

				RaceMemberData mem = getMem(roleId);
				if (mem == null || oppRoleId != mem.getLastPVPRoleId()) {
					return 0;
				}
				long nowTime = System.currentTimeMillis();
				if (nowTime >= mem.getLastPVPCDEndTime()) {
					mem.clearPVPCD();
					return 0;
				}
				return mem.getLastPVPCDEndTime() - nowTime;
			}

			/**
			 * <pre>
			 * 发放军团和成员奖励
			 * 
			 * @author CamusHuang
			 * @creation 2013-8-29 下午6:51:13
			 * </pre>
			 */
			void sendRoundRewardToAllMems(int round, boolean isWin) {
				GangIntegrateData gangs = KGangModuleExtension.getGangAndSet(gangId);
				if (gangs == null) {
					return;
				}
				KGang gang = (KGang) gangs.getGang();
				KGangExtCASet gangExtSet = (KGangExtCASet) gangs.getGangExtCASet();
				//
				WarRankRewardData reward = KGangWarDataManager.mGangWarRewardDataManager.getData(round, isWin);
				RewardRatioData ratio = KGangWarDataManager.mGangWarRewardRatioDataManager.getData(gangData.gangLv);
				{// 发放军团奖励
					int addExp = (int) (reward.exp * ratio.coefficient);
					int addResource = (int) (reward.GangMoney * ratio.coefficient);
					KSupportFactory.getGangSupport().addGangExp(gangId, addExp, addResource);
					// 军团日志
					String Q = GangWarLogic.getQiangFromRound(round, isWin);
					String dialy = null;
					if (isWin) {
						dialy = StringUtil.format(GangWarTips.军团战第x场成功晋级x军团获得奖励x经验和x资金, round, Q, addExp, addResource);
					} else {
						dialy = StringUtil.format(GangWarTips.军团战第x场止步于x军团获得奖励x经验和x资金, round, Q, addExp, addResource);
					}
					KGangLogic.addDialy(gang, gangExtSet, dialy, true, true, false);
				}
				{// 发放成员奖励
					for (RaceMemberData mem : allMembersMap.values()) {
						KRole role = KSupportFactory.getRoleModuleSupport().getRole(mem.roleId);
						if (role == null) {
							GangWarLogic.GangWarLogger.error("警告：军团战第{}场，军团ID={}，军团名称={}，是否胜出={}，成员角色ID={}，成员积分={}，发送奖励时找不到此军团！", round, gangId, gangData.gangName, isWin, mem.roleId, mem.score);
							continue;
						}

						GangWarLogic.GangWarLogger.warn("单场奖励：军团战第{}场，军团ID={}，军团名称={}，是否胜出={}，成员角色ID={}，成员积分={}", round, gangId, gangData.gangName, isWin, mem.roleId, mem.score);

						// 发出邮件
						KSupportFactory.getMailModuleSupport().sendMoneyMailBySystem(role.getId(), reward.getAddMoneys(ratio.coefficient), PresentPointTypeEnum.军团战, reward.baseMailContent.getMailTitle(), reward.baseMailContent.getMailContent());
						// 浮动提示通知
						KDialogService.sendUprisingDialog(role, RewardTips.系统奖励已发送请查看邮件);
					}
				}
			}

			private void clearData() {
				allMembersMap.clear();
			}

			/**
			 * <pre>
			 * 参与本场军团战的角色数据
			 * 
			 * @author CamusHuang
			 * @creation 2013-7-14 下午2:43:16
			 * </pre>
			 */
			static class RaceMemberData {

				final long roleId;
				final String roleName;
				final byte job;
				private final long gangId;
				//
				private int score;// 我的积分
				private int maxWinCount;// 最高连杀
				private int winCount;// 当前连杀
				//
				private long lastPVPRoleId;// 上次PVP的角色ID
				private long lastPVPCDEndTime;// 上次PVP的CD时间
				//
				private long PVPCDEndTime;// 上次PVP的CD时间（此时间内不接受他人PVP）
				//
				private long reviveTime;// 复活时间
				//
				private long releaseHP;// 剩余血量
				private Map<Long, Long> petReleaseHP = new HashMap<Long, Long>();

				private MemStatusEnum mMemStatusEnum = MemStatusEnum.OUT;// 成员状态

				private RaceMemberData(long roleId, String roleName, byte job, long gangId) {
					this.roleId = roleId;
					this.roleName = roleName;
					this.job = job;
					this.gangId = gangId;
				}

				private void clearPVPCD() {
					lastPVPRoleId = 0;
					lastPVPCDEndTime = 0;
					PVPCDEndTime = 0;
				}

				private boolean notifyPVPResult(boolean isWin, long oppRoleId, int addScore) {

					// 判断角色状态
					if (mMemStatusEnum != MemStatusEnum.WAR) {
						return false;
					}

					long nowTime = System.currentTimeMillis();
					//
					lastPVPRoleId = oppRoleId;
					lastPVPCDEndTime = nowTime + KGangWarConfig.getInstance().LastPVPCDTime;
					//
					PVPCDEndTime = nowTime + KGangWarConfig.getInstance().PVPCDTime;
					//
					score += addScore;

					notifyWarResult(isWin);
					return true;
				}

				private boolean notifyPVEResult(boolean isWin, int addScore) {
					// 判断角色状态
					if (mMemStatusEnum != MemStatusEnum.WAR) {
						return false;
					}
					//
					score += addScore;

					notifyWarResult(isWin);
					return true;
				}

				int getScore() {
					return score;
				}

				private void notifyWarResult(boolean isWin) {
//					mMemStatusEnum = MemStatusEnum.IN;
					if (isWin) {
						winCount++;
						if (winCount > maxWinCount) {
							maxWinCount = winCount;
						}

					} else {
						winCount = 0;
						reviveTime = System.currentTimeMillis() + KGangWarConfig.getInstance().DeadCDTime;
					}
				}

				int getMaxWinCount() {
					return maxWinCount;
				}

				int getWinCount() {
					return winCount;
				}

				long getLastPVPRoleId() {
					return lastPVPRoleId;
				}

				long getLastPVPCDEndTime() {
					return lastPVPCDEndTime;
				}
				
				long getPVPCDEndTime() {
					return PVPCDEndTime;
				}

				private long getReviveTime() {
					return reviveTime;
				}
				
				int getReviveReleaseTime() {
					return (int) (Math.max(0, reviveTime-System.currentTimeMillis())/Timer.ONE_SECOND);
				}

				/**
				 * <pre>
				 * 设置成员状态
				 * 
				 * @param isInWar
				 * @author CamusHuang
				 * @creation 2013-9-20 下午12:08:32
				 * </pre>
				 */
				private void setStatus(MemStatusEnum newStatus) {
					mMemStatusEnum = newStatus;
				}

				MemStatusEnum getStatus() {
					if (mMemStatusEnum == MemStatusEnum.IN) {
						if (System.currentTimeMillis() < reviveTime) {
							// 如果在场景中且处于复活CD中，则返回复活状态
							return MemStatusEnum.REVIVE;
						}
					}
					return mMemStatusEnum;
				}

				void setReleaseHP(long releaseHP, long petId, long petReleaseHP) {
					this.releaseHP = releaseHP;
					if (petId > 0) {
						this.petReleaseHP.put(petId, petReleaseHP);
					}
				}

				long getReleaseHP() {
					return releaseHP;
				}

				long getPetReleaseHP(long petId) {
					Long value = petReleaseHP.get(petId);
					return value == null ? 0 : value;
				}

				/**
				 * <pre>
				 * 军团参战成员的状态
				 * 
				 * @author CamusHuang
				 * @creation 2013-9-24 下午5:30:56
				 * </pre>
				 */
				enum MemStatusEnum {
					IN(1, "主场景中"), OUT(2, "曾经进入主场景，但已离场"), WAR(3, "PVP或PVE中"), REVIVE(4, "复活处理中"), ;

					// 标识数值
					public final byte sign;
					// 名称
					public final String name;

					private MemStatusEnum(int sign, String name) {
						this.sign = (byte) sign;
						this.name = name;
					}
				}

			}
		}

		/**
		 * <pre>
		 * 积分榜
		 * 同时用于连杀榜
		 * 
		 * @author CamusHuang
		 * @creation 2014-5-8 下午12:05:38
		 * </pre>
		 */
		public static class RaceScoreRank {

			// 读写锁
			private final ReentrantLock rwLock = new ReentrantLock();
			//
			private List<BPElement> elementList = new ArrayList<BPElement>();
			private List<BPElement> copyElementList = new ArrayList<BPElement>(elementList);
			private Map<Long, BPElement> elementMap = new HashMap<Long, BPElement>();

			/**
			 * <pre>
			 * 
			 * 
			 * @param data
			 * @param gangName
			 * @param newValue
			 * @return 前三名是否发生变化
			 * @author CamusHuang
			 * @creation 2014-11-6 下午11:45:42
			 * </pre>
			 */
			boolean notifyChange(RaceMemberData data, String gangName, int newValue) {
				rwLock.lock();
				try {
					BPElement element = elementMap.get(data.roleId);
					if (element == null) {
						element = new BPElement(data.roleId, data.roleName, data.job, gangName, newValue);
						elementMap.put(element.elementId, element);
						elementList.add(element);
					} else {
						if (element.score == newValue) {
							return false;
						}
						element.score = newValue;
					}

					// 重排
					return resort();
				} finally {
					rwLock.unlock();
				}
			}

			/**
			 * <pre>
			 * 
			 * @return 前三名是否发生变化
			 * @author CamusHuang
			 * @creation 2014-11-6 下午11:42:51
			 * </pre>
			 */
			private boolean resort() {
				// 重排
				Collections.sort(elementList, RComparator.instance);

				// copy一份，并重新设置名次
				copyElementList = new ArrayList<BPElement>(elementList);
				
				boolean isTopChange = false;
				int rank = 1;
				for (BPElement e : copyElementList) {
					if(e.rank != rank){
						e.rank = rank;
						if(rank<3){
							isTopChange = true;
						}
					}
					rank++;
				}
				return isTopChange;
			}

			BPElement getElement(long gangId) {
				return elementMap.get(gangId);
			}

			List<BPElement> getCopyElementList() {
				return copyElementList;
			}

			void clear() {
				rwLock.lock();
				try {
					elementList.clear();
					copyElementList.clear();
					elementMap.clear();
				} finally {
					rwLock.unlock();
				}
			}

			class BPElement {
				int rank;
				final long elementId;// 角色ID
				final String elementName;// 角色名称
				final byte job;
				final String gangName;// 军团名称
				int score;

				private BPElement(long roleId, String roleName, byte job, String gangName, int price) {
					this.elementId = roleId;
					this.elementName = roleName;
					this.job = job;
					this.gangName = gangName;
					this.score = price;
				}
			}

			static class RComparator implements Comparator<BPElement> {
				static final RComparator instance = new RComparator();

				@Override
				public int compare(BPElement o1, BPElement o2) {
					if (o1.score > o2.score) {
						return -1;
					}
					if (o1.score < o2.score) {
						return 1;
					}
					return 0;
				}
			}
		}
	}
}
