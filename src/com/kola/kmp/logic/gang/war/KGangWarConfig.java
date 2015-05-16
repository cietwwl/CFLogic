package com.kola.kmp.logic.gang.war;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.timer.Timer;

import org.jdom.Document;
import org.jdom.Element;

import ch.qos.logback.classic.pattern.Util;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.StringUtil;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.gang.war.GangWarStatusManager.WarTime;
import com.kola.kmp.logic.map.duplicatemap.CollisionEventObjectData;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMap;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapBornPoint;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.tips.GangWarTips;

/**
 * <pre>
 * 军团--模块配置表
 * 
 * @author camus
 * @creation 2012-12-30 下午2:49:48
 * </pre>
 */
public class KGangWarConfig implements Cloneable{
	private static KGangWarConfig instance;

	private static String configPath;

	// =========================
	/** 最大场数 */
	final int MaxRound = 4;
	/** 最多16个军团入围 */
	final int MaxGangCount = 16;
	/** 积分榜显示和获取的数量 */
	final int ScoreRankEffectCount = 10;
	/** 连杀榜显示和获取的数量 */
	final int KeepKillRankEffectCount = 3;

	// =========================
	String 军团战开始时间;
	String 军团战报名时间;
	/** 活动报名时间为：周一0：00至周五00:00(距周一0点0分的毫秒数) */
	final long signUpStartTime;// 结束休息，开始报名
	long signUpEndTime;// 结束报名，开始准备
	/** 各场的时间(距周一0点0分的毫秒数) */
	Map<Integer, RoundTimeConfig> roundTimeMap = new HashMap<Integer, RoundTimeConfig>();

	// =========================
	final int 军团战场景地图;// 22001
	final String 军团战PVP地图文件名;
	final int 军团战PVP地图背景音乐;
	final int 军团战PVE地图关卡ID;

	// =========================
	/** 开场倒计时（s） */
	public final int StartRoundCountDown;
	
	/** 报名宣传播报周期 */
	final long SignupBroadcastPeroid;// = UtilTool.parseDHMS("30M");

	/** 对战裁决扫描周期 */
	final long WarScanStartDelay;// = UtilTool.parseDHMS("3S");
	final long WarScanPeroid;// = UtilTool.parseDHMS("3S");

	/** BOSS选择：双方成员等级混合后取前10名求平均等级 */
	final int BoosAvgRoleLvNum;

	/** 连杀达到此值被清0时世界广播 */
	final int WinCountClearBroad;

	/** PVP后有30秒的保护时间，不可以和同一个人切磋 */
	public final long LastPVPCDTime;// = UtilTool.parseDHMS("30S");
	/** 死亡后有30秒的复活时间 */
	public final long DeadCDTime;// = UtilTool.parseDHMS("30S");
	/** 胜利后有10秒的免被P时间 */
	public final long PVPCDTime;// = UtilTool.parseDHMS("30S");
	/** PVP\PVE限时：只要超时，就算角色输*/
	public final long PKMaxTime;

