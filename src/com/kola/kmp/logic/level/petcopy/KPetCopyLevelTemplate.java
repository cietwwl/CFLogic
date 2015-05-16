package com.kola.kmp.logic.level.petcopy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.db.names.TableName;

import com.koala.game.exception.KGameServerException;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.level.FightEvaluateData;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.ObstructionData;
import com.kola.kmp.logic.level.KCompleteLevelCondition;
import com.kola.kmp.logic.level.KEnterLevelCondition;
import com.kola.kmp.logic.level.petcopy.KPetCopyBattlefield.KPetCopyBattlefieldDropData;
import com.kola.kmp.logic.level.tower.KTowerLevelTemplate;
import com.kola.kmp.logic.other.KCurrencyTypeEnum;
import com.kola.kmp.logic.other.KGameLevelTypeEnum;
import com.kola.kmp.logic.other.KJobTypeEnum;
import com.kola.kmp.logic.other.KDataStructs.ItemCountStruct;
import com.kola.kmp.logic.support.KSupportFactory;

public class KPetCopyLevelTemplate {
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

	public byte levelNum;
	// 关卡推荐战斗力
	private int fightPower;
	// 关卡开启条件
	private KEnterLevelCondition enterCondition;
	// 完成关卡条件
	private KCompleteLevelCondition completeCondition;

	// 关卡评级数据，Key：级别（星数），value：
	private Map<Byte, Map<Byte, FightEvaluateData>> fightEvaluateDataMap = new HashMap<Byte, Map<Byte, FightEvaluateData>>();

	// 后置开放的关卡
	private List<KPetCopyLevelTemplate> hinderGameLevelList = new ArrayList<KPetCopyLevelTemplate>();
	// 随从副本掉落方案表，Key：掉落方案ID，Value：掉落方案（打开笼子的掉落）
	private Map<Integer, KPetCopyBattlefieldDropTemplate> dropTemplateMap = new LinkedHashMap<Integer, KPetCopyBattlefieldDropTemplate>();
	// 随从副本掉落方案表，Key：累加权重值，Value：掉落方案（打开笼子的掉落）
	private Map<Integer, KPetCopyBattlefieldDropTemplate> dropTemplateMapByWight = new LinkedHashMap<Integer, KPetCopyBattlefieldDropTemplate>();
	private List<KPetCopyBattlefieldDropTemplate> dropTemplateList = new ArrayList<KPetCopyBattlefieldDropTemplate>();
	// 总权重
	public int totalDropRate;
	// 必定出现的掉落方案（保底）
	private List<KPetCopyBattlefieldDropTemplate> defaultDropTemplateList = new ArrayList<KPetCopyBattlefieldDropTemplate>();
	// 随从副本战场模版数据
	private KPetCopyBattlefield _battlefieldTemplate;
	// 随从副本关卡结算数据表，Key：角色等级，Value：等级对应奖励数据
	private Map<Integer, KPetCopyReward> rewardMap = new LinkedHashMap<Integer, KPetCopyReward>();
	// 关卡UI显示的掉落道具
	public List<ItemCountStruct> itemRewardShowList = new ArrayList<ItemCountStruct>();
	// 关卡UI显示的掉落道具几率
	public List<Byte> itemRewardShowDropRateList = new ArrayList<Byte>();

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
		if (levelType == KGameLevelTypeEnum.随从副本关卡) {
			setDesc(xlsRow.getData("priceTips"));
		}
		short openRoleLevel = xlsRow.getShort("minlvl");
		KEnterLevelCondition enterCon = new KEnterLevelCondition(
				levelLimitJoinCount, useStamina, frontMissionTemplateId,
				frontLevelId, openRoleLevel);
		setEnterCondition(enterCon);

		KCompleteLevelCondition completeCon = new KCompleteLevelCondition();
		setCompleteCondition(completeCon);

