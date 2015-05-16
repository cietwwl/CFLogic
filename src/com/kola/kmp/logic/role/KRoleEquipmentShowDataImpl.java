package com.kola.kmp.logic.role;

import com.kola.kmp.logic.other.KItemQualityEnum;
import com.kola.kmp.logic.util.IRoleEquipShowData;

public class KRoleEquipmentShowDataImpl implements IRoleEquipShowData {

	private byte _part;
	private String _res;
	private KItemQualityEnum _quality;
	
	KRoleEquipmentShowDataImpl() {
		
	}
	
	public KRoleEquipmentShowDataImpl(byte pPart, String pRes, KItemQualityEnum pQuality) {
		this._part = pPart;
		this._res = pRes;
		this._quality = pQuality;
	}
	
	void copy(IRoleEquipShowData equipShowData) {
		this._part = equipShowData.getPart();
		this._res = equipShowData.getRes();
		this._quality = equipShowData.getQuality();
	}
	
	@Override
	public byte getPart() {
		return _part;
	}

	@Override
	public String getRes() {
		return _res;
	}

	@Override
	public KItemQualityEnum getQuality() {
		return _quality;
	}
	
}
