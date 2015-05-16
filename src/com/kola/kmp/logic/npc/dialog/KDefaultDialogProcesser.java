package com.kola.kmp.logic.npc.dialog;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.gang.message.KExitGangMsg;
import com.kola.kmp.logic.gang.message.KInviteRoleMsg;
import com.kola.kmp.logic.gang.message.KMakeOverSirMsg;
import com.kola.kmp.logic.gang.reswar.ResPointBeSeizedManager;
import com.kola.kmp.logic.gang.reswar.message.KGrwOccResPointMsg;
import com.kola.kmp.logic.item.message.KBuyEnchasePositionMsg;
import com.kola.kmp.logic.item.message.KBuyItemPackCellMsg;
import com.kola.kmp.logic.item.message.KCancelEnchaseMsg;
import com.kola.kmp.logic.item.message.KCompose2Msg;
import com.kola.kmp.logic.item.message.KEquiInheritMsg;
import com.kola.kmp.logic.item.message.KEquipmentEnchaseMsg;
import com.kola.kmp.logic.item.message.KOneKeyCancelEnchaseMsg;
import com.kola.kmp.logic.item.message.KSelectDreamBoxRewardMsg;
import com.kola.kmp.logic.mount.message.KMountResetSPMsg;
import com.kola.kmp.logic.npc.message.KNPCOrderMsg;
import com.kola.kmp.logic.other.KNPCOrderEnum;
import com.kola.kmp.logic.rank.message.KGoodJobMsg;
import com.kola.kmp.logic.reward.garden.message.KGardenCollectTopMsg;
import com.kola.kmp.logic.reward.garden.message.KGardenGetVipSaveMsg;
import com.kola.kmp.logic.reward.login.message.KLoginAddCheckUpMsg;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.shop.PhyPowerShopCenter;
import com.kola.kmp.logic.shop.message.KBuyExcitingActivityMsg;
import com.kola.kmp.logic.shop.message.KBuyFashionMsg;
import com.kola.kmp.logic.shop.random.message.KRefreshRandomShopMsg;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 默认的对话框按钮处理器
 * 
 * @author CamusHuang
 * @creation 2014-3-11 下午2:54:18
 * </pre>
 */
public class KDefaultDialogProcesser extends IDialogProcesser {

	// --------- 以下是关于对话框按钮功能ID的定义 1~100 ---------------
	public static final short DIALOG_FUN_ID_OPEN_UI = 1;// 确认则打开相应界面
	public static final short DIALOG_FUN_ID_EXTEND_PACK = 2;// 确认付费扩容道具包
	public static final short DIALOG_FUN_ID_EXTEND_ENCHANSE = 3;// 确认付费扩容镶嵌槽
	public static final short DIALOG_FUN_ID_CANCEL_ENCHANSE = 4;// 确认付费取消镶嵌
	public static final short DIALOG_FUN_ID_MAKEOVER_SIR = 5;// 确认禅让军团长职位
	public static final short DIALOG_FUN_ID_EXIT_AND_DIMISS = 6;// 确认退团并解散
	public static final short DIALOG_FUN_ID_PAY_FOR_REFRESH_RANDOMSHOP = 7;// 确认付费进行随机商店刷新
	public static final short DIALOG_FUN_ID_GIVEUP_OCCRESPOINT = 8;// 确认放弃资源占领点
	public static final short DIALOG_FUN_ID_FOCEJOIN_SEIZE = 9;// 确认前去抢夺被别人抢占的资源点
	public static final short DIALOG_FUN_ID_BUY_PHYPOW = 10;// 确认是否购买体力
	public static final short DIALOG_FUN_ID_PAY_COMPOSEITEM = 11;// 确认是否付费合成
	public static final short DIALOG_FUN_ID_RANKGOODJOBPAY = 12;// 确认是否支付点赞
	public static final short DIALOG_FUN_ID_ONEKEY_CANCEL_ENCHANSE = 13;// 确认付费一键取消镶嵌
	public static final short DIALOG_FUN_ID_CONFIRM_SWALLOW_SENIOR_PET = 14; // 确认合成包含紫色品质的随从
	public static final short DIALOG_FUN_ID_ENCHANSE = 15;// 确认付费镶嵌更高级的宝石
	public static final short DIALOG_FUN_ID_ONEKEY_INHERIT = 16;// 确认一键继承
	public static final short DIALOG_FUN_ID_GET_GARDEN_VIPSAVE = 17;// 确认获取庄园VIP存储
	public static final short DIALOG_FUN_ID_BUY_FASHION = 18;// 确认是否购买时装
	public static final short DIALOG_FUN_ID_ACCEPT_GANG_INVITE = 19;// 确认接受军团邀请
	public static final short DIALOG_FUN_ID_CONFRIM_DREAMBOX_REWARD = 20;// 二次确认固定宝箱线下或线上奖励
	public static final short DIALOG_FUN_ID_ADD_CHUCKUP = 22;// 确认是否补签到
	public static final short DIALOG_FUN_ID_PAYFOR_EXCITING = 23;// 确认是否购买精彩活动
	public static final short DIALOG_FUN_ID_PAY_GARDEN = 24;// 确认是否付费摘果
	public static final short DIALOG_FUN_ID_CONFIRM_PET_EXP_OVERFLOW = 25;
	public static final short DIALOG_FUN_ID_CONFIRM_SET_FREE = 26;
	public static final short DIALOG_FUN_ID_PAY_RESET_SP = 27;// 确认是否付费重置机甲SP点

	public static KDefaultDialogProcesser instance;

	KDefaultDialogProcesser(short minFunId, short maxFunId) {
		super(minFunId, maxFunId);
		instance = this;
	}

