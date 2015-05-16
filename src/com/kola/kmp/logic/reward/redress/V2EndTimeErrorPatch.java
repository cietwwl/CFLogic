package com.kola.kmp.logic.reward.redress;

import java.text.ParseException;

import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.RunTimeTask;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;

/**
 * <pre>
 * 1.0.5 V2补偿 修改角色截止时间补丁
 * 原由：IOS正版配置的角色截止时间为2015-02-04 09:00，需要 修改为2015-02-10 09:30
 * 同时扫描一次缓存中的角色，全部触发一次补偿
 * 
 * @author CamusHuang
 * @creation 2015-2-4 下午4:29:07
 * </pre>
 */
public class V2EndTimeErrorPatch implements RunTimeTask {

	public String run(String param) {
		try {
			//yyyyMMddHHmm
			long newTime = UtilTool.DATE_FORMAT3.parse(param).getTime();
			//
			KRedressDataManagerV2.RoleCreateEndTime = newTime;
			//
			RoleModuleSupport mRoleModuleSupport = KSupportFactory.getRoleModuleSupport();
			for (long roleId : mRoleModuleSupport.getRoleIdCache()) {
				KRole role = mRoleModuleSupport.getRole(roleId);
				KRedressCenter.doRedress(role);
			}
		} catch (ParseException e) {
			return "发生异常=" + e.getMessage() + ", param=" + param;
		}

		return "执行完毕 param=" + param;
	}
}
