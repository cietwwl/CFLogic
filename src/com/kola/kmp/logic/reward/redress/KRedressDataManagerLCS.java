package com.kola.kmp.logic.reward.redress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Element;

import com.koala.game.dataaccess.KGameDBException;
import com.koala.game.exception.KGameServerException;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kgame.db.dataaccess.DataAccesserFactory;
import com.kola.kgame.db.dataobject.DBGameExtCA;
import com.kola.kmp.logic.other.KCurrencyCountStruct;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameExtDataDBTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailContent;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseMailRewardData;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;
import com.kola.kmp.logic.util.tips.RewardTips;

/**
 * <pre>
 * 合服补偿
 * 
 * @author CamusHuang
 * @creation 2015-1-31 下午8:08:00
 * </pre>
 */
public class KRedressDataManagerLCS {

	// 最近一次合服时间
	public static long LastCombimeServerTime;
	// 所有并区历史
	private static List<CSStruct> TotalCSGSList = new ArrayList<CSStruct>();
	// 最近一次并区
	private static CSStruct LastCSGS;
	
	// <GSID, 奖励>
	private static Map<Integer,BaseMailRewardData> RewardMap = new HashMap<Integer,BaseMailRewardData>();

	/**
	 * <pre>
	 * 服务器启动完成
	 * 
	 * @author CamusHuang
	 * @creation 2012-12-3 下午8:41:04
	 * </pre>
	 */
	static void notifyCacheLoadComplete() throws KGameServerException {
		/**
		 * 警告：使用本类实例前，请务必调用load方法
		 * 本方法只需要在服务器启动完成时调用一次
		 */
		try {
			List<DBGameExtCA> nowDBList = DataAccesserFactory.getGameExtCADataAccesser().getDBGameExtCA(KGameExtDataDBTypeEnum.合服信息数据.dbType);
			if (!nowDBList.isEmpty()) {
				decode(nowDBList.get(0).getAttribute());
			}
		} catch (Exception e) {
			throw new KGameServerException(e.getMessage(), e);
		} catch (KGameDBException e) {
			throw new KGameServerException(e.getMessage(), e);
		}
	}
	
//	public static void main(String[] s) throws Exception{
//		decode("7,6,3#20150210114003;4,1,2,#20150210113902;");
//	}
	
	/**
	 * <pre>
	 * 2,4#20150210113902;6,7#20150210114003
	 * 
	 * @param datas
	 * @author CamusHuang
	 * @throws Exception 
	 * @creation 2015-2-10 下午7:02:35
	 * </pre>
	 */
	private static void decode(String datas) throws Exception{
		LastCombimeServerTime = 0;
		LastCSGS = null;
		TotalCSGSList.clear();
		RewardMap.clear();
		
		String[] dataAs = datas.split(";");
		for(String dataA:dataAs){
			if(dataA.isEmpty()){
				continue;
			}
			CSStruct struct = new CSStruct();
			TotalCSGSList.add(struct);
			//
			String[] dataBs = dataA.split("#");
			//
			struct.csTime = UtilTool.DATE_FORMAT12.parse(dataBs[1]).getTime();
			//
			String[] dataCs = dataBs[0].split(",");
			for(String dataC:dataCs){
				if(dataC.isEmpty()){
					continue;
				}
				struct.gsIdSet.add(Integer.parseInt(dataC));
			}
			struct.gsIdList.addAll(struct.gsIdSet);
			Collections.sort(struct.gsIdList);
		}
		if(TotalCSGSList.isEmpty()){
			return;
		}
		//
		Collections.sort(TotalCSGSList);
		//
		LastCSGS = TotalCSGSList.get(TotalCSGSList.size()-1);
		LastCombimeServerTime = LastCSGS.csTime;
		//
		int GSCount=LastCSGS.gsIdSet.size();
		if(TotalCSGSList.size()>1){
			GSCount++;
		}
		if (GSCount < 2) {
			GSCount = 2;
		}
		//
		int rank = 1;
		if(TotalCSGSList.size()>1){
			BaseMailRewardData baseData = buildReward(rank, GSCount);
			for(CSStruct data:TotalCSGSList){
				if(data==LastCSGS){
					continue;
				}
				for(int gsId:data.gsIdList){
					RewardMap.put(gsId, baseData);
				}
			}
			rank++;
		}
		
		for(int gsId:LastCSGS.gsIdList){
			BaseMailRewardData baseData = buildReward(rank, GSCount);
			RewardMap.put(gsId, baseData);
			rank++;
		}
	}
	
	/**
	 * <pre>
	 * 	 获得的奖励
	 *   所在服开服时间排名M
	 *   所在的将要合成同一个服的服务器数量N
	 * 
	 *   钻石=50+200/(N-1)*(M-1)
	 *   金币=500000+2000000/(N-1)*(M-1)
	 * 
	 * @param rank
	 * @return
	 * @author CamusHuang
	 * @creation 2015-2-15 下午7:22:54
	 * </pre>
	 */
	private static BaseMailRewardData buildReward(int rank, int GSCount){
		int diamond = 50+200/(GSCount-1)*(rank-1);
		int gold = 500000+2000000/(GSCount-1)*(rank-1);
		//
		List<KCurrencyCountStruct> moneys = new ArrayList<KCurrencyCountStruct>();
		if(diamond>0){
			moneys.add(new KCurrencyCountStruct(KCurrencyTypeEnum.DIAMOND, diamond));
		}
		if(gold>0){
			moneys.add(new KCurrencyCountStruct(KCurrencyTypeEnum.GOLD, gold));
		}
		
		if(moneys.isEmpty()){
			return null;
		}
		
		return new BaseMailRewardData(1, new BaseMailContent(RewardTips.合服奖励邮件标题, RewardTips.合服奖励邮件内容, null, null), new BaseRewardData(null, moneys, null, null, null));
	}
	
	/**
	 * <pre>
	 * 
	 * @author CamusHuang
	 * @creation 2015-2-15 下午7:22:40
	 * </pre>
	 */
	static class CSStruct implements Comparable<CSStruct>{
		Set<Integer> gsIdSet = new HashSet<Integer>();
		List<Integer> gsIdList = new ArrayList<Integer>();
		long csTime;
		
		@Override
		public int compareTo(CSStruct o) {
			if (csTime < o.csTime) {
				return -1;
			}
			if (csTime > o.csTime) {
				return 1;
			}
			return 0;
		}
	}

	/**
	 * <pre>
	 * 先将将要合服的所有服按照开服时间（已经合过服的但因为影响到精彩活动，要用新变量记录合服时间，作为排名，开服时间用所有服中的最老开服时间），进行排名，开服时间越早名次越高。
	 * 例如 
	 *      1服开服时间1月，
	 *      2服开服时间2月，
	 *      3服开服时间3月
	 * 名次分别是
	 *    1服名次：1
	 *    2服名次：2
	 *    3服名次：3
	 * 
	 * 获得的奖励
	 *   所在服开服时间排名M
	 *   所在的将要合成同一个服的服务器数量N
	 * 
	 *   钻石=50+200/(N-1)*(M-1)
	 *   金币=500000+2000000/(N-1)*(M-1)
	 *   
	 * 
	 * @param id
	 * @return
	 * @author CamusHuang
	 * @creation 2015-2-9 下午8:30:09
	 * </pre>
	 */
	public static BaseMailRewardData countRedress(int gsId) {
		return RewardMap.get(gsId);
	}
}
