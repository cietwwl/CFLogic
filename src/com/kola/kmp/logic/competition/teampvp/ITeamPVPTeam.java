package com.kola.kmp.logic.competition.teampvp;

import java.util.List;

/**
 * 
 * @author PERRY CHAN
 */
public interface ITeamPVPTeam {

	/**
	 * 
	 * @return
	 */
	public long getId();
	
	/**
	 * 
	 * @return
	 */
	public String getTeamName();
	
	/**
	 * 
	 * @return
	 */
	public boolean isRobotTeam();
	
	/**
	 * 
	 * @return
	 */
	public List<ITeamPVPTeamMember> getTeamMembers();
	
	/**
	 * 
	 * @return
	 */
	public long[] getAllMemberIds();
}
