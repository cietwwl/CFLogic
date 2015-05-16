package com.kola.kmp.logic.pet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.npc.KNPCDataStructs.MonstUIData;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KPetAtkType;
import com.kola.kmp.logic.other.KPetGetWay;
import com.kola.kmp.logic.other.KPetQuality;
import com.kola.kmp.logic.other.KPetType;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KPetTemplate implements ITransferable {

	public final int templateId;
	public final String defaultName;
	public final KPetAtkType atkType;
	public final KPetType type;
	public final byte attributeDeviation; // 属性偏向
	public final KPetQuality quality; // 品质
	public final int maxLv;
	public final int growMin;
	public final int growMax;
	public final int atkPeriod; // 攻击间隔
	public final int critMultiple; // 暴击倍数
	public final int moveSpeed; // 移动速度
	public final int vision;
	public final int basicComposeExp; // 基础合成经验
	public final boolean canBeAutoSelected;
	public final String aiId;
	public final int level;
	public final int monsterUITemplateId;
	public final int fullImmunityDuration;
	public final int fullImmunityIterval;
	public final int starLvUpRate; // 升星成功率（当作为材料被吞噬的时候）
	public final int starLvUpRateHundred; // 升星成功率（百分比）
//	private int _swallowFee; // 吞噬费用
	private int _upgradeExp;
	private String _defaultNameEx;
	public final Map<Integer, Integer> skillMap; // key=skillTemplateId, value=几率
	public final Map<KPetGetWay, String> getWayMap;
	public final Map<ItemCountStruct, Integer> itemsForSetFree;
	public final boolean willWorldBroadcast;
	public final boolean showInHandbook;
	
	private int _atkRange;
	private int _headResId; // 头像资源id
	private int _inMapResId; // 地图资源id
	private int[] _normalAtkAudios; // 普通攻击声效
	private int[] _hittedAudios; // 击中声效
	private int[] _injuryAudios; // 受伤声效
	private int[] _deadAudios; // 死亡声效
	private int _atk;
	private int _def;
	private int _maxHp;
	private int _hitRating;
	private int _dodgeRating;
	private int _critRating;
	private int _resilienceRating;
	private int _faintResistRating;
	private int _defIgnore;
	private int _atkCountPerTime;
	private List<IPetSkill> _skills; // 展示的技能列表，默认是全部技能都展示
	
	public KPetTemplate(KGameExcelRow row) {
		this.templateId = row.getInt("templateId");
		this.defaultName = row.getData("name");
		this.atkType = KPetAtkType.getPetAtkType(row.getInt("atkType"));
		this.type = KPetType.getPetType(row.getInt("type"));
		this.quality = KPetQuality.getEnumQuality(row.getInt("quality"));
		this.attributeDeviation = row.getByte("attributeDeviation");
		this.canBeAutoSelected = row.getBoolean("canBeAutoSelected");
		this.maxLv = row.getInt("maxLv");
		this.growMin = row.getInt("growMin");
		this.growMax = row.getInt("growMax");
//		this.headResId = row.getInt("headResId");
//		this.inMapResId = row.getInt("inMapResId");
		this.atkPeriod = Math.round(row.getFloat("atkPeriod") * 1000); // 本来是秒，这里改为毫秒
		this.critMultiple = row.getInt("critMultiple");
		this.monsterUITemplateId = row.getInt("monsterUITemplateId");
		this.moveSpeed = row.getInt("move");
		this.vision = row.getInt("vision");
		this.basicComposeExp = row.getInt("comptarexp");
		this.fullImmunityDuration = row.getIntSafely("armortime") * 1000; // 转化为毫秒
		this.fullImmunityIterval = row.getIntSafely("armorinterval") * 1000; // 转化为毫秒
//		this.normalAtkAudios = UtilTool.getStringToIntArray(row.getData("attack_audios"), ",");
//		this.hittedAudios = UtilTool.getStringToIntArray(row.getData("hitted_audios"), ",");
//		this.injuryAudios = UtilTool.getStringToIntArray(row.getData("hitted_scream_audios"), ",");
//		this.deadAudios = UtilTool.getStringToIntArray(row.getData("dead_audio"), ",");
		this.starLvUpRate = row.getInt("successRate");
		this.starLvUpRateHundred = starLvUpRate / 100;
		this.aiId = row.getData("model_id");
		Map<Integer, Integer> tempSkillMap = new LinkedHashMap<Integer, Integer>();
		boolean flag = true;
		int index = 1;
		String skillColName = "skillId";
		String skillProName = "skillPro";
		String nowSkillColName;
		String nowSkillProName;
		while (flag) {
			nowSkillColName = skillColName + index;
			nowSkillProName = skillProName + index;
			if (row.containsCol(nowSkillColName)) {
				int skillId = row.getIntSafely(nowSkillColName);
				if (skillId > 0) {
					tempSkillMap.put(skillId, row.getInt(nowSkillProName));
				}
				index++;
			} else {
				break;
			}
		}
		this.skillMap = Collections.unmodifiableMap(tempSkillMap);
		this.level = 1;
		this._defaultNameEx = quality.formatPetName(defaultName);
		if(this.atkType == null) {
			throw new RuntimeException("不存在指定的攻击类型：" + row.getInt("atkType") + "，模板id：" + templateId);
		}
		String[] getWays = row.getData("typeOfGet").split(";");
		if(getWays.length > 0 && getWays[0].length() > 0) {
			Map<KPetGetWay, String> map = new HashMap<KPetGetWay, String>();
			KPetGetWay way;
			String script;
			for (int i = 0; i < getWays.length; i++) {
				String[] temp = getWays[i].split(",");
				if (temp.length > 0 && temp[0].length() > 0) {
					way = KPetGetWay.getEnum(Integer.parseInt(temp[0]));
					if (way == null) {
						throw new RuntimeException("未知的获取方式：" + temp[0]);
					}
					switch (way) {
					case WAY_COMMON_WISH:
						script = "1"; // 客户端约定，0是普通扭蛋
						break;
					case WAY_SENIOR_WISH:
						script = "2"; // 客户端约定，1是高级扭蛋
						break;
					default:
						if (temp.length > 1) {
							script = temp[1];
						} else {
							script = "";
						}
						break;
					}
					map.put(way, script);
				}
			}
			getWayMap = Collections.unmodifiableMap(map);
		} else {
			getWayMap = Collections.emptyMap();
		}
		this.willWorldBroadcast = row.getBoolean("willWorldBroadcast");
		this.showInHandbook = row.getBoolean("showInHandbook");
		String[] items = row.getData("itemsForSetFree").split(";");
		String[] singleItem;
		Map<ItemCountStruct, Integer> map = new HashMap<ItemCountStruct, Integer>();
		if (items.length > 0 && items[0].trim().length() > 0) {
			for (int i = 0; i < items.length; i++) {
				singleItem = items[i].split(",");
				ItemCountStruct itemCount = new ItemCountStruct(singleItem[0], Integer.parseInt(singleItem[1]));
				map.put(itemCount, Integer.parseInt(singleItem[2]));
			}
		}
		itemsForSetFree = Collections.unmodifiableMap(map);
	}
	
	void onGameWorldInitComplete() {
		this._upgradeExp = KPetModuleManager.getUpgradeExp(quality, this.level);
		KPetAttrPara attrDeviPara = KPetModuleManager.getAttrDeviPara(attributeDeviation);
		KPetAttrPara lvPara = KPetModuleManager.getLvAttrPara(level);
		float lvProportion = KPetModuleConfig.getLvProportion();
		this._maxHp = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.maxHpPara, lvPara.maxHpPara, growMin, lvProportion));
		this._atk = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.atkPara, lvPara.atkPara, growMin, lvProportion));
		this._def = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.defPara, lvPara.defPara, growMin, lvProportion));
		this._hitRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.hitRatingPara, lvPara.hitRatingPara, growMin, lvProportion));
		this._dodgeRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.dodgeRatingPara, lvPara.dodgeRatingPara, growMin, lvProportion));
		this._critRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.critRatingPara, lvPara.critRatingPara, growMin, lvProportion));
		this._resilienceRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.resilienceRatingPara, lvPara.resilienceRatingPara, growMin, lvProportion));
		this._faintResistRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.faintResistRatingPara, lvPara.faintResistRatingPara, growMin, lvProportion));
		this._defIgnore = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.defIgnoreParaPara, lvPara.defIgnoreParaPara, growMin, lvProportion));
		MonstUIData monsterUIData = KSupportFactory.getNpcModuleSupport().getMonsterUIData(monsterUITemplateId);
		if(monsterUIData==null){
			throw new RuntimeException("宠物表 monsterUITemplateId错误=" + monsterUITemplateId + "，模板id：" + templateId);
		}
		if(type == null) {
			throw new RuntimeException("宠物表 type错误，模板id：" + templateId);
		}
		for (Iterator<Map.Entry<KPetGetWay, String>> itr = getWayMap.entrySet().iterator(); itr.hasNext();) {
			Map.Entry<KPetGetWay, String> entry = itr.next();
			switch (entry.getKey()) {
			case WAY_COMMON_COPY:
			case WAY_ELITE_COPY:
				if (entry.getValue() == null || entry.getValue().length() == 0) {
					throw new RuntimeException("宠物表获取类型参数错误，获取类型=" + entry.getKey().sign + "，应该包含关卡id的参数！模板id：" + templateId);
				}
				break;
			default:
				break;
			}
		}
		this._inMapResId = monsterUIData.res_id;
		this._headResId = monsterUIData.monster_head;
		this._atkRange = monsterUIData.att_range;
		this._normalAtkAudios = new int[]{monsterUIData.attack_audios};
		this._hittedAudios = new int[]{monsterUIData.hitted_audios};
		this._injuryAudios = new int[]{monsterUIData.hitted_scream_audios};
		this._deadAudios = new int[]{monsterUIData.dead_audio};
		this._atkCountPerTime = monsterUIData.atkCountPerTime;
		if (skillMap.size() > 0) {
			List<IPetSkill> list = new ArrayList<IPetSkill>();
			Map.Entry<Integer, Integer> entry;
			for (Iterator<Map.Entry<Integer, Integer>> itr = skillMap.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				if(KSupportFactory.getSkillModuleSupport().getPetSkillTemplate(entry.getKey()) == null && KSupportFactory.getSkillModuleSupport().getPetPasSkillTemplate(entry.getKey()) == null) {
					throw new RuntimeException("不存在宠物技能：" + entry.getKey() + ",模板id：" + this.templateId);
				}
				list.add(new KPetSkillImplOfTemplate(entry.getKey(), entry.getValue()));
			}
			this._skills = Collections.unmodifiableList(list);
		} else {
			this._skills = Collections.emptyList();
		}
		Map.Entry<ItemCountStruct, Integer> entry;
		for (Iterator<Map.Entry<ItemCountStruct, Integer>> itr = itemsForSetFree.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			if (KSupportFactory.getItemModuleSupport().getItemTemplate(entry.getKey().itemCode) == null) {
				throw new RuntimeException(StringUtil.format("不存在遣散道具：{}，随从模板id：{}", entry.getKey().itemCode, templateId));
			} else if (entry.getKey().itemCount <= 0) {
				throw new RuntimeException(StringUtil.format("遣散道具数量不合法：{}，随从模板id：{}", entry.getKey().itemCount, templateId));
			}
		}
		
	}
	
	/**
	 * 
	 * 获取带颜色的默认名字
	 * 
	 * @return
	 */
	public String getNameEx() {
		return _defaultNameEx;
	}

	@Override
	public int getTemplateId() {
		return templateId;
	}

	@Override
	public int getHeadResId() {
		return _headResId;
	}

	@Override
	public int getInMapResId() {
		return _inMapResId;
	}

	@Override
	public String getName() {
		return defaultName;
	}

	@Override
	public int getLevel() {
		return level;
	}
	
	@Override
	public int getMaxLevel() {
		return maxLv;
	}
	
	@Override
	public int getGrowValue() {
		return growMax;
	}
	
	@Override
	public int getMaxGrowValue() {
		return growMax;
	}

	@Override
	public int getCurrentExp() {
		return 0;
	}

	@Override
	public int getUpgradeExp() {
		return _upgradeExp;
	}

	@Override
	public int getBeComposedExp() {
		return basicComposeExp;
	}

	@Override
	public int getSwallowFee() {
		return 0;
	}

	@Override
	public KPetQuality getQuality() {
		return quality;
	}

	@Override
	public KPetType getPetType() {
		return type;
	}

	@Override
	public KPetAtkType getAtkType() {
		return atkType;
	}

	@Override
	public int getAttributeByType(KGameAttrType type) {
		switch (type) {
		case ATK:
			return _atk;
		case DEF:
			return _def;
		case MAX_HP:
			return _maxHp;
		case HIT_RATING:
			return _hitRating;
		case DODGE_RATING:
			return _dodgeRating;
		case CRIT_RATING:
			return _critRating;
		case RESILIENCE_RATING:
			return _resilienceRating;
		case FAINT_RESIST_RATING:
			return _faintResistRating;
		case DEF_IGNORE:
			return _defIgnore;
		case MOVE_SPEED_X:
			return moveSpeed;
		case BATTLE_POWER:
			return 0;
		default:
			return 0;
		}
	}

