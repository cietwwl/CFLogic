package com.kola.kmp.logic.level;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.util.KGameExcelTable.KGameExcelRow;


public class KGameScenario {
	// // 剧本ID
	private int scenarioId;
	// 剧本名字
	private String scenarioName;
	// 剧本背景图片资源Id
	private int scenarioBgResId;
	// 剧本描述
	private String scenarioDesc;
	// 表示章节数目（从1开始）(add 2014-1-4)
	private short chapterId;
	//章节剧本奖励
	private KGameScenarioReward reward;
	//章节剧本奖励
	private KGameScenarioReward s_reward;
	
	//章节剧本可进入最小角色等级
	private int minRoleLv;
	
	//章节剧本推荐角色等级
	private int fitRoleLv;
	//章节对应主城ID
	private int mapId;
	//前置章节ID
	private int frontScenarioId;

	// 本剧本的所有关卡
	private Map<Integer, KLevelTemplate> allGameLevel = new LinkedHashMap<Integer, KLevelTemplate>();
	
	public void initKGameScenario(KGameExcelRow excelData) throws Exception {
		
	}

	public int getScenarioId() {
		return scenarioId;
	}

	public void setScenarioId(int scenarioId) {
		this.scenarioId = scenarioId;
	}

	public String getScenarioName() {
		return scenarioName;
	}

	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	public int getScenarioBgResId() {
		return scenarioBgResId;
	}

	public void setScenarioBgResId(int scenarioBgResId) {
		this.scenarioBgResId = scenarioBgResId;
	}

	public String getScenarioDesc() {
		return scenarioDesc;
	}

	public void setScenarioDesc(String scenarioDesc) {
		this.scenarioDesc = scenarioDesc;
	}

	public KGameScenarioReward getReward() {
		return reward;
	}

	public void setReward(KGameScenarioReward reward) {
		this.reward = reward;
	}

	public int getMinRoleLv() {
		return minRoleLv;
	}

	public void setMinRoleLv(int minRoleLv) {
		this.minRoleLv = minRoleLv;
	}

	public int getFitRoleLv() {
		return fitRoleLv;
	}

	public void setFitRoleLv(int fitRoleLv) {
		this.fitRoleLv = fitRoleLv;
	}

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public int getFrontScenarioId() {
		return frontScenarioId;
	}

	public void setFrontScenarioId(int frontScenarioId) {
		this.frontScenarioId = frontScenarioId;
	}
	
	/**
	 * 增加一个关卡
	 * 
	 * @param level
	 */
	public void addKGameLevel(KLevelTemplate level) {
		this.allGameLevel.put(level.getLevelId(), level);
	}

	/**
	 * 获取本剧本的所有关卡
	 * 
	 * @return
	 */
	public List<KLevelTemplate> getAllGameLevel() {
		return new ArrayList<KLevelTemplate>(allGameLevel.values());
	}

	/**
	 * 根据关卡Id获取本剧本的某个关卡
	 * 
	 * @param levelId
	 * @return
	 */
	public KLevelTemplate getKGameLevel(int levelId) {
		return allGameLevel.get(levelId);
	}

	public short getChapterId() {
		return chapterId;
	}

	public void setChapterId(short chapterId) {
		this.chapterId = chapterId;
	}

	public KGameScenarioReward getS_reward() {
		return s_reward;
	}

	public void setS_reward(KGameScenarioReward s_reward) {
		this.s_reward = s_reward;
	}


	
	
	
}
