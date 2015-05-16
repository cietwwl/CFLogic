package com.kola.kmp.logic.competition.teampvp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGameGender;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;

/**
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPRobotTemplate {

	/**
	 * 最小等级
	 */
	public final int minLevel;
	/**
	 * 最大等级
	 */
	public final int maxLevel;
	/**
	 * 属性集合
	 */
	public final Map<KGameAttrType, Integer> attrMap;
	/**
	 * 技能集合
	 */
	public final List<KCompetitionRobotSkill> skillList;
	/**
	 * 
	 */
	public final KJobTypeEnum job;
	/**
	 * 
	 */
	private boolean _isMale;
	/**
	 * 随从集合，key=几率，value=模板
	 */
	private final Map<Integer, List<Integer>> _petMap;
	/**
	 * 
	 */
	private final Map<Integer, List<Integer>> _mountMap;
	/**
	 * 
	 */
	private int _petTotalRate;
	/**
	 * 
	 */
	private int _mountTotalRate;
	
	public KTeamPVPRobotTemplate(KGameExcelRow row, int[] skillIds, int[] skillLevels, String petData, String mountData, KJobTypeEnum pJob) throws Exception {
		this.minLevel = row.getInt("minLevel");
		this.maxLevel = row.getInt("maxLevel");
		int[] attrKeys = UtilTool.getStringToIntArray(row.getData("attributeKeys"), ",");
		int[] attrValues = UtilTool.getStringToIntArray(row.getData("attributeValues"), ",");
		this.attrMap = Collections.unmodifiableMap(KGameUtilTool.genAttribute(attrKeys, attrValues, true));
		List<KCompetitionRobotSkill> tempSkillList = new ArrayList<KCompetitionRobotSkill>();
		for (int i = 0; i < skillIds.length; i++) {
			tempSkillList.add(new KCompetitionRobotSkill(skillIds[i], skillLevels[i]));
		}
		this.skillList = Collections.unmodifiableList(tempSkillList);
		this.job = pJob;
		Map<Integer, List<Integer>> petMap = new LinkedHashMap<Integer, List<Integer>>();
		_petMap = Collections.unmodifiableMap(petMap);
		_petTotalRate = initCompanyData(petData, petMap);
		
		Map<Integer, List<Integer>> mountMap = new LinkedHashMap<Integer, List<Integer>>();
		_mountMap = Collections.unmodifiableMap(mountMap);
		_mountTotalRate = initCompanyData(mountData, mountMap);
	}
	
	private int initCompanyData(String companyData, Map<Integer, List<Integer>> map) {
		int totalRate = 0;
		String[] petInfos = companyData.split(",");
		String[] singlePetInfo;
		for (int i = 0; i < petInfos.length; i++) {
			singlePetInfo = petInfos[i].split("\\*");
			totalRate += Integer.parseInt(singlePetInfo[2]);
			map.put(totalRate, Collections.unmodifiableList(Arrays.asList((Integer.parseInt(singlePetInfo[0])), Integer.parseInt(singlePetInfo[1]))));
		}
		return totalRate;
	}
	
	private List<Integer> getCompanyInfo(int totalRate, Map<Integer, List<Integer>> map){
		int rate = UtilTool.random(totalRate);
		Map.Entry<Integer, List<Integer>> entry;
		for (Iterator<Map.Entry<Integer, List<Integer>>> itr = map.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			if (rate < entry.getKey()) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	void onGameWorldInitComplete() {
		_isMale = KSupportFactory.getRoleModuleSupport().getRoleTemplateByJob(this.job.getJobType()).gender == KGameGender.MALE;
		List<Integer> list;
		int exCount = 0;
		for (Iterator<List<Integer>> itr = _petMap.values().iterator(); itr.hasNext();) {
			list = itr.next();
			if (KSupportFactory.getPetModuleSupport().getPetTemplate(list.get(0)) == null) {
				KTeamPVPManager.LOGGER.error("宠物模板不存在！模板id：{}，最少等级：{}，最大等级：{}，职业：{}", list.get(0), minLevel, maxLevel, job.getJobName());
				exCount++;
			}
		}
		for(Iterator<List<Integer>> itr = _mountMap.values().iterator(); itr.hasNext();) {
			list = itr.next();
			if (KSupportFactory.getMountModuleSupport().getMountTemplate(list.get(0)) == null) {
				KTeamPVPManager.LOGGER.error("机甲模板不存在！模板id：{}，最少等级：{}，最大等级：{}，职业：{}", list.get(0), minLevel, maxLevel, job.getJobName());
				exCount++;
			}
		}
		if(exCount > 0) {
			throw new RuntimeException("队伍竞技，机器人数据检验不通过！");
		}
	}
	
	boolean isMale() {
		return _isMale;
	}
	
	/**
	 * 返回随机的随从，{0}表示随从模板id，{1}表示随从等级
	 * @return
	 */
	List<Integer> getPetInfo() {
		return this.getCompanyInfo(_petTotalRate, _petMap);
	}
	
	/**
	 * 返回随机的坐骑，{0}表示座驾模板id，{1}表示座驾等级
	 * @return
	 */
	List<Integer> getMountInfo() {
		return this.getCompanyInfo(_mountTotalRate, _mountMap);
	}
	
	public static class KCompetitionRobotSkill implements ICombatSkillData {

		private int _skillTemplateId;
		private int _skillLv;

		public KCompetitionRobotSkill(int pSkillTemplateId, int pSkillLv) {
			this._skillTemplateId = pSkillTemplateId;
			this._skillLv = pSkillLv;
		}

		@Override
		public int getSkillTemplateId() {
			return _skillTemplateId;
		}

		@Override
		public int getLv() {
			return _skillLv;
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
