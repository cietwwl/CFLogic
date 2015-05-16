package com.kola.kmp.logic.support;

import java.util.List;

import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.flowdata.impl.UsePointFunctionTypeEnum;
import com.kola.kmp.logic.currency.KCurrencyDataManager.ChargeInfoManager.ChargeInfoStruct;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.util.ResultStructs.MoneyResult_ExchangeGold;

public interface CurrencyModuleSupport {

	/**
	 * <pre>
	 * 查货币总数量
	 * PS：支持点数
	 * 
	 * @param roleId
	 * @param currencyType
	 * @return
	 * @author CamusHuang
	 * @creation 2012-12-6 下午7:42:32
	 * </pre>
	 */
	public long getMoney(long roleId, KCurrencyTypeEnum currencyType);

	/**
	 * <pre>
	 * 检查角色的货币金额，是否比checkValues中的值要大
	 * 如果有一个不符合，即返回false
	 * 
	 * </pre>
	 * 
	 * @param roleId
	 * @param checkValues
	 * @return 如果有货币不足，则返回相应货币；全部充足则返回null
	 * @return
	 */
	public KCurrencyCountStruct checkMoneysEnought(long roleId, List<KCurrencyCountStruct> checkValues);

	/**
	 * <pre>
	 * 添加货币
	 * 
	 * @param roleId
	 * @param currencyType 货币类型
	 * @param changeValue 正数
	 * @param type 当货币类型是元宝时，必须不为NULL
	 * @param isSyn 是否同步最新货币金额给客户端
	 * @return 最终值；-1表示金额不足
	 * @author CamusHuang
	 * @creation 2013-8-15 下午6:37:20
	 * </pre>
	 */
	public long increaseMoney(long roleId, KCurrencyTypeEnum currencyType, long changeValue, PresentPointTypeEnum type, boolean isSyn);

	/**
	 * <pre>
	 * 减少货币
	 * 
	 * @param roleId
	 * @param currencyType 货币类型
	 * @param changeValue 正数
	 * @param type 当货币类型是元宝时，必须不为NULL
	 * @return 最终值；-1表示金额不足
	 * @author CamusHuang
	 * @creation 2013-6-15 上午10:52:09
	 * </pre>
	 */
	public long decreaseMoney(long roleId, KCurrencyTypeEnum currencyType, long changeValue, UsePointFunctionTypeEnum type, boolean isSyn);

	/**
	 * <pre>
	 * 添加货币
	 * 
	 * @param roleId
	 * @param changeValue
	 * @param type 当货币类型是元宝时，必须不为NULL
	 * @return 最终值；-1表示金额不足
	 * @author CamusHuang
	 * @creation 2013-6-15 上午10:52:09
	 * </pre>
	 */
	public long increaseMoney(long roleId, KCurrencyCountStruct changeValue, PresentPointTypeEnum type, boolean isSyn);

	/**
	 * <pre>
	 * 减少货币
	 * 
	 * @param roleId
	 * @param changeValue
	 * @param type 当货币类型是元宝时，必须不为NULL
	 * @return 最终值；-1表示金额不足
	 * @author CamusHuang
	 * @creation 2013-6-15 上午10:52:09
	 * </pre>
	 */
	public long decreaseMoney(long roleId, KCurrencyCountStruct changeValue, UsePointFunctionTypeEnum type, boolean isSyn);

	/**
	 * <pre>
	 * 变更货币数量
	 * 
	 * @param roleId
	 * @param changeValues 正数表示增加，负数表示减少
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-13 下午8:46:16
	 * </pre>
	 */
	public boolean increaseMoneys(long roleId, List<KCurrencyCountStruct> changeValues, PresentPointTypeEnum type, boolean isSyn);

	/**
	 * <pre>
	 * 变更货币数量
	 * 
	 * @param roleId
	 * @param changeValues 正数表示增加，负数表示减少
	 * @return 如果有货币不足，则返回相应货币的类型；全部充足则返回null
	 * @author CamusHuang
	 * @creation 2012-11-13 下午8:46:16
	 * </pre>
	 */
	public KCurrencyCountStruct decreaseMoneys(long roleId, List<KCurrencyCountStruct> changeValues, UsePointFunctionTypeEnum type, boolean isSyn);

	/**
	 * <pre>
	 * 兑换金币
	 * 
	 * @param roleId
	 * @param diamond
	 * @return 
	 * @author CamusHuang
	 * @creation 2014-3-20 下午3:53:56
	 * </pre>
	 */
	public MoneyResult_ExchangeGold dealMsg_exchangeGold(KRole role, int diamond);

	/**
	 * <pre>
	 * 同步货币金额给客户端
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2014-3-5 上午11:07:09
	 * </pre>
	 */
	public void synCurrencyDataToClient(long roleId);

	/**
	 * <pre>
	 * 月卡加时
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-10-8 下午5:48:38
	 * </pre>
	 */
	public void notifyMonthCardAddTime(KRole role, ChargeInfoStruct chargeData);
	
	/**
	 * <pre>
	 * 月卡结束时间
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-10-8 下午5:48:38
	 * </pre>
	 */
	public String getMonthCardEndTime(KRole role);

	/**
	 * <pre>
	 * 克隆角色数据
	 * 
	 * @param myRole
	 * @param srcRole
	 * @author CamusHuang
	 * @creation 2014-10-15 上午11:50:31
	 * </pre>
	 */
	public void cloneRoleByCamus(KRole myRole, KRole srcRole);
}
