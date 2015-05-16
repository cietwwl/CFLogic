package com.kola.kmp.logic.mission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;


/**
 * 表示任务的触发条件
 * @author zhaizl
 *
 */
public class MissionCompleteCondition {
	
	public boolean isTaskTask;
	public boolean isKillMonsterTask;
	public boolean isCollectItemTask;
	public boolean isGameLeveTask;
	public boolean isUseItemTask;
	public boolean isUseFunctionTask;
	public boolean isAttributeTask;
	public boolean isBattlefieldTask;
	public boolean isAnswerQuestionTask;
	public boolean isUpgradeFunLvTask;
	
	private Map<Integer,KillMonsterTask> killMonsterTaskMap = new HashMap<Integer,KillMonsterTask>();
	private CollectItemTask collectItemTask;
	private GameLevelTask gameLevelTask;
	private UseItemTask useItemTask;
	private UseFunctionTask useFunctionTask;
	private AttributeTask attributeTask;
	private BattlefieldTask battlefieldTask;
	private AnswerQuestionTask answerQuestionTask;
	private UpgradeFunLvTask upgradeFunLvTask;
	
	public int completedTargetId;
	
	public int completeTimeLimitSeconds;
	
	
	public Map<Integer,KillMonsterTask> getKillMonsterTaskMap() {
		return killMonsterTaskMap;
	}

	public void setKillMonsterTaskMap(Map<Integer,KillMonsterTask> killMonsterTaskMap) {
		this.killMonsterTaskMap = killMonsterTaskMap;
	}

	public CollectItemTask getCollectItemTask() {
		return collectItemTask;
	}

	public void setCollectItemTask(CollectItemTask collectItemTask) {
		this.collectItemTask = collectItemTask;
	}

	public GameLevelTask getGameLevelTask() {
		return gameLevelTask;
	}

	public void setGameLevelTask(GameLevelTask gameLevelTask) {
		this.gameLevelTask = gameLevelTask;
	}

	public UseItemTask getUseItemTask() {
		return useItemTask;
	}

	public void setUseItemTask(UseItemTask useItemTask) {
		this.useItemTask = useItemTask;
	}

	public UseFunctionTask getUseFunctionTask() {
		return useFunctionTask;
	}

	public void setUseFunctionTask(UseFunctionTask useFunctionTask) {
		this.useFunctionTask = useFunctionTask;
	}

	public AttributeTask getAttributeTask() {
		return attributeTask;
	}

	public void setAttributeTask(AttributeTask attributeTask) {
		this.attributeTask = attributeTask;
	}
	
	public BattlefieldTask getBattlefieldTask() {
		return battlefieldTask;
	}

	public void setBattlefieldTask(BattlefieldTask battlefieldTask) {
		this.battlefieldTask = battlefieldTask;
	}

    
	public AnswerQuestionTask getAnswerQuestionTask() {
		return answerQuestionTask;
	}

	public void setAnswerQuestionTask(AnswerQuestionTask answerQuestionTask) {
		this.answerQuestionTask = answerQuestionTask;
	}
	
	public UpgradeFunLvTask getUpgradeFunLvTask() {
		return upgradeFunLvTask;
	}

	public void setUpgradeFunLvTask(UpgradeFunLvTask upgradeFunLvTask) {
		this.upgradeFunLvTask = upgradeFunLvTask;
	}
	
	



	public static class TalkTask{
		
	}
	/**
	 * 杀怪类型任务条件
	 * @author zhaizl
	 *
	 */
	public static class KillMonsterTask{
		public final static int ANY_TYPE_MONSTER_ID = -999;
		public KMonstTemplate monsterTemplate;
		public int killCount;
		public int monsterLevel;
		public boolean isAnyTypeMonster;
		public boolean isMonsterLevelLimit;

		public KillMonsterTask(KMonstTemplate monsterTemplate, int killCount,
				int monsterLevel, boolean isAnyTypeMonster,
				boolean isMonsterLevelLimit) {
			super();
			this.monsterTemplate = monsterTemplate;
			this.killCount = killCount;
			this.monsterLevel = monsterLevel;
			this.isAnyTypeMonster = isAnyTypeMonster;
			this.isMonsterLevelLimit = isMonsterLevelLimit;
		}
		
		
	}
	/**
	 * 收集道具类型任务条件
	 * @author zhaizl
	 *
	 */
	public static class CollectItemTask{
		public KItemTempAbs itemTemplate;
		public Map<Byte,KItemTempAbs> itemTemplateMap;
		public int collectCount;
		public boolean isLimitJob;
		public Set<String> itemCodeSet = new HashSet<String>();
		public CollectItemTask(KItemTempAbs itemTemplate, int collectCount) {
			this.itemTemplate = itemTemplate;
			this.collectCount = collectCount;
			this.isLimitJob = false;
			itemCodeSet.add(itemTemplate.itemCode);
		}
		
