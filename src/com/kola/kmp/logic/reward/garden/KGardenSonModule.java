package com.kola.kmp.logic.reward.garden;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.KGameExcelFile;
import com.kola.kmp.logic.reward.KRewardModuleExtension;
import com.kola.kmp.logic.reward.KRewardRoleExtCACreator;
import com.kola.kmp.logic.reward.KRewardSonModuleAbs;
import com.kola.kmp.logic.reward.KRoleReward;
import com.kola.kmp.logic.reward.KRoleRewardSonAbs;
import com.kola.kmp.logic.reward.garden.message.KGardenSynMsg;
import com.kola.kmp.logic.reward.login.KRoleRewardLogin;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 庄园
 * 
 * @author CamusHuang
 * @creation 2014-4-22 下午6:08:54
 * </pre>
 */
public class KGardenSonModule extends KRewardSonModuleAbs<KRoleRewardSonAbs> {

	public static final KGardenSonModule instance = new KGardenSonModule();

	private KGardenSonModule() {
		super(KRewardSonModuleType.僵尸庄园);
	}

	@Override
	public void loadConfig(Element e) throws KGameServerException {
		e = e.getChild("garden"); 
		KGardenDataManager.loadConfig(e);
	}

	@Override
	public void goToParamsRewardData(KGameExcelFile file, int HeaderIndex) throws Exception {

		KGardenDataManager.goToLoadData(file, HeaderIndex);

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
		KGardenTaskManager.notifyCacheLoadComplete();
		//
		KGardenDataManager.notifyCacheLoadComplete();
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		// 忽略
	}

	public void notifyForDayChangeTask(long nowTime) {
		for (long roleId : KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds()) {
			KRoleGarden data = KGardenRoleExtCACreator.getRoleGarden(roleId);
			if (data == null) {
				continue;
			}
			
			// 通知在线角色进行个人数据刷新
			if (data.notifyForDayChange(nowTime)) {
				// 进行在线数据同步
				KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
				KGardenSynMsg.sendMyGardenData(role);
			}
		}
	}

	public KRoleRewardSonAbs newRewardSon(KRoleReward roleReward, boolean isFirstNew) {
		return null;
	}

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role, long nowTime) {
		KRoleGarden data = KGardenRoleExtCACreator.getRoleGarden(role.getId());
		// 重置数据
		data.notifyForLogin(nowTime);
		// VIP自动收获
		data.autoRefreshZombieAndCollectForVIP(role, nowTime);
		//
		KGardenSynMsg.sendGardenConstance(role);
		KGardenSynMsg.sendMyGardenData(role);
		KGardenSynMsg.sendVipSaveLogo(role, data);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session, */KRole role, long nowTime) {
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
		// 忽略

	}

	public void notifyAfterDayChangeTask(long roleId) {
		// 忽略
	}
}
