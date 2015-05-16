package com.kola.kmp.logic.level.gamestory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jxl.read.biff.BiffException;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kmp.logic.level.KGameLevelManager;
import com.kola.kmp.logic.level.KGameLevelModuleExtension;
import com.kola.kmp.logic.level.KLevelTemplate;
import com.kola.kmp.logic.mission.KMissionModuleExtension;
import com.kola.kmp.logic.mission.guide.KGuideManager;
import com.kola.kmp.logic.support.KSupportFactory;

public class AnimationManager {

	private static final KGameLogger _LOGGER = KGameLogger
			.getLogger(AnimationManager.class);

	// private final static String EXCEL_FILE_PATH =
	// "./res/gamedata/scenariomodule/gameStoryConfig.xls";

	private static AnimationManager instance;
	// 接受任务后触发的剧情动画列表：KEY为任务模版ID
	private HashMap<Integer, Animation> missionAcceptTypeAnimations = new HashMap<Integer, Animation>();
	// 提交任务后触发的剧情动画列表：KEY为任务模版ID
	private HashMap<Integer, Animation> missionSubmitTypeAnimations = new HashMap<Integer, Animation>();
	// 进入关卡时触发的剧情动画列表：KEY为关卡ID
	private HashMap<Integer, List<Animation>> levelTypeAnimations = new HashMap<Integer, List<Animation>>();
	// 功能开启时触发的剧情动画列表：KEY为功能ID
	private HashMap<Short, Animation> functionTypeAnimations = new HashMap<Short, Animation>();
	// 功能开启时触发的剧情动画列表：KEY为功能ID
	private HashMap<Integer, List<Animation>> noviceGuideBattleTypeAnimations = new HashMap<Integer, List<Animation>>();

	// 结束关卡时触发的剧情动画列表：KEY为AnimationID
	private HashMap<Integer, Animation> allAnimations = new HashMap<Integer, Animation>();

	public static AnimationManager getInstance() {
		if (instance == null) {
			instance = new AnimationManager();
		}
		return instance;
	}

	public void init(String configPath) throws Exception {
		KGameExcelFile xlsFile = null;
		try {
			xlsFile = new KGameExcelFile(configPath);
		} catch (BiffException e) {
			throw new KGameServerException("读取剧情动画excel表头发生错误！", e);
		} catch (IOException e) {
			throw new KGameServerException("读取剧情动画excel表头发生错误！", e);
		}

		if (xlsFile != null) {
			// 初始化剧本数据
			KGameExcelTable storyDataTable = xlsFile.getTable("剧情配置表", 5);
			KGameExcelRow[] allStoryDataRows = storyDataTable.getAllDataRows();

			for (int i = 0; i < allStoryDataRows.length; i++) {
				int animationId = allStoryDataRows[i].getInt("animationId");
				int animationResId = allStoryDataRows[i]
						.getInt("animationResId");
				byte animationStartType = allStoryDataRows[i]
						.getByte("animationStartType");
				int animationTargetId = allStoryDataRows[i]
						.getInt("animationTargetId");
				int battlefieldSerialNumber = allStoryDataRows[i]
						.getInt("battlefieldSerialNumber");
				int waveNum = allStoryDataRows[i].getInt("waveNum");

				Animation animation = new Animation(animationId,
						animationResId, animationTargetId, animationStartType,
						battlefieldSerialNumber, waveNum);

				if (animation.animationStartType == Animation.ANIMATION_START_TYPE_ACCEPT_MISSION) {
					missionAcceptTypeAnimations.put(
							animation.animationTargetId, animation);
				} else if (animation.animationStartType == Animation.ANIMATION_START_TYPE_SUBMIT_MISSION) {
					missionSubmitTypeAnimations.put(
							animation.animationTargetId, animation);
				} else if (animation.animationStartType == Animation.ANIMATION_START_TYPE_LEVEL_START
						|| animation.animationStartType == Animation.ANIMATION_START_TYPE_LEVEL_END
						|| animation.animationStartType == Animation.ANIMATION_START_TYPE_BATTLE_WAVE) {
					if (!levelTypeAnimations
							.containsKey(animation.animationTargetId)) {
						levelTypeAnimations.put(animation.animationTargetId,
								new ArrayList<Animation>());
					}
					levelTypeAnimations.get(animation.animationTargetId).add(
							animation);
				} else if (animation.animationStartType == Animation.ANIMATION_START_TYPE_FUNCTION_OPEN) {
					this.functionTypeAnimations.put((short) animationTargetId,
							animation);
				} else if (animation.animationStartType == Animation.ANIMATION_START_TYPE_NOVICE_GUIDE_BATTLE_START
						|| animation.animationStartType == Animation.ANIMATION_START_TYPE_NOVICE_GUIDE_BATTLE_END
						|| animation.animationStartType == Animation.ANIMATION_START_TYPE_NOVICE_GUIDE_BATTLE_WAVE) {
					if (!noviceGuideBattleTypeAnimations
							.containsKey(animation.animationTargetId)) {
						noviceGuideBattleTypeAnimations.put(
								animation.animationTargetId,
								new ArrayList<Animation>());
					}
					if (animation.animationStartType == Animation.ANIMATION_START_TYPE_NOVICE_GUIDE_BATTLE_END) {
						animation.animationStartType = Animation.ANIMATION_START_TYPE_LEVEL_END;
					} else if (animation.animationStartType == Animation.ANIMATION_START_TYPE_NOVICE_GUIDE_BATTLE_START) {
						animation.animationStartType = Animation.ANIMATION_START_TYPE_LEVEL_START;
					} else {
						animation.animationStartType = Animation.ANIMATION_START_TYPE_BATTLE_WAVE;
					}
					noviceGuideBattleTypeAnimations.get(
							animation.animationTargetId).add(animation);
				}

				allAnimations.put(animation.animationId, animation);
			}
		}
	}

