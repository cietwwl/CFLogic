package com.kola.kmp.logic.rank;

import javax.management.timer.Timer;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KRankTypeEnum;

/**
 * <pre>
 * 模块配置表
 * 
 * @author camus
 * @creation 2012-12-30 下午2:49:48
 * </pre>
 */
public class KRankConfig {
	private static KRankConfig instance;

	/* 排行榜排名变化通知时间任务，延时多久执行 */
	public final long RankChangeTaskDelayTime;
	/* 排行榜重排周期，DHMS格式 */
	public final long RankResortPeriod;

	private KRankConfig(Element root) throws KGameServerException {

		RankChangeTaskDelayTime = UtilTool.parseDHMS(root.getChildTextTrim("RankChangeTaskDelayTime"));
		if (RankChangeTaskDelayTime < Timer.ONE_SECOND) {
			throw new KGameServerException("RankChangeTaskDelayTime 数据错误");
		}

		RankResortPeriod = UtilTool.parseDHMS(root.getChildTextTrim("RankResortPeriod"));
		if (RankResortPeriod < Timer.ONE_SECOND) {
			throw new KGameServerException("RankResortPeriod 数据错误");
		}

		Element rankTypes = root.getChild("role").getChild("RankType");
		for (KRankTypeEnum type : KRankTypeEnum.values()) {
			Element dataE = rankTypes.getChild(type.name());
			type.reset(Integer.parseInt(dataE.getChildTextTrim("minJoinLv")), Integer.parseInt(dataE.getChildTextTrim("maxLen")));
			if (type.getMaxLen() < 10) {
				throw new KGameServerException("maxLen 数据错误，排行榜=" + type.name());
			}
		}
	}

	public static void init(Element root) throws KGameServerException {
		instance = new KRankConfig(root);
	}

	public static KRankConfig getInstance() {
		return instance;
	}
}
