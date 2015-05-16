package com.kola.kmp.logic.gang.war;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.koala.game.KGameMessage;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangDataManager;
import com.kola.kmp.logic.gang.KGangDataStruct.GangLevelData;
import com.kola.kmp.logic.gang.KGangLogic;
import com.kola.kmp.logic.gang.KGangMsgPackCenter;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace.RaceGangData;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace.RaceGangData.RaceMemberData;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace.RaceGangData.RaceMemberData.MemStatusEnum;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace.RaceScoreRank.BPElement;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangMedalDataManager.GangMedalData;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangWarRewardDataManager.WarRankRewardData;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangWarRewardRatioDataManager.RewardRatioData;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.rank.gang.GangRank;
import com.kola.kmp.logic.rank.gang.GangRankElementWarSignUp;
import com.kola.kmp.logic.rank.gang.KGangRankLogic;
import com.kola.kmp.logic.rank.gang.KGangRankTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.GangWarTips;
import com.kola.kmp.protocol.gang.war.KGangWarProtocol;

public class KGangWarMsgPackCenter {

	public static void packConstance(KGameMessage msg, KRole role) {

		msg.writeUtf8String(KGangWarConfig.getInstance().军团战开始时间);
		msg.writeUtf8String(KGangWarConfig.getInstance().军团战报名时间);
		
		KGang gang = KSupportFactory.getGangSupport().getGangByRoleId(role.getId());
		long gangId = gang ==null?0:gang.getId();
		{
			GangRank<GangRankElementWarSignUp> rank = KGangRankLogic.getRank(KGangRankTypeEnum.军团战报名);
			GangRankElementWarSignUp element = rank.getTempCacheData().getElement(gangId);
			msg.writeBoolean(element != null);
			
			packGangWarStatus(msg, gangId);
			msg.writeInt(rank.getPublishData().getUnmodifiableElementList().size());
		}
		
		{
			int gangLv = gang == null ? 0 : gang.getLevel();
			Map<Integer, WarRankRewardData> showDatas = KGangWarDataManager.mGangWarRewardDataManager.getDataCacheForShow();
			RewardRatioData ratioData = KGangWarDataManager.mGangWarRewardRatioDataManager.getData(gangLv);
			float coefficient = ratioData == null ? 1 : ratioData.coefficient;
			msg.writeByte(showDatas.size());
			for (int rank = KGangWarConfig.getInstance().MaxRound; rank >= 1 ; rank--) {
				WarRankRewardData data = showDatas.get(rank);
				msg.writeInt((int)(data.Contribution*coefficient));
				msg.writeInt((int)(data.exp*coefficient));
				msg.writeInt((int)(data.GangMoney*coefficient));
			}
		}

		Map<Integer, GangMedalData> datas = KGangWarDataManager.mGangMedalDataManager.getDataCache();
		msg.writeByte(datas.size());
		for (GangMedalData data : datas.values()) {
			msg.writeByte(data.rank);
			msg.writeInt(data.icon);
			msg.writeUtf8String(data.name);
			msg.writeUtf8String(data.explain);
			msg.writeByte(data.addAtts.size());
			for (AttValueStruct att : data.addAtts) {
				msg.writeInt(att.roleAttType.sign);
				msg.writeInt(att.addValue);
			}
		}
	}
	
