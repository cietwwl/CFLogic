package com.kola.kmp.logic.level;

import java.util.ArrayList;
import java.util.List;

public class KEnterLevelCondition {
	// 进入关卡限制次数
	private int levelLimitJoinCount;
	// 免费进入限制次数
	private int freeLimitJoinCount;

	// 该关卡是否限制进入次数
	private boolean isLimitJoinCountLevel;
	// 进入该关卡消耗角色的体力值
	private int useStamina;
	// 该关卡的前置任务Id
	private int frontMissionTemplateId;
	// 该关卡的前置关卡Id
	private int frontLevelId;
	// 关卡开放的角色等级
	private int openRoleLevel;

	// 当进入次数已满时，再进入需要消耗元宝数量
	private int fullJoinCountUseGamePoint;
	// 当进入次数已满时,是否可以通过消耗元宝进入
	private boolean isCanUseGamePointWhileFullJoinCount = false;
	
	public List<Integer> resetUsePointList = new ArrayList<Integer>();

	public KEnterLevelCondition(int levelLimitJoinCount, int useStamina,
			int frontMissionTemplateId, int frontLevelId,int openRoleLevel) {
		this.levelLimitJoinCount = levelLimitJoinCount;
		this.useStamina = useStamina;
		this.frontMissionTemplateId = frontMissionTemplateId;
		this.frontLevelId = frontLevelId;
		if (levelLimitJoinCount > 0) {
			isLimitJoinCountLevel = true;
		} else {
			isLimitJoinCountLevel = false;
		}
		this.openRoleLevel = openRoleLevel;
	}

	/**
	 * 获取进入关卡限制次数
	 * 
	 * @return
	 */
	public int getLevelLimitJoinCount() {
		return levelLimitJoinCount;
	}

	/**
	 * 免费进入限制次数
	 * 
	 * @return
	 */
	public int getFreeLimitJoinCount() {
		return freeLimitJoinCount;
	}

	/**
	 * 设置免费进入限制次数
	 * 
	 * @param freeLimitJoinCount
	 */
	public void setFreeLimitJoinCount(int freeLimitJoinCount) {
		this.freeLimitJoinCount = freeLimitJoinCount;
	}

	/**
	 * 该关卡是否限制进入次数
	 * 
	 * @return
	 */
	public boolean isLimitJoinCountLevel() {
		return isLimitJoinCountLevel;
	}

	/**
	 * 获取进入该关卡消耗角色的体力值
	 * 
	 * @return
	 */
	public int getUseStamina() {
		return useStamina;
	}

	/**
	 * 该关卡的前置任务Id
	 * 
	 * @return
	 */
	public int getFrontMissionTemplateId() {
		return frontMissionTemplateId;
	}

	/**
	 * 该关卡的前置关卡Id
	 * 
	 * @return
	 */
	public int getFrontLevelId() {
		return frontLevelId;
	}

	/**
	 * 当进入次数已满时，再进入需要消耗元宝数量
	 * 
	 * @return
	 */
	public int getFullJoinCountUseGamePoint() {
		return fullJoinCountUseGamePoint;
	}

	/**
	 * 设置当进入次数已满时，再进入需要消耗元宝数量
	 * 
	 * @param fullJoinCountUseGamePoint
	 */
	public void setFullJoinCountUseGamePoint(int fullJoinCountUseGamePoint) {
		this.fullJoinCountUseGamePoint = fullJoinCountUseGamePoint;
	}

	/**
	 * 当进入次数已满时,是否可以通过消耗元宝进入
	 * 
	 * @return
	 */
	public boolean isCanUseGamePointWhileFullJoinCount() {
		return isCanUseGamePointWhileFullJoinCount;
	}

	/**
	 * 设置当进入次数已满时,是否可以通过消耗元宝进入
	 * 
	 * @param isUseGamePointWhileFullJoinCount
	 */
	public void setCanUseGamePointWhileFullJoinCount(
			boolean isUseGamePointWhileFullJoinCount) {
		this.isCanUseGamePointWhileFullJoinCount = isUseGamePointWhileFullJoinCount;
	}
	


	public int getOpenRoleLevel() {
		return openRoleLevel;
	}

	public void setOpenRoleLevel(short openRoleLevel) {
		this.openRoleLevel = openRoleLevel;
	}

	/**
	 * 重置关卡需要消耗钻石数量列表
	 * @return
	 */
	public List<Integer> getResetUsePointList() {
		return resetUsePointList;
	}
	
	
}
