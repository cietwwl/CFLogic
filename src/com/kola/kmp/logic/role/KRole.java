package com.kola.kmp.logic.role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleBaseImpl;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.KGameGlobalConfig;
import com.kola.kmp.logic.character.CharacterAttrMap;
import com.kola.kmp.logic.character.ICharacterBattleAttr;
import com.kola.kmp.logic.combat.api.ICombatRole;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.combat.api.ICombatSkillSupport;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.flow.KRoleAttrModifyType;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KItemQualityEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.role.KRoleModuleManager.ExpData;
import com.kola.kmp.logic.role.message.KRoleServerMsgPusher;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;
import com.kola.kmp.logic.support.ItemModuleSupport.ISecondWeapon;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.IRoleEquipShowData;
import com.kola.kmp.logic.util.KBattlePowerCalculator;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.RoleTips;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;

/**
 * 
 * @author PERRY CHAN
 */
public class KRole extends RoleBaseImpl implements ICombatRole, ICharacterBattleAttr, ICombatSkillSupport, IRoleBaseInfo {

	private static final int[] EMPTY_ARRAY = new int[0];
	private static final Map<KGameAttrType, Integer> _EMPTY_ATTR_MAP = Collections.emptyMap();
	
	private static final Logger _LOGGER = KGameLogger.getLogger(KRole.class);

	private static final String JSON_NULL_STRING = "null";
	private static final String EMPTY_STRING = "";

	private static final String KEY_CURRENT_EXP = "1"; // 当前的exp
	private static final String KEY_CURRENT_ENGERGY = "2"; // 当前的怒气
	private static final String KEY_BATTLE_POWER = "3"; // 战斗力，保存一份，用于离线的时候，不至于显示战斗力为0
	private static final String KEY_TEAM_BATTLE_POWER = "4"; // 总战斗力，保存一份，用于离线的时候，不至于显示为0
	// private static final String KEY_HAD_PLAY_STORY = "5"; // 是否已经播放过开场动画
	private static final String KEY_PHY_POWER = "6"; // 当前体力
	private static final String KEY_ENERGY_BEAN_COUNT = "7"; // 怒气豆数量
	private static final String KEY_VIP_LEVEL = "8";
	private static final String KEY_OFFLINE_ATTR = "offline"; // 离线保存一份属性
	private static final String KEY_MAP_DATA = "map";
	private static final String KEY_SETTING_DATA = "setting";
	private static final String KEY_SECOND_WEAPON_DATA = "2ndWeapon";
	private static final String KEY_OFFLINE_ACTIVATE_SKILL = "offASkill";
	private static final String KEY_OFFLINE_PASSIVE_SKILL = "offPSkill";
	static final String KEY_EQUIPMENT_RES_MAP = "equip";
	static final String KEY_FASHION_RES_ID = "fashion";
	static final String KEY_EQUIPMENT_SET_RES = "setRes";

	private static final String KEY_OFFLINE_SKILL_SIZE = "s";

	private static final String KEY_OFFLINE_HP = "HP";
	private static final String KEY_OFFLINE_ATK = "ATK";
	private static final String KEY_OFFLINE_DEF = "DEF";
	private static final String KEY_OFFLINE_HIT_RATING = "HR";
	private static final String KEY_OFFLINE_CRIT_RATING = "CR";
	private static final String KEY_OFFLINE_DODGE_RATING = "DG";
	private static final String KEY_OFFLINE_MOVE_SPEED_X = "MS";
	private static final String KEY_OFFLINE_MOVE_SPEED_Y = "MSY";
	private static final String KEY_OFFLINE_RESILIENCE_RATING = "RR";
	private static final String KEY_OFFLINE_FAINT_RESIST_RATING = "FR";
	private static final String KEY_OFFLINE_DEF_IGNORE = "DI";
	private static final String KEY_OFFLINE_SKILL_DM_PCT_INC = "SKI";

	private int _preLv; //上一个等级
	private String _exName; // 染色的名字
	private int _exp; // 当前的experience
	private int _upgradeExp; // 当前的升级经验
	private int _maxExpOnce; // 每次可获得的最大经验值
	private int _inMapResId; // 在地图的资源id
	private int _headResId; // 头像资源id
	private int _animationHeadResId; // 剧情头像资源id
	private int _currentHp; // 当前的生命值
	private int _currentEnergy; // 当前的怒气值
	private int _maxHp; // 生命值上限
	private int _maxEnergy; // 怒气值上限
	private int _maxEnergyBean; // 怒气豆上限
	private int _hpRecovery; // 生命恢复速度
	private int _atk; // 攻击力
	private int _def; // 防御力
	private int _critRating; // 暴击等级
	private int _dodgeRating; // 闪避等级
	private int _basicHitRating; // 基础命中等级（角色1级的时候）
	private int _hitRating; // 命中等级
	private int _resilienceRating; // 抗爆等级
	private int _critMultiple; // 暴击伤害加成比例
	private int _cdReduce; // 技能冷却缩短百分比
	private int _hpAbsorb; // 生命吸取百分比（每次产生伤害之后，有几率将伤害的一定比例转化为自身的生命值）
	private int _defIgnore; // 无视防御的数值
	private int _faintResistRating; // 眩晕抵抗等级
	private int _shortRaAtkItr; // 近程攻速
	private int _longRaAtkItr; // 远程攻速
	private int _shortRaAtkDist; // 近程攻击距离
	private int _longRaAtkDist; // 远程攻击距离
	private int _moveSpeedX; // 移动速度
	private int _moveSpeedY; // 移动速度
	private int _battleMoveSpeedX; // 战斗x移动速度
	private int _battleMoveSpeedY; // 战斗y移动速度
	private int _block; // 格挡值
	private int _cohesionDm; // 蓄力伤害
	private int _bulletDm; // 子弹伤害
	private int _skillDmPctInc; // 技能伤害比例加成
	private int _dmReducePct;
	private int _vision;
	private int _skillBattlePower; // 技能战斗力
	private int _battlePower; // 战斗力
	private String _fightAI; // ai的编号
	private int _vipLv; // vip等级（复制一个数值出来，这里只是给流水使用）
	private int _minEnergyBeans; // 角色最少能拥有的怒气豆

	private IRoleMapData _roleMapData;
	private IRoleGameSettingData _roleGameSettingData;
	private KRoleSecondWeapon _secondWeapon = new KRoleSecondWeapon();
	private List<ICombatSkillData> _offlineActiveSkills = new ArrayList<ICombatSkillData>();
	private List<ICombatSkillData> _offlinePassiveSkills = new ArrayList<ICombatSkillData>();

	private AtomicInteger _phyPower = new AtomicInteger();
	private int _maxPhyPower;
	private int _totalBattlePower; // 总战斗力
	private int _energyBeanCount; // 怒气豆数量

	private final AtomicBoolean _fighting = new AtomicBoolean(false); // 是否正在战斗
	private byte _job;
	private boolean _firstInitFinish;
	// private boolean _hadPlayStory;

	private int[] _normalAtkAudios;
//	private int[] _onHitAudios;
	private int[] _onHitScreamAudios;
	private int[] _deadAudios;

//	private Map<Byte, String> _equipmentResMap = new ConcurrentHashMap<Byte, String>(); // 装备资源id的缓存，只负责保存，在KRoleBaseInfo会解析进行使用
//	private Map<Byte, String> _equipmentResMapRO = Collections.unmodifiableMap(_equipmentResMap);
	private List<IRoleEquipShowData> _equipmentShowDataList = new ArrayList<IRoleEquipShowData>();
	private List<IRoleEquipShowData> _equipmentShowDataListRO = Collections.unmodifiableList(_equipmentShowDataList);
	private String _fashionRes = ""; // 时装资源，只负责保存，在KRoleBaseInfo会解析进行使用
	private int[] _equipSetRes = new int[2]; // 装备特效资源id

