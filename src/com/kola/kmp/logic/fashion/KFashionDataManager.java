package com.kola.kmp.logic.fashion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.fashion.KFashionDataStructs.KFashionTemplate;

public class KFashionDataManager {
	/** 所有时装模板数据 */
	public final static FashionTemplateManager mFashionTemplateManager = new FashionTemplateManager();

	// /////////////////////////////
	/**
	 * <pre>
	 * 时装模板数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-7 下午6:26:36
	 * </pre>
	 */
	public static class FashionTemplateManager {
		/**
		 * <pre>
		 * 全部时装模板
		 * 主要用于查询模板数据，提高性能
		 * KEY = tempId
		 * </pre>
		 */
		private Map<Integer, KFashionTemplate> fashionTemplateMap = new HashMap<Integer, KFashionTemplate>();
		/**
		 * <pre>
		 * 全部时装模板
		 * 主要用于遍历，提高性能
		 * </pre>
		 */
		private List<KFashionTemplate> fashionTemplateList = new ArrayList<KFashionTemplate>();

		private FashionTemplateManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 如果存在tempId重复，则抛异常
		 * 
		 * @param fashionTemplate
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2012-11-12 下午1:27:53
		 * </pre>
		 */
		void initDatas(List<KFashionTemplate> datas) throws Exception {
			for (KFashionTemplate fashionTemplate : datas) {
				KFashionTemplate oldData = fashionTemplateMap.put(fashionTemplate.id, fashionTemplate);
				if (oldData != null) {
					throw new Exception("加载时装模板错误：重复的tempId=" + oldData.id);
				}
			}
			fashionTemplateList.addAll(datas);
			
			//
			fashionTemplateMap = Collections.unmodifiableMap(fashionTemplateMap);
			fashionTemplateList = Collections.unmodifiableList(fashionTemplateList);
		}

		public KFashionTemplate getFashionTemplate(int tempId) {
			return fashionTemplateMap.get(tempId);
		}

		boolean containTemplate(int tempId) {
			return fashionTemplateMap.containsKey(tempId);
		}

		public List<KFashionTemplate> getFashionTemplateList() {
			return fashionTemplateList;
		}
		
		void dataLoadFinishedNotify() throws Exception {
			for (KFashionTemplate temp : fashionTemplateList) {
				temp.dataLoadFinishedNotify();
			}
		}

		void notifyCacheLoadComplete() throws Exception {

			for (KFashionTemplate temp : fashionTemplateList) {
				temp.notifyCacheLoadComplete();
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
	static void dataLoadFinishedNotify() throws KGameServerException {
		try {
			mFashionTemplateManager.dataLoadFinishedNotify();
		} catch (Exception e) {
			throw new KGameServerException("加载道具模板错误：" + e.getMessage(), e);
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
	static void notifyCacheLoadComplete() throws KGameServerException {

		try {
			mFashionTemplateManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载时装模板错误：" + e.getMessage(), e);
		}
	}
}
