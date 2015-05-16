package com.kola.kmp.logic.reward;

import com.kola.kmp.logic.reward.garden.message.KGardenSynMsg;
import com.kola.kmp.logic.vip.KRoleVIP;
import com.kola.kmp.logic.vip.KVIPUpLvListener;

public class KRewardVIPListener implements KVIPUpLvListener {

	@Override
	public void notifyVIPLevelUp(KRoleVIP vip, int preLv) {
		KGardenSynMsg.sendVipSaveLogo(vip);
	}

}
