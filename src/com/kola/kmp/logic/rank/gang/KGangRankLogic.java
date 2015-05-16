package com.kola.kmp.logic.rank.gang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.kola.kgame.cache.DataCacheAccesserFactory;
import com.kola.kgame.cache.GangEntireDataCacheAccesser;
import com.kola.kgame.cache.gang.Gang;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.message.KSyncGangDataMsg;
import com.kola.kmp.logic.rank.abs.ElementAbs;

/**
 * <pre>
 * 军团相关的排行榜
 * 
 * @author CamusHuang
 * @creation 2013-8-12 下午5:47:37
 * </pre>
 */
public class KGangRankLogic {

	private static final Logger _LOGGER = KGameLogger.getLogger(KGangRankLogic.class);

	private KGangRankLogic() {
	}

	// 所有排行榜
	static final GangRank<GangRankElement> gangRank = new GangRank<GangRankElement>(KGangRankTypeEnum.全部军团);
	static final GangRank<GangRankElementWar> gangWarRank = new GangRank<GangRankElementWar>(KGangRankTypeEnum.军团战积分);
	static final GangRank<GangRankElementWarSignUp> gangWarSignUpRank = new GangRank<GangRankElementWarSignUp>(KGangRankTypeEnum.军团战报名);
	static final GangRank<GangRankElementPower> gangPowRank = new GangRank<GangRankElementPower>(KGangRankTypeEnum.军团战力);
	// 所有排行榜
	private static final GangRank[] AllRankList = new GangRank[] { gangRank, gangWarRank, gangWarSignUpRank, gangPowRank};
	private static final Map<KGangRankTypeEnum, GangRank> AllRankMap = new HashMap<KGangRankTypeEnum, GangRank>();
	static {
		for (GangRank rank : AllRankList) {
			AllRankMap.put(rank.getType(), rank);
		}
	}

	public static void init(Element root) throws KGameServerException {
		{
			Element rankTypes=root.getChild("RankType");
			for (KGangRankTypeEnum type : KGangRankTypeEnum.values()) {
				Element dataE = rankTypes.getChild(type.name());
				type.reset(Integer.parseInt(dataE.getChildTextTrim("maxLen")));
			}
		}
	}

	public static void notifyCacheLoadComplete() {
		// 从DB加载排行榜
		try {
			loadRankFromDB();
		} catch (Exception e) {
			_LOGGER.error(e.getMessage(), e);
		}

		// 启动任务
		GangRankTaskManager.notifyCacheLoadComplete();
	}

	private static void loadRankFromDB() throws KGameServerException {

		// 军团排行榜与角色排行榜不同：在开服时，遍历全体军团，重新插入到各类排行榜
		GangEntireDataCacheAccesser cache = DataCacheAccesserFactory.getGangEntireDataCacheAccesser();

		List<Gang> copyGangList = cache.getAllGangsCopy();
		for (Gang temp : copyGangList) {
			KGang gang = (KGang) temp;
			notifyGangCreate(gang);
		}
		
		// 对于军团战报名榜，则需要根据DB中的排行榜数据进行恢复
		try {
			gangWarSignUpRank.load();
		} catch (Exception e) {
			throw new KGameServerException("加载排行榜错误：" + e.getMessage(), e);
		}

		// 加载完数据发布一份排行榜
		onTimeSignalForPublish(false, false, false);
	}

	public static void serverShutdown() throws KGameServerException {
		// 关服前发布一份排行榜
		onTimeSignalForPublish(true, true, true);
	}

	/**
	 * <pre>
	 * 提供于手工调用
	 * 即随时保存一份当前发布榜的数据
	 * 
	 * @param isAddDate
	 * @author CamusHuang
	 * @creation 2014-2-21 上午10:08:12
	 * </pre>
	 */
	public static void saveRankByGM(boolean isSaveToDB, boolean isSaveToFile, boolean isFileNameAddDate) {
		for (GangRank rank : AllRankList) {
			rank.save(isSaveToDB, isSaveToFile, isFileNameAddDate);
		}
	}

	/**
	 * <pre>
	 * 发布排行榜
	 * 
	 * @deprecated 仅供时效任务调用
	 * @author CamusHuang
	 * @creation 2014-2-21 上午9:48:25
	 * </pre>
	 */
	static void onTimeSignalForPublish(boolean isSaveToDB, boolean isSaveToFile, boolean isFileNameAddDate) {
		for (GangRank rank : AllRankList) {
			rank.onTimeSignalForPublish(isSaveToDB, isSaveToFile, isFileNameAddDate);
		}
	}

