package com.kola.kmp.logic.activity.transport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import jxl.read.biff.BiffException;

import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.dataaccess.KGameDBException;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.XmlUtil;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.currency.CurrencyCountStruct;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kgame.db.dataaccess.DataAccesserFactory;
import com.kola.kgame.db.dataobject.DBGameExtCA;
import com.kola.kmp.logic.actionrecord.KActionRecorder;
import com.kola.kmp.logic.actionrecord.KActionType;
import com.kola.kmp.logic.activity.KActivityModuleDialogProcesser;
import com.kola.kmp.logic.activity.KActivityRoleExtCaCreator;
import com.kola.kmp.logic.activity.transport.KTransporter.InterceptHistory;
import com.kola.kmp.logic.combat.ICombatCommonResult;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.competition.KCompetitionManager;
import com.kola.kmp.logic.flow.KRoleAttrModifyType;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.npc.message.KNPCOrderMsg;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameExtDataDBTypeEnum;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.other.KNPCOrderEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.CombatTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.TimeLimitProducActivityTips;
import com.kola.kmp.logic.util.tips.TransportTips;
import com.kola.kmp.protocol.activity.KActivityProtocol;

public class KTransportManager {

	private static final Logger _LOGGER = KGameLogger.getLogger(KTransportManager.class);

	// 开放等级
	public static int openRoleLv;

	private static final int UP_SEARCH_LV = 5;// 向下搜索等级范围
	private static final int DOWN_SEARCH_LV = 10;// 向上搜索等级范围
	private static int MAX_INTERCEPT_COUNT = 10;// 镖车最大被拦截次数
	private static final int LAND_SEARCH_COUNT = 6;// 其他角色陆路载具搜索数量
	private static final int WATER_SEARCH_COUNT = 2;// 其他角色水路载具搜索数量

	// 每天角色可拦截次数
	public static int max_can_challenge_count = 10;
	// 每天角色可运输次数
	public static int max_can_transport_count = 5;
	// 刷新载具消钻石数量
	// public static int reflash_carrier_use_point = 20;
	// 清除冷却时间消钻石数量
	public static int clear_cooltime_use_point = 20;
	// 拦截冷却时间，单位：秒
	public static int intercept_cool_time_seconds = 300;

	// 载具数据表
	private static Map<Integer, CarrierData> carrierDataMap = new LinkedHashMap<Integer, CarrierData>();
	// 默认载具
	public static CarrierData defaultCarrierData;

	// 正在押运的运输者数据
	private static ConcurrentHashMap<Long, KTransporter> transporterMap = new ConcurrentHashMap<Long, KTransporter>();
	// 陆路运输的所有角色ID，按等级分布，Key：角色等级
	private static ConcurrentHashMap<Integer, HashSet<Long>> landTransporterRoleLvMap = new ConcurrentHashMap<Integer, HashSet<Long>>();
	// 水路运输的所有角色ID，按等级分布，Key：角色等级
	private static ConcurrentHashMap<Integer, HashSet<Long>> waterTransporterRoleLvMap = new ConcurrentHashMap<Integer, HashSet<Long>>();
	// 基础经验奖励表，主Key：载具ID，副Key：角色等级
	private static LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> baseExpMap = new LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>>();
	// 基础潜能奖励表，主Key：载具ID，副Key：角色等级
	private static LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> basePotentialMap = new LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>>();
	// 刷新次数与消耗货币的关系表，key：刷新次数
	private static HashMap<Integer, KCurrencyCountStruct> reflashUsePoint = new HashMap<Integer, KCurrencyCountStruct>();
	// 最大刷新次数消耗货币
	private static KCurrencyCountStruct maxReflashUsePoint;

	public static boolean isShutdown = false;

	// 拦截奖励结算系数
	public static float i_rate1, i_rate2, i_rate3;
	// 被拦截损失结算系数
	public static float t_rate1, t_rate2, t_rate3;

	// 竞技场战斗时间限制（单位：毫秒）
	public static int battleTimeMillis;

	public KTransportManager() {

	}

	public static void init(String configPath) throws KGameServerException {
		Document doc = XmlUtil.openXml(configPath);
		if (doc != null) {
			Element root = doc.getRootElement();

			String excelFilePath = root.getChildText("excelFilePath");
			// max_can_challenge_count = Integer.parseInt(root
			// .getChildText("max_can_challenge_count"));
			// max_can_transport_count = Integer.parseInt(root
			// .getChildText("max_can_transport_count"));
			// // reflash_carrier_use_point = Integer.parseInt(root
			// // .getChildText("reflash_carrier_use_point"));
			// clear_cooltime_use_point = Integer.parseInt(root
			// .getChildText("clear_cooltime_use_point"));
			// intercept_cool_time_seconds = Integer.parseInt(root
			// .getChildText("intercept_cool_time_seconds"));

			loadExcelData(excelFilePath);
			int maxLv = KRoleModuleConfig.getRoleMaxLv();
			for (int i = 1; i <= maxLv; i++) {
				landTransporterRoleLvMap.put(i, new HashSet<Long>());
				waterTransporterRoleLvMap.put(i, new HashSet<Long>());
			}
		} else {
			throw new NullPointerException("物资运输活动配置文件不存在！！");
		}
	}

	private static void loadExcelData(String xlsPath) throws KGameServerException {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(xlsPath);
		} catch (BiffException e) {
			throw new KGameServerException("读取物资运输活动excel表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取物资运输活动excel表头发生错误！", e);
		}

