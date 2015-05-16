package com.kola.kmp.logic.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.other.KChatChannelTypeEnum;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.text.HyperText;
import com.kola.kmp.logic.util.text.HyperText.HyperURL;
import com.kola.kmp.logic.util.text.HyperText.HyperUrlTagEnum;
import com.kola.kmp.logic.util.text.HyperTextTool;
import com.kola.kmp.logic.util.tips.ChatTips;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-3-6 下午8:09:43
 * </pre>
 */
public class ChatDataFromRole extends ChatDataAbs {
	// 图片和声音附件ID生成器（运行时有效，停服销毁）
	static final AtomicInteger attIDGen = new AtomicInteger();

	// -----------------------------------
	private String outChatString;// 需要发送给接收者的内容
	private String febackChatString;// 需要反馈给发送者的内容（私聊时需要使用）

	// 用于在数据分析阶段临时保存附件，发送成功后放入附件缓存并清空
	private List<ChatAttachment> tempAttachments = Collections.emptyList();
	private boolean isContainPic;
	private boolean isContainSound;

	ChatDataFromRole(KChatChannelTypeEnum channelType, String chatString, KRole senderRole, byte vipLv, byte[][] datas) {
		this(channelType, chatString, senderRole, vipLv, 0, datas);
	}

	ChatDataFromRole(KChatChannelTypeEnum channelType, String chatString, KRole senderRole, byte vipLv, long receiverId, byte[][] datas) {
		this(channelType,  chatString,  senderRole.getId(), senderRole.getName(),  vipLv,  receiverId, datas);
	}
	
	ChatDataFromRole(KChatChannelTypeEnum channelType, String chatString, long roleId, String roleName, byte vipLv, long receiverId, byte[][] datas) {
		super(channelType, chatString, roleId, roleName, vipLv, receiverId);
		//
		chatString = parseInchatstring(datas);
		super.setOrgChatString(chatString);
		//
		// 私聊反馈打包
		{
			// 如果是普通角色发出的私聊，则生成反馈聊天
			if (channelType == KChatChannelTypeEnum.私聊) {
				// 你对x说x = "你对{}说：{}";
				String receiverExtRoleName = KSupportFactory.getRoleModuleSupport().getRoleExtName(receiverId);
				febackChatString = StringUtil.format(ChatTips.你对x说x, receiverExtRoleName, chatString);
			}
		}
	}