	private KGangWarConfig(Element root) throws KGameServerException {
		// CNEXT 军团--模块配置表初始化

		// =========================
		{
			Element e = root.getChild("warTime");
			String mode = e.getAttributeValue("mode");
			if(mode.equals("1")){
				e = e.getChild("mode1");
				
				军团战开始时间 = e.getChildTextTrim("军团战开始时间");
				军团战报名时间 = e.getChildTextTrim("军团战报名时间");
	
				signUpStartTime = UtilTool.parseDHMS(e.getChildTextTrim("signUpStartTime"));
				signUpEndTime = UtilTool.parseDHMS(e.getChildTextTrim("signUpEndTime"));
				RoundTimeConfig lastRoundTime = null;
				for (Object obj : e.getChildren("round")) {
					Element ee = (Element) obj;
					int time = Integer.parseInt(ee.getAttributeValue("id"));
					long ReadyTime = UtilTool.parseDHMS(ee.getChildTextTrim("ReadyTime"));// 结束等待，开始准备
					long StartTime = UtilTool.parseDHMS(ee.getChildTextTrim("StartTime"));// 结束准备，开始对战
					long EndTime = UtilTool.parseDHMS(ee.getChildTextTrim("EndTime"));// 结束对战，开始休息
	
					if (signUpStartTime >= ReadyTime || ReadyTime >= StartTime || StartTime >= EndTime) {
						throw new KGameServerException("军团战活动时间先后顺序错误");
					}
	
					if (lastRoundTime != null && lastRoundTime.EndTime >= ReadyTime) {
						throw new KGameServerException("军团战活动时间先后顺序错误");
					}
	
					RoundTimeConfig roundTime = new RoundTimeConfig(time, ReadyTime, StartTime, EndTime);
					roundTimeMap.put(time, roundTime);
				}
	
				if (MaxRound != roundTimeMap.size()) {
					throw new KGameServerException("军团战活动时间场次配置错误");
				}
				if (roundTimeMap.get(MaxRound).EndTime > (Timer.ONE_DAY * 7)) {
					throw new KGameServerException("军团战活动时间不能超过7天");
				}
				if ((roundTimeMap.get(MaxRound).EndTime - signUpStartTime) > (Timer.ONE_DAY * 7)) {
					throw new KGameServerException("军团战活动时间跨度不能超过7天");
				}
			} else if(mode.equals("2")){
				e = e.getChild("mode2");
				
				signUpStartTime = UtilTool.parseDHMS(e.getChildTextTrim("signUpStartTime"));
				signUpEndTime = UtilTool.parseDHMS(e.getChildTextTrim("signUpEndTime"));
				
				long ERound1ReadyTime = UtilTool.parseDHMS(e.getChildTextTrim("Round1ReadyTime"));
				long EReadyTime = UtilTool.parseDHMS(e.getChildTextTrim("ReadyTime"));
				long EPKTime = UtilTool.parseDHMS(e.getChildTextTrim("PKTime"));
				long RestTime = UtilTool.parseDHMS(e.getChildTextTrim("RestTime"));
				
				long roundKeepTime = EReadyTime+EPKTime+RestTime;
				
				if (signUpStartTime >= ERound1ReadyTime) {
					throw new KGameServerException("军团战活动时间先后顺序错误");
				}
				
				for (int round = 1;round<=MaxRound; round++) {
					long temp = (round-1)*roundKeepTime;
					long ReadyTime = ERound1ReadyTime+temp;
					long StartTime = ERound1ReadyTime+EReadyTime+temp;
					long EndTime = ERound1ReadyTime+EReadyTime+EPKTime+temp;
					RoundTimeConfig roundTime = new RoundTimeConfig(round, ReadyTime, StartTime, EndTime);
					roundTimeMap.put(round, roundTime);
				}
	
				if (MaxRound != roundTimeMap.size()) {
					throw new KGameServerException("军团战活动时间场次配置错误");
				}
				if (roundTimeMap.get(MaxRound).EndTime > (Timer.ONE_DAY * 7)) {
					throw new KGameServerException("军团战活动时间不能超过7天");
				}
				if ((roundTimeMap.get(MaxRound).EndTime - signUpStartTime) > (Timer.ONE_DAY * 7)) {
					throw new KGameServerException("军团战活动时间跨度不能超过7天");
				}
				
				
				// 初始化本周军团战的相关时间节点
				long nowTime = System.currentTimeMillis();
				long thisWeekStartTime = UtilTool.getThisWeekStart(nowTime).getTimeInMillis();
				initTimeStr(thisWeekStartTime);
				
			} else {
				throw new KGameServerException("军团战时间表mode错误");
			}
		} 
		// =========================
		军团战场景地图 = Integer.parseInt(root.getChildTextTrim("军团战场景地图"));
		军团战PVP地图文件名 = root.getChildTextTrim("军团战PVP地图文件名");
		军团战PVP地图背景音乐 = Integer.parseInt(root.getChildTextTrim("军团战PVP地图背景音乐"));
		军团战PVE地图关卡ID = Integer.parseInt(root.getChildTextTrim("军团战PVE地图关卡ID"));

		// =========================
		StartRoundCountDown = (int)(UtilTool.parseDHMS(root.getChildTextTrim("StartRoundCountDown"))/Timer.ONE_SECOND);
		if(StartRoundCountDown <= 1 || StartRoundCountDown>Timer.ONE_MINUTE/Timer.ONE_SECOND){
			throw new KGameServerException("数值错误 StartRoundCountDown="+StartRoundCountDown);
		}
		
		/** 报名宣传播报周期 */
		SignupBroadcastPeroid = UtilTool.parseDHMS(root.getChildTextTrim("SignupBroadcastPeroid"));// 结束对战，开始休息
		if(SignupBroadcastPeroid <= Timer.ONE_MINUTE){
			throw new KGameServerException("数值错误 SignupBroadcastPeroid="+SignupBroadcastPeroid);
		}
		
		/** 对战裁决扫描周期 */
		WarScanStartDelay = UtilTool.parseDHMS(root.getChildTextTrim("WarScanStartDelay"));
		if(WarScanStartDelay <= Timer.ONE_SECOND){
			throw new KGameServerException("数值错误 WarScanStartDelay="+WarScanStartDelay);
		}
		WarScanPeroid = UtilTool.parseDHMS(root.getChildTextTrim("WarScanPeroid"));// 结束对战，开始休息
		if(WarScanPeroid <= Timer.ONE_SECOND || WarScanPeroid >= Timer.ONE_MINUTE){
			throw new KGameServerException("数值错误 WarScanPeroid="+WarScanPeroid);
		}

		WinCountClearBroad = Integer.parseInt(root.getChildTextTrim("WinCountClearBroad"));
		if(WinCountClearBroad < 1){
			throw new KGameServerException("数值错误 WinCountClearBroad="+WinCountClearBroad);
		}

		BoosAvgRoleLvNum = Integer.parseInt(root.getChildTextTrim("BoosAvgRoleLvNum"));
		if(BoosAvgRoleLvNum < 1){
			throw new KGameServerException("数值错误 BoosAvgRoleLvNum="+BoosAvgRoleLvNum);
		}
		
		LastPVPCDTime = UtilTool.parseDHMS(root.getChildTextTrim("LastPVPCDTime"));
		if(LastPVPCDTime <= Timer.ONE_SECOND){
			throw new KGameServerException("数值错误 LastPVPCDTime="+LastPVPCDTime);
		}
		DeadCDTime = UtilTool.parseDHMS(root.getChildTextTrim("DeadCDTime"));
		if(DeadCDTime <= Timer.ONE_SECOND){
			throw new KGameServerException("数值错误 DeadCDTime="+DeadCDTime);
		}
		PVPCDTime = UtilTool.parseDHMS(root.getChildTextTrim("PVPCDTime"));
		if(PVPCDTime <= Timer.ONE_SECOND){
			throw new KGameServerException("数值错误 PVPCDTime="+PVPCDTime);
		}

		PKMaxTime = UtilTool.parseDHMS(root.getChildTextTrim("PKMaxTime"));
		if(PKMaxTime <= Timer.ONE_MINUTE){
			throw new KGameServerException("数值错误 PVPMaxTime="+PKMaxTime);
		}
		// =========================
	}
	
