package com.kola.kmp.logic.util;

import java.util.List;

/**
 * 
 * @author PERRY CHAN
 */
public interface IRoleMapResInfo {

	/**
	 * 
	 * @return
	 */
	public List<IRoleEquipShowData> getEquipmentRes();
	
	/**
	 * 
	 * @return
	 */
	public String getFashionRes();
	
	/**
	 * 
	 * @return
	 */
	public int[] getEquipSetRes();
}
