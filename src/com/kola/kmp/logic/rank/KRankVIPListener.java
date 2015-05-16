package com.kola.kmp.logic.rank;

import com.kola.kmp.logic.vip.KRoleVIP;
import com.kola.kmp.logic.vip.KVIPUpLvListener;

public class KRankVIPListener implements KVIPUpLvListener{

	@Override
	public void notifyVIPLevelUp(KRoleVIP vip, int preLv) {
		KRankLogic.notifyRoleVipUp(vip.getRoleId(), (byte)vip.getLv());
	}

}
