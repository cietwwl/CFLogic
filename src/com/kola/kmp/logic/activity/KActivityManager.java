package com.kola.kmp.logic.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import jxl.read.biff.BiffException;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.XmlUtil;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.activity.KActivity.ActivityGangRewardLogoStruct;
import com.kola.kmp.logic.gamble.wish.KWishSystemManager;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KActivityTimeTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.ActivityTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.activity.KActivityProtocol;
import com.kola.kmp.protocol.competition.KCompetitionProtocol;

public class KActivityManager {
	private static final KGameLogger _LOGGER = KGameLogger
			.getLogger(KActivityManager.class);
	private Map<Integer, KActivity> allActivityMap = new LinkedHashMap<Integer, KActivity>();

	private Map<Integer, KActivity> openActivityMap = new LinkedHashMap<Integer, KActivity>();

	private static KActivityManager instance;

	public KActivityManager() {
		instance = this;
	}

	public static KActivityManager getInstance() {
		return instance;
	}

	public void init(String configPath) throws Exception {
		Document doc = XmlUtil.openXml(configPath);
		if (doc != null) {
			Element root = doc.getRootElement();

			List<Element> actEList = root.getChild("activitConfig")
					.getChildren("activity");
			for (Element actE : actEList) {
				int id = Integer.parseInt(actE.getChildText("id"));
				String classPath = actE.getChildText("classPath");
				String configFilePath = actE.getChildText("configPath");
				KActivity activity = (KActivity) Class.forName(classPath)
						.newInstance();
				activity.activityId = id;
				activity.configFilePath = configFilePath;
				allActivityMap.put(id, activity);
			}

			String excelFilePath = root.getChildText("excelConfigPath");
			loadExcelPath(excelFilePath);

			checkActivityInitCompleted();

		} else {
			throw new NullPointerException("活动模块配置不存在！！");
		}
	}

	public void loadExcelPath(String excelPath) throws KGameServerException {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(excelPath);
		} catch (BiffException e) {
			throw new KGameServerException("读取活动系统excel表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取活动系统excel表头发生错误！", e);
		}

