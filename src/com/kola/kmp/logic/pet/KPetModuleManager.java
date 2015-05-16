package com.kola.kmp.logic.pet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.pet.Pet;
import com.kola.kgame.cache.pet.PetModuleFactory;
import com.kola.kgame.cache.pet.PetSet;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.chat.KWordBroadcastType;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.PropertyTypeEnum;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KPetGetWay;
import com.kola.kmp.logic.other.KPetQuality;
import com.kola.kmp.logic.other.KPetType;
import com.kola.kmp.logic.other.KTableInfo;
import com.kola.kmp.logic.pet.message.KPetServerMsgSender;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KBattlePowerCalculator;
import com.kola.kmp.logic.util.KGameUtilTool;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.PetTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KPetModuleManager {
	
//	private static final Logger _LOGGER = KGameLogger.getLogger(KPetModuleManager.class);

	private static Map<Integer, KPetTemplate> _allTemplates = new HashMap<Integer, KPetTemplate>();
	private static Map<Integer, KPetTemplate> _allTemplatesReadOnly = Collections.unmodifiableMap(_allTemplates);
	private static Map<KPetQuality, List<KPetTemplateHandbookModel>> _petHandbookMap;
	private static Map<KPetQuality, Map<Integer, Integer>> _petUpgradeExpMap = new HashMap<KPetQuality, Map<Integer,Integer>>();
	private static Map<KPetQuality, Float> _composeExpParaMap = new HashMap<KPetQuality, Float>(); // 合成经验系数，key=品质，value=系数
	private static Map<KPetType, Map<KPetQuality, Integer>> _composeFeeMap = new HashMap<KPetType, Map<KPetQuality,Integer>>(); // 合成费用，key=阶级，value={key=品质，费用值}
//	private static Map<KPetType, Map<KPetQuality, KPetLevelAttribute>> _upgradeAttrMap = new HashMap<KPetType, Map<KPetQuality,KPetLevelAttribute>>();
	private static Map<Byte, KPetAttrPara> _attrDeviParaMap = new HashMap<Byte, KPetAttrPara>(); // 属性偏向参数列表，key=属性偏向类型，value=属性偏向的参数
	private static Map<Integer, KPetAttrPara> _upgradeAttrParaMap = new HashMap<Integer, KPetAttrPara>(); // 升级属性参数
	private static Map<Integer, KPetAttrPara> _starAttrParaMap = new HashMap<Integer, KPetAttrPara>(); // 升星属性参数
	
	private static void checkBroadcast(KRole role, KPet pet) {
		if (pet.getQuality().isSenior()) {
			KPetTemplate template = getPetTemplate(pet.getTemplateId());
			if (template.willWorldBroadcast) {
				if (pet.getQuality() == KPetQuality.RED) {
					KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(KWordBroadcastType.随从_x角色获得了一个红色随从x.content, role.getExName(), pet.getNameEx()),
							KWordBroadcastType.随从_x角色获得了一个红色随从x);
				} else {
					KSupportFactory.getChatSupport().sendSystemChat(StringUtil.format(KWordBroadcastType.随从_XX通过努力终于召唤出XXX随从.content, role.getExName(), pet.getNameEx()),
							KWordBroadcastType.随从_XX通过努力终于召唤出XXX随从);
				}
			}
		}
	}
	
	static void loadPetData(String path, Map<Byte, KTableInfo> tableMap) throws Exception {
		KGameExcelFile file = new KGameExcelFile(path);
		tableMap = Collections.unmodifiableMap(tableMap);
		KGameExcelRow[] allRows = KGameUtilTool.getAllDataRows(file, tableMap, IPetTableConfig.TABLE_PET_TEMPLATE_DATA);
		KPetTemplate template;
		Map<KPetQuality, List<KPetTemplateHandbookModel>> templateQualityMap = new LinkedHashMap<KPetQuality, List<KPetTemplateHandbookModel>>();
		_petHandbookMap = Collections.unmodifiableMap(templateQualityMap);
		List<KPetTemplateHandbookModel> tempList;
		for (int i = 0; i < allRows.length; i++) {
			template = new KPetTemplate(allRows[i]);
			_allTemplates.put(template.templateId, template);
			tempList = templateQualityMap.get(template.quality);
			if (tempList == null) {
				tempList = new ArrayList<KPetTemplateHandbookModel>();
				templateQualityMap.put(template.quality, tempList);
			}
			tempList.add(new KPetTemplateHandbookModel(template));
		}
		for (Iterator<Map.Entry<KPetQuality, List<KPetTemplateHandbookModel>>> itr = templateQualityMap.entrySet().iterator(); itr.hasNext();) {
			Map.Entry<KPetQuality, List<KPetTemplateHandbookModel>> tempEntry = itr.next();
			Collections.sort(tempEntry.getValue());
			tempEntry.setValue(Collections.unmodifiableList(tempEntry.getValue()));
		}
//		tableInfo = tableMap.get(IPetTableConfig.TABLE_PET_RANDOM_SKILL);
//		table = file.getTable(tableInfo.tableName, tableInfo.headerIndex);
//		allRows = table.getAllDataRows();
//		Map<Integer, Map<Integer, Integer>> randomMap = new HashMap<Integer, Map<Integer,Integer>>();
//		KGameExcelRow row;
//		int templateId;
//		int skillId;
//		int rate;
//		for (int i = 0; i < allRows.length; i++) {
//			row = allRows[i];
//			templateId = row.getInt("templateId");
//			skillId = row.getInt("skillId");
//			rate = row.getInt("rate");
//			Map<Integer, Integer> tempRandomMap = randomMap.get(templateId);
//			if (tempRandomMap == null) {
//				tempRandomMap = new LinkedHashMap<Integer, Integer>();
//				randomMap.put(templateId, tempRandomMap);
//			} else {
//				Map.Entry<Integer, Integer> current;
//				for (Iterator<Map.Entry<Integer, Integer>> itr = tempRandomMap.entrySet().iterator(); itr.hasNext();) {
//					current = itr.next();
//					rate += current.getValue();
//				}
//			}
//			tempRandomMap.put(skillId, rate);
//		}
//		for (Iterator<Map.Entry<Integer, Map<Integer, Integer>>> itr = randomMap.entrySet().iterator(); itr.hasNext();) {
//			Map.Entry<Integer, Map<Integer, Integer>> temp = itr.next();
//			KRandomModel<Integer> model = new KRandomModel<Integer>(temp.getValue(), 0);
//			_petBornSkillRandom.put(temp.getKey(), model);
//		}
		
		allRows = KGameUtilTool.getAllDataRows(file, tableMap, IPetTableConfig.TABLE_PET_UPGRADE_EXP);
		KPetQuality[] allQualitys = KPetQuality.values();
		for(int i = 0; i < allQualitys.length; i++) {
			_petUpgradeExpMap.put(allQualitys[i], new HashMap<Integer, Integer>());
		}
		for(int i = 0; i < allRows.length; i++) {
			KGameExcelRow row = allRows[i];
			int level = row.getInt("level");
			_petUpgradeExpMap.get(KPetQuality.GREEN).put(level, row.getInt("green"));
			_petUpgradeExpMap.get(KPetQuality.BLUE).put(level, row.getInt("blue"));
			_petUpgradeExpMap.get(KPetQuality.PURPLE).put(level, row.getInt("purple"));
			_petUpgradeExpMap.get(KPetQuality.ORANGE).put(level, row.getInt("orange"));
			_petUpgradeExpMap.get(KPetQuality.RED).put(level, row.getInt("red"));
		}
		
		allRows = KGameUtilTool.getAllDataRows(file, tableMap, IPetTableConfig.TABLE_PET_COMPOSE_EXP_PARA);
		for (int i = 0; i < allRows.length; i++) {
			KGameExcelRow row = allRows[i];
			_composeExpParaMap.put(KPetQuality.getEnumQuality(row.getInt("qualityexp")), row.getFloat("BasisQuality"));
		}
		
		allRows = KGameUtilTool.getAllDataRows(file, tableMap, IPetTableConfig.TABLE_PET_COMPOSE_FEE);
		for (int i = 0; i < KPetType.values().length; i++) {
			_composeFeeMap.put(KPetType.values()[i], new HashMap<KPetQuality, Integer>());
		}
		for(int i = 0; i < allRows.length; i++) {
			KGameExcelRow row = allRows[i];
			KPetType type = KPetType.getPetType(row.getInt("type"));
			Map<KPetQuality, Integer> tempMap = _composeFeeMap.get(type);
			Integer pre = tempMap.put(KPetQuality.getEnumQuality(row.getInt("quality")), row.getInt("goldcost"));
			if(pre != null) {
				throw new RuntimeException("重复的合成费用记录！类型：" + type.sign + "，品质：" + row.getInt("quality"));
			}
		}
		
//		allRows = KGameUtilTool.getAllDataRows(file, tableMap, IPetTableConfig.TABLE_PET_UPGRADE_ATTRIBUTE);
//		for (int i = 0; i < KPetType.values().length; i++) {
//			_upgradeAttrMap.put(KPetType.values()[i], new HashMap<KPetQuality, KPetLevelAttribute>());
//		}
//		for (int i = 0; i < allRows.length; i++) {
//			KGameExcelRow row = allRows[i];
//			KPetType type = KPetType.getPetType(row.getInt("type"));
//			Map<KPetQuality, KPetLevelAttribute> tempMap = _upgradeAttrMap.get(type);
//			KPetLevelAttribute pre = tempMap.put(KPetQuality.getEnumQuality(row.getInt("quality")), new KPetLevelAttribute(row));
//			if (pre != null) {
//				throw new RuntimeException("重复的升级属性记录！阶级：" + type.sign + "，品质：" + row.getInt("quality"));
//			}
//		}
		
		allRows = KGameUtilTool.getAllDataRows(file, tableMap, IPetTableConfig.TABLE_PET_ATTR_DEVI_PARA);
		for(int i = 0; i < allRows.length; i++) {
			KGameExcelRow row = allRows[i];
			KPetAttrPara para = new KPetAttrPara(row);
			_attrDeviParaMap.put(row.getByte("AttributeDeviation"), para);
		}
		
		allRows = KGameUtilTool.getAllDataRows(file, tableMap, IPetTableConfig.TABLE_PET_UPGRADE_ATTR_PARA);
		for(int i = 0; i < allRows.length; i++) {
			KGameExcelRow row = allRows[i];
			KPetAttrPara para = new KPetAttrPara(row);
			_upgradeAttrParaMap.put(row.getInt("lv"), para);
		}
		
		allRows = KGameUtilTool.getAllDataRows(file, tableMap, IPetTableConfig.TABLE_PET_STAR_ATTR_PARA);
		for (int i = 0; i < allRows.length; i++) {
			KGameExcelRow row = allRows[i];
			KPetAttrPara para = new KPetAttrPara(row);
			_starAttrParaMap.put(row.getInt("starLv"), para);
		}
		
		allRows = KGameUtilTool.getAllDataRows(file, tableMap, IPetTableConfig.TABLE_PET_PCT_CONFIG);
		KPetModuleConfig.initProportion(allRows[0]);
		
		allRows = KGameUtilTool.getAllDataRows(file, tableMap, IPetTableConfig.TABLE_PET_BATTLE_POWER_PARA);
		KBattlePowerCalculator.initPetCalculatePara(allRows);
		
		allRows = KGameUtilTool.getAllDataRows(file, tableMap, IPetTableConfig.TABLE_PET_GET_WAY);
		for(int i = 0; i < allRows.length; i++) {
			KGameExcelRow row = allRows[i];
			KPetGetWay way = KPetGetWay.getEnum(row.getInt("typeId"));
			if(way == null) {
				throw new RuntimeException("找不到获取类型：" + row.getInt("typeId") + "的枚举！");
			}
			way.name = row.getData("typeName");
		}
	}
	
	static void onGameWorldInitComplete() {
		for(Iterator<KPetTemplate> itr = _allTemplates.values().iterator(); itr.hasNext();) {
			itr.next().onGameWorldInitComplete();
		}
		for(Iterator<List<KPetTemplateHandbookModel>> itr = _petHandbookMap.values().iterator(); itr.hasNext();) {
			List<KPetTemplateHandbookModel> list = itr.next();
			for(int i = 0; i < list.size(); i++) {
				list.get(i).onGameWorldInitCompete();
			}
		}
		KPetType[] allTypes = KPetType.values();
		KPetQuality[] allQualities = KPetQuality.values();
		Map<KPetQuality, Integer> map;
//		Map<KPetQuality, KPetLevelAttribute> lvAttrMap;
		for(int i = 0; i < allTypes.length; i++) {
			map = _composeFeeMap.get(allTypes[i]);
//			lvAttrMap = _upgradeAttrMap.get(allTypes[i]);
			for (int k = 0; k < allQualities.length; k++) {
				if (!map.containsKey(allQualities[k])) {
					throw new RuntimeException("随从金币消耗表，阶级：[" + allTypes[i].sign + "]，不存在品质为[" + allQualities[k].sign + "]的价格信息！");
				}
//				if(!lvAttrMap.containsKey(allQualities[k])) {
//					throw new RuntimeException("随从升级属性表，阶级：[" + allTypes[i].sign + "]，不存在品质为[" + allQualities[k].sign + "]的升级属性信息！");
//				}
			}
		}
		KPetModuleConfig.initComplete();
	}
	
	static final KPetTemplate getPetTemplate(int pTemplateId) {
		return _allTemplates.get(pTemplateId);
	}
	
	static final void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		KPetServerMsgSender.sendAllPets(session, role);
	}
	
	static final int getUpgradeExp(KPetQuality quality, int lv) {
		Map<Integer, Integer> map = _petUpgradeExpMap.get(quality);
		return map.get(lv);
	}
	
	static final float getComposeExpPara(KPetQuality quality) {
		return _composeExpParaMap.get(quality);
	}
	
	static final int getComposeFee(KPetType type, KPetQuality quality) {
		Map<KPetQuality, Integer> map = _composeFeeMap.get(type);
		return map.get(quality);
	}
	
