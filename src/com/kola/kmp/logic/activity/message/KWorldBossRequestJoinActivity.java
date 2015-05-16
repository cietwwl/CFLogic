package com.kola.kmp.logic.activity.message;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.activity.KActivityModuleDialogProcesser;
import com.kola.kmp.logic.activity.worldboss.KWorldBossManager;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.npc.message.KShowDialogMsg;
import com.kola.kmp.logic.util.tips.WorldBossTips;
import com.kola.kmp.protocol.activity.KActivityProtocol;

public class KWorldBossRequestJoinActivity implements GameMessageProcesser{

	private static final List<KDialogButton> _buttons;
	
	static {
		_buttons = new ArrayList<KDialogButton>();
		_buttons.add(KDialogButton.CANCEL_BUTTON);
		_buttons.add(new KDialogButton(KActivityModuleDialogProcesser.FUN_CONFIRM_JOIN_WORLD_BOSS, "", KDialogButton.CONFIRM_DISPLAY_TEXT));
	}
	
	@Override
	public GameMessageProcesser newInstance() {
		return new KWorldBossRequestJoinActivity();
	}

	@Override
	public int getMsgIdHandled() {
		return KActivityProtocol.CM_REQUEST_JOIN_WORLD_BOSS_ACTIVITY;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		if (KWorldBossManager.getWorldBossActivity().isWorldBossStart()) {
			KGameMessage msg = KShowDialogMsg.createFunMsg("", WorldBossTips.getTipsWorldBossStartPromptUp(), false, (byte) -1, _buttons);
			msgEvent.getPlayerSession().send(msg);
		} else {
			KDialogService.sendUprisingDialog(msgEvent.getPlayerSession(), WorldBossTips.getTipsWorldBossActivityFinish());
		}
	}

}
