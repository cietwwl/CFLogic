package com.kola.kmp.logic.mail.attachment;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGameMessage;
import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kmp.logic.fashion.KFashionDataManager;
import com.kola.kmp.logic.fashion.KFashionDataStructs.KFashionTemplate;
import com.kola.kmp.logic.fashion.KFashionMsgPackCenter;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 时装附件
 * 
 * @author CamusHuang
 * @creation 2013-2-18 上午9:37:26
 * </pre>
 */
public final class MailAttachmentFashion extends MailAttachmentAbs {

	// 所有模板ID
	private List<Integer> dataStructs = new ArrayList<Integer>();

	// -----------------

	public MailAttachmentFashion() {
		super(MailAttachmentTypeEnum.FASHION);
	}

	public MailAttachmentFashion(List<Integer> dataStructs) {
		super(MailAttachmentTypeEnum.FASHION);
		this.dataStructs = dataStructs;
	}

	public MailAttachmentFashion(int fashionTempId) {
		super(MailAttachmentTypeEnum.FASHION);
		this.dataStructs = new ArrayList<Integer>();
		this.dataStructs.add(fashionTempId);
	}

	public void decodeCA(Object json) throws Exception {
		JSONArray jsonCA = (JSONArray) json;
		// 由底层调用,解释出逻辑层数据
		int len = jsonCA.length();
		for (int i = 0; i < len; i++) {
			int tempId = jsonCA.getInt(i);
			KFashionTemplate temp = KSupportFactory.getFashionModuleSupport().getFashionTemplate(tempId);
			if (temp != null) {
				dataStructs.add(tempId);
			}
		}
	}

	@Override
	public JSONArray encodeCA() throws Exception {
		JSONArray jsonArray = new JSONArray();
		for (Integer data : dataStructs) {
			jsonArray.put(data);
		}
		return jsonArray;
	}

	public void packToMsg(KGameMessage msg) {
		int writeIndex = msg.writerIndex();
		int count = 0;
		
		msg.writeByte(dataStructs.size());
		for (Integer tempId : dataStructs) {
			if (KFashionMsgPackCenter.packFashion(msg, 0, tempId)) {
				count++;
			}
		}
		msg.setByte(writeIndex, count);
	}

	@Override
	public void packToMsgForGM(StringBuffer sbf) {
		sbf.append("时装:");
		for (Integer tempId : dataStructs) {
			KFashionTemplate temp = KFashionDataManager.mFashionTemplateManager.getFashionTemplate(tempId);
			if (temp == null) {
				continue;
			}
			sbf.append(temp.name).append("、");
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
	public List<Integer> getDataStructCache() {
		return dataStructs;
	}

}
