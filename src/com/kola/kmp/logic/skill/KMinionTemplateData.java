package com.kola.kmp.logic.skill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.kola.kmp.logic.combat.api.ICombatMinionTemplateData;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.npc.KNPCDataStructs.MonstUIData;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KObstructionTargetType;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;

/**
 * 
 * 召唤物数据
 * 
 * @author PERRY CHAN
 */
public class KMinionTemplateData implements ICombatMinionTemplateData {

	public static final int ATTR_GEN_BY_OWNER = 0;
	public static final int ATTR_GEN_BY_SELF = 1;
	
	public int templateId; // 模板id
	public String name; // 名字
	public int duration; // 持续时间
	public int genAttrType; // 属性生成规则
	public boolean canBeAttacked; // 能否被攻击
	public int targetType; // 目标类型
	public boolean is_armor; // 是否霸体
	public int armorTime; // 霸体时间
	public int armorIterval; // 霸体间隔
	public int walk_speed; // 行走速度
	private int _walk_speed_y; // 行走速度y
	public int per_anger; // 击杀的怒气
	public float atk_period;//攻击间隔
	public int atk_period_ivalue; // 攻击间隔（毫秒）
//	public int hp; //生命值
//	public int atk; // 攻击力
//	public int def; // 防御力
//	public int hit; // 命中等级
//	public int dodge; // 闪避等级
//	public int crit; // 暴击等级
	private int[] attribute_id;// 怪物属性编号
	private int[] attribute;// 怪物属性数量
	public int skill1; // 技能1
	public int skill2; // 技能2
	public int skill3; // 技能3
	public int vision; // 视野
	public int atk_hoot_res_id; // 叫嚣动作
	public int res_id; // 地图资源id
	public int monster_img; // 怪物半身像资源id
	public int monster_head; // 关务头像资源id
	public int attack_audios; // 普通攻击声效
	public int attack_scream_audios; // 普通喊叫声效
	public int hitted_scream_audios; // 受伤喊叫声
	public int dead_audio; // 死亡声效
	public int hitted_audios; // 击中声效
	public int hoot_audio; // 叫嚣声
	public int ko_scream_audio; // 死亡长啸声
	public int monsterUITemplateId;
	public String aiid;
	public int call_lvl; // 召唤物等级
	public int att_range;// 攻击距离
	public int atkCountPerTime; // 单次普通攻击的攻击次数
	private List<ICombatSkillData> _skillDatas;
	private List<ICombatSkillData> _skillDatasReadOnly;
	private boolean _genByOwner;
	private KObstructionTargetType _targetType;
	
	public Map<KGameAttrType, Integer> allEffects;
	
	private void checkAndAddSkill(int skillTemplateId) {
		if (skillTemplateId > 0) {
			if (KSupportFactory.getSkillModuleSupport().getMonsterSkillTemplate(skillTemplateId) != null) {
				_skillDatas.add(new KMinionSkill(skillTemplateId, 1));
			} else {
				throw new RuntimeException("召唤物技能不存在！技能编号是：" + skillTemplateId);
			}
		}
	}

	void onGameWorldInitComplete() throws Exception {
		this._walk_speed_y = this.walk_speed / 2;
		this._genByOwner = this.genAttrType == ATTR_GEN_BY_OWNER;
		this._targetType = KObstructionTargetType.getEnum(targetType);
		if (this._targetType == null) {
			throw new Exception("召唤物，目标类型为null！模板id：" + templateId);
		}
		_skillDatas = new ArrayList<ICombatSkillData>();
		this.checkAndAddSkill(skill1);
		this.checkAndAddSkill(skill2);
		this.checkAndAddSkill(skill3);
		_skillDatasReadOnly = Collections.unmodifiableList(_skillDatas);
		Map<KGameAttrType, Integer> tempEffects = null;
		try {
			tempEffects = KGameUtilTool.genAttribute(attribute_id, attribute, true);
		} catch (Exception e) {
			throw new Exception("生成召唤物属性出现异常，id=" + templateId, e);
		}
		if (tempEffects.isEmpty()) {
			throw new Exception("召唤物模板属性未配置 id=" + templateId);
		}
		allEffects = Collections.unmodifiableMap(tempEffects);
		if(this.armorTime > 0) {
			this.armorTime *= 1000; // 转化为毫秒
			this.armorIterval *= 1000; // 转化为毫秒
			if(this.armorIterval <= 0) {
				throw new RuntimeException("召唤物霸体间隔为0！召唤物id：" + this.templateId);
			}
		}
		this.is_armor = this.armorTime > 0;
		this.atk_period_ivalue = (int) (this.atk_period * 1000);
		MonstUIData monsterUIData = KSupportFactory.getNpcModuleSupport().getMonsterUIData(monsterUITemplateId);
		this.res_id = monsterUIData.res_id;
		this.monster_head = monsterUIData.monster_head;
		this.att_range = monsterUIData.att_range;
		this.atk_hoot_res_id = monsterUIData.atk_hoot;
		this.attack_audios = monsterUIData.attack_audios;
		this.hitted_scream_audios = monsterUIData.hitted_scream_audios;
		this.dead_audio = monsterUIData.dead_audio;
		this.hitted_audios = monsterUIData.hitted_audios;
		this.monster_img = monsterUIData.monster_img;
	}

