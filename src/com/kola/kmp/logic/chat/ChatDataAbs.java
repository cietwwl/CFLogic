package com.kola.kmp.logic.chat;

import java.util.Date;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.util.text.HyperTextTool;

/**
 * <pre>
 * 一条聊天消息
 * 支持富文本，具体参考{@link HyperTextTag}
 * 
 * </pre>
 * 
 * @author AHONG
 * @see HyperTextTool
 */
public abstract class ChatDataAbs {

	public static final Logger _LOGGER = KGameLogger.getLogger(ChatDataAbs.class);

	// 频道类型
	public final KChatChannelTypeEnum channelType;
	// 发送者角色
	public final long senderRoleId;
	public final String senderRoleName;
	public final byte vipLv;
	// 接收者信息（可能是角色、军团、地图的ID，可无效）
	public final long receiverId;// 接收者ID（角色ID，地图ID，军团ID）
	// 发送时间
	public final long sendTime = System.currentTimeMillis();
	// 聊天内容
	private String orgChatString;// 客户端发来的原始内容（GM需要使用）

	ChatDataAbs(KChatChannelTypeEnum channelType, String chatString, long senderRoleId, String senderRoleName, byte vipLv, long receiverId) {
		this.channelType = channelType;
		this.orgChatString = chatString;
		//
		this.senderRoleId = senderRoleId;
		this.senderRoleName = senderRoleName;
		this.vipLv = vipLv;
		//
		this.receiverId = receiverId;
	}

	/**
	 * <pre>
	 * 
	 * @deprecated 角色邮件在处理完附件后重新设置原始内容
	 * @author CamusHuang
	 * @creation 2014-3-7 下午12:26:37
	 * </pre>
	 */
	void setOrgChatString(String orgChatString) {
		this.orgChatString = orgChatString;
	}

	public String getOrgChatString() {
		return orgChatString;
	}

	public abstract String getOutChatString();

	public abstract void release();

	public abstract boolean isShowTop();

	public abstract boolean isShowInChannel();

	public abstract int getMinRoleLv();

	public abstract int getMaxRoleLv();

	public abstract boolean isShouldSend(int roleLv);
	
	/**
	 * <pre>
	 * 私聊延时发送时加时间后缀
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-23 下午9:54:03
	 * </pre>
	 */
	public abstract String notifyDelay();

	@Override
	public String toString() {
		return getOutChatString();
	}
	
	protected final String getDelayTimeStr(){
//		orgChatString = StringUtil.format("{} ({})", orgChatString, sendTimeStr);
		String sendTimeStr = UtilTool.DATE_FORMAT.format(new Date(sendTime));
		sendTimeStr = HyperTextTool.extColor(sendTimeStr, KColorFunEnum.私聊发送时间);
		return sendTimeStr;
	}

}
