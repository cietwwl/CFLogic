package com.kola.kmp.logic.map;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.RunTimeTask;

public class ReloadMapActivityPatch implements RunTimeTask {
	private static final Logger _LOGGER = KGameLogger.getLogger(ReloadMapActivityPatch.class);
	@Override
	public String run(String args) {
		
		KNormalMapActivityManager manager = KGameMapManager.normalMapActivityManager;
		
		try {
			KGameMapManager.reloadNormalMapActivityData();
		} catch (Exception e) {
			_LOGGER.error(e.getMessage(), e);
			KGameMapManager.normalMapActivityManager = manager;
			return "ReloadMapActivityPatch异常="+e.getMessage();
		}
		
		return "执行成功";
	}

}
