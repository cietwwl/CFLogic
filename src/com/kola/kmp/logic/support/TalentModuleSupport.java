package com.kola.kmp.logic.support;

import java.util.List;
import java.util.Map;

import com.kola.kmp.logic.talent.ITalent;
import com.kola.kmp.logic.talent.KTalentEntireData;

/**
 * 
 * @author PERRY CHAN
 */
public interface TalentModuleSupport {

	/**
	 * 
	 * @param roleId
	 * @return
	 */
	public KTalentEntireData getTalentData(long roleId);
	
	/**
	 * 获取所有的天赋，key=天赋树模板id，value=天赋树的所有天赋
	 * @param roleId
	 * @return
	 */
	public Map<String, List<ITalent>> getAllTalentData(long roleId);
}
