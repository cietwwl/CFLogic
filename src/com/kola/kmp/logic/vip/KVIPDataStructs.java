package com.kola.kmp.logic.vip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.management.timer.Timer;

import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.RewardTips;

public class KVIPDataStructs {

	/**
	 * <pre>
	 * 
	 * @author CamusHuang
	 * @creation 2014-2-21 下午3:10:02
	 * </pre>
	 */
	public static class VIPLevelData {
		// ----------以下是EXCEL表格直导数据---------
		public int lvl;// 每等级
		public int needrmb;// 需要充值钻石数额
		public int[] fatbuyrmb;// 购买体力消耗钻石
		public int dungeonsexpup;// 副本产出经验额外增加万分比
		public int[] daytaskrmb;// 日常任务购买消耗钻石
		public int friendmaxcount;// 最大好友数
		public int[] pvpbuyrmb;// 竞技场购买消耗钻石
		private String[] viplvlaward;// vip充值礼包
		private int giftpackage;// vip每日礼包
		private int vipbox;// vip通关开箱子
		private int givepet;// vip赠送随从
		public int levelClear;// 精英副本扫荡
		public int BosslevelClear;// 技术副本扫荡
		private int gardensavetime;// 保卫庄园存储时长
		public int[] friendbuyrmb;// 好友副本购买消耗钻石
		public int presentRoleLv;// 封测按角色赠送VIP等级
		public int[] ladderbuyrmb;// 天梯赛购买消耗
		public int[] rescueHostages;// 随从副本购买消耗
		public boolean Autobattle;// 是否开启自动战斗
		public int minBean;
		public boolean bAutomaticParticipation;
		public int petTestSweepCount;
		
		// ----------以下是逻辑数据---------
		public BaseRewardData lvBaseMailRewardDataForShow;// LV奖励封装（viplvlaward+givepet）
		public BaseMailRewardData lvBaseMailRewardDataForSend;// LV奖励封装（viplvlaward）
		public BaseMailRewardData dayBaseMailRewardData;// 每日奖励封装（道具）
		public boolean 是否通关开箱子;
		public long gardensavetimeInMills;// 保卫庄园存储时长(毫秒)
		public long gardensavetimeInHours;// 保卫庄园存储时长(小时)

		public String desc;
		public String newDesc;
		
		public int totalCharge;//达到本等级要充的钻石总量

		void notifyCacheLoadComplete() throws Exception {

			if (lvl < 0) {
				throw new Exception("数值错误 lvl = " + lvl);
			}
			if (lvl > 0 && needrmb < 1) {
				throw new Exception("数值错误 needrmb = " + needrmb);
			}
			if (gardensavetime < 0) {
				throw new Exception("数值错误 gardensavetime = " + gardensavetime);
			}
			gardensavetimeInHours = gardensavetime;
			gardensavetimeInMills = gardensavetime * Timer.ONE_HOUR;
			for (int a : fatbuyrmb) {
				if (a < 0) {
					throw new Exception("数值错误 fatbuyrmb");
				}
			}
			if (dungeonsexpup < 0) {
				throw new Exception("数值错误 dungeonsexpup = " + dungeonsexpup);
			}
			for (int a : daytaskrmb) {
				if (a < 0) {
					throw new Exception("数值错误 daytaskrmb");
				}
			}
			if (friendmaxcount < 1) {
				throw new Exception("数值错误 friendmaxcount = " + friendmaxcount);
			}
			for (int a : daytaskrmb) {
				if (a < 0) {
					throw new Exception("数值错误 daytaskrmb");
				}
			}
			for (int a : pvpbuyrmb) {
				if (a < 0) {
					throw new Exception("数值错误 pvpbuyrmb");
				}
			}
			for (int a : friendbuyrmb) {
				if (a < 0) {
					throw new Exception("数值错误 friendbuyrmb");
				}
			}
			for (int a : ladderbuyrmb) {
				if (a < 0) {
					throw new Exception("数值错误 ladderbuyrmb");
				}
			}

			if (levelClear < 0) {
				throw new Exception("数值错误 levelClear = " + levelClear);
			}
			{
				if (lvl > 0) {
					// 310001*1,310002*1
					List<ItemCountStruct> addItems = null;
					if (viplvlaward.length > 0) {
						addItems = new ArrayList<ItemCountStruct>();
						ItemCountStruct.paramsItems(viplvlaward, addItems, 1);
					}

					if (givepet > 0 && KSupportFactory.getPetModuleSupport().getPetTemplate(givepet) == null) {
						throw new Exception("数值错误 givepet = " + givepet);
					}
					
					lvBaseMailRewardDataForShow = new BaseRewardData(null, null, addItems, null, givepet > 0 ? Arrays.asList(givepet) : null);
					
					BaseRewardData baseReward = new BaseRewardData(null, null, addItems, null, null);
					BaseMailContent baseMail = new BaseMailContent(StringUtil.format(RewardTips.VIPx等级礼包邮件标题, lvl), StringUtil.format(RewardTips.VIPx等级礼包邮件内容, lvl), null, null);
					lvBaseMailRewardDataForSend = new BaseMailRewardData(0, baseMail, baseReward);
				}
			}
			{
				if (lvl > 0) {
					ItemCountStruct vipDayReward = new ItemCountStruct(giftpackage + "", 1);
					if (vipDayReward.getItemTemplate() == null) {
						throw new Exception("数值错误 giftpackage = " + giftpackage);
					}

					BaseMailContent baseMail = new BaseMailContent(StringUtil.format(RewardTips.VIPx每日礼包, lvl), StringUtil.format(RewardTips.VIPx每日礼包物品请查收, lvl), null, null);
					// List<AttValueStruct> attList, List<KCurrencyCountStruct>
					// moneyList, List<ItemCountStruct> itemStructs,
					// List<Integer>
					// fashionTempIdList, List<Integer> petTempIdList
					BaseRewardData baseReward = new BaseRewardData(Collections.<AttValueStruct> emptyList(), Collections.<KCurrencyCountStruct> emptyList(), Arrays.asList(vipDayReward),
							Collections.<Integer> emptyList(), Collections.<Integer> emptyList());
					dayBaseMailRewardData = new BaseMailRewardData(0, baseMail, baseReward);
				}
			}

			是否通关开箱子 = vipbox == 1;
			
			if (presentRoleLv < 0) {
				throw new Exception("数值错误 presentRoleLv = " + presentRoleLv);
			}
			
			for (int a : rescueHostages) {
				if (a < 0) {
					throw new Exception("数值错误 rescueHostages = " + rescueHostages);
				}
			}
		}

	}
}
