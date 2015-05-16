package com.kola.kmp.logic.mission.message;

import static com.kola.kmp.protocol.mission.KMissionProtocol.CM_NOTIFY_FIRST_ANIMATION_COMPLETE;

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

public class CompleteNoviceGuideAnimationMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new CompleteNoviceGuideAnimationMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_NOTIFY_FIRST_ANIMATION_COMPLETE;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();

		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);

		KSupportFactory.getNoviceGuideSupport()
		.notifyRoleEnterFirstNoviceGuideBattle(role);
	}
}
