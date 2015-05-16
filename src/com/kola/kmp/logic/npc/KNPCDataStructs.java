package com.kola.kmp.logic.npc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.combat.api.ICombatDropInfoTemplate;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KMonsterType;
import com.kola.kmp.logic.other.KObstructionTargetType;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;

public class KNPCDataStructs {

	/**
	 * <pre>
	 * 怪物模板
	 * 
	 * @author CamusHuang
	 * @creation 2014-2-21 下午3:09:18
	 * </pre>
	 */
	public static class KNPCTemplate {
		// ----------以下是EXCEL表格直导数据---------
		public int templateId;// 模板ID
		public int instanceId;// 实体ID
		public String name;// 名称
		public int level;// NPC等级
		private String talkabout1;// 闲时对话内容1
		private String talkabout2;// 闲时对话内容2
		private String talkabout3;// 闲时对话内容3
		public int talkHeadUI;// 对话框半身像
		public int taskHeadUI;// 任务追踪头像
		public int talkact;// 对话动作
		public int actionAIScript;// 行为AI
		public int bubblesRuleId;// 聊天泡泡规则

		// ----------以下是逻辑数据---------
		public List<String> talkabouts;

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
			talkabouts = new ArrayList<String>();
			if (talkabout1 != null && !talkabout1.isEmpty()) {
				talkabouts.add(talkabout1);
			}
			if (talkabout2 != null && !talkabout2.isEmpty()) {
				talkabouts.add(talkabout2);
			}
			if (talkabout3 != null && !talkabout3.isEmpty()) {
				talkabouts.add(talkabout3);
			}
			if (talkabouts.isEmpty()) {
				talkabouts = Collections.emptyList();
			} else {
				talkabouts = Collections.unmodifiableList(talkabouts);
			}
			// CTODO 其它约束检查
		}
	}

	/**
	 * <pre>
	 * 怪物模板
	 * 
	 * @author CamusHuang
	 * @creation 2014-2-21 下午3:09:18
	 * </pre>
	 */
	public static class KMonstTemplate {

		// ----------以下是EXCEL表格直导数据---------
		public int id;// 怪物id号
		public String name;// 怪物名字
		public int monster_type;// 怪物类型
		public int qua;// 怪物品质
		public int lvl;// 怪物等级
//		public int is_armor;// 是否霸体
		public int armortime;
		public int armorinterval;
		public int walk_speed;// 移动速度
		public int per_anger;// 杀死增加的怒气
		private int[] attribute_id;// 怪物属性编号
		private long[] attribute;// 怪物属性数量
		public int atk_type;// 怪物攻击类型
		public float atk_period;// 攻击间隔
		public String skill;// 怪物技能
		public float cast_skill_rol;// 施放技能概率
		public float atk_probality;// 攻击概率
		public float follow_probality;// 追击概率
		public int vision;// 视野
		public int changing_round_x;// 乱逛范围x
		public int changing_round_y;// 乱逛范围y
		public int monstUITempId;// 怪物形象模板ID
		public String aiid;// ai编号

		// ----------以下是逻辑数据---------
		public MonstUIData monstUIData;// 怪物形象模板
		public Map<KGameAttrType, Long> allEffects = Collections.emptyMap();// LinkedHashMap
		public List<ICombatSkillData> combatSkills;
		private boolean _fullImmunity;
		private KMonsterType _monster_type_enum; // 怪物类型（枚举）
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

			monstUIData = KNPCDataManager.mMonstUIDataManager.getData(monstUITempId);
			if (monstUIData == null) {
				throw new Exception("怪物形象模板不存在 monstUITempId=" + monstUITempId + "，怪物模板id=" + id);
			}
			{
				Map<KGameAttrType, Long> tempEffects = null;
				try {
					if (attribute_id.length != attribute.length) {
						throw new Exception("属性id与属性值的长度不一致！");
					}
					tempEffects = new LinkedHashMap<KGameAttrType, Long>();
					for (int index = 0; index < attribute_id.length; index++) {
						if (attribute_id[index] < 1) {
							continue;
						}
						KGameAttrType attrType = KGameAttrType.getAttrTypeEnum(attribute_id[index]);
						if (attrType == null) {
							throw new Exception("模板属性类型不存在 type=" + attribute_id[index]);
						}
						tempEffects.put(attrType, attribute[index]);
					}
				} catch (Exception e) {
					throw new Exception("生成怪物属性时出现异常，怪物id=" + id, e);
				}
				if (tempEffects.isEmpty()) {
					throw new Exception("怪物形象模板属性未配置 id=" + id);
				}
				allEffects = Collections.unmodifiableMap(tempEffects);
			}
			List<ICombatSkillData> tempList = new ArrayList<ICombatSkillData>();
			String[] skillStrs = skill.split(";");
			if (skillStrs.length > 0 && skillStrs[0].trim().length() > 0) {
				for (int i = 0; i < skillStrs.length; i++) {
					tempList.add(new KCombatMonsterSkill(Integer.parseInt(skillStrs[i].split(":")[0]), 1));
				}
			}
			this.combatSkills = Collections.unmodifiableList(tempList);
			this._monster_type_enum = KMonsterType.getTypeEnum(monster_type);
			this.armortime *= 1000;
			this.armorinterval *= 1000;
			this._fullImmunity = this.armortime > 0;
			// CTODO 其它约束检查

		}
		
		public KMonsterType getMonsterTypeEnum() {
			return this._monster_type_enum;
		}
		
		public boolean isFullImmunity() {
			return this._fullImmunity;
		}
	}

	/**
	 * <pre>
	 * 怪物属性
	 * 与怪物模板一一对象
	 * 
	 * @author CamusHuang
	 * @creation 2014-2-21 下午3:08:51
	 * </pre>
	 */
	public static class MonstUIData {
		// ----------以下是EXCEL表格直导数据---------
		public int monstUITempId;// 怪物形象模板ID
		public String name;// 怪物名称
		public String doc;// 怪物描述
		public float actionperiod;// 行动反应时间
		public float up_time;// 倒地起身时间(s)
		public int atk_hoot;// 叫嚣动作
		public int res_id;// 美术资源
		public int monster_img;// 怪物半身像
		public int monster_head;// 怪物头像
		public int attack_audios;// 普通攻击音效
		public int hitted_scream_audios;// 受伤喊叫声
		public int dead_audio;// 死亡声
		public int hitted_audios;// 击中声
		public int att_range;// 攻击距离
		public int atkCountPerTime; // 单次普通攻击产生的伤害次数

		// ----------以下是逻辑数据---------

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
			// CTODO 其它约束检查

		}
	}

	/**
	 * <pre>
	 * 战场障碍物数据模版
	 * 
	 * @author CamusHuang
	 * @creation 2014-2-21 下午3:08:51
	 * </pre>
	 */
	public static class ObstructionTemplate {
		// ----------以下是EXCEL表格直导数据---------
		public int id;// 障碍物编号
		public String name;// 障碍物名称
		public int[] attribute_id;//陷阱属性编号
		public int[] attribute;//陷阱属性数量
		public int directstatus_id;// 直接造成状态ID
		public int destroy;// 如何摧毁
		public int destroystatus_id;// 死亡触发的技能
		public int probability;//场景中出现的几率（万分比）
		public int target;// 影响目标
//		public int dropid;// 掉落ID
		public String dropid;
		public int attack;// 是否可以被攻击
		public int col;// 是否有碰撞
		public int inMapResId;// 场景资源id
		public int headResId;// 头像资源id
		private Map<Map<Integer, Integer>, Integer> _dropMap;
		private List<Integer> _totalRate;
//		private int _totalRate;
		public int attack_audios; 
		public int hitted_scream_audios;
		public int dead_audio;
		public int hitted_audios;
		
		public int[] attack_audios_array;
		public int[] hitted_scream_audios_array;
		public int[] dead_audio_array;
		public int[] hitted_audios_array;


		// ----------以下是逻辑数据---------
		public Map<KGameAttrType, Integer> allEffects;
		
		private int getSingleDropId(Map<Integer, Integer> map, int totalRate) {
			if (map.size() == 1) {
				return map.keySet().iterator().next();
			} else {
				Map.Entry<Integer, Integer> entry;
				int ran = UtilTool.random(totalRate);
				for (Iterator<Map.Entry<Integer, Integer>> itr = map.entrySet().iterator(); itr.hasNext();) {
					entry = itr.next();
					if (ran < entry.getValue()) {
						return entry.getKey();
					}
				}
				return 0;
			}
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
			// CTODO 其它约束检查
			KObstructionTargetType type = KObstructionTargetType.getEnum(target);
			if (type == null) {
				throw new IllegalArgumentException("未知的障碍物目标类型：" + target);
			}
			Map<KGameAttrType, Integer> tempEffects = null;
			try {
				tempEffects = KGameUtilTool.genAttribute(attribute_id, attribute, true);
			} catch (Exception e) {
				throw new Exception("生成障碍物属性时出现异常，障碍物id=" + id, e);
			}

			if (tempEffects.isEmpty()) {
				throw new Exception("障碍物形象模板属性未配置 id=" + id);
			}
			allEffects = Collections.unmodifiableMap(tempEffects);
			String[] allDropInfo = dropid.split(";");
			String[] dropInfo;
			String[] singleDropInfo;
			Map<Map<Integer, Integer>, Integer> tempDropMap = new HashMap<Map<Integer, Integer>, Integer>();
			int totalRate;
			Map<Integer, Integer> tempMap;
			if (allDropInfo.length > 0 && allDropInfo[0].length() > 0 && !allDropInfo[0].equals("0")) {
				for (int i = 0; i < allDropInfo.length; i++) {
					totalRate = 0;
					dropInfo = allDropInfo[i].split(",");
					tempMap = new LinkedHashMap<Integer, Integer>();
					for (int k = 0; k < dropInfo.length; k++) {
						singleDropInfo = dropInfo[k].split("\\*");
						if (singleDropInfo.length > 1) {
							totalRate += Integer.parseInt(singleDropInfo[1]);
							tempMap.put(Integer.parseInt(singleDropInfo[0]), totalRate);
						} else {
							totalRate = UtilTool.TEN_THOUSAND_RATIO_UNIT;
							tempMap.put(Integer.parseInt(singleDropInfo[0]), UtilTool.TEN_THOUSAND_RATIO_UNIT);
						}
					}
					tempDropMap.put(Collections.unmodifiableMap(tempMap), totalRate);
				}
			}
			_dropMap = Collections.unmodifiableMap(tempDropMap);
			attack_audios_array = new int[] { attack_audios };
			hitted_scream_audios_array = new int[] { hitted_scream_audios };
			dead_audio_array = new int[] { dead_audio };
			hitted_audios_array = new int[] { hitted_audios };
		}
		
		List<Integer> getRandomDropId() {
			if (this._dropMap.isEmpty()) {
				return Collections.emptyList();
			} else {
				List<Integer> rtnList = new ArrayList<Integer>();
				Map.Entry<Map<Integer, Integer>, Integer> entry;
				for (Iterator<Map.Entry<Map<Integer, Integer>, Integer>> itr = _dropMap.entrySet().iterator(); itr.hasNext();) {
					entry = itr.next();
					rtnList.add(this.getSingleDropId(entry.getKey(), entry.getValue()));
				}
				return rtnList;
			}
		}
	}
	
	public static class KDropInfoTemplate implements ICombatDropInfoTemplate {
		private int dropId;
		private int resId;
		private byte dropType;
		private int dropRate;
		private int gold;
		private String droppet;
		private String script;
		private KDropItemData[][] _items;
		private int[] _totalRates;
		public Map<Integer, Integer> _pets;
		private int dropStateId;
		private int dropHp;
		private int dropEnergy;
		
		void onGameWorldInitComplete() {
			List<KDropItemData> tempList;
			if (script != null && script.length() > 0) {
				String[] allDropItems = script.split("#");
				List<KDropItemData[]> dropList = new ArrayList<KDropItemData[]>();
				List<Integer> rateList = new ArrayList<Integer>();
				if (allDropItems.length > 0 && allDropItems[0].length() > 0) {
					String[] dropItems;
					String[] tempItem;
					int rate;
					for (int i = 0; i < allDropItems.length; i++) {
						rate = 0;
						tempList = new ArrayList<KDropItemData>();
						dropItems = allDropItems[i].split(",");
						for (int k = 0; k < dropItems.length; k++) {
							tempItem = dropItems[k].split("\\*");
							rate += Integer.parseInt(tempItem[2]);
							tempList.add(new KDropItemData(tempItem[0], Integer.parseInt(tempItem[1]), rate));
						}
						dropList.add(tempList.toArray(new KDropItemData[tempList.size()]));
						rateList.add(rate);
					}
					this._items = new KDropItemData[dropList.size()][];
					this._totalRates = new int[rateList.size()];
					dropList.toArray(this._items);
					for (int i = 0; i < _totalRates.length; i++) {
						this._totalRates[i] = rateList.get(i);
					}
				}
				this.script = null;
			}
			if (droppet != null && droppet.length() > 0) {
				String[] petData = droppet.split("\\*");
				Map<Integer, Integer> map = new HashMap<Integer, Integer>();
				if (petData.length > 0 && petData[0].length() > 0) {
					map.put(Integer.parseInt(petData[0]), Integer.parseInt(petData[1]));
				}
				_pets = Collections.unmodifiableMap(map);
				this.droppet = null;
			} else {
				_pets = Collections.emptyMap();
			}
			
			if (this._items != null) {
				KDropItemData[] temp;
				for (int i = 0; i < _items.length; i++) {
					temp = _items[i];
					for (int k = 0; k < temp.length; k++) {
						if (KSupportFactory.getItemModuleSupport().getItemTemplate(temp[k].itemCode) == null) {
							throw new RuntimeException("不存在道具编号：" + temp[k] + "，掉落id是：" + this.dropId);
						}
					}
				}
			}
			if (this._pets.size() > 0) {
				Integer templateId;
				for (Iterator<Integer> itr = _pets.keySet().iterator(); itr.hasNext();) {
					templateId = itr.next();
					if (KSupportFactory.getPetModuleSupport().getPetTemplate(templateId) == null) {
						throw new RuntimeException("不存在随从模板编号：" + templateId + "，掉落id是：" + this.dropId);
					}
				}
			}
			if(this.dropStateId > 0) {
				if (KSupportFactory.getSkillModuleSupport().getStateTemplate(dropStateId) == null) {
					throw new RuntimeException("不存在状态模板：" + dropStateId + "，掉落id是：" + this.dropId);
				}
			}
			switch(this.dropType) {
			case DROP_TYPE_ITEM:
				if(this.gold <= 0 && (this._items == null || this._items.length == 0)) {
					throw new RuntimeException("掉落id：" + this.dropId + "，至少须符合金币数量>0或者掉落道具数据>0的条件");
				}
				break;
			case DROP_TYPE_HP:
				if(this.dropHp <= 0) {
					throw new RuntimeException("掉落id：" + this.dropId + "，须符合HP>0的条件");
				}
				break;
			case DROP_TYPE_PET:
				if(this._pets.isEmpty()) {
					throw new RuntimeException("掉落id：" + this.dropId + "，须符合随从数量>0的条件");
				}
				break;
			case DROP_TYPE_BUFF:
				if(this.dropStateId <= 0) {
					throw new RuntimeException("掉落id：" + this.dropId + "，须符合状态id>0的条件");
				}
				break;
			case DROP_TYPE_ENERGY:
				if (this.dropEnergy <= 0) {
					throw new RuntimeException("掉落id：" + this.dropId + "，须符合怒气>0的条件");
				}
				break;
			default:
				throw new RuntimeException("不能识别的掉落类型，掉落id：" + this.dropId);
			}
		}
		
		public Map<String, Integer> getDropItems() {
			if (_items != null) {
				Map<String, Integer> map = new HashMap<String, Integer>();
				int tempTotalRate;
				KDropItemData[] tempDropItems;
				int actualRate;
				KDropItemData tempData;
				for (int i = 0; i < _items.length; i++) {
					tempTotalRate = _totalRates[i];
					tempDropItems = _items[i];
					actualRate = UtilTool.random(tempTotalRate);
					for (int k = 0; k < tempDropItems.length; k++) {
						tempData = tempDropItems[k];
						if (actualRate < tempData.rate) {
							map.put(tempData.itemCode, tempData.count);
							break;
						}
					}
				}
				return map;
			} else {
				return Collections.emptyMap();
			}
		}
		
		public Map<Integer, Integer> getDropPets() {
			return _pets;
		}
		
		public int getDropGold() {
			return gold;
		}

		public int getDropId() {
			return dropId;
		}

		public int getResId() {
			return resId;
		}

		@Override
		public byte getDropType() {
			return dropType;
		}

		@Override
		public boolean willDrop() {
			return UtilTool.random(UtilTool.TEN_THOUSAND_RATIO_UNIT) < dropRate;
		}

		@Override
		public int getDropStateId() {
			return dropStateId;
		}

		@Override
		public int getDropHp() {
			return dropHp;
		}
		
		@Override
		public int getDropEnergy() {
			return dropEnergy;
		}
	}
	
	public static class KDropItemData {
		public final String itemCode;
		public final int count;
		public final int rate;
		
		/**
		 * @param itemCode
		 * @param count
		 * @param rate
		 */
		public KDropItemData(String pItemCode, int pCount, int pRate) {
			this.itemCode = pItemCode;
			this.count = pCount;
			this.rate = pRate;
		}
		
		
	}
	
	public static class KCombatMonsterSkill implements ICombatSkillData {


		private int _skillTemplateId;
		private int _lv;
		KCombatMonsterSkill(int pSkillTemplateId, int lv) {
			this._skillTemplateId = pSkillTemplateId;
			this._lv = lv;
		}
		
		@Override
		public int getSkillTemplateId() {
			return _skillTemplateId;
		}

		@Override
		public int getLv() {
			return _lv;
		}

		@Override
		public boolean isSuperSkill() {
			return false;
		}
		
		@Override
		public boolean onlyEffectInPVP() {
			return false;
		}
		
	}
}
