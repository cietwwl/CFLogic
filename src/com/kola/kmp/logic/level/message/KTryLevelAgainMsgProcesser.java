package com.kola.kmp.logic.level.message;

import static com.kola.kmp.protocol.level.KLevelProtocol.CM_TRY_LEVEL_AGAIN;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.level.KLevelProtocol;
import com.kola.kmp.protocol.mission.KMissionProtocol;

public class KTryLevelAgainMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KTryLevelAgainMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {

		return CM_TRY_LEVEL_AGAIN;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		int levelId = msg.readInt();
		byte levelType = msg.readByte();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);

		KGameLevelTypeEnum levelTypeEnum = KGameLevelTypeEnum
				.getEnum(levelType);

		KActionResult result = null;
		if (levelTypeEnum == KGameLevelTypeEnum.普通关卡) {
			result = KGameLevelModuleExtension.getManager()
					.playerRoleJoinGameLevel(role, levelId, true, false);
		} else if (levelTypeEnum == KGameLevelTypeEnum.精英副本关卡
				|| levelTypeEnum == KGameLevelTypeEnum.技术副本关卡) {
			result = KGameLevelModuleExtension
					.getManager()
					.getCopyManager()
					.playerRoleJoinGameLevel(role, levelId, levelTypeEnum,
							true, false);
		} else if (levelTypeEnum == KGameLevelTypeEnum.好友副本关卡) {
			long friendId = msg.readLong();

			result = KGameLevelModuleExtension.getManager()
					.getFriendCopyManager()
					.playerRoleJoinGameLevel(role, friendId, levelId, true);
		} else if (levelTypeEnum == KGameLevelTypeEnum.新手引导关卡) {
			if (levelId == KGameLevelModuleExtension.getManager().firstNoviceGuideBattle
					.getLevelId()) {

				KSupportFactory.getNoviceGuideSupport()
						.notifyRoleEnterFirstNoviceGuideBattle(role);
				result = new KActionResult(true, "");
			} else if (levelId == KGameLevelModuleExtension.getManager().firstNormalGameLevel
					.getLevelId()) {
				KGameMessage sendMsg = KGame
						.newLogicMessage(KMissionProtocol.SM_NOTIFY_ABOUT_ENTER_GUIDE_BATTLE);
				sendMsg.writeByte(1);
				role.sendMsg(sendMsg);

				KGameLevelModuleExtension.getManager().playerRoleJoinGameLevel(
						role, levelId, true, false);
				result = new KActionResult(true, "");
			}
		} else if (levelTypeEnum == KGameLevelTypeEnum.随从副本关卡) {
			result = KGameLevelModuleExtension.getManager().getPetCopyManager()
					.playerRoleJoinGameLevel(role, levelId, true, false);
		}

		if (result != null) {
			KGameMessage sendMsg = KGame
					.newLogicMessage(KLevelProtocol.SM_TRY_LEVEL_AGAIN);
			sendMsg.writeInt(levelId);
			sendMsg.writeBoolean(result.success);
			sendMsg.writeUtf8String(result.tips);
			role.sendMsg(sendMsg);
		}

	}

}
