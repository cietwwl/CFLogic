package com.kola.kmp.logic.talent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.KGameMessage;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.tips.TalentTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KTalentTree {

	private static final String FLAG_TEMPLATE_ID = "1";
	private static final String FLAG_IS_ACTIVATE = "2";
//	private static final String FLAG_ACTIVATE_TALENT_SIZE = "3";
	private static final String FLAG_TALENT_DATA_PREFIX = "t_";
	
	private KTalentTreeTemplate _template; // 本天赋树的模板数据
	private final Map<Integer, KTalent> _allTalentData = new LinkedHashMap<Integer, KTalent>(); // 本天赋树的所有天赋
	private final List<ITalent> _allTalentDataReadOnly;
	private final Map<KGameAttrType, Integer> _effectAttr = new HashMap<KGameAttrType, Integer>();
	private final Map<KGameAttrType, Integer> _effectAttrReadOnly = Collections.unmodifiableMap(_effectAttr);
	private int _battlePower; // 本天赋树的战斗力
	private boolean _activate; // 是否已经激活
	private long _roleId; // 角色id
	private boolean _open; // 是否开放
	private boolean _allMainlineEnable; // 是否所有的主线都已经激活
	
	public KTalentTree(KTalentTreeTemplate pTemplate, long pRoleId) {
		this._template = pTemplate;
		this._roleId = pRoleId;
		Map<Integer, KTalentTemplate> allTemplates = _template.talentData;
		List<ITalent> list = new ArrayList<ITalent>();
		KTalentTemplate template;
		for (Iterator<KTalentTemplate> itr = allTemplates.values().iterator(); itr.hasNext();) {
			template = itr.next();
			_allTalentData.put(template.talentId, new KTalent(template, this._roleId));
		}
		list.addAll(_allTalentData.values());
		_allTalentDataReadOnly = Collections.unmodifiableList(list);
	}
	
	private void initComplete() {
		KTalent talent;
		this._allMainlineEnable = isAllMainlineEnable(_allMainlineEnable);
		this._activate = isTreeActivate(_activate);
		for (Iterator<KTalent> itr = _allTalentData.values().iterator(); itr.hasNext();) {
			talent = itr.next();
			KGameUtilTool.combinMap(_effectAttr, talent.getEffectAttr());
		}
		if (this._activate) {
			KGameUtilTool.combinMap(_effectAttr, _template.activateEffectMap);
		}
	}
	
	private boolean isAllMainlineEnable(boolean nowStatus) {
		if (!nowStatus) {
			KTalent talent;
			boolean allMainlineEnable = true;
			for (Iterator<KTalent> itr = _allTalentData.values().iterator(); itr.hasNext();) {
				talent = itr.next();
				if (talent.isMainline()) {
					if (!talent.isEnable()) {
						allMainlineEnable = false;
						break;
					}
				}
			}
			return allMainlineEnable;
		} else {
			return true;
		}
	}
	
	/**
	 * 
	 * 检查天赋树是否被激活
	 * 
	 * @param nowStatus
	 * @return
	 */
	private boolean isTreeActivate(boolean nowStatus) {
		if (!nowStatus) {
			KTalent talent;
			boolean activate = true;
			for (Iterator<KTalent> itr = _allTalentData.values().iterator(); itr.hasNext();) {
				talent = itr.next();
				if (!talent.isMaxLv()) {
					activate = false;
					break;
				}
			}
			return activate;
		} else {
			return true;
		}
	}
	
	void calculateBattlePower() {
		/* 初始化战斗力，注意：不能在decode中调用，因为本方法会获取角色等级，
		 * 如果在decode中调用，会导致死循环
		**/
		this._battlePower = KSupportFactory.getRoleModuleSupport().calculateBattlePower(_effectAttrReadOnly, _roleId);
	}
	
	String save() throws Exception {
		JSONObject obj = new JSONObject();
		obj.put(FLAG_TEMPLATE_ID, _template.talentTreeId);
		obj.put(FLAG_IS_ACTIVATE, _activate);
		Map.Entry<Integer, KTalent> entry;
		for (Iterator<Map.Entry<Integer, KTalent>> itr = _allTalentData.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			if (entry.getValue().isEnable()) {
				obj.put(FLAG_TALENT_DATA_PREFIX + entry.getKey(), entry.getValue().save());
			}
		}
		return obj.toString();
	}
	
	void parse(String str) throws Exception {
		JSONObject obj = new JSONObject(str);
		this._activate = obj.optBoolean(FLAG_IS_ACTIVATE, false);
		Map.Entry<Integer, KTalent> entry;
		String data;
		for (Iterator<Map.Entry<Integer, KTalent>> itr = _allTalentData.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			data = obj.optString(FLAG_TALENT_DATA_PREFIX + entry.getKey(), "");
			if (data.length() > 0) {
				entry.getValue().parse(this._template.talentTreeId, data);
			}
		}
		this.initComplete();
	}
	
	public List<ITalent> getAllTalentDatas() {
		return _allTalentDataReadOnly;
	}
	
	/**
	 * 
	 * 获取本天赋树的战斗力
	 * 
	 * @return
	 */
	public int getBattlePower() {
		return this._battlePower;
	}
	
	/**
	 * 
	 * 增加某个天赋的天赋等级
	 * 
	 * @param talentId
	 */
	public void addTalentLevel(int talentId) {
		if (!_activate) {
			KTalent talent = this._allTalentData.get(talentId);
			if (talent != null) {
				boolean preStatus = _activate;
				talent.addTalentLevel();
				Map<KGameAttrType, Integer> preEffect = talent.getPreEffectAttr();
				Map<KGameAttrType, Integer> nowEffect = talent.getEffectAttr();
				this._activate = isTreeActivate(preStatus);
				this._allMainlineEnable = isAllMainlineEnable(_allMainlineEnable);
				KGameUtilTool.decreaseFromMap(_effectAttr, preEffect);
				KGameUtilTool.combinMap(_effectAttr, nowEffect);
				if (!preStatus && _activate) {
					KGameUtilTool.combinMap(_effectAttr, _template.activateEffectMap);
					KTalentFlowLogger.logTalentActivate(_roleId, this._template.name);
				}
				this.calculateBattlePower();
				KTalentFlowLogger.logTalentUpgrade(_roleId, _template.name, talent.getName(), talent.getCurrentLevel());
			}
		}
	}
	
	/**
	 * 
	 * 获取本天赋树的模板数据
	 * 
	 * @return
	 */
	public int getTreeTemplateId() {
		return _template.talentTreeId;
	}
	
	/**
	 * 
	 * 获取本天赋树的影响属性
	 * 
	 * @return
	 */
	public Map<KGameAttrType, Integer> getEffectAttr() {
		return _effectAttrReadOnly;
	}	
	
	/**
	 * 
	 * 检查某个天赋点是否能增加天赋等级
	 * 
	 * @param talentId
	 * @return
	 */
	public KActionResult<Boolean> checkCondition(int talentId) {
		KActionResult<Boolean> result = new KActionResult<Boolean>();
		result.success = true;
		int roleLv = KSupportFactory.getRoleModuleSupport().getLevel(_roleId);
		if (roleLv < _template.openLv) {
			result.tips = TalentTips.getTipsTreeNotOpenByLv(_template.openLv);
			result.success = false;
		} else if (!_open) {
			result.tips = TalentTips.getTipsTalentTreeNotOpen();
			result.success = false;
		} else if (_activate) {
			result.tips = TalentTips.getTipsTalentTreeAlreadyActivate();
			result.success = false;
		} else {
			KTalent talent = this._allTalentData.get(talentId);
			if (talent != null) {
				int preTalentId = talent.getPreTalentId();
				if (preTalentId > 0) {
					KTalent preTalent = _allTalentData.get(preTalentId);
//					if (!preTalent.isEnable()) {
//						result.tips = TalentTips.getTipsPreTalentNotEnable();
//						result.success = false;
//					} else if (!preTalent.isMaxLv()) {
//						result.tips = TalentTips.getTipsPreTalentMustBeMaxLv();
//						result.success = false;
//					}
					if (!preTalent.isMaxLv()) {
						result.tips = TalentTips.getTipsPreTalentMustBeMaxLv();
						result.success = false;
					}
				}
				if (result.success) {
					return talent.checkCondition();
				}
			}
		}
		return result;
	}
	
	/**
	 * 
	 * 执行天赋点的消耗
	 * 
	 * @param talentId
	 * @return
	 */
	public KActionResult<Boolean> executeConsume(int talentId) {
		KTalent talent = this._allTalentData.get(talentId);
		if (talent != null) {
			return talent.executeConsume();
		}
		return new KActionResult<Boolean>();
	}
	
	/**
	 * 
	 * 获取指定id的天赋实例
	 * 
	 * @param talentId
	 * @return
	 */
	public KTalent getTalent(int talentId) {
		return this._allTalentData.get(talentId);
	}
	
	/**
	 * 
	 * 天赋树是否已经激活
	 * 
	 * @return
	 */
	public boolean isActivate() {
		return _activate;
	}
	
	/**
	 * 
	 * 天赋树的所有主线天赋是否已经开启
	 * 
	 * @return
	 */
	public boolean isAllMainlineEnable() {
		return _allMainlineEnable;
	}
	
	/**
	 * 
	 * 获取天赋的称号
	 * 
	 * @return
	 */
	public String getTalentTitle() {
		return _template.titleName;
	}
	
	/**
	 * 
	 * 下级天赋树的id
	 * 
	 * @return
	 */
	public int getNextTalentTreeId() {
		return _template.nextTalentTreeId;
	}
	
	/**
	 * 
	 * 天赋树的名称
	 * 
	 * @return
	 */
	public String getTalentTreeName() {
		return _template.name;
	}
	
	/**
	 * 
	 */
	public void setOpen() {
		this._open = true;
	}
	
	/**
	 * 
	 * 打包天赋数据到消息
	 * {@link com.kola.kmp.protocol.talent.KTalentProtocol#TALENT_TREE_DATA_STRUCTURE}
	 * 
	 * @param msg
	 */
	public void packDataToMsg(KGameMessage msg) {
		msg.writeInt(_template.talentTreeId);
		msg.writeInt(_template.iconId);
		msg.writeShort(_template.openLv);
		msg.writeUtf8String(_template.name);
		msg.writeUtf8String(_template.titleName);
		msg.writeUtf8String(_template.descr);
		msg.writeInt(this._battlePower);
		msg.writeBoolean(this._activate);
		msg.writeByte(this._allTalentData.size());
		for(Iterator<KTalent> itr = this._allTalentData.values().iterator(); itr.hasNext();) {
			itr.next().packDataToMsg(msg);
		}
		msg.writeByte(this._effectAttr.size());
		Map.Entry<KGameAttrType, Integer> entry;
		for (Iterator<Map.Entry<KGameAttrType, Integer>> itr = this._effectAttr.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			msg.writeShort(entry.getKey().sign);
			msg.writeInt(entry.getValue());
		}
	}
}
