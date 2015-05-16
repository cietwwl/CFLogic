package com.kola.kmp.logic.support;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.kola.kgame.cache.flowdata.impl.PresentPointTypeEnum;
import com.kola.kgame.cache.mail.impl.KAMail;
import com.kola.kmp.logic.mail.KMail;
import com.kola.kmp.logic.mail.KMailConfig;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.util.ResultStructs.MailResult;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2014-2-21 下午4:58:49
 * </pre>
 */
public interface MailModuleSupport {

	/**
	 * <pre>
	 * 系统发奖
	 * 
	 * @param receiverRoleId
	 * @param itemCode
	 * @param count
	 * @param title 不能为null
	 * @param content 不能为null
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-22 下午5:45:31
	 * </pre>
	 */
	public boolean sendAttMailBySystem(long receiverRoleId, String itemCode, long count, String title, String content);

	/**
	 * <pre>
	 * 系统发奖
	 * 
	 * @param receiverRoleId
	 * @param itemStruct
	 * @param title 不能为null
	 * @param content 不能为null
	 * @return
	 * @author CamusHuang
	 * @creation 2014-3-14 下午8:32:28
	 * </pre>
	 */
	public boolean sendAttMailBySystem(long receiverRoleId, ItemCountStruct itemStruct, String title, String content);

	/**
	 * <pre>
	 * 系统发奖
	 * 
	 * @param receiverRoleId
	 * @param itemList
	 * @param title 不能为null
	 * @param content 不能为null
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-22 下午5:42:38
	 * </pre>
	 */
	public MailResult sendAttMailBySystem(long receiverRoleId, List<ItemCountStruct> itemList, String title, String content);

	/**
	 * <pre>
	 * 系统发奖
	 * 
	 * @param receiverRoleId
	 * @param mailReward
	 * @param presentType
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-22 下午5:30:52
	 * </pre>
	 */
	public MailResult sendAttMailBySystem(long receiverRoleId, BaseMailRewardData mailReward, PresentPointTypeEnum presentType);

	/**
	 * <pre>
	 * 系统发奖
	 * 
	 * @param receiverRoleId
	 * @param mainContent
	 * @param reward
	 * @param presentType
	 * @return
	 * @author CamusHuang
	 * @creation 2014-8-15 下午12:19:09
	 * </pre>
	 */
	public MailResult sendAttMailBySystem(long receiverRoleId, BaseMailContent mainContent, BaseRewardData reward, PresentPointTypeEnum presentType);

	/**
	 * <pre>
	 * 系统发奖
	 * 
	 * @param receiverRoleId
	 * @param moneyList
	 * @param presentType
	 * @param title
	 * @param content
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-12 上午11:47:09
	 * </pre>
	 */
	public MailResult sendMoneyMailBySystem(long receiverRoleId, List<KCurrencyCountStruct> moneyList, PresentPointTypeEnum presentType, String title, String content);

	/**
	 * <pre>
	 * 以任意身份（可以是系统）发送普通邮件
	 * 
	 * @param senderRoleId 发送者角色ID，如果是系统类型则使用{@link KMailConfig#SYS_MAIL_SENDER_ID}
	 * @param senderRoleName 发送者角色ID，如果是系统类型则使用{@link KMailConfig#SYS_MAIL_SENDER_NAME}
	 * @param receiverRoleId 接收者角色ID
	 * @param mailType 邮件类型，参考{@link KAMail#TYPE_COMMON}
	 * @param title 不能为null
	 * @param content 不能为null
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-23 上午10:44:25
	 * </pre>
	 */
	public MailResult sendSimpleMail(long senderRoleId, String senderRoleName, long receiverRoleId, int mailType, String title, String content);

	/**
	 * <pre>
	 * 系统发送普通邮件
	 * 
	 * @param receiverRoleId
	 * @param title
	 * @param content
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-22 下午5:40:17
	 * </pre>
	 */
	public MailResult sendSimpleMailBySystem(long receiverRoleId, String title, String content);

	/**
	 * <pre>
	 * 以任意身份（可以是系统）群发普通邮件
	 * 
	 * @param senderRoleId 发送者角色ID，如果是系统类型则使用{@link KMailConfig#SYS_MAIL_SENDER_ID}
	 * @param senderRoleName 发送者角色ID，如果是系统类型则使用{@link KMailConfig#SYS_MAIL_SENDER_NAME}
	 * @param receiverRoleIds 接收者角色ID
	 * @param mailType 邮件类型，参考{@link KAMail#TYPE_COMMON}
	 * @param title 不能为null
	 * @param content 不能为null
	 * @return
	 * @author CamusHuang
	 * @creation 2014-2-22 下午5:30:57
	 * </pre>
	 */
	public Map<Long, KMail> sendGroupSimpleMail(long senderRoleId, String senderRoleName, Collection<Long> receiverRoleIds, int mailtype, String title, String content);

	/**
	 * <pre>
	 * 系统向指定等级范围的在线角色群发邮件
	 * 
	 * @param minLv
	 * @param maxLv
	 * @param title
	 * @param content
	 * @return
	 * @author CamusHuang
	 * @creation 2014-11-3 上午10:29:28
	 * </pre>
	 */
	public Map<Long, KMail> sendSimpleMailToOnlineBySystem(int minLv, int maxLv, String title, String content);
	
	/**
	 * <pre>
	 * 克隆角色数据
	 * 
	 * @param myRole
	 * @param srcRole
	 * @author CamusHuang
	 * @creation 2014-10-15 上午11:50:31
	 * </pre>
	 */
	public void cloneRoleByCamus(KRole myRole, KRole srcRole);
}
