package com.kola.kmp.logic.reward.redress;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.KGameExcelFile;
import com.kola.kmp.logic.reward.KRewardSonModuleAbs;
import com.kola.kmp.logic.reward.KRoleReward;
import com.kola.kmp.logic.role.KRole;

/**
 * <pre>
 * 改版补偿
 * 
 * @author CamusHuang
 * @creation 2014-11-18 下午6:43:02
 * </pre>
 */
public class KRedressSonModule extends KRewardSonModuleAbs<KRoleRedress> {

	public static final KRedressSonModule instance = new KRedressSonModule();
	
	boolean isNotifyCacheLoadComplete = false;

	private KRedressSonModule() {
		super(KRewardSonModuleType.改版补偿);
	}

	@Override
	public void loadConfig(Element e) throws KGameServerException {
		e = e.getChild("redress");
		KRedressDataManagerV1.loadConfig(e.getChild("v1"));
		KRedressDataManagerV2.loadConfig(e.getChild("v2"));
	}

	@Override
	public void goToParamsRewardData(KGameExcelFile file, int HeaderIndex) throws Exception {

	}

	@Override
	public void goToLoadData(Element excelE) throws Exception {
		{
			Element tempE = excelE.getChild("redressV1");
			String url = tempE.getChildTextTrim("path");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			{
				KRedressDataManagerV1.goToLoadData(url, HeaderIndex);
			}
		}
		{
			Element tempE = excelE.getChild("redressV2");
			String url = tempE.getChildTextTrim("path");
			int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
			{
				KRedressDataManagerV2.goToLoadData(url, HeaderIndex);
			}
		}
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		// 忽略

	}

	@Override
	public void notifyCacheLoadComplete() throws KGameServerException {
		isNotifyCacheLoadComplete = true;
		//
		KRedressDataManagerV1.notifyCacheLoadComplete();
		KRedressDataManagerV2.notifyCacheLoadComplete();
		KRedressDataManagerLCS.notifyCacheLoadComplete();
	}
	
	public void afterNotifyCacheLoadComplete() throws KGameServerException{
		// 对已经加入缓存的角色进行补偿
		KRedressCenter.notifyCacheLoadComplete();
	}

	@Override
	public void serverShutdown() throws KGameServerException {
	}

	public void notifyForDayChangeTask(long nowTime) {
	}

	public KRoleRedress newRewardSon(KRoleReward roleReward, boolean isFirstNew) {
		KRoleRedress roleData = new KRoleRedress(roleReward, super.type, isFirstNew);
		return roleData;
	}

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role, long nowTime) {
		// 忽略
	}

	@Override
	public void notifyRoleLeavedGame(/* KGamePlayerSession session, */KRole role, long nowTime) {
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
	}
}
