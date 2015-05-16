package com.kola.kmp.logic.reward.exciting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.combat.api.ICombatMount;
import com.kola.kmp.logic.competition.KCompetitor;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.EquiSetStruct;
import com.kola.kmp.logic.other.KRankTypeEnum;
import com.kola.kmp.logic.rank.KRankLogic;
import com.kola.kmp.logic.rank.Rank;
import com.kola.kmp.logic.rank.RankElementAbs;
import com.kola.kmp.logic.rank.gang.GangRank;
import com.kola.kmp.logic.rank.gang.GangRankElementAbs;
import com.kola.kmp.logic.rank.gang.KGangRankLogic;
import com.kola.kmp.logic.rank.gang.KGangRankTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.ExcitingDataManager.ExcitionActivity;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.ExcitingRuleManager.RewardRule;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.RankTimeLimitRewardDataManager.RankTimeLimitReward;
import com.kola.kmp.logic.reward.exciting.message.KSynDataMsg;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.CompetitionModuleSupport;
import com.kola.kmp.logic.support.GangModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.ResultStructs.BuyExcitingActivityResult;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_SendMail;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.logic.util.tips.ShopTips;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2013-5-21 下午12:24:52
 * </pre>
 */
public class KExcitingCenter {

	private static final Logger _LOGGER = KGameLogger.getLogger(KExcitingCenter.class);

	private KExcitingCenter() {
	}

	static void activityEndNotify(int activityId) {
		ExcitionActivity activity = KExcitingDataManager.mExcitingDataManager.getData(activityId);
		if (activity == null) {
			return;
		}

		long nowTime = System.currentTimeMillis() + 10 * Timer.ONE_SECOND;
		if (activity.caTime.endTime <= nowTime) {
			KSynDataMsg.sendMsgForDelete(activityId);
		}
	}

	static void activityStartNotify(int activityId) {
		ExcitionActivity activity = KExcitingDataManager.mExcitingDataManager.getData(activityId);
		if (activity == null) {
			return;
		}

		long nowTime = System.currentTimeMillis() + 20 * Timer.ONE_SECOND;
		if (activity.caTime.isInEffectTime(nowTime)) {
			KSynDataMsg.sendMsgForAdd(activity);
		}
	}

