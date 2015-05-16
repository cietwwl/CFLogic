package com.kola.kmp.logic.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.util.tips.LevelTips;

public class KLevelTemplate {

	/**
	 * 表示关卡的状态为开放可进入状态
	 */
	public final static byte GAME_LEVEL_STATE_OPEN = 0;

	/**
	 * 表示关卡的状态为未开放状态
	 */
	public final static byte GAME_LEVEL_STATE_NOT_OPEN = 1;
	/**
	 * 表示关卡的状态为隐藏状态
	 */
	public final static byte GAME_LEVEL_STATE_HIDE = 2;

	// 关卡ID
	private int levelId;
	// 关卡名称
	private String levelName;
	// 关卡类型
	private KGameLevelTypeEnum levelType;
	// 关卡所属剧本ID
	private int scenarioId;
	// 关卡描述信息
	private String desc;
	// 关卡在剧本中的编号
	private int levelNumber;
	// 关卡对应客户端显示的图片资源ID
	private int iconResId;
	// 关卡BOSS的图片资源ID
	private int bossIconResId;
	// 关卡产出的道具的图片资源ID
	private int itemIconResId;
	// 关卡推荐战斗力
	private int fightPower;
	// 关卡开启条件
	private KEnterLevelCondition enterCondition;
	// 完成关卡条件
	private KCompleteLevelCondition completeCondition;
	// 关卡奖励数据
	private KLevelReward reward;
	// 关卡中所有的普通战斗场景
	private Map<Integer, KGameBattlefield> allNormalBattlefields = new LinkedHashMap<Integer, KGameBattlefield>();
	// 关卡评级数据，Key：级别（星数），value：
	private Map<Byte, Map<Byte, FightEvaluateData>> fightEvaluateDataMap = new HashMap<Byte, Map<Byte, FightEvaluateData>>();
	// S级评级数据
	public Map<Byte,FightEvaluateData> sLevelFightEvaluateDataMap = new HashMap<Byte,FightEvaluateData>();

	// 后置开放的关卡
	private List<KLevelTemplate> hinderGameLevelList = new ArrayList<KLevelTemplate>();
	// 关卡难度类型，0：普通，1：困难。（当关卡为精英副本关卡时有效）
	public byte difficulty = 0;

