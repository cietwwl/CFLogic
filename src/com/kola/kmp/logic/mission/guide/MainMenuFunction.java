package com.kola.kmp.logic.mission.guide;

public class MainMenuFunction {
	/**
	 * 功能开启方式类型为达到角色等级开放
	 */
	public final static byte FUNCTION_OPEN_TYPE_ROLE_LEVEL = 1;

	/**
	 * 功能开启方式类型为任务引导开放
	 */
	public final static byte FUNCTION_OPEN_TYPE_MISSION = 2;

	/**
	 * 表示功能引导类型为不进行引导
	 */
	public final static byte FUNCTION_GUIDE_TYPE_NONE = 0;
	/**
	 * 表示功能引导类型为通过用户第一次打开该功能面板进行引导
	 */
	public final static byte FUNCTION_GUIDE_TYPE_FISRT_OPEN = 1;
	/**
	 * 表示功能引导类型为任务引导
	 */
	public final static byte FUNCTION_GUIDE_TYPE_MISSION = 2;

	// 功能编号
	private short functionId;
	// 功能名称
	private String functionName;
	// 功能开放模式，参考FUNCTION_OPEN_TYPE_ROLE_LEVEL和FUNCTION_OPEN_TYPE_MISSION
	private byte functionOpenType;
	// 功能开放的角色等级限制
	private int openRoleLevelLimit;
	// 功能开放的完成任务模版Id限制
	private int openCompleteMissionTemplateId;
	// 功能引导类型
	private int guideType;
	// 功能图标资源Id
	private int iconResId;
	// 功能描述
	private String desc;
	// 功能开放时是否弹出ICON特效
	private boolean isOpenShowIcon;
	// 是否需要二次引导
	private boolean isSecondGuide;
	// 触发二次引导的角色等级
	private int secondGuideRoleLv;
	// 触发二次引导的完成任务模版ID
	private int secondGuideMissionTemplateId;
	// 二次引导需要完成功能的次数
	private int secondGuideCount;
	// 功能图标临时显示开关状态
	private boolean iconShowStatus;

	public MainMenuFunction(short functionId, String functionName, byte functionOpenType, int openRoleLevelLimit, int openCompleteMissionTemplateId, int guideType, int iconResId, String desc,
			boolean isOpenShowIcon, boolean isSecondGuide, int secondGuideRoleLv, int secondGuideMissionTemplateId, int secondGuideCount, boolean iconShowStatus) {
		this.functionId = functionId;
		this.functionName = functionName;
		this.functionOpenType = functionOpenType;
		this.openRoleLevelLimit = openRoleLevelLimit;
		this.openCompleteMissionTemplateId = openCompleteMissionTemplateId;
		this.iconResId = iconResId;
		this.guideType = guideType;
		this.desc = desc;
		this.isOpenShowIcon = isOpenShowIcon;
		this.isSecondGuide = isSecondGuide;
		this.secondGuideRoleLv = secondGuideRoleLv;
		this.secondGuideMissionTemplateId = secondGuideMissionTemplateId;
		this.secondGuideCount = secondGuideCount;
		this.iconShowStatus = iconShowStatus;
	}

	public short getFunctionId() {
		return functionId;
	}

	public String getFunctionName() {
		return functionName;
	}

	public byte getFunctionOpenType() {
		return functionOpenType;
	}

	public int getOpenRoleLevelLimit() {
		return openRoleLevelLimit;
	}

	public int getOpenCompleteMissionTemplateId() {
		return openCompleteMissionTemplateId;
	}

	public int getIconResId() {
		return iconResId;
	}

	public int getGuideType() {
		return guideType;
	}

	public String getDesc() {
		return desc;
	}

	public boolean isOpenShowIcon() {
		return isOpenShowIcon;
	}

	public boolean isSecondGuide() {
		return isSecondGuide;
	}

	public int getSecondGuideMissionTemplateId() {
		return secondGuideMissionTemplateId;
	}

	public int getSecondGuideCount() {
		return secondGuideCount;
	}

	public int getSecondGuideRoleLv() {
		return secondGuideRoleLv;
	}

	public boolean getIconShowStatus() {
		return iconShowStatus;
	}

	public void setIconShowStatus(boolean iconShowStatus) {
		this.iconShowStatus = iconShowStatus;
	}

}
