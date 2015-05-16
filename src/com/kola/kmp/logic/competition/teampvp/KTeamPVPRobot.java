package com.kola.kmp.logic.competition.teampvp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.KGameGlobalConfig;
import com.kola.kmp.logic.combat.api.ICombatObjectBase;
import com.kola.kmp.logic.combat.api.ICombatRole;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.combat.api.ICombatSkillSupport;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.KRoleModuleConfig;
import com.kola.kmp.logic.role.KRoleTemplate;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.IRoleEquipShowData;
import com.kola.kmp.logic.util.KBattlePowerCalculator;
import com.kola.kmp.logic.util.KGameUtilTool;

/**
 * 
 * 机器人数据
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPRobot implements ICombatRole, ITeamPVPTeamMember, ICombatSkillSupport {

	private static final int[] _EMPTY_ARRAY = new int[0];
	
	private static final int[] _EQUIP_SET_RES = new int[2];
	
	private static final AtomicLong _ID_GENERATOR = new AtomicLong();
	
	private static final int _MAX_ENERGY = 100;
	
	private long _id;
	private String _name;
	private int _level;
	private int _battlePower;
	private KRoleTemplate _roleTemplate;
	private KTeamPVPRobotTemplate _robotTemplate;
	private List<ICombatSkillData> _skillDatas;
	private String _fashionRes;
	
	public KTeamPVPRobot(KTeamPVPRobotTemplate robotTemplate, String fashion) {
		this._id = _ID_GENERATOR.incrementAndGet();
//		String hex = Long.toHexString(this._id);
//		if (hex.length() < 4) {
//			StringBuilder strBld = new StringBuilder("R_0000");
//			strBld.replace(strBld.length() - hex.length(), strBld.length(), hex);
//			_name = strBld.toString();
//		} else {
//			_name = "R_" + hex.toString();
//		}
		this.init(robotTemplate, fashion);
	}
	
	void init(KTeamPVPRobotTemplate robotTemplate, String fashion) {
		_roleTemplate = KSupportFactory.getRoleModuleSupport().getRoleTemplateByJob(robotTemplate.job.getJobType());
		_robotTemplate = robotTemplate;
		_fashionRes = fashion;
		_name = KTeamPVPRandomName.randomRobotName(robotTemplate.isMale());
		_level = UtilTool.random(robotTemplate.minLevel, robotTemplate.maxLevel);
		_battlePower = KBattlePowerCalculator.calculateBattlePower(_robotTemplate.attrMap, _level, false);
		int[] indexes = new int[_robotTemplate.skillList.size()];
		int count = KSupportFactory.getSkillModuleSupport().getMaxSkillSlotCount() - 1;
		_skillDatas = new ArrayList<ICombatSkillData>(count);
		int random;
		int tempIndex;
		for(int i = 0; i < indexes.length; i++) {
			indexes[i] = i;
		}
		for (int i = 0; i < count; i++) {
			tempIndex = indexes.length - (i + 1);
			random = UtilTool.random(0, tempIndex);
			_skillDatas.add(_robotTemplate.skillList.get(indexes[random]));
			if (random != tempIndex) {
				int temp = indexes[tempIndex];
				indexes[tempIndex] = indexes[random];
				indexes[random] = temp;
			}
		}
	}
	
	@Override
	public boolean canBeAttack() {
		return true;
	}

	@Override
	public byte getObjectType() {
		return ICombatObjectBase.OBJECT_TYPE_ROLE_MONSTER;
	}

	@Override
	public int getTemplateId() {
		return _roleTemplate.templateId;
	}

	@Override
	public long getId() {
		return _id;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public int getHeadResId() {
		return _roleTemplate.headResId;
	}

	@Override
	public int getInMapResId() {
		return _roleTemplate.inMapResId;
	}

	@Override
	public int getLevel() {
		return _level;
	}

	@Override
	public long getCurrentHp() {
		return KGameUtilTool.getAttrValueSafely(_robotTemplate.attrMap, KGameAttrType.MAX_HP);
	}

	@Override
	public long getMaxHp() {
		return KGameUtilTool.getAttrValueSafely(_robotTemplate.attrMap, KGameAttrType.MAX_HP);
	}

	@Override
	public int getBattleMoveSpeedX() {
		return _roleTemplate.battleMoveSpeedX;
	}

	@Override
	public int getBattleMoveSpeedY() {
		return _roleTemplate.battleMoveSpeedY;
	}

	@Override
	public int getVision() {
		return _roleTemplate.vision;
	}

	@Override
	public int getAtk() {
		return KGameUtilTool.getAttrValueSafely(_robotTemplate.attrMap, KGameAttrType.ATK);
	}

	@Override
	public int getDef() {
		return KGameUtilTool.getAttrValueSafely(_robotTemplate.attrMap, KGameAttrType.DEF);
	}

	@Override
	public int getHitRating() {
		return KGameUtilTool.getAttrValueSafely(_robotTemplate.attrMap, KGameAttrType.HIT_RATING);
	}

	@Override
	public int getDodgeRating() {
		return KGameUtilTool.getAttrValueSafely(_robotTemplate.attrMap, KGameAttrType.DODGE_RATING);
	}

	@Override
	public int getCritRating() {
		return KGameUtilTool.getAttrValueSafely(_robotTemplate.attrMap, KGameAttrType.CRIT_RATING);
	}

	@Override
	public int getCritMultiple() {
		return KGameUtilTool.getAttrValueSafely(_robotTemplate.attrMap, KGameAttrType.CRIT_MULTIPLE);
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
		return KGameUtilTool.getAttrValueSafely(_robotTemplate.attrMap, KGameAttrType.DEF_IGNORE);
	}

	@Override
	public int getResilienceRating() {
		return KGameUtilTool.getAttrValueSafely(_robotTemplate.attrMap, KGameAttrType.RESILIENCE_RATING);
	}

	@Override
	public int getFaintResistRating() {
		return 0;
	}

	@Override
	public int getShortRaAtkItr() {
		return _roleTemplate.shortRaAtkItr;
	}

	@Override
	public int getLongRaAtkItr() {
		return _roleTemplate.longRaAtkItr;
	}

	@Override
	public int getShortRaAtkDist() {
		return _roleTemplate.shortRaAtkDist;
	}

	@Override
	public int getLongRaAtkDist() {
		return _roleTemplate.longRaAtkDist;
	}

	@Override
	public int[] getNormalAtkAudioResIdArray() {
		return _roleTemplate.normalAtkAudios;
	}

	@Override
	public int[] getOnHitAudioResIdArray() {
		return _EMPTY_ARRAY;
	}

	@Override
	public int[] getInjuryAudioResIdArray() {
		return _roleTemplate.injuryAudios;
	}

	@Override
	public int[] getDeadAudioResIdArray() {
		return _roleTemplate.deadAudios;
	}
	
	@Override
	public int getDmReducePct() {
		return 0;
	}
	
	@Override
	public int getSkillDmPctInc() {
		return 0;
	}

	@Override
	public int getCurrentEnergy() {
		return _MAX_ENERGY;
	}

	@Override
	public int getMaxEnergy() {
		return _MAX_ENERGY;
	}

	@Override
	public int getEnergyBean() {
		return KRoleModuleConfig.getMaxEnergyBean();
	}

	@Override
	public int getMaxEnergyBean() {
		return KRoleModuleConfig.getMaxEnergyBean();
	}

	@Override
	public int getHpRecovery() {
		return 0;
	}

	@Override
	public String getAIId() {
		return _roleTemplate.fightAI;
	}

	@Override
	public byte getJob() {
		return _roleTemplate.job;
	}

	@Override
	public int getBattlePower() {
		return _battlePower;
	}

	@Override
	public ICombatSkillSupport getSkillSupport() {
		return this;
	}

	@Override
	public int getVipLevel() {
		return 0;
	}

	@Override
	public List<IRoleEquipShowData> getEquipmentRes() {
		return Collections.emptyList();
	}

	@Override
	public String getFashionResId() {
		return _fashionRes;
	}

	@Override
	public int[] getEquipSetRes() {
		return _EQUIP_SET_RES;
	}

	@Override
	public List<ICombatSkillData> getUsableSkills() {
		return _skillDatas;
	}

	@Override
	public List<ICombatSkillData> getPassiveSkills() {
		return Collections.emptyList();
	}

}
