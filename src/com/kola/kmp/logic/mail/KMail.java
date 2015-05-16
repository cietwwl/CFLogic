package com.kola.kmp.logic.mail;

import java.util.LinkedHashMap;
import java.util.List;

import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONException;
import com.koala.thirdpart.json.JSONObject;
import com.kola.kgame.cache.mail.impl.KAMail;
import com.kola.kgame.cache.mail.impl.KAMailSet;
import com.kola.kgame.db.dataobject.DBMailData;
import com.kola.kmp.logic.mail.attachment.MailAttachmentAbs;
import com.kola.kmp.logic.mail.attachment.MailAttachmentFactory;
import com.kola.kmp.logic.mail.attachment.MailAttachmentItemCode;
import com.kola.kmp.logic.mail.attachment.MailAttachmentMoney;
import com.kola.kmp.logic.mail.attachment.MailAttachmentRoleAtt;
import com.kola.kmp.logic.mail.attachment.MailAttachmentTypeEnum;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-6 上午11:21:25
 * </pre>
 */
public class KMail extends KAMail {

	private String senderRoleName;
	private int[] picResIds;// 图片资源ID
	private String[] urlLinks;// 外链URL-富文本
	private LinkedHashMap<MailAttachmentTypeEnum, MailAttachmentAbs> attsMap = new LinkedHashMap<MailAttachmentTypeEnum, MailAttachmentAbs>(MailAttachmentTypeEnum.values().length);// 所有附件

	// /////////////////////////////////
	private static final String JSON_VER = "A";// 版本
	//
	private static final String JSON_CA_SENDER_NAME = "B";
	private static final String JSON_CA_PICS = "C";// 图片资源ID
	private static final String JSON_CA_LINKS = "D";// UI链接ID
	//
	private static final String JSON_ATT_ATTARRAY = "B";// 所有附件
	private static final String JSON_ATT_ATTARRAY_TYPE = "1";// 附件类型
	private static final String JSON_ATT_ATTARRAY_DATA = "2";// 内容附件

	KMail(KMailSet owner, int type, long senderRoleId, String senderRoleName, String title, String content, int[] picResIds, String[] uiLinks) {
		super(owner, type, senderRoleId, title, content);
		this.senderRoleName = senderRoleName;
		if (picResIds == null) {
			picResIds = new int[0];
		}
		if (uiLinks == null) {
			uiLinks = new String[0];
		}
		this.picResIds = picResIds;
		this.urlLinks = uiLinks;
		//
	}

	KMail(KMailSet owner, DBMailData dbdata) {
		super(owner, dbdata.getId(), dbdata.getCreateTimeMillis(), dbdata.getType(), dbdata.getSenderRoleId(), dbdata.getStatus(), dbdata.getTitle(), dbdata.getContent());
		// 以防父类【在子类的全局属性还没有执行初始化时】回调子类decodeCA方法导致NullPointerException
		decodeCA(dbdata.getCustomizeAttribute());
		decodeAttachments(dbdata.getAttachments());
	}

