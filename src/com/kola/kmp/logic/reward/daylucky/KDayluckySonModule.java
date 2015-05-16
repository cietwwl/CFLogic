package com.kola.kmp.logic.reward.daylucky;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.KGameExcelFile;
import com.kola.kmp.logic.reward.KRewardSonModuleAbs;
import com.kola.kmp.logic.reward.KRoleReward;
import com.kola.kmp.logic.reward.daylucky.KDayluckyDataManager.KDayluckyRateDataManager.DayluckyRateData;
import com.kola.kmp.logic.reward.daylucky.message.KDayluckySyncMsg;
import com.kola.kmp.logic.reward.garden.KGardenDataManager;
import com.kola.kmp.logic.role.KRole;

/**
 * <pre>
 * 每日幸运
 * 
 * @author CamusHuang
 * @creation 2014-4-22 下午6:08:54
 * </pre>
 */
public class KDayluckySonModule extends KRewardSonModuleAbs<KRoleRewardDaylucky> {

	public static final KDayluckySonModule instance = new KDayluckySonModule();

	private KDayluckySonModule() {
		super(KRewardSonModuleType.每日幸运);
	}

	@Override
	public void loadConfig(Element e) throws KGameServerException {
		e = e.getChild("dayluck"); 
		KDayluckyDataManager.loadConfig(e);
	}

	@Override
	public void goToParamsRewardData(KGameExcelFile file, int HeaderIndex) throws Exception {

		KDayluckyDataManager.goToLoadData(file, HeaderIndex);

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
		KDayluckyDataManager.notifyCacheLoadComplete();
		//
		KDayluckyCenter.notifyCacheLoadComplete();
	}

	@Override
	public void serverShutdown() throws KGameServerException {

		KDayluckyCenter.getDialyManager().saveDialys();
	}

	public void notifyForDayChangeTask(long nowTime) {
		// 重置幸运号码数据
		KDayluckyCenter.resetLuckNums();
	}

	public KRoleRewardDaylucky newRewardSon(KRoleReward roleReward, boolean isFirstNew) {
		KRoleRewardDaylucky roleData =  new KRoleRewardDaylucky(roleReward, super.type, isFirstNew);
		//已在KRoleReward的登陆中处理跨天数据重置
		return roleData;
	}

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role, long nowTime) {
		//已在KRoleReward的登陆中处理跨天数据重置
		KDayluckySyncMsg.sendDayluckRewardData(role);
		KDayluckySyncMsg.sendDayluckRoleData(role.getId());

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
		KDayluckySyncMsg.sendDayluckRoleData(roleId);
	}
}
