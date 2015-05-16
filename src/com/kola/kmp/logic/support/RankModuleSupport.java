package com.kola.kmp.logic.support;

import java.util.List;

import com.kola.kmp.logic.other.KRankTypeEnum;
import com.kola.kmp.logic.rank.RankElementLevel;
import com.kola.kmp.logic.role.KRole;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-21 下午4:58:45
 * </pre>
 */
public interface RankModuleSupport {
	/**
	 * <pre>
	 * 有战力变化时，通知排行榜
	 * 
	 * @param role
	 * @param battlePower
	 * @author CamusHuang
	 * @creation 2014-2-21 下午5:03:11
	 * </pre>
	 */
	public void notifyBattlePowerChange(KRole role, int battlePower);
	
	/**
	 * <pre>
	 * 角色切换随从、随从升级、随从战力变化时通知本方法
	 * 取消出战不用通知，出战新的随从才通知
	 * 
	 * @param role
	 * @param petName
	 * @param petLv
	 * @param petPow
	 * @author CamusHuang
	 * @creation 2014-8-30 下午12:19:58
	 * </pre>
	 */
	public void notifyPetInfoChange(KRole role, String petName, int petLv, int petPow);

	/**
	 * <pre>
	 * 查询角色在排行榜中的排名
	 * 
	 * @param ranType
	 * @param roleId
	 * @return 0表示未上榜
	 * @author CamusHuang
	 * @creation 2014-2-21 上午11:11:44
	 * </pre>
	 */
	public int checkRank(KRankTypeEnum ranType, long roleId);

	/**
	 * <pre>
	 * 获取等级排行榜
	 * 
	 * @param page
	 * @param pageSize
	 * @return
	 * @author CamusHuang
	 * @creation 2014-12-5 上午11:32:36
	 * </pre>
	 */
	public List<RankElementLevel> getRankElements_Level(int page, int pageSize);
	
	/**
	 * <pre>
	 * 获取排行榜
	 * 
	 * @param ranType
	 * @param page 第几页：从1开始算
	 * @param pageSize 一页的长度
	 * @return 一定不为null
	 * @author CamusHuang
	 * @creation 2014-2-21 下午5:06:39
	 * </pre>
	 */
	public List getRankElements(KRankTypeEnum ranType, int page, int pageSize);
}
