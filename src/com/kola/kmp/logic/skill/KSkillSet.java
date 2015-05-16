package com.kola.kmp.logic.skill;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.skill.impl.KASkillSet;
import com.kola.kgame.db.dataobject.DBSkillData;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-5 下午8:14:02
 * </pre>
 */
public class KSkillSet extends KASkillSet<KSkill> {

	// 技能栏放置数据（技能模板ID）
	private int[] skillTempIdSlot = new int[KSkillConfig.getInstance().MaxSkillForWar];
	// /////////////////////////////////
	static final String JSON_NULL = "NULL";// null
	static final String JSON_VER = "A";// 版本
	//
	static final String JSON_SLOTDATA = "B";// 技能栏数据

	KSkillSet(long roleId, boolean isFirstNew) {
		super(roleId, isFirstNew);
	}

	protected Map<Long, KSkill> initDBSkills(List<DBSkillData> dbdatas) {
		Map<Long, KSkill> result = new HashMap<Long, KSkill>();
		for (DBSkillData dbdata : dbdatas) {
//			清理非法技能			
//			KRoleIniSkillTemp initemplate = KSkillDataManager.mRoleIniSkillTempManager.getTemplate(dbdata.getTemplateId());
//			KRolePasSkillTemp pastemplate = KSkillDataManager.mRolePasSkillTempManager.getTemplate(dbdata.getTemplateId());
//			if(initemplate==null && pastemplate==null){
//				super.notifyElementDelete(dbdata.getId());
//				continue;
//			}
			
			KSkill data = new KSkill(this, dbdata);
			result.put(data._id, data);
		}
		return result;
	}

	@Override
	protected void decodeCA(String jsonStr) {
		// 由底层调用,解释出逻辑层数据
		try {
			JSONObject obj = new JSONObject(jsonStr);
			int ver = obj.getInt(JSON_VER);// 默认版本
			// CEND 暂时只有版本0
			switch (ver) {
			case 0:
				decodeSlotData(obj.optJSONObject(JSON_SLOTDATA));
				break;
			}

		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + _roleId + " ----丢失数据，存在运行隐患！", ex);
		}
	}

	private void decodeSlotData(JSONObject obj) throws JSONException {
		if (obj == null) {
			return;
		}
		for (int i = 0; i < skillTempIdSlot.length; i++) {
			skillTempIdSlot[i] = obj.optInt(i + "");
		}
	}

	@Override
	protected String encodeCA() {
		rwLock.lock();
		// 构造一个数据对象给底层
		try {
			JSONObject obj = new JSONObject();
			obj.put(JSON_VER, 0);
			// CEND 暂时只有版本0
			obj.put(JSON_SLOTDATA, encodeSlotData());
			return obj.toString();
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + _roleId + " ----丢失数据！", ex);
			return "";
		} finally {
			rwLock.unlock();
		}
	}

	private JSONObject encodeSlotData() throws JSONException {
		JSONObject obj = new JSONObject();
		for (int i = 0; i < skillTempIdSlot.length; i++) {
			obj.put(i + "", skillTempIdSlot[i]);
		}
		return obj;
	}

	int getSkillSlotLen() {
		return skillTempIdSlot.length;
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 缓存层内部方法，小心使用
	 * @return 技能栏模板ID
	 * @author CamusHuang
	 * @creation 2014-2-27 下午8:18:57
	 * </pre>
	 */
	int[] getSkillSlotDataCache() {
		return skillTempIdSlot;
	}

	/**
	 * <pre>
	 * 搜索指定模板ID的技能
	 * 
	 * @param isIni
	 * @param templateId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-26 下午3:16:44
	 * </pre>
	 */
	KSkill searchSkill(boolean isIni, int templateId) {
		rwLock.lock();
		try {
			Map<Long, KSkill> _elementMap = getAllSkillsCache();
			for (KSkill skill : _elementMap.values()) {
				if (skill.isInitiative() == isIni && skill._templateId == templateId) {
					return skill;
				}
			}
			return null;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 搜索所有主动或被动技能
	 * 
	 * @param isIni
	 * @return <模板ID, KSkill>
	 * @author CamusHuang
	 * @creation 2014-3-26 上午11:57:33
	 * </pre>
	 */
	Map<Integer, KSkill> searchAllSkills(boolean isIni) {
		rwLock.lock();
		try {
			Map<Integer, KSkill> result = new LinkedHashMap<Integer, KSkill>();
			Map<Long, KSkill> map = super.getAllSkillsCache();
			for (KSkill skill : map.values()) {
				if (skill.isInitiative() == isIni) {
					result.put(skill._templateId, skill);
				}
			}
			return result;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param skillsMap <技能栏位置（0~4）,技能模板ID>
	 * @author CamusHuang
	 * @creation 2014-4-1 下午12:21:42
	 * </pre>
	 */
	boolean updateSlotSkills(Map<Integer, Integer> skillsMap) {
		rwLock.lock();
		try {
			boolean isChange = false;
			for (Iterator<Entry<Integer, Integer>> it = skillsMap.entrySet().iterator(); it.hasNext();) {
				Entry<Integer, Integer> entry = it.next();
				int slotId = entry.getKey();
				int skillTempId = entry.getValue();

				if (slotId >= skillTempIdSlot.length) {
					continue;
				}
				if (skillTempIdSlot[slotId] != skillTempId) {
					skillTempIdSlot[slotId] = skillTempId;
					isChange = true;
				}
			}
			//
			if (isChange) {
				notifyDB();
			}
			return isChange;
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 尝试放入空置的快捷栏
	 * 
	 * @param set
	 * @param newSkills
	 * @author CamusHuang
	 * @creation 2014-6-12 下午5:31:20
	 * </pre>
	 */
	boolean tryToJoinSkillSlot(List<KSkill> newSkills) {
		if (newSkills.isEmpty()) {
			return false;
		}
		rwLock.lock();
		try {
			boolean isChange = false;
			for (KSkill skill : newSkills) {
				if (skill.isInitiative()) {
					KRoleIniSkillTemp temp = KSkillDataManager.mRoleIniSkillTempManager.getTemplate(skill._templateId);
					if (temp.isSuperSkill) {
						int index = skillTempIdSlot.length - 1;
						if (skillTempIdSlot[index] < 1) {
							skillTempIdSlot[index] = skill._templateId;
							isChange = true;
						}
					} else {
						int maxIndex = skillTempIdSlot.length - 1;
						for (int index = 0; index < maxIndex; index++) {
							if (skillTempIdSlot[index] < 1) {
								skillTempIdSlot[index] = skill._templateId;
								isChange = true;
								break;
							}
						}
					}
				}
			}
			//
			if (isChange) {
				notifyDB();
			}
			return isChange;
		} finally {
			rwLock.unlock();
		}
	}
}