		// 战士结算评价
		String soldier_copy_passtime = xlsRow.getData("soldier_copy_passtime");
		String soldier_copy_maxhits = xlsRow.getData("soldier_copy_maxhits");
		String soldier_copy_hitby = xlsRow.getData("soldier_copy_hitby");
		initFightEvaluateData(tableName, soldier_copy_passtime,
				soldier_copy_maxhits, soldier_copy_hitby,
				xlsRow.getIndexInFile(), KJobTypeEnum.WARRIOR.getJobType());
		// 忍者结算评价
		String ninja_copy_passtime = xlsRow.getData("ninja_copy_passtime");
		String ninja_copy_maxhits = xlsRow.getData("ninja_copy_maxhits");
		String ninja_copy_hitby = xlsRow.getData("ninja_copy_hitby");
		initFightEvaluateData(tableName, ninja_copy_passtime,
				ninja_copy_maxhits, ninja_copy_hitby, xlsRow.getIndexInFile(),
				KJobTypeEnum.SHADOW.getJobType());
		// 枪手结算评价
		String gun_copy_passtime = xlsRow.getData("gun_copy_passtime");
		String gun_copy_maxhits = xlsRow.getData("gun_copy_maxhits");
		String gun_copy_hitby = xlsRow.getData("gun_copy_hitby");
		initFightEvaluateData(tableName, gun_copy_passtime, gun_copy_maxhits,
				gun_copy_hitby, xlsRow.getIndexInFile(),
				KJobTypeEnum.GUNMAN.getJobType());

		String showItemStr = xlsRow.getData("soldier_itemdisplay");
		decodeShowItemString(tableName, "soldier_itemdisplay", showItemStr,
				xlsRow.getIndexInFile());

