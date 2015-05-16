package com.kola.kmp.logic.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.role.RoleBaseInfoBaseImpl;
import com.kola.kmp.logic.map.KGameMapEntity.RoleEquipShowData;
import com.kola.kmp.logic.other.KItemQualityEnum;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.IRoleEquipShowData;

/**
 * 
 * @author PERRY CHAN
 */
public class KRoleBaseInfo extends RoleBaseInfoBaseImpl implements IRoleBaseInfo, Comparable<IRoleBaseInfo> {

	private static final String JSON_NULL_STRING = "null";
//	private Map<Byte, String> _equipmentResMap = new HashMap<Byte, String>();
//	private Map<Byte, String> _equipmentResMapRO = Collections.unmodifiableMap(_equipmentResMap);
	private List<IRoleEquipShowData> _equipmentShowDataList = new ArrayList<IRoleEquipShowData>();
	private List<IRoleEquipShowData> _equipmentShowDataListRO = Collections.unmodifiableList(_equipmentShowDataList);
	private String _fashionRes;
	private int[] _equipSetRes = new int[2];
	
	public static final Comparator<IRoleBaseInfo> CMP = new Comparator<IRoleBaseInfo>() {
		
		@Override
		public int compare(IRoleBaseInfo o1, IRoleBaseInfo o2) {
			if (o1.getLevel() > o2.getLevel()) {
				return -1;
			} else if (o1.getLevel() < o2.getLevel()) {
				return 1;
			} else {
				return o1.getLastJoinGameTime() > o2.getLastJoinGameTime() ? -1 : 1;
			}
		}
		
	};
	
	@Override
	protected void decode(String attribute) {
		try {
			JSONObject obj = new JSONObject(attribute);
			JSONObject equipObj = obj.optJSONObject(KRole.KEY_EQUIPMENT_RES_MAP);
			if (equipObj != null) {
				String key;
				String value;
				String[] equipArg;
				byte part;
				for (@SuppressWarnings("unchecked")
				Iterator<String> itr = obj.keys(); itr.hasNext();) {
					key = itr.next();
					value = obj.getString(key);
					if (value != null && !value.equals(JSON_NULL_STRING)) {
//						_equipmentResMap.put(Byte.parseByte(key), value);
						equipArg = value.split(";");
						part = Byte.parseByte(key);
						if (equipArg.length > 1) {
							_equipmentShowDataList.add(new KRoleEquipmentShowDataImpl(part, equipArg[0], KItemQualityEnum.getEnum(Integer.parseInt(equipArg[1]))));
						} else {
							_equipmentShowDataList.add(new KRoleEquipmentShowDataImpl(part, equipArg[0], KItemQualityEnum.优秀的));
						}
					}
				}
			}
			this._fashionRes = obj.optString(KRole.KEY_FASHION_RES_ID, "");
			String equipSetStr = obj.optString(KRole.KEY_EQUIPMENT_SET_RES, null);
			if (equipSetStr != null) {
				String[] array = equipSetStr.split(",");
				if (_equipSetRes.length < array.length) {
					_equipSetRes = new int[array.length];
				}
				for (int i = 0; i < array.length; i++) {
					_equipSetRes[i] = Integer.parseInt(array[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void copyFrom(KRole role) {
		this.id = role.getId();
		this.level = role.getLevel();
		this.name = role.getName();
		this.type = role.getType();
		this._fashionRes = role.getFashionRes();
		this._equipmentShowDataList.addAll(role.getEquipmentRes());
		if(role.getEquipSetRes() != null) {
			this._equipSetRes = new int[role.getEquipSetRes().length];
			System.arraycopy(role.getEquipSetRes(), 0, this._equipSetRes, 0, this._equipSetRes.length);
		} else {
			this._equipSetRes = new int[]{0, 0};
		}
	}
	
	void update(KRole role) {
		this.level = role.getLevel();
	}
	
	void updateEquipmentRes() {
		List<RoleEquipShowData> equipList = KSupportFactory.getItemModuleSupport().getRoleEquipShowDataList(this.getId());
		this._equipmentShowDataList.clear();
		RoleEquipShowData tempData;
		for(int i = 0; i < equipList.size(); i++) {
			tempData = equipList.get(i);
			_equipmentShowDataList.add(new KRoleEquipmentShowDataImpl(tempData.getPart(), tempData.getRes(), tempData.getQuality()));
		}
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(id);
		if(role != null) {
			role.updateEquipmentRes(_equipmentShowDataListRO);
		}
	}
	
	void updateFashionResId() {
		String pre = this._fashionRes;
		this._fashionRes = KSupportFactory.getFashionModuleSupport().getFashingResId(getId());
		if ((pre == null && _fashionRes != null) || !(pre.equals(_fashionRes))) {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(id);
			role.updateFashionRes(_fashionRes);
		}
	}
	
	void updateEquipSetRes() {
		int[] array = KSupportFactory.getItemModuleSupport().getEquiSetMapResIds(getId());
		if (this._equipSetRes.length < array.length) {
			this._equipSetRes = new int[array.length];
			System.arraycopy(array, 0, _equipSetRes, 0, array.length);
		} else {
			for (int i = 0; i < array.length; i++) {
				this._equipSetRes[i] = array[i];
			}
		}
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(id);
		role.updateEquipSetRes(array);
	}

	@Override
	public List<IRoleEquipShowData> getEquipmentRes() {
		return _equipmentShowDataListRO;
	}

	@Override
	public String getFashionRes() {
		return _fashionRes;
	}
	
	@Override
	public int[] getEquipSetRes() {
		return _equipSetRes;
	}
	
	@Override
	public boolean isOnline() {
		return false;
	}

	@Override
	public int compareTo(IRoleBaseInfo o) {
		if(this.level > o.getLevel()) {
			return -1;
		} else if(this.level < o.getLevel()){
			return 1;
		} else {
			
		}
		return 0;
	}

}
