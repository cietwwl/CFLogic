package com.kola.kmp.logic.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.XmlUtil;
import com.kola.kmp.logic.combat.IBattlefieldLevelInfo;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.BornPointData;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.ExitData;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.MonsterData;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.ObstructionData;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.SectionPointData;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.ObstructionTemplate;
import com.kola.kmp.logic.other.KBattleObjectTypeEnum;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;
import com.kola.kmp.logic.support.KSupportFactory;

public class KGameBattlefield implements IBattlefieldLevelInfo {
	private static final KGameLogger _LOGGER = KGameLogger
			.getLogger(KGameBattlefield.class);
	
	public final static String battlefield_res_file_path = "./res/gsconfig/mapModule/mapXmlData/";

	// 战斗场景类型
	private KGameBattlefieldTypeEnum battlefieldType;
	// 战斗场景编号
	private int battlefieldId;
	// 战斗场景客户端资源编号ID
	private int battlefieldResId;
	// 战斗场景客户端资源编路径文件名
	private String battlePathName;	

	// 战斗场景所属关卡ID
	private int levelId;
	// 战斗场景序号，表示战场的顺序关系
	private int battlefieldSerialNumber;
	// 上一层战斗场景编号
	private int frontBattlefieldSerialNumber;
	// 战斗背景音乐
	private int bgMusicResId;

	// 是否为第一层战斗场景
	private boolean isFirstBattlefield;
	// 是否为最后一层战斗场景
	private boolean isLastBattlefield;
	// 下个战斗场景编号（当isLastBattlefield为false的时候，该值不为0）
	private int nextBattleFieldId;
	// 本战斗场景中的所有分段数据
	public List<BattlefieldWaveViewInfo> allWaveInfo = new ArrayList<BattlefieldWaveViewInfo>();
	// 本战斗场景中的所有怪物数据 Key：怪物的实体Id
	public Map<Integer, MonsterData> monsterMap = new LinkedHashMap<Integer, KBattleObjectDataStruct.MonsterData>();
	// 本战斗场景中的所有怪物数据 Key：障碍物的实体Id
	public Map<Integer, ObstructionData> obstructionMap = new LinkedHashMap<Integer, KBattleObjectDataStruct.ObstructionData>();

	// 角色出生点坐标
	public BornPointData _bornPoint;
	
	// 表示战场上的每一个分段的终点坐标信息
	public List<SectionPointData> sectionPointDataList = new ArrayList<KBattleObjectDataStruct.SectionPointData>();
	
	public boolean isInitOK = true;

	public void initBattlefield(String tableName, String fileName,
			int nextBattlefieldId, int rowIndex) throws KGameServerException {
		this.battlePathName = fileName;
		String path = battlefield_res_file_path + fileName + ".assetbundle";
		Document doc = XmlUtil.openXml(path);
		if (doc == null) {
			throw new KGameServerException("加载表<" + tableName
					+ ">的战场xml数据错误，不存在此路径xml文件，值=" + path + "，Row=" + rowIndex);
		}
		Element root = doc.getRootElement();

		Map<Integer, BattlefieldWaveViewInfo> waveMap = new HashMap<Integer, BattlefieldWaveViewInfo>();
		// 读取NPC位置
		List<Element> mapObjEList = root.getChildren("gameObject");
		try {
			for (Element mapObjE : mapObjEList) {
				int objType = Integer.parseInt(mapObjE
						.getAttributeValue("type"));
				if (objType == KBattleObjectTypeEnum.OBJ_TYPE_SECTION_POINT.entityType) {
					initSectionPoint(fileName, mapObjE);
				}
			}
			Collections.sort(this.sectionPointDataList);

			for (Element mapObjE : mapObjEList) {
				int objType = Integer.parseInt(mapObjE
						.getAttributeValue("type"));
				if (objType == KBattleObjectTypeEnum.OBJ_TYPE_MONSTER.entityType) {
					initMonsterData(fileName, mapObjE, waveMap);
				} else if (objType == KBattleObjectTypeEnum.OBJ_TYPE_BORN_POINT.entityType) {
					initBornPointData(fileName, mapObjE);
				} else if (objType == KBattleObjectTypeEnum.OBJ_TYPE_LEVEL_EXIT.entityType) {
					initExitData(fileName, mapObjE, waveMap, nextBattlefieldId);
				} else if (objType == KBattleObjectTypeEnum.OBJ_TYPE_OBSTRUCTION.entityType) {
					initObstructionData(fileName, mapObjE, waveMap);
				}
			}
			this.allWaveInfo.addAll(waveMap.values());
			Collections.sort(this.allWaveInfo);
			
			
		} catch (Exception e) {
			throw new KGameServerException("读取表<" + tableName + ">的关卡id为："
					+ levelId + "的战斗场景XML数据发生错误！excel行数：" + rowIndex, e);
		}
	}

