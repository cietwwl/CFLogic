package com.kola.kmp.logic.skill;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.combat.api.ICombatSkillTemplateData;
import com.kola.kmp.logic.combat.state.ICombatStateTemplate;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;

public class KSkillDataStructs {

	/**
	 * <pre>
	 * 技能模板抽象类
	 * 主动技能请参考{@link KRoleIniSkillTemp}
	 * 被动技能请参考{@link KRolePasSkillTemp}
	 * 
	 * @author CamusHuang
	 * @creation 2014-3-26 上午11:01:26
	 * </pre>
	 */
	public static class KRoleSkillTempAbs {
		// ----------以下是EXCEL表格直导数据---------
		public int id;// id号
		public String name;// 技能名称
		public String mark;// 备注
		public String state;// 技能描述
		public int skilltype;// 技能类型
		public int skills_goal;// 技能作用目标
		private int[] canshu1;// 技能描述参数1
		private int[] canshu2;// 技能描述参数2
		private int[] canshu3;// 技能描述参数3
		private int[] canshu4;// 技能描述参数4
		private int[] canshu5;// 技能描述参数5
		public int skill_icon;// 技能图标
		public int max_lvl;// 最高等级
		private int[] prev_id;// 每等级
		public int job;// 需求职业
		private int[] learn_lvl;// 需要等级
		public int Currency1;// 货币1类型
		public int Currency2;// 货币2类型
		private int[] need_Currency1;// 货币1需求量（金币或其它）
		private int[] need_Currency2;// 货币2需求量(潜能)
		public int count1;// 战斗力计算参数1
		public float count2;// 战斗力计算参数2
		public int lvUpRate; // 技能升级概率
		public boolean onlyEffectInPVP;

		// ----------以下是逻辑数据---------
		public boolean isIniSkill;// 是否主动技能
		private boolean isForUpLv;// 是否角色升级自动加技能
		/** 各等级数据[0]表示1级 */
		public List<SkillTempLevelData> skillLevelDatas;

		/**
		 * <pre>
		 * 获取指定的等级数据
		 * 
		 * @param lv
		 * @return
		 * @author CamusHuang
		 * @creation 2014-3-12 下午3:24:38
		 * </pre>
		 */
		public SkillTempLevelData getLevelData(int lv) {
			int index = lv - 1;
			if (index >= skillLevelDatas.size()) {
				return null;
			}
			return skillLevelDatas.get(index);
		}

		/**
		 * <pre>
		 * 是否角色升级自动加技能
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2014-6-12 下午5:08:08
		 * </pre>
		 */
		public boolean isAddForRoleUpLv() {
			return isForUpLv;
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-2-18 下午6:01:47
		 * </pre>
		 */
		void onGameWorldInitComplete() throws Exception {

			// 验证填表数组长度
			// excuteFields.add("status_id");// 排除检查此属性
			KSkillDataManager.checkArrayLenth(this, max_lvl, Collections.<String> emptySet());

			if (KCurrencyTypeEnum.getEnum(Currency1) == null) {
				Currency1 = KCurrencyTypeEnum.GOLD.sign;
			}
			if (KCurrencyTypeEnum.getEnum(Currency2) == null) {
				Currency2 = KCurrencyTypeEnum.POTENTIAL.sign;
			}

			// 初始化等级数据
			skillLevelDatas = new ArrayList<SkillTempLevelData>();
			for (int i = 0; i < max_lvl; i++) {
				skillLevelDatas.add(new SkillTempLevelData(this, i + 1));
			}

			if (skillLevelDatas.get(0).learnLvMoneys.isEmpty()) {
				isForUpLv = true;
			}
		}

		/**
		 * <pre>
		 * 技能模板中每一等级的数据
		 * 
		 * @author CamusHuang
		 * @creation 2014-3-12 下午3:01:33
		 * </pre>
		 */
		public static class SkillTempLevelData implements ICombatSkillTemplateData {
			// ----------以下是EXCEL表格直导数据---------
			public int skillLv;// 技能等级
			// <技能描述参数>按参数顺序保存
			public List<Integer> canshu;
			public int learn_lvl;// 学习要求角色等级

			// ----------以下是逻辑数据---------
			// 技能升级消耗的货币打包[金币、潜能]
			public List<KCurrencyCountStruct> learnLvMoneys;
			private int _skillTemplateId; // 涛新加：技能模板id
			private int _skillType; // 涛新加：技能类型

			private SkillTempLevelData(KRoleSkillTempAbs temp, int lv) {
				int index = lv - 1;
				this._skillTemplateId = temp.id;
				this._skillType = temp.skilltype;
				skillLv = temp.prev_id[index];
				canshu = new ArrayList<Integer>();
				canshu.add(temp.canshu1[index]);
				canshu.add(temp.canshu2[index]);
				canshu.add(temp.canshu3[index]);
				canshu.add(temp.canshu4[index]);
				canshu.add(temp.canshu5[index]);
				//
				learn_lvl = temp.learn_lvl[index];
				//
				List<KCurrencyCountStruct> learnLvMoney = new ArrayList<KCurrencyCountStruct>();
				if (temp.need_Currency1[index] > 0) {
					learnLvMoney.add(new KCurrencyCountStruct(KCurrencyTypeEnum.getEnum(temp.Currency1), temp.need_Currency1[index]));
				}
				if (temp.need_Currency2[index] > 0) {
					learnLvMoney.add(new KCurrencyCountStruct(KCurrencyTypeEnum.getEnum(temp.Currency2), temp.need_Currency2[index]));
				}
				if (learnLvMoney.isEmpty()) {
					learnLvMoneys = Collections.emptyList();
				} else {
					learnLvMoneys = learnLvMoney;
				}
				//
			}

			@Override
			public int getSkillTemplateId() {
				return _skillTemplateId;
			}

			@Override
			public int getSkillType() {
				return _skillType;
			}

			@Override
			public int getSkillLv() {
				return skillLv;
			}

			@Override
			public List<Integer> getSkillArgs() {
				return canshu;
			}
		}
	}

