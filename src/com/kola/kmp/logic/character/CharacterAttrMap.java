package com.kola.kmp.logic.character;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kola.kmp.logic.other.KGameAttrType;


/**
 * 
 * @author PERRY CHAN
 */
public class CharacterAttrMap {

//	/**
//	 * 属性数据类型：整型
//	 */
//	public static final byte TYPE_INTEGER = 1;
//	/**
//	 * 属性数据类型：浮点（float）
//	 */
//	public static final byte TYPE_FLOAT = 2;
//	
//	private static final Map<KGameAttrType, Byte> _ATTRIBUTE_TYPE = new HashMap<KGameAttrType, Byte>();
//	static {
//		_ATTRIBUTE_TYPE.put(KGameAttrType.MAX_HP, TYPE_INTEGER);
//		_ATTRIBUTE_TYPE.put(KGameAttrType.HP_RECOVERY, TYPE_INTEGER);
//		_ATTRIBUTE_TYPE.put(KGameAttrType.ATK, TYPE_INTEGER);
//		_ATTRIBUTE_TYPE.put(KGameAttrType.DEF, TYPE_INTEGER);
//		_ATTRIBUTE_TYPE.put(KGameAttrType.HIT_RATING, TYPE_INTEGER);
//		_ATTRIBUTE_TYPE.put(KGameAttrType.CRIT_RATING, TYPE_INTEGER);
//		_ATTRIBUTE_TYPE.put(KGameAttrType.DODGE_RATING, TYPE_INTEGER);
//		_ATTRIBUTE_TYPE.put(KGameAttrType.RESILIENCE_RATING, TYPE_INTEGER);
//		_ATTRIBUTE_TYPE.put(KGameAttrType.CRIT_MULTIPLE, TYPE_FLOAT);
//		_ATTRIBUTE_TYPE.put(KGameAttrType.CD_REDUCE, TYPE_FLOAT);
//		_ATTRIBUTE_TYPE.put(KGameAttrType.HP_ABSORB, TYPE_FLOAT);
//		_ATTRIBUTE_TYPE.put(KGameAttrType.DEF_IGNORE, TYPE_INTEGER);
//		_ATTRIBUTE_TYPE.put(KGameAttrType.FAINT_RESIST_RATING, TYPE_INTEGER);
//		_ATTRIBUTE_TYPE.put(KGameAttrType.MOVE_SPEED, TYPE_INTEGER);
//	}
	
	private Map<KGameAttrType, Integer> _attrMap = new HashMap<KGameAttrType, Integer>();
	
	public void putValue(KGameAttrType type, Integer value) {
		Integer nowValue = _attrMap.get(type);
		if (nowValue != null) {
			value += nowValue;
		}
		_attrMap.put(type, value);
	}
	
//	public Number getRawValue(KGameAttrType type) {
//		return _attrMap.get(type);
//	}
	
	public float getFloatValue(KGameAttrType type) {
		Number num = _attrMap.get(type);
		if(num != null) {
			return num.floatValue();
		}
		return 0;
	}
	
	public int getAttrValue(KGameAttrType type) {
		Number num = _attrMap.get(type);
		if (num != null) {
			return num.intValue();
		}
		return 0;
	}
	
//	public static byte getValueType(KGameAttrType type) {
//		Byte vType = _ATTRIBUTE_TYPE.get(type);
//		if(vType != null) {
//			return vType;
//		} else {
//			return TYPE_INTEGER;
//		}
//	}
	
	public Set<KGameAttrType> keySet() {
		return _attrMap.keySet();
	}
	
	public void clear() {
		this._attrMap.clear();
	}
	
	public void replace(Map<KGameAttrType, Integer> map) {
		this.clear();
		this._attrMap.putAll(map);
	}
	
	public void replace(CharacterAttrMap other) {
		this.clear();
		this._attrMap.putAll(other._attrMap);
	}
	
	public void combine(CharacterAttrMap other) {
		Map.Entry<KGameAttrType, Integer> entry;
		Integer otherValue;
		if (this._attrMap.isEmpty()) {
			this._attrMap.putAll(other._attrMap);
		} else {
			Map<KGameAttrType, Integer> otherMap = other._attrMap;
			List<KGameAttrType> allKeys = new ArrayList<KGameAttrType>(other._attrMap.keySet());
			for (Iterator<Map.Entry<KGameAttrType, Integer>> itr = _attrMap.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				otherValue = otherMap.get(entry.getKey());
				if (otherValue != null) {
					entry.setValue(entry.getValue() + otherValue);
				}
				allKeys.remove(entry.getKey());
			}
			for (int i = 0; i < allKeys.size(); i++) {
				KGameAttrType temp = allKeys.get(i);
				otherValue = other._attrMap.get(temp);
				_attrMap.put(temp, otherValue);
			}
		}
	}
	
	public Map<KGameAttrType, Integer> differentWith(CharacterAttrMap other, KGameAttrType[] compareAttrList) {
		Map<KGameAttrType, Integer> different = new HashMap<KGameAttrType, Integer>();
		Integer myValue;
		Integer otherValue;
		KGameAttrType currentAttrType;
		for (int i = 0; i < compareAttrList.length; i++) {
			currentAttrType = compareAttrList[i];
			myValue = _attrMap.get(currentAttrType);
			otherValue = other._attrMap.get(currentAttrType);
			if (myValue == null) {
				myValue = 0;
			}
			if (otherValue == null) {
				otherValue = 0;
			}
			if(myValue.intValue() != otherValue.intValue()) {
				different.put(currentAttrType, myValue);
			}
		}
		return different;
	}
}
