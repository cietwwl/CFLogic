package com.kola.kmp.logic.pet;

import java.util.ArrayList;
import java.util.List;

import com.koala.thirdpart.json.JSONObject;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.combat.api.ICombatSkillSupport;

/**
 * 
 * @author PERRY CHAN
 */
public class KPetSkillSet implements ICombatSkillSupport {

	private static final String KEY_SIZE_OF_SKILL_LIST = "S";
//	private static final String KEY_OPEN_SKILL_COUNT = "O";
//	private int _openSkillCount;
//	private Map<Integer, IPetSkill> _skillMap = new HashMap<Integer, IPetSkill>();
	private List<IPetSkill> _skillListReadOnly = new ArrayList<IPetSkill>();
	private List<IPetSkill> _activeSkills = new ArrayList<IPetSkill>();
	private List<IPetSkill> _passiveSkills = new ArrayList<IPetSkill>();
//	private Pet _owner;
//	
//	KPetSkillSet(Pet pPet) {
//		this._owner = pPet;
//	}
	
	String saveSkillInfo() throws Exception {
		JSONObject json = new JSONObject();
		json.put(KEY_SIZE_OF_SKILL_LIST, _skillListReadOnly.size());
//		Map.Entry<Integer, IPetSkill> entry;
//		for(Iterator<Map.Entry<Integer, IPetSkill>> itr = _skillMap.entrySet().iterator(); itr.hasNext();) {
//			entry = itr.next();
//			json.put(String.valueOf(entry.getKey()), entry.getValue().getSkillTemplateId() + ";" + entry.getValue().getLv());
//		}
//		json.put(KEY_OPEN_SKILL_COUNT, _openSkillCount);
		for(int i = 0; i < _skillListReadOnly.size(); i++) {
			json.put(String.valueOf(i), _skillListReadOnly.get(i).getSkillTemplateId() + ";" + _skillListReadOnly.get(i).getLv());
		}
		return json.toString();
	}
	
	void parseSkillInfo(String data) throws Exception {
		JSONObject obj = new JSONObject(data);
		int size = obj.optInt(KEY_SIZE_OF_SKILL_LIST, 0);
		String[] skillInfo;
		for (int i = 0; i < size; i++) {
			String skillData = obj.optString(String.valueOf(i), "");
			if (skillData.length() > 0) {
				skillInfo = skillData.split(";");
				this.addSkill(Integer.parseInt(skillInfo[0]), Integer.parseInt(skillInfo[1]));
			}
		}
	}
	
//	void addSkill(int index, int templateId, int lv) {
	void addSkill(int templateId, int lv) {
		IPetSkill skill = new KPetSkillImpl(templateId, lv);
//		this._skillMap.put(index, skill);
		this._skillListReadOnly.add(skill);
		if(skill.isActiveSkill()) {
			this._activeSkills.add(skill);
		} else {
			this._passiveSkills.add(skill);
		}
	}
	
	List<IPetSkill> getAllSkills() {
		return _skillListReadOnly;
	}
	
//	void setOpenSkillCount(int pSkillCount) {
//		this._openSkillCount = pSkillCount;
//	}
//
//	int getOpenSkillCount() {
//		return _openSkillCount;
//	}
	
	@Override
	public List<ICombatSkillData> getUsableSkills() {
		return new ArrayList<ICombatSkillData>(_activeSkills);
	}
	
	@Override
	public List<ICombatSkillData> getPassiveSkills() {
		return new ArrayList<ICombatSkillData>(_passiveSkills);
	}
}
