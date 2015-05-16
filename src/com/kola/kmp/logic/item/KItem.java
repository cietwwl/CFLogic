package com.kola.kmp.logic.item;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.timer.Timer;

import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.CustomizeAttribute;
import com.kola.kgame.cache.DataStatus;
import com.kola.kgame.cache.item.impl.KAItem;
import com.kola.kgame.db.dataobject.DBItem;
import com.kola.kmp.logic.item.KItem.KItemCA.KItem_EquipmentData;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempEqui;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempStone;
import com.kola.kmp.logic.item.listener.KItemListenerManager;
import com.kola.kmp.logic.other.KEquipmentTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KItemPackTypeEnum;
import com.kola.kmp.logic.other.KItemTypeEnum;
import com.kola.kmp.logic.support.ItemModuleSupport.ISecondWeapon;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-11-5 上午10:10:24
 * </pre>
 */
public class KItem extends KAItem {

	private KItemCA ca;

	// /////////////////////////////////
	/**
	 * <pre>
	 * 逻辑新建对象时使用
	 * ID由缓存赋值
	 * 
	 * @param template
	 * @param itemPackType
	 * @param count
	 * @author CamusHuang
	 * @creation 2013-7-9 下午12:51:04
	 * </pre>
	 */
	KItem(KItemTempAbs template, long count) {
		super(template.itemCode, count);

		// 必须在itemPackType初始化之后才能NEW CA
		ca = new KItemCA(this, true);
	}

	/**
	 * <pre>
	 * 为缓存生成对象时使用
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-14 下午8:14:02
	 * </pre>
	 */
	KItem(DBItem dbdata) {
		super(dbdata.getId(), dbdata.getRoleId(), dbdata.getItemCode(), dbdata.getCount(), dbdata.getUUID(), dbdata.getCreateTimeMillis(), dbdata.getItemPackType());
		// 以防父类【在子类的全局属性还没有执行初始化时】回调子类decodeCA方法导致NullPointerException
		ca = new KItemCA(this, false);
		ca.decodeAttribute(dbdata.getCustomizeAttribute());
	}

	@Override
	protected String encodeCA() {
		return ca.encodeAttribute();
	}

	public KItemTempAbs getItemTemplate() {
		return KItemDataManager.mItemTemplateManager.getItemTemplate(_itemCode);
	}

	public KItemPackTypeEnum getItemPackTypeEnum() {
		return KItemPackTypeEnum.getEnum(super.getItemPackType());
	}

	protected int getMaxStack() {
		// 添加叠加上限控制
		KItemTempAbs temp = getItemTemplate();
		if (temp == null) {
			return Integer.MAX_VALUE;
		}
		return temp.stack;
	}
	
	public KItemCA getCA(){
		return ca;
	}

	public long changeCount(long changeValue) {
		long result = super.changeCount(changeValue);

		// 通知监听器
		KItemListenerManager.notifyBagItemChangeCount(_roleId, getItemTemplate(), result, changeValue > 0);
		return result;
	}

	public KItem_EquipmentData getEquipmentData() {
		return ca.mItemData_Equiment;
	}

	/**
	 * <pre>
	 * 
	 * @author CamusHuang
	 * @creation 2012-11-5 上午10:10:24
	 * </pre>
	 */
	public static class KItemCA implements CustomizeAttribute {

		private final KItem owner;
		
		private String activiteCode;//来源于哪个激活码

		// ////////////////////////////////////
		/** 不同道具类型的专用数据 */
		private KItem_EquipmentData mItemData_Equiment;

		// /////////////////////////////////

		private static final String JSON_NULL = "NULL";// null
		private static final String JSON_VER = "A";// 版本
		//
		private static final String JSON_BASEINFO = "B";// 基础信息
		private static final String JSON_BASEINFO_ACTIVITECODE = "1";// 来源于哪个激活码
		//
		private static final String JSON_EQUIMENT = "C";// 装备类型
		private static final String JSON_EQUIMENT_BASEATT = "1";// 基础信息
		private static final String JSON_EQUIMENT_BASEATT_STRONGLV = "1";// 强化等级
		private static final String JSON_EQUIMENT_BASEATT_STARLV = "2";// 星级
		private static final String JSON_EQUIMENT_BASEATT_LV = "4";// 基础属性评级

		private static final String JSON_EQUIMENT_ENCHASE_DATAS = "2";// 镶嵌
//		private static final String JSON_EQUIMENT_ENCHASE_DATAS_V1 = "4";// 镶嵌
//		private static final String JSON_EQUIMENT_ENCHASE_POSITION = "3";// 镶嵌孔位数量
		private static final String JSON_EQUIMENT_ENCHASE_BUYPOSITION = "5";// 购买的镶嵌孔位数量

//		private static final String JSON_EQUIMENT_APPENDATT = "3";// 附加属性

