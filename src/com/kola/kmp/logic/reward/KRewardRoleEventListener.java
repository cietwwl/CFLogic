package com.kola.kmp.logic.reward;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.FengceRoleUplvRewardData;
import com.kola.kmp.logic.reward.message.KPhyPowSyncIconMsg;
import com.kola.kmp.logic.reward.redress.KRedressCenter;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2013-1-29 下午5:50:54
 * </pre>
 */
public class KRewardRoleEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		KRoleReward data = KRewardRoleExtCACreator.getRoleReward(role.getId());
		// 尝试跨天数据重置
		data.notifyForLogin();
		
		// 发送维护奖励
		KRewardLogic.sendShutdownReward(role);
		
		// 发送卡机补偿
		KRewardLogic.sendKAJIReward(role);
		
		//
		KPhyPowSyncIconMsg.instance.sendSynMsg(role, KRewardLogic.isShowPhyPowerIcon(role));

		// 通知子模块
		long nowTime = System.currentTimeMillis();
		for (KRewardSonModuleAbs impl : KRewardModuleExtension.rewardSonImplMap.values()) {
			impl.notifyRoleJoinedGame(session, role, nowTime);
		}
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		long nowTime  = System.currentTimeMillis();
		// 通知子模块
		for (KRewardSonModuleAbs impl : KRewardModuleExtension.rewardSonImplMap.values()) {
			impl.notifyRoleLeavedGame(/*session, */role, nowTime);
		}
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		// 通知子模块
		for (KRewardSonModuleAbs impl : KRewardModuleExtension.rewardSonImplMap.values()) {
			impl.notifyRoleCreated(session, role);
		}
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		// 通知子模块
		for (KRewardSonModuleAbs impl : KRewardModuleExtension.rewardSonImplMap.values()) {
			impl.notifyRoleDeleted(roleId);
		}
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		// 通知子模块
		for (KRewardSonModuleAbs impl : KRewardModuleExtension.rewardSonImplMap.values()) {
			impl.notifyRoleLevelUp(role, preLv);
		}
		
		// 发送封测期间升级奖励
		int nowLv = role.getLevel();
		for (int lv = preLv + 1; lv <= nowLv; lv++) {
			FengceRoleUplvRewardData temp = KRewardDataManager.mFengceRoleUpLvRewardManager.getData(lv);
			if (temp != null) {
				temp.reward.sendReward(role, PresentPointTypeEnum.封测角色升级奖励, false);
			}
		}
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		KRedressCenter.notifyForLoadFinished(role);
	}

}
