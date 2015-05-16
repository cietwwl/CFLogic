package com.kola.kmp.logic.chat;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayer;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.role.RoleBaseInfo;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.KGameCheatCenter;
import com.kola.kmp.logic.chat.ChatDataFromRole.ChatAttachment;
import com.kola.kmp.logic.chat.ChatTaskManager.PrivateChatSendTask;
import com.kola.kmp.logic.chat.message.KChatPushMsg;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.other.KChatLinkedTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.ChatTips;
import com.kola.kmp.logic.util.tips.GangTips;
import com.kola.kmp.logic.util.tips.RelationShipTips;

public class KChatLogic {

	public static final Logger _LOGGER = KGameLogger.getLogger(KChatLogic.class);

	/**
	 * <pre>
	 * 多媒体开关指令
	 * 
	 * @param picSatus -1保持不变，0关闭，1开启
	 * @param soundSatus -1保持不变，0关闭，1开启
	 * @author CamusHuang
	 * @creation 2013-6-13 上午9:16:43
	 * </pre>
	 */
	public static void notifyMediaOrder(int picSatus, int soundSatus) {
		if (picSatus == 1) {
			KChatConfig.getInstance().isOpenPic = true;
		} else if (picSatus == 0) {
			KChatConfig.getInstance().isOpenPic = false;
		}

		if (soundSatus == 1) {
			KChatConfig.getInstance().isOpenSound = true;
		} else if (soundSatus == 0) {
			KChatConfig.getInstance().isOpenSound = false;
		}

		// 重新发送配置信息给所有在线角色
		KChatPushMsg.pushChatInitMsgToAllOnlineRoles();
	}

