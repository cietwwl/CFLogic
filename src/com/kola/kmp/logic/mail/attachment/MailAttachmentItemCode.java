package com.kola.kmp.logic.mail.attachment;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGameMessage;
import com.koala.thirdpart.json.JSONArray;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 道具附件
 * 只用于可重叠、不含变化属性的道具
 * 
 * @author CamusHuang
 * @creation 2013-2-18 上午9:37:26
 * </pre>
 */
public class MailAttachmentItemCode extends MailAttachmentAbs {

	private List<ItemCountStruct> dataStructs = new ArrayList<ItemCountStruct>();

	// -------------------
	private static final int JSON_INDEX_TYPE = 0;// 道具编码
	private static final int JSON_INDEX_COUNT = 1;// 道具数量

	public MailAttachmentItemCode() {
		super(MailAttachmentTypeEnum.ITEMCODE);
	}

	public MailAttachmentItemCode(List<ItemCountStruct> dataStructs) {
		super(MailAttachmentTypeEnum.ITEMCODE);
		this.dataStructs = dataStructs;
	}

	public MailAttachmentItemCode(ItemCountStruct dataStruct) {
		super(MailAttachmentTypeEnum.ITEMCODE);
		this.dataStructs = new ArrayList<ItemCountStruct>();
		this.dataStructs.add(dataStruct);
	}

	public void decodeCA(Object json) throws Exception {
		JSONArray jsonCA = (JSONArray) json;
		// 由底层调用,解释出逻辑层数据
		int len = jsonCA.length();
		for (int i = 0; i < len; i++) {
			JSONArray temp = jsonCA.getJSONArray(i);
			KItemTempAbs itemTemp = KSupportFactory.getItemModuleSupport().getItemTemplate(temp.getString(JSON_INDEX_TYPE));
			if (itemTemp != null) {
				int count = temp.getInt(JSON_INDEX_COUNT);
				dataStructs.add(new ItemCountStruct(itemTemp, count));
			}
		}
	}

	@Override
	public JSONArray encodeCA() throws Exception {
		JSONArray jsonArray = new JSONArray();
		for (ItemCountStruct data : dataStructs) {
			JSONArray temp = new JSONArray();
			jsonArray.put(temp);
			temp.put(JSON_INDEX_TYPE, data.itemCode);
			temp.put(JSON_INDEX_COUNT, data.itemCount);
		}
		return jsonArray;
	}

	@Override
	public void packToMsg(KGameMessage msg) {
		msg.writeByte(dataStructs.size());
		for (ItemCountStruct data : dataStructs) {
			KItemMsgPackCenter.packItem(msg, data.getItemTemplate(), data.itemCount);
		}
	}
	
	
	@Override
	public void packToMsgForGM(StringBuffer sbf) {
		sbf.append("物品:");
		for (ItemCountStruct data : dataStructs) {
			sbf.append(data.getItemTemplate().name).append('x').append(data.itemCount).append('、');
		}
	}	

	/**
	 * <pre>
	 * 
	 * @deprecated 直接获取缓存，谨慎使用
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-1 下午5:12:03
	 * </pre>
	 */
	public List<ItemCountStruct> getDataStructCache() {
		return dataStructs;
	}
}
