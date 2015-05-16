package com.kola.kmp.logic.other;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kola.kgame.cache.util.UtilTool;

/**
 * 
 * @author PERRY CHAN
 */
public enum KGameAttrType {

		MAX_HP					(101, "生命", "生命上限", true, false),
		MAX_ENERGY				(102, "怒气上限", "怒气上限", true, false),
		HP						(103, "生命", "当前生命", false, false),
		ENERGY					(104, "怒气", "当前气力", false, false),
		HP_RECOVERY				(105, "生命恢复速度", "生命恢复速度", true, false),
		ATK						(106, "攻击", "攻击力", true, false),
		DEF						(107, "防御", "物防", true, false),
		HIT_RATING				(108, "命中", "命中等级", true, false),
		CRIT_RATING				(109, "暴击", "暴击等级", true, false),
		DODGE_RATING			(110, "闪避", "闪避等级", true, false),
		RESILIENCE_RATING		(111, "暴击抵抗", "抗爆等级", true, false),
		CRIT_MULTIPLE			(112, "暴击加成", "暴击加成", false, true),
		CD_REDUCE				(113, "冷却缩减", "冷却缩减", false, true),
		HP_ABSORB				(114, "生命吸取", "生命吸取", false, true),
		FAINT_RESIST_RATING		(115, "眩晕抵抗", "眩晕抵抗", false, false),
		DEF_IGNORE				(116, "无视防御", "无视防御", false, false),
		SHORT_RA_ATK_ITR		(117, "近程攻击速度", "近程攻击速度", false, false),
		LONG_RA_ATK_ITR			(118, "远程攻击速度", "远程攻击速度", false, false),
		MOVE_SPEED_X			(119, "移动速度x", "移动速度", false, false),
		MOVE_SPEED_Y			(120, "移动速度y", "移动速度", false, false),
		BATTLE_MOVE_SPEED_X		(121, "战斗移动速度x", "战斗移动速度", false, false),
		BATTLE_MOVE_SPEED_Y		(122, "战斗移动速度y", "战斗移动速度", false, false),
		SHORT_RA_ATK_DIST		(123, "近程攻击距离", "近程攻击距离", false, false),
		LONG_RA_ATK_DIST		(124, "远程攻击距离", "远程攻击距离", false, false),
		BLOCK					(125, "格挡值", "格挡值", false, false), // 装备专用
		COHESION_DM				(126, "聚力伤害", "聚力伤害", false, false), // 聚力伤害，比例，万分比
		SHOT_DM_PCT				(127, "子弹伤害", "子弹伤害", false, false), // 子弹的伤害，比例，万分比
		COHESION_FIXED			(128, "聚力追加伤害", "聚力追加伤害", false, false), // 聚力追加的伤害，固定值
		BASIC_ATTR_INC			(129, "基础属性加成", "基础属性加成", false, false), // 角色基础属性加成，比例，万分比
		SKILL_DM_INC			(130, "技能伤害加成", "技能伤害加成", false, false), // 技能伤害比例加成，比例，万分比
		DAMAGE_REDUCTION		(131, "伤害减免", "伤害减免", false, false), // 伤害减免，比例，万分比
		ACTION_SPEED			(132, "动作速度", "动作速度", false, false),
		
		MAX_HP_PCT				(201, "生命", "生命上限万分比", false, true),
		MAX_ENERGY_PCT			(202, "怒气上限", "怒气上限万分比", false, true),
		HP_PCT					(203, "生命", "生命百分比", false, true),
		ENERGY_PCT				(204, "怒气", "怒气百分比", false, true),	
		HP_RECOVERY_PCT			(205, "生命恢复", "生命恢复万分比", false, true),
		ATK_PCT					(206, "攻击", "攻击力万分比", false, true),
		DEF_PCT					(207, "防御", "防御力万分比", false, true),
		HIT_RATING_PCT			(208, "命中", "命中万分比", false, true),
		CRIT_RATING_PCT			(209, "暴击", "暴击万分比", false, true),
		DODGE_RATING_PCT		(210, "闪避", "闪避万分比", false, true),
		RESILIENCE_RATING_PCT	(211, "暴击抵抗", "抗爆等级万分比", false, true),
		SHORT_RA_ATK_ITR_PCT	(217, "近程攻击速度", "近程攻击速度万分比", false, true),
		LONG_RA_ATK_ITR_PCT 	(218, "近程攻击速度", "近程攻击速度万分比", false, true),
		
		BATTLE_POWER			(300, "战斗力", "战斗力", false, false),
//		BATTLE_POWER_TOTAL		(301, "总战斗力", "总战斗力", false, false),
		EXPERIENCE				(302, "经验值", "经验值", false, false),
		
		PHY_POWER				(401, "体力", "体力", false, false),
		XIUWEI					(402, "修为", "修为", false, false),
		;
		
