package com.kola.kmp.logic.talent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KGameAttrType;

/**
 * 
 * @author PERRY CHAN
 */
public class KTalentTemplate {

	public final int talentId; // 天赋点模板id
	public final String name; // 名字
	public final int iconId; // 图标资源id
	public final String descr; // 描述
	public final int talentTreeId; // 天赋树id
	public final int preTalentId; // 前置天赋id
	public final boolean isMainline; // 是否主线天赋
	public final int requireLv; // 需求等级
	public final int potential; // 升级所需的潜能
	public final int honor; // 升级的荣誉
	public final int skillId; // 天赋所带的技能id
	public final Map<Integer, Map<KGameAttrType, Integer>> effectAttr; // 天赋影响的属性，key=等级，value=map{属性类型，影响数值}
	public final boolean isSkillTalent; // 是否技能天赋
	public final int maxLv; // 天赋点最大的等级
	private Map<Integer, String> _effectDescr; // 天赋影响的属性的描述，key=等级
	
	public KTalentTemplate(KGameExcelRow basicRow, KGameExcelRow dataRow) {
		this.talentId = basicRow.getInt("talentId");
		this.name = basicRow.getData("name");
		this.iconId = basicRow.getInt("iconId");
		this.descr = basicRow.getData("descr");
		this.talentTreeId = dataRow.getInt("talentTreeId");
		this.preTalentId = dataRow.getInt("preTalentId");
		this.isMainline = dataRow.getBoolean("isMainline");
		this.requireLv = dataRow.getInt("requiredLv");
		this.potential = dataRow.getInt("potential");
		this.honor = dataRow.getInt("honor");
		this.skillId = UtilTool.getStringToInt(dataRow.getData("skillId"));
		this.maxLv = dataRow.getInt("maxLv");
		this.isSkillTalent = this.skillId > 0;
		Map<Integer, Map<KGameAttrType, Integer>> tempEffectMap = new HashMap<Integer, Map<KGameAttrType, Integer>>();
		effectAttr = Collections.unmodifiableMap(tempEffectMap);
		if (!isSkillTalent) {
			String[] effectInfo = dataRow.getData("script").split(";");
			if (effectInfo.length > 0 && effectInfo[0].length() > 0) {
				Map<KGameAttrType, List<Integer>> attrByType = new HashMap<KGameAttrType, List<Integer>>();
				String[] singleEffectInfo;
				List<Integer> list;
				String[] attrValues;
				for (int i = 0; i < effectInfo.length; i++) {
					singleEffectInfo = effectInfo[i].split(":");
					attrValues = singleEffectInfo[1].split(",");
					list = new ArrayList<Integer>();
					attrByType.put(KGameAttrType.getAttrTypeEnum(Integer.parseInt(singleEffectInfo[0])), list);
					for (int k = 0; k < attrValues.length; k++) {
						list.add(Integer.parseInt(attrValues[k]));
					}
				}
				Map<KGameAttrType, Integer> totalEffect;
				Map.Entry<KGameAttrType, List<Integer>> entry;
				for (int i = 0; i < this.maxLv; i++) {
					totalEffect = new LinkedHashMap<KGameAttrType, Integer>();
					tempEffectMap.put(i + 1, Collections.unmodifiableMap(totalEffect));
					for (Iterator<Map.Entry<KGameAttrType, List<Integer>>> itr = attrByType.entrySet().iterator(); itr.hasNext();) {
						entry = itr.next();
						totalEffect.put(entry.getKey(), entry.getValue().get(i));
					}
				}
			}
		}
		if (isSkillTalent) {
			if (this.maxLv > 1) {
				throw new RuntimeException("技能天赋的等级不能大于1！天赋点id是：" + this.talentId);
			}
		} else {
			if (effectAttr.size() != maxLv) {
				throw new RuntimeException("影响属性的数量与等级数不一致！天赋点id是：" + this.talentId);
			}
		}
	}
	
	public void onGameWorldInitComplete() {
		Map<Integer, String> descrMap = new HashMap<Integer, String>(effectAttr.size());
		Map.Entry<KGameAttrType, Integer> entry2;
		StringBuilder strBld = new StringBuilder();
		Map.Entry<Integer, Map<KGameAttrType, Integer>> entry;
		Map<KGameAttrType, Integer> map;
		for (Iterator<Map.Entry<Integer, Map<KGameAttrType, Integer>>> itr = effectAttr.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			map = entry.getValue();
			for (Iterator<Map.Entry<KGameAttrType, Integer>> itr2 = map.entrySet().iterator(); itr2.hasNext();) {
				entry2 = itr2.next();
				strBld.append(entry2.getKey().getValueDesc(entry2.getValue())).append("、");
			}
			strBld.deleteCharAt(strBld.length() - 1);
			descrMap.put(entry.getKey(), strBld.toString());
			strBld.delete(0, strBld.length());
		}
		_effectDescr = Collections.unmodifiableMap(descrMap);
	}
	
	public String getDescrByLv(int lv) {
		return _effectDescr.get(lv);
	}
}
