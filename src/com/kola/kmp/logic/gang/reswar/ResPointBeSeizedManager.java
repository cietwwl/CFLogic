package com.kola.kmp.logic.gang.reswar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.koala.game.player.KGamePlayerSession;
import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KCityTempManager.CityTemplate;
import com.kola.kmp.logic.gang.reswar.KResWarDataManager.KResPointTempManager.ResPointTemplate;
import com.kola.kmp.logic.gang.reswar.message.KGrwForceJoinMsg;
import com.kola.kmp.logic.npc.dialog.KDefaultDialogProcesser;
import com.kola.kmp.logic.npc.dialog.KDialogButton;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.ResultStructs.GangResWarResult_Occ;
import com.kola.kmp.logic.util.tips.GangResWarTips;
import com.kola.kmp.logic.util.tips.GlobalTips;

/**
 * <pre>
 * 资源点被抢夺事件管理器
 * 对话框：你占领的XX被占领，是否前去抢夺？---玩家在城市界面时才弹出，否则缓存
 * 您在【XX城市】占领的【警察局】被敌方军团的XXXX（LV.30）所占领，是否前去抢夺？
 * 
 * @author CamusHuang
 * @creation 2014-5-14 下午2:41:37
 * </pre>
 */
public class ResPointBeSeizedManager {

	private static final Map<Long, SeizeEvent> eventMap = new ConcurrentHashMap<Long, SeizeEvent>();

	static void clear() {
		eventMap.clear();
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param orgRoleId
	 * @param cityId
	 * @param resPointId
	 * @param oppRole null表示超时
	 * @author CamusHuang
	 * @creation 2014-5-15 上午9:59:24
	 * </pre>
	 */
	static void newEvent(long orgRoleId, int cityId, int resPointId, KRole oppRole) {
		if (orgRoleId < 1) {
			return;
		}
		// 判断角色是否在城市中，在，则立刻发送，不在则缓存
		SeizeEvent newEvent = null;
		if (oppRole == null) {
			newEvent = new SeizeEvent(orgRoleId, cityId, resPointId, null, 0);
		} else {
			new SeizeEvent(orgRoleId, cityId, resPointId, oppRole.getName(), oppRole.getLevel());
		}

		if (ResWarDataCenter.isRoleJoinedCity(orgRoleId)) {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(orgRoleId);
			sendDialog(role, newEvent);
			return;
		}

		eventMap.put(newEvent.orgRoleId, newEvent);
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param role
	 * @author CamusHuang
	 * @creation 2014-5-14 下午3:02:55
	 * </pre>
	 */
	static void notifyJoinCity(KRole role) {
		// 查找是否有缓存事件，有则发送
		SeizeEvent event = eventMap.remove(role.getId());
		if (event != null) {
			sendDialog(role, event);
		}
	}

	private static void sendDialog(KRole role, SeizeEvent event) {
		if(!ResWarDataCenter.isCityInWar(event.cityId)){
			return;
		}
		if (event.oppRoleName == null) {
			sendDialog_OutTime(role, event);
		} else {
			sendDialog_PVP(role, event);
		}
	}

	private static void sendDialog_OutTime(KRole role, SeizeEvent event) {
		CityTemplate cityTemp = KResWarDataManager.mCityTempManager.getData(event.cityId);
		ResPointTemplate pointTemp = KResWarDataManager.mResPointTempManager.getData(event.resPointId);
		// 需要二次确认
		List<KDialogButton> buttons = new ArrayList<KDialogButton>();
		buttons.add(KDialogButton.CANCEL_BUTTON);
		buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_FOCEJOIN_SEIZE, event.cityId + "," + event.resPointId, KDialogButton.CONFIRM_DISPLAY_TEXT));
		String tips = StringUtil.format(GangResWarTips.您在x城市占领的x资源点由于超时失去控制权是否前去抢夺, cityTemp.cityname, pointTemp.name);
		KDialogService.sendFunDialog(role, "", tips, buttons, true, (byte) -1);
	}

	private static void sendDialog_PVP(KRole role, SeizeEvent event) {
		CityTemplate cityTemp = KResWarDataManager.mCityTempManager.getData(event.cityId);
		ResPointTemplate pointTemp = KResWarDataManager.mResPointTempManager.getData(event.resPointId);
		// 需要二次确认
		List<KDialogButton> buttons = new ArrayList<KDialogButton>();
		buttons.add(KDialogButton.CANCEL_BUTTON);
		buttons.add(new KDialogButton(KDefaultDialogProcesser.DIALOG_FUN_ID_FOCEJOIN_SEIZE, event.cityId + "," + event.resPointId, KDialogButton.CONFIRM_DISPLAY_TEXT));
		String tips = StringUtil.format(GangResWarTips.您在x城市占领的x资源点被敌方军团的x玩家lvx所占领是否前去抢夺, cityTemp.cityname, pointTemp.name, event.oppRoleName, event.oppRoleLv);
		KDialogService.sendFunDialog(role, "", tips, buttons, true, (byte) -1);
	}

	public static void confirmByDialog(KGamePlayerSession session, String script) {
		String[] scrs = script.split(",");
		int cityId = Integer.parseInt(scrs[0]);
		int resPointId = Integer.parseInt(scrs[1]);
		// -------------
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.服务器繁忙请稍候再试);
			return;
		}
		// -------------
		ResWarCity city = ResWarDataCenter.allCityMap.get(cityId);
		if (city == null) {
			KDialogService.sendUprisingDialog(session, GlobalTips.服务器繁忙请稍候再试);
			return;
		}
		if(city.isWarEnd()){
			KDialogService.sendUprisingDialog(session, GangResWarTips.此城市争夺胜负已分);
			return;
		}
		KGrwForceJoinMsg.sendMsg(session, role, city, resPointId);
	}

	static class SeizeEvent {
		long orgRoleId;
		byte cityId;
		byte resPointId;
		String oppRoleName;
		byte oppRoleLv;

		private SeizeEvent(long orgRoleId, int cityId, int resPointId, String oppRoleName, int oppRoleLv) {
			this.orgRoleId = orgRoleId;
			this.cityId = (byte) cityId;
			this.resPointId = (byte) resPointId;
			this.oppRoleName = oppRoleName;
			this.oppRoleLv = (byte) oppRoleLv;
		}
	}
}
