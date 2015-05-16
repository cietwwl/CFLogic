package com.kola.kmp.logic.item;

import com.koala.thirdpart.json.JSONObject;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-13 上午1:33:46
 * </pre>
 */
public interface KItemPackCustomizeAttribute {

	/**
	 * 
	 * @param attribute
	 */
	public void decodeAttribute(JSONObject attribute);
	
	/**
	 * 
	 * @return
	 */
	public JSONObject encodeAttribute();
}
