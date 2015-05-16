package com.kola.kmp.logic.gang.reswar;

import javax.management.timer.Timer;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.util.UtilTool;

public class KResWarConfig {
	private static String cfgPath = null;

	
	// 排行榜保存路径
	static final String saveDirPath = "./res/output/resWarBidRank/";
	// 排行榜保存文件名称
	static final String saveFileName = "city";
	// 排行榜保存文件后缀
	static final String saveFileNameSuffix = ".xml";
	// 排行榜保存到DB时的排行榜类型起始值
	static final int MinRankTypeForDB = 20;
	// /////////////////////////////
	static int FirstBidPrice;// = 20000;// 首次竞价的金额
	static int OtherBidPrice;// = 1000;// 后续追加竞价的价格
	static int SeizeFailBackRate;// 城市争夺失败，返回金额的比率60表示60%

	static int BID_RANK_SHOWCOUNT;// 竞价榜显示的最大数量

	/** 竞价宣传播报周期 */
	static long BID_RANK_SAVE_PERIOD = UtilTool.parseDHMS("1M");
	
	/** 对战裁决扫描周期 */
	static long WarScanStartDelay;// = UtilTool.parseDHMS("3S");
	static long WarScanPeroid;// = UtilTool.parseDHMS("3S");

	/** 竞价宣传播报周期 */
	static long BidBroadcastPeroid;
	/** 本周结果播报周期 */
	static long WarFinalResultBroadcastPeroid;
	
	/** 积分产出周期(秒) */
	static int AddScorePeroid;
	/** 已方积分先超过10000：己方胜 */
	static int WinScoreValue;// = 10000;
	
	static String 资源战PVP地图文件名;
	static int 资源战PVP地图背景音乐;

	/** 竞价时间为：周一0：00至周日00:00(距周一0点0分的毫秒数) */
	static long bidStartTime;// 结束休息，开始报名
	static long bidEndTime;// 结束报名，开始准备
	/** 战斗开始和结束时间(距周一0点0分的毫秒数) */
	static long warStartTime;// 结束准备，开始对战
	static long warEndTime;// 结束对战，开始休息

	public static void init(String cfgPath) throws KGameServerException {

		if (cfgPath == null) {
			cfgPath = KResWarConfig.cfgPath;
		}
		Document doc = XmlUtil.openXml(cfgPath);
		Element root = doc.getRootElement();
		Element logicE = root.getChild("logicConfig");
		KResWarConfig.cfgPath = cfgPath;

		FirstBidPrice = Integer.parseInt(logicE.getChildTextTrim("FirstBidPrice"));
		OtherBidPrice = Integer.parseInt(logicE.getChildTextTrim("OtherBidPrice"));
		SeizeFailBackRate = Integer.parseInt(logicE.getChildTextTrim("SeizeFailBackRate"));

		BID_RANK_SHOWCOUNT = Integer.parseInt(logicE.getChildTextTrim("BID_RANK_SHOWCOUNT"));

		资源战PVP地图文件名 = logicE.getChildTextTrim("资源战PVP地图文件名");
		资源战PVP地图背景音乐=Integer.parseInt(logicE.getChildTextTrim("资源战PVP地图背景音乐"));
		
		// =========================
		{
			Element e = logicE.getChild("warTime");
			
			/** 对战裁决扫描周期 */
			WarScanStartDelay = UtilTool.parseDHMS(e.getChildTextTrim("WarScanStartDelay"));
			WarScanPeroid = UtilTool.parseDHMS(e.getChildTextTrim("WarScanPeroid"));// 结束对战，开始休息

			BidBroadcastPeroid = UtilTool.parseDHMS(e.getChildTextTrim("BidBroadcastPeroid"));
			WarFinalResultBroadcastPeroid = UtilTool.parseDHMS(e.getChildTextTrim("WarFinalResultBroadcastPeroid"));

			AddScorePeroid = (int) (UtilTool.parseDHMS(e.getChildTextTrim("AddScorePeroid")) / Timer.ONE_SECOND);
			WinScoreValue = Integer.parseInt(e.getChildTextTrim("WinScoreValue"));
			
			/** 竞价时间为：周一0：00至周日00:00(距周一0点0分的毫秒数) */
			bidStartTime = UtilTool.parseDHMS(e.getChildTextTrim("bidStartTime"));// 结束休息，开始报名
			bidEndTime = UtilTool.parseDHMS(e.getChildTextTrim("bidEndTime"));// 结束报名，开始准备
			/** 战斗开始和结束时间(距周一0点0分的毫秒数) */
			warStartTime = UtilTool.parseDHMS(e.getChildTextTrim("warStartTime"));// 结束准备，开始对战
			warEndTime = UtilTool.parseDHMS(e.getChildTextTrim("warEndTime"));// 结束对战，开始休息

			if ((warEndTime - bidStartTime) > (Timer.ONE_DAY * 7)) {
				throw new KGameServerException("军团资源争夺活动时间跨度不能超过7天");
			}
			if (bidStartTime >= bidEndTime || bidEndTime >= warStartTime || warStartTime >= warEndTime) {
				throw new KGameServerException("军团资源争夺活动时间先后顺序错误");
			}
		}
	}
}
