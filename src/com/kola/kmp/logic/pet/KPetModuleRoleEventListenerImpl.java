package com.kola.kmp.logic.pet;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.flow.KPetFlowType;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KPetModuleRoleEventListenerImpl implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		KPetModuleManager.notifyRoleJoinedGame(session, role);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
//		testDelPet(role);
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		if(KPetModuleConfig.getPresentPetsWhenRoleCreated().size() > 0) {
			KSupportFactory.getPetModuleSupport().createPetsToRole(role.getId(), KPetModuleConfig.getPresentPetsWhenRoleCreated(), KPetFlowType.新手赠送.name());
		}
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}
	
//	private void testAddPet(KRole role) {
//		PetSet ps = PetModuleFactory.getPetModule().getPetSet(role.getId());
//		Pet pet = new KPet(KPetModuleManager.getPetTemplate(1));
//		ps.addPet(pet);
//		pet = new KPet(KPetModuleManager.getPetTemplate(2));
//		ps.addPet(pet);
//		pet = new KPet(KPetModuleManager.getPetTemplate(3));
//		ps.addPet(pet);
//	}
//	
//	private void testDelPet(KRole role) {
//		PetSet ps = PetModuleFactory.getPetModule().getPetSet(role.getId());
//		List<Pet> allPets = ps.getAllPets();
//		if(allPets.size() > 0) {
//			Pet pet = allPets.get(UtilTool.random(allPets.size()));
//			ps.deletePet(pet.getId());
//		}
//	}

}
