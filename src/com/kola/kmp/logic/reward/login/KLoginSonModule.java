package com.kola.kmp.logic.reward.login;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.KGameExcelFile;
import com.kola.kmp.logic.reward.KRewardSonModuleAbs;
import com.kola.kmp.logic.reward.KRoleReward;
import com.kola.kmp.logic.reward.login.message.KLoginPushMsg;
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
public class KLoginSonModule extends KRewardSonModuleAbs<KRoleRewardLogin> {

	public static final KLoginSonModule instance = new KLoginSonModule();

	private KLoginSonModule() {
		super(KRewardSonModuleType.登陆奖励);
	}

	@Override
	public void loadConfig(Element e) throws KGameServerException {
		e = e.getChild("login"); 
		KLoginDataManager.loadConfig(e);
	}

	@Override
	public void goToParamsRewardData(KGameExcelFile file, int HeaderIndex) throws Exception {

		KLoginDataManager.goToLoadData(file, HeaderIndex);

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
		KLoginDataManager.notifyCacheLoadComplete();
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		// 忽略
	}

	public void notifyForDayChangeTask(long nowTime) {
		// 忽略
	}

	public KRoleRewardLogin newRewardSon(KRoleReward roleReward, boolean isFirstNew) {
		return new KRoleRewardLogin(roleReward, super.type, isFirstNew);
	}

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role, long nowTime) {
		// {// 处理登陆天数
		// KLoginCenter.notifyForLoginOrLogout(role, nowTime);
		// }
		// 已在KRoleReward的登陆中处理跨天数据重置
		//
		
		//
		KLoginPushMsg.sendCheckUpDataMsg(role);
		//
		KLoginPushMsg.sendSevenDataMsg(role);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session, */KRole role, long nowTime) {
		// 已在KRoleReward的登陆中处理跨天数据重置
		// {// 处理登陆天数
		// KLoginCenter.notifyForLoginOrLogout(role, nowTime);
		// }

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
		//
		KLoginPushMsg.sendCheckUpDataMsg(role);
		//
		KLoginPushMsg.sendSevenDataMsg(role);
	}
}
