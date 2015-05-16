package com.kola.kmp.logic.level.petcopy;

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
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.combat.IBattlefieldBaseInfo;
import com.kola.kmp.logic.combat.IBattlefieldLevelInfo;
import com.kola.kmp.logic.level.BattlefieldWaveViewInfo;
import com.kola.kmp.logic.level.KBattleObjectDataStruct;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.BornPointData;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.MonsterData;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.ObstructionData;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.SectionPointData;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.ObstructionTemplate;
import com.kola.kmp.logic.other.KBattleObjectTypeEnum;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;
import com.kola.kmp.logic.other.KPetCopyDropTypeEnum;
import com.kola.kmp.logic.support.KSupportFactory;
/**
 * 随从副本战场数据
 * @author Administrator
 *
 */
public class KPetCopyBattlefield implements IBattlefieldLevelInfo {

	private static final KGameLogger _LOGGER = KGameLogger
			.getLogger(KPetCopyBattlefield.class);

	// 战斗场景类型
	public KGameBattlefieldTypeEnum battlefieldType = KGameBattlefieldTypeEnum.随从副本战场;
	// 对应关卡编号
	public int levelId;
	// 战斗场景编号
	public int battlefieldId;
	// 战斗场景客户端资源编号ID
	public int battlefieldResId;
	// 战斗场景客户端资源编路径文件名
	public String battlePathName;
	// 战斗背景音乐
	public int bgMusicResId;

	// 主角角色出生点坐标
	public BornPointData bornPoint;
	// 本战斗场景中的所有分段数据
	public List<BattlefieldWaveViewInfo> allWaveInfo = new ArrayList<BattlefieldWaveViewInfo>();
	// 本战斗场景中的所有怪物数据 Key：怪物的实体Id
	public Map<Integer, MonsterData> monsterMap = new LinkedHashMap<Integer, KBattleObjectDataStruct.MonsterData>();
	// 本战斗场景中的所有怪物数据 Key：障碍物的实体Id
	public Map<Integer, ObstructionData> obstructionMap = new LinkedHashMap<Integer, KBattleObjectDataStruct.ObstructionData>();
	// 笼子（障碍物）对应掉落数据表，Key：笼子（障碍物）实例，Value：掉落数据
	public Map<ObstructionData, KPetCopyBattlefieldDropData> dropMap = new HashMap<ObstructionData, KPetCopyBattlefieldDropData>();
	// 表示战场上的每一个分段的终点坐标信息
	public List<SectionPointData> sectionPointDataList = new ArrayList<KBattleObjectDataStruct.SectionPointData>();
	// 战斗时间限制（单位：毫秒）
	public long battleTimeMillis;

	public boolean isInitOK = true;

