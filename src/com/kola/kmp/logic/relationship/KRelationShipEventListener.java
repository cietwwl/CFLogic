package com.kola.kmp.logic.relationship;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.relationship.KRelationShipDataStructs.RSPushData;
import com.kola.kmp.logic.relationship.message.KPushConstanceMsg;
import com.kola.kmp.logic.relationship.message.KPushRSsMsg;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2013-1-29 下午5:50:54
 * </pre>
 */
public class KRelationShipEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		// 清理此角色所有失效的关系（对方被删除）
		KRelationShipLogic.clearAllLoseRelationShips(role);
		// 推送此角色所有关系
		KPushRSsMsg.pushAllRelationShips(role);
		// 推送关系数量上限
		KPushConstanceMsg.pushMsg(role.getId());
		
		// 处理切磋相关逻辑
		IntercourseCenter.notifyRoleJoinedGame(session, role);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		// 忽略
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
		// 好友推荐
		KRelationShipLogic.recommondFriends(role, preLv);
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}

}