	private CharacterAttrMap _basicAttr = new CharacterAttrMap();
	// private CharacterAttrMap _equipmentAttr = new CharacterAttrMap();
	// private CharacterAttrMap _petAttr = new CharacterAttrMap();
	// private CharacterAttrMap _fashionAttr = new CharacterAttrMap();
	// private CharacterAttrMap _mountAttr = new CharacterAttrMap();
	private Map<Integer, CharacterAttrMap> _otherAttr = new HashMap<Integer, CharacterAttrMap>();

	KRole() {
		this._roleGameSettingData = KSupportFactory.getMapSupport().newIRoleGameSettingDataInstance();
		this._roleGameSettingData.setDataStatusInstance(this.getDataStatus());
		this._roleMapData = KSupportFactory.getMapSupport().newIRoleMapDataInstance();
		this._roleMapData.setDataStatusInstance(this.getDataStatus());
		this._maxPhyPower = KRoleModuleConfig.getMaxPhyPower();
		for (int i = 0; i < KRoleModuleManager.allProviderTypes.size(); i++) {
			_otherAttr.put(KRoleModuleManager.allProviderTypes.get(i), new CharacterAttrMap());
		}
	}

	KRole(KRoleTemplate template) {
		this();
		this.level = template.level;
		this._phyPower.set(this._maxPhyPower);
		this.initFromTemplate(template);
	}

	private void putAttributeToAttrMap(CharacterAttrMap attrMap, ICharacterBattleAttr characterAttr, boolean append) {
		if (!append) {
			attrMap.clear();
		}
		attrMap.putValue(KGameAttrType.MAX_HP, (int) characterAttr.getMaxHp());
		attrMap.putValue(KGameAttrType.MAX_ENERGY, characterAttr.getMaxEnergy());
		attrMap.putValue(KGameAttrType.ATK, characterAttr.getAtk());
		attrMap.putValue(KGameAttrType.DEF, characterAttr.getDef());
		attrMap.putValue(KGameAttrType.HIT_RATING, characterAttr.getHitRating());
		attrMap.putValue(KGameAttrType.DODGE_RATING, characterAttr.getDodgeRating());
		attrMap.putValue(KGameAttrType.CRIT_RATING, characterAttr.getCritRating());
		attrMap.putValue(KGameAttrType.RESILIENCE_RATING, characterAttr.getResilienceRating());
		attrMap.putValue(KGameAttrType.HP_RECOVERY, characterAttr.getHpRecovery());
		attrMap.putValue(KGameAttrType.HP_ABSORB, characterAttr.getHpAbsorb());
		attrMap.putValue(KGameAttrType.DEF_IGNORE, characterAttr.getDefIgnore());
		attrMap.putValue(KGameAttrType.CD_REDUCE, characterAttr.getCdReduce());
		attrMap.putValue(KGameAttrType.CRIT_MULTIPLE, characterAttr.getCritMultiple());
		attrMap.putValue(KGameAttrType.FAINT_RESIST_RATING, characterAttr.getFaintResistRating());
		attrMap.putValue(KGameAttrType.SHORT_RA_ATK_ITR, characterAttr.getShortRaAtkItr());
		attrMap.putValue(KGameAttrType.LONG_RA_ATK_ITR, characterAttr.getLongRaAtkItr());
		attrMap.putValue(KGameAttrType.SHORT_RA_ATK_DIST, characterAttr.getShortRaAtkDist());
		attrMap.putValue(KGameAttrType.LONG_RA_ATK_DIST, characterAttr.getLongRaAtkDist());
		attrMap.putValue(KGameAttrType.BATTLE_MOVE_SPEED_X, characterAttr.getBattleMoveSpeedX());
		attrMap.putValue(KGameAttrType.BATTLE_MOVE_SPEED_Y, characterAttr.getBattleMoveSpeedY());
		attrMap.putValue(KGameAttrType.MOVE_SPEED_X, characterAttr.getMoveSpeedX());
		attrMap.putValue(KGameAttrType.MOVE_SPEED_Y, characterAttr.getMoveSpeedY());
		attrMap.putValue(KGameAttrType.BLOCK, characterAttr.getBlock());
		attrMap.putValue(KGameAttrType.COHESION_DM, characterAttr.getCohesionDm());
		attrMap.putValue(KGameAttrType.SHOT_DM_PCT, characterAttr.getBulletDm());
	}

	private CharacterAttrMap copyAttr() {
		CharacterAttrMap backup = new CharacterAttrMap();
		this.putAttributeToAttrMap(backup, this, false);
		return backup;
	}

	private Map<KGameAttrType, Integer> calculate(CharacterAttrMap allAttrMap) {
		Map<KGameAttrType, Integer> map = new HashMap<KGameAttrType, Integer>();
		Set<KGameAttrType> keySet = allAttrMap.keySet();
		List<KGameAttrType> percentageType = new ArrayList<KGameAttrType>();
		KGameAttrType currentType;
		Integer currentValue;
		for (Iterator<KGameAttrType> itr = keySet.iterator(); itr.hasNext();) {
			currentType = itr.next();
			if (currentType.isPercentageType) {
				percentageType.add(currentType);
			} else {
				currentValue = map.get(currentType);
				if (currentValue != null) {
					map.put(currentType, currentValue + allAttrMap.getAttrValue(currentType));
				} else {
					map.put(currentType, allAttrMap.getAttrValue(currentType));
				}
			}
		}
		for (int i = 0; i < percentageType.size(); i++) {
			currentType = percentageType.get(i);
			int percentage = allAttrMap.getAttrValue(currentType);
			KGameAttrType srcType = KGameAttrType.getSrcAttrTypeOfPercentage(currentType);
			if (srcType != null) {
				currentValue = map.get(srcType);
				currentValue += UtilTool.calculateTenThousandRatio(currentValue.intValue(), percentage);
				map.put(srcType, currentValue);
			} else {
				Integer pre = map.get(currentType);
				if (pre != null) {
					percentage += pre;
				}
				map.put(currentType, percentage);
			}
		}
		return map;
	}

	private void setLevelAttribute() {
		KJobTypeEnum job = KJobTypeEnum.getJob(_job);
		KRoleLevelAttribute lvAttr = KRoleModuleManager.getRoleLevelAttribute(job, level);
		this.putAttributeToAttrMap(_basicAttr, lvAttr, false);
		this._maxEnergy = lvAttr.getMaxEnergy();
	}

	private void setExpData() {
		ExpData expdata = KRoleModuleManager.getExpData(level);
		this._upgradeExp = expdata.upgradeExp;
		this._maxExpOnce = expdata.maxExpOnce;
	}

	private final void calculateBattlePower(Map<KGameAttrType, Integer> attrMap, boolean updateToClient) {
		Map<KGameAttrType, Integer> calMap = new HashMap<KGameAttrType, Integer>(attrMap);
		Integer hitRating = calMap.get(KGameAttrType.HIT_RATING);
		if (hitRating != null) {
			calMap.put(KGameAttrType.HIT_RATING, hitRating -_basicHitRating);
		}
		this._battlePower = KBattlePowerCalculator.calculateBattlePower(calMap, this.level, false);
		calculateTotalBattlePower(updateToClient);
	}

	private final void calculateTotalBattlePower(boolean updateToClient) {
		int pre = this._totalBattlePower;
		this._totalBattlePower = this._battlePower + this._skillBattlePower;
		if (updateToClient && pre != this._totalBattlePower) {
			KRoleServerMsgPusher.syncAttributeToClient(this, KGameAttrType.BATTLE_POWER, this._totalBattlePower);
			KRoleServerMsgPusher.sendAttributeChgMsg(this, pre, _totalBattlePower, _EMPTY_ATTR_MAP);
		}
	}

