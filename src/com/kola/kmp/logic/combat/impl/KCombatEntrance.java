package com.kola.kmp.logic.combat.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.combat.ICombatDropInfo;
import com.kola.kmp.logic.combat.ICombatEnhanceInfo;
import com.kola.kmp.logic.combat.ICombatEventListener;
import com.kola.kmp.logic.combat.ICombatForce;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.ICombatMinion;
import com.kola.kmp.logic.combat.api.ICombatBlock;
import com.kola.kmp.logic.combat.api.ICombatMonster;
import com.kola.kmp.logic.combat.api.ICombatMount;
import com.kola.kmp.logic.combat.api.ICombatObjectBase;
import com.kola.kmp.logic.combat.api.ICombatObjectFight;
import com.kola.kmp.logic.combat.api.ICombatPet;
import com.kola.kmp.logic.combat.api.ICombatRole;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.combat.api.ICombatSkillSupport;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KObstructionTargetType;
import com.kola.kmp.logic.util.KGameUtilTool;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatEntrance {
	
	private static final int _BEGIN_INSTANCE_ID = 1000000;
	
	private static final AtomicInteger _instanceIdGenerator = new AtomicInteger(_BEGIN_INSTANCE_ID);
	
	private static final ICombatSkillSupport _DEFAULT_SKILL_SUPPORT = new KCombatDefaultSkillSupport();
	
	private static final int[] EMPTY_ARRAY = new int[0];
	
	private static final int _DEFAULT_ATK_COUNT_PER_TIME = 1;
	
	private ICombatObjectBase _obj;
	
	private boolean _canBeAttack;
	private float _bornX;
	private float _bornY;
	private int _clientInstancingId;
	private long _ownerId;
	private long _srcObjId;
	private byte _srcObjType;
	private int _srcObjTemplateId;
	private byte _combatObjType;
	private byte _job;
	private int _lv;
	private byte _forceType;
	private String _name;
	private int _inMapResId;
	private int _headResId;
	private int _speedX;
	private int _speedY;
	private int _vision;
	private int _color;
	private String _aiId;
	private int _killedEnergy;
	private int _duration; // 生存时长，如果为0，表示没有生存时长限制
	private float _reduceDuration;
//	private List<Integer> _dropIds = new ArrayList<Integer>(10); // 掉落id，暂时只有怪物和障碍物有
	private List<ICombatDropInfo> _dropInfos = new ArrayList<ICombatDropInfo>(10);
	private boolean _atkByPercentage; // 攻击是否百分比攻击
	private boolean _fullImmunity; // 是否霸体
	private int _fullImmunityDuration; // 霸体持续时间
	private int _fullImmunityIteration; // 霸体间隔时间
	
	//**** 基础战斗属性（当前）
	private long _currentHp;	// 当前的HP
	private int _skillDmPctInc; // 技能伤害比例加成
	private int _currentEnergy; // 当前的怒气值
	private int _currentEnergyBean; // 当前的怒气豆
	private long _maxHp; // 当前的总HP
	private int _maxEnergy; // 怒气上限
	private int _maxEnergyBean; // 怒气豆上限
	private int _hpRecovery; // 当前的总HP恢复速度
	private int _atkCountPerTime = _DEFAULT_ATK_COUNT_PER_TIME;
	private int _atk; // 当前的总攻击力
	private int _def; // 当前的总防御力
	private int _hitRating; // 当前的总命中等级
	private int _dodgeRating; // 当前的总闪避等级
	private int _critRating; // 当前的总暴击等级
	private int _resilienceRating; // 当前的总抗暴等级
	private int _faintResistRating; // 当前的眩晕抵抗等级
	private int _critMultiple; // 当前的总暴击伤害加成
	private int _hpAbsorb; // 当前的总生命吸取比例
	private int _dmReducePct; // 伤害减免比例
	private int _defIgnore; // 当前的总无视防御
	private int _cdReduce; // 技能冷却时间缩短的百分比
	private int _shortRaAtkDist; // 近程攻击距离
	private int _longRaAtkDist; // 远程攻击距离
	private int _shortRaAtkItr; // 近程攻击速度
	private int _longRaAtkItr; // 远程攻击速度
	private Map<Integer, Integer> _durationTime;
	
	// 音效资源
//	private int[][] _audios = new int[AUDIO_TYPE_COUNT][];
	
	private ICombatSkillSupport _skillSupport;
	
	private KObstructionTargetType _targetType;
	
//	private ICombatRecorder _combatRecorder;
	
	private KCombatEntrance _mount;
	
	private List<ICombatEventListener> _eventList = new ArrayList<ICombatEventListener>(5);
	
//	public KCombatEntrance(ICombatRecorder recorder) {
//		this._combatRecorder = recorder;
//	}
	
	public void init(ICombatObjectBase pObj, KCombatEntrance mount, float pBornX, float pBornY, int pClientInstancingId) {
		this._obj = pObj;
		this._bornX = pBornX;
		this._bornY = pBornY;
		this._clientInstancingId = pClientInstancingId;
		this.initBaseAttribute(pObj);
		if(pObj instanceof ICombatObjectFight) {
			this.initFightAttribute((ICombatObjectFight)pObj);
		}
//		else {
//			for(int i = 0; i < _audios.length; i++) {
//				this._audios[i] = new int[0];
//			}
//		}
		switch(pObj.getObjectType()) {
		case ICombatObjectBase.OBJECT_TYPE_ROLE:
		case ICombatObjectBase.OBJECT_TYPE_ROLE_MONSTER:
			initFromCombatRole((ICombatRole) pObj);
			break;
		case ICombatObjectBase.OBJECT_TYPE_MONSTER:
		case ICombatObjectBase.OBJECT_TYPE_MONSTER_BOSS:
		case ICombatObjectBase.OBJECT_TYPE_MONSTER_ELITIST:
			initFromCombatMonster((ICombatMonster) pObj);
			break;
		case ICombatObjectBase.OBJECT_TYPE_PET:
			initFromCombatPet((ICombatPet) pObj);
			break;
		case ICombatObjectBase.OBJECT_TYPE_BLOCK:
			initFromCombatBlock((ICombatBlock) pObj);
			break;
		case ICombatObjectBase.OBJECT_TYPE_VEHICLE:
			initFromCombatVehicle((ICombatMount)pObj);
			break;
		case ICombatObjectBase.OBJECT_TYPE_MINION:
			initFromCombatMinion((ICombatMinion)pObj);
			break;
		}
//		if(pObj instanceof ICombatRole) {
//			this._combatRecorder = new KRoleCombatRecorderImpl();
//		} else {
//			this._combatRecorder = new KEmptyCombatRecorderImpl();
//		}
		_mount = mount;
		checkInstanceId();
	}
	
//	private void initAudioArray(int audioIndex, int[] audios) {
//		int[]array = this._audios[audioIndex] = new int[audios.length];
//		System.arraycopy(audios, 0, array, 0, array.length);
//	}
	
	private void initBaseAttribute(ICombatObjectBase obj) {
		this._canBeAttack = obj.canBeAttack();
		this._currentHp = obj.getMaxHp();
		this._srcObjId = obj.getId();
		this._srcObjType = obj.getObjectType();
		this._srcObjTemplateId = obj.getTemplateId();
		this._lv = obj.getLevel();
		this._name = obj.getName();
		this._inMapResId = obj.getInMapResId();
		this._headResId = obj.getHeadResId();
		this._speedX = obj.getBattleMoveSpeedX();
		this._speedY = obj.getBattleMoveSpeedY();
		this._vision = obj.getVision();
		this._maxHp = obj.getMaxHp();
	}
	
	private void initFightAttribute(ICombatObjectFight obj) {
		this._atk = obj.getAtk();
		this._def = obj.getDef();
		this._hitRating = obj.getHitRating();
		this._dodgeRating = obj.getDodgeRating();
		this._critRating = obj.getCritRating();
		this._resilienceRating = obj.getResilienceRating();
		this._faintResistRating = obj.getFaintResistRating();
		this._critMultiple = obj.getCritMultiple();
		this._hpAbsorb = obj.getHpAbsorb();
		this._defIgnore = obj.getDefIgnore();
		this._cdReduce = obj.getCdReduce();
		this._shortRaAtkDist = obj.getShortRaAtkDist();
		this._longRaAtkDist = obj.getLongRaAtkDist();
		this._shortRaAtkItr = obj.getShortRaAtkItr();
		this._longRaAtkItr = obj.getLongRaAtkItr();
//		this.initAudioArray(INDEX_SHORT_ATK_AUDIO, obj.getShortAtkAudioResId());
//		this.initAudioArray(INDEX_LONG_ATK_AUDIO, obj.getLongAtkAudioResId());
//		this.initAudioArray(INDEX_ATK_SCREAM_AUDIO, obj.getAtkScreamAudioResId());
//		this.initAudioArray(INDEX_ON_HIT_AUDIO, obj.getOnHitAudioResId());
//		this.initAudioArray(INDEX_ON_HIT_SCREAM_AUDIO, obj.getOnHitScreamAudioResId());
//		this.initAudioArray(INDEX_HOOT_AUDIO, obj.getHootAudioResId());
//		this.initAudioArray(INDEX_DEAD_AUDIO, obj.getDeadAudioResId());
//		this.initAudioArray(INDEX_KO_SCREAM_AUDIO, obj.getKOScreamResId());
	}
	
	private void setCommonInfo(byte pObjType, ICombatSkillSupport pSupport, byte pForceType) {
		this._combatObjType = pObjType;
		this._skillSupport = pSupport;
		this._forceType = pForceType;
	}
	
	private void initFromCombatRole(ICombatRole combatRole) {
		this.setCommonInfo(ICombatMember.MEMBER_TYPE_ROLE, combatRole.getSkillSupport(), ICombatForce.FORCE_TYPE_ROLE_SIDE);
		this._skillDmPctInc = combatRole.getSkillDmPctInc();
		this._currentEnergy = combatRole.getCurrentEnergy();
		this._maxEnergy = combatRole.getMaxEnergy();
		this._currentEnergyBean = combatRole.getEnergyBean();
		this._maxEnergyBean = combatRole.getMaxEnergyBean();
		this._hpRecovery = combatRole.getHpRecovery();
		this._aiId = combatRole.getAIId();
		this._job = combatRole.getJob();
	}
	
	private void initFromCombatPet(ICombatPet combatPet) {
		this.setCommonInfo(ICombatMember.MEMBER_TYPE_PET, combatPet.getCombatSkillSupport(), ICombatForce.FORCE_TYPE_ROLE_SIDE);
		this._shortRaAtkDist = combatPet.getShortRaAtkDist();
		this._longRaAtkDist = combatPet.getLongRaAtkDist();
		this._shortRaAtkItr = combatPet.getShortRaAtkItr();
		this._longRaAtkItr = combatPet.getLongRaAtkItr();
		this._aiId = combatPet.getAIId();
		this._atkCountPerTime = combatPet.getAtkCountPerTime();
		this._ownerId = combatPet.getOwnerId();
	}
	
	private void initFromCombatMonster(ICombatMonster monster) {
		this.setCommonInfo(monster.getObjectType(), monster.getCombatSkillSupport(), ICombatForce.FORCE_TYPE_MONSTER_SIDE);
		this._color = monster.getColor();
		this._aiId = monster.getAIId();
		this._killedEnergy = monster.getKilledEnergy();
//		this._dropIds = monster.getDropId();
		this.initDropInfos(monster.getDropId());
		this._fullImmunity = monster.isFullImmunity();
		this._fullImmunityDuration = monster.getFullImmunityDuration();
		this._fullImmunityIteration = monster.getFullImmunityIteration();
		this._atkCountPerTime = monster.getAtkCountPerTime();
	}
	
	private void initFromCombatMinion(ICombatMinion minion) {
		this.setCommonInfo(ICombatMember.MEMBER_TYPE_MINION, minion.getSKillSupport(), minion.getForceType());
		this._duration = minion.getDuration();
		this._fullImmunity = minion.isFullImmunity();
		this._fullImmunityDuration = minion.getFullImmunityDuration();
		this._fullImmunityIteration = minion.getFullImmunityIteration();
	}
	
	private void initFromCombatBlock(ICombatBlock block) {
		byte forceType;
		switch (block.getTargetType()) {
		case TARGET_ON_ALL:
		case TARGET_ON_NONE:
			forceType = ICombatForce.FORCE_TYPE_NEUTRAL;
			break;
		case TARGET_ON_MONSTER:
			forceType = ICombatForce.FORCE_TYPE_ROLE_SIDE;
			break;
		default:
		case TARGET_ON_ROLE:
			forceType = ICombatForce.FORCE_TYPE_MONSTER_SIDE;
			break;
		}
		this.setCommonInfo(ICombatMember.MEMBER_TYPE_BLOCK, _DEFAULT_SKILL_SUPPORT, forceType);
		this._targetType = block.getTargetType();
//		this._dropIds = block.getDropId();
		this.initDropInfos(block.getDropId());
		this._atkByPercentage = true;
	}
	
	private void initFromCombatVehicle(ICombatMount vehicle) {
		this.setCommonInfo(ICombatMember.MEMBER_TYPE_VEHICLE, vehicle, ICombatForce.FORCE_TYPE_NEUTRAL);
//		this._duration = vehicle.getDuration();
//		this._reduceDuration = vehicle.getReduceDuration();
//		this._duration = vehicle.getBeanTime();
		this._atkCountPerTime = vehicle.getAtkCountPerTime();
		this._fullImmunityDuration = vehicle.getFullImmunityDuration();
		this._fullImmunityIteration = vehicle.getFullImmunityIteration();
		this._fullImmunity = this._fullImmunityDuration > 0;
		this._aiId = vehicle.getAI();
		this._durationTime = vehicle.getBeanTime();
	}
	
	private void checkInstanceId() {
		if (this._forceType != ICombatForce.FORCE_TYPE_ROLE_SIDE) {
			if (this._clientInstancingId == 0) {
				switch (this._srcObjType) {
				case ICombatObjectBase.OBJECT_TYPE_BLOCK:
				case ICombatObjectBase.OBJECT_TYPE_VEHICLE:
					// 障碍物instanceId为0，表示是随机生成的，客户端要求为0，否则不能正确生成
					// 座驾不需要instanceId
					break;
				default:
					_instanceIdGenerator.compareAndSet(Integer.MAX_VALUE, _BEGIN_INSTANCE_ID);
					this._clientInstancingId = _instanceIdGenerator.incrementAndGet();
					break;
				}
			}
		}
	}
	
	private void initDropInfos(List<Integer> dropIds) {
		if (dropIds != null && dropIds.size() > 0) {
			for (int i = 0; i < dropIds.size(); i++) {
				ICombatDropInfo dropInfo = KCombatDropInfoFactory.getCombatDropInfo(dropIds.get(i));
				if (dropInfo != null) {
					_dropInfos.add(dropInfo);
				}
			}
		}
		
	}
	
	void release() {
		this._job = 0;
		this._color = 0;
		this._aiId = "";
		this._killedEnergy = 0;
		this._duration = 0;
		this._reduceDuration = 0;
//		this._dropIds.clear();
		this._dropInfos.clear();
		this._atkByPercentage = false;
		this._fullImmunity = false;
		this._maxEnergy = 0;
		this._maxEnergyBean = 0;
		this._currentEnergy = 0;
		this._currentEnergyBean = 0;
		this._hpRecovery = 0;
		this._atkCountPerTime = _DEFAULT_ATK_COUNT_PER_TIME;
		this._atk = 0;
		this._def = 0;
		this._hitRating = 0;
		this._dodgeRating = 0;
		this._critRating = 0;
		this._resilienceRating = 0;
		this._faintResistRating = 0;
		this._critMultiple = 0;
		this._hpAbsorb = 0;
		this._defIgnore = 0;
		this._cdReduce = 0;
		this._shortRaAtkDist = 0;
		this._longRaAtkDist = 0;
		this._shortRaAtkItr = 0;
		this._longRaAtkItr = 0;
		this._ownerId = 0;
		this._skillDmPctInc = 0;
		this._dmReducePct = 0;
		this._targetType = null;
		this._eventList.clear();
		this._durationTime = null;
//		this._combatRecorder.release();
	}
	
	void initVehicleAttr(ICombatRole master) {
		if (_obj instanceof ICombatMount) {
			ICombatMount vehicle = (ICombatMount) _obj;
			Map<KGameAttrType, Integer> basicAttrMap = vehicle.getBasicAttrs();
			if (basicAttrMap != null && basicAttrMap.size() > 0) {
				basicAttrMap = new HashMap<KGameAttrType, Integer>(basicAttrMap);
			} else {
				basicAttrMap = new HashMap<KGameAttrType, Integer>();
			}
			KGameUtilTool.combinMap(basicAttrMap, vehicle.getEquipmentAttrs());
			
			this._maxHp = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.MAX_HP);
			int pct = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.MAX_HP_PCT);
			if(pct > 0 ) {
				this._maxHp += UtilTool.calculateTenThousandRatioL(master.getMaxHp(), pct);
			}
			this._maxHp += master.getMaxHp();
			
			this._atk = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.ATK);
			pct = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.ATK_PCT);
			if(pct > 0) {
				this._atk += UtilTool.calculateTenThousandRatioL(master.getAtk(), pct);
			}
			this._atk += master.getAtk();
			
			this._def = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.DEF);
			pct = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.ATK_PCT);
			if(pct > 0) {
				this._def += UtilTool.calculateTenThousandRatioL(master.getDef(), pct);
			}
			this._def += master.getDef();
			
			this._hitRating = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.HIT_RATING);
			pct = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.HIT_RATING_PCT);
			if(pct > 0) {
				this._hitRating += UtilTool.calculateTenThousandRatioL(master.getHitRating(), pct);
			}
			this._hitRating += master.getHitRating();
			
			this._dodgeRating = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.DODGE_RATING);
			pct = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.DODGE_RATING_PCT);
			if(pct > 0) {
				this._dodgeRating += UtilTool.calculateTenThousandRatioL(master.getDodgeRating(), pct);
			}
			this._dodgeRating += master.getDodgeRating();
			
			this._critRating = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.CRIT_RATING);
			pct = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.CRIT_RATING_PCT);
			if(pct > 0) {
				this._critRating += UtilTool.calculateTenThousandRatioL(master.getCritRating(), pct);
			}
			this._critRating += master.getCritRating();
			
			this._resilienceRating = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.RESILIENCE_RATING);
			pct = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.RESILIENCE_RATING_PCT);
			if(pct > 0) {
				this._resilienceRating += UtilTool.calculateTenThousandRatioL(master.getResilienceRating(), pct);
			}
			this._resilienceRating += master.getResilienceRating();
			
			this._defIgnore = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.DEF_IGNORE);
			this._defIgnore += master.getDefIgnore();
			
			this._dmReducePct = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.DAMAGE_REDUCTION);
			this._dmReducePct += master.getDmReducePct();
			
			this._skillDmPctInc = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.SKILL_DM_INC);
			this._skillDmPctInc += master.getSkillDmPctInc();
			
			this._cdReduce = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.CD_REDUCE);
			this._cdReduce += master.getCdReduce();
			
			this._hpAbsorb = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.HP_ABSORB);
			this._hpAbsorb += master.getHpAbsorb();
			
			this._critMultiple = KGameUtilTool.getAttrValueSafely(basicAttrMap, KGameAttrType.CRIT_MULTIPLE);
			if(this._critMultiple == 0) {
				this._critMultiple = master.getCritMultiple();
			}
			this._shortRaAtkDist = master.getShortRaAtkDist();
			this._shortRaAtkItr = master.getShortRaAtkItr();
		}
	}
	
	void inheritBasicAttribute(ICombatObjectBase obj) {
		this._vision = obj.getVision();
		this._maxHp = obj.getMaxHp();
		this._currentHp = _maxHp;
		if(this._obj instanceof ICombatMount) {
			float speedUpTimes = ((ICombatMount) this._obj).getSpeedUpTimes();
			this._speedX = UtilTool.round(obj.getBattleMoveSpeedX() * speedUpTimes);
			this._speedY = UtilTool.round(obj.getBattleMoveSpeedY() * speedUpTimes);
		} else {
			this._speedX = obj.getBattleMoveSpeedX();
			this._speedY = obj.getBattleMoveSpeedY();
		}
	}
	
	/**
	 * 
	 * 继承攻击属性，角色的随从会继承主角的攻击属性
	 * 
	 * @param obj
	 */
	void inheritAttackAttribute(ICombatObjectFight obj) {
		this._atk = obj.getAtk();
		this._def = obj.getDef();
		this._hitRating = obj.getHitRating();
	}
	
	void updateAttackAttribute(ICombatEnhanceInfo info) {
		if (info.getAtkInc() > 0) {
			this._atk += info.getAtkInc();
		}
		if (info.getAtkPctInc() > 0) {
			this._atk += UtilTool.calculateTenThousandRatio(this._atk, info.getAtkPctInc());
		}
		if (info.getDefInc() > 0) {
			this._def += info.getDefInc();
		}
		if (info.getDefPctInc() > 0) {
			this._def += UtilTool.calculateTenThousandRatio(this._def, info.getDefPctInc());
		}
	}
	
	void notifyMasterMaxHpChange(long pre, long value) {
		this._maxHp -= pre;
		int multiple = (int) (value / pre);
		if (multiple > 1) {
			this._maxHp *= multiple;
		}
		this._maxHp += value;
	}
	
	void updateHp(long updateValue) {
		this._currentHp = updateValue;
	}
	
	void addEvent(ICombatEventListener event) {
		this._eventList.add(event);
	}
	
	void addAllEvent(List<ICombatEventListener> list) {
		this._eventList.addAll(list);
	}
	
	void changeDropInfos(List<ICombatDropInfo> pDropList) {
		this._dropInfos.clear();
		this._dropInfos.addAll(pDropList);
	}
	
	void clearDropInfos() {
		this._dropInfos.clear();
	}
	
	void updateOwnerId(long ownerId) {
		this._ownerId = ownerId;
	}
	
	public boolean canBeAttack() {
		return _canBeAttack;
	}
	
	public void changeMemberType(byte pMemberType) {
		this._combatObjType = pMemberType;
	}
	
	public void changeDropId(int pDropId) {
		this._dropInfos.clear();
		this.initDropInfos(Arrays.asList(pDropId));
	}
	
	public void changeForceType(byte pForceType) {
		this._forceType = pForceType;
		checkInstanceId();
	}
	
	public byte getMemberType() {
		return this._combatObjType;
	}
	
	public float getBornX() {
		return _bornX;
	}
	
	public float getBornY() {
		return _bornY;
	}
	
	public int getClientInstancingId() {
		return this._clientInstancingId;
	}
	
	void setClientInstancingId(int pClientInstancingId) {
		this._clientInstancingId = pClientInstancingId;
	}
	
	public long getCurrentHp() {
		return this._currentHp;
	}
	
	public ICombatSkillSupport getSkillSupport() {
		return _skillSupport;
	}
	
	public long getOwnerId() {
		return _ownerId;
	}
	
	public long getSrcObjId() {
		return _srcObjId;
	}

	public byte getSrcObjType() {
		return _srcObjType;
	}
	
	public int getSrcObjTemplateId() {
		return _srcObjTemplateId;
	}

	public int getLv() {
		return _lv;
	}

	public byte getForceType() {
		return _forceType;
	}

	public String getName() {
		return _name;
	}

	public int getInMapResId() {
		return _inMapResId;
	}

	public int getHeadResId() {
		return _headResId;
	}

	public int getSpeedX() {
		return _speedX;
	}
	
	public int getSpeedY() {
		return _speedY;
	}

	public int getVision() {
		return _vision;
	}

	public int getColor() {
		return _color;
	}
	
	public int getSkillDmPctInc() {
		return _skillDmPctInc;
	}

	public int getCurrentEnergy() {
		return _currentEnergy;
	}

	public int getCurrentEnergyBean() {
		return _currentEnergyBean;
	}

	public long getMaxHp() {
		return _maxHp;
	}

	public int getMaxEnergy() {
		return _maxEnergy;
	}

	public int getMaxEnergyBean() {
		return _maxEnergyBean;
	}

	public int getHpRecovery() {
		return _hpRecovery;
	}
	
	public int getAtkCountPerTime() {
		return _atkCountPerTime;
	}

	public int getAtk() {
		return _atk;
	}

	public int getDef() {
		return _def;
	}

	public int getHitRating() {
		return _hitRating;
	}

	public int getDodgeRating() {
		return _dodgeRating;
	}

	public int getCritRating() {
		return _critRating;
	}

	public int getResilienceRating() {
		return _resilienceRating;
	}

	public int getFaintResistRating() {
		return _faintResistRating;
	}

	public int getCritMultiple() {
		return _critMultiple;
	}

	public int getHpAbsorb() {
		return _hpAbsorb;
	}
	
	public int getDmReducePct() {
		return _dmReducePct;
	}

	public int getDefIgnore() {
		return _defIgnore;
	}

	public int getCdReduce() {
		return _cdReduce;
	}

	public int getShortRaAtkDist() {
		return _shortRaAtkDist;
	}

	public int getLongRaAtkDist() {
		return _longRaAtkDist;
	}

	public int getShortRaAtkItr() {
		return _shortRaAtkItr;
	}

	public int getLongRaAtkItr() {
		return _longRaAtkItr;
	}
	
	public KObstructionTargetType getTargetType() {
		return _targetType;
	}
	
	public String getAIId() {
		return _aiId;
	}
	
	public int getKilledEnergy() {
		return _killedEnergy;
	}
	
	/**
	 * 
	 * 设置存在时间
	 * 
	 * @param seconds
	 */
	void setDuration(int seconds) {
		this._duration = seconds;
	}
	
	/**
	 * 获取存在时间（秒）
	 * 
	 * @return
	 */
	public int getDuration() {
		return _duration;
	}
	
	public float getReduceDuration() {
		return _reduceDuration;
	}
	
