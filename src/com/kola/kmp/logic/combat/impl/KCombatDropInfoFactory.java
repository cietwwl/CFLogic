package com.kola.kmp.logic.combat.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.koala.game.KGameMessage;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatDropInfo;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.api.ICombatDropInfoTemplate;
import com.kola.kmp.logic.level.ICombatAdditionalReward;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatDropInfoFactory {
	
	public static ICombatDropInfo getCombatDropInfo(int dropId) {
		ICombatDropInfoTemplate template = KSupportFactory.getNpcModuleSupport().getDropInfoTemplate(dropId);
		if (template.willDrop()) {
			switch (template.getDropType()) {
			case ICombatDropInfoTemplate.DROP_TYPE_ITEM:
				return new KCombatItemDropInfo(template);
			case ICombatDropInfoTemplate.DROP_TYPE_PET:
				return new KCombatPetDropInfo(template);
			case ICombatDropInfoTemplate.DROP_TYPE_BUFF:
				return new KCombatStateDropInfo(template);
			case ICombatDropInfoTemplate.DROP_TYPE_HP:
				return new KCombatHpDropInfo(template);
			case ICombatDropInfoTemplate.DROP_TYPE_ENERGY:
				return new KCombatEnergyDropInfo(template);
			default:
				return null;
			}
		}
		return null;
	}
	
	public static List<ICombatDropInfo> createCurrencyDropInfo(Map<KCurrencyTypeEnum, Integer> map, int pResId) {
		List<ICombatDropInfo> list = new ArrayList<ICombatDropInfo>();
		Map.Entry<KCurrencyTypeEnum, Integer> entry;
		for (Iterator<Map.Entry<KCurrencyTypeEnum, Integer>> itr = map.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			list.add(new KCombatCurrencyDropInfo(entry.getKey(), entry.getValue(), pResId));
		}
		return list;
	}
	
	public static List<ICombatDropInfo> createItemDropInfo(Map<String, Integer> map, int pResId) {
		List<ICombatDropInfo> list = new ArrayList<ICombatDropInfo>();
		list.add(new KCombatItemDropInfo(map, pResId));
		return list;
	}
	
	private static abstract class KCombatDropInfoBaseImpl implements ICombatDropInfo {
		
		private int _serialId;
		private int _resId;
		private KCurrencyCountStruct _dropCurrency;
		private byte _type;
		private String _descr;
		private String _detail;
		
		protected KCombatDropInfoBaseImpl(byte pDropType) {
			this._serialId = idGenerator.incrementAndGet();
			this._type = pDropType;
		}
		
		
		protected KCombatDropInfoBaseImpl(ICombatDropInfoTemplate template) {
			this(template.getDropType());
			this._resId = template.getResId();
			if(template.getDropGold() > 0) {
				_dropCurrency = new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, template.getDropGold());
			}
		}
		
		protected abstract void notifyExecuteReward(ICombat combat, ICombatMember member, ICombatAdditionalReward reward, long happenTime);
		
		protected abstract String getDropDescr();
		
		protected abstract String getDropDetail();
		
		protected void setDropCurrency(KCurrencyTypeEnum type, int count) {
			this._dropCurrency = new KCurrencyCountStruct(type, count);
		}
		
		protected void setResId(int pResId) {
			this._resId = pResId;
		}
		
		protected void genDecrAndDetail() {
			StringBuilder strDescr = new StringBuilder();
			StringBuilder strDetail = new StringBuilder();
			if (_dropCurrency != null) {
				strDescr.append(_dropCurrency.currencyType.extName).append("、");
				strDetail.append(StringUtil.format("{}+{}、", KCurrencyTypeEnum.GOLD.extName, _dropCurrency.currencyCount));
			}
			strDescr.append(this.getDropDescr());
			strDetail.append(this.getDropDetail());
			if(strDescr.length() > 0 && strDescr.lastIndexOf("、") == strDescr.length() - 1) {
				strDescr.deleteCharAt(strDescr.length() - 1);
			}
			if(strDetail.length() > 0 && strDetail.lastIndexOf("、") == strDetail.length() - 1) {
				strDetail.deleteCharAt(strDetail.length() - 1);
			}
			this._descr = strDescr.toString();
			this._detail = strDetail.toString();
		}
		
		@Override
		public final int getSerialId() {
			return _serialId;
		}
		
		@Override
		public final int getResId() {
			return _resId;
		}
		
		@Override
		public final byte getType() {
			return _type;
		}
		
		@Override
		public void packAdditionalInfoToMsg(KGameMessage msg) {
			
		}
		
		@Override
		public final void executeReward(ICombat combat, ICombatMember member, ICombatAdditionalReward reward, long happenTime) {
			if (this._dropCurrency !=null ) {
				reward.addCurrencyReward(_dropCurrency.currencyType, (int)_dropCurrency.currencyCount);
			}
			this.notifyExecuteReward(combat, member, reward, happenTime);
		}
		
		@Override
		public final String getDescr() {
			return this._descr;
		}
		
		@Override
		public String getDetail() {
			return this._detail;
		}
		
	}
	
	private static class KCombatCurrencyDropInfo extends KCombatDropInfoBaseImpl {

		KCombatCurrencyDropInfo(KCurrencyTypeEnum type, int count, int pResId) {
			super(DROP_TYPE_COMMON);
			this.setDropCurrency(type, count);
			this.setResId(pResId);
			this.genDecrAndDetail();
		}

		@Override
		protected void notifyExecuteReward(ICombat combat, ICombatMember member, ICombatAdditionalReward reward, long happenTime) {

		}

		@Override
		protected String getDropDescr() {
			return "";
		}

		@Override
		protected String getDropDetail() {
			return "";
		}

	}
	
	private static class KCombatItemDropInfo extends KCombatDropInfoBaseImpl {
		
		private Map<String, Integer> _items; // 掉落的道具
		
		public KCombatItemDropInfo(Map<String, Integer> pItemMap, int pResId) {
			super(DROP_TYPE_COMMON);
			this._items = new HashMap<String, Integer>(pItemMap);
			this.setResId(pResId);
			this.genDecrAndDetail();
		}

		public KCombatItemDropInfo(ICombatDropInfoTemplate template) {
			super(template);
			this._items = template.getDropItems();
			this.genDecrAndDetail();
		}
		
		@Override
		protected String getDropDescr() {
			StringBuilder builder = new StringBuilder();
			if (this._items.size() > 0) {
				Map.Entry<String, Integer> entry;
				for (Iterator<Map.Entry<String, Integer>> itr = _items.entrySet().iterator(); itr.hasNext();) {
					entry = itr.next();
					builder.append(KSupportFactory.getItemModuleSupport().getItemTemplate(entry.getKey()).extItemName).append("、");
				}
			}
			return builder.toString();
		}
		
		@Override
		protected String getDropDetail() {
			StringBuilder builder = new StringBuilder();
			if (this._items.size() > 0) {
				Map.Entry<String, Integer> entry;
				for (Iterator<Map.Entry<String, Integer>> itr = _items.entrySet().iterator(); itr.hasNext();) {
					entry = itr.next();
					builder.append(KSupportFactory.getItemModuleSupport().getItemTemplate(entry.getKey()).extItemName).append("+").append(entry.getValue()).append("、");
				}
			}
			return builder.toString();
		}

		@Override
		protected void notifyExecuteReward(ICombat combat, ICombatMember member, ICombatAdditionalReward reward, long happenTime) {
			if (this._items.size() > 0) {
				Map.Entry<String, Integer> entry;
				for (Iterator<Map.Entry<String, Integer>> itr = this._items.entrySet().iterator(); itr.hasNext();) {
					entry = itr.next();
					reward.addItemReward(entry.getKey(), entry.getValue());
				}
			}
		}
	}
	
	private static class KCombatPetDropInfo extends KCombatDropInfoBaseImpl {

		private Map<Integer, Integer> _pets; // 掉落的随从

		public KCombatPetDropInfo(ICombatDropInfoTemplate template) {
			super(template);
			this._pets = template.getDropPets();
			this.genDecrAndDetail();
		}
		
		@Override
		protected String getDropDescr() {
			StringBuilder builder = new StringBuilder();
			if (this._pets.size() > 0) {
				Map.Entry<Integer, Integer> entry;
				for (Iterator<Map.Entry<Integer, Integer>> itr = _pets.entrySet().iterator(); itr.hasNext();) {
					entry = itr.next();
					builder.append(KSupportFactory.getPetModuleSupport().getPetTemplate(entry.getKey()).defaultName).append("、");
				}
			}
			return builder.toString();
		}
		
		@Override
		protected String getDropDetail() {
			StringBuilder builder = new StringBuilder();
			if (this._pets.size() > 0) {
				Map.Entry<Integer, Integer> entry;
				for (Iterator<Map.Entry<Integer, Integer>> itr = _pets.entrySet().iterator(); itr.hasNext();) {
					entry = itr.next();
					builder.append(KSupportFactory.getPetModuleSupport().getPetTemplate(entry.getKey()).defaultName).append("+").append(entry.getValue()).append("、");
				}
			}
			return builder.toString();
		}

		@Override
		protected void notifyExecuteReward(ICombat combat, ICombatMember member, ICombatAdditionalReward reward, long happenTime) {
			if (this._pets.size() > 0) {
				Map.Entry<Integer, Integer> entry;
				for (Iterator<Map.Entry<Integer, Integer>> itr = this._pets.entrySet().iterator(); itr.hasNext();) {
					entry = itr.next();
					reward.addAdditionalPetReward(entry.getKey(), entry.getValue());
				}
			}
		}
	}
	
	private static class KCombatStateDropInfo extends KCombatDropInfoBaseImpl {

		private int _stateId; // 增加的状态id

		public KCombatStateDropInfo(ICombatDropInfoTemplate template) {
			super(template);
			this._stateId = template.getDropStateId();
			this.genDecrAndDetail();
		}
		
		@Override
		protected String getDropDescr() {
			return KSupportFactory.getSkillModuleSupport().getStateTemplate(_stateId).getStateName();
		}
		
		@Override
		protected String getDropDetail() {
			return KSupportFactory.getSkillModuleSupport().getStateTemplate(_stateId).getStateName();
		}

		@Override
		public void packAdditionalInfoToMsg(KGameMessage msg) {
			msg.writeInt(_stateId);
		}
		
		@Override
		protected void notifyExecuteReward(ICombat combat, ICombatMember member, ICombatAdditionalReward reward, long happenTime) {
			if (member.getMemberType() == ICombatMember.MEMBER_TYPE_ROLE && member.isHang()) {
				ICombatMember mountMember = combat.getInUseMount(member);
				if (mountMember != null) {
					// 如果是在坐骑上面，要切换为坐骑
					member = mountMember;
				}
			}
			member.getSkillActor().addState(member, _stateId, happenTime);
		}
	}
	
	
	private static class KCombatHpDropInfo extends KCombatDropInfoBaseImpl {

		private int _hpPct; // 增加的生命值（万分比）

		public KCombatHpDropInfo(ICombatDropInfoTemplate template) {
			super(template);
			this._hpPct = template.getDropHp();
			this.genDecrAndDetail();
		}
		
		@Override
		protected String getDropDescr() {
			return KGameAttrType.HP.getName();
		}
		
		@Override
		protected String getDropDetail() {
			return StringUtil.format("{}+{}%", KGameAttrType.HP.getName(), Math.round((float) this._hpPct / UtilTool.HUNDRED_RATIO_UNIT));
		}
		
		@Override
		public void packAdditionalInfoToMsg(KGameMessage msg) {
			msg.writeInt(_hpPct);
		}

		@Override
		protected void notifyExecuteReward(ICombat combat, ICombatMember member, ICombatAdditionalReward reward, long happenTime) {
			if (member.getMemberType() == ICombatMember.MEMBER_TYPE_ROLE && member.isHang()) {
				ICombatMember mountMember = combat.getInUseMount(member);
				if (mountMember != null) {
					member = mountMember;
				}
			}
			int increaseHp = (int) UtilTool.calculateTenThousandRatioL(member.getMaxHp(), _hpPct);
			member.increaseHp(increaseHp);
			ICombat.LOGGER.info("战斗单位：{}，补给HP：{}", member.getName(), increaseHp);
		}
	}
	
	private static class KCombatEnergyDropInfo extends KCombatDropInfoBaseImpl {

		private int _energy;

		public KCombatEnergyDropInfo(ICombatDropInfoTemplate template) {
			super(template);
			this._energy = template.getDropEnergy();
			this.genDecrAndDetail();
		}
		
		@Override
		protected String getDropDescr() {
			return KGameAttrType.ENERGY.getName();
		}
		
		@Override
		protected String getDropDetail() {
			return StringUtil.format("{}+{}", KGameAttrType.ENERGY.getName(), this._energy);
		}
		
		@Override
		public void packAdditionalInfoToMsg(KGameMessage msg) {
			msg.writeShort(_energy);
		}

		@Override
		protected void notifyExecuteReward(ICombat combat, ICombatMember member, ICombatAdditionalReward reward, long happenTime) {
			member.increaseEnergy(_energy);
		}
	}
}
