package com.kola.kmp.logic.mission.guide;

public class FunctionOpenRecord {
	public short funtionId;
	public boolean isOpen;
	public boolean isAlreadyGuide;

	public FunctionOpenRecord() {

	}

	public FunctionOpenRecord(short funtionId,
			boolean isOpen, boolean isAlreadyGuide) {
		this.funtionId = funtionId;
		this.isOpen = isOpen;
		this.isAlreadyGuide = isAlreadyGuide;
	}

	/**
	 * encode 所有数据变成DB字符串
	 * 
	 * @return
	 */
	public String encodeAttribute() {
		String encodeString = "";
		encodeString += (this.isOpen ? 1 : 0) + ":";
		encodeString += this.isAlreadyGuide ? 1 : 0;
		return encodeString;
	}

	/**
	 * 解释DB字符串，初始化所有属性
	 * 
	 * @param levelIdString
	 * @param jsonString
	 */
	public void decodeAttribute(String funtionIdString, String jsonString) {
		if (jsonString != null) {
			String[] attributes = jsonString.split(":");
			this.funtionId = Short.parseShort(funtionIdString);
			this.isOpen = (attributes[0].equals("1")) ? true : false;
			this.isAlreadyGuide = (attributes[1].equals("1")) ? true
					: false;
		}
	}
}