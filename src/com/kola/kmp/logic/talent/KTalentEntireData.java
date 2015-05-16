package com.kola.kmp.logic.talent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleExtCABaseImpl;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;

/**
 * 
 * @author PERRY CHAN
 */
public class KTalentEntireData extends RoleExtCABaseImpl {

	
	private static final Logger _LOGGER = KGameLogger.getLogger(KTalentEntireData.class);

	private final KTalentTree[] _allTalentTree;
	
	private final Map<KGameAttrType, Integer> _effectAttr = new HashMap<KGameAttrType, Integer>();
	private final Map<KGameAttrType, Integer> _effectAttrReadOnly = Collections.unmodifiableMap(_effectAttr);
	private int _totalBattlePower;
	private boolean _initFinish;
//	/**
//	 * 
//	 */
//	public KTalentEntireData() {
//		this.initTalentData();
//		this.bindRoleId();
//	}
	
	public KTalentEntireData(long roleId, int type) {
		super(roleId, type);
		List<KTalentTreeTemplate> allTreeTemplates = KTalentModuleManager.getAllTalentTreeTemplates();
		_allTalentTree = new KTalentTree[allTreeTemplates.size()];
		List<Integer> nextIdList = new ArrayList<Integer>();
		KTalentTreeTemplate template;
		for(int i = 0; i < allTreeTemplates.size(); i++) {
			template = allTreeTemplates.get(i);
			_allTalentTree[i] = new KTalentTree(template, this.getRoleId());
			nextIdList.add(template.nextTalentTreeId);
		}
		KTalentTree tree;
		for(int i = 0; i < _allTalentTree.length; i++) {
			tree = _allTalentTree[i];
			if(!nextIdList.contains(tree.getTreeTemplateId())) {
				// 第一棵天赋树，硬性设为open
				tree.setOpen();
				break;
			}
		}
	}
	
	private boolean checkNextTree(int targetTreeId, int beginIndex, int endIndex) {
		KTalentTree next;
		for (int i = beginIndex; i < endIndex; i++) {
			next = _allTalentTree[i];
			if (next.getTreeTemplateId() == targetTreeId) {
				next.setOpen();
				return true;
			}
		}
		return false;
	}
	
	private void initComplete() {
		KTalentTree tree;
		boolean found;
		for (int i = 0; i < _allTalentTree.length; i++) {
			tree = _allTalentTree[i];
			KGameUtilTool.combinMap(_effectAttr, tree.getEffectAttr());
			if (tree.isAllMainlineEnable() && tree.getNextTalentTreeId() > 0) {
				found = this.checkNextTree(tree.getNextTalentTreeId(), i + 1, _allTalentTree.length);
				if (!found) {
					this.checkNextTree(tree.getNextTalentTreeId(), 0, i);
				}
			}
		}
	}

	@Override
	protected void decode(String attribute) {
		try {
			JSONObject obj = new JSONObject(attribute);
			String talentTreeData;
			KTalentTree talentTree;
			for (int i = 0; i < _allTalentTree.length; i++) {
				talentTree = _allTalentTree[i];
				talentTreeData = obj.optString(String.valueOf(talentTree.getTreeTemplateId()), "");
				if (talentTreeData.length() > 0) {
					talentTree.parse(talentTreeData);
				}
			}
			this.initComplete();
		} catch (Exception e) {
			_LOGGER.error("解析天赋整体数据出现异常！角色id是：{}", this.getRoleId());
		}
	}

	@Override
	protected String encode() {
		JSONObject obj = new JSONObject();
		try {
			KTalentTree tree;
			for (int i = 0; i < _allTalentTree.length; i++) {
				tree = _allTalentTree[i];
				obj.put(String.valueOf(tree.getTreeTemplateId()), tree.save());
			}
		} catch (Exception e) {
			_LOGGER.error("保存天赋树数据出错！角色id是：{}", this.getRoleId());
		}
		return obj.toString();
	}
	
	void onLogin() {
		if (!_initFinish) {
			_initFinish = true;
			this.firstInit();
		}
	}
	
