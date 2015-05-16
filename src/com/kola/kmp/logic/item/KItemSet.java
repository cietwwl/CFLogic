package com.kola.kmp.logic.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.item.impl.KAItemPack;
import com.kola.kgame.cache.item.impl.KAItemSet;
import com.kola.kgame.db.dataobject.DBItem;
import com.kola.kmp.logic.other.KItemPackTypeEnum;

public class KItemSet extends KAItemSet<KItem, KAItemPack<KItem>> {

	private KItemSetCA ca;

	KItemSet(long roleId, boolean isFirstNew) {
		super(roleId, isFirstNew);

		// 必须在roleId初始化之后才能NEW CA
		ca = new KItemSetCA(this, isFirstNew);
	}

	@Override
	protected Map<Integer, KAItemPack<KItem>> initItemPacks(boolean isFirstNew) {
		// 建立所有类型的道具包
		Map<Integer, KAItemPack<KItem>> allPacksMap = new HashMap<Integer, KAItemPack<KItem>>();
		for (KItemPackTypeEnum type : KItemPackTypeEnum.values()) {
			KAItemPack<KItem> pack = newItemPack(_roleId, type, isFirstNew);
			allPacksMap.put((int) type.sign, pack);
		}
		return allPacksMap;
	}

	private KAItemPack<KItem> newItemPack(long roleId, KItemPackTypeEnum packTypeEnum, boolean isFirstNew) {
		switch (packTypeEnum) {
		case BAG:
			return new KItemPack_Bag(this, roleId, packTypeEnum, isFirstNew);
		case BODYSLOT:
		default:
			return new KItemPack_BodySlot(this, roleId, packTypeEnum, isFirstNew);
		}
	}

	@Override
	protected List<KItem> initItems(List<DBItem> dbdatas) {
		List<KItem> result = new ArrayList<KItem>();
		for (DBItem dbdata : dbdatas) {
			result.add(new KItem(dbdata));
		}
		return result;
	}

	protected void decodeCA(JSONObject caObj) {
		ca.decodeAttribute(caObj);
	}

	protected JSONObject encodeCA() {
		return ca.encodeAttribute();
	}

	public KAItemPack<KItem> getPackByEnum(KItemPackTypeEnum packTypeEnum) {
		return getItemPack(packTypeEnum.sign);
	}

	public KItemPack_Bag getBag() {
		return (KItemPack_Bag) getItemPack(KItemPackTypeEnum.BAG.sign);
	}

	public KItemPack_BodySlot getSlot() {
		return (KItemPack_BodySlot) getItemPack(KItemPackTypeEnum.BODYSLOT.sign);
	}

	public String getClientData() {
		return ca.clientData;
	}

	public void setClientData(String data) {
		ca.clientData = data;
		super.notifyDB();
	}
	
	public AtomicInteger getUpStartFailTime(int star){
		AtomicInteger count = ca.upstarTimeMap.get(star);
		if(count==null){
			count = new AtomicInteger();
			ca.upstarTimeMap.put(star, count);
		}
		return count;
	}

	public static class KItemSetCA implements KItemPackCustomizeAttribute {

		final KItemSet owner;
		//
		private String clientData = "";// 客户端数据
		// <星数,升星连续失败次数> 升星保护
		private Map<Integer, AtomicInteger> upstarTimeMap = new ConcurrentHashMap<Integer, AtomicInteger>();

		private static final String JSON_VER = "0";// 版本

		//
		private static final String JSON_CLIENTDATA = "A";
		private static final String JSON_STARTTIME = "B";

		KItemSetCA(KItemSet owner, boolean isFirstNew) {
			this.owner = owner;
		}

		@Override
		public void decodeAttribute(JSONObject obj) {
			if (obj == null) {
				return;
			}
			// 由底层调用,解释出逻辑层数据
			owner.rwLock.lock();
			try {
				int ver = obj.getInt(JSON_VER);// 默认版本
				// CEND 道具--暂时只有版本0
				switch (ver) {
				case 0:
					clientData = obj.optString(JSON_CLIENTDATA);
					//
					decodeStarTime(obj.optJSONObject(JSON_STARTTIME));

					break;
				}

			} catch (Exception ex) {
				_LOGGER.error("decode数据时发生错误 roleId=" + owner._roleId + " ----丢失数据！", ex);
			} finally {
				owner.rwLock.unlock();
			}
		}
		
		private void decodeStarTime(JSONObject json) throws Exception {
			if(json==null){
				return;
			}
			for(Iterator<String> it =json.keys();it.hasNext();){
				String key=it.next();
				int count = json.getInt(key);
				if(count>0){
					upstarTimeMap.put(Integer.parseInt(key), new AtomicInteger(count));
				}
			}
		}

		public JSONObject encodeAttribute() {
			owner.rwLock.lock();
			try {
				JSONObject obj = new JSONObject();
				obj.put(JSON_VER, 0);// 默认版本
				// CEND 道具--暂时只有版本0
				obj.put(JSON_CLIENTDATA, clientData);
				// 
				obj.put(JSON_STARTTIME, encodeStarTime());
				return obj;
			} catch (Exception ex) {
				_LOGGER.error("encode数据时发生错误roleId=" + owner._roleId + " ----丢失数据！", ex);
				return null;
			} finally {
				owner.rwLock.unlock();
			}
		}
		
		private JSONObject encodeStarTime() throws Exception {
			JSONObject json = new JSONObject();
			//
			for (Entry<Integer, AtomicInteger> e : upstarTimeMap.entrySet()) {
				if (e.getValue().get() > 0) {
					json.put(e.getKey().intValue() + "", e.getValue().get());
				}
			}
			return json;
		}
	}

}