		String battlefieldPath = xlsRow.getData("battle_res_path");
		int battleMusicId = xlsRow.getInt("music");
		long battleTime = xlsRow.getLong("DieTime") * 1000;
		_battlefieldTemplate = new KPetCopyBattlefield();
		_battlefieldTemplate.initBattlefield(levelId, tableName,
				battlefieldPath, battleMusicId, battleTime,
				xlsRow.getIndexInFile());

	}

	public void initFightEvaluateData(String tableName, String copy_passtime,
			String copy_maxhits, String copy_hitby, int index, byte job)
			throws KGameServerException {
		if (copy_passtime == null || copy_passtime.split(",").length != 4) {
			throw new KGameServerException("初始化表<" + tableName
					+ ">的字段<copy_passtime>错误：，excel行数：" + index + ",值："
					+ copy_passtime);
		}
		if (copy_maxhits == null || copy_maxhits.split(",").length != 4) {
			throw new KGameServerException("初始化表<" + tableName
					+ ">的字段<copy_maxhits>错误：，excel行数：" + index + ",值："
					+ copy_maxhits);
		}
		if (copy_hitby == null || copy_hitby.split(",").length != 4) {
			throw new KGameServerException("初始化表<" + tableName
					+ ">的字段<copy_hitby>错误：，excel行数：" + index + ",值："
					+ copy_hitby);
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
				this.fightEvaluateDataMap.put(job,
						new LinkedHashMap<Byte, FightEvaluateData>());
			}
			this.fightEvaluateDataMap.get(job).put(data.fightLv, data);
		}
	}

	private List<ItemCountStruct> decodeShowItemString(String tableName,
			String fieldName, String data, int index)
			throws KGameServerException {
		List<ItemCountStruct> list = new ArrayList<ItemCountStruct>();
		String[] itemCodes = data.split(",");
		for (int i = 0; i < itemCodes.length; i++) {
			String[] codes = itemCodes[i].split("\\*");
			if (codes == null || codes.length != 2) {
				throw new KGameServerException("初始化表<" + tableName + ">的道具错误："
						+ itemCodes[i] + "，字段：" + fieldName + "=" + data
						+ "，excel行数：" + index);
			}
			if (KSupportFactory.getItemModuleSupport()
					.getItemTemplate(codes[0]) == null) {
				throw new KGameServerException("初始化表<" + tableName
						+ ">的道具错误，找不到道具类型：" + itemCodes[i] + "，字段：" + fieldName
						+ "=" + data + "，excel行数：" + index);
			}
			itemRewardShowList.add(new ItemCountStruct(codes[0], 1));
			byte rate = Byte.parseByte(codes[1]);
			itemRewardShowDropRateList.add(rate);
		}
		return list;
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

	public KPetCopyBattlefield getPetBattlefieldTemplate() {
		return _battlefieldTemplate;
	}

	public Map<Integer, KPetCopyReward> getRewardMap() {
		return rewardMap;
	}

	public Map<Byte, FightEvaluateData> getFightEvaluateDataMap(byte job) {
		return fightEvaluateDataMap.get(job);
	}

	public List<KPetCopyLevelTemplate> getHinderGameLevelList() {
		return hinderGameLevelList;
	}

	public void addKPetCopyBattlefieldDropTemplate(
			KPetCopyBattlefieldDropTemplate template) {

		this.dropTemplateList.add(template);
		this.totalDropRate += template.appearWeight;
		int weightKey = this.totalDropRate;
		this.dropTemplateMapByWight.put(weightKey, template);
		this.dropTemplateMap.put(template.dropId, template);
		if (template.isDefault) {
			this.defaultDropTemplateList.add(template);
		}
	}

	public KPetCopyBattlefield getBattlefieldTemplate() {
		return _battlefieldTemplate;
	}

	public Map<Integer, KPetCopyBattlefieldDropTemplate> getDropTemplateMap() {
		return dropTemplateMap;
	}

	public Map<Integer, KPetCopyBattlefieldDropTemplate> getDropTemplateMapByWight() {
		return dropTemplateMapByWight;
	}

	/**
	 * 创建随机掉落笼子的随从副本战场
	 * 
	 * @param multiple
	 * @return
	 */
	public KPetCopyBattlefield createPetCopyBattlefield(int multiple) {
		KPetCopyBattlefield battlefield = new KPetCopyBattlefield();
		battlefield.battlefieldId = _battlefieldTemplate.battlefieldId;
		battlefield.battlefieldResId = _battlefieldTemplate.battlefieldResId;
		battlefield.battlefieldType = _battlefieldTemplate.battlefieldType;
		battlefield.battlePathName = _battlefieldTemplate.battlePathName;
		battlefield.bgMusicResId = _battlefieldTemplate.bgMusicResId;
		battlefield.bornPoint = _battlefieldTemplate.bornPoint;
		battlefield.isInitOK = _battlefieldTemplate.isInitOK;
		battlefield.levelId = _battlefieldTemplate.levelId;
		battlefield.battleTimeMillis = _battlefieldTemplate.battleTimeMillis;

		battlefield.sectionPointDataList
				.addAll(_battlefieldTemplate.sectionPointDataList);
		battlefield.allWaveInfo.addAll(_battlefieldTemplate.allWaveInfo);
		battlefield.monsterMap.putAll(_battlefieldTemplate.monsterMap);
		battlefield.obstructionMap.putAll(_battlefieldTemplate.obstructionMap);

		int index = 0;
		int maxIndex = dropTemplateMapByWight.size() - 1;
		boolean isDefault = defaultDropTemplateList.size() > 0;
		boolean isAppearDefault = false;

		for (ObstructionData obstruction : battlefield.obstructionMap.values()) {
			if (index == maxIndex && isDefault && !isAppearDefault) {
				KPetCopyBattlefieldDropTemplate dropTemplate = defaultDropTemplateList
						.get(defaultDropTemplateList.size());
				battlefield.dropMap.put(obstruction,
						getKPetCopyBattlefieldDropData(dropTemplate, multiple));
			} else {
				int weight = UtilTool.random(totalDropRate);
				int targetWeight = 0;
				for (Integer dropWeight : dropTemplateMapByWight.keySet()) {
					if (weight >= dropWeight) {
						targetWeight = dropWeight;
					}
				}
				KPetCopyBattlefieldDropTemplate dropTemplate = dropTemplateMapByWight
						.get(targetWeight);
				if (dropTemplate == null) {
					dropTemplate = dropTemplateList.get(UtilTool.random(0,
							maxIndex));
				}
				if (dropTemplate.isDefault) {
					isAppearDefault = true;
				}
				battlefield.dropMap.put(obstruction,
						getKPetCopyBattlefieldDropData(dropTemplate, multiple));
			}
		}

		return battlefield;
	}

	private KPetCopyBattlefieldDropData getKPetCopyBattlefieldDropData(
			KPetCopyBattlefieldDropTemplate dropTemplate, int multiple) {
		KPetCopyBattlefieldDropData data = new KPetCopyBattlefieldDropData();
		data.dropId = dropTemplate.dropId;
		data.resId = dropTemplate.resId;
		data.dropType = dropTemplate.dropType;
		switch (dropTemplate.dropType) {
		case MONSTER:
			data.monsterMap.putAll(dropTemplate.monsterMap);
			break;
		case ITEM:
			data.itemMap.putAll(dropTemplate.caculateItemReward(multiple));
			break;
		case CURRENCY:
			data.currencyMap.put(KCurrencyTypeEnum.GOLD, dropTemplate.dropGold);
			break;
		}
		return data;
	}
}
