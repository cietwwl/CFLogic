package com.kola.kmp.logic.mission;

import java.util.ArrayList;
import java.util.List;

import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.mission.impl.KAMissionCompleteRecord;
import com.kola.kgame.cache.mission.impl.KAMissionCompleteRecordSet;
import com.kola.kgame.db.dataobject.DBMissionCompleteRecord;

public class KMissionCompleteRecord extends KAMissionCompleteRecord {

	private static final int VERSION_FIRST = 20130108;
	private static final int CURRENT_VERSION = VERSION_FIRST;

	private static final String KEY_VERSION = "0";// 自定义属性KEY：版本
	private static final String KEY_COMPLETED_MISSION_TEMPLATEID_LIST = "4";// 自定义属性KEY：已完成的任务模版ID列表

	/**
	 * 完成任务记录的最大模版ID保存数量
	 */
	public final static int COMPLETED_MISSION_TEMPLATE_ID_MAX_COUNT = 100;
	// 已完成的任务模版ID列表（当KGameMissionDBTypeEnum不为MISSION_DB_TYPE_COMPLETED时，该值为null）
	private List<Integer> completedMissionTemplateIdList;

	protected KMissionCompleteRecord(KAMissionCompleteRecordSet owner,
			DBMissionCompleteRecord dbdata) {
		super(owner, dbdata);
	}

	public KMissionCompleteRecord(KAMissionCompleteRecordSet owner, int type) {
		super(owner, type);
		completedMissionTemplateIdList = new ArrayList<Integer>();
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

		JSONArray jArray = jObj
				.getJSONArray(KEY_COMPLETED_MISSION_TEMPLATEID_LIST);
		if (jArray != null && jArray.length() > 0) {
			this.completedMissionTemplateIdList = new ArrayList<Integer>();
			for (int i = 0; i < jArray.length(); i++) {
				int missionTemplateId = jArray.getInt(i);
				this.completedMissionTemplateIdList.add(missionTemplateId);
			}
		}

	}

	/**
	 * encode任务自定义属性
	 * 
	 * @return
	 */
	private String encodeDBMissionAttribute() {

			JSONObject json = new JSONObject();
			try {
				json.put(KEY_VERSION, CURRENT_VERSION);

				JSONArray array = new JSONArray();
				for (Integer missionTemplateId : completedMissionTemplateIdList) {
					array.put(missionTemplateId.intValue());
				}
				json.put(KEY_COMPLETED_MISSION_TEMPLATEID_LIST, array);

			} catch (JSONException ex) {
				_LOGGER.error("任务模块KMisson encodeAttribute出现异常！此时json的字符串是："
						+ json.toString(), ex);
			}
			return json.toString();
		
	}

	@Override
	protected String encodeCA() {
		return encodeDBMissionAttribute();
	}
	
	/**
	 * 获取已完成的任务模版ID列表
	 * 
	 * @return
	 */
	public List<Integer> getCompletedMissionTemplateIdList() {
		return completedMissionTemplateIdList;
	}

}