	private String encodeOfflineAttr() {
		JSONObject obj = new JSONObject();
		try {
			obj.put(KEY_OFFLINE_HP, this._maxHp);
			obj.put(KEY_OFFLINE_ATK, this._atk);
			obj.put(KEY_OFFLINE_DEF, this._def);
			obj.put(KEY_OFFLINE_HIT_RATING, this._hitRating);
			obj.put(KEY_OFFLINE_CRIT_RATING, this._critRating);
			obj.put(KEY_OFFLINE_DODGE_RATING, this._dodgeRating);
			obj.put(KEY_OFFLINE_MOVE_SPEED_X, this._moveSpeedX);
			obj.put(KEY_OFFLINE_MOVE_SPEED_Y, this._moveSpeedY);
			obj.put(KEY_OFFLINE_RESILIENCE_RATING, this._resilienceRating);
			obj.put(KEY_OFFLINE_FAINT_RESIST_RATING, this._faintResistRating);
			obj.put(KEY_OFFLINE_DEF_IGNORE, this._defIgnore);
			obj.put(KEY_OFFLINE_SKILL_DM_PCT_INC, this._skillDmPctInc);
		} catch (Exception e) {
			LOGGER.error("保存离线属性出现异常！角色id：{}", getId(), e);
		}
		return obj.toString();
	}

	private void decodeOfflineAttr(String attr) {
		if (attr != null && attr.length() > 0) {
			try {
				JSONObject obj = new JSONObject(attr);
				this._maxHp = obj.optInt(KEY_OFFLINE_HP, 0);
				this._atk = obj.optInt(KEY_OFFLINE_ATK, 0);
				this._def = obj.optInt(KEY_OFFLINE_DEF, 0);
				this._hitRating = obj.optInt(KEY_OFFLINE_HIT_RATING, 0);
				this._critRating = obj.optInt(KEY_OFFLINE_CRIT_RATING, 0);
				this._dodgeRating = obj.optInt(KEY_OFFLINE_DODGE_RATING, 0);
				this._moveSpeedX = obj.optInt(KEY_OFFLINE_MOVE_SPEED_X, 0);
				this._moveSpeedY = obj.optInt(KEY_OFFLINE_MOVE_SPEED_Y, 0);
				this._resilienceRating = obj.optInt(KEY_OFFLINE_RESILIENCE_RATING, 0);
				this._faintResistRating = obj.optInt(KEY_OFFLINE_FAINT_RESIST_RATING, 0);
				this._defIgnore = obj.optInt(KEY_OFFLINE_DEF_IGNORE, 0);
				this._skillDmPctInc = obj.optInt(KEY_OFFLINE_SKILL_DM_PCT_INC, 0);
			} catch (Exception e) {
				_LOGGER.error("解析离线数据出现异常！角色id：{}", getId(), e);
			}
		}
	}
	
	private String encodeSetRes() {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < _equipSetRes.length; i++) {
			builder.append(_equipSetRes[i]).append(",");
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}

	private String encodeOfflineSkill(List<ICombatSkillData> list) throws Exception {
		if (this._offlineActiveSkills.size() > 0) {
			JSONObject obj = new JSONObject();
			StringBuilder single;
			ICombatSkillData temp;
			obj.put(KEY_OFFLINE_SKILL_SIZE, list.size());
			for (int i = 0; i < list.size(); i++) {
				temp = list.get(i);
				single = new StringBuilder();
				single.append(temp.getSkillTemplateId()).append(";").append(temp.getLv()).append(";").append(temp.isSuperSkill());
				obj.put(String.valueOf(i), single.toString());
			}
			return obj.toString();
		} else {
			return "";
		}
	}

	private void decodeOfflineSkill(String data, List<ICombatSkillData> list) throws Exception {
		if (data != null && (data = data.trim()).length() > 0) {
			JSONObject obj = new JSONObject(data);
			int size = obj.optInt(KEY_OFFLINE_SKILL_SIZE, 0);
			String[] tempData;
			int skillTemplateId;
			for (int i = 0; i < size; i++) {
				tempData = obj.getString(String.valueOf(i)).split(";");
				skillTemplateId = Integer.parseInt(tempData[KRoleOfflineSkill.INDEX_SKILL_TEMPLATE_ID]);
				if (KSupportFactory.getSkillModuleSupport().isRoleSkill(skillTemplateId)) {
					list.add(new KRoleOfflineSkill(skillTemplateId, Integer.parseInt(tempData[KRoleOfflineSkill.INDEX_SKILL_LV]), Boolean
							.parseBoolean(tempData[KRoleOfflineSkill.INDEX_IS_SUPER_SKILL])));
				}
			}
		}
	}

	private boolean handleOfflineSkill(List<ICombatSkillData> srcList, List<ICombatSkillData> skillList) {
		boolean change = false;
		if (srcList.size() > 0) {
			ICombatSkillData temp;
			if (skillList.isEmpty()) {
				for (int i = 0; i < srcList.size(); i++) {
					temp = srcList.get(i);
					skillList.add(new KRoleOfflineSkill(temp));
				}
				this.notifyUpdate();
				change = true;
			} else {
				if (srcList.size() < skillList.size()) {
					for (ListIterator<ICombatSkillData> itr = skillList.listIterator(srcList.size()); itr.hasNext();) {
						itr.next();
						itr.remove();
					}
					for (int i = 0; i < srcList.size(); i++) {
						KRoleOfflineSkill offTemp = (KRoleOfflineSkill) skillList.get(i);
						offTemp.replace(srcList.get(i));
					}
					this.notifyUpdate();
					change = true;
				} else {
					KRoleOfflineSkill offTemp;
					boolean notFound;
					for (int i = 0; i < srcList.size(); i++) {
						temp = srcList.get(i);
						notFound = true;
						for (int k = 0; k < skillList.size(); k++) {
							offTemp = (KRoleOfflineSkill) skillList.get(k);
							if (offTemp.getSkillTemplateId() == temp.getSkillTemplateId()) {
								notFound = false;
								if (offTemp.compareAndUpdate(temp)) {
									this.notifyUpdate();
									change = true;
								}
								break;
							}
						}
						if (notFound) {
							skillList.add(new KRoleOfflineSkill(temp));
							this.notifyUpdate();
							change = true;
						}
					}
				}
			}
		} else {
			skillList.clear();
			this.notifyUpdate();
			change = true;
		}
		return change;
	}

	private void handleSecondWeapon() {
		ISecondWeapon weapon = KSupportFactory.getItemModuleSupport().getSecondWeaponArgs(this.getId());
		if (weapon != null) {
			this._secondWeapon.setInUse(true);
			this._secondWeapon.copy(weapon);
		} else {
			this._secondWeapon.setInUse(false);
		}
	}
	
	private String encodeEquipmentResInfo() throws Exception {
		IRoleEquipShowData data;
		JSONObject json = new JSONObject();
		for (int i = 0; i < _equipmentShowDataList.size(); i++) {
			data = _equipmentShowDataList.get(i);
			json.put(String.valueOf(data.getPart()), data.getRes() + ";" + data.getQuality().sign);
		}
		return json.toString();
	}

	void onLogin() {
		firstInit();
	}

	void effectAttr(KGameAttrType type, int value, KRoleAttrModifyType modifyType, Object... args) {
		switch (type) {
		case EXPERIENCE:
			this.addExp(value, modifyType, args);
			break;
		case PHY_POWER:
			this.increasePhyPower(value, true, true, modifyType.getFlowDescr(args));
			break;
		default:
			break;
		}
	}

	void firstInit() {
		if (!_firstInitFinish) {
			if (this._exName == null) {
				// init complete的时候已经赋值过一次
				this._exName = HyperTextTool.extRoleName(this.getName());
			}
			this._skillBattlePower = KSupportFactory.getSkillModuleSupport().getSkillBattlePower(this.getId(), level);
			this.prepareAttribute();
			this.calculateAttribute(false, false);
			this.handleOfflineSkill(KSupportFactory.getSkillModuleSupport().getRoleInUseIniSkillInstances(this.getId()), _offlineActiveSkills);
			this.handleOfflineSkill(KSupportFactory.getSkillModuleSupport().getRolePasSkillInstances(this.getId()), _offlinePassiveSkills);
			this.handleSecondWeapon();
			int[] resMap = KSupportFactory.getItemModuleSupport().getEquiSetMapResIds(this.getId());
			if (resMap != null) {
				if (_equipSetRes.length < resMap.length) {
					_equipSetRes = new int[resMap.length];
				}
				System.arraycopy(resMap, 0, _equipSetRes, 0, resMap.length);
			}
			VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(this.getId());
			if(vipData != null) {
				if(this._vipLv != vipData.lvl) {
					this._vipLv = vipData.lvl;
				}
				if (this._minEnergyBeans < vipData.minBean) {
					this._minEnergyBeans = vipData.minBean;
				}
				if(this._energyBeanCount < this._minEnergyBeans) {
					this._energyBeanCount = this._minEnergyBeans;
				}
			}
			_firstInitFinish = true;
		}
	}

