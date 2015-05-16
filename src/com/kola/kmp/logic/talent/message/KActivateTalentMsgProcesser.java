package com.kola.kmp.logic.talent.message;

import static com.kola.kmp.protocol.talent.KTalentProtocol.CM_ACTIVATE_TALENT;
import static com.kola.kmp.protocol.talent.KTalentProtocol.SM_NOTIFIED_TALENT_ACTIVATE;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.talent.KTalent;
import com.kola.kmp.logic.talent.KTalentEntireData;
import com.kola.kmp.logic.talent.KTalentTree;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.TalentTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KActivateTalentMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KActivateTalentMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_ACTIVATE_TALENT;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		int treeId = msgEvent.getMessage().readInt();
		int talentId = msgEvent.getMessage().readInt();
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		KActionResult<Boolean> result;
		Map<KGameAttrType, Integer> talentTreePreEffect = null;
		Map<KGameAttrType, Integer> preTotalEffect = null;
		Map<KGameAttrType, Integer> talentTreeNowEffect = null;
		Map<KGameAttrType, Integer> nowTotalEffect = null;
		KTalentTree talentTree = null;
		KTalentEntireData entireData = null;
		KTalent talent = null;
		int talentLv = 0;
		if (role != null) {
			entireData = KSupportFactory.getTalentSupport().getTalentData(role.getId());
			result = entireData.checkCondition(treeId, talentId);
			if (result.success) {
				result = entireData.executeConsume(treeId, talentId);
				if(result.success) {
					talentTree = entireData.getTalentTree(treeId);
					talentTreePreEffect = new HashMap<KGameAttrType, Integer>(talentTree.getEffectAttr());
					preTotalEffect = new HashMap<KGameAttrType, Integer>(entireData.getEffectAttr());
					entireData.addTalentLevel(treeId, talentId);
					talentTreeNowEffect = talentTree.getEffectAttr();
					nowTotalEffect = entireData.getEffectAttr();
					result.tips = TalentTips.getTipsTalentLvUpSuccess();
					talentLv = talentTree.getTalent(talentId).getCurrentLevel();
					talent = talentTree.getTalent(talentId);
				}
			}
		} else {
			result = new KActionResult<Boolean>();
			result.tips = GlobalTips.getTipsServerBusy();
		}
		KDialogService.sendUprisingDialog(msgEvent.getPlayerSession(), result.tips);
		if(result.success) {
			Map<KGameAttrType, Integer> treeDifferent = KGameUtilTool.getDifferent(talentTreePreEffect, talentTreeNowEffect);
			Map<KGameAttrType, Integer> totalDifferent = KGameUtilTool.getDifferent(preTotalEffect, nowTotalEffect);
			Map.Entry<KGameAttrType, Integer> entry;
			KGameMessage msg = KGame.newLogicMessage(SM_NOTIFIED_TALENT_ACTIVATE);
			msg.writeInt(treeId);
			msg.writeInt(talentId);
			msg.writeShort(talentLv);
			msg.writeBoolean(talentTree.isActivate());
			msg.writeByte(treeDifferent.size());
			for(Iterator<Map.Entry<KGameAttrType, Integer>> itr = treeDifferent.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				msg.writeShort(entry.getKey().sign);
				msg.writeInt(entry.getValue());
			}
			msg.writeByte(totalDifferent.size());
			for(Iterator<Map.Entry<KGameAttrType, Integer>> itr = totalDifferent.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				msg.writeShort(entry.getKey().sign);
				msg.writeInt(entry.getValue());
			}
			msg.writeInt(talentTree.getBattlePower());
			msg.writeInt(entireData.getTotalBattlePower());
			msg.writeUtf8String(talent.getDescr());
			boolean hasNextDescr = !talent.isMaxLv() && !talent.isSkillTalent();
			msg.writeBoolean(hasNextDescr);
			if(hasNextDescr) {
				msg.writeUtf8String(talent.getNextDescr());
			}
			msgEvent.getPlayerSession().send(msg);
			if(talentTree.isActivate()) {
				KDialogService.sendUprisingDialog(msgEvent.getPlayerSession(), TalentTips.getTipsTalentTreeIsActivate(talentTree.getTalentTitle()));
			}
			if (talent.isSkillTalent()) {
				KRolePasSkillTemp skillTemplate = KSupportFactory.getSkillModuleSupport().getRolePasSkillTemplate(talent.getSkillTemplateId());
				if (skillTemplate != null) {
					KSupportFactory.getChatSupport().sendSystemChat(
							StringUtil.format(KWordBroadcastType.天赋_XX激活了XX天赋获得XX被动技能.content, role.getExName(), talentTree.getTalentTreeName(), skillTemplate.name),
							KWordBroadcastType.天赋_XX激活了XX天赋获得XX被动技能);
				}
			}
		}
	}

}
