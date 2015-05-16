package com.kola.kmp.logic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.dataaccess.dbobj.flowdata.DBFlowGlobalData;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.timer.KGameTimeSignal;
import com.kola.kgame.cache.CacheDataSyncListenerImpl;
import com.kola.kgame.db.CacheDataSyncListener;
import com.kola.kgame.db.dataset.DBDataSet;
import com.kola.kgame.db.dataset.DBGangEntireData;
import com.kola.kgame.db.dataset.DBRoleEntireData;
import com.kola.kgame.db.syncdata.SyncDataTask;

public class ManualSyncTask implements RunTimeTask {

	public static final int CACHE_TYPE_ROLE = 1;

	public static final int CACHE_TYPE_GLOBAL = 2;
	
	public static final int CACHE_TYPE_ZONE = 3;
	
	public static final int CACHE_TYPE_FLOW = 4;

	private int type;

	private boolean isShutdown;

	private SyncCounter counter;
	
	private CacheDataSyncListener syncListener;
	
	private Logger logger = KGameLogger.getLogger(ManualSyncTask.class);
	
	private Field fStartIndex;
	private Field fEndIndex;
	
	public ManualSyncTask() {
		
	}
	
	private int[][] getPageCount(int count) {
		int[][] pageCount;
		int page;
		page = count / 1000 + (count % 1000 == 0 ? 0 : 1);

		pageCount = new int[page][2];
		for (int i = 0, j = 0; i < page; i++, j += 1000) {
			if (i == page - 1) {
				pageCount[i] = new int[] { j, count };
			} else {
				pageCount[i] = new int[] { j, j + 1000 };
			}
		}
		return pageCount;
	}
	
	@Override
	public String run(String args) {
		String[] array = args.split(";");
		this.type = Integer.parseInt(array[0]);
		this.isShutdown = Boolean.parseBoolean(array[1]);
		this.syncListener = new CacheDataSyncListenerImpl();
		this.counter = new SyncCounter(1);
		try {
			fStartIndex = SyncDataTask.class.getDeclaredField("startIndex");
			fEndIndex = SyncDataTask.class.getDeclaredField("endIndex");
			fStartIndex.setAccessible(true);
			fEndIndex.setAccessible(true);
		} catch (Exception e) {
			return e.getMessage();
		}
		try {
			this.doJob();
		} catch (Exception e) {
			return e.getMessage();
		}
		return "执行成功";
	}