//	@Override
//	public int getOpenSkillCount() {
//		return 0;
//	}

	@Override
	public List<IPetSkill> getSkillList() {
		return _skills;
	}
	
	@Override
	public int getStarLv() {
		return 0;
	}
	
	@Override
	public Map<KGameAttrType, Integer> getAttrOfStar() {
		return Collections.emptyMap();
	}
	
	@Override
	public int getStarLvUpRateHundred() {
		return starLvUpRateHundred;
	}

	@Override
	public boolean isCanBeAutoSelected() {
		return canBeAutoSelected;
	}

	public int getAtkRange() {
		return _atkRange;
	}

	public int[] getHittedAudios() {
		return _hittedAudios;
	}

	public int[] getInjuryAudios() {
		return _injuryAudios;
	}

	public int[] getDeadAudios() {
		return _deadAudios;
	}

	public int[] getNormalAtkAudios() {
		return _normalAtkAudios;
	}
	
	public int getAtkCountPerTime() {
		return _atkCountPerTime;
	}
	
	private static class KPetSkillImplOfTemplate implements IPetSkill {

		private int _templateId;
		private byte _type; // 主动或被动
		private boolean _activeSkill;
		private int _lvUpRate;
		private int _rate;
//		private int _skillType;
		
		public KPetSkillImplOfTemplate(int pTemplateId, int rate) {
			this._templateId = pTemplateId;
			KRoleIniSkillTemp skillTemplate = KSupportFactory.getSkillModuleSupport().getPetSkillTemplate(_templateId);	
			if (skillTemplate != null && skillTemplate.isIniSkill) {
				_type = SKILL_TYPE_ACTIVE;
				_lvUpRate = skillTemplate.lvUpRate;
			} else {
				_type = SKILL_TYPE_PASSIVE;
				KRolePasSkillTemp pasSkillTemplate = KSupportFactory.getSkillModuleSupport().getPetPasSkillTemplate(_templateId);
				_lvUpRate = pasSkillTemplate.lvUpRate;
			}
			_activeSkill = _type == SKILL_TYPE_ACTIVE;
			this._rate = rate / UtilTool.HUNDRED_RATIO_UNIT;
		}
		@Override
		public int getSkillTemplateId() {
			return _templateId;
		}

		@Override
		public int getLv() {
			return 1;
		}

		@Override
		public boolean isSuperSkill() {
			return false;
		}

		@Override
		public boolean onlyEffectInPVP() {
			return false;
		}

		@Override
		public byte getType() {
			return _type;
		}

		@Override
		public boolean isActiveSkill() {
			return _activeSkill;
		}

		@Override
		public int getLvUpRate() {
			return _lvUpRate;
		}

		@Override
		public boolean isMaxLv() {
			return true;
		}
		
		@Override
		public int getRate() {
			return _rate;
		}
		
	}
}
