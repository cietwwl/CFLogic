package com.kola.kmp.logic.mount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountEquiTemp;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountLv;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountLvMaxAtts;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountResetSPData;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountTemplate;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountUpBigLvData;
import com.kola.kmp.logic.mount.KMountDataStructs.KMountUpLvData;

public final class KMountDataManager {
	// 机甲模板数据
	public static MountTemplateManager mMountTemplateManager = new MountTemplateManager();
	// 机甲等级数据
	static MountLvDataManager mMountLvDataManager = new MountLvDataManager();
	// 机甲升级材料数据
	static MountLvMaxDataManager mMountLvMaxDataManager = new MountLvMaxDataManager();
	// 机甲升级材料数据
	static MountUpLvDataManager mMountUpLvDataManager = new MountUpLvDataManager();
	// 机甲进阶材料数据
	static MountUpBigLvDataManager mMountUpBigLvDataManager = new MountUpBigLvDataManager();
	// 机甲装备数据
	static MountEquiDataManager mMountEquiDataManager = new MountEquiDataManager();
	// 重置sp点花费数据
	static MountResetSPDataManager mMountResetSPDataManager = new MountResetSPDataManager();

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 机甲模板数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-5 下午7:45:26
	 * </pre>
	 */
	public static class MountTemplateManager {
		/**
		 * <pre>
		 * 全部数据
		 * KEY = 模版ID
		 * </pre>
		 */
		private final LinkedHashMap<Integer, KMountTemplate> dataMap = new LinkedHashMap<Integer, KMountTemplate>();
		/**
		 * <pre>
		 * 全部数据
		 * <机甲型号,<机甲阶级,机甲数据>>
		 * </pre>
		 */
		private final LinkedHashMap<Integer, LinkedHashMap<Integer, KMountTemplate>> dataMapByModel = new LinkedHashMap<Integer, LinkedHashMap<Integer, KMountTemplate>>();

		// 新手引导机甲
		private KMountTemplate mountTempForNewRole;
		
		private MountTemplateManager() {
		}

		/**
		 * <pre>
		 * 数据添加
		 * 
		 * @param tempDatas
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2015-1-5 下午7:45:34
		 * </pre>
		 */
		void initData(List<KMountTemplate> tempDatas) throws Exception {
			dataMap.clear();
			dataMapByModel.clear();

			for (KMountTemplate tempData : tempDatas) {
				KMountTemplate oldData = dataMap.put(tempData.mountsID, tempData);
				if (oldData != null) {
					throw new Exception("重复的模板ID=" + tempData.mountsID);
				}
				LinkedHashMap<Integer, KMountTemplate> tempMap = dataMapByModel.get(tempData.Model);
				if (tempMap == null) {
					tempMap = new LinkedHashMap<Integer, KMountDataStructs.KMountTemplate>();
					dataMapByModel.put(tempData.Model, tempMap);
				}

				oldData = tempMap.put(tempData.bigLv, tempData);
				if (oldData != null) {
					throw new Exception("重复的阶级=" + tempData.bigLv + " 模板ID=" + tempData.mountsID);
				}
				
				//
				tempData.initAtts();
			}
		}

		public KMountTemplate getTemplate(int templateId) {
			return dataMap.get(templateId);
		}

		KMountTemplate getTemplateByLv(int model, int bigLv) {
			LinkedHashMap<Integer, KMountTemplate> tempMap = dataMapByModel.get(model);
			if (tempMap == null) {
				return null;
			}
			return tempMap.get(bigLv);
		}

		public Map<Integer, KMountTemplate> getTemplateByModel(int model) {
			return dataMapByModel.get(model);
		}

		/**
		 * <pre>
		 * <机甲型号,<机甲阶级,机甲数据>>
		 * 
		 * @deprecated
		 * @return
		 * @author CamusHuang
		 * @creation 2015-1-5 下午7:54:24
		 * </pre>
		 */
		Map<Integer, LinkedHashMap<Integer, KMountTemplate>> getDataCache() {
			return dataMapByModel;
		}
		
		public KMountTemplate getMountForNewRole(){
			return mountTempForNewRole;
		}

