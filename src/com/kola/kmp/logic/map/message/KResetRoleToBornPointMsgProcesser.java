package com.kola.kmp.logic.map.message;

import static com.kola.kmp.protocol.map.KMapProtocol.CM_RESET_SPRITE_XY;

import com.koala.game.KGameMessage;
import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kgame.cache.util.GameMessageProcesser;
import com.kola.kmp.logic.map.KCoordinate;
import com.kola.kmp.logic.map.KGameNormalMap;
import com.kola.kmp.logic.map.KMapModuleFactory;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.tips.GlobalTips;

public class KResetRoleToBornPointMsgProcesser implements GameMessageProcesser {

	@Override
	public GameMessageProcesser newInstance() {
		return new KResetRoleToBornPointMsgProcesser();
	}

	@Override
	public int getMsgIdHandled() {
		return CM_RESET_SPRITE_XY;
	}

	@Override
	public void processMessage(KGameMessageEvent msgEvent) throws Exception {
		KGamePlayerSession session = msgEvent.getPlayerSession();
		KGameMessage msg = msgEvent.getMessage();
		
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if(role!=null){
			KGameNormalMap map = KMapModuleFactory.getModule().getGameMapManager().getGameMap(role.getRoleMapData().getCurrentMapId());
			if (map == null) {
				KDialogService.sendUprisingDialog(role, GlobalTips.服务器繁忙请稍候再试);
				return;
			}
			
			KCoordinate coor = map.getGameMapData().getPlayerRoleJumpMapCoordinateWithNoDirection();
			if(coor!=null){
				KMapModuleFactory.getModule().getGameMapManager().processPlayerRoleResetCoordinate(role, coor.getX(), coor.getY());
			}else{
				KDialogService.sendUprisingDialog(role, GlobalTips.服务器繁忙请稍候再试);
				return;
			}
		}
		
	}

}
