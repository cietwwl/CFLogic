package com.kola.kmp.logic.competition.teampvp;

import java.util.List;

import com.kola.kmp.logic.combat.ICombatMirrorDataGroup;
import com.kola.kmp.logic.combat.api.ICombatMount;
import com.kola.kmp.logic.combat.api.ICombatPet;
import com.kola.kmp.logic.combat.api.ICombatRole;
import com.kola.kmp.logic.support.ItemModuleSupport.ISecondWeapon;
import com.kola.kmp.logic.util.IRoleEquipShowData;

public class KTeamPVPRobotGroup implements ICombatMirrorDataGroup {

	private KTeamPVPRobot _roleData;
	private KTeamPVPRobotPet _petData;
	private KTeamPVPRobotMount _mountData;
	
	private boolean _hasPetData;
	private boolean _hasMountData;
	
	KTeamPVPRobotGroup() {
	}
	
	void setRoleData(KTeamPVPRobot pRoleData) {
		this._roleData = pRoleData;
	}
	
	void setPetData(KTeamPVPRobotPet pPetData) {
		this._petData = pPetData;
	}
	
	void setMountData(KTeamPVPRobotMount pMountData) {
		this._mountData = pMountData;
	}
	
	void setPetStatus(boolean usable) {
		_hasPetData = usable;
	}
	
	void setMountStatus(boolean usable) {
		_hasMountData = usable;
	}
	
	KTeamPVPRobotPet getPetData() {
		return _petData;
	}
	
	KTeamPVPRobot getRoleData() {
		return _roleData;
	}
	
	KTeamPVPRobotMount getMountData() {
		return _mountData;
	}
	
	@Override
	public List<IRoleEquipShowData> getEquipmentRes() {
		return _roleData.getEquipmentRes();
	}

	@Override
	public String getFashionRes() {
		return _roleData.getFashionResId();
	}

	@Override
	public int[] getEquipSetRes() {
		return _roleData.getEquipSetRes();
	}

	@Override
	public ICombatRole getRoleMirror() {
		return _roleData;
	}

	@Override
	public ICombatPet getPetMirror() {
		return _hasPetData ? _petData : null;
	}

	@Override
	public ICombatMount getMountMirror() {
		return _hasMountData ? _mountData : null;
	}

	@Override
	public ISecondWeapon getSecondWeapon() {
		return null;
	}

}
