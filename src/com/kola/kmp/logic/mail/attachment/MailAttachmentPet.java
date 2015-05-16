package com.kola.kmp.logic.mail.attachment;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGameMessage;
import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kmp.logic.pet.KPetTemplate;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 随从附件
 * 
 * @author CamusHuang
 * @creation 2013-2-18 上午9:37:26
 * </pre>
 */
public final class MailAttachmentPet extends MailAttachmentAbs {

	// 所有模板ID
	private List<Integer> dataStructs = new ArrayList<Integer>();

	// -----------------

	public MailAttachmentPet() {
		super(MailAttachmentTypeEnum.PET);
	}

	public MailAttachmentPet(List<Integer> dataStructs) {
		super(MailAttachmentTypeEnum.PET);
		this.dataStructs = dataStructs;
	}

	public MailAttachmentPet(int petTempId) {
		super(MailAttachmentTypeEnum.PET);
		this.dataStructs = new ArrayList<Integer>();
		this.dataStructs.add(petTempId);
	}

	public void decodeCA(Object json) throws Exception {
		JSONArray jsonCA = (JSONArray) json;
		// 由底层调用,解释出逻辑层数据
		int len = jsonCA.length();
		for (int i = 0; i < len; i++) {
			int tempId = jsonCA.getInt(i);
			KPetTemplate temp = KSupportFactory.getPetModuleSupport().getPetTemplate(tempId);
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

	@Override
	public void packToMsg(KGameMessage msg) {
		msg.writeByte(dataStructs.size());
		for (Integer dataStruct : dataStructs) {
			KSupportFactory.getPetModuleSupport().packPetTemplateMsg(msg, dataStruct);
		}
	}
	
	@Override
	public void packToMsgForGM(StringBuffer sbf) {
		sbf.append("随从:");
		for (Integer data : dataStructs) {
			KPetTemplate temp = KSupportFactory.getPetModuleSupport().getPetTemplate(data);
			sbf.append(temp.getName()).append('、');
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
