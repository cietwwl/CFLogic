package com.kola.kmp.logic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import com.kola.kmp.logic.activity.transport.KTransportManager;
import com.kola.kmp.logic.activity.worldboss.KWorldBossManager;
import com.kola.kmp.logic.mission.KMission;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.mission.KMissionSet;
import com.kola.kmp.logic.mission.KMissionSet.DailyMissionInfo;
import com.kola.kmp.logic.mission.KMissionSet.UpdateDailyMissionStruct;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KRepairDailyMissionCount implements RunTimeTask {

	@Override
	public String run(String args) {
		try {
			long roleId = Long.parseLong(args);
//			KRole role = KSupportFactory.getRoleModuleSupport().getRole(
//					1000002461l);
//			KMissionSet missionSet = KMissionModuleExtension
//					.getMissionSet(1000002461l);
			 KRole role =
			 KSupportFactory.getRoleModuleSupport().getRole(roleId);
			 KMissionSet missionSet =
			 KMissionModuleExtension.getMissionSet(roleId);
			if (missionSet != null && role != null) {
				DailyMissionInfo info = missionSet.getDailyMissionInfo();
				if (info != null) {
					int nowCount = info.getRestFreeCompletedMissionCount();
					Field field = info.getClass().getDeclaredField(
							"restFreeCompletedMissionCount");

					field.setAccessible(true);
					field.set(info, new AtomicInteger(nowCount + 5));
					field.setAccessible(false);
					if (role.isOnline()) {
						KMissionModuleExtension
								.getManager()
								.getDailyMissionManager()
								.completeOrDropMissionReflashNewDailyMission(
										role, new UpdateDailyMissionStruct());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "执行修复【日常任务】成功。";
	}

}