	@Override
	public void processFun(short funId, String script, KGamePlayerSession session) {
		switch (funId) {
		case DIALOG_FUN_ID_OPEN_UI:
			confirmToOpenUI(session, script);
			break;
		case DIALOG_FUN_ID_EXTEND_PACK:
			KBuyItemPackCellMsg.confirmExtendPack(session, script);
			break;
		case DIALOG_FUN_ID_EXTEND_ENCHANSE:
			KBuyEnchasePositionMsg.confirmExtendEnchanse(session, script);
			break;
		case DIALOG_FUN_ID_CANCEL_ENCHANSE:
			KCancelEnchaseMsg.confirmCancelEnchanse(session, script);
			break;
		case DIALOG_FUN_ID_MAKEOVER_SIR:
			KMakeOverSirMsg.confirmByDialog(session, script);
			break;
		case DIALOG_FUN_ID_EXIT_AND_DIMISS:
			KExitGangMsg.confirmByDialog(session, script);
			break;
		case DIALOG_FUN_ID_PAY_FOR_REFRESH_RANDOMSHOP:
			KRefreshRandomShopMsg.confirmByDialog(session, script);
			break;
		case DIALOG_FUN_ID_GIVEUP_OCCRESPOINT:
			KGrwOccResPointMsg.confirmByDialog(session, script);
			break;
		case DIALOG_FUN_ID_FOCEJOIN_SEIZE:
			ResPointBeSeizedManager.confirmByDialog(session, script);
			break;
		case DIALOG_FUN_ID_BUY_PHYPOW:
			PhyPowerShopCenter.confirmBuyPhyPower(session);
			break;
		case DIALOG_FUN_ID_PAY_COMPOSEITEM:
			KCompose2Msg.confirmPayByDialog(session, script);
			break;
		case DIALOG_FUN_ID_RANKGOODJOBPAY:
			KGoodJobMsg.confirmByDialog(session, script);
			break;
		case DIALOG_FUN_ID_ONEKEY_CANCEL_ENCHANSE:
			KOneKeyCancelEnchaseMsg.confirmCancelEnchanse(session, script);
			break;
		case DIALOG_FUN_ID_CONFIRM_SWALLOW_SENIOR_PET:
		case DIALOG_FUN_ID_CONFIRM_PET_EXP_OVERFLOW:
		{
			boolean confirmSenior;
			boolean confirmOverflow;
			if(funId == DIALOG_FUN_ID_CONFIRM_SWALLOW_SENIOR_PET) {
				confirmSenior = true;
				confirmOverflow = false;
			} else {
				confirmSenior = true;
				confirmOverflow = true;
			}
			String[] args = script.split(",");
			List<Long> beComposedIds = new ArrayList<Long>();
			long swallowerPetId = Long.parseLong(args[0]);
			for (int i = 1; i < args.length; i++) {
				beComposedIds.add(Long.parseLong(args[i]));
			}
			KRole krole = KSupportFactory.getRoleModuleSupport().getRole(session);
			KSupportFactory.getPetModuleSupport().processComposePet(krole, swallowerPetId, beComposedIds, confirmSenior, confirmOverflow);
		}
			break;
		case DIALOG_FUN_ID_ENCHANSE:
			KEquipmentEnchaseMsg.confirmCancelEnchanse(session, script);
			break;
		case DIALOG_FUN_ID_ONEKEY_INHERIT:
			KEquiInheritMsg.confirmOneKeyInherit(session, script);
			break;
		case DIALOG_FUN_ID_GET_GARDEN_VIPSAVE:
			KGardenGetVipSaveMsg.confirmToGetVipSave(session, script);
			break;
		case DIALOG_FUN_ID_BUY_FASHION:
			KBuyFashionMsg.confirmByDialog(session, script);
			break;
		case DIALOG_FUN_ID_ACCEPT_GANG_INVITE:
			KInviteRoleMsg.confirmByDialog(session, script);
			break;
		case DIALOG_FUN_ID_CONFRIM_DREAMBOX_REWARD:
			KSelectDreamBoxRewardMsg.confirmForDreamBoxReward(session, script);
			break;
		case DIALOG_FUN_ID_ADD_CHUCKUP:
			KLoginAddCheckUpMsg.confirmByDialog(session, script);
			break;
		case DIALOG_FUN_ID_PAYFOR_EXCITING:
			KBuyExcitingActivityMsg.confirmByDialog(session, script);
			break;
		case DIALOG_FUN_ID_PAY_GARDEN:
			KGardenCollectTopMsg.confirmByDialog(session, script);
			break;
		case DIALOG_FUN_ID_CONFIRM_SET_FREE:
		{
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
			long setFreePetId = Long.parseLong(script);
			KSupportFactory.getPetModuleSupport().processSetFreePet(role, setFreePetId, true);
			break;
		}
		case DIALOG_FUN_ID_PAY_RESET_SP:
			KMountResetSPMsg.confirmByDialog(session, script);
			break;
		default:
		}
	}

	/**
	 * <pre>
	 * 玩家确认打开界面
	 * 
	 * @param session
	 * @param script 界面ID,脚本
	 * @author CamusHuang
	 * @creation 2013-6-27 下午4:54:16
	 * </pre>
	 */
	private static void confirmToOpenUI(KGamePlayerSession session, String script) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		String[] strs = script.split(",");
		KNPCOrderEnum order = KNPCOrderEnum.getEnum(Integer.parseInt(strs[0]));
		if (role == null || order == null) {
			return;
		}
		KDialogService.sendNullDialog(session);
		if (strs.length > 1) {
			KNPCOrderMsg.sendNPCMenuOrder(role, order, strs[1]);
		} else {
			KNPCOrderMsg.sendNPCMenuOrder(role, order, null);
		}
	}
}
