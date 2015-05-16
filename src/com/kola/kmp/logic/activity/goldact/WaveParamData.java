package com.kola.kmp.logic.activity.goldact;

import java.util.ArrayList;
import java.util.List;

import com.kola.kgame.cache.util.UtilTool;

public class WaveParamData {
	
	//本波Id
	public int waveId;
	//本波油桶数量
	public int waveBarrelCount;
	//本波时间
	public int waveTimeSecond;
	//最小出桶时间间隔（毫秒）
	public long minPerTimeMillis; 
	//最小出桶时间间隔（毫秒）
	public long maxPerTimeMillis;
	//每个区间最小出桶数量
	public int minPerBarrelCount;
	//每个区间最大出桶数量
	public int maxPerBarrelCount;
	
	public List<Long> perTimeMillisList = new ArrayList<Long>();
	
	public void initPerTimeMillisList(){
		int minPer = (int)(minPerTimeMillis / 500);
		int maxPer = (int)(maxPerTimeMillis / 500);
		for (int i = minPer; i <= maxPer; i++) {
			perTimeMillisList.add((long)(i*500));
		}
	}
	
	public long getRamdomPerTime(){
		int index = UtilTool.random(perTimeMillisList.size());
		return perTimeMillisList.get(index);
	}
}
