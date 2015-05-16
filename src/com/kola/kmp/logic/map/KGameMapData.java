package com.kola.kmp.logic.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.XmlUtil;
import com.kola.kmp.logic.npc.KNPCDataStructs.KNPCTemplate;
import com.kola.kmp.logic.other.KMapEntityTypeEnum;
import com.kola.kmp.logic.other.KMapTypeEnum;
import com.kola.kmp.logic.support.KSupportFactory;

public class KGameMapData implements Cloneable {

	private static final KGameLogger _LOGGER = KGameLogger
			.getLogger(KGameMapData.class);

	// /**
	// * 地图出口 向上
	// */
	// public static final byte DIR_UP = 0;
	// /**
	// * 地图出口 向左
	// */
	// public static final byte DIR_LEFT = 1;
	// /**
	// * 地图出口 向下
	// */
	// public static final byte DIR_DOWN = 2;
	// /**
	// * 地图出口 向右
	// */
	// public static final byte DIR_RIGHT = 3;
	//
	// /**
	// * 表示4个方向的出口是否存在，四个方向在数组的顺序依次是[上,左,下,右]
	// */
	// private boolean[] _exitsIfExist = null;

	/**
	 * <pre>
	 * 地图出口事件，数组长度为4，参考DIR_UP、DIR_LEFT、DIR_DOWN、DIR_RIGHT四个方向，
	 * 各方向对应的出口事件可能为null，表示没有出口
	 * </pre>
	 */
	private Map<Integer, GameMapExitsEventData> _exitsEventData = new LinkedHashMap<Integer, GameMapExitsEventData>();
	/**
	 * 出入口的出生点
	 */
	private Map<Integer, KCoordinate> _exitsBronPoint = new HashMap<Integer, KCoordinate>();

	// 行走区域起始坐标
	private KCoordinate _walkAreaBeginCoordinate;

	// private float _mapWalkAreaWidth;// 地图行走区域长度；
	// private float _mapWalkAreaHeight;// 图行走区域高度；

	private float _jumpAreaBronX;// 从大地图跳转后进入该地图的角色出生点X坐标
	private float _jumpAreaBronY;// 从大地图跳转后进入该地图的角色出生点Y坐标

	/**
	 * 初始化MapData
	 * 
	 * @param map
	 * @param flashXmlPath
	 * @param dataRow
	 * @throws Exception
	 */
	public void initMapData(KMap map, String flashXmlPath,
			KGameExcelRow dataRow, int rowIndex) throws Exception {

		try {
			_LOGGER.info("！！！读取地图ID为" + map.getMapId() + "的地图XML数据！！！");

			// _exitsIfExist = new boolean[] { false, false, false, false };

			// _exitsEventData = new GameMapExitsEventData[4];

			Document doc = XmlUtil.openXml(flashXmlPath);
			if (doc == null) {
				throw new KGameServerException(
						"加载主城配置表的地图xml数据错误，不存在此路径xml文件，值=" + flashXmlPath
								+ "，Row=" + rowIndex);
			} else {
				Element root = doc.getRootElement();

				// 读取NPC位置
				List<Element> mapObjEList = root.getChildren("gameObject");

				for (Element mapObjE : mapObjEList) {

					int objType = Integer.parseInt(mapObjE
							.getAttributeValue("type"));
					if (objType == KMapEntityTypeEnum.ENTITY_TYPE_NPC.entityType) {
						initNpcEntityAndPutIntoMap(map, mapObjE, rowIndex);
					} else if (objType == KMapEntityTypeEnum.ENTITY_TYPE_MAP_EXIT.entityType) {
						initExitsEntityAndPutIntoMap(map, mapObjE, dataRow,
								rowIndex,
								GameMapExitsEventData.EXIT_EVENT_TYPE_JUMP_MAP);
					} else if (objType == KMapEntityTypeEnum.ENTITY_TYPE_BORN_POINT.entityType) {
						initBornPointAndPutIntoMap(mapObjE);
					} else if (objType == KMapEntityTypeEnum.ENTITY_TYPE_LEVEL_EXIT.entityType) {
						initExitsEntityAndPutIntoMap(
								map,
								mapObjE,
								dataRow,
								rowIndex,
								GameMapExitsEventData.EXIT_EVENT_TYPE_GAMELEVELS);
					}

				}

			}
		} catch (Exception e) {
			throw new KGameServerException("读取地图id为：" + map.getMapId()
					+ "的地图XML数据发生错误！", e);
		}
	}

