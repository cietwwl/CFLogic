package com.kola.kmp.logic.mount.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.mount.KMountAttributeProvider;
import com.kola.kmp.logic.mount.KMountLogic;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.MountResult_UpBigLv;
import com.kola.kmp.logic.util.ResultStructs.MountResult_UpLv;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.mount.KMountProtocol;

public class KMountUpBigLvMsg implements GameMessageProcesser, KMountProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KMountUpBigLvMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_MOUNT_UPBIGLV;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int modelId = msg.readInt();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			MountResult_UpBigLv result = new MountResult_UpBigLv();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, modelId, result);
			return;
		}

		MountResult_UpBigLv result = KMountLogic.dealMsg_upBigLvMount(role, modelId);
		
		// -------------
		dofinally(session, role, modelId, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, int modelId, MountResult_UpBigLv result) {
		if (result.isSucess) {
			KPushMountMsg.SM_SYN_MOUNT(role, result.mount);
		}
		
		KGameMessage backmsg = KGame.newLogicMessage(SM_MOUNT_UPBIGLV_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeInt(modelId);
		session.send(backmsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);

		if (result.isSucess) {
			if (result.isMountInUsed) {
				// 刷新角色属性
				KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(role.getId(), KMountAttributeProvider.getType());
				
				// 通知地图模块
				KSupportFactory.getMapSupport().notifyMountStatus(role.getId(), result.isMountInUsed, result.mount.getInMapResId());
			}
			
			// 通知日常任务
			KSupportFactory.getMissionSupport().notifyUseFunction(role, KUseFunctionTypeEnum.座驾进阶);
			
			// 世界广播
			KWordBroadcastType _boradcastType = KWordBroadcastType.机甲进阶_XX将机甲进阶到XXXX;
			KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(_boradcastType.content, role.getExName(), result.mount.getName()), _boradcastType);
		}
	}
}
