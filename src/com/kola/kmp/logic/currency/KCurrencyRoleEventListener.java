package com.kola.kmp.logic.currency;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kmp.logic.currency.message.KPushCurrencyMsg;
import com.kola.kmp.logic.currency.message.KPushFirstChargeRewardMsg;
import com.kola.kmp.logic.currency.message.KPushPayInfoMsg;
import com.kola.kmp.logic.role.IRoleEventListener;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

public class KCurrencyRoleEventListener implements IRoleEventListener {

	@Override
	public void notifyRoleJoinedGame(KGamePlayerSession session, KRole role) {
		KPushPayInfoMsg.sendMsg(role);
		// 通知客户端
		KPushCurrencyMsg.sendMsg(role);
		
		// 首充数据推送
		KPushFirstChargeRewardMsg.sendMsg(session, role);
		
		// 月卡用户每天发钻石
		KCurrencySupportImpl.tryToSendMonthCardDayReward(role);
		
		// 限时返现活动
		KCurrencySupportImpl.notifyForStartTimeLimitPresentActivity(role);
	}

	@Override
	public void notifyRoleLeavedGame(/*KGamePlayerSession session,*/KRole role) {
		// 忽略
	}

	@Override
	public void notifyRoleCreated(KGamePlayerSession session, KRole role) {
		KSupportFactory.getCurrencySupport().increaseMoneys(role.getId(), KCurrencyConfig.NewRoleMoneys, PresentPointTypeEnum.角色初始值, false);
	}

	@Override
	public void notifyRoleDeleted(long roleId) {
		// 忽略
	}

	@Override
	public void notifyRoleLevelUp(KRole role, int preLv) {
		// 同步货币兑换比例
		KPushCurrencyMsg.sendMsg(role);
	}
	
	@Override
	public void notifyRoleDataPutToCache(KRole role) {
		
	}
}
