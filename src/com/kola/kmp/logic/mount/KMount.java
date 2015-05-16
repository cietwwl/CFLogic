package com.kola.kmp.logic.mount;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.mount.impl.KAMount;
import com.kola.kgame.db.dataobject.DBMount;
import com.kola.kmp.logic.combat.api.ICombatMount;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountEquiTemp;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountLv;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountTemplate;
import com.kola.kmp.logic.other.KGameAttrType;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2015-1-6 上午10:34:46
 * </pre>
 */
public class KMount extends KAMount implements ICombatMount {

	private KMountTemplate temp;
	private int lv = 1;
	private int exp;
	private int usedSP;
	private boolean isUsed;// 以set中的值为准，此处仅为方便打包消息，不作永久保存
	// 已获得的装备
	private Set<Integer> equiSet = new HashSet<Integer>(4);
	// 技能数据
	private Map<Integer, KMountSkill> skillMap = new HashMap<Integer, KMountSkill>(4);
	// /////////////////////////////////
	static final String JSON_NULL = "NULL";// null
	static final String JSON_VER = "A";// 版本
	//
	static final String JSON_BASEINFO = "B";// 基础信息
	static final String JSON_BASEINFO_LV = "1";
	static final String JSON_BASEINFO_EXP = "2";
	static final String JSON_BASEINFO_USED_SP = "3";

	static final String JSON_EQUILIST = "C";
	static final String JSON_SKILLIST = "D";
	static final String JSON_SKILLIST_LV = "1";

	KMount(KMountSet owner, KMountTemplate temp) {
		super(owner, temp.mountsID);
		this.temp = temp;
		initCombatSkill();
	}

	KMount(KMountSet owner, KMountTemplate temp, DBMount dbdata) {
		super(owner, dbdata.getId(), dbdata.getUUID(), dbdata.getTemplateId(), dbdata.getCreateTimeMillis());
		//
		this.temp = temp;
		initCombatSkill();
		// 以防父类【在子类的全局属性还没有执行初始化时】回调子类decodeCA方法导致NullPointerException
		decodeCA(dbdata.getCustomizeAttribute());
	}
	
	/**
	 * <pre>
	 * 
	 * @deprecated 主要用于旧版（1.0.4）机甲数据初始化
	 * @param owner
	 * @param dbdata
	 * @author CamusHuang
	 * @creation 2015-1-31 上午11:40:10
	 * </pre>
	 */
	KMount(KMountSet owner, DBMount dbdata) {
		super(owner, dbdata.getId(), dbdata.getUUID(), dbdata.getTemplateId(), dbdata.getCreateTimeMillis());
	}

	private void initCombatSkill() {
		usedSP = 0;
		skillMap.clear();
		for (int skillId : temp.skillIdList) {
			skillMap.put(skillId, new KMountSkill(skillId, 1));
		}
	}

