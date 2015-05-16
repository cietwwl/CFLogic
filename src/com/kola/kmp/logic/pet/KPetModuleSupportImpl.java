package com.kola.kmp.logic.pet;

import static com.kola.kmp.protocol.pet.KPetProtocol.SM_NOTIFY_COMPOSE_INC_CHANGE;
import static com.kola.kmp.protocol.pet.KPetProtocol.SM_RESPONSE_SWALLOW_PETS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kgame.cache.pet.Pet;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.KGameGlobalConfig;
import com.kola.kmp.logic.combat.api.ICombatPet;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.PropertyTypeEnum;
import com.kola.kmp.logic.flow.KPetFlowType;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGangTecTypeEnum;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.other.KUseFunctionTypeEnum;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.pet.message.KPetServerMsgSender;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.PetModuleSupport;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.PetTips;
import com.kola.kmp.logic.util.tips.ShopTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KPetModuleSupportImpl implements PetModuleSupport {
	
	private static final AtomicLong _idGenerator = new AtomicLong(0);
	
	private void sendConfirmSwallowDialog(KRole role, long swallowerId, List<Long> beComposedIds, short key, String tips) {
		StringBuilder builder = new StringBuilder();
		builder.append(swallowerId);
		for(int i = 0; i < beComposedIds.size(); i++) {
			builder.append(",");
			builder.append(beComposedIds.get(i));
		}
		KDialogButton button = new KDialogButton(key, builder.toString(), KDialogButton.CONFIRM_DISPLAY_TEXT);
		KDialogService.sendFunDialog(role, "", tips, Arrays.asList(KDialogButton.CANCEL_BUTTON, button), false, (byte)-1);
	}
	
	private int calculateBoth(KGameAttrType type, int attrDeviPara, int lvPara, int starPara, int growValue, float lvProportion, float starProportion) {
		float lvResult = KPetModuleManager.calculateSingle(attrDeviPara, lvPara, growValue, lvProportion);
		float starResult = KPetModuleManager.calculateSingle(attrDeviPara, starPara, growValue, starProportion);
		return Math.round(lvResult + starResult);
	}
	
	@Override
	public KPetSet getPetSet(long roleId) {
		return KPetModuleManager.getPetSet(roleId);
	}
	
	@Override
	public KPet getFightingPet(long roleId) {
		return KPetModuleManager.getFightingPet(roleId);
	}

	@Override
	public ICombatPet getFightingPetForBattle(long roleId) {
		return KPetModuleManager.getFightingPet(roleId);
	}

//	@Override
//	public Map<KGameAttrType, Integer> getEffectRoleAttribute(long roleId) {
//		KPetSet ps = this.getPetSet(roleId);
//		if(ps != null) {
//			return ps.getFightingPetEffect();
//		}
//		return null;
//	}
	
	@Override
	public List<KPet> getAllPets(long roleId) {
		KPetSet ps = this.getPetSet(roleId);
		if (ps != null) {
			List<Pet> allPets = ps.getAllPets();
			List<KPet> rtnList = new ArrayList<KPet>(allPets.size());
			for (int i = 0; i < allPets.size(); i++) {
				rtnList.add((KPet) allPets.get(i));
			}
			return rtnList;
		}
		return Collections.emptyList();
	}
	
	@Override
	public KPet getPet(long roleId, long petId) {
		KPetSet ps = this.getPetSet(roleId);
		if(ps != null) {
			Pet pet = ps.getPet(petId);
			if(pet != null) {
				return (KPet)pet;
			} 
		}
		return null;
	}
	
	@Override
	public List<KPet> getPets(long roleId, List<Long> petIds) {
		KPetSet ps = this.getPetSet(roleId);
		if (ps != null) {
			List<KPet> petList = new ArrayList<KPet>();
			Pet temp;
			for (int i = 0; i < petIds.size(); i++) {
				temp = ps.getPet(petIds.get(i));
				if (temp != null) {
					petList.add((KPet) temp);
				}
			}
			return petList;
		} else {
			return new ArrayList<KPet>(0);
		}
	}
	
	@Override
	public boolean deletePets(long roleId, List<Long> petIds, String descr) {
		if (petIds.isEmpty()) {
			return true;
		} else {
			KPetSet ps = this.getPetSet(roleId);
			if (ps != null) {
				List<Pet> petList = new ArrayList<Pet>();
				Pet temp;
				for (int i = 0; i < petIds.size(); i++) {
					temp = ps.getPet(petIds.get(i));
					if (temp != null) {
						petList.add(temp);
					}
				}
				if (petList.size() != petIds.size()) {
					return false;
				} else {
					boolean delSuccess = true;
					for (int i = 0; i < petIds.size(); i++) {
						temp = ps.getPet(petIds.get(i));
						if (ps.deletePet(temp.getId()) == null) {
							delSuccess = false;
							break;
						} else {
							FlowManager.logPropertyAddOrDelete(roleId, PropertyTypeEnum.宠物, temp.getUUID(), temp.getTemplateId(), temp.getName(), false, descr);
						}
					}
					return delSuccess;
				}
			}
		}
		return false;
	}
	
	@Override
	public KActionResult<Pet> createPetToRole(long roleId, int templateId, String descr) {
		return KPetModuleManager.createPetToRole(roleId, templateId, descr);
	}

	@Override
	public CommonResult createPetsToRole(long roleId, List<Integer> petTempIds, String descr) {
		KActionResult<List<KPet>> result = KPetModuleManager.addPets(roleId, petTempIds, false, descr);
		CommonResult cResult = new CommonResult();
		cResult.isSucess = result.success;
		cResult.tips = result.tips;
		return cResult;
	}
	
	@Override
	public boolean createPetsToRole(long roleId, Map<Integer, Integer> petTemplateIds, boolean allowOverFlow, String descr) {
		List<Integer> templateIds = new ArrayList<Integer>();
		Map.Entry<Integer, Integer> entry;
		for (Iterator<Map.Entry<Integer, Integer>> itr = petTemplateIds.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			for (int i = entry.getValue(); i-- > 0;) {
				templateIds.add(entry.getKey());
			}
		}
		KActionResult<List<KPet>> result = KPetModuleManager.addPets(roleId, templateIds, allowOverFlow, descr);
		return result.success;
	}

	@Override
	public KPetTemplate getPetTemplate(int petTemplateId) {
		return KPetModuleManager.getPetTemplate(petTemplateId);
	}

	@Override
	public void packPetTemplateMsg(KGameMessage msg, int petTemplateId) {
		KPetTemplate template = KPetModuleManager.getPetTemplate(petTemplateId);
		KPetServerMsgSender.packSinglePetData(template, msg);
	}
	
	@Override
	public void addExpToFightingPet(KRole role, int exp, String reason) {
		KPet pet = KPetModuleManager.getFightingPet(role.getId());
		if (pet != null) {
			int preLv = pet.getLevel();
			boolean add = true;
			if (pet.getLevel() < role.getLevel()) {
				pet.addExp(exp, true, role.getLevel());
			} else if (pet.getLevel() == role.getLevel()) {
				if((exp + pet.getCurrentExp()) > pet.getUpgradeExp()) {
					exp = pet.getUpgradeExp() - pet.getCurrentExp() - 1; // 不升级
				}
				if(exp > 0) {
					pet.addExp(exp, true, role.getLevel());
				}
			} else {
				add = false;
			}
			if (add) {
				FlowManager.logPropertyModify(role.getId(), PropertyTypeEnum.宠物, pet.getUUID(), pet.getTemplateId(), pet.getName(), PetTips.getTipsAddExpDescr(exp, preLv, pet.getLevel(), reason));
			}
		}
	}
	
	@Override
	public void processComposePet(KRole role, long swallowerId, List<Long> beComposedIds, boolean confirm, boolean confirmOverflow) {
		boolean swallowSuccess = false;
		String tips = null;
		Map<Integer, Integer> skillEffectMap = null;
		if(role != null) {
			Pet fightingPet = KSupportFactory.getPetModuleSupport().getFightingPet(role.getId());
			if (fightingPet != null && beComposedIds.contains(fightingPet.getId())) {
				beComposedIds.remove(fightingPet.getId());
			}
			KPet swallowerPet = KSupportFactory.getPetModuleSupport().getPet(role.getId(), swallowerId);
			if(swallowerPet == null) {
				tips = PetTips.getTipsNoSuchPet();
			} else /*if (swallowerPet.getMaxLevel() > swallowerPet.getLevel())*/ {
				List<KPet> beComposedPets = KSupportFactory.getPetModuleSupport().getPets(role.getId(), beComposedIds);
				KPet temp;
				int money = 0;
				int exp = 0;
				boolean containSenior = false;
				int starCount = 0;
				int starLvUpRate = 0;
				for (int i = 0; i < beComposedPets.size(); i++) {
					temp = beComposedPets.get(i);
					money += temp.getSwallowFee();
					if (temp.getTemplateId() == swallowerPet.getTemplateId()) {
						exp += temp.getBeComposedExp() * 2; // 同品种双倍经验
					} else {
						exp += temp.getBeComposedExp();
					}
					if(temp.getQuality().isSenior() && !containSenior) {
						containSenior = true;
					}
					if(temp.getStarLv() > 0) {
						starCount += temp.getStarLv();
					}
					if(temp.getStarLvUpRateHundred() > 0) {
						starLvUpRate += temp.getStarLvUpRate();
					}
				}
				if (containSenior && !confirm) {
//					StringBuilder builder = new StringBuilder();
//					builder.append(swallowerId);
//					for(int i = 0; i < beComposedIds.size(); i++) {
//						builder.append(",");
//						builder.append(beComposedIds.get(i));
//					}
//					KDialogButton button = new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_CONFIRM_SWALLOW_SENIOR_PET, builder.toString(), KDialogButton.CONFIRM_DISPLAY_TEXT);
//					KDialogService.sendFunDialog(role, "", PetTips.getTipsConfirmContainSenior(), Arrays.asList(KDialogButton.CANCEL_BUTTON, button), false, (byte)-1);
					sendConfirmSwallowDialog(role, swallowerId, beComposedIds, KDefaultDialogProcesser.DIALOG_FUN_ID_CONFIRM_SWALLOW_SENIOR_PET, PetTips.getTipsConfirmContainSenior());
					return;
				} else {
					if (KSupportFactory.getCurrencySupport().getMoney(role.getId(), KCurrencyTypeEnum.GOLD) < money) {
//						tips = PetTips.getTipsSwallowMoneyNotEnough(KCurrencyTypeEnum.GOLD.extName, money);
						KDialogService.showExchangeDialog(role.getId(), ShopTips.您的金币不足是否前去兑换, (long) (money - KSupportFactory.getCurrencySupport().getMoney(role.getId(), KCurrencyTypeEnum.GOLD)));
					} else {
						int finalExp = exp;
						int expPct = KSupportFactory.getGangSupport().getGangEffect(KGangTecTypeEnum.随从合成经验加成, role.getId());
						int gangTectInc = 0;
						if(expPct > 0) {
							// 军团科技加成
							gangTectInc = UtilTool.calculateTenThousandRatio(exp, expPct);
							finalExp += gangTectInc;
						}
						int activityInc = 0;
						TimeLimieProduceActivity activity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.随从合成经验倍率);
						if (activity != null && activity.isActivityTakeEffectNow()) {
							activityInc = Math.round((activity.expRate - 1) * exp);
							finalExp += activityInc;
						}
						int[] results = swallowerPet.addExp(finalExp, false, role.getLevel());
						if ((results[1] > role.getLevel() || results[0] != finalExp) && !confirmOverflow) {
							sendConfirmSwallowDialog(role, swallowerId, beComposedIds, KDefaultDialogProcesser.DIALOG_FUN_ID_CONFIRM_PET_EXP_OVERFLOW, PetTips.getTipsConfirmOverflow());
							return;
						} else if (KSupportFactory.getCurrencySupport().decreaseMoney(role.getId(), KCurrencyTypeEnum.GOLD, money, UsePointFunctionTypeEnum.吞噬随从, true) < 0) {
							tips = PetTips.getTipsSwallowMoneyNotEnough(KCurrencyTypeEnum.GOLD.extName, money);
						} else if (this.deletePets(role.getId(), beComposedIds, KPetFlowType.随从吞噬.name())) {
							if (starLvUpRate > 0) {
//								int starPct = KSupportFactory.getGangSupport().getGangEffect(KGangTecTypeEnum.随从升星成功率, role.getId());
//								if (starPct > 0) {
//									// 军团科技加成
//									starLvUpRate += starPct;
//								}
								int random = UtilTool.random(UtilTool.TEN_THOUSAND_RATIO_UNIT);
								if (random < starLvUpRate) {
									starCount++;
								}
							}
							swallowSuccess = true;
							int preExp = swallowerPet.getCurrentExp();
							int level = swallowerPet.getLevel();
							results = swallowerPet.addExp(finalExp, true, role.getLevel());
							skillEffectMap = swallowerPet.processSkillLvUpOperation(beComposedPets);
							if(starCount > 0 && swallowerPet.getStarLv() < KPetModuleConfig.getPetMaxStarLv()) {
								swallowerPet.increaseStarLv(starCount);
							}
							KPetFlowLogger.logSwallowPets(swallowerPet, beComposedPets, finalExp, preExp, level, skillEffectMap);
							StringBuilder strBld = new StringBuilder(PetTips.getTipsSwallowPetsResult(finalExp, results[0]));
							if(gangTectInc > 0) {
								strBld.append("\n").append(PetTips.getTipsGangTechInc(gangTectInc));
							}
							if(activityInc > 0) {
								strBld.append("\n").append(PetTips.getTipsActivityInc(activityInc));
							}
							tips = strBld.toString();
						} else {
							tips = GlobalTips.getTipsServerBusy();
						}
					}
				}
			} /*else {
				tips = PetTips.getTipsPetIsMaxLv(swallowerPet.getNameEx());
			}*/
		} else {
			tips = GlobalTips.getTipsServerBusy();
		}
		KGameMessage respMsg = KGame.newLogicMessage(SM_RESPONSE_SWALLOW_PETS);
		respMsg.writeBoolean(swallowSuccess);
		if(swallowSuccess) {
			respMsg.writeLong(swallowerId);
			respMsg.writeByte(beComposedIds.size());
			for(int i = 0; i < beComposedIds.size(); i++) {
				respMsg.writeLong(beComposedIds.get(i));
			}
			if (skillEffectMap != null) {
				respMsg.writeByte(skillEffectMap.size());
				Map.Entry<Integer, Integer> entry;
				for (Iterator<Map.Entry<Integer, Integer>> itr = skillEffectMap.entrySet().iterator(); itr.hasNext();) {
					entry = itr.next();
					respMsg.writeInt(entry.getKey());
					respMsg.writeByte(entry.getValue());
				}
			} else {
				respMsg.writeByte(0);
			}
		} else {
			respMsg.writeUtf8String(tips);
		}
		role.sendMsg(respMsg);
		if(swallowSuccess) {
			KDialogService.sendUprisingDialog(role, tips.split("\n"));
			KSupportFactory.getRewardModuleSupport().recordFun(role, KVitalityTypeEnum.随从合成);
			KSupportFactory.getMissionSupport().notifyUseFunction(role, KUseFunctionTypeEnum.随从合成);
		} else {
			KDialogService.sendNullDialog(role);
		}
	}
	
	@Override
	public void processSetFreePet(KRole role, long petId, boolean confirm) {
		KPet pet = this.getPet(role.getId(), petId);
		String tips;
		if(pet != null) {
			if(pet.isFighting()) {
				tips = PetTips.getTipsCannotSetFreeFightingPet();
			} else if(confirm) {
				KPetTemplate template = KPetModuleManager.getPetTemplate(pet.getTemplateId());
				Map<ItemCountStruct, Integer> map = template.itemsForSetFree;
				List<ItemCountStruct> list = new ArrayList<ItemCountStruct>();
				Map.Entry<ItemCountStruct, Integer> entry;
				int rate;
				for(Iterator<Map.Entry<ItemCountStruct, Integer>> itr = map.entrySet().iterator(); itr.hasNext();) {
					entry = itr.next();
					rate = UtilTool.random(UtilTool.TEN_THOUSAND_RATIO_UNIT);
					if(rate < entry.getValue()) {
						list.add(entry.getKey());
					}
				}
				List<Long> petIds = Arrays.asList(petId);
				if (this.deletePets(role.getId(), petIds, PresentPointTypeEnum.随从遣散.name())) {
					if (KSupportFactory.getItemModuleSupport().isCanAddItemsToBag(role.getId(), list)) {
						KSupportFactory.getItemModuleSupport().addItemsToBag(role, list, PresentPointTypeEnum.随从遣散.name());
					} else {
						KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), list, PetTips.getTipsSetFreeMailTitle(), PetTips.getTipsSetFreeMailContent(pet.getNameEx()));
					}
					tips = PetTips.getTipsSetFreeSuccess(pet.getNameEx());
					KPetServerMsgSender.sendDeletePets(role, petIds);
				} else {
					tips = PetTips.getTipsNoSuchPet();
				}
			} else {
				List<KDialogButton> buttons = new ArrayList<KDialogButton>();
				buttons.add(KDialogButton.CANCEL_BUTTON);
				buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_CONFIRM_SET_FREE, String.valueOf(petId), KDialogButton.CONFIRM_DISPLAY_TEXT));
				KDialogService.sendFunDialog(role, "", PetTips.getTipsConfirmSetFree(pet.getNameEx()), buttons, false, (byte)-1);
				return;
			}
		} else {
			tips = PetTips.getTipsNoSuchPet();
		}
		KDialogService.sendUprisingDialog(role, tips);
	}
	
	@Override
	public void packPetDataToMsg(KGameMessage msg, KPet pet) {
		KPetServerMsgSender.packSinglePetData(pet, msg);
	}
	
	@Override
	public void addNoviceGuideFightingPet(KRole role) {
		if(role.getLevel() == 1) {
			KPetSet ps = this.getPetSet(role.getId());
			KPet pet = new KPet(KPetModuleConfig.getNoviceGuidePetTemplate());
			ps.addTemporaryPet(pet);
			ps.setFightingPet(pet);
		}
	}
	
	@Override
	public void removeNoviceGuideFightingPet(KRole role) {
		KPetSet ps = this.getPetSet(role.getId());
		if (ps.getFightingPet() != null && ps.getPet(ps.getFightingPet().getId()) == null) {
			ps.setFightingPet(null);
		}
	}
	
	@Override
	public void notifyPetComposeIncChange(long roleId) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role != null && role.isOnline()) {
			String composeIncStr = KSupportFactory.getGangSupport().getGangEffectDescr(KGangTecTypeEnum.随从合成经验加成, roleId);
			KGameMessage msg = KGame.newLogicMessage(SM_NOTIFY_COMPOSE_INC_CHANGE);
			msg.writeUtf8String(composeIncStr);
			KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
		}
	}
	
	@Override
	public ICombatPet createFightingPet(int templateId) {
		KPet pet = new KPet(this.getPetTemplate(templateId));
		pet.setTempId(_idGenerator.incrementAndGet());
		return pet;
	}
	
	@Override
	public Map<KGameAttrType, Integer> calcualteAttrs(KPetTemplate template, int lv, int starLv, int growthValue) {
		KPetAttrPara attrDeviPara = KPetModuleManager.getAttrDeviPara(template.attributeDeviation);
		KPetAttrPara lvPara = KPetModuleManager.getLvAttrPara(lv);
		KPetAttrPara starPara = KPetModuleManager.getStarAttrPara(starLv);
		float lvProportion = KPetModuleConfig.getLvProportion();
		float starProportion = KPetModuleConfig.getStarLvProportion();
		int maxHp;
		int atk;
		int def;
		int hitRating;
		int dodgeRating;
		int critRating;
		int resilienceRating;
		int faintResistRating;
		int defIgnore;
		if (starPara != null) {
			maxHp = calculateBoth(KGameAttrType.MAX_HP, attrDeviPara.maxHpPara, lvPara.maxHpPara, starPara.maxHpPara, growthValue, lvProportion, starProportion);
			atk = calculateBoth(KGameAttrType.ATK, attrDeviPara.atkPara, lvPara.atkPara, starPara.atkPara, growthValue, lvProportion, starProportion);
			def = calculateBoth(KGameAttrType.DEF, attrDeviPara.defPara, lvPara.defPara, starPara.defPara, growthValue, lvProportion, starProportion);
			hitRating = calculateBoth(KGameAttrType.HIT_RATING, attrDeviPara.hitRatingPara, lvPara.hitRatingPara, starPara.hitRatingPara, growthValue, lvProportion, starProportion);
			dodgeRating = calculateBoth(KGameAttrType.DODGE_RATING, attrDeviPara.dodgeRatingPara, lvPara.dodgeRatingPara, starPara.dodgeRatingPara, growthValue, lvProportion, starProportion);
			critRating = calculateBoth(KGameAttrType.CRIT_RATING, attrDeviPara.critRatingPara, lvPara.critRatingPara, starPara.critRatingPara, growthValue, lvProportion, starProportion);
			resilienceRating = calculateBoth(KGameAttrType.RESILIENCE_RATING, attrDeviPara.resilienceRatingPara, lvPara.resilienceRatingPara, starPara.resilienceRatingPara, growthValue, lvProportion, starProportion);
			faintResistRating = calculateBoth(KGameAttrType.FAINT_RESIST_RATING, attrDeviPara.faintResistRatingPara, lvPara.faintResistRatingPara, starPara.faintResistRatingPara, growthValue, lvProportion, starProportion);
			defIgnore = calculateBoth(KGameAttrType.DEF_IGNORE, attrDeviPara.defIgnoreParaPara, lvPara.defIgnoreParaPara, starPara.defIgnoreParaPara, growthValue, lvProportion, starProportion);
		} else {
			maxHp = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.maxHpPara, lvPara.maxHpPara, growthValue, lvProportion));
			atk = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.atkPara, lvPara.atkPara, growthValue, lvProportion));
			def = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.defPara, lvPara.defPara, growthValue, lvProportion));
			hitRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.hitRatingPara, lvPara.hitRatingPara, growthValue, lvProportion));
			dodgeRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.dodgeRatingPara, lvPara.dodgeRatingPara, growthValue, lvProportion));
			critRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.critRatingPara, lvPara.critRatingPara, growthValue, lvProportion));
			resilienceRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.resilienceRatingPara, lvPara.resilienceRatingPara, growthValue, lvProportion));
			faintResistRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.faintResistRatingPara, lvPara.faintResistRatingPara, growthValue, lvProportion));
			defIgnore = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.defIgnoreParaPara, lvPara.defIgnoreParaPara, growthValue, lvProportion));
		}
		hitRating += KGameGlobalConfig.getBasicHitRating();
		Map<KGameAttrType, Integer> map = new HashMap<KGameAttrType, Integer>();
		map.put(KGameAttrType.MAX_HP, maxHp);
		map.put(KGameAttrType.ATK, atk);
		map.put(KGameAttrType.DEF, def);
		map.put(KGameAttrType.HIT_RATING, hitRating);
		map.put(KGameAttrType.DODGE_RATING, dodgeRating);
		map.put(KGameAttrType.CRIT_RATING, critRating);
		map.put(KGameAttrType.RESILIENCE_RATING, resilienceRating);
		map.put(KGameAttrType.FAINT_RESIST_RATING, faintResistRating);
		map.put(KGameAttrType.DEF_IGNORE, defIgnore);
		return map;
	}
}