	/**
	 * <pre>
	 * 异步通知：排行榜变化
	 * 
	 * @param rankType
	 * @param changeElementIds
	 * @author CamusHuang
	 * @creation 2013-7-7 下午2:06:03
	 * </pre>
	 */
	static void synNotifyForRankChange(KGangRankTypeEnum rankType, Set<Long> changeElementIds) {
		
		// 同步所有军团所有在线成员的军团当前排名
		if(rankType==KGangRankTypeEnum.全部军团){
			for(Long gangId:changeElementIds){
				KSyncGangDataMsg.sendMsg(gangId);
			}
		}
	}

	public static GangRank getRank(KGangRankTypeEnum type) {
		return AllRankMap.get(type);
	}

	public static int checkRank(KGangRankTypeEnum type, long gangId) {
		GangRank rank = getRank(type);
		if (rank == null) {
			return -1;
		}
		ElementAbs e = rank.getPublishData().getElement(gangId);
		if (e == null) {
			return -1;
		}
		return e.getRank();
	}

	/**
	 * <pre>
	 * 军团创建
	 * 
	 * @param gang
	 * @author CamusHuang
	 * @creation 2013-8-28 下午3:46:17
	 * </pre>
	 */
	public static void notifyGangCreate(KGang gang) {
		for (GangRank rank : AllRankList) {
			rank.notifyLevelUp(gang, gang.getLevel(), gang.getExp());
			rank.notifyGangWarScore(gang, gang.getWarSocre());
			rank.notifyGangBattlePower(gang, gang.countGangBattlePower());
		}
	}

	/**
	 * <pre>
	 * 军团解散
	 * 
	 * @param gangId
	 * @author CamusHuang
	 * @creation 2013-8-28 下午3:46:23
	 * </pre>
	 */
	public static void notifyGangDelete(long gangId) {
		for (GangRank rank : AllRankList) {
			rank.getTempCacheData().removeElement(gangId);
		}
	}

	/**
	 * <pre>
	 * 军团升级
	 * 
	 * @param gang
	 * @param lv
	 * @author CamusHuang
	 * @creation 2013-8-28 下午3:46:41
	 * </pre>
	 */
	public static void notifyGangLevelUp(KGang gang, int lv, int exp) {
		for (GangRank rank : AllRankList) {
			rank.resetGangLevelForAllRank(gang, lv);
			rank.notifyLevelUp(gang, lv, exp);
		}
	}

	/**
	 * <pre>
	 * 军团战积分
	 * 
	 * @param gang
	 * @param warScore
	 * @author CamusHuang
	 * @creation 2013-8-28 下午3:46:00
	 * </pre>
	 */
	public static void notifyGangWarFinish(KGang gang, int warScore) {
		for (GangRank rank : AllRankList) {
			rank.notifyGangWarScore(gang, warScore);
		}
	}
	
	/**
	 * <pre>
	 * 军团战报名
	 * 
	 * @param gang
	 * @param flourish
	 * @author CamusHuang
	 * @creation 2013-8-28 下午3:46:00
	 * </pre>
	 */
	public static void notifyGangWarSignUp(KGang gang, int flourish) {
		for (GangRank rank : AllRankList) {
			rank.notifyGangWarSignup(gang, flourish);
		}
	}
	
	/**
	 * <pre>
	 * 军团繁荣度变化
	 * 
	 * @param gang
	 * @param flourish
	 * @author CamusHuang
	 * @creation 2013-8-28 下午3:46:00
	 * </pre>
	 */
	public static void notifyGangFlourish(KGang gang, int flourish) {
		for (GangRank rank : AllRankList) {
			rank.notifyGangFlourish(gang, flourish);
		}
	}
	
	/**
	 * <pre>
	 * 军团总战力变化
	 * 
	 * @param gang
	 * @param lv
	 * @author CamusHuang
	 * @creation 2013-8-28 下午3:46:41
	 * </pre>
	 */
	public static void notifyGangBattlePow(KGang gang, int battlePow) {
		for (GangRank rank : AllRankList) {
			rank.notifyGangBattlePower(gang, battlePow);
		}
	}
	

}