		// //

		// /////////////////////////////////
		/**
		 * <pre>
		 * 
		 * 
		 * @param owner
		 * @author CamusHuang
		 * @creation 2014-1-21 下午4:31:38
		 * </pre>
		 */
		KItemCA(KItem owner, boolean isFirstNew) {
			this.owner = owner;

			KItemTempAbs temp = owner.getItemTemplate();
			if (temp.ItemType == KItemTypeEnum.装备) {
				mItemData_Equiment = new KItem_EquipmentData(this, (KItemTempEqui) temp, isFirstNew);
			}
		}

		public void decodeAttribute(String attribute) {
			try {
				JSONObject json = new JSONObject(attribute);
				// 由底层调用,解释出逻辑层数据
				int ver = json.getInt(JSON_VER);// 默认版本
				// CEND 道具--暂时只有版本0
				switch (ver) {
				case 0:
					decodeBaseInfo(json.getJSONObject(JSON_BASEINFO));
					// 装备独有信息
					if (mItemData_Equiment != null) {
						mItemData_Equiment.decodeInfo(json.getJSONObject(JSON_EQUIMENT));
					}

					break;
				}
				// CEND 道具--道具相关--其它类型的道具独有信息解释
			} catch (Exception ex) {
				_LOGGER.error("decode数据时发生错误 roleId=" + owner.getRoleId() + " itemId=" + owner.getId() + " itemCode=" + owner._itemCode + " ----丢失数据！", ex);
			}
		}

		/**
		 * <pre>
		 * 
		 * @param obj
		 * @throws JSONException
		 * @author CamusHuang
		 * @creation 2013-1-18 上午10:25:06
		 * </pre>
		 */
		private void decodeBaseInfo(JSONObject obj) throws JSONException {
			activiteCode = obj.optString(JSON_BASEINFO_ACTIVITECODE);
		}

		/**
		 * 
		 * @return
		 */
		public String encodeAttribute() {

			JSONObject obj = new JSONObject();
			try {
				obj.put(JSON_VER, 0);// 默认版本
				// CEND 道具--暂时只有版本0
				obj.put(JSON_BASEINFO, encodeBaseInfo());// 基础信息
				// 装备独有信息
				if (mItemData_Equiment != null) {
					obj.put(JSON_EQUIMENT, mItemData_Equiment.encodeInfo());
				}
				// CEND 道具--其它类型的道具独有信息打包
			} catch (Exception ex) {
				_LOGGER.error("encode数据时发生错误 roleId=" + owner.getRoleId() + " itemId=" + owner.getId() + " itemCode=" + owner._itemCode + " ----丢失数据！", ex);
			}
			return obj.toString();
		}

		private JSONObject encodeBaseInfo() throws JSONException {
			JSONObject json = new JSONObject();
			if(activiteCode!=null){
				json.put(JSON_BASEINFO_ACTIVITECODE, activiteCode);
			}
			return json;
		}

		/**
		 * @deprecated 空实现
		 */
		public DataStatus getDataStatus() {
			return null;
		}
		
		public String getActiviteCode(){
			return activiteCode;
		}
		
		public void setActiviteCode(String activiteCode){
			this.activiteCode = activiteCode;
			owner.notifyDB();
		}

		/**
		 * <pre>
		 * 用于装备类道具的专用数据结构
		 * 
		 * @author CamusHuang
		 * @creation 2012-11-6 下午3:09:30
		 * </pre>
		 */
		public static class KItem_EquipmentData {
			// 母体
			private final KItemCA owner;
			// * 随机数n=rand（0，基础属性最大划分段数）
			// * n为随机在0到基础属性最大划分段数的整数中去一个值
			// * n在装备出生时随机设定，终身不变
			// 基础属性评级，保存到DB
			private int baseAttsLevel = 0;
			// 基础属性，初始时根据评级生成，不保存
			private Map<KGameAttrType, Integer> baseAtts = new HashMap<KGameAttrType, Integer>();
			//
			// 副武器专用参数
			public SecondWeapon mSecondWeapon;
			//
			private int strongLv;// 强化等级

			// 付费开启的镶嵌孔数
			private int buyEnchansePositionNum;
			// <宝石类型,镶嵌物品>
			private Map<Integer, String> enchanse = new HashMap<Integer, String>();// 镶嵌内容
			//
			private int starLv;

