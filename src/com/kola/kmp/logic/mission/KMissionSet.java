package com.kola.kmp.logic.mission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.mission.impl.KAMissionSet;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kgame.db.dataobject.DBMissionData;
import com.kola.kmp.logic.mission.daily.KDailyMissionManager;
import com.kola.kmp.logic.mission.daily.KDailyMissionManager.DailyMissionQualityType;
import com.kola.kmp.logic.mission.guide.FunctionOpenRecord;
import com.kola.kmp.logic.other.KGameMissionDBTypeEnum;
import com.kola.kmp.logic.other.KGameMissionTemplateTypeEnum;
import com.kola.kmp.logic.role.KRole;

public class KMissionSet extends KAMissionSet<KMission> {

	private static final int VERSION_FIRST = 20130108;
	private static final int CURRENT_VERSION = VERSION_FIRST;

	private static final String KEY_VERSION = "K0";

	private static final String KEY_DAILY_MISSION_INFO = "K1";

	private static final String KEY_FUNCTION_RECORD = "K2";

	/**
	 * 角色最大接受修行任务的数量上限
	 */
	public static final int MAX_ACCEPTABLE_DAILY_MISSION_COUNT = 3;

	// 已接未关闭的任务表:Key:任务模版ID
	private HashMap<Integer, KMission> unclosedMissionMap;
	// 空数据的任务列表
	private List<KMission> emptyMissionList;

	// 可接受任务的模版表
	private HashMap<Integer, KMissionTemplate> acceptableMissionTemplateMap;

	// 修行（日常）任务记录列表
	private LinkedHashMap<Integer, KMission> dailyMissionMap;

	// 未完成任务数量
	private int uncompletedMissionSize = 0;
	// 当前角色正在操作的任务模版Id
	public int currentTargetMissionTemplateId = -1;
	// 日常任务数据记录
	private DailyMissionInfo dailyMissionInfo;
	// 功能开放记录
	public Map<Short, FunctionOpenRecord> funtionMap = new LinkedHashMap<Short, FunctionOpenRecord>();

	protected KMissionSet(long roleId, boolean isFirstNew) {
		super(roleId, isFirstNew);
		unclosedMissionMap = new HashMap<Integer, KMission>();
		emptyMissionList = new ArrayList<KMission>();
		acceptableMissionTemplateMap = new HashMap<Integer, KMissionTemplate>();
		dailyMissionMap = new LinkedHashMap<Integer, KMission>();
	}

	@Override
	protected Map<Long, KMission> initMissions(List<DBMissionData> dbdatas) {
		Map<Long, KMission> result = new HashMap<Long, KMission>();
		for (DBMissionData dbdata : dbdatas) {
			KMission mission = new KMission(this, dbdata);
			result.put(mission.getId(), mission);
			if (mission.getMissionDbType() == KGameMissionDBTypeEnum.MISSION_DB_TYPE_UNCLOSED) {
				unclosedMissionMap
						.put(mission.getMissionTemplate().missionTemplateId,
								mission);
			} else if (mission.getMissionDbType() == KGameMissionDBTypeEnum.MISSION_DB_TYPE_EMPTY) {
				emptyMissionList.add(mission);
			} else if (mission.getMissionDbType() == KGameMissionDBTypeEnum.MISSION_DB_TYPE_DAILY) {
				dailyMissionMap
						.put(mission.getMissionTemplate().missionTemplateId,
								mission);
			}
		}
		return result;
	}

	@Override
	protected void decodeCA(String attribute) {
		// TODO 处理角色的关卡数据记录扩展属性的decode方法
		try {
			JSONObject obj = new JSONObject(attribute);
			int version = obj.optInt(KEY_VERSION);
			obj.remove(KEY_VERSION);
			switch (version) {
			case VERSION_FIRST:
				this.decodeV1(obj);
				break;
			}
		} catch (Exception e) {
			// TODO 解析角色剧本关卡记录扩展属性的处理
			_LOGGER.error("解析角色剧本关卡记录扩展属性的时候出现异常！！arrt=" + attribute, e);
		}
	}

