package com.kola.kmp.logic.pet;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.pet.PetBaseImpl;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.KGameGlobalConfig;
import com.kola.kmp.logic.combat.api.ICombatPet;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.combat.api.ICombatSkillSupport;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KPetAtkType;
import com.kola.kmp.logic.other.KPetQuality;
import com.kola.kmp.logic.other.KPetType;
import com.kola.kmp.logic.pet.message.KPetServerMsgSender;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KBattlePowerCalculator;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.text.HyperTextTool;

/**
 * 
 * @author PERRY CHAN
 */
public class KPet extends PetBaseImpl implements ITransferable, ICombatPet {

	private static final Logger _LOGGER = KGameLogger.getLogger(KPet.class);
	
	private static final String KEY_FIGHTING = "1";
	private static final String KEY_CURRENT_EXP = "2";
	private static final String KEY_LAST_CHG_NAME_TIME = "3";
	private static final String KEY_GROW_VALUE = "4";
	private static final String KEY_STAR_LV = "5";
	private static final String KEY_SKILL_INFO = "skill";
	
	private String _nameEx;
	private KPetQuality _quality;
	private KPetAtkType _atkType;
	private KPetType _type;
	private int _headResId;
	private int _inMapResId;
//	private int _srcAtk;
//	private int _srcDef;
//	private int _srcMaxHp;
//	private int _srcHitRating;
//	private int _srcDodgeRating;
//	private int _srcCritRating;
//	private int _srcResilienceRating;
	private int _basicComposeExp;
	private int _growValue; // 成长值
	private int _starLv; // 星级
	private byte _attrDeviation;
	private int _maxLv;
	private boolean _canBeAutoSelected;
	private int _battlePower;
	private int[] _normalAtkAudios;
	private int[] _hittedAudios;
	private int[] _injuryAudios;
	private int[] _deadAudios;
	private int _atkPeriod;
	private int _critMultiple;
	private int _maxGrowValue;
	private int _fullImmunityDuration;
	private int _fullImmunityIterval;
	private int _atkCountPerTime;
	
	private int _atk; // 攻击力
	private int _def; // 防御力
	private int _maxHp; // 最大生命值
	private int _hitRating; // 命中等级
	private int _showHitRating; // 展示的命中等级（因为显示的命中等级需要-9500）
	private int _dodgeRating; // 闪避等级
	private int _critRating; // 暴击等级
	private int _resilienceRating; // 抗爆等级
	private int _faintResistRating; // 眩晕抵抗等级
	private int _defIgnore; // 无视防御
	private int _speedX; // 移动速度x
	private int _speedY; // 移动速度y
	private int _vision; // 视野
	private int _exp; // 当前经验值
	private int _upgradeExp; // 当前升级经验值
	private int _beComposeExp; // 基础合成经验
	private int _swallowFee; // 吞噬费用
	private int _atkRange; // 攻击距离
	private int _starLvUpRate;
	private int _starLvUpRateHundred;
	
	private String _aiid;
	
	private long _lastChgNameTimeMillis; // 上一次的改名时间（毫秒）
	
	private Map<KGameAttrType, Integer> _effectRoleAttrs = new HashMap<KGameAttrType, Integer>();
	private Map<KGameAttrType, Integer> _effectRoleAttrsRO = Collections.unmodifiableMap(_effectRoleAttrs);
	private AtomicBoolean _fighting = new AtomicBoolean(false);
	private KPetSkillSet _skillSet;
	private final Map<KGameAttrType, Integer> _attrOfStar = new LinkedHashMap<KGameAttrType, Integer>();
	private final Map<KGameAttrType, Integer> _attrOfStarRO = Collections.unmodifiableMap(_attrOfStar);
	
	private KPet(String pName, int pTemplateId) {
		super(pName, pTemplateId);
//		this._skillSet = new KPetSkillSet(this);
		this._skillSet = new KPetSkillSet();
	}
	
	KPet() {
		this(null, 0);
	}
	
	KPet(KPetTemplate template) {
		this(template.defaultName, template.templateId);
		this._growValue = UtilTool.random(template.growMin, template.growMax);
		this.initFromTemplate(template);
		this.handleDynamicAttr();
		this.genEffectRoleAttrs();
//		this._skillSet.setOpenSkillCount(KPetModuleConfig.getPetInitSkillCount());
	}
	
