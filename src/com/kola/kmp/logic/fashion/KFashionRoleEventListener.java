package com.kola.kmp.logic.fashion;

import java.util.HashMap;
import java.util.List;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.fashion.KFashionDataStructs.KFashionTemplate;
import com.kola.kmp.logic.fashion.KRoleFashion.FashionData;
import com.kola.kmp.logic.fashion.message.KPushFashionsMsg;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2013-1-29 下午5:50:54
 * </pre>
 */
public class KFashionRoleEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		//KFashionLogic.clearTimeOutFashion(role, false);
		//
		KPushFashionsMsg.pushAllFashions(role);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		//KFashionLogic.clearTimeOutFashion(role, false);
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		// 忽略
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		// 忽略
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		KPushFashionsMsg.pushAllFashions(role);
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}

}
