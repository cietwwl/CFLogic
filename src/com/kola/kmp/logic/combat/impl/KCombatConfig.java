package com.kola.kmp.logic.combat.impl;

import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatConfig {

	private static int _normalAtkDmMinPercentage = 95; // 普通攻击伤害最低的百分比，这里是分子
	private static int _normalAtkDmMaxPercentage = 105; // 普通攻击伤害最高的百分比，这里是分子
	private static int _normalAtkDmSubPercentage; // 最高与最低攻击百分比的差值
	private static int _maxCritMultiple = 30000; // 最大的暴击比例（万分比）
	private static int _superSkillConsumeEnergyBeanCount = 1; // 超杀技能所需要的怒气豆数量
	private static int _lowHitRating = 6000; // 算出来的命中率少于这个，会用备用公式算命中
	private static float _lowHitRatingPara = 2.5f; // 低于60%命中时，命中计算公式：Math.min(Math.max((1+(命中等级-闪避等级)/10000)/_lowHitRatingPara, 0), 0.6)
	private static int _energyBeanCountOfMountSummoning = 1; // 每次召唤机甲需要的怒气豆数量
	private static int _competitionHpMultiple = 10; // 竞技场hp的倍数
	private static int _normalAtkMinDmFormularPctMin = 3; // 最小伤害计算公式，最少百分比，ceiling(攻击力*random(_normalAtkMinDmFormularPctMin,_normalAtkMinDmFormularPctMax)/100)
	private static int _normalAtkMinDmFormularPctMax = 8; // 最小伤害计算公式，最少百分比，ceiling(攻击力*random(_normalAtkMinDmFormularPctMin,_normalAtkMinDmFormularPctMax)/100)
	private static int _cohesionDmMin;
	private static int _ctnAtkMillis;
	private static int _offlineCombatMaxLv;
	private static int _maxMultipleOfCompetition; // 竞技场战力差，最大的属性倍数
	private static float _calParaOfCompetition;
	private static float _calParaOfTeamPVP;
	private static int _minLevelOfAutoFight;
	private static float _hitRatingBasicPara;
	private static int _hitRatingFixPara;
	private static float _lowHitRatingCalPara;
	private static float _highHitRatingCalPara;
//	private static int[] _incEnergyCtnAtkCountList;
	private static int _energyCalPara;
	private static int _energyIncCountEachTime;
	
	static void loadConfig(String path) throws Exception{
		KGameExcelFile file = new KGameExcelFile(path);
		KGameExcelRow[]  allRows = file.getTable("战斗参数配置", 2).getAllDataRows();
		ReflectPaser.parseExcel(KCombatConfig.class, allRows);
		_normalAtkDmSubPercentage = _normalAtkDmMaxPercentage - _normalAtkDmMinPercentage + 1;
	}
	
	/**
	 * 
	 * 获取普通攻击伤害最低的百分比
	 * 
	 * @return
	 */
	public static int getNormalAtkDmMinPercentage() {
		return _normalAtkDmMinPercentage;
	}
	
	/**
	 * 
	 * 获取普通攻击伤害最高的百分比
	 * 
	 * @return
	 */
	public static int getNormalAtkDmMaxPercentage() {
		return _normalAtkDmMaxPercentage;
	}
	
	/**
	 * 
	 * 获取普通攻击伤害百分比的差值，用于random
	 * 
	 * @return
	 */
	public static int getNormalAtkDmSubPercentage() {
		return _normalAtkDmSubPercentage;
	}
	
	/**
	 * 
	 * 获取最大的暴击倍数
	 * 
	 * @return
	 */
	public static int getMaxCritMultiple() {
		return _maxCritMultiple;
	}

	/**
	 * 
	 * 获取超杀技能需要消耗的怒气豆数量
	 * 
	 * @return
	 */
	public static int getSuperSkillConsumeEnergyBeanCount() {
		return _superSkillConsumeEnergyBeanCount;
	}
	
	/**
	 * 
	 * 获取命中临界值
	 * 
	 * @return
	 */
	public static int getLowHitRating() {
		return _lowHitRating;
	}

	/**
	 * 
	 * 获取低命中率时，备用公式的计算参数
	 * 
	 * @return
	 */
	public static float getLowHitRatingPara() {
		return _lowHitRatingPara;
	}

	/**
	 * 
	 * 获取召唤机甲所需要的怒气豆数量
	 * 
	 * @return
	 */
	public static int getEnergyBeanCountOfMountSummoning() {
		return _energyBeanCountOfMountSummoning;
	}

	/**
	 * 
	 * 获取竞技场HP扩大的血量倍数
	 * 
	 * @return
	 */
	public static int getCompetitionHpMultiple() {
		return _competitionHpMultiple;
	}

	/**
	 * 
	 * 最小伤害计算公式，最少百分比
	 * 
	 * @return
	 */
	public static int getNormalAtkMinDmFormularPctMin() {
		return _normalAtkMinDmFormularPctMin;
	}

	/**
	 * 
	 * 最小伤害计算公式，最大百分比
	 * 
	 * @return
	 */
	public static int getNormalAtkMinDmFormularPctMax() {
		return _normalAtkMinDmFormularPctMax;
	}

	/**
	 * 
	 * 获取忍者聚力伤害最少的万分比
	 * 
	 * @return
	 */
	public static int getCohesionDmMin() {
		return _cohesionDmMin;
	}

	/**
	 * 
	 * 连击之间的间隔（毫秒）
	 * 
	 * @return
	 */
	public static int getCtnAtkMillis() {
		return _ctnAtkMillis;
	}

	/**
	 * 
	 * 离线战斗的最大等级（不包含边界值）
	 * 
	 * @return
	 */
	public static int getOfflineCombatMaxLv() {
		return _offlineCombatMaxLv;
	}

	public static int getMaxMultipleOfCompetition() {
		return _maxMultipleOfCompetition;
	}

	public static float getCalParaOfCompetition() {
		return _calParaOfCompetition;
	}

	public static float getCalParaOfTeamPVP() {
		return _calParaOfTeamPVP;
	}

	public static int getMinLevelOfAutoFight() {
		return _minLevelOfAutoFight;
	}

	public static float getHitRatingBasicPara() {
		return _hitRatingBasicPara;
	}

	public static int getHitRatingFixPara() {
		return _hitRatingFixPara;
	}

	public static float getHighHitRatingCalPara() {
		return _highHitRatingCalPara;
	}
	
	public static float getLowHitRatingCalPara() {
		return _lowHitRatingCalPara;
	}
	
//	public static boolean isProduceEnergy(int ctnAtkCount) {
//		for (int i = 0; i < _incEnergyCtnAtkCountList.length; i++) {
//			if (_incEnergyCtnAtkCountList[i] == ctnAtkCount) {
//				return true;
//			}
//		}
//		return false;
//	}
	
	public static int getEnergyCalPara() {
		return _energyCalPara;
	}

	public static int getEnergyIncCountEachTime() {
		return _energyIncCountEachTime;
	}
	
}
