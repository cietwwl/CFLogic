package com.kola.kmp.logic.competition.teampvp;

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
import com.kola.kmp.logic.support.CombatModuleSupport.ICombatEnvPlus;

/**
 * 
 * @author PERRY CHAN
 */
public class KTeamPVPBattlefield implements ICombatEnvPlus {

	private int musicId;
	private String battlePath;
	private List<float[]> _roleCorDatas;
	private List<float[]> _enemyCorDatas;

	public void initBattlefield(String fileName, int musicId) throws KGameServerException {
		this.musicId = musicId;
		this.battlePath = fileName;
		String path = KGameBattlefield.battlefield_res_file_path + fileName + ".assetbundle";
		Document doc = XmlUtil.openXml(path);
		if (doc == null) {
			throw new KGameServerException("加载竞技场战场xml数据错误，不存在此路径xml文件，值=" + path);
		}
		Element root = doc.getRootElement();
		// 读取NPC位置
		@SuppressWarnings("unchecked")
		List<Element> mapObjEList = root.getChildren("gameObject");
		List<BornPointData> bornPointList = new ArrayList<BornPointData>();
		try {

			for (Element mapObjE : mapObjEList) {
				int objType = Integer.parseInt(mapObjE.getAttributeValue("type"));
				if (objType == KBattleObjectTypeEnum.OBJ_TYPE_BORN_POINT.entityType) {
					bornPointList.add(initBornPointData(fileName, mapObjE));
				}
			}
		} catch (Exception e) {
			throw new KGameServerException("加载竞技场战场xml数据错误！", e);
		}

		if (bornPointList.size() != 4) {
			throw new KGameServerException("读取战场资源文件：" + fileName + "的XML数据发生错误,设置了小于2个的出生点！");
		}
		_roleCorDatas = new ArrayList<float[]>();
		_enemyCorDatas = new ArrayList<float[]>();
		Collections.sort(bornPointList);
		_roleCorDatas.add(new float[]{bornPointList.get(0)._corX, bornPointList.get(0)._corY});
		_roleCorDatas.add(new float[]{bornPointList.get(1)._corX, bornPointList.get(1)._corY});
		_enemyCorDatas.add(new float[]{bornPointList.get(2)._corX, bornPointList.get(2)._corY});
		_enemyCorDatas.add(new float[]{bornPointList.get(3)._corX, bornPointList.get(3)._corY});
	}

	private BornPointData initBornPointData(String fileName, Element e)
			throws KGameServerException {
		float[] result = KGameBattlefield.getPosition(e);
		int instance_id = Integer.parseInt(e.getAttributeValue("instanceId"));

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
	public List<float[]> getRoleCorDatas() {
		return _roleCorDatas;
	}

	@Override
	public List<float[]> getEnemyCorDatas() {
		return _enemyCorDatas;
	}
	
	

}
