package com.kola.kmp.logic.currency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;

/**
 * <pre>
 * 模块配置表
 * 
 * @author CamusHuang
 * @creation 2013-12-8 下午5:08:45
 * </pre>
 */
public class KCurrencyConfig {
	private static KCurrencyConfig instance;

	/** 是否在充值成功时通知客户端 */
	public final boolean IS_PUSH_CHARGERESULT_TO_CLIENT;

	public final long MaxGoldForExchange;// = 500000000;// 金币购买时检查极限数量
	public final int DiamondToGoldBase;// = 10;// 兑换金币的基准钻石数量为10
	public final int DiamondToGoldBuyRate;// = 10000;// 单次兑换金币最多只能10000倍基准钻石数量
	public final long DiamondToGoldMax;// 恒等于DiamondToGoldBuyRate*DiamondToGoldBase;
	// 新角色初始默认货币
	public static final List<KCurrencyCountStruct> NewRoleMoneys = new ArrayList<KCurrencyCountStruct>();// new

	private KCurrencyConfig(Element root) throws KGameServerException {
		// 模块配置表初始化

		IS_PUSH_CHARGERESULT_TO_CLIENT = Boolean.parseBoolean(root.getChildTextTrim("IS_PUSH_CHARGERESULT_TO_CLIENT"));
		{
			Element tempe = root.getChild("GOLD_EXCHANGE");
			MaxGoldForExchange = Long.parseLong(tempe.getChildTextTrim("MaxGoldForExchange"));
			if (MaxGoldForExchange < 1) {
				throw new KGameServerException("新角色 MaxGoldForExchange错误");
			}
			DiamondToGoldBase = Integer.parseInt(tempe.getChildTextTrim("DiamondToGoldBase"));
			if (DiamondToGoldBase < 1) {
				throw new KGameServerException("新角色 DiamondToGoldBase错误");
			}
			DiamondToGoldBuyRate = Integer.parseInt(tempe.getChildTextTrim("DiamondToGoldBuyRate"));
			if (DiamondToGoldBuyRate < 1) {
				throw new KGameServerException("新角色 DiamondToGoldBuyRate错误");
			}
			DiamondToGoldMax = DiamondToGoldBase * DiamondToGoldBuyRate;
		}

		{
			Map<KCurrencyTypeEnum, KCurrencyCountStruct> moneys = new HashMap<KCurrencyTypeEnum, KCurrencyCountStruct>();
			for (Object obj : root.getChild("NEW_ROLE_MONEY").getChildren("Money")) {
				Element tempe = (Element) obj;
				KCurrencyTypeEnum type = KCurrencyTypeEnum.getEnum(Integer.parseInt(tempe.getAttributeValue("type")));
				KCurrencyCountStruct struct = new KCurrencyCountStruct(type, Long.parseLong(tempe.getText()));
				if (struct.currencyCount < 1) {
					throw new KGameServerException("新角色 默认货币数量错误 类型=" + type.name);
				}
				if (moneys.put(type, struct) != null) {
					throw new KGameServerException("新角色 默认货币类型重复");
				}
			}
			NewRoleMoneys.addAll(moneys.values());
		}

	}

	static void init(Element logicE) throws KGameServerException {
		instance = new KCurrencyConfig(logicE);
	}

	public static KCurrencyConfig getInstance() {
		return instance;
	}


}
