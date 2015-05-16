package com.kola.kmp.logic.mission.guide;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import jxl.read.biff.BiffException;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.StringUtil;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.level.gamestory.AnimationManager;
import com.kola.kmp.logic.mission.KMissionCompleteRecordSet;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.mission.KMissionSet;
import com.kola.kmp.logic.mission.KMissionTemplate;
import com.kola.kmp.logic.mission.daily.DailyMissionCheckerTask;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.mission.KMissionProtocol;

public class KGuideManager {
	private static final KGameLogger _LOGGER = KGameLogger.getLogger(KGuideManager.class);

	public static boolean isTestOpenAllFunction = false;

	public static boolean isCloseNoviceGuide = false;

	public static String funcionConfigFilePath = null;

	private static long lastConfigModifyTime;

	private static HashMap<Short, MainMenuFunction> mainMenuFunctionInfoMap = new LinkedHashMap<Short, MainMenuFunction>();

	public static void init(String configPath) throws KGameServerException {
		funcionConfigFilePath = configPath;

		File confile = new File(configPath);
		if (confile.exists() && confile.isFile()) {
			lastConfigModifyTime = confile.lastModified();
		}

		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(configPath);
		} catch (BiffException e) {
			throw new KGameServerException("读取任务模块missionConfig.xls发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取任务模块missionConfig.xls发生错误！", e);
		}

		if (xlsFile != null) {
			int dataRowIndex = 2;
			KGameExcelTable dataTable = xlsFile.getTable("主菜单功能类型信息", dataRowIndex);
			KGameExcelRow[] allDataRows = dataTable.getAllDataRows();
			for (int i = 0; i < allDataRows.length; i++) {
				short functionId = allDataRows[i].getShort("functionId");
				String functionName = allDataRows[i].getData("functionName");
				byte functionOpenType = allDataRows[i].getByte("functionOpenType");
				byte functionGuideType = allDataRows[i].getByte("guideType");

				int openRoleLevelLimit = allDataRows[i].getInt("roleLevel");
				int openCompleteMissionTemplateId = allDataRows[i].getInt("missionTemplateId");
				int icon = allDataRows[i].getInt("iconResId");
				String desc = allDataRows[i].getData("desc");
				boolean isOpenShowIcon = allDataRows[i].getBoolean("isShowIcon");
				boolean isSecondGuide = allDataRows[i].getBoolean("isSecondGuide");
				int secondGuideMissionId = allDataRows[i].getInt("secondGuideMissionId");
				int secondGuideLv = allDataRows[i].getInt("secondGuidelv");
				int secondGuideCount = allDataRows[i].getInt("secondGuideCount");
				boolean iconShowStatus = allDataRows[i].getBoolean("iconShowStatus");
				MainMenuFunction fun = new MainMenuFunction(functionId, functionName, functionOpenType, openRoleLevelLimit, openCompleteMissionTemplateId, functionGuideType, icon, desc,
						isOpenShowIcon, isSecondGuide, secondGuideLv, secondGuideMissionId, secondGuideCount, iconShowStatus);
				mainMenuFunctionInfoMap.put(functionId, fun);
			}
		}
		
