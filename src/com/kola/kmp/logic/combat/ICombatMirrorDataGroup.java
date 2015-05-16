package com.kola.kmp.logic.combat;

import com.kola.kmp.logic.combat.api.ICombatMount;
import com.kola.kmp.logic.combat.api.ICombatPet;
import com.kola.kmp.logic.combat.api.ICombatRole;
import com.kola.kmp.logic.support.ItemModuleSupport.ISecondWeapon;
import com.kola.kmp.logic.util.IRoleMapResInfo;

/**
 * 
 * @author PERRY CHAN
 */
public interface ICombatMirrorDataGroup extends IRoleMapResInfo {

	/**
	 * 
	 * @return
	 */
	public ICombatRole getRoleMirror();
	
	/**
	 * 
	 * @return
	 */
	public ICombatPet getPetMirror();
	
	/**
	 * 
	 * @return
	 */
	public ICombatMount getMountMirror();
	
	/**
	 * 
	 * 获取副武器
	 * 
	 * @return
	 */
	public ISecondWeapon getSecondWeapon();
}
