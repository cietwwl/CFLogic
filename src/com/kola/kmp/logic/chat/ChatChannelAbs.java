package com.kola.kmp.logic.chat;

import java.util.HashMap;

import com.koala.game.KGameMessage;
import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.chat.message.KChatPushMsg;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.other.KColorFunEnum.KColor;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.RoleModuleSupport;
import com.kola.kmp.logic.util.text.HyperTextTool;

/**
 * <pre>
 * 抽象聊天频道
 * 系统、世界、区域、军团、队伍、私聊
 * 
 * @author CamusHuang
 * @creation 2014-7-22 上午10:13:51
 * </pre>
 */
public abstract class ChatChannelAbs {

	static final KGameLogger _LOGGER = KGameLogger.getLogger(ChatChannelAbs.class);

	static final HashMap<KChatChannelTypeEnum, ChatChannelAbs> chatchannels = new HashMap<KChatChannelTypeEnum, ChatChannelAbs>();
	static {
		chatchannels.put(KChatChannelTypeEnum.世界, new ChatChannelWorld(KChatChannelTypeEnum.世界));
		chatchannels.put(KChatChannelTypeEnum.军团, new ChatChannelGang(KChatChannelTypeEnum.军团));
		chatchannels.put(KChatChannelTypeEnum.私聊, new ChatChannelPrivate(KChatChannelTypeEnum.私聊));
		chatchannels.put(KChatChannelTypeEnum.系统, new ChatChannelSystem(KChatChannelTypeEnum.系统));
		chatchannels.put(KChatChannelTypeEnum.组队, new ChatChannelTeam(KChatChannelTypeEnum.组队));
		chatchannels.put(KChatChannelTypeEnum.附近, new ChatChannelArea(KChatChannelTypeEnum.附近));

		if (chatchannels.size() != KChatChannelTypeEnum.values().length) {
			throw new RuntimeException("聊天频道初始化错误");
		}
	}
	// /////////////////////////////////////////////////////////////////

	public final KChatChannelTypeEnum channelType;
	private String nameWithColor;// {【name】@c1}
	private boolean on;
	private long cd;// 毫秒

	ChatChannelAbs(KChatChannelTypeEnum channelType) {
		this.channelType = channelType;
	}

	void init(KColor kColor, long cd, boolean onoff) {
		this.nameWithColor = HyperTextTool.extColor("【" + channelType.name() + "】", kColor);
		this.cd = cd;
		this.on = onoff;
	}

	public KChatChannelTypeEnum getTypeEnum() {
		return channelType;
	}

	public boolean isOn() {
		return on;
	}

	@Override
	public String toString() {
		return channelType.name();
	}

	public long getCD() {
		return cd;
	}

	public void turn(boolean onoff) {
		on = onoff;
	}

	/**
	 * <pre>
	 * 各频道发送消息的具体逻辑
	 * 
	 * @param chatData
	 * @param msg
	 * @return
	 * @author CamusHuang
	 * @creation 2014-7-22 上午10:45:26
	 * </pre>
	 */
	abstract int broadcastByChannel(ChatDataAbs chatData, KGameMessage msg);

	/**
	 * <pre>
	 * 本方法是频道发送聊天入口
	 * 
	 * @param chatData
	 * @return
	 * @author CamusHuang
	 * @creation 2014-7-22 上午10:28:27
	 * </pre>
	 */
	public static int broadcast(ChatDataAbs chatData) {

		ChatChannelAbs channel = chatchannels.get(chatData.channelType);
		if (channel == null) {
			return 0;
		}

		KGameMessage msg = KChatPushMsg.genChatData(chatData);

		if (chatData instanceof ChatDataFromSystem) {
			ChatDataFromSystem tempData = (ChatDataFromSystem) chatData;
			if (tempData.receiverRoleIds != null) {
				// 按指定角色进行发送
				return broadcastToSomeRoles(tempData, msg);
			}
		} else {
			// 缓存附件
			ChatCache.catchChatAttachmentFIFO((ChatDataFromRole) chatData);
		}

		// 按频道发送
		return channel.broadcastByChannel(chatData, msg);
	}

	private static int broadcastToSomeRoles(ChatDataFromSystem chatData, KGameMessage msg) {
		KRole role;
		RoleModuleSupport roleSupport = KSupportFactory.getRoleModuleSupport();
		//
		KGameMessage dupMsg = msg.duplicate();
		int count = 0;
		for (long roleId : chatData.receiverRoleIds) {
			if (!roleSupport.isRoleDataInCache(roleId)) {
				continue;
			}
			role = roleSupport.getRole(roleId);
			if (role == null) {
				continue;
			}
			if (!role.isOnline()) {
				continue;
			}
			// 等级过滤
			if (chatData.isShouldSend(role.getLevel())) {
				if(role.sendMsg(dupMsg)){
					dupMsg = msg.duplicate();
					count++;
				}
			}
		}
		// 通知GM
		KSupportFactory.getGMSupport().onChat(chatData);
		return count;
	}
}
