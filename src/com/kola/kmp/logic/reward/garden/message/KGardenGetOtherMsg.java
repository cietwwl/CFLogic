package com.kola.kmp.logic.reward.garden.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.role.RoleBaseInfo;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.reward.garden.KGardenCenter;
import com.kola.kmp.logic.reward.garden.KGardenRoleExtCACreator;
import com.kola.kmp.logic.reward.garden.KRoleGarden;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.CommonResult_Ext;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.util.tips.RelationShipTips;
import com.kola.kmp.protocol.reward.KRewardProtocol;

/**
 * <pre>
 * 
 * @author CamusHuang
 * @creation 2012-12-12 上午10:11:40
 * </pre>
 */
public class KGardenGetOtherMsg implements GameMessageProcesser, KRewardProtocol {

	@Override
	public GameMessageProcesser newInstance() {
		return new KGardenGetOtherMsg();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_GARDEN_OTHERS;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		// -------------
		long oppRoleId = msg.readLong();
		// -------------
		CommonResult_Ext result = new CommonResult_Ext();
		//
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		KRoleGarden oppGarden = null;
		KRole oppRole = KSupportFactory.getRoleModuleSupport().getRole(oppRoleId);
		if (oppRole == null) {
			result.tips = GlobalTips.服务器繁忙请稍候再试;
			dofinally(session, role, result, oppRoleId, oppRole, oppGarden);
			return;
		}
		
		oppGarden = KGardenRoleExtCACreator.getRoleGarden(oppRoleId);
		if (oppGarden == null) {
			result.tips = GlobalTips.此角色数据暂不能访问;
			dofinally(session, role, result, oppRoleId, oppRole, oppGarden);
			return;
		}

		if (!KSupportFactory.getRelationShipModuleSupport().isInFriendList(role.getId(), oppRoleId)) {
			result.tips = RelationShipTips.请先添加对方为好友;
			dofinally(session, role, result, oppRoleId, oppRole, oppGarden);
			return;
		}

		result.isSucess = true;
		result.tips = UtilTool.getNotNullString(null);
		dofinally(session, role, result, oppRoleId, oppRole, oppGarden);
	}

	private void dofinally(KGamePlayerSession session, KRole role, CommonResult_Ext result, long oppRoleId, KRole oppRole, KRoleGarden oppGarden) {
		if (result.isSucess) {
			if (!oppRole.isOnline()) {
				// 对方不在线
				oppGarden.autoRefreshZombieAndCollectForVIP(oppRole, System.currentTimeMillis());
			}
		}

		KGameMessage backmsg = KGame.newLogicMessage(SM_GARDEN_OTHERS_RESULT);
		backmsg.writeBoolean(result.isSucess);
		backmsg.writeUtf8String(result.tips);
		backmsg.writeLong(oppRoleId);
		if (result.isSucess) {
			KRoleGarden myGarden = KGardenRoleExtCACreator.getRoleGarden(role.getId());
			KGardenCenter.packGardenData(backmsg, oppRole, oppGarden, myGarden.getSpeedCDReleaseTime(oppRoleId));
		}
		session.send(backmsg);
	}
}
