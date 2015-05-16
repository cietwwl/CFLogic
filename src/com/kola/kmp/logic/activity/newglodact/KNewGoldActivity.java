package com.kola.kmp.logic.activity.newglodact;

import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.KActivity;
import com.kola.kmp.logic.activity.KActivityRoleExtCaCreator;
import com.kola.kmp.logic.activity.KActivityRoleExtData;
import com.kola.kmp.logic.activity.KActivity.ActivityGangRewardLogoStruct;
import com.kola.kmp.logic.npc.message.KNPCOrderMsg;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.other.KNPCOrderEnum;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.ActivityTips;
import com.kola.kmp.protocol.activity.KActivityProtocol;

public class KNewGoldActivity extends KActivity {

	private static KNewGoldActivity instance;

	public KNewGoldActivity() {
		super();
		instance = this;
		this.isOpened = true;
	}

	public static KNewGoldActivity getInstance() {
		return instance;
	}

	@Override
	public void init(String activityConfigPath) throws KGameServerException {
		KNewGoldActivityManager.init(activityConfigPath);
		KNewGoldActivityManager.max_can_challenge_count = this.canJoinCount;
	}

	@Override
	public KActionResult playerRoleJoinActivity(KRole role) {
		// 通知跳转到好友副本界面
		KActivityRoleExtData actData = KActivityRoleExtCaCreator
				.getActivityRoleExtData(role.getId());
		if(actData!=null && actData.isActivityCdTime(activityId)){
			return new KActionResult(false, ActivityTips.getTipsActivityCdTime(activityName));
		}

		return KNewGoldActivityManager.playerRoleJoinActivity(role);
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		KNewGoldActivityManager.onGameWorldInitComplete();
	}

	@Override
	public void notifyCacheLoadComplete() throws KGameServerException {

	}

	@Override
	public void serverShutdown() throws KGameServerException {
	}

	public void sendUpdateActivity(KRole role) {
		KActivityRoleExtData actData = KActivityRoleExtCaCreator
				.getActivityRoleExtData(role.getId());

		KGameMessage msg = KGame
				.newLogicMessage(KActivityProtocol.SM_REPONSE_UPDATE_ACTIVITY_LIST);

		msg.writeShort(0);

		// 更新数量
		msg.writeShort(1);
		msg.writeInt(this.activityId);
		msg.writeByte(this.activityType);
		msg.writeInt(this.iconId);
		msg.writeUtf8String(this.activityName);
		msg.writeUtf8String(this.openTimeShortTips);
		msg.writeUtf8String(this.desc);
		msg.writeUtf8String(this.mainProduceTips);
		msg.writeUtf8String(this.openTimeTips);
		msg.writeBoolean(this.isOpened());
		msg.writeInt(this.openRoleLv);
		msg.writeInt(this.serialNum);
		
		this.activityShowReward.packMsg(msg);
		msg.writeByte(this.gangLogoList.size());
		for (ActivityGangRewardLogoStruct struct:this.gangLogoList) {
			msg.writeByte(struct.logoType);
			msg.writeInt(struct.logoId);
		}
		
		msg.writeBoolean(this.isLimitJointCount);
		if (this.isLimitJointCount) {
			msg.writeShort(this.getRestJoinActivityCount(role));
		}
		boolean isCdTime = false;
		int restCdTime = 0;
		if (this.isCdTimeLimit && actData != null) {
			if (actData.isActivityCdTime(this.activityId)) {
				isCdTime = true;
				restCdTime = actData
						.getActivityRestCdTimeSeconds(this.activityId);
			}
		}
		msg.writeBoolean(isCdTime);
		if (isCdTime) {
			msg.writeInt(restCdTime);
		}

		role.sendMsg(msg);

	}

	@Override
	public int getRestJoinActivityCount(KRole role) {
		NewGoldActivityRoleRecordData data = KActivityRoleExtCaCreator.getNewGoldActivityRoleRecordData(role
					.getId());
		data.checkAndRestData(true);
		
		int addChallengeCount = 0;
		TimeLimieProduceActivity activity1 = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.赚金币增加挑战次数);
		if(activity1!=null && activity1.isActivityTakeEffectNow()){
			addChallengeCount = activity1.discountTimes;
		}
		return (data.restChallengeCount + addChallengeCount);
	}

}