	/**
	 * <pre>
	 * 聊天等级限制指令
	 * 
	 * @param worldMinLv >=1 表示需要更新限制
	 * @param areaMinLv >=1 表示需要更新限制
	 * @author CamusHuang
	 * @creation 2013-12-3 下午4:47:40
	 * </pre>
	 */
	public static short[] notifyChatLvOrder(short worldMinLv, short areaMinLv) {
		short[] result = new short[] { KChatConfig.getInstance().worldChatOpenLevel, KChatConfig.getInstance().areaChatOpenLevel };

		if (worldMinLv >= 1 || areaMinLv >= 1) {

			if (worldMinLv >= 1) {
				KChatConfig.getInstance().worldChatOpenLevel = worldMinLv;
			}
			if (areaMinLv >= 1) {
				KChatConfig.getInstance().areaChatOpenLevel = areaMinLv;
			}

			// 重新发送配置信息给所有在线角色
			KChatPushMsg.pushChatInitMsgToAllOnlineRoles();
		}

		return result;
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param senderSession
	 * @param senderPlayer
	 * @param senderRole
	 * @param receiverRoleId
	 * @param channelType
	 * @param chatStr
	 * @param attDatas 附加数据（声音、图片）
	 * @author CamusHuang
	 * @creation 2014-3-5 下午12:04:24
	 * </pre>
	 */
	public static void dealMsg_sendChat(KGamePlayerSession senderSession, KGamePlayer senderPlayer, KRole senderRole, long receiverRoleId, KChatChannelTypeEnum channelType, String chatStr,
			byte[][] attDatas) {

		if (channelType == null) {
			return;
		}

		// 附件确认反馈
		if (attDatas != null && attDatas.length > 0) {
			KChatPushMsg.pushAttachmentConfirm(senderSession);
		}

		// 禁言检测
		{
			long releaseForbidTime = senderPlayer.getGagEndtime();
			if (releaseForbidTime > System.currentTimeMillis()) {
				// 禁言反馈
				KDialogService.sendUprisingDialog(senderSession, StringUtil.format(ChatTips.对不起已禁言至x时间, UtilTool.DATE_FORMAT.format(new Date(releaseForbidTime))));
				return;
			}
		}

		long receiverId = 0;
		switch (channelType) {
		case 系统:
			// 忽略客户端发来的系统消息
			return;
		case 世界:
			// 功能指令处理
			if (KGameCheatCenter.processCheatFromRole(senderRole, chatStr)) {
				return;
			}
			// 等级限制检测
			if (senderRole.getLevel() < KChatConfig.getInstance().worldChatOpenLevel) {
				return;
			}
			break;
		case 附近:
			// 功能指令处理
			if (KGameCheatCenter.processCheatFromRole(senderRole, chatStr)) {
				return;
			}
			// 等级限制检测
			if (senderRole.getLevel() < KChatConfig.getInstance().areaChatOpenLevel) {
				return;
			}
			// 获取角色当前所处的地图ID
			receiverId = senderRole.getRoleMapData().getCurrentMapId();
			break;
		case 军团:
			// 获取角色当前所处的军团ID
			receiverId = KSupportFactory.getGangSupport().getGangIdByRoleId(senderRole.getId());
			if (receiverId < 1) {
				KDialogService.sendUprisingDialog(senderSession, GangTips.对不起您不属于任何军团);
				return;
			}
			break;
		case 组队:
			receiverId = senderRole.getId();
			break;
		case 私聊:
			// 私聊对象检测
			KRole receiverRole = KSupportFactory.getRoleModuleSupport().getRole(receiverRoleId);
			if (receiverRole == null) {
				return;
			}
			// 黑名单检测
			if (KSupportFactory.getRelationShipModuleSupport().isInBlackList(receiverRoleId, senderRole.getId())) {
				KDialogService.sendUprisingDialog(senderSession, RelationShipTips.你已在对方黑名单中禁止私聊);
				return;
			}
			// 亲密度增值
			KSupportFactory.getRelationShipModuleSupport().notifyCloseAction_PMChat(senderRole.getId(), receiverRoleId);
			//
			receiverId = receiverRoleId;
			break;
		default:
			// 非法频道类型
			return;
		}

		// 强制字符数量缩减处理
		{
			if (UtilTool.getStringLength(chatStr) > KChatConfig.getInstance().MaxChatStrLen) {
				// 由于有表情、物品ID、声音图片ID等内容，因此这里不进行严格的文本长度限制
				chatStr = chatStr.substring(0, KChatConfig.getInstance().MaxChatStrLen);
			}
		}

		int vipLv = KSupportFactory.getVIPModuleSupport().getVipLv(senderRole.getId());
		ChatDataFromRole chatData = new ChatDataFromRole(channelType, chatStr, senderRole, (byte)vipLv, receiverId, attDatas);
		// 声音图片限制检测
		{

			if (chatData.isContainPic()) {
				if (!KChatConfig.getInstance().isOpenPic) {
					chatData.release();
					return;
				}
			}
			if (chatData.isContainSound()) {
				if (!KChatConfig.getInstance().isOpenSound) {
					chatData.release();
					return;
				}
			}
		}

		sendChatFinally(senderSession, chatData);
	}

	static int sendChatFinally(ChatDataAbs chatData) {
		if (chatData instanceof ChatDataFromRole) {
			return sendChatFinally(null, (ChatDataFromRole) chatData);
		} else {
			return sendChatFinally((ChatDataFromSystem) chatData);
		}
	}

	/**
	 * <pre>
	 * 有条件执行聊天发送：若是私聊且接收者不在线，则不发送且缓存
	 * 
	 * @param senderSession 私聊发送者:若接收者不在线，则通过此参数提醒发送者“未送达”
	 * @param cchannel
	 * @param chatData
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-5 下午3:24:22
	 * </pre>
	 */
	private static int sendChatFinally(KGamePlayerSession senderSession, ChatDataFromRole chatData) {

		try {
			// 私聊缓存处理：若角色不在线，则放入缓存，回复发送者
			if (chatData.channelType == KChatChannelTypeEnum.私聊) {
				KRole receiverRole = KSupportFactory.getRoleModuleSupport().getRole(chatData.receiverId);
				if (receiverRole == null) {
					return 0;
				}
				if (!receiverRole.isOnline()) {
					// 缓存聊天
					ChatCache.cachePrivateChatFIFO(chatData);
					// 回复发送者
					if (senderSession != null) {
						StringBuffer sbf = new StringBuffer();
						sbf.append(StringUtil.format(ChatTips.x不在线系统将尽量为您送达, receiverRole.getExName()));
						KChatPushMsg.pushChatDataWithNOSpeaker(senderSession, chatData.channelType, sbf.toString(), false, true);
					}
					return 1;
				}
			}

			// 按频道发送
			return ChatChannelAbs.broadcast(chatData);
		} finally {
			// 发送完毕，若发言来自于玩家，则进行特殊处理
			actionAfterSendedChatFromRole(chatData);
		}

	}

	/**
	 * <pre>
	 * 有条件执行聊天发送：若是私聊且接收者不在线，则不发送且缓存
	 * 
	 * @param senderSession 私聊发送者:若接收者不在线，则通过此参数提醒发送者“未送达”
	 * @param receiverRole 私聊接收者
	 * @param cchannel
	 * @param chatData
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-5 下午3:24:22
	 * </pre>
	 */
	static int sendChatFinally(ChatDataFromSystem chatData) {

		// 私聊缓存处理：若角色不在线，则放入缓存，回复发送者
		if (chatData.channelType == KChatChannelTypeEnum.私聊) {
			KRole receiverRole = KSupportFactory.getRoleModuleSupport().getRole(chatData.receiverId);
			if (receiverRole == null) {
				return 0;
			}
			if (!receiverRole.isOnline()) {
				// 缓存聊天
				ChatCache.cachePrivateChatFIFO(chatData);
				return 1;
			}
		}

		// 按频道发送
		return ChatChannelAbs.broadcast(chatData);
	}

	/**
	 * <pre>
	 * 聊天发送完毕后的后续操作
	 * 
	 * @param chatData
	 * @author CamusHuang
	 * @creation 2014-3-5 下午3:12:57
	 * </pre>
	 */
	private static void actionAfterSendedChatFromRole(ChatDataFromRole chatData) {
		if (chatData.senderRoleId > 0) {
			switch (chatData.channelType) {
			case 系统:
				// 忽略
				break;
			case 世界:
				// CTODO 世界聊天，行为奖励记录
				// KSupportFactory.getRewardModuleSupport().recordFun(chatData.senderRoleId,
				// KFunTypeEnum.世界发言);
				// KSupportFactory.getMissionSupport().notifyUseFunction(chatData.senderRoleId,
				// FunctionTypeEnum.聊天);
				break;
			case 附近:
				// 忽略
				break;
			case 军团:
				// 忽略
				break;
			case 组队:
				// 忽略
				break;
			case 私聊:
				KSupportFactory.getRelationShipModuleSupport().notifyPMChat(chatData.senderRoleId, chatData.receiverId);
				// 忽略
				break;
			default:
				// 忽略
				break;
			}
		}
	}

	public static byte[] dealMsg_getLinkedAction(int actionType, String linkedScript) {

		KChatLinkedTypeEnum actionTypeEnum = KChatLinkedTypeEnum.getEnum(actionType);

		// 根据类型获取对应的数据
		switch (actionTypeEnum) {
		case 道具:
			// 道具信息，客户端应该直接使用道具模块协议获取道具数据
			// CTODO 忽略
			return null;
		case 图片:
		case 声音:
			// 图片和声音的数据在接收到的时候已经缓存好了
			ChatAttachment chatAtt = ChatCache.getChatAttachment(Integer.parseInt(linkedScript));
			if (chatAtt == null) {
				return null;
			}
			return chatAtt.attData;
		case 菜单:
			// 显示菜单（暂时不会从客户端收到）
			// CTODO 忽略
			return null;
		default:
			// 非法类型
			// CTODO 忽略
			return null;
		}
	}

	/**
	 * <pre>
	 * 从缓存中搜索相关私聊并发送
	 * PS：本方法从私聊缓存中搜索指定角色的私聊，并提交时效任务延时发送私聊内容给客户端
	 * 
	 * @deprecated 一般只在角色上线时调用
	 * @param session
	 * @param role
	 * @author CamusHuang
	 * @creation 2013-7-9 下午11:11:00
	 * </pre>
	 */
	static void notifyForSendCachePrivateChat(KGamePlayerSession session, KRole role) {
		List<ChatDataAbs> list = ChatCache.pollPrivateChats(role.getId());
		if (list.isEmpty()) {
			return;
		}

		// 提交时效任务，延时通知客户端
		PrivateChatSendTask.submitTast(list);
	}
}