	/**
	 * <pre>
	 * 对附件进行处理，并返回处理后的chatString
	 * 
	 * @param senderRole
	 * @param attDatas
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-7 下午12:24:00
	 * </pre>
	 */
	private String parseInchatstring(byte[][] attDatas) {

		List<ChatAttachment> tempAttachments = new ArrayList<ChatAttachment>();
		// 聊天消息内容打包------------------------------------------------------
		boolean isClearDirtyWord = true;
		HyperText hyperText = HyperTextTool.split(getOrgChatString(), isClearDirtyWord);
		if (!hyperText.isContainUrlSign()) {
			// 不包含URL，即无附件
			return hyperText.toString();
		}

		// ////////////////////////////
		boolean isHyperTextChange = false;
		List<Object> splitObjs = hyperText.getSplitObjes();
		for (Iterator<Object> it = splitObjs.iterator(); it.hasNext();) {
			Object obj = it.next();
			if (obj instanceof String) {
				// 纯文本
				continue;
			}

			// URL对象
			HyperURL hyperUrl = (HyperURL) obj;
			if (hyperUrl.tag != HyperUrlTagEnum.a2 || hyperUrl.tag != HyperUrlTagEnum.a3) {
				// 不含有图片、声音的超文本对象
				continue;
			}

			if (hyperUrl.params == null || hyperUrl.params.length < 1) {
				// 非法的脚本
				it.remove();
				isHyperTextChange = true;
				continue;
			}

			// URL对象:含有图片、声音的超文本对象
			// {@a附件类型:附件ID}以下代码根据“附件类型”进行不同处理
			{
				if (attDatas == null || attDatas.length < 1) {
					// 不存在任何附件数据
					it.remove();
					isHyperTextChange = true;
					continue;
				}

				// {@a附件类型:脚本}
				// 以下代码将“脚本”（客户端为附件数组序号）替换成服务器生成的真实附件ID，同时缓存附件数据
				int attDataIndex = -1;// 附件数组序号
				try {
					attDataIndex = Integer.parseInt(hyperUrl.params[0]);
				} catch (Exception e) {
					it.remove();
					isHyperTextChange = true;
					continue;
				}
				if (attDataIndex < 0 || attDataIndex >= attDatas.length) {
					// 非法的附件数组序号
					it.remove();
					isHyperTextChange = true;
					continue;
				}

				// 图片、声音
				{
					// 以下代码将“脚本”（客户端为附件数组序号）替换成服务器生成的真实附件ID，同时缓存附件数据
					int attId = attIDGen.getAndIncrement();
					hyperUrl.params[0] = attId + "";
					isHyperTextChange = true;
					//
					ChatAttachment chatAtt = new ChatAttachment(attId, attDatas[attDataIndex]);
					tempAttachments.add(chatAtt);

					// CTODO 记录玩家行为
					// KActionTypeEnum type;
					if (hyperUrl.tag != HyperUrlTagEnum.a2) {
						isContainPic = true;
						// type = KActionTypeEnum.聊天发图;
					} else {
						isContainSound = true;
						// type = KActionTypeEnum.聊天发语音;
					}
					// if (senderRole != null) {
					// ActionRecord.recordAction(type, senderRole,
					// datas[attDataIndex].length);
					// }
				}
			}
		}

		if (!tempAttachments.isEmpty()) {
			this.tempAttachments = tempAttachments;
		}

		if (isHyperTextChange) {
			hyperText.remakeResultString();
		}
		return hyperText.toString();
	}

	/**
	 * <pre>
	 * 获取并清理附件对象
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-24 上午9:53:41
	 * </pre>
	 */
	List<ChatAttachment> getAndClearAttachments() {
		List<ChatAttachment> temp = tempAttachments;
		tempAttachments = null;
		return temp;
	}

	public String getOutChatString() {
		if(outChatString!=null){
			return outChatString;
		}
		return getOrgChatString();
	}

	public String getSelfChatString() {
		return febackChatString;
	}

	public void release() {
		outChatString = null;
		febackChatString = null;
		//
		tempAttachments = null;
	}

	boolean isContainPic() {
		return isContainPic;
	}

	boolean isContainSound() {
		return isContainSound;
	}

	public boolean isShowTop() {
		return false;
	}

	public boolean isShowInChannel() {
		return true;
	}

	public int getMinRoleLv() {
		return 1;
	}

	public int getMaxRoleLv() {
		return Short.MAX_VALUE;
	}

	public boolean isShouldSend(int roleLv) {
		return true;
	}

	/**
	 * <pre>
	 * 私聊延时发送时加时间后缀
	 * 
	 * @author CamusHuang
	 * @creation 2013-7-23 下午9:54:03
	 * </pre>
	 */
	public String notifyDelay() {
		String sendTimeStr = super.getDelayTimeStr();
		outChatString = StringUtil.format("{} ({})", getOrgChatString(), sendTimeStr);
		febackChatString = StringUtil.format("{} ({})", febackChatString, sendTimeStr);
		return null;
	}

	/**
	 * <pre>
	 * 聊天附件
	 * 只针对声音、图片需要进行缓存而设计
	 * 
	 * @author CamusHuang
	 * @creation 2014-3-7 下午12:18:17
	 * </pre>
	 */
	public static class ChatAttachment {

		public final int attId;
		public final byte[] attData;

		public ChatAttachment(int attId, byte[] attData) {
			this.attId = attId;
			this.attData = attData;
		}
	}
}