	private void getCommonAttributeFromTemplate(KPetTemplate template) {
		this._headResId = template.getHeadResId();
		this._inMapResId = template.getInMapResId();
		this._quality = template.quality;
		this._atkType = template.atkType;
		this._type = template.type;
//		this._srcAtk = template.atk;
//		this._srcDef = template.def;
//		this._srcMaxHp = template.maxHp;
//		this._srcHitRating = template.hitRating;
//		this._srcDodgeRating = template.dodgeRating;
//		this._srcCritRating = template.critRating;
//		this._srcResilienceRating = template.resilienceRating;
		this._basicComposeExp = template.basicComposeExp;
		this._aiid = template.aiId;
		this._speedX = template.moveSpeed;
		this._speedY = template.moveSpeed / 2;
		this._vision = template.vision;
		this._attrDeviation = template.attributeDeviation;
		this._maxLv = template.maxLv;
		this._canBeAutoSelected = template.canBeAutoSelected;
		this._atkPeriod = template.atkPeriod;
		this._critMultiple = template.critMultiple;
		this._maxGrowValue = template.growMax;
		this._atkCountPerTime = template.getAtkCountPerTime();
		this._atkRange = template.getAtkRange();
		this._fullImmunityDuration = template.fullImmunityDuration;
		this._fullImmunityIterval = template.fullImmunityIterval;
		this._starLvUpRate = template.starLvUpRate;
		this._starLvUpRateHundred = template.starLvUpRateHundred;
		this._normalAtkAudios = Arrays.copyOf(template.getNormalAtkAudios(), template.getNormalAtkAudios().length);
		this._hittedAudios = Arrays.copyOf(template.getHittedAudios(), template.getHittedAudios().length);
		this._injuryAudios = Arrays.copyOf(template.getInjuryAudios(), template.getInjuryAudios().length);
		this._deadAudios = Arrays.copyOf(template.getDeadAudios(), template.getDeadAudios().length);
		this._nameEx = HyperTextTool.extColor(this.getName(), this._quality.getColorEnum());
	}
	
	private void initFromTemplate(KPetTemplate template) {
		this.level = 1;
		this.getCommonAttributeFromTemplate(template);
		Map.Entry<Integer, Integer> entry;
		int rate;
		for(Iterator<Map.Entry<Integer, Integer>> itr = template.skillMap.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			rate = UtilTool.random(UtilTool.TEN_THOUSAND_RATIO_UNIT);
			if(rate < entry.getValue()) {
				_skillSet.addSkill(entry.getKey(), 1);
			}
		}
	}
	
	private Map<KGameAttrType, Integer> copyAttr() {
		Map<KGameAttrType, Integer> map = new HashMap<KGameAttrType, Integer>(9);
		map.put(KGameAttrType.MAX_HP, this._maxHp);
		map.put(KGameAttrType.ATK, this._atk);
		map.put(KGameAttrType.DEF, this._def);
		map.put(KGameAttrType.HIT_RATING, this._hitRating);
		map.put(KGameAttrType.DODGE_RATING, this._dodgeRating);
		map.put(KGameAttrType.CRIT_RATING, this._critRating);
		map.put(KGameAttrType.RESILIENCE_RATING, this._resilienceRating);
		map.put(KGameAttrType.FAINT_RESIST_RATING, this._faintResistRating);
		map.put(KGameAttrType.DEF_IGNORE, this._defIgnore);
		map.put(KGameAttrType.MOVE_SPEED_X, this._speedX);
		map.put(KGameAttrType.MOVE_SPEED_Y, this._speedY);
		return map;
	}
	