			private KItem_EquipmentData(KItemCA owner, KItemTempEqui temp, boolean isFirstNew) {
				this.owner = owner;
//				this.openEnchansePositionNum = temp.holenum;
				if (temp.typeEnum == KEquipmentTypeEnum.副武器) {
					mSecondWeapon = new SecondWeapon(this);
				}

				if (isFirstNew) {
					baseAttsLevel = temp.randomN();
					baseAtts.putAll(temp.getBaseAttForLv(baseAttsLevel));
				}
			}

			private JSONObject encodeInfo() throws JSONException {
				JSONObject obj = new JSONObject();
				{
					JSONObject tempObj = new JSONObject();
					obj.put(JSON_EQUIMENT_BASEATT, tempObj);
					tempObj.put(JSON_EQUIMENT_BASEATT_STRONGLV, strongLv);
					tempObj.put(JSON_EQUIMENT_BASEATT_STARLV, starLv);
					tempObj.put(JSON_EQUIMENT_BASEATT_LV, baseAttsLevel);
				}
				{
					obj.put(JSON_EQUIMENT_ENCHASE_BUYPOSITION, buyEnchansePositionNum);
					//
					JSONObject tempObj = new JSONObject();
					obj.put(JSON_EQUIMENT_ENCHASE_DATAS, tempObj);
					for (Entry<Integer, String> entry : enchanse.entrySet()) {
						tempObj.put(entry.getKey() + "", entry.getValue());
					}
				}
				return obj;
			}

			private void decodeInfo(JSONObject obj) throws JSONException {
				KItemTempEqui temp = (KItemTempEqui) owner.owner.getItemTemplate();

				{
					JSONObject tempObj = obj.getJSONObject(JSON_EQUIMENT_BASEATT);
					strongLv = tempObj.getInt(JSON_EQUIMENT_BASEATT_STRONGLV);
					starLv = tempObj.optInt(JSON_EQUIMENT_BASEATT_STARLV);
					{
						
						baseAttsLevel = tempObj.optInt(JSON_EQUIMENT_BASEATT_LV, Integer.MAX_VALUE);
						if (baseAttsLevel == Integer.MAX_VALUE) {
							baseAttsLevel = temp.getMaxAattributeSection();
						}
						baseAtts.putAll(temp.getBaseAttForLv(baseAttsLevel));
					}
				}
				{
					buyEnchansePositionNum = obj.optInt(JSON_EQUIMENT_ENCHASE_BUYPOSITION);
					JSONObject tempObj = obj.getJSONObject(JSON_EQUIMENT_ENCHASE_DATAS);
					for (Iterator<String> it = tempObj.keys(); it.hasNext();) {
						String index = it.next();
						String itemCode = tempObj.getString(index);
						KItemTempAbs itemTemp = KItemDataManager.mItemTemplateManager.getItemTemplate(itemCode);
						if(itemTemp==null || itemTemp.ItemType!=KItemTypeEnum.宝石){
							continue;
						}
						KItemTempStone stoneTemp = (KItemTempStone)itemTemp;
						enchanse.put(stoneTemp.stoneType, itemCode);
					}
				}
			}
			
			/**
			 * <pre>
			 * 装备评级
			 * 
			 * @return
			 * @author CamusHuang
			 * @creation 2014-10-30 下午4:50:28
			 * </pre>
			 */
			int getLv(){
				return baseAttsLevel;
			}

			int getStrongLv() {
				return strongLv;
			}

			void setStrongLv(int nextStrongLv) {
				this.strongLv = nextStrongLv;
				notifyDB();
			}

			/**
			 * <pre>
			 * 
			 * @deprecated 获取内部缓存，请谨慎使用
			 * @return <孔位,镶嵌物品>
			 * @author CamusHuang
			 * @creation 2014-3-18 上午9:43:22
			 * </pre>
			 */
			Map<Integer, String> getEnchanseCache() {
				return enchanse;
			}

			boolean containEnchanseStone(String stoneItemCode) {
				return enchanse.containsValue(stoneItemCode);
			}

			String getEnchanseStone(int stoneType) {
				return enchanse.get(stoneType);
			}

			/**
			 * <pre>
			 * 取下镶嵌宝石
			 * 
			 * @param stoneItemCode
			 * @return
			 * @author CamusHuang
			 * @creation 2014-3-19 下午6:28:36
			 * </pre>
			 */
			int removeEnchanseStone(String stoneItemCode) {
				for (Iterator<Entry<Integer, String>> it = enchanse.entrySet().iterator(); it.hasNext();) {
					Entry<Integer, String> entry = it.next();
					if (entry.getValue().equals(stoneItemCode)) {
						it.remove();
						notifyDB();
						return entry.getKey();
					}
				}
				return -1;
			}

