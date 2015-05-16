package com.kola.kmp.logic.combat.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.KGameMessage;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatEvent;
import com.kola.kmp.logic.combat.ICombatEventListener;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.ICombatMinion;
import com.kola.kmp.logic.combat.ICombatRecorder;
import com.kola.kmp.logic.combat.IOperationResult;
import com.kola.kmp.logic.combat.api.ICombatObjectBase;
import com.kola.kmp.logic.combat.api.ICombatSkillActor;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.combat.event.KMountUnderAttackEvent;
import com.kola.kmp.logic.combat.impl.KCombatRecorderBaseImpl.KEnergyRecoveryResult;
import com.kola.kmp.logic.combat.operation.IOperation;
import com.kola.kmp.logic.combat.skill.ICombatSkillExecution;
import com.kola.kmp.logic.combat.state.ICombatState;
import com.kola.kmp.logic.combat.state.ICombatStateTemplate;
import com.kola.kmp.logic.other.KEquipmentTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KItemQualityEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.support.ItemModuleSupport.ISecondWeapon;
import com.kola.kmp.logic.util.IRoleEquipShowData;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatMemberImpl implements ICombatMember, ICombatSkillActor {
	
	// 声效相关的常量 BEGIN
	private static final int AUDIO_TYPE_COUNT = 4;

	private static final int INDEX_NORMAL_ATK_AUDIO = 0;
	private static final int INDEX_ON_HIT_AUDIO = 1;
	private static final int INDEX_INJURY_AUDIO = 2;
	private static final int INDEX_DEAD_AUDIO = 3;
	// END
	
	private static final List<IRoleEquipShowData> EMPTY_EQUIP_RES_List = Collections.emptyList();
	private static final String EMPTY_FASHION_RES = "";
	private static final int[] EMPTY_EQUIP_SET_RES = new int[] { 0, 0 };
	
	/**
	 * 战斗数据记录（如果本对象类型是机甲，则会提前释放_recorder对象，并且把_recorder指向主人的_recorder对象）。
	 * 详情请参见{@link #summonMount(long)}
	 */
	private ICombatRecorder _recorder; 
	private ISecondWeapon _secondWeapon; // 副武器
	
	//**** 基础战斗对象属性
	private boolean _hang; // 是否被挂起
	private boolean _escape; // 是否逃跑，一般对主角才有效
	private boolean _canBeAttacked; // 能否被攻击，由源对象本身决定
	private long _srcObjId; // 源对象的id
	private byte _srcObjType; // 源对象类型
	private int _srcObjTemplateId; // 源对象模板id
	private KJobTypeEnum _job; // 职业
	private byte _memberType; // 成员类型
	private boolean _usingSecondWeapon; // 是否正在使用副武器
	private int _lv; // 等级
	private short _shadowId; // 战场的成员id（每个战场独立生成）
	private long _terminateTime; // 自动终结时间，如果为0，表示不是时间终结的
//	private int _reduceDuration; // 每次受击衰减的时间
	private boolean _terminateByTime; // 是否会随时间而终结
	private boolean _atkByPercentage; // 是否按照百分比进行攻击
	private byte _forceType; // 阵营
	private String _name; // 名字
	private int _inMapResId; // 地图资源id
	private int _headResId; // 头像资源id
	private float _x; // x坐标
	private float _y; // y坐标
	private int _speedX; // x轴速度
	private int _speedY; // y轴速度
	private int _vision; // 视野
	private int _color; // 颜色
	private String _aiId; // ai的id
	private int _killedEnergy; // 被击杀之后，击杀者能获得多少怒气
	private boolean _canAddBuff; // 能否添加状态到此对象身上
	private int[][] _audios = new int[AUDIO_TYPE_COUNT][]; // 音效资源
	
	//**** 基础战斗属性（当前）
	private long _currentHp;	// 当前的HP
	private int _currentEnergy; // 当前的怒气值
	private int _currentEnergyBean; // 当前的怒气豆
	private long _maxHp; // 当前的总HP
	private int _maxEnergy; // 怒气上限
	private int _maxEnergyBean; // 怒气豆上限
	private int _hpRecovery; // 当前的总HP恢复速度
	private int _atkCountPerTime; // 单次普通攻击的伤害次数
	private int _atk; // 当前的总攻击力
	private int _def; // 当前的总防御力
	private int _hitRating; // 当前的总命中等级
	private int _dodgeRating; // 当前的总闪避等级
	private int _critRating; // 当前的总暴击等级
	private int _resilienceRating; // 当前的总抗暴等级
	private int _faintResistRating; // 当前的眩晕抵抗等级
	private int _critMultiple; // 当前的总暴击伤害加成
	private int _hpAbsorb; // 当前的总生命吸取比例
	private int _defIgnore; // 当前的总无视防御
	private int _skillDmPctInc; // 技能伤害比例加成
	private int _dmReducePct; // 伤害减免比例
	private int _cdReduce; // 技能冷却时间缩短的百分比
	private int _shortRaAtkDist; // 近程攻击距离
	private int _longRaAtkDist; // 远程攻击距离
	private int _shortRaAtkItr; // 近程攻击速度
	private int _longRaAtkItr; // 远程攻击速度
	private int _dmBlock; // 伤害格挡值，一般情况下，这里只有角色类型的战士职业才会有这个值（切换副武器之后）
//	private int _cohesionPct; // 最大的聚力伤害比例（万分比），一般情况，这里只有角色类型的忍者职业才会有这个值（切换副武器之后）
	private int _secondWeaponFixedDm; // 聚力固定伤害加成
	private int _secondWeaponDmPct; // 当前的伤害加成，一般情况下，只有角色类型，并且更换了副武器，才会有这个值产生，例如忍者的聚力时间计算出来的比例，抢手的子弹伤害加成
	
	//**** 基础战斗属性（原始）
	private long _srcMaxHp; // 原始的HP上限
	private int _srcMaxEnergy; // 原始的怒气上限
	private int _srcHpRecovery; // 原始的HP恢复速度
	private int _srcAtk; // 原始的攻击力
	private int _srcDef; // 原始的防御力
	private int _srcHitRating; // 原始的命中等级
	private int _srcDodgeRating; // 原始的闪避等级
	private int _srcCritRating; // 原始的暴击等级
	private int _srcResilienceRating; // 原始的抗暴等级	
	private int _srcFaintResistRating; // 原始的眩晕抵抗等级
	private int _srcCritMultiple; // 原始的暴击伤害加成比例
	private int _srcHpAbsorb; // 原始的生命吸取比例
	private int _srcDefIgnore; // 原始的无视防御值
	private int _srcCdReduce; // 原始的技能冷却时间缩短
	private int _srcShortRaAtkItr;
	private int _srcLongRaAtkItr;
	private int _srcSpeedX;
	private int _srcSpeedY;
	private int _srcSkillDmPctInc;
	private int _srcDmReducePct;
	
	//**** 战斗过程中的属性附加值
	private int _addMaxHp; // 战斗过程中附加的HP上限
//	private int _addHpRecovery; // 战斗过程中附加的HP恢复速度
	private int _addAtk; // 战斗过程中附加的攻击力
	private int _addDef; // 战斗过程中附加的防御力
	private int _addHitRating; // 战斗过程附加的命中等级
	private int _addDodgeRating; // 战斗过程中附加的闪避等级
	private int _addCritRating; // 战斗过程中附加的暴击等级
	private int _addResilienceRating; // 战斗过程附加的抗暴等级
	private int _addCritMultiple; // 战斗过程中附加的暴击伤害加成比例
//	private int _addHpAbsorb; // 战斗过程附加的HP吸收比例
	private int _addDefIgnore; // 战斗过程附加的无视防御
	private int _addSpeedX; // 增加的x轴移动速度
	private int _addSpeedY; // 增加的y轴移动速度
	private int _addSkillDmPctInc;
	private int _addCdReduce; // 增加的技能冷却缩短时间
	
	//**** 战斗过程中的属性附加比例
	private int _addMaxHpPct; // 战斗过程中附加的HP上限
//	private int _addHpRecoveryPct; // 战斗过程中附加的HP恢复速度
	private int _addAtkPct; // 战斗过程中附加的攻击力
	private int _addDefPct; // 战斗过程中附加的防御力
	private int _addHitRatingPct; // 战斗过程附加的命中等级
	private int _addDodgeRatingPct; // 战斗过程中附加的闪避等级
	private int _addCritRatingPct; // 战斗过程中附加的暴击等级
	private int _addSpeedXPct; // 增加的x轴移动速度比例
	private int _addSpeedYPct; // 增加的y轴移动速度比例
	private int _addResilienceRatingPct; // 战斗过程附加的抗暴等级
//	private int _addHpAbsorbPct; // 战斗过程附加的HP吸收比例
//	private int _addDefIgnorePct; // 战斗过程附加的无视防御
//	private int _addCdReducePct; // 增加的技能冷却缩短时间
	private int _fullImmunityDuration; // 霸体时间（单位：毫秒）
	private int _fullImmunityIteration; // 霸体间隔（单位：毫秒）
	private boolean _invincible; // 是否无敌
	
	private long _deadTime; // 死亡时间
	
	private AtomicInteger _faint = new AtomicInteger(); // 眩晕状态记录（中了眩晕状态就+1，解除就-1）
	private AtomicInteger _freeze = new AtomicInteger(); // 定身状态记录（中了定身状态就+1，解除就-1）
	private AtomicInteger _fullImmunity = new AtomicInteger(); // 霸体记录
	
	private volatile boolean _alive; // 是否生存
	
	private ICombat _combat; // 本战场成员所附属于的战场实例
	
	private Map<Integer, KCombatSkill> _skillMap = new LinkedHashMap<Integer, KCombatSkill>(); // 本战场成员的技能列表
	
	private Map<Integer, ICombatMinion> _minions; // 本对象的召唤物列表
	
	private KCombatEntrance _mount; // 本战场成员所拥有的机甲对象
	
//	private int _mountConsumeBeans; // 机甲消耗的怒气豆数量
	
	private Map<Integer, ICombatState> _stateMap = new HashMap<Integer, ICombatState>(); // 目前身上带有的buff与debuff记录
	private Map<Integer, ICombatState> _permanentStateMap = new HashMap<Integer, ICombatState>(); // 永久buff记录，战斗中有效
	
	private Map<Integer, ICombatEventListener> _temporaryEffect = new HashMap<Integer, ICombatEventListener>(); // 临时状态记录
	private ArrayList<ICombatEventListener> _permanentEffect = new ArrayList<ICombatEventListener>(); // 永久状态记录
	
	private static void resetRecorder(ICombatMember member, ICombatRecorder nowRecorder) {
		if (member instanceof KCombatMemberImpl) {
			KCombatMemberImpl memberImpl = (KCombatMemberImpl)member;
			memberImpl._recorder.release(); // 提前释放对象的_recorder所引用的对象
			KCombatRecorderPool.returnRecorder(memberImpl._recorder);
			memberImpl._recorder = nowRecorder; // 把战斗对象的_recorder指向新的_recorder所引用的对象
		}
	}
	
	private int calculateAttribute(int src, int add, int percentage) {
		// 计算属性
		// 每个属性都有三个变量组成：原始值，增量值，增量比例，每次修改了这三个值中的其中一个，都会执行一次重新计算
		// 原始值一般以_src开头，增量值以_add开头，增量比例以_add开头，pct结尾
		int result = src + add;
		if (percentage > 0) {
			result += UtilTool.calculateTenThousandRatio(result, percentage);
		}
		return result;
	}
	
	private void notifyMaxHpChange() {
		// 通知最大HP发生改变
		if(this._currentHp > this.getMaxHp()) {
			this._currentHp = this.getMaxHp();
		}
	}
	
	private void loadSkillData(KCombatEntrance entrance) {
		// 加载角色技能数据
		List<ICombatSkillData> skillList = entrance.getSkillSupport().getUsableSkills(); // 从源对象处获取技能数据
		if (skillList.size() > 0) {
			Map<Integer, ICombatMinion> tempMap = new HashMap<Integer, ICombatMinion>();
			KCombatSkill combatSkill;
			ICombatMinion minion;
			for (int i = 0; i < skillList.size(); i++) {
				combatSkill = new KCombatSkill(skillList.get(i));
				this._skillMap.put(combatSkill.getSkillData().getSkillTemplateId(), combatSkill);
				// 检查技能是否召唤技能，这里就单纯判断一下，技能有没有带召唤模板
				minion = KCombatManager.getCombatMinion(combatSkill.getSkillData().getSkillTemplateId(), combatSkill.getSkillData().getLv(), this);
				if (minion != null) {
					tempMap.put(minion.getTemplateId(), minion);
				}
				// end
			}
			// 有召唤物的处理
			if(tempMap.size() > 0) {
				_minions = Collections.unmodifiableMap(tempMap);
			}
			// end
		}
	}
	
	private void loadPassSkillEffect(KCombatEntrance entrance) {
		// 加载被动技能附加的特殊效果
		List<ICombatSkillData> skillList = entrance.getSkillSupport().getPassiveSkills();
		ICombatSkillData combatSkill;
		KCombatSpecialEffect effect;
		List<ICombatSkillData> addStateSkillList = new ArrayList<ICombatSkillData>();
		for(int i = 0; i < skillList.size(); i++) {
			combatSkill = skillList.get(i);
			if(combatSkill.onlyEffectInPVP() && !_combat.getCombatType().isCanUsePVPSkill()) {
				continue;
			}
			effect = KCombatManager.getPassiveSkillSpecialEffect(combatSkill.getSkillTemplateId(), combatSkill.getLv());
			if(effect != null) {
				// 放到特殊效果列表
				this._permanentEffect.add(effect);
			} else {
				addStateSkillList.add(combatSkill);
			}
		}
		this.loadPVPPassSkillEffect(addStateSkillList);
	}
	
	private void loadPVPPassSkillEffect(List<ICombatSkillData> skillList) {
		if (this._combat.getCombatType().isCanUsePVPSkill()) {
			if (skillList != null) {
				List<Integer> stateIds;
				ICombatState state;
				for (int i = 0; i < skillList.size(); i++) {
					stateIds = KCombatManager.getPassAddStates(skillList.get(i).getSkillTemplateId());
					if (stateIds != null) {
						for (int k = 0; k < stateIds.size(); k++) {
							state = KCombatManager.getCombatState(this, KCombatManager.getCombatStateTemplate(stateIds.get(k)));
							this._permanentStateMap.put(state.getStateTemplateId(), state);
							state.notifyAdded(_combat, this, System.currentTimeMillis());
						}
					}
				}
			}
		}
	}
	
	private void fireEventToEffect(Collection<ICombatEventListener> map, int eventId, long happenTime) {
		// 战场事件通知
		ICombatEventListener effect;
		for (Iterator<ICombatEventListener> itr = map.iterator(); itr.hasNext();) {
			effect = itr.next();
			if (effect.isEffective(happenTime)) {
				if (effect.getEventId() == eventId) {
					effect.run(_combat, this, happenTime);
				}
			} else {
				itr.remove();
			}
		}
	}
	
	private void die(boolean fireEvent, long happenTime) {
		// 死亡逻辑
		this._alive = false;
		this._combat.notifyMemberDead(this); // 通知战场，把自己放到死亡列表，发送到客户端
		this._deadTime = happenTime;
		if (fireEvent) {
			this.combatEventNotify(ICombatEvent.EVENT_SELF_DEAD, happenTime);
		}
	}
	
	private void initAudioRes(KCombatEntrance entrance) {
		this._audios[INDEX_NORMAL_ATK_AUDIO] = entrance.getNormalAtkAudioResIdArray();
		this._audios[INDEX_ON_HIT_AUDIO] = entrance.getOnHitAudioResId();
		this._audios[INDEX_INJURY_AUDIO] = entrance.getInjuryAudioResIdArray();
		this._audios[INDEX_DEAD_AUDIO] = entrance.getDeadAudioResId();
	}
	
	private void packAudioRes(KGameMessage msg, int index) {
		int[] tempArray = this._audios[index];
		msg.writeByte(tempArray.length);
		for (int i = 0; i < tempArray.length; i++) {
			msg.writeInt(tempArray[i]);
		}
	}
	
	//**** 从ICombatMember 实现的方法 BEGIN
	@Override
	public void init(byte forceType, short pShadowId, long pCreateTime, KCombatEntrance entrance, ICombat pCombat) {
		// 初始化数据
		this._forceType = forceType;
		this._shadowId = pShadowId;
		this._combat = pCombat;
//		this._recorder = entrance.getCombatRecorder();
		this._canBeAttacked = entrance.canBeAttack();
		this._srcObjId = entrance.getSrcObjId();
		this._srcObjType = entrance.getSrcObjType();
		this._job = KJobTypeEnum.getJob(entrance.getJob());
		this._srcObjTemplateId = entrance.getSrcObjTemplateId();
		this._memberType = entrance.getMemberType();
//		this._canAddBuff = this._memberType != MEMBER_TYPE_BLOCK; // 障碍物不能添加buff
		this._inMapResId = entrance.getInMapResId();
		this._headResId = entrance.getHeadResId();
		this._x = entrance.getBornX();
		this._y = entrance.getBornY();
		this._lv = entrance.getLv();
		this._name = entrance.getName();
		this._srcSpeedX = entrance.getSpeedX();
		this._speedX = this._srcSpeedX;
		this._srcSpeedY = entrance.getSpeedY();
		this._speedY = this._srcSpeedY;
		this._vision = entrance.getVision();
		this._srcMaxHp = entrance.getMaxHp(); // 赋值原始的HP上限
		this._maxHp = this._srcMaxHp; // 赋值当前的HP上限
		this._currentHp = entrance.getCurrentHp(); // 赋值当前HP
		this._srcAtk = entrance.getAtk(); // 赋值原始的攻击力
		this._atkCountPerTime = entrance.getAtkCountPerTime();
		this._atk = this._srcAtk; // 赋值当前的攻击力
		this._srcDef = entrance.getDef(); // 赋值原始的防御力
		this._def = this._srcDef; // 赋值当前的防御力
		this._srcHitRating = entrance.getHitRating(); // 赋值原始的命中等级
		this._hitRating = this._srcHitRating; // 赋值当前的命中等级
		this._srcDodgeRating = entrance.getDodgeRating(); // 赋值原始的闪避等级
		this._dodgeRating = this._srcDodgeRating; // 赋值当前的闪避等级
		this._srcCritRating = entrance.getCritRating(); // 赋值原始的暴击等级
		this._critRating = this._srcCritRating; // 赋值当前的暴击等级
		this._srcResilienceRating = entrance.getResilienceRating(); // 赋值原始的抗暴等级
		this._resilienceRating = this._srcResilienceRating; // 赋值当前的抗暴等级
		this._srcFaintResistRating = entrance.getFaintResistRating();
		this._faintResistRating = this._srcFaintResistRating;
		this._srcCritMultiple = entrance.getCritMultiple(); // 赋值原始的暴击倍数
		this._critMultiple = this._srcCritMultiple; // 赋值当前的暴击倍数
		this._srcHpAbsorb = entrance.getHpAbsorb(); // 赋值原始的HP吸取
		this._hpAbsorb = this._srcHpAbsorb; // 赋值当前的HP吸取
		this._srcDefIgnore = entrance.getDefIgnore(); // 赋值原始的无视防御
		this._defIgnore = this._srcDefIgnore; // 赋值当前的无视防御
		this._srcSkillDmPctInc = entrance.getSkillDmPctInc();
		this._skillDmPctInc = this._srcSkillDmPctInc;
		this._srcDmReducePct = entrance.getDmReducePct();
		this._dmReducePct = this._srcDmReducePct;
		this._srcCdReduce = entrance.getCdReduce();
		this._cdReduce = this._srcCdReduce;
		this._srcShortRaAtkItr = entrance.getShortRaAtkItr();
		this._shortRaAtkItr = this._srcShortRaAtkItr;
		this._srcLongRaAtkItr = entrance.getLongRaAtkItr();
		this._longRaAtkItr = this._srcLongRaAtkItr;
		this._shortRaAtkDist = entrance.getShortRaAtkDist();
		this._longRaAtkDist = entrance.getLongRaAtkDist();
		this._srcHpRecovery = entrance.getHpRecovery(); // 赋值原始的生命恢复速度
		this._hpRecovery = this._srcHpRecovery; // 赋值当前的生命恢复速度
		this._currentEnergyBean = entrance.getCurrentEnergyBean();
		this._srcMaxEnergy = entrance.getMaxEnergy();
		this._maxEnergy = this._srcMaxEnergy;
		this._currentEnergy = entrance.getCurrentEnergy();
		this._maxEnergyBean = entrance.getMaxEnergyBean();
		this._color = entrance.getColor();
		this._aiId = entrance.getAIId();
		this._killedEnergy = entrance.getKilledEnergy();
		this._atkByPercentage = entrance.isAtkByPercentage();
		if (entrance.isFullImmunity()) {
//			this._fullImmunity.incrementAndGet();
			this._fullImmunityDuration = entrance.getFullImmunityDuration();
			this._fullImmunityIteration = entrance.getFullImmunityIteration();
		}
		if (entrance.getDuration() > 0) {
			this._terminateTime = pCreateTime + TimeUnit.MILLISECONDS.convert(entrance.getDuration(), TimeUnit.SECONDS);
//			LOGGER.info("createTime={}, terminateTime={}", pCreateTime, _terminateTime);
			this._terminateByTime = true;
			this._combat.addExtractOperation(new KTerminateByTimeOperation(this, _terminateTime));
			if(entrance.getReduceDuration() > 0) {
				this.registPermanentEffect(new KMountUnderAttackEvent((long) (entrance.getReduceDuration() * 1000)));
			}
		}
		this._alive = true;
		this._mount = entrance.getCombatMount();
//		this._audios = new int[entrance.getAudioResIds().length][];
//		for (int i = 0; i < _audios.length; i++) {
//			_audios[i] = new int[entrance.getAudioResIds()[i].length];
//			System.arraycopy(entrance.getAudioResIds()[i], 0, _audios[i], 0, _audios[i].length);
//		}
		this.initAudioRes(entrance);
		this.loadSkillData(entrance);
		this.loadPassSkillEffect(entrance);
		switch (this._memberType) {
		case MEMBER_TYPE_ROLE:
		case MEMBER_TYPE_ROLE_MONSTER:
		case MEMBER_TYPE_TEAM_MATE_ROLE:
			if (this._srcObjType == ICombatObjectBase.OBJECT_TYPE_ROLE) {
				this._secondWeapon = KCombatManager.getSecondWeapon(_srcObjId);
				this._recorder = KCombatRecorderPool.borrowRecorder(true);
			} else {
				this._recorder = KCombatRecorderPool.borrowRecorder(false);
			}
			break;
		case MEMBER_TYPE_PET:
		case MEMBER_TYPE_TEAM_MATE_PET:
		case MEMBER_TYPE_MINION:
			this._recorder = KCombatRecorderPool.borrowRecorder(true);
			break;
		default:
			this._recorder = KCombatRecorderPool.borrowRecorder(false);
		}
		switch (this._memberType) {
		case MEMBER_TYPE_ASSISTANT:
		case MEMBER_TYPE_BLOCK:
		case MEMBER_TYPE_BARREL_MONSTER:
			// 塔防战斗的小助手、障碍物、油桶怪物，都不能添加buff
			this._canAddBuff = false;
			break;
		case MEMBER_TYPE_BOSS_MONSTER:
			switch (this._combat.getCombatType()) {
			case WORLD_BOSS:
			case GANG_WAR_PVE:
				// 军团战和世界boss的boss怪物免疫buff
				this._canAddBuff = false;
				break;
			default:
				this._canAddBuff = true;
				break;
			}
			break;
		default:
			this._canAddBuff = true;
			break;
		}
		this._recorder.setCombetMemberId(this._shadowId);
		this._recorder.setCombat(_combat);
		List<ICombatEventListener> eventList = entrance.getEventList();
		if(eventList.size() > 0) {
			for(int i = 0; i < eventList.size(); i++) {
				this.registPermanentEffect(eventList.get(i));
			}
		}
	}
	
	public void release() {
		this._secondWeapon = null;
//		if (this._memberType != MEMBER_TYPE_VEHICLE) {
//			// 机甲已经归还过了
//			this._recorder.release();
//			KCombatRecorderPool.returnRecorder(_recorder);
//		}
		switch (this._memberType) {
		case MEMBER_TYPE_VEHICLE:
			break;
		default:
			this._recorder.release();
			KCombatRecorderPool.returnRecorder(_recorder);
			break;
		}
		this._hang = false;
		this._escape = false;
		this._usingSecondWeapon = false;
		this._terminateByTime = false;
		this._terminateTime = 0;
		this._alive = true;
		this._canAddBuff = true;
		
		// 副武器相关
		this._dmBlock = 0;
//		this._cohesionPct = 0;
		this._secondWeaponFixedDm = 0;
		this._secondWeaponDmPct = 0;
		
		// 属性增量相关
		this._addMaxHp = 0;
		this._addAtk = 0;
		this._addDef = 0;
		this._addHitRating = 0;
		this._addDodgeRating = 0;
		this._addCritRating = 0;
		this._addResilienceRating = 0;
		this._addCritMultiple = 0;
		this._addDefIgnore = 0;
		this._addMaxHpPct = 0;
		this._addAtkPct = 0;
		this._addDefPct = 0;
		this._addHitRatingPct = 0;
		this._addDodgeRatingPct = 0;
		this._addCritRatingPct = 0;
		this._addResilienceRatingPct = 0;
		this._addSpeedX = 0;
		this._addSpeedXPct = 0;
		this._addSpeedY = 0;
		this._addSpeedYPct = 0;
		this._addCdReduce = 0;
		this._addSkillDmPctInc = 0;
		// 属性增量相关 END
		
		this._deadTime = 0;
		this._faint.set(0);
		this._freeze.set(0);
		this._fullImmunityDuration = 0;
		this._fullImmunityIteration = 0;
		this._fullImmunity.set(0);
		this._skillMap.clear();
		this._minions = null;
		this._stateMap.clear();
		this._permanentStateMap.clear();
		this._temporaryEffect.clear();
		this._permanentEffect.clear();
		
		if(this._mount != null) {
			KCombatEntrancePool.returnEntrance(_mount);
		}
		
		for (int i = 0; i < _audios.length; i++) {
			_audios[i] = null;
		}
	}
	
	@Override
	public ICombatSkillActor getSkillActor() {
		return this;
	}
	
	@Override
	public ICombatRecorder getCombatRecorder() {
		return _recorder;
	}
	
	@Override
	public Map<Integer, ICombatMinion> getCombatMinions() {
		return this._minions;
	}
	
//	@Override
//	public List<IOperation> getPeriodOperation(long start, long end) {
//		List<IOperation> result;
//		if (this._stateMap.size() > 0 /*|| this._terminateByTime*/) {
//			result = new ArrayList<IOperation>();
//			ICombatState state;
//			List<IOperation> list;
//			for (Iterator<ICombatState> itr = _stateMap.values().iterator(); itr.hasNext();) {
//				state = itr.next();
//				list = state.getCycStateOperation(_combat, this, start, end);
//				if (list != null) {
//					result.addAll(list);
//				}
//			}
////			if (this._terminateByTime && end > this._terminateTime) {
////				// 终结自己
////				result.add(new KTerminateByTimeOperation(this, _terminateTime));
////			}
//		} else {
//			result = Collections.emptyList();
//		}
//		return result;
//	}
	
	@Override
	public void combatEventNotify(int eventId, long happenTime) {
		this.fireEventToEffect(_temporaryEffect.values(), eventId, happenTime);
		this.fireEventToEffect(_permanentEffect, eventId, happenTime);
	}

	/**
	 * {@inheritDoc}
	 * {@link com.kola.kmp.protocol.fight.KFightProtocol#BATTLE_OBJECT_STRUCTURE}
	 */
	@Override
	public void packDataToMsg(KGameMessage msg) {
		boolean fullImmunity = this._fullImmunityDuration > 0;
		boolean hasSecondWeapon = this._secondWeapon != null;
		List<IRoleEquipShowData> equipmentResList = null;
		String fashionRes = null;
		Map.Entry<Byte, String> equipResEntry;
		int[] weaponIcons = null;
		short masterShadowId = -1;
		int[] equipSetRes = EMPTY_EQUIP_SET_RES;
		switch (_memberType) {
		case MEMBER_TYPE_ROLE:
		case MEMBER_TYPE_ROLE_MONSTER:
		case MEMBER_TYPE_TEAM_MATE_ROLE:
		case MEMBER_TYPE_WORLD_BOSS_OTHER_ROLE:
			if (_srcObjType == ICombatObjectBase.OBJECT_TYPE_ROLE) {
				equipmentResList = KCombatManager.getRoleEquipmentResMap(_srcObjId);
				fashionRes = KCombatManager.getRoleFashionRes(_srcObjId, true);
				if (_memberType == MEMBER_TYPE_ROLE) {
					weaponIcons = KCombatManager.getRoleWeaponIcon(_srcObjId);
				}
				equipSetRes = KCombatManager.getEquipSetRes(_srcObjId);
			} else {
				equipmentResList = EMPTY_EQUIP_RES_List;
				fashionRes = KCombatManager.getRoleFashionRes(_srcObjId, false);
			}
			break;
		case MEMBER_TYPE_PET:
		case MEMBER_TYPE_PET_MONSTER:
		case MEMBER_TYPE_TEAM_MATE_PET:
		case MEMBER_TYPE_WORLD_BOSS_OTHER_PET:
			equipmentResList = EMPTY_EQUIP_RES_List;
			fashionRes = EMPTY_FASHION_RES;
			masterShadowId = this._combat.getMasterShadowIdOfPet(this._shadowId);
			break;
		default:
			equipmentResList = EMPTY_EQUIP_RES_List;
			fashionRes = EMPTY_FASHION_RES;
			break;
		}
		msg.writeShort(_shadowId);
		msg.writeByte(_memberType);
		if (weaponIcons != null) {
			msg.writeInt(weaponIcons[0]); // 主武器icon
			msg.writeInt(weaponIcons[1]); // 副武器icon
		}
		if (masterShadowId != -1) {
			msg.writeShort(masterShadowId); // 主人的shadowId
		}
		msg.writeShort(_lv);
		msg.writeInt(_headResId);
		msg.writeInt(_inMapResId);
		msg.writeByte(_forceType);
		msg.writeFloat(_x);
		msg.writeFloat(_y);
		msg.writeUtf8String(_name);
//		msg.writeInt(_currentHp);
		msg.writeLong(_currentHp);
//		System.out.println("！！！！当前血量：" + _currentHp + "，名字：" + _name);
//		msg.writeInt(_maxHp);
		msg.writeLong(_maxHp);
//		if (this._srcObjType == MEMBER_TYPE_BOSS_MONSTER) {
//			LOGGER.info("最大血量：" + _maxHp + "，名字：" + _name);
//		}
		msg.writeShort(_currentEnergy);
		msg.writeShort(_maxEnergy);
		msg.writeByte(_currentEnergyBean);
		msg.writeByte(_maxEnergyBean);
		msg.writeShort(_hpRecovery);
		msg.writeInt(_speedX); // x移动速度
		msg.writeInt(_speedY); // y移动速度
		msg.writeShort(_vision);
		msg.writeByte(_atkCountPerTime);
		msg.writeInt(_atk);
		msg.writeInt(_def);
		msg.writeInt(_hitRating);
		msg.writeInt(_dodgeRating);
		msg.writeInt(_critRating);
		msg.writeInt(_resilienceRating);
		msg.writeInt(_faintResistRating);
		msg.writeInt(_critMultiple);
		msg.writeInt(_cdReduce);
		msg.writeInt(_defIgnore);
		msg.writeInt(_skillDmPctInc);
		msg.writeInt(_hpAbsorb);
		msg.writeInt(_dmReducePct);
		msg.writeInt(_shortRaAtkItr); // 目前攻击速度只用近程攻击速度
		msg.writeShort(_shortRaAtkDist); // 普通攻击距离
		msg.writeInt(_color);
		msg.writeUtf8String(_aiId);
		msg.writeBoolean(fullImmunity);
		if(fullImmunity) {
			msg.writeInt(_fullImmunityDuration);
			msg.writeInt(_fullImmunityIteration);
		}
		msg.writeBoolean(_atkByPercentage);
		msg.writeBoolean(hasSecondWeapon);
		if(hasSecondWeapon) {
			int[] attrArray;
			switch (_secondWeapon.getType()) {
			case ISecondWeapon.SECOND_WEAPON_TYPE_BROADSWORD:
				attrArray = new int[]{_secondWeapon.getCohesionFixedDm(), _secondWeapon.getCohesionPct()};
				break;
			case ISecondWeapon.SECOND_WEAPON_TYPE_MACHINE_GUN:
				attrArray = new int[]{_secondWeapon.getClip(), (int)_secondWeapon.getMachineGunCD(), _secondWeapon.getMachineGunDmPct() + UtilTool.TEN_THOUSAND_RATIO_UNIT};
				break;
			case ISecondWeapon.SECOND_WEAPON_TYPE_SHIELD:
				attrArray = new int[]{_secondWeapon.getBlock()};
				break;
			default:
				attrArray = null;
			}
			msg.writeByte(_secondWeapon.getType());
			if(attrArray != null) {
				msg.writeByte(attrArray.length);
				for(int i = 0; i < attrArray.length; i++) {
					msg.writeInt(attrArray[i]);
				}
			}
		}
		msg.writeByte(_skillMap.size()); // 技能数量
		KCombatSkill temp;
		for(Iterator<KCombatSkill> itr = _skillMap.values().iterator(); itr.hasNext();) {
			temp = itr.next();
			msg.writeInt(temp.getSkillData().getSkillTemplateId());
			msg.writeByte(temp.getSkillData().getLv());
		}
//		int[] tempArray;
//		for (int i = 0; i < _audios.length; i++) {
//			tempArray = _audios[i];
//			msg.writeByte(tempArray.length);
//			for (int j = 0; j < tempArray.length; j++) {
//				msg.writeInt(tempArray[j]);
//			}；
//		}
		this.packAudioRes(msg, INDEX_NORMAL_ATK_AUDIO);
//		this.packAudioRes(msg, INDEX_ON_HIT_AUDIO);
		this.packAudioRes(msg, INDEX_INJURY_AUDIO);
		this.packAudioRes(msg, INDEX_DEAD_AUDIO);
		boolean hasMount = _mount != null;
		msg.writeBoolean(hasMount);
		if(hasMount) {
			if(_srcMaxHp != _maxHp) {
				this._mount.notifyMasterMaxHpChange(_srcMaxHp, _maxHp);
			}
			List<ICombatSkillData> combatSkills = _mount.getSkillSupport().getUsableSkills();
			Map<Integer, Integer> durationMap = _mount.getDurationMap();
			msg.writeInt(_mount.getSrcObjTemplateId());
			msg.writeUtf8String(_mount.getName());
			msg.writeInt(_mount.getHeadResId());
			msg.writeInt(_mount.getInMapResId());
//			msg.writeShort(_mount.getDuration());
			msg.writeShort(durationMap.get(1));
//			msg.writeShort(durationMap.get(2));
//			msg.writeShort(durationMap.get(3));
			msg.writeLong(_mount.getMaxHp());
			msg.writeShort(_mount.getHpRecovery());
			msg.writeInt(_mount.getSpeedX());
			msg.writeInt(_mount.getSpeedY());
			msg.writeShort(_mount.getVision());
			msg.writeByte(_mount.getAtkCountPerTime());
			msg.writeInt(_mount.getAtk());
			msg.writeInt(_mount.getDef());
			msg.writeInt(_mount.getHitRating());
			msg.writeInt(_mount.getDodgeRating());
			msg.writeInt(_mount.getCritRating());
			msg.writeInt(_mount.getResilienceRating());
			msg.writeInt(_mount.getFaintResistRating());
			msg.writeInt(_mount.getCritMultiple());
			msg.writeInt(_mount.getCdReduce());
			msg.writeInt(_mount.getDefIgnore());
			msg.writeInt(_mount.getSkillDmPctInc());
			msg.writeInt(_mount.getHpAbsorb());
			msg.writeInt(_mount.getDmReducePct());
			msg.writeInt(_mount.getShortRaAtkItr());
			msg.writeShort(_mount.getShortRaAtkDist());
			msg.writeInt(_mount.getColor());
			msg.writeBoolean(_mount.canBeAttack());
			msg.writeBoolean(_mount.isFullImmunity());
			if(_mount.isFullImmunity()) {
				msg.writeInt(_mount.getFullImmunityDuration());
				msg.writeInt(_mount.getFullImmunityIteration());
			}
			msg.writeUtf8String(_mount.getAIId());
			msg.writeByte(combatSkills.size());
			for(int i = 0; i < combatSkills.size(); i++) {
				ICombatSkillData skillData = combatSkills.get(i);
				msg.writeInt(skillData.getSkillTemplateId());
				msg.writeByte(skillData.getLv());
			}
		}
		msg.writeByte(_job == null ? 0 : _job.getJobType());
		msg.writeByte(equipmentResList.size());
		IRoleEquipShowData showData;
		IRoleEquipShowData weapon = null;
		for(int i = 0; i < equipmentResList.size(); i++) {
			showData = equipmentResList.get(i);
			msg.writeByte(showData.getPart());
			msg.writeUtf8String(showData.getRes());
			if(showData.getPart() == KEquipmentTypeEnum.主武器.sign) {
				weapon = showData;
			}
		}
		msg.writeUtf8String(fashionRes);
		msg.writeInt(equipSetRes[0]);
		msg.writeInt(equipSetRes[1]);
		msg.writeBoolean(weapon == null ? false : weapon.getQuality() == KItemQualityEnum.无敌的);
		if (_memberType == MEMBER_TYPE_BLOCK) {
			msg.writeInt(KCombatManager.getObstStateByAttack(_srcObjTemplateId));
			msg.writeInt(KCombatManager.getObstStateAfterDestroyed(_srcObjTemplateId));
			msg.writeBoolean(KCombatManager.willObstEffectTarget(_srcObjTemplateId));
		}
		msg.writeByte(_permanentStateMap.size());
		for(Iterator<ICombatState> itr = _permanentStateMap.values().iterator(); itr.hasNext();) {
			msg.writeInt(itr.next().getIconResId());
		}
		//后续的掉落信息，交给战场去处理
	}
	
	@Override
	public void switchWeapon(boolean switchToSecond) {
		if (switchToSecond) {
			if (_secondWeapon == null) {
				return;
			} else {
//				switch (_job) {
//				case GUNMAN:
//					// 枪械师切换弹夹
//					_secondWeaponDmPct = _secondWeapon.getMachineGunDmPct();
//					break;
//				case SHADOW:
//					_secondWeaponDmPct = _secondWeapon.getCohesionPct();
//					_secondWeaponFixedDm = _secondWeapon.getCohesionFixedDm();
//					break;
//				case WARRIOR:
////					_dmBlock = _secondWeapon.getBlock();
//					break;
//				}
				switch(_secondWeapon.getType()) {
				case ISecondWeapon.SECOND_WEAPON_TYPE_MACHINE_GUN:
					_secondWeaponDmPct = _secondWeapon.getMachineGunDmPct() + UtilTool.TEN_THOUSAND_RATIO_UNIT;
					break;
				case ISecondWeapon.SECOND_WEAPON_TYPE_BROADSWORD:
					_secondWeaponDmPct = _secondWeapon.getCohesionPct();
					_secondWeaponFixedDm = _secondWeapon.getCohesionFixedDm();
					break;
				case ISecondWeapon.SECOND_WEAPON_TYPE_SHIELD:
					break;
				}
				_usingSecondWeapon = true;
			}
		} else {
//			switch (_job) {
//			case GUNMAN:
//				_secondWeaponDmPct = 0;
//				break;
//			case SHADOW:
////				_cohesionPct = 0;
//				_secondWeaponFixedDm = 0;
//				_secondWeaponDmPct = 0;
//				break;
//			case WARRIOR:
//				_dmBlock = 0;
//				break;
//			}
			switch (this._secondWeapon.getType()) {
			case ISecondWeapon.SECOND_WEAPON_TYPE_MACHINE_GUN:
				_secondWeaponDmPct = 0;
				break;
			case ISecondWeapon.SECOND_WEAPON_TYPE_BROADSWORD:
				_secondWeaponDmPct = 0;
				_secondWeaponFixedDm = 0;
				break;
			case ISecondWeapon.SECOND_WEAPON_TYPE_SHIELD:
				_dmBlock = 0;
				break;
			}
			_usingSecondWeapon = false;
		}
	}
	
	@Override
	public void processCohension(int cohensionTime, int clientResult) {
//		if (_usingSecondWeapon) {
//			int pct = UtilTool.calculatePercentageL(cohensionTime, _secondWeapon.getCohesionPct(), true);
//			_secondWeaponDmPct = UtilTool.calculateTenThousandRatio(_cohesionPct, pct);
//			if (_secondWeaponDmPct < KCombatConfig.getCohesionDmMin()) {
//				_secondWeaponDmPct = KCombatConfig.getCohesionDmMin();
//			}
//			if (_secondWeaponDmPct != clientResult) {
//				if (Math.abs(_secondWeaponDmPct - clientResult) < 101) {
//					// 误差在1%以内
//					_secondWeaponDmPct = clientResult;
//				}
//			}
//		}
	}
	
	@Override
	public void switchBlockStatus(boolean start) {
		if (start && _usingSecondWeapon && _secondWeapon.getType() == ISecondWeapon.SECOND_WEAPON_TYPE_SHIELD) {
			this._dmBlock = _secondWeapon.getBlock();
//			LOGGER.info("切换到格挡状态！角色id：{}，格挡值：{}", _srcObjId, _dmBlock);
		} else {
			this._dmBlock = 0;
//			LOGGER.info("切换到普通状态！角色id：{}，格挡值：{}", _srcObjId, _dmBlock);
		}
	}
	
	@Override
	public void setEscape(boolean pEscape) {
		this._escape = pEscape;
	}
	
	@Override
	public boolean isEscape() {
		return this._escape;
	}

	@Override
	public long getSrcObjId() {
		return _srcObjId;
	}

	@Override
	public byte getSrcObjType() {
		return _srcObjType;
	}
	
	@Override
	public int getSrcObjTemplateId() {
		return _srcObjTemplateId;
	}

	@Override
	public byte getMemberType() {
		return _memberType;
	}
	
	@Override
	public boolean isGeneralMonster() {
		switch (this._memberType) {
		case MEMBER_TYPE_BARREL_MONSTER:
		case MEMBER_TYPE_BOSS_MONSTER:
		case MEMBER_TYPE_ELITIST_MONSTER:
		case MEMBER_TYPE_MONSTER:
		case MEMBER_TYPE_ROLE_MONSTER:
		case MEMBER_TYPE_PET_MONSTER:
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isNarrowMonster() {
		switch (this._memberType) {
		case MEMBER_TYPE_BARREL_MONSTER:
		case MEMBER_TYPE_BOSS_MONSTER:
		case MEMBER_TYPE_ELITIST_MONSTER:
		case MEMBER_TYPE_MONSTER:
			return true;
		}
		return false;
	}

	@Override
	public short getShadowId() {
		return _shadowId;
	}

	@Override
	public byte getForceType() {
		return _forceType;
	}

	@Override
	public String getName() {
		return _name;
	}
	
	@Override
	public int getLv() {
		return _lv;
	}
	
	@Override
	public boolean canBeAttacked() {
		return _canBeAttacked || _invincible;
	}
	
	@Override
	public boolean isHang() {
		return _hang;
	}
	
	@Override
	public boolean canOperate() {
//		if(this._memberType == MEMBER_TYPE_ROLE) {
//			System.out.println();
//		}
		return !_hang;
	}
	
	@Override
	public boolean isInDiffForce(ICombatMember member) {
		return this._forceType != member.getForceType();
	}
	
	@Override
	public void resume() {
		this._hang = false;
	}
	
	@Override
	public float getX() {
		return _x;
	}
	
	@Override
	public float getY() {
		return _y;
	}

	@Override
	public long getCurrentHp() {
		return _currentHp;
	}
	
	@Override
	public long getSrcMaxHp() {
		return _srcMaxHp;
	}

	@Override
	public long getMaxHp() {
		return _maxHp;
	}

	@Override
	public int getCurrentEnergy() {
		return _currentEnergy;
	}

	@Override
	public int getCurrentEnergyBean() {
		return _currentEnergyBean;
	}

	@Override
	public int getHpRecovery() {
		return _hpRecovery;
	}
	
	@Override
	public int getAtkCountPerTime() {
		return _atkCountPerTime;
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
	public int getCritMultiple() {
		return _critMultiple;
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
	public int getSkillDmPctInc() {
		return _skillDmPctInc;
	}
	
	@Override
	public int getDmReducePct() {
		return _dmReducePct;
	}
	
	@Override
	public int getKilledEnergy() {
		return _killedEnergy;
	}
	
	@Override
	public long getDeadTime() {
		return _deadTime;
	}

	@Override
	public boolean isAlive() {
		return _alive;
	}
	
	@Override
	public void sentenceToDead(long happenTime) {
		this.die(true, happenTime);
	}

	@Override
	public void increaseHp(long quantity) {
		if (quantity < 0) {
			quantity = Math.abs(quantity);
		}
		long result = this._currentHp + quantity;
		long maxHp = this.getMaxHp();
		if (result > maxHp) {
			result = maxHp;
		}
		this._currentHp = result;
	}

	@Override
	public void decreaseHp(long quantity, long happenTime) {
		if (/*this._memberType != MEMBER_TYPE_VEHICLE && */this._alive) {
			// 机甲不执行hp扣减
			if (quantity < 0) {
				quantity = Math.abs(quantity);
			}
			this._currentHp -= quantity;
			if (this._currentHp <= 0) {
				this._currentHp = 0;
				this.die(true, happenTime);
			}
		}
	}

	@Override
	public void increaseEnergy(int quantity) {
		int add = 0;
		int pre = this._currentEnergy;
		this._currentEnergy += quantity;
		if (this._currentEnergy >= this._maxEnergy) {
			if (this._currentEnergyBean < this._maxEnergyBean) {
				while (this._currentEnergy > 0) {
					add += this._currentEnergy - pre;
					this._currentEnergyBean++;
					this._currentEnergy = this._currentEnergy - this._maxEnergy;
					if (this._currentEnergy < this._maxEnergy) {
						break;
					}
					if (this._maxEnergyBean < this._currentEnergyBean) {
						break;
					}
					pre = this._maxEnergy;
				}
			} else {
				this._currentEnergy = this._maxEnergy;
				add = this._currentEnergy - pre;
			}
		} else {
			add = quantity;
		}
		this._recorder.recordEnergyRecovery(add, _currentEnergy, _currentEnergyBean);
	}
	
	@Override
	public void registPermanentEffect(ICombatEventListener effect) {
		if (effect != null) {
			this._permanentEffect.add(effect);
		}
	}
	
	@Override
	public void summonMount(long happenTime) {
		if (this._hang) {
			ICombat.LOGGER.error("召唤者：[{},{}]处于挂起状态，不能重复召唤机甲！", this._shadowId, this._name);
			return;
		}
		if (this._mount != null /*&& this.decreaseEnergyBean(_mountConsumeBeans)*/) {
//			int beans = _currentEnergyBean;
			int time = _mount.getDurationMap().get(1);
			if (time > 0 && this.decreaseEnergyBean(1)) {
				this._mount.setDuration(time);
				ICombatMember mountMember = this._combat.addMount(this, _mount, happenTime);
				this._hang = true;
				if (this._mount.getDuration() > 0) {
//					mountMember.getSkillActor().changeCombatAttr(KGameAttrType.MAX_HP, (int) this._srcMaxHp, true);
//					mountMember.getSkillActor().changeCombatAttr(KGameAttrType.ATK, this._srcAtk, true);
//					mountMember.getSkillActor().changeCombatAttr(KGameAttrType.DEF, this._srcDef, true);
//					mountMember.getSkillActor().changeCombatAttr(KGameAttrType.HIT_RATING, this._srcHitRating, true);
//					mountMember.getSkillActor().changeCombatAttr(KGameAttrType.DODGE_RATING, this._srcDodgeRating, true);
//					mountMember.getSkillActor().changeCombatAttr(KGameAttrType.CRIT_RATING, this._srcCritRating, true);
//					mountMember.getSkillActor().changeCombatAttr(KGameAttrType.RESILIENCE_RATING, this._srcResilienceRating, true);
					int hpPct = UtilTool.calculatePercentageL(this._currentHp, this._maxHp, true);
					long calHp = UtilTool.calculateTenThousandRatioL(mountMember.getMaxHp(), hpPct);
					if (mountMember.getCurrentHp() > calHp) {
						mountMember.decreaseHp(mountMember.getCurrentHp() - calHp, happenTime);
					} else {
						mountMember.increaseHp(calHp - mountMember.getCurrentHp());
					}
					mountMember.registPermanentEffect(new KMountDieEvent(this));
//					ICombatState tempState;
//					for(Iterator<ICombatState> itr = _stateMap.values().iterator(); itr.hasNext();) {
//						tempState = itr.next();
//						if(tempState.isCycState()) {
//							continue;
//						} else {
//							mountMember.getSkillActor().addState(mountMember, tempState.getStateTemplateId(), tempState.getStartTimeMillis());
//						}
//					}
//					ICombat.LOGGER.info("机甲属性：maxHp={},atk={},def={},hitRating={},dodgeRating={},critRating={},resilenceRating={},defIgnore={},skillDmInc={},dmReducePct={},hpAbsorb={},cdReduce={}", 
//							mountMember.getMaxHp(), mountMember.getAtk(), mountMember.getDef(), mountMember.getHitRating(), mountMember.getDodgeRating(), mountMember.getCritRating(), mountMember.getResilienceRating(), mountMember.getDefIgnore(), mountMember.getSkillDmPctInc(), mountMember.getDmReducePct(), mountMember.getHpAbsorb());
				}
				resetRecorder(mountMember, this._recorder);
			}
		}
	}
	
	@Override
	public void reduceSurviveTime(long reduceCount) {
		this._terminateTime -= reduceCount;
	}
	
	@Override
	public long getTerminateTime() {
		return this._terminateTime;
	}
	
	@Override
	public boolean isTerminateByTime() {
		return _terminateByTime;
	}
	
	@Override
	public boolean isAtkByPercentage() {
		return _atkByPercentage;
	}
	
	@Override
	public boolean isFullImmunity() {
		return _fullImmunity.get() > 0;
	}
	
	@Override
	public int getDmBlock() {
		return _dmBlock;
	}
	
	@Override
	public int getSecondWeaponDmPct() {
		return _secondWeaponDmPct;
	}
	
	@Override
	public int getSecondWeaponFixedDm() {
		return _secondWeaponFixedDm;
	}
	
	@Override
	public void blockSuperSkill() {
		if (this._skillMap.size() > 0) {
			for (Iterator<Map.Entry<Integer, KCombatSkill>> itr = _skillMap.entrySet().iterator(); itr.hasNext();) {
				if (itr.next().getValue().getSkillData().isSuperSkill()) {
					itr.remove();
				}
			}
		}
	}
	
	@Override
	public void blockMount() {
		if(this._mount != null) {
			KCombatEntrancePool.returnEntrance(_mount);
			this._mount = null;
		}
	}
	
	//**** 从ICombatMember 实现的方法 END

	//**** 从ICombatSkillActor 实现的方法 BEGIN
	@Override
	public void recordSkillUsed(int skillTemplateId, short targetId, String useCode, long happenTime) {
		KCombatSkill skill = this._skillMap.get(skillTemplateId);
		if (skill != null) {
//			LOGGER.info("技能:{}，useCode:{}，结算时间:{}", skillTemplateId, useCode, happenTime);
			skill.recordSkillUsed(useCode, targetId, happenTime);
		}
	}
	
	@Override
	public void recordSkillUse(int skillTemplateId, String useCode, long happenTime) {
		KCombatSkill combatSkill = this._skillMap.get(skillTemplateId);
		if (combatSkill != null) {
			combatSkill.recordSkillUsed(useCode, happenTime);
		}
	}
	
	@Override
	public int getSkillSettleCount(int skillTemplateId, String useCode) {
		KCombatSkill skill = this._skillMap.get(skillTemplateId);
		if(skill != null) {
			return skill.getSettleTimes(useCode);
		}
		return 0;
	}
	
	@Override
	public boolean isTimeInSettleRecord(int skillTemplateId, String useCode, long time) {
		KCombatSkill skill = this._skillMap.get(skillTemplateId);
		if (skill != null) {
			return skill.isTimeInSettleRecord(useCode, time);
		}
		return true;
	}
//	
//	@Override
//	public boolean isTimeHasBeenSettleOfThisTarget(int skillTemplateId, short targetId, String useCode, long happenTime) {
//		// 检查技能结算时间是否被结算过，这里是因为，客户端有可能把两条同一时间的结算，分开两条消息
//		// 发到服务器，此时，服务器可能因为上次的一条消息，而已经吧技能的计算次数计算满了，所以这里
//		// 要重新检查下，这个时间，是否已经被结算，如果已经被结算，那么下一条消息，只要结算时间还是
//		// 这个时间，也有效
//		KCombatSkill skill = this._skillMap.get(skillTemplateId);
//		if(skill != null) {
////			return skill.hasBeenSettleBefore(useCode, happenTime);
//			return skill.hasTargetBeenSettleBefore(useCode, happenTime, targetId);
//		}
//		return false;
//	}
	
	@Override
	public boolean isTimeFirstSettle(int skillTemplateId, String useCode, long happenTime) {
		KCombatSkill skill = this._skillMap.get(skillTemplateId);
		if (skill != null) {
			return skill.isTimeFirstSettleOfThisCode(useCode, happenTime);
		}
		return false;
	}
	
	@Override
	public int getTargetSettleCount(int skillTemplateId, String useCode, short targetId, long happenTime) {
		KCombatSkill skill = this._skillMap.get(skillTemplateId);
		if (skill != null) {
			return skill.getTargetSettleTimes(useCode, targetId, happenTime);
		}
		return 0;
	}
	
	@Override
	public void recordSkillCoolDown(int skillTemplateId, long happenTime, long cooldownMillis) {
		KCombatSkill combatSkill = this._skillMap.get(skillTemplateId);
		if(combatSkill != null) {
			if(_cdReduce > 0) {
				cooldownMillis -= UtilTool.calculateTenThousandRatioL(cooldownMillis, _cdReduce);
			}
			combatSkill.setCoolDownEndTime(happenTime + cooldownMillis);
//			ICombat.LOGGER.info("设置技能cd时间，技能id：{}，当前时间：{}，cd时间：{}", skillTemplateId, happenTime, cooldownMillis);
		}
	}
	
	@Override
	public void reduceSkillCoolDown(int skillTemplateId, int newCdMillis) {
		KCombatSkill combatSkill = this._skillMap.get(skillTemplateId);
		if(combatSkill != null) {
			combatSkill.reduceCoolDownTime(newCdMillis);
		}
	}
	
	@Override
	public boolean isCanUseSkill(int skillTemplateId, String useCode, long skillUseTime) {
		KCombatSkill combatSkill = this._skillMap.get(skillTemplateId);
		if(combatSkill != null) {
			if (!useCode.equals(combatSkill.getCurrentUseCode())) {
				if(combatSkill.getSkillData().isSuperSkill()) {
					if(this._currentEnergyBean < KCombatConfig.getSuperSkillConsumeEnergyBeanCount()) {
						return false;
					}
				}
				return combatSkill.isCoolDownFinished(skillUseTime);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isNewSettle(int skillTemplateId, String useCode) {
		KCombatSkill combatSkill = this._skillMap.get(skillTemplateId);
		if (combatSkill != null) {
			return combatSkill.isNewSettle(useCode);
		}
		return false;
	}
	
	@Override
	public boolean executeSkillConsume(int skillTemplateId, String useCode) {
		KCombatSkill combatSkill = this._skillMap.get(skillTemplateId);
		if(combatSkill != null) {
			if (!useCode.equals(combatSkill.getCurrentUseCode())) {
				if(combatSkill.getSkillData().isSuperSkill()) {
					return this.decreaseEnergyBean(KCombatConfig.getSuperSkillConsumeEnergyBeanCount());
				} else {
					return true;
				}
			} else {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public ICombatSkillExecution getSkillExecution(int skillTemplateId) {
		KCombatSkill skill = this._skillMap.get(skillTemplateId);
		if(skill != null) {
			return KCombatManager.getSkillExecution(skill.getSkillData().getSkillTemplateId(), skill.getSkillData().getLv());
		}
		return null;
	}
	
	@Override
	public boolean decreaseEnergyBean(int count) {
		if(this._currentEnergyBean < count) {
			return false;
		} else {
			this._currentEnergyBean -= count;
			this._combat.addOperationResult(new KEnergyRecoveryResult(this._shadowId, 0, _currentEnergy, _currentEnergyBean));
			return true;
		}
	}

	@Override
	public void changeCombatAttr(KGameAttrType attrType, int quantity, boolean add) {
		if (!add) {
			quantity = -Math.abs(quantity);
		}
		switch (attrType) {
		case MAX_HP:
		case MAX_ENERGY_PCT:
			if (attrType.isPercentageType) {
				this._addMaxHpPct += quantity;
			} else {
				this._addMaxHp += quantity;
			}
//			long pre = _maxHp;
//			this._maxHp = this.calculateAttribute(_srcMaxHp, _addMaxHp, _addMaxHpPct);
			this._maxHp = this._srcMaxHp + _addMaxHp;
			this._maxHp += UtilTool.calculateTenThousandRatioL(this._maxHp, _addMaxHpPct);
			this.notifyMaxHpChange();
//			if(this._mount != null) {
//				this._mount.notifyMasterMaxHpChange(pre, this._maxHp);
//			}
			break;
		case ATK:
		case ATK_PCT:
			if (attrType.isPercentageType) {
				this._addAtkPct += quantity;
			} else {
				this._addAtk += quantity;
			}
			this._atk = this.calculateAttribute(_srcAtk, _addAtk, _addAtkPct);
			break;
		case DEF:
		case DEF_PCT:
			if (attrType.isPercentageType) {
				this._addDefPct += quantity;
			} else {
				this._addDef += quantity;
			}
			this._def = this.calculateAttribute(_srcDef, _addDef, _addDefPct);
			break;
		case HIT_RATING:
		case HIT_RATING_PCT:
			if (attrType.isPercentageType) {
				this._addHitRatingPct += quantity;
			} else {
				this._addHitRating += quantity;
			}
			this._hitRating = this.calculateAttribute(_srcHitRating, _addHitRating, _addHitRatingPct);
			break;
		case DODGE_RATING:
		case DODGE_RATING_PCT:
			if(attrType.isPercentageType) {
				this._addDodgeRatingPct += quantity;
			} else {
				this._addDodgeRating += quantity;
			}
			this._dodgeRating = this.calculateAttribute(_srcDodgeRating, _addDodgeRating, _addDodgeRatingPct);
			break;
		case CRIT_RATING:
		case CRIT_RATING_PCT:
			if(attrType.isPercentageType) {
				this._addCritRatingPct += quantity;
			} else {
				this._addCritRating += quantity;
			}
			this._critRating = this.calculateAttribute(_srcCritRating, _addCritRating, _addCritRatingPct);
			break;
		case RESILIENCE_RATING:
		case RESILIENCE_RATING_PCT:
			if(attrType.isPercentageType) {
				this._addResilienceRatingPct += quantity;
			} else {
				this._addResilienceRating += quantity;
			}
			this._resilienceRating = this.calculateAttribute(_srcResilienceRating, _addResilienceRating, _addResilienceRatingPct);
			break;
		case CRIT_MULTIPLE:
			this._addCritMultiple += quantity;
			this._critMultiple = this.calculateAttribute(_srcCritMultiple, _addCritMultiple, 0);
			break;
		case DEF_IGNORE:
			this._addDefIgnore += quantity;
			this._defIgnore = this.calculateAttribute(_srcDefIgnore, _addDefIgnore, 0);
			break;
		case MOVE_SPEED_X:
			this._addSpeedX += quantity;
			this._addSpeedY += quantity / 2;
			this._speedX = this.calculateAttribute(_srcSpeedX, _addSpeedX, _addSpeedXPct);
			this._speedY = this.calculateAttribute(_srcSpeedY, _addSpeedY, _addSpeedYPct);
			break;
		case SKILL_DM_INC:
			this._addSkillDmPctInc += quantity;
			this._skillDmPctInc = this.calculateAttribute(_srcSkillDmPctInc, _addSkillDmPctInc, 0);
			break;
		case CD_REDUCE:
			this._addCdReduce += quantity;
			this._cdReduce = this.calculateAttribute(_srcCdReduce, _addCdReduce, 0);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void addState(ICombatMember operator, int stateId, long happenTime) {
		if(this._canAddBuff) {
//			LOGGER.info("buff, id={}, time={}", stateId, happenTime);
			ICombatStateTemplate template = KCombatManager.getCombatStateTemplate(stateId);
			ICombatState preState = this._stateMap.get(template.getGroupId());
			if (preState != null) {
				if (preState.getLevel() > template.getLevel()) {
					// 已有状态等级大于将要添加的状态，直接忽略
					return;
				} else if (preState.getLevel() == template.getLevel()) {
					// 已有状态等级等于将要添加的状态，延长时间
					preState.notifyExtend(this._combat, this, happenTime);
					// LOGGER.info("buff, id={}, time={}, notifyExtend",
					// stateId, happenTime);
					_combat.addExtractOperation(new KCheckStateOperation(_shadowId, preState.getStateTemplateId(), preState.getEndTime()));
					return;
				} else {
					// 已有状态等级小于将要添加的状态，先把状态移除
					this._stateMap.remove(template.getGroupId());
					preState.notifyRemoved(this._combat, this);
				}
			}
			ICombatState nowState = KCombatManager.getCombatState(operator, template);
			this._stateMap.put(nowState.getGroupId(), nowState);
			nowState.notifyAdded(_combat, this, happenTime);
			_combat.addExtractOperation(new KCheckStateOperation(_shadowId, nowState.getStateTemplateId(), nowState.getEndTime()));
		} else {
			ICombat.LOGGER.warn("[{},{}]免疫buff", _name, _shadowId);
		}
	}
	
	@Override
	public void checkAndRemoveState(int stateTemplateId, long happenTime) {
		ICombatState state;
		for (Iterator<Map.Entry<Integer, ICombatState>> itr = _stateMap.entrySet().iterator(); itr.hasNext();) {
			state = itr.next().getValue();
			if (state.getStateTemplateId() == stateTemplateId) {
				if (state.getEndTime() <= happenTime) {
					state.notifyRemoved(this._combat, this);
					itr.remove();
				}
				break;
			}
		}
	}
	
	@Override
	public void addTemporaryEffec(int effectId) {
		KCombatSpecialEffect effect = KCombatManager.getSpecialEffect(effectId);
		this._temporaryEffect.put(effectId, effect);
	}
	
	@Override
	public void removeRemporaryEffect(int effectId) {
		this._temporaryEffect.remove(effectId);
	}
	
	@Override
	public void handleFaint(boolean add) {
		if(add) {
			_faint.incrementAndGet();
		} else {
			_faint.decrementAndGet();
		}
	}
	
	@Override
	public void handleFreeze(boolean add) {
		if(add) {
			_freeze.incrementAndGet();
		} else {
			_freeze.decrementAndGet();
		}
	}
	
	@Override
	public void handleFullImmunity(boolean add) {
		if (add) {
			_fullImmunity.incrementAndGet();
		} else {
			_fullImmunity.decrementAndGet();
		}
	}
	
	@Override
	public void handleInvincible(boolean add) {
		this._invincible = add;
	}
	
	@Override
	public void summon(int minionTemplateId, int count, long happenTime) {
		if (this._minions != null) {
			ICombatMinion minion = this._minions.get(minionTemplateId);
			if (minion != null) {
				for (int i = 0; i < count; i++) {
					this._combat.addMinion(this, minion, happenTime);
				}
			}
		}
	}
	//**** 从ICombatSkillActor实现的方法 END
	
	private static class KTerminateByTimeOperation implements IOperation {

		private ICombatMember _prepareMember;
		private long _happenTime;
		
		KTerminateByTimeOperation(ICombatMember member, long happenTime) {
			this._prepareMember = member;
			this._happenTime = happenTime;
		}
		
		@Override
		public IOperationResult executeOperation(ICombat combat) {
			if (_prepareMember.isAlive()) {
//				LOGGER.info("{}, terminate, time={}", _prepareMember.getName(), _happenTime);
				_prepareMember.sentenceToDead(_happenTime);
			}
			return null;
		}

		@Override
		public long getOperationTime() {
			return _happenTime;
		}
		
		@Override
		public int getPriority() {
			return PRIORITY_URGENT;
		}
		
		@Override
		public void notifyMountAdded(ICombatMember master, ICombatMember mount) {
			
		}
		
		@Override
		public void notifyMountReleased(ICombatMember master, ICombatMember mount) {
			
		}

	}
	
//	private static class KResumeOperation implements IOperation {
//		
//		private KCombatMemberImpl _member;
//		private long _happenTime;
//		
//		KResumeOperation(KCombatMemberImpl member, long happenTime) {
//			this._member = member;
//			this._happenTime = happenTime;
//		}
//		
//		@Override
//		public IOperationResult executeOperation(ICombat combat) {
//			_member._hang = false;
//			return null;
//		}
//		@Override
//		public long getOperationTime() {
//			return _happenTime;
//		}
//	}
	
	private static class KMountDieEvent implements ICombatEventListener {

		private KCombatMemberImpl _master;
		
		
		public KMountDieEvent(KCombatMemberImpl pMaster) {
			this._master = pMaster;
		}

		@Override
		public int getEventId() {
			return ICombatEvent.EVENT_SELF_DEAD;
		}

		@Override
		public void run(ICombat combat, ICombatMember operator, long happenTime) {
			_master._hang = false;
			ICombat.LOGGER.info("masterId={}, resume normal, time={}", _master.getShadowId(), happenTime);
			if (operator.getCurrentHp() <= 0) {
				_master.decreaseHp(_master.getCurrentHp(), happenTime);
			} else {
				int pct = UtilTool.calculatePercentageL(operator.getCurrentHp(), operator.getMaxHp(), true);
				long hp = UtilTool.calculateTenThousandRatioL(_master.getMaxHp(), pct);
				if (_master.getCurrentHp() > hp) {
					long decreaseHp = _master.getCurrentHp() - hp;
					_master.decreaseHp(decreaseHp, happenTime);
					ICombat.LOGGER.info("masterId={},hp pct={}, decrease hp={}, currentHp={}", _master.getShadowId(), pct, decreaseHp, _master.getCurrentHp());
				} else if (_master.getCurrentHp() < hp) {
					long increaseHp = hp - _master.getCurrentHp();
					_master.increaseHp(increaseHp);
					ICombat.LOGGER.info("masterId={},hp pct={}, increase hp={}, currentHp={}", _master.getShadowId(), pct, increaseHp, _master.getCurrentHp());
				} else {
					return;
				}
				
				combat.addSyncHpShadowId(_master._shadowId);
			}
		}

		@Override
		public boolean isEffective(long happenTime) {
			return true;
		}
		
	}
	
	private static class KCheckStateOperation implements IOperation {

		private short _memberId;
		private int _stateTemplateId;
		private long _endTime;
		
		
		public KCheckStateOperation(short pMemberId, int pStateTemplateId, long pEndTime) {
			this._memberId = pMemberId;
			this._stateTemplateId = pStateTemplateId;
			this._endTime = pEndTime;
//			LOGGER.info("KCheckStateOperation#memberId={},stateTemplateId={},endTime={}", _memberId, _stateTemplateId, _endTime);
		}
		
		@Override
		public IOperationResult executeOperation(ICombat combat) {
			ICombatMember member = combat.getCombatMember(_memberId);
			member.getSkillActor().checkAndRemoveState(_stateTemplateId, _endTime);
			return null;
		}

		@Override
		public long getOperationTime() {
			return _endTime;
		}
		
		@Override
		public int getPriority() {
			return PRIORITY_URGENT;
		}
		
		@Override
		public void notifyMountAdded(ICombatMember master, ICombatMember mount) {

		}

		public void notifyMountReleased(ICombatMember master, ICombatMember mount) {

		}
	}

}