	private void decodeCA(String jsonCA) {
		// 由底层调用,解释出逻辑层数据
		try {
			JSONObject obj = new JSONObject(jsonCA);
			int ver = obj.getInt(JSON_VER);// 默认版本
			// CEND 暂时只有版本0
			switch (ver) {
			case 0:
				decodeBaseInfo(obj.getJSONObject(JSON_BASEINFO));
				decodeEquiList(obj.getJSONArray(JSON_EQUILIST));
				decodeSkillList(obj.getJSONObject(JSON_SKILLIST));
				break;
			}

		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + _ownerId + " ----丢失数据，存在运行隐患！", ex);
		}
	}

	/**
	 * <pre>
	 * 基础信息解码
	 * 
	 * @throws JSONException
	 * @author CamusHuang
	 * @creation 2013-1-12 下午3:30:24
	 * </pre>
	 */
	private void decodeBaseInfo(JSONObject obj) throws JSONException {
		this.lv = obj.getInt(JSON_BASEINFO_LV);
		this.exp = obj.getInt(JSON_BASEINFO_EXP);
		this.usedSP = obj.getInt(JSON_BASEINFO_USED_SP);
	}

	private void decodeEquiList(JSONArray obj) throws JSONException {
		for (int i = 0; i < obj.length(); i++) {
			int equiId = obj.getInt(i);
			equiSet.add(equiId);
		}
	}

	private void decodeSkillList(JSONObject obj) throws JSONException {
		for (Iterator<String> it = obj.keys(); it.hasNext();) {
			String key = it.next();
			int skillId = Integer.parseInt(key);
			int lv = obj.getInt(key);
			KMountSkill skill = skillMap.get(skillId);
			if(skill!=null){
				skill.lv = lv;
			}
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
			 obj.put(JSON_BASEINFO, encodeBaseInfo());// 基础信息
			 obj.put(JSON_EQUILIST, encodeEquiList());
			 obj.put(JSON_SKILLIST, encodeSkillList());
			return obj.toString();
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + _ownerId + " ----丢失数据！", ex);
			return "";
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 基础信息打包
	 * 
	 * @return
	 * @throws JSONException
	 * @author CamusHuang
	 * @creation 2013-1-11 下午12:29:08
	 * </pre>
	 */
	private JSONObject encodeBaseInfo() throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put(JSON_BASEINFO_LV, lv);
		obj.put(JSON_BASEINFO_EXP, exp);
		obj.put(JSON_BASEINFO_USED_SP, usedSP);
		return obj;
	}

	private JSONArray encodeEquiList() throws JSONException {
		JSONArray obj = new JSONArray();
		for (int eqiId : equiSet) {
			obj.put(eqiId);
		}
		return obj;
	}
	
	private JSONObject encodeSkillList() throws JSONException {
		JSONObject obj = new JSONObject();
		for (KMountSkill skill : skillMap.values()) {
			obj.put(skill._skillTemplateId+"", skill.lv);
		}
		return obj;
	}

	public KMountTemplate getTemplate() {
		return temp;
	}

	public int getExp() {
		return exp;
	}

	void setExp(int newExp) {
		rwLock.lock();
		try {
			this.exp = newExp;
			super.notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	void upLv(int lv, int exp) {
		rwLock.lock();
		try {
			this.lv = lv;
			this.exp = exp;
			super.notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	void upBigLv(KMountTemplate newTemplate) {
		rwLock.lock();
		try {
			super.setTemplateId(newTemplate.mountsID);
			this.temp = newTemplate;
			this.initCombatSkill();
			super.notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 
	 * @deprecated
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-8 下午12:08:35
	 * </pre>
	 */
	Map<Integer, KMountSkill> getSkillCache() {
		return skillMap;
	}

	void upLvSkill(int useSP, int skillTemplateId, int newLv) {
		rwLock.lock();
		try {
			this.usedSP += useSP;
			KMountSkill skill = skillMap.get(skillTemplateId);
			if (skill == null) {
				skill = new KMountSkill(skillTemplateId, newLv);
				skillMap.put(skillTemplateId, skill);
			} else {
				skill.lv = newLv;
			}
			super.notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	void resetSP() {
		rwLock.lock();
		try {
			usedSP = 0;
			for (KMountSkill skill : skillMap.values()) {
				skill.lv = 1;
			}
			super.notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	int getUsedSP() {
		return usedSP;
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 以set中的值为准，此处仅为方便打包消息，不作永久保存b
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-9 下午3:54:32
	 * </pre>
	 */
	boolean isUsed() {
		return isUsed;
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 以set中的值为准，此处仅为方便打包消息，不作永久保存
	 * @param isUsed
	 * @author CamusHuang
	 * @creation 2015-1-9 下午3:54:41
	 * </pre>
	 */
	void setUsed(boolean isUsed) {
		this.isUsed = isUsed;
	}

	boolean checkEqui(int equiId) {
		return equiSet.contains(equiId);
	}
	
	/**
	 * <pre>
	 * 
	 * @deprecated
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-12 上午10:49:52
	 * </pre>
	 */
	Set<Integer> getEquiCache(){
		return equiSet;
	}

	void upLvEqui(int oldEquiId, int newEquiId) {
		rwLock.lock();
		try {
			equiSet.remove(oldEquiId);
			equiSet.add(newEquiId);
			super.notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 机甲被骑乘时即生效的属性 即升级属性（培养属性）
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-12 上午10:49:48
	 * </pre>
	 */
	Map<KGameAttrType, Integer> getAttsForLv() {
		KMountTemplate temp = getTemplate();
		if (temp == null) {
			return Collections.emptyMap();
		}

		KMountLv lvData = KMountDataManager.mMountLvDataManager.getData(lv);
		return lvData.allEffects.get(temp.Model);
	}

	/**
	 * <pre>
	 * 机甲被召唤时即生效的属性 即 基础属性+装备属性
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-12 上午10:49:43
	 * </pre>
	 */
	Map<KGameAttrType, Integer> getAttsForWar() {
		Map<KGameAttrType, Integer> result = getEquipmentAttrs();
		
		KMountTemplate temp = getTemplate();
		if (temp == null) {
			return result;
		}
		//
		for (Entry<KGameAttrType, Integer> e : temp.allEffects.entrySet()) {
			Integer baseValue = result.get(e.getKey());
			int equValue = e.getValue() + (baseValue == null ? 0 : baseValue);
			result.put(e.getKey(), equValue);
		}
		return result;
	}
	
	/**
	 * <pre>
	 * 战斗（基础属性+装备属性）+培养
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-26 上午10:57:42
	 * </pre>
	 */
	Map<KGameAttrType, Integer> getAttsForAll() {
		Map<KGameAttrType, Integer> result = getAttsForWar();
		
		KMountTemplate temp = getTemplate();
		if (temp == null) {
			return result;
		}

		KMountLv lvData = KMountDataManager.mMountLvDataManager.getData(lv);
		Map<KGameAttrType, Integer> lvAtts = lvData.allEffects.get(temp.Model);
		
		for (Entry<KGameAttrType, Integer> e : lvAtts.entrySet()) {
			Integer baseValue = result.get(e.getKey());
			int equValue = e.getValue() + (baseValue == null ? 0 : baseValue);
			result.put(e.getKey(), equValue);
		}
		
		return result;
	}

	void cloneByCamus(KMount srcmount) {
		lv=srcmount.lv;
		exp=srcmount.exp;
		usedSP=srcmount.usedSP;
		isUsed=srcmount.isUsed;
		equiSet.addAll(srcmount.equiSet);
		for(KMountSkill skill:srcmount.skillMap.values()){
			skillMap.put(skill._skillTemplateId, new KMountSkill(skill._skillTemplateId, skill.lv));
		}
	}

	@Override
	public boolean canBeAttack() {
		return true;
	}

	@Override
	public byte getObjectType() {
		return OBJECT_TYPE_VEHICLE;
	}

	@Override
	public String getName() {
		return temp.Name;
	}

	@Override
	public int getHeadResId() {
		return temp.HeadID;
	}

	@Override
	public int getInMapResId() {
		return temp.res_id;
	}

	@Override
	public int getLevel() {
		return lv;
	}

	@Override
	public long getCurrentHp() {
		return 0;
	}

	@Override
	public long getMaxHp() {
		return 0;
	}

	@Override
	public int getBattleMoveSpeedX() {
		return 0;
	}

	@Override
	public int getBattleMoveSpeedY() {
		return 0;
	}

	@Override
	public int getVision() {
		return 0;
	}

	@Override
	public List<ICombatSkillData> getUsableSkills() {
		return new ArrayList<ICombatSkillData>(skillMap.values());
	}

	@Override
	public List<ICombatSkillData> getPassiveSkills() {
		return Collections.emptyList();
	}

	@Override
	public float getSpeedUpTimes() {
		return temp.fightMoveSpeed;
	}

	@Override
	public int getAtkCountPerTime() {
		return temp.atkCountPerTime;
	}

	@Override
	public int getFullImmunityDuration() {
		return temp.armortime;
	}

	@Override
	public int getFullImmunityIteration() {
		return temp.armorinterval;
	}

	@Override
	public String getAI() {
		return temp.mountsAI;
	}
	
	@Override
	public Map<Integer, Integer> getBeanTime(){
		return temp.beanTimeMap;
	}

	@Override
	public Map<KGameAttrType, Integer> getBasicAttrs() {
		KMountTemplate temp = getTemplate();
		if (temp == null) {
			return Collections.emptyMap();
		}
		return temp.allEffects;
	}

	@Override
	public Map<KGameAttrType, Integer> getEquipmentAttrs() {
		Map<KGameAttrType, Integer> result = new HashMap<KGameAttrType, Integer>();
		//
		for (int equipID : equiSet) {
			KMountEquiTemp equTemp = KMountDataManager.mMountEquiDataManager.getData(equipID);
			if (equTemp == null) {
				continue;
			}
			for (Entry<KGameAttrType, Integer> e : equTemp.allEffects.entrySet()) {
				Integer baseValue = result.get(e.getKey());
				int equValue = e.getValue() + (baseValue == null ? 0 : baseValue);
				result.put(e.getKey(), equValue);
			}
		}
		return result;
	}

	static class KMountSkill implements ICombatSkillData {

		private int _skillTemplateId;
		private int lv = 1;

		public KMountSkill(int pSkillTemplateId, int lv) {
			this._skillTemplateId = pSkillTemplateId;
			this.lv = lv;
		}

		@Override
		public int getSkillTemplateId() {
			return _skillTemplateId;
		}

		@Override
		public int getLv() {
			return lv;
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
