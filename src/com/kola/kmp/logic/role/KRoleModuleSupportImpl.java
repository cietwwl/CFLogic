package com.kola.kmp.logic.role;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.role.Role;
import com.kola.kgame.cache.role.RoleBaseInfo;
import com.kola.kgame.cache.role.RoleExtCA;
import com.kola.kgame.cache.role.RoleExtCASet;
import com.kola.kgame.cache.role.RoleModuleFactory;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.flow.KRoleAttrModifyType;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KRoleExtTypeEnum;
import com.kola.kmp.logic.role.KRoleModuleManager.ExpData;
import com.kola.kmp.logic.support.ItemModuleSupport.ISecondWeapon;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.IRoleEquipShowData;
import com.kola.kmp.logic.util.IRoleMapResInfo;
import com.kola.kmp.logic.util.KBattlePowerCalculator;

/**
 * 
 * @author PERRY CHAN
 */
public class KRoleModuleSupportImpl implements RoleModuleSupport {
	
	private void packDataToMsg(IRoleMapResInfo info, byte job, boolean packJob, KGameMessage msg) {
		List<IRoleEquipShowData> list;
		if (info.getEquipmentRes() != null) {
			list = info.getEquipmentRes();
		} else {
			list = Collections.emptyList();
		}
		if (packJob) {
			msg.writeByte(job);
		}
		msg.writeByte(list.size());
		IRoleEquipShowData temp;
		if (list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				temp = list.get(i);
				msg.writeByte(temp.getPart());
				msg.writeUtf8String(temp.getRes());
			}
		}
		msg.writeUtf8String(info.getFashionRes() == null ? "" : info.getFashionRes());
	}
	
	/**
	 * 
	 * @param roleId
	 * @param loadAllIfNotExists
	 * @return
	 */
	private KRole getRoleInternal(long roleId, boolean loadAllIfNotExists) {
		Role role = RoleModuleFactory.getRoleModule().getRoleById(roleId, loadAllIfNotExists);
		if(role != null) {
			return (KRole)role;
		}
		return null;
	}

	@Override
	public IRoleMapData getRoleMapData(long roleId) {
		Role role = RoleModuleFactory.getRoleModule().getRoleById(roleId, true);
		if(role != null) {
			return ((KRole)role).getRoleMapData();
		}
		return null;
	}

	@Override
	public KRole getRole(KGamePlayerSession session) {
		Role role = RoleModuleFactory.getRoleModule().getPlayerInUseRole(session);
		if(role != null) {
			return (KRole)role;
		} else {
			return null;
		}
	}

	@Override
	public KRole getRole(long roleId) {
		return this.getRoleInternal(roleId, false);
	}
	
	@Override
	public KRole getRoleWithOfflineAttr(long roleId) {
		KRole role = this.getRoleInternal(roleId, false);
		if (RoleModuleFactory.getRoleModule().isRoleInCache(roleId)) {
			if (!role.isFirstInitFinish()) {
				role.firstInit();
			}
		}
		return role;
	}
	
	@Override
	public Set<Long> getRoleIdCache() {
		return RoleModuleFactory.getRoleModule().getRoleIdCache();
	}
	
	@Override
	public int getLevel(long roleId) {
		Role role = this.getRoleInternal(roleId, false);
		if (role != null) {
			return role.getLevel();
		} else {
			return 0;
		}
	}

	@Override
	public boolean sendMsg(long roleId, KGameMessage msg) {
		Role role = this.getRoleInternal(roleId, true);
		if (role != null) {
			return role.sendMsg(msg);
		}
		return false;
	}

	@Override
	public void notifyUseConsumeItemEffect(long roleId, AttValueStruct addAtt, KRoleAttrModifyType type, Object... args) {
		KRole role = this.getRole(roleId);
		if (role != null) {
			role.effectAttr(addAtt.roleAttType, addAtt.addValue, type, args);
		}
	}
	
	@Override
	public void notifyEffectAttrChange(long roleId, int providerType) {
		KRole role = this.getRoleInternal(roleId, true);
		if(role != null) {
			role.notifyEffectChange(providerType);
		}
	}
	
	@Override
	public void notifySecondWeaponUpdate(long roleId, ISecondWeapon weapon) {
		KRole role = this.getRoleInternal(roleId, true);
		if (role != null) {
			role.updateSecondWeapon(weapon);
		}
	}
	
	@Override
	public int addExp(long roleId, int exp, KRoleAttrModifyType type, Object... args) {
		KRole role = this.getRoleInternal(roleId, false);
		if (role != null) {
			return role.addExp(exp, type, args);
		}
		return 0;
	}

	@Override
	public List<Long> getAllOnLineRoleIds() {
		return RoleModuleFactory.getRoleModule().getAllOnLineRoleIds();
	}

	@Override
	public int getAllOnLineRoleNum() {
		return RoleModuleFactory.getRoleModule().getAllOnLineRoleIds().size();
	}

	@Override
	public KRole getRole(String roleName) {
		Role role = RoleModuleFactory.getRoleModule().getRoleByName(roleName, false);
		if(role != null) {
			return (KRole)role;
		}
		return null;
	}
	
	@Override
	public List<IRoleBaseInfo> getRoleList(long playerId) {
		return KRoleBaseInfoCacheManager.getRoleListOfPlayer(playerId, true);
	}
	
	@Override
	public RoleBaseInfo getRoleBaseInfo(long roleId) {
		return KRoleBaseInfoCacheManager.getRoleBaseInfo(roleId);
	}

	@Override
	public String getRoleName(long roleId) {
		Role role = this.getRoleInternal(roleId, false);
		if(role != null) {
			return role.getName();
		}
		return "";
	}
	
	@Override
	public String getRoleExtName(long roleId) {
		Role role = this.getRoleInternal(roleId, false);
		if(role != null) {
			return ((KRole)role).getExName();
		} else {
			return "";
		}
		
	}
	

	@Override
	public void addAttsFromBaseReward(KRole role, List<AttValueStruct> attList, KRoleAttrModifyType type, Object... args) {
		AttValueStruct temp;
		for (int i = 0; i < attList.size(); i++) {
			temp = attList.get(i);
			role.effectAttr(temp.roleAttType, temp.addValue, type, args);
		}
	}
	
	@Override
	public int calculateBattlePower(Map<KGameAttrType, Integer> attrMap, long roleId) {
		if (roleId > 0) {
			KRole role = this.getRoleInternal(roleId, false);
			if (role != null) {
				return KBattlePowerCalculator.calculateBattlePowerOfRole(attrMap, role);
			}
		}
		return KBattlePowerCalculator.calculateBattlePower(attrMap, 1, false);
	}
	
	@Override
	public int calculateBattlePower(Map<KGameAttrType, Integer> attrMap, int lv) {
		return KBattlePowerCalculator.calculateBattlePower(attrMap, lv, false);
	}
	
	@Override
	public RoleExtCA getRoleExtCA(long roleId, KRoleExtTypeEnum type, boolean addIfAbsent) {
		RoleExtCASet set = RoleModuleFactory.getRoleModule().getRoleExtCASet(roleId);
		if (set != null) {
			RoleExtCA extCA = set.getExtCAByType(type.sign);
			if (extCA == null && addIfAbsent) {
				return set.addExtCA(type.sign);
			} else {
				return extCA;
			}
		}
		return null;
	}
	
	@Override
	public RoleExtCA addExtCAToRole(long roleId, KRoleExtTypeEnum type) {
		RoleExtCASet set = RoleModuleFactory.getRoleModule().getRoleExtCASet(roleId);
		if (set != null) {
			return set.addExtCA(type.sign);
		}
		return null;
	}

	@Override
	public boolean isPlayerFirstCharge(long id) {
		Role role = this.getRoleInternal(id, false);
		if (role != null) {
			KGamePlayerSession session = KGame.getPlayerSession(role.getPlayerId());
			if (session != null && session.getBoundPlayer() != null) {
				return session.getBoundPlayer().isFirstCharge();
			}
		}
		return true;
	}

	@Override
	public void setPlayerFirstCharge(long roleId, boolean flag) {
		Role role = this.getRoleInternal(roleId, false);
		if (role != null) {
			KGamePlayerSession session = KGame.getPlayerSession(role.getPlayerId());
			if (session != null && session.getBoundPlayer() != null) {
				session.getBoundPlayer().setFirstCharge(flag);
			}
		}
	}
	
	@Override
	public boolean decreasePhyPower(KRole role, int value, String reason) {
		return role.decreasePhyPower(value, reason);
	}
	
	@Override
	public int getUpgradeExp(int level) {
		if (level > KRoleModuleConfig.getRoleMaxLv()) {
			return Integer.MAX_VALUE;
		} else {
			ExpData expData = KRoleModuleManager.getExpData(level);
			return expData.upgradeExp;
		}
	}

	@Override
	public boolean isRoleOnline(long roleId) {
		return RoleModuleFactory.getRoleModule().isRoleOnline(roleId);
	}
	
	@Override
	public boolean isPhyPowerFull(KRole role) {
		return role.getPhyPower() >= KRoleModuleConfig.getMaxPhyPower();
	}
	
	public int checkPhyPowerUsed(KRole role){
		return Math.max(KRoleModuleConfig.getMaxPhyPower() - role.getPhyPower(), 0);
	}
	
	@Override
	public void addPhyPower(long roleId, int count, boolean allowOverflow, String reason) {
		KRole role = this.getRoleInternal(roleId, true);
		if(role != null) {
			role.increasePhyPower(count, true, allowOverflow, reason);
		}
	}

	@Override
	public void notifyPhyPowerBuyFull(long roleId, String reason) {
		KRole role = this.getRoleInternal(roleId, true);
		if (role != null) {
			role.fullPhyPower(reason);
		}
	}
	
	@Override
	public void syncEnergy(long roleId, int energy, int energyBeans) {
		KRole role = this.getRoleInternal(roleId, true);
		if(role!=null) {
			role.syncEnergyInfo(energy, energyBeans);
		}
	}
	
	@Override
	public void updateEquipmentRes(long roleId) {
		KRoleBaseInfoCacheManager.updateEquipmentResMap(roleId);
	}
	
	@Override
	public void updateEquipmentSetRes(long roleId) {
		KRoleBaseInfoCacheManager.updateEquipSetRes(roleId);
	}
	
	@Override
	public void updateFashionRes(long roleId) {
		KRoleBaseInfoCacheManager.updateFashionRes(roleId);
	}
	
	@Override
	public void packRoleResToMsg(IRoleMapResInfo info, byte job, KGameMessage msg) {
		packDataToMsg(info, job, true, msg);
	}

	@Override
	public void packRoleResToMsg(IRoleMapResInfo info, KGameMessage msg) {
		packDataToMsg(info, (byte) 0, false, msg);
	}
	
	@Override
	public boolean isRoleDataInCache(long roleId) {
		return RoleModuleFactory.getRoleModule().isRoleInCache(roleId);
	}
	
	@Override
	public void notifySkillUpgrade(long roleId, ICombatSkillData skill) {
		KRole role = this.getRoleInternal(roleId, true);
		if (role != null) {
			role.updateOfflineSkill(skill);
		}
	}
	
	@Override
	public void notifySkillListChange(long roleId) {
		KRole role = this.getRoleInternal(roleId, true);
		if (role != null) {
			role.notifySkillListChange();
		}
	}
	
	@Override
	public KRoleTemplate getRoleTemplateByJob(byte job) {
		return KRoleModuleManager.getRoleTemplateByJob(job);
	}
	
	@Override
	public int getPhyPowerFullSize() {
		return KRoleModuleConfig.getMaxPhyPower();
	}
	
	@Override
	public int getHpMultiple(int lv) {
		Integer value = KRoleModuleManager.pvpHpMultipleMap.get(lv);
		if(value == null) {
			value = 1;
		}
		return value;
	}
}
