package com.kola.kmp.logic.talent;

import java.util.Collections;
import java.util.Map;

import com.koala.game.KGameMessage;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.TalentTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KTalent implements ITalent {

	private static final String FLAG_TEMPLATE_ID = "1";
	private static final String FLAG_CURRENT_LEVEL = "2";
	
	private KTalentTemplate _template;
	private int _currentLevel; // 当前的等级
	private boolean _isMaxLv; // 是否已经满级
	private boolean _enable; // 是否已经启用，等级大于0表示已经启用
	private long _roleId;
	private String _descr; // 当前等级的描述
	private String _nextDescr; // 下一个等级的描述
//	private final Map<KGameAttrType, Integer> _effectAttr = new LinkedHashMap<KGameAttrType, Integer>();
//	private final Map<KGameAttrType, Integer> _effectAttrReadOnly = Collections.unmodifiableMap(_effectAttr);
	private Map<KGameAttrType, Integer> _effectAttr = Collections.emptyMap();
	
	public KTalent(KTalentTemplate pTemplate, long pRoleId) {
		this._template = pTemplate;
		this._roleId = pRoleId;
	}
	
	private void setEffectAttr() {
		if (!_template.isSkillTalent && this._template.effectAttr.size() > 0) {
//			this._effectAttr.clear();
//			this._effectAttr.putAll(this._template.effectAttr.get(_currentLevel));
			this._effectAttr = this._template.effectAttr.get(_currentLevel);
		}
	}
	
//	private String generateAttributeDescr(Map<KGameAttrType, Integer> map) {
//		if (map.size() > 0) {
//			Map.Entry<KGameAttrType, Integer> entry;
//			StringBuilder strBld = new StringBuilder();
//			for (Iterator<Map.Entry<KGameAttrType, Integer>> itr = map.entrySet().iterator(); itr.hasNext();) {
//				entry = itr.next();
//				strBld.append(entry.getKey().getValueDesc(entry.getValue())).append("、");
//			}
//			strBld.deleteCharAt(strBld.length() - 1);
//			return strBld.toString();
//		} else {
//			return "";
//		}
//	}
	
	private void generateDescr() {
		if (this._template.isSkillTalent) {
			if (this._descr == null) {
				this._descr = _template.descr;
			}
		} else {
			if (this._currentLevel > 0) {
				this._descr = this._template.getDescrByLv(_currentLevel);
				if (!this._isMaxLv) {
					this._nextDescr = this._template.getDescrByLv(_currentLevel + 1);
				} else {
					this._nextDescr = "";
				}
			} else {
				this._descr = "";
				this._nextDescr = this._template.getDescrByLv(1);
			}
		}
	}
	
	String save() throws Exception {
		JSONObject obj = new JSONObject();
		obj.put(FLAG_TEMPLATE_ID, _template.talentId);
		obj.put(FLAG_CURRENT_LEVEL, _currentLevel);
		return obj.toString();
	}
	
	void parse(int treeId, String data) throws Exception {
		JSONObject obj = new JSONObject(data);
		int templateId = obj.getInt(FLAG_TEMPLATE_ID);
		this._template = KTalentModuleManager.getTalentTemplateData(treeId, templateId);
		this._currentLevel = obj.optInt(FLAG_CURRENT_LEVEL, 0);
		this._isMaxLv = this._currentLevel == this._template.maxLv;
		this._enable = this._currentLevel > 0;
		setEffectAttr();
		if (this._currentLevel > _template.maxLv) {
			this._currentLevel = _template.maxLv;
		}
	}
	
	void packDataToMsg(KGameMessage msg) {
		if(this._descr == null) {
			this.generateDescr();
		}
		msg.writeInt(this._template.talentId);
		msg.writeInt(this._template.iconId);
		msg.writeUtf8String(this._template.name);
		msg.writeShort(this._currentLevel);
		msg.writeShort(this._template.maxLv);
		msg.writeBoolean(this._template.isMainline);
		msg.writeBoolean(this._template.isSkillTalent);
		msg.writeInt(this._template.preTalentId);
		msg.writeShort(this._template.requireLv);
		msg.writeInt(this._template.potential);
		msg.writeInt(this._template.honor);
		if(this._template.isSkillTalent) {
			msg.writeUtf8String(this._descr);
		} else {
			msg.writeUtf8String(this._descr);
			if(!this._isMaxLv) {
				msg.writeUtf8String(this._nextDescr);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	void addTalentLevel() {
		if (!this._isMaxLv) {
			this._currentLevel++;
			if (this._currentLevel == this._template.maxLv) {
				this._isMaxLv = true;
			}
			if(!_enable) {
				this._enable = true;
			}
			this.setEffectAttr();
			this.generateDescr();
			if (_template.isSkillTalent) {
				KSupportFactory.getSkillModuleSupport().addSkillToRole(_roleId, false, _template.skillId, "天赋");
			}
		}
	}
	
	public KActionResult<Boolean> checkCondition() {
		KActionResult<Boolean> result = new KActionResult<Boolean>();
		result.success = true;
		int lv = KSupportFactory.getRoleModuleSupport().getLevel(_roleId);
		if(this._isMaxLv) {
			result.success = false;
			result.tips = TalentTips.getTipsTalentLvIsMax();
		}
		if (result.success && lv < this._template.requireLv) {
			result.success = false;
			result.tips = TalentTips.getTipsTalentRequireLvNotMatch(this._template.requireLv);
		}
		if (result.success && this._isMaxLv) {
			result.success = false;
			result.tips = TalentTips.getTipsTalentLvIsMax();
		}
		if (result.success && this._template.honor > 0) {
			if(KSupportFactory.getCurrencySupport().getMoney(_roleId, KCurrencyTypeEnum.SCORE) < _template.honor) {
				result.tips = GlobalTips.getTipsMaterialNotEnough(KCurrencyTypeEnum.SCORE.extName, _template.honor);
				result.success = false;
			}
		}
		if (result.success && this._template.potential > 0) {
			if (KSupportFactory.getCurrencySupport().getMoney(_roleId, KCurrencyTypeEnum.POTENTIAL) < _template.potential) {
				result.tips = GlobalTips.getTipsMaterialNotEnough(KCurrencyTypeEnum.POTENTIAL.extName, _template.potential);
				result.success = false;
			}
		}
		return result;
	}
	
	public KActionResult<Boolean> executeConsume() {
		KActionResult<Boolean> result = new KActionResult<Boolean>();
		result.success = true;
		if (this._template.honor > 0) {
			if (KSupportFactory.getCurrencySupport().decreaseMoney(_roleId, KCurrencyTypeEnum.SCORE, _template.honor, UsePointFunctionTypeEnum.天赋点激活, true) < 0) {
				result.tips = GlobalTips.getTipsMaterialNotEnough(KCurrencyTypeEnum.SCORE.extName, _template.honor);
				result.success = false;
			}
		}
		if (result.success && this._template.potential > 0) {
			if (KSupportFactory.getCurrencySupport().decreaseMoney(_roleId, KCurrencyTypeEnum.POTENTIAL, _template.potential, UsePointFunctionTypeEnum.天赋点激活, true) < 0) {
				result.tips = GlobalTips.getTipsMaterialNotEnough(KCurrencyTypeEnum.POTENTIAL.extName, _template.potential);
				result.success = false;
			}
		}
		return result;
	}
	
	public Map<KGameAttrType, Integer> getPreEffectAttr() {
		if (this._currentLevel > 1) {
			return this._template.effectAttr.get(this._currentLevel - 1);
		} else {
			return Collections.emptyMap();
		}
	}
	
	public String getDescr() {
		return _descr;
	}

	public String getNextDescr() {
		return _nextDescr;
	}
	
	public boolean isSkillTalent() {
		return _template.isSkillTalent;
	}
	
	public int getSkillTemplateId() {
		return _template.skillId;
	}
	
	@Override
	public int getTalentId() {
		return _template.talentId;
	}
	
	@Override
	public String getName() {
		return _template.name;
	}
	
	@Override
	public int getPreTalentId() {
		return _template.preTalentId;
	}
	
	@Override
	public boolean isMainline() {
		return _template.isMainline;
	}
	
	@Override
	public boolean isEnable() {
		return _enable;
	}
	
	@Override
	public int getCurrentLevel() {
		return _currentLevel;
	}
	
	@Override
	public boolean isMaxLv() {
		return _isMaxLv;
	}
	
	@Override
	public Map<KGameAttrType, Integer> getEffectAttr() {
//		return _effectAttrReadOnly;
		return _effectAttr;
	}
}
