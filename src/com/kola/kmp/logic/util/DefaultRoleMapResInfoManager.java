package com.kola.kmp.logic.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kola.kmp.logic.item.KItemDataStructs.KItemTempEqui;
import com.kola.kmp.logic.other.KEquipmentTypeEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.role.KRoleEquipmentShowDataImpl;

/**
 * <pre>
 * 角色默认装备、时装UI形象
 * 
 * @author CamusHuang
 * @creation 2014-6-25 上午11:39:22
 * </pre>
 */
public class DefaultRoleMapResInfoManager {

	private final static Map<KJobTypeEnum, DefaultRoleMapResInfo> DefaultInfoMap = new HashMap<KJobTypeEnum, DefaultRoleMapResInfo>();
	static {
		for (KJobTypeEnum job : KJobTypeEnum.values()) {
			DefaultInfoMap.put(job, new DefaultRoleMapResInfo());
		}
	}

	public static DefaultRoleMapResInfo getDefaultRoleMapResInfo(KJobTypeEnum job) {
		return DefaultInfoMap.get(job);
	}

	public static class DefaultRoleMapResInfo implements IRoleMapResInfo {
		
		private static final int[] _DEFAULT_EQUIP_SET_RES = new int[]{0, 0};
		private List<IRoleEquipShowData> equipmentRes = new ArrayList<IRoleEquipShowData>();
		
		private DefaultRoleMapResInfo() {
		}
		
		public void initEquipmentRes(Map<KEquipmentTypeEnum, KItemTempEqui> data) {
			for(KItemTempEqui temp:data.values()){
				equipmentRes.add(new KRoleEquipmentShowDataImpl((byte)temp.part, temp.showResId, temp.ItemQuality));
			}
		}

		@Override
		public List<IRoleEquipShowData> getEquipmentRes() {
			return equipmentRes;
		}

		@Override
		public String getFashionRes() {
			return null;
		}

		@Override
		public int[] getEquipSetRes() {
			return _DEFAULT_EQUIP_SET_RES;
		}
	}
}