		if (xlsFile != null) {
			// 许愿数据表
			int dataRowIndex = 3;
			KGameExcelTable dataTable = xlsFile.getTable("载具设置", dataRowIndex);
			KGameExcelRow[] allDataRows = dataTable.getAllDataRows();

			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					int carrierId = allDataRows[i].getInt("Id");
					String carrierName = allDataRows[i].getData("name");
					int tranTimeSeconds = allDataRows[i].getInt("time");
					int baseExp = allDataRows[i].getInt("exp");
					int basePotential = allDataRows[i].getInt("potency");
					int baseWeight = allDataRows[i].getInt("weight");
					int maxWeight = allDataRows[i].getInt("cap_weight");
					int reflashUpWieghtCount = allDataRows[i].getInt("add_weight");
					byte laneType = allDataRows[i].getByte("laneType");
					if (laneType < CarrierData.LANE_TYPE_LAND || laneType > CarrierData.LANE_TYPE_WATER) {
						throw new KGameServerException("加载《物资运输》表：" + xlsPath + "的laneType字段出错，该值只能为1或2，excel行数：" + allDataRows[i].getIndexInFile());
					}
					int resId = allDataRows[i].getInt("resId");
					CarrierData carrier = new CarrierData(carrierId, carrierName, tranTimeSeconds, baseExp, basePotential, baseWeight, maxWeight, reflashUpWieghtCount, laneType, resId);
					carrierDataMap.put(carrierId, carrier);
					if (i == 0) {
						defaultCarrierData = carrier;
					}
				}
			}

			dataTable = xlsFile.getTable("等级系数", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();

			LinkedHashMap<Integer, Float> expMap = new LinkedHashMap<Integer, Float>();
			LinkedHashMap<Integer, Float> potentialMap = new LinkedHashMap<Integer, Float>();
			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					int lv = allDataRows[i].getInt("lv");
					float exp = allDataRows[i].getFloat("x_exp");
					float potential = allDataRows[i].getFloat("x_potency");
					expMap.put(lv, exp);
					potentialMap.put(lv, potential);
				}
			}

			for (CarrierData carrier : carrierDataMap.values()) {
				if (!baseExpMap.containsKey(carrier.getCarrierId())) {
					baseExpMap.put(carrier.getCarrierId(), new LinkedHashMap<Integer, Integer>());
				}
				if (!basePotentialMap.containsKey(carrier.getCarrierId())) {
					basePotentialMap.put(carrier.getCarrierId(), new LinkedHashMap<Integer, Integer>());
				}
				for (Integer lv : expMap.keySet()) {
					int exp = (int) (carrier.getBaseExp() * expMap.get(lv));
					baseExpMap.get(carrier.getCarrierId()).put(lv, exp);
				}
				for (Integer lv : potentialMap.keySet()) {
					int potential = (int) (carrier.getBasePotential() * potentialMap.get(lv));
					basePotentialMap.get(carrier.getCarrierId()).put(lv, potential);
				}
			}

			dataTable = xlsFile.getTable("刷新消耗", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();
			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					int count = allDataRows[i].getInt("refresh");
					byte type = allDataRows[i].getByte("moneyType");
					int point = allDataRows[i].getInt("moneyCount");
					KCurrencyTypeEnum cType = KCurrencyTypeEnum.getEnum(type);
					if (cType == null) {
						throw new KGameServerException("加载物资运输《刷新消耗》表：" + xlsPath + "的type字段出错，该值只能为1或2，excel行数：" + allDataRows[i].getIndexInFile());
					}
					reflashUsePoint.put(count, new KCurrencyCountStruct(cType, point));

					if (i == allDataRows.length - 1) {
						maxReflashUsePoint = new KCurrencyCountStruct(cType, point);
					}
				}
			}

			dataTable = xlsFile.getTable("其他参数", dataRowIndex);
			allDataRows = dataTable.getAllDataRows();
			if (allDataRows != null) {
				for (int i = 0; i < allDataRows.length; i++) {
					max_can_challenge_count = allDataRows[i].getInt("interceptweight");
					max_can_transport_count = allDataRows[i].getInt("transweight");
					clear_cooltime_use_point = allDataRows[i].getInt("cdPoint");
					intercept_cool_time_seconds = allDataRows[i].getInt("cd");
					MAX_INTERCEPT_COUNT = allDataRows[i].getInt("beinterceptweight");
					i_rate1 = allDataRows[i].getFloat("i_rate1");
					i_rate2 = allDataRows[i].getFloat("i_rate2");
					i_rate3 = allDataRows[i].getFloat("i_rate3");
					t_rate1 = allDataRows[i].getFloat("t_rate1");
					t_rate2 = allDataRows[i].getFloat("t_rate2");
					t_rate3 = allDataRows[i].getFloat("t_rate3");
					battleTimeMillis = allDataRows[i].getInt("battleTime") * 1000;
				}
			}
		}
	}

	public static CarrierData getCarrierData(int carrierId) {
		return carrierDataMap.get(carrierId);
	}

	public static Map<Integer, CarrierData> getCarrierDataMap() {
		return carrierDataMap;
	}

	public static boolean isRoleTransporting(long roleId) {
		return transporterMap.containsKey(roleId);
	}

	public static KTransporter getTransporter(long roleId) {
		return transporterMap.get(roleId);
	}

	public static void addTransporter(KTransporter transporter) {
		transporterMap.put(transporter.getRoleId(), transporter);
		if (transporter.getCarrier().getLaneType() == CarrierData.LANE_TYPE_LAND) {
			landTransporterRoleLvMap.get(transporter.getRoleLv()).add(transporter.getRoleId());
		} else if (transporter.getCarrier().getLaneType() == CarrierData.LANE_TYPE_WATER) {
			waterTransporterRoleLvMap.get(transporter.getRoleLv()).add(transporter.getRoleId());
		}
	}

	public static void removeTransporter(long roleId) {
		if (transporterMap.containsKey(roleId)) {
			KTransporter transporter = transporterMap.remove(roleId);
			if (transporter.getCarrier().getLaneType() == CarrierData.LANE_TYPE_LAND) {
				landTransporterRoleLvMap.get(transporter.getRoleLv()).remove(roleId);
			} else {
				waterTransporterRoleLvMap.get(transporter.getRoleLv()).remove(roleId);
			}
		}
	}

	public static void sendTransportData(KRole role, boolean isFirstGet, boolean isReflashOther) {
		KTransportData data = KActivityRoleExtCaCreator.getActivityRoleExtData(role.getId()).getTransportData();
		if (data == null) {
			KDialogService.sendUprisingDialog(role, GlobalTips.getTipsServerBusy());
			return;
		}

		data.checkAndRestData(true);

		KGameMessage sendMsg = KGame.newLogicMessage(KActivityProtocol.SM_SEND_TRANSPORT_DATA);
		sendMsg.writeBoolean(isFirstGet);
		if (isFirstGet) {
			sendMsg.writeByte(carrierDataMap.size());
			for (CarrierData carrier : carrierDataMap.values()) {
				sendMsg.writeInt(carrier.getCarrierId());
				sendMsg.writeByte(carrier.getLaneType());
				sendMsg.writeUtf8String(carrier.getCarrierName());
				sendMsg.writeInt(carrier.getResId());
				sendMsg.writeInt(getCarrierBaseExp(carrier.getCarrierId(), role.getLevel()));
				sendMsg.writeInt(getCarrierBasePotential(carrier.getCarrierId(), role.getLevel()));
				sendMsg.writeInt(carrier.getTranTimeSeconds());
				sendMsg.writeShort(MAX_INTERCEPT_COUNT);
			}
		}

		KCurrencyCountStruct reflashPoint = getReflashUseCount(data);
		sendMsg.writeByte(reflashPoint.getCurrencyType());
		sendMsg.writeLong(reflashPoint.currencyCount);
		sendMsg.writeInt(clear_cooltime_use_point);
		sendMsg.writeShort(data.getCanTransportRestCount());
		sendMsg.writeShort(data.getCanInterceptRestCount());
		sendMsg.writeInt(data.getInterceptCoolTimeSeconds());

		// 是否正在运输
		boolean isTransporting = isRoleTransporting(role.getId());
		sendMsg.writeBoolean(isTransporting);
		if (isTransporting) {
			KTransporter transporter = transporterMap.get(role.getId());
			// sendMsg.writeInt(transporter.getCarrier().getCarrierId());
			// sendMsg.writeInt(transporter.getRestTimeSeconds());
			// sendMsg.writeShort(transporter.getInterceptCount());
			// sendMsg.writeInt(transporter.getRestExp());
			// sendMsg.writeInt(transporter.getRestPotential());
			transporter.packMyTransporterMsg(sendMsg);
		} else {
			sendMsg.writeInt(data.getNowCarrierId());
		}

		sendMsg.writeBoolean(isReflashOther);
		if (isReflashOther) {
			List<Long> landList = searchLandTransporter(role.getId(), role.getLevel(), LAND_SEARCH_COUNT);
			List<Long> waterList = searchWaterTransporter(role.getId(), role.getLevel(), WATER_SEARCH_COUNT);

			int landSize = 0, waterSize = 0;
			int landIndex = sendMsg.writerIndex();
			sendMsg.writeByte(landSize);
			for (Long roleId : landList) {
				KTransporter transporter = transporterMap.get(roleId);
				if (transporter != null) {
					/**
					 * <pre>
					 * long roleId 拥有载具的角色Id 
					 * String roleName 角色名称 
					 * byte jobType 角色职业类型 
					 * int roleLv 角色等级 
					 * int carrierId 当前载具Id
					 * </pre>
					 */
					// sendMsg.writeLong(transporter.getRoleId());
					// sendMsg.writeUtf8String(transporter.getRoleName());
					// sendMsg.writeByte(transporter.getJobType());
					// sendMsg.writeInt(transporter.getRoleLv());
					// sendMsg.writeInt(transporter.getCarrier().getCarrierId());
					transporter.packOtherTransporterMsg(sendMsg, role.getLevel());
					landSize++;
				}
			}
			sendMsg.setByte(landIndex, landSize);

			int waterIndex = sendMsg.writerIndex();
			sendMsg.writeByte(waterSize);
			for (Long roleId : waterList) {
				KTransporter transporter = transporterMap.get(roleId);
				if (transporter != null) {
					// sendMsg.writeLong(transporter.getRoleId());
					// sendMsg.writeUtf8String(transporter.getRoleName());
					// sendMsg.writeByte(transporter.getJobType());
					// sendMsg.writeInt(transporter.getRoleLv());
					// sendMsg.writeInt(transporter.getCarrier().getCarrierId());
					transporter.packOtherTransporterMsg(sendMsg, role.getLevel());
					waterSize++;
				}
			}
			sendMsg.setByte(waterIndex, waterSize);
		}

		role.sendMsg(sendMsg);

		pushInterceptHistoryData(role, data.getInterceptInfoList());
	}

	public static KCurrencyCountStruct getReflashUseCount(KTransportData data) {
		if (data != null) {
			int count = data.getReflashCount() + 1;
			if (reflashUsePoint.containsKey(count)) {
				return reflashUsePoint.get(count);
			}
		}
		return maxReflashUsePoint;
	}

	public static void transport(KRole role) {
		if (role.getLevel() < openRoleLv) {
			KDialogService.sendUprisingDialog(role, TransportTips.getTipsNotOpen());
			return;
		}
		KTransportData data = KActivityRoleExtCaCreator.getActivityRoleExtData(role.getId()).getTransportData();
		if (data == null) {
			KDialogService.sendUprisingDialog(role, GlobalTips.getTipsServerBusy());
			return;
		}

		if (isRoleTransporting(role.getId())) {
			KDialogService.sendUprisingDialog(role, TransportTips.getTipsIsTransporting());
			return;
		}

		if (data.getTransportCount() > max_can_transport_count) {
			KDialogService.sendUprisingDialog(role, TransportTips.getTipsMaxTransportCount(max_can_transport_count));
			return;
		}

		CarrierData carrier = getCarrierData(data.getNowCarrierId());
		if (carrier == null) {
			KDialogService.sendUprisingDialog(role, GlobalTips.getTipsServerBusy());
			return;
		}

		// 检测是否有限时产出活动
		boolean isCopyActivityPrice = false;
		float expRate = 1, goldRate = 1, potentialRate = 1;
		int itemMultiple = 1;
		TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.物资运输活动);

		if (activity != null && activity.isActivityTakeEffectNow() && activity.carrierId != null) {
			for (int i = 0; i < activity.carrierId.length; i++) {
				if (activity.carrierId[i] == carrier.getCarrierId()) {
					isCopyActivityPrice = true;
					break;
				}
			}
			if (isCopyActivityPrice) {
				expRate = activity.expRate;
				goldRate = activity.goldRate;
				potentialRate = activity.potentialRate;
				itemMultiple = activity.itemMultiple;
			}
		}

		KTransporter transporter = new KTransporter(role.getId(), role.getName(), role.getJob(), role.getLevel(), role.getBattlePower(), carrier, expRate, potentialRate, isCopyActivityPrice);
		transporter.startTranspot();

		addTransporter(transporter);

		data.transport(carrier.getCarrierId());

		sendTransportData(role, false, false);

		KDialogService.sendUprisingDialog(role, TransportTips.getTipsTransportStrat(carrier.getExtName(), carrier.getTranTimeStr()));

		// 通知活跃度模块
		KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.运送物资);

		// 角色行为统计
		KActionRecorder.recordAction(role.getId(), KActionType.ACTION_TYPE_TRANSPORT, 1);
	}

	public static void intercept(KRole role, long transporterRoleId) {
		if (role.getLevel() < openRoleLv) {
			KDialogService.sendUprisingDialog(role, TransportTips.getTipsNotOpen());
			return;
		}

		if (role.isFighting()) {
			KDialogService.sendUprisingDialog(role, CombatTips.getTipsRoleIsFighting(role.getName()));
			return;
		}

		KTransporter transporter = transporterMap.get(transporterRoleId);

		KTransportData data = KActivityRoleExtCaCreator.getActivityRoleExtData(role.getId()).getTransportData();
		if (data == null) {
			KDialogService.sendUprisingDialog(role, GlobalTips.getTipsServerBusy());
			return;
		}

		if (data.getInterceptCount() >= max_can_challenge_count) {
			KDialogService.sendUprisingDialog(role, TransportTips.getTipsMaxInterceptCount(max_can_challenge_count));
			return;
		}

		if (transporter == null) {
			KDialogService.sendUprisingDialog(role, TransportTips.getTipsTransporterNotFound());
			sendTransportData(role, false, true);
			return;
		}

		if (transporter.getInterceptCount() >= MAX_INTERCEPT_COUNT) {
			if (transporter.getCarrier().getLaneType() == CarrierData.LANE_TYPE_LAND && landTransporterRoleLvMap.containsKey(transporter.getRoleLv())
					&& landTransporterRoleLvMap.get(transporter.getRoleLv()).contains(transporter.getRoleId())) {
				landTransporterRoleLvMap.get(transporter.getRoleLv()).remove(transporter.getRoleId());
			}
			if (transporter.getCarrier().getLaneType() == CarrierData.LANE_TYPE_WATER && waterTransporterRoleLvMap.containsKey(transporter.getRoleLv())
					&& waterTransporterRoleLvMap.get(transporter.getRoleLv()).contains(transporter.getRoleId())) {
				waterTransporterRoleLvMap.get(transporter.getRoleLv()).remove(transporter.getRoleId());
			}
		}

		if (transporter.getInterceptCount() >= MAX_INTERCEPT_COUNT) {
			KDialogService.sendUprisingDialog(role, TransportTips.getTipsTransporterMaxInterceptCount(MAX_INTERCEPT_COUNT));
			sendTransportData(role, false, true);
			return;
		}

		if (data.getInterceptCoolTimeSeconds() > 0) {
			int point = KTransportManager.clear_cooltime_use_point;

			if (point < 0) {
				KDialogService.sendUprisingDialog(role, GlobalTips.getTipsServerBusy());
				return;
			}

			long result = KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), KCurrencyTypeEnum.DIAMOND, point, UsePointFunctionTypeEnum.清除拦截冷却时间, true);
			// 元宝不足，发送提示
			if (result == -1) {
				KDialogService.sendUprisingDialog(role, TransportTips.getTipsClearCoolTimeNotEnoughIgot(point));
				return;
			} else {
				KTransportManager.clearCoolTime(role, false);
			}
		}

		transporter.intercept();
		data.intercept();

		TransportCombatData combatData = new TransportCombatData(transporterRoleId, transporter.getRoleLv(), transporter.getCarrier().getCarrierId(), transporter.getRoleName());
		KDialogService.sendNullDialog(role);
		// KSupportFactory.getCombatModuleSupport().fightWithAI(role,
		// transporterRoleId, KCombatType.TRANSPORT_COMBAT,
		// KCompetitionManager.battlefield, combatData);
		KSupportFactory.getCombatModuleSupport().fightWithAIWithTimeLimit(role, transporterRoleId, KCombatType.TRANSPORT_COMBAT, KCompetitionManager.battlefield, combatData, battleTimeMillis);

		// 通知活跃度模块
		KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.抢夺他人物资);

		// 角色行为统计
		KActionRecorder.recordAction(role.getId(), KActionType.ACTION_TYPE_INTERCEPT, 1);
	}

	public static void notifyBattleFinished(KRole challenger, ICombatCommonResult result) {
		TransportCombatData combatData = (TransportCombatData) result.getAttachment();
		if (combatData == null) {

			return;
		}
		KTransporter transporter = transporterMap.get(combatData.roleId);
		if (transporter != null) {
			transporter.interceptResult(challenger, result.isWin());
		}

		if (result.isWin()) {

			CarrierData carrier = getCarrierData(combatData.carrierId);
			// 计算拦截者经验
			int exp = caculateInterceptGetResource(combatData.roleLv, challenger.getLevel(), getCarrierBaseExp(combatData.carrierId, combatData.roleLv));
			// 计算拦截者潜能
			int potential = caculateInterceptGetResource(combatData.roleLv, challenger.getLevel(), getCarrierBasePotential(combatData.carrierId, combatData.roleLv));
			if (exp > 0) {
				// KSupportFactory.getRoleModuleSupport().addExp(
				// challenger.getId(), exp);
				KSupportFactory.getRoleModuleSupport().addExp(challenger.getId(), exp, KRoleAttrModifyType.物资运输拦截奖励, carrier.getCarrierId());
			}
			if (potential > 0) {
				KSupportFactory.getCurrencySupport().increaseMoney(challenger.getId(), KCurrencyTypeEnum.POTENTIAL, potential, PresentPointTypeEnum.物资运输奖励, true);
			}

			sendBattleResult(challenger, result, exp, potential);

			KTransportData data = KActivityRoleExtCaCreator.getActivityRoleExtData(challenger.getId()).getTransportData();
			if (data != null) {
				String carrierName = carrier.getExtName();
				String intercepterName = (combatData.transportRoleName == null) ? "" : HyperTextTool.extRoleName(combatData.transportRoleName);
				InterceptHistory info = new InterceptHistory(true, TransportTips.getTipsInterceptSuccessInfo(UtilTool.DATE_FORMAT.format(System.currentTimeMillis()), intercepterName, carrierName,
						exp, potential));
				data.addInterceptInfoQueue(info);
				pushInterceptHistoryData(challenger, info);
			}
		} else {
			sendBattleResult(challenger, result, 0, 0);

			CarrierData carrier = getCarrierData(combatData.carrierId);
			KTransportData data = KActivityRoleExtCaCreator.getActivityRoleExtData(challenger.getId()).getTransportData();
			if (data != null) {
				String carrierName = carrier.getExtName();
				String intercepterName = (combatData.transportRoleName == null) ? "" : HyperTextTool.extRoleName(combatData.transportRoleName);
				InterceptHistory info = new InterceptHistory(false, TransportTips.getTipsInterceptFaildInfo(UtilTool.DATE_FORMAT.format(System.currentTimeMillis()), intercepterName, carrierName));
				data.addInterceptInfoQueue(info);
				pushInterceptHistoryData(challenger, info);
			}
		}
	}

	public static void confirmCompleteBattle(KRole role) {
		KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(role);
		KNPCOrderMsg.sendNPCMenuOrder(role, KNPCOrderEnum.ORDER_OPEN_TRANSPORT, UtilTool.getNotNullString(null));
	}

	public static void sendBattleResult(KRole role, ICombatCommonResult result, int exp, int potential) {
		KGameMessage msg = KGame.newLogicMessage(KActivityProtocol.SM_SHOW_COMPLETE_INTERCEPT_BATTLE_RESULT);
		msg.writeBoolean(result.isWin());
		msg.writeInt((int) (result.getCombatTime() / 1000));
		if (result.isWin()) {
			msg.writeInt(exp);
			msg.writeInt(potential);
		}
		role.sendMsg(msg);
	}

	/**
	 * 计算被拦截者失去的资源数量
	 * 
	 * @param transporterLv
	 * @param intercepterLv
	 * @param sourceCount
	 * @return
	 */
	public static int caculateInterceptLoseResource(int transporterLv, int intercepterLv, int sourceCount) {
		// final float rate1 = 0.05f, rate2 = 0.025f, rate3 = 0.0025f;
		if (intercepterLv - transporterLv <= 0) {
			return (int) ((float) sourceCount * t_rate1);
		} else if (intercepterLv - transporterLv <= 10) {
			return (int) ((float) sourceCount * (t_rate1 - (intercepterLv - transporterLv) * t_rate3));
		} else {
			return (int) ((float) sourceCount * t_rate2);
		}
	}

	/**
	 * 计算拦截者获得的资源数量
	 * 
	 * @param transporterLv
	 * @param intercepterLv
	 * @param sourceCount
	 * @return
	 */
	public static int caculateInterceptGetResource(int transporterLv, int intercepterLv, int sourceCount) {
		// final float rate1 = 0.1f, rate2 = 0.05f, rate3 = 0.005f;
		if (intercepterLv - transporterLv <= 0) {
			return (int) ((float) sourceCount * i_rate1);
		} else if (intercepterLv - transporterLv <= 10) {
			return (int) ((float) sourceCount * (i_rate1 - (intercepterLv - transporterLv) * i_rate3));
		} else {
			return (int) ((float) sourceCount * i_rate2);
		}
	}

	private static List<Long> searchLandTransporter(long myRoleId, int roleLv, int searchCount) {
		if (roleLv < openRoleLv) {
			return Collections.emptyList();
		}
		List<Long> list = new ArrayList<Long>();
		int minSearchLv = ((roleLv - UP_SEARCH_LV) <= openRoleLv) ? openRoleLv : (roleLv - UP_SEARCH_LV);
		// int minSearchLv = ((roleLv - UP_SEARCH_LV) <= 20) ? 20
		// : (roleLv - UP_SEARCH_LV);
		int maxSearchLv = ((roleLv + DOWN_SEARCH_LV) >= KRoleModuleConfig.getRoleMaxLv()) ? KRoleModuleConfig.getRoleMaxLv() : (roleLv + DOWN_SEARCH_LV);

		// int maxSearchLv = ((roleLv + DOWN_SEARCH_LV) >= 60) ? 60
		// : (roleLv + DOWN_SEARCH_LV);

		System.out.println("minSearchLv:" + minSearchLv + ",maxSearchLv:" + maxSearchLv);
		int nowCount = 0;

		Set<Long> searchSet = new HashSet<Long>();
		Map<Integer, AtomicBoolean> lvMap;

		int targetSearchCount = 3;

		// 先搜索相同等级
		if (roleLv == minSearchLv || roleLv == maxSearchLv) {
			targetSearchCount = 6;
		}
		search(myRoleId, roleLv, targetSearchCount, searchSet, true);

		// 搜索小于自己等级
		if (roleLv > minSearchLv) {
			targetSearchCount = 3;
			if (roleLv == maxSearchLv) {
				targetSearchCount = searchCount - searchSet.size();
			}
			for (int lv = (roleLv - 1); (lv >= minSearchLv) && targetSearchCount > 0; lv--) {
				Set<Long> tempSet = search(myRoleId, lv, targetSearchCount, searchSet, true);
				targetSearchCount = targetSearchCount - tempSet.size();
			}
		}

		// 搜索大于自己等级
		if (roleLv < maxSearchLv) {
			targetSearchCount = 3;
			if (roleLv == minSearchLv) {
				targetSearchCount = searchCount - searchSet.size();
			}
			for (int lv = (roleLv + 1); (lv <= maxSearchLv) && targetSearchCount > 0; lv++) {
				Set<Long> tempSet = search(myRoleId, lv, targetSearchCount, searchSet, true);
				targetSearchCount = targetSearchCount - tempSet.size();
			}
		}
		// 如果还有剩余空位
		if (searchCount - searchSet.size() > 0) {
			targetSearchCount = searchCount - searchSet.size();
			for (int lv = (roleLv - 1); (lv >= minSearchLv) && targetSearchCount > 0; lv--) {
				Set<Long> tempSet = search(myRoleId, lv, targetSearchCount, searchSet, true);
				targetSearchCount = targetSearchCount - tempSet.size();
			}
			for (int lv = (roleLv + 1); (lv <= maxSearchLv) && targetSearchCount > 0; lv++) {
				Set<Long> tempSet = search(myRoleId, lv, targetSearchCount, searchSet, true);
				targetSearchCount = targetSearchCount - tempSet.size();
			}
		}

		list.addAll(searchSet);
		return list;
	}

	private static List<Long> searchWaterTransporter(long myRoleId, int roleLv, int searchCount) {
		if (roleLv < openRoleLv) {
			return Collections.emptyList();
		}
		List<Long> list = new ArrayList<Long>();
		int minSearchLv = ((roleLv - UP_SEARCH_LV) <= openRoleLv) ? openRoleLv : (roleLv - UP_SEARCH_LV);
		// int minSearchLv = ((roleLv - UP_SEARCH_LV) <= 20) ? 20
		// : (roleLv - UP_SEARCH_LV);
		int maxSearchLv = ((roleLv + DOWN_SEARCH_LV) >= KRoleModuleConfig.getRoleMaxLv()) ? KRoleModuleConfig.getRoleMaxLv() : (roleLv + DOWN_SEARCH_LV);

		// int maxSearchLv = ((roleLv + DOWN_SEARCH_LV) >= 60) ? 60
		// : (roleLv + DOWN_SEARCH_LV);

		Set<Long> searchSet = new HashSet<Long>();
		int targetSearchCount = searchCount - searchSet.size();
		// 搜索大于自己等级
		if (roleLv < maxSearchLv) {
			for (int lv = (roleLv + 1); (lv <= maxSearchLv) && targetSearchCount > 0; lv++) {
				Set<Long> tempSet = search(myRoleId, lv, targetSearchCount, searchSet, false);
				targetSearchCount = targetSearchCount - tempSet.size();
			}
		}
		if (searchCount - searchSet.size() > 0) {
			targetSearchCount = searchCount - searchSet.size();
			for (int lv = roleLv; (lv >= minSearchLv) && targetSearchCount > 0; lv--) {
				Set<Long> tempSet = search(myRoleId, lv, targetSearchCount, searchSet, false);
				targetSearchCount = targetSearchCount - tempSet.size();
			}
		}
		list.addAll(searchSet);
		return list;
	}

	private static Set<Long> search(long myRoleId, int lv, int count, Set<Long> seachSet, boolean isLand) {
		int nowCount = 0;
		Set<Long> resultSet = new HashSet<Long>();
		List<Long> lvList = new ArrayList<Long>();
		if (isLand) {
			if (landTransporterRoleLvMap.get(lv) == null || landTransporterRoleLvMap.get(lv).isEmpty()) {
				return resultSet;
			}
			lvList.addAll(landTransporterRoleLvMap.get(lv));
		} else {
			if (waterTransporterRoleLvMap.get(lv) == null || waterTransporterRoleLvMap.get(lv).isEmpty()) {
				return resultSet;
			}
			lvList.addAll(waterTransporterRoleLvMap.get(lv));
		}
		UtilTool.randomList(lvList);

		for (int index = 0; index < lvList.size() && nowCount < count; index++) {
			long roleId = lvList.get(index);
			if (!seachSet.contains(roleId) && myRoleId != roleId) {
				KTransporter transporter = getTransporter(roleId);
				if (transporter != null && transporter.getInterceptCount() < MAX_INTERCEPT_COUNT) {
					seachSet.add(roleId);
					resultSet.add(roleId);
					nowCount++;
				}
			}
		}
		return resultSet;
	}

	public static void reflashCarrier(KRole role, boolean isNeedCheck) {

		if (role.getLevel() < openRoleLv) {
			KDialogService.sendUprisingDialog(role, TransportTips.getTipsNotOpen());
			return;
		}
		KTransportData data = KActivityRoleExtCaCreator.getActivityRoleExtData(role.getId()).getTransportData();
		if (data == null) {
			KDialogService.sendUprisingDialog(role, GlobalTips.getTipsServerBusy());
			return;
		}
		if (isRoleTransporting(role.getId())) {
			KDialogService.sendUprisingDialog(role, TransportTips.getTipsIsTransporting());
			return;
		}
		if (isNeedCheck) {
			KCurrencyCountStruct cur = getReflashUseCount(data);
			// if (data.getReflashCount() > 0) {
			// sendTips(
			// role,
			// KActivityModuleDialogProcesser.KEY_REFLASH_TRANSPORT_CARRIER,
			// TransportTips
			// .getTipsReflashCarrier(getReflashUseCount(data).currencyCount));
			// return;
			// }
			long result = KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), cur, UsePointFunctionTypeEnum.刷新运输载具, true);
			if (result == -1) {
				KDialogService.sendUprisingDialog(role, TransportTips.getTipsReflashCarrierNotEnoughIgot(cur.currencyCount, cur.currencyType.extName));
				return;
			}
		}

		CarrierData carrier = data.reflashCarrier(true);
		sendTransportData(role, false, false);
		KDialogService.sendUprisingDialog(role, TransportTips.getTipsReflashCarrierSuccess(carrier.getExtName()));
	}

	public static void clearCoolTime(KRole role, boolean isNeedCheck) {
		if (role.getLevel() < openRoleLv) {
			KDialogService.sendUprisingDialog(role, TransportTips.getTipsNotOpen());
			return;
		}
		KTransportData data = KActivityRoleExtCaCreator.getActivityRoleExtData(role.getId()).getTransportData();
		if (data == null) {
			KDialogService.sendUprisingDialog(role, GlobalTips.getTipsServerBusy());
			return;
		}

		data.clearInterceptCoolTime();
		sendTransportData(role, false, false);
	}

	private static void sendTips(KRole role, short key, String tips) {
		List<KDialogButton> list = new ArrayList<KDialogButton>();
		list.add(KDialogButton.CANCEL_BUTTON);
		list.add(new KDialogButton(key, "", KDialogButton.CONFIRM_DISPLAY_TEXT));

		KDialogService.sendFunDialog(role, GlobalTips.getTipsDefaultTitle(), tips, list, true, (byte) -1);
	}

	public static int getCarrierBaseExp(int carrierId, int roleLv) {
		return baseExpMap.get(carrierId).get(roleLv);
	}

	public static int getCarrierBasePotential(int carrierId, int roleLv) {
		return basePotentialMap.get(carrierId).get(roleLv);
	}

	// public static int getCarrierBaseExpByActivityRate(int carrierId,
	// int roleLv, float rate) {
	// return (int) (baseExpMap.get(carrierId).get(roleLv) * rate);
	// }
	//
	// public static int getCarrierBasePotentialByActivityRate(int carrierId,
	// int roleLv, float rate) {
	// return (int) (basePotentialMap.get(carrierId).get(roleLv) * rate);
	// }

	public static void saveTransporters() {

		try {
			List<DBGameExtCA> addList = new ArrayList<DBGameExtCA>();

			for (KTransporter transporter : transporterMap.values()) {
				if (transporter != null) {
					if (transporter.getRoleId() > 0 && transporter.getJobType() >= 0 && transporter.getCarrier() != null) {
						addList.add(transporter.encode());
					}
				}
			}
			if (!addList.isEmpty()) {
				try {
					DataAccesserFactory.getGameExtCADataAccesser().deleteDBGameExtCAByType(KGameExtDataDBTypeEnum.物资运送数据.dbType);

					DataAccesserFactory.getGameExtCADataAccesser().addDBGameExtCAs(addList);
				} catch (KGameDBException e) {
					_LOGGER.error("保存物资运输数据出错！", e);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void readTransporters() {
		try {
			List<DBGameExtCA> list = DataAccesserFactory.getGameExtCADataAccesser().getDBGameExtCA(KGameExtDataDBTypeEnum.物资运送数据.dbType);
			if (!list.isEmpty()) {
				for (DBGameExtCA ca : list) {
					KTransporter transporter = new KTransporter();
					if (transporter.decode(ca)) {
						boolean isEnd = transporter.startTranspot();
						if (!isEnd) {
							addTransporter(transporter);
						}
					}
				}

				// DataAccesserFactory.getGameExtCADataAccesser()
				// .deleteDBGameExtCAByType(
				// KGameExtDataDBTypeEnum.物资运送数据.dbType);
			}
		} catch (KGameDBException e) {
			_LOGGER.error("加载物资运输数据出错！", e);
		}

	}

	public static void pushInterceptHistoryData(KRole role, InterceptHistory... info) {
		KGameMessage sendMsg = KGame.newLogicMessage(KActivityProtocol.SM_PUSH_INTERCEPT_HISTORY);
		sendMsg.writeBoolean(false);
		sendMsg.writeInt(info.length);
		for (int i = 0; i < info.length; i++) {
			sendMsg.writeBoolean(info[i].isWin);
			sendMsg.writeUtf8String(info[i].info);
		}
		role.sendMsg(sendMsg);
	}

	public static void pushInterceptHistoryData(KRole role, List<InterceptHistory> infoList) {
		KGameMessage sendMsg = KGame.newLogicMessage(KActivityProtocol.SM_PUSH_INTERCEPT_HISTORY);
		sendMsg.writeBoolean(true);
		sendMsg.writeInt(infoList.size());
		for (InterceptHistory info : infoList) {
			sendMsg.writeBoolean(info.isWin);
			sendMsg.writeUtf8String(info.info);
		}

		role.sendMsg(sendMsg);
	}

	public static class TransportCombatData {
		public long roleId;
		public int roleLv;
		public int carrierId;
		public String transportRoleName;

		public TransportCombatData(long roleId, int roleLv, int carrierId, String transportRoleName) {
			this.roleId = roleId;
			this.roleLv = roleLv;
			this.carrierId = carrierId;
			this.transportRoleName = transportRoleName;
		}
	}

	public static void main(String[] a) {
		// UtilTool.random(10);
		// List<Long> list = new ArrayList<Long>();
		// for (long i = 0; i < 1000; i++) {
		// list.add(i);
		// }
		//
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<Integer> aa = new ArrayList<Integer>();
		aa.add(1);
		aa.add(2);
		aa.add(3);
		aa.add(4);
		for (int i = (aa.size() - 1); i >= 0; i--) {
			int bb = aa.get(i);
			System.out.println(bb);
		}
		//
		// long time = System.currentTimeMillis();
		// List<Long> list1 = new ArrayList<Long>();
		// list1.addAll(list);
		//
		// UtilTool.randomList(list1);
		//
		// time = System.currentTimeMillis() - time;
		// System.out.println(time);
		// System.out.println(list1.get(0));
		// System.out.println(list1.get(1));
		// System.out.println(list1.get(2));

		KTransportManager manager = new KTransportManager();

		int ca = manager.caculateInterceptGetResource(10, 30, 10000);
		System.out.println("ca:" + ca);

		int perLvCount = 100;
		for (int lv = 20; lv <= 60; lv++) {
			manager.landTransporterRoleLvMap.put(lv, new HashSet<Long>());
			manager.waterTransporterRoleLvMap.put(lv, new HashSet<Long>());
			for (long id = (lv * 10000); (id < (lv * 10000) + perLvCount) && lv <= 60; id++) {
				manager.landTransporterRoleLvMap.get(lv).add(id);
				// System.out.println("add_id:" + id);
			}
			for (long id = (lv * 100000); (id < (lv * 100000) + perLvCount) && lv <= 23; id++) {
				manager.waterTransporterRoleLvMap.get(lv).add(id);
				// System.out.println("add_id:" + id);
			}
		}
		long time = System.currentTimeMillis();
		List<Long> list = manager.searchLandTransporter(1, 23, 9);
		time = System.currentTimeMillis() - time;
		System.out.println(time);
		Collections.sort(list);
		for (Long id : list) {
			System.out.println("land_id:" + id);
		}

		List<Long> waterList = manager.searchWaterTransporter(1, 23, 2);
		Collections.sort(waterList);
		for (Long id : waterList) {
			System.out.println("water_id:" + id);
		}
	}

}
