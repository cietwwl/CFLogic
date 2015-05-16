package com.kola.kmp.logic.currency;

import com.kola.kmp.logic.RunTimeTask;
import com.kola.kmp.logic.currency.message.KPushCurrencyMsg;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;

/**
 * <pre>
 * 开关月卡无限购买
 * 
 * @author CamusHuang
 * @creation 2015-2-4 下午5:47:13
 * </pre>
 */
public class MonthCardPatch implements RunTimeTask {

	public String run(String param) {
		boolean isOpen = Boolean.parseBoolean(param);
		//
		if(KCurrencyDataManager.mChargeInfoManager.monthCard==null){
			return "没有月卡";
		}
		
		KCurrencyDataManager.mChargeInfoManager.monthCard.isMonthCardCanBuyMuil = isOpen;
		RoleModuleSupport mRoleModuleSupport = KSupportFactory.getRoleModuleSupport();
		for(long roleId:mRoleModuleSupport.getAllOnLineRoleIds()){
			KRole role = mRoleModuleSupport.getRole(roleId);
			if(role==null){
				continue;
			}
			KPushCurrencyMsg.sendMsg(role);
		}
		
		return "执行完毕="+isOpen;
	}
}