	void notifyExpAdded() {
		KRoleServerMsgPusher.syncAttributeToClient(this, KGameAttrType.EXPERIENCE, _exp);
	}

	void notifyEffectChange(int type) {
		CharacterAttrMap attrMap = this._otherAttr.get(type);
		IRoleAttributeProvider provider = KRoleModuleManager.getAttributeProvider(type);
		if (provider != null && attrMap != null) {
			attrMap.replace(provider.getEffectAttr(this));
			this.calculateAttribute(true, false);
		}
	}

	void fullPhyPower(String reason) {
		int pre = this._phyPower.get();
		if (pre < _maxPhyPower) {
			// this._phyPower.set(_maxPhyPower);
			// this.notifyUpdate();
			// KRoleServerMsgPusher.syncAttributeToClient(this,
			// KGameAttrType.PHY_POWER, this._phyPower.get());
			increasePhyPower(_maxPhyPower - pre, true, false, reason);
		}
	}

	void increasePhyPower(int value, boolean sendMsg, boolean allowOverflow, String reason) {
		if (this._phyPower.get() < _maxPhyPower || allowOverflow) {
			this._phyPower.addAndGet(value);
			if (this._phyPower.get() >= _maxPhyPower && !allowOverflow) {
				this._phyPower.getAndSet(_maxPhyPower);
			}
			this.notifyUpdate();
			if (sendMsg) {
				KRoleServerMsgPusher.syncAttributeToClient(this, KGameAttrType.PHY_POWER, this._phyPower.get());
			}
		}
		FlowManager.logOther(this.getId(), OtherFlowTypeEnum.体力恢复, RoleTips.getTipsFlowPhyPowerRecovery(value, reason));
	}

	boolean decreasePhyPower(int value, String reason) {
		if (this._phyPower.get() < value) {
			return false;
		} else {
			if (value < 0) {
				value = Math.abs(value);
			}
			boolean dResult = false;
			int counter = 0;
			while (!dResult && counter < 10) {
				int now = this._phyPower.get();
				int result = now - value;
				dResult = this._phyPower.compareAndSet(now, result);
				counter++;
			}
			if (dResult) {
				KRoleServerMsgPusher.syncAttributeToClient(this, KGameAttrType.PHY_POWER, this._phyPower.get());
				KSupportFactory.getExcitingRewardSupport().notifyUsePhyPow(this.getId(), value);
				FlowManager.logOther(this.getId(), OtherFlowTypeEnum.体力恢复, RoleTips.getTipsFlowDecPhyPower(value, reason));
			}
			return dResult;
		}
	}

	void onLevelUp(int toLv) {
		this._preLv = this.level;
		this.level = toLv;
		this.setExpData();
		this.setLevelAttribute();
		this.calculateAttribute(true, true);
		this.notifyUpdate();
		KRoleServerMsgPusher.sendRoleLvUpMsg(this);
		KRoleModuleManager.notifyRoleLevelUp(this, _preLv);
	}

	void decodeEquipmentRes(String dbString) throws Exception {
		if (dbString != null && dbString.length() > 0) {
			JSONObject obj = new JSONObject(dbString);
			String key;
			String value;
			String[] equipArg;
			byte part;
			for (@SuppressWarnings("unchecked")
			Iterator<String> itr = obj.keys(); itr.hasNext();) {
				key = itr.next();
				value = obj.getString(key);
				if (value != null && !value.equals(JSON_NULL_STRING)) {
//					_equipmentResMap.put(Byte.parseByte(key), value);
					equipArg = value.split(";");
					part = Byte.parseByte(key);
					if (equipArg.length > 1) {
						_equipmentShowDataList.add(new KRoleEquipmentShowDataImpl(part, equipArg[0], KItemQualityEnum.getEnum(Integer.parseInt(equipArg[1]))));
					} else {
						_equipmentShowDataList.add(new KRoleEquipmentShowDataImpl(part, equipArg[0], KItemQualityEnum.优秀的));
					}
				}
			}
		}
	}

	void getCommonAttrFromTemplate(KRoleTemplate template) {
		this._vision = template.vision;
		this._job = template.job;
		this._inMapResId = template.inMapResId;
		this._headResId = template.headResId;
		this._animationHeadResId = template.animationHeadResId;
		// this._maxEnergy = template.maxEnergy;
		this._basicHitRating = KGameGlobalConfig.getBasicHitRating();
		this._maxEnergyBean = KRoleModuleConfig.getMaxEnergyBean();
		this._fightAI = template.fightAI;
		// if (this.level > 1) {
		// this.setLevelAttribute();
		// } else {
		// this.putAttributeToAttrMap(_basicAttr, template, false);
		// }
		this.setLevelAttribute();
		this._normalAtkAudios = Arrays.copyOf(template.normalAtkAudios, template.normalAtkAudios.length);
//		this._onHitAudios = Arrays.copyOf(template.onHitAudios, template.onHitAudios.length);
		this._onHitScreamAudios = Arrays.copyOf(template.injuryAudios, template.injuryAudios.length);
		this._deadAudios = Arrays.copyOf(template.deadAudios, template.deadAudios.length);
		this._battleMoveSpeedX = template.battleMoveSpeedX;
		this._battleMoveSpeedY = template.battleMoveSpeedY;
	}

	void initFromTemplate(KRoleTemplate template) {
		getCommonAttrFromTemplate(template);
		this._exp = 0;
		this._currentHp = _maxHp;
	}

	void prepareAttribute() {
		// Map<KGameAttrType, Integer> effect =
		// KSupportFactory.getItemModuleSupport().getEquipmentRoleEffects(getId());
		// _equipmentAttr.replace(effect);

		// effect = new HashMap<KGameAttrType, Integer>();
		// effect.putAll(KSupportFactory.getPetModuleSupport().getEffectRoleAttribute(getId()));
		// _petAttr.replace(effect);
		IRoleAttributeProvider provider;
		Map.Entry<Integer, CharacterAttrMap> entry;
		for (Iterator<Map.Entry<Integer, CharacterAttrMap>> itr = _otherAttr.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			provider = KRoleModuleManager.getAttributeProvider(entry.getKey());
			if (provider != null) {
				entry.getValue().replace(provider.getEffectAttr(this));
			}
		}
	}

