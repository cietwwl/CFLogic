package com.kola.kmp.logic.reward.vitality;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.KGameExcelFile;
import com.kola.kmp.logic.reward.KRewardSonModuleAbs;
import com.kola.kmp.logic.reward.KRoleReward;
import com.kola.kmp.logic.reward.vitality.message.KVitalityPushFunDataMsg;
import com.kola.kmp.logic.reward.vitality.message.KVitalityPushRewardDataMsg;
import com.kola.kmp.logic.role.KRole;

/**
 * <pre>
 * 每日幸运
 * 
 * @author CamusHuang
 * @creation 2014-4-22 下午6:08:54
 * </pre>
 */
public class KVitalitySonModule extends KRewardSonModuleAbs<KRoleRewardVitality> {

	public static final KVitalitySonModule instance = new KVitalitySonModule();

	private KVitalitySonModule() {
		super(KRewardSonModuleType.活跃度奖励);
	}

	@Override
	public void loadConfig(Element e) throws KGameServerException {
		e = e.getChild("vitality"); 
		KVitalityDataManager.loadConfig(e);
	}

	@Override
	public void goToParamsRewardData(KGameExcelFile file, int HeaderIndex) throws Exception {

		KVitalityDataManager.goToLoadData(file, HeaderIndex);
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
		KVitalityDataManager.notifyCacheLoadComplete();
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		// 忽略
	}

	public void notifyForDayChangeTask(long nowTime) {
		// 忽略
	}

	public KRoleRewardVitality newRewardSon(KRoleReward roleReward, boolean isFirstNew) {
		return new KRoleRewardVitality(roleReward, type, isFirstNew);
	}

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role, long nowTime) {
		//已在KRoleReward的登陆中处理跨天数据重置
		KVitalityPushFunDataMsg.sendMsg(role);
		KVitalityPushRewardDataMsg.sendMsg(role);

	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session, */ KRole role, long nowTime) {
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
//		if(KVitalityDataManager.mFunTypeDataManager.checkOpenType(preLv, role.getLevel())){
//			KVitalityPushFunDataMsg.sendMsg(role);
//		}
		if(KVitalityDataManager.mVitalityRewardDataManager.checkChangeReward(preLv, role.getLevel())){
			KVitalityPushRewardDataMsg.sendMsg(role);
		}
	}

	public void notifyAfterDayChangeTask(long roleId) {
		KVitalityPushFunDataMsg.sendMsg(roleId);
	}
}
