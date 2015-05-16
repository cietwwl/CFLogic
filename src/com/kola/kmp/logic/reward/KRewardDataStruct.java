package com.kola.kmp.logic.reward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.KActivityTimeStruct.TimeIntervalStruct;
import com.kola.kmp.logic.fashion.KFashionDataStructs.KFashionTemplate;
import com.kola.kmp.logic.fashion.KFashionMsgPackCenter;
import com.kola.kmp.logic.flow.KPetFlowType;
import com.kola.kmp.logic.flow.KRoleAttrModifyType;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.pet.KPetTemplate;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.CommonActivityTime;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;
import com.kola.kmp.logic.util.ResultStructs.MailResult;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_SendMail;
import com.kola.kmp.logic.util.text.HyperTextTypeEnum;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ItemTips;
import com.kola.kmp.logic.util.tips.MailTips;
import com.kola.kmp.logic.util.tips.ShopTips;

/**
 * <pre>
 * 本类负责定义本模块的数据结构
 * 本类纯粹定义数据结构,而不管理数据
 * 
 * @author CamusHuang
 * @creation 2013-5-17 上午11:56:24
 * </pre>
 */
public class KRewardDataStruct {
	public static final Logger _LOGGER = KGameLogger.getLogger(KRewardDataStruct.class);

	private KRewardDataStruct() {
	}

	/**
	 * <pre>
	 * 用于保存加载的邮件标题和内容
	 * 邮件标题	邮件正文
	 * mailTitle	mailContent
	 * 
	 * @author CamusHuang
	 * @creation 2013-12-24 下午5:14:33
	 * </pre>
	 */
	public final static class BaseMailContent {
		private String mailTitle;// 邮件标题
		private String mailContent;// 邮件内容
		private int[] picResIds;// 图片资源ID
		private String[] urlLinks;// 外链URL-富文本

		/**
		 * <pre>
		 * 
		 * 
		 * @param mailTitle
		 * @param mailContent
		 * @param picResIds 可为NULL
		 * @param uiLinks 可为NULL
		 * @author CamusHuang
		 * @creation 2014-5-14 下午7:53:03
		 * </pre>
		 */
		public BaseMailContent(String mailTitle, String mailContent, int[] picResIds, String[] uiLinks) {
			if (picResIds == null) {
				picResIds = new int[0];
			}
			if (uiLinks == null) {
				uiLinks = new String[0];
			}
			//
			this.mailTitle = mailTitle;
			this.mailContent = mailContent;
			this.picResIds = picResIds;
			this.urlLinks = uiLinks;
			//
			if (mailTitle == null || mailTitle.isEmpty()) {
				throw new RuntimeException("邮件标题不允许为空");
			}
			if (mailContent == null || mailContent.isEmpty()) {
				throw new RuntimeException("邮件内容不允许为空");
			}
		}

		private BaseMailContent() {
		}

		public String getMailTitle() {
			return mailTitle;
		}

		public String getMailContent() {
			return mailContent;
		}

		public int[] getPicResIds() {
			return picResIds;
		}

		public String[] getUrlLinks() {
			return urlLinks;
		}

		public static BaseMailContent loadData(KGameExcelRow row) throws Exception {

			if (row.containsCol("mailTitle")) {
				BaseMailContent obj = new BaseMailContent();
				ReflectPaser.parseExcelData(obj, row, true);
				if (obj.mailTitle == null || obj.mailTitle.isEmpty()) {
					throw new Exception("邮件标题不允许为空");
				}
				if (obj.mailContent == null || obj.mailContent.isEmpty()) {
					throw new Exception("邮件内容不允许为空");
				}
				if (obj.picResIds == null) {
					obj.picResIds = new int[0];
				}
				if (obj.urlLinks == null) {
					obj.urlLinks = new String[0];
				}
				//
				String startSign = HyperTextTypeEnum.url.startSign.substring(0, HyperTextTypeEnum.url.startSign.length() - 1);
				for (String url : obj.urlLinks) {
					if (!url.endsWith(HyperTextTypeEnum.url.endSign)) {
						throw new Exception("URL格式错误=" + url);
					}
					if (!url.startsWith(startSign)) {
						throw new Exception("URL格式错误=" + url);
					}
				}
				return obj;
			}
			return null;
		}
	}

	/**
	 * <pre>
	 * 对应《奖励表头-标准》
	 * 
	 * 警告：服务器初始化阶段，必须使用{@link BaseRewardData#BaseRewardData(List, List, List, false)}建立对象，并且在服务器启动完成后调用{@link BaseRewardData#notifyCacheLoadComplete()}方法检测数据和初始化tips
	 * 警告：服务器运行过程中，必须使用{@link BaseRewardData#BaseRewardData(List, List, List)}建立对象，内部会同时检测数据和初始化tips
	 * 
	 * @author CamusHuang
	 * @creation 2013-12-24 下午5:44:20
	 * </pre>
	 */
	public final static class BaseRewardData {
		public final List<AttValueStruct> attList;// 所有属性增量，不为NULL
		public final List<KCurrencyCountStruct> moneyList;// 所有货币，不为NULL
		public final List<ItemCountStruct> itemStructs;// 道具，不为NULL
		public final List<Integer> fashionTempIdList;// 时装，不为null
		public final List<Integer> petTempIdList;// 宠物，不为null

		//
		public final List<String> dataUprisingTips = new ArrayList<String>();// 所有奖励内容的浮动提示
		private String rewardTips;// 所有奖励内容用、号分隔串成一串

		/**
		 * <pre>
		 * 内部会检查道具模板和货币类型是否存在或重复，并生成相应tips，一般用于在运行时建立的对象
		 * 注意：必须存在有效奖励项，即参数不能全部为null
		 * 
		 * @param attList null表示无相关奖励
		 * @param moneyList null表示无相关奖励
		 * @param itemStructs null表示无相关奖励
		 * @param fashionTempIdList null表示无相关奖励
		 * @param petTempIdList null表示无相关奖励
		 * @author CamusHuang
		 * @creation 2014-4-25 下午3:03:51
		 * </pre>
		 */
		public BaseRewardData(List<AttValueStruct> attList, List<KCurrencyCountStruct> moneyList, List<ItemCountStruct> itemStructs, List<Integer> fashionTempIdList, List<Integer> petTempIdList) {
			this(attList, moneyList, itemStructs, fashionTempIdList, petTempIdList, true, true);
		}