	/**
	 * <pre>
	 * 角色主动技能模板
	 * 
	 * @author CamusHuang
	 * @creation 2014-2-21 下午3:10:02
	 * </pre>
	 */
	public static class KRoleIniSkillTemp extends KRoleSkillTempAbs {

		/** 技能类型：普通技能 */
		public static final int COMMON_SKILL = 0;
		/** 技能类型：超杀技能 */
		public static final int SUPER_SKILL = 1;

		// ----------以下是EXCEL表格直导数据---------
		public int fireAudio;
		public int animation_index;// 动作资源索引
		public int is_commonskill;// 是否是普通技能（1是超杀技能）
		public float cast_time1;// 施法时间
		public int cast_range_x;// 施法距离x
		public int cast_range_y;// 施法距离y
		public String preview; // 预览资源路径

		// ----------以下是逻辑数据---------
		public boolean isSuperSkill;// 是否超杀技能

		/**
		 * <pre>
		 * 
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-2-18 下午6:01:47
		 * </pre>
		 */
		void onGameWorldInitComplete() throws Exception {
			super.isIniSkill = true;
			super.onGameWorldInitComplete();
			isSuperSkill = is_commonskill == SUPER_SKILL;
		}

	}

	/**
	 * <pre>
	 * 角色被动技能模板
	 * 
	 * @author CamusHuang
	 * @creation 2014-2-21 下午3:10:02
	 * </pre>
	 */
	public static class KRolePasSkillTemp extends KRoleSkillTempAbs {

		public static final int OpenType_init = 0;// 按等级
		public static final int OpenType_tianfu = 1;// 天赋

		// ----------以下是EXCEL表格直导数据---------
		public int skill_id;// 资源索引
		public int open_type;// 开启类型
		@KImmunityOfArrayCheck
		private int[][] incAttrKeys; // 第一维表示增加的属性数量；第二维表示增加属性的键值
		@KImmunityOfArrayCheck
		private int[][] incAttrValues; // 第一维表示增加的属性数量；第二维表示增加属性的值

		// ----------以下是逻辑数据---------
		// <技能等级,<属性类型,属性值>>
		public Map<Integer, Map<KGameAttrType, Integer>> allEffects = Collections.emptyMap();

