package com.kola.kmp.logic.mission;

import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KJobTypeEnum;

/**
 * 表示任务的触发条件
 * 
 * @author zhaizl
 * 
 */
public class MissionTriggerCondition {
	// 角色等级限制条件
	public int roleLevelLimit;
	// 前置任务条件的任务ID
	public int frontMissionTemplateId;
	// 数值触发条件
	public AttributeCondition attributeCondition;
	// 是否以数值触发条件
	public boolean isAttributeCondition;
	// 任务的角色职业条件类型
	public KJobTypeEnum occupationType;
	// 是否以角色职业作为限制条件
	public boolean isOccupationCondition;
	// 是否以角色加入帮会为条件
	public boolean isGangCondition;
	//接受任务次数限制
	public int acceptLimitCount;
	//任务开启时间限制
	public int openTimeLimitSeconds;
	//任务等级
	public int missionLevel;
	
	
	

	public MissionTriggerCondition() {
	}

	

	/**
	 * 任务数值触发条件
	 * 
	 * @author zhaizl
	 * 
	 */
	public static class AttributeCondition {
		// 数值触发条件类型
		public KGameAttrType attrType;
		// 达到数值触发条件的值
		public int attributeValue;

		public AttributeCondition(KGameAttrType attrType,
				int attributeValue) {
			this.attrType = attrType;
			this.attributeValue = attributeValue;
		}

	}

}
