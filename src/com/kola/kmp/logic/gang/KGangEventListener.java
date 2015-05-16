package com.kola.kmp.logic.gang;

import java.util.Arrays;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.gang.GangIntegrateData;
import com.kola.kmp.logic.gang.message.KPushGangConstanceMsg;
import com.kola.kmp.logic.gang.message.KSyncAppChangeCountMsg;
import com.kola.kmp.logic.gang.message.KSyncMemberListMsg;
import com.kola.kmp.logic.gang.message.KSyncOwnGangDataMsg;
import com.kola.kmp.logic.gang.war.KGangWarDataManager;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KGangMedalDataManager.GangMedalData;
import com.kola.kmp.logic.gang.war.message.KGWPushMsg;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2013-1-29 下午5:50:54
 * </pre>
 */
public class KGangEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		// 上线清理一次军团模块角色锁
		KGangLogic.mGangMappingDataManager.unlockRoleId(role.getId());
		//
		KRoleGangData roleData = KGangRoleExtCACreator.getData(role.getId(), true);
		roleData.notifyForLogin(System.currentTimeMillis());

		// // 发送军团模块常量
		KPushGangConstanceMsg.sendMsg(session);

		GangIntegrateData gangData = KGangLogic.getGangAndSetByRoleId(role.getId());
		if (gangData != null) {
			KGang gang = (KGang) gangData.getGang();
			KGangExtCASet set = (KGangExtCASet) gangData.getGangExtCASet();

			KGangMember mem = gang.getMember(role.getId());
			mem.setLastSynContributionDataTime(0);

			// 推送军团数据
			KSyncOwnGangDataMsg.sendMsg(role.getId(), gang, set);

			// 推送APP界面列表数量
			if (mem.getPositionEnum() != KGangPositionEnum.成员) {
				int count = set.getAppCache().getDataCache().size();
				KSyncAppChangeCountMsg.sendMsg(session, count);
			}

			// 更新成员列表
			KSyncMemberListMsg.sendMsg(gang, Arrays.asList(gang.getMember(role.getId())), null);

			// 军团频道通知
			
			
//			GangMedalData medal = KGangWarDataManager.mGangMedalDataManager.getDataByRank(1);
//			KGangLogic.sendGangWarFinalReward(gangData.getGang().getId(), medal);
//			KGangLogic.clearGangWarMedalForSignUpEnd();
			
			// 勋章同步
			{
				GangMedalData medal = KGangWarDataManager.mGangMedalDataManager.getDataByRank(mem.getMedal());
				if (medal != null) {
					KGWPushMsg.syncMedal(mem._roleId, medal, mem.hasShowedMedal());
				}
			}
		}
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/ KRole role) {
		// 取消军团搜索CD
		KGangLogic.mGangSearchCDManager.removeCD(role.getId());

		KGang gang = KGangLogic.getGangByRoleId(role.getId());
		if (gang != null) {
			// 更新成员列表
			KSyncMemberListMsg.sendMsg(gang, Arrays.asList(gang.getMember(role.getId())), null);

			// 军团频道通知
		}
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		// 忽略
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		// 清理所有申请书
		KGangLogic.clearAppFromAllGangs(roleId);

		// 角色删除，退出军团
		KGangLogic.notifyRoleDeleted(roleId);
		
		// 取消军团搜索CD
		KGangLogic.mGangSearchCDManager.removeCD(roleId);
		// 取消角色ID与军团ID映射
		KGangLogic.mGangMappingDataManager.removeRoleIdToGangId(roleId);
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		// CTEST 军团--角色升级，通知军团成员数据变更
		KGang gang = KGangLogic.getGangByRoleId(role.getId());
		if (gang != null) {
			// 更新成员列表
			KSyncMemberListMsg.sendMsg(gang, Arrays.asList(gang.getMember(role.getId())), null);
		} else {
			// 通知所有APP
			// long nowTime = System.currentTimeMillis();
			// List<Long> gangIdList =
			// KSupportFactory.getDataCacheSupport().getAllGangIds();
			// for (Long idL : gangIdList) {
			// gang = KGangLogic.getGang(idL);
			// if (gang != null) {
			// gang.notifyAppDataChange(roleId, nowTime);
			// }
			// }
		}
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}

}