	void calculateAttribute(boolean updateToClient, boolean isLvUp) {
		CharacterAttrMap before = this.copyAttr();
		CharacterAttrMap allAttr = new CharacterAttrMap();
		CharacterAttrMap otherAttr = new CharacterAttrMap();
		allAttr.combine(_basicAttr);
		CharacterAttrMap temp;
		for (Iterator<CharacterAttrMap> itr = _otherAttr.values().iterator(); itr.hasNext();) {
			temp = itr.next();
			// System.out.println(temp.getAttrValue(KGameAttrType.RESILIENCE_RATING));
			otherAttr.combine(temp);
		}
		int basicAttrInc = otherAttr.getAttrValue(KGameAttrType.BASIC_ATTR_INC);
		if(basicAttrInc > 0) {
			int tempHp = UtilTool.calculateTenThousandRatio(allAttr.getAttrValue(KGameAttrType.MAX_HP), UtilTool.TEN_THOUSAND_RATIO_UNIT + basicAttrInc);
			int tempAtk = UtilTool.calculateTenThousandRatio(allAttr.getAttrValue(KGameAttrType.ATK), UtilTool.TEN_THOUSAND_RATIO_UNIT + basicAttrInc);
			int tempDef = UtilTool.calculateTenThousandRatio(allAttr.getAttrValue(KGameAttrType.DEF), UtilTool.TEN_THOUSAND_RATIO_UNIT + basicAttrInc);
			allAttr.putValue(KGameAttrType.MAX_HP, tempHp);
			allAttr.putValue(KGameAttrType.ATK, tempAtk);
			allAttr.putValue(KGameAttrType.DEF, tempDef);
		}
		allAttr.combine(otherAttr);
		Map<KGameAttrType, Integer> map = this.calculate(allAttr);
		this._maxHp = map.get(KGameAttrType.MAX_HP).intValue();
		this._hpRecovery = map.get(KGameAttrType.HP_RECOVERY).intValue();
		this._atk = map.get(KGameAttrType.ATK).intValue();
		this._def = map.get(KGameAttrType.DEF).intValue();
		this._critRating = map.get(KGameAttrType.CRIT_RATING).intValue();
		this._dodgeRating = map.get(KGameAttrType.DODGE_RATING).intValue();
		this._hitRating = map.get(KGameAttrType.HIT_RATING).intValue();
		this._resilienceRating = map.get(KGameAttrType.RESILIENCE_RATING).intValue();
		this._critMultiple = map.get(KGameAttrType.CRIT_MULTIPLE).intValue();
		this._cdReduce = map.get(KGameAttrType.CD_REDUCE);
		this._hpAbsorb = map.get(KGameAttrType.CD_REDUCE);
		this._defIgnore = map.get(KGameAttrType.DEF_IGNORE).intValue();
		this._faintResistRating = map.get(KGameAttrType.FAINT_RESIST_RATING).intValue();
		this._shortRaAtkItr = map.get(KGameAttrType.SHORT_RA_ATK_ITR).intValue();
		this._longRaAtkItr = map.get(KGameAttrType.LONG_RA_ATK_ITR).intValue();
		this._shortRaAtkDist = map.get(KGameAttrType.SHORT_RA_ATK_DIST).intValue();
		this._longRaAtkDist = map.get(KGameAttrType.LONG_RA_ATK_DIST).intValue();
		this._moveSpeedX = map.get(KGameAttrType.MOVE_SPEED_X).intValue();
		this._moveSpeedY = map.get(KGameAttrType.MOVE_SPEED_Y).intValue();
		this._battleMoveSpeedX = map.get(KGameAttrType.BATTLE_MOVE_SPEED_X).intValue();
		this._battleMoveSpeedY = map.get(KGameAttrType.BATTLE_MOVE_SPEED_Y).intValue();
		this._block = map.get(KGameAttrType.BLOCK);
		this._bulletDm = map.get(KGameAttrType.SHOT_DM_PCT);
		this._cohesionDm = map.get(KGameAttrType.COHESION_DM);
		Integer tempSkillDmPctInc = map.get(KGameAttrType.SKILL_DM_INC);
		if(tempSkillDmPctInc != null) {
			this._skillDmPctInc = tempSkillDmPctInc;
		} else {
			this._skillDmPctInc = 0;
		}
		Integer dmReducePct = map.get(KGameAttrType.DAMAGE_REDUCTION);
		if (dmReducePct != null) {
			this._dmReducePct = dmReducePct;
		} else {
			this._dmReducePct = 0;
		}
		// this._battlePower = KRoleModuleConfig.calculateBattlePower(map,
		// level);
		int preBattlePower = this._totalBattlePower;
		this.calculateBattlePower(map, false);
		if (updateToClient) {
			CharacterAttrMap after = this.copyAttr();
			Map<KGameAttrType, Integer> different = after.differentWith(before, KGameAttrType.DISPLAY_FOR_ROLE);
			if (different.size() > 0) {
				Integer hitting = different.get(KGameAttrType.HIT_RATING);
				if (hitting != null) {
					different.put(KGameAttrType.HIT_RATING, hitting - _basicHitRating);
				}
				if (preBattlePower != _totalBattlePower) {
					different.put(KGameAttrType.BATTLE_POWER, _totalBattlePower);
				}
				KRoleServerMsgPusher.sendRefurbishAttribute(this, different);
				Map<KGameAttrType, Integer> offsetMap = new HashMap<KGameAttrType, Integer>(different);
				Map.Entry<KGameAttrType, Integer> entry;
				Integer beforeValue;
				for (Iterator<Map.Entry<KGameAttrType, Integer>> itr = offsetMap.entrySet().iterator(); itr.hasNext();) {
					entry = itr.next();
					beforeValue = before.getAttrValue(entry.getKey());
					if (beforeValue != null) {
						if (entry.getKey() == KGameAttrType.HIT_RATING) {
							entry.setValue(entry.getValue() - beforeValue + _basicHitRating); // 命中要加回基础命中值，因为发送到客户端的命中值是减掉基础命中值的
						} else {
							entry.setValue(entry.getValue() - beforeValue);
						}
					}
				}
				KRoleServerMsgPusher.sendAttributeChgMsg(this, preBattlePower, _totalBattlePower, offsetMap);
				if (isLvUp) {
					KRoleServerMsgPusher.sendLevelUpDialog(this, offsetMap, _preLv);
				}
			}
		}
		if (preBattlePower != _totalBattlePower) {
			KSupportFactory.getRankModuleSupport().notifyBattlePowerChange(this, _totalBattlePower);
			KSupportFactory.getGangSupport().notifyRoleBattlePowerChange(this, _totalBattlePower);
			KSupportFactory.getExcitingRewardSupport().notifyRoleBattlePowChange(this);
		}
	}

	boolean isFirstInitFinish() {
		return this._firstInitFinish;
	}

	void setVipLv(int nowVipLv) {
		this._vipLv = nowVipLv;
		VIPLevelData data = KSupportFactory.getVIPModuleSupport().getVIPLevelData(nowVipLv);
		if (data.minBean > this._minEnergyBeans) {
			this._energyBeanCount = data.minBean;
			this._minEnergyBeans = data.minBean;
		}
		this.notifyUpdate();
	}

	void syncEnergyInfo(int energy, int energyBeans) {
		if (energyBeans < _minEnergyBeans) {
			this._energyBeanCount = _minEnergyBeans;
			this._currentEnergy = 0;
		} else {
			this._currentEnergy = energy > _maxEnergy ? _maxEnergy : energy;
			this._energyBeanCount = energyBeans > _maxEnergyBean ? _maxEnergyBean : energyBeans;
		}
	}

	void updateEquipmentRes(List<IRoleEquipShowData> list) {
//		_equipmentResMap.clear();
//		_equipmentResMap.putAll(map);
		_equipmentShowDataList.clear();
		_equipmentShowDataList.addAll(list);
		this.notifyUpdate();
	}

	void updateFashionRes(String res) {
		this._fashionRes = res;
		this.notifyUpdate();
	}
	
	void updateEquipSetRes(int[] array) {
		if (this._equipSetRes.length < array.length) {
			this._equipSetRes = new int[array.length];
			System.arraycopy(array, 0, _equipSetRes, 0, array.length);
		} else {
			for (int i = 0; i < array.length; i++) {
				this._equipSetRes[i] = array[i];
			}
		}
		this.notifyUpdate();
	}

	void updateSecondWeapon(ISecondWeapon weapon) {
		if (weapon != null) {
			boolean update = false;
			if (_secondWeapon.isInUse()) {
				if (_secondWeapon.isDifferent(weapon)) {
					_secondWeapon.copy(weapon);
				}
			} else {
				_secondWeapon.setInUse(true);
				_secondWeapon.copy(weapon);
				update = true;
			}
			if (update) {
				this.notifyUpdate();
			}
		} else {
			_secondWeapon.setInUse(false);
		}
	}