		/**
		 * <pre>
		 * 是否角色升级自动加技能
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2014-6-12 下午5:08:08
		 * </pre>
		 */
		public boolean isAddForRoleUpLv() {
			boolean isForUpLv = super.isAddForRoleUpLv();
			return isForUpLv && open_type == OpenType_init;
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-2-18 下午6:01:47
		 * </pre>
		 */
		void onGameWorldInitComplete(boolean isForRole) throws Exception {
			super.isIniSkill = false;
			super.onGameWorldInitComplete();

			if (skilltype == 1) {
				//参考ICombatSkillTemplateData.java
//				/** 被动技能类型：增加属性 */
//				public static final int PASSIVE_SKILL_INCREASE_ATTR = 1;
//				/** 被动技能类型：添加特效 */
//				public static final int PASSIVE_SKILL_SPECIAL_EFFECT = 2;
//				/** 被动技能类型：出战添加状态 */
//				public static final int PASSIVE_SKILL_PVP_ADD_STATE = 3;
				if (incAttrKeys.length != incAttrValues.length) {
					throw new KGameServerException("属性键值的数量和属性值的数量不匹配！技能id={}" + id);
				}
				for (int i = 0; i < incAttrKeys.length; i++) {
					if (incAttrKeys[i].length < max_lvl) {
						throw new KGameServerException("属性键值[" + incAttrKeys[i][0] + "]的等级数量与等级不匹配！最大等级：" + max_lvl + "，属性的数量：" + incAttrKeys[i].length);
					}
					if (incAttrKeys[i].length != incAttrValues[i].length) {
						throw new KGameServerException("属性键值[" + incAttrKeys[i][0] + "]的等级数量与数值的数量不匹配！");
					}
				}
				allEffects = new HashMap<Integer, Map<KGameAttrType, Integer>>();
				KGameAttrType type;
				int value;
				int lvIndex;
				for (int lv = 1; lv <= max_lvl; lv++) {
					lvIndex = lv - 1;
					Map<KGameAttrType, Integer> map = new HashMap<KGameAttrType, Integer>();
					allEffects.put(lv, map);
					for (int i = 0; i < incAttrKeys.length; i++) {
						type = KGameAttrType.getAttrTypeEnum(incAttrKeys[i][lvIndex]);
						if (type == null) {
							throw new KGameServerException("被动技能属性不存在 技能ID=" + id);
						}
						
						if(isForRole){
							if(open_type == OpenType_init){
								if(type.isPercentageType){
									if(KGameAttrType.getSrcAttrTypeOfPercentage(type)==null){
										throw new KGameServerException("被动技能属性比例值必须有对应绝对值类型 技能ID=" + id);
									}
								}
							}
						}
						
						value = incAttrValues[i][lvIndex];
						if (value < 0) {
							throw new KGameServerException("被动技能属性数值 技能ID=" + id);
						}
						map.put(type, value);
					}
				}
			}
		}
	}

	/**
	 * <pre>
	 * 技能状态数据
	 * 
	 * @author CamusHuang
	 * @creation 2014-2-21 下午3:10:11
	 * </pre>
	 */
	public static class SkillStatusData implements ICombatStateTemplate {
		// ----------以下是EXCEL表格直导数据---------
		public int status_id;// id号
		public String name;// 状态名称
		public String state;// 状态描述
		public int level;// 状态等级
		public int groupId;// 状态组
		public int skilltype;// 技能类型
		public int canshu1;// 状态描述参数1
		public int canshu2;// 状态描述参数2
		public int canshu3;// 状态描述参数3
		public int canshu4;// 状态描述参数4
		public int canshu5;// 状态描述参数5
		public int skill_icon;// 状态图标
		public int skill_id;// 资源索引

		// ----------以下是逻辑数据---------
		private int[] _paras; // 把参数串成一个数组

		/**
		 * <pre>
		 * 
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-2-18 下午6:01:47
		 * </pre>
		 */
		void onGameWorldInitComplete() throws Exception {
			this._paras = new int[] { canshu1, canshu2, canshu3, canshu4, canshu5 };
		}

		@Override
		public int getStateTemplateId() {
			return status_id;
		}

		@Override
		public String getStateName() {
			return name;
		}

		@Override
		public String getStateDesc() {
			return state;
		}

		@Override
		public int getStateType() {
			return skilltype;
		}

		@Override
		public int[] getParas() {
			return _paras;
		}

		@Override
		public int getStateIcon() {
			return skill_icon;
		}

		@Override
		public int getResId() {
			return skill_id;
		}

		@Override
		public int getGroupId() {
			return groupId;
		}

		@Override
		public int getLevel() {
			return level;
		}
	}

	/**
	 * 
	 * <pre>
	 * 被动技能增加了一个二维数组，为了避免KSkillDataManager.checkArrayLenth中的检查
	 * 所以添加了这个注解，凡是有这个注解的数组，在KSkillDataManager.checkArrayLenth都可以跳过检查
	 * </pre>
	 * 
	 * @author PERRY
	 * 
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface KImmunityOfArrayCheck {

	}
}
