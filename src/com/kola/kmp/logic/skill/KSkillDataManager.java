package com.kola.kmp.logic.skill;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.skill.KSkillDataStructs.KImmunityOfArrayCheck;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleSkillTempAbs.SkillTempLevelData;
import com.kola.kmp.logic.skill.KSkillDataStructs.SkillStatusData;

public final class KSkillDataManager {
	// 所有角色主动技能模板数据
	static RoleIniSkillTempManager mRoleIniSkillTempManager = new RoleIniSkillTempManager();
	// 所有角色被动技能模板数据
	static RolePasSkillTempManager mRolePasSkillTempManager = new RolePasSkillTempManager();
	// 所有机甲技能模板数据
	static RoleIniSkillTempManager mMountSkillTempManager = new RoleIniSkillTempManager();
	// 所有宠物技能模板数据
	static RoleIniSkillTempManager mPetSkillTempManager = new RoleIniSkillTempManager();
	// 所有随从被动技能模板数据
	static RolePasSkillTempManager mPetPasSkillTempManager = new RolePasSkillTempManager();
	// 所有的怪物技能模板数据
	static RoleIniSkillTempManager mMonsterSkillTempManager = new RoleIniSkillTempManager();
	// 所有技能状态数据
	static SkillStatusDataManager mSkillStatusManager = new SkillStatusDataManager();
	// 所有技能召唤物数据
	static SkillMinionDataManager mSkillMinionManager = new SkillMinionDataManager();

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 角色主动技能模板数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-27 下午9:11:34
	 * </pre>
	 */
	static class RoleIniSkillTempManager {
		/**
		 * <pre>
		 * 全部技能
		 * KEY = 模版ID
		 * </pre>
		 */
		private final LinkedHashMap<Integer, KRoleIniSkillTemp> templateMap = new LinkedHashMap<Integer, KRoleIniSkillTemp>();

