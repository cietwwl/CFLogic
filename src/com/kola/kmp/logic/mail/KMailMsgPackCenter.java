package com.kola.kmp.logic.mail;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.timer.Timer;

import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.mail.attachment.MailAttachmentAbs;
import com.kola.kmp.logic.mail.attachment.MailAttachmentTypeEnum;

/**
 * <pre>
 * 由于在打包模块消息的过程中，涉及到访问及复杂的逻辑结构
 * 因此设立此类，用于打包消息
 * 
 * @author CamusHuang
 * @creation 2013-1-5 下午12:03:37
 * </pre>
 */
public class KMailMsgPackCenter {

	private static final KGameLogger logger = KGameLogger.getLogger(KMailMsgPackCenter.class);

	/**
	 * <pre>
	 * 将全部邮件信息写入一条既有的消息里面
	 * 
	 * @param msg
	 * @param set
	 * @author CamusHuang
	 * @creation 2014-2-22 下午6:20:55
	 * </pre>
	 */
	public static void packAllMails(long roleId, KGameMessage msg) {

		KMailSet set = KMailModuleExtension.getMailSet(roleId);

		int count = 0;
		int writerindexmark = msg.writerIndex();
		msg.writeShort(count);// 先写后面再改

		set.rwLock.lock();
		try {
			Map<Long, KMail> map = set.getAllMailsCache();
			// 按从旧到新的原则，扫描清理警戒容量以内的邮件
			int scanCount = 0;// 扫描数量计数
			for (KMail mail : map.values()) {
				try {
					packMail(msg, mail);
					count++;
				} finally {
					scanCount++;
					if (scanCount >= KMailConfig.getInstance().MailBoxWarnSize) {
						// 最多只扫描警戒容量
						break;
					}
				}
			}
			msg.setShort(writerindexmark, count);
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 将一个邮件信息写入一条既有的消息里面
	 * 
	 * @param msg
	 * @param mail
	 * @author CamusHuang
	 * @creation 2014-2-22 下午6:21:22
	 * </pre>
	 */
	public static void packMail(KGameMessage msg, KMail mail) {
		/**
		 * <pre>
		 * 一封邮件的详细数据
		 * 
		 *  long 邮件ID
		 *  byte 邮件类型
		 *  boolean 是否已读
		 *  long 发送者角色ID
		 *  String 发送者角色名
		 *  String 发送时间
		 *  short 剩余天数
		 *  String 标题
		 *  String 正文
		 *  
		 *  byte 图片资源数量，0表示不包含附件
		 *  for(0~N){
		 *  	int　图片资源ID
		 *  }
		 *  
		 *  byte 链接数量
		 *  for(0~N){
		 *  	int　链接ID
		 *  }
		 *  
		 *  byte 附件类型数量M，0表示不包含附件
		 *  for(0~M){
		 *    byte 附件类型（1道具，2货币，3角色属性，4时装，5随从）
		 *    byte 附件数量N，0表示不包含附件
		 *    for(0~N){
		 *    	if(1){
		 *    		参考{@link KItemProtocol#MSG_STRUCT_ITEM_DETAILS}
		 *   	} else if(2){
		 *    		byte 货币类型
		 *    		int 货币数量
		 *    	} else if(3){
		 *    		String 名称
		 *    		float 数量
		 *   	} else if(4){
		 *    		参考{@link KFashionProtocol#MSG_STRUCT_FASHION_DETAILS}
		 *   	} else if(5){
		 *    		参考{@link KPetProtocol#PET_MSG_DATA_STRUCTURE}
		 *   	}
		 *    }
		 *  }
		 * </pre>
		 */
		msg.writeLong(mail._id);
		msg.writeByte(mail._type);
		msg.writeBoolean(mail.getStatus() == KMail.STATUS_READED);
		msg.writeLong(mail._senderRoleId);
		if (mail._type == KMail.TYPE_SYSTEM) {
			msg.writeUtf8String(KMailConfig.getInstance().SYS_MAIL_SENDER_NAME);
		} else {
			msg.writeUtf8String(mail.getSenderRoleName());
		}
		msg.writeUtf8String(UtilTool.DATE_FORMAT.format(new Date(mail._createTime)));
		//
		{
			long endTime = mail._createTime+KMailConfig.getInstance().OutDateTimeInMills;
			int releaseDay = (int)((endTime - System.currentTimeMillis())/Timer.ONE_DAY);
			releaseDay = Math.max(1,releaseDay);
			msg.writeShort(releaseDay);
		}
		//
		msg.writeUtf8String(mail._title);
		msg.writeUtf8String(mail._content);

		{// 图片(s)
			int[] pics = mail.getPicResIds();
			msg.writeByte(pics.length);
			for (int pic : pics) {
				msg.writeInt(pic);
			}
		}

		{// 链接(s)
			String[] uiLinks = mail.getUiLinks();
			msg.writeByte(uiLinks.length);
			for (String url : uiLinks) {
				msg.writeUtf8String(url);
			}
		}

		{// 附件(s)
			LinkedHashMap<MailAttachmentTypeEnum, MailAttachmentAbs> attMap = mail.getAllAttachmentsCache();
			msg.writeByte(attMap.size());
			for (MailAttachmentAbs a : attMap.values()) {
				msg.writeByte(a.type.sign);
				a.packToMsg(msg);
			}
		}
	}


	/**
	 * <pre>
	 * 将全部邮件信息写入一条既有的消息里面
	 * 
	 * @param msg
	 * @param set
	 * @author CamusHuang
	 * @creation 2014-2-22 下午6:20:55
	 * </pre>
	 */
	public static void packAllMailsForGM(List<String> infos, long roleId) {

		StringBuffer sbf = new StringBuffer();
		sbf.append("ID").append('\t').append("发件人").append('\t').append("时间").append('\t').append("标题").append('\t').append("内容").append('\t').append("附件");
		infos.add(sbf.toString());

		KMailSet set = KMailModuleExtension.getMailSet(roleId);

		set.rwLock.lock();
		try {
			Map<Long, KMail> map = set.getAllMailsCache();
			for (KMail mail : map.values()) {
				infos.add(packMailForGM(mail));
			}
		} finally {
			set.rwLock.unlock();
		}
	}
	
	/**
	 * <pre>
	 * 将一个邮件信息写入一条既有的消息里面
	 * 
	 * @param msg
	 * @param mail
	 * @author CamusHuang
	 * @creation 2014-2-22 下午6:21:22
	 * </pre>
	 */
	public static String packMailForGM(KMail mail) {
		StringBuffer sbf = new StringBuffer();
		
		sbf.append(mail._id).append('\t');
		if (mail._type == KMail.TYPE_SYSTEM) {
			sbf.append(KMailConfig.getInstance().SYS_MAIL_SENDER_NAME).append('\t');
		} else {
			sbf.append(mail.getSenderRoleName()).append('\t');
		}
		sbf.append(UtilTool.DATE_FORMAT.format(new Date(mail._createTime))+(mail.getStatus() == KMail.STATUS_READED?"(已读)":"")).append('\t');
		sbf.append(mail._title).append('\t');
		sbf.append(mail._content).append('\t');

		{// 附件(s)
			LinkedHashMap<MailAttachmentTypeEnum, MailAttachmentAbs> attMap = mail.getAllAttachmentsCache();
			if(!attMap.isEmpty()){
				StringBuffer sbf2 = new StringBuffer();
				
				for (MailAttachmentAbs a : attMap.values()) {
					a.packToMsgForGM(sbf2);
					sbf2.append('；');
				}
				
				sbf.append(sbf2.toString());
			}
		}
		return sbf.toString();
	}	
}