		if (xlsFile != null) {
			// 许愿数据表
			int dataRowIndex = 5;
			KGameExcelTable dataTable = xlsFile.getTable("活动总表", dataRowIndex);
			KGameExcelRow[] allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					int activityId = allDataRows[i].getInt("id");
					if (!allActivityMap.containsKey(activityId)) {
						throw new KGameServerException(
								"初始化Excel《活动总表》的参数id出错，配置文件没有配置该活动，,活动ID："
										+ activityId + "，excel行数："
										+ allDataRows[i].getIndexInFile());
					}
					KActivity activity = allActivityMap.get(activityId);
					activity.loadExcelConfig(allDataRows[i]);
					if(activity.isStart){
						openActivityMap.put(activityId, activity);
					}
				}
			}
		}
	}

	public void checkActivityInitCompleted() throws KGameServerException {
		for (KActivity activity : allActivityMap.values()) {
			if (!activity.isloadExcelConfig) {
				throw new KGameServerException("### 检测活动模块的id为"
						+ activity.activityId + "的活动出错，该活动没有加载Excel《活动总表》的数据。");
			}
			activity.init(activity.configFilePath);
		}
	}

	public void onGameWorldInitComplete() throws KGameServerException {
		for (KActivity activity : allActivityMap.values()) {
			activity.onGameWorldInitComplete();
		}
	}

	public void notifyCacheLoadComplete() throws KGameServerException {
		for (KActivity activity : allActivityMap.values()) {
			activity.notifyCacheLoadComplete();
		}
	}

	public void serverShutdown() {
		for (KActivity activity : allActivityMap.values()) {
			try {
				activity.serverShutdown();
			} catch (KGameServerException e) {
				_LOGGER.error("服务器关闭时，处理结束活动逻辑出现异常，活动Id：" + activity.activityId
						+ "，活动名称：" + activity.activityName, e);
			}
		}
	}

	public void sendAllActivityData(KRole role) {		
		KActivityRoleExtData actData = KActivityRoleExtCaCreator.getActivityRoleExtData(role.getId());
		KGameMessage msg = KGame
				.newLogicMessage(KActivityProtocol.SM_REPONSE_ACTIVITY_LIST);
		msg.writeShort(openActivityMap.size());
		for (KActivity activity : openActivityMap.values()) {
			/**
			 * <pre>
			 * 活动列表Item的数据结构
			 * 		int 活动id
			 * 		byte 活动类型(1.日常 2.定时 3.家族)
			 * 		int 活动icon id
			 * 		string 活动名称
			 *      string 活动时间短描述
			 * 		string 活动描述
			 *      string 产出描述
			 * 		bool 活动是否开放
			 *      int  活动开放的角色等级
			 *      int  活动的排序序号
			 * </pre>
			 */
			msg.writeInt(activity.activityId);
			msg.writeByte(activity.activityType);
			msg.writeInt(activity.iconId);
			msg.writeUtf8String(activity.activityName);
			msg.writeUtf8String(activity.openTimeShortTips);
			msg.writeUtf8String(activity.desc);
			msg.writeUtf8String(activity.mainProduceTips);
			msg.writeUtf8String(activity.openTimeTips);
			msg.writeBoolean(activity.isOpened());
			msg.writeInt(activity.openRoleLv);
			msg.writeInt(activity.serialNum);
			
			activity.activityShowReward.packMsg(msg);
			msg.writeByte(activity.gangLogoList.size());
			for (ActivityGangRewardLogoStruct struct:activity.gangLogoList) {
				msg.writeByte(struct.logoType);
				msg.writeInt(struct.logoId);
			}			
			
			msg.writeBoolean(activity.isLimitJointCount);
			if(activity.isLimitJointCount){
				int restCount = activity.getRestJoinActivityCount(role);
				msg.writeShort(restCount);
			}
			boolean isCdTime = false;
			int restCdTime = 0;
			if(activity.isCdTimeLimit && actData != null){
				if(actData.isActivityCdTime(activity.activityId)){
					isCdTime = true;
					restCdTime = actData.getActivityRestCdTimeSeconds(activity.activityId);
				}
			}
			msg.writeBoolean(isCdTime);
			if(isCdTime){
				msg.writeInt(restCdTime);
			}
		}
		role.sendMsg(msg);
	}

	public KActionResult processPlayerRoleEnterActivity(KRole role,
			int activityId) {
		KActivity activity = openActivityMap.get(activityId);

		if (activity == null) {
			_LOGGER.error("###error：角色请求进入活动发生错误，找不到对应的活动数据。活动ID：" + activityId);
			// 找不到竞技场对象
//			KDialogService.sendSimpleDialog(role, "",
//					GlobalTips.getTipsServerBusy());
			return new KActionResult(false, GlobalTips.getTipsServerBusy());
		}
		
		if(role.getLevel()<activity.openRoleLv){
			return new KActionResult(false, ActivityTips.getTipsNotOpenByRoleLv(activity.getOpenRoleLv()));
		}
		
		return activity.playerRoleJoinActivity(role);
	}
	
	public KActivity getActivity(int id) {
		return this.allActivityMap.get(id);
	}

	public void notifyActivityOpenStatus(int activityId) {
		KActivity activity = allActivityMap.get(activityId);

		if (activity == null) {
			_LOGGER.error("###error：更新活动开启状态时发生错误，找不到对应的活动数据。活动ID："
					+ activityId);
			return;
		}
		List<Integer> deleteList = Collections.emptyList();
		List<KActivity> updateList = new ArrayList<KActivity>();
		updateList.add(activity);

		SendUpdateActivityMsgTask task = new SendUpdateActivityMsgTask(
				deleteList, updateList);
		task.send();
	}
	
	public void notifyRoleJoinedGame(KRole role) {
		KActivity temp;
		for (Iterator<KActivity> itr = allActivityMap.values().iterator(); itr.hasNext();) {
			temp = itr.next();
			try {
				temp.notifyRoleJoinedGame(role);
			} catch (Exception e) {
				_LOGGER.error("通知[{}]角色登录游戏出现异常！角色id：{}", temp.getActivityName(), role.getId());
			}
		}
	}
	
	public void notifyRoleLeavedGame(KRole role) {
		KActivity temp;
		for (Iterator<KActivity> itr = allActivityMap.values().iterator(); itr.hasNext();) {
			temp = itr.next();
			try {
				temp.notifyRoleLeavedGame(role);
			} catch (Exception e) {
				_LOGGER.error("通知[{}]角色离开游戏出现异常！角色id：{}", temp.getActivityName(), role.getId());
			}
		}
	}

	public static class SendUpdateActivityMsgTask implements KGameTimerTask {
		private List<Integer> deleteList;
		private List<KActivity> updateList;

		public SendUpdateActivityMsgTask(List<Integer> deleteList,
				List<KActivity> updateList) {
			this.deleteList = deleteList;
			this.updateList = updateList;
		}

		@Override
		public String getName() {
			return SendUpdateActivityMsgTask.class.getName();
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal timeSignal)
				throws KGameServerException {
			sendUpdateActivityMsgForAllRoles(deleteList, updateList);
			return null;
		}

		@Override
		public void done(KGameTimeSignal timeSignal) {

		}

		@Override
		public void rejected(RejectedExecutionException e) {

		}

		public void send() {
			KGame.newTimeSignal(this, 0, TimeUnit.SECONDS);
		}

		private void sendUpdateActivityMsgForAllRoles(List<Integer> deleteList,
				List<KActivity> updateList) {

			List<Long> allOnlineRoles = KSupportFactory.getRoleModuleSupport()
					.getAllOnLineRoleIds();
			for (Long roleId : allOnlineRoles) {
				KRole role = KSupportFactory.getRoleModuleSupport().getRole(
						roleId);
				KActivityRoleExtData actData = KActivityRoleExtCaCreator.getActivityRoleExtData(roleId);
				
				KGameMessage msg = KGame
						.newLogicMessage(KActivityProtocol.SM_REPONSE_UPDATE_ACTIVITY_LIST);
				// 删除数量
				if (deleteList != null) {
					msg.writeShort(deleteList.size());
					for (Integer actId : deleteList) {
						msg.writeInt(actId);
					}
				} else {
					msg.writeShort(0);
				}
				// 更新数量
				if (updateList != null) {
					msg.writeShort(updateList.size());
					for (KActivity activity : updateList) {
						msg.writeInt(activity.activityId);
						msg.writeByte(activity.activityType);
						msg.writeInt(activity.iconId);
						msg.writeUtf8String(activity.activityName);
						msg.writeUtf8String(activity.openTimeShortTips);
						msg.writeUtf8String(activity.desc);
						msg.writeUtf8String(activity.mainProduceTips);
						msg.writeUtf8String(activity.openTimeTips);
						msg.writeBoolean(activity.isOpened());
						msg.writeInt(activity.openRoleLv);
						msg.writeInt(activity.serialNum);
						
						activity.activityShowReward.packMsg(msg);
						msg.writeByte(activity.gangLogoList.size());
						for (ActivityGangRewardLogoStruct struct:activity.gangLogoList) {
							msg.writeByte(struct.logoType);
							msg.writeInt(struct.logoId);
						}
						
						msg.writeBoolean(activity.isLimitJointCount);
						if(activity.isLimitJointCount){
							msg.writeShort(activity.getRestJoinActivityCount(role));
						}
						boolean isCdTime = false;
						int restCdTime = 0;
						if(activity.isCdTimeLimit && actData != null){
							if(actData.isActivityCdTime(activity.activityId)){
								isCdTime = true;
								restCdTime = actData.getActivityRestCdTimeSeconds(activity.activityId);
							}
						}
						msg.writeBoolean(isCdTime);
						if(isCdTime){
							msg.writeInt(restCdTime);
						}
					}
				} else {
					msg.writeShort(0);
				}
				
				role.sendMsg(msg);
			}
		}

	}
}
