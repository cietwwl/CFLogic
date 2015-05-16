package com.kola.kmp.logic.util.tips;

import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.pet.KPetModuleConfig;

/**
 * 
 * @author PERRY CHAN
 */
public class PetTips {

	private static String _tipsPetSetIsFull;
	private static String _tipsNoSuchPetTemplate;
	private static String _tipsNoSuchPet;
	private static String _tipsPetNameLengthNotPass;
	private static String _tipsPetNameContainsDirtyWord;
	private static String _tipsPetChgNameIsCoolingDown;
	private static String _tipsModifyNameSuccess;
	private static String _tipsPetIsStrongerThanRole;
	private static String _tipsSwallowMoneyNotEnough;
	private static String _tipsPetWillStrongerThanRole;
	private static String _tipsConfirmOverflow;
	private static String _tipsPetFlowSuccess;
	private static String _tipsPetCancelFlowSuccess;
	private static String _tipsMaxGrowValueFlag;
	private static String _tipsPetIsMaxLv;
	private static String _tipsConfirmContainSenior;
	private static String _tipsSwallowPets;
	private static String _tipsAddExpDescr;
	private static String _tipsSwallowPetsResult;
	private static String _tipsGangTechInc;
	private static String _tipsActivityInc;
	private static String _tipsNameFormatNotIllegal;
	private static String _tipsNoSuchGetWay;
	private static String _tipsFuncNotOpen;
	private static String _tipsConfirmSetFree;
	private static String _tipsSetFreeSuccess;
	private static String _tipsSetFreeMailTitle;
	private static String _tipsSetFreeMailContent;
	private static String _tipsCannotSetFreeFightingPet;
	
	public static void initComplete() {
		_tipsPetNameLengthNotPass = StringUtil.format(_tipsPetNameLengthNotPass, KPetModuleConfig.getPetNameLengthMin(), KPetModuleConfig.getPetNameLengthMax());
	}
	
	public static String getTipsPetSetIsFull() {
		return _tipsPetSetIsFull;
	}
	
	public static String getTipsNoSuchPetTemplate() {
		return _tipsNoSuchPetTemplate;
	}

	public static String getTipsNoSuchPet() {
		return _tipsNoSuchPet;
	}

	public static String getTipsPetNameLengthNotPass() {
		return _tipsPetNameLengthNotPass;
	}

	public static String getTipsPetNameContainsDirtyWord() {
		return _tipsPetNameContainsDirtyWord;
	}

	public static String getTipsPetChgNameIsCoolingDown() {
		return _tipsPetChgNameIsCoolingDown;
	}

	public static String getTipsModifyNameSuccess() {
		return _tipsModifyNameSuccess;
	}

	public static String getTipsPetIsStrongerThanRole() {
		return _tipsPetIsStrongerThanRole;
	}

	public static String getTipsSwallowMoneyNotEnough(String name, int money) {
		return StringUtil.format(_tipsSwallowMoneyNotEnough, name, money);
	}

	public static String getTipsPetWillStrongerThanRole() {
		return _tipsPetWillStrongerThanRole;
	}

	public static String getTipsPetFlowSuccess() {
		return _tipsPetFlowSuccess;
	}

	public static String getTipsPetCancelFlowSuccess() {
		return _tipsPetCancelFlowSuccess;
	}

	public static String getTipsMaxGrowValueFlag() {
		return _tipsMaxGrowValueFlag;
	}

	public static String getTipsPetIsMaxLv(String name) {
		return StringUtil.format(_tipsPetIsMaxLv, name);
	}

	public static String getTipsConfirmContainSenior() {
		return _tipsConfirmContainSenior;
	}

	public static String getTipsSwallowPets() {
		return _tipsSwallowPets;
	}

	public static String getTipsAddExpDescr(int value, int prelv, int nowlv, String reason) {
		return StringUtil.format(_tipsAddExpDescr, value, prelv, nowlv, reason);
	}

	public static String getTipsSwallowPetsResult(int exp, int actualExp) {
		return StringUtil.format(_tipsSwallowPetsResult, exp, actualExp);
	}

	public static String getTipsGangTechInc(int exp) {
		return StringUtil.format(_tipsGangTechInc, exp);
	}

	public static String getTipsActivityInc(int exp) {
		return StringUtil.format(_tipsActivityInc, exp);
	}

	public static String getTipsNameFormatNotIllegal() {
		return _tipsNameFormatNotIllegal;
	}

	public static String getTipsNoSuchGetWay() {
		return _tipsNoSuchGetWay;
	}

	public static String getTipsFuncNotOpen() {
		return _tipsFuncNotOpen;
	}

	public static String getTipsConfirmOverflow() {
		return _tipsConfirmOverflow;
	}

	public static String getTipsConfirmSetFree(String petName) {
		return StringUtil.format(_tipsConfirmSetFree, petName);
	}

	public static String getTipsSetFreeSuccess(String petName) {
		return StringUtil.format(_tipsSetFreeSuccess, petName);
	}

	public static String getTipsSetFreeMailTitle() {
		return _tipsSetFreeMailTitle;
	}

	public static String getTipsSetFreeMailContent(String petName) {
		return StringUtil.format(_tipsSetFreeMailContent, petName);
	}

	public static String getTipsCannotSetFreeFightingPet() {
		return _tipsCannotSetFreeFightingPet;
	}
}
