package com.kola.kmp.logic.competition.teampvp;

import java.util.List;

import com.kola.kmp.logic.util.IRoleEquipShowData;

/**
 * 
 * @author PERRY CHAN
 */
public interface ITeamPVPTeamMember {
	
	/**
	 * 
	 * @return
	 */
	public long getId();

	/**
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * 
	 * @return
	 */
	public byte getJob();

	/**
	 * 
	 * @return
	 */
	public int getLevel();

	/**
	 * 
	 * @return
	 */
	public int getVipLevel();

	/**
	 * 
	 * @return
	 */
	public int getHeadResId();

	/**
	 * 
	 * @return
	 */
	public int getInMapResId();

	/**
	 * 
	 * @return
	 */
	public List<IRoleEquipShowData> getEquipmentRes();

	/**
	 * 
	 * @return
	 */
	public String getFashionResId();

	/**
	 * 
	 * @return
	 */
	public int[] getEquipSetRes();
}