	private void decodeV1(JSONObject obj) throws Exception {
		try {
			if (obj.has(KEY_DAILY_MISSION_INFO)) {
				String infoStr = obj.optString(KEY_DAILY_MISSION_INFO, null);
				if (infoStr != null) {
					decodeDailyMissionInfo(infoStr);
				}
				obj.remove(KEY_DAILY_MISSION_INFO);
			}
			if (obj.has(KEY_FUNCTION_RECORD)) {
				decodeFunctionRecord(obj.getString(KEY_FUNCTION_RECORD));
			}
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	protected String encodeCA() {
		// TODO 处理角色的关卡数据记录扩展属性的encode方法
		JSONObject obj = new JSONObject();
		try {
			obj.put(KEY_VERSION, CURRENT_VERSION);
			String dailyMissionInfoStr = encodeDailyMissionInfoString();
			if (dailyMissionInfoStr != null) {
				obj.put(KEY_DAILY_MISSION_INFO, dailyMissionInfoStr);
			}
			if (funtionMap != null && funtionMap.size() > 0) {
				obj.put(KEY_FUNCTION_RECORD, encodeFunctionRecord());
			}
		} catch (Exception ex) {
			_LOGGER.error("encodeAttribute出现异常！此时json的字符串是：" + obj.toString(),
					ex);
		}
		return obj.toString();
	}

	public void decodeFunctionRecord(String data) throws Exception {
		if (data != null) {
			JSONObject obj = new JSONObject(data);

			JSONArray array = obj.names();
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					String recordKey = array.getString(i);
					FunctionOpenRecord record = new FunctionOpenRecord();
					record.decodeAttribute(recordKey, obj.getString(recordKey));
					funtionMap.put(record.funtionId, record);
				}
			}
		}
	}

	public String encodeFunctionRecord() throws Exception {
		JSONObject obj = new JSONObject();
		for (Short funId : funtionMap.keySet()) {
			FunctionOpenRecord record = funtionMap.get(funId);
			obj.put(funId + "", record.encodeAttribute());
		}
		return obj.toString();
	}

	public DailyMissionInfo getDailyMissionInfo() {
		return dailyMissionInfo;
	}

	/**
	 * 从空数据任务记录列表中取出一笔空数据任务记录，并设置对应DB保存类型KGameMissionDBTypeEnum
	 * 
	 * @param dbType
	 * @return
	 */
	private KMission getAndUseEmptyMission(KGameMissionDBTypeEnum dbType) {
		if (!emptyMissionList.isEmpty()) {

			KMission mission = emptyMissionList.remove(0);
			if (mission != null) {
				mission.setMissionDbType(dbType);
			}
			return mission;

		}
		return null;
	}

	/**
	 * 根据任务模版接受一个新任务
	 * 
	 * @param missionTemplate
	 * @return
	 */
	public KMission receiveNewMission(KRole role,
			KMissionTemplate missionTemplate) {
		if (missionTemplate != null) {
			rwlock.lock();
			try {

				// 尝试从空数据任务记录表中取出一个缓存任务数据对象
				KMission mission = getAndUseEmptyMission(KGameMissionDBTypeEnum.MISSION_DB_TYPE_UNCLOSED);
				if (mission == null) {
					// 如果没有空数据任务，则创建一笔新的任务数据缓存记录,并将DB保存类型
					// 设置为KGameMissionDBTypeEnum.MISSION_DB_TYPE_CLOSED
					mission = new KMission(this,
							KGameMissionDBTypeEnum.MISSION_DB_TYPE_UNCLOSED
									.getMissionDbType(),
							missionTemplate.missionTemplateId);
					this.addMission(mission);
				}
				// 初始化任务记录数据
				mission.initNowAcceptedMission(missionTemplate, role.getJob());
				// 讲此任务放入未完成任务列表
				this.unclosedMissionMap
						.put(mission.getMissionTemplate().missionTemplateId,
								mission);

				// 通知该KMission对数据对象更改数据状态为KGameDataStatus.STATUS_UPDATE
				mission.notifyDB();
				// 如果mission非null，表示接受任务成功，则从任务容器的可接任务模版列表中删除该任务
				this.acceptableMissionTemplateMap
						.remove(missionTemplate.missionTemplateId);
				// 设置当前正在操作的任务模版Id
				this.currentTargetMissionTemplateId = mission
						.getMissionTemplate().missionTemplateId;

				return mission;

			} finally {
				rwlock.unlock();
			}
		} else {
			return null;
		}
	}

	/**
	 * 放弃一个已接的任务
	 * 
	 * @param missionTemplate
	 * @return
	 */
	public boolean dropMission(KMissionTemplate missionTemplate) {
		if (!this.unclosedMissionMap
				.containsKey(missionTemplate.missionTemplateId)) {
			return false;
		}
		rwlock.lock();
		try {
			// 从未关闭任务中移除这个任务
			KMission dropMission = unclosedMissionMap
					.remove(missionTemplate.missionTemplateId);

			if (dropMission != null) {
				// 清空任务数据，并将此任务数据对象设置为空数据任务：KGameMissionDBTypeEnum.MISSION_DB_TYPE_EMPTY
				dropMission.notifyMissionCompletedAndSetEmptyData();
				// 将这个任务模版添加进可接任务表
				this.acceptableMissionTemplateMap.put(
						missionTemplate.missionTemplateId, missionTemplate);
				// 当前正在操作的任务模版Id等于放弃的任务模版ID，则将其设置为-1
				if (this.currentTargetMissionTemplateId == missionTemplate.missionTemplateId) {
					this.currentTargetMissionTemplateId = -1;
				}
				return true;
			}
		} finally {
			rwlock.unlock();
		}
		return false;
	}

