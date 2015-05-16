package com.kola.kmp.logic.competition.teampvp;

import java.util.List;
import java.util.Map;

import com.kola.kmp.logic.competition.teampvp.KTeamPVPTeam.KTeamPVPTeamMember;
import com.kola.kmp.logic.item.message.KPushItemsMsg;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.TeamPVPSupport;

/**
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPSupportImpl implements TeamPVPSupport {
	
	private void syncFriendStatus(long hostRoleId, long friendId, boolean add) {
		KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(hostRoleId);
		if (team != null) {
			KTeamPVPTeamMember member = team.getMember(hostRoleId);
			if (member.hasOpen()) {
				KTeamPVPMsgCenter.syncFriendId(hostRoleId, friendId, add);
			}
		}
	}

	@Override
	public int getTeamPVPGoodCount(long teamId) {
		KTeamPVPTeam team = KTeamPVPManager.getTeamByTeamId(teamId);
		if (team == null) {
			return 0;
		}
		
		return team.getGoodCount();
	}
	
	@Override
	public void increaseTeamPVPGoodCount(long teamId, int count) {
		KTeamPVPTeam team = KTeamPVPManager.getTeamByTeamId(teamId);
		if (team != null) {
			team.increaseGoodCount(count);
		}
	}
	
	@Override
	public void notifyFriendAdded(long hostRoleId, long friendId) {
		this.syncFriendStatus(hostRoleId, friendId, true);
	}
	
	@Override
	public void notifyFriendRemoved(long hostRoleId, long friendId) {
		this.syncFriendStatus(hostRoleId, friendId, false);
	}
	
	@Override
	public String getRobotFashion(long robotId) {
		KTeamPVPRobotTeam robotTeam = KTeamPVPManager.getRobotTeamByRobotId(robotId);
		if (robotTeam != null) {
			List<ITeamPVPTeamMember> list = robotTeam.getTeamMembers();
			ITeamPVPTeamMember temp;
			for (int i = 0; i < list.size(); i++) {
				temp = list.get(i);
				if (temp.getId() == robotId) {
					return temp.getFashionResId();
				}
			}
		}
		return "";
	}
	
	@Override
	public void notifyRoleEquipmentResUpdate(long roleId) {
		KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(roleId);
		if (team != null) {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			KTeamPVPTeamMember member = team.getMember(roleId);
			if (member.updateEquipmentRes(role.getEquipmentRes())) {
				KTeamPVPMsgCenter.notifyMemberResChange(team, member, KTeamPVPTeamMember.RES_TYPE_EQUIPMENT);
			}
		}
	}
	
	@Override
	public void notifyRoleEquipmentSetResUpdate(long roleId) {
		KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(roleId);
		if (team != null) {
			KTeamPVPTeamMember member = team.getMember(roleId);
			if (member.updateEquipSetRes(KSupportFactory.getItemModuleSupport().getEquiSetMapResIds(roleId))) {
				KTeamPVPMsgCenter.notifyMemberResChange(team, member, KTeamPVPTeamMember.RES_TYPE_EQUIPMENT_SET);
			}
		}
	}
	
	@Override
	public void notifyRoleFashionUpdate(long roleId) {
		KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(roleId);
		if (team != null) {
			KTeamPVPTeamMember member = team.getMember(roleId);
			if (member.updateFashionRes(KSupportFactory.getFashionModuleSupport().getFashingResId(roleId))) {
				KTeamPVPMsgCenter.notifyMemberResChange(team, member, KTeamPVPTeamMember.RES_TYPE_FASHION);
			}
		}
	}
	
	@Override
	public ITeamPVPAttrSetInfo[] getTeamPVPAttrSetInfo(long roleId) {
		KTeamPVPTeam team = KTeamPVPManager.getTeamByRoleId(roleId);
		ITeamPVPAttrSetInfo[] array = new ITeamPVPAttrSetInfo[2];
		if (team != null) {
			KTeamPVPAttrSetInfoImpl now = new KTeamPVPAttrSetInfoImpl(true, team.getDanStageInfo());
			array[0] = now;
			KTeamPVPDanStageInfo nextDanStageInfo = KTeamPVPManager.getNextDanRank(team.getDanStageInfo().danStageId);
			if (nextDanStageInfo != null) {
				array[1] = new KTeamPVPAttrSetInfoImpl(false, nextDanStageInfo);
			}
		} else {
			KTeamPVPDanStageInfo danStageInfo = KTeamPVPManager.getDanStageInfo(KTeamPVPConfig.getFirstDanRankId());
			array[0] = new KTeamPVPAttrSetInfoImpl(false, danStageInfo);
			array[1] = new KTeamPVPAttrSetInfoImpl(false, KTeamPVPManager.getNextDanRank(danStageInfo.danStageId));
		}
		return array;
	}
	
	@Override
	public void notifyRoleTeamDataChange(KRole role) {
		KPushItemsMsg.pushEquiSetData(role);
		KSupportFactory.getRoleModuleSupport().notifyEffectAttrChange(role.getId(), KTeamPVPAttributeProvider.getType());
	}
	
	private static class KTeamPVPAttrSetInfoImpl implements ITeamPVPAttrSetInfo {

		private String _name;
		private int _level;
		private Map<KGameAttrType, Integer> _attrMap;
		private boolean _activate;
		
		public KTeamPVPAttrSetInfoImpl(boolean pActivate, KTeamPVPDanStageInfo danStageData) {
			this._activate = pActivate;
			this._attrMap = danStageData.attribute;
			this._name = danStageData.danStageName;
			this._level = danStageData.danStageId;
		}

		@Override
		public String getSetName() {
			return _name;
		}
		
		@Override
		public int getLevel() {
			return _level;
		}

		@Override
		public boolean isActivate() {
			return _activate;
		}

		@Override
		public Map<KGameAttrType, Integer> getAttrMap() {
			return _attrMap;
		}

	}
}
