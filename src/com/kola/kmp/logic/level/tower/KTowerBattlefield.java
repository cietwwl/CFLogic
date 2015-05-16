package com.kola.kmp.logic.level.tower;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.XmlUtil;
import com.kola.kmp.logic.combat.IBattlefieldBaseInfo;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.BornPointData;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.MonsterData;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.other.KBattleObjectTypeEnum;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;
import com.kola.kmp.logic.support.KSupportFactory;

public class KTowerBattlefield implements IBattlefieldBaseInfo {
	// 战斗场景类型
	private KGameBattlefieldTypeEnum battlefieldType;
	// 对应关卡编号
	private int levelId;
	// 战斗场景编号
	private int battlefieldId;
	// 战斗场景客户端资源编号ID
	private int battlefieldResId;
	// 战斗场景客户端资源编路径文件名
	private String battlePathName;
	// 战场总战斗波数
	private int totalWave;
	// 战斗背景音乐
	private int bgMusicResId;
	// 第一波塔防编号
	private int beginTowerId;
	// 最后一波塔防编号
	private int endTowerId;

	// 战场战斗波数信息（Key：塔防ID）
	private Map<Integer, KTowerData> towerDataMapByTowerId = new LinkedHashMap<Integer, KTowerData>();
	// 战场战斗波数信息（Key：波数序号WaveNum）
	private Map<Integer, KTowerData> towerDataMapByWaveNum = new LinkedHashMap<Integer, KTowerData>();
	// 守卫建筑数据信息
	private MonsterData defenceTowerData;
	// 主角角色出生点坐标
	private BornPointData myBornPoint;
	// 好友角色出生点坐标
	private BornPointData friendBornPoint;
	// 地图左边怪物出生点坐标
	private BornPointData leftMonsterBornPoint;
	// 地图右边怪物出生点坐标
	private BornPointData rightMonsterBornPoint;

	public void initBattlefield(int levelId, String tableName,
			KGameExcelRow xlsRow) throws KGameServerException {
		this.battlefieldType = KGameBattlefieldTypeEnum.好友副本战场;
		this.levelId = levelId;
		this.battlefieldId = KGameLevelModuleExtension.getManager()
				.getBattlefieldIdGenerator().nextBattlefieldId();
		this.battlefieldResId = xlsRow.getInt("battle_sceneId");
		this.bgMusicResId = xlsRow.getInt("music");
		String[] towerInfo = xlsRow.getData("towerInfo").split(",");
		if (towerInfo == null || towerInfo.length != 2) {
			throw new KGameServerException("读取表<" + tableName + ">的关卡id为："
					+ levelId + "的字段towerInfo格式错误，值="
					+ xlsRow.getData("towerInfo") + "，行数"
					+ xlsRow.getIndexInFile());
		}
		int[] towerId = new int[] { Integer.parseInt(towerInfo[0]),
				Integer.parseInt(towerInfo[1]) };
		if (KTowerDataManager.getTowerData(towerId[0]) == null
				|| KTowerDataManager.getTowerData(towerId[1]) == null) {
			throw new KGameServerException("读取表<" + tableName + ">的关卡id为："
					+ levelId + "的字段towerInfo格式错误，值="
					+ xlsRow.getData("towerInfo") + "，找不到塔防数据，行数"
					+ xlsRow.getIndexInFile());
		}
		this.beginTowerId = towerId[0];
		this.endTowerId = towerId[1];
		this.totalWave = towerId[1] - towerId[0] + 1;
		for (int id = towerId[0]; id <= towerId[1]; id++) {
			KTowerData data = KTowerDataManager.getTowerData(id);
			if (data == null) {
				throw new KGameServerException("读取表<" + tableName + ">的关卡id为："
						+ levelId + "的字段towerInfo格式错误，值="
						+ xlsRow.getData("towerInfo") + "，找不到塔防编号为:" + id
						+ "的数据，行数" + xlsRow.getIndexInFile());
			}
			this.towerDataMapByTowerId.put(data.getTowerId(), data);
			this.towerDataMapByWaveNum.put(data.getWaveNum(), data);
			if (id == towerId[0]) {
				data.setFirstWave(true);
				data.setNextWaveNum(data.getWaveNum() + 1);
			} else if (id == towerId[1]) {
				data.setLastWave(true);
				data.setNextWaveNum(0);
			} else {
				data.setNextWaveNum(data.getWaveNum() + 1);
			}
		}

		String fileName = xlsRow.getData("battle_res_path");
		this.battlePathName = fileName;
		String path = KGameBattlefield.battlefield_res_file_path + fileName
				+ ".assetbundle";
		Document doc = XmlUtil.openXml(path);
		if (doc == null) {
			throw new KGameServerException("加载表<" + tableName
					+ ">的战场xml数据错误，不存在此路径xml文件，值=" + path + "，Row="
					+ xlsRow.getIndexInFile());
		}
		Element root = doc.getRootElement();
		// 读取NPC位置
		List<Element> mapObjEList = root.getChildren("gameObject");
		try {
			List<BornPointData> bornPointList = new ArrayList<BornPointData>();
			for (Element mapObjE : mapObjEList) {
				int objType = Integer.parseInt(mapObjE
						.getAttributeValue("type"));
				if (objType == KBattleObjectTypeEnum.OBJ_TYPE_MONSTER.entityType) {
					initMonsterData(fileName, mapObjE);
				} else if (objType == KBattleObjectTypeEnum.OBJ_TYPE_BORN_POINT.entityType) {
					bornPointList.add(initBornPointData(fileName, mapObjE));
				}
			}

			if (bornPointList.size() != 4) {
				throw new KGameServerException("读取好友副本战场资源文件：" + fileName
						+ "的XML数据发生错误,没有设置4个出生点的数据！表<" + tableName + ">");
			}
			Collections.sort(bornPointList);
			leftMonsterBornPoint = bornPointList.get(0);
			myBornPoint = bornPointList.get(1);
			friendBornPoint = bornPointList.get(2);
			rightMonsterBornPoint = bornPointList.get(3);
			// System.err.println("BornPoint:::"+leftMonsterBornPoint._corX+","+myBornPoint._corX+","+friendBornPoint._corX+","+rightMonsterBornPoint._corX+",");

		} catch (Exception e) {
			throw new KGameServerException("读取表<" + tableName + ">的关卡id为："
					+ levelId + "的战斗场景XML数据发生错误！excel行数："
					+ xlsRow.getIndexInFile(), e);
		}
		if (defenceTowerData == null) {
			throw new KGameServerException("读取好友副本战场资源文件：" + fileName
					+ "的XML数据发生错误,没有相关守卫建筑的数据！表<" + tableName + ">");
		}

		if (myBornPoint == null || friendBornPoint == null
				|| leftMonsterBornPoint == null
				|| rightMonsterBornPoint == null) {
			throw new KGameServerException("读取好友副本战场资源文件：" + fileName
					+ "的XML数据发生错误,有出生点为NULL！表<" + tableName + ">。myBornPoint="
					+ myBornPoint + ",friendBornPoint=" + friendBornPoint
					+ ",leftMonsterBornPoint=" + leftMonsterBornPoint
					+ ",rightMonsterBornPoint=" + rightMonsterBornPoint);
		}
	}

