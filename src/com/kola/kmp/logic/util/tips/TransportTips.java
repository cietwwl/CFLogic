package com.kola.kmp.logic.util.tips;

import com.koala.game.util.StringUtil;

public class TransportTips {
	private static String _tipsNotOpen;// 该功能未开放
	private static String _tipsTransporterNotFound; // 运输者不存在
	private static String _tipsTransporterMaxInterceptCount; // 运输者已超过最大被拦截次数
	private static String _tipsMaxInterceptCount; // 已达到最大拦截次数
	private static String _tipsIsTransporting; // 已经在运送中
	private static String _tipsMaxTransportCount; // 已达到最大运输次数
	private static String _tipsCannotInterceptCooling; // 拦截时间冷却中
	private static String _tipsReflashCarrier;
	private static String _tipsReflashCarrierNotEnoughIgot;// 刷新载具元宝不足
	private static String _tipsClearCoolTime;
	private static String _tipsClearCoolTimeNotEnoughIgot;// 清除冷却时间元宝不足
	private static String _tipsTransportMailTitle;
	private static String _tipsTransportMailContent;
	private static String _tipsFinishTransport;
	private static String _tipsTransportStrat;
	private static String _tipsReflashCarrierSuccess;
	private static String _tipsInterceptSuccessInfo;
	private static String _tipsInterceptFaildInfo;
	private static String _tipsBeInterceptSuccessInfo;
	private static String _tipsBeInterceptFaildInfo;

	public static String getTipsNotOpen() {
		return _tipsNotOpen;
	}

	public static String getTipsTransporterNotFound() {
		return _tipsTransporterNotFound;
	}

	public static String getTipsTransporterMaxInterceptCount(int maxCount) {
		return StringUtil.format(_tipsTransporterMaxInterceptCount, maxCount);
	}

	public static String getTipsMaxInterceptCount(int maxCount) {
		return StringUtil.format(_tipsMaxInterceptCount, maxCount);
	}

	public static String getTipsIsTransporting() {
		return _tipsIsTransporting;
	}

	public static String getTipsMaxTransportCount(int maxCount) {
		return StringUtil.format(_tipsMaxTransportCount, maxCount);
	}

	public static String getTipsCannotInterceptCooling(int point) {
		return StringUtil.format(_tipsCannotInterceptCooling, point);
	}

	public static String getTipsReflashCarrier(long point) {
		return StringUtil.format(_tipsReflashCarrier, point);
	}

	public static String getTipsReflashCarrierNotEnoughIgot(long point,
			String curName) {
		return StringUtil.format(_tipsReflashCarrierNotEnoughIgot, point,
				curName, curName);
	}

	public static String getTipsClearCoolTime(int point) {
		return StringUtil.format(_tipsClearCoolTime, point);
	}

	public static String getTipsClearCoolTimeNotEnoughIgot(int point) {
		return StringUtil.format(_tipsClearCoolTimeNotEnoughIgot, point);
	}

	public static String getTipsTransportMailTitle() {
		return _tipsTransportMailTitle;
	}

	public static String getTipsTransportMailContent(String carrierName,
			int interceptCount) {
		return StringUtil.format(_tipsTransportMailContent, carrierName,
				interceptCount);
	}

	public static String getTipsFinishTransport() {
		return _tipsFinishTransport;
	}

	public static String getTipsTransportStrat(String carrierName,
			String endTime) {
		return StringUtil.format(_tipsTransportStrat, carrierName, endTime);
	}

	public static String getTipsReflashCarrierSuccess(String carrierName) {
		return StringUtil.format(_tipsReflashCarrierSuccess, carrierName);
	}

	public static String getTipsInterceptSuccessInfo(String time,
			String otherRoleName, String carrierName, int exp, int potential) {
		return StringUtil.format(_tipsInterceptSuccessInfo, time,
				otherRoleName, carrierName, exp, potential);
	}

	public static String getTipsInterceptFaildInfo(String time,
			String otherRoleName, String carrierName) {
		return StringUtil.format(_tipsInterceptFaildInfo, time, otherRoleName,
				carrierName);
	}

	public static String getTipsBeInterceptSuccessInfo(String time,
			String otherRoleName, String carrierName) {
		return StringUtil.format(_tipsBeInterceptSuccessInfo, time,
				otherRoleName, carrierName);
	}

	public static String getTipsBeInterceptFaildInfo(String time,
			String otherRoleName, String carrierName, int exp, int potential) {
		return StringUtil.format(_tipsBeInterceptFaildInfo, time,
				otherRoleName, carrierName, exp, potential);
	}

}
