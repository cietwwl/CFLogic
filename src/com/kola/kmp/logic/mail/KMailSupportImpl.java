package com.kola.kmp.logic.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.logging.KGameLogger;
import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.mail.Mail;
import com.kola.kmp.logic.mail.attachment.MailAttachmentAbs;
import com.kola.kmp.logic.mail.attachment.MailAttachmentFashion;
import com.kola.kmp.logic.mail.attachment.MailAttachmentItemCode;
import com.kola.kmp.logic.mail.attachment.MailAttachmentMoney;
import com.kola.kmp.logic.mail.attachment.MailAttachmentPet;
import com.kola.kmp.logic.mail.attachment.MailAttachmentRoleAtt;
import com.kola.kmp.logic.mail.attachment.MailAttachmentTypeEnum;
import com.kola.kmp.logic.mail.message.KPushMailsMsg;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.MailModuleSupport;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.ResultStructs.MailResult;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.MailTips;

/**
 * 游戏系统支撑类
 * 
 * @author AHONG
 * 
 */
public class KMailSupportImpl implements MailModuleSupport {

	private static final KGameLogger logger = KGameLogger.getLogger(KMailSupportImpl.class);

	public boolean sendAttMailBySystem(long receiverRoleId, String itemCode, long count, String title, String content) {
		//
		KMailSet set = KMailModuleExtension.getMailSet(receiverRoleId);
		if (set == null) {
			return false;
		}
		//
		KMail mail = new KMail(set, KMail.TYPE_SYSTEM, KMailConfig.SYS_MAIL_SENDER_ID, KMailConfig.SYS_MAIL_SENDER_NAME, title, content, null, null);
		//
		ItemCountStruct struct = new ItemCountStruct(KSupportFactory.getItemModuleSupport().getItemTemplate(itemCode), count);
		MailAttachmentItemCode att = new MailAttachmentItemCode(struct);
		mail.addAttachment(att);
		//
		KRole receiverRole = KSupportFactory.getRoleModuleSupport().getRole(receiverRoleId);
		return KMailLogic.sendMailFinally(receiverRole, set, mail).isSucess;
	}

	public boolean sendAttMailBySystem(long receiverRoleId, ItemCountStruct itemStruct, String title, String content) {
		//
		KMailSet set = KMailModuleExtension.getMailSet(receiverRoleId);
		if (set == null) {
			return false;
		}
		//
		KMail mail = new KMail(set, KMail.TYPE_SYSTEM, KMailConfig.SYS_MAIL_SENDER_ID, KMailConfig.SYS_MAIL_SENDER_NAME, title, content, null, null);
		//
		MailAttachmentItemCode att = new MailAttachmentItemCode(itemStruct);
		mail.addAttachment(att);
		//
		KRole receiverRole = KSupportFactory.getRoleModuleSupport().getRole(receiverRoleId);
		return KMailLogic.sendMailFinally(receiverRole, set, mail).isSucess;
	}

	public MailResult sendAttMailBySystem(long receiverRoleId, List<ItemCountStruct> itemList, String title, String content) {
		//
		KMailSet set = KMailModuleExtension.getMailSet(receiverRoleId);
		if (set == null) {
			MailResult result = new MailResult();
			result.isSucess = false;
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			return result;
		}

		KMail mail = new KMail(set, KMail.TYPE_SYSTEM, KMailConfig.SYS_MAIL_SENDER_ID, KMailConfig.SYS_MAIL_SENDER_NAME, title, content, null, null);
		//
		MailAttachmentItemCode att = new MailAttachmentItemCode(itemList);
		mail.addAttachment(att);
		//
		KRole receiverRole = KSupportFactory.getRoleModuleSupport().getRole(receiverRoleId);
		return KMailLogic.sendMailFinally(receiverRole, set, mail);
	}

	public MailResult sendAttMailBySystem(long receiverRoleId, BaseMailRewardData mailReward, PresentPointTypeEnum presentType) {
		if (mailReward == null || mailReward.baseMail == null) {
			MailResult result = new MailResult();
			result.isSucess = false;
			result.tips = MailTips.没有邮件需要发送;
			return result;
		}

		BaseRewardData reward = mailReward.baseRewardData;
		return sendAttMailBySystem(receiverRoleId, mailReward.baseMail, reward, presentType);
	}

	public MailResult sendAttMailBySystem(long receiverRoleId, BaseMailContent mainContent, BaseRewardData reward, PresentPointTypeEnum presentType) {
		if (mainContent == null) {
			MailResult result = new MailResult();
			result.isSucess = false;
			result.tips = MailTips.没有邮件需要发送;
			return result;
		}

		//
		KMailSet set = KMailModuleExtension.getMailSet(receiverRoleId);
		if (set == null) {
			MailResult result = new MailResult();
			result.isSucess = false;
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			return result;
		}
		//
		KMail mail = new KMail(set, KMail.TYPE_SYSTEM, KMailConfig.SYS_MAIL_SENDER_ID, KMailConfig.SYS_MAIL_SENDER_NAME, mainContent.getMailTitle(), mainContent.getMailContent(),
				mainContent.getPicResIds(), mainContent.getUrlLinks());

		if (reward != null) {
			if (reward.attList != null && !reward.attList.isEmpty()) {
				MailAttachmentRoleAtt att = new MailAttachmentRoleAtt(reward.attList);
				mail.addAttachment(att);
			}

			if (reward.moneyList != null && !reward.moneyList.isEmpty()) {
				MailAttachmentMoney att = new MailAttachmentMoney(reward.moneyList, presentType);
				mail.addAttachment(att);
			}

			if (reward.itemStructs != null && !reward.itemStructs.isEmpty()) {
				MailAttachmentItemCode att = new MailAttachmentItemCode(reward.itemStructs);
				mail.addAttachment(att);
			}

			if (reward.fashionTempIdList != null && !reward.fashionTempIdList.isEmpty()) {
				MailAttachmentFashion att = new MailAttachmentFashion(reward.fashionTempIdList);
				mail.addAttachment(att);
			}

			if (reward.petTempIdList != null && !reward.petTempIdList.isEmpty()) {
				MailAttachmentPet att = new MailAttachmentPet(reward.petTempIdList);
				mail.addAttachment(att);
			}
		}

		KRole receiverRole = KSupportFactory.getRoleModuleSupport().getRole(receiverRoleId);
		return KMailLogic.sendMailFinally(receiverRole, set, mail);
	}

