package com.kola.kmp.logic.currency;

import java.util.List;

import com.koala.game.KGameMessage;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.currency.KCurrencyDataManager.FirstChargeRewardDataManager;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.role.KRole;

public class KCurrencyMsgPackCenter {

	public static void packFirstChargeRewardData(KRole role, KGameMessage msg) {
//		 * byte 首充奖励状态（0等待首充，1已首充）
//		 * if(等待首充){
//		 * 	显示首充或领奖图标
//		 * 	short 道具数量
//		 *  for (道具数量) {
//		 * 		道具（参考{@link com.koala.kgame.protocol.item.KItemProtocol#MSG_STRUCT_ITEM_DETAILS}）
//		 *  }
//		 * } else {
//		 * 	不显示任何图标
//		 * }
		FirstChargeRewardDataManager fchargeManager = KCurrencyDataManager.mFirstChargeRewardDataManager;

		if (fchargeManager.keepDays < 1) {
			// 无首充数据
			msg.writeByte(1);
			return;
		}

		long nowTime = System.currentTimeMillis();
		int N = UtilTool.countDays(role.getCreateTime(), nowTime);// 角色创建了多少天
		if (N > fchargeManager.keepDays) {
			// 过时
			msg.writeByte(1);
			return;
		}

		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(role.getId());

		if (!set.hasNotCatchFirstChargeGiftReward()) {
			// 已经首充
			msg.writeByte(1);
			return;
		}

		set.rwLock.lock();
		try {

			msg.writeByte(0);// （0等待首充，1已首充）

			// 常规打包
			List<ItemCountStruct> itemStructs = fchargeManager.getReward(role.getJob());
			msg.writeShort(itemStructs.size());
			for(ItemCountStruct s:itemStructs){
				KItemMsgPackCenter.packItem(msg, s.getItemTemplate(), s.itemCount);
			}
		} finally {
			set.rwLock.unlock();
		}
	}
}
