package com.kola.kmp.logic.reward.redress;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.item.KItemConfig;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.reward.redress.KRedressDataManagerV1.KEquiEnchanseRedressDataManager.EquiEnchanseRedressData;
import com.kola.kmp.logic.reward.redress.KRedressDataManagerV1.KFashionRedressDataManager.FashionRedressData;
import com.kola.kmp.logic.reward.redress.KRedressDataManagerV1.KSkillRedressDataManager.SkillRedressData;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRoleIniSkillTemp;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.RewardTips;

/**
 * <pre>
 * 第一次改版补偿
 * 
 * @author CamusHuang
 * @creation 2013-4-8 下午2:39:56
 * </pre>
 */
public class KRedressDataManagerV1 {
	
	// 补偿的角色创建时间截止日期
	public static long RoleCreateEndTime;
	
	// 是否重置为未首充
	public static boolean isResetFirstCharge = false;

	static void loadConfig(Element e) throws KGameServerException{
		
		try {
			RoleCreateEndTime = UtilTool.DATE_FORMAT.parse(e.getChildTextTrim("RoleCreateEndTime")).getTime();
		} catch (ParseException e1) {
			throw new KGameServerException(e1.getMessage(), e1);
		}
		
		isResetFirstCharge = Integer.parseInt(e.getChildTextTrim("isResetFirstCharge"))==1;
	}

	//
	static final String SheetName_时装回收 = "时装回收";
	static final String SheetName_装备孔位补偿 = "装备孔位补偿";
	static final String SheetName_主动技能重置 = "主动技能重置";
	static final String SheetName_被动技能重置 = "被动技能重置";
	static final String SheetName_全局补偿 = "全局补偿";

	/**
	 * <pre>
	 * 开始加载文件
	 * @param configPath
	 * @author CamusHuang
	 * @creation 2012-11-9 下午7:38:27
	 * </pre>
	 */
	static void goToLoadData(String ExcelPath, int HeaderIndex) throws Exception {

		KGameExcelFile file = new KGameExcelFile(ExcelPath);
		{
			KGameExcelTable table = file.getTable(SheetName_装备孔位补偿, HeaderIndex);

			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length < 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
			}

			try {
				List<EquiEnchanseRedressData> datas = new ArrayList<EquiEnchanseRedressData>();
				//
				List<String> headerNames = table.getHeaderNames();
				Class<EquiEnchanseRedressData> clazz = EquiEnchanseRedressData.class;
				for (KGameExcelRow row : rows) {
					EquiEnchanseRedressData obj = clazz.newInstance();
					ReflectPaser.parseExcelData(obj, headerNames, row, false, false);
					obj.mailReward = BaseMailRewardData.loadData(row, false);
					datas.add(obj);
				}
				mEquiEnchanseRedressDataManager.init(datas);
			} catch (Exception e) {
				throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
			}
		}

		{
			KGameExcelTable table = file.getTable(SheetName_时装回收, HeaderIndex);

			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length < 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
			}

