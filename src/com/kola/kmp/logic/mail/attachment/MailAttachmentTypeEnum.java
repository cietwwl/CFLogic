package com.kola.kmp.logic.mail.attachment;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 邮件附件类型
 * 
 * @author CamusHuang
 * @creation 2014-2-23 上午9:43:26
 * </pre>
 */
public enum MailAttachmentTypeEnum {
	ITEMCODE(1), // "道具编码及数量"),
	MONEY(2), // "货币"),
	ROLEATT(3),// "角色属性");
	FASHION(4),//时装
	PET(5),//随从
	;
	//
	// 标识数值
	public final int sign;

	private MailAttachmentTypeEnum(int sign) {
		this.sign = sign;
	}

	// 所有枚举
	private static final Map<Integer, MailAttachmentTypeEnum> TypeMap = new HashMap<Integer, MailAttachmentTypeEnum>();
	static {
		MailAttachmentTypeEnum[] enums = MailAttachmentTypeEnum.values();
		MailAttachmentTypeEnum type;
		for (int i = 0; i < enums.length; i++) {
			type = enums[i];
			TypeMap.put(type.sign, type);
		}
	}

	// //////////////////
	/**
	 * <pre>
	 * 通过标识数值获取类型枚举对象
	 * 
	 * @param sign
	 * @return
	 * @author CamusHuang
	 * @creation 2012-11-5 上午10:53:13
	 * </pre>
	 */
	public static MailAttachmentTypeEnum getEnum(int sign) {
		return TypeMap.get(sign);
	}
}
