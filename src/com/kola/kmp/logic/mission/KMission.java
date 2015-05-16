package com.kola.kmp.logic.mission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.mission.impl.KAMission;
import com.kola.kgame.cache.mission.impl.KAMissionSet;
import com.kola.kgame.db.dataobject.DBMissionData;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.mission.MissionCompleteCondition.AnswerQuestionTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.CollectItemTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.GameLevelTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.KillMonsterTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.UpgradeFunLvTask;
import com.kola.kmp.logic.mission.MissionCompleteCondition.UseFunctionTask;
import com.kola.kmp.logic.mission.daily.KDailyMissionManager.DailyMissionQualityType;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGameMissionDBTypeEnum;
import com.kola.kmp.logic.other.KGameMissionFunTypeEnum;
import com.kola.kmp.logic.other.KGameMissionStatusEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.ItemModuleSupport;
import com.kola.kmp.logic.support.KSupportFactory;

public class KMission extends KAMission {

	private static final int VERSION_FIRST = 20130108;
	private static final int CURRENT_VERSION = VERSION_FIRST;

	private static final String KEY_VERSION = "0";// 自定义属性KEY：版本
	// private static final String KEY_MISSION_TEMPLATE_ID = "1";//
	// 自定义属性KEY：任务模版ID
	private static final String KEY_MISSION_FUN_TYPE = "2";// 自定义属性KEY：任务目标类型
	private static final String KEY_MISSION_STATUS = "3";// 自定义属性KEY：任务状态
	private static final String KEY_COMPLETED_MISSION_TEMPLATEID_LIST = "4";// 自定义属性KEY：已完成的任务模版ID列表
	private static final String KEY_ACCEPT_MISSION_TIMEMILLIS = "5";// 自定义属性KEY：接受任务的时间（毫秒）
	private static final String KEY_KILL_MONSTER_ARRAY = "6";// 自定义属性KEY：杀怪类型任务的杀怪记录数组
	private static final String KEY_KILL_MONSTER_TEMPLATE_ID = "7";// 自定义属性KEY：收集道具类型任务的收集道具记录的怪物模版ID
	private static final String KEY_KILL_MONSTER_TEMPLATE_COUNT = "8";// 自定义属性KEY：收集道具类型任务的收集道具记录的击杀怪物数量
	private static final String KEY_COMPLETE_LEVEL_RECORD_ID = "9";// 自定义属性KEY：完成关卡类型任务的关卡ID
	private static final String KEY_COMPLETE_LEVEL_RECORD_COUNT = "10";// 自定义属性KEY：完成关卡类型任务的完成次数
	private static final String KEY_COMPLETE_USE_FUNCTION_ID = "11";// 自定义属性KEY：完成功能类型任务的功能ID
	private static final String KEY_COMPLETE_USE_FUNCTION_COUNT = "12";// 自定义属性KEY：完成功能类型任务的完成次数
	private static final String KEY_COMPLETE_ANSWER_QUESTION_COUNT = "13";// 自定义属性KEY：完成答题类型任务的完成次数

	private static final String KEY_DAILY_MISSION_QUALITY = "14";// 自定义属性KEY：日常任务品质
	private static final String KEY_DAILY_MISSION_REWARD_ROLE_LV = "15";// 自定义属性KEY：日常任务品质

	// 任务数据DB保存类型
	private KGameMissionDBTypeEnum missionDbType;
	// 任务模版（当KGameMissionDBTypeEnum为MISSION_DB_TYPE_EMPTY或MISSION_DB_TYPE_COMPLETED时，该值为null）
	private KMissionTemplate missionTemplate;
	// 任务状态（当KGameMissionDBTypeEnum为MISSION_DB_TYPE_EMPTY或MISSION_DB_TYPE_COMPLETED时，该值为null）
	private KGameMissionStatusEnum missionStatus;

	// 杀怪任务数据记录表,Key:怪物模版Id
	private Map<Integer, KillMonsterRecord> killMonsterRecordMap;
	// 完成关卡次数记录
	private CompletedGameLevelRecord completedGameLevelRecord;
	// 完成功能次数记录
	private UseFunctionRecord useFunctionRecord;
	// 完成答题次数记录
	private AnswerQuestionRecord answerQuestionRecord;

	// 接受任务的时间
	private long missionAcceptedTimeMillis;
	// 日常任务品质
	public DailyMissionQualityType qualityType;
	// 日常任务奖励
	public BaseRewardData dailyMissionReward;
	// 日常任务奖励对应的角色等距
	private int dailyMissionRewardRoleLv;

	public KMission(KAMissionSet owner, DBMissionData dbdata) {
		super(owner, dbdata);
	}

	public KMission(KAMissionSet owner, int type, int templateId) {
		super(owner, type, templateId);
		setMissionDbType(KGameMissionDBTypeEnum.getEnum(type));
	}

	@Override
	protected void decodeCA(String attribute) {
		if (attribute == null || attribute.equals("")) {
			return;
		}

		try {
			JSONObject json = new JSONObject(attribute);
			int version = json.getInt(KEY_VERSION);
			switch (version) {
			case VERSION_FIRST:
				decodeV1(json);
				break;

			default:
				break;
			}

		} catch (JSONException ex) {
			_LOGGER.error("任务模块KMisson decodeAttribute出现异常！此时json的字符串是："
					+ attribute, ex);
		}
	}