	public void init(String tableName, KGameExcelRow xlsRow, KGameLevelTypeEnum levelType) throws KGameServerException {
		setLevelType(levelType);

		setLevelId(xlsRow.getInt("sceneId"));
		setLevelName(xlsRow.getData("name"));

		setIconResId(xlsRow.getInt("copy_icon"));
		setBossIconResId(xlsRow.getInt("boss_icon"));
		if (levelType == KGameLevelTypeEnum.普通关卡) {
			if (xlsRow.getData("dropicon") != null && xlsRow.getData("dropicon").length() > 0) {
				setItemIconResId(xlsRow.getInt("dropicon"));
			}
		}

		setFightPower(xlsRow.getInt("fightpoint"));

		// 进入关卡条件
		int levelLimitJoinCount = xlsRow.getInt("limit_join");
		// 进入该关卡消耗角色的体力值
		int useStamina = xlsRow.getInt("energy_consumption");
		// 该关卡的前置任务Id
		int frontMissionTemplateId = 0;// xlsRow.getInt("");
		// 该关卡的前置关卡Id
		int frontLevelId = 0;
		if (levelType != KGameLevelTypeEnum.好友副本关卡) {
			frontLevelId = xlsRow.getInt("pre_copy");
		}
		if (levelType == KGameLevelTypeEnum.普通关卡 || levelType == KGameLevelTypeEnum.新手引导关卡) {
			setScenarioId(xlsRow.getInt("pre_chapter"));
			setDesc(xlsRow.getData("desc"));
		} else if (levelType == KGameLevelTypeEnum.精英副本关卡) {
			difficulty = xlsRow.getByte("difficulty");
			if (!(difficulty == 0 || difficulty == 1)) {
				throw new KGameServerException("加载表<" + tableName + ">的字段difficulty数据错误，该值只能为0或者1，Row=" + xlsRow.getIndexInFile());
			}
		} else if (levelType == KGameLevelTypeEnum.随从挑战副本关卡||levelType == KGameLevelTypeEnum.高级随从挑战副本关卡) {
			setDesc(xlsRow.getData("desc"));
		} else {
			setDesc("");
		}
		int openRoleLevel = xlsRow.getInt("minLv");
		KEnterLevelCondition enterCon = new KEnterLevelCondition(levelLimitJoinCount, useStamina, frontMissionTemplateId, frontLevelId, openRoleLevel);
		setEnterCondition(enterCon);

		KCompleteLevelCondition completeCon = new KCompleteLevelCondition();
		setCompleteCondition(completeCon);

		// 战士结算评价
		String soldier_copy_passtime = xlsRow.getData("soldier_copy_passtime");
		String soldier_copy_maxhits = xlsRow.getData("soldier_copy_maxhits");
		String soldier_copy_hitby = xlsRow.getData("soldier_copy_hitby");
		initFightEvaluateData(tableName, soldier_copy_passtime, soldier_copy_maxhits, soldier_copy_hitby, xlsRow.getIndexInFile(), KJobTypeEnum.WARRIOR.getJobType());
		// 忍者结算评价
		String ninja_copy_passtime = xlsRow.getData("ninja_copy_passtime");
		String ninja_copy_maxhits = xlsRow.getData("ninja_copy_maxhits");
		String ninja_copy_hitby = xlsRow.getData("ninja_copy_hitby");
		initFightEvaluateData(tableName, ninja_copy_passtime, ninja_copy_maxhits, ninja_copy_hitby, xlsRow.getIndexInFile(), KJobTypeEnum.SHADOW.getJobType());
		// 枪手结算评价
		String gun_copy_passtime = xlsRow.getData("gun_copy_passtime");
		String gun_copy_maxhits = xlsRow.getData("gun_copy_maxhits");
		String gun_copy_hitby = xlsRow.getData("gun_copy_hitby");
		initFightEvaluateData(tableName, gun_copy_passtime, gun_copy_maxhits, gun_copy_hitby, xlsRow.getIndexInFile(), KJobTypeEnum.GUNMAN.getJobType());

		if (levelType != KGameLevelTypeEnum.好友副本关卡) {
			KLevelReward reward = new KLevelReward();
			reward.initKLevelReward(tableName, xlsRow, levelType);
			setReward(reward);
		}

		// if (levelType == KGameLevelTypeEnum.精英副本关卡
		// || levelType == KGameLevelTypeEnum.技术副本关卡) {
		// // 重置副本消耗钻石数量
		// String resetInfo = xlsRow.getData("reset_point");
		// if (resetInfo == null || resetInfo.split(",").length == 0) {
		// throw new KGameServerException("加载表<" + tableName
		// + ">的字段reset_point数据错误，值=" + resetInfo + "，Row="
		// + xlsRow.getIndexInFile());
		// }
		// String[] resetPoint = resetInfo.split(",");
		// for (int j = 0; j < resetPoint.length; j++) {
		// this.enterCondition.getResetUsePointList().add(
		// Integer.parseInt(resetPoint[j]));
		// }
		// }

		// 读取战场数据 ///////////////////
		String[] battleResIdStr = xlsRow.getData("battle_sceneId").split(",");
		String[] battleResPathStr = xlsRow.getData("battle_res_path").split(",");
		if (battleResIdStr == null || battleResIdStr.length <= 0) {
			throw new KGameServerException("加载表<" + tableName + ">的字段battle_sceneId数据错误，值=" + battleResIdStr + "，Row=" + xlsRow.getIndexInFile());
		}

		if (battleResPathStr == null || battleResPathStr.length <= 0) {
			throw new KGameServerException("加载表<" + tableName + ">的字段battle_res_path数据错误，值=" + battleResIdStr + "，Row=" + xlsRow.getIndexInFile());
		}
		String[] battleMusicResStr = xlsRow.getData("music").split(",");
		if (battleMusicResStr == null || battleMusicResStr.length <= 0) {
			throw new KGameServerException("加载表<" + tableName + ">的字段music数据错误，值=" + battleResIdStr + "，Row=" + xlsRow.getIndexInFile());
		}
		if (battleResIdStr.length != battleResPathStr.length || battleResIdStr.length != battleMusicResStr.length) {
			throw new KGameServerException("加载表<" + tableName + ">的字段battle_sceneId与battle_res_path与music数据错误，三者长度不一致，Row=" + xlsRow.getIndexInFile());
		}
		for (int j = 0; j < battleResPathStr.length; j++) {
			int battlefieldId = KGameLevelModuleExtension.getManager().getBattlefieldIdGenerator().nextBattlefieldId();
			KGameBattlefield battle = new KGameBattlefield();
			battle.setBattlefieldId(battlefieldId);
			battle.setBattlefieldType(getBattlefieldTypeByLevelType(levelType));
			battle.setLevelId(this.levelId);
			battle.setBattlefieldSerialNumber(j + 1);
			battle.setBattlefieldResId(Integer.parseInt(battleResIdStr[j]));
			battle.setBgMusicResId(Integer.parseInt(battleMusicResStr[j]));
			if (j == 0) {
				battle.setFirstBattlefield(true);
			} else {
				battle.setFrontBattlefieldSerialNumber(j);
			}
			int nextBattleFieldId = -1;
			if (j == battleResPathStr.length - 1) {
				battle.setLastBattlefield(true);
			} else {
				nextBattleFieldId = battlefieldId - 1;
				battle.setNextBattleFieldId(nextBattleFieldId);
			}
			battle.initBattlefield(tableName, battleResPathStr[j], nextBattleFieldId, xlsRow.getIndexInFile());
			allNormalBattlefields.put(battle.getBattlefieldId(), battle);
			if (levelType == KGameLevelTypeEnum.普通关卡 || levelType == KGameLevelTypeEnum.新手引导关卡) {
				KGameLevelModuleExtension.getManager().allKGameBattlefield.put(battle.getBattlefieldId(), battle);
			} else if(levelType == KGameLevelTypeEnum.精英副本关卡 || levelType == KGameLevelTypeEnum.技术副本关卡) {
				KGameLevelModuleExtension.getManager().getCopyManager().allKGameBattlefield.put(battle.getBattlefieldId(), battle);
			} else if(levelType == KGameLevelTypeEnum.随从挑战副本关卡 ) {
				KGameLevelModuleExtension.getManager().getPetChallengeCopyManager().allKGameBattlefield.put(battle.getBattlefieldId(), battle);
			} else if(levelType == KGameLevelTypeEnum.高级随从挑战副本关卡 ) {
				KGameLevelModuleExtension.getManager().getKSeniorPetChallengeCopyManager().allKGameBattlefieldTemplate.put(battle.getBattlefieldId(), battle);
			}
		}
	}

