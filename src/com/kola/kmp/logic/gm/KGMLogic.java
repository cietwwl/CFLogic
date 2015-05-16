package com.kola.kmp.logic.gm;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.gameserver.KGameServer;
import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.KGameCheatCenter;
import com.kola.kmp.logic.KGameCheatCenter.CheatResult;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GMResult;
import com.kola.kmp.logic.util.ResultStructs.MailResult;
import com.kola.kmp.protocol.gm.ProtocolGs;

/**
 * <pre>
 * GM中心
 * 暂时挂在奖励模块中，使用奖励模块中的消息ID段
 * 
 * @author CamusHuang
 * @creation 2013-5-21 下午12:24:52
 * </pre>
 */
public class KGMLogic implements ProtocolGs {

	private static final Logger _LOGGER = KGameLogger.getLogger(KGMLogic.class);
	// 提供在线邮件服务的GM数量
	public static int gmInMailCount;

	/**
	 * <pre>
	 * 一般用于发送消息
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2013-5-23 上午11:49:50
	 * </pre>
	 */
	public static KGamePlayerSession getGMSession() {
		return KGameServer.getInstance().getPlayerManager().getPlayerSession(KGMConfig.getInstance().gmPlayerId);
	}

	/**
	 * <pre>
	 * 返回玩家邮件是否成功发送给GM
	 * long 发送者邮件的角色Id
	 * String 发送邮件的角色昵称
	 * String 信件标题
	 * String 信件内容
	 * byte 处理结果（-1：没有GM在线，-2：发送失败）
	 * </pre>
	 */
	static void dealMsg_processGMMailConfirm(KGameMessage msg) {
		long roleId = msg.readLong();
		String roleName = msg.readUtf8String();
		String title = msg.readUtf8String();
		String content = msg.readUtf8String();
		byte result = msg.readByte();

		// CTODO 是否需要通知玩家？？
		if (result == -1) {
			// KGameLogger.getLogger(GMCenter.class).error("BUG转发GM:没有GM在线");
		} else if (result == -2) {
			// KGameLogger.getLogger(GMCenter.class).error("BUG转发GM:发送失败");
		} else {
			// KGameLogger.getLogger(GMCenter.class).error("BUG转发GM:转发GM成功");
		}

	}

	static int getGmInMailCount() {
		return gmInMailCount;
	}

	static void dealMsg_processGMInMailCount(KGameMessage msg) {
		int oldCount = gmInMailCount;
		gmInMailCount = msg.readInt();

		if (gmInMailCount < 1) {
			if (oldCount > 0) {
				// KGameLogger.getLogger(GMCenter.class).error("GM离线！");
			}
		} else {
			if (oldCount == 0) {
				// CTODO 是否需要通知玩家有GM提供服务？
				// KGameLogger.getLogger(GMCenter.class).error("GM在线！");
			}
		}
	}

	/**
	 * <pre>
	 * 回复邮件(GS收到回复的邮件后以GS_GMS_TCP_EMAIL_CONFIRM作结果返回)
	 * long  角色ID
	 * String 角色昵称
	 * long GS邮件的ID
	 * String GM名称
	 * long GM邮件的ID
	 * String 回复的邮件标题
	 * String 回复的邮件内容
	 * </pre>
	 */
	static void dealMsg_processGMCMail(KGamePlayerSession session, KGameMessage msg) {
		long roleId = msg.readLong();
		String roleName = msg.readUtf8String();
		long gsMailId = msg.readLong();
		String gmc = msg.readUtf8String();
		long gmMailId = msg.readLong();
		String title = msg.readUtf8String();
		String content = msg.readUtf8String();

		// 转发邮件给角色
		MailResult result = KSupportFactory.getMailModuleSupport().sendSimpleMailBySystem(roleId, title, content);

		if (result.isSucess) {
			// 已发送
			sendBackMailConfirm(session, roleId, roleName, gsMailId, gmc, gmMailId, true, "邮件已发送！");

		} else {
			// 发送失败
			sendBackMailConfirm(session, roleId, roleName, gsMailId, gmc, gmMailId, false, result.tips);
		}
	}

	/**
	 * <pre>
	 * GS收到GM回复后的确认消息
	 * long  角色Id
	 * String 角色名称
	 * long GS邮件的ID
	 * String GM名称
	 * long GM邮件的ID
	 * boolean 回复是否成功
	 * String 回复结果提示
	 * </pre>
	 */
	private static void sendBackMailConfirm(KGamePlayerSession session, long roleId, String roleName, long gsMailId, String gmName, long gmMailId, boolean isSuccess, String tips) {
		KGameMessage msg = KGame.newLogicMessage(GS_GMS_TCP_EMAIL_CONFIRM);

		msg.writeLong(roleId);
		msg.writeUtf8String(roleName);
		msg.writeLong(gsMailId);
		msg.writeUtf8String(gmName);
		msg.writeLong(gmMailId);
		msg.writeBoolean(isSuccess);
		msg.writeUtf8String(tips);

		session.send(msg);
	}

	static void dealMsg_processOrder(KGamePlayerSession session, KGameMessage msg) {
		String gmc = msg.readUtf8String();
		String roleName = msg.readUtf8String();
		String content = msg.readUtf8String();

		CheatResult result = KGameCheatCenter.processCheatFromGM(content, roleName);
		GMResult result2=new GMResult();
		result2.isSucess =  result.isSuccess;
		result2.tips =  result.tips;
		//
		sendBackCommonMsg(session, gmc, result2);
	}

	/**
	 * <pre>
	 * 通用修改结果消息,针对一些数据修改操作
	 * String gmc名称
	 * boolean 是否修改成功
	 * String 附带的结果消息(如果需要GMC客户端忽略该消息，则该值为"")
	 * </pre>
	 */
	private static void sendBackCommonMsg(KGamePlayerSession session, String gmc, GMResult result) {
		KGameMessage msg = KGame.newLogicMessage(GS_GMS_TCP_COMMON_DATA_MODIFY_RESULT);

		msg.writeUtf8String(gmc);
		msg.writeBoolean(result.isSucess);
		msg.writeUtf8String(result.tips);
		msg.writeBoolean(result.data != null);
		if (result.data != null) {
			msg.writeInt(result.data.length);
			msg.writeBytes(result.data);
		}
		session.send(msg);
	}

	static void dealMsg_processAffine(KGameMessage msg) {
		// （0系统，1世界，2地图，3家族，4组队，5私聊）
		String content = msg.readUtf8String();
		String[] datas = content.split('\t' + "");
		content = datas[0];
		int minRoleLv = 0;
		int maxRoleLv = Short.MAX_VALUE;
		if (datas.length == 3) {
			minRoleLv = Integer.parseInt(datas[1]);
			maxRoleLv = Integer.parseInt(datas[2]);
		}

		KSupportFactory.getChatSupport().sendSystemChat(content, true, true, minRoleLv, maxRoleLv);
		// KSupportFactory.getChatSupport().sendChatBySystem(KChatChannel.CHAT_CHANNEL_TYPE_SYSTEM,
		// content, null);
	}
	
	static void dealMsg_processDataQuery(KGamePlayerSession session, KGameMessage msg) {
		String gmc = msg.readUtf8String();
		String order = msg.readUtf8String();
		GMOrderCenter.dealGMOrder(session, gmc, order);
	}	
}
