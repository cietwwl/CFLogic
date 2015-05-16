package com.kola.kmp.logic.talent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.GameModule;
import com.kola.kmp.logic.other.KTableInfo;

/**
 * 
 * @author PERRY CHAN
 */
public class KTalentModule implements GameModule {

	private String _moduleName;
	
	private Map<Byte, KTableInfo> loadTableConfigMap(Element element) {
		Map<Byte, KTableInfo> map = new HashMap<Byte, KTableInfo>();
		@SuppressWarnings("unchecked")
		List<Element> children = element.getChildren("table");
		KTableInfo tableInfo;
		Element child;
		for(int i = 0; i < children.size(); i++) {
			child = children.get(i);
			tableInfo = new KTableInfo(Byte.parseByte(child.getAttributeValue("type")), child.getAttributeValue("name"), Integer.parseInt(child.getAttributeValue("headerIndex")));
			map.put(tableInfo.tableType, tableInfo);
		}
		return map;
	}
	
	@Override
	public void init(String cfgPath, String pModuleName, int pMsgLowerId, int pMsgUpperId) throws Exception {
		this._moduleName = pModuleName;
		Document doc = XmlUtil.openXml(cfgPath);
		Element root = doc.getRootElement();
		Map<Byte, KTableInfo> map = loadTableConfigMap(root.getChild("tableConfig"));
		KTalentModuleManager.loadData(root.getChildTextTrim("talentDataPath"), map);
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		KTalentModuleManager.onGameWorldInitComplete();
	}

	@Override
	public void notifyCacheLoadComplete() throws KGameServerException {
		
	}

	@Override
	public boolean messageReceived(KGameMessageEvent msgEvent) throws KGameServerException {
		return false;
	}

	@Override
	public void exceptionCaught(KGameServerException ex) {
		
	}

	@Override
	public void serverShutdown() throws KGameServerException {
		
	}

	@Override
	public String getModuleName() {
		return _moduleName;
	}

}
