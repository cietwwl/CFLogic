package com.kola.kmp.logic.relationship;

import com.kola.kmp.logic.relationship.message.KPushConstanceMsg;
import com.kola.kmp.logic.vip.KRoleVIP;
import com.kola.kmp.logic.vip.KVIPUpLvListener;

public class KRelationVIPListener implements KVIPUpLvListener{

	@Override
	public void notifyVIPLevelUp(KRoleVIP vip, int preLv) {
		// 推送关系数量上限
		KPushConstanceMsg.pushMsg(vip.getRoleId());
	}

}
