package com.kola.kmp.logic.mission.assistant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jxl.read.biff.BiffException;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.mission.assistant.KAssistantDataStruct.MajorAssistantData;
import com.kola.kmp.logic.mission.assistant.KAssistantDataStruct.MinorAssistantData;
import com.kola.kmp.logic.mission.guide.KGuideManager;
import com.kola.kmp.logic.other.KNPCOrderEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.protocol.mission.KMissionProtocol;

public class KAssistantManager {
	private static final KGameLogger _LOGGER = KGameLogger
			.getLogger(KAssistantManager.class);

	private static Map<Integer, MajorAssistantData> majorMap = new LinkedHashMap<Integer, MajorAssistantData>();

	private static Map<Integer, MinorAssistantData> minorMap = new LinkedHashMap<Integer, MinorAssistantData>();

	public static String currentNoticeVersion;

	public static List<KNoticeData> noticeList = new ArrayList<KNoticeData>();

	public static void init(String configPath) throws KGameServerException {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(configPath);
		} catch (BiffException e) {
			throw new KGameServerException("读取任务模块missionConfig.xls发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取任务模块missionConfig.xls发生错误！", e);
		}

		if (xlsFile != null) {
			int dataRowIndex = 5;
			KGameExcelTable dataTable = xlsFile.getTable("大项", dataRowIndex);
			KGameExcelRow[] allDataRows = dataTable.getAllDataRows();
			for (int i = 0; i < allDataRows.length; i++) {
				int id = allDataRows[i].getInt("id");
				String name = allDataRows[i].getData("name");
				String minId = allDataRows[i].getData("minId");
				int iconId = allDataRows[i].getInt("iconId");
				MajorAssistantData data = new MajorAssistantData(id, name,
						iconId, minId);
				majorMap.put(id, data);
			}

			dataTable = xlsFile.getTable("小项", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();
			for (int i = 0; i < allDataRows.length; i++) {
				int id = allDataRows[i].getInt("id");
				String name = allDataRows[i].getData("name");
				String desc = allDataRows[i].getData("desc");
				int iconId = allDataRows[i].getInt("iconId");
				int order = allDataRows[i].getInt("orderId");
				KNPCOrderEnum orderType = KNPCOrderEnum.getEnum(order);
				int starIconId = allDataRows[i].getInt("staricon");
				// if(orderType == null){
				//
				// }
				MinorAssistantData data = new MinorAssistantData(id, name,
						desc, iconId, orderType,starIconId);
				minorMap.put(id, data);
			}

			dataTable = xlsFile.getTable("游戏公告", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();
			for (int i = 0; i < allDataRows.length; i++) {
				String title = allDataRows[i].getData("name");
				String content = allDataRows[i].getData("content");
				KNoticeData data = new KNoticeData(title, content);
				noticeList.add(data);
			}
		}
	}

	public static void checkInit() throws KGameServerException {
		boolean isSuccess = true;
		for (MajorAssistantData data : majorMap.values()) {
			for (Integer minId : data.idList) {
				if (minorMap.containsKey(minId)) {
					data.minDataList.add(minorMap.get(minId));
				} else {
					isSuccess = false;
					_LOGGER.error(
							"加载assitantConfig.xls<大项>表的minId错误：不存在的小项ID={}，大项名称={}，大项ID={}",
							minId, data.title, data.id);
				}
			}
		}
		if (!isSuccess) {
			throw new KGameServerException("加载assitantConfig.xls<大项>表的minId错误.");
		}
		for (MinorAssistantData data : minorMap.values()) {
			if (data.npcOrder == null) {
				isSuccess = false;
				_LOGGER.error(
						"加载assitantConfig.xls<小项>表的orderId错误：不存在的打开界面指令，小项名称={}，小项ID={}",
						data.title, data.id);
			}
		}
		if (!isSuccess) {
			throw new KGameServerException(
					"加载assitantConfig.xls<小项>表的orderId错误.");
		}
	}

	public static void sendAssistandData(KRole role) {
		KGameMessage sendMsg = KGame
				.newLogicMessage(KMissionProtocol.SM_SEND_ASSISTANT_DATA);
		sendMsg.writeByte(majorMap.size());
		for (MajorAssistantData data : majorMap.values()) {
			sendMsg.writeInt(data.id);
			sendMsg.writeUtf8String(data.title);
			sendMsg.writeInt(data.iconId);
			sendMsg.writeByte(data.idList.size());
			for (Integer minId : data.idList) {
				sendMsg.writeInt(minId);
			}
		}
		sendMsg.writeByte(minorMap.size());
		for (MinorAssistantData data : minorMap.values()) {
			sendMsg.writeInt(data.id);
			sendMsg.writeUtf8String(data.title);
			sendMsg.writeInt(data.iconId);
			sendMsg.writeUtf8String(data.produceTips);
			sendMsg.writeInt(data.npcOrder.sign);
			sendMsg.writeInt(1);
			sendMsg.writeInt(data.starIconId);
		}
		role.sendMsg(sendMsg);

//		sendNoticeDataMsg(role, true);
	}

	public static void sendNoticeDataMsg(KRole role,boolean isShow) {
		KGameMessage sendMsg = KGame
				.newLogicMessage(KMissionProtocol.SM_SEND_NOTICE_DATA);
		sendMsg.writeByte(noticeList.size());
		for(KNoticeData data:noticeList){
			sendMsg.writeUtf8String(data.title);
			sendMsg.writeUtf8String(data.content);
		}
		sendMsg.writeBoolean(isShow);
		role.sendMsg(sendMsg);
	}
}
