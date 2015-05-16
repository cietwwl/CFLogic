package com.kola.kmp.logic.reward.activatecode;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.KGameExcelFile;
import com.kola.kmp.logic.reward.KRewardSonModuleAbs;
import com.kola.kmp.logic.reward.KRoleReward;
import com.kola.kmp.logic.reward.KRoleRewardSonAbs;
import com.kola.kmp.logic.reward.daylucky.KDayluckyCenter;
import com.kola.kmp.logic.reward.daylucky.KRoleRewardDaylucky;
import com.kola.kmp.logic.reward.daylucky.message.KDayluckySyncMsg;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager;
import com.kola.kmp.logic.role.KRole;

/**
 * <pre>
 * 激活码
 * 
 * @author CamusHuang
 * @creation 2014-7-16 下午6:41:19
 * </pre>
 */
public class KActivateCodeSonModule extends KRewardSonModuleAbs<KRoleRewardActivatecode> {

	public static final KActivateCodeSonModule instance = new KActivateCodeSonModule();

	private KActivateCodeSonModule() {
		super(KRewardSonModuleType.激活码);
	}

	@Override
	public void loadConfig(Element e) throws KGameServerException {
		e = e.getChild("activateCode");
		KActivateCodeDataManager.loadConfig(e);
	}

	@Override
	public void goToParamsRewardData(KGameExcelFile file, int HeaderIndex) throws Exception {
		// 忽略
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		// 忽略
	}

	@Override
	public void notifyCacheLoadComplete() throws KGameServerException {
		//
		KActivateCodeDataManager.notifyCacheLoadComplete();
	}
	
	@Override
	public void goToLoadData(Element excelE) throws Exception {
		Element tempE = excelE.getChild("activateCode");
		String url = tempE.getChildTextTrim("path");
		int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
		{
			KActivateCodeDataManager.goToLoadData(url, HeaderIndex);
		}
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		// 忽略
	}

	public void notifyForDayChangeTask(long nowTime) {
		// 忽略
	}

	public KRoleRewardActivatecode newRewardSon(KRoleReward roleReward, boolean isFirstNew) {
		KRoleRewardActivatecode roleData =  new KRoleRewardActivatecode(roleReward, super.type, isFirstNew);
		return roleData;
	}

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role, long nowTime) {
		// 忽略
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