	void updateOfflineSkill(ICombatSkillData skill) {
		List<ICombatSkillData> list;
		if (KSupportFactory.getSkillModuleSupport().getRoleIniSkillTemplate(skill.getSkillTemplateId()) != null) {
			list = _offlineActiveSkills;
		} else {
			list = _offlinePassiveSkills;
		}
		boolean found = false;
		if (list.size() > 0) {
			KRoleOfflineSkill temp;
			for (int i = 0; i < list.size(); i++) {
				temp = (KRoleOfflineSkill) list.get(i);
				if (temp.getSkillTemplateId() == skill.getSkillTemplateId()) {
					found = true;
					temp.compareAndUpdate(skill);
					break;
				}
			}
		}
		if (found) {
			this.notifyUpdate();
			this._skillBattlePower = KSupportFactory.getSkillModuleSupport().getSkillBattlePower(this.getId(), this.level);
			calculateTotalBattlePower(this.isOnline());
		}
	}

	void notifySkillListChange() {
		List<ICombatSkillData> inUseList = KSupportFactory.getSkillModuleSupport().getRoleInUseIniSkillInstances(this.getId());
//		ICombatSkillData temp;
//		KRoleOfflineSkill offTemp;
//		boolean change = false;
//		if (inUseList.size() < this._offlineActiveSkills.size()) {
//			for (Iterator<ICombatSkillData> itr = this._offlineActiveSkills.listIterator(inUseList.size()); itr.hasNext();) {
//				itr.next();
//				itr.remove();
//			}
//			for (int i = 0; i < inUseList.size(); i++) {
//				offTemp = (KRoleOfflineSkill) this._offlineActiveSkills.get(i);
//				offTemp.replace(inUseList.get(i));
//			}
//			change = true;
//		} else {
//			for (int i = 0; i < inUseList.size(); i++) {
//				temp = inUseList.get(i);
//				if (i < this._offlineActiveSkills.size()) {
//					offTemp = (KRoleOfflineSkill) this._offlineActiveSkills.get(i);
//					if (offTemp.getSkillTemplateId() != temp.getSkillTemplateId()) {
//						offTemp.replace(temp);
//						change = true;
//					}
//				} else {
//					this._offlineActiveSkills.add(new KRoleOfflineSkill(temp.getSkillTemplateId(), temp.getLv(), temp.isSuperSkill()));
//					change = true;
//				}
//			}
//		}
		boolean change = this.handleOfflineSkill(inUseList, _offlineActiveSkills);
		if (change) {
//			this.notifyUpdate();
			this._skillBattlePower = KSupportFactory.getSkillModuleSupport().getSkillBattlePower(this.getId(), this.level);
			calculateTotalBattlePower(this.isOnline());
		}
	}

	@Override
	protected String saveLogicAttribute() {
		JSONObject obj = new JSONObject();
		try {
			obj.put(KEY_CURRENT_EXP, _exp);
			obj.put(KEY_CURRENT_ENGERGY, _currentEnergy);
			// obj.put(KEY_HAD_PLAY_STORY, _hadPlayStory);
			obj.put(KEY_BATTLE_POWER, _battlePower);
			obj.put(KEY_TEAM_BATTLE_POWER, _totalBattlePower);
			obj.put(KEY_PHY_POWER, _phyPower.get());
			obj.put(KEY_ENERGY_BEAN_COUNT, _energyBeanCount);
			obj.put(KEY_VIP_LEVEL, _vipLv);
			obj.put(KEY_OFFLINE_ATTR, this.encodeOfflineAttr());
			obj.put(KEY_MAP_DATA, _roleMapData.encode());
			obj.put(KEY_SETTING_DATA, _roleGameSettingData.encode());
			obj.put(KEY_EQUIPMENT_RES_MAP, this.encodeEquipmentResInfo());
			obj.put(KEY_FASHION_RES_ID, _fashionRes);
			obj.put(KEY_EQUIPMENT_SET_RES, this.encodeSetRes());
			obj.put(KEY_SECOND_WEAPON_DATA, _secondWeapon.save());
			obj.put(KEY_OFFLINE_ACTIVATE_SKILL, this.encodeOfflineSkill(_offlineActiveSkills));
			obj.put(KEY_OFFLINE_PASSIVE_SKILL, this.encodeOfflineSkill(_offlinePassiveSkills));
		} catch (Exception e) {
			_LOGGER.error("保存属性出现异常！", e);
		}
		return obj.toString();
	}

	@Override
	protected void parseLogicAttribute(String attribute) {
		try {
			JSONObject obj = new JSONObject(attribute);
			this._exp = obj.optInt(KEY_CURRENT_EXP, 0);
			this._currentEnergy = (obj.optInt(KEY_CURRENT_ENGERGY, 0));
			this._battlePower = (obj.optInt(KEY_BATTLE_POWER, 0));
			this._totalBattlePower = obj.optInt(KEY_TEAM_BATTLE_POWER, 0);
			// _hadPlayStory = obj.optBoolean(KEY_HAD_PLAY_STORY, true);
			_phyPower.set(obj.optInt(KEY_PHY_POWER));
			_energyBeanCount = obj.optInt(KEY_ENERGY_BEAN_COUNT, 0);
			_vipLv = obj.optInt(KEY_VIP_LEVEL, 0);
			decodeOfflineAttr(obj.optString(KEY_OFFLINE_ATTR, EMPTY_STRING));
			_roleMapData.decode(obj.optString(KEY_MAP_DATA, EMPTY_STRING));
			_roleGameSettingData.decode(obj.optString(KEY_SETTING_DATA, EMPTY_STRING));
			decodeEquipmentRes(obj.optString(KEY_EQUIPMENT_RES_MAP, null));
			_fashionRes = obj.optString(KEY_FASHION_RES_ID, "");
			_secondWeapon.parse(obj.optString(KEY_SECOND_WEAPON_DATA, ""));
			decodeOfflineSkill(obj.optString(KEY_OFFLINE_ACTIVATE_SKILL, ""), _offlineActiveSkills);
			decodeOfflineSkill(obj.optString(KEY_OFFLINE_PASSIVE_SKILL, ""), _offlinePassiveSkills);
			String equipSetStr = obj.optString(KEY_EQUIPMENT_SET_RES, null);
			if (equipSetStr != null) {
				String[] array = equipSetStr.split(",");
				if (_equipSetRes.length < array.length) {
					_equipSetRes = new int[array.length];
				}
				for (int i = 0; i < array.length; i++) {
					_equipSetRes[i] = Integer.parseInt(array[i].replace(" ", ""));
				}
			}
		} catch (Exception e) {
			_LOGGER.error("解析属性出现异常！", e);
		}
	}

	@Override
	protected void initFromDBComplete() {
		KRoleTemplate template = KRoleModuleManager.getRoleTemplate(this.getType());
		this.getCommonAttrFromTemplate(template);
		this.setExpData();
		this._exName = HyperTextTool.extRoleName(this.getName());
		this._currentHp = this._maxHp;
	}
	
	@Override
	protected void onEntireDataLoadComplete() {
		KRoleModuleManager.notifyRolePutToCache(this);
	}
	
	public void fullEnergy() {
		this._energyBeanCount = _maxEnergyBean;
		this._currentEnergy = _maxEnergy;
	}

	@Override
	public int getVipLevel() {
		return _vipLv;
	}

	/**
	 * 
	 * 获取染色的名字
	 * 
	 * @return
	 */
	public String getExName() {
		return _exName;
	}

	/**
	 * 
	 * @return
	 */
	public IRoleMapData getRoleMapData() {
		return this._roleMapData;
	}

	/**
	 * 
	 * @return
	 */
	public IRoleGameSettingData getRoleGameSettingData() {
		return this._roleGameSettingData;
	}

	/**
	 * <pre>
	 * 获取角色的职业类型值
	 * 参考{@link com.kola.kmp.logic.other.KJobTypeEnum#getJobType()}
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-20 下午6:51:59
	 * </pre>
	 */
	public byte getJob() {
		return _job;
	}

	/**
	 * 
	 * @return
	 */
	public int getPhyPower() {
		return _phyPower.get();
	}

	/**
	 * 
	 * @return
	 */
	public int getMaxPhyPower() {
		return _maxPhyPower;
	}

	/**
	 * <pre>
	 * 获取角色自身的战力
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-20 下午6:53:24
	 * </pre>
	 */
	public int getBattlePower() {
		return _totalBattlePower;
	}

