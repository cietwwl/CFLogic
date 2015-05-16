package com.kola.kmp.logic.mail;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.timer.Timer;

import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.flow.FlowManager;
import com.kola.kmp.logic.flow.FlowManager.OtherFlowTypeEnum;
import com.kola.kmp.logic.gang.KGang;
import com.kola.kmp.logic.gang.KGangMember;
import com.kola.kmp.logic.gang.KGangPositionEnum;
import com.kola.kmp.logic.mail.attachment.MailAttachmentFactory;
import com.kola.kmp.logic.mail.message.KPushMailsMsg;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RelationShipModuleSupport;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.ResultStructs.MailResult;
import com.kola.kmp.logic.util.ResultStructs.MailResult_Sync;
import com.kola.kmp.logic.util.ResultStructs.MailResult_TakeAtt;
import com.kola.kmp.logic.util.tips.MailTips;

/**
 * <pre>
 * 邮件功能：图片、链接、发送
 * 完成：（一键）删除、未读、已读、（一键）提取、
 * 
 * @author CamusHuang
 * @creation 2014-2-22 下午3:32:01
 * </pre>
 */
public class KMailLogic {

	static final KGameLogger roleMailLogger = KGameLogger.getLogger("roleMail");

	static void initDefaultMails(KRole role) {
		// CTODO 新注册邮箱需不需要默认邮件？
	}

	/**
	 * <pre>
	 * 删除邮件通知
	 * 
	 * @param role
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-22 下午3:00:59
	 * </pre>
	 */
	public static MailResult_Sync dealMsg_deleteMail(KRole role, long mailId) {
		MailResult_Sync result = new MailResult_Sync();
		//
		KMailSet set = KMailModuleExtension.getMailSet(role.getId());
		KMail mail = null;
		set.rwLock.lock();
		try {
			List<KMail> elementList = set.getAllMailsCopy();

			mail = set.notifyElementDelete(mailId);
			if (mail == null) {
				result.tips = MailTips.不存在此邮件;
				return result;
			}

			result.deleteMails.add(mailId);

			// 若删除了邮件，且原有邮件数量超越警戒容量，则找出上浮邮件
			findUpMails(result, elementList, result.deleteMails.size());

			result.isSucess = true;
			result.tips = MailTips.删除成功;
			return result;
		} finally {
			set.rwLock.unlock();

			if (result.isSucess) {
				// 财产日志
				if (!mail.getAllAttachmentsCache().isEmpty()) {
					String tips = StringUtil.format("dbId:{};邮件标题:{};邮件内容:{}", mail._id, mail._title, mail._content);
					FlowManager.logOther(role.getId(), OtherFlowTypeEnum.删除附件邮件, tips);
				}
			}
		}
	}

