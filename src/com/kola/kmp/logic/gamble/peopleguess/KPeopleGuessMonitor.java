package com.kola.kmp.logic.gamble.peopleguess;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.management.timer.Timer;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.util.HourClearTask;


/**
 * 全民竞猜 活动开关控制器  
 * @author Alex
 * @create 2015年2月28日 下午3:53:45
 */
public class KPeopleGuessMonitor implements KGameTimerTask{
	
	public static final Logger LOG = KGameLogger.getLogger(KPeopleGuessMonitor.class);
	
	private final int totalSecondOfDay = (int)TimeUnit.SECONDS.convert(24, TimeUnit.HOURS);
	
	private static KPeopleGuessMonitor monitor = null;

	/**比赛开启时间点*/
	public int[] raceStartTime;
//	public Integer[] raceStartTime;
	
	private int nowTimeIndex = -1;
	
	
	private String activityCfgPath;
	
	/**活动进程管理器*/
	public KPeopleGuessManager maneger;
	
	
	
	
	private  KPeopleGuessMonitor() {
		this.maneger = new KPeopleGuessManager();
	}


	public static KPeopleGuessMonitor getMonitor(){
		if(monitor == null){
			monitor = new KPeopleGuessMonitor();
		}
		return monitor;
	}
	
	
	
	
	public void init(String activityConfigPath) throws KGameServerException {
		LOG.info("初始化 全民竞猜 活动。。。。");
		Document openXml = XmlUtil.openXml(activityConfigPath);
		Element rootElement = openXml.getRootElement();
		activityCfgPath = activityConfigPath;
		
		
//		String[] starts = rootElement.getChildText("racePerion").split(";");
//		
//		
//		setTimePerion(getTimeArray(starts));
		
		String[] starts = rootElement.getChildText("startTime").split(";");
		
		setStartTimes(getTimeArray(starts));
		
		
//		int delayTime = Integer.parseInt(rootElement.getChildText("delayTime"));
//		caculaStartTime(starts, delayTime);
	}
	
//	private void caculaStartTime(String[] perion, int delay){
//		int[] timeArray = getTimeArray(perion);
//		List<Integer> timePoint = new LinkedList<Integer>();
//		int point = timeArray[0];
//		while (point < timeArray[1]) {
//			timePoint.add(point);
//			point += delay;
//		}
//		timePoint.add(timeArray[1]);
//		raceStartTime = timePoint.toArray(new Integer[timePoint.size()]);
//		
//	}
	
	
	private int[] getTimeArray(String[] starts){
		String[] singleTimeStr ;
		int[] startTimes = new int[starts.length];
		int  temp;
		for (int i = 0; i < startTimes.length; i++) {
			singleTimeStr = starts[i].split(":");
			temp = 0;
			for (int j = 0; j < singleTimeStr.length; j++) {
				int value = Integer.parseInt(singleTimeStr[j]);
				TimeUnit unit = null;
				switch (j) {
				case 0:
					unit = TimeUnit.HOURS;
					break;
				case 1:
					unit = TimeUnit.MINUTES;
					break;
				case 2:
					temp += value;
					continue;

				default:
					break;
				}
				temp += TimeUnit.SECONDS.convert(value, unit);
			}
			startTimes[i] = temp;
		}
		return startTimes;
	}

	

	@Override
	public String getName() {
		return "KPeopleGuessMonitor";
	}

	@Override
	public Object onTimeSignal(KGameTimeSignal timeSignal)
			throws KGameServerException {
		this.startUp();
		if(maneger.isOpen){
			maneger.startYabaoLast5Minute();
		}
		
		
		return null;
	}

	@Override
	public void done(KGameTimeSignal timeSignal) {
		
	}

	@Override
	public void rejected(RejectedExecutionException e) {
		
	}

	/**
	 * 游戏世界初始化完成
	 */
	public void onGameWorldInitComplete(){
		
		//启动活动的大时效
		startUp();
	}


	public void setStartTimes(int[] startTimes) {
		raceStartTime = startTimes;
	}
	
	/**
	 * 启动活动时效
	 */
	private void startUp(){
		int delay = getNextDelayTime();
		KGame.newTimeSignal(this, delay, TimeUnit.SECONDS);
		
		System.out.println("开始启动 全民竞猜 活动时效，距离下次启动还有" + delay +"秒'");
		
//		LOG.info("开始启动 全民竞猜 活动时效，距离下次启动还有" + delay +"秒'");
	}
	
	/**
	 * 获取下一次启动赛马时间点
	 * @return
	 */
	public int getNextDelayTime(){
		int startTime = 0;
		
		int nowSecond = UtilTool.getDayTimeInSecond();
		
		
		if(nowTimeIndex >=0){
			boolean nextDay = false;
			if((nowTimeIndex = nowTimeIndex + 1) == raceStartTime.length){
				nowTimeIndex = 0;
				nextDay = true;
			}
			startTime = raceStartTime[nowTimeIndex];
			if(nextDay){
				startTime += totalSecondOfDay;
			}
		}else{
			for (int i = 0; i < raceStartTime.length; i++) {
				startTime = raceStartTime[i];
				if(nowSecond < startTime || nowSecond < (startTime + 10)){
					nowTimeIndex = i;
					break;
				}else{
					startTime = 0;
				}
			}
			if(startTime ==0 ){
				nowTimeIndex = 0;
				startTime = raceStartTime[0] + totalSecondOfDay;
			}
			else if(startTime - nowSecond < 10){
				startTime += 10;
			}
		}
		
		return startTime - nowSecond;
	}

	public int getNowDelayTime(){
		int startTime = 0;
		int nowSecond = UtilTool.getDayTimeInSecond();
		for (int i = 0; i < raceStartTime.length; i++) {
			startTime = raceStartTime[i] + maneger.prepare_live_time;
//			startTime = raceStartTime[i] - maneger.prepare_live_time;
			if (nowSecond < startTime || nowSecond < (startTime + 10)) {
				break;
			} else {
				startTime = 0;
			}
		}
		if (startTime == 0) {
			startTime = raceStartTime[0] + totalSecondOfDay;
		} else if (startTime - nowSecond < 10) {
			startTime += 10;
		}
		
		return startTime - nowSecond + 5; //加5秒延时
	}
	

	/**
	 * 初始化活动进程控制器
	 * @param activityCfgPath
	 */
	public void initManager() throws KGameServerException {
		
		try {
			maneger.init(activityCfgPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
}