		private RoleIniSkillTempManager() {
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
		void initData(List<KRoleIniSkillTemp> datas) throws Exception {
			templateMap.clear();

			for (KRoleIniSkillTemp tempData : datas) {
				KRoleIniSkillTemp oldData = templateMap.put(tempData.id, tempData);
				if (oldData != null) {
					throw new Exception("重复的模板ID=" + tempData.id);
				}
			}
		}

		KRoleIniSkillTemp getTemplate(int templateId) {
			return templateMap.get(templateId);
		}

		boolean containTemplate(int templateId) {
			return templateMap.containsKey(templateId);
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 慎用
		 * @return
		 * @author CamusHuang
		 * @creation 2014-3-26 下午12:00:44
		 * </pre>
		 */
		Map<Integer, KRoleIniSkillTemp> getCache() {
			return templateMap;
		}

		private void onGameWorldInitComplete() throws Exception {
			for (KRoleIniSkillTemp temp : templateMap.values()) {
				try {
					temp.onGameWorldInitComplete();
				} catch (Exception e) {
					throw new Exception(e.getMessage() + " 技能ID=" + temp.id, e);
				}
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 角色被动技能模板数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-27 下午9:11:34
	 * </pre>
	 */
	static class RolePasSkillTempManager {
		/**
		 * <pre>
		 * 全部技能
		 * KEY = 模版ID
		 * </pre>
		 */
		private final LinkedHashMap<Integer, KRolePasSkillTemp> templateMap = new LinkedHashMap<Integer, KRolePasSkillTemp>();

		private RolePasSkillTempManager() {
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
		void initData(List<KRolePasSkillTemp> datas) throws Exception {
			templateMap.clear();

			for (KRolePasSkillTemp tempData : datas) {
				KRolePasSkillTemp oldData = templateMap.put(tempData.id, tempData);
				if (oldData != null) {
					throw new Exception("重复的模板ID=" + tempData.id);
				}
			}
		}

		KRolePasSkillTemp getTemplate(int templateId) {
			return templateMap.get(templateId);
		}

		boolean containTemplate(int templateId) {
			return templateMap.containsKey(templateId);
		}

		/**
		 * <pre>
		 * 
		 * @deprecated 慎用
		 * @return
		 * @author CamusHuang
		 * @creation 2014-3-26 下午12:00:44
		 * </pre>
		 */
		Map<Integer, KRolePasSkillTemp> getCache() {
			return templateMap;
		}

		private void onGameWorldInitComplete(boolean isForRole) throws Exception {
			for (KRolePasSkillTemp temp : templateMap.values()) {
				try {
					temp.onGameWorldInitComplete(isForRole);
				} catch (Exception e) {
					throw new Exception(e.getMessage() + " 技能ID=" + temp.id);
				}
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 技能状态数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-27 下午9:11:34
	 * </pre>
	 */
	static class SkillStatusDataManager {
		/**
		 * <pre>
		 * 全部技能状态
		 * KEY = 状态ID
		 * </pre>
		 */
		private final HashMap<Integer, SkillStatusData> elementMap = new HashMap<Integer, SkillStatusData>();
		private final Map<Integer, SkillStatusData> elementMapReadOnly = Collections.unmodifiableMap(elementMap);

		private SkillStatusDataManager() {
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
		void initData(List<SkillStatusData> datas) throws Exception {
			elementMap.clear();

			for (SkillStatusData tempData : datas) {
				SkillStatusData oldData = elementMap.put(tempData.status_id, tempData);
				if (oldData != null) {
					throw new Exception("重复的状态ID=" + tempData.status_id);
				}
			}
		}

		SkillStatusData getData(int statusId) {
			return elementMap.get(statusId);
		}

		boolean containData(int statusId) {
			return elementMap.containsKey(statusId);
		}

		private void onGameWorldInitComplete() throws Exception {
			for (SkillStatusData temp : elementMap.values()) {
				try {
					temp.onGameWorldInitComplete();
				} catch (Exception e) {
					throw new Exception(e.getMessage() + " 技能状态ID=" + temp.status_id);
				}
			}
		}

		Map<Integer, SkillStatusData> getCache() {
			return elementMapReadOnly;
		}
	}

	static class SkillMinionDataManager {

		private final Map<Integer, KMinionTemplateData> _elementMap = new HashMap<Integer, KMinionTemplateData>();
		private final Map<Integer, KMinionTemplateData> _elementMapReadOnly = Collections.unmodifiableMap(_elementMap);

		private SkillMinionDataManager() {

		}

		void initData(List<KMinionTemplateData> list) {
			_elementMap.clear();

			KMinionTemplateData minion;
			KMinionTemplateData pre;
			for (int i = 0; i < list.size(); i++) {
				minion = list.get(i);
				pre = _elementMap.put(minion.templateId, minion);
				if (pre != null) {
					throw new RuntimeException("重复的召唤物模板，模板id是：" + minion.templateId);
				}
			}
		}

		KMinionTemplateData getMinion(int templateId) {
			return _elementMap.get(templateId);
		}

		Map<Integer, KMinionTemplateData> getCache() {
			return _elementMapReadOnly;
		}

		private void onGameWorldInitComplete() throws Exception {

			for (Iterator<KMinionTemplateData> itr = _elementMap.values().iterator(); itr.hasNext();) {
				itr.next().onGameWorldInitComplete();
			}
		}
	}

	/**
	 * <pre>
	 * 检查指定对象内的全部数组字段的长度是否等于len
	 * 若长度为0则生成len长度的数组
	 * 
	 * @param obj
	 * @param len
	 * @param excuteFields 忽略的字段
	 * @author CamusHuang
	 * @throws  
	 * @throws Exception 
	 * @creation 2014-2-19 上午10:29:20
	 * </pre>
	 */
	public static void checkArrayLenth(Object obj, int len, Set<String> excuteFields) throws Exception {
		Class clazz = obj.getClass();
		checkArrayLenth(obj, clazz, len, excuteFields);
		for (;;) {
			clazz = clazz.getSuperclass();
			if (clazz == null) {
				break;
			}
			checkArrayLenth(obj, clazz, len, excuteFields);
		}
	}

	private static void checkArrayLenth(Object obj, Class clazz, int len, Set<String> excuteFields) throws Exception {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (excuteFields.contains(field.getName())) {
				continue;
			}
			if (field.getType().isArray()) {
				if (field.getAnnotation(KImmunityOfArrayCheck.class) != null) {
					continue;
				}
				field.setAccessible(true);
				try {
					Object arrayObj = field.get(obj);
					if (arrayObj == null) {
						// 忽略未赋值的数组
						continue;
					}
					int nowLen = Array.getLength(arrayObj);
					if (nowLen == 0) {
						field.setAccessible(true);
						Object newArray = Array.newInstance(field.getType().getComponentType(), len);
						field.set(obj, newArray);
						field.setAccessible(false);
					} else {
						if (nowLen != len) {
							throw new Exception(field.getName() + ":非法的数组字段长度=" + nowLen);
						}
					}
				} finally {
					field.setAccessible(false);
				}
			}
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
			mRoleIniSkillTempManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KSkillDataLoader.SheetName_角色主动技能模板 + "]错误：" + e.getMessage(), e);
		}

		try {
			mRolePasSkillTempManager.onGameWorldInitComplete(true);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KSkillDataLoader.SheetName_角色被动技能模板 + "]错误：" + e.getMessage(), e);
		}

		try {
			mMountSkillTempManager.onGameWorldInitComplete();
			// 新版机甲要求技能必须有钻石升级消耗，表示SP点
			for (KRoleIniSkillTemp temp : mMountSkillTempManager.templateMap.values()) {
				for (SkillTempLevelData lvData : temp.skillLevelDatas) {
					if (lvData.learnLvMoneys.size()!=2) {
						throw new KGameServerException("新版机甲要求货币1为钻石，表示SP消耗");
					}
					if (lvData.learnLvMoneys.get(0).currencyType != KCurrencyTypeEnum.DIAMOND) {
						throw new KGameServerException("新版机甲要求货币1为钻石，表示SP消耗");
					}
				}
			}
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KSkillDataLoader.SheetName_机甲技能模板 + "]错误：" + e.getMessage(), e);
		}

		try {
			mPetSkillTempManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KSkillDataLoader.SheetName_宠物技能模板 + "]错误：" + e.getMessage(), e);
		}

		try {
			mPetPasSkillTempManager.onGameWorldInitComplete(false);
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KSkillDataLoader.SheetName_宠物被动技能模板 + "]错误：" + e.getMessage(), e);
		}

		try {
			mMonsterSkillTempManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KSkillDataLoader.SheetName_怪物技能模板 + "]错误：" + e.getMessage(), e);
		}

		try {
			mSkillStatusManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KSkillDataLoader.SheetName_技能状态数据 + "]错误：" + e.getMessage(), e);
		}

		try {
			mSkillMinionManager.onGameWorldInitComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KSkillDataLoader.SheetName_技能召唤物数据 + "]错误：" + e.getMessage(), e);
		}

		// 全体技能模板ID不能重复
		Set<Integer> tempIdSet = new HashSet<Integer>(mRoleIniSkillTempManager.templateMap.keySet());
		for (Integer tempId : mRolePasSkillTempManager.templateMap.keySet()) {
			if (tempIdSet.add(tempId)) {

			}
		}

	}
}
