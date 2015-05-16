package com.kola.kmp.logic.other;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kola.kgame.cache.currency.CurrencyCountStruct;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;

/**
 * <pre>
 * 货币及其数量
 * 
 * @author CamusHuang
 * @creation 2012-11-9 上午10:57:34
 * </pre>
 */
public class KCurrencyCountStruct implements CurrencyCountStruct {
	/** 货币类型 */
	public final KCurrencyTypeEnum currencyType;
	/** 货币数量 正数 */
	public final long currencyCount;

	public KCurrencyCountStruct(KCurrencyTypeEnum currencyType, long currencyCount) {
		this.currencyType = currencyType;
		this.currencyCount = currencyCount;
	}

	@Override
	public byte getCurrencyType() {
		return currencyType.sign;
	}

	@Override
	public long getCount() {
		return currencyCount;
	}

	/**
	 * <pre>
	 * 将货币结构数组中货币类型重复的进行合并
	 * 
	 * @param structs
	 * @return
	 * @author CamusHuang
	 * @creation 2012-12-10 下午7:48:49
	 * </pre>
	 */
	public static List<KCurrencyCountStruct> mergeCurrencyCountStructs(KCurrencyCountStruct... structs) {
		if (structs == null || structs.length < 1) {
			return Collections.emptyList();
		}
		//
		Map<KCurrencyTypeEnum, Long> map = new HashMap<KCurrencyTypeEnum, Long>();
		for (KCurrencyCountStruct struct : structs) {
			if (struct == null || struct.currencyCount < 1) {
				continue;
			}

			Long old = map.get(struct.currencyType);
			if (old == null) {
				map.put(struct.currencyType, struct.currencyCount);
			} else {
				map.put(struct.currencyType, struct.currencyCount + old);
			}
		}
		//
		if (map.size() == structs.length) {
			return Arrays.asList(structs);
		}
		//
		if (map.isEmpty()) {
			return Collections.emptyList();
		}
		//
		List<KCurrencyCountStruct> list = new ArrayList<KCurrencyCountStruct>();
		for (KCurrencyTypeEnum type : map.keySet()) {
			list.add(new KCurrencyCountStruct(type, map.get(type)));
		}
		return list;
	}

	/**
	 * <pre>
	 * 将货币结构数组中货币类型重复的进行合并
	 * 
	 * @param structs
	 * @return
	 * @author CamusHuang
	 * @creation 2012-12-10 下午7:48:49
	 * </pre>
	 */
	public static List<KCurrencyCountStruct> mergeCurrencyCountStructs(List<KCurrencyCountStruct> structs) {
		if (structs == null || structs.size() < 1) {
			return Collections.emptyList();
		}
		//
		Map<KCurrencyTypeEnum, Long> map = new HashMap<KCurrencyTypeEnum, Long>();
		for (KCurrencyCountStruct struct : structs) {
			if (struct == null || struct.currencyCount < 1) {
				continue;
			}

			Long old = map.get(struct.currencyType);
			if (old == null) {
				map.put(struct.currencyType, struct.currencyCount);
			} else {
				map.put(struct.currencyType, struct.currencyCount + old);
			}
		}
		//
		if (map.size() == structs.size()) {
			return structs;
		}
		//
		if (map.isEmpty()) {
			return Collections.emptyList();
		}
		//
		List<KCurrencyCountStruct> list = new ArrayList<KCurrencyCountStruct>();
		for (KCurrencyTypeEnum type : map.keySet()) {
			list.add(new KCurrencyCountStruct(type, map.get(type)));
		}
		return list;
	}

	/**
	 * <pre>
	 * 解释货币参数
	 * 货币类型*货币数量*权重
	 * 
	 * @param moneyStrs
	 * @param moneyStructs
	 * @param moneyRates
	 * @param totalRate
	 * @param minCount 货币数量以及权重允许的最小值
	 * @throws Exception
	 * @author CamusHuang
	 * @creation 2014-4-28 下午12:24:56
	 * </pre>
	 */
	public static int paramsMoneys(String[] moneyStrs, List<KCurrencyCountStruct> moneyStructs, List<Integer> moneyRates, int minCount) throws Exception {
		int totalRate = 0;
		if (moneyStrs != null) {
			for (String temp : moneyStrs) {
				String[] temps = temp.split("\\*");
				// 1*100*50
				KCurrencyCountStruct tempS = new KCurrencyCountStruct(KCurrencyTypeEnum.getEnum(Integer.parseInt(temps[0])), Integer.parseInt(temps[1]));

				if (tempS.currencyType == null) {
					throw new Exception("货币类型错误=" + temps[0]);
				}
				if (tempS.currencyCount < minCount) {
					throw new Exception("货币数量错误=" + temps[1]);
				}
				int rate = Integer.parseInt(temps[2]);
				if (rate < minCount) {
					throw new Exception("货币权重错误=" + rate);
				}
				moneyStructs.add(tempS);
				moneyRates.add(rate);
				totalRate += rate;
			}
		}
		return totalRate;
	}

	/**
	 * <pre>
	 * 解释货币参数
	 * 货币类型*货币数量
	 * 
	 * @param moneyStrs
	 * @param moneyStructs
	 * @param minCount 物品数量以及权重允许的最小值
	 * @throws Exception
	 * @author CamusHuang
	 * @creation 2014-4-28 下午12:24:56
	 * </pre>
	 */
	public static void paramsMoneys(String[] moneyStrs, List<KCurrencyCountStruct> moneyStructs, int minCount) throws Exception {
		if (moneyStrs != null) {
			for (String temp : moneyStrs) {
				String[] temps = temp.split("\\*");
				KCurrencyCountStruct tempS = new KCurrencyCountStruct(KCurrencyTypeEnum.getEnum(Integer.parseInt(temps[0])), Integer.parseInt(temps[1]));

				if (tempS.currencyType == null) {
					throw new Exception("货币类型错误=" + temps[0]);
				}
				if (tempS.currencyCount < minCount) {
					throw new Exception("货币数量错误=" + temps[1]);
				}
				moneyStructs.add(tempS);
			}
		}
	}

	/**
	 * <pre>
	 * 有元宝的情况下PresentPointTypeEnum不能为null
	 * 
	 * @param moneyList
	 * @param presentPointTypeEnum
	 * @author CamusHuang
	 * @creation 2014-6-2 下午2:39:02
	 * </pre>
	 */
	public static boolean checkPresentPointTypeCorrect(List<KCurrencyCountStruct> moneyList, PresentPointTypeEnum presentPointTypeEnum) {
		if (moneyList == null || moneyList.isEmpty() || presentPointTypeEnum != null) {
			return true;
		}
		boolean isFindPoint = false;
		for (KCurrencyCountStruct struct : moneyList) {
			if (struct.currencyType == KCurrencyTypeEnum.DIAMOND) {
				isFindPoint = true;
				break;
			}
		}
		if (isFindPoint) {
			return false;
		}
		return true;
	}
}