	private void initMonsterData(String fileName, Element e)
			throws KGameServerException {
		int monsterId = Integer.parseInt(e.getAttributeValue("id"));
		float[] result = KGameBattlefield.getPosition(e);
		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));

		KMonstTemplate mon = KSupportFactory.getNpcModuleSupport()
				.getMonstTemplate(monsterId);
		if (mon == null) {
			throw new KGameServerException("读取好友副本战场资源文件：" + fileName
					+ "的XML数据发生错误,找不到守卫建筑数据的怪物模版=" + monsterId + "！实体ID："
					+ instance_id);
		}
		if (this.defenceTowerData == null) {
			this.defenceTowerData = new MonsterData(instance_id, mon,
					result[0], result[1]);
		} else {
			throw new KGameServerException("读取好友副本战场资源文件：" + fileName
					+ "的XML数据发生错误,设置了重复的守卫建筑数据！实体ID：" + instance_id);
		}
	}

	private BornPointData initBornPointData(String fileName, Element e)
			throws KGameServerException {
		float[] result = KGameBattlefield.getPosition(e);
		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));
		BornPointData bornPoint = new BornPointData(instance_id, result[0],
				result[1]);

		return bornPoint;
	}

	/**
	 * 战斗场景类型
	 * 
	 * @return
	 */
	public KGameBattlefieldTypeEnum getBattlefieldType() {
		return battlefieldType;
	}

	/**
	 * 对应关卡编号
	 * 
	 * @return
	 */
	public int getLevelId() {
		return levelId;
	}

	/**
	 * 战斗场景编号
	 * 
	 * @return
	 */
	public int getBattlefieldId() {
		return battlefieldId;
	}

	/**
	 * 战斗场景客户端资源编号ID
	 * 
	 * @return
	 */
	public int getBattlefieldResId() {
		return battlefieldResId;
	}

	/**
	 * 战斗背景音乐
	 * 
	 * @return
	 */
	public int getBgMusicResId() {
		return bgMusicResId;
	}

	/**
	 * 战场总战斗波数
	 * 
	 * @return
	 */
	public int getTotalWave() {
		return totalWave;
	}

	/**
	 * 战场战斗波数信息（Key：塔防Id）
	 * 
	 * @return
	 */
	public Map<Integer, KTowerData> getTowerDataMapByTowerId() {
		return towerDataMapByTowerId;
	}

	/**
	 * 战场战斗波数信息（Key：波数序号WaveNum）
	 * 
	 * @return
	 */
	public Map<Integer, KTowerData> getTowerDataMapByWaveNum() {
		return towerDataMapByWaveNum;
	}

	/**
	 * 守卫建筑数据信息
	 * 
	 * @return
	 */
	public MonsterData getDefenceTowerData() {
		return defenceTowerData;
	}

	/**
	 * 主角角色出生点坐标
	 * 
	 * @return
	 */
	public BornPointData getMyBornPoint() {
		return myBornPoint;
	}

	/**
	 * 好友角色出生点坐标
	 * 
	 * @return
	 */
	public BornPointData getFriendBornPoint() {
		return friendBornPoint;
	}

	/**
	 * 地图左边怪物出生点坐标
	 * 
	 * @return
	 */
	public BornPointData getLeftMonsterBornPoint() {
		return leftMonsterBornPoint;
	}

	/**
	 * 地图右边怪物出生点坐标
	 * 
	 * @return
	 */
	public BornPointData getRightMonsterBornPoint() {
		return rightMonsterBornPoint;
	}

	/**
	 * 第一波塔防编号
	 * 
	 * @return
	 */
	public int getBeginTowerId() {
		return beginTowerId;
	}

	/**
	 * 最后一波塔防编号
	 * 
	 * @return
	 */
	public int getEndTowerId() {
		return endTowerId;
	}

	/**
	 * 战斗场景客户端资源编路径文件名
	 * 
	 * @return
	 */
	public String getBattlePathName() {
		return battlePathName;
	}

	@Override
	public int getNextBattleFieldId() {
		return 0;
	}

}
