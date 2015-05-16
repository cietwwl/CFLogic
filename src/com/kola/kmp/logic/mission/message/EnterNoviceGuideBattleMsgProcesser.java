package com.kola.kmp.logic.mission.message;

import static com.kola.kmp.protocol.mission.KMissionProtocol.CM_ENTER_NOVICE_GUIDE_BATTLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.level.FightEventListener;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.level.gamestory.Animation;
import com.kola.kmp.logic.level.gamestory.AnimationManager;
import com.kola.kmp.logic.mission.IMissionMenuImpl;
import com.kola.kmp.logic.mission.KMission;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.mission.KMissionModuleSupportImpl;
import com.kola.kmp.logic.mission.KMissionSet;
import com.kola.kmp.logic.mission.KMissionTemplate;
import com.kola.kmp.logic.npc.menu.IMissionMenu;
import com.kola.kmp.logic.npc.menu.KMenuService;
import com.kola.kmp.logic.other.KGameMissionFunTypeEnum;
import com.kola.kmp.logic.other.KGameMissionStatusEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.mission.KMissionProtocol;

public class EnterNoviceGuideBattleMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new EnterNoviceGuideBattleMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_ENTER_NOVICE_GUIDE_BATTLE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
//		KGamePlayerSession session = msgEvent.getPlayerSession();
//		KGameMessage msg = msgEvent.getMessage();
//
//		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
//
//		KLevelTemplate level = KGameLevelModuleExtension.getManager()
//				.getNoviceGuideGameLevel();
//		if (level != null) {
//			// List<FightEventListener> listenerList = KGameLevelModuleExtension
//			// .getManager().getFightEventListenerList();
//			//
//			// // 检测是否有触发的剧情
//			// List<Animation> animation = AnimationManager.getInstance()
//			// .getNoviceGuideBattleTypeAnimations()
//			// .get(battle.getLevelId());
//			//
//			// if (animation == null) {
//			// animation = Collections.emptyList();
//			// }
//			//
//			// for (FightEventListener listener : listenerList) {
//			// listener.notifyBattle(role, battleList, animation);
//			// }
//			KGameMessage sendMsg = KGame
//					.newLogicMessage(KMissionProtocol.SM_NOTIFY_ABOUT_ENTER_GUIDE_BATTLE);
//			sendMsg.writeByte(1);
//			role.sendMsg(sendMsg);
//
//			KGameLevelModuleExtension.getManager().playerRoleJoinGameLevel(
//					role, level.getLevelId(), true, true);
//		}
	}
}
