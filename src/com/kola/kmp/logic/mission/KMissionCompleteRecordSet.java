package com.kola.kmp.logic.mission;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.kola.kgame.cache.mission.impl.KAMissionCompleteRecordSet;
import com.kola.kgame.db.dataobject.DBMissionCompleteRecord;
import com.kola.kmp.logic.mission.guide.NoviceGuideRecord;
import com.kola.kmp.logic.other.KMissionCompleteRecordTypeEnum;
import com.kola.kmp.logic.other.KNoviceGuideStepEnum;

public class KMissionCompleteRecordSet extends KAMissionCompleteRecordSet<KMissionCompleteRecord> {

	// 已完成的任务模版容器
	private Map<Integer, KMissionTemplate> completedMissionTemplateMap = new LinkedHashMap<Integer, KMissionTemplate>();

	// 当前使用中的完成任务记录（包含在completedMissionList中）
	private KMissionCompleteRecord currentInUsedClosedMissionRecord;

	private NoviceGuideRecord noviceGuideRecord;

	protected KMissionCompleteRecordSet(long roleId, boolean isFirstNew) {
		super(roleId, isFirstNew);
	}

	@Override
	protected Map<Long, KMissionCompleteRecord> initMissionCompleteRecords(List<DBMissionCompleteRecord> dbdatas) {
		Map<Long, KMissionCompleteRecord> result = new HashMap<Long, KMissionCompleteRecord>();
		long lastRecordId = 0;
		for (DBMissionCompleteRecord dbdata : dbdatas) {
			KMissionCompleteRecord data = new KMissionCompleteRecord(this, dbdata);
			result.put(data.getId(), data);
			for (Integer missionTemplateId : data.getCompletedMissionTemplateIdList()) {
				KMissionTemplate template = KMissionModuleExtension.getManager().getMissionTemplate(missionTemplateId);
				if (template != null) {
					completedMissionTemplateMap.put(missionTemplateId, template);
				}
			}
			if (data.getId() > lastRecordId && data.getCompletedMissionTemplateIdList().size() < KMissionCompleteRecord.COMPLETED_MISSION_TEMPLATE_ID_MAX_COUNT) {
				lastRecordId = data.getId();
			}
		}
		if (!result.isEmpty() && lastRecordId > 0) {
			currentInUsedClosedMissionRecord = result.get(lastRecordId);
		}
		return result;
	}

	@Override
	protected void decodeCA(String ca) {
		noviceGuideRecord = new NoviceGuideRecord();
		noviceGuideRecord.decode(ca);
	}

	@Override
	protected String encodeCA() {
		return noviceGuideRecord.encode();
	}

	/**
	 * 向任务容器的已完成任务列表添加一个已完成的任务模版Id
	 * 
	 * @param missionTemplateId
	 */
	public void addCompletedMission(KMissionTemplate missionTemplate) {
		if (currentInUsedClosedMissionRecord != null && currentInUsedClosedMissionRecord.getCompletedMissionTemplateIdList().size() < KMissionCompleteRecord.COMPLETED_MISSION_TEMPLATE_ID_MAX_COUNT) {
			// 如果当前当前使用中的完成任务记录不为null，并且记录的任务模版ID未满，则
			// 添加这个已完成的任务模版Id
			currentInUsedClosedMissionRecord.getCompletedMissionTemplateIdList().add(missionTemplate.missionTemplateId);
			// 通知该KMission对数据对象更改数据状态为KGameDataStatus.STATUS_UPDATE
			currentInUsedClosedMissionRecord.notifyDB();
		} else {
			// 创建一个完成任务记录
			KMissionCompleteRecord record = new KMissionCompleteRecord(this, KMissionCompleteRecordTypeEnum.DB_TYPE_NORMAL.getDbType());
			this.addMissionCompleteRecord(record);
			// 设置完成任务记录列表并添加一个模版Id
			record.getCompletedMissionTemplateIdList().add(missionTemplate.missionTemplateId);
			this.currentInUsedClosedMissionRecord = record;
		}
		this.completedMissionTemplateMap.put(missionTemplate.missionTemplateId, missionTemplate);
	}

	public Map<Integer, KMissionTemplate> getCompletedMissionTemplateMap() {
		return completedMissionTemplateMap;
	}

	/**
	 * 根据任务模版Id查找该任务是否已经完成
	 * 
	 * @param missiontemplateId
	 * @return
	 */
	public boolean checkMissionIsCompleted(int missiontemplateId) {
		if (this.completedMissionTemplateMap.containsKey(missiontemplateId)) {
			return true;
		} else {
			return false;
		}
	}

	public NoviceGuideRecord getNoviceGuideRecord() {
		if (noviceGuideRecord == null) {
			noviceGuideRecord = new NoviceGuideRecord();
			notifyDB();
		}
		return noviceGuideRecord;
	}

	public void completeNoviceGuideStep(int stepId) {
		if (noviceGuideRecord == null) {
			noviceGuideRecord = new NoviceGuideRecord();
		}
		if (noviceGuideRecord.guideStep < stepId) {
			noviceGuideRecord.guideStep = stepId;
			notifyDB();
		}
	}

	public void finishNoviceGuide() {
		if (noviceGuideRecord == null) {
			noviceGuideRecord = new NoviceGuideRecord();
		}
		if (!noviceGuideRecord.isCompleteGuide) {
			noviceGuideRecord.isCompleteGuide = true;
			noviceGuideRecord.isCompleteFirstGuideBattle = true;
			if (noviceGuideRecord.guideStep < KNoviceGuideStepEnum.进入第一个主城.type) {
				noviceGuideRecord.guideStep = KNoviceGuideStepEnum.进入第一个主城.type;
			}
			notifyDB();
		}
	}

	public void finishFirstNoviceGuideBattle() {
		if (noviceGuideRecord.guideStep < KNoviceGuideStepEnum.结算界面.type) {
			noviceGuideRecord.guideStep = KNoviceGuideStepEnum.结算界面.type;
		}
		noviceGuideRecord.isCompleteFirstGuideBattle = true;
		notifyDB();
	}
}
