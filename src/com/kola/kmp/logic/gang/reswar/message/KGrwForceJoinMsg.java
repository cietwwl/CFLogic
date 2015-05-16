package com.kola.kmp.logic.gang.reswar.message;

import com.koala.game.KGame;
import com.koala.game.KGameMessage;
import com.koala.game.player.KGamePlayerSession;
import com.kola.kmp.logic.gang.reswar.KResWarMsgPackCenter;
import com.kola.kmp.logic.gang.reswar.ResWarCity;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.protocol.gang.reswar.KGangResWarProtocol;

public class KGrwForceJoinMsg implements KGangResWarProtocol {

	public static void sendMsg(KGamePlayerSession session, KRole role, ResWarCity city, int pointId) {
		KGameMessage msg = KGame.newLogicMessage(SM_GANGRW_FORCE_JOIN);

		/**
		 * <pre>
		 * 服务器强制玩家进入指定城市、并打开指定资源点的对话框
		 * 
		 * byte 城市ID---进入指定城市
		 * byte 资源点ID---打开指定资源点的对话框（0表示忽略）
		 * 参考{@link #CITY_WAR_INFO}
		 * byte 资源点数量n
		 * for(0~n){
		 * 	参考{@link #RESPOINT_DATA}
		 * }
		 * </pre>
		 */
		packMsg(msg, city, pointId);
		//
		session.send(msg);
	}

	public static void packMsg(KGameMessage msg, ResWarCity city, int pointId) {

		/**
		 * <pre>
		 * 服务器强制玩家进入指定城市、并打开指定资源点的对话框
		 * 
		 * byte 城市ID---进入指定城市
		 * byte 资源点ID---打开指定资源点的对话框（0表示忽略）
		 * 参考{@link #CITY_WAR_INFO}
		 * byte 资源点数量n
		 * for(0~n){
		 * 	参考{@link #RESPOINT_DATA}
		 * }
		 * </pre>
		 */
		msg.writeByte(city.id);
		msg.writeByte(pointId);
		//
		KResWarMsgPackCenter.packCityWarInfo(msg, city);
		//
		KResWarMsgPackCenter.packResPoints(msg, city);
	}
}