		public CollectItemTask(Map<Byte,KItemTempAbs> itemMap, int collectCount) {
			this.itemTemplateMap = itemMap;
			this.collectCount = collectCount;
			this.isLimitJob = true;
			for (KItemTempAbs itemTemp : itemMap.values()) {
				itemCodeSet.add(itemTemp.itemCode);
			}
		}
	}
	/**
	 * 关卡类型任务条件
	 * @author zhaizl
	 *
	 */
	public static class GameLevelTask{
		public final static int ANY_TYPE_LEVEL = -999;
		
		public int levelId;
		public int completeCount;
		private boolean isLevelTypeAny;
		public GameLevelTask(int levelId, int completeCount,
				boolean isLevelTypeAny) {
			super();
			this.levelId = levelId;
			this.completeCount = completeCount;
			this.isLevelTypeAny = isLevelTypeAny;
		}
		
		/**
		 * 目标关卡是否任意关卡
		 * @return
		 */
		public boolean isLevelTypeAny() {
			return isLevelTypeAny;
		}	
		
		
	}
	/**
	 * 使用道具类型任务条件
	 * @author zhaizl
	 *
	 */
	public static class UseItemTask{
		/**
		 * 使用道具的模版
		 */
		public KItemTempAbs itemTemplate;

		public UseItemTask(KItemTempAbs itemTemplate) {
			this.itemTemplate = itemTemplate;
		}
		
	}
	/**
	 * 使用功能类型任务条件
	 * @author zhaizl
	 *
	 */
	public static class UseFunctionTask{
		/**
		 * 功能ID
		 */
		public short functionId;
		/**
		 * 使用该功能的目标对象（对象可能为道具或者其他东西）
		 */
		public String functionTarget;
		/**
		 * 使用功能需要达到的目标次数
		 */
		public int useCount;
		public UseFunctionTask(short functionId, String functionTarget,
				int useCount) {
			super();
			this.functionId = functionId;
			this.functionTarget = functionTarget;
			this.useCount = useCount;
		}	
		
		
	}
	/**
	 * 数值类型任务条件
	 * @author zhaizl
	 *
	 */
	public static class AttributeTask{
		/**
		 * 数值类型
		 */
		public int attributeType;
		/**
		 * 该数值类型需要达到的目标值
		 */
		public int targetAttributeValue;
		public AttributeTask(int attributeType, int targetAttributeValue) {
			this.attributeType = attributeType;
			this.targetAttributeValue = targetAttributeValue;
		}		
		
	}
	
	/**
	 * 直接进入战场类型任务条件
	 * @author zhaizl
	 *
	 */
	public static class BattlefieldTask{
		/**
		 * 战场类型
		 */
		public KGameBattlefieldTypeEnum battlefieldType;

		public BattlefieldTask(KGameBattlefieldTypeEnum battlefieldType) {
			this.battlefieldType = battlefieldType;
		}
		
	}
	/**
	 * 回答问题类型任务条件
	 * @author zhaizl
	 *
	 */
	public static class AnswerQuestionTask{
		
		
		private int totalQuestionCount;
		private boolean isRandomQuestion;
		private int questionLevel;
		private List<Integer> questionIdList;		
		
		
		public AnswerQuestionTask(int totalQuestionCount,
				boolean isRandomQuestion,int questionLevel,int... questionId) {
			super();
			this.totalQuestionCount = totalQuestionCount;
			this.isRandomQuestion = isRandomQuestion;
			this.questionLevel = questionLevel;
			questionIdList = new ArrayList<Integer>();
			if(questionId!=null&&questionId.length>0){
				for (int i = 0; i < questionId.length; i++) {
					questionIdList.add(questionId[i]);
				}
			}
			
		}
		public int getTotalQuestionCount() {
			return totalQuestionCount;
		}
		public boolean isRandomQuestion() {
			return isRandomQuestion;
		}
		public List<Integer> getQuestionIdList() {
			return questionIdList;
		}
		public int getQuestionLevel() {
			return questionLevel;
		}
		
		
	}
	
	/**
	 * 提升功能（如强化、进阶）等级类型任务条件
	 * @author zhaizl
	 *
	 */
	public static class UpgradeFunLvTask{
		public final static String ANY_TARGET_TYPE = "-999";
		/**
		 * 功能ID
		 */
		public short functionId;
		/**
		 * 该功能的目标对象（对象可能为道具或者其他东西）
		 */
		public String functionTarget;
		/**
		 * 功能需要达到的目标等级
		 */
		public int targetLv;
		public UpgradeFunLvTask(short functionId, String functionTarget,
				int targetLv) {
			super();
			this.functionId = functionId;
			this.functionTarget = functionTarget;
			this.targetLv = targetLv;
		}	
	}
}
