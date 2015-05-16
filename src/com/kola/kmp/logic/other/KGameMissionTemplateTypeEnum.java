package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

import com.kola.kmp.logic.other.KColorFunEnum.KColor;
import com.kola.kmp.logic.other.KColorFunEnum.KColorManager;
import com.kola.kmp.logic.util.text.HyperTextTool;

/**
 * 任务模版类型枚举，分类例如主线、支线、日常、帮会、活动任务等等
 * @author zhaizl
 *
 */
public enum KGameMissionTemplateTypeEnum {
	
	MISSION_TYPE_MAIN_LINE(1,5,"【主】",7),
	//
	MISSION_TYPE_BRANCH_LINE(2,4,"【支】",8),
	//
	MISSION_TYPE_DAILY(3,2,"【日常】",9),
	//
	MISSION_TYPE_GANG(4,3,"【帮】",9),
	//
	MISSION_TYPE_ACTIVITY(5,1,"【活动】",9),
	//
	MISSION_TYPE_NEW_GUIDE(6,6,"【新手引导】",9);
	
	public final byte missionType; // 数据类型的标识
	public final byte trackingSerial;//任务跟踪时的状态排序
	public final String typeName; // 数据类型的描述
	public final String extTypeName; // 数据类型的描述
	public final int color; // 数据类型的描述的颜色值
	
	private KGameMissionTemplateTypeEnum(int pDataType,int trackingSerial, String pDataDesc,int color) {
		this.missionType = (byte) pDataType;
		this.trackingSerial = (byte)trackingSerial;
		this.typeName = pDataDesc;
		this.color = color;
		this.extTypeName=HyperTextTool.extColor(typeName, color);
	}
	
	// 所有枚举
		private static final Map<Byte, KGameMissionTemplateTypeEnum> enumMap = new HashMap<Byte, KGameMissionTemplateTypeEnum>();
		static {
			KGameMissionTemplateTypeEnum[] enums = KGameMissionTemplateTypeEnum.values();
			KGameMissionTemplateTypeEnum type;
			for (int i = 0; i < enums.length; i++) {
				type = enums[i];
				enumMap.put(type.missionType, type);
			}
		}

		/**
		 * <pre>
		 * 通过标识数值获取任务类型枚举对象
		 * 
		 * @param type
		 * @return
		 * @creation 2012-12-3 下午3:53:28
		 * </pre>
		 */
		public static KGameMissionTemplateTypeEnum getEnum(byte missionType) {
			return enumMap.get(missionType);
		}

}