	private ManualSyncTask doJob()throws KGameServerException {

		ArrayList<KGameTimeSignal> syncTimeSignalList = new ArrayList<KGameTimeSignal>();

		if (type == CACHE_TYPE_ROLE) {

			List<DBRoleEntireData> syncRoleDatas = syncListener.scanCacheDirtyData(isShutdown);
			
			counter.scanRoleCacheUsedTime = System.currentTimeMillis() - counter.syncUseTime;

			int roelDataCount = syncRoleDatas.size();

			int[][] roleSyncTaskPage = getPageCount(roelDataCount);

			for (int i = 0; i < roleSyncTaskPage.length; i++) {
				logger.info("page:{}，内容:{}", i, Arrays.toString(roleSyncTaskPage[i]));
				KGameTimeSignal syncTimeSignal = KGame.newTimeSignal(new SyncDataTask("SyncDataTask_" + i, syncRoleDatas, SyncDataTask.SYNC_DATA_TASK_TYPE_ROLE, roleSyncTaskPage[i][0],
						roleSyncTaskPage[i][1]), 0, TimeUnit.SECONDS);
				syncTimeSignalList.add(syncTimeSignal);
			}
			

			for (DBRoleEntireData dbPlayerRoleGlobal : syncRoleDatas) {
				if (dbPlayerRoleGlobal.getDBRoleData() != null) {
					counter.playerRoleDataCount.incrementAndGet();
				}
				counter.currencyDataCount.addAndGet(getDirtyDataSize(dbPlayerRoleGlobal.getCurrencyAccountDataSet()));
				counter.itemDataCount.addAndGet(getDirtyDataSize(dbPlayerRoleGlobal.getItemDataSet()));
				counter.mailDataCount.addAndGet(getDirtyDataSize(dbPlayerRoleGlobal.getMailDataSet()));
				counter.missionDataCount.addAndGet(getDirtyDataSize(dbPlayerRoleGlobal.getMissionDataSet()));
				counter.missionCompleteRecordDataCount.addAndGet(getDirtyDataSize(dbPlayerRoleGlobal.getMissionCompleteRecordSet()));
				counter.petDataCount.addAndGet(getDirtyDataSize(dbPlayerRoleGlobal.getPetDataSet()));
				counter.mountDataCount.addAndGet(getDirtyDataSize(dbPlayerRoleGlobal.getMountDataSet()));
				counter.playerRoleExtAttrDataCount.addAndGet(getDirtyDataSize(dbPlayerRoleGlobal.getRoleExtDataSet()));
				counter.playerRoleRelationShipDataCount.addAndGet(getDirtyDataSize(dbPlayerRoleGlobal.getRelationShipDataSet()));
				counter.skillDataCount.addAndGet(getDirtyDataSize(dbPlayerRoleGlobal.getDBSkillDataSet()));
				counter.dataSetAttributeCount.addAndGet(dbPlayerRoleGlobal.getSetAttributes().size());
			}
		}else if(type == CACHE_TYPE_GLOBAL){
			List<DBGangEntireData> syncGangEntireDatas = syncListener
					.scanGlobalDataCacheDirtyData(isShutdown);
			int gangDataCount = syncGangEntireDatas.size();
			int[][] gangSyncTaskPage = getPageCount(gangDataCount);
			for (int i = 0; i < gangSyncTaskPage.length; i++) {
				KGameTimeSignal syncTimeSignal = KGame.newTimeSignal(
						new SyncDataTask("SyncDataTask_" + i, syncGangEntireDatas, SyncDataTask.SYNC_DATA_TASK_TYPE_GANG, gangSyncTaskPage[i][0], gangSyncTaskPage[i][1]), 0, TimeUnit.SECONDS);
				syncTimeSignalList.add(syncTimeSignal);
			}			
			
			for (DBGangEntireData dbGangGlobal : syncGangEntireDatas) {
				if (dbGangGlobal.getDBGangData() != null) {
					counter.gangDataCount.incrementAndGet();
				}
				counter.gangMemberDataCount.addAndGet(dbGangGlobal
						.getDBGangMembers().size());
				counter.gangExtAttrDataCount.addAndGet(dbGangGlobal
						.getDBGangExtCADatas().size());					
			}
						
		}
		else if(type == CACHE_TYPE_FLOW){
			DBFlowGlobalData syncFlowGlobalDatas = syncListener.scanFlowGlobalDataDirtyData(isShutdown);
			KGameTimeSignal syncTimeSignal = KGame.newTimeSignal(new SyncDataTask("SyncDataTask_Flow", syncFlowGlobalDatas, SyncDataTask.SYNC_DATA_TASK_TYPE_FLOW, 0, 0), 0, TimeUnit.SECONDS);
			syncTimeSignalList.add(syncTimeSignal);
			
			counter.insertRoleLoginRecordCount = syncFlowGlobalDatas.getLoginRecordList().size();
			counter.updateRoleLoginRecordCount = syncFlowGlobalDatas.getLogoutRecordList().size();
			counter.funConsumeRecordCount = syncFlowGlobalDatas.getFunPointConsumeRecordList().size();
			counter.shopSellItemRecordCount = syncFlowGlobalDatas.getShopSellItemRecordList().size();
		}
		
		for (KGameTimeSignal syncTimeSignal : syncTimeSignalList) {
			try {
				SyncDataTask task = (SyncDataTask) syncTimeSignal.get();
				logger.info(String.format("[%s]完成同步线程任务,范围[%d,%d]，时间：%dms***********！！！", task.getName(), fStartIndex.get(task), fEndIndex.get(task), task.syncTime));
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		counter.syncUseTime = System.currentTimeMillis() - counter.syncUseTime;
		counter.plintResult();

		return this;
	}
	
	public int getDirtyDataSize(DBDataSet set){
		int size = 0;
		if(set.getInsertData()!=null){
			size +=set.getInsertData().size(); 
		}
		if(set.getUpdateData()!=null){
			size +=set.getUpdateData().size(); 
		}
		if(set.getDeleteData()!=null){
			size +=set.getDeleteData().size(); 
		}
		return size;
	}
	
	public class SyncCounter {
		public int syncTimes;
		public long scanRoleCacheUsedTime;
		public long syncUseTime;
		public AtomicInteger playerRoleDataCount = new AtomicInteger(0);
		public AtomicInteger playerRoleRelationShipDataCount = new AtomicInteger(0);
		public AtomicInteger playerRoleExtAttrDataCount = new AtomicInteger(0);
		public AtomicInteger petDataCount = new AtomicInteger(0);
		public AtomicInteger mountDataCount = new AtomicInteger(0);
		public AtomicInteger itemDataCount = new AtomicInteger(0);
		public AtomicInteger currencyDataCount = new AtomicInteger(0);
		public AtomicInteger missionDataCount = new AtomicInteger(0);
		public AtomicInteger missionCompleteRecordDataCount = new AtomicInteger(0);
		public AtomicInteger mailDataCount = new AtomicInteger(0);
		public AtomicInteger skillDataCount = new AtomicInteger(0);
		public AtomicInteger dataSetAttributeCount = new AtomicInteger(0);
		
		public AtomicInteger flowCount = new AtomicInteger(0);
		public AtomicInteger gangDataCount = new AtomicInteger(0);
		public AtomicInteger gangMemberDataCount = new AtomicInteger(0);
		public AtomicInteger gangExtAttrDataCount = new AtomicInteger(0);
		public AtomicInteger goodsDataCount = new AtomicInteger(0);
		public AtomicInteger zoneDataCount = new AtomicInteger(0);
		public AtomicInteger zoneCommentDataCount = new AtomicInteger(0);
		public AtomicInteger moodDataCount = new AtomicInteger(0);
		public AtomicInteger moodAnswerDataCount = new AtomicInteger(0);
		public AtomicInteger boxMsgDataCount = new AtomicInteger(0);
		public AtomicInteger roleMoodMappingDataCount = new AtomicInteger(0);
		
		public int insertLoginRecordCount;
		public int updateLoginRecordCount;
		public int insertChargeRecordCount;
		public int insertPresentPointRecordCount;
		
		public int insertRoleLoginRecordCount;
		public int updateRoleLoginRecordCount;
		public int funConsumeRecordCount;
		public int shopSellItemRecordCount;
		
		public String counterInfo;

		public SyncCounter(int syncTimes) {
			syncUseTime = System.currentTimeMillis();
			this.syncTimes = syncTimes;
		}

		public void plintResult() {
			StringBuilder builder = new StringBuilder();

			builder.append("\r\n*********完成第" + syncTimes + "次数据库同步，时间："
					+ new Date().toString() + "***********");
			builder.append("\r\n----------扫描角色缓存耗时："+scanRoleCacheUsedTime+" ms--------");
			builder.append("\r\n----------同步角色相关数据--------");
			builder.append("\r\n角色数据记录数：" + playerRoleDataCount.get());
			builder.append("\r\n角色关系数据记录数：" + playerRoleRelationShipDataCount.get());
			builder.append("\r\n角色扩展属性数据记录数：" + playerRoleExtAttrDataCount.get());
			builder.append("\r\n道具数据记录数：" + itemDataCount.get());
			builder.append("\r\n宠物数据记录数：" + petDataCount.get());
			builder.append("\r\n坐骑数据记录数：" + mountDataCount.get());
			builder.append("\r\n任务数据记录数：" + missionDataCount.get());
			builder.append("\r\n完成任务数据记录数：" + missionCompleteRecordDataCount.get());
			builder.append("\r\n货币数据记录数：" + currencyDataCount.get());
			builder.append("\r\n邮件数据记录数：" + mailDataCount.get());
			builder.append("\r\n技能数据记录数：" + skillDataCount.get());
			builder.append("\r\n数据集属性数据记录数：" + dataSetAttributeCount.get());
			builder.append("\r\n----------同步公共数据--------");			
			builder.append("\r\n同步帮会数据记录数：" + gangDataCount.get());
			builder.append("\r\n同步帮会成员数据记录数：" + gangMemberDataCount.get());
			builder.append("\r\n同步帮会扩展属性数据记录数：" + gangExtAttrDataCount.get());
			builder.append("\r\n同步寄卖数据记录数：" + goodsDataCount.get());
			builder.append("\r\n----------同步空间数据--------");			
			builder.append("\r\n同步空间数据记录数：" + zoneDataCount.get());
			builder.append("\r\n同步空间评价数据记录数：" + zoneCommentDataCount.get());
			builder.append("\r\n同步空间心情数据记录数：" + moodDataCount.get());
			builder.append("\r\n同步心情回复数据记录数：" + moodAnswerDataCount.get());
			builder.append("\r\n同步消息盒子数据记录数：" + boxMsgDataCount.get());
			builder.append("\r\n同步角色与心情数Mapping据记录数：" + roleMoodMappingDataCount.get());
			builder.append("\r\n----------同步平台流水数据--------");
			builder.append("\r\n同步插入平台登录流水据记录数：" + insertLoginRecordCount);
			builder.append("\r\n同步更新平台登录流水记录数：" + updateLoginRecordCount);
			builder.append("\r\n同步充值记录据记录数：" + insertChargeRecordCount);
			builder.append("\r\n同步赠送点数据记录数：" + insertPresentPointRecordCount);
			builder.append("\r\n----------同步游戏流水数据--------");
			builder.append("\r\n同步角色登录流水据记录数：" + insertRoleLoginRecordCount);
			builder.append("\r\n同步更新角色登出流水据记录数：" + updateRoleLoginRecordCount);
			builder.append("\r\n同步功能消耗点数流水据记录数：" + funConsumeRecordCount);
			builder.append("\r\n同步商城购买道具流水据记录数：" + shopSellItemRecordCount);
			builder.append("\r\n同步流水记录数：" + flowCount);
			builder.append(counterInfo);
			builder.append("\r\n%%%%%%总耗时：" + syncUseTime + " ms %%%%%%%%%\r\n");
			builder.append("*********************************************\r\n");

			logger.warn(builder.toString());
		}
	}

}
