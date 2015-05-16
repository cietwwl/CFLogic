package com.kola.kmp.logic.activity.goldact;

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
import com.kola.kmp.logic.other.KNPCOrderEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.ActivityTips;
import com.kola.kmp.protocol.activity.KActivityProtocol;

public class KGoldActivity extends KActivity {

	private static KGoldActivity instance;

	public KGoldActivity() {
		super();
		instance = this;
		this.isOpened = true;
	}

	public static KGoldActivity getInstance() {
		return instance;
	}

	@Override
	public void init(String activityConfigPath) throws KGameServerException {
		KGoldActivityManager.init(activityConfigPath);
		KGoldActivityManager.max_can_challenge_count = this.canJoinCount;
	}

	@Override
	public KActionResult playerRoleJoinActivity(KRole role) {
		// 通知跳转到好友副本界面
		KActivityRoleExtData actData = KActivityRoleExtCaCreator
				.getActivityRoleExtData(role.getId());
		if(actData!=null && actData.isActivityCdTime(activityId)){
			return new KActionResult(false, ActivityTips.getTipsActivityCdTime(activityName));
		}

		return KGoldActivityManager.playerRoleJoinActivity(role);
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		KGoldActivityManager.onGameWorldInitCompleted();
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
		return KActivityRoleExtCaCreator.getGoldActivityRoleRecordData(role
				.getId()).restChallengeCount;
	}

}