	private void calculateAttribute() {
		KPetAttrPara attrDeviPara = KPetModuleManager.getAttrDeviPara(_attrDeviation);
		KPetAttrPara lvPara = KPetModuleManager.getLvAttrPara(level);
		KPetAttrPara starPara = KPetModuleManager.getStarAttrPara(_starLv);
		float lvProportion = KPetModuleConfig.getLvProportion();
		float starProportion = KPetModuleConfig.getStarLvProportion();
		_attrOfStar.clear();
		if (starPara != null) {
			this._maxHp = calculateBoth(KGameAttrType.MAX_HP, attrDeviPara.maxHpPara, lvPara.maxHpPara, starPara.maxHpPara, _growValue, lvProportion, starProportion);
			this._atk = calculateBoth(KGameAttrType.ATK, attrDeviPara.atkPara, lvPara.atkPara, starPara.atkPara, _growValue, lvProportion, starProportion);
			this._def = calculateBoth(KGameAttrType.DEF, attrDeviPara.defPara, lvPara.defPara, starPara.defPara, _growValue, lvProportion, starProportion);
			this._hitRating = calculateBoth(KGameAttrType.HIT_RATING, attrDeviPara.hitRatingPara, lvPara.hitRatingPara, starPara.hitRatingPara, _growValue, lvProportion, starProportion);
			this._dodgeRating = calculateBoth(KGameAttrType.DODGE_RATING, attrDeviPara.dodgeRatingPara, lvPara.dodgeRatingPara, starPara.dodgeRatingPara, _growValue, lvProportion, starProportion);
			this._critRating = calculateBoth(KGameAttrType.CRIT_RATING, attrDeviPara.critRatingPara, lvPara.critRatingPara, starPara.critRatingPara, _growValue, lvProportion, starProportion);
			this._resilienceRating = calculateBoth(KGameAttrType.RESILIENCE_RATING, attrDeviPara.resilienceRatingPara, lvPara.resilienceRatingPara, starPara.resilienceRatingPara, _growValue, lvProportion, starProportion);
			this._faintResistRating = calculateBoth(KGameAttrType.FAINT_RESIST_RATING, attrDeviPara.faintResistRatingPara, lvPara.faintResistRatingPara, starPara.faintResistRatingPara, _growValue, lvProportion, starProportion);
			this._defIgnore = calculateBoth(KGameAttrType.DEF_IGNORE, attrDeviPara.defIgnoreParaPara, lvPara.defIgnoreParaPara, starPara.defIgnoreParaPara, _growValue, lvProportion, starProportion);
		} else {
			this._maxHp = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.maxHpPara, lvPara.maxHpPara, _growValue, lvProportion));
			this._atk = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.atkPara, lvPara.atkPara, _growValue, lvProportion));
			this._def = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.defPara, lvPara.defPara, _growValue, lvProportion));
			this._hitRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.hitRatingPara, lvPara.hitRatingPara, _growValue, lvProportion));
			this._dodgeRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.dodgeRatingPara, lvPara.dodgeRatingPara, _growValue, lvProportion));
			this._critRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.critRatingPara, lvPara.critRatingPara, _growValue, lvProportion));
			this._resilienceRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.resilienceRatingPara, lvPara.resilienceRatingPara, _growValue, lvProportion));
			this._faintResistRating = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.faintResistRatingPara, lvPara.faintResistRatingPara, _growValue, lvProportion));
			this._defIgnore = Math.round(KPetModuleManager.calculateSingle(attrDeviPara.defIgnoreParaPara, lvPara.defIgnoreParaPara, _growValue, lvProportion));
		}
		Map<KGameAttrType, Integer> map = this.copyAttr();
		this._showHitRating = _hitRating;
		this._hitRating += KGameGlobalConfig.getBasicHitRating();
		this._battlePower = KBattlePowerCalculator.calculateBattlePower(map, this.level, true);
	}
	
	private int calculateBoth(KGameAttrType type, int attrDeviPara, int lvPara, int starPara, int growValue, float lvProportion, float starProportion) {
		float lvResult = KPetModuleManager.calculateSingle(attrDeviPara, lvPara, growValue, lvProportion);
		float starResult = KPetModuleManager.calculateSingle(attrDeviPara, starPara, growValue, starProportion);
		if(starResult > 0) {
			this._attrOfStar.put(type, (int) starResult);
		}
		return Math.round(lvResult + starResult);
	}
	
	private void calculateComposeExp() {
		// 计算被吞噬的经验
//		this._beComposeExp = UtilTool.round(_basicComposeExp + (this.level - 1) * 25 * KPetModuleManager.getComposeExpPara(this._quality));
		this._beComposeExp = (int)Math.round(((this.level * (this.level - 1) * 25 + 10 * (1 - Math.pow(1.2, this.level)) / (1 - 1.2))* KPetModuleManager.getComposeExpPara(this._quality) + this._basicComposeExp) * KPetModuleConfig.getExpProportion());
	}
	
	private void handleDynamicAttr() {
		this._upgradeExp = KPetModuleManager.getUpgradeExp(this._quality, this.level);
		this.calculateAttribute();
		this.calculateComposeExp();
		this._swallowFee = KPetModuleManager.getComposeFee(_type, _quality);
	}
	
	private void onLevelUp(int toLv) {
		this.level = toLv;
		this.handleDynamicAttr();
		this.notifyDataChange();
		KPetServerMsgSender.sendPetLevelUpMsg(this.getOwnerId(), this);
		KPetModuleManager.notifyPetInfoChane(this);
//		if (this._fighting.get()) {
//			KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(this.getOwnerId(), KPetModuleRoleAttributeProvider.getType());
//		}
	}
	
	private void notifyExpAdded() {
		this.notifyDataChange();
		KPetServerMsgSender.sendSyncExpToClient(this.getOwnerId(), this);
	}
	
	private void genEffectRoleAttrs() {
		_effectRoleAttrs.clear();
		List<ICombatSkillData> skills = this._skillSet.getPassiveSkills();
		if (skills.size() > 0) {
			ICombatSkillData skill;
			for (int i = 0; i < skills.size(); i++) {
				skill = skills.get(i);
				KRolePasSkillTemp template = KSupportFactory.getSkillModuleSupport().getPetPasSkillTemplate(skills.get(i).getSkillTemplateId());
				KGameUtilTool.combinMap(_effectRoleAttrs, template.allEffects.get(skill.getLv()));
			}
		}
	}
	
	private void checkMissSkill() {
		KPetTemplate template = KPetModuleManager.getPetTemplate(this.getTemplateId());
		List<IPetSkill> skill = _skillSet.getAllSkills();
		Map.Entry<Integer, Integer> entry;
		boolean duplicate;
		for (Iterator<Map.Entry<Integer, Integer>> itr = template.skillMap.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			if (entry.getValue() == UtilTool.TEN_THOUSAND_RATIO_UNIT) {
				duplicate = false;
				for (int i = 0; i < skill.size(); i++) {
					if (skill.get(i).getSkillTemplateId() == entry.getKey()) {
						duplicate = true;
						break;
					}
				}
				if (!duplicate) {
					_skillSet.addSkill(entry.getKey(), 1);
				}
			}
		}
	}
	
	void setTempId(long id) {
		this.setId(id);
	}
	
	void increaseStarLv(int value) {
		if (this._starLv < KPetModuleConfig.getPetMaxStarLv()) {
			this._starLv += value;
			if (this._starLv > KPetModuleConfig.getPetMaxStarLv()) {
				this._starLv = KPetModuleConfig.getPetMaxStarLv();
			}
			this.calculateAttribute();
			this.notifyDataChange();
			KPetServerMsgSender.sendSyncStarLvToClient(this.getOwnerId(), this);
		}
	}
	
	int getStarLvUpRate() {
		return _starLvUpRate;
	}
	
	Map<KGameAttrType, Integer> getEffectRoleAttrs() {
		return _effectRoleAttrsRO;
	}
	
	Map<Integer, Integer> processSkillLvUpOperation(List<KPet> beComposePets) {
		List<IPetSkill> mySkills = _skillSet.getAllSkills();
		IPetSkill skill;
		if(mySkills.isEmpty()) {
			return Collections.emptyMap();
		} else {
			boolean allMax = true;
			for(int i = 0; i < mySkills.size(); i++) {
				skill = mySkills.get(i);
				if(!skill.isMaxLv()) {
					allMax = false;
					break;
				}
			}
			if(allMax) {
				return Collections.emptyMap();
			}
		}
		Map<Integer, Integer> skillLvMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> skillLvUpRateMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> skillPetCount = new HashMap<Integer, Integer>();
		KPet temp;
		List<IPetSkill> skillList;
		Integer tempValue;
		for (int i = 0; i < beComposePets.size(); i++) {
			temp = beComposePets.get(i);
			if (temp.getOwnerId() == this.getOwnerId()) {
				skillList = temp.getSkillList();
				for (int k = 0; k < skillList.size(); k++) {
					skill = skillList.get(k);
					tempValue = skillLvMap.get(skill.getSkillTemplateId());
					if (tempValue == null) {
						tempValue = skill.getLv();
					} else {
						tempValue = skill.getLv() + tempValue;
					}
					skillLvMap.put(skill.getSkillTemplateId(), tempValue);

					tempValue = skillLvUpRateMap.get(skill.getSkillTemplateId());
					if (tempValue == null) {
						tempValue = skill.getLvUpRate();
					} else {
						tempValue = skill.getLvUpRate() + tempValue;
					}
					skillLvUpRateMap.put(skill.getSkillTemplateId(), tempValue);
					
					tempValue = skillPetCount.get(skill.getSkillTemplateId());
					if (tempValue == null) {
						tempValue = 1;
					} else {
						tempValue++;
					}
					skillPetCount.put(skill.getSkillTemplateId(), tempValue);
				}
			}
		}
		if(skillLvMap.isEmpty() && skillLvUpRateMap.isEmpty()) {
			return Collections.emptyMap();
		} else {
			Map<Integer, Integer> skillUpMap = new HashMap<Integer, Integer>();
			Integer totalLv;
			Integer totalRate;
			int incLv;
			int leftRate;
			for(int i = 0; i < mySkills.size(); i++) {
				skill = mySkills.get(i);
				totalLv = skillLvMap.get(skill.getSkillTemplateId());
				totalRate = skillLvUpRateMap.get(skill.getSkillTemplateId());
				incLv = 0;
				if(totalLv == null) {
					totalLv = 0;
				}
				if(totalRate == null) {
					totalRate = 0;
				}
				if(totalLv > 0) {
					incLv += totalLv - skillPetCount.get(skill.getSkillTemplateId());
				}
				if(totalRate > 0) {
					incLv += totalRate / UtilTool.TEN_THOUSAND_RATIO_UNIT;
					leftRate = totalRate % UtilTool.TEN_THOUSAND_RATIO_UNIT;
					if(UtilTool.random(UtilTool.TEN_THOUSAND_RATIO_UNIT) < leftRate) {
						incLv++;
					}
				}
				if(incLv > 0) {
					if (!skill.isMaxLv()) {
						((KPetSkillImpl) skill).updateLv(incLv);
						skillUpMap.put(skill.getSkillTemplateId(), skill.getLv());
					}
				}
			}
			if(skillUpMap.size() > 0) {
				this.notifyDataChange();
			}
			return skillUpMap;
		}
	}

	@Override
	protected String saveAttribute() {
		JSONObject obj = new JSONObject();
		try {
			obj.put(KEY_FIGHTING, _fighting.get());
			obj.put(KEY_CURRENT_EXP, _exp);
			obj.put(KEY_LAST_CHG_NAME_TIME, _lastChgNameTimeMillis);
			obj.put(KEY_GROW_VALUE, _growValue);
			obj.put(KEY_STAR_LV, _starLv);
			obj.put(KEY_SKILL_INFO, _skillSet.saveSkillInfo());
		} catch (Exception e) {
			_LOGGER.error("保存宠物数据出错！宠物id：{}", this.getId(), e);
		}
		return obj.toString();
	}

	@Override
	protected void parseAttribute(String attribute) {
		try {
			JSONObject obj = new JSONObject(attribute);
			this._fighting.set(obj.optBoolean(KEY_FIGHTING, false));
			this._exp = obj.optInt(KEY_CURRENT_EXP, 0);
			this._lastChgNameTimeMillis = obj.optLong(KEY_LAST_CHG_NAME_TIME, 0);
			this._growValue = obj.optInt(KEY_GROW_VALUE, 0);
			this._starLv = obj.optInt(KEY_STAR_LV, 0);
			this._skillSet.parseSkillInfo(obj.optString(KEY_SKILL_INFO, null));
			this.checkMissSkill();
		} catch (Exception e) {
			_LOGGER.error("解析宠物数据出错！宠物id：{}", this.getId(), e);
		}
	}
	
	protected void initFromDBComplete() {
		KPetTemplate template = KPetModuleManager.getPetTemplate(this.getTemplateId());
		if ((template.growMax == template.growMin && template.growMax != this._growValue) || (template.growMax < this._growValue)) {
			this._growValue = template.growMax;
		} else if (this._growValue < template.growMin) {
			this._growValue = template.growMin;
		} else if (this._growValue > template.growMax) {
			this._growValue = template.growMax;
		}
		this.getCommonAttributeFromTemplate(template);
		this.handleDynamicAttr();
		this.genEffectRoleAttrs();
	}
	