	public MailResult sendMoneyMailBySystem(long receiverRoleId, List<KCurrencyCountStruct> moneyList, PresentPointTypeEnum presentType, String title, String content) {
		//
		KMailSet set = KMailModuleExtension.getMailSet(receiverRoleId);
		if (set == null) {
			MailResult result = new MailResult();
			result.isSucess = false;
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			return result;
		}
		KMail mail = new KMail(set, KMail.TYPE_SYSTEM, KMailConfig.SYS_MAIL_SENDER_ID, KMailConfig.SYS_MAIL_SENDER_NAME, title, content, null, null);
		//
		MailAttachmentMoney att = new MailAttachmentMoney(moneyList, presentType);
		mail.addAttachment(att);
		//
		KRole receiverRole = KSupportFactory.getRoleModuleSupport().getRole(receiverRoleId);
		return KMailLogic.sendMailFinally(receiverRole, set, mail);
	}

	public MailResult sendSimpleMail(long senderRoleId, String senderRoleName, long receiverRoleId, int mailType, String title, String content) {
		KMailSet set = KMailModuleExtension.getMailSet(receiverRoleId);
		if (set == null) {
			MailResult result = new MailResult();
			result.isSucess = false;
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			return result;
		}
		KMail mail = new KMail(set, mailType, senderRoleId, senderRoleName, title, content, null, null);
		//
		KRole receiverRole = KSupportFactory.getRoleModuleSupport().getRole(receiverRoleId);
		return KMailLogic.sendMailFinally(receiverRole, set, mail);
	}

	public MailResult sendSimpleMailBySystem(long receiverRoleId, String title, String content) {
		KMailSet set = KMailModuleExtension.getMailSet(receiverRoleId);
		if (set == null) {
			MailResult result = new MailResult();
			result.tips = GlobalTips.角色不存在;
			return result;
		}
		KMail mail = new KMail(set, KMail.TYPE_SYSTEM, KMailConfig.SYS_MAIL_SENDER_ID, KMailConfig.SYS_MAIL_SENDER_NAME, title, content, null, null);
		//
		KRole receiverRole = KSupportFactory.getRoleModuleSupport().getRole(receiverRoleId);
		return KMailLogic.sendMailFinally(receiverRole, set, mail);
	}

	public Map<Long, KMail> sendGroupSimpleMail(long senderRoleId, String senderRoleName, Collection<Long> receiverRoleIds, int mailtype, String title, String content) {
		Map<Long, KMail> result = new HashMap<Long, KMail>();

		MailResult sendResult = null;
		for (Long tempReceiverRoleId : receiverRoleIds) {
			sendResult = sendSimpleMail(senderRoleId, senderRoleName, tempReceiverRoleId, mailtype, title, content);
			if (sendResult.isSucess) {
				result.put(tempReceiverRoleId, sendResult.mail);
			}
		}
		return result;
	}

	@Override
	public Map<Long, KMail> sendSimpleMailToOnlineBySystem(int minLv, int maxLv, String mailTitle, String mailContent) {
		RoleModuleSupport roleSupport = KSupportFactory
				.getRoleModuleSupport();
		List<Long> roles = roleSupport.getAllOnLineRoleIds();
		List<Long> receiverRoleIds = new ArrayList<Long>();
		for (Long roleId : roles) {
			KRole temp = roleSupport.getRole(roleId);
			if (temp == null) {
				continue;
			}
			if (minLv <= temp.getLevel() && temp.getLevel() <= maxLv) {
				receiverRoleIds.add(roleId);
			}
		}

		Map<Long, KMail> sendResult = sendGroupSimpleMail(
						KMailConfig.SYS_MAIL_SENDER_ID,
						KMailConfig.SYS_MAIL_SENDER_NAME, receiverRoleIds,
						KMail.TYPE_SYSTEM, mailTitle, mailContent);
		
		return sendResult;
	}

	@Override
	public void cloneRoleByCamus(KRole myRole, KRole srcRole) {
		KMailSet myset = KMailModuleExtension.getMailSet(myRole.getId());
		KMailSet srcset = KMailModuleExtension.getMailSet(srcRole.getId());

		KMailLogic.deleteAllMailsByGM(myRole);
		List<KMail> srclist = srcset.getAllMailsCopy();
		for (KMail srcmail : srclist) {
			KMail mymail = new KMail(myset, srcmail._type, srcmail._senderRoleId, srcmail.getSenderRoleName(), srcmail._title, srcmail._content, null, null);

			for (MailAttachmentAbs srcAtt : srcmail.getAllAttachmentsCache().values()) {
				mymail.addAttachment(srcAtt);
			}
			myset.addMail(mymail);
		}
		
		KPushMailsMsg.pushAllMails(myRole);
	}
}
