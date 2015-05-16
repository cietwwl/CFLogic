package com.kola.kmp.logic.chat;

import java.util.List;

import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.mail.KMailConfig;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.util.tips.ChatTips;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-3-6 下午8:09:55
 * </pre>
 */
public class ChatDataFromSystem extends ChatDataAbs {

	public final List<Long> receiverRoleIds;// 指定接收者角色组

	private String outChatString;// 需要发送给接收者的内容

	// 世界播报专用参数
	private boolean isShowTop = false;// 是否将本消息显示于【世界播报】位置
	private boolean isShowInChannel = true;// 是否将本消息显示于频道界面

	// 世界播报角色等级范围过滤
	private int minRoleLv = 0;
	private int maxRoleLv = Short.MAX_VALUE;

	ChatDataFromSystem(KChatChannelTypeEnum channelType, String chatString, long receiverId) {
		super(channelType, chatString, KMailConfig.SYS_MAIL_SENDER_ID, UtilTool.getNotNullString(null), (byte)0, receiverId);
		//
		this.receiverRoleIds = null;
	}

	ChatDataFromSystem(KChatChannelTypeEnum channelType, String chatString, List<Long> receiverRoleIds) {
		super(channelType, chatString, KMailConfig.SYS_MAIL_SENDER_ID, UtilTool.getNotNullString(null), (byte)0, 0);
		//
		this.receiverRoleIds = receiverRoleIds;
	}

	/**
	 * <pre>
	 * 用作【世界播报】功能 
	 * 
	 * @param chatstring
	 * @param isShowTop
	 * @param isShowInChannel
	 * @author CamusHuang
	 * @creation 2013-5-15 下午3:08:11
	 * </pre>
	 */
	ChatDataFromSystem(String chatString, boolean isShowTop, boolean isShowInChannel, int minRoleLv, int maxRoleLv) {
		super(KChatChannelTypeEnum.系统, chatString, KMailConfig.SYS_MAIL_SENDER_ID, UtilTool.getNotNullString(null), (byte)0, 0);
		//
		this.receiverRoleIds = null;
		//
		this.isShowTop = isShowTop;
		this.isShowInChannel = isShowInChannel;
		this.minRoleLv = minRoleLv;
		this.maxRoleLv = maxRoleLv;
	}

	public String getOutChatString() {
		if (outChatString != null) {
			return outChatString;
		}
		return getOrgChatString();
	}

	public boolean isShowTop() {
		return isShowTop;
	}

	public boolean isShowInChannel() {
		return isShowInChannel;
	}

	public int getMinRoleLv() {
		return minRoleLv;
	}

	public int getMaxRoleLv() {
		return maxRoleLv;
	}

	/**
	 * <pre>
	 * 只有系统频道发送前需要按等级筛选
	 * 
	 * @param roleLv
	 * @return
	 * @author CamusHuang
	 * @creation 2013-8-7 下午3:41:44
	 * </pre>
	 */
	public boolean isShouldSend(int roleLv) {
		return minRoleLv <= roleLv && roleLv <= maxRoleLv;
	}

	public void release() {
		outChatString = null;
	}

	/**
	 * <pre>
	 * 延时发送时加时间后缀
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-23 下午9:54:03
	 * </pre>
	 */
	public String notifyDelay() {
		String sendTimeStr = super.getDelayTimeStr();
		outChatString = StringUtil.format("{} ({})", getOrgChatString(), sendTimeStr);
		return null;
	}
}
