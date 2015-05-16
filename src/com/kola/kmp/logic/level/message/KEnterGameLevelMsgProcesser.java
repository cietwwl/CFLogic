package com.kola.kmp.logic.level.message;

import static com.kola.kmp.protocol.level.KLevelProtocol.CM_ENTER_GAME_LEVEL;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.level.FightResult;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.level.copys.KPetChallengeCopyManager;
import com.kola.kmp.logic.level.copys.KTowerCopyManager;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 消息{@link KGameScenarioProtocol#CM_ENTER_GAME_LEVEL}的处理类
 * 
 * @author zhaizl
 */
public class KEnterGameLevelMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KEnterGameLevelMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_ENTER_GAME_LEVEL;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		byte levelType = msg.readByte();
		int levelId = msg.readInt();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);

		KGameLevelTypeEnum levelTypeEnum = KGameLevelTypeEnum
				.getEnum(levelType);
		if (levelTypeEnum == KGameLevelTypeEnum.普通关卡) {
			KGameLevelModuleExtension.getManager().playerRoleJoinGameLevel(
					role, levelId, true, true);
		} else if (levelTypeEnum == KGameLevelTypeEnum.精英副本关卡
				|| levelTypeEnum == KGameLevelTypeEnum.技术副本关卡) {
			KGameLevelModuleExtension
					.getManager()
					.getCopyManager()
					.playerRoleJoinGameLevel(role, levelId, levelTypeEnum,
							true, true);
		} else if (levelTypeEnum == KGameLevelTypeEnum.好友副本关卡) {
			long friendId = msg.readLong();

			KGameLevelModuleExtension.getManager().getFriendCopyManager()
					.playerRoleJoinGameLevel(role, friendId, levelId, true);
		} else if (levelTypeEnum == KGameLevelTypeEnum.新手引导关卡) {
			if (levelId == KGameLevelModuleExtension.getManager().firstNoviceGuideBattle
					.getLevelId()) {

				KSupportFactory.getNoviceGuideSupport()
						.notifyRoleEnterFirstNoviceGuideBattle(role);
			} else if (levelId == KGameLevelModuleExtension.getManager().firstNormalGameLevel
					.getLevelId()) {
				KGameLevelModuleExtension.getManager().playerRoleJoinGameLevel(
						role, levelId, true, true);
			}
		} else if (levelTypeEnum == KGameLevelTypeEnum.随从副本关卡) {
			KGameLevelModuleExtension.getManager().getPetCopyManager()
					.playerRoleJoinGameLevel(role, levelId, true, true);
		} else if (levelTypeEnum == KGameLevelTypeEnum.爬塔副本关卡) {
			KActionResult result = KGameLevelModuleExtension.getManager().getTowerCopyManager().processPlayerRoleJoinLevel(role, levelId);
//			if(result.success){
//				KLevelTemplate level = KTowerCopyManager.towerCopyLevelMap.get(levelId);
//				FightResult fightResult = new FightResult();
//				fightResult.setBattlefieldId(level.getAllNormalBattlefields().get(0).getBattlefieldId());
//				fightResult.setBattlefieldType(KGameBattlefieldTypeEnum.爬塔副本战场);
//				fightResult.setWin(true);
//				KGameLevelModuleExtension.getManager().getTowerCopyManager().processPlayerRoleCompleteCopyLevel(role, level, fightResult);
//			}
		}else if (levelTypeEnum == KGameLevelTypeEnum.随从挑战副本关卡) {
			KActionResult result = KGameLevelModuleExtension.getManager().getPetChallengeCopyManager().processPlayerRoleJoinLevel(role, levelId);
//			if(result.success){
//				KLevelTemplate level = KPetChallengeCopyManager.petChallengeCopyLevelMap.get(levelId);
//				FightResult fightResult = new FightResult();
//				fightResult.setBattlefieldId(level.getAllNormalBattlefields().get(0).getBattlefieldId());
//				fightResult.setBattlefieldType(KGameBattlefieldTypeEnum.随从挑战副本战场);
//				fightResult.setWin(true);
//				KGameLevelModuleExtension.getManager().getPetChallengeCopyManager().processPlayerRoleCompleteCopyLevel(role, level, fightResult);
//			}
		}else if (levelTypeEnum == KGameLevelTypeEnum.高级随从挑战副本关卡) {
			KActionResult result = KGameLevelModuleExtension.getManager().getKSeniorPetChallengeCopyManager().processPlayerRoleJoinLevel(role, levelId);
		}

		KDialogService.sendNullDialog(role);
	}

}
