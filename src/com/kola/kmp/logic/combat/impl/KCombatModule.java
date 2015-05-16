package com.kola.kmp.logic.combat.impl;

import static com.kola.kmp.protocol.fight.KFightProtocol.CM_BAEELE_COMMAND_EXIT;
import static com.kola.kmp.protocol.fight.KFightProtocol.CM_BATTLE_COMMAND;
import static com.kola.kmp.protocol.fight.KFightProtocol.CM_BATTLE_STATUS;
import static com.kola.kmp.protocol.fight.KFightProtocol.CM_VERIFY_BATTLE_TIMESTAMP;
import static com.kola.kmp.protocol.fight.KFightProtocol.CM_REQUEST_MORE_MONSTER;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.util.XmlUtil;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatModule {
	
	public static void init(String path) throws Exception  {
		Document doc = XmlUtil.openXml(path);
		Element root = doc.getRootElement();
		KCombatManager.loadCombatResultHandler(root.getChildTextTrim("combatResultHandlerCfgPath"));
		KCombatManager.loadOperationMsgHandler(root.getChild("operationMsgHandler"));
		KCombatManager.loadCommandCreator(root.getChild("commandCreator"));
		KCombatManager.loadMirrorDataHandler(root.getChild("mirrorDataHandler"));
		KCombatManager.loadSkillExecutionParser(root.getChildTextTrim("combatSkillExecutionParser"));
		KCombatManager.loadSpecialEffectFunctionParser(root.getChildTextTrim("combatFunctionCfgPath"));
		KCombatManager.loadStateParser(root.getChildTextTrim("combatStateConfigPath"));
		KCombatManager.loadRandomObstructions(root.getChildTextTrim("randomObstructionPath"));
		KCombatManager.setAssistantPetPath(root.getChildTextTrim("assistantPetPath"));
		KCombatConfig.loadConfig(root.getChildTextTrim("combatParaCfgPath"));
		KCombatManager.submitCombatMonitor();
		KCombatEntrancePool.init();
		KCombatMemberFactory.init();
		KCombatPool.init();
		KCombatGroundPool.init();
		KCombatRecorderPool.init();
	}
	
	public static void onGameWorldInitComplete() throws KGameServerException {
		try {
			KCombatManager.loadSkillExecution();
			KCombatManager.loadSpecialEffectFunction();
			KCombatManager.loadPassiveSkillSpecialEffect();
			KCombatManager.loadObstructionStates();
//			KCombatManager.loadMountConsume();
			KCombatManager.loadAssistantPetOfGameLevel();
		} catch (Exception e) {
			throw new KGameServerException(e);
		}
	}
	
	public static boolean processMsg(KGameMessageEvent msgEvent) {
		int msgId = msgEvent.getMessage().getMsgID();
		switch (msgId) {
		case CM_VERIFY_BATTLE_TIMESTAMP:
		case CM_BATTLE_COMMAND:
		case CM_BATTLE_STATUS:
		case CM_BAEELE_COMMAND_EXIT:
		case CM_REQUEST_MORE_MONSTER:
			handleBattleMsg(msgEvent);
			break;
		default:
			return false;
		}
		return true;
	}
	
	private static void handleBattleMsg(KGameMessageEvent msgEvent) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(msgEvent.getPlayerSession());
		if (role != null) {
			ICombat combat = KCombatManager.getCombat(role.getId());
			if (combat != null) {
				combat.msgReceived(role.getId(), msgEvent.getMessage());
			}
		}
	}
}
