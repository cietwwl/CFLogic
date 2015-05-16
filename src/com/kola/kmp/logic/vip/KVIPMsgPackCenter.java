package com.kola.kmp.logic.vip;

import com.koala.game.KGameMessage;
import com.kola.kmp.logic.currency.KPaymentListener;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;
import com.kola.kmp.protocol.vip.KVIPProtocol;

/**
 * <pre>
 * 由于在打包模块消息的过程中，涉及到访问及复杂的逻辑结构
 * 因此设立此类，用于打包消息
 * 
 * @author CamusHuang
 * @creation 2013-3-22 下午4:08:56
 * </pre>
 */
public class KVIPMsgPackCenter {

	/**
	 * <pre>
	 * 参考{@link KVIPProtocol#SM_VIP_PUSH_CONSTANCE}
	 * 
	 * </pre>
	 */
	public static void packConstance(KGameMessage msg, KRole role) {
		VIPLevelData minLevel = KVIPDataManager.mVIPLevelDataManager.getMinLevel();
		VIPLevelData maxLevel = KVIPDataManager.mVIPLevelDataManager.getMaxLevel();

		msg.writeByte(maxLevel.lvl - minLevel.lvl + 1);
		for (int lv = minLevel.lvl; lv <= maxLevel.lvl; lv++) {
			VIPLevelData data = KVIPDataManager.mVIPLevelDataManager.getLevelData(lv);
			msg.writeInt(data.lvl);
			msg.writeInt(data.needrmb);
			msg.writeUtf8String(data.desc);
			msg.writeUtf8String(data.newDesc);
			//
			data.lvBaseMailRewardDataForShow.packMsg(msg);
			data.dayBaseMailRewardData.packMsg(msg);
		}
		
		msg.writeInt(KPaymentListener.ChargeRate);
	}

	/**
	 * <pre>
	 * 参考{@link KVIPProtocol#SM_VIP_SYNC_VIPDATA}
	 * 
	 * @param msg
	 * @param role
	 * @param vip
	 * @author CamusHuang
	 * @creation 2014-4-16 下午12:07:48
	 * </pre>
	 */
	public static void packVipData(KGameMessage msg, KRoleVIP vip) {
		/**
		 * <pre>
		 * 角色VIP数据
		 * 服务器在有必要时，服务器会主动推送此消息
		 * 
		 * int 角色当前VIP等级
		 * int 角色当前VIP经验
		 * byte 每日礼包状态(1.未可领取 2.可领取 3.已领取)---客户端根据当前VIP等级显示相应每日礼包奖励
		 * 
		 * byte VIP等级数量
		 * for(1~N){
		 * 	int VIP等级（从1开始）
		 *  byte 领取等级奖励状态(1.未可领取 2.可领取 3.已领取)
		 * }
		 * </pre>
		 */

		msg.writeInt(vip.getLv());
		msg.writeInt(vip.getExp());
		{
			if (vip.getLv() < 1) {
				msg.writeByte(1);
			} else {
				if (vip.isCollectedDayReward(System.currentTimeMillis())) {
					msg.writeByte(3);
				} else {
					msg.writeByte(2);
				}
			}
		}

		VIPLevelData minLevel = KVIPDataManager.mVIPLevelDataManager.getMinLevel();
		VIPLevelData maxLevel = KVIPDataManager.mVIPLevelDataManager.getMaxLevel();

		msg.writeByte(maxLevel.lvl - minLevel.lvl + 1);
		for (int lv = minLevel.lvl; lv <= maxLevel.lvl; lv++) {
			VIPLevelData data = KVIPDataManager.mVIPLevelDataManager.getLevelData(lv);
			msg.writeInt(data.lvl);
			//
			{
				if (vip.getLv() < lv) {
					msg.writeByte(1);
				} else {
					if (vip.isCollectedLvReward(lv)) {
						msg.writeByte(3);
					} else {
						msg.writeByte(2);
					}
				}
			}
		}
	}

}