	private void decodeCA(String jsonCA) {
		// 由底层调用,解释出逻辑层数据
		try {
			JSONObject obj = new JSONObject(jsonCA);
			int ver = obj.getInt(JSON_VER);// 默认版本
			// CEND 暂时只有版本0
			switch (ver) {
			case 0:
				senderRoleName = obj.optString(JSON_CA_SENDER_NAME);
				decodeCAPics(obj.getJSONArray(JSON_CA_PICS));
				decodeCALinks(obj.getJSONArray(JSON_CA_LINKS));
				break;
			}
		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + owner._ownerId + " ----丢失数据，存在运行隐患！", ex);
		}
	}

	private void decodeCAPics(JSONArray array) throws JSONException {
		int len = array.length();
		picResIds = new int[len];
		for (int i = 0; i < len; i++) {
			picResIds[i] = array.getInt(i);
		}
	}

	private void decodeCALinks(JSONArray array) throws JSONException {
		int len = array.length();
		urlLinks = new String[len];
		for (int i = 0; i < len; i++) {
			urlLinks[i] = array.getString(i);
		}
	}

	@Override
	protected String encodeCA() {
		rwLock.lock();
		// 构造一个数据对象给底层
		try {
			JSONObject obj = new JSONObject();
			obj.put(JSON_VER, 0);
			// CEND 暂时只有版本0
			obj.put(JSON_CA_SENDER_NAME, senderRoleName);
			obj.put(JSON_CA_PICS, encodeCAPics());
			obj.put(JSON_CA_LINKS, encodeCALinks());
			return obj.toString();
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + owner._ownerId + " ----丢失数据！", ex);
			return "";
		} finally {
			rwLock.unlock();
		}
	}

	private JSONArray encodeCAPics() throws JSONException {
		JSONArray array = new JSONArray();
		for (int temp : picResIds) {
			array.put(temp);
		}
		return array;
	}

	private JSONArray encodeCALinks() throws JSONException {
		JSONArray array = new JSONArray();
		for (String temp : urlLinks) {
			array.put(temp);
		}
		return array;
	}

	private void decodeAttachments(String jsonAttachments) {
		// 由底层调用,解释出逻辑层数据
		try {
			JSONObject obj = new JSONObject(jsonAttachments);
			int ver = obj.getInt(JSON_VER);// 默认版本
			// CEND 暂时只有版本0
			switch (ver) {
			case 0:
				JSONArray attArray = obj.optJSONArray(JSON_ATT_ATTARRAY);
				if (attArray != null) {
					int len = attArray.length();
					for (int i = 0; i < len; i++) {
						JSONObject temp = attArray.getJSONObject(i);
						int type = temp.getInt(JSON_ATT_ATTARRAY_TYPE);
						Object data = temp.get(JSON_ATT_ATTARRAY_DATA);
						//
						MailAttachmentTypeEnum typeEnum = MailAttachmentTypeEnum.getEnum(type);
						MailAttachmentAbs att = MailAttachmentFactory.createAndInitAttachmentForDB(typeEnum, data);
						try {
							attsMap.put(att.type, att);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				break;
			}
		} catch (Exception ex) {
			_LOGGER.error("decode数据时发生错误 roleId=" + owner._ownerId + " ----丢失数据，存在运行隐患！", ex);
		}
	}

	@Override
	protected String encodeAttachments() {
		rwLock.lock();
		// 构造一个数据对象给底层
		try {
			JSONObject obj = new JSONObject();
			obj.put(JSON_VER, 0);
			// CEND 暂时只有版本0
			if (!attsMap.isEmpty()) {
				JSONArray attArray = new JSONArray();
				obj.put(JSON_ATT_ATTARRAY, attArray);

				for (MailAttachmentAbs att : attsMap.values()) {
					JSONObject temp = new JSONObject();
					attArray.put(temp);

					temp.put(JSON_ATT_ATTARRAY_TYPE, att.type.sign);
					temp.put(JSON_ATT_ATTARRAY_DATA, att.encodeCA());
				}
			}
			return obj.toString();
		} catch (Exception ex) {
			_LOGGER.error("encode数据时发生错误 roleId=" + owner._ownerId + " ----丢失数据！", ex);
			return "";
		} finally {
			rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 是否包含附件
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-21 下午9:00:21
	 * </pre>
	 */
	boolean isContainAttachments() {
		return !attsMap.isEmpty();
	}

	String getSenderRoleName() {
		return senderRoleName;
	}

	int[] getPicResIds() {
		return picResIds;
	}

	String[] getUiLinks() {
		return urlLinks;
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 添加新附件，会覆盖原有的同类型附件
	 * @param att
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-22 下午3:53:17
	 * </pre>
	 */
	MailAttachmentAbs addAttachment(MailAttachmentAbs att) {
		return attsMap.put(att.type, att);
	}

	MailAttachmentAbs addAttachment(MailAttachmentTypeEnum attType) {
		return attsMap.get(attType);
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 直接获取缓存，谨慎使用
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-22 下午3:48:56
	 * </pre>
	 */
	public LinkedHashMap<MailAttachmentTypeEnum, MailAttachmentAbs> getAllAttachmentsCache() {
		return attsMap;
	}

}
