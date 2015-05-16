package com.kola.kmp.logic.flow;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_AddItem;

/**
 * <pre>
 * 1)	对于实体资产（非货币）：资产获得时，记录一条；资产失去时，记录一条。
 * 2)	对于非实体资产（货币）：添加货币时，记录一条，注明来源；使用货币时，记录一条，并注明原因。
 * 3)	实体资产流向记录：在获得及失去时，需记录原因。
 * 4)	实体资产修改记录：实体资产发生属性发生变化时，添加一条修改记录（例如道具的升星、镶嵌、随从强化）。
 * 5)	实体资产的获得以及流出记录，独立一张表记录。此处可用于查询，玩家是否拥有过该资产，以及该资产是否流出过，用于处理玩家投诉的丢失问题。
 * 6)	实体资产的修改，独立一张表记录。此处可用于查询，玩家的资产的改造情况，用于处理玩家投诉的回档问题。
 * 7)	货币资产，独立一张表记录，记录的数据是，收入以及使用情况。此处可用于查询玩家的货币情况，亦可作为不明来源货币的查询记录。
 * 8)	经验流水表，用于记录角色的经验增加记录。经验在游戏中是比较敏感的数据，所以需要记录
 * 9)	其他流水表，用于记录游戏当中，比较敏感，但是不固定的流水记录。
 * 
 * 
 * @author CamusHuang
 * @creation 2014-8-16 下午10:10:18
 * </pre>
 */
public class FlowManager {

	public static final Logger _LOGGER = KGameLogger.getLogger("propertyFlow");

	// private static final String PropertyAddOrDeleteTemp =
	// "logType={},roleId={},uuid={},propertyType={},propertyName={},isAdd={},tips={}";
	private static final String PropertyAddOrDeleteTemp = "logType={},propertyType={},propertyName={},roleId={},uuid={},tempId={},tempName={},isAdd={},tips={}";
	// private static final String PropertyModifyTemp =
	// "logType={},roleId={},uuid={},propertyType={},propertyName={},tips={}";
	private static final String PropertyModifyTemp = "logType={},propertyType={},propertyName={},roleId={},uuid={},tempId={},tempName={},tips={}";
	private static final String MoneyTemp = "logType={},roleId={},moneyType={},count={},isAdd={},tips={}";
	private static final String ExpTemp = "logType={},roleId={},addExp={},tips={}";
	// private static final String OtherTemp =
	// "logType={},otherType={},roleId={},logName={},tips={}";
	private static final String OtherTemp = "logType={},otherType={},otherTypeName={},roleId={},tips={}";

	/**
	 * <pre>
	 * 资产类型
	 * 由记录流水的开发人员协调增加枚举类型
	 * 
	 * @author CamusHuang
	 * @creation 2014-8-18 上午10:18:07
	 * </pre>
	 */
	public enum PropertyTypeEnum {
		道具(1), //
		时装(2), //
		坐驾(3), //
		宠物(4), //
		技能(5), //
		;

		// 标识数值
		public final int sign;

		private PropertyTypeEnum(int sign) {
			this.sign = sign;
		}
	}

	/**
	 * <pre>
	 * 基础流水类型
	 * 由记录流水的开发人员协调增加枚举类型
	 * 
	 * @author CamusHuang
	 * @creation 2014-8-18 上午10:18:07
	 * </pre>
	 */
	public enum BaseFlowTypeEnum {
		货币增减(11), //
		财产增减(12), //
		财产修改(13), //
		经验(14), //
		其它(15), //
		;

		// 标识数值
		public final int sign;

		private BaseFlowTypeEnum(int sign) {
			this.sign = sign;
		}
	}

	/**
	 * <pre>
	 * 其它流水类型
	 * 由记录流水的开发人员协调增加枚举类型
	 * 
	 * @author CamusHuang
	 * @creation 2014-8-18 上午10:18:07
	 * </pre>
	 */
	public enum OtherFlowTypeEnum {
		背包扩容(1), //
		资源战竞价(2), //
		买随机物品(3), //
		登陆奖励大奖(4), //
		登陆奖励小奖(5), //
		删除附件邮件(6), //
		VIP充值处理(7), //
		退出军团(8), //
		被开除出军团(9), //
		转让团长(10), //
		自动转让团长(11), //
		删除角色转让团长(12), //
		军团科技升级(13), //
		军团升级(14), //
		军团解散(15), //
		天赋(16),
		战队竞技(17),
		角色升级(18),
		体力恢复(19),
		月卡加时(20),
		兑换送金币限时活动(21),
		买热购商品(22), //
		充值优惠限时活动(23),
		示例(99), ;

		// 标识数值
		public final int sign;

		private OtherFlowTypeEnum(int sign) {
			this.sign = sign;
		}
	}

	/**
	 * <pre>
	 * 资产出入记录
	 * 
	 * @param roleId
	 * @param UUID 资产的UUID，流水表将会以UUID为区分
	 * @param propertyType 用于区分是哪种资产（道具、宠物）
	 * @param isAdd 用于标识是流入还是流出
	 * @param tips 本条流水记录的一些备注信息（可以是数据描述）---不能包含,=号
	 * @author CamusHuang
	 * @creation 2014-8-18 上午10:15:51
	 * </pre>
	 */
	public static void logPropertyAddOrDelete(long roleId, PropertyTypeEnum propertyType, String UUID, Object tempId, String tempName, boolean isAdd, String tips) {
		_LOGGER.warn(StringUtil.format(PropertyAddOrDeleteTemp, BaseFlowTypeEnum.财产增减.sign, propertyType.sign, propertyType.name(), roleId, UUID, tempId, tempName, isAdd ? 1 : 0, tips));
	}

