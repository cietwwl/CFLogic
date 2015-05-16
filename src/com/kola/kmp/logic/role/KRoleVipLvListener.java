package com.kola.kmp.logic.role;

import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.vip.KRoleVIP;
import com.kola.kmp.logic.vip.KVIPUpLvListener;

/**
 * 
 * @author PERRY CHAN
 */
public class KRoleVipLvListener implements KVIPUpLvListener {

	@Override
	public void notifyVIPLevelUp(KRoleVIP vip, int preLv) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(vip.getRoleId());
		if (role != null) {
			role.setVipLv(vip.getLv());
		}
	}

}