	/**
	 * <pre>
	 * 获取角色是否正在战斗中
	 * </pre>
	 * 
	 * @return
	 */
	public boolean isFighting() {
		return _fighting.get();
	}

	/**
	 * <pre>
	 * 将角色设置为战斗状态
	 * </pre>
	 * 
	 * @return 设置是否成功
	 */
	public boolean setFighting() {
		boolean success = this._fighting.compareAndSet(false, true);
		return success;
	}

	/**
	 * <pre>
	 * 将角色设为非战斗状态
	 * </pre>
	 * 
	 * @return 设置是否成功
	 */
	public boolean setNotFighting() {
		if (this._fighting.get()) {
			boolean success = this._fighting.compareAndSet(true, false);
			return success;
		}
		return true;
	}

	// /**
	// *
	// * 角色是否已经播放了开场动画
	// *
	// * @return
	// */
	// public boolean isHadPlayStory() {
	// return _hadPlayStory;
	// }

	public void checkOffLineIncreasePhyPower(boolean sendMsg) {
		if (_maxPhyPower > _phyPower.get()) {
			int subValue = _maxPhyPower - _phyPower.get();
			int times;
			if (getLastLeaveGameTime() < KRoleModuleConfig.getLastIncreasePhyPowerTime()) {
				/*
				 * 如果上一次离开游戏之后，服务器有对全服玩家进行过体力值刷新，则需要计算
				 * 上一次离开游戏之后，到最后一次刷新时间期间的一共刷新次数，并返还体力值 给玩家。
				 */
				times = Math.max((int) (KRoleModuleConfig.getLastIncreasePhyPowerTime() - getLastLeaveGameTime()) / KRoleModuleConfig.getRecoverPhyPowerTimeItr(), 1);
			} else {
				/*
				 * 如果上一次离开游戏的时间，到在此登陆，服务器仍未有刷新体力的记录，则证
				 * 明，服务器才刚刚开服，则需要计算停服期间，一共刷新了多少次体力值，并计 算返还给玩家
				 */
				times = (int) (System.currentTimeMillis() - getLastLeaveGameTime()) / KRoleModuleConfig.getRecoverPhyPowerTimeItr();
			}
			if (times > 0) {
				int increaseValue = KRoleModuleConfig.getPhyPowerRecoverItr() * times;
				if (increaseValue > subValue) {
					increaseValue = subValue;
				}
				this.increasePhyPower(increaseValue, sendMsg, false, RoleTips.getTipsFlowOfflineIncreasPhyPower(getLastLeaveGameTime(), System.currentTimeMillis()));
			}
		}
	}

	public int addExp(int addExp, KRoleAttrModifyType type, Object... args) {
		if (this.level < KRoleModuleConfig.getRoleMaxLv()) {
			if (addExp > _maxExpOnce) {
				addExp = _maxExpOnce;
			}
			int supposeExp = addExp + this._exp;
			int addValue = 0;
			if (supposeExp >= this._upgradeExp) {
				int nowLv = this.getLevel();
				int max = this._upgradeExp;
				addValue -= this._exp;
				while ((supposeExp = supposeExp - max) >= 0) {
					addValue += max;
					nowLv++;
					if (nowLv >= KRoleModuleConfig.getRoleMaxLv()) {
						break;
					}
					max = KRoleModuleManager.getUpgradeExp(nowLv);
				}
				if (supposeExp < 0) {
					supposeExp += max;
				}
				if (nowLv == KRoleModuleConfig.getRoleMaxLv()) {
					addValue -= supposeExp;
					supposeExp = 0;
				}
				this._upgradeExp = max;
				this._exp = supposeExp;
				addValue += supposeExp;
				this.onLevelUp(nowLv);
			} else {
				addValue = addExp;
				_exp = supposeExp;
			}
			FlowManager.logExp(this.getId(), addExp, type.getFlowDescr(args));
			notifyExpAdded();
			return addValue;
		} else {
			return 0;
		}
	}

	public int getAttributeByType(KGameAttrType attrType) {
		switch (attrType) {
		case MAX_HP:
			return this._maxHp;
		case ATK:
			return this._atk;
		case DEF:
			return this._def;
		case HIT_RATING:
			return this._hitRating - this._basicHitRating;
		case CRIT_RATING:
			return this._critRating;
		case DODGE_RATING:
			return this._dodgeRating;
		case MOVE_SPEED_X:
			return this._moveSpeedX;
		case MOVE_SPEED_Y:
			return this._moveSpeedY;
		case RESILIENCE_RATING:
			return this._resilienceRating;
		case FAINT_RESIST_RATING:
			return this._faintResistRating;
		case DEF_IGNORE:
			return this._defIgnore;
		case BATTLE_POWER:
//		case BATTLE_POWER_TOTAL:
			return this._totalBattlePower;
		default:
			return 0;
		}
	}
	
	public int getBasicAttrByType(KGameAttrType type) {
		Integer value = this._basicAttr.getAttrValue(type);
		if (value != null) {
			return value;
		}
		return 0;
	}

	public int getCurrentExp() {
		return _exp;
	}

	public int getUpgradeExp() {
		return _upgradeExp;
	}

	/**
	 * 
	 * 获取剧情头像资源id
	 * 
	 * @return
	 */
	public int getAnimationHeadResId() {
		return _animationHeadResId;
	}

