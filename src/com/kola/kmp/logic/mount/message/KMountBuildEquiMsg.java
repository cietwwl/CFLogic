package com.kola.kmp.logic.mount.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.mount.KMountAttributeProvider;
import com.kola.kmp.logic.mount.KMountLogic;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.MountResult_BuildEqui;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.mount.KMountProtocol;

/**
 * <pre>
 * 装备强化消息
 * 
 * @author CamusHuang
 * @creation 2012-12-10 下午4:35:52
 * </pre>
 */
public class KMountBuildEquiMsg implements GameMessageProcesser, KMountProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KMountBuildEquiMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_MOUNT_BUILD_EQUI;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int modelId = msg.readInt();
		int equiId = msg.readInt();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			MountResult_BuildEqui result = new MountResult_BuildEqui();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, modelId, equiId, result);
			return;
		}

		MountResult_BuildEqui result = KMountLogic.dealMsg_buildEqui(role, modelId, equiId);
		
		// -------------
		dofinally(session, role, modelId, equiId, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, int modelId, int equiId, MountResult_BuildEqui result) {

		KGameMessage backmsg = KGame.newLogicMessage(SM_MOUNT_BUILD_EQUI_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeInt(modelId);
		backmsg.writeInt(equiId);
		session.send(backmsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);

		if (result.isSucess) {
			
			KPushMountMsg.SM_SYN_MOUNT(role, result.mount);
			
			if(result.isMountInUsed){
				// 刷新角色属性
				KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(role.getId(), KMountAttributeProvider.getType());
			}
		}
	}
}
