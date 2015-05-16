package com.kola.kmp.logic.reward;

import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.FengceCompetitionRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.FengceFriendFubenRewardData;
import com.kola.kmp.logic.reward.vitality.KVitalityCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RewardModuleSupport;
import com.kola.kmp.logic.util.tips.RewardTips;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-21 下午4:59:04
 * </pre>
 */
public class KRewardSupportImpl implements RewardModuleSupport {

	@Override
	public boolean isWaitFirstCharge(long roleId) {
		return false;
	}

	public void recordFun(KRole role, KVitalityTypeEnum funType) {
		KVitalityCenter.recordFuns(role, funType, 1);
	}

	public void recordFuns(KRole role, KVitalityTypeEnum funType, int addTime) {
		KVitalityCenter.recordFuns(role, funType, addTime);
	}

	/**
	 * <pre>
	 * 为免邮件先后顺序冲突
	 * 由邮件模块在发送邮件列表前通知本模块发送每日邮件
	 * @author CamusHuang
	 * @creation 2013-7-25 下午11:31:30
	 * </pre>
	 */
	public void notifyForDayMail(KRole role) {
		KRewardLogic.sendDialyMail(role);
	}

	public void notifyForFengceCompetionReward(long roleId, int ranking) {
		FengceCompetitionRewardData reward = KRewardDataManager.mFengceCompetitionRewardManager.getData(ranking);
		if (reward != null) {
			BaseMailContent baseMail = new BaseMailContent(StringUtil.format(reward.mail.getMailTitle(), ranking), StringUtil.format(reward.mail.getMailContent(), ranking), null, null);
			KSupportFactory.getMailModuleSupport().sendAttMailBySystem(roleId, baseMail, reward.reward, PresentPointTypeEnum.封测竞技排名奖励);
		}
	}

	public void notifyForFengceFriendFubenReward(KRole role, int GetQuantity) {
		FengceFriendFubenRewardData reward = KRewardDataManager.mFengceFriendFubenRewardManager.getData(GetQuantity);
		if (reward != null) {
			reward.mailReward.sendReward(role, PresentPointTypeEnum.封测好友副本奖励, false);
		}
	}
}
