package com.kola.kmp.logic.level.tower;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.level.FightEvaluateData;
import com.kola.kmp.logic.level.KCompleteLevelCondition;
import com.kola.kmp.logic.level.KEnterLevelCondition;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.reward.KRewardDataStruct.BaseRewardData;

public class KTowerLevelTemplate {

	// 关卡ID
	private int levelId;
	// 关卡名称
	private String levelName;
	// 关卡类型
	private KGameLevelTypeEnum levelType;
	// 关卡描述信息
	private String desc;
	// 关卡对应客户端显示的图片资源ID
	private int iconResId;
	// 关卡BOSS的图片资源ID
	private int bossIconResId;
	
	private byte levelNum;
	// 关卡推荐战斗力
	private int fightPower;
	// 关卡开启条件
	private KEnterLevelCondition enterCondition;
	// 完成关卡条件
	private KCompleteLevelCondition completeCondition;
	// 塔防战场数据
	private KTowerBattlefield towerBattlefield;
	// 关卡评级数据，Key：级别（星数），value：
	private Map<Byte, Integer> fightEvaluateDataMap = new LinkedHashMap<Byte, Integer>();
	
	// 后置开放的关卡
	private List<KTowerLevelTemplate> hinderGameLevelList = new ArrayList<KTowerLevelTemplate>();
	
	private Map<Integer,ItemCountStruct> wavePriceBoxMap = new LinkedHashMap<Integer, ItemCountStruct>();

	public void init(String tableName, KGameExcelRow xlsRow,
			KGameLevelTypeEnum levelType) throws KGameServerException {
		setLevelType(levelType);

		setLevelId(xlsRow.getInt("dungeonid"));
		setLevelName(xlsRow.getData("dungeonname"));

		setIconResId(xlsRow.getInt("copy_icon"));
		setBossIconResId(xlsRow.getInt("boss_icon"));

		setFightPower(xlsRow.getInt("fightpoint"));

		// 进入关卡条件
		int levelLimitJoinCount = xlsRow.getInt("limit_join");
		// 进入该关卡消耗角色的体力值
		int useStamina = xlsRow.getInt("energy_consumption");
		// 该关卡的前置任务Id
		int frontMissionTemplateId = 0;// xlsRow.getInt("");
		// 该关卡的前置关卡Id
		int frontLevelId = 0;
		if (levelType == KGameLevelTypeEnum.好友副本关卡) {
			setDesc(xlsRow.getData("priceTips"));
		}
		short openRoleLevel = xlsRow.getShort("minlvl");
		KEnterLevelCondition enterCon = new KEnterLevelCondition(
				levelLimitJoinCount, useStamina, frontMissionTemplateId,
				frontLevelId, openRoleLevel);
		setEnterCondition(enterCon);

		KCompleteLevelCondition completeCon = new KCompleteLevelCondition();
		setCompleteCondition(completeCon);

		String copy_passtime = xlsRow.getData("copy_passtime");
		initFightEvaluateData(tableName, copy_passtime, xlsRow.getIndexInFile());
	}

	public void initFightEvaluateData(String tableName, String copy_passtime, int index)
			throws KGameServerException {
		if (copy_passtime == null || copy_passtime.split(",").length != 4) {
			throw new KGameServerException("初始化表<" + tableName
					+ ">的字段<copy_passtime>错误：，excel行数：" + index + ",值："
					+ copy_passtime);
		}
		
//		FightEvaluateData data0 = new FightEvaluateData();
//		this.fightEvaluateDataMap.put(data0.fightLv, 1);

		String[] passtimeStr = copy_passtime.split(",");
		byte lv = (byte)(passtimeStr.length + 1);
		for (byte i = 0; i < passtimeStr.length; i++, lv--) {
			this.fightEvaluateDataMap.put(lv, Integer.parseInt(passtimeStr[i]));
		}
	}

	public int getLevelId() {
		return levelId;
	}

	public void setLevelId(int levelId) {
		this.levelId = levelId;
	}

	public String getLevelName() {
		return levelName;
	}

	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}

	public KGameLevelTypeEnum getLevelType() {
		return levelType;
	}

	public void setLevelType(KGameLevelTypeEnum levelType) {
		this.levelType = levelType;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getIconResId() {
		return iconResId;
	}

	public void setIconResId(int iconResId) {
		this.iconResId = iconResId;
	}

	public int getBossIconResId() {
		return bossIconResId;
	}

	public void setBossIconResId(int bossIconResId) {
		this.bossIconResId = bossIconResId;
	}

	public int getFightPower() {
		return fightPower;
	}

	public void setFightPower(int fightPower) {
		this.fightPower = fightPower;
	}

	public KEnterLevelCondition getEnterCondition() {
		return enterCondition;
	}

	public void setEnterCondition(KEnterLevelCondition enterCondition) {
		this.enterCondition = enterCondition;
	}

	public KCompleteLevelCondition getCompleteCondition() {
		return completeCondition;
	}

	public void setCompleteCondition(KCompleteLevelCondition completeCondition) {
		this.completeCondition = completeCondition;
	}

	public KTowerBattlefield getTowerBattlefield() {
		return towerBattlefield;
	}

	public void setTowerBattlefield(KTowerBattlefield towerBattlefield) {
		this.towerBattlefield = towerBattlefield;
	}

	public Map<Byte, Integer> getFightEvaluateDataMap() {
		return fightEvaluateDataMap;
	}

	public Map<Integer, ItemCountStruct> getWavePriceBoxMap() {
		return wavePriceBoxMap;
	}

	public List<KTowerLevelTemplate> getHinderGameLevelList() {
		return hinderGameLevelList;
	}

	public byte getLevelNum() {
		return levelNum;
	}

	public void setLevelNum(byte levelNum) {
		this.levelNum = levelNum;
	}
	
	
	

}
