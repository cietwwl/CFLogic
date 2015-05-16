package com.kola.kmp.logic.mail.attachment;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGameMessage;
import com.koala.thirdpart.json.JSONArray;
import com.kola.kmp.logic.other.KDataStructs.AttValueStruct;
import com.kola.kmp.logic.other.KGameAttrType;

/**
 * <pre>
 * 货币附件
 * 
 * @author CamusHuang
 * @creation 2013-2-18 上午9:37:26
 * </pre>
 */
public final class MailAttachmentRoleAtt extends MailAttachmentAbs {

	private List<AttValueStruct> dataStructs = new ArrayList<AttValueStruct>();

	// -----------------
	private static final int JSON_INDEX_TYPE = 0;// 属性类型
	private static final int JSON_INDEX_COUNT = 1;// 属性数量

	public MailAttachmentRoleAtt() {
		super(MailAttachmentTypeEnum.ROLEATT);
	}

	public MailAttachmentRoleAtt(List<AttValueStruct> dataStructs) {
		super(MailAttachmentTypeEnum.ROLEATT);
		this.dataStructs = dataStructs;
	}

	public MailAttachmentRoleAtt(AttValueStruct dataStruct) {
		super(MailAttachmentTypeEnum.ROLEATT);
		this.dataStructs = new ArrayList<AttValueStruct>();
		this.dataStructs.add(dataStruct);
	}

	public void decodeCA(Object json) throws Exception {
		JSONArray jsonCA = (JSONArray) json;
		// 由底层调用,解释出逻辑层数据
		int len = jsonCA.length();
		for (int i = 0; i < len; i++) {
			JSONArray temp = jsonCA.getJSONArray(i);
			KGameAttrType type = KGameAttrType.getAttrTypeEnum(temp.getInt(JSON_INDEX_TYPE));
			if (type != null) {
				int count = temp.getInt(JSON_INDEX_COUNT);
				dataStructs.add(new AttValueStruct(type, count));
			}
		}
	}

	@Override
	public JSONArray encodeCA() throws Exception {
		JSONArray jsonArray = new JSONArray();
		for (AttValueStruct data : dataStructs) {
			JSONArray temp = new JSONArray();
			jsonArray.put(temp);
			temp.put(JSON_INDEX_TYPE, data.roleAttType.sign);
			temp.put(JSON_INDEX_COUNT, data.addValue);
		}
		return jsonArray;
	}

	@Override
	public void packToMsg(KGameMessage msg) {
		msg.writeByte(dataStructs.size());
		for (AttValueStruct dataStruct : dataStructs) {
			msg.writeInt(dataStruct.roleAttType.sign);
			msg.writeFloat(dataStruct.addValue);
		}
	}
	
	@Override
	public void packToMsgForGM(StringBuffer sbf) {
		sbf.append("属性:");
		for (AttValueStruct dataStruct : dataStructs) {
			sbf.append(dataStruct.roleAttType.getName()).append('x').append(dataStruct.addValue).append('、');
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
	public List<AttValueStruct> getDataStructCache() {
		return dataStructs;
	}

}
