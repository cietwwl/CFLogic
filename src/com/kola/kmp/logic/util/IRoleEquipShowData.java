package com.kola.kmp.logic.util;

import com.kola.kmp.logic.other.KItemQualityEnum;

public interface IRoleEquipShowData {

	/**
	 * 
	 * 获取部位
	 * 
	 * @return
	 */
	public byte getPart();

	/**
	 * 获取装备资源
	 * 
	 * @return
	 */
	public String getRes();

	/**
	 * 
	 * 获取装备的品质
	 * 
	 * @return
	 */
	public KItemQualityEnum getQuality();
}