	private void initObstructionData(String fileName, Element e,
			Map<Integer, BattlefieldWaveViewInfo> waveMap)
			throws KGameServerException {
		int obstructionId = Integer.parseInt(e.getAttributeValue("id"));
		float[] result = getPosition(e);
		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));

		ObstructionTemplate obs = KSupportFactory.getNpcModuleSupport()
				.getObstructionTemp(obstructionId);
		if (obs == null) {
//			throw new KGameServerException("读取战场资源文件：" + fileName
//					+ "的XML数据发生错误,找不到障碍物模版=" + obstructionId + "！实体ID："
//					+ instance_id);
			isInitOK = false;
			_LOGGER.error("读取战场资源文件：" + fileName + "的XML数据发生错误,找不到障碍物模版=" + obstructionId + "！实体ID：" + instance_id + "！节点名：" + e.getAttributeValue("name"));
		    return;
		}
		int waveId = checkWaveId(result[0]);
		BattlefieldWaveViewInfo wave = null;
		if (!waveMap.containsKey(waveId)) {
			wave = new BattlefieldWaveViewInfo(waveId, this.battlefieldId);
			waveMap.put(waveId, wave);
		} else {
			wave = waveMap.get(waveId);
		}
		ObstructionData data = new ObstructionData(instance_id, obs, result[0],
				result[0]);
		wave.getAllObstructions().add(data);
		obstructionMap.put(instance_id, data);
	}

	private void initMonsterData(String fileName, Element e,
			Map<Integer, BattlefieldWaveViewInfo> waveMap)
			throws KGameServerException {
		int monsterId = Integer.parseInt(e.getAttributeValue("id"));
		float[] result = getPosition(e);
		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));

		KMonstTemplate mon = KSupportFactory.getNpcModuleSupport()
				.getMonstTemplate(monsterId);
		if (mon == null) {
//			throw new KGameServerException("读取战场资源文件：" + fileName
//					+ "的XML数据发生错误,找不到怪物模版=" + monsterId + "！实体ID："
//					+ instance_id);
			isInitOK = false;
			_LOGGER.error("读取战场资源文件：" + fileName + "的XML数据发生错误,找不到怪物模版=" + monsterId + "！实体ID：" + instance_id + "！节点名：" + e.getAttributeValue("name"));
			return;
		}
		int waveId = checkWaveId(result[0]);
		BattlefieldWaveViewInfo wave = null;
		if (!waveMap.containsKey(waveId)) {
			wave = new BattlefieldWaveViewInfo(waveId, this.battlefieldId);
			waveMap.put(waveId, wave);
		} else {
			wave = waveMap.get(waveId);
		}
		MonsterData data = new MonsterData(instance_id, mon, result[0], result[1]);
		wave.getAllMonsters().add(data);
		monsterMap.put(instance_id, data);
	}

	private void initExitData(String fileName, Element e,
			Map<Integer, BattlefieldWaveViewInfo> waveMap, int nextBattlefieldId)
			throws KGameServerException {
		float[] result = getPosition(e);
		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));
		int waveId = checkWaveId(result[0]);
		BattlefieldWaveViewInfo wave = null;
		if (!waveMap.containsKey(waveId)) {
			wave = new BattlefieldWaveViewInfo(waveId, this.battlefieldId);
			waveMap.put(waveId, wave);
		} else {
			wave = waveMap.get(waveId);
		}
		ExitData data = new ExitData(instance_id, nextBattlefieldId, result[0],
				result[1]);
		wave.setExitData(data);
		wave.setHasExit(true);
	}

	private void initBornPointData(String fileName, Element e)
			throws KGameServerException {
		float[] result = getPosition(e);
		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));

		BornPointData data = new BornPointData(instance_id, result[0], result[1]);
		this._bornPoint = data;
	}
	
	public void initSectionPoint(String fileName, Element e)throws KGameServerException {
		float[] result = getPosition(e);
		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));

		
		SectionPointData data = new SectionPointData(instance_id,  result[0],
				result[1]);
		this.sectionPointDataList.add(data);
	}
	
	public int checkWaveId(float pos_x){
		int waveId = 1;
		float temp_x = 0;
		for (SectionPointData data:sectionPointDataList) {
			if(pos_x>=temp_x&&pos_x<data._corX){
				return waveId;
			}else{
				waveId++;
				temp_x = data._corX;
			}
		}
		
		return waveId;
	}

	public KGameBattlefieldTypeEnum getBattlefieldType() {
		return battlefieldType;
	}

	public int getBattlefieldId() {
		return battlefieldId;
	}

	public int getLevelId() {
		return levelId;
	}

	public int getBattlefieldSerialNumber() {
		return battlefieldSerialNumber;
	}

	public int getFrontBattlefieldSerialNumber() {
		return frontBattlefieldSerialNumber;
	}

	public int getBgMusicResId() {
		return bgMusicResId;
	}

	public boolean isFirstBattlefield() {
		return isFirstBattlefield;
	}

	public boolean isLastBattlefield() {
		return isLastBattlefield;
	}

	/**
	 * 本战斗场景中的所有分段数据
	 * 
	 * @return
	 */
	public List<BattlefieldWaveViewInfo> getAllWaveInfo() {
		return allWaveInfo;
	}

	/**
	 * 本战斗场景中的所有怪物数据
	 * 
	 * @return
	 */
	public Map<Integer, MonsterData> getMonsterMap() {
		return monsterMap;
	}

	/**
	 * 本战斗场景中的所有障碍物数据
	 * 
	 * @return
	 */
	public Map<Integer, ObstructionData> getObstructionMap() {
		return obstructionMap;
	}

	public void setBattlefieldType(KGameBattlefieldTypeEnum battlefieldType) {
		this.battlefieldType = battlefieldType;
	}

	public void setBattlefieldId(int battlefieldId) {
		this.battlefieldId = battlefieldId;
	}

	public void setLevelId(int levelId) {
		this.levelId = levelId;
	}

	public void setBattlefieldSerialNumber(int battlefieldSerialNumber) {
		this.battlefieldSerialNumber = battlefieldSerialNumber;
	}

	public void setFrontBattlefieldSerialNumber(int frontBattlefieldSerialNumber) {
		this.frontBattlefieldSerialNumber = frontBattlefieldSerialNumber;
	}

	public void setBgMusicResId(int bgMusicResId) {
		this.bgMusicResId = bgMusicResId;
	}

	public void setFirstBattlefield(boolean isFirstBattlefield) {
		this.isFirstBattlefield = isFirstBattlefield;
	}

	public void setLastBattlefield(boolean isLastBattlefield) {
		this.isLastBattlefield = isLastBattlefield;
	}

	public BornPointData getBornPoint() {
		return _bornPoint;
	}

	public void setBornPoint(BornPointData bornPoint) {
		this._bornPoint = bornPoint;
	}

	/**
	 * 战斗场景客户端资源编号ID
	 * 
	 * @return
	 */
	public int getBattlefieldResId() {
		return battlefieldResId;
	}

	public void setBattlefieldResId(int battlefieldResId) {
		this.battlefieldResId = battlefieldResId;
	}

	/**
	 * 下个战斗场景编号（当isLastBattlefield为false的时候，该值不为0）
	 * 
	 * @return
	 */
	public int getNextBattleFieldId() {
		return nextBattleFieldId;
	}

	public void setNextBattleFieldId(int nextBattleFieldId) {
		this.nextBattleFieldId = nextBattleFieldId;
	}

	/**
	 * 战斗场景客户端资源编路径文件名
	 * @return
	 */
	public String getBattlePathName() {
		return battlePathName;
	}
	
	public void setBattlePathName(String battlePathName) {
		this.battlePathName = battlePathName;
	}
	
	public void resetAllMonsterData(List<MonsterData> monDataList){
		monsterMap.clear();
		for (MonsterData data:monDataList) {
			monsterMap.put(data._objInstanceId, data);
		}
	}
	
	public static float[] getPosition(Element e) throws KGameServerException{
		float[] result = new float[]{0f,0f};
		
		if((e.getChild("transform").getChild("position"))!=null){
			result[0] = Float.parseFloat(e.getChild("transform")
					.getChild("position").getAttributeValue("x"));
			result[1] = Float.parseFloat(e.getChild("transform")
					.getChild("position").getAttributeValue("y"));
		}
		
		return result;
	}

	
}
