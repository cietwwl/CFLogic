package com.kola.kmp.logic.activity.worldboss;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.XmlUtil;
import com.kola.kmp.logic.activity.KActivity;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KTableInfo;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;

/**
 * 
 * <pre>
 * 接入活动模块的实现类，主要是转发一些事件到{@link KWorldBossManager}
 * </pre>
 * @author PERRY CHAN
 */
public class KWorldBossActivityImpl extends KActivity {
	
//	void setOpened(boolean flag) {
//		this.isOpened = flag;
//	}
	
	@Override
	public void init(String activityConfigPath) throws KGameServerException {
		Document doc = XmlUtil.openXml(activityConfigPath);
		Map<Byte, KTableInfo> tableMap = new HashMap<Byte, KTableInfo>();
		Element root = doc.getRootElement();
		@SuppressWarnings("unchecked")
		List<Element> tableElements = root.getChild("tableConfigs").getChildren();
		Element child;
		for(int i = 0; i < tableElements.size(); i++) {
			child = tableElements.get(i);
			KTableInfo tableInfo = new KTableInfo(Byte.parseByte(child.getAttributeValue("type")), child.getAttributeValue("name"), Integer.parseInt(child.getAttributeValue("headerIndex")));
			tableMap.put(tableInfo.tableType, tableInfo);
		}
		try {
			KWorldBossManager.loadData(root.getChildText("excelDataPath"), tableMap);
		} catch (Exception e) {
			throw new KGameServerException(e);
		}
		try {
			KWorldBossConfig.init(root.getChildTextTrim("commonConfigPath"), tableMap);
		} catch (Exception e) {
			throw new KGameServerException(e);
		}
		KWorldBossActivityMonitor.setActivityId(activityId);
		KWorldBossActivityMain.loadSavePathInfo(root.getChild("rankingSaveInfo"));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public KActionResult playerRoleJoinActivity(KRole role) {
		CommonResult result = KWorldBossManager.getWorldBossActivity().joinActivity(role);
		KActionResult<Boolean> rtnResult = new KActionResult<Boolean>();
		rtnResult.success = result.isSucess;
		rtnResult.tips = result.tips;
		return rtnResult;
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		KWorldBossManager.onGameWorldInitComplete();
	}

	@Override
	public void notifyCacheLoadComplete() throws KGameServerException {
		
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		try {
			KWorldBossManager.getWorldBossActivity().shutdown();
		} catch (Exception e) {
			throw new KGameServerException(e);
		}
	}
	
	@Override
	public void notifyRoleJoinedGame(KRole role) {
		KWorldBossMessageHandler.syncAutoJoinFlagToClient(role);
	}

	@Override
	public void notifyRoleLeavedGame(KRole role) {
		KWorldBossManager.getWorldBossActivity().notifyRoleLeavedGame(role);
	}

	@Override
	public int getRestJoinActivityCount(KRole role) {
		return 0;
	}

}
