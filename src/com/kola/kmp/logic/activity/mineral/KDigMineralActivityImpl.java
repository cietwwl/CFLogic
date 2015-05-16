package com.kola.kmp.logic.activity.mineral;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.XmlUtil;
import com.kola.kmp.logic.activity.KActivity;
import com.kola.kmp.logic.activity.mineral.KDigMineralTaskManager.KMineralSyncTask;
import com.kola.kmp.logic.activity.mineral.message.KPushMsg;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapBornPoint;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KDigMineralActivityImpl extends KActivity {

	public static KDigMineralActivityImpl instance;

	@Override
	public void init(String activityConfigPath) throws KGameServerException {
		instance = this;
		isOpened = true;
		//
		Document doc = XmlUtil.openXml(activityConfigPath);
		if (doc != null) {
			Element root = doc.getRootElement();

			KDigMineralDataManager.loadConfig(root.getChild("logicConfig"));

			try {
				Element excelE = root.getChild("excelConfig");
				Element tempE = excelE.getChild("mine");
				int HeaderIndex = Integer.parseInt(tempE.getChildTextTrim("HeaderIndex"));
				KGameExcelFile file = new KGameExcelFile(tempE.getChildTextTrim("path"));
				KDigMineralDataManager.goToLoadData(file, HeaderIndex);
			} catch (Exception e) {
				throw new KGameServerException(e.getMessage(), e);
			}
		} else {
			throw new NullPointerException("配置文件不存在 = " + activityConfigPath);
		}
	}

	@Override
	public void notifyRoleJoinedGame(KRole role) {
		super.notifyRoleJoinedGame(role);

		KRoleDiggerData roleData = KDigMineralActivityManager.getRoleDiggerData(role.getId());
		roleData.notifyForLogin();

		// int dupMapId = KDigMineralDataManager.mineMap.getDuplicateId();
		// roleData.isInMap.set(role.getRoleMapData().getLastMapId() ==
		// dupMapId);

		// 推送初始数据
		KPushMsg.pushDigActivityData(role.getId(), roleData);
		KPushMsg.pushCountDown(role.getId(), roleData);
		//
		KPushMsg.synMineJob(role.getId(), roleData.getMineId());
		// 发送离线奖励统计邮件
		KDigMineralActivityManager.sendOfflineRewardMail(role, roleData);

		// 今天奖励统计
		KPushMsg.pushTodayReward(role, roleData);
	}

	@Override
	public void notifyRoleLeavedGame(KRole role) {
		super.notifyRoleLeavedGame(role);

		KRoleDiggerData roleData = KDigMineralActivityManager.getRoleDiggerData(role.getId());
		roleData.isInMap.set(false);
		// 角色在副本中的坐标
		KDuplicateMapBornPoint orgPoint = KDigMineralActivityManager.getDigPoint(role.getId());
		if (orgPoint != null) {
			roleData.digPoint.set(orgPoint);
		}
	}

	@Override
	public KActionResult playerRoleJoinActivity(KRole role) {

		KRoleDiggerData roleData = KDigMineralActivityManager.getRoleDiggerData(role.getId());

		KDuplicateMapBornPoint orgPoint = roleData.getDigPoint();
		KActionResult result = KSupportFactory.getDuplicateMapSupport().playerRoleJoinDuplicateMap(role, KDigMineralDataManager.mineMap.getDuplicateId(), orgPoint._corX, orgPoint._corY);
		if (result.success) {
			//
			roleData.isInMap.set(true);
			KPushMsg.synMineJob(role.getId(), roleData.getMineId());
			// 今天奖励统计
			KPushMsg.pushTodayReward(role, roleData);
			// 同步周围玩家状态
			KMineralSyncTask.submit(role);
		}
		return result;
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {

	}

	@Override
	public void notifyCacheLoadComplete() throws KGameServerException {
		KDigMineralDataManager.notifyCacheLoadComplete();

		//
		KDigMineralTaskManager.notifyCacheLoadComplete();
	}

	@Override
	public void serverShutdown() throws KGameServerException {

	}

	@Override
	public int getRestJoinActivityCount(KRole role) {
		return 0;
	}

}
