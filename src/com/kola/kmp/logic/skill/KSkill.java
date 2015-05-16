package com.kola.kmp.logic.skill;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.skill.impl.KASkill;
import com.kola.kgame.cache.skill.impl.KASkillSet;
import com.kola.kgame.db.dataobject.DBSkillData;
import com.kola.kmp.logic.combat.api.ICombatSkillData;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-6 上午11:21:25
 * </pre>
 */
public class KSkill extends KASkill implements ICombatSkillData {

	private int lv = 1;
	private boolean isInitiative;// 是否主动技能
	private boolean isSuperSkill;// 是否超杀技能
	private boolean onlyEffectInPVP; // 

	// /////////////////////////////////
	static final String JSON_NULL = "NULL";// null
	static final String JSON_VER = "A";// 版本
	//
	static final String JSON_BASEINFO = "B";// 基础信息
	static final String JSON_BASEINFO_ISINI = "1";
	static final String JSON_BASEINFO_LV = "2";

	KSkill(KSkillSet owner, boolean isInitiative, int templateId) {
		super(owner, templateId);
		this.isInitiative = isInitiative;
		this.initComplete();
	}

	KSkill(KSkillSet owner, DBSkillData dbdata) {
		super(owner, dbdata.getId(), dbdata.getTemplateId());
		// 以防父类【在子类的全局属性还没有执行初始化时】回调子类decodeCA方法导致NullPointerException
		decodeCA(dbdata.getCustomizeAttribute());
		this.initComplete();
	}

	private void initComplete() {
		if (this.isInitiative) {
			KRoleIniSkillTemp template = KSkillDataManager.mRoleIniSkillTempManager.getTemplate(_templateId);
			this.isSuperSkill = template.isSuperSkill;
		} else {
			KRolePasSkillTemp template = KSkillDataManager.mRolePasSkillTempManager.getTemplate(_templateId);
			this.onlyEffectInPVP = template.onlyEffectInPVP;
					
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
				break;
			}

		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + owner._roleId + " ----丢失数据，存在运行隐患！", ex);
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
		this.isInitiative = obj.getBoolean(JSON_BASEINFO_ISINI);
		this.lv = obj.getInt(JSON_BASEINFO_LV);
	}

	@Override
	protected String encodeCA() {
		owner.rwLock.lock();
		// 构造一个数据对象给底层
		try {
			JSONObject obj = new JSONObject();
			obj.put(JSON_VER, 0);
			// CEND 暂时只有版本0
			obj.put(JSON_BASEINFO, encodeBaseInfo());// 基础信息
			return obj.toString();
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + owner._roleId + " ----丢失数据！", ex);
			return "";
		} finally {
			owner.rwLock.unlock();
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
		obj.put(JSON_BASEINFO_ISINI, isInitiative);
		obj.put(JSON_BASEINFO_LV, lv);
		return obj;
	}

	/**
	 * <pre>
	 * 是否主动技能
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-7 下午4:54:11
	 * </pre>
	 */
	public boolean isInitiative() {
		return isInitiative;
	}

	@Override
	public int getLv() {
		return lv;
	}

	@Override
	public int getSkillTemplateId() {
		return this._templateId;
	}

	@Override
	public boolean isSuperSkill() {
		return this.isSuperSkill;
	}
	
	@Override
	public boolean onlyEffectInPVP() {
		return onlyEffectInPVP;
	}

	void setLv(int lv) {
		rwLock.lock();
		try {
			this.lv = lv;
			this.notifyDB();
		} finally {
			rwLock.unlock();
		}
	}

}
