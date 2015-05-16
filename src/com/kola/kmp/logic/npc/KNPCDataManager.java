package com.kola.kmp.logic.npc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.combat.api.ICombatMonster;
import com.kola.kmp.logic.npc.KNPCDataStructs.KDropInfoTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.KNPCTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.MonstUIData;
import com.kola.kmp.logic.npc.KNPCDataStructs.ObstructionTemplate;

public final class KNPCDataManager {
	// 所有NPC模板数据
	static NPCTemplateManager mNPCTemplateManager = new NPCTemplateManager();
	// 所有怪物模板数据
	static MonstTemplateManager mMonstTemplateManager = new MonstTemplateManager();
	// 所有怪物属性数据
	static MonstUIDataManager mMonstUIDataManager = new MonstUIDataManager();
	// 战场障碍物数据模版数据
	static ObstructionTempDataManager mObstructionTempDataManager = new ObstructionTempDataManager();
	// 掉落模板数据
	static KDropInfoTemplateDataManager mDropInfoTempDataMamanger = new KDropInfoTemplateDataManager();

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * NPC模板数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-27 下午9:11:34
	 * </pre>
	 */
	static class NPCTemplateManager {
		/**
		 * <pre>
		 * 全部怪物
		 * KEY = 模版ID
		 * </pre>
		 */
		private final HashMap<Integer, KNPCTemplate> templateMap = new HashMap<Integer, KNPCTemplate>();

		private NPCTemplateManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param temps
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2012-11-27 下午9:13:25
		 * </pre>
		 */
		void initData(List<KNPCTemplate> tempDatas) throws Exception {
			templateMap.clear();

			for (KNPCTemplate tempData : tempDatas) {
				KNPCTemplate oldData = templateMap.put(tempData.templateId, tempData);
				if (oldData != null) {
					throw new Exception("重复的模板ID=" + tempData.templateId);
				}
			}
		}

		KNPCTemplate getTemplate(int templateId) {
			return templateMap.get(templateId);
		}

		boolean containTemplate(int templateId) {
			return templateMap.containsKey(templateId);
		}

