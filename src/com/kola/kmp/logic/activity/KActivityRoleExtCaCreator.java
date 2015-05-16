package com.kola.kmp.logic.activity;

import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kmp.logic.activity.goldact.GoldActivityRoleRecordData;
import com.kola.kmp.logic.activity.newglodact.NewGoldActivityRoleRecordData;
import com.kola.kmp.logic.activity.worldboss.KWorldBossRoleData;
import com.kola.kmp.logic.other.KRoleExtTypeEnum;
import com.kola.kmp.logic.role.IRoleExtCACreator;
import com.kola.kmp.logic.support.KSupportFactory;

public class KActivityRoleExtCaCreator implements IRoleExtCACreator {

//	@Override<extType type="9" creatorClassPath="com.kola.kmp.logic.reward.garden.KGardenRoleExtCACreator" />
	public RoleExtCABaseImpl createRoleExtCAForDB(long roleId, int type) {
		return new KActivityRoleExtData(roleId, type);
	}

	@Override
	public RoleExtCABaseImpl newRoleExtCAInstance(long roleId, int type) {
		return new KActivityRoleExtData(roleId, type);
	}
	
	
	public static KActivityRoleExtData getActivityRoleExtData(long roleId) {
		return (KActivityRoleExtData)KSupportFactory.getRoleModuleSupport().getRoleExtCA(roleId, KRoleExtTypeEnum.ACTIVITY, true);
	}
	
	public static KWorldBossRoleData getWorldBossRoleData(long roleId) {
		KActivityRoleExtData ext = getActivityRoleExtData(roleId);
		return ext.getWorldBossRoleData();
	}
	
	public static GoldActivityRoleRecordData getGoldActivityRoleRecordData(long roleId) {
		KActivityRoleExtData ext = getActivityRoleExtData(roleId);
		return ext.getGlodActivityData();
	}
	
	public static NewGoldActivityRoleRecordData getNewGoldActivityRoleRecordData(long roleId) {
		KActivityRoleExtData ext = getActivityRoleExtData(roleId);
		return ext.getNewGoldActivityData();
	}

}