	/**
	 * <pre>
	 * 精彩活动自动发奖
	 * 
	 * @param activitysMap <角色ID,Map<活动ID,Set<规则ID>>>
	 * @author CamusHuang
	 * @creation 2014-1-11 下午5:34:58
	 * </pre>
	 */
	static void autoCollectExcitingReward(Map<Long, Map<Integer, Set<Integer>>> activitysMap) {
		for (Entry<Long, Map<Integer, Set<Integer>>> entry : activitysMap.entrySet()) {
			long roleId = entry.getKey();

			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role == null) {
				continue;
			}

			RewardResult_SendMail result = new RewardResult_SendMail();
			for (Entry<Integer, Set<Integer>> entry2 : entry.getValue().entrySet()) {
				int activityId = entry2.getKey();
				for (Integer ruleId : entry2.getValue()) {
					RewardResult_SendMail tempResult = collectExcitingReward(role, activityId, ruleId, true);
					if (tempResult.isSucess) {
						result.isSucess = true;
					}
				}
			}
			//
			if (result.isSucess) {
				KDialogService.sendUprisingDialog(role, UtilTool.getNotNullString(null), RewardTips.系统奖励已发送请查看邮件);
				KSynDataMsg.sendMsgForStatus(roleId);
			}
		}
	}

	/**
	 * <pre>
	 * 处理单次充值自动发送
	 * 
	 * 遍历所有活动，若其它条件已满足，只缺单次充值，则发奖
	 * 
	 * @param roleId
	 * @param charge
	 * @author CamusHuang
	 * @creation 2013-12-23 上午11:11:55
	 * </pre>
	 */
	static boolean dealChargeInOneTime(long roleId, int charge, boolean isFirstCharge) {

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role == null) {
			return false;
		}
		KRoleExciting roleExcitingData = KExcitingExtCACreator.getRoleExciting(roleId);
		if (roleExcitingData == null) {
			return false;
		}
		
		RewardResult_SendMail result = new RewardResult_SendMail();
		long nowTime = System.currentTimeMillis();
		for (ExcitionActivity tempActivity : KExcitingDataManager.mExcitingDataManager.getDatas()) {

			if (!tempActivity.caTime.isInEffectTime(nowTime)) {
				continue;
			}

			if (!tempActivity.isSenstiveForChargeInOneTime()) {
				continue;
			}

			if (isFirstCharge && tempActivity.isExcuteFirstCharge()) {
				continue;
			}
			
			for (RewardRule tempRule : tempActivity.ruleMap.values()) {
				
				ExcitiongStatusEnum resultStatus = checkRewardStatus(role, roleExcitingData, tempActivity, tempRule);
				if (resultStatus == ExcitiongStatusEnum.NOT_FINISHED || resultStatus == ExcitiongStatusEnum.COLLECTED || resultStatus == ExcitiongStatusEnum.EMPTY) {
					// 未完成 或 已领奖 或 已发完
					continue;
				}

				if (isFirstCharge && tempRule.isExcuteFirstCharge()) {
					continue;
				}

				if (tempRule.isSenstiveForChargeInOneTime()) {
					if (tempRule.chargeInOneTimeMin <= charge && charge <= tempRule.chargeInOneTimeMax) {
						// 命中、给奖励(可以循环领取)
						sendExcitingRewardFinal(result, role, tempActivity, tempRule, charge, true);

						// 记录次数
						if(roleExcitingData.increaseCollectRuleTime(tempActivity, tempRule)){
							result.isSucess = true;
						}
					}
				}
			}
		}
		
		if(result.isSendByMail){
			KDialogService.sendUprisingDialog(role, UtilTool.getNotNullString(null), RewardTips.系统奖励已发送请查看邮件);
		}
		
		return result.isSucess;
	}

	/**
	 * <pre>
	 * 处理玩家经验任务阶级奖励
	 * 遍历所有活动，若其它条件已满足，只缺本条件，则发奖
	 * 
	 * @param roleId
	 * @param expTaskRewardLv
	 * @author CamusHuang
	 * @creation 2014-12-13 下午3:49:22
	 * </pre>
	 */
	static boolean dealExpTaskRewardLv(long roleId, int expTaskRewardLv) {

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role == null) {
			return false;
		}
		KRoleExciting roleExcitingData = KExcitingExtCACreator.getRoleExciting(roleId);
		if (roleExcitingData == null) {
			return false;
		}

		RewardResult_SendMail result = new RewardResult_SendMail();
		long nowTime = System.currentTimeMillis();
		for (ExcitionActivity tempActivity : KExcitingDataManager.mExcitingDataManager.getDatas()) {

			if (!tempActivity.caTime.isInEffectTime(nowTime)) {
				continue;
			}

			if (!tempActivity.isSenstiveForExpTaskRewardLv()) {
				continue;
			}

			for (RewardRule tempRule : tempActivity.ruleMap.values()) {

				ExcitiongStatusEnum resultStatus = checkRewardStatus(role, roleExcitingData, tempActivity, tempRule);
				if (resultStatus == ExcitiongStatusEnum.NOT_FINISHED || resultStatus == ExcitiongStatusEnum.COLLECTED || resultStatus == ExcitiongStatusEnum.EMPTY) {
					// 未完成 或 已领奖 或 已发完
					continue;
				}

				if (tempRule.isSenstiveForExpTaskRewardLv()) {
					if (tempRule.expTaskRewardLv == expTaskRewardLv) {
						// 命中、给奖励(可以循环领取)
						sendExcitingRewardFinal(result, role, tempActivity, tempRule, 0, true);
						
						// 记录次数
						if(roleExcitingData.increaseCollectRuleTime(tempActivity, tempRule)){
							result.isSucess = true;
						}
					}
				}
			}
		}
		
		if(result.isSendByMail){
			KDialogService.sendUprisingDialog(role, UtilTool.getNotNullString(null), RewardTips.系统奖励已发送请查看邮件);
		}
		
		return result.isSucess;
	}

	/**
	 * <pre>
	 * 处理玩家经验任务阶级奖励
	 * 遍历所有活动，若其它条件已满足，只缺本条件，则发奖
	 * 
	 * @param roleId
	 * @param vitalityTaskRewardLv
	 * @author CamusHuang
	 * @creation 2014-12-13 下午3:49:22
	 * </pre>
	 */
	static boolean dealVitalityTaskRewardLv(long roleId, int vitalityTaskRewardLv) {

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role == null) {
			return false;
		}
		KRoleExciting roleExcitingData = KExcitingExtCACreator.getRoleExciting(roleId);
		if (roleExcitingData == null) {
			return false;
		}

		RewardResult_SendMail result = new RewardResult_SendMail();
		long nowTime = System.currentTimeMillis();
		for (ExcitionActivity tempActivity : KExcitingDataManager.mExcitingDataManager.getDatas()) {

			if (!tempActivity.caTime.isInEffectTime(nowTime)) {
				continue;
			}

			if (!tempActivity.isSenstiveForVitalityRewardLv()) {
				continue;
			}

			for (RewardRule tempRule : tempActivity.ruleMap.values()) {

				ExcitiongStatusEnum resultStatus = checkRewardStatus(role, roleExcitingData, tempActivity, tempRule);
				if (resultStatus == ExcitiongStatusEnum.NOT_FINISHED || resultStatus == ExcitiongStatusEnum.COLLECTED || resultStatus == ExcitiongStatusEnum.EMPTY) {
					// 未完成 或 已领奖 或 已发完
					continue;
				}

				if (tempRule.isSenstiveForVitalityRewardLv()) {
					if (tempRule.vitalityTaskRewardLv == vitalityTaskRewardLv) {
						// 命中、给奖励(可以循环领取)
						sendExcitingRewardFinal(result, role, tempActivity, tempRule, 0, true);
						
						// 记录次数
						if(roleExcitingData.increaseCollectRuleTime(tempActivity, tempRule)){
							result.isSucess = true;
						}
					}
				}
			}
		}
		
		if(result.isSendByMail){
			KDialogService.sendUprisingDialog(role, UtilTool.getNotNullString(null), RewardTips.系统奖励已发送请查看邮件);
		}
		
		return result.isSucess;
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param activity
	 * @param rule
	 * @return 状态（0未达成，1可领取，2邮件已发送奖励，3自动发奖，4发放完毕）
	 * @author CamusHuang
	 * @creation 2013-7-6 上午11:33:42
	 * </pre>
	 */
	static ExcitiongStatusEnum checkRewardStatus(KRoleExciting roleExcitingActivity, ExcitionActivity activity, RewardRule rule) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleExcitingActivity.getRoleId());
		return checkRewardStatus(role, roleExcitingActivity, activity, rule);
	}

	/**
	 * <pre>
	 * 检查【硬条件】和【累计条件】
	 * 
	 * @param activity
	 * @param rule
	 * @return 状态（0未达成，1可领取，2邮件已发送奖励）
	 * @author CamusHuang
	 * @creation 2013-7-6 上午11:33:42
	 * </pre>
	 */
	private static ExcitiongStatusEnum checkRewardStatus(KRole role, KRoleExciting roleExcitingActivity, ExcitionActivity activity, RewardRule rule) {
		// 综合考虑全局和个人次数
		int releaseTime = roleExcitingActivity.getRuleReleaseTime(activity, rule);
		if(releaseTime == 0){
			// 次数已达极限
			return ExcitiongStatusEnum.EMPTY;
		}
		
		// 检查【累计条件】是否达到
		ExcitiongStatusEnum result = roleExcitingActivity.checkRewardStatus(activity, rule);
		if (result == ExcitiongStatusEnum.NOT_FINISHED || result == ExcitiongStatusEnum.COLLECTED) {
			// 已领奖或未完成
			return result;
		}
		
		long roleId = role.getId();
		// 检查【硬条件】是否达到
		{
			if (rule.isSenstiveForLv()) {
				if (role.getLevel() < rule.minLv) {
					return ExcitiongStatusEnum.NOT_FINISHED;
				}
			}

			if (rule.isSenstiveForBattlePow()) {
				if (role.getBattlePower() < rule.minBattlePow) {
					return ExcitiongStatusEnum.NOT_FINISHED;
				}
			}

			if (rule.isSenstiveForMountLv()) {
				ICombatMount mount = KSupportFactory.getMountModuleSupport().getMountOfRole(roleId);
				int mountLv = mount == null ? 0 : mount.getLevel();
				if (mountLv < rule.minMountLv) {
					return ExcitiongStatusEnum.NOT_FINISHED;
				}
			}

			if (rule.isSenstiveForEquiStrongSetLv() || rule.isSenstiveForEquiStoneSetLv() || rule.isSenstiveForEquiStarSetLv()) {
				//
				EquiSetStruct sets = KSupportFactory.getItemModuleSupport().getEquiSets(roleId);
				if (rule.isSenstiveForEquiStrongSetLv()) {
					if (sets.strongSetLv < rule.minEquiStrongSetLv) {
						return ExcitiongStatusEnum.NOT_FINISHED;
					}
				}
				if (rule.isSenstiveForEquiStoneSetLv()) {
					if (sets.stoneSetLv < rule.minEquiStoneSetLv) {
						return ExcitiongStatusEnum.NOT_FINISHED;
					}
				}
				if (rule.isSenstiveForEquiStarSetLv()) {
					if (sets.starSetLv < rule.minEquiStarSetLv) {
						return ExcitiongStatusEnum.NOT_FINISHED;
					}
				}
			}

			if (rule.isSenstiveForHisTotalCharge()) {
				int charge = KSupportFactory.getVIPModuleSupport().getTotalCharge(roleId);
				if (charge < rule.hisTotalCharge) {
					return ExcitiongStatusEnum.NOT_FINISHED;
				}
			}
		}
		
		// 即时触发任务
		if (activity.isSenstiveForChargeInOneTime() || activity.isSenstiveForExpTaskRewardLv() || activity.isSenstiveForVitalityRewardLv()) {
			// 自动发奖
			return ExcitiongStatusEnum.AUTOSENT;
		}

		// 所有条件均已达到
		if (activity.isAutoSendReward) {
			// 非即时触发任务 且 自动发奖 且 完成 加入任务中
			ExcitingTaskManager.AutoCollectedRewardTaskForExciting.instance.addData(roleExcitingActivity.getRoleId(), activity.id, rule.ruleId);
			return ExcitiongStatusEnum.AUTOSENT;
		}

		return ExcitiongStatusEnum.FINISHED;
	}

	public static RewardResult_SendMail dealMsg_getExcitingReward(KRole role, int activityId, int ruleId) {
		return collectExcitingReward(role, activityId, ruleId, false);
	}

	private static RewardResult_SendMail collectExcitingReward(KRole role, int activityId, int ruleId, boolean isAutoCollect) {
		RewardResult_SendMail result = new RewardResult_SendMail();

		ExcitionActivity activity = KExcitingDataManager.mExcitingDataManager.getData(activityId);
		if (activity == null) {
			result.tips = RewardTips.不存在此奖励;
			return result;
		}

		if (!isAutoCollect && activity.isAutoSendReward) {
			result.tips = RewardTips.此奖励为自动发奖无需手动领取;
			return result;
		}

		long nowTime = System.currentTimeMillis();
		if (nowTime > activity.caTime.endTime) {
			result.tips = StringUtil.format(RewardTips.此活动领奖时间已经截止x, activity.caTime.endTime);
			return result;
		}

		RewardRule rule = activity.ruleMap.get(ruleId);
		if (rule == null) {
			result.tips = RewardTips.不存在此奖励;
			return result;
		}

		long roleId = role.getId();
		KRoleExciting roleExcitingActivity = KExcitingExtCACreator.getRoleExciting(roleId);
		
		// 综合考虑全局和个人次数
		int releaseTime = roleExcitingActivity.getRuleReleaseTime(activity, rule);
		if(releaseTime == 0){
			// 次数已达极限
			result.tips = RewardTips.本项奖励已发放完毕;
			return result;
		}

		if (activity.buyPrice != null) {
			// 需要预先支付
			if (!roleExcitingActivity.isPayed(activity)) {
				// 未支付
				result.tips = RewardTips.请先支付参与此活动;
				return result;
			}
		}
		
		// 检查历史条件
		{
			if (rule.isSenstiveForLv()) {
				if (role.getLevel() < rule.minLv) {
					result.tips = StringUtil.format(RewardTips.主角等级未达到x级, rule.minLv);
					return result;
				}
			}

			if (rule.isSenstiveForBattlePow()) {
				if (role.getBattlePower() < rule.minBattlePow) {
					result.tips = StringUtil.format(RewardTips.主角战力未达到x, rule.minBattlePow);
					return result;
				}
			}

			if (rule.isSenstiveForMountLv()) {
				ICombatMount mount = KSupportFactory.getMountModuleSupport().getMountOfRole(roleId);
				int mountLv = mount == null ? 0 : mount.getLevel();
				if (mountLv < rule.minMountLv) {
					result.tips = StringUtil.format(RewardTips.主角机甲等级未达到x级, rule.minMountLv);
					return result;
				}
			}

			if (rule.isSenstiveForEquiStrongSetLv() || rule.isSenstiveForEquiStoneSetLv() || rule.isSenstiveForEquiStarSetLv()) {
				// []{升星套装等级,宝石套装等级,强化套装等级}
				EquiSetStruct sets = KSupportFactory.getItemModuleSupport().getEquiSets(roleId);
				if (rule.isSenstiveForEquiStrongSetLv()) {
					if (sets.strongSetLv < rule.minEquiStrongSetLv) {
						result.tips = RewardTips.装备强化套装未达到要求;
						return result;
					}
				}
				if (rule.isSenstiveForEquiStoneSetLv()) {
					if (sets.stoneSetLv < rule.minEquiStoneSetLv) {
						result.tips = RewardTips.装备宝石套装未达到要求;
						return result;
					}
				}
				if (rule.isSenstiveForEquiStarSetLv()) {
					if (sets.starSetLv < rule.minEquiStarSetLv) {
						result.tips = RewardTips.装备升星套装未达到要求;
						return result;
					}
				}
			}

			if (rule.isSenstiveForHisTotalCharge()) {
				int charge = KSupportFactory.getVIPModuleSupport().getTotalCharge(roleId);
				if (charge < rule.hisTotalCharge) {
					result.tips = StringUtil.format(RewardTips.历史充值金额未满x钻石, rule.hisTotalCharge);
					return result;
				}
			}
		}

		// 检查活动期间统计条件
		result.tips = roleExcitingActivity.collectReward(activity, rule);
		result.isSucess = result.tips == null;
		if (result.isSucess) {
			result.tips = RewardTips.成功领取奖励;
			// 给奖励
			if (rule.isSenstiveForTotalCharge()) {
				sendExcitingRewardFinal(result, role, activity, rule, rule.totalCharge, isAutoCollect);
			} else if (rule.isSenstiveForTotalPay()) {
				sendExcitingRewardFinal(result, role, activity, rule, rule.totalPay, isAutoCollect);
			} else {
				sendExcitingRewardFinal(result, role, activity, rule, 0, isAutoCollect);
			}
			
			// 记录次数
			roleExcitingActivity.increaseCollectRuleTime(activity, rule);

			if (roleExcitingActivity.hasCollectedAllReward(activity)) {
				// 投资计划：已领取完全部奖励
				KSynDataMsg.sendMsgForDelete(activity.id);
			}
		}
		return result;
	}

	private static void sendExcitingRewardFinal(RewardResult_SendMail result, KRole role, ExcitionActivity activity, RewardRule rule, int chargeIngot, boolean isAutoCollect) {
		// 给奖励
		BaseMailRewardData rewardMail = rule.rewardMail;
		if (rule.presentIngotRate > 0 && chargeIngot > 0) {
			// 钻石百分比返现
			List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>(rewardMail.baseRewardData.moneyList);
			moneyList.add(0, new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, chargeIngot * rule.presentIngotRate / 100));
			moneyList = KCurrencyCountStruct.mergeCurrencyCountStructs(moneyList);

			BaseRewardData rewardData = rewardMail.baseRewardData;
			rewardMail = new BaseMailRewardData(1, rewardMail.baseMail, new BaseRewardData(rewardData.attList, moneyList, rewardData.itemStructs, rewardData.fashionTempIdList,
					rewardData.petTempIdList));
		}
		
		RewardResult_SendMail sendMailResult = rewardMail.sendReward(role, PresentPointTypeEnum.精彩活动, isAutoCollect?false:true);
		result.addDataUprisingTips(sendMailResult.getDataUprisingTips());
		result.addUprisingTips(sendMailResult.getUprisingTips());
		//
		if (sendMailResult.isSendByMail) {
			result.isSendByMail = true;
		}

		// 世界广播
		if (rule.worldChat != null) {
			KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(rule.worldChat, role.getExName()), true, true);
		}
		// 记录日志
		_LOGGER.warn("精彩活动领奖邮件发送记录：,角色ID=,{},角色名=,{},活动ID：,{},活动名称：,{},规则ID：,{},奖励内容：,{}", role.getId(), role.getName(), activity.id, activity.title, rule.ruleId, rewardMail.toCVSString());
	}

	/**
	 * <pre>
	 * 排行榜定时活动自动发奖
	 * 
	 * @param rewardID
	 * @return 下轮发奖的时间
	 * @author CamusHuang
	 * @creation 2014-11-27 下午9:28:21
	 * </pre>
	 */
	static long autoCollectRankLimitReward(int rewardId, long nowTime) {

		RankTimeLimitReward data = KExcitingDataManager.mRankTimeLimitRewardDataManager.getData(rewardId);
		if (data == null) {
			return -1;
		}

		// 执行发奖
		sendRankLimitReward(data);

		return data.getNextRewardCollectTime(nowTime);
	}

	private static void sendRankLimitReward(RankTimeLimitReward data) {
		{
			KGangRankTypeEnum ge = KGangRankTypeEnum.getEnum(data.rankType);
			if (ge != null) {
				GangRank<GangRankElementAbs> gangRank = KGangRankLogic.getRank(ge);
				if (gangRank == null) {
					return;
				}

				RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
				GangModuleSupport gangSupport = KSupportFactory.getGangSupport();
				for (int rank = data.startRank; rank <= data.endRank; rank++) {
					GangRankElementAbs e = gangRank.getPublishData().getElementByRank(rank);
					if (e == null) {
						continue;
					}
					// 按职位发奖
					List<Long> roleIds = gangSupport.searchPositions(e.elementId, data.gangPositionSet);
					for (Long roleId : roleIds) {
						KRole role = roleSupport.getRole(roleId);
						if (role == null) {
							continue;
						}

						data.rewardMail.sendReward(role, PresentPointTypeEnum.排行榜定时发奖, false);
					}
				}
				return;
			}
		}
		{
			KRankTypeEnum ge = KRankTypeEnum.getEnum(data.rankType);
			if (ge != null) {
				if (ge == KRankTypeEnum.竞技榜) {
					CompetitionModuleSupport competionSupport = KSupportFactory.getCompetitionModuleSupport();
					RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
					for (int rank = data.startRank; rank <= data.endRank; rank++) {
						KCompetitor e = competionSupport.getCompetitor(rank);
						if (e == null) {
							continue;
						}

						KRole role = roleSupport.getRole(e.getRoleId());
						if (role == null) {
							continue;
						}

						data.rewardMail.sendReward(role, PresentPointTypeEnum.排行榜定时发奖, false);
					}
					return;
				} else {
					Rank<RankElementAbs> roleRank = KRankLogic.getRank(ge);

					RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
					for (int rank = data.startRank; rank <= data.endRank; rank++) {
						RankElementAbs e = roleRank.getPublishData().getElementByRank(rank);
						if (e == null) {
							continue;
						}
						KRole role = roleSupport.getRole(e.elementId);
						if (role == null) {
							continue;
						}

						data.rewardMail.sendReward(role, PresentPointTypeEnum.排行榜定时发奖, false);
					}
					return;
				}
			}
		}
	}

	public static BuyExcitingActivityResult dealMsg_buyActivity(KRole role, int activityId, boolean isConfirm) {
		BuyExcitingActivityResult result = new BuyExcitingActivityResult();

		ExcitionActivity activity = KExcitingDataManager.mExcitingDataManager.getData(activityId);
		if (activity == null) {
			result.tips = ShopTips.此活动未开放;
			return result;
		}

		long nowTime = System.currentTimeMillis();
		if (nowTime > activity.caTime.endTime) {
			result.tips = StringUtil.format(RewardTips.此活动领奖时间已经截止x, activity.caTime.endTimeStr);
			return result;
		}

		if (activity.buyPrice == null) {
			result.tips = RewardTips.本活动无须购买;
			return result;
		}

		if (activity.buyVipLv > 0) {
			int vipLv = KSupportFactory.getVIPModuleSupport().getVipLv(role.getId());
			if (vipLv < activity.buyVipLv) {
				result.tips = StringUtil.format(RewardTips.本活动要求达到VIPx级或以上, activity.buyVipLv);
				return result;
			}
		}

		KRoleExciting roleExcitingActivity = KExcitingExtCACreator.getRoleExciting(role.getId());
		roleExcitingActivity.rwLock.lock();
		try {

			if (roleExcitingActivity.isPayed(activity)) {
				result.tips = RewardTips.请勿重复为此活动支付;
				return result;
			}

			if (!isConfirm) {
				result.isGoConfirm = true;
				result.tips = StringUtil.format(RewardTips.是否支付x数量x货币参与此活动, activity.buyPrice.currencyCount, activity.buyPrice.currencyType.extName);
				return result;
			}

			long decreaseResult = KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), activity.buyPrice, UsePointFunctionTypeEnum.参与精彩活动, true);
			if (decreaseResult < 0) {
				result.isGoMoneyUI = true;
				result.goMoneyUIType = activity.buyPrice.currencyType;
				result.goMoneyUICount = activity.buyPrice.currencyCount - KSupportFactory.getCurrencySupport().getMoney(role.getId(), activity.buyPrice.currencyType);
				result.tips = StringUtil.format(ShopTips.x货币数量不足x, activity.buyPrice.currencyType.extName, activity.buyPrice.currencyCount);
				return result;
			}

			roleExcitingActivity.setPayed(activity, true);

			result.isSucess = true;
			result.tips = RewardTips.参与此活动成功;
			result.addDataUprisingTips(StringUtil.format(ShopTips.x减x, activity.buyPrice.currencyType.extName, activity.buyPrice.currencyCount));
			return result;

		} finally {
			roleExcitingActivity.rwLock.unlock();
		}
	}
}