	/**
	 * <pre>
	 * 【军团战状态】
	 * 1报名，显示报名按钮
	 * 2开战，显示对阵图入口按钮
	 * 3休战，不显示任何按钮
	 * 
	 * 新的方案，未实施
	 * 1报名，显示报名按钮
	 * 2准备，显示对阵图入口按钮，禁止在对阵地图内行走，禁止碰撞
	 * 3开战，显示对阵图入口按钮，可以在对阵地图内行走，可以碰撞
	 * 4休战，不显示任何按钮
	 *  
	 * 状态机：
	 *  SIGNUP_START_NOW->
	 *  WAR_WAIT_NOW->WAR_ROUND_READY_NOW->WAR_ROUND_START_NOW->
	 *  WAR_WAIT_NOW->WAR_ROUND_READY_NOW->WAR_ROUND_START_NOW->
	 *  WAR_WAIT_NOW->WAR_ROUND_READY_NOW->WAR_ROUND_START_NOW->
	 *  WAR_WAIT_NOW->WAR_ROUND_READY_NOW->WAR_ROUND_START_NOW->
	 *  ->REST_START_NOW
	 *  
	 *  参考{@link KGangWarProtocol#SM_GW_SYN_WAR_STATE}
	 *  
	 *  @param msg
	 *  @param warStatusEnum
	 *  @author CamusHuang
	 *  @creation 2014-11-6 下午9:47:09
	 * </pre>
	 */
	public static void packGangWarStatus(KGameMessage msg, long gangId){
		
		GangWarStatusEnum warStatusEnum = GangWarStatusManager.getNowStatus();
		
		switch (warStatusEnum) {
		case SIGNUP_START_NOW:
			msg.writeByte(1);
			break;
		case WAR_WAIT_NOW:
			msg.writeByte(2);
			{
				GangData gangData = GangWarDataCenter.getWarGang(gangId);
				if(gangData == null){
					msg.writeBoolean(false);
					msg.writeUtf8String(GangWarTips.你的军团未入围本周军团战);
				} else {
					GangWarRound round = GangWarDataCenter.getNowRoundData();
					GangRace race = round == null?null:round.getRaceByGangId(gangId);
					if(race==null){
						msg.writeBoolean(false);
						msg.writeUtf8String(GangWarTips.晋级失败);
					} else {
						msg.writeBoolean(false);
						
						long time = GangWarStatusManager.getWarTime().getTime_Ready(round.round);
						String tips = KGameUtilTool.genTimeStrForClient(time);
						msg.writeUtf8String(StringUtil.format(GangWarTips.本军团将于x时间对阵x军团, tips, race.getOppRaceGang(gangId).gangData.extGangName));
					}
				}
			}
			break;
		case WAR_ROUND_READY_NOW:
			msg.writeByte(2);
			{
				GangData gangData = GangWarDataCenter.getWarGang(gangId);
				if(gangData == null){
					msg.writeBoolean(false);
					msg.writeUtf8String(GangWarTips.你的军团未入围本周军团战);
				} else {
					GangWarRound round=GangWarDataCenter.getNowRoundData();
					GangRace race = round==null?null:round.getRaceByGangId(gangId);
					if(race==null){
						msg.writeBoolean(false);
						msg.writeUtf8String(GangWarTips.晋级失败);
					} else {
						msg.writeBoolean(true);
						msg.writeUtf8String(GangWarTips.本场军团战准备中);
					}
				}
			}
			break;
		case WAR_ROUND_START_NOW:
			msg.writeByte(2);
			{
				GangData gangData = GangWarDataCenter.getWarGang(gangId);
				if(gangData == null){
					msg.writeBoolean(false);
					msg.writeUtf8String(GangWarTips.你的军团未入围本周军团战);
				} else {
					GangWarRound round=GangWarDataCenter.getNowRoundData();
					GangRace race = round==null?null:round.getRaceByGangId(gangId);
					if(race==null){
						msg.writeBoolean(false);
						msg.writeUtf8String(GangWarTips.晋级失败);
					} else {
						msg.writeBoolean(true);
						msg.writeUtf8String(GangWarTips.正在进行中);
					}
				}
			}
			break;
		case REST_START_NOW:
		case WAR_ROUND_END_NOW:
		case REST_END:
			msg.writeByte(3);
			break;
		}
	}

