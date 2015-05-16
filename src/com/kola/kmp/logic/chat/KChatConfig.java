package com.kola.kmp.logic.chat;

import java.util.List;

import javax.management.timer.Timer;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.other.KColorFunEnum.KColor;
import com.kola.kmp.logic.other.KColorFunEnum.KColorManager;

/**
 * <pre>
 * 模块配置表
 * 颜色值请参考{@link KColorFunEnum}
 * 
 * @author CamusHuang
 * @creation 2012-11-14 下午4:10:27
 * </pre>
 */
public class KChatConfig {
	private static KChatConfig instance;
	//
	/** 所有定义的菜单的串联 */
	public final String popupmenus;

	// 初始化配置项
	public final int cachedChatDataCapacity_serverside;
	public final int cachedChatDataCapacity_clientside;
	public final int MaxPrivateCacheSize;// 私聊缓存数量
	public final int maxcharpermsg;
	public final int maxemopermsg;

	// 聊天发言内容最大长度(字符数量，英文1，汉字2)
	public final int MaxChatStrLen;

	// 发送图片的相关参数
	public final int picColdTime;// 发送图片的CD时间（秒）
	public final short picOpenLevel; // 发送图片等级限制 >=
	public final byte picVipLimite; // 发送图片的VIP等级限制 >=
	public final short picTimesLimite; // 每天发送图片的次数限制 <=
	public boolean isOpenPic; // 是否开放图片功能
	// 发送语音的相关参数
	public final int soundColdTime;// 发送语音的CD时间（秒）
	public final short soundOpenLevel; // 发送语音等级限制 >=
	public final byte soundVipLimite; // 发送声音的VIP等级限制 >=
	public final short soundTimesLimite; // 每天发送声音的次数限制 <=
	public boolean isOpenSound; // 是否开放语音功能

	public short worldChatOpenLevel = 0; // 发送世界聊天等级限制 >=
	public short areaChatOpenLevel = 1; // 发送附近聊天等级限制 >=

	private KChatConfig(Element logicE) throws KGameServerException {
		// CTODO 模块配置表初始化
		cachedChatDataCapacity_serverside = Integer.parseInt(logicE.getChild("cachedChatDataCapacity").getAttributeValue("server"));
		cachedChatDataCapacity_clientside = Integer.parseInt(logicE.getChild("cachedChatDataCapacity").getAttributeValue("client"));
		MaxPrivateCacheSize = Integer.parseInt(logicE.getChildTextTrim("MaxPrivateCacheSize"));
		ChatCache.init(cachedChatDataCapacity_serverside, MaxPrivateCacheSize);
		//
		maxcharpermsg = Integer.parseInt(logicE.getChildTextTrim("maxcharpermsg"));
		maxemopermsg = Integer.parseInt(logicE.getChildTextTrim("maxemopermsg"));
		//
		MaxChatStrLen = Integer.parseInt(logicE.getChildTextTrim("MaxChatStrLen"));

		{// 发送图片的相关参数
			Element limitE = logicE.getChild("picLimit");
			picColdTime = (int) (UtilTool.parseDHMS(limitE.getChildTextTrim("coldTime")) / Timer.ONE_SECOND);
			picOpenLevel = Short.parseShort(limitE.getChildTextTrim("openLevel"));
			picVipLimite = Byte.parseByte(limitE.getChildTextTrim("vipLimite"));
			picTimesLimite = Short.parseShort(limitE.getChildTextTrim("timesLimite"));
			isOpenPic = Boolean.parseBoolean(limitE.getChildTextTrim("isOpen"));
		}

		{// 发送语音的相关参数
			Element limitE = logicE.getChild("voiceLimit");
			soundColdTime = (int) (UtilTool.parseDHMS(limitE.getChildTextTrim("coldTime")) / Timer.ONE_SECOND);
			soundOpenLevel = Short.parseShort(limitE.getChildTextTrim("openLevel"));
			soundVipLimite = Byte.parseByte(limitE.getChildTextTrim("vipLimite"));
			soundTimesLimite = Short.parseShort(limitE.getChildTextTrim("timesLimite"));
			isOpenSound = Boolean.parseBoolean(limitE.getChildTextTrim("isOpen"));
		}

		{// 频道等级限制
			Element limitE = logicE.getChild("channelLimit");
			worldChatOpenLevel = Short.parseShort(limitE.getChildTextTrim("worldChatOpenLevel")); // 发送世界聊天等级限制
		}

		// 点击弹出的菜单项
		@SuppressWarnings("unchecked")
		List<Element> e_sendermenu = logicE.getChild("popup-menus").getChildren();
		StringBuilder sb = new StringBuilder();
		for (Element e_menuitem : e_sendermenu) {
			int menuid = Integer.parseInt(e_menuitem.getAttributeValue("id"));
			String menulabel = e_menuitem.getTextTrim();
			sb.append(menuid).append(",").append(menulabel).append(";");
		}
		popupmenus = sb.toString();

		// 频道定义
		@SuppressWarnings("unchecked")
		// <私聊 fontcolor="c16" switch="ON" coolingseconds="3"/>
		Element e_chatchannels = logicE.getChild("chatchannels");
		for (KChatChannelTypeEnum type : KChatChannelTypeEnum.values()) {
			Element e_chatchannel = e_chatchannels.getChild(type.name());
			if (e_chatchannel == null) {
				throw new KGameServerException("频道配置数据不存在 频道=" + type.name());
			}
			String colorSign = e_chatchannel.getAttributeValue("colorSign");
			KColor kColor = KColorManager.getColor(colorSign);
			if (kColor == null) {
				throw new KGameServerException("频道定义指定的颜色不存在：" + colorSign);
			}

			boolean bswitch = "ON".equalsIgnoreCase(e_chatchannel.getAttributeValue("switch"));
			long cd = UtilTool.parseDHMS(e_chatchannel.getAttributeValue("cd"));

			ChatChannelAbs channel = ChatChannelAbs.chatchannels.get(type);
			if (channel == null) {
				throw new KGameServerException("频道初始化错误，缺少频道实例：" + type.name());
			}
			channel.init(kColor, cd, bswitch);
		}

		// pattern = root.getChildTextTrim("pattern");
	}

	public static void init(Element logicE) throws KGameServerException {
		instance = new KChatConfig(logicE);
	}

	public static KChatConfig getInstance() {
		return instance;
	}
}
