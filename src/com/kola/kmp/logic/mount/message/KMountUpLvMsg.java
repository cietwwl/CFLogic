package com.kola.kmp.logic.mount.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.mount.KMountAttributeProvider;
import com.kola.kmp.logic.mount.KMountLogic;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.MountResult_UpLv;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.mount.KMountProtocol;

public class KMountUpLvMsg implements GameMessageProcesser, KMountProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KMountUpLvMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_MOUNT_UPLV;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int modelId = msg.readInt();
		String payItemCode = msg.readUtf8String();
		boolean isAutoBuy = msg.readBoolean();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			MountResult_UpLv result = new MountResult_UpLv();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, modelId, result);
			return;
		}

		MountResult_UpLv result = KMountLogic.dealMsg_uplvMount(role, modelId, payItemCode, isAutoBuy);
		
		// -------------
		dofinally(session, role, modelId, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, int modelId, MountResult_UpLv result) {
		
		if (result.isLvUp) {
			KPushMountMsg.SM_SYN_MOUNT_UPLV(role, result.mount);
		}

		KGameMessage backmsg = KGame.newLogicMessage(SM_MOUNT_UPLV_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeInt(modelId);
		backmsg.writeInt(result.mount==null?0:result.mount.getExp());
		session.send(backmsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);

		if (result.isLvUp) {
			// 刷新角色属性
			KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(role.getId(), KMountAttributeProvider.getType());
				
			// 通知精彩活动
			KSupportFactory.getExcitingRewardSupport().notifyMountLevelUp(role, result.oldLv, result.newLv);
		}
		
		if (result.isSucess) {
			// 通知日常任务
			KSupportFactory.getMissionSupport().notifyUseFunction(role, KUseFunctionTypeEnum.座驾培养);
			
			// 通知每日任务
			KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.机甲升级);
		}
	}
}