	/**
	 * decode任务自定义属性，V1版本
	 * 
	 * @param jObj
	 * @throws JSONException
	 */
	private void decodeV1(JSONObject jObj) throws JSONException {
		setMissionDbType(KGameMissionDBTypeEnum.getEnum(this._missionDataType));
		// 处理任务数据DB保存类型为MISSION_DB_TYPE_CLOSED的情况
		if (this._missionDataType == KGameMissionDBTypeEnum.MISSION_DB_TYPE_UNCLOSED
				.getMissionDbType()) {
			try {
				// 处理任务数据DB保存类型为MISSION_DB_TYPE_CLOSED的情况
				setMissionTemplate(KMissionModuleExtension.getManager()
						.getMissionTemplate(this._templateId));
				this.missionStatus = KGameMissionStatusEnum.getEnum(jObj
						.getByte(KEY_MISSION_STATUS));
				this.missionAcceptedTimeMillis = jObj
						.getLong(KEY_ACCEPT_MISSION_TIMEMILLIS);
				_LOGGER.debug("-=-=-=-=-=-=:::: mission decodeV1():missionTemplateId:"
						+ missionTemplate.missionTemplateId
						+ "  missionStatus:" + missionStatus);
				if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_KILL_MONSTER) {
					JSONArray array = jObj.getJSONArray(KEY_KILL_MONSTER_ARRAY);
					if (array != null && array.length() > 0) {
						this.killMonsterRecordMap = new HashMap<Integer, KMission.KillMonsterRecord>();
						for (int i = 0; i < array.length(); i++) {
							JSONObject recordJson = array.getJSONObject(i);
							if (recordJson != null) {
								int monsterTemplateId = recordJson
										.optInt(KEY_KILL_MONSTER_TEMPLATE_ID);
								int killCount = recordJson
										.getInt(KEY_KILL_MONSTER_TEMPLATE_COUNT);
								if (KSupportFactory.getNpcModuleSupport()
										.getMonstTemplate(monsterTemplateId) != null) {
									if (missionTemplate.missionCompleteCondition
											.getKillMonsterTaskMap()
											.containsKey(monsterTemplateId)) {
										KillMonsterRecord record = new KillMonsterRecord(
												monsterTemplateId, killCount);
										this.killMonsterRecordMap.put(
												monsterTemplateId, record);
									}
								}
							}
						}
					}
				} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL) {
					int levelId = jObj.optInt(KEY_COMPLETE_LEVEL_RECORD_ID,
							this.missionTemplate.missionCompleteCondition
									.getGameLevelTask().levelId);
					int completeCount = jObj.optInt(
							KEY_COMPLETE_LEVEL_RECORD_COUNT, 0);
					this.completedGameLevelRecord = new CompletedGameLevelRecord(
							levelId, completeCount);
				} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION) {
					short funId = jObj.optShort(KEY_COMPLETE_USE_FUNCTION_ID,
							this.missionTemplate.missionCompleteCondition
									.getUseFunctionTask().functionId);

					int completeCount = jObj.optInt(
							KEY_COMPLETE_USE_FUNCTION_COUNT, 0);
					this.useFunctionRecord = new UseFunctionRecord(funId,
							completeCount);

				} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
					int answerCount = jObj.optInt(
							KEY_COMPLETE_ANSWER_QUESTION_COUNT, 0);
					this.answerQuestionRecord = new AnswerQuestionRecord(
							answerCount);
				}
			} catch (Exception e) {
				_LOGGER.error("读取数据库加载任务数据出错，任务模版ID：" + this._templateId
						+ ",角色ID：" + _roleId, e);
			}

		} else if (this._missionDataType == KGameMissionDBTypeEnum.MISSION_DB_TYPE_EMPTY
				.getMissionDbType()) {
			// 暂时没有东西处理
			return;
		} else if (this._missionDataType == KGameMissionDBTypeEnum.MISSION_DB_TYPE_DAILY
				.getMissionDbType()) {

			try {
				// 处理任务数据DB保存类型为MISSION_DB_TYPE_CLOSED的情况

				setMissionTemplate(KMissionModuleExtension.getManager()
						.getDailyMissionManager()
						.getMissionTemplate(this._templateId));
				this.missionStatus = KGameMissionStatusEnum.getEnum(jObj
						.getByte(KEY_MISSION_STATUS));
				this.missionAcceptedTimeMillis = jObj
						.getLong(KEY_ACCEPT_MISSION_TIMEMILLIS);
				_LOGGER.debug("-=-=-=-=-=-=:::: mission decodeV1():missionTemplateId:"
						+ missionTemplate.missionTemplateId
						+ "  missionStatus:" + missionStatus);
				int quality = jObj.optInt(KEY_DAILY_MISSION_QUALITY, 1);
				this.dailyMissionRewardRoleLv = jObj.optInt(
						KEY_DAILY_MISSION_REWARD_ROLE_LV, 1);
				this.qualityType = KMissionModuleExtension.getManager()
						.getDailyMissionManager().qualityTypeMap.get(quality);
				this.dailyMissionReward = KMissionModuleExtension
						.getManager()
						.getDailyMissionManager()
						.initDailyMissionReward(dailyMissionRewardRoleLv,
								qualityType);
				if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_KILL_MONSTER) {
					JSONArray array = jObj.getJSONArray(KEY_KILL_MONSTER_ARRAY);
					if (array != null && array.length() > 0) {
						this.killMonsterRecordMap = new HashMap<Integer, KMission.KillMonsterRecord>();
						for (int i = 0; i < array.length(); i++) {
							JSONObject recordJson = array.getJSONObject(i);
							if (recordJson != null) {
								int monsterTemplateId = recordJson
										.optInt(KEY_KILL_MONSTER_TEMPLATE_ID);
								int killCount = recordJson
										.getInt(KEY_KILL_MONSTER_TEMPLATE_COUNT);
								KillMonsterRecord record = new KillMonsterRecord(
										monsterTemplateId, killCount);
								this.killMonsterRecordMap.put(
										monsterTemplateId, record);
							}
						}
					}
				} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL) {
					int levelId = jObj.optInt(KEY_COMPLETE_LEVEL_RECORD_ID,
							this.missionTemplate.missionCompleteCondition
									.getGameLevelTask().levelId);
					int completeCount = jObj.optInt(
							KEY_COMPLETE_LEVEL_RECORD_COUNT, 0);
					this.completedGameLevelRecord = new CompletedGameLevelRecord(
							levelId, completeCount);
				} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION) {
					short funId = jObj.optShort(KEY_COMPLETE_USE_FUNCTION_ID,
							this.missionTemplate.missionCompleteCondition
									.getUseFunctionTask().functionId);
					int completeCount = jObj.optInt(
							KEY_COMPLETE_USE_FUNCTION_COUNT, 0);
					this.useFunctionRecord = new UseFunctionRecord(funId,
							completeCount);

				} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_ITEM) {

				} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
					int answerCount = jObj.optInt(
							KEY_COMPLETE_ANSWER_QUESTION_COUNT, 0);
					this.answerQuestionRecord = new AnswerQuestionRecord(
							answerCount);
				}
			} catch (Exception e) {
				_LOGGER.error("读取数据库加载任务数据出错，任务模版ID：" + this._templateId
						+ ",角色ID：" + _roleId, e);
			}
		}
	}

	@Override
	protected String encodeCA() {
		return encodeDBMissionAttribute();
	}

	/**
	 * encode任务自定义属性
	 * 
	 * @return
	 */
	private String encodeDBMissionAttribute() {
		String attribute = null;

		if (this.missionDbType == KGameMissionDBTypeEnum.MISSION_DB_TYPE_UNCLOSED) {
			JSONObject json = new JSONObject();
			try {
				json.put(KEY_VERSION, CURRENT_VERSION);
				json.put(KEY_MISSION_FUN_TYPE,
						this.missionTemplate.missionFunType.missionFunType);
				json.put(KEY_MISSION_STATUS, this.missionStatus.statusType);
				json.put(KEY_ACCEPT_MISSION_TIMEMILLIS,
						this.missionAcceptedTimeMillis);
				if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_KILL_MONSTER) {
					JSONArray array = new JSONArray();
					for (KillMonsterRecord record : this.killMonsterRecordMap
							.values()) {
						JSONObject recordJson = new JSONObject();
						recordJson.put(KEY_KILL_MONSTER_TEMPLATE_ID,
								record.monsterTemplateId);
						recordJson.put(KEY_KILL_MONSTER_TEMPLATE_COUNT,
								record.killCount);
						array.put(recordJson);
					}
					json.put(KEY_KILL_MONSTER_ARRAY, array);
				} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL) {
					json.put(KEY_COMPLETE_LEVEL_RECORD_ID,
							completedGameLevelRecord.levelId);
					json.put(KEY_COMPLETE_LEVEL_RECORD_COUNT,
							completedGameLevelRecord.completeCount);
				} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL) {
					json.put(KEY_COMPLETE_LEVEL_RECORD_ID,
							completedGameLevelRecord.levelId);
					json.put(KEY_COMPLETE_LEVEL_RECORD_COUNT,
							completedGameLevelRecord.completeCount);
				} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_ITEM) {

				} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION) {
					json.put(KEY_COMPLETE_USE_FUNCTION_ID,
							useFunctionRecord.functionId);
					json.put(KEY_COMPLETE_USE_FUNCTION_COUNT,
							useFunctionRecord.completeCount);
				} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
					json.put(KEY_COMPLETE_ANSWER_QUESTION_COUNT,
							answerQuestionRecord.completeCount);
				}

			} catch (JSONException ex) {
				_LOGGER.error("任务模块KMisson encodeAttribute出现异常！此时json的字符串是："
						+ json.toString(), ex);
			}
			return json.toString();
		} else if (this.missionDbType == KGameMissionDBTypeEnum.MISSION_DB_TYPE_EMPTY) {
			JSONObject json = new JSONObject();
			try {
				json.put(KEY_VERSION, CURRENT_VERSION);

			} catch (JSONException ex) {
				_LOGGER.error("任务模块KMisson encodeAttribute出现异常！此时json的字符串是："
						+ json.toString(), ex);
			}
			return json.toString();
		} else if (this.missionDbType == KGameMissionDBTypeEnum.MISSION_DB_TYPE_DAILY) {
			JSONObject json = new JSONObject();
			try {
				json.put(KEY_VERSION, CURRENT_VERSION);
				json.put(KEY_MISSION_FUN_TYPE,
						this.missionTemplate.missionFunType.missionFunType);
				json.put(KEY_MISSION_STATUS, this.missionStatus.statusType);
				json.put(KEY_ACCEPT_MISSION_TIMEMILLIS,
						this.missionAcceptedTimeMillis);
				json.put(KEY_DAILY_MISSION_QUALITY,
						(this.qualityType != null) ? this.qualityType.star : 1);
				json.put(KEY_DAILY_MISSION_REWARD_ROLE_LV,
						this.dailyMissionRewardRoleLv);
				if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_KILL_MONSTER) {
					JSONArray array = new JSONArray();
					for (KillMonsterRecord record : this.killMonsterRecordMap
							.values()) {
						JSONObject recordJson = new JSONObject();
						recordJson.put(KEY_KILL_MONSTER_TEMPLATE_ID,
								record.monsterTemplateId);
						recordJson.put(KEY_KILL_MONSTER_TEMPLATE_COUNT,
								record.killCount);
						array.put(recordJson);
					}
					json.put(KEY_KILL_MONSTER_ARRAY, array);
				} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL) {
					json.put(KEY_COMPLETE_LEVEL_RECORD_ID,
							completedGameLevelRecord.levelId);
					json.put(KEY_COMPLETE_LEVEL_RECORD_COUNT,
							completedGameLevelRecord.completeCount);
				} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_ITEM) {

				} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION) {
					json.put(KEY_COMPLETE_USE_FUNCTION_ID,
							useFunctionRecord.functionId);
					json.put(KEY_COMPLETE_USE_FUNCTION_COUNT,
							useFunctionRecord.completeCount);
				} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
					json.put(KEY_COMPLETE_ANSWER_QUESTION_COUNT,
							answerQuestionRecord.completeCount);
				}

			} catch (JSONException ex) {
				_LOGGER.error("任务模块KMisson encodeAttribute出现异常！此时json的字符串是："
						+ json.toString(), ex);
			}
			return json.toString();
		}
		return "";
	}

	/**
	 * 获取任务数据库保存类型
	 * 
	 * @return {@link KGameMissionDBTypeEnum#MISSION_DB_TYPE_EMPTY}||
	 *         {@link KGameMissionDBTypeEnum#MISSION_DB_TYPE_CLOSED}||
	 *         {@link KGameMissionDBTypeEnum#MISSION_DB_TYPE_UNCLOSED}||
	 *         {@link KGameMissionDBTypeEnum#MISSION_DB_TYPE_DAILY}
	 */
	public KGameMissionDBTypeEnum getMissionDbType() {
		return missionDbType;
	}

	/**
	 * <pre>
	 * 获取任务对应的任务模版（当KGameMissionDBTypeEnum为MISSION_DB_TYPE_EMPTY
	 * 或MISSION_DB_TYPE_COMPLETED时，该值为null）
	 * @return
	 * </pre>
	 */
	public KMissionTemplate getMissionTemplate() {
		return missionTemplate;
	}

	public void setMissionTemplate(KMissionTemplate template) {
		this.missionTemplate = template;
		this._templateId = template.missionTemplateId;
	}

	/**
	 * <pre>
	 * 获取当前任务的状态，（当KGameMissionDBTypeEnum为MISSION_DB_TYPE_EMPTY
	 * 或MISSION_DB_TYPE_COMPLETED时，该值为null）
	 * @return {@link KGameMissionStatusEnum#MISSION_STATUS_CANTRECEIVE}||
	 *         {@link KGameMissionStatusEnum#MISSION_STATUS_TRYFINISH}||
	 *         {@link KGameMissionStatusEnum#MISSION_STATUS_TRYRECEIVE}||
	 *         {@link KGameMissionStatusEnum#MISSION_STATUS_TRYSUBMIT}
	 * </pre>
	 */
	public KGameMissionStatusEnum getMissionStatus() {
		return missionStatus;
	}

	/**
	 * 设置任务数据库保存类型
	 * 
	 * @param missionDbType
	 */
	public void setMissionDbType(KGameMissionDBTypeEnum missionDbType) {
		this.missionDbType = missionDbType;
		this._missionDataType = missionDbType.getMissionDbType();
	}

	/**
	 * 设置当前任务的状态
	 * 
	 * @param missionStatus
	 */
	public void setMissionStatus(KGameMissionStatusEnum missionStatus) {
		this.missionStatus = missionStatus;
	}

	// /**
	// * 设置任务模版
	// * @param missionTemplate
	// */
	// public void setMissionTemplate(KMissionTemplate missionTemplate) {
	// this.missionTemplate = missionTemplate;
	// }

	// /**
	// * 获取杀怪类型任务的杀怪记录表
	// * @return
	// */
	// public Map<Integer, KillMonsterRecord> getKillMonsterRecordMap() {
	// return killMonsterRecordMap;
	// }
	//
	// /**
	// * 获取关卡类型任务的关卡记录
	// * @return
	// */
	// public CompletedGameLevelRecord getCompletedGameLevelRecord() {
	// return completedGameLevelRecord;
	// }

	public Map<Integer, KillMonsterRecord> getKillMonsterRecordMap() {
		return killMonsterRecordMap;
	}

	public UseFunctionRecord getUseFunctionRecord() {
		return useFunctionRecord;
	}

	public CompletedGameLevelRecord getCompletedGameLevelRecord() {
		return completedGameLevelRecord;
	}

	public AnswerQuestionRecord getAnswerQuestionRecord() {
		return answerQuestionRecord;
	}

	/**
	 * 通知任务杀怪的怪物模版ID以及数量，更新杀怪记录表
	 * 
	 * @param monsterTemplateId
	 * @param killCount
	 * @return 当任务杀怪记录存在此怪物模版ID并更新杀怪数量时，返回true。否则返回false
	 */
	public boolean notifyKillMonsterMission(KMonstTemplate monsterTemplate,
			int killCount) {
		if (this.missionStatus == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
			if (this.killMonsterRecordMap.containsKey(monsterTemplate.id)) {
				this.killMonsterRecordMap.get(monsterTemplate.id).killCount += killCount;
				notifyDB();
				return true;
			} else if (this.killMonsterRecordMap
					.containsKey(KillMonsterTask.ANY_TYPE_MONSTER_ID)
					&& this.missionTemplate.missionCompleteCondition
							.getKillMonsterTaskMap().containsKey(
									KillMonsterTask.ANY_TYPE_MONSTER_ID)) {
				KillMonsterTask task = this.missionTemplate.missionCompleteCondition
						.getKillMonsterTaskMap().get(
								KillMonsterTask.ANY_TYPE_MONSTER_ID);
				if (task.isMonsterLevelLimit) {
					if (monsterTemplate.lvl >= task.monsterLevel) {
						this.killMonsterRecordMap
								.get(KillMonsterTask.ANY_TYPE_MONSTER_ID).killCount += killCount;
						notifyDB();
						return true;
					}
				} else {
					this.killMonsterRecordMap
							.get(KillMonsterTask.ANY_TYPE_MONSTER_ID).killCount += killCount;
					notifyDB();
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 通知任务关卡完成，并更新任务关卡完成次数记录
	 * 
	 * @param levelId
	 * @return
	 */
	public boolean notifyGameLevelMission(int levelId) {
		if (this.missionStatus == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
			if (this.completedGameLevelRecord != null) {
				if (this.completedGameLevelRecord.levelId == levelId
						|| this.completedGameLevelRecord.levelId == GameLevelTask.ANY_TYPE_LEVEL) {
					this.completedGameLevelRecord.completeCount++;
					notifyDB();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 通知任务回到问题，并更新任务答题完成次数记录
	 * 
	 * @return
	 */
	public boolean notifyAnswerQuestionMission() {
		if (this.missionStatus == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
			if (this.answerQuestionRecord != null) {
				this.answerQuestionRecord.completeCount++;
				notifyDB();
				return true;
			}
		}
		return false;
	}

	/**
	 * <pre>
	 * 响应任务完成时的处理,并将任务数据清空，把DB保存类型标志位设置为:
	 * {@link KGameMissionDBTypeEnum#MISSION_DB_TYPE_EMPTY}
	 * </pre>
	 */
	public void notifyMissionCompletedAndSetEmptyData() {
		setMissionDbType(KGameMissionDBTypeEnum.MISSION_DB_TYPE_EMPTY);
		this.missionStatus = null;
		this.missionTemplate = null;
		this.useFunctionRecord = null;
		this.completedGameLevelRecord = null;
		if (this.killMonsterRecordMap != null) {
			killMonsterRecordMap.clear();
		}
		this._templateId = 0;
		this.notifyDB();
	}

	/**
	 * <pre>
	 * 清空日常任务数据
	 * </pre>
	 */
	public void clearDailyMissionData() {
		this.missionStatus = null;
		this.missionTemplate = null;
		this.useFunctionRecord = null;
		this.completedGameLevelRecord = null;
		if (this.killMonsterRecordMap != null) {
			killMonsterRecordMap.clear();
		}
		this._templateId = 0;
		this.qualityType = null;
		this.dailyMissionReward = null;
		this.dailyMissionRewardRoleLv = 0;
	}

	/**
	 * 通知使用道具类型任务该道具已完成使用操作，并更新任务状态
	 * 
	 * @param itemTemplate
	 *            使用道具对应的道具模版
	 * @return
	 */
	public boolean notifyUseItemMissionAndChangeMissionStatus(
			KItemTempAbs itemTemplate) {
		if (this.missionStatus == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
			if (this.missionTemplate.missionCompleteCondition.getUseItemTask().itemTemplate.itemCode == itemTemplate.itemCode) {
				if (this.missionStatus == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
					this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT;
					notifyDB();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 通知使用功能类型任务该功能已完成使用操作，并更新任务状态
	 * 
	 * @param functionId
	 *            使用功能对应的功能ID
	 * @return
	 */
	public boolean notifyUseFunctionMissionAndChangeMissionStatus(
			short functionId, int useCount) {
		if (this.missionStatus == KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH) {
			if (this.useFunctionRecord != null
					&& this.missionTemplate.missionCompleteCondition
							.getUseFunctionTask().functionId == functionId
					&& this.useFunctionRecord.functionId == functionId) {
				if (useFunctionRecord.completeCount < this.missionTemplate.missionCompleteCondition
						.getUseFunctionTask().useCount) {
					useFunctionRecord.completeCount += useCount;
					if (useFunctionRecord.completeCount > this.missionTemplate.missionCompleteCondition
							.getUseFunctionTask().useCount) {
						useFunctionRecord.completeCount = this.missionTemplate.missionCompleteCondition
								.getUseFunctionTask().useCount;
					}
					notifyDB();
				}

				if (useFunctionRecord.completeCount >= this.missionTemplate.missionCompleteCondition
						.getUseFunctionTask().useCount) {
					this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT;
					notifyDB();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * <pre>
	 * 检测该任务是否可完成提交。
	 * 如果当任务状态为{@link KGameMissionStatusEnum#MISSION_STATUS_TRYSUBMIT}时，表示
	 * 任务完成并可提交，则返回true。
	 * 如果当任务状态为{@link KGameMissionStatusEnum#MISSION_STATUS_TRYFINISH}时，表示
	 * 任务状态未完成，则会检测当前任务是否达到完成条件，如果达到则将任务状态设置为{@link KGameMissionStatusEnum#MISSION_STATUS_TRYSUBMIT}，
	 * 并返回true。否则返回false
	 * @return
	 * </pre>
	 */
	public boolean checkMissionCanSubmitAndChangeMissionStatus(byte jobType) {

		if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG) {
			// 对话任务已达到完成条件，则更新任务状态
			this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT;
			notifyDB();
			return true;
		} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_KILL_MONSTER) {
			// 杀怪类型任务检测
			if (this.missionStatus == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
				return true;
			}
			if (this.missionTemplate.missionCompleteCondition
					.getKillMonsterTaskMap() != null
					&& this.killMonsterRecordMap != null) {

				for (KillMonsterTask task : this.missionTemplate.missionCompleteCondition
						.getKillMonsterTaskMap().values()) {
					KillMonsterRecord record = this.killMonsterRecordMap
							.get(task.isAnyTypeMonster ? task.ANY_TYPE_MONSTER_ID
									: task.monsterTemplate.id);
					if (record.killCount < task.killCount) {
						return false;
					}
				}
				// 这里表示检测杀怪任务已达到完成条件，则更新任务状态
				this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT;
				notifyDB();
				return true;
			}

		} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_COLLECT_ITEMS) {
			// 收集道具类型任务检测
			if (this.missionTemplate.missionCompleteCondition
					.getCollectItemTask() != null) {
				ItemModuleSupport support = KSupportFactory
						.getItemModuleSupport();
				CollectItemTask task = this.missionTemplate.missionCompleteCondition
						.getCollectItemTask();
				String itemCode;
				if (task.isLimitJob) {
					itemCode = task.itemTemplateMap.get(jobType).itemCode;
				} else {
					itemCode = task.itemTemplate.itemCode;
				}

				if (support.checkItemCountInBag(_roleId, itemCode) < task.collectCount) {
					if (this.missionStatus == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
						// 如果道具数量不足，但是任务状态却为MISSION_STATUS_TRYSUBMIT，则修改为MISSION_STATUS_TRYFINISH
						this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH;
						notifyDB();
					}
					return false;
				}

				// 这里表示检测收集道具任务已达到完成条件，则更新任务状态
				if (this.missionStatus != KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
					this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT;
					notifyDB();
				}
				return true;
			}

		} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL) {
			if (this.missionStatus == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
				return true;
			}
			GameLevelTask task = this.missionTemplate.missionCompleteCondition
					.getGameLevelTask();

			if (this.completedGameLevelRecord.completeCount >= task.completeCount) {
				// 关卡任务已达到完成条件，则更新任务状态
				this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT;
				notifyDB();
				return true;
			}

		} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_ITEM) {
			if (this.missionStatus == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
				return true;
			}

		} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION) {
			if (this.missionStatus == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
				return true;
			}
			if (this.useFunctionRecord != null) {
				if (this.useFunctionRecord.completeCount >= this.missionTemplate.missionCompleteCondition
						.getUseFunctionTask().useCount) {
					// 使用功能任务已达到完成条件，则更新任务状态
					this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT;
					notifyDB();
					return true;
				}
			}

		} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_ATTRIBUTE_DATA) {
			if (this.missionStatus == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
				return true;
			}

		} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_BATTLEFIELD) {
			if (this.missionStatus == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
				return true;
			} else {
				this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT;
				notifyDB();
				return true;
			}
		} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
			if (this.missionStatus == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
				return true;
			}
			AnswerQuestionTask task = this.missionTemplate.missionCompleteCondition
					.getAnswerQuestionTask();

			if (this.answerQuestionRecord.completeCount >= task
					.getTotalQuestionCount()) {
				// 关卡任务已达到完成条件，则更新任务状态
				this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT;
				notifyDB();
				return true;
			}
		} else if (this.missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_UP_FUNC_LV) {
			if (this.missionStatus == KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT) {
				return false;
			} else {
				// 对话任务已达到完成条件，则更新任务状态
				this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT;
				notifyDB();
				return true;
			}
		}
		return false;

	}

	/**
	 * 根据任务模版初始化新接受任务数据
	 * 
	 * @param missionTemplate
	 */
	public void initNowAcceptedMission(KMissionTemplate missionTemplate,byte jobType) {
		setMissionTemplate(missionTemplate);
		this.missionAcceptedTimeMillis = System.currentTimeMillis();
		if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG) {
			// 如果是对话任务，只需要设置任务状态为MISSION_STATUS_TRYSUBMIT
			this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT;
		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_KILL_MONSTER) {
			// 杀怪任务，则设置任务状态为MISSION_STATUS_TRYFINISH
			this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH;
			// 初始化杀怪记录数据表
			int monsterTypeSize = missionTemplate.missionCompleteCondition
					.getKillMonsterTaskMap().size();
			killMonsterRecordMap = new HashMap<Integer, KMission.KillMonsterRecord>();
			for (KillMonsterTask task : missionTemplate.missionCompleteCondition
					.getKillMonsterTaskMap().values()) {
				int monsterTemplateId = task.isAnyTypeMonster ? task.ANY_TYPE_MONSTER_ID
						: task.monsterTemplate.id;
				KillMonsterRecord record = new KillMonsterRecord(
						monsterTemplateId, 0);
				killMonsterRecordMap.put(monsterTemplateId, record);
			}
		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_COLLECT_ITEMS) {
			// 收集道具任务，首先判断该任务是否达到完成条件（背包是否有足够目标道具）
			if (!this.checkMissionCanSubmitAndChangeMissionStatus(jobType)) {
				// 如果背包目标道具数量不足，设置任务状态为MISSION_STATUS_TRYFINISH
				this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH;
			}
		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL) {
			// 关卡任务，则设置任务状态为MISSION_STATUS_TRYFINISH
			this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH;
			GameLevelTask task = missionTemplate.missionCompleteCondition
					.getGameLevelTask();

			this.completedGameLevelRecord = new CompletedGameLevelRecord(
					task.levelId, 0);

		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_ITEM) {
			// 使用道具任务，则设置任务状态为MISSION_STATUS_TRYFINISH
			this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH;

		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION) {
			// 使用道具任务，则设置任务状态为MISSION_STATUS_TRYFINISH
			this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH;
			UseFunctionTask task = missionTemplate.missionCompleteCondition
					.getUseFunctionTask();
			this.useFunctionRecord = new UseFunctionRecord(task.functionId, 0);

		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_ATTRIBUTE_DATA) {
			// 角色数值任务，则设置任务状态为MISSION_STATUS_TRYFINISH
			this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH;

		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_BATTLEFIELD) {
			// 直接进入战场任务，则设置任务状态为MISSION_STATUS_TRYFINISH
			this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH;
		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {
			// 使用道具任务，则设置任务状态为MISSION_STATUS_TRYFINISH
			this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH;
			this.answerQuestionRecord = new AnswerQuestionRecord(0);
		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_UP_FUNC_LV) {
			UpgradeFunLvTask task = missionTemplate.missionCompleteCondition
					.getUpgradeFunLvTask();
			// if (task.functionId == FunctionTypeEnum.强化.functionId) {
			// KGameEquipmentTypeEnum equipType = null;
			// if (task.functionTarget != UpgradeFunLvTask.ANY_TARGET_TYPE) {
			// equipType = KGameEquipmentTypeEnum.getEnum(Byte
			// .parseByte(task.functionTarget));
			// }
			// if (KSupportFactory.getSkillSupport().checkEquiStrongLv(_roleId,
			// equipType) >= task.targetLv) {
			// this.missionStatus =
			// KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT;
			// } else {
			// this.missionStatus =
			// KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH;
			// }
			// } else if (task.functionId == FunctionTypeEnum.装备升级.functionId) {
			// KGameEquipmentTypeEnum equipType = null;
			// if (task.functionTarget != UpgradeFunLvTask.ANY_TARGET_TYPE) {
			// equipType = KGameEquipmentTypeEnum.getEnum(Byte
			// .parseByte(task.functionTarget));
			// }
			// if (KSupportFactory.getItemSupport().checkEquiStrongLv(_roleId,
			// equipType) >= task.targetLv) {
			// this.missionStatus =
			// KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT;
			// } else {
			// this.missionStatus =
			// KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH;
			// }
			// } else if (task.functionId == FunctionTypeEnum.进阶.functionId) {
			// KPetEnhanceType type = null;
			// if (task.functionTarget != UpgradeFunLvTask.ANY_TARGET_TYPE) {
			// type = KPetEnhanceType.getEnum(Byte
			// .parseByte(task.functionTarget));
			// }
			// if (KSupportFactory.getPetSupport().checkAllPetMaxEnhanceLevel(
			// _roleId, type) >= task.targetLv) {
			// this.missionStatus =
			// KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT;
			// } else {
			// this.missionStatus =
			// KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH;
			// }
			// } else if (task.functionId == FunctionTypeEnum.轮回.functionId) {
			// KPetEnhanceType type = null;
			// if (task.functionTarget != UpgradeFunLvTask.ANY_TARGET_TYPE) {
			// type = KPetEnhanceType.getEnum(Byte
			// .parseByte(task.functionTarget));
			// }
			// if (KSupportFactory.getPetSupport().checkAllPetMaxEnhanceGrade(
			// _roleId, type) >= task.targetLv) {
			// this.missionStatus =
			// KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT;
			// } else {
			// this.missionStatus =
			// KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH;
			// }
			// } else if (task.functionId == FunctionTypeEnum.角色技能.functionId) {
			// if (KSupportFactory.getSkillSupport().checkSkillsMaxLv(_roleId)
			// >= task.targetLv) {
			// this.missionStatus =
			// KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT;
			// } else {
			// this.missionStatus =
			// KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH;
			// }
			// } else if (task.functionId == FunctionTypeEnum.队伍.functionId) {
			// if
			// (KSupportFactory.getRoleSupport().checkTeamAllPosMaxLevel(_roleId)
			// >= task.targetLv) {
			// this.missionStatus =
			// KGameMissionStatusEnum.MISSION_STATUS_TRYSUBMIT;
			// } else {
			// this.missionStatus =
			// KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH;
			// }
			// }
		}
	}

	/**
	 * 初始化日常任务
	 */
	public void initDailyMission(int roleLv, KMissionTemplate missionTemplate,
			DailyMissionQualityType qualityType,byte jobType) {
		setMissionTemplate(missionTemplate);
		this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH;
		if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_DIALOG) {

		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_KILL_MONSTER) {
			// 初始化杀怪记录数据表
			int monsterTypeSize = missionTemplate.missionCompleteCondition
					.getKillMonsterTaskMap().size();
			killMonsterRecordMap = new HashMap<Integer, KMission.KillMonsterRecord>();
			for (KillMonsterTask task : missionTemplate.missionCompleteCondition
					.getKillMonsterTaskMap().values()) {
				int monsterTemplateId = task.isAnyTypeMonster ? task.ANY_TYPE_MONSTER_ID
						: task.monsterTemplate.id;
				KillMonsterRecord record = new KillMonsterRecord(
						monsterTemplateId, 0);
				killMonsterRecordMap.put(monsterTemplateId, record);
			}
		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_COLLECT_ITEMS) {
			// 收集道具任务，首先判断该任务是否达到完成条件（背包是否有足够目标道具）
			if (!this.checkMissionCanSubmitAndChangeMissionStatus(jobType)) {
				// 如果背包目标道具数量不足，设置任务状态为MISSION_STATUS_TRYFINISH
				this.missionStatus = KGameMissionStatusEnum.MISSION_STATUS_TRYFINISH;
			}
		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_GAME_LEVEL) {
			// 关卡任务
			GameLevelTask task = missionTemplate.missionCompleteCondition
					.getGameLevelTask();

			this.completedGameLevelRecord = new CompletedGameLevelRecord(
					task.levelId, 0);

		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_ITEM) {
			// 使用道具任务

		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_USE_FUNCTION) {
			// 使用道具任务
			UseFunctionTask task = missionTemplate.missionCompleteCondition
					.getUseFunctionTask();
			this.useFunctionRecord = new UseFunctionRecord(task.functionId, 0);

		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_ATTRIBUTE_DATA) {
			// 角色数值任务

		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_BATTLEFIELD) {
			// 直接进入战场任务
		} else if (missionTemplate.missionFunType == KGameMissionFunTypeEnum.MISSION_FUN_TYPE_QUESTION) {

		}

		this.qualityType = qualityType;
		this.dailyMissionReward = KMissionModuleExtension.getManager()
				.getDailyMissionManager()
				.initDailyMissionReward(roleLv, qualityType);
		this.dailyMissionRewardRoleLv = roleLv;
	}

	/**
	 * 杀怪类型任务记录数据
	 * 
	 * @author zhaizl
	 * 
	 */
	public static class KillMonsterRecord {
		/**
		 * 怪物npc模版ID
		 */
		public int monsterTemplateId;//
		/**
		 * 已击杀数量
		 */
		public int killCount;//

		public KillMonsterRecord(int monsterTemplateId, int killCount) {
			super();
			this.monsterTemplateId = monsterTemplateId;
			this.killCount = killCount;
		}

	}

	/**
	 * 完成关卡类型任务的完成关卡次数记录
	 * 
	 * @author Administrator
	 * 
	 */
	public static class CompletedGameLevelRecord {
		public int levelId;
		public int completeCount;

		public CompletedGameLevelRecord(int levelId, int completeCount) {
			super();
			this.levelId = levelId;
			this.completeCount = completeCount;
		}

	}

	/**
	 * 完成功能类型任务的完成功能次数记录
	 * 
	 * @author Administrator
	 * 
	 */
	public static class UseFunctionRecord {
		public short functionId;
		public int completeCount;

		public UseFunctionRecord(short functionId, int completeCount) {
			super();
			this.functionId = functionId;
			this.completeCount = completeCount;
		}

	}

	public static class AnswerQuestionRecord {
		public int completeCount;

		public AnswerQuestionRecord(int completeCount) {
			super();
			this.completeCount = completeCount;
		}

	}

}
