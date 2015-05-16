package com.kola.kmp.logic.npc.dialog;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGameMessage;
import com.koala.game.exception.KGameServerException;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.KGameGlobalConfig;
import com.kola.kmp.logic.activity.KActivityModuleDialogProcesser;
import com.kola.kmp.logic.competition.KCompetitionDialogProcesser;
import com.kola.kmp.logic.level.KLevelModuleDialogProcesser;
import com.kola.kmp.logic.mission.KMissionModuleDialogProcesser;
import com.kola.kmp.logic.npc.message.KShowDialogMsg;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KNPCOrderEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.CurrencyTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.protocol.npc.KNpcProtocol;

/**
 * <pre>
 * 工作原理：
 * 1.在本类{@link #onGameWorldInitComplete()}方法中硬代码注册相应的对话处理器
 * 
 * @author CamusHuang
 * @creation 2014-2-26 下午4:23:36
 * </pre>
 */
public class KDialogService {

	/**
	 * 默认的对话框显示时间（秒）
	 */
	public static final byte DEFAULT_TIME = 5;

	/**
	 * <pre>
	 * 保存所有对话框按钮处理器
	 * </pre>
	 */
	private static final List<IDialogProcesser> _processers = new ArrayList<IDialogProcesser>();

	public static void onGameWorldInitComplete() throws KGameServerException {
		{
			short minFunId = -100;
			short maxFunId = 100;// 不包含
			KDefaultDialogProcesser processer = new KDefaultDialogProcesser(minFunId, maxFunId);
			_processers.add(processer);
		}
		// CTODO 所有对话框处理器必须显式在下面注册，必须保证功能ID互不冲突
		{
			short minFunId = 600;
			short maxFunId = 700;// 不包含
			KMissionModuleDialogProcesser processer = new KMissionModuleDialogProcesser(minFunId, maxFunId);
			_processers.add(processer);
		}
		// 关卡模块处理器
		{
			short minFunId = 400;
			short maxFunId = 500;// 不包含
			KLevelModuleDialogProcesser processer = new KLevelModuleDialogProcesser(minFunId, maxFunId);
			_processers.add(processer);
		}
		// 竞技场模块处理器
		{
			short minFunId = 700;
			short maxFunId = 800;// 不包含
			KCompetitionDialogProcesser processer = new KCompetitionDialogProcesser(minFunId, maxFunId);
			_processers.add(processer);
		}
		// 活动模块
		{
			short minFunId = 900;
			short maxFunId = 1000; // 不包含
			KActivityModuleDialogProcesser processer = new KActivityModuleDialogProcesser(minFunId, maxFunId);
			_processers.add(processer);
		}
	}

	/**
	 * <pre>
	 * 处理客户端发来的对话框按钮信息
	 * 
	 * @param funId
	 * @param script
	 * @param session
	 * @author CamusHuang
	 * @creation 2014-3-11 下午2:52:33
	 * </pre>
	 */
	public static void processDialogFun(KGamePlayerSession session, short funId, String script) {
		// 根据功能ID，找到相应的处理器进行处理
		for (IDialogProcesser processer : _processers) {
			if (processer.isMyFunId(funId)) {
				processer.processFun(funId, script, session);
				break;
			}
		}
	}

	/**
	 * <pre>
	 * 发送一个简单的对话框消息到客户端
	 * 该对话框，只会单纯显示参数所带的标题（title），内容（content），
	 * 并且只有一个”确定“按钮，客户端点解后关闭对话框，不会有任何消息
	 * 返回到服务器
	 * </pre>
	 * 
	 * @param roleId
	 * @param title
	 *            标题
	 * @param content
	 *            内容
	 */
	public static void sendSimpleDialog(long roleId, String title, String content) {
		sendSimpleDialog(roleId, title, content, false, DEFAULT_TIME);
	}

