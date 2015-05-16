package com.kola.kmp.logic.skill.message;

import java.util.HashMap;
import java.util.Map;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.KSkillLogic;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.SkillResul;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.skill.KSkillProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KSkillUpdateSlotsMsg implements GameMessageProcesser, KSkillProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KSkillUpdateSlotsMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_SYNC_INSTALLED_SKILLS;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		byte size = msg.readByte();
		for (int i = 0; i < size; i++) {
			map.put(msg.readInt() - 1, msg.readInt());
		}
		// -------------
		SkillResul result = null;
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			result = new SkillResul();
			result.tips = GlobalTips.服务器繁忙请稍候再试;
		} else {
			result = KSkillLogic.dealMsg_updateSkillSlot(role, map);
		}
		// -------------
//		if(result.isSucess){
//			KDialogService.sendUprisingDialog(role, result.tips);
//			KSupportFactory.getRoleModuleSupport().notifySkillListChange(role.getId());
//		} else {
//			KDialogService.sendSimpleDialog(session, "", result.tips, true, (byte)-1);
//			KPushSkillsMsg.pushSelectedSkills(role);
//		}
		if (result.isSucess) {
			KDialogService.sendUprisingDialog(role, result.tips);
		} else {
			KDialogService.sendSimpleDialog(session, "", result.tips, true, (byte) -1);
		}

		KPushSkillsMsg.pushSelectedSkills(role);

		if (result.isChange) {
			// 刷新角色属性
			KSupportFactory.getRoleModuleSupport().notifySkillListChange(role.getId());
		}
	}
}