//	static final KPetLevelAttribute getLvAttribute(KPetType type, KPetQuality quality) {
//		Map<KPetQuality, KPetLevelAttribute> map = _upgradeAttrMap.get(type);
//		return map.get(quality);
//	}
	
	static final KPetAttrPara getLvAttrPara(int lv) {
		return _upgradeAttrParaMap.get(lv);
	}
	
	static final KPetAttrPara getStarAttrPara(int starLv) {
		return _starAttrParaMap.get(starLv);
	}
	
	static final KPetAttrPara getAttrDeviPara(byte attrDevi) {
		return _attrDeviParaMap.get(attrDevi);
	}
	
	static final KPetSet getPetSet(long roleId) {
		PetSet ps = PetModuleFactory.getPetModule().getPetSet(roleId);
		if (ps != null) {
			return (KPetSet) ps;
		}
		return null;
	}
	
	static KActionResult<Pet> createPetToRole(long roleId, int templateId, String descr) {
		KActionResult<Pet> result = new KActionResult<Pet>();
		KPetSet ps = getPetSet(roleId);
		if (ps != null) {
			if(ps.getAvailableCapacity() > 0) {
				KPetTemplate template = getPetTemplate(templateId);
				if(template != null) {
					KPet pet = new KPet(template);
					pet = (KPet) ps.addPet(pet, false);
					result.attachment = pet;
					result.success = true;
					KPetServerMsgSender.sendAddPetToClient(roleId, pet);
//					KPetFlowLogger.logNewSinglePet(roleId, pet);
				} else {
					result.tips = PetTips.getTipsNoSuchPetTemplate();
				}
			} else {
				result.tips = PetTips.getTipsPetSetIsFull();
			}
		} else {
			result.tips = GlobalTips.getTipsServerBusy();
		}
		if(result.success) {
			checkBroadcast(KSupportFactory.getRoleModuleSupport().getRole(roleId), (KPet)result.attachment);
		}
		return result;
	}
	
	static KActionResult<List<KPet>> addPets(long roleId, List<Integer> templateIds, boolean allowOverFlow, String descr) {
		KActionResult<List<KPet>> result = new KActionResult<List<KPet>>();
		KPetSet ps = getPetSet(roleId);
		if (ps != null) {
			boolean capacityNotPass = false;
			if (!allowOverFlow) {
				capacityNotPass = ps.getAvailableCapacity() < templateIds.size();
			}
			if (capacityNotPass) {
				result.tips = PetTips.getTipsPetSetIsFull();
			} else {
				int templateId;
				KPetTemplate template;
				List<KPet> addPets = new ArrayList<KPet>(templateIds.size());
				for (int i = 0; i < templateIds.size(); i++) {
					templateId = templateIds.get(i);
					template = getPetTemplate(templateId);
					if (template != null) {
						KPet pet = new KPet(template);
						pet = (KPet) ps.addPet(pet, allowOverFlow);
						addPets.add(pet);
					}
				}
				result.attachment = addPets;
				result.success = true;
				KPetServerMsgSender.sendAddMultiplePetsToClient(roleId, addPets);
//				KPetFlowLogger.logNewPets(roleId, addPets);
				for(int i = 0; i < addPets.size(); i++) {
					KPet pet = addPets.get(i);
					FlowManager.logPropertyAddOrDelete(roleId, PropertyTypeEnum.宠物, pet.getUUID(), pet.getTemplateId(), pet.getName(), true, descr);
				}
			}
		} else {
			result.tips = GlobalTips.getTipsServerBusy();
		}
		if(result.success) {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			List<KPet> list = result.attachment;
			for(int i = 0; i < list.size(); i++) {
				checkBroadcast(role, list.get(i));
			}
		}
		return result;
	}
	
	static KPet getFightingPet(long roleId) {
		KPetSet ps = getPetSet(roleId);
		if(ps != null) {
			return ps.getFightingPet();
		}
		return null;
	}
	
	static float calculateSingle(int attrDeviPara, int lvPara, int growValue, float attrProportion) {
		/*
		 * 宠物等级对应基础属性=属性偏向对应属性最大值*等级对应属性比例/10000*成长值/100*宠物升级属性系数
		 * 宠物升星对应基础属性=属性偏向对应属性最大值*星级对应属性比例/10000*成长值/100*宠物升星属性系数
		 */
		return (float)attrDeviPara * lvPara / UtilTool.TEN_THOUSAND_RATIO_UNIT * growValue / UtilTool.HUNDRED_RATIO_UNIT * attrProportion;
	}
	
	static void notifyPetInfoChane(KPet pet) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(pet.getOwnerId());
		KSupportFactory.getRankModuleSupport().notifyPetInfoChange(role, pet.getName(), pet.getLevel(), pet.getAttributeByType(KGameAttrType.BATTLE_POWER));
	}
	
	public static Map<Integer, KPetTemplate> getAllPetTemplates() {
		return _allTemplatesReadOnly;
	}
	
	public static Map<KPetQuality, List<KPetTemplateHandbookModel>> getAllPetTemplatesByQuality() {
		return _petHandbookMap;
	}
	
	
}