			void setEnchanseStone(int stoneType, String itemCode) {
				enchanse.put(stoneType, itemCode);
				notifyDB();
			}
			
			void setEnchanseStone(KItemTempStone stoneTemp) {
				enchanse.put(stoneTemp.stoneType, stoneTemp.itemCode);
				notifyDB();
			}

			void notifyDB() {
				owner.owner.notifyDB();
			}

			int getStarLv() {
				return starLv;
			}

			void setStarLv(int nextStar) {
				starLv = nextStar;
				notifyDB();
			}

			int getBuyEnchansePosition() {
				return buyEnchansePositionNum;
			}

			void setBuyEnchansePosition(int newNum) {
				buyEnchansePositionNum = newNum;
				notifyDB();
			}
			
			public Map<KGameAttrType, Integer> getBaseAtts() {
				return baseAtts;
			}

			/**
			 * <pre>
			 * 计算装备当前所有属性并返回
			 * 单件装备属性（不含套装） = 装备基础属性+强化属性+(装备基础属性*升星万分比)+镶嵌宝石属性
			 * 
			 * @param totalResult 累加单件装备属性（不含套装）
			 * @return 累加单件装备属性（不含套装）
			 * @author CamusHuang
			 * @creation 2014-3-31 下午3:00:06
			 * </pre>
			 */
			Map<KGameAttrType, AtomicInteger> getAllEffect(Map<KGameAttrType, AtomicInteger> totalResult) {
				if (totalResult == null) {
					totalResult = new HashMap<KGameAttrType, AtomicInteger>();
				}
				//
				{// 计算基础属性、强化、升星

					for (Entry<KGameAttrType, Integer> entry : baseAtts.entrySet()) {
						int 装备基础属性 = entry.getValue();
						//
						{
							//
							int 强化属性 = KItemLogic.ExpressionForStrongAtt(strongLv, entry.getKey(), entry.getValue());
							int 升星属性 = KItemLogic.ExpressionForStarAtt(starLv, entry.getKey(), entry.getValue());
							int 总结果 = 装备基础属性 + 强化属性 + 升星属性;
							AtomicInteger oldValue = totalResult.get(entry.getKey());
							if (oldValue == null) {
								oldValue = new AtomicInteger();
								totalResult.put(entry.getKey(), oldValue);
							}
							oldValue.addAndGet(总结果);
						}
					}
				}
				{// 叠加镶嵌属性
					for (String itemCode : enchanse.values()) {
						KItemTempStone stoneTemp = (KItemTempStone) KItemDataManager.mItemTemplateManager.getItemTemplate(itemCode);
						for (Entry<KGameAttrType, Integer> entry : stoneTemp.allEffects.entrySet()) {
							AtomicInteger oldValue = totalResult.get(entry.getKey());
							if (oldValue == null) {
								oldValue = new AtomicInteger();
								totalResult.put(entry.getKey(), oldValue);
							}
							oldValue.addAndGet(entry.getValue());
						}
					}
				}
				return totalResult;
			}
			
			/**
			 * <pre>
			 * 计算装备当前所有属性并返回
			 * 单件装备属性（不含升星套装） = 装备基础属性+强化属性+(装备基础属性*升星万分比)+镶嵌宝石属性
			 * 
			 * @param baseResult 累加单件装备基础属性
			 * @param totalResult 累加单件装备属性（不含升星套装）
			 * @return [累加单件装备基础属性,累加单件装备属性]
			 * @author CamusHuang
			 * @creation 2014-3-31 下午3:00:06
			 * </pre>
			 */
			Map<KGameAttrType, AtomicInteger> getBaseEffect(Map<KGameAttrType, AtomicInteger> baseResult) {
				if (baseResult == null) {
					baseResult = new HashMap<KGameAttrType, AtomicInteger>();
				}
				//
//				KItemTempEqui temp = (KItemTempEqui) owner.owner.getItemTemplate();
				{// 计算基础属性、强化、升星

					for (Entry<KGameAttrType, Integer> entry : baseAtts.entrySet()) {
						int 装备基础属性 = entry.getValue();
						//
						{
							AtomicInteger oldValue = baseResult.get(entry.getKey());
							if (oldValue == null) {
								oldValue = new AtomicInteger();
								baseResult.put(entry.getKey(), oldValue);
							}
							oldValue.addAndGet(装备基础属性);
						}
					}
				}
				return baseResult;
			}			

