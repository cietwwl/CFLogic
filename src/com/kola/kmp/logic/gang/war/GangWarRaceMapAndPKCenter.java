package com.kola.kmp.logic.gang.war;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.kola.kmp.logic.combat.ICombatCommonResult;
import com.kola.kmp.logic.combat.ICombatGlobalCommonResult;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.gang.war.GangWarLogic.GangWarPVEAtt;
import com.kola.kmp.logic.gang.war.GangWarRaceMapAndPKCenter.GangRacePVECenter.RacePVEBoss;
import com.kola.kmp.logic.gang.war.GangWarRound.GangRace;
import com.kola.kmp.logic.gang.war.KGangWarDataManager.KBossDataManager.BossData;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.MonsterData;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.map.duplicatemap.CollisionEventListener;
import com.kola.kmp.logic.map.duplicatemap.CollisionEventObjectData;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMap;
import com.kola.kmp.logic.map.duplicatemap.KDuplicateMapBornPoint;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.support.CombatModuleSupport.ICombatMonsterUpdateInfo;
import com.kola.kmp.logic.util.ResultStructs.CommonResult;
import com.kola.kmp.logic.util.tips.GangWarTips;

/**
 * <pre>
 * 一个{@link GangWarRaceMapAndPKCenter}实例对应一个{@link GangRace}实例
 * 本类管理一个{@link GangRace}对应的场景地图（包括PVE碰撞）、PVE BOSS及战斗（包括发起和结束通知）
 * 
 * @author PERRY CHAN
 * @creation 2014-6-5 下午6:37:07
 * </pre>
 */
public class GangWarRaceMapAndPKCenter {
	private BossData bossData;// BOSS数据
	private KDuplicateMap raceMap;// 当前对阵场景的地图

	// <军团ID,出生点对象>
	private final Map<Long, KDuplicateMapBornPoint> raceMapBornPointMap = new HashMap<Long, KDuplicateMapBornPoint>();// 场景出生点
	// <军团ID,场景BOSS碰撞对象（内附BOSS实例）>
	private final Map<Long, CollisionEventObjectData> raceMapBossObjMap = new HashMap<Long, CollisionEventObjectData>();// 场景BOSS碰撞对象

	// 场景地图碰撞通知监听器
	final GangRaceCollisionEventListener raceMapCollisionListner = new GangRaceCollisionEventListener();
	// PVE BOSS管理
	final GangRacePVECenter PVECenter = new GangRacePVECenter();

	/**
	 * <pre>
	 * 根据场景地图配置的NPC，初始化NPC数据
	 * 
	 * @author CamusHuang
	 * @creation 2013-8-31 上午10:46:48
	 * </pre>
	 */
	void initMapAndBoss(GangRace race) {
		raceMap = KSupportFactory.getDuplicateMapSupport().createDuplicateMap(KGangWarConfig.getInstance().军团战场景地图);
		raceMap.setCollisionEventListener(raceMapCollisionListner);
		// 识别出生点
		List<KDuplicateMapBornPoint> points = raceMap.getAllBornPointEntity();
		raceMapBornPointMap.put(race.gangDataA.gangId, points.get(1));
		raceMapBornPointMap.put(race.gangDataB.gangId, points.get(0));
		// 识别BOSS
		List<CollisionEventObjectData> colObjs = raceMap.getAllCollisionEventObject();
		raceMapBossObjMap.put(race.gangDataA.gangId, colObjs.get(1));
		raceMapBossObjMap.put(race.gangDataB.gangId, colObjs.get(0));
		// 绑定BOSS数据
		bossData = KGangWarDataManager.mBossDataManager.getData(race.avgRoleLv);
		RacePVEBoss bossA = PVECenter.createRacePVEBoss(race.gangDataA.gangId, bossData.bossTemp1, GangWarDataCenter.PVEBattlefield);
		RacePVEBoss bossB = PVECenter.createRacePVEBoss(race.gangDataB.gangId, bossData.bossTemp2, GangWarDataCenter.PVEBattlefield);
		raceMapBossObjMap.get(race.gangDataA.gangId).setAttachment(bossA);
		raceMapBossObjMap.get(race.gangDataB.gangId).setAttachment(bossB);
	}

