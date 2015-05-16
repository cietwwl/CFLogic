package com.kola.kmp.logic.mail.attachment;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGameMessage;
import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;

/**
 * <pre>
 * 货币附件
 * 
 * @author CamusHuang
 * @creation 2013-2-18 上午9:37:26
 * </pre>
 */
public final class MailAttachmentMoney extends MailAttachmentAbs {

	private PresentPointTypeEnum presentType;
	private List<KCurrencyCountStruct> dataStructs = new ArrayList<KCurrencyCountStruct>();

	// -----------------
	private static final String JSON_PRESENTTYPE = "A";// 功能类型
	private static final String JSON_DATA = "B";// 功能类型
	private static final int JSON_INDEX_TYPE = 0;// 货币类型
	private static final int JSON_INDEX_COUNT = 1;// 货币数量
	

	public MailAttachmentMoney() {
		super(MailAttachmentTypeEnum.MONEY);
	}
	
	public MailAttachmentMoney(List<KCurrencyCountStruct> dataStructs, PresentPointTypeEnum presentType) {
		super(MailAttachmentTypeEnum.MONEY);
		this.dataStructs = dataStructs;
		this.presentType = presentType;
	}
	
	public MailAttachmentMoney(KCurrencyCountStruct dataStruct, PresentPointTypeEnum presentType) {
		super(MailAttachmentTypeEnum.MONEY);
		this.dataStructs = new ArrayList<KCurrencyCountStruct>();
		this.dataStructs.add(dataStruct);
		this.presentType = presentType;
	}

	public void decodeCA(Object json) throws Exception {
		JSONObject jsonCA = (JSONObject) json;
		// 由底层调用,解释出逻辑层数据
		presentType = PresentPointTypeEnum.getEnum(jsonCA.optInt(JSON_PRESENTTYPE));
		{
			JSONArray jsonArray = jsonCA.getJSONArray(JSON_DATA);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONArray temp = jsonArray.getJSONArray(i);
				KCurrencyTypeEnum type = KCurrencyTypeEnum.getEnum(temp.getInt(JSON_INDEX_TYPE));
				if (type != null) {
					int count = temp.getInt(JSON_INDEX_COUNT);
					dataStructs.add(new KCurrencyCountStruct(type, count));
				}
			}
		}
	}

	@Override
	public JSONObject encodeCA() throws Exception {
		JSONObject jsonCA= new JSONObject();
		if(presentType!=null){
			jsonCA.put(JSON_PRESENTTYPE, presentType.funType);
		}
		JSONArray jsonArray = new JSONArray();
		jsonCA.put(JSON_DATA, jsonArray);
		for (KCurrencyCountStruct data : dataStructs) {
			JSONArray temp = new JSONArray();
			jsonArray.put(temp);
			temp.put(JSON_INDEX_TYPE, data.currencyType.sign);
			temp.put(JSON_INDEX_COUNT, data.currencyCount);
		}
		return jsonCA;
	}

	@Override
	public void packToMsg(KGameMessage msg) {
		msg.writeByte(dataStructs.size());
		for (KCurrencyCountStruct data : dataStructs) {
			msg.writeByte(data.currencyType.sign);
			msg.writeLong((int) data.currencyCount);
		}
	}
	
	@Override
	public void packToMsgForGM(StringBuffer sbf) {
		sbf.append("货币:");
		for (KCurrencyCountStruct data : dataStructs) {
			sbf.append(data.currencyType.name).append('x').append(data.currencyCount).append('、');
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
	public List<KCurrencyCountStruct> getDataStructCache() {
		return dataStructs;
	}

	public PresentPointTypeEnum getPresentType() {
		if (presentType == null) {
			return PresentPointTypeEnum.邮件附件;
		}
		return presentType;
	}

}