		private static final String _FORMAT = "%s+%s";
		public static final KGameAttrType[] DISPLAY_FOR_PET;
		public static final KGameAttrType[] DISPLAY_FOR_ROLE;
		public static final KGameAttrType[] BASIC_ATTRIBUTE_ARRAY = new KGameAttrType[] { MAX_HP, ATK, DEF };
		private static final Map<Integer, KGameAttrType> _ALL_ATTR = new HashMap<Integer, KGameAttrType>();
		private static final Map<String, KGameAttrType> _ALL_ATTR_BY_DESC = new HashMap<String, KGameAttrType>();
		private static final Map<KGameAttrType, KGameAttrType> _srcTypeOfPercentage = new HashMap<KGameAttrType, KGameAttrType>();
		static {
			
			resetString();
			
			KGameAttrType[] allTypes = KGameAttrType.values();
			KGameAttrType type;
			for (int i = 0; i < allTypes.length; i++){
				type = allTypes[i];
				_ALL_ATTR.put(type.sign, type);
				_ALL_ATTR_BY_DESC.put(type.getRealName(), type);
			}
			List<KGameAttrType> list = new ArrayList<KGameAttrType>();
			list.add(MAX_HP);
			list.add(ATK);
			list.add(DEF);
			list.add(HIT_RATING);
			list.add(CRIT_RATING);
			list.add(DODGE_RATING);
			list.add(RESILIENCE_RATING);
			list.add(FAINT_RESIST_RATING);
			list.add(DEF_IGNORE);
			list.add(MOVE_SPEED_X);
			list.add(MOVE_SPEED_Y);
			list.add(BATTLE_POWER);
			
			DISPLAY_FOR_PET = new KGameAttrType[list.size()];
			list.toArray(DISPLAY_FOR_PET);
			
//			list.add(BATTLE_POWER_TOTAL);
			list.add(BLOCK);
			list.add(COHESION_DM);
			list.add(SHOT_DM_PCT);
			DISPLAY_FOR_ROLE = new KGameAttrType[list.size()];
			list.toArray(DISPLAY_FOR_ROLE);
			MAX_HP.bindPercentageType(MAX_HP_PCT);
			MAX_ENERGY.bindPercentageType(MAX_ENERGY_PCT);
			ATK.bindPercentageType(ATK_PCT);
			DEF.bindPercentageType(DEF_PCT);
			HP_RECOVERY.bindPercentageType(HP_RECOVERY_PCT);
			CRIT_RATING.bindPercentageType(CRIT_RATING_PCT);
			HIT_RATING.bindPercentageType(HIT_RATING_PCT);
			DODGE_RATING.bindPercentageType(DODGE_RATING_PCT);
			RESILIENCE_RATING.bindPercentageType(RESILIENCE_RATING_PCT);
			SHORT_RA_ATK_ITR.bindPercentageType(SHORT_RA_ATK_ITR_PCT);
			LONG_RA_ATK_ITR.bindPercentageType(LONG_RA_ATK_ITR_PCT);
			for(int i = 0; i < allTypes.length; i++){
				type = allTypes[i];
				if(type.hasPercentage && type._percentageType == null){
					throw new RuntimeException("类型[" + type._realName + "]拥有百分比的属性,但是百分比属性为null");
				}
			}
		}
		
		/**
		 * <pre>
		 * 当字符串被重新映射时，通过本方法重设MAP数据
		 * 
		 * @author CamusHuang
		 * @creation 2013-11-27 下午1:17:35
		 * </pre>
		 */
		public static void resetString(){
			_ALL_ATTR_BY_DESC.clear();
			
			KGameAttrType[] allTypes = KGameAttrType.values();
			KGameAttrType type;
			for (int i = 0; i < allTypes.length; i++){
				type = allTypes[i];
				_ALL_ATTR_BY_DESC.put(type.getRealName(), type);
			}
		}
		
		
		public final int sign;
		private String _name;
		private String _extName;// 染色的属性名称
		private String _realName;
		public final boolean hasPercentage;
		public final boolean isPercentageType;
		private KGameAttrType _percentageType;
		
		private KGameAttrType(int pSign, String pName, String pRealName, boolean pHasPercentage, boolean pIsPercentageType){
			this.sign = pSign;
			this._name = pName;
			this._extName = _name;//by camus: 主策要求属性名不带颜色 HyperTextTool.extColor(_name, KColorFunEnum.属性);
			this._realName = pRealName;
			this.hasPercentage = pHasPercentage;
			this.isPercentageType = pIsPercentageType;
		}
		
		private void bindPercentageType(KGameAttrType pType) {
			this._percentageType = pType;
			_srcTypeOfPercentage.put(pType, this);
		}
		
		public String getRealName(){
			return this._realName;
		}
		
		public KGameAttrType getPercentageType(){
			return _percentageType;
		}
		
		public String getValueDesc(int value) {
			if (isPercentageType) {
				return String.format(_FORMAT, this._name, String.valueOf(UtilTool.getTenThousandFormat(value)));
			} else {
				return String.format(_FORMAT, this._name, String.valueOf(value));
			}
		}
		
		public String getName() {
			return _name;
		}
		
		/**
		 * <pre>
		 * 染色的属性名称
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2014-4-1 下午4:28:14
		 * </pre>
		 */
		public String getExtName() {
			return _extName;
		}		

		public static KGameAttrType getAttrTypeEnum(int type){
			return _ALL_ATTR.get(type);
		}
		
		public static KGameAttrType getAttrTypeEnumByDesc(String desc){
			return _ALL_ATTR_BY_DESC.get(desc);
		}
		
		public static KGameAttrType getSrcAttrTypeOfPercentage(KGameAttrType pType) {
			return _srcTypeOfPercentage.get(pType);
		}
}