	private void calculateBattlePower() {
		int temp = 0;
		for (int i = 0; i < _allTalentTree.length; i++) {
			temp += _allTalentTree[i].getBattlePower();
		}
		this._totalBattlePower = temp;
	}
	
	private void firstInit() {
		KTalentTree tempTree;
		ITalent tempTalent;
		List<ITalent> allTalents;
		List<Integer> rolePasSkillIds = KSupportFactory.getSkillModuleSupport().getRoleAllPasSkills(this.getRoleId());
		for (int i = 0; i < _allTalentTree.length; i++) {
			tempTree = _allTalentTree[i];
			tempTree.calculateBattlePower();
			this._totalBattlePower += tempTree.getBattlePower();
			allTalents = tempTree.getAllTalentDatas();
			for (int k = 0; k < allTalents.size(); k++) {
				tempTalent = allTalents.get(k);
				if(tempTalent.isEnable() && tempTalent.isSkillTalent() && !rolePasSkillIds.contains(tempTalent.getSkillTemplateId())) {
					KSupportFactory.getSkillModuleSupport().addSkillToRole(getRoleId(), false, tempTalent.getSkillTemplateId(), "天赋");
				}
			}
		}
	}
	
	public int getTotalBattlePower() {
		return _totalBattlePower;
	}
	
	public void addTalentLevel(int treeId, int talentId) {
		KTalentTree tree = this.getTalentTree(treeId);
		if (tree != null) {
			boolean pre = tree.isAllMainlineEnable();
			Map<KGameAttrType, Integer> preEffect = new HashMap<KGameAttrType, Integer>(tree.getEffectAttr());
			tree.addTalentLevel(talentId);
			Map<KGameAttrType, Integer> nowEffect = tree.getEffectAttr();
			KGameUtilTool.decreaseFromMap(_effectAttr, preEffect);
			KGameUtilTool.combinMap(_effectAttr, nowEffect);
			this.calculateBattlePower();
			this.notifyUpdate();
			if(!pre && tree.isAllMainlineEnable()) {
				KTalentTree nextTree = this.getTalentTree(tree.getNextTalentTreeId());
				if(nextTree != null) {
					nextTree.setOpen();
					KTalentFlowLogger.logTalentTreeOpen(getRoleId(), nextTree.getTalentTreeName());
				}
			}
			KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(getRoleId(), KTalentAttributeProvider.getType());
		}
	}
	
	public KActionResult<Boolean> checkCondition(int talentTreeId, int talentId) {
		KTalentTree tree = this.getTalentTree(talentTreeId);
		if(tree != null) {
			return tree.checkCondition(talentId);
		}
		return new KActionResult<Boolean>();
	}
	
	public KActionResult<Boolean> executeConsume(int treeId, int talentId) {
		KTalentTree tree = this.getTalentTree(treeId);
		if(tree != null) {
			return tree.executeConsume(talentId);
		}
		return new KActionResult<Boolean>();
	}
	
	public Map<KGameAttrType, Integer> getEffectAttr() {
		return this._effectAttrReadOnly;
	}
	
	public KTalentTree getTalentTree(int treeId) {
		KTalentTree tree;
		for (int i = 0; i < _allTalentTree.length; i++) {
			tree = _allTalentTree[i];
			if (tree.getTreeTemplateId() == treeId) {
				return tree;
			}
		}
		return null;
	}
	
	public KTalent getTalent(int treeId, int talentId) {
		KTalentTree tree = this.getTalentTree(treeId);
		if (tree != null) {
			return tree.getTalent(talentId);
		}
		return null;
	}
	
	public void packDataToMsg(KGameMessage msg) {
		msg.writeByte(_allTalentTree.length);
		for (int i = 0; i < _allTalentTree.length; i++) {
			_allTalentTree[i].packDataToMsg(msg);
		}
		msg.writeByte(_effectAttr.size());
		Map.Entry<KGameAttrType, Integer> entry;
		for(Iterator<Map.Entry<KGameAttrType, Integer>> itr = _effectAttr.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			msg.writeShort(entry.getKey().sign);
			msg.writeInt(entry.getValue());
		}
		msg.writeInt(_totalBattlePower);
	}

}
