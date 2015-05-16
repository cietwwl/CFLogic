package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.Map;

import com.kola.kmp.logic.other.KColorFunEnum.KColorManager;
import com.kola.kmp.logic.util.text.HyperTextTool;

/**
 * 
 * @author PERRY CHAN
 */
public enum KPetQuality {

	/** 宠物品质：绿色 */
	GREEN(1, "绿色", KColorFunEnum.品质_绿, false),
	/** 宠物品质：蓝色 */
	BLUE(2, "蓝色", KColorFunEnum.品质_蓝, false),
	/** 宠物品质：紫色 */
	PURPLE(3, "紫色", KColorFunEnum.品质_紫, true),
	/** 宠物品质：橙色 */
	ORANGE(4, "橙色", KColorFunEnum.品质_橙, true),
	/** 宠物品质：红色 */
	RED(5, "红色", KColorFunEnum.品质_红, true)
	;
	private static final Map<Integer, KPetQuality> _ALL_PET_QUALITY = new HashMap<Integer, KPetQuality>();
	
//	private static final String _petNameFormatter = "[{}]{}[-]";
	
	public final int sign;
	private String _name;
	private KColorFunEnum _colorEnum;
	private int _color;
	private boolean _senior;
	private String _colorHex; // 16位的颜色值
	
	static {
		KPetQuality[] array = values();
		KPetQuality quality;
		for (int i = 0; i < array.length; i++) {
			quality = array[i];
			_ALL_PET_QUALITY.put(quality.sign, quality);
		}
//		GREEN._color = (int)Long.parseLong("ff0000ff", 16);
//		BLUE._color = (int)Long.parseLong("ff00000ff", 16);
//		PURPLE._color = (int)Long.parseLong("ff0000ff", 16);
//		ORANGE._color = (int)Long.parseLong("ff0000ff", 16);
//		RED._color = (int)Long.parseLong("ff0000ff", 16);
	}
	
	private KPetQuality(int pSign, String pName, KColorFunEnum pColor, boolean pSenior) {
		this.sign = pSign;
		this._name = pName;
		this._colorEnum = pColor;
		this._senior = pSenior;
	}
	
	public int getColor() {
		return this._color;
	}
	
	public String getName() {
		return this._name;
	}
	
	public String formatPetName(String petName) {
		return HyperTextTool.extColor(petName, _colorEnum);
	}
	
	public KColorFunEnum getColorEnum() {
		return _colorEnum;
	}
	
	public boolean isSenior() {
		return _senior;
	}
	
	public static void initColor() {
		KPetQuality quality;
		for (int i = 0; i < values().length; i++) {
			quality = values()[i];
			quality._colorHex = KColorManager.getColor(quality._colorEnum.sign).color;
			quality._color = (int) Long.parseLong("ff" + quality._colorHex, 16);
		}
	}
	
	public static KPetQuality getEnumQuality(int pQuality) {
		return _ALL_PET_QUALITY.get(pQuality);
	}
}
