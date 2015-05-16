package com.kola.kmp.logic.level.petcopy;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.KActivity;
import com.kola.kmp.logic.activity.KActivityRoleExtCaCreator;
import com.kola.kmp.logic.activity.KActivityRoleExtData;
import com.kola.kmp.logic.activity.KActivity.ActivityGangRewardLogoStruct;
import com.kola.kmp.logic.activity.newglodact.KNewGoldActivity;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KGameLevelRecord;
import com.kola.kmp.logic.level.KGameLevelSet;
import com.kola.kmp.logic.npc.message.KNPCOrderMsg;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KNPCOrderEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.protocol.activity.KActivityProtocol;

public class KPetCopyActivity extends KActivity{
	
	private static KPetCopyActivity instance;

	public static KPetCopyActivity getInstance() {
		return instance;
	}
	
	public KPetCopyActivity() {
		super();
		instance = this;
		this.isOpened = true;
	}

	@Override
	public void init(String activityConfigPath) throws KGameServerException {

	}

	@Override
	public KActionResult playerRoleJoinActivity(KRole role) {
		// 通知跳转到好友副本界面
		KActionResult result = new KActionResult();
		KNPCOrderMsg.sendNPCMenuOrder(role,
				KNPCOrderEnum.ORDER_OPEN_PET_COPY,
				UtilTool.getNotNullString(null));

		result.success = true;
		result.tips = UtilTool.getNotNullString(null);
		return result;
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {

	}

	@Override
	public void notifyCacheLoadComplete() throws KGameServerException {

	}

	@Override
	public void serverShutdown() throws KGameServerException {
	}

	@Override
	public int getRestJoinActivityCount(KRole role) {
		// 获取关卡记录
				KGameLevelSet levelSet = KGameLevelModuleExtension.getInstance()
						.getGameLevelSet(role.getId());
		KGameLevelRecord record = levelSet
				.getCopyRecord(KGameLevelTypeEnum.随从副本关卡);
		int remainCount = KPetCopyManager.free_challenge_count;
		if (record != null) {
			remainCount = record.petCopyData.remainChallengeCount;
		}
		return remainCount;
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
}
