package com.kola.kmp.logic.talent;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.other.KGameAttrType;

/**
 * 
 * @author PERRY CHAN
 */
public class KTalentTreeTemplate {

	private static final Logger _LOGGER = KGameLogger.getLogger(KTalentTreeTemplate.class);
	
	public final int talentTreeId;
	public final String name;
	public final int iconId;
	public final int openLv;
	public final int nextTalentTreeId;
	public final String titleName;
	public final String descr;
	public final Map<KGameAttrType, Integer> activateEffectMap;
	public final Map<Integer, KTalentTemplate> talentData;
	
	public KTalentTreeTemplate(KGameExcelRow basicRow, KGameExcelRow dataRow, Map<Integer, KTalentTemplate> pTalentData) {
		this.talentTreeId = basicRow.getInt("talentTreeId");
		this.name = basicRow.getData("name");
		this.iconId = basicRow.getInt("iconId");
		this.openLv = dataRow.getInt("openLv");
		this.nextTalentTreeId = dataRow.getInt("nextTalentTreeId");
		this.titleName = basicRow.getData("titleName");
		this.descr = basicRow.getData("descr");
		String[] effectAtts = dataRow.getData("script").split(";");
		Map<KGameAttrType, Integer> tempEffectMap = new LinkedHashMap<KGameAttrType, Integer>();
		if (effectAtts.length > 0 && effectAtts[0].length() > 0) {
			String[] singleEffect;
			for (int i = 0; i < effectAtts.length; i++) {
				singleEffect = effectAtts[i].split(":");
				tempEffectMap.put(KGameAttrType.getAttrTypeEnum(Integer.parseInt(singleEffect[0])), Integer.parseInt(singleEffect[1]));
			}
		} 
		this.activateEffectMap = Collections.unmodifiableMap(tempEffectMap);
		this.talentData = Collections.unmodifiableMap(pTalentData);
		if(activateEffectMap.isEmpty()) {
			throw new RuntimeException("天赋树id：" + talentTreeId + "，激活影响属性数量为0！");
		}
	}
	
	void onGameWorldInitComplete() {
		int exCount = 0;
		KTalentTemplate template;
		for (Iterator<KTalentTemplate> itr = this.talentData.values().iterator(); itr.hasNext();) {
			template = itr.next();
			template.onGameWorldInitComplete();
			if(template.preTalentId > 0 && !this.talentData.containsKey(template.preTalentId)) {
				_LOGGER.error(StringUtil.format("天赋[{}]的前置天赋[{}]不存在于本天赋树中！", template.talentId, template.preTalentId));
				exCount++;
			} else if (template.preTalentId > template.talentId) {
				_LOGGER.error("天赋[{}]的前置天赋[{}]比其位置还要后！", template.talentId, template.preTalentId);
			}
		}
		if (exCount > 0) {
			throw new RuntimeException(StringUtil.format("天赋树数据异常！！天赋树id：{}", this.talentTreeId));
		}
	}
}
