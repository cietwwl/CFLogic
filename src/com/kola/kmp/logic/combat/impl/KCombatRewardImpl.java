package com.kola.kmp.logic.combat.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kmp.logic.flow.KPetFlowType;
import com.kola.kmp.logic.level.ICombatAdditionalReward;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatRewardImpl implements ICombatAdditionalReward {

	private long _roleId;
	private Map<KCurrencyTypeEnum, Integer> _currencyMap = new HashMap<KCurrencyTypeEnum, Integer>();
	private Map<String, Integer> _itemMap = new HashMap<String, Integer>();
	private Map<Integer, Integer> _petMap = new HashMap<Integer, Integer>();
	
	KCombatRewardImpl(long pRoleId) {
		this._roleId = pRoleId;
	}
	
	@Override
	public long getRoleId() {
		return _roleId;
	}

	@Override
	public Map<KCurrencyTypeEnum, Integer> getAdditionalCurrencyReward() {
		return _currencyMap;
	}
	
	@Override
	public void addCurrencyReward(KCurrencyTypeEnum type, int value) {
		Integer currentValue = _currencyMap.get(type);
		if (currentValue != null) {
			value += currentValue;
		}
		_currencyMap.put(type, value);
	}

	@Override
	public Map<String, Integer> getAdditionalItemReward() {
		return _itemMap;
	}
	
	@Override
	public void addItemReward(String itemCode, int value) {
		Integer currentValue = _itemMap.get(itemCode);
		if (currentValue != null) {
			value += currentValue;
		}
		_itemMap.put(itemCode, value);
	}
	
	@Override
	public Map<Integer, Integer> getAdditionalPetReward() {
		return _petMap;
	}
	
	@Override
	public void addAdditionalPetReward(int templateId, int count) {
		Integer currentValue = _petMap.get(templateId);
		if (currentValue != null) {
			count += currentValue;
		}
		_petMap.put(templateId, count);
	}

	@Override
	public void executeReward(KRole role) {
		if(this._currencyMap.size() > 0) {
			List<KCurrencyCountStruct> list = new ArrayList<KCurrencyCountStruct>(this._currencyMap.size());
			Map.Entry<KCurrencyTypeEnum, Integer> entry;
			for(Iterator<Map.Entry<KCurrencyTypeEnum, Integer>> itr = _currencyMap.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				list.add(new KCurrencyCountStruct(entry.getKey(), entry.getValue()));
			}
			KSupportFactory.getCurrencySupport().increaseMoneys(_roleId, list, PresentPointTypeEnum.战斗奖励, true);
		}
		if(this._itemMap.size() > 0) {
			List<ItemCountStruct> list = new ArrayList<ItemCountStruct>(_itemMap.size());
			Map.Entry<String, Integer> entry;
			for(Iterator<Map.Entry<String, Integer>> itr = _itemMap.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				list.add(new ItemCountStruct(entry.getKey(), entry.getValue()));
			}
			KSupportFactory.getItemModuleSupport().addItemsToBag(role, list, this.getClass().getSimpleName());
		}
		if (this._petMap.size() > 0) {
			KSupportFactory.getPetModuleSupport().createPetsToRole(_roleId, _petMap, true, KPetFlowType.关卡掉落.name());
		}
	}

}
