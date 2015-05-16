package com.kola.kmp.logic.activity.worldboss;

import java.util.ArrayList;
import java.util.List;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.level.BattlefieldWaveViewInfo;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.MonsterData;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.support.KSupportFactory;

/**
 * 
 * @author PERRY CHAN
 */
public class KWorldBossFieldData {

	/** 模板id */
	public final int templateId;
//	/** 最小等级 */
//	public final int minLv;
//	/** 最大等级 */
//	public final int maxLv;
	/** 场景地图id */
	public final int mapId;
//	/** 出怪模板id */
//	public final int monsterGenId;
//	/** 伤害基数 */
//	public final float dmBasicPara;
//	/** 排名奖励id */
//	public final int rankRewardId;
//	/** 小怪波数 */
//	public final int waveCount;
	/** 小怪刷新间隔 */
	public final int refurbishSeconds;
//	/** 等级描述 */
//	public final String lvDescr;
	public final int levelId;
	/** 所有的怪物信息 */
	public final List<KWorldBossMonsterKey> monsterIds;
	
	private KGameBattlefield _battlefield;
	
	KWorldBossFieldData(KGameExcelRow row) {
		this.templateId = row.getInt("templateId");
//		this.minLv = row.getInt("minLv");
//		this.maxLv = row.getInt("maxLv");
		this.mapId = row.getInt("mapId");
//		this.monsterGenId = row.getInt("monsterGenId");
//		this.dmBasicPara = row.getFloat("dmBasicPara");
//		this.rankRewardId = row.getInt("rankRewardId");
//		this.waveCount = row.getInt("waveCount");
		this.refurbishSeconds = row.getInt("refurbishSeconds");
//		this.lvDescr = "lv" + minLv + "-" + maxLv;
		this.levelId = row.getInt("levelId");
		this.monsterIds = new ArrayList<KWorldBossMonsterKey>();
	}
	
	void onGameWorldInitComplete() {
//		if (KWorldBossManager.getWorldBossGenRule(monsterGenId) == null) {
//			throw new NullPointerException("不存在id为[" + monsterGenId + "]的出怪模板数据！");
//		}
//		if (KSupportFactory.getDuplicateMapSupport().getDuplicateMapStruct(mapId) == null) {
//			throw new NullPointerException("不存在id为[" + mapId + "]的副本模板数据！");
//		}
		_battlefield = KSupportFactory.getLevelSupport().getWorldBossBattlefield(levelId);
		List<BattlefieldWaveViewInfo> allWaveInfos = _battlefield.getAllWaveInfo();
		List<MonsterData> monsterList;
		MonsterData current;
		for (int i = 0; i < allWaveInfos.size(); i++) {
			monsterList = allWaveInfos.get(i).getAllMonsters();
			for (int k = 0; k < monsterList.size(); k++) {
				current = monsterList.get(k);
				this.monsterIds.add(new KWorldBossMonsterKey(current._objInstanceId, current._monsterTemplate.id));
			}
		}
		if (this.monsterIds.isEmpty()) {
			throw new IllegalArgumentException("世界boss，场景模板[" + mapId + "]所挂载的怪物数量为0！关卡id是：" + levelId);
		}
	}

	public KGameBattlefield getBattlefield() {
		return _battlefield;
	}
	
	/**
	 * 
	 * 世界boss怪物的键值，包含自生成的instanceId和模板templateId
	 * 
	 * @author PERRY CHAN
	 */
	public static class KWorldBossMonsterKey {
		public final int instanceId;
		public final int templateId;

		public KWorldBossMonsterKey(int pInstanceId, int pTemplateId) {
			this.instanceId = pInstanceId;
			this.templateId = pTemplateId;
		}
	}
}
