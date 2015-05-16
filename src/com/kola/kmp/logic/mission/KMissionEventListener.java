package com.kola.kmp.logic.mission;

import com.kola.kmp.logic.role.KRole;


public interface KMissionEventListener {
	/**
	 * 通知任务接受
	 * @param role
	 * @param missionTemplate
	 */
	public void notifyMissionAccepted(KRole role, KMissionTemplate missionTemplate);
	
	
	/**
	 * 通知任务完成
	 * @param role
	 * @param missionTemplate
	 */
	public void notifyMissionCompleted(KRole role, KMissionTemplate missionTemplate);
	
	/**
	 * 通知任务条件达成
	 * @param role
	 * @param missionTemplate
	 */
	public void nofifyMissionConditionCompleted(KRole role, KMissionTemplate missionTemplate);
}