	@Override
	public boolean canBeAttack() {
		return this.canBeAttacked;
	}

	@Override
	public byte getObjectType() {
		return OBJECT_TYPE_MINION;
	}

	@Override
	public int getTemplateId() {
		return templateId;
	}

	@Override
	public long getId() {
		return 0;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getHeadResId() {
		return monster_head;
	}

	@Override
	public int getInMapResId() {
		return res_id;
	}

	@Override
	public int getLevel() {
		return call_lvl;
	}

	@Override
	public long getCurrentHp() {
		return KGameUtilTool.getAttrValueSafely(this.allEffects, KGameAttrType.MAX_HP);
	}

	@Override
	public long getMaxHp() {
		return KGameUtilTool.getAttrValueSafely(this.allEffects, KGameAttrType.MAX_HP);
	}

	@Override
	public int getBattleMoveSpeedX() {
		return walk_speed;
	}

	@Override
	public int getBattleMoveSpeedY() {
		return _walk_speed_y;
	}

	@Override
	public int getVision() {
		return vision;
	}
	
	@Override
	public int getAtkCountPerTime() {
		return atkCountPerTime;
	}

	@Override
	public int getAtk() {
		return KGameUtilTool.getAttrValueSafely(this.allEffects, KGameAttrType.ATK);
	}

	@Override
	public int getDef() {
		return KGameUtilTool.getAttrValueSafely(this.allEffects, KGameAttrType.DEF);
	}

	@Override
	public int getHitRating() {
		return KGameUtilTool.getAttrValueSafely(this.allEffects, KGameAttrType.HIT_RATING);
	}

	@Override
	public int getDodgeRating() {
		return KGameUtilTool.getAttrValueSafely(this.allEffects, KGameAttrType.DODGE_RATING);
	}

	@Override
	public int getCritRating() {
		return KGameUtilTool.getAttrValueSafely(this.allEffects, KGameAttrType.CRIT_RATING);
	}
	
	@Override
	public int getResilienceRating() {
		return KGameUtilTool.getAttrValueSafely(this.allEffects, KGameAttrType.RESILIENCE_RATING);
	}
	
	@Override
	public int getCritMultiple() {
		return KGameUtilTool.getAttrValueSafely(this.allEffects, KGameAttrType.CRIT_MULTIPLE);
	}
	
	@Override
	public int getAtkRange() {
		return att_range;
	}
	
	@Override
	public int getAtkPeriod() {
		return atk_period_ivalue;
	}
	
	@Override
	public boolean isGenerateByOwner() {
		return _genByOwner;
	}
	
	@Override
	public int getDuration() {
		return duration;
	}
	
	@Override
	public KObstructionTargetType getTargetType() {
		return this._targetType;
	}
	
	@Override
	public boolean isFullImmunity() {
		return this.is_armor;
	}
	
	@Override
	public int getFullImmunityDuration() {
		return armorTime;
	}
	
	@Override
	public int getFullImmunityIteration() {
		return armorIterval;
	}
	
	@Override
	public List<ICombatSkillData> getAllSkills() {
		return this._skillDatasReadOnly;
	}
	
	@Override
	public String getAIId() {
		return aiid;
	}
	
	static class KMinionSkill implements ICombatSkillData {

		private int _skillTemplateId;
		private int _lv;
		
		/**
		 * 
		 */
		public KMinionSkill(int pSkillTemplateId, int pLv) {
			this._skillTemplateId = pSkillTemplateId;
			this._lv = pLv;
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
