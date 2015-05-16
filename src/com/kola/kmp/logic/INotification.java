package com.kola.kmp.logic;


public interface INotification {

	/**
	 * 
	 * @return
	 */
	public int getYear();
	
	/**
	 * 
	 * @return
	 */
	public int getMonth();
	
	/**
	 * 
	 * @return
	 */
	public int getDay();
	
	/**
	 * 
	 * @return
	 */
	public int getHour();
	
	/**
	 * 
	 * @return
	 */
	public int getMinute();
	
	/**
	 * 
	 * @return
	 */
	public String getContent();
}
