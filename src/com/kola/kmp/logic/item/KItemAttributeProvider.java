package com.kola.kmp.logic.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.IRoleAttributeProvider;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.KSkillAttributeProvider;
import com.kola.kmp.logic.support.ItemModuleSupport.ISecondWeapon;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.KGameUtilTool;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-4-8 下午8:49:10
 * </pre>
 */
public class KItemAttributeProvider implements IRoleAttributeProvider {

	private static int _type;

	public static int getType() {
		return _type;
	}

	@Override
	public Map<KGameAttrType, Integer> getEffectAttr(KRole role) {
		// 单件装备属性（不含套装）A = 装备基础属性+强化属性+(装备基础属性*升星万分比)+镶嵌宝石属性
		// 装备套装属性加成B = 累加单件装备基础属性*(升星)套装额外增加比例 +(强化、品质)套装额外增加固定值
		// 装备总属性Z = 累加单件装备属性（不含套装）A+装备套装属性加成B+背包扩容属性D+被动技能加成C
		//
		Map<KGameAttrType, AtomicInteger> totalBaseAtts = new HashMap<KGameAttrType, AtomicInteger>();// 累加单件装备基础属性
		Map<KGameAttrType, AtomicInteger> totalAtts = new HashMap<KGameAttrType, AtomicInteger>();// 累加单件装备属性（不含套装）
		//
		KItemSet set = KItemModuleExtension.getItemSet(role.getId());
		set.rwLock.lock();
		try {
			// /////////////////////////////
			// 累加单件装备基础属性、累加单件装备属性（不含套装）
			KItemPack_BodySlot slot = set.getSlot();
			List<KItem> allEquis = slot.searchSlotItemList(KItemConfig.MAIN_BODYSLOT_ID);
			for (KItem equi : allEquis) {
				totalBaseAtts = equi.getEquipmentData().getBaseEffect(totalBaseAtts);
				totalAtts = equi.getEquipmentData().getAllEffect(totalAtts);
			}
			// /////////////////////////////
			// 装备套装属性加成B = 累加单件装备基础属性*(升星)套装额外增加比例 +(强化、品质)套装额外增加固定值
			totalAtts = slot.getAllSlotSetEffect(KItemConfig.MAIN_BODYSLOT_ID, totalBaseAtts, totalAtts);
			// /////////////////////////////
			// 背包扩容属性D
			totalAtts = KItemDataManager.mBagExtDataManager.getBagExtEffect(set.getBag().getVolume(), totalAtts);
			// /////////////////////////////
		} finally {
			set.rwLock.unlock();
		}
		
		// 被动技能加成C = 累加单件装备基础属性*被动技能额外增加比例
		totalAtts = KSkillAttributeProvider.getEffectForEqui(role.getId(), totalBaseAtts, totalAtts);
		
		// /////////////////////////////
		return KGameUtilTool.changeAttMap(totalAtts);
	}

	@Override
	public void notifyTypeAssigned(int type) {
		_type = type;
	}

	public static void notifyEffectAttrChange(KRole role) {
		// 刷新角色属性
		KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(role.getId(), getType());
		// 刷新副武器
		ISecondWeapon secondWepond = KSupportFactory.getItemModuleSupport().getSecondWeaponArgs(role.getId());
		KSupportFactory.getRoleModuleSupport().notifySecondWeaponUpdate(role.getId(), secondWepond);
	}
}
