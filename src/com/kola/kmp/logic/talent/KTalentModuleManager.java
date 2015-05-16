package com.kola.kmp.logic.talent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.role.RoleExtCA;
import com.kola.kmp.logic.other.KRoleExtTypeEnum;
import com.kola.kmp.logic.other.KTableInfo;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;

/**
 * 
 * @author PERRY CHAN
 */
public class KTalentModuleManager {

	private static final Logger _LOGGER = KGameLogger.getLogger(KTalentModuleManager.class);
	
	private static Map<Integer, KTalentTreeTemplate> _allTalentTreeTemplates = new HashMap<Integer, KTalentTreeTemplate>();
	private static List<KTalentTreeTemplate> _allTalentTreeTemplatesReadOnly;
	
	static void loadData(String path, Map<Byte, KTableInfo> tableMap) throws Exception {
		KGameExcelFile file = new KGameExcelFile(path);
		KGameExcelRow[] allRows;
		KGameExcelRow row;
		Map<Integer, Map<Integer, KTalentTemplate>> allTalentTemplate = new HashMap<Integer, Map<Integer, KTalentTemplate>>();
		
		allRows = KGameUtilTool.getAllDataRows(file, tableMap, ITalentTableConfig.TABLE_TYPE_TALENT_BASIC);
		Map<Integer, KGameExcelRow> talentBasicRows = new HashMap<Integer, KGameExcelTable.KGameExcelRow>();
		for(int i = 0; i < allRows.length; i++) {
			row = allRows[i];
			talentBasicRows.put(row.getInt("talentId"), row);
		}
		
		allRows = KGameUtilTool.getAllDataRows(file, tableMap, ITalentTableConfig.TABLE_TYPE_TALENT_DATA);
		KGameExcelRow dataRow;
		KTalentTemplate talentTemplate;
		for(int i = 0; i < allRows.length; i++) {
			dataRow = allRows[i];
			row = talentBasicRows.get(dataRow.getInt("talentId"));
			if(row == null) {
				throw new RuntimeException("不存在天赋点[" + dataRow.getInt("talentId") + "]的属性");
			}
			try {
				talentTemplate = new KTalentTemplate(row, dataRow);
			} catch (Exception e) {
				_LOGGER.error("加载天赋点：" + dataRow.getInt("talentId") + "出错！");
				throw e;
			}
			Map<Integer, KTalentTemplate> tempMap = allTalentTemplate.get(talentTemplate.talentTreeId);
			if(tempMap == null) {
				tempMap = new LinkedHashMap<Integer, KTalentTemplate>();
				allTalentTemplate.put(talentTemplate.talentTreeId, tempMap);
			}
			talentTemplate = tempMap.put(talentTemplate.talentId, talentTemplate);
			if(talentTemplate != null) {
				throw new RuntimeException("重复的天赋点id：" + talentTemplate.talentId);
			}
		}
		
		allRows = KGameUtilTool.getAllDataRows(file, tableMap, ITalentTableConfig.TABLE_TYPE_TALENT_TREE_BASIC);
		Map<Integer, KGameExcelRow> talentTreeBasicRows = new HashMap<Integer, KGameExcelTable.KGameExcelRow>();
		for (int i = 0; i < allRows.length; i++) {
			row = allRows[i];
			talentTreeBasicRows.put(row.getInt("talentTreeId"), row);
		}
		
		allRows = KGameUtilTool.getAllDataRows(file, tableMap, ITalentTableConfig.TABLE_TYPE_TALENT_TREE_DATA);
		KTalentTreeTemplate treeTemplate;
		for(int i = 0; i < allRows.length; i++) {
			dataRow = allRows[i];
			row = talentTreeBasicRows.get(dataRow.getInt("talentTreeId"));
			treeTemplate = new KTalentTreeTemplate(row, dataRow, allTalentTemplate.remove(dataRow.getInt("talentTreeId")));
			_allTalentTreeTemplates.put(treeTemplate.talentTreeId, treeTemplate);
		}
		
		if(allTalentTemplate.size() > 0) {
			throw new RuntimeException("天赋树id：" + allTalentTemplate.keySet().toString() + "在天赋树数据表中不存在！");
		}
		
		_allTalentTreeTemplatesReadOnly = Collections.unmodifiableList(new ArrayList<KTalentTreeTemplate>(_allTalentTreeTemplates.values()));
	}
	
	static void onGameWorldInitComplete() {
		KTalentTreeTemplate treeTemplate;
		int exCount = 0;
		for (int i = 0; i < _allTalentTreeTemplatesReadOnly.size(); i++) {
			treeTemplate = _allTalentTreeTemplatesReadOnly.get(i);
			try {
				treeTemplate.onGameWorldInitComplete();
			} catch (Exception e) {
				_LOGGER.error(e.getMessage());
				exCount++;
			}
			if (treeTemplate.nextTalentTreeId > 0) {
				KTalentTreeTemplate nextTemplate = _allTalentTreeTemplates.get(treeTemplate.nextTalentTreeId);
				if (nextTemplate == null) {
					_LOGGER.error("天赋树[{}]的下级天赋树为不存在！", treeTemplate.name);
					exCount++;
				}
			}
		}
		if(exCount > 0) {
			throw new RuntimeException("天赋模块数据异常！");
		}
	}
	
	static int getRoleLevel(long roleId) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role != null) {
			return role.getLevel();
		}
		return 0;
	}
	
	public static KTalentTreeTemplate getTalentTreeTemplate(int treeId) {
		return _allTalentTreeTemplates.get(treeId);
	}
	
	public static KTalentTemplate getTalentTemplateData(int treeId, int talentId) {
		KTalentTreeTemplate treeTemplate = _allTalentTreeTemplates.get(treeId);
		if(treeTemplate != null) {
			return treeTemplate.talentData.get(talentId);
		}
		return null;
	}
	
	public static List<KTalentTreeTemplate> getAllTalentTreeTemplates() {
		return _allTalentTreeTemplatesReadOnly;
	}
	
	public static KTalentEntireData getTalentEntireData(long roleId) {
		RoleExtCA extCA = KSupportFactory.getRoleModuleSupport().getRoleExtCA(roleId, KRoleExtTypeEnum.TALENT, true);
		if (extCA != null) {
			return (KTalentEntireData) extCA;
		}
		return null;
	}
}
