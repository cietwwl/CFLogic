package com.kola.kmp.logic.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KEquipmentTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;

/**
 * <pre>
 * CEND 道具--道具模块配置表
 * 
 * 
 * @author CamusHuang
 * @creation 2012-11-14 下午4:10:27
 * </pre>
 */
public class KItemConfig {

	/** 默认的，属于主角色的装备栏ID */
	public static final long MAIN_BODYSLOT_ID = -1;
	
	public static final int FixeBOx_RewardType_NONE = 0;// 0表示未进行确认，1表示选择离线奖励，2表示选择在线奖励
	public static final int FixeBOx_RewardType_OFFLINE = 1;// 0表示未进行确认，1表示选择离线奖励，2表示选择在线奖励
	public static final int FixeBOx_RewardType_ONLINE = 2;// 0表示未进行确认，1表示选择离线奖励，2表示选择在线奖励

	private static KItemConfig instance;
	//
	public final int TotalMaxEquiNum = KEquipmentTypeEnum.values().length;// 一个装备栏最多装备10件装备
	public final int MaxEnchansePositionPerOne = 4;// 每件装备最大镶嵌孔位数量
	public final int TotalMaxEnchansePosition = TotalMaxEquiNum * MaxEnchansePositionPerOne;// 一个装备栏最多镶嵌宝石数量
	public final int EquiStarBigLv = 6;// 装备每6星为一阶
	//
	public final KCurrencyTypeEnum strongPayType = KCurrencyTypeEnum.GOLD;
	public final int StrongLvAddMax;// 强化等级比角色等级最高高多少级
	// 镶嵌孔扩容价格
	// 第2个孔位的价格系数：装备等级*500金币
	public final KCurrencyCountStruct ExtEnchanse2PriceRate = new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, 500);
	// 第3个凹槽开启需要5钻石
	public final KCurrencyCountStruct ExtEnchanse3Price = new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, 5);
	// 第4个凹槽开启需要10钻石
	public final KCurrencyCountStruct ExtEnchanse4Price = new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, 10);
	//
	// public final int EquiInheritMaxStrongLv = 10;// 装备继承时源装备最大强化等级

	// 新角色初始默认物品
	public static final List<ItemCountStruct> NewRoleItems = new ArrayList<ItemCountStruct>();

	private KItemConfig(Element logicE) throws Exception {
		StrongLvAddMax = Integer.parseInt(logicE.getChildTextTrim("StrongLvAddMax"));

		{
			Map<String, ItemCountStruct> moneys = new HashMap<String, ItemCountStruct>();
			for (Object obj : logicE.getChild("NEW_ROLE_ITEM").getChildren("Item")) {
				Element tempe = (Element) obj;
				String itemCode = tempe.getAttributeValue("itemCode");
				ItemCountStruct struct = new ItemCountStruct(itemCode, Long.parseLong(tempe.getText()));

				if (struct.itemCount < 1) {
					throw new Exception("新角色 默认物品数量错误 id=" + itemCode);
				}
				if (moneys.put(itemCode, struct) != null) {
					throw new Exception("新角色 默认物品id重复 id=" + itemCode);
				}
			}
			NewRoleItems.addAll(moneys.values());
		}
	}

	public static void init(Element logicE) throws KGameServerException {
		try {
			instance = new KItemConfig(logicE);
		} catch (KGameServerException e) {
			throw e;
		} catch (Exception e) {
			throw new KGameServerException("道具模块配置表异常：" + e.getMessage(), e);
		}
	}

	public static KItemConfig getInstance() {
		return instance;
	}

	static void onGameWorldInitComplete() throws KGameServerException {
		try {
			if (NewRoleItems != null && !NewRoleItems.isEmpty()) {
				for (ItemCountStruct data : NewRoleItems) {
					if (data.getItemTemplate() == null) {
						throw new Exception("新手礼包不存在 id=" + data.itemCode);
					}
				}
			}
		} catch (KGameServerException e) {
			throw e;
		} catch (Exception e) {
			throw new KGameServerException("道具模块配置表异常：" + e.getMessage(), e);
		}
	}
}
