package com.kola.kmp.logic.reward.online;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.KGameExcelFile;
import com.kola.kmp.logic.reward.KRewardSonModuleAbs;
import com.kola.kmp.logic.reward.KRoleReward;
import com.kola.kmp.logic.reward.online.message.KOnlineSyncMsg;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 每日幸运
 * 
 * @author CamusHuang
 * @creation 2014-4-22 下午6:08:54
 * </pre>
 */
public class KOnlineSonModule extends KRewardSonModuleAbs<KRoleRewardOnline> {

	public static final KOnlineSonModule instance = new KOnlineSonModule();

	private KOnlineSonModule() {
		super(KRewardSonModuleType.在线奖励);
	}

	@Override
	public void loadConfig(Element e) throws KGameServerException {
		// 忽略

	}

	@Override
	public void goToParamsRewardData(KGameExcelFile file, int HeaderIndex) throws Exception {

		KOnlineDataManager.goToLoadData(file, HeaderIndex);

	}
	
	@Override
	public void goToLoadData(Element excelE) throws Exception {
		// 忽略
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		// 忽略

	}

	@Override
	public void notifyCacheLoadComplete() throws KGameServerException {
		//
		KOnlineDataManager.notifyCacheLoadComplete();
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		// 忽略
	}

	public void notifyForDayChangeTask(long nowTime) {
		// 忽略
	}

	public KRoleRewardOnline newRewardSon(KRoleReward roleReward, boolean isFirstNew) {
		return new KRoleRewardOnline(roleReward, super.type, isFirstNew);
	}

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role, long nowTime) {
		// 已在KRoleReward的登陆中处理跨天数据重置，此处记录上线时刻
		KRoleRewardOnline roleData = KOnlineSonModule.instance.getRewardSon(role.getId());
		roleData.notifyForLogin(nowTime);
		//
		KOnlineSyncMsg.sendMsg(role);

	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session, */KRole role, long nowTime) {
		KRoleRewardOnline roleData = KOnlineSonModule.instance.getRewardSon(role.getId());
		roleData.countOnlineTime(nowTime);
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
		// 忽略

	}

	public void notifyAfterDayChangeTask(long roleId) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		KOnlineSyncMsg.sendMsg(role);
	}
}