	public static void packRaceInit(KGameMessage msg, KRole role, GangRace race, long gangId) {
		/**
		 * <pre>
		 * 服务器主动推送军团战地图的初始数据
		 * 
		 * int 结束倒计时（秒）
		 * int 复活倒计时（秒）
		 * int 开场倒计时（秒）
		 * long 我方BOSS ID
		 * int 我的当前积分
		 * short 我的最高连杀
		 * short 我的当前连杀
		 * A军团对战数据，参考{@link #x军团对战初始数据}
		 * B军团对战数据，参考{@link #x军团对战初始数据}
		 * </pre>
		 */
		RaceGangData gangData = race.getRaceGang(gangId);
		RaceMemberData mem = gangData.getMem(role.getId());
		
		msg.writeInt(GangWarStatusManager.getReleaseTimeToEnd(race.roundId));
		
		if (mem == null) {
			msg.writeInt(0);
		} else {
			msg.writeInt(mem.getReviveReleaseTime());
		}
		msg.writeInt(GangWarStatusManager.getReleaseTimeToStart(race.roundId));
		msg.writeLong(race.MapAndPKCenter.getRaceMapBossId(gangId));
		
		if (mem == null) {
			msg.writeInt(0);
			msg.writeShort(0);
			msg.writeShort(0);
		} else {
			msg.writeInt(mem.getScore());
			msg.writeShort(mem.getMaxWinCount());
			msg.writeShort(mem.getWinCount());
		}
		
		packRaceInit(msg, race, race.gangDataA);
		//
		packRaceInit(msg, race, race.gangDataB);
	}

	private static void packRaceInit(KGameMessage msg, GangRace race, RaceGangData gangData) {
		/**
		 * <pre>
		 * 
		 * string 军团名称
		 * long boss总血量
		 * </pre>
		 */
		msg.writeUtf8String(gangData.gangData.gangName);
		msg.writeLong(race.MapAndPKCenter.getBossMaxHp(gangData.gangId));
	}

	public static void packRaceInfo(KGameMessage msg, GangRace race) {
		/**
		 * <pre>
		 * 服务器主动推送军团战地图的对战数据（人数、积分）
		 * 
		 * int 结束倒计时（秒）
		 * A军团对战数据，参考{@link #x军团对战数据}
		 * B军团对战数据，参考{@link #x军团对战数据}
		 * </pre>
		 */
		msg.writeInt(GangWarStatusManager.getReleaseTimeToEnd(race.roundId));
		//
		packRaceInfo(msg, race, race.gangDataA);
		//
		packRaceInfo(msg, race, race.gangDataB);
	}

	private static void packRaceInfo(KGameMessage msg, GangRace race, RaceGangData gangData) {
		/**
		 * <pre>
		 * 
		 * 	short 人数
		 * 	int 积分
		 * 	long boss现存血量
		 * </pre>
		 */
		msg.writeShort(gangData.countMemsInRace());
		msg.writeInt(gangData.getScore());
		msg.writeLong(race.MapAndPKCenter.getBossCurrentHp(gangData.gangId));
	}
	
	public static void packRaceKeepWin(KGameMessage msg, GangRace race) {
		/**
		 * <pre>
		 * 服务器主动推送军团战地图的连杀数据
		 * 
		 * byte 数量n
		 * for(0~n) {
		 * 	String 角色名称
		 * 	byte 角色职业（影射头像）
		 * 	String 军团名称
		 * 	short 最高连杀
		 * }
		 * </pre>
		 */
		List<BPElement> list = race.keepKillRank.getCopyElementList();
		list = list.subList(0, Math.min(list.size(), KGangWarConfig.getInstance().KeepKillRankEffectCount));
		
		msg.writeByte(list.size());
		for (BPElement e : list) {
			packRaceKeepWin(msg, e);
		}
	}

	private static void packRaceKeepWin(KGameMessage msg, BPElement e) {
		/**
		 * <pre>
		 * 	String 角色名称
		 * 	byte 角色职业（影射头像）
		 * 	String 军团名称
		 * 	short 最高连杀
		 * </pre>
		 */
		msg.writeUtf8String(e.elementName);
		msg.writeByte(e.job);
		msg.writeUtf8String(e.gangName);
		msg.writeShort(e.score);
	}

