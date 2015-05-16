package com.kola.kmp.logic.mail;

import javax.management.timer.Timer;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.GlobalTips;

/**
 * <pre>
 * 模块配置表
 * 
 * @author CamusHuang
 * @creation 2013-12-8 下午5:08:45
 * </pre>
 */
public class KMailConfig {
	private static KMailConfig instance;

	public static final long SYS_MAIL_SENDER_ID = 1;
	public static String SYS_MAIL_SENDER_NAME = "系统";
	public static String SYS_MAIL_SENDER_EXTNAME = HyperTextTool.extRoleName(SYS_MAIL_SENDER_NAME);

	/** 邮箱警戒容量（达到警戒容量，则按从旧到新的原则，扫描清理警戒容量以内的、指定数量、无附件的邮件（登陆时检查）） */
	final int MailBoxWarnSize;// = 100;
	/** 达到警戒容量时的清理数量 */
	final int ClearOfWarnSize;// = 20;

	/** 邮件超时时长(一旦超时，无条件清理) */
	final long OutDateTimeInMills;// = 30L * Timer.ONE_DAY;
	
	
	/** 发送军团邮件最大CD时长 */
	final long SendGangMailMaxCDInMills;

	/**  每发送一个邮件的CD时长 */
	final long SendMailCDInMillsPerOne;
	
	/** 发送好友邮件名单最大长度 */
	final long SendMailNameListMaxForFriend;

	private KMailConfig(Element logicE) throws KGameServerException {
		MailBoxWarnSize = Integer.parseInt(logicE.getChildTextTrim("MailBoxWarnSize"));
		if (MailBoxWarnSize <= 10) {
			throw new KGameServerException("加载模块配置文件错误：MailBoxWarnSize 有误！");
		}
		
		ClearOfWarnSize = Integer.parseInt(logicE.getChildTextTrim("ClearOfWarnSize"));
		if (ClearOfWarnSize <= 0 || ClearOfWarnSize >= MailBoxWarnSize) {
			throw new KGameServerException("加载模块配置文件错误：ClearOfWarnSize 有误！");
		}

		OutDateTimeInMills = UtilTool.parseDHMS(logicE.getChildTextTrim("OutDateTimeInMills"));
		if (OutDateTimeInMills <= Timer.ONE_MINUTE) {
			throw new KGameServerException("加载模块配置文件错误：OutDateTimeInMills 有误！");
		}
		
		SendGangMailMaxCDInMills = UtilTool.parseDHMS(logicE.getChildTextTrim("SendGangMailMaxCDInMills"));
		if (SendGangMailMaxCDInMills < Timer.ONE_MINUTE) {
			throw new KGameServerException("加载模块配置文件错误：SendGangMailMaxCDInMills 有误！");
		}
		SendMailCDInMillsPerOne = UtilTool.parseDHMS(logicE.getChildTextTrim("SendMailCDInMillsPerOne"));
		if (SendMailCDInMillsPerOne < Timer.ONE_SECOND) {
			throw new KGameServerException("加载模块配置文件错误：SendMailCDInMillsPerOne 有误！");
		}
		SendMailNameListMaxForFriend = Integer.parseInt(logicE.getChildTextTrim("SendMailNameListMaxForFriend"));
		if (SendMailNameListMaxForFriend > 10 || SendMailNameListMaxForFriend<1) {
			throw new KGameServerException("加载模块配置文件错误：SendMailNameListMaxForFriend 有误！");
		}
		
		SYS_MAIL_SENDER_NAME = GlobalTips.系统;
		SYS_MAIL_SENDER_EXTNAME = HyperTextTool.extRoleName(SYS_MAIL_SENDER_NAME);
	}

	public static void init(Element logicE) throws KGameServerException {
		instance = new KMailConfig(logicE);
	}

	public static KMailConfig getInstance() {
		return instance;
	}
}
