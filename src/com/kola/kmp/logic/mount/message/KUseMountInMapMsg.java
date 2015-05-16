package com.kola.kmp.logic.mount.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.mount.KMountLogic;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.MountResult_Use;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.mount.KMountProtocol;

public class KUseMountInMapMsg implements GameMessageProcesser, KMountProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KUseMountInMapMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_MOUNT_USE_INMAP;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int modelId = msg.readInt();
		boolean isUse = msg.readBoolean();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			MountResult_Use result = new MountResult_Use();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, modelId, isUse, result);
			return;
		}

		MountResult_Use result = KMountLogic.dealMsg_useMountInMap(role, modelId, isUse);
		// -------------
		dofinally(session, role, modelId, isUse, result);

		// 通知地图模块
		if (result.isSucess) {
			KSupportFactory.getMapSupport().notifyMountStatus(role.getId(), isUse, result.mountTemplate.res_id);
		}
	}

	private void dofinally(KGamePlayerSession session, KRole role, int modelId, boolean isUse, MountResult_Use result) {

		KGameMessage backmsg = KGame.newLogicMessage(SM_MOUNT_USE_INMAP_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeInt(modelId);
		backmsg.writeBoolean(isUse);
		session.send(backmsg);

		//
		result.doFinally(role);
	}
}
