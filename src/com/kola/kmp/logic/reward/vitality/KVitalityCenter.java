package com.kola.kmp.logic.reward.vitality;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.koala.game.KGameMessage;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KVitalityTypeEnum;
import com.kola.kmp.logic.reward.vitality.KVitalityDataManager.FunTypeDataManager.FunTypeData;
import com.kola.kmp.logic.reward.vitality.KVitalityDataManager.VitalityRewardDataManager.VitalityRewardData;
import com.kola.kmp.logic.reward.vitality.message.KVitalitySyncFunTimeMsg;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.RewardResult_SendMail;
import com.kola.kmp.logic.util.ResultStructs.VatalityRewardResult;
import com.kola.kmp.logic.util.tips.RewardTips;

public class KVitalityCenter {

	private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();// 读写锁

	public static int getVitalityValue(long roleId) {
		KRoleRewardVitality vdata = KVitalitySonModule.instance.getRewardSon(roleId);
		return vdata.getVitalityValue();
	}

	/**
	 * <pre>
	 * 为活跃度奖励记录操作行为
	 * 
	 * @deprecated 模块内调用
	 * @param role
	 * @param funtype
	 * @param addTime
	 * @author CamusHuang
	 * @creation 2014-6-2 下午6:08:18
	 * </pre>
	 */
	public static void recordFuns(KRole role, KVitalityTypeEnum funtype, int addTime) {
		if (funtype == null) {
			return;
		}

		FunTypeData fun = KVitalityDataManager.mFunTypeDataManager.getData(funtype.sign);
		if(fun == null){
			return;
		}
//		if (role.getLevel() < fun.mixlv) {
//			return;
//		}

		boolean isTaskFinished = false;
		String showTips = null;
		int newTime = 0;
		KRoleRewardVitality vdata = KVitalitySonModule.instance.getRewardSon(role.getId());
		vdata.rwLock.lock();
		try {

			int releaseTime = fun.time - vdata.getFunTime(funtype);
			if (releaseTime < 1) {
				return;
			}
			addTime = Math.min(releaseTime, addTime);
			newTime = vdata.recordFuns(funtype, addTime);
			if (newTime >= fun.time) {
				// 完成任务
				vdata.addVitality(fun.vitalityValue);
				showTips = StringUtil.format(RewardTips.活跃度任务x完成, fun.name);
				isTaskFinished = true;
			}
		} finally {
			vdata.rwLock.unlock();
		}

		// 通知客户端
		KVitalitySyncFunTimeMsg.sendMsg(role.getId(), funtype, newTime, vdata.getVitalityValue());
		if (showTips != null) {
			KDialogService.sendUprisingDialog(role.getId(), showTips);
		}

		if(isTaskFinished){
			// 通知军团模块
			KSupportFactory.getGangSupport().notifyVitalityAdd(role, fun.vitalityValue);
		}
	}

	public static VatalityRewardResult dealMsg_getReward(KRole role, int rewardScore) {
		VatalityRewardResult result = new VatalityRewardResult();

		VitalityRewardData rewardData = null;

		KRoleRewardVitality vdata = KVitalitySonModule.instance.getRewardSon(role.getId());
		vdata.rwLock.lock();
		try {

			if (vdata.isCollectedReward(rewardScore)) {
				result.tips = RewardTips.你已领取此礼包;
				return result;
			}

			rewardData = KVitalityDataManager.mVitalityRewardDataManager.getData(role.getLevel(), rewardScore);
			if (rewardData == null) {
				result.tips = RewardTips.不存在此奖励;
				return result;
			}

			if(vdata.getVitalityValue() < rewardScore){
				result.tips = RewardTips.你还不能领取此礼包;
				return result;
			}

			{// 可领取

				RewardResult_SendMail sendResult = rewardData.baseMailReward.sendReward(role, PresentPointTypeEnum.其它, true);
				if (sendResult.isSendByMail) {
					result.addUprisingTips(RewardTips.背包已满奖励通过邮件发送);
				} else {
					result.addDataUprisingTips(rewardData.baseMailReward.baseRewardData.dataUprisingTips);
				}

				// 修改数据
				vdata.setCollectedReward(rewardScore);

				result.isSucess = true;
				result.tips = RewardTips.成功领取奖励;
				result.rewardLv = rewardData.rewardLv;
				return result;
			}
		} finally {
			vdata.rwLock.unlock();
		}
	}

	public static void packRewardDatas(KRole role, KGameMessage msg) {
		KRoleRewardVitality vdata = KVitalitySonModule.instance.getRewardSon(role.getId());
		
		List<VitalityRewardData> list = KVitalityDataManager.mVitalityRewardDataManager.getData(role.getLevel());
		
		msg.writeByte(list.size());
		for(VitalityRewardData data:list){
			msg.writeInt(data.score);
			msg.writeBoolean(vdata.isCollectedReward(data.score));
			
			data.baseMailReward.packMsg(msg);
		}
	}

	public static void packFunDatas(long roleId, KGameMessage msg) {
		KRoleRewardVitality vdata = KVitalitySonModule.instance.getRewardSon(roleId);
		vdata.rwLock.lock();
		try {
			msg.writeInt(vdata.getVitalityValue());

			// <类型,数据>
			LinkedHashMap<Integer, FunTypeData> map = KVitalityDataManager.mFunTypeDataManager.getDataCache();
			msg.writeByte(map.size());
			for (FunTypeData data : map.values()) {
				msg.writeByte(data.typeEnum.sign);
				msg.writeUtf8String(data.name);
				msg.writeInt(data.icon);
				msg.writeInt(data.orderId);
				msg.writeInt(data.vitalityValue);
				msg.writeShort(vdata.getFunTime(data.typeEnum));
				msg.writeShort(data.time);
			}
		} finally {
			vdata.rwLock.unlock();
		}
	}
}