	public void initFightEvaluateData(String tableName, String copy_passtime, String copy_maxhits, String copy_hitby, int index, byte job) throws KGameServerException {
		if (copy_passtime == null || copy_passtime.split(",").length != 4) {
			throw new KGameServerException("初始化表<" + tableName + ">的字段<copy_passtime>错误：，excel行数：" + index + ",值：" + copy_passtime);
		}
		if (copy_maxhits == null || copy_maxhits.split(",").length != 4) {
			throw new KGameServerException("初始化表<" + tableName + ">的字段<copy_maxhits>错误：，excel行数：" + index + ",值：" + copy_maxhits);
		}
		if (copy_hitby == null || copy_hitby.split(",").length != 4) {
			throw new KGameServerException("初始化表<" + tableName + ">的字段<copy_hitby>错误：，excel行数：" + index + ",值：" + copy_hitby);
		}
		// FightEvaluateData data0 = new FightEvaluateData();
		// data0.fightLv = (byte) 0;
		// data0.fightTime = 0;
		// data0.maxHitCount = 0;
		// data0.hitByCount = 0;
		// this.fightEvaluateDataMap.put(data0.fightLv, data0);

		String[] passtimeStr = copy_passtime.split(",");
		String[] maxhitStr = copy_maxhits.split(",");
		String[] hitbyStr = copy_hitby.split(",");
		int lv = hitbyStr.length + 1;
		for (byte i = 0; i < hitbyStr.length; i++, lv--) {
			FightEvaluateData data = new FightEvaluateData();
			data.fightLv = (byte) (lv);
			data.fightTime = Integer.parseInt(passtimeStr[i]);
			data.maxHitCount = Integer.parseInt(maxhitStr[i]);
			data.hitByCount = Integer.parseInt(hitbyStr[i]);
			if (!this.fightEvaluateDataMap.containsKey(job)) {
				this.fightEvaluateDataMap.put(job, new LinkedHashMap<Byte, FightEvaluateData>());
			}
			this.fightEvaluateDataMap.get(job).put(data.fightLv, data);
			if(data.fightLv == FightEvaluateData.MAX_FIGHT_LEVEL){
				sLevelFightEvaluateDataMap.put(job, data);
			}
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

	public int getScenarioId() {
		return scenarioId;
	}

	public void setScenarioId(int scenarioId) {
		this.scenarioId = scenarioId;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
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

	public KLevelReward getReward() {
		return reward;
	}

	public void setReward(KLevelReward reward) {
		this.reward = reward;
	}

	public Map<Integer, KGameBattlefield> getNormalBattlefieldMap() {
		return allNormalBattlefields;
	}

	public List<KGameBattlefield> getAllNormalBattlefields() {
		List<KGameBattlefield> list = new ArrayList<KGameBattlefield>();
		list.addAll(allNormalBattlefields.values());
		return list;
	}

	public void setAllNormalBattlefields(Map<Integer, KGameBattlefield> allNormalBattlefields) {
		this.allNormalBattlefields = allNormalBattlefields;
	}

	public Map<Byte, FightEvaluateData> getFightEvaluateDataMap(byte job) {
		return fightEvaluateDataMap.get(job);
	}

	// public void setFightEvaluateDataMap(
	// Map<Byte, FightEvaluateData> fightEvaluateDataMap) {
	// this.fightEvaluateDataMap = fightEvaluateDataMap;
	// }

	public List<KLevelTemplate> getHinderGameLevelList() {
		return hinderGameLevelList;
	}

	public void setHinderGameLevelList(List<KLevelTemplate> hinderGameLevelList) {
		this.hinderGameLevelList = hinderGameLevelList;
	}

	/**
	 * 获取关卡在剧本中的编号
	 * 
	 * @return
	 */
	public int getLevelNumber() {
		return levelNumber;
	}

	/**
	 * 设置关卡在剧本中的编号
	 * 
	 * @return
	 */
	public void setLevelNumber(int levelNumber) {
		this.levelNumber = levelNumber;
	}

	public int getIconResId() {
		return iconResId;
	}

	public void setIconResId(int iconResId) {
		this.iconResId = iconResId;
	}

	public int getFightPower() {
		return fightPower;
	}

	public void setFightPower(int fightPower) {
		this.fightPower = fightPower;
	}

	public int getBossIconResId() {
		return bossIconResId;
	}

	public void setBossIconResId(int bossIconResId) {
		this.bossIconResId = bossIconResId;
	}

	public int getItemIconResId() {
		return itemIconResId;
	}

	public void setItemIconResId(int itemIconResId) {
		this.itemIconResId = itemIconResId;
	}
	
	public String[] getSLevelFightEvaluateDataTips(byte job){
		FightEvaluateData data = sLevelFightEvaluateDataMap.get(job);
		if(data == null){
			return new String[]{"",""};
		}else{
			return new String[]{LevelTips.getTipsSLevelbeHitCount(data.hitByCount),LevelTips.getTipsSLevelUseTime(data.fightTime * 1000)};
		}		
	}
	
	public KGameBattlefieldTypeEnum getBattlefieldTypeByLevelType(KGameLevelTypeEnum levelType) {
		switch (levelType) {
		case 普通关卡:
			return KGameBattlefieldTypeEnum.普通关卡战场;
		case 精英副本关卡:
			return KGameBattlefieldTypeEnum.精英副本战场;
		case 技术副本关卡:
			return KGameBattlefieldTypeEnum.技术副本战场;
		case 好友副本关卡:
			return KGameBattlefieldTypeEnum.好友副本战场;
		case 新手引导关卡:
			return KGameBattlefieldTypeEnum.新手引导战场;
		case 随从挑战副本关卡:
			return KGameBattlefieldTypeEnum.随从挑战副本战场;
		case 高级随从挑战副本关卡:
			return KGameBattlefieldTypeEnum.高级随从挑战副本战场;
		case 爬塔副本关卡:
			return KGameBattlefieldTypeEnum.爬塔副本战场;
		default:
			return KGameBattlefieldTypeEnum.普通关卡战场;
		}
	}

}