	/**
	 * <pre>
	 * 2.一键删除：点击一键删除，便可删除所有警戒容量以内的邮件。
	 * 点击一键删除时，会有提示信息，告诉玩家，删除信息将无法恢复。玩家点击确认之后，再将信息清除。
	 * 
	 * 注：一键删除，不能删除有附件的邮件。
	 * 
	 * @param role
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-22 下午3:00:59
	 * </pre>
	 */
	public static MailResult_Sync dealMsg_oneKeyDeleteMails(KRole role) {

		MailResult_Sync result = new MailResult_Sync();
		//
		KMailSet set = KMailModuleExtension.getMailSet(role.getId());
		set.rwLock.lock();
		try {

			List<KMail> elementList = set.getAllMailsCopy();
			//
			{
				// 按从旧到新的原则，扫描清理警戒容量以内的无附件的邮件
				int scanCount = 0;// 扫描数量计数
				for (KMail mail : elementList) {
					{
						if (scanCount >= KMailConfig.getInstance().MailBoxWarnSize) {
							// 最多只扫描警戒容量
							break;
						}
						scanCount++;
					}
					//
					if (mail.isContainAttachments()) {
						// 包含附件:忽略
						continue;
					}

					// 不包含附件:删除
					set.notifyElementDelete(mail._id);
					//
					result.deleteMails.add(mail._id);
				}
			}
			//
			if (result.deleteMails.isEmpty()) {
				result.tips = MailTips.不存在可以删除的邮件;
				return result;
			}

			// 若删除了邮件，且原有邮件数量超越警戒容量，则找出上浮邮件
			findUpMails(result, elementList, result.deleteMails.size());

			result.isSucess = true;
			result.tips = StringUtil.format(MailTips.成功删除了x封邮件, result.deleteMails.size());
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 隐藏邮件上浮
	 * 
	 * @param result
	 * @param elementList 原邮件列表
	 * @param findSize 由于删除了findSize个邮件，而需要从隐藏部分找出的findSize个上浮邮件
	 * @author CamusHuang
	 * @creation 2014-9-3 下午7:35:47
	 * </pre>
	 */
	private static void findUpMails(MailResult_Sync result, List<KMail> elementList, int findSize) {
		if (findSize > 0) {
			if (elementList.size() > KMailConfig.getInstance().MailBoxWarnSize) {
				int fromIndex = KMailConfig.getInstance().MailBoxWarnSize;
				int toIndex = fromIndex + findSize;
				toIndex = Math.min(elementList.size(), toIndex);
				result.updateMails = elementList.subList(fromIndex, toIndex);
			}
		}
	}

	/**
	 * <pre>
	 * 读取邮件通知
	 * 
	 * @param role
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-22 下午3:00:59
	 * </pre>
	 */
	public static void dealMsg_readedMail(long roleId, long mailId) {

		KMailSet set = KMailModuleExtension.getMailSet(roleId);
		set.rwLock.lock();
		try {
			KMail mail = set.getMail(mailId);
			if (mail != null) {
				mail.setStatus(KMail.STATUS_READED);
			}
		} finally {
			set.rwLock.unlock();
		}
	}

	public static MailResult_Sync dealMsg_takeAttachment(KRole role, long mailId) {

		MailResult_Sync result = new MailResult_Sync();
		
		if(!KSupportFactory.getMissionSupport().checkFunctionIsOpen(role, KFunctionTypeEnum.背包)){
			result.tips = MailTips.请在背包开放后再来提取附件;
			return result;
		}
		
		//
		KMailSet set = KMailModuleExtension.getMailSet(role.getId());
		set.rwLock.lock();
		try {
			List<KMail> elementList = set.getAllMailsCopy();

			KMail mail = set.getMail(mailId);
			if (mail == null) {
				result.tips = MailTips.不存在此邮件;
				return result;
			}

			if (!mail.isContainAttachments()) {
				result.tips = MailTips.此邮件不包含附件;
				return result;
			}

			MailResult_TakeAtt tempResult = MailAttachmentFactory.takeAttachment(role, mail);
			result.isSucess = tempResult.isSucess;
			result.tips = tempResult.tips;
			result.addUprisingTips(tempResult.getUprisingTips());
			result.addDataUprisingTips(tempResult.getDataUprisingTips());

			if (result.isSucess) {
				// 删除邮件
				set.notifyElementDelete(mailId);
				result.deleteMails.add(mailId);
				result.tips = MailTips.提取附件成功;

				// 若删除了邮件，且原有邮件数量超越警戒容量，则找出上浮邮件
				findUpMails(result, elementList, result.deleteMails.size());
			} else {
				if(tempResult.isTakePartAtts){
					result.updateMails.add(mail);
				}
			}
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	public static MailResult_Sync dealMsg_oneKeyTakeAttachment(KRole role) {

		MailResult_Sync result = new MailResult_Sync();
		
		if(!KSupportFactory.getMissionSupport().checkFunctionIsOpen(role, KFunctionTypeEnum.背包)){
			result.tips = MailTips.请在背包开放后再来提取附件;
			return result;
		}

		//
		KMailSet set = KMailModuleExtension.getMailSet(role.getId());
		set.rwLock.lock();
		try {
			List<KMail> elementList = set.getAllMailsCopy();
			// 是否在提取过程中遇到失败
			boolean isFailed = false;
			// 是否提取了任意附件
			boolean isTakeSome = false;
			// 按从旧到新的原则，扫描清理警戒容量以内的有附件的邮件
			int scanCount = 0;// 扫描数量计数
			for (KMail mail : elementList) {
				{
					if (scanCount >= KMailConfig.getInstance().MailBoxWarnSize) {
						// 最多只扫描警戒容量
						break;
					}
					scanCount++;
				}

				if (!mail.isContainAttachments()) {
					// 不包含附件:忽略
					continue;
				}

				// 包含附件->提取:可能成功、有可能部分提取、也有可能完全失败
				MailResult_TakeAtt tempResult = MailAttachmentFactory.takeAttachment(role, mail);
				result.addUprisingTips(tempResult.getUprisingTips());
				result.addDataUprisingTips(tempResult.getDataUprisingTips());
				//
				if (tempResult.isSucess) {
					// 删除邮件
					isTakeSome = true;
					set.notifyElementDelete(mail._id);
					result.deleteMails.add(mail._id);
					continue;
				} else {
					// 失败
					isFailed = true;
					result.tips = tempResult.tips;
					if (tempResult.isTakePartAtts) {
						// 部分提取
						isTakeSome = true;
						result.updateMails.add(mail);

					}
					break;
				}
			}

			// 若删除了邮件，且原有邮件数量超越警戒容量，则找出上浮邮件
			findUpMails(result, elementList, result.deleteMails.size());

			if (isFailed) {
				// 在提取过程中遇到失败
				return result;
			}

			if (!isTakeSome) {
				result.isSucess = false;
				result.tips = MailTips.不存在可以提取的附件;
				return result;
			}

			result.isSucess = true;
			result.tips = MailTips.提取附件成功;
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 发送一个新邮件给某个角色
	 * 如果是非系统邮件，则检测角色的警戒容量
	 * 内部会同步新邮件给客户端、记录日志
	 * 
	 * @param receiverRole
	 * @param set
	 * @param mail
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-22 下午5:15:17
	 * </pre>
	 */
	static MailResult sendMailFinally(KRole receiverRole, KMailSet set, KMail mail) {
		MailResult result = new MailResult();
		result.mail = mail;

		boolean isFull = false;
		mail.rwLock.lock();
		try {
			if (set.size() >= KMailConfig.getInstance().MailBoxWarnSize) {
				if (mail._type != KMail.TYPE_SYSTEM) {
					result.tips = MailTips.对方邮箱已满;
					return result;
				}
			}
			//
			set.addMail(mail);
			if (set.size() >= KMailConfig.getInstance().MailBoxWarnSize) {
				isFull = true;
			}
			//
			result.isSucess = true;
			result.tips = MailTips.成功发送;
			return result;
		} finally {
			mail.rwLock.unlock();

			if (result.isSucess) {
				if (!isFull) {
					// 通知接收者角色有新邮件
					KPushMailsMsg.synMail(receiverRole, mail);
				}

				// 记录日志
				KMailLogic.roleMailLogger.warn("发送者{PLAYER:{},ROLE{{}:{}}},接收者{PLAYER:{},ROLE{{}:{}}},邮件{标题:{},内容:{}}", 0, mail._senderRoleId, mail.getSenderRoleName(), 0, set._ownerId,
						(receiverRole == null ? null : receiverRole.getName()), mail._title, mail._content);
			}
		}
	}

	public static MailResult_Sync deleteAllMailsByGM(KRole role) {
		MailResult_Sync result = new MailResult_Sync();
		//
		KMailSet set = KMailModuleExtension.getMailSet(role.getId());
		set.rwLock.lock();
		try {

			List<KMail> elementList = set.getAllMailsCopy();
			//
			{
				// 按从旧到新的原则，扫描清理警戒容量以内的无附件的邮件
				for (KMail mail : elementList) {
					set.notifyElementDelete(mail._id);
					result.deleteMails.add(mail._id);
				}
			}

			if (result.deleteMails.isEmpty()) {
				result.tips = MailTips.不存在可以删除的邮件;
				return result;
			}

			// 若删除了邮件，且原有邮件数量超越警戒容量，则找出上浮邮件
			findUpMails(result, elementList, result.deleteMails.size());

			result.isSucess = true;
			result.tips = StringUtil.format(MailTips.成功删除了x封邮件, result.deleteMails.size());
			return result;
		} finally {
			set.rwLock.unlock();
		}
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param role
	 * @param mode 模式0表示好友，1表示军团，不支持混发
	 * @param roleIds
	 * @param mailTitle
	 * @param mailContent
	 * @return
	 * @author CamusHuang
	 * @creation 2014-11-12 下午4:30:45
	 * </pre>
	 */
	public static CommonResult dealMsg_sendMail(KRole role, byte mode, Set<Long> roleIds, String mailTitle, String mailContent) {
		CommonResult result = new CommonResult();
		if (roleIds.isEmpty()) {
			result.tips = MailTips.请指定邮件接收者;
			return result;
		}

		final int MODE_FRIEND = 0;
		final int MODE_GANG = 1;

		// 对发送者进行检查
		KMailSet set = KMailModuleExtension.getMailSet(role.getId());
		long nowTime = System.currentTimeMillis();
		long cdEndTime = set.getSendCDEndTime();
		if (cdEndTime > nowTime) {
			result.tips = StringUtil.format(MailTips.发送邮件冷却剩余时间x, UtilTool.genReleaseCDTimeString(cdEndTime - nowTime));
			return result;
		}

		// 剔除非法名单
		if (mode == MODE_FRIEND) {
			RelationShipModuleSupport support = KSupportFactory.getRelationShipModuleSupport();
			for (Iterator<Long> it = roleIds.iterator(); it.hasNext();) {
				long receiverRoleId = it.next();
				if (!support.isInFriendList(role.getId(), receiverRoleId)) {
					it.remove();
				}
			}
		} else {
			KGang gang = KSupportFactory.getGangSupport().getGangByRoleId(role.getId());
			KGangMember mem = gang.getMember(role.getId());
			if (mem.getPositionEnum() != KGangPositionEnum.军团长) {
				result.tips = MailTips.你没有权限向军团成员发送邮件;
				return result;
			}

			for (Iterator<Long> it = roleIds.iterator(); it.hasNext();) {
				long receiverRoleId = it.next();
				if (gang.getMember(receiverRoleId) == null) {
					it.remove();
				}
			}
		}

		// 群发人数限制
		if (mode == MODE_FRIEND) {
			if (roleIds.size() > KMailConfig.getInstance().SendMailNameListMaxForFriend) {
				result.tips = StringUtil.format(MailTips.好友邮件群发限x人以内, KMailConfig.getInstance().SendMailNameListMaxForFriend);
				return result;
			}
		} else {

		}

		// 敏感字处理
		mailTitle = KSupportFactory.getDirtyWordSupport().clearDirtyWords(mailTitle, true);
		mailContent = KSupportFactory.getDirtyWordSupport().clearDirtyWords(mailContent, true);

		// 循环发送
		int count = 0;
		MailResult tempResult = null;
		for (long receiverRoleId : roleIds) {
			KMailSet receiverSet = KMailModuleExtension.getMailSet(receiverRoleId);
			if (receiverSet == null) {
				continue;
			}
			KMail mail = new KMail(receiverSet, KMail.TYPE_COMMON, role.getId(), role.getName(), mailTitle, mailContent, null, null);
			//
			KRole receiverRole = KSupportFactory.getRoleModuleSupport().getRole(receiverRoleId);
			tempResult = sendMailFinally(receiverRole, receiverSet, mail);
			if (tempResult.isSucess) {
				count++;
			}
		}

		// 更新CD时间
		// 好友群发：成功发送给N人，CD N分钟。N必须<=5
		// 军团群发：成功发送给M人，CD Min(M,10)分钟。
		{
			long addTime = 0;
			if (mode == MODE_FRIEND) {
				addTime = count * KMailConfig.getInstance().SendMailCDInMillsPerOne;
			} else {
				addTime = Math.min(count* KMailConfig.getInstance().SendMailCDInMillsPerOne, KMailConfig.getInstance().SendGangMailMaxCDInMills) ;
			}
			set.setSendCDEndTime(nowTime + addTime);
		}
		
		result.isSucess = true;
		if (mode == MODE_FRIEND) {
			result.tips = StringUtil.format(MailTips.成功将邮件发送给x位好友, count);
		} else {
			result.tips = StringUtil.format(MailTips.成功将邮件发送给x位成员, count);
		}
		return result;
	}
}