	// /**
	// * 该地图行走区域起始坐标；
	// *
	// * @return
	// */
	// public KCoordinate getWalkAreaBeginCoordinate() {
	// return _walkAreaBeginCoordinate;
	// }

	/**
	 * 检测是否存在对应方向出入口事件数据
	 * 
	 * @param direction
	 *            ,该值只能为:DIR_UP,DIR_DOWN,DIR_LEFT,DIR_RIGHT
	 * @return
	 */
	public boolean isHasGameMapExitsEventData(int exitId) {
		return _exitsEventData.containsKey(exitId);
	}

	/**
	 * 获取该地图所有出入口事件数据
	 * 
	 * @return
	 */
	public List<GameMapExitsEventData> getAllGameMapExitsEventData() {
		List<GameMapExitsEventData> list = new ArrayList<GameMapExitsEventData>();
		list.addAll(_exitsEventData.values());
		return list;
	}

	/**
	 * 获取该地图所有出入口事件数据
	 * 
	 * @return
	 */
	public GameMapExitsEventData getGameMapExitsEventData(int exitId) {
		return _exitsEventData.get(exitId);
	}

	public GameMapExitsEventData getGameMapExitsEventDataByTargetMapId(int mapId) {
		GameMapExitsEventData result = null;
		for (GameMapExitsEventData data : _exitsEventData.values()) {
			if (data.getExitType() == GameMapExitsEventData.EXIT_EVENT_TYPE_JUMP_MAP
					&& data.targetId == mapId) {
				result = data;
				break;
			}
		}
		return result;
	}

	@Override
	public Object clone() {

		KGameMapData o = null;
		try {
			o = (KGameMapData) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void loadFlashXmlData(String path) {

	}

	/**
	 * 初始化NPC地图实体,并将其放入地图
	 */
	private void initNpcEntityAndPutIntoMap(KMap map, Element e, int rowIndex)
			throws Exception {
		// 读取xml的npc信息，并初始化NpcOrExitsMapViewInfo的数据
		int npcId = Integer.parseInt(e.getAttributeValue("id"));
		float[] result = getPosition(e);
		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));

		// 根据NPC ID获取NPC信息
		KNPCTemplate npc = KSupportFactory.getNpcModuleSupport()
				.getNPCTemplate((int) npcId);
		if (npc == null) {
			// throw new KGameServerException("读取地图的NPCID为：" + npcId
			// + "的NPC模版实体对象为null！！，Row=" + rowIndex);
		}

		if (npc != null) {
			npc.instanceId = instance_id;
			KGameMapEntity npcEntity = new KGameMapEntity(
					KMapEntityTypeEnum.ENTITY_TYPE_NPC, npcId, npc);
			npcEntity.setCoordinate(new KCoordinate(result[0], result[1]));
			npcEntity.setEditorInstanceId(instance_id);

			try {
				map.putEntityTo(npcEntity, result[0], result[1]);
			} catch (KGameMapModuleException ex) {
				_LOGGER.error("初始化NPC发生错误，NpcID：" + npcId, ex);
				throw ex;
			}

			if (map.getMapType() == KMapTypeEnum.普通主城地图) {
				KMapModule.getGameMapManager().getNpcMappingGameMap()
						.put(npcId, (KGameNormalMap) map);
			}
		}

	}