	void resetGangWarTime(long newSignupEndTime) throws KGameServerException{
		long nowTime = System.currentTimeMillis();
		long thisWeekStartTime = UtilTool.getThisWeekStart(nowTime).getTimeInMillis();
		long thisWeekSignUpEndTime=thisWeekStartTime+signUpEndTime;
		// 各时间点偏移值
		long addTime = newSignupEndTime - thisWeekSignUpEndTime;
		
		signUpEndTime += addTime;
		
	
		if (signUpStartTime >= signUpEndTime) {
			throw new KGameServerException("军团战活动时间先后顺序错误");
		}
		
		Map<Integer, RoundTimeConfig> oldRoundTimeMap = roundTimeMap;
		roundTimeMap = new HashMap<Integer, RoundTimeConfig>();
		for (int round = 1;round<=MaxRound; round++) {
			RoundTimeConfig oldRoundTime = oldRoundTimeMap.get(round);
			RoundTimeConfig newRoundTime = new RoundTimeConfig(round, oldRoundTime.ReadyTime+addTime, oldRoundTime.StartTime+addTime, oldRoundTime.EndTime+addTime);
			roundTimeMap.put(round, newRoundTime);
		}
		

		if (MaxRound != roundTimeMap.size()) {
			throw new KGameServerException("军团战活动时间场次配置错误");
		}
		if (roundTimeMap.get(MaxRound).EndTime > (Timer.ONE_DAY * 7)) {
			throw new KGameServerException("军团战活动时间不能超过7天");
		}
		if ((roundTimeMap.get(MaxRound).EndTime - signUpStartTime) > (Timer.ONE_DAY * 7)) {
			throw new KGameServerException("军团战活动时间跨度不能超过7天");
		}
		
		
		// 初始化本周军团战的相关时间节点
		initTimeStr(thisWeekStartTime);
	}
	