	public static void packScoreRank(KGameMessage msg, KRole role, byte numPerPage, short startPage, byte pageNum) {

		long gangId = KGangLogic.getGangIdByRoleId(role.getId());
		if (gangId < 1) {
			msg.writeShort(0);
			return;
		}

		GangWarRound round = GangWarDataCenter.getNowRoundData();
		if (round == null) {
			msg.writeShort(0);
			return;
		}

		GangRace race = round.getRaceByGangId(gangId);
		if (race == null) {
			msg.writeShort(0);
			return;
		}

		int startIndex = (startPage - 1) * numPerPage;// 起始位置
		int endIndex = startIndex + pageNum * numPerPage;// 结束位置（不包含）
		//
		{
			List<BPElement> list = race.scoreRank.getCopyElementList();

			int writeIndex = msg.writerIndex();
			msg.writeShort(0);
			int count = 0;
			if (startIndex < list.size()) {
				if (endIndex > list.size()) {
					endIndex = list.size();
				}

				for (int index = startIndex; index < endIndex; index++) {
					BPElement element = list.get(index);
					packScoreRankElement(msg, element);
					count++;
				}
			}
			msg.setShort(writeIndex, count);
		}
	}

	private static void packScoreRankElement(KGameMessage msg, BPElement element) {
		/**
		 * <pre>
		 * short 军团数(已经按名次排好序)
		 * for() {
		 * 	short　名次
		 * 	String 角色名
		 * 	String 军团名	
		 * 	int 积分
		 * }
		 * </pre>
		 */
		msg.writeShort(element.rank);
		msg.writeUtf8String(element.elementName);
		msg.writeUtf8String(element.gangName);
		msg.writeInt(element.score);
	}

	/**
	 * <pre>
	 * 
	 * @param msg
	 * @param role
	 * @param numPerPage
	 * @param startPage
	 * @param pageNum
	 * @author CamusHuang
	 * @creation 2014-4-13 下午5:13:35
	 * </pre>
	 */
	public static void packSignUpList(KGameMessage msg, int numPerPage, int startPage, int pageNum) {
		/**
		 * <pre>
		 * short 军团数(已经按名次排好序)
		 * for() {
		 * 	short　名次
		 * 	String 军团名	
		 * 	byte 军团等级
		 * 	String　军团人数
		 * 	int 繁荣度
		 * 	int 军团总战力
		 * }
		 * </pre>
		 */
		int startIndex = (startPage - 1) * numPerPage;// 起始位置
		int endIndex = startIndex + pageNum * numPerPage;// 结束位置（不包含）
		//
		{
			GangRank<GangRankElementWarSignUp> rank = KGangRankLogic.getRank(KGangRankTypeEnum.军团战报名);
			List<GangRankElementWarSignUp> list = new ArrayList<GangRankElementWarSignUp>(rank.getPublishData().getUnmodifiableElementList());

			int writeIndex = msg.writerIndex();
			msg.writeShort(0);
			int count = 0;
			if (startIndex < list.size()) {
				if (endIndex > list.size()) {
					endIndex = list.size();
				}

				for (int index = startIndex; index < endIndex; index++) {
					GangRankElementWarSignUp element = list.get(index);
					KGang gang = KSupportFactory.getGangSupport().getGang(element.elementId);
					if (gang == null) {
						continue;
					}
					packSignUpElement(msg, gang, element);
					count++;
				}
			}
			msg.setShort(writeIndex, count);
		}
	}

	private static void packSignUpElement(KGameMessage msg, KGang gang, GangRankElementWarSignUp element) {
		/**
		 * <pre>
		 * short 军团数(已经按名次排好序)
		 * for() {
		 * 	short　名次
		 * 	String 军团名	
		 * 	byte 军团等级
		 * 	String　军团人数
		 * 	int 繁荣度
		 * 	int 军团总战力
		 * }
		 * </pre>
		 */
		msg.writeShort(element.getRank());
		msg.writeUtf8String(element.elementName);
		msg.writeByte(gang.getLevel());
		GangLevelData lvData = KGangDataManager.mGangLevelDataManager.getLevelData(gang.getLevel());
		msg.writeUtf8String(gang.memberSize() + "/" + lvData.maxuser);
		msg.writeInt(element.getFlourish());
		msg.writeInt(gang.countGangBattlePower());
	}