	/**
	 * <pre>
	 * 资产出入记录
	 * 道具资产新增记录
	 * 
	 * @param roleId
	 * @param addItemResult 加道具结果
	 * @param itemCounts 加道具数量，null表示每件加1
	 * @param sourceTips 本条流水记录的一些备注信息（可以是数据描述）---不能包含,=号
	 * @author CamusHuang
	 * @creation 2014-8-18 下午5:40:41
	 * </pre>
	 */
	public static void logPropertyAdd(long roleId, ItemResult_AddItem addItemResult, List<ItemCountStruct> itemCounts, String sourceTips) {
		if (addItemResult == null) {
			return;
		}
		// 财产日志
		Map<String, ItemCountStruct> structMap = ItemCountStruct.changeItemStruct(itemCounts);
		if (addItemResult.newItemList != null) {
			for (KItem item : addItemResult.newItemList) {
				ItemCountStruct struct = structMap.get(item.getItemCode());
				long addCount = struct == null ? 1 : struct.itemCount;
				KItemTempAbs temp = item.getItemTemplate();
				logPropertyAddOrDelete(roleId, PropertyTypeEnum.道具, item.getUUID(), temp.itemCode, temp.name, true, sourceTips + ";增加数量:" + addCount + ";当前数量:" + item.getCount());
			}
		}
		if (addItemResult.updateItemCountList != null) {
			for (KItem item : addItemResult.updateItemCountList) {
				ItemCountStruct struct = structMap.get(item.getItemCode());
				long addCount = struct == null ? 1 : struct.itemCount;
				KItemTempAbs temp = item.getItemTemplate();
				logPropertyAddOrDelete(roleId, PropertyTypeEnum.道具, item.getUUID(), temp.itemCode, temp.name, true, sourceTips + ";增加数量:" + addCount + ";当前数量:" + item.getCount());
			}
		}
	}

	/**
	 * <pre>
	 * 资产出入记录
	 * 道具资产减少记录
	 * 
	 * @param roleId
	 * @param addItemResult 加道具结果
	 * @param itemCounts 加道具数量，null表示每件加1
	 * @param sourceTips 本条流水记录的一些备注信息（可以是数据描述）---不能包含,=号
	 * @author CamusHuang
	 * @creation 2014-8-18 下午5:40:41
	 * </pre>
	 */
	public static void logPropertyDelete(long roleId, KItem item, long deleteCount, String sourceTips) {
		if (item == null) {
			return;
		}
		// 财产日志
		logPropertyAddOrDelete(roleId, PropertyTypeEnum.道具, item.getUUID(), item.getItemTemplate().itemCode, item.getItemTemplate().name, false,
				sourceTips + ";减少数量:" + deleteCount + ";当前数量:" + item.getCount());
	}

	/**
	 * <pre>
	 * 资产修改记录
	 * 
	 * @param roleId
	 * @param UUID 资产的唯一UUID
	 * @param tips 修改的内容---不能包含,=号
	 * @author CamusHuang
	 * @creation 2014-8-18 上午10:14:23
	 * </pre>
	 */
	public static void logPropertyModify(long roleId, PropertyTypeEnum propertyType, String UUID, Object tempId, String tempName, String tips) {
		_LOGGER.warn(StringUtil.format(PropertyModifyTemp, BaseFlowTypeEnum.财产修改.sign, propertyType.sign, propertyType.name(), roleId, UUID, tempId, tempName, tips));
	}

	/**
	 * <pre>
	 * 货币流水记录
	 * 
	 * @param roleId
	 * @param moneyType 本条记录的货币类型
	 * @param count 本条流水涉及的数额
	 * @param isAdd 用于标识是流入还是流出
	 * @param tips 使用的备注---不能包含,=号
	 * @author CamusHuang
	 * @creation 2014-8-18 上午10:13:09
	 * </pre>
	 */
	public static void logMoney(long roleId, KCurrencyTypeEnum moneyType, long count, boolean isAdd, String tips) {
		_LOGGER.warn(StringUtil.format(MoneyTemp, BaseFlowTypeEnum.货币增减.sign, roleId, moneyType.sign, count, isAdd ? 1 : 0, tips));
	}

	/**
	 * <pre>
	 * 经验流水记录
	 * 
	 * @param roleId
	 * @param addExp
	 * @param tips 本次增加经验的备注信息（例如来源）---不能包含,=号
	 * @author CamusHuang
	 * @creation 2014-8-18 上午10:11:06
	 * </pre>
	 */
	public static void logExp(long roleId, int addExp, String tips) {
		_LOGGER.warn(StringUtil.format(ExpTemp, BaseFlowTypeEnum.经验.sign, roleId, addExp, tips));
	}

	/**
	 * <pre>
	 * 其它流水记录
	 * 
	 * @param roleId
	 * @param logType 自定义的类型
	 * @param tips 针对于这条流水的详细记录---不能包含,=号
	 * @author CamusHuang
	 * @creation 2014-8-16 下午10:08:50
	 * </pre>
	 */
	public static void logOther(long roleId, OtherFlowTypeEnum logType, String tips) {
		_LOGGER.warn(StringUtil.format(OtherTemp, BaseFlowTypeEnum.其它.sign, logType.sign, logType.name(), roleId, tips));
	}

}