//	public int[][] getAudioResIds() {
//		return this._audios;
//	}
	
	public List<ICombatDropInfo> getDropInfos() {
		return _dropInfos;
	}
	
	public KCombatEntrance getCombatMount() {
		return _mount;
	}
	
//	public ICombatRecorder getCombatRecorder() {
////		return _combatRecorder;
//		boolean roleType = this._obj instanceof ICombatRole;
//		return KCombatRecorderPool.borrowRecorder(roleType);
//	}
	
	public byte getJob() {
		return _job;
	}
	
	public boolean isAtkByPercentage() {
		return _atkByPercentage;
	}
	
	public boolean isFullImmunity() {
		return _fullImmunity;
	}
	
	public int getFullImmunityDuration() {
		return _fullImmunityDuration;
	}

	public int getFullImmunityIteration() {
		return _fullImmunityIteration;
	}
	
	public int[] getNormalAtkAudioResIdArray() {
		if(_obj instanceof ICombatObjectFight) {
			return ((ICombatObjectFight)_obj).getNormalAtkAudioResIdArray();
		} else {
			return EMPTY_ARRAY;
		}
	}

	public int[] getOnHitAudioResId() {
		if(_obj instanceof ICombatObjectFight) {
			return ((ICombatObjectFight)_obj).getOnHitAudioResIdArray();
		} else {
			return EMPTY_ARRAY;
		}
	}

	public int[] getInjuryAudioResIdArray() {
		if(_obj instanceof ICombatObjectFight) {
			return ((ICombatObjectFight)_obj).getInjuryAudioResIdArray();
		} else {
			return EMPTY_ARRAY;
		}
	}

	public int[] getDeadAudioResId() {
		if(_obj instanceof ICombatObjectFight) {
			return ((ICombatObjectFight)_obj).getDeadAudioResIdArray();
		} else {
			return EMPTY_ARRAY;
		}
	}
	
	public List<ICombatEventListener> getEventList() {
		return _eventList;
	}
	
	public Map<Integer, Integer> getDurationMap() {
		return _durationTime;
	}
	
	private static class KCombatDefaultSkillSupport implements ICombatSkillSupport {

		@Override
		public List<ICombatSkillData> getUsableSkills() {
			return Collections.emptyList();
		}
		
		@Override
		public List<ICombatSkillData> getPassiveSkills() {
			return Collections.emptyList();
		}
		
	}

}
