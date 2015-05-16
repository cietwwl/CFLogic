package com.kola.kmp.logic;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.communication.KGameMessageEvent;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.GameModule;
import com.kola.kmp.logic.actionrecord.KActionRecorder;
import com.kola.kmp.logic.combat.impl.KCombatModule;
import com.kola.kmp.logic.reward.redress.TWLevelMountPatch;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.EnumStrTool;
import com.kola.kmp.logic.util.tips.KTipsModule;
import com.kola.kmp.logic.util.tips.RoleTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KGlobalModule implements GameModule {

	private static final Logger _LOGGER = KGameLogger.getLogger(KGlobalModule.class);
	private String _moduleName;
	
	private static final String _GAME_VERSION_YY = "ios_yy";
	private static final String _GAME_VERSION_APPSTORE = "ios_appstore";
	private static final String _GAME_VERSION_ANDROID = "android";
	
	private static final String _GAME_DATA_PATH_YY = "./res/gamedata_yy";
	private static final String _GAME_DATA_PATH_APPSTORE = "./res/gamedata_appstore";
	private static final String _GAME_DATA_PATH_ANDROID = "./res/gamedata_android";
	
	private static final String _GAME_DATA_PATH = "./res/gamedata";
	
	static {
		String osName = System.getProperty("os.name");
		if (osName != null && osName.toLowerCase().startsWith("windows")) {
			File file = new File(_GAME_DATA_PATH);
			if (!file.exists()) {
				file.mkdir();
			}
			String gameVersion = System.getProperty("gameVersion");
			if (gameVersion != null) {
				String sourcePath = null;
				if (gameVersion.equals(_GAME_VERSION_YY)) {
					sourcePath = _GAME_DATA_PATH_YY;
				} else if (gameVersion.equals(_GAME_VERSION_APPSTORE)) {
					sourcePath = _GAME_DATA_PATH_APPSTORE;
				} else if (gameVersion.equals(_GAME_VERSION_ANDROID)) {
					sourcePath = _GAME_DATA_PATH_ANDROID;
				}
				if (sourcePath != null) {
					try {
						copyFileToGameData(sourcePath, new File(sourcePath), _GAME_DATA_PATH);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private static void copyFileToGameData(String srcRootPath, File srcFile, String targetRoot) throws Exception {
		if (srcFile.isDirectory()) {
			File[] allFiles = srcFile.listFiles();
			File temp;
			File targetTemp;
			for (int i = 0; i < allFiles.length; i++) {
				temp = allFiles[i];
				if (temp.isDirectory()) {
					targetTemp = new File(targetRoot + File.separator + temp.getName());
					if(!targetTemp.exists()) {
						targetTemp.mkdir();
					}
					copyFileToGameData(srcRootPath, temp, targetTemp.getAbsolutePath());
				} else {
					copyFile(temp, srcRootPath, targetRoot);
				}
			}
		} else {
			copyFile(srcFile, srcRootPath, targetRoot);
		}
	}
	
	private static void copyFile(File srcFile, String srcRootPath, String targetDir) throws Exception {
		FileInputStream fis = new FileInputStream(srcFile);
		FileOutputStream fos = new FileOutputStream(targetDir + File.separator + srcFile.getName());
		byte[] array = new byte[1024];
		int length;
		while ((length = fis.read(array)) > 0) {
			fos.write(array, 0, length);
		}
		fis.close();
		fos.flush();
		fos.close();
	}
	
	@Override
	public void init(String cfgPath, String pModuleName, int pMsgLowerId, int pMsgUpperId) throws Exception {
		_LOGGER.info("---- {}模块初始化开始 ----", pModuleName);
		_moduleName = pModuleName;
		Document doc = XmlUtil.openXml(cfgPath);
		Element root = doc.getRootElement();
		KGameGlobalConfig.init(root.getChildTextTrim("globalConfigPath"));
		KGameGlobalConfig.loadGlobalConfig(root.getChild("globalConfig"));
		EnumStrTool.loadEnumStrForGlobalModuleInit(root.getChildTextTrim("enumStrXml"), root.getChildTextTrim("enumStrXls"));
		KTipsModule.init(root.getChildTextTrim("logicTipsXml"), root.getChildTextTrim("logicTipsXls"));
		KSupportFactory.init(root.getChildTextTrim("supportConfigPath"));
		KCombatModule.init(root.getChildTextTrim("combatConfigPath"));
		KNotificationCenter.loadNotification(root.getChildTextTrim("notificationPath"));
		KActionRecorder.init();
		_LOGGER.info("---- {}模块初始化结束 ----", pModuleName);
	}

	@Override
	public void onGameWorldInitComplete() {
		RoleTips.onGameWorldInitComplete();
	}

	@Override
	public void notifyCacheLoadComplete() throws KGameServerException {
		KCombatModule.onGameWorldInitComplete();
		KGameGlobalConfig.onGameWorldInitComplete();
		
		// ===========================================
		// 以下是运行时统一执行的脚本
		_LOGGER.error("---------------->>>>>>>>>>> 执行补丁：{} 作用：{}", TWLevelMountPatch.class.getName(), "定时扫描在线角色，已进行V2补偿，未有指定机甲，背包、邮件不含指定机甲礼包，赠送指定机甲");
		new TWLevelMountPatch().run("start");
		//
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
		KActionRecorder.shutdown();
	}

	@Override
	public String getModuleName() {
		return _moduleName;
	}

}