	/**
	 * <pre>
	 * 打包对阵图数据
	 * {@link KGangWarProtocol#军团战数据结构}
	 * 
	 * @param msg
	 * @author CamusHuang
	 * @creation 2013-7-15 上午1:31:12
	 * </pre>
	 */
	public static void packWarDrawView(KGameMessage msg, KRole role) {
		/**
		 * <pre>
		 * 获取军团战对阵图数据结果
		 * 
		 * A组数据参考{@link #军团战对阵图X组数据}
		 * B组数据参考{@link #军团战对阵图X组数据}
		 * 
		 * boolean 是否有总决赛
		 * if(true){
		 * 	byte 0表示未有胜负，1表示A军团胜出，2表示B军团胜出
		 * 	String A军团名称
		 * 	String B军团名称
		 * }
		 * 
		 * boolean 是否有下一场（有才能入场）
		 * if(true){
		 * 	String 军团1名称
		 * 	String 军团2名称
		 * 	String 显示时间（正在进行中、周五21:45）
		 * }
		 * </pre>
		 */
		packRoundGroupData(msg, true);
		packRoundGroupData(msg, false);
		//
		GangWarRound lastRound = GangWarDataCenter.getRoundData(KGangWarConfig.getInstance().MaxRound);
		List<GangRace> list = lastRound==null?Collections.<GangRace>emptyList():lastRound.getUnmodifyRaceList();
		msg.writeBoolean(!list.isEmpty());
		if (!list.isEmpty()) {
			GangRace race = list.get(0);

			RaceGangData data = race.getWinner();
			if (data == null) {
				msg.writeByte(0);
			} else if (data.gangId == race.gangDataA.gangId) {
				msg.writeByte(1);
			} else {
				msg.writeByte(2);
			}
			msg.writeUtf8String(race.gangDataA.gangData.gangName);
			msg.writeUtf8String(race.gangDataB.gangData.gangName);
		}
		// 存在下一场，且本人所属军团有资格参战
		{
			if (role == null) {
				msg.writeBoolean(false);
			} else {
				GangWarRound round = GangWarDataCenter.getNowRoundData();
				if (round==null || round.isEnd()) {
					msg.writeBoolean(false);
				} else {
					long gangId = KGangLogic.getGangIdByRoleId(role.getId());
					GangRace race = round.getRaceByGangId(gangId);
					if (race == null) {
						msg.writeBoolean(false);
					} else {
						msg.writeBoolean(true);
						msg.writeUtf8String(race.gangDataA.gangData.gangName);
						msg.writeUtf8String(race.gangDataB.gangData.gangName);
						if (GangWarStatusManager.isCanJoinMap()) {
							msg.writeUtf8String(GangWarTips.正在进行中);
						} else {
							long time = GangWarStatusManager.getWarTime().getTime_Ready(round.round);
							msg.writeUtf8String(KGameUtilTool.genTimeStrForClient(time));
						}
					}
				}
			}
		}
	}

	private static void packRoundGroupData(KGameMessage msg, boolean isGroupA) {
		/**
		 * <pre>
		 * 
		 * byte 军团数量n(16)
		 * for(0~n) {
		 * 	String 军团名称
		 * }
		 * 
		 * byte 场数n（4）
		 * for(0~n) {
		 * 	byte 对战数量m
		 * 	for(0~m) {
		 * 		byte 0表示未有胜负，1表示A军团胜出，2表示B军团胜出
		 * 	}
		 * }
		 * </pre>
		 */
		{
			GangWarRound round1 = GangWarDataCenter.getRoundData(1);
			List<GangRace> list = round1==null?Collections.<GangRace>emptyList():round1.getUnmodifyRaceList(isGroupA);
			msg.writeByte(list.size() * 2);
			for (GangRace race : list) {
				msg.writeUtf8String(race.gangDataA.gangData.gangName);
				msg.writeUtf8String(race.gangDataB.gangData.gangName);
			}
		}
		msg.writeByte(KGangWarConfig.getInstance().MaxRound - 1);
		for (int roundId = 1; roundId < KGangWarConfig.getInstance().MaxRound; roundId++) {
			GangWarRound round = GangWarDataCenter.getRoundData(roundId);
			List<GangRace> list = round==null?Collections.<GangRace>emptyList():round.getUnmodifyRaceList(isGroupA);
			msg.writeByte(list.size());
			for (GangRace race : list) {
				RaceGangData data = race.getWinner();
				if (data == null) {
					msg.writeByte(0);
				} else if (data.gangId == race.gangDataA.gangId) {
					msg.writeByte(1);
				} else {
					msg.writeByte(2);
				}
			}
		}
	}