		private void notifyCacheLoadComplete() throws Exception {
			for (KMountTemplate temp : dataMap.values()) {
				try{
					temp.notifyCacheLoadComplete();
				} catch(Exception e){
					throw new Exception(e.getMessage()+ ",mountsID=" + temp.mountsID, e);
				}
			}

			// 保证机甲阶级不断连
			for (Entry<Integer, LinkedHashMap<Integer, KMountTemplate>> e : dataMapByModel.entrySet()) {
				LinkedHashMap<Integer, KMountTemplate> map = e.getValue();
				for (int bigLv = 1; bigLv <= map.size(); bigLv++) {
					if (!map.containsKey(bigLv)) {
						throw new Exception("缺少阶级=" + bigLv + " 机甲型号=" + e.getKey());
					}
				}
				KMountTemplate temp = map.get(1);
				if(temp.isForNewRole){
					if(map.size()>1){
						throw new Exception("新手引导机甲只能有1阶 机甲型号=" + e.getKey());
					}
					if (mountTempForNewRole != null) {
						throw new Exception("新手引导机甲不能有多个 机甲型号=" + e.getKey());
					}
					mountTempForNewRole = temp;
				} else {
					if (map.size() != mMountUpBigLvDataManager.getMaxBigLv()) {
						throw new Exception("缺少阶级 机甲型号=" + e.getKey());
					}
				}
			}
			
			if(mountTempForNewRole ==  null){
				throw new Exception("缺少新手引导机甲");
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 机甲等级数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-5 下午7:57:10
	 * </pre>
	 */
	static class MountLvDataManager {
		/**
		 * <pre>
		 * 全部数据
		 * KEY = 属性百分比
		 * </pre>
		 */
		private final HashMap<Integer, KMountLv> dataMap = new HashMap<Integer, KMountLv>();
		private final List<KMountLv> dataList = new ArrayList<KMountLv>();

		private MountLvDataManager() {
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
		void initData(List<KMountLv> datas) throws Exception {
			dataMap.clear();
			dataList.clear();
			//
			dataList.addAll(datas);
			for (KMountLv data : datas) {
				KMountLv oldData = dataMap.put(data.lv, data);
				if (oldData != null) {
					throw new Exception("重复的等级=" + data.lv);
				}
			}

			{
				// 保证等级不断连，属性不下降
				KMountLv minLv = null;
				for (int lv = 1; lv <= dataMap.size(); lv++) {
					KMountLv temp = dataMap.get(lv);
					if (temp == null) {
						throw new Exception("缺少等级=" + lv);
					}

					if (minLv != null) {
						if (temp.exp < minLv.exp) {
							throw new Exception("exp不能下降， 等级=" + lv);
						}
						if (temp.attributeProportion < minLv.attributeProportion) {
							throw new Exception("attributeProportion不能下降， 等级=" + lv);
						}
						if (temp.spPoint < minLv.spPoint) {
							throw new Exception("spPoint不能下降， 等级=" + lv);
						}
					}

					minLv = temp;
				}
			}
		}

		KMountLv getData(int lv) {
			return dataMap.get(lv);
		}

		int getMaxLv() {
			return dataMap.size();
		}

		private void notifyCacheLoadComplete() throws Exception {
			for (KMountLv temp : dataMap.values()) {
				try{
					temp.notifyCacheLoadComplete();
				} catch(Exception e){
					throw new Exception(e.getMessage()+ ",lv=" + temp.lv, e);
				}
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 机甲升级属性上限管理器
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-5 下午7:57:10
	 * </pre>
	 */
	static class MountLvMaxDataManager {
		/**
		 * <pre>
		 * 全部数据
		 * KEY = 机甲型号
		 * </pre>
		 */
		private final HashMap<Integer, KMountLvMaxAtts> dataMap = new HashMap<Integer, KMountLvMaxAtts>();

		private MountLvMaxDataManager() {
		}

		void initData(List<KMountLvMaxAtts> datas) throws Exception {
			dataMap.clear();
			//
			for (KMountLvMaxAtts data : datas) {
				KMountLvMaxAtts oldData = dataMap.put(data.Model, data);
				if (oldData != null) {
					throw new Exception("重复的机甲型号=" + data.Model);
				}
				//
				data.initAtts();
			}
		}

		KMountLvMaxAtts getData(int model) {
			return dataMap.get(model);
		}
		
		/**
		 * <pre>
		 * 
		 * @deprecated
		 * @return
		 * @author CamusHuang
		 * @creation 2015-1-8 上午11:52:17
		 * </pre>
		 */
		Map<Integer, KMountLvMaxAtts> getDataCache(){
			return dataMap;
		}

		private void notifyCacheLoadComplete() throws Exception {
			// 保证所有机甲均有数据
			for (int model : mMountTemplateManager.dataMapByModel.keySet()) {
				KMountLvMaxAtts temp = dataMap.get(model);
				if (temp == null) {
					throw new Exception("缺少机甲型号=" + model);
				}
				try{
					temp.notifyCacheLoadComplete();
				} catch(Exception e){
					throw new Exception(e.getMessage()+ ",型号=" + temp.Model, e);
				}
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 机甲升级材料管理器
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-5 下午8:04:50
	 * </pre>
	 */
	static class MountUpLvDataManager {
		/**
		 * <pre>
		 * 全部数据
		 * KEY = 材料ItemCode
		 * </pre>
		 */
		private final LinkedHashMap<String, KMountUpLvData> dataMap = new LinkedHashMap<String, KMountUpLvData>();

		private MountUpLvDataManager() {
		}

		void initData(List<KMountUpLvData> datas) throws Exception {
			dataMap.clear();
			//
			{
				// 保证属性不下降
				KMountUpLvData minData = null;
				for (KMountUpLvData tempData : datas) {
					KMountUpLvData oldData = dataMap.put(tempData.itemTempId, tempData);
					if (oldData != null) {
						throw new Exception("重复的升级材料 =" + tempData.itemTempId);
					}
					if (minData != null) {
						if (tempData.addExp <= minData.addExp) {
							throw new Exception("addExp不能下降， 材料=" + tempData.itemTempId);
						}
					}
					minData = tempData;
				}
			}
		}

		KMountUpLvData getData(String itemTempId) {
			return dataMap.get(itemTempId);
		}
		
		/**
		 * <pre>
		 * 
		 * @deprecated
		 * @return
		 * @author CamusHuang
		 * @creation 2015-1-8 下午5:36:41
		 * </pre>
		 */
		Map<String, KMountUpLvData> getDataCache(){
			return dataMap;
		}

		private void notifyCacheLoadComplete() throws Exception {
			for (KMountUpLvData temp : dataMap.values()) {
				try{
					temp.notifyCacheLoadComplete();
				} catch(Exception e){
					throw new Exception(e.getMessage()+ ",材料=" + temp.itemTempId, e);
				}
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 机甲进阶材料管理器
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-5 下午8:04:50
	 * </pre>
	 */
	static class MountUpBigLvDataManager {
		/**
		 * <pre>
		 * 全部数据
		 * KEY = 阶级
		 * </pre>
		 */
		private final LinkedHashMap<Integer, KMountUpBigLvData> dataMap = new LinkedHashMap<Integer, KMountUpBigLvData>();

		private MountUpBigLvDataManager() {
		}

		void initData(List<KMountUpBigLvData> datas) throws Exception {
			dataMap.clear();
			//
			for (KMountUpBigLvData tempData : datas) {
				KMountUpBigLvData oldData = dataMap.put(tempData.bigLv, tempData);
				if (oldData != null) {
					throw new Exception("重复的阶级 =" + tempData.bigLv);
				}
			}

			{
				// 保证不断连，属性不下降
				KMountUpBigLvData minLv = null;
				for (int bigLv = 1; bigLv <= dataMap.size(); bigLv++) {
					KMountUpBigLvData temp = dataMap.get(bigLv);
					if (temp == null) {
						throw new Exception("缺少阶级=" + bigLv);
					}

					if (minLv != null) {
						if (temp.lv < minLv.lv) {
							throw new Exception("lv不能下降， 阶级=" + bigLv);
						}
					}

					minLv = temp;
				}
			}
		}

		KMountUpBigLvData getData(int bigLv) {
			return dataMap.get(bigLv);
		}

		int getMaxBigLv() {
			return dataMap.size();
		}

		private void notifyCacheLoadComplete() throws Exception {
			
			for (KMountUpBigLvData temp : dataMap.values()) {
				try{
					temp.notifyCacheLoadComplete();
				} catch(Exception e){
					throw new Exception(e.getMessage()+ ",阶级=" + temp.bigLv+" 等级="+ temp.lv, e);
				}
			}
			
			KMountUpBigLvData temp = dataMap.get(dataMap.size());
			if(temp.lv!=KMountDataManager.mMountLvDataManager.getMaxLv()){
				throw new Exception("最高阶要求等级="+temp.lv);
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 机甲装备数据管理器
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-5 下午8:04:50
	 * </pre>
	 */
	static class MountEquiDataManager {
		/**
		 * <pre>
		 * 全部数据
		 * KEY = 装备ID
		 * </pre>
		 */
		private final HashMap<Integer, KMountEquiTemp> dataMap = new HashMap<Integer, KMountEquiTemp>();

		private MountEquiDataManager() {
		}

		void initData(List<KMountEquiTemp> datas) throws Exception {
			dataMap.clear();
			//
			for (KMountEquiTemp tempData : datas) {
				KMountEquiTemp oldData = dataMap.put(tempData.equipID, tempData);
				if (oldData != null) {
					throw new Exception("重复的 equipID=" + tempData.equipID);
				}
				//
				tempData.initAtts();
			}
		}

		KMountEquiTemp getData(int equipID) {
			return dataMap.get(equipID);
		}

		private void notifyCacheLoadComplete() throws Exception {
			for (KMountEquiTemp temp : dataMap.values()) {
				try{
					temp.notifyCacheLoadComplete();
				} catch(Exception e){
					throw new Exception(e.getMessage()+ ",装备ID=" + temp.equipID, e);
				}
			}
		}
	}

	// ///////////////////////////////////////////////////
	/**
	 * <pre>
	 * 重置sp点花费数据
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-5 下午8:04:50
	 * </pre>
	 */
	static class MountResetSPDataManager {
		/**
		 * <pre>
		 * 全部数据
		 * KEY = 等级
		 * </pre>
		 */
		private final LinkedHashMap<Integer, KMountResetSPData> dataMap = new LinkedHashMap<Integer, KMountResetSPData>();

		private MountResetSPDataManager() {
		}

		void initData(List<KMountResetSPData> datas) throws Exception {
			dataMap.clear();
			//
			for (KMountResetSPData tempData : datas) {
				KMountResetSPData oldData = dataMap.put(tempData.lv, tempData);
				if (oldData != null) {
					throw new Exception("重复的lv =" + tempData.lv);
				}
			}
		}

		KMountResetSPData getData(int lv) {
			return dataMap.get(lv);
		}

		private void notifyCacheLoadComplete() throws Exception {
			for (KMountLv lvData : mMountLvDataManager.dataList) {
				KMountResetSPData temp = dataMap.get(lvData.lv);
				if (temp == null) {
					throw new Exception("缺少等级 lv=" + lvData.lv);
				}
				try{
					temp.notifyCacheLoadComplete();
				} catch(Exception e){
					throw new Exception(e.getMessage()+ ",lv=" + temp.lv, e);
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
	static void notifyCacheLoadComplete() throws KGameServerException {
		try {
			mMountTemplateManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KMountDataLoader.SheetName_机甲信息 + "]错误：" + e.getMessage(), e);
		}
		try {
			mMountLvDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KMountDataLoader.SheetName_机甲培养经验与属性比例 + "]错误：" + e.getMessage(), e);
		}
		try {
			mMountLvMaxDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KMountDataLoader.SheetName_机甲培养属性上限 + "]错误：" + e.getMessage(), e);
		}
		try {
			mMountUpLvDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KMountDataLoader.SheetName_机甲培养材料 + "]错误：" + e.getMessage(), e);
		}
		try {
			mMountUpBigLvDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KMountDataLoader.SheetName_机甲进阶条件 + "]错误：" + e.getMessage(), e);
		}
		try {
			mMountEquiDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KMountDataLoader.SheetName_机甲装备打造 + "]错误：" + e.getMessage(), e);
		}
		try {
			mMountResetSPDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + KMountDataLoader.SheetName_重置sp点花费 + "]错误：" + e.getMessage(), e);
		}
	}
}
