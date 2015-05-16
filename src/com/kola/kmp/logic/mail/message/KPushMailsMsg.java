package com.kola.kmp.logic.mail.message;

import java.util.Collections;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.mail.KMail;
import com.kola.kmp.logic.mail.KMailMsgPackCenter;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.protocol.item.KItemProtocol;
import com.kola.kmp.protocol.mail.KMailProtocol;

public class KPushMailsMsg implements KItemProtocol {
	/**
	 * <pre>
	 * 推送角色的所有邮件
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-2-27 下午4:53:42
	 * </pre>
	 */
	public static void pushAllMails(KRole receiver) {
		if (receiver.isOnline()) {
			KGameMessage msg = KGame.newLogicMessage(KMailProtocol.SM_SYN_MAILS);
			msg.writeShort(0);
			msg.writeShort(0);
			KMailMsgPackCenter.packAllMails(receiver.getId(), msg);
			receiver.sendMsg(msg);
		}
	}

	/**
	 * <pre>
	 * 同步新增的邮件
	 * 
	 * @param newMail
	 * @param receiver
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-22 下午5:12:45
	 * </pre>
	 */
	public static void synMail(KRole receiver, KMail newMail) {
		if (receiver.isOnline()) {
			KGameMessage msg = KGame.newLogicMessage(KMailProtocol.SM_SYN_MAILS);
			msg.writeShort(0);
			msg.writeShort(0);
			msg.writeShort(1);
			KMailMsgPackCenter.packMail(msg, newMail);
			receiver.sendMsg(msg);
		}
	}

	/**
	 * <pre>
	 * 同步删除、新增的邮件
	 * 
	 * @param receiver
	 * @param deleteMails
	 * @param newMails
	 * @author CamusHuang
	 * @creation 2014-2-22 下午6:07:55
	 * </pre>
	 */
	public static void synMails(KRole receiver, List<Long> deleteMails, List<KMail> newMails) {
		if (receiver.isOnline()) {
			KGameMessage msg = KGame.newLogicMessage(KMailProtocol.SM_SYN_MAILS);
			if (deleteMails == null) {
				deleteMails = Collections.emptyList();
			}
			msg.writeShort(deleteMails.size());
			for (Long mailId : deleteMails) {
				msg.writeLong(mailId);
			}

			List<Long> deleteAttMails = Collections.emptyList();
			msg.writeShort(deleteAttMails.size());
			for (Long mailId : deleteAttMails) {
				msg.writeLong(mailId);
			}

			if (newMails == null) {
				newMails = Collections.emptyList();
			}
			msg.writeShort(newMails.size());
			for (KMail mail : newMails) {
				KMailMsgPackCenter.packMail(msg, mail);
			}
			receiver.sendMsg(msg);
		}
	}
}