	public void checkInitAnimation() throws KGameServerException {
		boolean isInitSuccess = true;
		for (Animation animation : allAnimations.values()) {
			if (animation.animationStartType == Animation.ANIMATION_START_TYPE_ACCEPT_MISSION
					|| animation.animationStartType == Animation.ANIMATION_START_TYPE_SUBMIT_MISSION) {
				if (KMissionModuleExtension.getManager().getMissionTemplate(
						animation.animationTargetId) == null) {
					_LOGGER.error(
							"#########  加载levelConfig.xls的表<剧情配置表>的模版={}的剧情数据错误，找不到对应的任务模版={}。",
							animation.animationId, animation.animationTargetId);
					isInitSuccess = false;
				}
			}
			if (animation.animationStartType == Animation.ANIMATION_START_TYPE_LEVEL_START
					|| animation.animationStartType == Animation.ANIMATION_START_TYPE_LEVEL_END
					|| animation.animationStartType == Animation.ANIMATION_START_TYPE_BATTLE_WAVE) {

				KLevelTemplate level = KGameLevelModuleExtension.getManager()
						.getKGameLevel(animation.animationTargetId);
				if (level == null) {
					if (KSupportFactory.getLevelSupport()
							.getNoviceGuideBattlefield().getLevelId() != animation.animationTargetId) {
						_LOGGER.error(
								"#########  加载levelConfig.xls的表<剧情配置表>的模版={}的剧情数据错误，找不到对应的关卡模版={}。",
								animation.animationId,
								animation.animationTargetId);
						isInitSuccess = false;
					}
				}
				if (level != null
						&& animation.animationStartType == Animation.ANIMATION_START_TYPE_BATTLE_WAVE
						&& level.getAllNormalBattlefields().size() < animation.battlefieldSerialNumber) {
					_LOGGER.error(
							"#########  加载levelConfig.xls的表<剧情配置表>的模版={}的剧情数据错误，战场序号={}，大于关卡的战场数量。",
							animation.animationId, animation.animationTargetId);
					isInitSuccess = false;
				}
			}

			if (animation.animationStartType == Animation.ANIMATION_START_TYPE_FUNCTION_OPEN) {
				short funId = (short) animation.animationTargetId;
				if (!KGuideManager.getMainMenuFunctionInfoMap().containsKey(
						funId)) {
					_LOGGER.error(
							"#########  加载levelConfig.xls的表<剧情配置表>的模版={}的剧情数据错误，找不到对应的开放功能ID={}。",
							animation.animationId, animation.animationTargetId);
					isInitSuccess = false;
				}
			}
		}
		if (!isInitSuccess) {
			throw new KGameServerException(
					"#########  加载levelConfig.xls表<剧情配置表>的数据错误。");
		}
	}

	public HashMap<Integer, Animation> getMissionAcceptTypeAnimations() {
		return missionAcceptTypeAnimations;
	}

	public HashMap<Integer, Animation> getMissionSubmitTypeAnimations() {
		return missionSubmitTypeAnimations;
	}

	public HashMap<Integer, List<Animation>> getLevelTypeAnimations() {
		return levelTypeAnimations;
	}

	public HashMap<Short, Animation> getFunctionTypeAnimations() {
		return functionTypeAnimations;
	}

	public HashMap<Integer, List<Animation>> getNoviceGuideBattleTypeAnimations() {
		return noviceGuideBattleTypeAnimations;
	}

	public HashMap<Integer, Animation> getAllAnimations() {
		return allAnimations;
	}

}
