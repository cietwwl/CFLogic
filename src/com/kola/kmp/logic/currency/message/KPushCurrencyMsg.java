package com.kola.kmp.logic.currency.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.kola.kmp.logic.currency.KCurrencyAccountSet;
import com.kola.kmp.logic.currency.KCurrencyConfig;
import com.kola.kmp.logic.currency.KCurrencyDataManager;
import com.kola.kmp.logic.currency.KCurrencyModuleExtension;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.protocol.currency.KCurrencyProtocol;

/**
 * <pre>
 * 
 * 
 * @author CamusHuang
 * @creation 2012-12-10 下午3:53:53
 * </pre>
 */
public class KPushCurrencyMsg implements KCurrencyProtocol {

	/**
	 * <pre>
	 * 服务器端反馈（在有必要时，服务器会主动推送此消息）
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2012-12-10 下午4:02:57
	 * </pre>
	 */
	public static void sendMsg(KCurrencyAccountSet set) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(set._ownerId);
		if (role != null) {
			sendMsg(role, set);
		}
	}

	/**
	 * <pre>
	 * 服务器端反馈（在有必要时，服务器会主动推送此消息）
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2012-12-10 下午4:02:57
	 * </pre>
	 */
	public static void sendMsg(KRole role) {
		KCurrencyAccountSet set = KCurrencyModuleExtension.getCurrencyAccountSet(role.getId());
		if (set != null) {
			sendMsg(role, set);
		}
	}

	/**
	 * <pre>
	 * 服务器端反馈（在有必要时，服务器会主动推送此消息）
	 * 
	 * @param roleId
	 * @author CamusHuang
	 * @creation 2012-12-10 下午4:02:57
	 * </pre>
	 */
	public static void sendMsg(KRole role, KCurrencyAccountSet set) {
		if (role.isOnline()) {
			KGameMessage msg = KGame.newLogicMessage(SM_PUSH_CURRENCY);
			msg.writeLong(set.getAccount(KCurrencyTypeEnum.DIAMOND.sign).getBalance());
			msg.writeLong(set.getAccount(KCurrencyTypeEnum.GOLD.sign).getBalance());
			msg.writeLong(set.getAccount(KCurrencyTypeEnum.POTENTIAL.sign).getBalance());
			msg.writeLong(set.getAccount(KCurrencyTypeEnum.SCORE.sign).getBalance());
			msg.writeLong(set.getAccount(KCurrencyTypeEnum.GANG_CONTRIBUTION.sign).getBalance());
			//
			msg.writeInt(KCurrencyConfig.getInstance().DiamondToGoldBase);
			int DiamondToGoldRate = KCurrencyDataManager.mDiamondToGoldDataManager.getRateForTen(role.getLevel());
			msg.writeInt(DiamondToGoldRate);
			msg.writeInt(KCurrencyConfig.getInstance().DiamondToGoldBuyRate);
			
			msg.writeInt(set.getMontyCardReleaseDay());
			msg.writeBoolean(KCurrencyDataManager.mChargeInfoManager.monthCard==null?false:KCurrencyDataManager.mChargeInfoManager.monthCard.isMonthCardCanBuyMuil);
			role.sendMsg(msg);
		}
	}
}
