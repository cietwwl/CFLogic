package com.kola.kmp.logic.vip;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;
import com.kola.kmp.logic.vip.message.KPushConstanceMsg;
import com.kola.kmp.logic.vip.message.KSyncVipDataMsg;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2013-1-29 下午5:50:54
 * </pre>
 */
public class KVIPRoleEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {

		KRoleVIP vip = KVIPRoleExtCACreator.getRoleVIP(role.getId());
		vip.notifyForLogin();
		//
		KPushConstanceMsg.sendMsg(role);
		//
		KSyncVipDataMsg.sendMsg(role, vip);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		// 忽略
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		// 封测阶段角色升级时，赠送VIP等级
		presentVip(role, 1);
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		// 忽略
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		// 封测阶段角色升级时，赠送VIP等级
		presentVip(role, preLv);
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}
	
	/**
	 * <pre>
	 * 封测阶段角色升级时，赠送VIP等级
	 * 
	 * @param role
	 * @param preLv
	 * @author CamusHuang
	 * @creation 2014-8-14 下午4:01:06
	 * </pre>
	 */
	private static void presentVip(KRole role, int preLv){
		VIPLevelData vipData = KVIPDataManager.mVIPLevelDataManager.searchDataByRoleLv(preLv, role.getLevel());
		if(vipData != null){
			KRoleVIP vip = KVIPRoleExtCACreator.getRoleVIP(role.getId());
			if(vip.getLv() < vipData.lvl){
				KVIPLogic.presentVIPLv(role, vipData.lvl);
			}
		}
	}

}
