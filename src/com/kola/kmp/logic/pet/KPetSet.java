package com.kola.kmp.logic.pet;

import java.util.List;

import com.kola.kgame.cache.pet.Pet;
import com.kola.kgame.cache.pet.PetSetBaseImpl;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KPetSet extends PetSetBaseImpl {

//	private long _fightingPetId; // 出战随从的id
	private KPet _fightingPet;
	
	@Override
	protected String saveLogicAttr() {
		return "";
	}

	@Override
	protected void parseLogicAttr(String attribute) {
		
	}
	
	@Override
	protected void notifyInitFromDBComplete() {
		List<Pet> allPets = this.getAllPets();
		KPet pet;
		for (int i = 0; i < allPets.size(); i++) {
			pet = (KPet) allPets.get(i);
			if (pet.isFighting()) {
				if(this._fightingPet == null) {
					this._fightingPet = pet;
				} else {
					pet.setFightingStatus(false);
				}
			}
		}
	}
	
	void setFightingPet(KPet pet) {
		this._fightingPet = pet;
	}
	
	public KPet getFightingPet() {
		return this._fightingPet;
	}
	
	public long getFightingPetId() {
		if(this._fightingPet == null) {
			return 0;
		} else {
			return this._fightingPet.getId();
		}
	}
	
	public void updateFightingPet(long petId) {
		long preId = this._fightingPet == null ? 0 : this._fightingPet.getId();
		Pet pet = this.getPet(petId);
		if(pet != null) {
			this._fightingPet = (KPet)pet;
		} else {
			this._fightingPet = null;
		}
		KSupportFactory.getMapSupport().notifyRoleFightingPetChange(this.getRoleId(), preId);
		KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(this.getRoleId(), KPetModuleAttributeProvider.getProviderType());
	}

//	Map<KGameAttrType, Integer> getFightingPetEffect() {
//		KPet pet = this.getFightingPet();
//		if (pet != null) {
//			return pet.getIncreaseRoleAttrMap();
//		} else {
//			return new HashMap<KGameAttrType, Integer>();
//		}
//	}

}
