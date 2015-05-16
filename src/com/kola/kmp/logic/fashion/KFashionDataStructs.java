package com.kola.kmp.logic.fashion;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.management.timer.Timer;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KItemQualityEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.util.text.HyperTextTool;

/**
 * <pre>
 * 本类负责定义本模块的数据结构
 * 本类纯粹定义数据结构,而不管理数据
 * @author CamusHuang
 * @creation 2012-11-6 上午11:09:57
 * </pre>
 */
public class KFashionDataStructs {

	public static final Logger _LOGGER = KGameLogger.getLogger(KFashionDataStructs.class);

	/**
	 * <pre>
	 * 时装模板
	 * 
	 * @param <ED>
	 * @author CamusHuang
	 * @creation 2014-2-23 下午4:59:40
	 * </pre>
	 */
	public static class KFashionTemplate {
		// ----------以下是EXCEL表格直导数据---------
		public int id;// 时装id号
		public String name;// 时装名称
		private int qua;// 品质
		public boolean isShowInList;// 未获得时是否显示于列表
		private int bgold;// 买入金币
		private int brmb;// 买入钻石
		public int icon;// 对应图片
		public String desc;// 物品描述
		public int job;// 需求职业
		private int[] attribute_id;// 增加属性编号
		private int[] attribute;// 增加属性数量
		private int time;// 时效类型
		private float duration;// 有效期（秒）
		public String res_id;// 美术资源
		public int status_id;// 附带时装被动状态ID
		public String catchDesc; //获得描述
		public String catchDesc2; //点击穿戴时的详细描述

		// ----------以下是逻辑数据---------
		public String extName;
		public KJobTypeEnum jobEnum;// null表示无职业需求
		public KCurrencyCountStruct buyMoney;// null表示不能购买
		public KItemQualityEnum ItemQuality;// 品质
		public long effectTime;// 有效期（毫秒，<=0表示永久）
		public Map<KGameAttrType, Integer> allEffects = Collections.emptyMap();// LinkedHashMap
		
		/**
		 * <pre>
		 * 服务器启动完成
		 * 
		 * @author CamusHuang
		 * @creation 2012-12-3 下午8:41:04
		 * </pre>
		 */
		void dataLoadFinishedNotify() throws Exception {
			jobEnum = KJobTypeEnum.getJob((byte) job);
			ItemQuality = KItemQualityEnum.getEnum(qua);
			if (ItemQuality == null) {
				throw new Exception("品质不存在  qua=" + qua + " id=" + id);
			}
			extName = HyperTextTool.extColor(name, ItemQuality.color);
		}
		
		//

		/**
		 * <pre>
		 * 
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-2-18 下午6:01:47
		 * </pre>
		 */
		void notifyCacheLoadComplete() throws Exception {
			if (bgold > 0 && brmb > 0) {
				throw new Exception("价格配置错误 id=" + id);
			}
			if (bgold > 0) {
				buyMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, bgold);
			} else if (brmb > 0) {
				buyMoney = new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, brmb);
			}
			
			if(isShowInList){
				if (buyMoney == null && (catchDesc.isEmpty() || catchDesc2.isEmpty()) ) {
					throw new Exception("显示于列表的时装必须配置价格或者获取来源 id=" + id);
				}
			}
			
			if (time == 1) {
				effectTime = 0;
			} else {
				effectTime = (long) (duration * Timer.ONE_SECOND);
			}
			{
				if (attribute_id.length != attribute.length || attribute_id.length < 1) {
					throw new Exception("属性加成未配置 id=" + id);
				}
				Map<KGameAttrType, Integer> tempEffects = new LinkedHashMap<KGameAttrType, Integer>();
				for (int index = 0; index < attribute_id.length; index++) {
					if (attribute[index] < 1) {
						continue;
					}
					KGameAttrType type = KGameAttrType.getAttrTypeEnum(attribute_id[index]);
					if (type == null) {
						throw new Exception("属性加成类型不存在 type=" + attribute_id[index] + " id=" + id);
					}
					tempEffects.put(type, attribute[index]);
				}

				if (tempEffects.isEmpty()) {
					throw new Exception("属性加成未配置 id=" + id);
				}
				allEffects = Collections.unmodifiableMap(tempEffects);
			}
			// CTODO 其它约束检查

		}
	}

}