		/**
		 * <pre>
		 * isCheck = true时，内部会检查道具模板和货币类型是否存在或重复，并生成相应tips，一般用于在运行时建立的对象
		 * 注意：必须存在有效奖励项，即参数不能全部为null
		 * 
		 * @param attList null表示无相关奖励
		 * @param moneyList null表示无相关奖励
		 * @param itemStructs null表示无相关奖励
		 * @param fashionTempIdList null表示无相关奖励
		 * @param petTempIdList null表示无相关奖励
		 * @param isMerge   true表示是否合并数据 
		 * @param isCheck true时，内部会检查道具模板和货币类型是否存在或重复，并生成相应tips，一般用于在运行时建立的对象
		 * @author CamusHuang
		 * @creation 2014-4-25 下午3:04:53
		 * </pre>
		 */
		public BaseRewardData(List<AttValueStruct> attList, List<KCurrencyCountStruct> moneyList, List<ItemCountStruct> itemStructs, List<Integer> fashionTempIdList, List<Integer> petTempIdList,
				boolean isMerge, boolean isCheck) {
			if (isMerge) {
				if (attList != null && !attList.isEmpty()) {
					attList = AttValueStruct.mergeCountStructs(attList);
				}
				if (moneyList != null && !moneyList.isEmpty()) {
					moneyList = KCurrencyCountStruct.mergeCurrencyCountStructs(moneyList);
				}
				if (itemStructs != null && !itemStructs.isEmpty()) {
					itemStructs = ItemCountStruct.mergeItemCountStructs(itemStructs);
				}
			}
			this.attList = (attList == null || attList.isEmpty()) ? Collections.<AttValueStruct> emptyList() : Collections.unmodifiableList(attList);
			this.moneyList = (moneyList == null || moneyList.isEmpty()) ? Collections.<KCurrencyCountStruct> emptyList() : Collections.unmodifiableList(moneyList);
			this.itemStructs = (itemStructs == null || itemStructs.isEmpty()) ? Collections.<ItemCountStruct> emptyList() : Collections.unmodifiableList(itemStructs);
			this.fashionTempIdList = (fashionTempIdList == null || fashionTempIdList.isEmpty()) ? Collections.<Integer> emptyList() : Collections.unmodifiableList(fashionTempIdList);
			this.petTempIdList = (petTempIdList == null || petTempIdList.isEmpty()) ? Collections.<Integer> emptyList() : Collections.unmodifiableList(petTempIdList);

			if (isCheck) {
				try {
					notifyCacheLoadComplete();
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @param row
		 * @param isCheckEffect 是否检查有无奖励，true且没有奖励数据时会抛异常
		 * @return
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-1-9 上午11:46:37
		 * </pre>
		 */
		public static BaseRewardData loadData(KGameExcelRow row, boolean isCheckEffect) throws Exception {

			final int MaxColCount = 20;
			List<Integer> petList = new ArrayList<Integer>();
			{
				for (int i = 0; i < MaxColCount; i++) {
					int index = i + 1;
					String colName = "petTempId" + index;
					if (!row.containsCol(colName)) {
						break;
					}
					//
					String colData = row.getData(colName);
					if (colData == null || colData.isEmpty()) {
						continue;
					}
					int tempId = row.getInt(colName);
					if (tempId <= 0) {
						continue;
					}

					petList.add(tempId);
				}

				if (petList.isEmpty()) {
					petList = Collections.emptyList();
				}
			}
			List<Integer> fashionList = new ArrayList<Integer>();
			{
				for (int i = 0; i < MaxColCount; i++) {
					int index = i + 1;
					String colName = "fashionTempId" + index;
					if (!row.containsCol(colName)) {
						break;
					}
					//
					String colData = row.getData(colName);
					if (colData == null || colData.isEmpty()) {
						continue;
					}
					int tempId = row.getInt(colName);
					if (tempId <= 0) {
						continue;
					}

					fashionList.add(tempId);
				}

				if (fashionList.isEmpty()) {
					fashionList = Collections.emptyList();
				}
			}
			List<AttValueStruct> attList = new ArrayList<AttValueStruct>();
			{
				for (int i = 0; i < MaxColCount; i++) {
					int index = i + 1;
					String colName = "attType" + index;
					if (!row.containsCol(colName)) {
						break;
					}
					//
					String colData = row.getData(colName);
					if (colData == null || colData.isEmpty()) {
						continue;
					}
					String data = row.getData(colName);
					if (data == null || data.isEmpty()) {
						continue;
					}
					int type = row.getInt(colName);
					if (type <= 0) {
						continue;
					}

					KGameAttrType typeE = KGameAttrType.getAttrTypeEnum(type);
					if (typeE == null) {
						throw new Exception("未经定义的属性类型 =" + type);
					}
					int count = row.getInt("attValue" + index);
					if (count == 0) {
						continue;
					}
					if (count <= 0) {
						throw new Exception("属性值不能 =" + count);
					}
					attList.add(new AttValueStruct(typeE, count, 0));
				}

				if (attList.isEmpty()) {
					attList = Collections.emptyList();
				}
			}
			//
			List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
			{
				Set<KCurrencyTypeEnum> moneyTypeSet = new HashSet<KCurrencyTypeEnum>();
				for (int i = 0; i < MaxColCount; i++) {
					int index = i + 1;
					String colName = "moneyType" + index;
					if (!row.containsCol(colName)) {
						continue;
					}
					//
					String colData = row.getData(colName);
					if (colData == null || colData.isEmpty()) {
						continue;
					}
					byte type = row.getByte(colName);
					if (type <= 0) {
						continue;
					}

					KCurrencyTypeEnum typeE = KCurrencyTypeEnum.getEnum(type);
					if (typeE == null) {
						throw new Exception("未经定义的货币类型 =" + type);
					}
					int count = row.getInt("moneyCount" + index);
					if (count == 0) {
						continue;
					}
					if (count < 0) {
						throw new Exception("货币数量不能 =" + count);
					}

					if (!moneyTypeSet.add(typeE)) {
						throw new Exception("货币类型重复=" + typeE.name);
					}

					moneyList.add(new KCurrencyCountStruct(typeE, count));
				}

				if (moneyList.isEmpty()) {
					moneyList = Collections.emptyList();
				}
			}
			//
			List<ItemCountStruct> itemStructs = new ArrayList<ItemCountStruct>();
			{
				for (int i = 0; i < MaxColCount; i++) {
					int index = i + 1;
					String colName = "itemTempId" + index;
					if (!row.containsCol(colName)) {
						continue;
					}
					//
					String itemCode = row.getData(colName);
					if (itemCode == null || itemCode.isEmpty()) {
						continue;
					}
					int count = row.getInt("itemCount" + index);
					if (count == 0) {
						continue;
					}
					if (count < 0) {
						throw new Exception("道具数量不能=" + count);
					}
					itemStructs.add(new ItemCountStruct(itemCode, count));
				}
				if (itemStructs.isEmpty()) {
					itemStructs = Collections.emptyList();
				}
			}

			BaseRewardData temp = new BaseRewardData(attList, moneyList, itemStructs, fashionList, petList, true, false);
			if (isCheckEffect) {
				temp.checkEffect();
			}
			return temp;
		}

		public void checkEffect() throws Exception {
			if (attList.isEmpty() && moneyList.isEmpty() && itemStructs.isEmpty() && petTempIdList.isEmpty() && fashionTempIdList.isEmpty()) {
				throw new Exception("所有奖励项无效");
			}
		}

		public boolean checkIsEffect() {
			if (attList.isEmpty() && moneyList.isEmpty() && itemStructs.isEmpty() && petTempIdList.isEmpty() && fashionTempIdList.isEmpty()) {
				return false;
			}
			return true;
		}

		/**
		 * <pre>
		 * 发送奖励
		 * 方法内部会检测背包是否满，已满则不发送并返回false
		 * 
		 * @param role
		 * @param presentPointTypeEnum 如果包含点数，则最好传入加点类型，否则使用默认类型
		 * @return
		 * @author CamusHuang
		 * @creation 2014-4-16 上午11:35:06
		 * </pre>
		 */
		public boolean sendReward(KRole role, PresentPointTypeEnum presentPointTypeEnum) {
			return sendReward2(role, presentPointTypeEnum).isSucess;
		}

		/**
		 * <pre>
		 * 发送奖励
		 * 方法内部会检测背包是否满，已满则不发送并返回false
		 * 
		 * @param role
		 * @param presentPointTypeEnum 如果包含点数，则最好传入加点类型，否则使用默认类型
		 * @return 返回一个带有新增物品列表的结果
		 * @author CamusHuang
		 * @creation 2014-11-24 下午4:44:02
		 * </pre>
		 */
		public ItemResult_AddItem sendReward2(KRole role, PresentPointTypeEnum presentPointTypeEnum) {
			ItemResult_AddItem result = new ItemResult_AddItem();

			// 检查背包容量
			if (!itemStructs.isEmpty()) {
				if (!KSupportFactory.getItemModuleSupport().isCanAddItemsToBag(role.getId(), itemStructs)) {
					result.tips = ItemTips.背包已满;
					return result;
				}
			}

			// 执行
			if (!petTempIdList.isEmpty()) {
				CommonResult petresult = KSupportFactory.getPetModuleSupport().createPetsToRole(role.getId(), petTempIdList,
						presentPointTypeEnum == null ? KPetFlowType.通用奖励.name() : presentPointTypeEnum.name());
				if (!petresult.isSucess) {
					result.tips = petresult.tips;
					return result;
				}
			}

			if (!itemStructs.isEmpty()) {
				ItemResult_AddItem itemresult = KSupportFactory.getItemModuleSupport().addItemsToBag(role, itemStructs, presentPointTypeEnum == null ? "通用奖励" : presentPointTypeEnum.name());
				if (!itemresult.isSucess) {
					// 正常情况下不会失败，若失败则只作记录，不回滚，不返回失败
					StringBuffer sbf = new StringBuffer();
					for (ItemCountStruct struct : itemStructs) {
						sbf.append(struct.itemCode).append('x').append(struct.itemCount).append(';');
					}
					_LOGGER.warn("发送物品奖励失败 roleID=,{},roleName=,{},随从=,{}", role.getId(), role.getName(), sbf.toString());
				}
				result.newItemList = itemresult.newItemList;
				result.updateItemCountList = itemresult.updateItemCountList;
			}

			if (!fashionTempIdList.isEmpty()) {
				CommonResult fashionresult = KSupportFactory.getFashionModuleSupport().addFashions(role, fashionTempIdList, "通用奖励");
				if (!fashionresult.isSucess) {
					// 正常情况下不会失败，若失败则只作记录，不回滚，不返回失败
					StringBuffer sbf = new StringBuffer();
					for (int tempId : fashionTempIdList) {
						sbf.append(tempId).append(';');
					}
					_LOGGER.warn("发送时装奖励失败 roleID=,{},roleName=,{},时装ID=,{}", role.getId(), role.getName(), sbf.toString());
				}
			}

			if (!moneyList.isEmpty()) {
				KSupportFactory.getCurrencySupport().increaseMoneys(role.getId(), moneyList, presentPointTypeEnum, true);
			}

			if (!attList.isEmpty()) {
				KSupportFactory.getRoleModuleSupport().addAttsFromBaseReward(role, attList, KRoleAttrModifyType.通用奖励,
						presentPointTypeEnum == null ? KRoleAttrModifyType.通用奖励.name() : presentPointTypeEnum.name());
			}

			result.isSucess = true;
			return result;
		}

		/**
		 * <pre>
		 * 验证所有数据
		 * 
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-1-4 下午8:26:02
		 * </pre>
		 */
		public void notifyCacheLoadComplete() throws Exception {
			// 验证是否所有道具均存在
			for (ItemCountStruct data : itemStructs) {
				KItemTempAbs temp = data.getItemTemplate();
				if (temp == null) {
					throw new Exception("要赠送的道具不存在 itemCode=" + data.itemCode);
				}
			}
			//
			for (Integer tempId : petTempIdList) {
				if (KSupportFactory.getPetModuleSupport().getPetTemplate(tempId) == null) {
					throw new Exception("宠物模板不存在 id=" + tempId);
				}
			}
			//
			for (Integer tempId : fashionTempIdList) {
				if (KSupportFactory.getFashionModuleSupport().getFashionTemplate(tempId) == null) {
					throw new Exception("时装模板不存在 id=" + tempId);
				}
			}

			{
				StringBuffer sbf = new StringBuffer();
				dataUprisingTips.clear();

				if (!attList.isEmpty()) {
					for (AttValueStruct data : attList) {
						dataUprisingTips.add(StringUtil.format(ShopTips.x加x, data.roleAttType.getExtName(), data.addValue));
						sbf.append(GlobalTips.顿号).append(data.roleAttType.getName()).append("x").append(data.addValue);
					}
				}

				if (!itemStructs.isEmpty()) {
					for (ItemCountStruct data : itemStructs) {
						dataUprisingTips.add(StringUtil.format(ShopTips.x加x, data.getItemTemplate().extItemName, data.itemCount));
						sbf.append(GlobalTips.顿号).append(data.getItemTemplate().extItemName).append("x").append(data.itemCount);
					}
				}

				if (!moneyList.isEmpty()) {
					for (KCurrencyCountStruct struct : moneyList) {
						dataUprisingTips.add(StringUtil.format(ShopTips.x加x, struct.currencyType.extName, struct.currencyCount));
						sbf.append(GlobalTips.顿号).append(struct.currencyType.extName).append("x").append(struct.currencyCount);
					}
				}

				//
				for (Integer tempId : petTempIdList) {
					KPetTemplate temp = KSupportFactory.getPetModuleSupport().getPetTemplate(tempId);
					dataUprisingTips.add(StringUtil.format(ShopTips.x加x, temp.getNameEx(), 1));
					sbf.append(GlobalTips.顿号).append(temp.defaultName).append("x").append(1);
				}
				//
				for (Integer tempId : fashionTempIdList) {
					KFashionTemplate temp = KSupportFactory.getFashionModuleSupport().getFashionTemplate(tempId);
					dataUprisingTips.add(StringUtil.format(ShopTips.x加x, temp.extName, 1));
					sbf.append(GlobalTips.顿号).append(temp.name).append("x").append(1);
				}

				if (sbf.length() > 0) {
					sbf.deleteCharAt(0);
				}
				rewardTips = sbf.toString();
			}
		}

		/**
		 * <pre>
		 * 所有奖励内容用、号分隔串成一串
		 * 例如 钻石x10、金币x200、。。。
		 * 
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2014-3-5 下午4:20:50
		 * </pre>
		 */
		public String getRewardTips() {
			return rewardTips;
		}

		public String toCVSString() {
			StringBuffer sbf = new StringBuffer();
			for (AttValueStruct temp : attList) {
				sbf.append(",").append(temp.roleAttType.getName()).append(",").append(temp.addValue);
			}
			for (KCurrencyCountStruct temp : moneyList) {
				sbf.append(",").append(temp.currencyType.name).append(",").append(temp.currencyCount);
			}
			for (ItemCountStruct temp : itemStructs) {
				sbf.append(",").append(temp.getItemTemplate().name).append(",").append(temp.itemCount);
			}
			//
			for (Integer tempId : petTempIdList) {
				KPetTemplate temp = KSupportFactory.getPetModuleSupport().getPetTemplate(tempId);
				sbf.append(",").append(temp.defaultName).append(",").append(1);
			}
			//
			for (Integer tempId : fashionTempIdList) {
				KFashionTemplate temp = KSupportFactory.getFashionModuleSupport().getFashionTemplate(tempId);
				if (temp != null) {
					sbf.append(",").append(temp.name).append(",").append(1);
				}
			}
			return sbf.toString();
		}

		/**
		 * <pre>
		 * 按倍数复制通用奖励
		 * 
		 * @param baseReward
		 * @param rate
		 * @return
		 * @author CamusHuang
		 * @creation 2014-11-16 下午9:16:04
		 * </pre>
		 */
		public static BaseRewardData copyForRate(BaseRewardData baseReward, int rate) {
			List<AttValueStruct> attList = baseReward.attList.isEmpty() ? null : new ArrayList<AttValueStruct>();
			List<KCurrencyCountStruct> moneyList = baseReward.moneyList.isEmpty() ? null : new ArrayList<KCurrencyCountStruct>();
			List<ItemCountStruct> itemStructs = baseReward.itemStructs.isEmpty() ? null : new ArrayList<ItemCountStruct>();
			List<Integer> fashionTempIdList = baseReward.fashionTempIdList.isEmpty() ? null : new ArrayList<Integer>();
			List<Integer> petTempIdList = baseReward.petTempIdList.isEmpty() ? null : new ArrayList<Integer>();
			for (AttValueStruct temp : baseReward.attList) {
				attList.add(new AttValueStruct(temp.roleAttType, temp.addValue * rate));
			}
			for (KCurrencyCountStruct temp : baseReward.moneyList) {
				moneyList.add(new KCurrencyCountStruct(temp.currencyType, temp.currencyCount * rate));
			}
			for (ItemCountStruct temp : baseReward.itemStructs) {
				itemStructs.add(new ItemCountStruct(temp.itemCode, temp.itemCount * rate));
			}
			for (Integer temp : baseReward.fashionTempIdList) {
				for (int i = 0; i < rate; i++) {
					fashionTempIdList.add(temp);
				}
			}
			for (Integer temp : baseReward.petTempIdList) {
				for (int i = 0; i < rate; i++) {
					petTempIdList.add(temp);
				}
			}

			BaseRewardData baseRewardForRate = new BaseRewardData(attList, moneyList, itemStructs, fashionTempIdList, petTempIdList, true, false);
			return baseRewardForRate;
		}

		/**
		 * <pre>
		 * 将baseReward 和 fromReward的奖励合并到新的BaseRewardData
		 * 
		 * @param baseReward
		 * @param fromReward
		 * @return
		 * @author CamusHuang
		 * @creation 2014-11-16 下午9:17:27
		 * </pre>
		 */
		public static BaseRewardData mergeReward(BaseRewardData baseReward, BaseRewardData fromReward) {
			List<AttValueStruct> attList = new ArrayList<AttValueStruct>(baseReward.attList);
			List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>(baseReward.moneyList);
			List<ItemCountStruct> itemStructs = new ArrayList<ItemCountStruct>(baseReward.itemStructs);
			List<Integer> fashionTempIdList = new ArrayList<Integer>(baseReward.fashionTempIdList);
			List<Integer> petTempIdList = new ArrayList<Integer>(baseReward.petTempIdList);

			attList.addAll(fromReward.attList);
			attList = AttValueStruct.mergeCountStructs(attList);
			//
			moneyList.addAll(fromReward.moneyList);
			moneyList = KCurrencyCountStruct.mergeCurrencyCountStructs(moneyList);
			//
			itemStructs.addAll(fromReward.itemStructs);
			itemStructs = ItemCountStruct.mergeItemCountStructs(itemStructs);
			//
			fashionTempIdList.addAll(fromReward.fashionTempIdList);
			//
			petTempIdList.addAll(fromReward.petTempIdList);

			BaseRewardData baseRewardForRate = new BaseRewardData(attList, moneyList, itemStructs, fashionTempIdList, petTempIdList);
			return baseRewardForRate;
		}

		/**
		 * <pre>
		 * 参考{@link com.kola.kmp.protocol.reward.KRewardProtocol#MSG_STRUCT_COMMON_REWARD}
		 * 
		 * @param msg
		 * @author CamusHuang
		 * @creation 2013-12-8 下午5:54:28
		 * </pre>
		 */
		public void packMsg(KGameMessage msg) {
			msg.writeByte(attList.size());
			for (AttValueStruct struct : attList) {
				msg.writeInt(struct.roleAttType.sign);
				msg.writeFloat(struct.addValue);
			}
			//
			msg.writeByte(moneyList.size());
			for (KCurrencyCountStruct struct : moneyList) {
				msg.writeByte(struct.currencyType.sign);
				msg.writeLong(struct.currencyCount);
			}
			//
			msg.writeByte(itemStructs.size());
			for (ItemCountStruct struct : itemStructs) {
				KItemMsgPackCenter.packItem(msg, struct.getItemTemplate(), struct.itemCount);
			}
			//
			msg.writeByte(petTempIdList.size());
			for (Integer tempId : petTempIdList) {
				KSupportFactory.getPetModuleSupport().packPetTemplateMsg(msg, tempId);
			}
			//
			{
				int writeIndex = msg.writerIndex();
				int count = 0;

				msg.writeByte(fashionTempIdList.size());
				for (Integer tempId : fashionTempIdList) {
					if (KFashionMsgPackCenter.packFashion(msg, 0, tempId)) {
						count++;
					}
				}
				msg.setByte(writeIndex, count);
			}
		}
	}

	/**
	 * <pre>
	 * 对应《奖励表头-分职业》
	 * 
	 * @author CamusHuang
	 * @creation 2014-11-25 下午4:22:17
	 * </pre>
	 */
	public final static class BaseRewardDataForJobs {
		private Map<Byte, BaseRewardData> datasMap = new HashMap<Byte, BaseRewardData>();

		/**
		 * <pre>
		 * isRunning = true时，内部会检查道具模板和货币类型是否存在，并生成相应tips
		 * 注意：必须存在有效奖励项，即参数不能全部为null
		 * 
		 * @param map
		 * @author CamusHuang
		 * @creation 2014-11-25 下午12:34:54
		 * </pre>
		 */
		public BaseRewardDataForJobs(Map<Byte, BaseRewardData> map, boolean isRunning) {
			datasMap.putAll(map);

			if (isRunning) {
				try {
					notifyCacheLoadComplete();
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @param row
		 * @param isCheckEffect 是否检查有无奖励，true且没有奖励数据时会抛异常
		 * @return
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-11-25 下午4:22:34
		 * </pre>
		 */
		public static BaseRewardDataForJobs loadData(KGameExcelRow row, boolean isCheckEffect) throws Exception {

			// 通用随从
			List<Integer> petList = paramsInts(row, "petTempIds");
			// 通用货币
			List<KCurrencyCountStruct> moneyList = new ArrayList<KCurrencyCountStruct>();
			{
				List<Integer> moneyTypes = paramsInts(row, "moneyTypes");
				List<Integer> moneyCounts = paramsInts(row, "moneyCounts");
				if (moneyTypes.size() != moneyCounts.size()) {
					throw new Exception("货币类型与数量长度不匹配");
				}
				//
				for (int i = 0; i < moneyTypes.size(); i++) {
					int type = moneyTypes.get(i);
					long count = moneyCounts.get(i);
					//
					KCurrencyTypeEnum typeE = KCurrencyTypeEnum.getEnum(type);
					if (typeE == null) {
						throw new Exception("未经定义的货币类型 =" + type);
					}
					if (count < 0) {
						throw new Exception("货币数量不能 =" + count);
					}
					moneyList.add(new KCurrencyCountStruct(typeE, count));
				}

				if (moneyList.isEmpty()) {
					moneyList = Collections.emptyList();
				}
			}

			// 各专业时装
			Map<Byte, List<Integer>> jobFashions = new HashMap<Byte, List<Integer>>();
			{
				// 通用时装
				List<Integer> fashionList = paramsInts(row, "fashionIds");
				//
				for (KJobTypeEnum job : KJobTypeEnum.values()) {
					List<Integer> job_fashionIds = paramsInts(row, "job" + job.getJobType() + "_fashionIds");
					job_fashionIds.addAll(fashionList);// 叠加通用时装
					jobFashions.put(job.getJobType(), job_fashionIds);
				}
			}

			// 各职业属性
			Map<Byte, List<AttValueStruct>> jobAttss = new HashMap<Byte, List<AttValueStruct>>();
			{
				// 通用属性
				List<AttValueStruct> attsList = new ArrayList<AttValueStruct>();
				{
					List<Integer> ids = paramsInts(row, "attIds");
					List<Integer> values = paramsInts(row, "attValues");
					if (ids.size() != values.size()) {
						throw new Exception("通用属性类型与数量长度不匹配");
					}
					//
					for (int i = 0; i < ids.size(); i++) {
						int type = ids.get(i);
						int count = values.get(i);
						//
						KGameAttrType typeE = KGameAttrType.getAttrTypeEnum(type);
						if (typeE == null) {
							throw new Exception("未经定义的属性类型 =" + type);
						}
						if (count < 0) {
							throw new Exception("属性值不能 =" + count);
						}
						attsList.add(new AttValueStruct(typeE, count));
					}

					if (attsList.isEmpty()) {
						attsList = Collections.emptyList();
					}
				}
				{

					for (KJobTypeEnum job : KJobTypeEnum.values()) {
						List<Integer> ids = paramsInts(row, "job" + job.getJobType() + "_attIds");
						List<Integer> values = paramsInts(row, "job" + job.getJobType() + "_attValues");
						if (ids.size() != values.size()) {
							throw new Exception("职业" + job.getJobType() + " 的属性类型与数量长度不匹配");
						}
						//
						List<AttValueStruct> tempAttsList = new ArrayList<AttValueStruct>();
						for (int i = 0; i < ids.size(); i++) {
							int type = ids.get(i);
							int count = values.get(i);
							//
							KGameAttrType typeE = KGameAttrType.getAttrTypeEnum(type);
							if (typeE == null) {
								throw new Exception("职业" + job.getJobType() + " 未经定义的属性类型 =" + type);
							}
							if (count < 0) {
								throw new Exception("职业" + job.getJobType() + " 属性值不能 =" + count);
							}
							tempAttsList.add(new AttValueStruct(typeE, count));
						}
						tempAttsList.addAll(attsList);// 叠加通用属性
						if (tempAttsList.isEmpty()) {
							tempAttsList = Collections.emptyList();
						}
						jobAttss.put(job.getJobType(), tempAttsList);
					}
				}
			}
			// 各职业物品
			Map<Byte, List<ItemCountStruct>> jobItems = new HashMap<Byte, List<ItemCountStruct>>();
			{
				// 通用物品
				List<ItemCountStruct> itemsList = new ArrayList<ItemCountStruct>();
				{
					List<String> ids = paramsStrings(row, "itemIds");
					List<Integer> values = paramsInts(row, "itemCounts");
					if (ids.size() != values.size()) {
						throw new Exception("通用物品id与数量长度不匹配");
					}
					//
					for (int i = 0; i < ids.size(); i++) {
						String type = ids.get(i);
						int count = values.get(i);
						//
						if (count < 0) {
							throw new Exception("通用物品数量不能 =" + count);
						}
						itemsList.add(new ItemCountStruct(type, count));
					}

					if (itemsList.isEmpty()) {
						itemsList = Collections.emptyList();
					}
				}
				{
					for (KJobTypeEnum job : KJobTypeEnum.values()) {
						List<String> ids = paramsStrings(row, "job" + job.getJobType() + "_itemIds");
						List<Integer> values = paramsInts(row, "job" + job.getJobType() + "_itemCounts");
						if (ids.size() != values.size()) {
							throw new Exception("职业" + job.getJobType() + " 的物品id与数量长度不匹配");
						}
						//
						List<ItemCountStruct> tempItemsList = new ArrayList<ItemCountStruct>();
						for (int i = 0; i < ids.size(); i++) {
							String type = ids.get(i);
							int count = values.get(i);
							//
							if (count < 0) {
								throw new Exception("职业" + job.getJobType() + " 的物品数量不能 =" + count);
							}
							tempItemsList.add(new ItemCountStruct(type, count));
						}
						tempItemsList.addAll(itemsList);// 叠加通用奖励
						if (tempItemsList.isEmpty()) {
							tempItemsList = Collections.emptyList();
						}

						jobItems.put(job.getJobType(), tempItemsList);
					}
				}
			}

			Map<Byte, BaseRewardData> map = new HashMap<Byte, BaseRewardData>();
			for (KJobTypeEnum jobE : KJobTypeEnum.values()) {
				byte job = jobE.getJobType();
				BaseRewardData temp = new BaseRewardData(jobAttss.get(job), moneyList, jobItems.get(job), jobFashions.get(job), petList, true, false);
				if (isCheckEffect) {
					temp.checkEffect();
				}
				map.put(job, temp);
			}

			return new BaseRewardDataForJobs(map, false);
		}

		/**
		 * <pre>
		 * 将指定列的数据，解释成int[]，并转换成list返回
		 * 
		 * @param row
		 * @param colName
		 * @return
		 * @author CamusHuang
		 * @creation 2014-11-25 下午3:33:21
		 * </pre>
		 */
		private static List<Integer> paramsInts(KGameExcelRow row, String colName) {
			List<Integer> dataList = new ArrayList<Integer>();
			{
				if (row.containsCol(colName)) {
					String colData = row.getData(colName);
					if (colData != null && !colData.isEmpty()) {
						int[] temps = (int[]) ReflectPaser.parseValue(colData, "int[]");
						for (int temp : temps) {
							dataList.add(temp);
						}
					}
				}
			}
			return dataList;
		}

		private static List<String> paramsStrings(KGameExcelRow row, String colName) {
			List<String> dataList = new ArrayList<String>();
			{
				if (row.containsCol(colName)) {
					String colData = row.getData(colName);
					if (colData != null && !colData.isEmpty()) {
						String[] temps = (String[]) ReflectPaser.parseValue(colData, "String[]");
						for (String temp : temps) {
							dataList.add(temp);
						}
					}
				}
			}
			return dataList;
		}

		public void checkEffect() throws Exception {
			for (KJobTypeEnum job : KJobTypeEnum.values()) {
				datasMap.get(job.getJobType()).checkEffect();
			}
		}

		public boolean checkIsEffect() {
			boolean isEffect = true;
			for (KJobTypeEnum job : KJobTypeEnum.values()) {
				if (!datasMap.get(job.getJobType()).checkIsEffect()) {
					isEffect = false;
				}
			}

			return isEffect;
		}

		/**
		 * <pre>
		 * 发送奖励
		 * 方法内部会检测背包是否满，已满则不发送并返回false
		 * 
		 * @param role
		 * @param presentPointTypeEnum 如果包含点数，则最好传入加点类型，否则使用默认类型
		 * @return 返回一个带有新增物品列表的结果
		 * @author CamusHuang
		 * @creation 2014-11-24 下午4:44:02
		 * </pre>
		 */
		public ItemResult_AddItem sendReward(KRole role, PresentPointTypeEnum presentPointTypeEnum) {

			BaseRewardData data = datasMap.get(role.getJob());
			return data.sendReward2(role, presentPointTypeEnum);
		}

		/**
		 * <pre>
		 * 验证所有数据
		 * 
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-1-4 下午8:26:02
		 * </pre>
		 */
		public void notifyCacheLoadComplete() throws Exception {
			for (KJobTypeEnum job : KJobTypeEnum.values()) {
				try {
					datasMap.get(job.getJobType()).notifyCacheLoadComplete();
				} catch (Exception e) {
					throw new Exception("职业类型=" + job.getJobType() + " " + e.getMessage(), e);
				}
			}
		}

		public BaseRewardData getBaseRewardData(byte job) {
			return datasMap.get(job);
		}

		/**
		 * <pre>
		 * 所有奖励内容用、号分隔串成一串
		 * 例如 钻石x10、金币x200、。。。
		 * 
		 * 
		 * @return
		 * @author CamusHuang
		 * @creation 2014-3-5 下午4:20:50
		 * </pre>
		 */
		public String getRewardTips(byte job) {
			return datasMap.get(job).getRewardTips();
		}

		public String toCVSString(byte job) {
			return datasMap.get(job).toCVSString();
		}

		/**
		 * <pre>
		 * 按倍数复制通用奖励
		 * 
		 * @param baseReward
		 * @param rate
		 * @return
		 * @author CamusHuang
		 * @creation 2014-11-16 下午9:16:04
		 * </pre>
		 */
		public static BaseRewardDataForJobs copyForRate(BaseRewardDataForJobs baseReward, int rate, boolean isRunning) {

			Map<Byte, BaseRewardData> datasMap = new HashMap<Byte, BaseRewardData>();
			for (KJobTypeEnum jobE : KJobTypeEnum.values()) {
				byte job = jobE.getJobType();
				BaseRewardData temp = BaseRewardData.copyForRate(baseReward.datasMap.get(job), rate);
				datasMap.put(job, temp);
			}

			return new BaseRewardDataForJobs(datasMap, isRunning);
		}

		/**
		 * <pre>
		 * 将baseReward 和 fromReward的奖励合并到新的BaseRewardData
		 * 
		 * @param baseReward
		 * @param fromReward
		 * @return
		 * @author CamusHuang
		 * @creation 2014-11-16 下午9:17:27
		 * </pre>
		 */
		public static BaseRewardDataForJobs mergeReward(BaseRewardDataForJobs baseReward, BaseRewardDataForJobs fromReward) {
			Map<Byte, BaseRewardData> datasMap = new HashMap<Byte, BaseRewardData>();
			for (KJobTypeEnum jobE : KJobTypeEnum.values()) {
				byte job = jobE.getJobType();
				BaseRewardData temp = BaseRewardData.mergeReward(baseReward.datasMap.get(job), fromReward.datasMap.get(job));
				datasMap.put(job, temp);
			}

			return new BaseRewardDataForJobs(datasMap, true);
		}

		/**
		 * <pre>
		 * 参考{@link com.kola.kmp.protocol.reward.KRewardProtocol#MSG_STRUCT_COMMON_REWARD}
		 * 
		 * @param msg
		 * @author CamusHuang
		 * @creation 2013-12-8 下午5:54:28
		 * </pre>
		 */
		public void packMsg(byte job, KGameMessage msg) {
			datasMap.get(job).packMsg(msg);
		}
	}

	/**
	 * <pre>
	 * 对应《奖励表头-邮件》
	 * 
	 * @author CamusHuang
	 * @creation 2013-12-24 下午5:44:20
	 * </pre>
	 */
	public final static class BaseMailRewardData {
		public final int id;
		public final BaseMailContent baseMail;// 邮件
		public final BaseRewardData baseRewardData;// 奖励内容

		public BaseMailRewardData(int id, BaseMailContent baseMail, BaseRewardData baseRewardData) {
			this.id = id;
			this.baseMail = baseMail;
			this.baseRewardData = baseRewardData;
		}

		/**
		 * <pre>
		 * 加载一行数据
		 * 
		 * @param row
		 * @param isCheckEffect 是否检查有无奖励，true且没有奖励数据时会抛异常
		 * @return
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-1-9 上午11:45:56
		 * </pre>
		 */
		public static BaseMailRewardData loadData(KGameExcelRow row, boolean isCheckEffect) throws Exception {

			int id = 0;
			if (row.containsCol("id")) {
				id = row.getInt("id");
			}

			BaseMailContent baseMail = BaseMailContent.loadData(row);
			if (baseMail == null) {
				throw new Exception("缺少邮件内容");
			}

			BaseRewardData baseRewardData = BaseRewardData.loadData(row, isCheckEffect);

			BaseMailRewardData result = new BaseMailRewardData(id, baseMail, baseRewardData);
			return result;
		}

		public void checkEffect() throws Exception {
			baseRewardData.checkEffect();
		}

		public boolean checkIsEffect() {
			return baseRewardData.checkIsEffect();
		}

		/**
		 * <pre>
		 * 验证所有数据
		 * 
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-1-4 下午8:26:02
		 * </pre>
		 */
		public void notifyCacheLoadComplete() throws Exception {
			baseRewardData.notifyCacheLoadComplete();
		}

		/**
		 * <pre>
		 * 发送奖励
		 * 尝试直接发放奖励，方法内部会检测背包是否满，已满则通过邮件发送
		 * 
		 * @param role
		 * @param presentPointTypeEnum 如果包含点数，则最好传入加点类型，否则使用默认类型
		 * @param isTryDirect 是否尝试直接发放奖励
		 * @return
		 * @author CamusHuang
		 * @creation 2014-4-16 上午11:36:53
		 * </pre>
		 */
		public RewardResult_SendMail sendReward(KRole role, PresentPointTypeEnum presentType, boolean isTryDirect) {

			RewardResult_SendMail result = new RewardResult_SendMail();
			if (isTryDirect) {
				if (baseRewardData.sendReward(role, presentType)) {
					result.isSucess = true;
					result.tips = MailTips.成功发送;
					result.addDataUprisingTips(baseRewardData.dataUprisingTips);
					return result;
				}
			}

			MailResult tempResult = KSupportFactory.getMailModuleSupport().sendAttMailBySystem(role.getId(), this, presentType);
			result.isSucess = tempResult.isSucess;
			result.tips = tempResult.tips;
			result.isSendByMail = true;
			return result;
		}

		public String toCVSString() {
			return baseRewardData.toCVSString();
		}

		/**
		 * <pre>
		 * 参考{@link com.kola.kmp.protocol.reward.KRewardProtocol#MSG_STRUCT_COMMON_REWARD}
		 * 
		 * @param msg
		 * @author CamusHuang
		 * @creation 2013-12-8 下午5:54:28
		 * </pre>
		 */
		public void packMsg(KGameMessage msg) {
			baseRewardData.packMsg(msg);
		}
	}

	/**
	 * <pre>
	 * 对应《奖励表头-邮件》
	 * 
	 * @author CamusHuang
	 * @creation 2013-12-24 下午5:44:20
	 * </pre>
	 */
	public final static class BaseMailRewardDataForJobs {

		private Map<Byte, BaseMailRewardData> datasMap = new HashMap<Byte, BaseMailRewardData>();

		public BaseMailRewardDataForJobs(int id, BaseMailContent baseMail, BaseRewardDataForJobs baseRewardData) {

			for (KJobTypeEnum job : KJobTypeEnum.values()) {
				datasMap.put(job.getJobType(), new BaseMailRewardData(id, baseMail, baseRewardData.getBaseRewardData(job.getJobType())));
			}
		}

		/**
		 * <pre>
		 * 加载一行数据
		 * 
		 * @param row
		 * @param isCheckEffect 是否检查有无奖励，true且没有奖励数据时会抛异常
		 * @return
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2014-1-9 上午11:45:56
		 * </pre>
		 */
		public static BaseMailRewardDataForJobs loadData(KGameExcelRow row, boolean isCheckEffect) throws Exception {

			int id = 0;
			if (row.containsCol("id")) {
				id = row.getInt("id");
			}

			BaseMailContent baseMail = BaseMailContent.loadData(row);
			if (baseMail == null) {
				throw new Exception("缺少邮件内容");
			}

			BaseRewardDataForJobs baseRewardData = BaseRewardDataForJobs.loadData(row, isCheckEffect);

			BaseMailRewardDataForJobs result = new BaseMailRewardDataForJobs(id, baseMail, baseRewardData);
			return result;
		}

		public void checkEffect() throws Exception {
			for (KJobTypeEnum job : KJobTypeEnum.values()) {
				datasMap.get(job.getJobType()).checkEffect();
			}
		}

		public boolean checkIsEffect() {
			boolean isEffect = true;
			for (KJobTypeEnum job : KJobTypeEnum.values()) {
				if (!datasMap.get(job.getJobType()).checkIsEffect()) {
					isEffect = false;
				}
			}

			return isEffect;
		}

		/**
		 * <pre>
		 * 验证所有数据
		 * 
		 * @throws KGameServerException
		 * @author CamusHuang
		 * @creation 2013-1-4 下午8:26:02
		 * </pre>
		 */
		public void notifyCacheLoadComplete() throws Exception {
			for (KJobTypeEnum job : KJobTypeEnum.values()) {
				try {
					datasMap.get(job.getJobType()).notifyCacheLoadComplete();
				} catch (Exception e) {
					throw new Exception("职业类型=" + job.getJobType() + " " + e.getMessage(), e);
				}
			}
		}

		/**
		 * <pre>
		 * 发送奖励
		 * 尝试直接发放奖励，方法内部会检测背包是否满，已满则通过邮件发送
		 * 
		 * @param role
		 * @param presentPointTypeEnum 如果包含点数，则最好传入加点类型，否则使用默认类型
		 * @param isTryDirect 是否尝试直接发放奖励
		 * @return
		 * @author CamusHuang
		 * @creation 2014-4-16 上午11:36:53
		 * </pre>
		 */
		public RewardResult_SendMail sendReward(KRole role, PresentPointTypeEnum presentType, boolean isTryDirect) {
			BaseMailRewardData baseMail = datasMap.get(role.getJob());
			return baseMail.sendReward(role, presentType, isTryDirect);
		}

		public String toCVSString(byte job) {
			BaseMailRewardData baseMail = datasMap.get(job);
			return baseMail.toCVSString();
		}

		/**
		 * <pre>
		 * 参考{@link com.kola.kmp.protocol.reward.KRewardProtocol#MSG_STRUCT_COMMON_REWARD}
		 * 
		 * @param msg
		 * @author CamusHuang
		 * @creation 2013-12-8 下午5:54:28
		 * </pre>
		 */
		public void packMsg(byte job, KGameMessage msg) {
			BaseMailRewardData baseMail = datasMap.get(job);
			baseMail.packMsg(msg);
		}
	}

	/**
	 * <pre>
	 * 每日邮件
	 * 
	 * @author CamusHuang
	 * @creation 2014-1-6 下午8:43:20
	 * </pre>
	 */
	public static class DialyMailData {

		public CommonActivityTime time;
		public final int minRoleLv;// 最小角色等级
		public final int maxRoleLv;// 最大角色等级
		public final BaseMailRewardData mail;

		DialyMailData(CommonActivityTime time, int minRoleLv, int maxRoleLv, BaseMailRewardData mail) {
			this.time = time;
			this.minRoleLv = minRoleLv;
			this.maxRoleLv = maxRoleLv;
			this.mail = mail;
		}

		public void notifyCacheLoadComplete() throws Exception {
			mail.notifyCacheLoadComplete();
		}
	}

	/**
	 * <pre>
	 * 封测期间角色升级奖励
	 * 
	 * @author CamusHuang
	 * @creation 2014-8-11 上午11:30:15
	 * </pre>
	 */
	public static class FengceRoleUplvRewardData {

		public final int lv;// 角色等级
		public final BaseMailRewardData reward;

		FengceRoleUplvRewardData(int lv, BaseMailRewardData reward) {
			this.lv = lv;
			this.reward = reward;
		}

		public void notifyCacheLoadComplete() throws Exception {
			reward.notifyCacheLoadComplete();
		}
	}

	/**
	 * <pre>
	 * 封测期间角色好友副本波数奖励
	 * 
	 * @author CamusHuang
	 * @creation 2014-8-11 上午11:30:15
	 * </pre>
	 */
	public static class FengceFriendFubenRewardData {

		public final int GetQuantity;// 副本达到波数
		public final BaseMailRewardData mailReward;

		FengceFriendFubenRewardData(int GetQuantity, BaseMailRewardData mailReward) {
			this.GetQuantity = GetQuantity;
			this.mailReward = mailReward;
		}

		public void notifyCacheLoadComplete() throws Exception {
			mailReward.notifyCacheLoadComplete();
		}
	}

	/**
	 * <pre>
	 * 封测期间竞技场排名奖励
	 * 
	 * @author CamusHuang
	 * @creation 2014-8-11 上午11:30:15
	 * </pre>
	 */
	public static class FengceCompetitionRewardData {

		public final int ranking;// 达到此排名的奖励
		public final BaseMailContent mail;
		public final BaseRewardData reward;

		FengceCompetitionRewardData(int ranking, BaseMailContent mail, BaseRewardData reward) {
			this.ranking = ranking;
			this.mail = mail;
			this.reward = reward;
		}
	}

	/**
	 * <pre>
	 * 导出名单的时间表
	 * 
	 * @author CamusHuang
	 * @creation 2014-9-20 下午3:20:30
	 * </pre>
	 */
	public static class FengceSaveOnlineRoleData {
		// ----------以下是EXCEL表格直导数据---------
		private String dateTime;// 导出时间
		int minLv;// 最小等级
		int maxLv;// 最大等级

		// ----------以下是逻辑数据---------
		long dateTimeInMills;
		boolean isOpen = true;// 是否继续有效，若为false则时效任务会忽略执行

		void notifyCacheLoadComplete() throws Exception {

			dateTimeInMills = UtilTool.DATE_FORMAT.parse(dateTime).getTime();

			if (minLv > maxLv || maxLv < 1 || minLv > 100) {
				throw new Exception("等级设置错误");
			}
		}
	}

	/**
	 * <pre>
	 * 维护奖励
	 * 
	 * @author CamusHuang
	 * @creation 2014-1-6 下午8:43:20
	 * </pre>
	 */
	public static class ShutdownRewardData {

		public final int minRoleLv;// 最小角色等级
		public final int maxRoleLv;// 最大角色等级
		public final BaseMailRewardData mail;

		ShutdownRewardData(int minRoleLv, int maxRoleLv, BaseMailRewardData mail) {
			this.mail = mail;
			this.minRoleLv = minRoleLv;
			this.maxRoleLv = maxRoleLv;
		}

		public void notifyCacheLoadComplete() throws Exception {
			mail.notifyCacheLoadComplete();
		}
	}
	
	/**
	 * <pre>
	 * 卡机补偿
	 * 
	 * @author CamusHuang
	 * @creation 2015-2-3 下午3:17:21
	 * </pre>
	 */
	public static class KAJIRewardData {

		public final int minRoleLv;// 最小角色等级
		public final int maxRoleLv;// 最大角色等级
		public final long createStartTime	;//	角色创建起始时间
		public final long createEndTime	;//	角色创建结束时间
		public final long effectStartTime	;//	奖励生效起始时间
		public final long effectEndTime	;//	奖励生效结束时间
		//
		public final BaseMailRewardData mail;

		KAJIRewardData(int minRoleLv, int maxRoleLv, long createStartTime, long createEndTime, long effectStartTime, long effectEndTime, BaseMailRewardData mail) {
			this.minRoleLv = minRoleLv;
			this.maxRoleLv = maxRoleLv;
			this.createStartTime = createStartTime;
			this.createEndTime = createEndTime;
			this.effectStartTime = effectStartTime;
			this.effectEndTime = effectEndTime;
			this.mail = mail;
		}



		public void notifyCacheLoadComplete() throws Exception {
			mail.notifyCacheLoadComplete();
		}
	}	
	
	/**
	 * <pre>
	 * 免费领取体力数据
	 * 
	 * @author CamusHuang
	 * @creation 2015-1-26 下午4:44:03
	 * </pre>
	 */
	public static class PhyPowerRewardData implements Comparable<PhyPowerRewardData>{
		// ----------以下是EXCEL表格直导数据---------
		private String startAndEndTimeStr;// 活动开始与结束时间点
		public int PhysicalCount;// 体力量
		
		// ----------以下是逻辑数据---------
		public TimeIntervalStruct timeInterval;

		public void notifyCacheLoadComplete() throws Exception {
			
			{
				String[] temp = startAndEndTimeStr.split("-");
				if (temp == null || temp.length != 2) {
					throw new KGameServerException("格式错误，str=" + startAndEndTimeStr);
				}
				long beginTime = UtilTool.parseHHmmToMillis(temp[0]);
				long endTime = UtilTool.parseHHmmToMillis(temp[1]);

				if (beginTime >= endTime) {
					throw new KGameServerException("beginTime >= endTime，str=" + startAndEndTimeStr);
				}
				timeInterval = new TimeIntervalStruct(temp[0], temp[1], beginTime, endTime);
			}
		}

		@Override
		public int compareTo(PhyPowerRewardData o) {
			return timeInterval.compareTo(o.timeInterval);
		}
	}

	// /**
	// * <pre>
	// * 寻宝奖励分组
	// *
	// * @author CamusHuang
	// * @creation 2013-9-14 下午2:53:41
	// * </pre>
	// */
	// public static class XunbaoGroup {
	// final int groupId;
	// final List<XunbaoReward> rewardList;
	//
	// XunbaoGroup(int groupId, List<XunbaoReward> rewardList) {
	// this.groupId = groupId;
	// this.rewardList = Collections.unmodifiableList(rewardList);
	// }
	// }
	// /**
	// * <pre>
	// * 寻宝奖励项内容
	// *
	// * @author CamusHuang
	// * @creation 2013-9-14 下午2:55:40
	// * </pre>
	// */
	// public static class XunbaoReward {
	// // 数据项品质 道具编码 道具数量 货币类型 货币数量 修为
	// // quality itemCode1 itemCount1 moneyType1 moneyCount1 xiuwei
	//
	// final KGameItemQualityEnum quality;
	// final ItemCountStruct3 itemStruct3;
	// final KCurrencyCountStruct money;
	// final List<XunbaoReward> rewardList;
	//
	//
	// }
}
