package com.kola.kmp.logic.other;

/**
 * 
 * @author PERRY CHAN
 */
public class KTableInfo {
	
	public final byte tableType;
	public final String tableName;
	public final int headerIndex;
	
	
	public KTableInfo(byte pTableType, String pTableName, int pHeaderIndex) {
		this.tableType = pTableType;
		this.tableName = pTableName;
		this.headerIndex = pHeaderIndex;
	}

}
