package com.kola.kmp.logic.vip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.FlowDataModuleFactory;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_SendMail;
import com.kola.kmp.logic.util.tips.RewardTips;
import com.kola.kmp.logic.util.tips.VIPTips;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;
import com.kola.kmp.logic.vip.message.KSyncVipDataMsg;

public class KVIPLogic {

	private static List<KVIPUpLvListener> upLvlisteners = new ArrayList<KVIPUpLvListener>();

	/**
	 * <pre>
	 * 添加VIP监听器
	 * 
	 * @param vip
	 * @param oldVipLv
	 * @author CamusHuang
	 * @creation 2013-11-8 下午12:04:41
	 * </pre>
	 */
	static void addVipUpLvListener(KVIPUpLvListener listener) {
		upLvlisteners.add(listener);
	}
	
	/**
	 * <pre>
	 * 本方法负责将VIP升级通知到各相关模块
	 * 
	 * @param vip
	 * @param oldVipLv
	 * @author CamusHuang
	 * @creation 2013-11-8 下午12:04:41
	 * </pre>
	 */
	public static void vipUpLvNotify(KRoleVIP vip, int oldVipLv) {
		
		// 记录流水
		FlowDataModuleFactory.getModule().recrodVipInfo(oldVipLv, vip.getLv());
		
		// 通知监听器
		for (KVIPUpLvListener listener : upLvlisteners) {
			listener.notifyVIPLevelUp(vip, oldVipLv);
		}
	}
	
	/**
	 * <pre>
	 * 赠送VIP等级给指定角色
	 * 
	 * @param role
	 * @param newVipLv
	 * @author CamusHuang
	 * @creation 2014-8-14 下午4:07:11
	 * </pre>
	 */
	public static void presentVIPLv(KRole role, int newVipLv) {
		KRoleVIP vip = KVIPRoleExtCACreator.getRoleVIP(role.getId());
		
		int oldLv = vip.getLv();
		vip.setLv(newVipLv);
		
		// vip礼包自动领取
		autoCollectVipLvReward(role);
		
		// 将VIP升级通知到各相关模块
		KVIPLogic.vipUpLvNotify(vip, oldLv);
		KDialogService.sendDataUprisingDialog(role.getId(), StringUtil.format(VIPTips.VIP升级到第x级, vip.getLv()));
		
		// 同步给客户端
		KSyncVipDataMsg.sendMsg(role, vip);
	}

