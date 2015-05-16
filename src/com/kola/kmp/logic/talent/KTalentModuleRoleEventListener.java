package com.kola.kmp.logic.talent;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.other.KRoleExtTypeEnum;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.talent.message.KTalentServerMsgSender;

/**
 * 
 * @author PERRY CHAN
 */
public class KTalentModuleRoleEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		KTalentEntireData entireData = KTalentModuleManager.getTalentEntireData(role.getId());
		if (entireData != null) {
			entireData.onLogin();
		}
		KTalentServerMsgSender.sendAllTalentData(role, session);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		KSupportFactory.getRoleModuleSupport().addExtCAToRole(role.getId(), KRoleExtTypeEnum.TALENT);
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}

}
