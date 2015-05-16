package com.kola.kmp.logic.skill;

import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.message.KPushSkillsMsg;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2013-1-29 下午5:50:54
 * </pre>
 */
public class KSkillRoleEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
//		//清理非法技能
//		System.err.println("清理非法技能");
//		KSkillSet set = KSkillModuleExtension.getSkillSet(role.getId());
//		Map<Long, KSkill> iniSkills = set.getAllSkillsCache();
//		
//		for(KSkill skill:iniSkills.values().toArray(new KSkill[iniSkills.size()])){
//			if(skill.isInitiative()){
//				if(KSkillDataManager.mRoleIniSkillTempManager.getTemplate(skill._templateId)==null){
//					set.notifyElementDelete(skill._id);
//					System.err.println("清理技能="+skill._templateId);
//				}
//			} else {
//				if(KSkillDataManager.mRolePasSkillTempManager.getTemplate(skill._templateId)==null){
//					set.notifyElementDelete(skill._id);
//					System.err.println("清理技能="+skill._templateId);
//				}
//			}
//		}
		// 处理1.0.5超杀技能修改
		try {
			SuperSkillPatch.instance.doWork(role, false);
		} catch (Exception e) {
			KSkillLogic._LOGGER.error(StringUtil.format("{},{},{}", role.getId(), role.getName(), "超杀技能处理"), e);
		}
				
		KPushSkillsMsg.pushAllSkills(role);
		KPushSkillsMsg.pushSelectedSkills(role);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		// 忽略
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		KSkillLogic.newSkillsForUplv(role, false);
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		// 忽略
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		KSkillLogic.newSkillsForUplv(role, true);
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}

}
