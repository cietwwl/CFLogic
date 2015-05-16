package com.kola.kmp.logic.support;

import java.util.List;
import java.util.Map;

import com.koala.game.KGameMessage;
import com.koala.game.player.KGameAccount;
import com.kola.kgame.cache.pet.Pet;
import com.kola.kmp.logic.combat.api.ICombatPet;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.pet.KPet;
import com.kola.kmp.logic.pet.KPetAttrPara;
import com.kola.kmp.logic.pet.KPetSet;
import com.kola.kmp.logic.pet.KPetTemplate;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;

/**
 * 
 * @author PERRY CHAN
 */
public interface PetModuleSupport {

	/**
	 * 
	 * @param roleId
	 * @return
	 */
	public KPetSet getPetSet(long roleId);

	/**
	 * 
	 * 获取出战状态的随从
	 * 
	 * @param roleId
	 * @return
	 */
	public KPet getFightingPet(long roleId);

	/**
	 * 
	 * 获取出战随从的战场对象形态
	 * 
	 * @param roleId
	 * @return
	 */
	public ICombatPet getFightingPetForBattle(long roleId);

//	/**
//	 * 
//	 * 获取影响角色的属性
//	 * 
//	 * @param roleId
//	 * @return
//	 */
//	public Map<KGameAttrType, Integer> getEffectRoleAttribute(long roleId);

	/**
	 * 
	 * 获取所有随从
	 * 
	 * @param roleId
	 * @return
	 */
	public List<KPet> getAllPets(long roleId);

	/**
	 * 
	 * 获取指定的随从实例
	 * 
	 * @param roleId
	 * @param petId
	 * @return
	 */
	public KPet getPet(long roleId, long petId);

	/**
	 * 
	 * 获取一组随从
	 * 
	 * @param roleId
	 * @param petIds
	 * @return
	 */
	public List<KPet> getPets(long roleId, List<Long> petIds);

	/**
	 * 
	 * 删除多个宠物
	 * 
	 * @param roleId
	 * @param petIds
	 * @return
	 */
	public boolean deletePets(long roleId, List<Long> petIds, String descr);

	/**
	 * 
	 * 给角色创建一个随从
	 * 
	 * @param roleId
	 * @param templateId
	 * @return
	 */
	public KActionResult<Pet> createPetToRole(long roleId, int templateId, String descr);
	/**
	 * <pre>
	 * 给角色创建多个随从
	 * 不允许部分创建
	 * 
	 * @param roleId
	 * @param petTempIds
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-2 上午10:55:49
	 * </pre>
	 */
	public CommonResult createPetsToRole(long roleId, List<Integer> petTempIds, String descr);
	
	/**
	 * 
	 * @param role
	 * @param petTemplateIds
	 * @param allowOverFlow
	 */
	public boolean createPetsToRole(long roleId, Map<Integer, Integer> petTemplateIds, boolean allowOverFlow, String descr);

	/**
	 * 
	 * 获取宠物模板
	 * 
	 * @param petTemplateId
	 * @return
	 */
	public KPetTemplate getPetTemplate(int petTemplateId);

	/**
	 * <pre>
	 * 打包一个随从的通用消息数据
	 * 
	 * @param dataStruct
	 * @author CamusHuang
	 * @creation 2014-4-2 上午9:40:45
	 * </pre>
	 */
	public void packPetTemplateMsg(KGameMessage msg, int petTemplateId);
	
	/**
	 * 
	 * @param roleId
	 */
	public void addExpToFightingPet(KRole role, int exp, String reason);
	
	/**
	 * 
	 * @param role
	 * @param swallowerId
	 * @param beComposedIds
	 */
	public void processComposePet(KRole role, long swallowerId, List<Long> beComposedIds, boolean confirmSenior, boolean confirmOverflow);
	
	/**
	 * 
	 * @param role
	 * @param petId
	 * @param confirm
	 */
	public void processSetFreePet(KRole role, long petId, boolean confirm);
	
	/**
	 * 
	 * @param msg
	 * @param pet
	 */
	public void packPetDataToMsg(KGameMessage msg, KPet pet);
	
	/**
	 * 
	 * 添加新手引导随从
	 * 
	 * @param role
	 */
	public void addNoviceGuideFightingPet(KRole role);
	
	/**
	 * 
	 * 
	 * 移除新手引导随从
	 * 
	 * @param role
	 */
	public void removeNoviceGuideFightingPet(KRole role);
	
	/**
	 * <pre>
	 * 通知随从经验加成发生改变
	 * </pre>
	 * @param roleId
	 */
	public void notifyPetComposeIncChange(long roleId);
	
	/**
	 * 
	 * @param templateId
	 * @return
	 */
	public ICombatPet createFightingPet(int templateId);
	
	/**
	 * 
	 * @param template
	 * @param lv
	 * @param growthValue
	 * @return
	 */
	public Map<KGameAttrType, Integer> calcualteAttrs(KPetTemplate template, int lv, int starLv, int growthValue);
}
