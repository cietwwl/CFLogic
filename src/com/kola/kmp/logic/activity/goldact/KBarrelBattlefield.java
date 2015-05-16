package com.kola.kmp.logic.activity.goldact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.goldact.BarrelDataStruct.BarrelData;
import com.kola.kmp.logic.combat.IBattlefieldBaseInfo;
import com.kola.kmp.logic.level.BattlefieldWaveViewInfo;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.BornPointData;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.other.KBattleObjectTypeEnum;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;
import com.kola.kmp.logic.role.KRole;

public class KBarrelBattlefield implements IBattlefieldBaseInfo {

	public final static String battlefield_res_file_path = "./res/gsconfig/mapModule/mapXmlData/";
	// 战斗场景类型
	private KGameBattlefieldTypeEnum battlefieldType;
	// 战斗场景编号
	private int battlefieldId;
	// 战斗场景客户端资源编号ID
	private int battlefieldResId;
	// 战斗场景客户端资源编路径文件名
	private String battlePathName;
	// 战斗背景音乐
	private int bgMusicResId;
	// 角色出生点坐标
	private BornPointData _bornPoint;
	// 战斗总时间限制，单位：毫秒
	private long totalBattleTimeMillis;
	// 油桶出生点列表
	private List<BornPointData> _barrelBornPointList = new ArrayList<BornPointData>();

	public void initBattlefield(String fileName)
			throws KGameServerException {
		this.battlePathName = fileName;
		String path = battlefield_res_file_path + fileName + ".assetbundle";
		Document doc = XmlUtil.openXml(path);
		if (doc == null) {
			throw new KGameServerException("加载产金活动的战场xml数据错误，不存在此路径xml文件，值="
					+ path);
		}
		Element root = doc.getRootElement();

		Map<Integer, BattlefieldWaveViewInfo> waveMap = new HashMap<Integer, BattlefieldWaveViewInfo>();
		// 读取NPC位置
		List<Element> mapObjEList = root.getChildren("gameObject");
		List<BornPointData> bornPointList = new ArrayList<BornPointData>();
		try {
			for (Element mapObjE : mapObjEList) {
				int objType = Integer.parseInt(mapObjE
						.getAttributeValue("type"));
				if (objType == KBattleObjectTypeEnum.OBJ_TYPE_BORN_POINT.entityType) {
					bornPointList.add(initBornPointData(fileName, mapObjE));
				}
			}
			
			if(bornPointList.size()!=4){
				throw new KGameServerException("加载产金活动的战场xml数据错误，文件值="
						+ path+",出生点必须为4个，当前值="+bornPointList.size());
			}
			
			Collections.sort(bornPointList);
			
			for (int i = 0; i < bornPointList.size(); i++) {
				if(i==0){
					this._bornPoint = bornPointList.get(i);
				}else{
					this._barrelBornPointList.add(bornPointList.get(i));
				}
			}

		} catch (Exception e) {
			throw new KGameServerException("加载产金活动的战场xml数据错误，不存在此路径xml文件，值="
					+ path, e);
		}
	}