		private void onGameWorldInitComplete() throws Exception {
			for (KNPCTemplate temp : templateMap.values()) {
				temp.onGameWorldInitComplete();
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 怪物模板数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-27 下午9:11:34
	 * </pre>
	 */
	static class MonstTemplateManager {
		/**
		 * <pre>
		 * 全部怪物
		 * KEY = 怪物模板ID
		 * </pre>
		 */
		private final HashMap<Integer, KMonstTemplate> templateMap = new HashMap<Integer, KMonstTemplate>();
		/**
		 * 
		 */
		private final Map<Integer, ICombatMonster> combatMonsterMap = new HashMap<Integer, ICombatMonster>();
		
		private MonstTemplateManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param temps
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2012-11-27 下午9:13:25
		 * </pre>
		 */
		void initData(List<KMonstTemplate> tempDatas) throws Exception {
			templateMap.clear();

			for (KMonstTemplate tempData : tempDatas) {
				KMonstTemplate oldData = templateMap.put(tempData.id, tempData);
				if (oldData != null) {
					throw new Exception("重复的模板ID=" + tempData.id);
				}
			}
		}

		KMonstTemplate getTemplate(int templateId) {
			return templateMap.get(templateId);
		}
		
		ICombatMonster getCombatMonster(KMonstTemplate template) {
			return combatMonsterMap.get(template.id);
		}

		boolean containTemplate(int templateId) {
			return templateMap.containsKey(templateId);
		}

		private void onGameWorldInitComplete() throws Exception {
			combatMonsterMap.clear();
			for (KMonstTemplate temp : templateMap.values()) {
				temp.onGameWorldInitComplete();
				combatMonsterMap.put(temp.id, new KGameMonster(temp));
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 怪物形象数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-27 下午9:11:34
	 * </pre>
	 */
	static class MonstUIDataManager {
		/**
		 * <pre>
		 * 全部数据
		 * KEY = 怪物形象模板ID
		 * </pre>
		 */
		private final HashMap<Integer, MonstUIData> elementMap = new HashMap<Integer, MonstUIData>();

		private MonstUIDataManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param temps
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2012-11-27 下午9:13:25
		 * </pre>
		 */
		void initData(List<MonstUIData> datas) throws Exception {
			elementMap.clear();

			for (MonstUIData tempData : datas) {
				MonstUIData oldData = elementMap.put(tempData.monstUITempId, tempData);
				if (oldData != null) {
					throw new Exception("重复的ID=" + tempData.monstUITempId);
				}
			}
		}

		MonstUIData getData(int monsteUITempId) {
			return elementMap.get(monsteUITempId);
		}

		boolean containData(int monsteUITempId) {
			return elementMap.containsKey(monsteUITempId);
		}

		private void onGameWorldInitComplete() throws Exception {
			for (MonstUIData temp : elementMap.values()) {
				temp.onGameWorldInitComplete();
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 战场障碍物数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-27 下午9:11:34
	 * </pre>
	 */
	static class ObstructionTempDataManager {
		/**
		 * <pre>
		 * 全部数据
		 * KEY = 战场障碍物模板ID
		 * </pre>
		 */
		private final HashMap<Integer, ObstructionTemplate> elementMap = new HashMap<Integer, ObstructionTemplate>();

		/**
		 * 
		 */
		private final Map<Integer, ObstructionTemplate> elementMapRO = Collections.unmodifiableMap(elementMap);
		
		/**
		 * 
		 */
		private final Map<Integer, KGameObstruction> obstructionMap = new HashMap<Integer, KGameObstruction>();
		
		private ObstructionTempDataManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param temps
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2012-11-27 下午9:13:25
		 * </pre>
		 */
		void initData(List<ObstructionTemplate> datas) throws Exception {
			elementMap.clear();

			for (ObstructionTemplate tempData : datas) {
				ObstructionTemplate oldData = elementMap.put(tempData.id, tempData);
				if (oldData != null) {
					throw new Exception("重复的ID=" + tempData.id);
				}
			}
		}

		ObstructionTemplate getData(int tempId) {
			return elementMap.get(tempId);
		}
		
		KGameObstruction getObstruction(int templateId) {
			return obstructionMap.get(templateId);
		}

		boolean containData(int tempId) {
			return elementMap.containsKey(tempId);
		}
		
		Map<Integer, ObstructionTemplate> getCache() {
			return elementMapRO;
		}

		private void onGameWorldInitComplete() throws Exception {
			for (ObstructionTemplate temp : elementMap.values()) {
				temp.onGameWorldInitComplete();
				this.obstructionMap.put(temp.id, new KGameObstruction(temp));
			}
		}
	}
	
	static class KDropInfoTemplateDataManager {
		
		private final Map<Integer, KDropInfoTemplate> elementMap = new HashMap<Integer, KDropInfoTemplate>();
		
		private KDropInfoTemplateDataManager() {
			
		}
		
		private void onGameWorldInitComplete() throws Exception {
			for (KDropInfoTemplate temp : elementMap.values()) {
				temp.onGameWorldInitComplete();
			}
		}
		
		void initData(List<KDropInfoTemplate> dataList) {
			KDropInfoTemplate template;
			for(int i = 0; i < dataList.size(); i++) {
				template = dataList.get(i);
				elementMap.put(template.getDropId(), template);
			}
		}
		
		KDropInfoTemplate getDropInfoTemplate(int dropId) {
			return this.elementMap.get(dropId);
		}
	}

	/**
	 * <pre>
	 * 服务器启动完成
	 * 
	 * @author CamusHuang
	 * @creation 2012-12-3 下午8:41:04
	 * </pre>
	 */
	static void onGameWorldInitComplete() throws KGameServerException {
		try {
			mNPCTemplateManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KNPCDataLoader.SheetName_NPC模板 + "]错误：" + e.getMessage(), e);
		}

		try {
			mMonstTemplateManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KNPCDataLoader.SheetName_怪物模板 + "]错误：" + e.getMessage(), e);
		}

		try {
			mMonstUIDataManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KNPCDataLoader.SheetName_怪物形象表 + "]错误：" + e.getMessage(), e);
		}
		
		try {
			mObstructionTempDataManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KNPCDataLoader.SheetName_战场障碍物数据表 + "]错误：" + e.getMessage(), e);
		}
		
		try {
			mDropInfoTempDataMamanger.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KNPCDataLoader.SheetName_掉落数据表 + "]错误：" + e.getMessage(), e);
		}
		
	}
}
