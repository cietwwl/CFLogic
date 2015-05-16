package com.kola.kmp.logic.combat.mirror;

import java.util.ArrayList;
import java.util.List;

import com.kola.kmp.logic.combat.ICombatMirrorDataGroup;
import com.kola.kmp.logic.combat.api.ICombatMount;
import com.kola.kmp.logic.combat.api.ICombatPet;
import com.kola.kmp.logic.combat.api.ICombatRole;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.ItemModuleSupport.ISecondWeapon;
import com.kola.kmp.logic.util.IRoleEquipShowData;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatMirrorDataGroupImpl implements ICombatMirrorDataGroup {

	private KCombatRoleMirrior _roleMirror;
	private KCombatPetMirror _petMirror;
	private KCombatMountMirror _mountMirror;
	private List<IRoleEquipShowData> _equipShowDataList;
	private String _fashionRes;
	private int[] _equipSetRes;
	private ISecondWeapon _secondWeapon;
	
	/**
	 * 
	 */
	KCombatMirrorDataGroupImpl(KRole role, ICombatPet combatPet, ICombatMount combatMount) {
		this._roleMirror = new KCombatRoleMirrior(role);
		if (combatPet != null) {
			this._petMirror = new KCombatPetMirror(combatPet);
		}
		if (combatMount != null) {
			this._mountMirror = new KCombatMountMirror(combatMount);
		}
		this._equipShowDataList = new ArrayList<IRoleEquipShowData>(role.getEquipmentRes());
		this._fashionRes = role.getFashionRes();
		this._secondWeapon = role.getSecondWeapon();
		this._equipSetRes = role.getEquipSetRes();
	}

	@Override
	public ICombatRole getRoleMirror() {
		return _roleMirror;
	}

	@Override
	public ICombatPet getPetMirror() {
		return _petMirror;
	}

	@Override
	public ICombatMount getMountMirror() {
		return _mountMirror;
	}
	
	@Override
	public ISecondWeapon getSecondWeapon() {
		return _secondWeapon;
	}
	
	@Override
	public List<IRoleEquipShowData> getEquipmentRes() {
		return _equipShowDataList;
	}
	
	@Override
	public String getFashionRes() {
		return _fashionRes;
	}
	
	@Override
	public int[] getEquipSetRes() {
		return _equipSetRes;
	}

}
