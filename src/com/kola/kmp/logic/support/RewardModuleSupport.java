package com.kola.kmp.logic.support;

import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataManager;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.FengceCompetitionRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.FengceFriendFubenRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.util.tips.RewardTips;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-21 下午4:58:49
 * </pre>
 */
public interface RewardModuleSupport {

	public boolean isWaitFirstCharge(long roleId);

	/**
	 * <pre>
	 * 记录活跃度行为
	 * 
	 * @param role
	 * @param funType
	 * @author CamusHuang
	 * @creation 2014-4-25 下午8:03:02
	 * </pre>
	 */
	public void recordFun(KRole role, KVitalityTypeEnum funType);

	/**
	 * <pre>
	 * 批量记录活跃度行为
	 * 
	 * @param role
	 * @param funType
	 * @param addTime
	 * @author CamusHuang
	 * @creation 2014-6-2 下午6:12:58
	 * </pre>
	 */
	public void recordFuns(KRole role, KVitalityTypeEnum funType, int addTime);
	
	/**
	 * <pre>
	 * 为免邮件先后顺序冲突
	 * 由邮件模块在发送邮件列表前通知本模块发送每日邮件
	 * @author CamusHuang
	 * @creation 2013-7-25 下午11:31:30
	 * </pre>
	 */
	public void notifyForDayMail(KRole role);

	/**
	 * <pre>
	 * 封测竞技排名奖励
	 * 
	 * @param role
	 * @param ranking
	 * @author CamusHuang
	 * @creation 2014-8-15 下午12:43:28
	 * </pre>
	 */
	public void notifyForFengceCompetionReward(long roleId, int ranking);
	
	/**
	 * <pre>
	 * 封测好友副本奖励
	 * 
	 * @param role
	 * @param GetQuantity
	 * @author CamusHuang
	 * @creation 2014-8-15 下午12:43:41
	 * </pre>
	 */
	public void notifyForFengceFriendFubenReward(KRole role, int GetQuantity) ;
}