			/**
			 * <pre>
			 * 副武器
			 * 
			 * @author CamusHuang
			 * @creation 2014-7-10 下午12:07:21
			 * </pre>
			 */
			public static class SecondWeapon implements ISecondWeapon {
				private KItem_EquipmentData equimentData;
				private byte type;
				private int Block;// 格挡值
				//
				// private long cohesionInMills;// 副武器聚力时间(毫秒，非副武器为0)
				private int CohesionPct; // 聚力伤害加成（万分比）
				private int CohesionFixedDm;// 聚力追加伤害
				//
				private long overheatedInMills;// 副武器过热时间(毫秒，非副武器为0)
				private int ClipVolume;// 弹夹容量
				private int PCT;// 单发子弹伤害

				/**
				 * <pre>
				 * 本构造方法仅用于模板测试
				 * 
				 * @param equi
				 * @author CamusHuang
				 * @creation 2014-10-27 下午3:59:03
				 * </pre>
				 */
				SecondWeapon(KItemTempEqui equi) {
					Map<KGameAttrType, Integer> baseEffects = equi.maxBaseAttMap;

					// 全部职业改用机枪作为副武器 by camus @201411241135
					switch (equi.jobEnum) {
					case WARRIOR: {
//						Integer value = baseEffects.get(KGameAttrType.BLOCK);
//						if (value == null) {
//							throw new RuntimeException("此副武器基础属性必须包含【格挡】类型 装备ID=" + equi.id);
//						}
//						break;
					}
					case SHADOW: {
//						if (equi.cohesionPct <= 0) {
//							throw new RuntimeException("副武器cohesion参数错误 装备ID=" + equi.id);
//						}
//						Integer value = baseEffects.get(KGameAttrType.COHESION_FIXED);
//						if (value == null) {
//							throw new RuntimeException("此副武器基础属性必须包含【聚力伤害】类型 装备ID=" + equi.id);
//						}
//						break;
					}
					case GUNMAN: {
						if (equi.overheated <= 0) {
							throw new RuntimeException("副武器overheated参数错误 装备ID=" + equi.id);
						}
						if (equi.volume <= 0) {
							throw new RuntimeException("副武器volume参数错误 装备ID=" + equi.id);
						}
						Integer value = baseEffects.get(KGameAttrType.SHOT_DM_PCT);
						if (value == null) {
							throw new RuntimeException("此副武器基础属性必须包含【弹夹容量】类型 装备ID=" + equi.id);
						}
						break;
					}
					}
				}

				private SecondWeapon(KItem_EquipmentData equimentData) {
					this.equimentData = equimentData;

					// 全部职业改用机枪作为副武器 by camus @201411241135
					KItemTempEqui equi = ((KItemTempEqui) equimentData.owner.owner.getItemTemplate());
					switch (equi.jobEnum) {
					case WARRIOR: {
//						type = SECOND_WEAPON_TYPE_SHIELD;
//						break;
					}
					case SHADOW: {
//						type = SECOND_WEAPON_TYPE_BROADSWORD;
//						CohesionPct = equi.cohesionPct;
//						break;
					}
					case GUNMAN: {
						type = SECOND_WEAPON_TYPE_MACHINE_GUN;
						overheatedInMills = (long) (Timer.ONE_SECOND * equi.overheated);
						ClipVolume = equi.volume;
						break;
					}
					}
				}

				void reset() {
					KItemTempEqui equi = ((KItemTempEqui) equimentData.owner.owner.getItemTemplate());

					// 全部职业改用机枪作为副武器 by camus @201411241135
					Map<KGameAttrType, AtomicInteger> baseEffects = equimentData.getAllEffect(null);
					switch (equi.jobEnum) {
					case WARRIOR: {
//						AtomicInteger value = baseEffects.get(KGameAttrType.BLOCK);
//						Block = value.get();
//						break;
					}
					case SHADOW: {
//						AtomicInteger value = baseEffects.get(KGameAttrType.COHESION_FIXED);
//						CohesionFixedDm = value.get();
//						break;
					}
					case GUNMAN: {
						AtomicInteger value = baseEffects.get(KGameAttrType.SHOT_DM_PCT);
						PCT = value.get();
						break;
					}
					}
				}

				@Override
				public byte getType() {
					return type;
				}

				@Override
				public int getBlock() {
					return Block;
				}

				@Override
				public int getCohesionFixedDm() {
					return CohesionFixedDm;
				}

				@Override
				public int getCohesionPct() {
					return CohesionPct;
				}

				@Override
				public int getMachineGunDmPct() {
					return PCT;
				}

				@Override
				public int getClip() {
					return ClipVolume;
				}

				@Override
				public long getMachineGunCD() {
					return overheatedInMills;
				}
			}
		}
	}
}