			try {
				List<FashionRedressData> datas = new ArrayList<FashionRedressData>();
				//
				List<String> headerNames = table.getHeaderNames();
				Class<FashionRedressData> clazz = FashionRedressData.class;
				for (KGameExcelRow row : rows) {
					FashionRedressData obj = clazz.newInstance();
					ReflectPaser.parseExcelData(obj, headerNames, row, false, false);
					obj.mailReward = BaseMailRewardData.loadData(row, false);
					datas.add(obj);
				}
				mFashionRedressDataManager.init(datas);
			} catch (Exception e) {
				throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
			}
		}

		{
			KGameExcelTable table = file.getTable(SheetName_主动技能重置, HeaderIndex);

			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length < 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
			}

			try {
				List<SkillRedressData> datas = new ArrayList<SkillRedressData>();
				//
				List<String> headerNames = table.getHeaderNames();
				Class<SkillRedressData> clazz = SkillRedressData.class;
				for (KGameExcelRow row : rows) {
					SkillRedressData obj = clazz.newInstance();
					ReflectPaser.parseExcelData(obj, headerNames, row, false, false);
					obj.baseReward = BaseRewardData.loadData(row, false);
					datas.add(obj);
				}
				mInitSkillRedressDataManager.init(datas);
			} catch (Exception e) {
				throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
			}
		}

		{
			KGameExcelTable table = file.getTable(SheetName_被动技能重置, HeaderIndex);

			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length < 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
			}

			try {
				List<SkillRedressData> datas = new ArrayList<SkillRedressData>();
				//
				List<String> headerNames = table.getHeaderNames();
				Class<SkillRedressData> clazz = SkillRedressData.class;
				for (KGameExcelRow row : rows) {
					SkillRedressData obj = clazz.newInstance();
					ReflectPaser.parseExcelData(obj, headerNames, row, false, false);
					obj.baseReward = BaseRewardData.loadData(row, false);
					datas.add(obj);
				}
				mPasSkillRedressDataManager.init(datas);
			} catch (Exception e) {
				throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
			}
		}

		{
			KGameExcelTable table = file.getTable(SheetName_全局补偿, HeaderIndex);

			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length != 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数必须为1！");
			}

			try {
				redressForAllRole = BaseMailRewardData.loadData(rows[rows.length - 1], false);
			} catch (Exception e) {
				throw new Exception("加载[" + table.getTableName() + "]错误：" + e.getMessage(), e);
			}
		}
	}

	// /////////////////////////////////////////

	/**
	 * <pre>
	 * 时装补偿数据
	 * </pre>
	 */
	public static KFashionRedressDataManager mFashionRedressDataManager = new KFashionRedressDataManager();

	/**
	 * <pre>
	 * 装备孔位补偿数据
	 * </pre>
	 */
	public static KEquiEnchanseRedressDataManager mEquiEnchanseRedressDataManager = new KEquiEnchanseRedressDataManager();

	/**
	 * <pre>
	 * 主动技能补偿数据
	 * </pre>
	 */
	public static KSkillRedressDataManager mInitSkillRedressDataManager = new KSkillRedressDataManager(true);

	/**
	 * <pre>
	 * 被动技能补偿数据
	 * </pre>
	 */
	public static KSkillRedressDataManager mPasSkillRedressDataManager = new KSkillRedressDataManager(false);

	/**
	 * 全局补偿
	 */
	public static BaseMailRewardData redressForAllRole;

	// ///////////////////////////////////////////////////
	public static class KFashionRedressDataManager {
		/**
		 * <pre>
		 * 各等级数据
		 * KEY=时装模板ID
		 * </pre>
		 */
		private HashMap<Integer, FashionRedressData> dataMap = new HashMap<Integer, FashionRedressData>();

		void init(List<FashionRedressData> datas) throws Exception {
			for (FashionRedressData data : datas) {
				if (dataMap.put(data.id, data) != null) {
					throw new Exception("数据重复 id=" + data.id);
				}
			}
		}

		public FashionRedressData getData(int id) {
			return dataMap.get(id);
		}

		Map<Integer, FashionRedressData> getDataCache() {
			return dataMap;
		}

		/**
		 * <pre>
		 * 验证数据/跨模块操作
		 * 
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-4-25 下午4:26:38
		 * </pre>
		 */
		void notifyCacheLoadComplete() throws Exception {

			for (Entry<Integer, FashionRedressData> e : dataMap.entrySet()) {
				if (KSupportFactory.getFashionModuleSupport().getFashionTemplate(e.getKey()) == null) {
					throw new Exception("不存在指定时装 id=" + e.getKey());
				}
				e.getValue().notifyCacheLoadComplete();
			}
		}

		// //////////////////////////
		public static class FashionRedressData {
			// ----------以下是EXCEL表格直导数据---------
			private int id;// 时装id号
			public BaseMailRewardData mailReward;

			// ----------以下是逻辑数据---------

			void notifyCacheLoadComplete() throws Exception {

				mailReward.notifyCacheLoadComplete();
				
			}

		}
	}

	// ///////////////////////////////////////////////////
	public static class KEquiEnchanseRedressDataManager {
		/**
		 * <pre>
		 * 各等级数据
		 * KEY=失去的孔位数量
		 * </pre>
		 */
		private HashMap<Integer, EquiEnchanseRedressData> dataMap = new HashMap<Integer, EquiEnchanseRedressData>();

		void init(List<EquiEnchanseRedressData> datas) throws Exception {
			for (EquiEnchanseRedressData data : datas) {
				if (dataMap.put(data.holes, data) != null) {
					throw new Exception("数据重复 holes=" + data.holes);
				}
			}
		}

		public EquiEnchanseRedressData getData(int loseHoles) {
			return dataMap.get(loseHoles);
		}

		/**
		 * <pre>
		 * 验证数据/跨模块操作
		 * 
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-4-25 下午4:26:38
		 * </pre>
		 */
		void notifyCacheLoadComplete() throws Exception {
			for (int holes = 1; holes <= KItemConfig.getInstance().TotalMaxEnchansePosition; holes++) {
				EquiEnchanseRedressData data = dataMap.get(holes);
				if (data == null) {
					throw new Exception("缺少孔位 = " + holes);
				}
				data.notifyCacheLoadComplete();
			}
		}

		// //////////////////////////
		public static class EquiEnchanseRedressData {
			// ----------以下是EXCEL表格直导数据---------
			private int holes;// 减少的孔数量
			public BaseMailRewardData mailReward;

			// ----------以下是逻辑数据---------

			void notifyCacheLoadComplete() throws Exception {

				if (holes < 1 || holes > KItemConfig.getInstance().TotalMaxEnchansePosition) {
					throw new Exception("错误的孔数 = " + holes);
				}

				mailReward.notifyCacheLoadComplete();
				
			}

		}
	}

	// ///////////////////////////////////////////////////
	public static class KSkillRedressDataManager {

		private boolean isInitSkill;
		/**
		 * <pre>
		 * <技能模板ID,<技能等级,补偿奖励>>
		 * </pre>
		 */
		private Map<Integer, Map<Integer, SkillRedressData>> dataMap = new HashMap<Integer, Map<Integer, SkillRedressData>>();

		BaseMailContent baseMailContent;
		
		private KSkillRedressDataManager(boolean isInitSkill) {
			this.isInitSkill = isInitSkill;
			if(isInitSkill){
				baseMailContent = new BaseMailContent(RewardTips.主动技能重置补偿邮件标题, RewardTips.主动技能重置补偿邮件内容, null, null);
			} else {
				baseMailContent = new BaseMailContent(RewardTips.被动技能重置补偿邮件标题, RewardTips.被动技能重置补偿邮件内容, null, null);
			}
		}

		void init(List<SkillRedressData> datas) throws Exception {
			for (SkillRedressData data : datas) {

				Map<Integer, SkillRedressData> temp = dataMap.get(data.id);
				if (temp == null) {
					temp = new HashMap<Integer, SkillRedressData>();
					dataMap.put(data.id, temp);
				}

				if (temp.put(data.lv, data) != null) {
					throw new Exception("数据重复 id=" + data.id + " lv=" + data.lv);
				}
			}
		}

		public SkillRedressData getData(int id, int lv) {
			Map<Integer, SkillRedressData> temp = dataMap.get(id);

			if (temp == null) {
				return null;
			}

			return temp.get(lv);
		}

		/**
		 * <pre>
		 * 验证数据/跨模块操作
		 * 
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-4-25 下午4:26:38
		 * </pre>
		 */
		void notifyCacheLoadComplete() throws Exception {

			if (isInitSkill) {
				for (Entry<Integer, Map<Integer, SkillRedressData>> e : dataMap.entrySet()) {
					KRoleIniSkillTemp initSkill = KSupportFactory.getSkillModuleSupport().getRoleIniSkillTemplate(e.getKey());
					if (initSkill == null) {
						throw new Exception("不存在指定主动技能 id=" + e.getKey());
					}

					for (Entry<Integer, SkillRedressData> ee : e.getValue().entrySet()) {
						if (initSkill.getLevelData(ee.getKey()) == null) {
							throw new Exception("主动技能不存在指定等级 id=" + e.getKey() + " lv=" + ee.getKey());
						}
						ee.getValue().notifyCacheLoadComplete();
					}
				}
			} else {
				for (Entry<Integer, Map<Integer, SkillRedressData>> e : dataMap.entrySet()) {
					KRolePasSkillTemp initSkill = KSupportFactory.getSkillModuleSupport().getRolePasSkillTemplate(e.getKey());
					if (initSkill == null) {
						throw new Exception("不存在指定被动技能 id=" + e.getKey());
					}

					for (Entry<Integer, SkillRedressData> ee : e.getValue().entrySet()) {
//						if (initSkill.getLevelData(ee.getKey()) == null) {
//							throw new Exception("被动技能不存在指定等级 id=" + e.getKey() + " lv=" + ee.getKey());
//						}
						ee.getValue().notifyCacheLoadComplete();
					}
				}
			}
		}

		Map<Integer, Map<Integer, SkillRedressData>> getDataCache() {
			return dataMap;
		}

		// //////////////////////////
		public static class SkillRedressData {
			// ----------以下是EXCEL表格直导数据---------
			private int id;// 技能ID
			private int lv;// 等级
			public boolean IsDelete;//未到达等级是否删除

			public BaseRewardData baseReward;

			// ----------以下是逻辑数据---------

			void notifyCacheLoadComplete() throws Exception {

				baseReward.notifyCacheLoadComplete();
				
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
			mFashionRedressDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_时装回收 + "]错误：" + e.getMessage(), e);
		}

		try {
			mEquiEnchanseRedressDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_装备孔位补偿 + "]错误：" + e.getMessage(), e);
		}

		try {
			mInitSkillRedressDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_主动技能重置 + "]错误：" + e.getMessage(), e);
		}

		try {
			mPasSkillRedressDataManager.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_被动技能重置 + "]错误：" + e.getMessage(), e);
		}

		try {
			redressForAllRole.notifyCacheLoadComplete();
		} catch (Exception e) {
			throw new KGameServerException("加载[" + SheetName_全局补偿 + "]错误：" + e.getMessage(), e);
		}
	}
}