	/**
	 * 完成一个任务，如果成功返回true，失败返回false
	 * 
	 * @param mission
	 * @return
	 */
	public boolean completeMission(KMission mission) {
		// 如果任务数据DB保存类型和任务模版非null，并且任务模版的任务类型不为日常任务
		if (mission != null
				&& mission.getMissionDbType() != null
				&& mission.getMissionTemplate() != null
				&& mission.getMissionTemplate().missionType != KGameMissionTemplateTypeEnum.MISSION_TYPE_DAILY) {
			// 如果任务数据DB保存类型为KGameMissionDBTypeEnum.MISSION_DB_TYPE_UNCLOSED
			if (mission.getMissionDbType() == KGameMissionDBTypeEnum.MISSION_DB_TYPE_UNCLOSED) {
				// 在已接未关闭的任务表中清除该任务
				rwlock.lock();
				try {
					unclosedMissionMap
							.remove(mission.getMissionTemplate().missionTemplateId);

					// 添加这个已完成的任务模版Id
					KMissionModuleExtension.getMissionCompleteRecordSet(
							_ownerId).addCompletedMission(
							mission.getMissionTemplate());
					int completedMissionTemplateId = mission
							.getMissionTemplate().missionTemplateId;

					// 清空任务数据，并将此任务数据对象设置为空数据任务：KGameMissionDBTypeEnum.MISSION_DB_TYPE_EMPTY
					mission.notifyMissionCompletedAndSetEmptyData();

					// 往空数据的任务列表添加这个任务(最后一步)
					this.emptyMissionList.add(mission);

					// 当前正在操作的任务模版Id等于放弃的任务模版ID，则将其设置为-1
					if (this.currentTargetMissionTemplateId == completedMissionTemplateId) {
						this.currentTargetMissionTemplateId = -1;
					}

				} finally {
					rwlock.unlock();
				}

				return true;
			}
		}
		return false;
	}

	public void completeNoviceGuideMission() {
		rwlock.lock();
		try {
			KMission mission = unclosedMissionMap.remove(1);
			KMissionTemplate template = KMissionModuleExtension.getManager()
					.getMissionTemplate(1);
			// 添加这个已完成的任务模版Id
			KMissionModuleExtension.getMissionCompleteRecordSet(_ownerId)
					.addCompletedMission(template);
			int completedMissionTemplateId = template.missionTemplateId;

			if (mission != null) {
				// 清空任务数据，并将此任务数据对象设置为空数据任务：KGameMissionDBTypeEnum.MISSION_DB_TYPE_EMPTY
				mission.notifyMissionCompletedAndSetEmptyData();

				// 往空数据的任务列表添加这个任务(最后一步)
				this.emptyMissionList.add(mission);

				// 当前正在操作的任务模版Id等于放弃的任务模版ID，则将其设置为-1
				if (this.currentTargetMissionTemplateId == completedMissionTemplateId) {
					this.currentTargetMissionTemplateId = -1;
				}
			}

		} finally {
			rwlock.unlock();
		}
	}