	/**
	 * 
	 * 获取副武器
	 * 
	 * @return
	 */
	public ISecondWeapon getSecondWeapon() {
		if (_secondWeapon.isInUse()) {
			return _secondWeapon;
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @return
	 */
	public List<IRoleEquipShowData> getEquipmentRes() {
//		return _equipmentResMapRO;
		return _equipmentShowDataListRO;
	}

	/**
	 * 
	 * @return
	 */
	public String getFashionRes() {
		return _fashionRes;
	}
	
	
	@Override
	public int[] getEquipSetRes() {
		return _equipSetRes;
	}

	/**
	 * <pre>
	 * {@inheritDoc}
	 * 角色可以被攻击
	 * 
	 * <pre>
	 */
	@Override
	public boolean canBeAttack() {
		return true;
	}

	@Override
	public byte getObjectType() {
		return OBJECT_TYPE_ROLE;
	}

	@Override
	public int getTemplateId() {
		return this.getType();
	}

	@Override
	public int getHeadResId() {
		return _headResId;
	}

	/**
	 * 
	 * <pre>
	 * {@inheritDoc}
	 * 内部调用{@link KCharacterAttr#getInMapResId()}
	 * </pre>
	 * 
	 * @return
	 */
	@Override
	public int getInMapResId() {
		return _inMapResId;
	}

	@Override
	public long getCurrentHp() {
		return _currentHp;
	}

	@Override
	public long getMaxHp() {
		return _maxHp;
	}

	@Override
	public int getBattleMoveSpeedX() {
		return _battleMoveSpeedX;
	}

	public int getBattleMoveSpeedY() {
		return _battleMoveSpeedY;
	}

	@Override
	public int getBlock() {
		return _block;
	}

	@Override
	public int getCohesionDm() {
		return _cohesionDm;
	}

	@Override
	public int getBulletDm() {
		return _bulletDm;
	}

	public int getVision() {
		return _vision;
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
	public int getResilienceRating() {
		return _resilienceRating;
	}

	@Override
	public int getFaintResistRating() {
		return _faintResistRating;
	}

	@Override
	public int getCritMultiple() {
		return _critMultiple;
	}

	@Override
	public int getCdReduce() {
		return _cdReduce;
	}

	@Override
	public int getHpAbsorb() {
		return _hpAbsorb;
	}

	@Override
	public int getDefIgnore() {
		return _defIgnore;
	}

	@Override
	public int getShortRaAtkItr() {
		return _shortRaAtkItr;
	}

	@Override
	public int getLongRaAtkItr() {
		return _longRaAtkItr;
	}

	@Override
	public int getShortRaAtkDist() {
		return _shortRaAtkDist;
	}

	@Override
	public int getLongRaAtkDist() {
		return _longRaAtkDist;
	}

	@Override
	public int getDmReducePct() {
		return _dmReducePct;
	}
	
	@Override
	public int getSkillDmPctInc() {
		return _skillDmPctInc;
	}
	
	@Override
	public int getCurrentEnergy() {
		return _currentEnergy;
	}

	@Override
	public int getMaxEnergy() {
		return _maxEnergy;
	}

	@Override
	public int getEnergyBean() {
		return _energyBeanCount;
	}

	@Override
	public int getMaxEnergyBean() {
		return _maxEnergyBean;
	}

	@Override
	public int getHpRecovery() {
		return _hpRecovery;
	}

	@Override
	public String getAIId() {
		return _fightAI;
	}

	@Override
	public int[] getNormalAtkAudioResIdArray() {
		return _normalAtkAudios;
	}

	@Override
	public int[] getOnHitAudioResIdArray() {
//		return _onHitAudios;
		return EMPTY_ARRAY;
	}

	@Override
	public int[] getInjuryAudioResIdArray() {
		return _onHitScreamAudios;
	}

	@Override
	public int[] getDeadAudioResIdArray() {
		return _deadAudios;
	}

	@Override
	public ICombatSkillSupport getSkillSupport() {
		return this;
	}

	@Override
	public int getMoveSpeedX() {
		return _moveSpeedX;
	}

	@Override
	public int getMoveSpeedY() {
		return _moveSpeedY;
	}

	@Override
	public List<ICombatSkillData> getUsableSkills() {
		if (this.isInCache()) {
			List<ICombatSkillData> inUseSkill = KSupportFactory.getSkillModuleSupport().getRoleInUseIniSkillInstances(this.getId());
			return inUseSkill;
		} else {
			return this._offlineActiveSkills;
		}
	}

	@Override
	public List<ICombatSkillData> getPassiveSkills() {
		if (this.isInCache()) {
			return KSupportFactory.getSkillModuleSupport().getRolePasSkillInstances(this.getId());
		} else {
			return this._offlinePassiveSkills;
		}
	}

	private static class KRoleSecondWeapon implements ISecondWeapon {

		private static final String KEY_IN_USE = "1";
		private static final String KEY_WEAPON_TYPE = "2";
		private static final String KEY_BLOCK = "3";
		private static final String KEY_COHESION_FIXED_DM = "4";
		private static final String KEY_COHESION_PCT = "5";
		private static final String KEY_CLIP = "6";
		private static final String KEY_MACHINE_GUN_CD = "7";
		private static final String KEY_MACHINE_GUN_DM_PCT = "8";

		private boolean _inUse;
		private byte _type;
		private int _block;
		private int _cohesionFixedDm;
		private int _cohesionPct;
		private int _clip;
		private long _machineGunCD;
		private int _machineGunDmPct; // 机枪伤害百分比

		String save() throws Exception {
			JSONObject obj = new JSONObject();
			obj.put(KEY_IN_USE, _inUse);
			obj.put(KEY_WEAPON_TYPE, _type);
			obj.put(KEY_BLOCK, _block);
			obj.put(KEY_COHESION_FIXED_DM, _cohesionFixedDm);
			obj.put(KEY_COHESION_PCT, _cohesionPct);
			obj.put(KEY_CLIP, _clip);
			obj.put(KEY_MACHINE_GUN_CD, _machineGunCD);
			obj.put(KEY_MACHINE_GUN_DM_PCT, _machineGunDmPct);
			return obj.toString();
		}

		void parse(String attr) throws Exception {
			if (attr != null && (attr = attr.trim()).length() > 0) {
				JSONObject obj = new JSONObject(attr);
				this._inUse = obj.getBoolean(KEY_IN_USE);
				this._type = obj.getByte(KEY_WEAPON_TYPE);
				this._block = obj.getInt(KEY_BLOCK);
				this._cohesionFixedDm = obj.getInt(KEY_COHESION_FIXED_DM);
				this._cohesionPct = obj.getInt(KEY_COHESION_PCT);
				this._clip = obj.getInt(KEY_CLIP);
				this._machineGunCD = obj.getInt(KEY_MACHINE_GUN_CD);
				this._machineGunDmPct = obj.getInt(KEY_MACHINE_GUN_DM_PCT);
			}
		}

		void copy(ISecondWeapon src) {
			this._type = src.getType();
			this._block = src.getBlock();
			this._cohesionFixedDm = src.getCohesionFixedDm();
			this._cohesionPct = src.getCohesionPct();
			this._machineGunCD = src.getMachineGunCD();
			this._clip = src.getClip();
			this._machineGunDmPct = src.getMachineGunDmPct();
		}

		void setInUse(boolean pInUse) {
			this._inUse = pInUse;
		}

		boolean isInUse() {
			return _inUse;
		}

		boolean isDifferent(ISecondWeapon src) {
			return _block != src.getBlock() || _cohesionFixedDm != src.getCohesionFixedDm() || _clip != src.getClip() || _machineGunDmPct != src.getMachineGunDmPct();
		}

		@Override
		public byte getType() {
			return _type;
		}

		@Override
		public int getBlock() {
			return _block;
		}

		@Override
		public int getCohesionFixedDm() {
			return _cohesionFixedDm;
		}

		@Override
		public int getCohesionPct() {
			return _cohesionPct;
		}

		@Override
		public int getClip() {
			return _clip;
		}

		@Override
		public long getMachineGunCD() {
			return _machineGunCD;
		}

		@Override
		public int getMachineGunDmPct() {
			return _machineGunDmPct;
		}

	}

	private static class KRoleOfflineSkill implements ICombatSkillData {

		static final int INDEX_SKILL_TEMPLATE_ID = 0;
		static final int INDEX_SKILL_LV = 1;
		static final int INDEX_IS_SUPER_SKILL = 2;

		private int _skillTemplateId;
		private int _lv;
		private boolean _superSkill;
		private boolean _onlyEffectInPVP;

		KRoleOfflineSkill(int pSkillTemplateId, int pLv, boolean pSuperSkill) {
			this._skillTemplateId = pSkillTemplateId;
			this._lv = pLv;
			this._superSkill = pSuperSkill;
			KRoleIniSkillTemp iniSkillTemp = KSupportFactory.getSkillModuleSupport().getRoleIniSkillTemplate(pSkillTemplateId);
			if(iniSkillTemp != null) {
				this._onlyEffectInPVP = iniSkillTemp.onlyEffectInPVP;
			} else {
				KRolePasSkillTemp pasSkillTemp = KSupportFactory.getSkillModuleSupport().getRolePasSkillTemplate(pSkillTemplateId);
				if (pasSkillTemp != null) {
					this._onlyEffectInPVP = pasSkillTemp.onlyEffectInPVP;
				}
			}
		}
		
		KRoleOfflineSkill(ICombatSkillData skillData) {
			this._skillTemplateId = skillData.getSkillTemplateId();
			this._lv = skillData.getLv();
			this._superSkill = skillData.isSuperSkill();
			this._onlyEffectInPVP = skillData.onlyEffectInPVP();
		}

		boolean compareAndUpdate(ICombatSkillData target) {
			if (this._lv != target.getLv()) {
				this._lv = target.getLv();
				return true;
			}
			return false;
		}

		void replace(ICombatSkillData target) {
			this._skillTemplateId = target.getSkillTemplateId();
			this._lv = target.getLv();
			this._superSkill = target.isSuperSkill();
			this._onlyEffectInPVP = target.onlyEffectInPVP();
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
			return _superSkill;
		}
		
		@Override
		public boolean onlyEffectInPVP() {
			return _onlyEffectInPVP;
		}

	}

	/**
	 * <pre>
	 * private static long getBeginId() {
	 * 		int areaId = KGame.getGSID();
	 * 		return 1000000000L * areaId + 1;
	 * 	}
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2015-2-10 下午7:09:46
	 * </pre>
	 */
	public int getGSId() {
		return (int)(getId()/1000000000L);
	}

}