	/**
	 * <pre>
	 * 当前对阵场景的地图
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-6-6 上午10:14:32
	 * </pre>
	 */
	KDuplicateMap getWarMap() {
		return raceMap;
	}
	
	BossData getBossData() {
		return bossData;
	}
	
	KDuplicateMapBornPoint getMapBornPoint(long gangId) {
		return raceMapBornPointMap.get(gangId);
	}
	
	long getBossCurrentHp(long gangId) {
		return getRacePVEBoss(gangId).getCurrentHp();
	}
	
	long getBossMaxHp(long gangId) {
		return getRacePVEBoss(gangId).maxHp;
	}
	
	RacePVEBoss getRacePVEBoss(long gangId) {
		return PVECenter._gangIdToBossMap.get(gangId);
	}
	
	int getRaceMapBossId(long gangId) {
		return raceMapBossObjMap.get(gangId).getMapInstanceId();
	}

	void clearData() {
		bossData = null;
		raceMap = null;
		raceMapBornPointMap.clear();
		raceMapBossObjMap.clear();
		PVECenter.clear();
	}

	/**
	 * <pre>
	 * PVE碰撞通知
	 * 
	 * @author CamusHuang
	 * @creation 2014-6-6 上午9:40:03
	 * </pre>
	 */
	public static class GangRaceCollisionEventListener implements CollisionEventListener {
		@Override
		public void notifyPlayerRoleCollisionEvent(KRole role, CollisionEventObjectData data) {
			CommonResult result = GangWarLogic.startPVE(role, data);
			if(result.tips!=null && !result.tips.isEmpty()){
				KDialogService.sendUprisingDialog(role, result.tips);
			}
		}
		
	    /**
		 * 通知角色与地图的另外一个角色发生碰撞事件
		 * @param role
		 * @param otherRole
		 */
		public void notifyPlayerRoleCollisionOtherRole(KRole role,KRole otherRole){
			GangWarLogic.tryToStartPVP(role, otherRole);
		}
	}

	/**
	 * <pre>
	 * 一个{@link GangRacePVECenter}实例对应一个{@link GangWarRaceMapAndPKCenter}实例
	 * {@link GangRacePVECenter}管理了{@link GangRace}实例中的所有BOSS即多个{@link RacePVEBoss}实现
	 * 
	 * @author CamusHuang
	 * @creation 2014-6-6 上午9:40:24
	 * </pre>
	 */
	static class GangRacePVECenter {

		//<gangId,RacePVEBoss>>
		private final Map<Long, RacePVEBoss> _gangIdToBossMap = new HashMap<Long, RacePVEBoss>();
		//<gangId,<战场怪物实例ID,RacePVEBoss>>
		private final Map<Long, Map<Integer, ICombatMonsterUpdateInfo>> _gangIdToBossesMap = new HashMap<Long, Map<Integer, ICombatMonsterUpdateInfo>>();
		//<gangId,战场>
		private final Map<Long, KGameBattlefield> _gangIdToBattlefieldMap = new HashMap<Long, KGameBattlefield>();

		/**
		 * <pre>
		 * 创建一个BOSS实例
		 * 
		 * @param templateId
		 * @param battlefield
		 * @return
		 * @author CamusHuang
		 * @creation 2014-6-5 下午6:38:07
		 * </pre>
		 */
		RacePVEBoss createRacePVEBoss(long gangId, KMonstTemplate bossTemp, KGameBattlefield battlefield) {
			MonsterData monsterData = battlefield.getAllWaveInfo().get(0).getAllMonsters().get(0); // 只有一个boss
			RacePVEBoss raceBoss = new RacePVEBoss(gangId, bossTemp, monsterData);
			{
				Map<Integer, ICombatMonsterUpdateInfo> map = new HashMap<Integer, ICombatMonsterUpdateInfo>();
				map.put(raceBoss.battleFiledMonsterInstanceId, raceBoss);
				_gangIdToBossesMap.put(raceBoss.gangId, map);
			}
			_gangIdToBattlefieldMap.put(raceBoss.gangId, battlefield);
			_gangIdToBossMap.put(raceBoss.gangId, raceBoss);
			return raceBoss;
		}

