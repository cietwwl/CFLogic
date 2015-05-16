package com.kola.kmp.logic.role;

import java.util.Map;

import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.support.RoleModuleSupport;

/**
 * 
 * 角色属性提供者
 * 
 * @author PERRY CHAN
 */
public interface IRoleAttributeProvider {

	/**
	 * 
	 * 获取这个提供者提供给角色的属性
	 * 
	 * @param role
	 * @return
	 */
	public Map<KGameAttrType, Integer> getEffectAttr(KRole role);
	
	/**
	 * <pre>
	 * 角色模块通知本提供者，已经分配一个id，提供者需要记录此id。
	 * 在发生属性改变的时候，需要调用{@link RoleModuleSupport#notifyEffectAttrChange(long, int)}
	 * 此时需要提供属性提供者类型
	 * <pre>
	 * @param type
	 */
	public void notifyTypeAssigned(int type);
}
