package com.kola.kmp.logic.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.chat.ChatDataFromRole.ChatAttachment;

/**
 * <pre>
 * 聊天缓存
 * 
 * 邮件及其附件缓存机制
 * 1.【已发邮件及其附件缓存】，缓存指定数量的邮件及其附件，当删除缓存的邮件时同时删除对应附件
 * 2.【离线私聊缓存】，离线角色的私聊附件仍然保存在私聊邮件中，直到下次发送时放入【已发邮件及其附件缓存】
 * 2.1【离线私聊缓存】，本功能主要是尽量保证经常上线的玩家可以收到信息。
 * 2.2【离线私聊缓存】，限10000条，太旧的记录会被无情的清掉
 * 2.3【离线私聊缓存】，角色上线10秒左右会收到。
 * 3.重启全清
 * 
 * @author CamusHuang
 * @creation 2013-7-9 下午10:43:58
 * </pre>
 */
public class ChatCache {
	private static final KGameLogger logger = KGameLogger.getLogger(ChatCache.class);

	private static final ReentrantLock lock = new ReentrantLock();

	// 【聊天附件缓存】
	private static LinkedBlockingQueue<ChatAttachment> chatAttachmentCacheQueue;// 定长
	private static Map<Integer, ChatAttachment> chatAttachmentCacheMap = new HashMap<Integer, ChatAttachment>();// 定长

	// 【离线私聊缓存】
	private static LinkedBlockingQueue<ChatDataAbs> privateChatCache;// 定长

	static void init(int sendChatCacheSize, int privateCacheSize) {
		chatAttachmentCacheQueue = new LinkedBlockingQueue<ChatAttachment>(sendChatCacheSize);
		privateChatCache = new LinkedBlockingQueue<ChatDataAbs>(privateCacheSize);
	}

	/**
	 * <pre>
	 * 缓存私聊
	 * 将私聊信息缓存起来，根据先进先出的原则
	 * 
	 * @param chatData
	 * @author CamusHuang
	 * @creation 2013-7-24 上午9:51:07
	 * </pre>
	 */
	static void cachePrivateChatFIFO(ChatDataAbs chatData) {
		lock.lock();
		try {
			boolean isSuccess = privateChatCache.offer(chatData);
			if (!isSuccess) {
				ChatDataAbs removed = privateChatCache.poll();
				if (removed != null) {
					removed.release();
				}
				privateChatCache.offer(chatData);
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * <pre>
	 * 从缓存提取指定角色的所有私聊
	 * 提取的私聊将同时从缓存中清除
	 * 不一定存在
	 * 
	 * @param receiverRoleId
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-24 上午9:51:20
	 * </pre>
	 */
	static List<ChatDataAbs> pollPrivateChats(long receiverRoleId) {
		lock.lock();
		try {
			ChatDataAbs msg = null;
			ArrayList<ChatDataAbs> result = new ArrayList<ChatDataAbs>();
			Iterator<ChatDataAbs> it = privateChatCache.iterator();
			while (it.hasNext()) {
				msg = it.next();
				if (msg.receiverId == receiverRoleId) {
					it.remove();
					result.add(msg);
				}
			}

			logger.warn("私聊缓存 size={}", privateChatCache.size());
			if (result.isEmpty()) {
				return Collections.emptyList();
			}
			return result;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * <pre>
	 * 聊天被成功发送
	 * 将带附件的聊天信息缓存起来，根据先进先出的原则
	 * 
	 * @param msg
	 * @author CamusHuang
	 * @creation 2013-7-19 上午12:36:15
	 * </pre>
	 */
	static void catchChatAttachmentFIFO(ChatDataFromRole chatData) {

		List<ChatAttachment> atts = chatData.getAndClearAttachments();
		if (atts == null || atts.isEmpty()) {
			// 没有附件，不作缓存
			return;
		}

		lock.lock();
		try {
			for (ChatAttachment att : atts) {
				if (!chatAttachmentCacheQueue.offer(att)) {
					ChatAttachment removed = chatAttachmentCacheQueue.poll();
					if (removed != null) {
						chatAttachmentCacheMap.remove(removed.attId);
					}
					chatAttachmentCacheQueue.offer(att);
					chatAttachmentCacheMap.put(att.attId, att);
				}
			}
			logger.warn("聊天附件缓存 size={}", chatAttachmentCacheQueue.size());
			return;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * <pre>
	 * 根据ID信息获取缓存附件
	 * 不一定存在
	 * 
	 * @param actiontype
	 * @param actionscript
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-24 上午9:57:25
	 * </pre>
	 */
	static ChatAttachment getChatAttachment(long attId) {
		return chatAttachmentCacheMap.get(attId);
	}
}