		FunctionOpenStateCheckTask checkerTask = new FunctionOpenStateCheckTask();
		KGame.getTimer().newTimeSignal(checkerTask, 30, TimeUnit.SECONDS);
	}

	public static void checkInit() throws KGameServerException {
		boolean isInitSuccess = true;
		Set<String> errorSet = new HashSet<String>();
		for (MainMenuFunction fun : mainMenuFunctionInfoMap.values()) {
			if (fun.getFunctionOpenType() < MainMenuFunction.FUNCTION_OPEN_TYPE_ROLE_LEVEL || fun.getFunctionOpenType() > MainMenuFunction.FUNCTION_GUIDE_TYPE_MISSION) {
				errorSet.add(StringUtil.format("加载missionConfig.xls<主菜单功能类型信息>表的functionOpenType错误：不合法的属性值={}，功能名称={}，功能ID={}", fun.getFunctionOpenType(), fun.getFunctionName(), fun.getFunctionId()));
				isInitSuccess = false;
			}
			if (fun.getFunctionOpenType() == MainMenuFunction.FUNCTION_GUIDE_TYPE_MISSION) {
				if (fun.getOpenCompleteMissionTemplateId() != 0) {
					if (KMissionModuleExtension.getManager().getMissionTemplate(fun.getOpenCompleteMissionTemplateId()) == null) {
						errorSet.add(StringUtil.format("加载missionConfig.xls<主菜单功能类型信息>表的missionTemplateId错误：不存在的任务模版ID={}，功能名称={}，功能ID={}", fun.getOpenCompleteMissionTemplateId(),
								fun.getFunctionName(), fun.getFunctionId()));
					}
				} else {
					errorSet.add(StringUtil.format("加载missionConfig.xls<主菜单功能类型信息>表的missionTemplateId错误：当功能开启方式是任务开启时，任务模版ID不能为0", fun.getOpenCompleteMissionTemplateId(), fun.getFunctionName(),
							fun.getFunctionId()));
				}
			}

			if (fun.isSecondGuide()) {
				if (KMissionModuleExtension.getManager().getMissionTemplate(fun.getSecondGuideMissionTemplateId()) == null) {
					errorSet.add(StringUtil.format("加载missionConfig.xls<主菜单功能类型信息>表的secondGuideMissionId错误：不存在的任务模版ID={}，功能名称={}，功能ID={}", fun.getSecondGuideMissionTemplateId(),
							fun.getFunctionName(), fun.getFunctionId()));
				}
			}
		}
		if (!isInitSuccess) {
			for (String errorMsg : errorSet) {
				_LOGGER.error(errorMsg);
			}
			throw new KGameServerException("加载missionConfig.xls<主菜单功能类型信息>表的functionOpenType错误.");
		}

	}

	public static HashMap<Short, MainMenuFunction> getMainMenuFunctionInfoMap() {
		return mainMenuFunctionInfoMap;
	}

	/**
	 * 检测处理角色等级对应的新功能开放
	 * 
	 * @param role
	 * @param roleLevel
	 */
	public static void processOpenNewFunctionByMissionCompleted(KRole role, int missionTemplateId) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());
		List<MainMenuFunction> openFunList = new ArrayList<MainMenuFunction>();

		for (MainMenuFunction info : mainMenuFunctionInfoMap.values()) {
			if (missionSet.funtionMap.containsKey(info.getFunctionId()) && !missionSet.funtionMap.get(info.getFunctionId()).isOpen) {
				if (info.getFunctionOpenType() == MainMenuFunction.FUNCTION_OPEN_TYPE_MISSION && info.getOpenCompleteMissionTemplateId() == missionTemplateId) {
					openFunList.add(info);
				}
			} else if (!missionSet.funtionMap.containsKey(info.getFunctionId())) {
				if (info.getFunctionOpenType() == MainMenuFunction.FUNCTION_OPEN_TYPE_MISSION && info.getOpenCompleteMissionTemplateId() == missionTemplateId) {
					openFunList.add(info);
				}
			}
		}
		boolean hasAnnimation = false;
		int animationId = 0;
		if (openFunList.size() > 0 && !isTestOpenAllFunction) {

			doSomethingWhileFunctionOpen(role, openFunList);

			for (MainMenuFunction info : openFunList) {
				if (AnimationManager.getInstance().getFunctionTypeAnimations().containsKey(info.getFunctionId())) {
					animationId = AnimationManager.getInstance().getFunctionTypeAnimations().get(info.getFunctionId()).animationResId;
					hasAnnimation = true;
				}
			}
		}

		if (openFunList.size() > 0 && !isTestOpenAllFunction) {
			KGameMessage sendMsg = KGame.newLogicMessage(KMissionProtocol.SM_OPEN_NEW_FUNCTION);
			sendMsg.writeInt(openFunList.size());
			for (MainMenuFunction info : openFunList) {
				sendMsg.writeShort(info.getFunctionId());
				sendMsg.writeBoolean(info.isOpenShowIcon());
			}
			sendMsg.writeBoolean(hasAnnimation);
			if (hasAnnimation) {
				sendMsg.writeInt(animationId);
			}
			role.sendMsg(sendMsg);

			for (MainMenuFunction info : openFunList) {
				missionSet.addOrUpdateFunctionInfo(info.getFunctionId(), true, false);
			}
		}
	}

	/**
	 * 检测处理角色等级对应的新功能开放
	 * 
	 * @param role
	 * @param roleLevel
	 */
	public static void processOpenNewFunctionByRoleLevel(KRole role) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());
		List<MainMenuFunction> openFunList = new ArrayList<MainMenuFunction>();

		for (MainMenuFunction info : mainMenuFunctionInfoMap.values()) {
			if (info.getFunctionOpenType() == MainMenuFunction.FUNCTION_OPEN_TYPE_ROLE_LEVEL && info.getOpenRoleLevelLimit() <= role.getLevel() && info.getOpenRoleLevelLimit() != 0) {
				if (missionSet.funtionMap.containsKey(info.getFunctionId()) && !missionSet.funtionMap.get(info.getFunctionId()).isOpen) {
					openFunList.add(info);

				} else if (!missionSet.funtionMap.containsKey(info.getFunctionId())) {
					openFunList.add(info);
				}
			}
		}

		boolean hasAnnimation = false;
		int animationId = 0;
		if (openFunList.size() > 0 && !isTestOpenAllFunction) {

			doSomethingWhileFunctionOpen(role, openFunList);

			for (MainMenuFunction info : openFunList) {
				if (AnimationManager.getInstance().getFunctionTypeAnimations().containsKey(info.getFunctionId())) {
					animationId = AnimationManager.getInstance().getFunctionTypeAnimations().get(info.getFunctionId()).animationResId;
					hasAnnimation = true;
				}
			}
		}

		if (openFunList.size() > 0 && !isTestOpenAllFunction) {
			KGameMessage sendMsg = KGame.newLogicMessage(KMissionProtocol.SM_OPEN_NEW_FUNCTION);
			sendMsg.writeInt(openFunList.size());
			for (MainMenuFunction info : openFunList) {
				sendMsg.writeShort(info.getFunctionId());
				sendMsg.writeBoolean(info.isOpenShowIcon());
			}
			sendMsg.writeBoolean(hasAnnimation);
			if (hasAnnimation) {
				sendMsg.writeInt(animationId);
			}
			role.sendMsg(sendMsg);

			for (MainMenuFunction info : openFunList) {
				missionSet.addOrUpdateFunctionInfo(info.getFunctionId(), true, false);
			}
		}
	}

	public static void sendAllOpenFunction(KRole role) {
		KGameMessage sendMsg = KGame.newLogicMessage(KMissionProtocol.SM_SEND_ALL_FUNCTION_LIST);

		if (!isTestOpenAllFunction) {
			KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());
			KMissionCompleteRecordSet missionCompleteRecordSet = KMissionModuleExtension.getMissionCompleteRecordSet(role.getId());
			sendMsg.writeInt(mainMenuFunctionInfoMap.size());
			for (MainMenuFunction info : mainMenuFunctionInfoMap.values()) {
				FunctionOpenRecord record;
				if (missionSet.funtionMap.containsKey(info.getFunctionId())) {
					record = missionSet.funtionMap.get(info.getFunctionId());
					if (!record.isOpen) {
						boolean isAlreadyGuide = false;
						boolean isOpen = false;
						if (info.getFunctionOpenType() == MainMenuFunction.FUNCTION_OPEN_TYPE_ROLE_LEVEL) {
							if (role.getLevel() >= info.getOpenRoleLevelLimit()) {
								isOpen = true;
							}
						} else if (info.getOpenCompleteMissionTemplateId() > 0 && info.getFunctionOpenType() == MainMenuFunction.FUNCTION_OPEN_TYPE_MISSION) {
							if (missionCompleteRecordSet.checkMissionIsCompleted(info.getOpenCompleteMissionTemplateId())) {
								isOpen = true;
							}

						}
						record = missionSet.addOrUpdateFunctionInfo(info.getFunctionId(), isOpen, isAlreadyGuide);
					}
				} else {
					boolean isAlreadyGuide = false;
					boolean isOpen = false;
					if (info.getFunctionOpenType() == MainMenuFunction.FUNCTION_OPEN_TYPE_ROLE_LEVEL) {
						if (role.getLevel() >= info.getOpenRoleLevelLimit()) {
							isOpen = true;
						}
					} else if (info.getOpenCompleteMissionTemplateId() > 0 && info.getFunctionOpenType() == MainMenuFunction.FUNCTION_OPEN_TYPE_MISSION) {
						if (missionCompleteRecordSet.checkMissionIsCompleted(info.getOpenCompleteMissionTemplateId())) {
							isOpen = true;
						}

					}
					// record = new FunctionOpenRecord(info.getFunctionId(),
					// isOpen, isAlreadyGuide);
					record = missionSet.addOrUpdateFunctionInfo(info.getFunctionId(), isOpen, isAlreadyGuide);
				}

				boolean openStatus = info.getIconShowStatus();
				if (openStatus) {
					openStatus = record.isOpen;
				}
				sendMsg.writeShort(info.getFunctionId());
				sendMsg.writeInt(info.getIconResId());
				sendMsg.writeBoolean(openStatus);
				sendMsg.writeByte(info.getGuideType());
				sendMsg.writeBoolean(record.isAlreadyGuide);
				sendMsg.writeUtf8String(info.getDesc());
				sendMsg.writeInt(info.getOpenRoleLevelLimit());
				sendMsg.writeBoolean((info.getFunctionOpenType() == MainMenuFunction.FUNCTION_OPEN_TYPE_MISSION));
			}
		} else {
			sendMsg.writeInt(mainMenuFunctionInfoMap.size());
			for (MainMenuFunction info : mainMenuFunctionInfoMap.values()) {
				sendMsg.writeShort(info.getFunctionId());
				sendMsg.writeInt(info.getIconResId());
				sendMsg.writeBoolean(true);
				sendMsg.writeByte(info.getGuideType());
				sendMsg.writeBoolean(true);
				sendMsg.writeUtf8String(info.getDesc());
				sendMsg.writeInt(info.getOpenRoleLevelLimit());
				sendMsg.writeBoolean((info.getFunctionOpenType() == MainMenuFunction.FUNCTION_OPEN_TYPE_MISSION));
			}
		}

		role.sendMsg(sendMsg);
	}

	public static List<MainMenuFunction> checkAndReloadFunctionConfigFile() {
		List<MainMenuFunction> funList = null;
		File confile = new File(funcionConfigFilePath);
		if (confile.exists() && confile.isFile()) {
			if (confile.lastModified() > lastConfigModifyTime) {
				lastConfigModifyTime = confile.lastModified();
			} else {
				return funList;
			}
		} else {
			return funList;
		}

		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(funcionConfigFilePath);
		} catch (BiffException e) {
			_LOGGER.error("读取任务模块missionConfig.xls发生错误！", e);
		} catch (IOException e) {
			_LOGGER.error("读取任务模块missionConfig.xls发生错误！", e);
		}

		if (xlsFile != null) {
			funList = new ArrayList<MainMenuFunction>();
			int dataRowIndex = 2;
			KGameExcelTable dataTable = xlsFile.getTable("主菜单功能类型信息", dataRowIndex);
			KGameExcelRow[] allDataRows = dataTable.getAllDataRows();
			for (int i = 0; i < allDataRows.length; i++) {
				short functionId = allDataRows[i].getShort("functionId");

				boolean iconShowStatus = allDataRows[i].getBoolean("iconShowStatus");
				MainMenuFunction fun = mainMenuFunctionInfoMap.get(functionId);
				if (fun != null) {
					if (fun.getIconShowStatus() != iconShowStatus) {
						fun.setIconShowStatus(iconShowStatus);
						funList.add(fun);
					}
				}
			}
		}
		return funList;
	}

	private static void doSomethingWhileFunctionOpen(KRole role, List<MainMenuFunction> funcList) {
		for (MainMenuFunction info : funcList) {
			KFunctionTypeEnum funType = KFunctionTypeEnum.getEnum(info.getFunctionId());
			if (funType != null) {
				switch (funType) {
				case 机甲:
					KSupportFactory.getMountModuleSupport().notifyStartMount(role.getId());
					break;
				case 时装:
					List<Integer> fashionList = new ArrayList<Integer>();
					if (role.getJob() == KJobTypeEnum.WARRIOR.getJobType()) {
						fashionList.add(15006);
					} else if (role.getJob() == KJobTypeEnum.SHADOW.getJobType()) {
						fashionList.add(16006);
					} else if (role.getJob() == KJobTypeEnum.GUNMAN.getJobType()) {
						fashionList.add(17006);
					}
					KSupportFactory.getFashionModuleSupport().addFashions(role, fashionList, "时装功能开启赠送时装");
					break;

				default:
					break;
				}
			}
		}
	}

	public static boolean checkFunctionIsOpen(KRole role, short functionId) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());
		KMissionCompleteRecordSet missionCompleteRecordSet = KMissionModuleExtension.getMissionCompleteRecordSet(role.getId());
		MainMenuFunction info = mainMenuFunctionInfoMap.get(functionId);
		if (info == null) {
			return false;
		}
		FunctionOpenRecord record;
		boolean isOpen = false;
		if (missionSet.funtionMap.containsKey(info.getFunctionId())) {
			record = missionSet.funtionMap.get(info.getFunctionId());
			isOpen = record.isOpen;
		} else {
			boolean isAlreadyGuide = false;

			if (info.getFunctionOpenType() == MainMenuFunction.FUNCTION_OPEN_TYPE_ROLE_LEVEL) {
				if (role.getLevel() >= info.getOpenRoleLevelLimit()) {
					isOpen = true;
				}
			} else if (info.getOpenCompleteMissionTemplateId() > 0 && info.getFunctionOpenType() == MainMenuFunction.FUNCTION_OPEN_TYPE_MISSION) {
				if (missionCompleteRecordSet.checkMissionIsCompleted(info.getOpenCompleteMissionTemplateId())) {
					isOpen = true;
				}

			}
			// record = new FunctionOpenRecord(info.getFunctionId(), isOpen,
			// isAlreadyGuide);
			record = missionSet.addOrUpdateFunctionInfo(info.getFunctionId(), isOpen, isAlreadyGuide);
		}
		return isOpen;
	}

	public static void checkAndSendSecondGuideFunctionByMission(KRole role, int missionTemplateId) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());
		KMissionCompleteRecordSet missionCompleteRecordSet = KMissionModuleExtension.getMissionCompleteRecordSet(role.getId());

		for (MainMenuFunction fun : mainMenuFunctionInfoMap.values()) {
			if (fun.isSecondGuide()) {
				int guideMissionTemplateId = fun.getSecondGuideMissionTemplateId();
				int secondGuideLv = fun.getSecondGuideRoleLv();
				// KMissionTemplate template =
				// KMissionModuleExtension.getManager().getMissionTemplate(missionTemplateId);
				if (missionTemplateId == guideMissionTemplateId) {
					KGameMessage sendMsg = KGame.newLogicMessage(KMissionProtocol.SM_NOTIFY_FUNCTION_SECOND_GUIDE);
					sendMsg.writeShort(fun.getFunctionId());
					sendMsg.writeByte(fun.getSecondGuideCount());
					role.sendMsg(sendMsg);
					break;
				}
			}
		}
	}

	public static void checkAndSendSecondGuideFunctionByRoleLv(KRole role) {
		KMissionSet missionSet = KMissionModuleExtension.getMissionSet(role.getId());
		KMissionCompleteRecordSet missionCompleteRecordSet = KMissionModuleExtension.getMissionCompleteRecordSet(role.getId());

		for (MainMenuFunction fun : mainMenuFunctionInfoMap.values()) {
			if (fun.isSecondGuide()) {
				int secondGuideLv = fun.getSecondGuideRoleLv();
				// KMissionTemplate template =
				// KMissionModuleExtension.getManager().getMissionTemplate(missionTemplateId);
				if (role.getLevel() == secondGuideLv) {
					KGameMessage sendMsg = KGame.newLogicMessage(KMissionProtocol.SM_NOTIFY_FUNCTION_SECOND_GUIDE);
					sendMsg.writeShort(fun.getFunctionId());
					sendMsg.writeByte(fun.getSecondGuideCount());
					role.sendMsg(sendMsg);
					break;
				}
			}
		}
	}

	/**
	 * 通知客户端主界面功能图标是否高闪亮显示
	 * 
	 * @param funType
	 * @param isShineIcon
	 */
	public static void sendShineIconStatusToAllOnlineRoles(KFunctionTypeEnum funType, boolean isShineIcon) {
		KGameMessage sendMsg = KGame.newLogicMessage(KMissionProtocol.SM_NOTIFY_FUNCTION_ICON_SHINNING);
		sendMsg.writeShort(funType.functionId);
		sendMsg.writeBoolean(isShineIcon);
		List<Long> allOnlineRoles = KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds();
		for (Long roleId : allOnlineRoles) {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			role.sendMsg(sendMsg.duplicate());
		}
	}

	public static class FunctionOpenStateCheckTask implements KGameTimerTask {

		@Override
		public String getName() {
			return FunctionOpenStateCheckTask.class.getName();
		}

		@Override
		public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {

			boolean isUpdateIcon = false;
			List<KGameMessage> updateIconMessageList = null;
			List<MainMenuFunction> checkList = KGuideManager.checkAndReloadFunctionConfigFile();
			if (checkList != null && !checkList.isEmpty()) {
				isUpdateIcon = true;

				updateIconMessageList = new ArrayList<KGameMessage>();
				for (MainMenuFunction fun : checkList) {
					KGameMessage msg = KGame.newLogicMessage(KMissionProtocol.SM_NOTIFY_FUNCTION_OPEN_STATUS);
					msg.writeShort(fun.getFunctionId());
					msg.writeBoolean(fun.getIconShowStatus());
					updateIconMessageList.add(msg);
				}
			}
			List<Long> allOnlineRoles = KSupportFactory.getRoleModuleSupport().getAllOnLineRoleIds();
			for (Long roleId : allOnlineRoles) {
				KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);

				// 检测功能临时开关
				if (isUpdateIcon && updateIconMessageList != null && !updateIconMessageList.isEmpty()) {
					for (int i = 0; i < updateIconMessageList.size(); i++) {
						MainMenuFunction fun = checkList.get(i);
						if (KGuideManager.checkFunctionIsOpen(role, fun.getFunctionId())) {
							role.sendMsg(updateIconMessageList.get(i).duplicate());
						}
					}
				}
			}

			return null;
		}

		@Override
		public void done(KGameTimeSignal timeSignal) {
			KGame.getTimer().newTimeSignal(this, 30, TimeUnit.SECONDS);
		}

		@Override
		public void rejected(RejectedExecutionException e) {

		}

	}
}