	private BornPointData initBornPointData(String fileName, Element e)
			throws KGameServerException {
		float[] result = KGameBattlefield.getPosition(e);
		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));

		return new BornPointData(instance_id, result[0], result[1]);
	}

	@Override
	public int getBattlefieldId() {
		return battlefieldId;
	}

	@Override
	public String getBattlePathName() {
		return battlePathName;
	}

	@Override
	public int getBgMusicResId() {
		return bgMusicResId;
	}

	@Override
	public int getNextBattleFieldId() {
		return 0;
	}

	public BornPointData getBornPoint() {
		return _bornPoint;
	}

	public long getTotalBattleTimeMillis() {
		return totalBattleTimeMillis;
	}

	public List<BornPointData> getBarrelBornPointList() {
		return _barrelBornPointList;
	}
	
	public KGameBattlefieldTypeEnum getBattlefieldType() {
		return battlefieldType;
	}

	public void setBattlefieldType(KGameBattlefieldTypeEnum battlefieldType) {
		this.battlefieldType = battlefieldType;
	}

	public void setBattlefieldId(int battlefieldId) {
		this.battlefieldId = battlefieldId;
	}

	public void setBattlefieldResId(int battlefieldResId) {
		this.battlefieldResId = battlefieldResId;
	}

	public void setBattlePathName(String battlePathName) {
		this.battlePathName = battlePathName;
	}

	public void setBgMusicResId(int bgMusicResId) {
		this.bgMusicResId = bgMusicResId;
	}

	public void setTotalBattleTimeMillis(long totalBattleTimeMillis) {
		this.totalBattleTimeMillis = totalBattleTimeMillis;
	}

	/**
	 * 根据角色等级，获取所有油桶出生点的全部油桶数据。
	 * 
	 * @param roleLv
	 * @return
	 */
	public Map<BornPointData, List<KBarrelBattleData>> getBarrelBattleDatas(
			int roleLv) {

		Map<BornPointData, List<KBarrelBattleData>> map = new HashMap<BornPointData, List<KBarrelBattleData>>();

		BarrelDataStruct struct = KGoldActivityManager
				.getBarrelDataStruct(roleLv);
		if (struct == null) {
			return map;
		}

		long nextWaveTime = 0;
		int totalBarrelCount = 0;
		long maxWaveTime = 0;
		long thisWaveTime = 0;
		for (int i = 0; i < KGoldActivityManager.waveParamList.size(); i++) {
			WaveParamData param = KGoldActivityManager.waveParamList.get(i);
			int waveBarrelCount = 0;

			for (int j = 0; j < _barrelBornPointList.size(); j++) {
				BornPointData bornPoint = _barrelBornPointList.get(j);
				if (!map.containsKey(bornPoint)) {
					map.put(bornPoint, new ArrayList<KBarrelBattleData>());
				}
				int thisWayBarrelCount = UtilTool.random(
						param.minPerBarrelCount, param.maxPerBarrelCount);
				if (thisWayBarrelCount > (param.waveBarrelCount - waveBarrelCount)) {
					thisWayBarrelCount = param.waveBarrelCount
							- waveBarrelCount;
				}
				long nextBarrelBornTime = thisWaveTime;
//				System.err.println("@@@@@@@@@@ wave:" + param.waveId
//						+ ", bornPoint_" + bornPoint._objInstanceId + ":x-"
//						+ bornPoint._corX + ",y-" + bornPoint._corY + ",size:"
//						+ thisWayBarrelCount + ",nextWaveTime:" + nextWaveTime);
				for (int k = 0; k < thisWayBarrelCount; k++) {
					if (waveBarrelCount < param.waveBarrelCount) {
						long barrelBornTime = param.getRamdomPerTime();
						if ((nextBarrelBornTime + barrelBornTime) <= (param.waveTimeSecond * 1000)
								+ nextWaveTime) {
							nextBarrelBornTime += barrelBornTime;
							KBarrelBattleData data = new KBarrelBattleData();
							data.bornPoint = bornPoint;
							data.bornTimeMillis = nextBarrelBornTime;
							map.get(bornPoint).add(data);
							totalBarrelCount++;
							waveBarrelCount++;
							if (nextBarrelBornTime > maxWaveTime) {
								maxWaveTime = nextBarrelBornTime;
							}
						}
					}
				}
			}
			thisWaveTime = maxWaveTime;
			nextWaveTime += (param.waveTimeSecond * 1000);
		}

		List<BarrelData> barrelDataList = new ArrayList<BarrelData>();

		for (int i = 0; i < totalBarrelCount; i++) {
			BarrelData data = struct.caculateBarrelData();
			barrelDataList.add(data);
		}

		int index = 0;
		for (BornPointData bornPoint : map.keySet()) {
			List<KBarrelBattleData> list = map.get(bornPoint);
			for (KBarrelBattleData barrelBattleData : list) {
				BarrelData data = barrelDataList.get(index);
				barrelBattleData.dropMap = data.dropMap;
				barrelBattleData.monstTemplate = data.template;
				barrelBattleData.totalDropRate = data._totalDropRate;
				index++;
			}
		}

		return map;
	}

	public static class KBarrelBattleData {
		// 油桶怪物模版
		public KMonstTemplate monstTemplate;
		// 油桶的掉落ID表，参考ObstructionTemplate的dropMap
		public Map<Integer, Integer> dropMap;
		// 油桶的总掉落值，参考ObstructionTemplate的_totalRate
		public int totalDropRate;
		// 油桶的出生点
		public BornPointData bornPoint;
		// 油桶的出生时间，单位：毫秒
		public long bornTimeMillis;
	}

}
