package com.kola.kmp.logic.competition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.XmlUtil;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.BornPointData;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.other.KBattleObjectTypeEnum;
import com.kola.kmp.logic.support.CombatModuleSupport.ICombatEnv;

public class KCompetitionBattlefield implements ICombatEnv {
	private int musicId;
	private String battlePath;
	private BornPointData bornPoint;
	private BornPointData enePoint;

	public void initBattlefield(String fileName, int musicId)
			throws KGameServerException {
		this.musicId = musicId;
		this.battlePath = fileName;
		String path = KGameBattlefield.battlefield_res_file_path + fileName
				+ ".assetbundle";
		Document doc = XmlUtil.openXml(path);
		if (doc == null) {
			throw new KGameServerException("加载竞技场战场xml数据错误，不存在此路径xml文件，值="
					+ path);
		}
		Element root = doc.getRootElement();
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
		} catch (Exception e) {
			throw new KGameServerException("加载竞技场战场xml数据错误！", e);
		}
		
		if (bornPointList.size() != 2) {
			throw new KGameServerException("读取战场资源文件：" + fileName
					+ "的XML数据发生错误,设置了小于2个的出生点！");
		}
		Collections.sort(bornPointList);
		bornPoint = bornPointList.get(0);
		enePoint = bornPointList.get(1);
	}

	private BornPointData initBornPointData(String fileName, Element e)
			throws KGameServerException {
		float[] result = KGameBattlefield.getPosition(e);
		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));

		// if (bornPoint == null) {
		// bornPoint = new BornPointData(instance_id, result[0], result[1]);
		// } else if (enePoint == null) {
		// enePoint = new BornPointData(instance_id, result[0], result[1]);
		// } else {
		// throw new KGameServerException("读取战场资源文件：" + fileName
		// + "的XML数据发生错误,设置了多于2个的出生点！实体ID：" + instance_id);
		// }

		return new BornPointData(instance_id, result[0], result[1]);
	}

	@Override
	public int getBgMusicResId() {
		return musicId;
	}

	@Override
	public String getBgResPath() {
		return battlePath;
	}

	@Override
	public float getBornCorX() {
		return bornPoint._corX;
	}

	@Override
	public float getBornCorY() {
		return bornPoint._corY;
	}

	@Override
	public float getEnermyCorX() {
		return enePoint._corX;
	}

	@Override
	public float getEnermyCorY() {
		return enePoint._corY;
	}

}