	public static CommonResult_Ext dealMsg_collectVipLvReward(KRole role, int viplv) {
		CommonResult_Ext result = new CommonResult_Ext();

		if (viplv < KVIPDataManager.mVIPLevelDataManager.getMinLevel().lvl) {
			result.tips = RewardTips.对不起不存在此礼包;
			return result;
		}

		VIPLevelData vipData = KVIPDataManager.mVIPLevelDataManager.getLevelData(viplv);
		if (vipData == null) {
			result.tips = RewardTips.对不起不存在此礼包;
			return result;
		}

		KRoleVIP vip = KVIPRoleExtCACreator.getRoleVIP(role.getId());

		vip.rwLock.lock();
		try {
			if (viplv > vip.getLv()) {
				result.tips = RewardTips.你还不能领取此礼包;
				return result;
			}

			if (vip.isCollectedLvReward(viplv)) {
				result.tips = RewardTips.你已领取此礼包;
				return result;
			}

			RewardResult_SendMail tempResult = vipData.lvBaseMailRewardDataForSend.sendReward(role, PresentPointTypeEnum.VIP等级奖励, true);
			if (!tempResult.isSucess) {
				result.tips = RewardTips.领取失败;
				return result;
			}

			if (tempResult.isSendByMail) {
				result.addUprisingTips(RewardTips.背包已满奖励通过邮件发送);
			} else {
				result.addDataUprisingTips(tempResult.getDataUprisingTips());
			}

			vip.collectedLvReward(viplv);

			result.tips = StringUtil.format(RewardTips.成功领取VIPx等级礼包, viplv);
			result.isSucess = true;
			return result;
		} finally {
			vip.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 充值时自动发送VIP等级奖励
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-9-26 下午3:37:30
	 * </pre>
	 */
	public static void autoCollectVipLvReward(KRole role) {
		KRoleVIP vip = KVIPRoleExtCACreator.getRoleVIP(role.getId());

		vip.rwLock.lock();
		try {
			int nowVipLv = vip.getLv();
			int minVipLv = KVIPDataManager.mVIPLevelDataManager.getMinLevel().lvl;

			int sendCount=0;
			int lastLv = 0;
			for (int lv = minVipLv; lv <= nowVipLv; lv++) {
				if (vip.isCollectedLvReward(lv)) {
					// 已经领取
					continue;
				}

				VIPLevelData vipData = KVIPDataManager.mVIPLevelDataManager.getLevelData(lv);
				if (vipData == null) {
					continue;
				}

				RewardResult_SendMail tempResult = vipData.lvBaseMailRewardDataForSend.sendReward(role, PresentPointTypeEnum.VIP等级奖励, false);
				if (!tempResult.isSucess) {
					continue;
				}

				vip.collectedLvReward(lv);
				sendCount++;
				lastLv = lv;
			}
			
			if(sendCount>1){
				KDialogService.sendUprisingDialog(role, StringUtil.format(RewardTips.VIPx等级礼包已通过邮件发送, ""));
			} else if(sendCount==1){
				KDialogService.sendUprisingDialog(role, StringUtil.format(RewardTips.VIPx等级礼包已通过邮件发送, lastLv));
			}
		} finally {
			vip.rwLock.unlock();
		}
	}

	public static CommonResult_Ext dealMsg_collectVipDayReward(KRole role) {
		CommonResult_Ext result = new CommonResult_Ext();

		KRoleVIP vip = KVIPRoleExtCACreator.getRoleVIP(role.getId());

		vip.rwLock.lock();
		try {
			if (vip.getLv() < KVIPDataManager.mVIPLevelDataManager.getMinLevel().lvl) {
				result.tips = RewardTips.你还不能领取此礼包;
				return result;
			}

			long nowTime = System.currentTimeMillis();

			if (vip.isCollectedDayReward(nowTime)) {
				result.tips = RewardTips.你已领取此礼包;
				return result;
			}

			VIPLevelData vipData = KVIPDataManager.mVIPLevelDataManager.getLevelData(vip.getLv());
			RewardResult_SendMail tempResult = vipData.dayBaseMailRewardData.sendReward(role, PresentPointTypeEnum.VIP每日奖励, true);
			if (!tempResult.isSucess) {
				result.tips = RewardTips.领取失败;
				return result;
			}
			
			if (tempResult.isSendByMail) {
				result.addUprisingTips(RewardTips.背包已满奖励通过邮件发送);
			} else {
				result.addDataUprisingTips(tempResult.getDataUprisingTips());
			}

			vip.collectedDayReward(nowTime);

			result.tips = RewardTips.成功领取VIP每日礼包;
			result.isSucess = true;
			return result;
		} finally {
			vip.rwLock.unlock();
		}
	}
	
	/**
	 * <pre>
	 * 加经验后，尝试递归升级
	 * 
	 * @param vip
	 * @return
	 * @author CamusHuang
	 * @creation 2015-1-30 下午3:27:53
	 * </pre>
	 */
	public static boolean tryToUpVIPLv(KRoleVIP vip){
		boolean isUpLv = false;
		while (true) {
			int nowLv = vip.getLv();
			int nowExp = vip.getExp();
			int nextLv = nowLv + 1;
			VIPLevelData nextData = KVIPDataManager.mVIPLevelDataManager.getLevelData(nextLv);
			{
				if (nextData == null) {
					// 没有下一级
					vip.setExp(0);
					break;
				}
			}

			if (nowExp < nextData.needrmb) {
				break;
			}
			// 升级
			vip.setLv(nextLv);
			vip.setExp(nowExp - nextData.needrmb);
			isUpLv = true;
		}
		return isUpLv;
	}
}
