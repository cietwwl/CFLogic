package com.kola.kmp.logic.support;

import java.util.List;

import com.kola.kmp.logic.fashion.KFashionDataStructs.KFashionTemplate;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;

public interface FashionModuleSupport {

	/**
	 * <pre>
	 * 
	 * 
	 * @param fashionTempId
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-2 上午10:23:32
	 * </pre>
	 */
	public KFashionTemplate getFashionTemplate(int fashionTempId);

	/**
	 * <pre>
	 * 批量添加时装
	 * 不允许部分添加
	 * 
	 * @param role
	 * @param fashionTempIds
	 * @param sourceTips 时装来源
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-2 上午11:00:08
	 * </pre>
	 */
	public CommonResult addFashions(KRole role, List<Integer> fashionTempIds, String sourceTips);

	/**
	 * <pre>
	 * 获取角色身上的时装id
	 * </pre>
	 * @param roleId
	 * @return
	 */
	public String getFashingResId(long roleId);
	
	/**
	 * <pre>
	 * 获取所有发时装模板
	 * </pre>
	 * @return
	 */
	public List<KFashionTemplate> getAllFashionTemplate();

	/**
	 * <pre>
	 * 克隆角色数据
	 * 
	 * @param myRole
	 * @param srcRole
	 * @author CamusHuang
	 * @creation 2014-10-15 上午11:50:31
	 * </pre>
	 */
	public void cloneRoleByCamus(KRole myRole, KRole srcRole);
}
