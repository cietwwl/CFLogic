package com.kola.kmp.logic.shop.random.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.shop.KShopRoleExtCACreator;
import com.kola.kmp.logic.shop.random.KRandomShopDataManager;
import com.kola.kmp.logic.shop.random.KRoleRandomData;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.shop.KShopProtocol;

public class KPushRandomShopConstanceMsg implements KShopProtocol {

	public static void sendMsg(long roleId, long nextRefreshDelayTime) {
		KGameMessage msg = KGame.newLogicMessage(SM_PUSH_RANDOMSHOP_CONSTANCE);
		msg.writeByte(KRandomShopDataManager.RandomLogMaxCount);
		{
			KRoleRandomData roleData = KShopRoleExtCACreator.getRoleRandomData(roleId);
			int nowTime = roleData.getRandomTime();
			if (nowTime >= KRandomShopDataManager.RandomFreeTime) {
				msg.writeInt(0);
			} else {
				msg.writeInt(KRandomShopDataManager.RandomFreeTime - nowTime);
			}
		}
		msg.writeLong(nextRefreshDelayTime);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}
}
