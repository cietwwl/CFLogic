package com.kola.kmp.logic.competition.teampvp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPRobotTeam implements ITeamPVPTeam {

	private static final AtomicLong _idGenerator = new AtomicLong();
	private long _teamId;
	private String _teamName;
	private List<ITeamPVPTeamMember> _members;
	private long[] _memberIds;
	
	KTeamPVPRobotTeam(KTeamPVPRobot pCaptain, KTeamPVPRobot pTeamMate) {
		_teamId = _idGenerator.incrementAndGet();
		_members = new ArrayList<ITeamPVPTeamMember>(KTeamPVPConfig.getMaxTeamMemberCount());
		_members.add(pCaptain);
		_members.add(pTeamMate);
		_memberIds = new long[]{pCaptain.getId(), pTeamMate.getId()};
//		String hex = Long.toHexString(_teamId);
//		if (hex.length() < 4) {
//			StringBuilder strBld = new StringBuilder(CompetitionTips.getTipsRobotTeamNamePrefix()).append("0000");
//			strBld.replace(strBld.length() - hex.length(), strBld.length(), hex);
//			_teamName = strBld.toString();
//		} else {
//			_teamName = CompetitionTips.getTipsRobotTeamNamePrefix() + hex;
//		}
		_teamName = KTeamPVPRandomName.randomTeamName();
	}
	
	void resetName() {
		_teamName = KTeamPVPRandomName.randomTeamName();
	}

	@Override
	public long getId() {
		return _teamId;
	}
	
	@Override
	public String getTeamName() {
		return _teamName;
	}
	
	@Override
	public boolean isRobotTeam() {
		return true;
	}
	
	@Override
	public List<ITeamPVPTeamMember> getTeamMembers() {
		return _members;
	}
	
	@Override
	public long[] getAllMemberIds() {
		return _memberIds;
	}
	
}
