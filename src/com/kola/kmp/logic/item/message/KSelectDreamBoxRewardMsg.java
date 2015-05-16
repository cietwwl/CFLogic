package com.kola.kmp.logic.item.message;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.item.KItem;
import com.kola.kmp.logic.item.KItemConfig;
import com.kola.kmp.logic.item.KItemDataManager;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempFixedBox;
import com.kola.kmp.logic.item.KItemLogic;
import com.kola.kmp.logic.item.KItemMsgPackCenter;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KLimitTimeProduceActivityTypeEnum;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager;
import com.kola.kmp.logic.reward.exciting.KExcitingDataManager.TimeLimitActivityDataManager.TimeLimieProduceActivity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.ItemResult_Use;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.ItemTips;
import com.kola.kmp.protocol.item.KItemProtocol;

public class KSelectDreamBoxRewardMsg implements GameMessageProcesser, KItemProtocol {
	@Override
	public GameMessageProcesser newInstance() {
		return new KSelectDreamBoxRewardMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_SELECT_DREAM_REWARD_RESULT;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		boolean isSelectOnline = msg.readBoolean();
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendSimpleDialog(session, "", GlobalTips.服务器繁忙请稍候再试, true);
			return;
		}
		//
		TimeLimieProduceActivity dreamActivity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.话费礼包活动);
		if (dreamActivity == null) {
			KDialogService.sendSimpleDialog(session, "", GlobalTips.服务器繁忙请稍候再试, true);
			return;
		}
		String tips = StringUtil.format(ItemTips.是否确认选择x奖励, isSelectOnline ? dreamActivity.commonGiftItemTemp.extItemName : dreamActivity.dreamGiftItemTemp.extItemName);

		// 发送菜单，二次确认
		List<KDialogButton> buttons = new ArrayList<KDialogButton>();
		buttons.add(KDialogButton.CANCEL_BUTTON);
		buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_CONFRIM_DREAMBOX_REWARD, isSelectOnline + "", KDialogButton.CONFIRM_DISPLAY_TEXT));
		KDialogService.sendFunDialog(role, "", tips, buttons, true, (byte) -1);
	}

	/**
	 * <pre>
	 * 二次确认选择
	 * 
	 * @param session
	 * @param script
	 * @author CamusHuang
	 * @creation 2014-11-24 下午3:54:56
	 * </pre>
	 */
	public static void confirmForDreamBoxReward(KGamePlayerSession session, String script) {
		boolean isSelectOnline = Boolean.parseBoolean(script);
		int type = isSelectOnline ? KItemConfig.FixeBOx_RewardType_ONLINE : KItemConfig.FixeBOx_RewardType_OFFLINE;
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.服务器繁忙请稍候再试);
			return;
		}

		TimeLimieProduceActivity dreamActivity = KSupportFactory.getExcitingRewardSupport().getTimeLimieProduceActivity(KLimitTimeProduceActivityTypeEnum.话费礼包活动);
		if (dreamActivity == null) {
			KDialogService.sendSimpleDialog(session, "", GlobalTips.服务器繁忙请稍候再试, true);
			return;
		}
		KItem item = KItemLogic.searchItemFromBag(role.getId(), dreamActivity.dreamGiftItemTemp.itemCode);
		if (item == null) {
			KDialogService.sendSimpleDialog(session, "", GlobalTips.服务器繁忙请稍候再试, true);
			return;
		}

		ItemResult_Use result = KItemLogic.dealMsg_openDreamBox(session, role, item.getId(), type);

		KDialogService.sendUprisingDialog(session, result.tips);
		// 处理各种提示、弹开界面二次确认
		result.doFinally(role);
	}

	public static void pushDialog(KRole role, KItem item, KItemTempFixedBox dreamCommonGiftItemTemp) {
		/**
		 * <pre>
		 * 服务器返回梦想礼包的数据，客户端显示指定选择界面后返回选择结果
		 * 返回{@link #CM_SELECT_DREAM_REWARD_RESULT}
		 * 
		 * 左边普通礼包道具，参考{@link #MSG_STRUCT_ITEM_DETAILS}
		 * 右边话费礼包道具，参考{@link #MSG_STRUCT_ITEM_DETAILS}
		 * </pre>
		 */
		KGameMessage backmsg = KGame.newLogicMessage(SM_SELECT_DREAM_REWARD);

		KItemMsgPackCenter.packItem(backmsg, dreamCommonGiftItemTemp, item.getCount());
		KItemMsgPackCenter.packItem(backmsg, role.getId(), role.getLevel(), item);
		role.sendMsg(backmsg);
	}
}