	/**
	 * 初始化出入口地图实体及其事件数据,并将其放入地图
	 */
	private void initExitsEntityAndPutIntoMap(KMap map, Element e,
			KGameExcelRow excelDataRow, int rowIndex, int eventType)
			throws Exception {
		// 读取xml的npc信息，并初始化NpcOrExitsMapViewInfo的数据
		int exitId = Integer.parseInt(e.getAttributeValue("id"));
		int targetId = excelDataRow.getInt("targetId" + exitId);
		if (targetId == 0) {
			throw new KGameServerException(
					"读取地图的出入口信息错误，xml与excel表数据不匹配，targetId" + exitId
							+ "的目标ID为0，Row=" + rowIndex);
		}

		float[] result = getPosition(e);

		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));

		// 处理地图出入口事件数据

		GameMapExitsEventData eventData = new GameMapExitsEventData(exitId,
				eventType, result[0], result[1]);
		eventData.srcMapId = map.getMapId();

		eventData.targetId = targetId;
		eventData.setNpcOrExitInstanceId(instance_id);
		_exitsEventData.put(eventData.getExitId(), eventData);
		if (map.getMapType() == KMapTypeEnum.普通主城地图) {
			if (eventType == GameMapExitsEventData.EXIT_EVENT_TYPE_GAMELEVELS) {
				KMapModule.getGameMapManager().getScenarioMappingGameMap()
						.put(eventData.targetId, (KGameNormalMap) map);
			}
		}
	}

	/**
	 * 初始化出入口地图实体及其事件数据,并将其放入地图
	 */
	private void initBornPointAndPutIntoMap(Element e) throws Exception {
		float[] result = getPosition(e);

		int exitsId = Integer.parseInt(e.getAttributeValue("id"));

		if (exitsId != 0) {
			this._exitsBronPoint.put(exitsId, new KCoordinate(result[0],
					result[1]));
		}
		if (this._walkAreaBeginCoordinate == null) {
			this._walkAreaBeginCoordinate = new KCoordinate(result[0],
					result[1]);
		}

	}

	private float[] getPosition(Element e) throws Exception {
		float[] result = new float[] { 0f, 0f };

		if ((e.getChild("transform").getChild("position")) != null) {
			result[0] = Float.parseFloat(e.getChild("transform")
					.getChild("position").getAttributeValue("x"));
			result[1] = Float.parseFloat(e.getChild("transform")
					.getChild("position").getAttributeValue("y"));
		}

		return result;
	}

	// /**
	// * 根据角色进入地图对应出口方向，已经地图的行走区域范围，在出口附近随机设定角色跳转地图后的出生点坐标
	// *
	// * @param direction
	// * @return
	// */
	// public KCoordinate getPlayerRoleJumpMapCoordinate(int srcExitId) {
	// // 如果没有该地图方向的出口，返回null
	// if (!_exitsEventData.containsKey(srcExitId)) {
	// return null;
	// }
	//
	// GameMapExitsEventData eventData = _exitsEventData.get(srcExitId);
	//
	// float x, y;
	// x = (float) UtilTool.random(
	// (int) (eventData.getX() - eventData.getW()),
	// (int) (eventData.getX() + eventData.getW()));
	// y = (float) UtilTool.random(
	// (int) (eventData.getY() - eventData.getH()),
	// (int) (eventData.getY() + eventData.getH()));
	// return new KCoordinate(x, y);
	//
	// }

	/**
	 * <pre>
	 * 处理角色跳转地图时，没有带出口方向的情况(一般情况为从大场景跳转的情况)，
	 * 则在地图行走范围中心区域随机选择一个坐标为出生点
	 * @return
	 * </pre>
	 */
	public KCoordinate getPlayerRoleJumpMapCoordinateWithNoDirection() {
		if (_walkAreaBeginCoordinate != null) {
			return _walkAreaBeginCoordinate;
		} else {
			for (KCoordinate coor : _exitsBronPoint.values()) {
				return coor;
			}
		}
		return new KCoordinate(0, 0);
	}

	/**
	 * <pre>
	 * 处理角色跳转地图时，没有带出口方向的情况(一般情况为从大场景跳转的情况)，
	 * 则在地图行走范围中心区域随机选择一个坐标为出生点
	 * @return
	 * </pre>
	 */
	public KCoordinate getPlayerRoleJumpMapCoordinate(int exitId) {
		KCoordinate targetCoor = null;
		if (_exitsBronPoint.containsKey(exitId)) {
			targetCoor = _exitsBronPoint.get(exitId);
		} else if (_walkAreaBeginCoordinate != null) {
			targetCoor = _walkAreaBeginCoordinate;
		} else {
			targetCoor = new KCoordinate(0, 0);
		}

		return targetCoor;
	}

	public KCoordinate checkEntityCoordinateAndGetNewPosition(float x, float y) {
		if (_walkAreaBeginCoordinate != null) {
			return _walkAreaBeginCoordinate;
		} else {
			for (KCoordinate coor : _exitsBronPoint.values()) {
				return coor;
			}
		}
		return new KCoordinate(0, 0);
	}

}