	/**
	 * <pre>
	 * 发送一个简单的对话框消息到客户端
	 * 该对话框，只会单纯显示参数所带的标题（title），内容（content），
	 * 并且只有一个”确定“按钮，停留时间为time秒，可以通过center变量控制
	 * 文字是否居中，客户端点解后关闭对话框，不会有任何消息返回到服务器
	 * </pre>
	 * 
	 * @param roleId
	 * @param title
	 *            标题
	 * @param content
	 *            内容
	 * @param center
	 *            是否居中
	 * @param time
	 *            停留时间（-1为永久停留）
	 */
	public static void sendSimpleDialog(long roleId, String title, String content, boolean center, byte time) {
		KGameMessage msg = KShowDialogMsg.createSimpleDialogMsg(title, content, center, time);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	/**
	 * <pre>
	 * 发送一个简单的对话框消息到客户端
	 * 该对话框，只会单纯显示参数所带的标题（title），内容（content），
	 * 并且只有一个”确定“按钮，停留时间为time秒，可以通过center变量控制
	 * 文字是否居中，客户端点解后关闭对话框，不会有任何消息返回到服务器
	 * </pre>
	 * 
	 * @param role
	 * @param title
	 *            标题
	 * @param content
	 *            内容
	 * @param center
	 *            是否居中
	 * @param time
	 *            停留时间（-1为永久停留）
	 */
	public static void sendSimpleDialog(KRole role, String title, String content, boolean center, byte time) {
		KGameMessage msg = KShowDialogMsg.createSimpleDialogMsg(title, content, center, time);
		role.sendMsg(msg);
	}

	public static void sendSimpleDialog(KGamePlayerSession session, String title, String content) {
		KGameMessage msg = KShowDialogMsg.createSimpleDialogMsg(title, content, false, DEFAULT_TIME);
		session.send(msg);
	}

	public static void sendSimpleDialog(KGamePlayerSession session, String title, String content, boolean center) {
		KGameMessage msg = KShowDialogMsg.createSimpleDialogMsg(title, content, center, DEFAULT_TIME);
		session.send(msg);
	}

	public static void sendSimpleDialog(KGamePlayerSession session, String title, String content, boolean center, byte time) {
		KGameMessage msg = KShowDialogMsg.createSimpleDialogMsg(title, content, center, time);
		session.send(msg);
	}

	/**
	 * <pre>
	 * 发送一个简单的对话框消息到客户端
	 * 该对话框，只会单纯显示参数所带的标题（title），内容（content），
	 * 并且只有一个”确定“按钮，客户端点解后关闭对话框，不会有任何消息
	 * 返回到服务器
	 * </pre>
	 * 
	 * @param role
	 * @param title
	 *            标题
	 * @param content
	 *            内容
	 */
	public static void sendSimpleDialog(KRole role, String title, String content) {
		sendSimpleDialog(role, title, content, false, DEFAULT_TIME);
	}

	/**
	 * <pre>
	 * 发送一个带有功能键的对话框消息到客户端
	 * 该对话框，可以自定义任意数量的功能键，并且通过自己唯一标识的功能
	 * 键id来等待客户端的选择。客户端点击功能键后，如果功能键不为{@link #FUN_ID_CLOSE_DIALOG}，
	 * 则会把功能键id通过{@link KNpcProtocol#CM_EXECUTE_DIALOG_FUN}消息返回到服务器，
	 * 服务器再根据功能id进行派送
	 * </pre>
	 * 
	 * @param role
	 * @param title
	 * @param content
	 * @param center
	 * @param time
	 * @param buttons
	 */
	public static void sendFunDialog(KRole role, String title, String content, List<KDialogButton> buttons, boolean center, byte time) {
		KGameMessage msg = KShowDialogMsg.createFunMsg(title, content, center, time, buttons);
		role.sendMsg(msg);
	}

	/**
	 * 
	 * @param roleId
	 * @param title
	 * @param content
	 * @param center
	 * @param time
	 * @param buttons
	 */
	public static void sendFunDialog(long roleId, String title, String content, List<KDialogButton> buttons, boolean center, byte time) {
		KGameMessage msg = KShowDialogMsg.createFunMsg(title, content, center, time, buttons);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	/**
	 * 
	 * @param session
	 * @param title
	 * @param content
	 * @param center
	 * @param time
	 * @param buttons
	 */
	public static void sendFunDialog(KGamePlayerSession session, String title, String content, List<KDialogButton> buttons, boolean center, byte time) {
		KGameMessage msg = KShowDialogMsg.createFunMsg(title, content, center, time, buttons);
		session.send(msg);
	}

	public static KDialogButton createButton(short funcId, String script, String displayText) {
		return new KDialogButton(funcId, script, displayText);
	}

	/**
	 * <pre>
	 * 发送一个UI风格（区别于NPC风格）的单选对话框到客户端
	 * 
	 * @param role
	 * @param title
	 * @param content
	 * @param center
	 * @param time
	 * @param buttons
	 * @author CamusHuang
	 * @creation 2013-8-26 上午10:32:50
	 * </pre>
	 */
	public static void sendUIRatioDialog(long roleId, String title, String content, List<KDialogButton> buttons, byte time) {
		KGameMessage msg = KShowDialogMsg.createUIRatioDialogMsg(title, content, true, time, buttons);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	/**
	 * <pre>
	 * 发送一条对话框的反馈消息到客户端，让客户端取消锁定
	 * </pre>
	 * 
	 * @param role
	 */
	public static void sendNullDialog(KRole role) {
		KGameMessage msg = KShowDialogMsg.createNullDialogMsg();
		role.sendMsg(msg);
	}

	/**
	 * 
	 * @param session
	 */
	public static void sendNullDialog(KGamePlayerSession session) {
		KGameMessage msg = KShowDialogMsg.createNullDialogMsg();
		session.send(msg);
	}

	public static void sendUprisingDialog(long roleId, String... content) {
		if (content == null || content.length < 1) {
			return;
		}
		KGameMessage msg = KShowDialogMsg.createUprisingDialogMsg(content);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	public static void sendUprisingDialog(KRole role, String... content) {
		if (content == null || content.length < 1) {
			return;
		}
		KGameMessage msg = KShowDialogMsg.createUprisingDialogMsg(content);
		role.sendMsg(msg);
	}

	public static void sendUprisingDialog(KRole role, List<String> content) {
		if (content == null || content.isEmpty()) {
			return;
		}
		KGameMessage msg = KShowDialogMsg.createUprisingDialogMsg(content);
		role.sendMsg(msg);
	}

	public static void sendUprisingDialog(KGamePlayerSession session, String... content) {
		if (content == null || content.length < 1) {
			return;
		}
		KGameMessage msg = KShowDialogMsg.createUprisingDialogMsg(content);
		session.send(msg);
	}

	public static void sendUprisingDialog(KGamePlayerSession session, List<String> content) {
		if (content == null || content.isEmpty()) {
			return;
		}
		KGameMessage msg = KShowDialogMsg.createUprisingDialogMsg(content);
		session.send(msg);
	}

	public static void sendDataUprisingDialog(KGamePlayerSession session, String... content) {
		if (content == null || content.length < 1) {
			return;
		}
		KGameMessage msg = KShowDialogMsg.createDataUprisingDialogMsg(content);
		session.send(msg);
	}

	public static void sendDataUprisingDialog(KGamePlayerSession session, List<String> content) {
		if (content == null || content.isEmpty()) {
			return;
		}
		KGameMessage msg = KShowDialogMsg.createDataUprisingDialogMsg(content);
		session.send(msg);
	}

	public static void sendDataUprisingDialog(KRole role, String... content) {
		if (content == null || content.length < 1) {
			return;
		}
		KGameMessage msg = KShowDialogMsg.createDataUprisingDialogMsg(content);
		role.sendMsg(msg);
	}

	public static void sendDataUprisingDialog(KRole role, List<String> content) {
		if (content == null || content.isEmpty()) {
			return;
		}
		KGameMessage msg = KShowDialogMsg.createDataUprisingDialogMsg(content);
		role.sendMsg(msg);
	}

	public static void sendDataUprisingDialog(long roleId, String... content) {
		if (content == null || content.length < 1) {
			return;
		}
		KGameMessage msg = KShowDialogMsg.createDataUprisingDialogMsg(content);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	public static void sendDataUprisingDialog(long roleId, List<String> content) {
		if (content == null || content.isEmpty()) {
			return;
		}
		KGameMessage msg = KShowDialogMsg.createDataUprisingDialogMsg(content);
		KSupportFactory.getRoleModuleSupport().sendMsg(roleId, msg);
	}

	/**
	 * <pre>
	 * 询问是否跳转到VIP界面
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2013-6-27 下午3:23:50
	 * </pre>
	 */
	public static void showVIPDialog(long roleId, String content) {

		List<KDialogButton> buttons = new ArrayList<KDialogButton>();
		buttons.add(KDialogButton.CANCEL_BUTTON);
		buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_OPEN_UI, KNPCOrderEnum.ORDER_OPEN_VIPUI.sign + "", KDialogButton.CONFIRM_DISPLAY_TEXT));
		KDialogService.sendFunDialog(roleId, "", content, buttons, true, (byte) -1);
	}

	/**
	 * <pre>
	 * 询问是否跳转到充值界面
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2013-6-27 下午3:23:50
	 * </pre>
	 */
	public static void showChargeDialog(long roleId, String content) {
		// 默认打开充值界面，如果未首充，则打开首充界面
		if (KSupportFactory.getRewardModuleSupport().isWaitFirstCharge(roleId)) {
			showFirstChargeDialog(roleId, content);
			return;
		}

		List<KDialogButton> buttons = new ArrayList<KDialogButton>();
		buttons.add(KDialogButton.CANCEL_BUTTON);
		buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_OPEN_UI, KNPCOrderEnum.ORDER_OPEN_CHARGE.sign + "", KDialogButton.CONFIRM_DISPLAY_TEXT));
		KDialogService.sendFunDialog(roleId, "", content, buttons, true, (byte) -1);
	}

	/**
	 * <pre>
	 * 询问是否跳转到首充界面
	 * 
	 * @param roleId
	 * @param content
	 * @author CamusHuang
	 * @creation 2013-12-31 下午4:51:31
	 * </pre>
	 */
	public static void showFirstChargeDialog(long roleId, String content) {

		List<KDialogButton> buttons = new ArrayList<KDialogButton>();
		buttons.add(KDialogButton.CANCEL_BUTTON);
		buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_OPEN_UI, KNPCOrderEnum.ORDER_OPEN_FIRST_CHARGE.sign + "", KDialogButton.CONFIRM_DISPLAY_TEXT));
		KDialogService.sendFunDialog(roleId, "", content, buttons, true, (byte) -1);
	}

	/**
	 * <pre>
	 * 询问是否跳转到金币兑换界面
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2013-6-27 下午3:23:50
	 * </pre>
	 */
	public static void showExchangeDialog(long roleId, String content, long goldCount) {
		if (KGameGlobalConfig.isVipEnable()) {
			List<KDialogButton> buttons = new ArrayList<KDialogButton>();
			buttons.add(KDialogButton.CANCEL_BUTTON);
			buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_OPEN_UI, KNPCOrderEnum.ORDER_OPEN_EXCHANGE.sign + "," + goldCount, KDialogButton.CONFIRM_DISPLAY_TEXT));
			KDialogService.sendFunDialog(roleId, "", content, buttons, true, (byte) -1);
		} else {
			KDialogService.sendUprisingDialog(roleId, GlobalTips.getTipsMaterialNeedCount(KCurrencyTypeEnum.GOLD.extName, (int) goldCount));
		}
	}

	/**
	 * <pre>
	 * 询问是否跳转到商店界面
	 * 
	 * @deprecated 未使用
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2013-6-27 下午3:23:50
	 * </pre>
	 */
	public static void showShopDialog(long roleId, String content, String itemCode) {

		List<KDialogButton> buttons = new ArrayList<KDialogButton>();
		buttons.add(KDialogButton.CANCEL_BUTTON);
		buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_OPEN_UI, KNPCOrderEnum.NPC_ORDER_SHOP.sign + "," + itemCode, KDialogButton.CONFIRM_DISPLAY_TEXT));
		KDialogService.sendFunDialog(roleId, "", content, buttons, true, (byte) -1);
	}
}
