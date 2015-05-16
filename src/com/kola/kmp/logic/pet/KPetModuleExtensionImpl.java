package com.kola.kmp.logic.pet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.pet.Pet;
import com.kola.kgame.cache.pet.PetModule;
import com.kola.kgame.cache.pet.PetModuleExtension;
import com.kola.kgame.cache.pet.PetSet;
import com.kola.kmp.logic.other.KTableInfo;
import com.kola.kmp.logic.util.tips.PetTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KPetModuleExtensionImpl implements PetModuleExtension {

	private static final Logger _LOGGER = KGameLogger.getLogger(KPetModuleExtensionImpl.class);
	
	private PetModule _module;
	
	@Override
	public void init(PetModule module, String cfgPath) throws Exception {
		_LOGGER.warn("------{}模块extension收到启动通知！！------", module.getModuleName());
		this._module = module;
		Document doc = XmlUtil.openXml(cfgPath);
		Element root = doc.getRootElement();
		Map<Byte, KTableInfo> tableMap = new HashMap<Byte, KTableInfo>();
		@SuppressWarnings("unchecked")
		List<Element> list = root.getChild("tableConfig").getChildren();
		for(int i = 0; i < list.size(); i++) {
			Element temp = list.get(i);
			KTableInfo tempTable = new KTableInfo(Byte.parseByte(temp.getAttributeValue("type")), temp.getAttributeValue("name"), Integer.parseInt(temp.getAttributeValue("headerIndex")));
			tableMap.put(tempTable.tableType, tempTable);
		}
		KPetModuleManager.loadPetData(root.getChildTextTrim("petDataPath"), tableMap);
		KPetModuleConfig.init(root.getChildTextTrim("commonConfigPath"));
		_LOGGER.warn("------{}模块extension初始化完成！！------", module.getModuleName());
	}

	@Override
	public void onGameWorldInitComplete() throws KGameServerException {
		_LOGGER.warn("------{}模块extension收到游戏世界初始化完成通知！！------", _module.getModuleName());
		KPetModuleManager.onGameWorldInitComplete();
		PetTips.initComplete();
		_LOGGER.warn("------{}模块extension游戏世界初始化完成处理完毕！！------", _module.getModuleName());
	}

	@Override
	public void notifyCacheLoadComplete() throws KGameServerException {
		_LOGGER.warn("------{}模块extension收到缓存数据加载完成通知！！------", _module.getModuleName());
		_LOGGER.warn("------{}模块extension缓存数据加载完成通知处理完毕！！------", _module.getModuleName());
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
		_LOGGER.warn("------{}模块extension收到停机通知！！------", _module.getModuleName());
		_LOGGER.warn("------{}模块extension处理停机通知完毕！！------", _module.getModuleName());
	}

	@Override
	public PetSet newPetSetInstance() {
		return new KPetSet();
	}
	
	@Override
	public Pet newPetInstanceForCache() {
		return new KPet();
	}

}