	/**
	 * <pre>
	 * 初始化本周军团战的相关时间节点
	 * 
	 * @param thisWeekStartTime
	 * @author CamusHuang
	 * @creation 2015-3-9 下午2:45:25
	 * </pre>
	 */
	private void initTimeStr(long thisWeekStartTime){
		WarTime mWarTime = new WarTime(thisWeekStartTime, this, false);
		String tips = GangWarTips.第x场x+'\n'+GangWarTips.第x场x+'\n'+GangWarTips.第x场x+'\n'+"第x场x决赛";
		for (int round = 1;round<=MaxRound; round++) {
			tips = StringUtil.format(tips, round, KGameUtilTool.genTimeStrForClient2(mWarTime.getTime_Ready(round)));
		}
		军团战开始时间 = tips;
		军团战报名时间 = GangWarTips.截止于+KGameUtilTool.genTimeStrForClient2(thisWeekStartTime+signUpEndTime);
		
		GangWarLogic.GangWarLogger.warn(军团战开始时间);
		GangWarLogic.GangWarLogger.warn(军团战报名时间);
	}

	void notifyCacheLoadComplete() throws KGameServerException {
		KDuplicateMap warMap = KSupportFactory.getDuplicateMapSupport().getDuplicateMapStruct(军团战场景地图);
		if (warMap == null) {
			throw new KGameServerException("军团战场景地图 不存在 id=" + 军团战场景地图);
		}
		// 识别出生点
		List<KDuplicateMapBornPoint> points = warMap.getAllBornPointEntity();
		if (points.size() != 2) {
			throw new KGameServerException("军团战场景地图 出生点数量错误="+points.size()+" id=" + 军团战场景地图);
		}
		// 识别BOSS
		List<CollisionEventObjectData> colObjs = warMap.getAllCollisionEventObject();
		if (colObjs.size() != 2) {
			throw new KGameServerException("军团战场景地图 BOSS数量错误="+colObjs.size()+" id=" + 军团战场景地图);
		}
	}
	

	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}



	class RoundTimeConfig {

		/** 第N场开始和结束时间(距周一0点0分的毫秒数) */
		final int Round;
		final long ReadyTime;
		final long StartTime;
		final long EndTime;

		private RoundTimeConfig(int round, long readyTime, long startTime, long endTime) {
			Round = round;
			ReadyTime = readyTime;
			StartTime = startTime;
			EndTime = endTime;
		}
	}

//	public static void main(String[] s) {
//		String a = "【军团争霸】分三场，在每周五，周六，周日21:00开战，每场30分钟\n周一由团长或团长代言人报名，周五20:00截止\n可以多次报名，多次报名视为“追加”报名资金\n报名时立即扣除军团资金\n报名截止时缴纳资金最高的前8名入围【军团争霸】，不入围则退还报名费用";
//		s = a.split("\n");
//		System.err.println(s[0]);
//	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param configPath null表示使用上次的URL进行重新加载
	 * @throws KGameServerException
	 * @author CamusHuang
	 * @creation 2013-9-27 上午11:04:13
	 * </pre>
	 */
	public static void init(String configPath) throws KGameServerException {
		if (configPath == null) {
			configPath = KGangWarConfig.configPath;
		}
		Document doc = XmlUtil.openXml(configPath);
		Element root = doc.getRootElement();
		Element logicE = root.getChild("logicConfig");

		instance = new KGangWarConfig(logicE);
		// 成功加载才缓存路径
		KGangWarConfig.configPath = configPath;
	}
	
	/**
	 * <pre>
	 * 克隆旧配置，并按指定时间重置时间点
	 * 
	 * @throws KGameServerException
	 * @author CamusHuang
	 * @creation 2013-9-27 上午11:04:13
	 * </pre>
	 */
	public static void init(long signupEndTime) throws Exception {
		KGangWarConfig newConfig = (KGangWarConfig)instance.clone();
		newConfig.resetGangWarTime(signupEndTime);

		instance = newConfig;
	}
	
	public static KGangWarConfig getInstance() {
		return instance;
	}
}