	/**
	 * 检测任务是否已接受且未完成、或处于为完结关闭状态
	 * 
	 * @param missiontemplateId
	 * @return
	 */
	public boolean checkMissionIsUncompleted(int missiontemplateId) {
		if (this.unclosedMissionMap.containsKey(missiontemplateId)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 检测任务是否可接
	 * 
	 * @param missiontemplateId
	 * @return
	 */
	public boolean checkMissionCanAccepted(KRole role, int missiontemplateId) {
		if (this.acceptableMissionTemplateMap.containsKey(missiontemplateId)) {
			KMissionTemplate template = KMissionModuleExtension.getManager()
					.getMissionTemplate(missiontemplateId);
			if (template.isMissionCanAccept(role)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public Map<Integer, KMissionTemplate> getAcceptableMissionTemplateMap() {
		return acceptableMissionTemplateMap;
	}

	public List<KMissionTemplate> getAllAcceptableMissionTemplate() {
		return new ArrayList<KMissionTemplate>(
				acceptableMissionTemplateMap.values());
	}

	public List<KMission> getAllUnclosedMission() {
		return new ArrayList<KMission>(unclosedMissionMap.values());
	}

	/**
	 * 根据任务模版ID获取为未完结关闭的任务
	 * 
	 * @param missionTemplateId
	 * @return
	 */
	public KMission getUnclosedMission(int missionTemplateId) {
		// rwlock.lock();
		// try {
		return this.unclosedMissionMap.get(missionTemplateId);
		// } finally {
		// rwlock.unlock();
		// }
	}

	/**
	 * 获取日常任务列表
	 * 
	 * @return
	 */
	public Map<Integer, KMission> getDailyMissionMap() {
		return dailyMissionMap;
	}

	public KMission getDailyMission(int missionTemplateId) {
		return dailyMissionMap.get(missionTemplateId);
	}

	public boolean isDailyMissionExist(int missionTemplateId) {
		return dailyMissionMap.containsKey(missionTemplateId);
	}

	/**
	 * 处理完成一个日常任务
	 * 
	 * @param role
	 * @param mission
	 * @return
	 */
	public UpdateDailyMissionStruct completeDailyMission(KRole role,
			KMission mission) {
		UpdateDailyMissionStruct struct = new UpdateDailyMissionStruct();
		// 如果任务数据DB保存类型和任务模版非null，并且任务模版的任务类型为日常任务
		if (mission != null
				&& mission.getMissionDbType() == KGameMissionDBTypeEnum.MISSION_DB_TYPE_DAILY
				&& mission.getMissionTemplate() != null
				&& mission.getMissionTemplate().missionType == KGameMissionTemplateTypeEnum.MISSION_TYPE_DAILY) {
			rwlock.lock();
			try {
				mission = dailyMissionMap
						.remove(mission.getMissionTemplate().missionTemplateId);
				if (mission != null) {

					this.dailyMissionInfo.todayCompletedMissionCount
							.incrementAndGet();
					this.dailyMissionInfo.restFreeCompletedMissionCount
							.decrementAndGet();
					this.dailyMissionInfo.completeMissionTimeMillis = System
							.currentTimeMillis();
					this.dailyMissionInfo.totalStar
							.addAndGet(mission.qualityType.jifen);
					notifyDB();

					struct.deleteMissionIdList.add(mission
							.getMissionTemplateId());
					mission.clearDailyMissionData();
					KMissionTemplate newTemplate = null;
					while (newTemplate == null
							|| dailyMissionMap
									.containsKey(newTemplate.missionTemplateId)) {
						newTemplate = KMissionModuleExtension.getManager()
								.getDailyMissionManager()
								.caculateNewMissionTemplate(role);
					}
					DailyMissionQualityType quality = KMissionModuleExtension
							.getManager().getDailyMissionManager()
							.caculateDailyMissionQualityType();
					mission.initDailyMission(role.getLevel(), newTemplate,
							quality, role.getJob());
					// 将新任务放入日常任务map
					dailyMissionMap.put(newTemplate.missionTemplateId, mission);
					mission.notifyDB();
					struct.addMissionList.add(mission);
					struct.isNeedNotify = true;
				}
			} finally {
				rwlock.unlock();
			}
		}

		return struct;
	}

	/**
	 * 刷新所有日常任务
	 * 
	 * @param role
	 * @return
	 */
	public UpdateDailyMissionStruct reflashDailyMission(KRole role,
			boolean isUsePoint) {
		UpdateDailyMissionStruct struct = new UpdateDailyMissionStruct();
		List<KMission> preAddMission = new ArrayList<KMission>();
		rwlock.lock();
		try {
			for (Integer mTempId : this.dailyMissionMap.keySet()) {
				struct.deleteMissionIdList.add(mTempId);
			}
			for (Integer mTempId : struct.deleteMissionIdList) {
				KMission mission = this.dailyMissionMap.remove(mTempId);
				mission.clearDailyMissionData();
				preAddMission.add(mission);
			}
			while (preAddMission.size() < MAX_ACCEPTABLE_DAILY_MISSION_COUNT) {
				KMission mission = getAndUseEmptyMission(KGameMissionDBTypeEnum.MISSION_DB_TYPE_DAILY);
				if (mission == null) {
					mission = new KMission(this,
							KGameMissionDBTypeEnum.MISSION_DB_TYPE_DAILY
									.getMissionDbType(), 0);
					this.addMission(mission);
				}
				preAddMission.add(mission);
			}
			List<KMissionTemplate> preAddMissionTempList = new ArrayList<KMissionTemplate>();
			int caculateTimes = 0;
			while (preAddMissionTempList.size() < MAX_ACCEPTABLE_DAILY_MISSION_COUNT
					&& preAddMissionTempList.size() < preAddMission.size() && caculateTimes < 10) {
				KMissionTemplate newTemplate = KMissionModuleExtension
						.getManager().getDailyMissionManager()
						.caculateNewMissionTemplate(role);
				if (newTemplate != null
						&& !preAddMissionTempList.contains(newTemplate)) {
					preAddMissionTempList.add(newTemplate);
				}
				caculateTimes ++;
			}
			List<DailyMissionQualityType> qualityList = new ArrayList<DailyMissionQualityType>();
			if (dailyMissionInfo.reflashType == KDailyMissionManager.MISSION_REFLASH_TYPE_FIRST) {
				for (int i = 0; i < MAX_ACCEPTABLE_DAILY_MISSION_COUNT && i < preAddMission.size(); i++) {
					qualityList.add(KDailyMissionManager.default_mission_quality_type);
				}
				this.dailyMissionInfo.reflashType = KDailyMissionManager.MISSION_REFLASH_TYPE_GUIDE;
				this.notifyDB();
			} else if (dailyMissionInfo.reflashType == KDailyMissionManager.MISSION_REFLASH_TYPE_GUIDE) {
				for (int i = 0; i < MAX_ACCEPTABLE_DAILY_MISSION_COUNT && i < preAddMission.size(); i++) {
					qualityList.add(KMissionModuleExtension.getManager().getDailyMissionManager().guideQualityList.get(i));
				}
				this.dailyMissionInfo.reflashType = KDailyMissionManager.MISSION_REFLASH_TYPE_NORMAL;
				this.notifyDB();
			} else {
				boolean hasHighQuality = false;
				for (int i = 0; i < MAX_ACCEPTABLE_DAILY_MISSION_COUNT && i < preAddMission.size(); i++) {
					DailyMissionQualityType quality = KMissionModuleExtension.getManager().getDailyMissionManager().caculateDailyMissionQualityType();
					if (quality.star > 2) {
						hasHighQuality = true;
					}
					qualityList.add(quality);
				}
				if (!hasHighQuality && isUsePoint) {
					qualityList.remove(0);
					qualityList.add(KDailyMissionManager.default_use_point_mission_quality_type);
				}
			}

			for (int i = 0; i < MAX_ACCEPTABLE_DAILY_MISSION_COUNT
					&& i < preAddMission.size() && i < preAddMissionTempList.size(); i++) {
				KMission mission = preAddMission.get(i);
				KMissionTemplate newTemplate = preAddMissionTempList.get(i);
//				DailyMissionQualityType quality = KMissionModuleExtension
//						.getManager().getDailyMissionManager()
//						.caculateDailyMissionQualityType();
				DailyMissionQualityType quality = qualityList.get(i);
				mission.initDailyMission(role.getLevel(), newTemplate, quality,
						role.getJob());
				// 将新任务放入日常任务map
				dailyMissionMap.put(newTemplate.missionTemplateId, mission);
				mission.notifyDB();
				struct.addMissionList.add(mission);
			}
			struct.isNeedNotify = true;
		} finally {
			rwlock.unlock();
		}

		return struct;
	}

	private void decodeDailyMissionInfo(String dbData) throws Exception {
		// String[] infoStr = str.split(",");
		// if (infoStr.length < 7) {
		// return;
		// }
		// int todayCompletedMissionCount = Integer.parseInt(infoStr[0]);
		// int restFreeCompletedMissionCount = Integer.parseInt(infoStr[1]);
		// int todayManualReflashCount = Integer.parseInt(infoStr[2]);
		// long lastReflashTimeMillis = Long.parseLong(infoStr[3]);
		// long completeMissionTimeMillis = Long.parseLong(infoStr[4]);
		// long lastRestCheckTime = Long.parseLong(infoStr[5]);
		// int totalStar = Integer.parseInt(infoStr[6]);
		//
		// initDailyMissionInfo(todayCompletedMissionCount,
		// restFreeCompletedMissionCount, todayManualReflashCount,
		// lastReflashTimeMillis, completeMissionTimeMillis, false,
		// lastRestCheckTime, totalStar);
		this.dailyMissionInfo = new DailyMissionInfo();
		this.dailyMissionInfo.decode(dbData);
	}

	private String encodeDailyMissionInfoString() throws Exception {
		if (this.dailyMissionInfo != null) {
			// return this.dailyMissionInfo.todayCompletedMissionCount.get() +
			// ","
			// + this.dailyMissionInfo.restFreeCompletedMissionCount.get()
			// + "," + this.dailyMissionInfo.todayManualReflashCount.get()
			// + "," + this.dailyMissionInfo.lastReflashTimeMillis + ","
			// + this.dailyMissionInfo.completeMissionTimeMillis + ","
			// + this.dailyMissionInfo.lastRestCheckTime + ","
			// + this.dailyMissionInfo.totalStar.get();
			return this.dailyMissionInfo.encode();
		} else {
			return null;
		}
	}

	/**
	 * 初始化日常任务数据记录
	 * 
	 * @param todayCompletedMissionCount
	 * @param todayManualReflashCount
	 * @param lastReflashTimeMillis
	 * @param completeMissionTimeMillis
	 * @param isGetTodayPriceBox
	 * @param isNewRecord
	 * @param lastRestCheckTime
	 * @param totalStar
	 */
	public void initDailyMissionInfo(int todayCompletedMissionCount,
			int restFreeCompletedMissionCount, int todayManualReflashCount,
			long lastReflashTimeMillis, long completeMissionTimeMillis,
			boolean isNewRecord, long lastRestCheckTime, int totalStar,
			int buyCount) {
		if (this.dailyMissionInfo == null) {
			this.dailyMissionInfo = new DailyMissionInfo(
					todayCompletedMissionCount, restFreeCompletedMissionCount,
					todayManualReflashCount, lastReflashTimeMillis,
					completeMissionTimeMillis, lastRestCheckTime, totalStar,
					buyCount);
			this.dailyMissionInfo.reflashType = KDailyMissionManager.MISSION_REFLASH_TYPE_FIRST;
		} else {
			this.dailyMissionInfo.todayCompletedMissionCount
					.set(todayCompletedMissionCount);
			this.dailyMissionInfo.restFreeCompletedMissionCount
					.set(restFreeCompletedMissionCount);
			this.dailyMissionInfo.todayManualReflashCount
					.set(todayManualReflashCount);
			this.dailyMissionInfo.lastReflashTimeMillis = lastReflashTimeMillis;
			this.dailyMissionInfo.completeMissionTimeMillis = completeMissionTimeMillis;
			this.dailyMissionInfo.lastRestCheckTime = lastRestCheckTime;
			this.dailyMissionInfo.totalStar.set(totalStar);
			this.dailyMissionInfo.buyCount = buyCount;
		}
		if (isNewRecord) {
			notifyDB();
		}
	}

	/**
	 * 检测并重置修行任务记录数据
	 */
	public boolean checkAndResetDailyMissionInfo(boolean isNeedCheck) {
		boolean isDataChanged = false;
		if (!isNeedCheck
				|| (isNeedCheck && UtilTool
						.checkNowTimeIsArriveTomorrow(this.dailyMissionInfo.lastRestCheckTime))) {
			this.dailyMissionInfo.lastRestCheckTime = System
					.currentTimeMillis();
			this.dailyMissionInfo.todayCompletedMissionCount.set(0);
			this.dailyMissionInfo.restFreeCompletedMissionCount
					.set(KDailyMissionManager.free_complete_mission_count);
			this.dailyMissionInfo.todayManualReflashCount.set(0);
			this.dailyMissionInfo.totalStar.set(0);
			this.dailyMissionInfo.getPriceBoxMap.clear();
			this.dailyMissionInfo.buyCount = 0;
			isDataChanged = true;
		}

		if (isDataChanged) {
			notifyDB();
		}

		return isDataChanged;
	}

	/**
	 * 记录手动刷新修行任务
	 */
	public void recordManualReflashDailyMission() {
		this.dailyMissionInfo.todayManualReflashCount.incrementAndGet();
		this.dailyMissionInfo.lastReflashTimeMillis = System
				.currentTimeMillis();
		notifyDB();
	}

	/**
	 * 记录购买修行任务
	 */
	public void buyDailyMission() {
		this.dailyMissionInfo.restFreeCompletedMissionCount
				.addAndGet(KDailyMissionManager.add_complete_mission_count);
		this.dailyMissionInfo.buyCount++;
		notifyDB();
	}

	// /**
	// * 完成某个修行任务，记录增加当天完成次数
	// */
	// public void recordCompleteDailyMissionCount() {
	// this.dailyMissionInfo.todayCompletedMissionCount.incrementAndGet();
	// this.dailyMissionInfo.completeMissionTimeMillis = System
	// .currentTimeMillis();
	// this.dailyMissionInfo.totalStar.addAndGet()
	// notifyDB();
	// }

	/**
	 * 记录获取日常任务宝箱奖励
	 */
	public void recordGetDailyMissionPriceBox(byte boxId) {
		this.dailyMissionInfo.getPriceBoxMap.put(boxId, true);
		notifyDB();
	}

	public boolean isGetDailyMissionPriceBox(byte boxId) {
		if (this.dailyMissionInfo.getPriceBoxMap.containsKey(boxId)) {
			return this.dailyMissionInfo.getPriceBoxMap.get(boxId);
		}
		return false;
	}

	/**
	 * 添加或修改一个功能开放记录
	 * 
	 * @param funtionId
	 * @param funtionBigType
	 * @param isOpen
	 * @param isAlreadyGuide
	 */
	public FunctionOpenRecord addOrUpdateFunctionInfo(short functionId,
			boolean isOpen, boolean isAlreadyGuide) {
		FunctionOpenRecord record = null;
		if (!funtionMap.containsKey(functionId)) {
			record = new FunctionOpenRecord(functionId, isOpen, isAlreadyGuide);
			funtionMap.put(functionId, record);
		} else {
			record = funtionMap.get(functionId);
			record.isOpen = isOpen;
			record.isAlreadyGuide = isAlreadyGuide;
		}
		notifyDB();

		return record;
	}

	/**
	 * 更新日常任务的数据结构
	 * 
	 * @author zhaizl
	 * 
	 */
	public static class UpdateDailyMissionStruct {
		public boolean isNeedNotify = false;
		public List<Integer> deleteMissionIdList = new ArrayList<Integer>();
		public List<KMission> updateMissionList = new ArrayList<KMission>();
		public List<KMission> addMissionList = new ArrayList<KMission>();
	}

	public static class DailyMissionInfo {

		private static final String KEY_COMPLETE_COUNT = "1";
		private static final String KEY_REST_FREE_COMPLETE_COUNT = "2";
		private static final String KEY_MANUAL_REFLASH_COUNT = "3";
		private static final String KEY_LAST_REFLASH_TIME = "4";
		private static final String KEY_COMPLETE_TIME = "5";
		private static final String KEY_LAST_RESET_TIME = "6";
		private static final String KEY_TOTAL_STAR = "7";
		private static final String KEY_GET_PRICE_BOX_INFO = "8";
		private static final String KEY_BUY_COUNT = "9";
		private static final String KEY_REFLASH_TYPE = "10";

		/**
		 * 今日完成修行任务的数量
		 */
		private AtomicInteger todayCompletedMissionCount;
		/**
		 * 今日剩余免费完成任务的数量
		 */
		private AtomicInteger restFreeCompletedMissionCount;
		/**
		 * 今日手动刷新任务列表的次数
		 */
		private AtomicInteger todayManualReflashCount;
		/**
		 * 最近一次刷新任务列表的时间
		 */
		private long lastReflashTimeMillis;
		/**
		 * 每次完成任务变化时间记录
		 */
		private long completeMissionTimeMillis;

		/**
		 * 上次重置时间
		 */
		private long lastRestCheckTime;

		/**
		 * 今日手动刷新任务列表的次数
		 */
		private AtomicInteger totalStar;

		private Map<Byte, Boolean> getPriceBoxMap = new LinkedHashMap<Byte, Boolean>();

		private int buyCount;
		// 刷新类型
		public byte reflashType = KDailyMissionManager.MISSION_REFLASH_TYPE_NORMAL;

		public DailyMissionInfo() {
		}

		public DailyMissionInfo(int todayCompletedMissionCount,
				int restFreeCompletedMissionCount, int todayManualReflashCount,
				long lastReflashTimeMillis, long completeMissionTimeMillis,
				long lastRestCheckTime, int totalStar, int buyCount) {
			super();
			this.todayCompletedMissionCount = new AtomicInteger(
					todayCompletedMissionCount);
			this.restFreeCompletedMissionCount = new AtomicInteger(
					restFreeCompletedMissionCount);
			this.todayManualReflashCount = new AtomicInteger(
					todayManualReflashCount);
			this.lastReflashTimeMillis = lastReflashTimeMillis;
			this.completeMissionTimeMillis = completeMissionTimeMillis;
			this.lastRestCheckTime = lastRestCheckTime;
			this.totalStar = new AtomicInteger(totalStar);
			this.buyCount = buyCount;
		}

		public int getTodayCompletedMissionCount() {
			return todayCompletedMissionCount.get();
		}

		public int getTodayManualReflashCount() {
			return todayManualReflashCount.get();
		}

		public long getLastReflashTimeMillis() {
			return lastReflashTimeMillis;
		}

		public long getCompleteMissionTimeMillis() {
			return completeMissionTimeMillis;
		}

		public long getLastRestCheckTime() {
			return lastRestCheckTime;
		}

		public int getTotalStar() {
			return totalStar.get();
		}

		public int getRestFreeCompletedMissionCount() {
			return restFreeCompletedMissionCount.get();
		}

		public int getBuyCount() {
			return buyCount;
		}

		public void decode(String dbData) throws Exception {
			if (dbData != null && dbData.length() > 0 && dbData.startsWith("{")) {
				JSONObject obj = new JSONObject(dbData);
				this.todayCompletedMissionCount = new AtomicInteger(obj.optInt(
						KEY_COMPLETE_COUNT, 0));
				this.restFreeCompletedMissionCount = new AtomicInteger(
						obj.optInt(KEY_REST_FREE_COMPLETE_COUNT, 0));
				this.todayManualReflashCount = new AtomicInteger(obj.optInt(
						KEY_MANUAL_REFLASH_COUNT, 0));
				this.lastReflashTimeMillis = obj.optLong(KEY_LAST_REFLASH_TIME,
						0);
				this.completeMissionTimeMillis = obj.optLong(KEY_COMPLETE_TIME,
						0);
				this.lastRestCheckTime = obj.optLong(KEY_LAST_RESET_TIME, 0);
				this.totalStar = new AtomicInteger(
						obj.optInt(KEY_TOTAL_STAR, 0));
				this.buyCount = obj.optInt(KEY_BUY_COUNT, 0);
				this.reflashType = obj.optByte(KEY_REFLASH_TYPE, KDailyMissionManager.MISSION_REFLASH_TYPE_NORMAL);
				if (obj.has(KEY_GET_PRICE_BOX_INFO)) {
					String priceInfo = obj.getString(KEY_GET_PRICE_BOX_INFO);
					JSONObject priceObj = new JSONObject(priceInfo);
					JSONArray array = priceObj.names();
					if (array != null) {
						for (int i = 0; i < array.length(); i++) {
							String boxIdKey = array.getString(i);
							byte boxId = Byte.parseByte(boxIdKey);
							boolean isGet = (priceObj.optInt(boxIdKey, 0) == 1);
							getPriceBoxMap.put(boxId, isGet);
						}
					}
				}
			} else {

				String[] infoStr = dbData.split(",");
				if (infoStr.length >= 7) {
					this.todayCompletedMissionCount = new AtomicInteger(
							Integer.parseInt(infoStr[0]));
					this.restFreeCompletedMissionCount = new AtomicInteger(
							Integer.parseInt(infoStr[1]));
					this.todayManualReflashCount = new AtomicInteger(
							Integer.parseInt(infoStr[2]));
					this.lastReflashTimeMillis = Long.parseLong(infoStr[3]);
					this.completeMissionTimeMillis = Long.parseLong(infoStr[4]);
					this.lastRestCheckTime = Long.parseLong(infoStr[5]);
					this.totalStar = new AtomicInteger(
							Integer.parseInt(infoStr[6]));
				} else {

					this.todayCompletedMissionCount = new AtomicInteger(0);
					this.restFreeCompletedMissionCount = new AtomicInteger(
							KDailyMissionManager.free_complete_mission_count);
					this.todayManualReflashCount = new AtomicInteger(0);
					this.lastReflashTimeMillis = 0;
					this.completeMissionTimeMillis = 0;
					this.lastRestCheckTime = System.currentTimeMillis();
					this.totalStar = new AtomicInteger(0);
				}
			}
		}

		public String encode() throws Exception {
			JSONObject obj = new JSONObject();
			obj.put(KEY_COMPLETE_COUNT, todayCompletedMissionCount.get());
			obj.put(KEY_REST_FREE_COMPLETE_COUNT,
					restFreeCompletedMissionCount.get());
			obj.put(KEY_MANUAL_REFLASH_COUNT, todayManualReflashCount.get());
			obj.put(KEY_LAST_REFLASH_TIME, lastReflashTimeMillis);
			obj.put(KEY_COMPLETE_TIME, completeMissionTimeMillis);
			obj.put(KEY_LAST_RESET_TIME, lastRestCheckTime);
			obj.put(KEY_TOTAL_STAR, totalStar.get());
			obj.put(KEY_REFLASH_TYPE, this.reflashType);

			JSONObject priceObj = new JSONObject();
			for (Byte boxId : getPriceBoxMap.keySet()) {
				int isGet = (getPriceBoxMap.get(boxId)) ? 1 : 0;
				priceObj.put(boxId + "", isGet);
			}
			obj.put(KEY_GET_PRICE_BOX_INFO, priceObj.toString());
			obj.put(KEY_BUY_COUNT, buyCount);
			return obj.toString();
		}

	}

}