		/**
		 * <pre>
		 * 启动一场PVE战斗
		 * 
		 * @param role
		 * @param raceBoss
		 * @return
		 * @author CamusHuang
		 * @creation 2014-6-5 下午6:38:16
		 * </pre>
		 */
		KActionResult<Integer> fightWithMonster(KRole role, RacePVEBoss raceBoss, GangWarPVEAtt gwAtt) {
			raceBoss = _gangIdToBossMap.get(raceBoss.gangId);
			if (raceBoss.isAlive()) {
				Map<Integer, ICombatMonsterUpdateInfo> map = _gangIdToBossesMap.get(raceBoss.gangId);
				gwAtt.oppCurrentHp = raceBoss.getCurrentHp();
				return KSupportFactory.getCombatModuleSupport().fightByUpdateInfo(role, _gangIdToBattlefieldMap.get(raceBoss.gangId), null, map, KCombatType.GANG_WAR_PVE, gwAtt, (int)KGangWarConfig.getInstance().PKMaxTime);
			} else {
				KActionResult<Integer> result = new KActionResult<Integer>();
				result.success = false;
				result.tips = GangWarTips.BOSS已经死亡;
				return result;
			}
		}

		/**
		 * <pre>
		 * PVE战斗结果回调
		 * 
		 * @param roleId
		 * @param roleResult
		 * @param globalResult
		 * @author CamusHuang
		 * @creation 2014-6-5 下午6:38:34
		 * </pre>
		 */
		long processCombatFinished(long roleId, ICombatCommonResult roleResult, ICombatGlobalCommonResult globalResult, GangWarPVEAtt gwAtt) {
			long lastHp = gwAtt.oppCurrentHp;
			RacePVEBoss oppRaceBoss = _gangIdToBossMap.get(gwAtt.oppGangId);
			long currentHp = globalResult.getMonsterHpInfo().get(oppRaceBoss.battleFiledMonsterInstanceId);
			long killHP = lastHp - currentHp;
			synchronized (oppRaceBoss) {
				oppRaceBoss.killHp(killHP);
			}
			// ///////////////
			
			GangWarLogic.GangWarLogger.warn("军团战：PVE结果：角色{}！boss原HP{}现HP{}伤害{}", roleId, lastHp, currentHp, killHP);
			
			return killHP;
		}
		
		RacePVEBoss getBossByGangId(long gangId){
			return _gangIdToBossMap.get(gangId);
		}

		private void clear() {
			this._gangIdToBossMap.clear();
			this._gangIdToBossesMap.clear();
			this._gangIdToBattlefieldMap.clear();
		}

		/**
		 * <pre>
		 * 一个BOSS
		 * 
		 * @author CamusHuang
		 * @creation 2014-6-5 下午6:38:54
		 * </pre>
		 */
		public static class RacePVEBoss implements ICombatMonsterUpdateInfo {

			public final long gangId;//所属军团ID
			//
			public final int bossTemplateId;//BOSS模板ID
			public final int battleFiledMonsterTemplateId;//战场怪物模板ID
			public final int battleFiledMonsterInstanceId;//战场怪物实例ID
			public final long maxHp;
			private AtomicLong _currentHp;
			private boolean _alive;

			private RacePVEBoss(long gangId, KMonstTemplate bossTemp, MonsterData monsterData) {
				this.gangId = gangId;
				//
				this.bossTemplateId = bossTemp.id;
				this.maxHp = bossTemp.allEffects.get(KGameAttrType.MAX_HP);
				this._currentHp = new AtomicLong(this.maxHp);
				this._alive = true;
				//
//				this.battleFiledMonsterTemplateId = monsterData._monsterTemplate.id;
				this.battleFiledMonsterTemplateId = bossTemp.id;
				this.battleFiledMonsterInstanceId = monsterData._objInstanceId;
			}

			@Override
			public int getInstanceId() {
				return battleFiledMonsterInstanceId;
			}

			@Override
			public int getTemplateId() {
				return battleFiledMonsterTemplateId;
			}

			@Override
			public long getCurrentHp() {
				return _currentHp.get();
			}
			
			private void killHp(long value) {
				if (_alive) {
					_currentHp.addAndGet(-value);
					if (_currentHp.get() <= 0) {
						this._alive = false;
						if (_currentHp.get() < 0) {
							_currentHp.set(0);
						}
					}
				}
			}

			boolean isAlive() {
				return _alive;
			}
		}
	}
}
