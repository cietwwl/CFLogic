package com.kola.kmp.logic.util.tips;

import com.koala.game.util.StringUtil;

public class GambleTips {
	private static String _tipsWishNotEnoughIgot;
	private static String _tipsWishPriceInfo;
	private static String _tipsWishBigPriceInfo;
	private static String _tipsWishPriceMailTitle;
	private static String _tipsWishPriceMailContent;
	private static String _tipsDicePriceMailTitle;
	private static String _tipsDicePriceMailContent;
	private static String _tipsDicePriceBagFull;
	private static String _tipsDiceNotEnoughCount;
	private static String _tipsDiceLuckyPriceWorldBroadcast;
	private static String _tipsDicePriceNone;
	private static String _tipsCirclePriceMailContent;
	private static String _tipsWishAddDiceCount;
	private static String _tipsDiceLuckyPriceMailContent;
	private static String _tipsNotFreeWish;
	private static String _tipsFreeWishNotAddDiceCount;

	private static String _tipsWish2NotEnoughIgot;
	private static String _tipsReflashWish2NotEnoughIgot;
	private static String _tipsWish2PriceMailTitle;
	private static String _tipsWish2PriceMailContent;
	private static String _tipsWish2CanNotLotteryAll;

	public static String getTipsWishNotEnoughIgot(int wishCount, int point) {
		return StringUtil.format(_tipsWishNotEnoughIgot, wishCount, point);
	}

	public static String getTipsWishPriceInfo(String itemName, long count) {
		return StringUtil.format(_tipsWishPriceInfo, itemName, count);
	}

	public static String getTipsWishBigPriceInfo(String roleName, String itemName, int count) {
		return StringUtil.format(_tipsWishBigPriceInfo, roleName, itemName, count);
	}

	public static String getTipsWishPriceMailTitle() {
		return _tipsWishPriceMailTitle;
	}

	public static String getTipsWishPriceMailContent() {
		return _tipsWishPriceMailContent;
	}

	public static String getTipsDicePriceMailTitle() {
		return _tipsDicePriceMailTitle;
	}

	public static String getTipsDicePriceMailContent(int totalDiceCount, int luckyCount, int luckyDiamond) {
		return StringUtil.format(_tipsDicePriceMailContent, totalDiceCount, luckyCount, luckyDiamond);
	}

	public static String getTipsCirclePriceMailContent(int circleCount, String itemName, int itemCount) {
		return StringUtil.format(_tipsCirclePriceMailContent, circleCount, itemName, itemCount);
	}

	public static String getTipsDicePriceBagFull() {
		return _tipsDicePriceBagFull;
	}

	public static String getTipsDiceNotEnoughCount() {
		return _tipsDiceNotEnoughCount;
	}

	public static String getTipsDiceLuckyPriceWorldBroadcast(String roleName, long count) {
		return StringUtil.format(_tipsDiceLuckyPriceWorldBroadcast, roleName, count);
	}

	public static String getTipsDicePriceNone() {
		return _tipsDicePriceNone;
	}

	public static String getTipsWishAddDiceCount(int wishCount, int diceCount) {
		return StringUtil.format(_tipsWishAddDiceCount, wishCount, diceCount);
	}

	public static String getTipsDiceLuckyPriceMailContent(int rate, long point) {
		return StringUtil.format(_tipsDiceLuckyPriceMailContent, rate, point);
	}

	public static String getTipsNotFreeWish() {
		return _tipsNotFreeWish;
	}

	public static String getTipsFreeWishNotAddDiceCount() {
		return _tipsFreeWishNotAddDiceCount;
	}

	public static String getTipsWish2NotEnoughIgot(int wishCount, int point, String curName) {
		return StringUtil.format(_tipsWish2NotEnoughIgot, wishCount, point, curName, curName);
	}

	public static String getTipsReflashWish2NotEnoughIgot(int point, String curName) {
		return StringUtil.format(_tipsReflashWish2NotEnoughIgot, point, curName, curName);
	}

	public static String getTipsWish2PriceMailTitle() {
		return _tipsWish2PriceMailTitle;
	}

	public static String getTipsWish2PriceMailContent() {
		return _tipsWish2PriceMailContent;
	}

	public static String getTipsWish2CanNotLotteryAll() {
		return _tipsWish2CanNotLotteryAll;
	}

}