//	Map<KGameAttrType, Integer> getIncreaseRoleAttrMap() {
//		Map<KGameAttrType, Integer> map = new HashMap<KGameAttrType, Integer>();
//		map.put(KGameAttrType.ATK, _atk);
//		map.put(KGameAttrType.DEF, _def);
//		map.put(KGameAttrType.MAX_HP, _maxHp);
//		map.put(KGameAttrType.HIT_RATING, _hitRating);
//		map.put(KGameAttrType.DODGE_RATING, _dodgeRating);
//		map.put(KGameAttrType.CRIT_RATING, _critRating);
//		map.put(KGameAttrType.RESILIENCE_RATING, _resilienceRating);
//		return map;
//	}
	
	public void setFightingStatus(boolean status) {
		this._fighting.compareAndSet(!status, status);
		this.notifyDataChange();
	}
	
	public boolean isFighting() {
		return this._fighting.get();
	}
	
	/**
	 * 
	 * 增加经验
	 * 
	 * @param addExp 增加的值
	 * @param isActual 是否真实执行
	 * @return 一个长度为2的数组，array[0]表示增加的经验值，array[1]表示增加经验后的等级
	 */
	public int[] addExp(int addExp, boolean isActual, int roleLv) {
		int[] result = new int[2];
		if (this.level >= _maxLv || this.level >= roleLv) {
			return result;
		} else {
			int supposeExp = addExp + this._exp;
			int addValue = 0;
			if (supposeExp >= this._upgradeExp) {
				int nowLv = this.getLevel();
				int max = this._upgradeExp;
				addValue -= this._exp;
				while ((supposeExp = supposeExp - max) >= 0) {
					addValue += max;
					nowLv++;
					if (nowLv >= _maxLv || nowLv >= roleLv) {
						break;
					}
					max = KPetModuleManager.getUpgradeExp(_quality, nowLv);
				}
				if (supposeExp < 0) {
					supposeExp += max;
				}
				if (nowLv == _maxLv || nowLv == roleLv) {
					addValue -= supposeExp;
					supposeExp = 0;
				}
				if (isActual) {
					this._exp = supposeExp;
					addValue += supposeExp;
					this.onLevelUp(nowLv);
				} else {
					addValue += supposeExp;
				}
				result[1] = nowLv;
			} else {
				if (isActual) {
					addValue = addExp;
					_exp = supposeExp;
					notifyExpAdded();
				}
				result[1] = this.level;
				addValue = addExp;
			}
			result[0] = addValue;
			return result;
		}
	}
	
	public boolean isChgNameCoolDownFinished() {
		long sub = Math.abs(this._lastChgNameTimeMillis - System.currentTimeMillis());
		if(sub < KPetModuleConfig.getPetChangeNameCoolDownTime()) {
			return false;
		} else {
			return true;
		}
	}
	
	public void modifyPetName(String name) {
		super.changeName(name);
		this._lastChgNameTimeMillis = System.currentTimeMillis();
		this._nameEx = HyperTextTool.extColor(name, this._quality.getColorEnum());
	}
	
	public String getNameEx() {
		return _nameEx;
	}
	
	@Override
	public boolean canBeAttack() {
		return true;
	}
	
	@Override
	public byte getObjectType() {
		return OBJECT_TYPE_PET;
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
	public int getMaxLevel() {
		return _maxLv;
	}
	
	@Override
	public int getGrowValue() {
		return _growValue;
	}
	
	@Override
	public int getMaxGrowValue() {
		return _maxGrowValue;
	}
	
	@Override
	public int getCurrentExp() {
		return this._exp;
	}
	
	@Override
	public int getUpgradeExp() {
		return this._upgradeExp;
	}
	
	@Override
	public int getBeComposedExp() {
		return _beComposeExp;
	}
	
	@Override
	public int getSwallowFee() {
		return _swallowFee;
	}

	@Override
	public KPetQuality getQuality() {
		return _quality;
	}
	
	@Override
	public KPetType getPetType() {
		return _type;
	}
	
	@Override
	public KPetAtkType getAtkType() {
		return _atkType;
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
			return _showHitRating;
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
			return _speedX;
		case MOVE_SPEED_Y:
			return _speedY;
		case BATTLE_POWER:
//		case BATTLE_POWER_TOTAL:
			return _battlePower;
		default:
			return 0;
		}
	}

	@Override
	public List<IPetSkill> getSkillList() {
		return _skillSet.getAllSkills();
	}
	
