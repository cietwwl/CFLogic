package com.kola.kmp.logic.reward.exciting;

import java.io.File;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.gameserver.KGameServer;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.reward.KRewardSonModuleAbs;
import com.kola.kmp.logic.reward.KRoleReward;
import com.kola.kmp.logic.reward.KRoleRewardSonAbs;
import com.kola.kmp.logic.reward.exciting.ExcitingTaskManager.ExcitingActivityTaskDataManager;
import com.kola.kmp.logic.reward.exciting.ExcitingTaskManager.TimeLimitActivityTaskDataManager;
import com.kola.kmp.logic.reward.exciting.message.KPushExcitingActivityDataMsg;
import com.kola.kmp.logic.reward.exciting.message.KSynDataMsg;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 精彩活动
 * 
 * @author CamusHuang
 * @creation 2014-4-22 下午6:08:54
 * </pre>
 */
public class KExcitingSonModule extends KRewardSonModuleAbs<KRoleRewardSonAbs> {

	public static final KExcitingSonModule instance = new KExcitingSonModule();

	private KExcitingSonModule() {
		super(KRewardSonModuleType.精彩活动);
	}

	@Override
	public void loadConfig(Element e) throws KGameServerException {
		e = e.getChild("exciting");
		KExcitingDataManager.loadConfig(e);
	}

	@Override
	public void goToParamsRewardData(KGameExcelFile file, int HeaderIndex) throws Exception {
		// 忽略
	}

	@Override
	public void goToLoadData(Element excelE) throws Exception {
		Element tempE = excelE.getChild("excitingReward");
		String url = tempE.getChildTextTrim("path");
		{
			String newUrl = StringUtil.format(url, KGameServer.getInstance().getGSID());
			File file = new File(newUrl);
			if(file.exists()){
				// 使用有区ID的文件路径
				url = newUrl;
			} else {
				// 使用无区ID的文件路径
				url = StringUtil.format(url, "");
			}
		}
		int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
		{
			KExcitingDataManager.goToLoadData(url, HeaderIndex);
		}
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		// 忽略
	}

	@Override
	public void notifyCacheLoadComplete() throws KGameServerException {
		//
		KExcitingDataManager.notifyCacheLoadComplete();
		//
		ExcitingGlobalDataImpl.instance.load();
		//
		// 启动所有活动的起始和结束任务
		ExcitingActivityTaskDataManager.restartActivityTast();
		// 启动排行榜排名奖励自动发奖任务
		ExcitingTaskManager.restartAutoCollectedRankRewardTask();
		// 启动精彩活动自动发奖任务
		ExcitingTaskManager.startAutoCollectedExcitingRewardTask();
		// 启动精彩活动全服状态同步任务
		ExcitingTaskManager.startAutoSynWorldTimeTaskForExciting();
		// 启动限时奖励活动起始和结束任务
		TimeLimitActivityTaskDataManager.restartAllActivityTast();
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		ExcitingGlobalDataImpl.instance.save();
	}

	public void notifyForDayChangeTask(long nowTime) {
		for (long roleId : KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds()) {
			KRoleExciting data = KExcitingExtCACreator.getRoleExciting(roleId);
			if (data == null) {
				continue;
			}
			
			// 通知在线角色进行个人数据刷新
			if (data.notifyForDayChange(nowTime)) {
				// 进行在线数据同步
				KSynDataMsg.sendMsgForStatus(roleId);
			}
		}
	}

	public KRoleRewardSonAbs newRewardSon(KRoleReward roleReward, boolean isFirstNew) {
		return null;
	}

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role, long nowTime) {
		KRoleExciting roleData = KExcitingExtCACreator.getRoleExciting(role.getId());
		roleData.notifyForLogin(nowTime);

		KPushExcitingActivityDataMsg.sendMsg(session, role.getId());
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session, */KRole role, long nowTime) {
		// 记录在线时长
		long onlineTime = System.currentTimeMillis() - role.getLastJoinGameTime();
		KRoleExciting roleData = KExcitingExtCACreator.getRoleExciting(role.getId());
		roleData.addOnlineTime(onlineTime);
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
		KRoleExciting roleData = KExcitingExtCACreator.getRoleExciting(role.getId());
		if (roleData.notifyLevelUp(preLv, role.getLevel())) {
			KSynDataMsg.sendMsgForStatus(role.getId());
		}
	}

	public void notifyAfterDayChangeTask(long roleId) {
		// 忽略
	}
}
