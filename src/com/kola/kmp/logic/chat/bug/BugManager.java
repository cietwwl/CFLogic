package com.kola.kmp.logic.chat.bug;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.ChatTips;
import com.kola.kmp.logic.util.tips.RewardTips;

/**
 * <pre>
 * 处理玩家提交的BUG\投诉\意见\其他
 * 同时处理发奖
 * 
 * @author CamusHuang
 * @creation 2013-5-21 下午12:24:52
 * </pre>
 */
public class BugManager {

	private static final Logger _LOGGER = KGameLogger.getLogger("bugRecord");
	// 最多1000字
	private static final int BUG_MAX_LEN = 1000;

	private BugManager() {
	}

	/**
	 * <pre>
	 * 记录玩家提交的BUG\投诉\意见\其他
	 * 
	 * @param roleId
	 * @param funtype
	 * @author CamusHuang
	 * @creation 2013-5-30 下午3:01:58
	 * </pre>
	 */
	public static CommonResult recordBug(KGamePlayerSession session, KRole role, int type, String content, String qq, String mobile) {
		CommonResult result = new CommonResult();
		// 问题类型、提交时间、玩家账号、角色名称、问题内容
		KBugTypeEnum typeE = KBugTypeEnum.getEnum(type);
		_LOGGER.warn("类型,{},帐号ID,{},角色ID,{},角色名,{},内容,{},QQ,{},手机号,{},设备参数,{}", (typeE == null ? type : typeE.name), role.getPlayerId(), role.getId(), role.getName(), content, qq, mobile, session.getBoundPlayer().getLastLoginDeviceModel());

		if (content == null || content.length() < 1) {
			result.tips = ChatTips.提交的内容有误请重新输入;
			return result;
		}

		if (content.length() > BUG_MAX_LEN) {
			// 限字数
			content = content.substring(0, BUG_MAX_LEN);
		}

		// 转发给GM
		KSupportFactory.getGMSupport().onMail(role.getId(), role.getName(), typeE.name, StringUtil.format("{}(QQ：{}，手机号：{}，机型：{})", content, qq, mobile, session.getBoundPlayer().getLastLoginDeviceModel()));
		result.isSucess = true;
		result.tips = ChatTips.成功提交;
		return result;
	}
}
