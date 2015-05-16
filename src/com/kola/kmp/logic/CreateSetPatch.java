package com.kola.kmp.logic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.kola.kgame.cache.CacheEntitySet;
import com.kola.kgame.cache.DBRoleEntireDataImpl;
import com.kola.kgame.cache.DataCacheAccesserFactory;
import com.kola.kgame.cache.RoleEntireDataCacheAccesserImpl;
import com.kola.kgame.cache.RoleEntireDataImpl;
import com.kola.kgame.cache.currency.CurrencyAccountSet;
import com.kola.kgame.cache.currency.CurrencyModuleFactory;
import com.kola.kgame.cache.item.ItemModuleFactory;
import com.kola.kgame.cache.item.ItemSet;
import com.kola.kgame.cache.level.GameLevelModuleFactory;
import com.kola.kgame.cache.level.GameLevelSet;
import com.kola.kgame.cache.mail.MailModuleFactory;
import com.kola.kgame.cache.mail.MailSet;
import com.kola.kgame.cache.mission.MissionCompleteRecordSet;
import com.kola.kgame.cache.mission.MissionModuleFactory;
import com.kola.kgame.cache.mission.MissionSet;
import com.kola.kgame.cache.mount.MountModuleFactory;
import com.kola.kgame.cache.mount.MountSet;
import com.kola.kgame.cache.pet.PetModuleFactory;
import com.kola.kgame.cache.pet.PetSet;
import com.kola.kgame.cache.relationship.RelationShipModuleFactory;
import com.kola.kgame.cache.relationship.RelationShipSet;
import com.kola.kgame.cache.skill.SkillModuleFactory;
import com.kola.kgame.cache.skill.SkillSet;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class CreateSetPatch implements RunTimeTask {
	private static Logger _LOGGER = KGameLogger.getLogger(CreateSetPatch.class);

	public String run(String data) {
		Set<Long> roleIds = new HashSet<Long>();
		for (String idStr : data.split(";")) {
			roleIds.add(Long.parseLong(idStr));
		}

		if (roleIds.isEmpty()) {
			return "请输入正确参数";
		}

		try {
			for (long roleId : roleIds) {
				KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
				if (role == null) {
					continue;
				}
				patchRole(role);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}

		return "执行成功";
	}

	private static void patchRole(KRole role) throws Exception {

		RoleEntireDataImpl roleData = null;
		{
			RoleEntireDataCacheAccesserImpl instance = (RoleEntireDataCacheAccesserImpl) DataCacheAccesserFactory.getRoleEntireDataCacheAccesser();

			Method method = RoleEntireDataCacheAccesserImpl.class.getDeclaredMethod("getRoleData", Object.class);
			method.setAccessible(true);
			roleData = (RoleEntireDataImpl) method.invoke(instance, role.getId());
			if (roleData == null) {
				return;
			}
		}

		List<CacheEntitySet> setList = new ArrayList<CacheEntitySet>();
		{
			if (roleData.getPetSet() == null) {
				PetSet ps = PetModuleFactory.getPetModule().newPetSetInstanceForCache();
				setList.add(ps);
				assignValueToField(roleData, "_petSet", ps);
			}
			if (roleData.getAccountSet() == null) {
				CurrencyAccountSet ac = CurrencyModuleFactory.getModule().newCurrencyAccountSet(role.getId(), true);
				setList.add(ac);
				assignValueToField(roleData, "_accountSet", ac);
			}
			if (roleData.getItemSet() == null) {
				ItemSet itemSet = ItemModuleFactory.getModule().newItemSet(role.getId(), true);
				setList.add(itemSet);
				assignValueToField(roleData, "_itemSet", itemSet);
			}
			if (roleData.getMailSet() == null) {
				MailSet mailSet = MailModuleFactory.getModule().newMailSet(role.getId(), true);
				setList.add(MailModuleFactory.getModule().newMailSet(role.getId(), true));
				assignValueToField(roleData, "_mailSet", mailSet);
			}
			if (roleData.getMountSet() == null) {
				MountSet mountSet = MountModuleFactory.getModule().newMountSet(role.getId(), role.getJob(), true);
				setList.add(mountSet);
				assignValueToField(roleData, "_mountSet", mountSet);
			}
			if (roleData.getRelationShipSet() == null) {
				RelationShipSet relationShipSet = RelationShipModuleFactory.getModule().newRelationShipSet(role.getId(), true);
				setList.add(relationShipSet);
				assignValueToField(roleData, "_relationShipSet", relationShipSet);
			}
			if (roleData.getSkillSet() == null) {
				SkillSet skillSet = SkillModuleFactory.getModule().newSkillSet(role.getId(), true);
				setList.add(skillSet);
				assignValueToField(roleData, "_skillSet", skillSet);
			}

			if (roleData.getMissionSet() == null) {
				MissionSet missionSet = MissionModuleFactory.getModule().newMissionSet(role.getId(), true);
				setList.add(missionSet);
				assignValueToField(roleData, "_missionSet", missionSet);
			}
			if (roleData.getMissionCompleteRecordSet() == null) {
				MissionCompleteRecordSet completeRecordSet = MissionModuleFactory.getModule().newMissionCompleteRecordSet(role.getId(), true);
				setList.add(completeRecordSet);
				assignValueToField(roleData, "_missionCompleteRecordSet", completeRecordSet);
			}
			if (roleData.getGameLevelSet() == null) {
				GameLevelSet gameLevelSet = GameLevelModuleFactory.getModule().newGameLevelSet(role.getId(), true);
				setList.add(GameLevelModuleFactory.getModule().newGameLevelSet(role.getId(), true));
				assignValueToField(roleData, "_gameLevelSet", gameLevelSet);
			}
		}

		if (!setList.isEmpty()) {
			Method method = RoleEntireDataImpl.class.getDeclaredMethod("initDataSetForFresh", CacheEntitySet.class);
			method.setAccessible(true);
			for (CacheEntitySet set : setList) {
				method.invoke(roleData, set);
			}
			method.setAccessible(false);
		}

		if (!setList.isEmpty()) {
			Method method = RoleEntireDataImpl.class.getDeclaredMethod("initComplete");
			method.setAccessible(true);
			method.invoke(roleData);
			method.setAccessible(true);
		}
		
		
		{
			Field field = RoleEntireDataImpl.class.getDeclaredField("_saveData");
			field.setAccessible(true);
			DBRoleEntireDataImpl _saveData = (DBRoleEntireDataImpl)field.get(roleData);
			if(_saveData==null){
				Constructor mConstructor = DBRoleEntireDataImpl.class.getDeclaredConstructor();
				mConstructor.setAccessible(true);
				_saveData = (DBRoleEntireDataImpl) mConstructor.newInstance();
				mConstructor.setAccessible(false);
				
				field.set(roleData, _saveData);
			}
			field.setAccessible(false);
		}
	}
	
	private static void assignValueToField(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
			field.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
