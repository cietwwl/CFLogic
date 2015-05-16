package com.kola.kmp.logic.mail.attachment;

import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.koala.thirdpart.json.JSONArray;
import com.koala.thirdpart.json.JSONObject;

public abstract class MailAttachmentAbs {

	public static final KGameLogger logger = KGameLogger.getLogger(MailAttachmentAbs.class);

	//
	private int id;// 各邮件独立编号
	public final MailAttachmentTypeEnum type;

	public MailAttachmentAbs(MailAttachmentTypeEnum type) {
		this.type = type;
	}

	public abstract Object encodeCA() throws Exception;

	public abstract void decodeCA(Object jsonCA) throws Exception;

	/**
	 * <pre>
	 * 将附件内容写到一条返回给客户端的消息里面，客户端根据内容做显示
	 * 
	 * @param respmsg
	 * @return
	 * @author CamusHuang
	 * @creation 2013-6-19 下午3:03:30
	 * </pre>
	 */
	public abstract void packToMsg(KGameMessage respmsg);
	
	/**
	 * <pre>
	 * 为GM打包
	 * 
	 * @param sbf
	 * @author CamusHuang
	 * @creation 2014-10-21 下午3:05:57
	 * </pre>
	 */
	public abstract void packToMsgForGM(StringBuffer sbf);

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