	public void initBattlefield(int levelId, String tableName, String fileName,int musicId,long battleTimeMillis,
			int rowIndex) throws KGameServerException {
		this.levelId = levelId;
		this.battlePathName = fileName;
		this.bgMusicResId = musicId;
		this.battleTimeMillis = battleTimeMillis;
		this.battlefieldId = KGameLevelModuleExtension.getManager()
				.getBattlefieldIdGenerator().nextBattlefieldId();
		String path = KGameBattlefield.battlefield_res_file_path + fileName
				+ ".assetbundle";
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
				} else if (objType == KBattleObjectTypeEnum.OBJ_TYPE_OBSTRUCTION.entityType) {
					initObstructionData(fileName, mapObjE, waveMap);
				}
			}
			this.allWaveInfo.addAll(waveMap.values());
			Collections.sort(this.allWaveInfo);
			
			if(this.bornPoint == null){
				isInitOK = false;
				throw new KGameServerException("读取战场资源文件：" + fileName + "的XML数据发生错误,没有设置出生点");
			}

		} catch (Exception e) {
			throw new KGameServerException("读取表<" + tableName + ">的关卡id为："
					+ levelId + "的战斗场景XML数据发生错误！excel行数：" + rowIndex, e);
		}
	}

	private void initSectionPoint(String fileName, Element e)
			throws KGameServerException {
		float[] result = getPosition(e);
		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));

		SectionPointData data = new SectionPointData(instance_id, result[0],
				result[1]);
		this.sectionPointDataList.add(data);
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
			// throw new KGameServerException("读取战场资源文件：" + fileName
			// + "的XML数据发生错误,找不到怪物模版=" + monsterId + "！实体ID："
			// + instance_id);
			isInitOK = false;
			_LOGGER.error("读取战场资源文件：" + fileName + "的XML数据发生错误,找不到怪物模版="
					+ monsterId + "！实体ID：" + instance_id + "！节点名："
					+ e.getAttributeValue("name"));
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
		MonsterData data = new MonsterData(instance_id, mon, result[0],
				result[1]);
		wave.getAllMonsters().add(data);
		monsterMap.put(instance_id, data);
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
			// throw new KGameServerException("读取战场资源文件：" + fileName
			// + "的XML数据发生错误,找不到障碍物模版=" + obstructionId + "！实体ID："
			// + instance_id);
			isInitOK = false;
			_LOGGER.error("读取战场资源文件：" + fileName + "的XML数据发生错误,找不到障碍物模版="
					+ obstructionId + "！实体ID：" + instance_id + "！节点名："
					+ e.getAttributeValue("name"));
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
				result[1]);
		wave.getAllObstructions().add(data);
		obstructionMap.put(instance_id, data);
	}

	private int checkWaveId(float pos_x) {
		int waveId = 1;
		float temp_x = 0;
		for (SectionPointData data : sectionPointDataList) {
			if (pos_x >= temp_x && pos_x < data._corX) {
				return waveId;
			} else {
				waveId++;
				temp_x = data._corX;
			}
		}

		return waveId;
	}

	private void initBornPointData(String fileName, Element e)
			throws KGameServerException {
		float[] result = getPosition(e);
		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));

		BornPointData data = new BornPointData(instance_id, result[0],
				result[1]);
		this.bornPoint = data;
	}

	public static float[] getPosition(Element e) throws KGameServerException {
		float[] result = new float[] { 0f, 0f };

		if ((e.getChild("transform").getChild("position")) != null) {
			result[0] = Float.parseFloat(e.getChild("transform")
					.getChild("position").getAttributeValue("x"));
			result[1] = Float.parseFloat(e.getChild("transform")
					.getChild("position").getAttributeValue("y"));
		}

		return result;
	}
	
	@Override
	public boolean isFirstBattlefield() {
		return true;
	}
	
	@Override
	public KGameBattlefieldTypeEnum getBattlefieldType() {
		return battlefieldType;
	}
	
	@Override
	public int getLevelId() {
		return levelId;
	}
	
	@Override
	public List<BattlefieldWaveViewInfo> getAllWaveInfo() {
		return allWaveInfo;
	}

	@Override
	public int getBattlefieldId() {
		return this.battlefieldId;
	}

	@Override
	public String getBattlePathName() {
		return this.battlePathName;
	}

	@Override
	public int getBgMusicResId() {
		return this.bgMusicResId;
	}

	@Override
	public int getNextBattleFieldId() {
		return 0;
	}
	
	/**
	 * 打开笼子掉落数据类型
	 * @author Administrator
	 *
	 */
	public static class KPetCopyBattlefieldDropData {
		// 掉落方案ID
		public int dropId;
		// 打开笼子掉落类型
		public KPetCopyDropTypeEnum dropType;
		// 掉落图标
		public int resId;
		// 掉落道具列表，Key：道具编码，Value：数量（当dropType=ITEM时有效）
		public Map<String, Integer> itemMap = new HashMap<String, Integer>();
		// 掉落道具列表，Key：道具编码，Value：数量（当dropType=ITEM时有效）
		public Map<Integer, Integer> monsterMap = new HashMap<Integer, Integer>();
		// 掉落道具列表，Key：道具编码，Value：数量（当dropType=ITEM时有效）
		public Map<KCurrencyTypeEnum, Integer> currencyMap = new HashMap<KCurrencyTypeEnum, Integer>();
		// 是否摧毁笼子（如果dropType==MONSTER类型，需要判断是否全部击杀笼子出现的怪物）
		public boolean isCompleted = false;
	}
}