//	@Override
//	public int getOpenSkillCount() {
//		return _skillSet.getOpenSkillCount();
//	}
	
	@Override
	public boolean isCanBeAutoSelected() {
		return _canBeAutoSelected;
	}

	@Override
	public long getCurrentHp() {
		return _maxHp;
	}

	@Override
	public long getMaxHp() {
		return _maxHp;
	}

	@Override
	public int getBattleMoveSpeedX() {
		return _speedX;
	}
	
	@Override
	public int getBattleMoveSpeedY() {
		return _speedY;
	}
	
	public int getVision() {
		return _vision;
	}

	@Override
	public int getShortRaAtkItr() {
		return _atkPeriod;
	}

	@Override
	public int getLongRaAtkItr() {
		return _atkPeriod;
	}

	@Override
	public int getShortRaAtkDist() {
		return _atkRange;
	}

	@Override
	public int getLongRaAtkDist() {
		return _atkRange;
	}
	
	@Override
	public String getAIId() {
		return _aiid;
	}
	
	@Override
	public int getStarLv() {
		return this._starLv;
	}
	
	@Override
	public Map<KGameAttrType, Integer> getAttrOfStar() {
		return this._attrOfStarRO;
	}
	
	@Override
	public int getStarLvUpRateHundred() {
		return this._starLvUpRateHundred;
	}

	@Override
	public ICombatSkillSupport getCombatSkillSupport() {
		return _skillSet;
	}

	@Override
	public int getAtk() {
		return _atk;
	}

	@Override
	public int getDef() {
		return _def;
	}

	@Override
	public int getHitRating() {
		return _hitRating;
	}

	@Override
	public int getDodgeRating() {
		return _dodgeRating;
	}

	@Override
	public int getCritRating() {
		return _critRating;
	}

	@Override
	public int getCritMultiple() {
		return _critMultiple;
	}

	@Override
	public int getCdReduce() {
		return 0;
	}

	@Override
	public int getHpAbsorb() {
		return 0;
	}

	@Override
	public int getDefIgnore() {
		return _defIgnore;
	}

	@Override
	public int getResilienceRating() {
		return _resilienceRating;
	}

	@Override
	public int getFaintResistRating() {
		return _faintResistRating;
	}

	@Override
	public int[] getNormalAtkAudioResIdArray() {
		return _normalAtkAudios;
	}

	@Override
	public int[] getOnHitAudioResIdArray() {
		return _hittedAudios;
	}

	@Override
	public int[] getInjuryAudioResIdArray() {
		return _injuryAudios;
	}

	@Override
	public int[] getDeadAudioResIdArray() {
		return _deadAudios;
	}
	
	@Override
	public int getFullImmunityDuration() {
		return _fullImmunityDuration;
	}
	
	@Override
	public int getFullImmunityIterval() {
		return _fullImmunityIterval;
	}
	
	@Override
	public int getAtkCountPerTime() {
		return _atkCountPerTime;
	}

}