	/**
	 * <pre>
	 * 发送消息给所有处于战斗场景中的玩家
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 下午9:26:49
	 * </pre>
	 */
	public static void sendMsgToRoleInWarOfRound(KGameMessage msg) {
		if (msg == null) {
			return;
		}
		GangWarRound round = GangWarDataCenter.getNowRoundData();
		if (round == null) {
			return;
		}
		for (GangRace race : round.getUnmodifyRaceList()) {
			sendMsgToRoleInWarOfRace(msg, race);
		}
	}
	/**
	 * <pre>
	 * 发送消息给本轮参战军团的所有成员
	 * 
	 * @param msg
	 * @author CamusHuang
	 * @creation 2014-11-26 下午12:00:06
	 * </pre>
	 */
	public static void sendMsgToRoleOfRound(KGameMessage msg) {
		if (msg == null) {
			return;
		}
		GangWarRound round = GangWarDataCenter.getNowRoundData();
		if (round == null) {
			return;
		}
		for (GangRace race : round.getUnmodifyRaceList()) {
			KGangMsgPackCenter.sendMsgToMemebers(msg, race.gangDataA.gangId);
			KGangMsgPackCenter.sendMsgToMemebers(msg, race.gangDataB.gangId);
		}
	}

	/**
	 * <pre>
	 * 发送消息给处于指定战斗场景中的玩家
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 下午9:26:49
	 * </pre>
	 */
	public static void sendMsgToRoleInWarOfRace(KGameMessage msg, GangRace race) {
		if(race==null || msg == null){
			return;
		}
		sendMsgToRoleInWarOfGang(race.gangDataA, msg);
		sendMsgToRoleInWarOfGang(race.gangDataB, msg);
	}

	public static void sendMsgToRoleInWarOfGang(RaceGangData gang, KGameMessage msg) {
		if(gang==null || msg == null){
			return;
		}
		
		KGameMessage dupMsg = msg.duplicate();
		for (RaceMemberData mem : gang.getAllMems()) {
			if (mem.getStatus() != MemStatusEnum.OUT) {
				if(KSupportFactory.getRoleModuleSupport().sendMsg(mem.roleId, dupMsg)){
					dupMsg = msg.duplicate();
				}
			}
		}
	}

	/**
	 * <pre>
	 * 发送消息给所有入围军团的在线成员
	 * 主要是对阵状态更新
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 下午9:26:49
	 * </pre>
	 */
	public static void sendMsgToRoleOfGangs(KGameMessage msg) {
		if (msg == null) {
			return;
		}
		for (GangData wf : GangWarDataCenter.getUnmodifyWarGangs()) {
			KGang gang = KSupportFactory.getGangSupport().getGang(wf.gangId);
			if (gang != null) {
				KGangMsgPackCenter.sendMsgToMemebers(msg, gang);
			}
		}
	}

	/**
	 * <pre>
	 * 发送消息给所有在线的玩家
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-28 下午9:26:49
	 * </pre>
	 */
	public static void sendMsgToAllOnlineRoles(KGameMessage msg) {
		if (msg == null) {
			return;
		}
		UtilTool.sendMsgToAllOnlineSession(msg);
	}
}
