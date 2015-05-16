package com.kola.kmp.logic.mount.message;

import java.util.Map.Entry;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.mount.KMountAttributeProvider;
import com.kola.kmp.logic.mount.KMountLogic;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.MountResult_UpLvSkill;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.mount.KMountProtocol;

public class KMountUpLvSkillMsg implements GameMessageProcesser, KMountProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KMountUpLvSkillMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_MOUNT_UPLV_SKILL;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		// 处理消息的过程
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		int modelId = msg.readInt();
		int skillId = msg.readInt();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			MountResult_UpLvSkill result = new MountResult_UpLvSkill();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, modelId, skillId, result);
			return;
		}

		MountResult_UpLvSkill result = KMountLogic.dealMsg_uplvSkill(role, modelId, skillId);

		dofinally(session, role, modelId, skillId, result);
	}

	private void dofinally(KGamePlayerSession session, KRole role, int modelId, int skillId, MountResult_UpLvSkill result) {
		KGameMessage backmsg = KGame.newLogicMessage(SM_MOUNT_UPLV_SKILL_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeInt(modelId);
		backmsg.writeInt(skillId);
		backmsg.writeInt(result.releaseSP);
		session.send(backmsg);

		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);
	}
}
