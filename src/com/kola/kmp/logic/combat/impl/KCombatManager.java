package com.kola.kmp.logic.combat.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.player.KGamePlayerSession;
import com.koala.game.timer.KGameTimeSignal;
import com.koala.game.timer.KGameTimerTask;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.XmlUtil;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.activity.goldact.KBarrelBattlefield;
import com.kola.kmp.logic.activity.goldact.KBarrelBattlefield.KBarrelBattleData;
import com.kola.kmp.logic.combat.IBattlefieldBaseInfo;
import com.kola.kmp.logic.combat.IBattlefieldLevelInfo;
import com.kola.kmp.logic.combat.ICombat;
import com.kola.kmp.logic.combat.ICombatConstant;
import com.kola.kmp.logic.combat.ICombatEnhanceInfo;
import com.kola.kmp.logic.combat.ICombatEvent;
import com.kola.kmp.logic.combat.ICombatEventListener;
import com.kola.kmp.logic.combat.ICombatForce;
import com.kola.kmp.logic.combat.ICombatRoleSideHpUpdater;
import com.kola.kmp.logic.combat.ICombatGround.ICombatGroundBuilder;
import com.kola.kmp.logic.combat.ICombatMember;
import com.kola.kmp.logic.combat.ICombatMinion;
import com.kola.kmp.logic.combat.ICombatMirrorDataGroup;
import com.kola.kmp.logic.combat.ICombatMirrorDataHandler;
import com.kola.kmp.logic.combat.ICombatPlugin;
import com.kola.kmp.logic.combat.KCombatType;
import com.kola.kmp.logic.combat.api.ICombatMinionTemplateData;
import com.kola.kmp.logic.combat.api.ICombatMonster;
import com.kola.kmp.logic.combat.api.ICombatMount;
import com.kola.kmp.logic.combat.api.ICombatObjectBase;
import com.kola.kmp.logic.combat.api.ICombatPet;
import com.kola.kmp.logic.combat.api.ICombatRole;
import com.kola.kmp.logic.combat.api.ICombatSkillTemplateData;
import com.kola.kmp.logic.combat.cmd.ICombatCommandCreator;
import com.kola.kmp.logic.combat.event.KBossDieEvent;
import com.kola.kmp.logic.combat.event.KGoldCombatAfterAtkEvent;
import com.kola.kmp.logic.combat.function.IFunctionExecution;
import com.kola.kmp.logic.combat.function.IFunctionParser;
import com.kola.kmp.logic.combat.function.event.ICondition;
import com.kola.kmp.logic.combat.function.event.KConditionFactory;
import com.kola.kmp.logic.combat.impl.KCombatGroundBarrelImpl.KCombatGroundBarrelBuilder;
import com.kola.kmp.logic.combat.impl.KCombatGroundBaseImpl.KCombatGroundBuilderBaseImpl;
import com.kola.kmp.logic.combat.impl.KCombatGroundImpl.KCombatGroundBuilder;
import com.kola.kmp.logic.combat.impl.KCombatGroundTowerImpl.KCombatGroundTowerBuilder;
import com.kola.kmp.logic.combat.impl.KCombatImpl.KCombatBuilder;
import com.kola.kmp.logic.combat.impl.KRandomObstruction.KObstructionPosistion;
import com.kola.kmp.logic.combat.operation.IOperationMsgHandler;
import com.kola.kmp.logic.combat.resulthandler.ICombatGameLevelInfo;
import com.kola.kmp.logic.combat.resulthandler.ICombatResult;
import com.kola.kmp.logic.combat.resulthandler.ICombatResultHandler;
import com.kola.kmp.logic.combat.skill.ICombatSkillExecution;
import com.kola.kmp.logic.combat.skill.ICombatSkillExecutionParser;
import com.kola.kmp.logic.combat.state.ICombatState;
import com.kola.kmp.logic.combat.state.ICombatStateParser;
import com.kola.kmp.logic.combat.state.ICombatStateTemplate;
import com.kola.kmp.logic.combat.vitorycondition.IVitoryCondition;
import com.kola.kmp.logic.level.BattlefieldWaveViewInfo;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.BornPointData;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.MonsterData;
import com.kola.kmp.logic.level.KBattleObjectDataStruct.ObstructionData;
import com.kola.kmp.logic.level.KGameBattlefield;
import com.kola.kmp.logic.level.gamestory.Animation;
import com.kola.kmp.logic.level.petcopy.KPetCopyBattlefield;
import com.kola.kmp.logic.level.petcopy.KPetCopyBattlefield.KPetCopyBattlefieldDropData;
import com.kola.kmp.logic.level.tower.KTowerBattlefield;
import com.kola.kmp.logic.level.tower.KTowerData;
import com.kola.kmp.logic.npc.KNPCDataStructs.KMonstTemplate;
import com.kola.kmp.logic.npc.KNPCDataStructs.ObstructionTemplate;
import com.kola.kmp.logic.npc.dialog.KDialogService;
import com.kola.kmp.logic.other.KActionResult;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KGameBattlefieldTypeEnum;
import com.kola.kmp.logic.other.KObstructionTargetType;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.skill.KSkillDataStructs.KRolePasSkillTemp;
import com.kola.kmp.logic.support.CombatModuleSupport.ICombatEnv;
import com.kola.kmp.logic.support.CombatModuleSupport.ICombatEnvPlus;
import com.kola.kmp.logic.support.CombatModuleSupport.ICombatMonsterUpdateInfo;
import com.kola.kmp.logic.support.ItemModuleSupport.ISecondWeapon;
import com.kola.kmp.logic.support.KSupportFactory;
import com.kola.kmp.logic.util.IRoleEquipShowData;
import com.kola.kmp.logic.util.tips.CombatTips;
import com.kola.kmp.logic.util.tips.GlobalTips;
import com.kola.kmp.logic.vip.KVIPDataStructs.VIPLevelData;

/**
 * 
 * @author PERRY CHAN
 */
public class KCombatManager {

	private static final Logger _LOGGER = KGameLogger.getLogger(KCombatManager.class);
	
	private static final Map<Integer, ICombat> _allCurrentCombats = new LinkedHashMap<Integer, ICombat>(500);
	private static final Map<Long, Integer> _roleIdToCombatId = new LinkedHashMap<Long, Integer>();
	private static final Map<Integer, List<ICombatResultHandler>> _combatResultHandlers = new HashMap<Integer, List<ICombatResultHandler>>();
	private static final Map<Integer, IVitoryCondition> _combatVitoryConditions = new HashMap<Integer, IVitoryCondition>();
	private static final Map<Integer, ICombatStateParser> _combatStateParser = new HashMap<Integer, ICombatStateParser>();
	private static final Map<Integer, ICombatSkillExecutionParser> _combatSkillExecutionParsers = new HashMap<Integer, ICombatSkillExecutionParser>();
	private static final Map<Integer, IFunctionParser> _allFunctionParser = new HashMap<Integer, IFunctionParser>();
	private static final Map<Integer, KCombatSpecialEffect> _allSpecialEffect = new HashMap<Integer, KCombatSpecialEffect>();
	private static final Map<Integer, Map<Integer,KCombatSpecialEffect>> _allPasSkillSpecialEffect = new HashMap<Integer, Map<Integer,KCombatSpecialEffect>>();
	private static final Map<Integer, Map<Integer, ICombatSkillExecution>> _combatSkillExecutions = new HashMap<Integer, Map<Integer, ICombatSkillExecution>>(); // key=技能id，value=执行类
	private static final Map<Integer, Map<Integer, Integer>> _summonSkillInfoMap = new HashMap<Integer, Map<Integer, Integer>>(); // key=技能模板id，value={key=技能等级，value=召唤物模板id}
	private static final Map<Long, ICombatMirrorDataGroup> _offlineData = new ConcurrentHashMap<Long, ICombatMirrorDataGroup>(); // 离线数据
	private static final Map<Integer, Integer> _obstStatesByAttack = new HashMap<Integer, Integer>();
	private static final Map<Integer, Integer> _obstStatesAfterDestroyed = new HashMap<Integer, Integer>();
	private static final Map<Byte, Map<Integer, KRandomObstruction>> _randomObstructions = new HashMap<Byte, Map<Integer, KRandomObstruction>>();
	private static final Map<Integer, List<ICombatPlugin>> _combatPlugin = new HashMap<Integer, List<ICombatPlugin>>();
//	private static final Map<Integer, Integer> _mountConsumeMap = new HashMap<Integer, Integer>(); // 机甲消耗数据，key=机甲模板id，value=机甲消耗的怒气豆数量
	private static final Map<Integer, ICombatPet> _assistantPetInstanceMap = new HashMap<Integer, ICombatPet>();
	private static final Map<Integer, List<Integer>> _passSkillAddStateMap = new HashMap<Integer, List<Integer>>();
	private static final Map<KCombatType, ICombatRoleSideHpUpdater> _roleSideHpUpdater = new HashMap<KCombatType, ICombatRoleSideHpUpdater>();
	private static KCombatSpecialEffect _addEnergyEffect;
	private static final float SPACE_BETWEEN_PET_AND_ROLE = 0.3f; // 随从与主角的间距（单位：百像素）
	private static IOperationMsgHandler _opMsgHandler;
	private static ICombatCommandCreator _cmdCreator;
	private static ICombatMirrorDataHandler _mirrorDataHandler;
	private static String _assistantPetPath;
	
	private static List<KCombatEntrance> getCombatMonster(IBattlefieldLevelInfo pbattleField, Map<Integer, ICombatMonsterUpdateInfo> updateMap, boolean genObstruction) {
		List<KCombatEntrance> entranceList = new ArrayList<KCombatEntrance>();
		List<BattlefieldWaveViewInfo> list = pbattleField.getAllWaveInfo();
		BattlefieldWaveViewInfo tempViewInfo;
		List<MonsterData> monsterList;
		List<ObstructionData> obstructionDList;
		int obstructionAppearRate;
		KCombatEntrance entrance;
		for(int i = 0; i < list.size(); i++) {
			tempViewInfo = list.get(i);
			monsterList = tempViewInfo.getAllMonsters();
			obstructionDList = tempViewInfo.getAllObstructions();
			if (updateMap.isEmpty()) {
				for (int k = 0; k < monsterList.size(); k++) {
					MonsterData monsterData = monsterList.get(k);
					ICombatMonster combatMonster = KSupportFactory.getNpcModuleSupport().getCombatMonster(monsterData._monsterTemplate);
//					System.out.println("怪物模板Id：" + monsterData._monsterTemplate.id + "，怪物形象id：" + monsterData._monsterTemplate.monstUIData.res_id);
//					entranceList.add(new KCombatEntrance(combatMonster, null, monsterData._corX, monsterData._corY, monsterData._objInstanceId));
					entrance = KCombatEntrancePool.borrowEntrance(combatMonster, null, monsterData._corX, monsterData._corY, monsterData._objInstanceId);
					entranceList.add(entrance);
					if(combatMonster.getObjectType() == ICombatObjectBase.OBJECT_TYPE_MONSTER_BOSS) {
						entrance.addEvent(new KBossDieEvent());
					}
				}
				
			} else {
				for (int k = 0; k < monsterList.size(); k++) {
					MonsterData monsterData = monsterList.get(k);
					ICombatMonsterUpdateInfo info = updateMap.get(monsterData._objInstanceId);
					ICombatMonster combatMonster;
					if (info == null) {
						combatMonster = KSupportFactory.getNpcModuleSupport().getCombatMonster(monsterData._monsterTemplate);
					} else {
						combatMonster = KSupportFactory.getNpcModuleSupport().getCombatMonster(KSupportFactory.getNpcModuleSupport().getMonstTemplate(info.getTemplateId()));
					}
					entrance = KCombatEntrancePool.borrowEntrance(combatMonster, null, monsterData._corX, monsterData._corY, monsterData._objInstanceId);
					entranceList.add(entrance);
					if(combatMonster.getObjectType() == ICombatObjectBase.OBJECT_TYPE_MONSTER_BOSS) {
						entrance.addEvent(new KBossDieEvent());
					}
				}
			}
			if (genObstruction) {
				for (int k = 0; k < obstructionDList.size(); k++) {
					ObstructionData obstructionData = obstructionDList.get(k);
					if (obstructionData._obsTemplate.probability < UtilTool.TEN_THOUSAND_RATIO_UNIT) {
						obstructionAppearRate = UtilTool.random(UtilTool.TEN_THOUSAND_RATIO_UNIT);
						if (obstructionData._obsTemplate.probability < obstructionAppearRate) {
							continue;
						}
					}
					ICombatObjectBase combatObstruction = KSupportFactory.getNpcModuleSupport().getCombatObstruction(obstructionData._obsTemplate);
					entrance = KCombatEntrancePool.borrowEntrance(combatObstruction, null, obstructionData._corX, obstructionData._corY, obstructionData._objInstanceId);
					entranceList.add(entrance);
				}
			}
		}
		Map<Integer, KRandomObstruction> map = _randomObstructions.get(pbattleField.getBattlefieldType().battlefieldType);
		if (map != null) {
			KRandomObstruction randomObst = map.get(pbattleField.getLevelId());
			if (randomObst != null && randomObst.validateShow()) {
				KObstructionPosistion pos = randomObst.getRandomPosistion();
				ICombatObjectBase combatObstruction = KSupportFactory.getNpcModuleSupport().getCombatObstruction(randomObst.getRandomObstructionTemplateId());
				entrance = KCombatEntrancePool.borrowEntrance(combatObstruction, null, pos._x, pos._y, 0);
				entranceList.add(entrance);
			}
		}
		return entranceList;
	}
	
	private static List<KCombatEntrance> getRoleSideByMirror(ICombatMirrorDataGroup mirror, float bornX, float bornY) {
		List<KCombatEntrance> roleSide = new ArrayList<KCombatEntrance>();
		KCombatEntrance mountEntrance = null;
		if(mirror.getMountMirror() != null) {
			mountEntrance = KCombatEntrancePool.borrowEntrance(mirror.getMountMirror(), null, bornX, bornY, 0);
//			mountEntrance.inheritAttackAttribute(mirror.getRoleMirror());
			mountEntrance.inheritBasicAttribute(mirror.getRoleMirror());
		}
		KCombatEntrance roleEntrance = KCombatEntrancePool.borrowEntrance(mirror.getRoleMirror(), mountEntrance, bornX, bornY, 0);
		roleSide.add(roleEntrance);
		if(mountEntrance != null) {
			mountEntrance.initVehicleAttr(mirror.getRoleMirror());
		}
		if(mirror.getPetMirror() != null) {
			KCombatEntrance petEntrance = KCombatEntrancePool.borrowEntrance(mirror.getPetMirror(), null, bornX + SPACE_BETWEEN_PET_AND_ROLE, bornY, 0);
			// 不再继承角色的属性，用自身的属性
//			petEntrance.inheritBasicAttribute(mirror.getRoleMirror());
//			petEntrance.inheritAttackAttribute(mirror.getRoleMirror());
			roleSide.add(petEntrance);
		}
		return roleSide;
	}
	
	private static List<KCombatEntrance> getRoleSide(KRole role, float pBornX, float pBornY, boolean setFighting, ICombatPet helpPet) {
		List<KCombatEntrance> roleSide = new ArrayList<KCombatEntrance>();
		boolean setSuccess = setFighting ? role.setFighting() : true;
		if (setSuccess) {
			try {
				ICombatMount mount = KSupportFactory.getMountModuleSupport().getMountCanWarOfRole(role.getId());
				KCombatEntrance mountEntrance = null;
				if(mount != null) {
					mountEntrance = KCombatEntrancePool.borrowEntrance(mount, null, pBornX, pBornY, 0);
//					mountEntrance.inheritAttackAttribute(role);
					mountEntrance.inheritBasicAttribute(role);
				}
				KCombatEntrance roleEntrance = KCombatEntrancePool.borrowEntrance(role, mountEntrance, pBornX, pBornY, 0);
				if(mountEntrance != null) {
					mountEntrance.initVehicleAttr(role);
				}
				roleSide.add(roleEntrance);
				ICombatPet combatPet;
				boolean updateOwnerId = false;
				if (helpPet != null) {
					combatPet = helpPet;
					updateOwnerId = true;
				} else {
					combatPet = KSupportFactory.getPetModuleSupport().getFightingPetForBattle(role.getId());
				}
				if (combatPet != null) {
					KCombatEntrance entrance = KCombatEntrancePool.borrowEntrance(combatPet, null, pBornX - SPACE_BETWEEN_PET_AND_ROLE, pBornY, 0);
					// 不再继承角色的属性，用自身的属性
//					entrance.inheritBasicAttribute(role);
//					entrance.inheritAttackAttribute(role);
					roleSide.add(entrance);
					if (updateOwnerId) {
						entrance.updateOwnerId(role.getId());
					}
				}
			} catch (Exception e) {
				_LOGGER.error("获取角色战斗对象列表出现异常！角色id：{}", role.getId(), e);
				if (setFighting) {
					role.setNotFighting();
				}
				if(roleSide.size() > 0) {
					releaseEntrance(roleSide);
				}
				roleSide.clear();
			}
		} else {
			if(setFighting && role.isFighting()) {
				// 复查是否真的在战斗中
				Integer combatId = _roleIdToCombatId.get(role.getId());
				if(combatId == null) {
					role.setNotFighting();
					return getRoleSide(role, pBornX, pBornY, setFighting, helpPet);
				} else {
					ICombat combat = _allCurrentCombats.get(combatId);
					if (combat.isTerminal()) {
						combat.handleClientExitFinished(role.getId());
						return getRoleSide(role, pBornX, pBornY, setFighting, helpPet);
					}
				}
			}
		}
		return roleSide;
	}
	
	private static KActionResult<List<KCombatEntrance>> getRoleAIEntranceList(long roleId, float bornX, float bornY) {
		KActionResult<List<KCombatEntrance>> result = new KActionResult<List<KCombatEntrance>>();
		List<KCombatEntrance> roleAIEntranceList = null;
		ICombatMirrorDataGroup mirror = _offlineData.get(roleId);
		if (mirror != null) {
			roleAIEntranceList = getRoleSideByMirror(mirror, bornX, bornY);
		} else {
			KRole targetRole = KSupportFactory.getRoleModuleSupport().getRoleWithOfflineAttr(roleId);
			if (targetRole != null) {
				if (targetRole.isOnline()) {
					roleAIEntranceList = getRoleSide(targetRole, bornX, bornY, false, null);
				} else {
					mirror = _mirrorDataHandler.getMirrorDataGroup(targetRole);
					_offlineData.put(roleId, mirror);
					roleAIEntranceList = getRoleSideByMirror(mirror, bornX, bornY);
				}
			} else {
				result.tips = CombatTips.getTipsTargetNotExists();
			}
		}
		result.attachment = roleAIEntranceList;
		return result;
	}
	
	private static KCombatBuilder createCombatBuilder(KCombatType pType) {
		KCombatBuilder builder = new KCombatBuilder();
		builder.combatType(pType);
		builder.opMsgHandler(_opMsgHandler);
		builder.cmdCreator(_cmdCreator);
		builder.vitoryCondition(getVitoryCondition(pType.sign));
		return builder;
	}
	
	private static void releaseEntranceOfCombatGround(List<ICombatGroundBuilder> groundBuilderList) {
		for (int i = 0; i < groundBuilderList.size(); i++) {
			groundBuilderList.get(i).onStartCombatFail();
		}
	}
	
	private static KActionResult<Integer> startCombat(KRole role, List<ICombatGroundBuilder> groundBuilderList, KCombatType type, KStartCombatArgument arg) {
		KActionResult<Integer> result = new KActionResult<Integer>();
		List<KCombatEntrance> roleSide = getRoleSide(role, arg.roleBornX, arg.roleBornY, true, arg.helpPet); // 获取角色的战斗对象
		if (roleSide.size() > 0) {
			switch (type) {
			case GAME_LEVEL:
				if (role.getLevel() < KCombatConfig.getOfflineCombatMaxLv()) {
					type = KCombatType.OFFLINE_COMBAT;
				}
				break;
			default:
				break;
			}
			try {
				if(arg.assistants != null) {
					roleSide.addAll(arg.assistants);
				}
				if (arg.roleEnhanceInfo != null) {
					KCombatEntrance entrance;
					for (int i = 0; i < roleSide.size(); i++) {
						entrance = roleSide.get(i);
						if (entrance.getMemberType() == ICombatMember.MEMBER_TYPE_ROLE) {
							entrance.updateAttackAttribute(arg.roleEnhanceInfo);
							break;
						}
					}
				}
				if (arg.eventList != null) {
					KCombatEntrance entrance;
					for (int i = 0; i < roleSide.size(); i++) {
						entrance = roleSide.get(i);
						entrance.addAllEvent(arg.eventList);
						if(entrance.getCombatMount() != null) {
							entrance.getCombatMount().addAllEvent(arg.eventList);
						}
					}
				}
				for (int i = 0; i < roleSide.size(); i++) {
					// 添加增加怒气的事件
					KCombatEntrance entrance = roleSide.get(i);
					if (entrance.getMemberType() == ICombatMember.MEMBER_TYPE_ROLE) {
						entrance.addEvent(new KIncEnergyEventListener());
						if (entrance.getCombatMount() != null) {
							entrance.getCombatMount().addEvent(new KIncEnergyEventListener());
						}
					}
				}
				List<KCombatAnimation> combatAnimations;
				if(arg.animationList != null) {
					combatAnimations = new ArrayList<KCombatAnimation>();
					for(int i = 0; i < arg.animationList.size(); i++) {
						combatAnimations.add(new KCombatAnimation(arg.animationList.get(i)));
					}
				} else {
					combatAnimations = Collections.emptyList();
				}
				ICombatRoleSideHpUpdater updater = _roleSideHpUpdater.get(type);
				if (updater != null) {
					KCombatEntrance entrance;
					long currentHp;
					for (Iterator<KCombatEntrance> itr = roleSide.iterator(); itr.hasNext();) {
						entrance = itr.next();
						switch (entrance.getMemberType()) {
						case ICombatMember.MEMBER_TYPE_ROLE:
							if (updater.handleRoleHpUpdate()) {
								currentHp = updater.getRoleHp(role.getId());
								if (currentHp <= 0) {
									currentHp = 1;
								}
							} else {
								continue;
							}
							break;
						case ICombatMember.MEMBER_TYPE_PET:
							if (updater.handlePetHpUpdate()) {
								currentHp = updater.getPetHp(role.getId(), entrance.getSrcObjId());
								if (currentHp <= 0) {
									itr.remove();
									KCombatEntrancePool.returnEntrance(entrance);
									continue;
								}
							} else {
								continue;
							}
							break;
						default:
							continue;
						}
						if (currentHp > 0) {
							entrance.updateHp(currentHp);
						}
					}
				}
				KCombatBuilder combatBuilder = createCombatBuilder(type);
				combatBuilder.combatGroundBuilderList(groundBuilderList); // 战场列表
				combatBuilder.roleSide(roleSide); // 角色势力
				combatBuilder.resultAttachment(arg.resultAttachment);
				combatBuilder.gameLevelInfo(arg.gameLevelInfo);
				combatBuilder.alsoSendFinishMsgIfEscape(arg.alsoSendFinishMsgIfEscape);
				combatBuilder.animations(combatAnimations);
				combatBuilder.pluginList(_combatPlugin.get(type.sign));
				combatBuilder.timeOutMillis(arg.timeOutMillis);
				combatBuilder.totalWaveCount(arg.totalWaveCount);
				combatBuilder.preEventList(arg.eventList);
				boolean canAutoFight = true;
				if (type.isCanAutoFight()) {
					if (role.getLevel() < KCombatConfig.getMinLevelOfAutoFight()) {
						VIPLevelData vipData = KSupportFactory.getVIPModuleSupport().getVIPLevelData(role.getId());
						if (vipData == null || !vipData.Autobattle) {
							canAutoFight = false;
						}
					}
				} else {
					canAutoFight = false;
				}
				combatBuilder.canAutoFight(canAutoFight);
				if(arg.robotList != null) {
					combatBuilder.robotList(arg.robotList);
				}
				ICombat combat = combatBuilder.build();
				_allCurrentCombats.put(combat.getSerialId(), combat);
				_roleIdToCombatId.put(role.getId(), combat.getSerialId());
				result.success = true;
				try {
					KSupportFactory.getDuplicateMapSupport().notifyPlayerRoleFightStatus(role, true);
				} catch (Exception e) {
					_LOGGER.error("通知副本地图角色进入战斗发生异常！角色id：{}", role.getId(), e);
				}
			} catch (Exception e) {
				_LOGGER.error("角色进入战斗出现异常！角色id：{}，战斗类型：{}", role.getId(), type, e);
				result.tips = GlobalTips.getTipsServerBusy();
				result.attachment = ICombatConstant.ERR_CODE_UNKNOW;
				setRoleNotFighting(role);
				releaseEntranceOfCombatGround(groundBuilderList);
			}
		} else {
			result.success = false;
			result.tips = CombatTips.getTipsRoleIsFighting(role.getName());
			result.attachment = ICombatConstant.ERR_CODE_ROLE_IS_FIGHTING;
			_LOGGER.error("角色：{}正在战斗中！", role.getName());
			releaseEntranceOfCombatGround(groundBuilderList);
		}
		return result;
	}
	
	private static void setBasePropertyToBuilder (KCombatGroundBuilderBaseImpl builder, IBattlefieldBaseInfo baseInfo, KCombatSpecialEffect pAddEnergyEffect) {
		builder.battleFieldTemplateId(baseInfo.getBattlefieldId());
		builder.mapResPath(baseInfo.getBattlePathName());
		builder.bgAudioResId(baseInfo.getBgMusicResId());
		builder.nextBattleFieldTemplateId(baseInfo.getNextBattleFieldId());
		builder.addEnergyEffect(pAddEnergyEffect);
	}
	
	private static KCombatGroundBuilder createGameLevelGroundBuilder(IBattlefieldLevelInfo battlefield, Map<Integer, ICombatMonsterUpdateInfo> map, boolean genObstruction) {
		if(map == null) {
			map = Collections.emptyMap();
		}
		KCombatGroundBuilder builder = new KCombatGroundBuilder();
		setBasePropertyToBuilder(builder, battlefield, _addEnergyEffect);
		List<KCombatEntrance> entranceList = getCombatMonster(battlefield, map, genObstruction);
		builder.entranceList(entranceList);
		builder.first(battlefield.isFirstBattlefield());
		return builder;
	}
	
	private static int getObstructionStateId(int templateId, Map<Integer, Integer> map) {
		Integer stateId = map.get(templateId);
		if (stateId == null) {
			stateId = 0;
		}
		return stateId;
	}
	
	static void releaseEntrance(List<KCombatEntrance> list) {
		KCombatEntrance temp;
		for (int i = 0; i < list.size(); i++) {
			temp = list.get(i);
			temp.release();
			KCombatEntrancePool.returnEntrance(temp);
			if (temp.getCombatMount() != null) {
				KCombatEntrancePool.returnEntrance(temp.getCombatMount());
			}
		}
	}
	
	/**
	 * 获取副武器
	 * @param roleId
	 * @return
	 */
	static ISecondWeapon getSecondWeapon(long roleId) {
		ICombatMirrorDataGroup group = _offlineData.get(roleId);
		if (group != null) {
			return group.getSecondWeapon();
		} else {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			return role.getSecondWeapon();
		}
	}
	
	static List<IRoleEquipShowData> getRoleEquipmentResMap(long roleId) {
		ICombatMirrorDataGroup data = _offlineData.get(roleId);
		if (data != null) {
			return data.getEquipmentRes();
		} else {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if (role != null) {
				return role.getEquipmentRes();
			} else {
				return Collections.emptyList();
			}
		}
	}
	
	/**
	 * 
	 * 获取角色武器的icon
	 * 
	 * @param roleId
	 * @return 武器的icon，[0]=主武器，[1]=副武器
	 */
	static int[] getRoleWeaponIcon(long roleId) {
		return KSupportFactory.getItemModuleSupport().getWeaponIcons(roleId);
	}
	
	static String getRoleFashionRes(long roleId, boolean isRole) {
		if (isRole) {
			ICombatMirrorDataGroup data = _offlineData.get(roleId);
			if (data != null) {
				return data.getFashionRes();
			} else {
				return KSupportFactory.getFashionModuleSupport().getFashingResId(roleId);
			}
		} else {
			return KSupportFactory.getTeamPVPSupport().getRobotFashion(roleId);
		}
	}
	
	static int[] getEquipSetRes(long roleId) {
		ICombatMirrorDataGroup data = _offlineData.get(roleId);
		if (data != null) {
			return data.getEquipSetRes();
		} else {
			return KSupportFactory.getItemModuleSupport().getEquiSetMapResIds(roleId);
		}
	}
	
//	static void packEquipmentAndFashionRes(long roleId, byte job, KGameMessage msg) {
//		ICombatMirrorDataGroup data = _offlineData.get(roleId);
//		if (data != null) {
//			KSupportFactory.getRoleModuleSupport().packRoleResToMsg(data, job, msg);
//		} else {
//			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
//			if (role != null) {
//				KSupportFactory.getRoleModuleSupport().packRoleResToMsg(role, job, msg);
//			}
//		}
//	}
	
	static List<KCombatEntrance> getTowerMonsters(Map<Integer, Integer> monsterMap, float corX, float corY) {
//		Map<Integer, Integer> monsterMap = getLeft ? towerData.getLeftMonsterMap() : towerData.getRightMonsterMap();
		if (monsterMap != null && monsterMap.size() > 0) {
//			float corX;
//			if(getLeft) {
//				corX = -1;
//			} else {
//				corX = 1;
//			}
			List<KCombatEntrance> list = new ArrayList<KCombatEntrance>();
			Map.Entry<Integer, Integer> entry;
			KCombatEntrance entrance;
			ICombatMonster monster;
			for (Iterator<Map.Entry<Integer, Integer>> itr = monsterMap.entrySet().iterator(); itr.hasNext();) {
				entry = itr.next();
				for (int i = entry.getValue().intValue(); i-- > 0;) {
					monster = KSupportFactory.getNpcModuleSupport().getCombatMonster(KSupportFactory.getNpcModuleSupport().getMonstTemplate(entry.getKey()));
//					entrance = new KCombatEntrance(monster, null, corX, corY, 0);
					entrance = KCombatEntrancePool.borrowEntrance(monster, null, corX, corY, 0);
					list.add(entrance);
				}
			}
			return list;
		} else {
			return Collections.emptyList();
		}
	}
	
	static ICombatSkillExecution getSkillExecution(int skillTemplateId, int lv) {
		Map<Integer, ICombatSkillExecution> map = _combatSkillExecutions.get(skillTemplateId);
		return map.get(lv);
	}
	
	static ICombatStateTemplate getCombatStateTemplate(int stateId) {
		return KSupportFactory.getSkillModuleSupport().getStateTemplate(stateId);
	}
	
	static ICombatState getCombatState(ICombatMember operator, ICombatStateTemplate template) {
		ICombatStateParser parser = _combatStateParser.get(template.getStateType());
		if(parser == null) {
			_LOGGER.error("不存在状态的解析器！状态类型：{}，状态id：{}", template.getStateType(), template.getStateTemplateId());
		}
		return parser.newInstance(operator, template);
	}
	
	static void removeCombat(int combatId) {
		ICombat combat = _allCurrentCombats.remove(combatId);
		if(combat instanceof KCombatImpl) {
			KCombatPool.returnCombat((KCombatImpl)combat);
		}
	}
	
    static IVitoryCondition getVitoryCondition(int combatType) {
		return _combatVitoryConditions.get(combatType);
	}
	
	@SuppressWarnings("unchecked")
	static void loadCombatResultHandler(String path) throws Exception {
		Element root = XmlUtil.openXml(path).getRootElement();
		Map<Integer, ICombatResultHandler> allProcessers = new HashMap<Integer, ICombatResultHandler>();
		Map<Integer, IVitoryCondition> allVitoryConditions = new HashMap<Integer, IVitoryCondition>();
		Map<Integer, ICombatPlugin> plugins = new HashMap<Integer, ICombatPlugin>();
		
		List<Element> elementList = root.getChild("resultHandlers").getChildren();
		Element temp;
		for (int i = 0; i < elementList.size(); i++) {
			temp = elementList.get(i);
			allProcessers.put(Integer.parseInt(temp.getAttributeValue("type")), (ICombatResultHandler)Class.forName(temp.getAttributeValue("clazzPath")).newInstance());
		}
		
		elementList = root.getChild("vitoryConditions").getChildren();
		for (int i = 0; i < elementList.size(); i++) {
			temp = elementList.get(i);
			allVitoryConditions.put(Integer.parseInt(temp.getAttributeValue("type")), (IVitoryCondition)Class.forName(temp.getAttributeValue("clazzPath")).newInstance());
		}
		
		elementList = root.getChild("plugins").getChildren();
		for(int i = 0; i < elementList.size(); i++) {
			temp = elementList.get(i);
			plugins.put(Integer.parseInt(temp.getAttributeValue("type")), (ICombatPlugin)Class.forName(temp.getAttributeValue("clazzPath")).newInstance());
		}
		
		elementList = root.getChild("combatConfigs").getChildren();
		for(int i = 0; i < elementList.size(); i++) {
			temp = elementList.get(i);
			int combatType = Integer.parseInt(temp.getChildTextTrim("combatType"));
			KCombatType type = KCombatType.getCombatType(combatType);
			type.setCanUsePVPSkill(Boolean.parseBoolean(temp.getChildTextTrim("canUsePVPSkill")));
			type.setCanAutoFight(Boolean.parseBoolean(temp.getChildTextTrim("canAutoFight")));
			type.setUseServerResIdOfMonster(Boolean.parseBoolean(temp.getChildTextTrim("useServerMonsterResId")));
			int[] array = UtilTool.getStringToIntArray(temp.getChildTextTrim("resultHandlers"), ",");
			List<ICombatResultHandler> handlerList = new ArrayList<ICombatResultHandler>();
			for(int k = 0; k < array.length; k++) {
				handlerList.add(allProcessers.get(array[k]));
			}
			_combatResultHandlers.put(combatType, handlerList);
			_combatVitoryConditions.put(combatType, allVitoryConditions.get(Integer.parseInt(temp.getChildTextTrim("vitoryCondition"))));
			array = UtilTool.getStringToIntArray(temp.getChildTextTrim("plugin"), ",");
			List<ICombatPlugin> pluginList = null;
			if (array != null && array.length > 0) {
				pluginList = new ArrayList<ICombatPlugin>();
				for (int k = 0; k < array.length; k++) {
					pluginList.add(plugins.get(array[k]));
				}
			} else {
				pluginList = Collections.emptyList();
			}
			_combatPlugin.put(combatType, pluginList);
		}
	}
	
	static void loadSkillExecutionParser(String path) throws Exception {
		Document doc = XmlUtil.openXml(path);
		@SuppressWarnings("unchecked")
		List<Element> children = doc.getRootElement().getChildren();
		Element child;
		for(int i = 0; i < children.size(); i++) {
			child = children.get(i);
			_combatSkillExecutionParsers.put(Integer.parseInt(child.getAttributeValue("type")), (ICombatSkillExecutionParser)Class.forName(child.getAttributeValue("clazz")).newInstance());
		}
	}
	
	static void loadStateParser(String path) throws Exception {
		Document doc = XmlUtil.openXml(path);
		Element root = doc.getRootElement();
		@SuppressWarnings("unchecked")
		List<Element> children = root.getChildren("stateParser");
		Element child;
		int stateType;
		ICombatStateParser parser;
		for (int i = 0; i < children.size(); i++) {
			child = children.get(i);
			stateType = Integer.parseInt(child.getAttributeValue("type"));
			parser = (ICombatStateParser) Class.forName(child.getAttributeValue("clazz")).newInstance();
			_combatStateParser.put(stateType, parser);
		}
	}
	 
	static void loadOperationMsgHandler(Element element) throws Exception {
		_opMsgHandler = (IOperationMsgHandler) Class.forName(element.getAttributeValue("clazz").trim()).newInstance();
	}
	
	static void loadCommandCreator(Element element) throws Exception {
		_cmdCreator = (ICombatCommandCreator) Class.forName(element.getAttributeValue("clazz").trim()).newInstance();
	}
	
	static void loadMirrorDataHandler(Element element) throws Exception {
		_mirrorDataHandler = (ICombatMirrorDataHandler) Class.forName(element.getAttributeValue("clazz").trim()).newInstance();
	}
	
	static void loadSkillExecution() {
		@SuppressWarnings("deprecation")
		List<ICombatSkillTemplateData> list = KSupportFactory.getSkillModuleSupport().getAllSkillTemplateData();
		ICombatSkillTemplateData template;
		ICombatSkillExecutionParser tempParser;
		Map<Integer, ICombatSkillExecution> executionMap;
		int exceptionCount = 0;
		for(int i = 0; i < list.size(); i++) {
			template = list.get(i);
			tempParser = _combatSkillExecutionParsers.get(template.getSkillType());
			executionMap = _combatSkillExecutions.get(template.getSkillTemplateId());
			if(executionMap == null) {
				executionMap = new HashMap<Integer, ICombatSkillExecution>();
				_combatSkillExecutions.put(template.getSkillTemplateId(), executionMap);
			}
			try {
				executionMap.put(template.getSkillLv(), tempParser.parse(template.getSkillTemplateId(), template.getSkillLv(), template.getSkillArgs()));
			} catch (Exception e) {
				_LOGGER.error("解析技能出现异常，技能id：{}", template.getSkillTemplateId(), e);
				exceptionCount++;
			}
		}
		if(exceptionCount > 0) {
			throw new RuntimeException("加载技能execution过程中出现问题！");
		}
	}
	
	static void loadSpecialEffectFunctionParser(String path) throws Exception {
		Document doc = XmlUtil.openXml(path);
		Element root = doc.getRootElement();
		@SuppressWarnings("unchecked")
		List<Element> list = root.getChild("functionParsers").getChildren();
		Element child;
		for (int i = 0; i < list.size(); i++) {
			child = list.get(i);
			_allFunctionParser.put(Integer.parseInt(child.getAttributeValue("type")), (IFunctionParser) Class.forName(child.getAttributeValue("clazz")).newInstance());
		}
		KConditionFactory.loadConditionType(root.getChild("functionCondition"));
	}
	
	static void loadSpecialEffectFunction() throws Exception {
		@SuppressWarnings("deprecation")
		List<ICombatStateTemplate> list = KSupportFactory.getSkillModuleSupport().getAllStateTemplateData();
		ICombatStateTemplate template;
		IFunctionParser parser = null;
		List<Integer> paras = null;
		for (int i = 0; i < list.size(); i++) {
			template = list.get(i);
			switch (template.getStateType()) {
			case ICombatStateTemplate.STATE_TYPE_ADD_STATE_TO_TARGET:
			case ICombatStateTemplate.STATE_TYPE_ADD_STATE_TO_OWN:
				parser = _allFunctionParser.get(IFunctionExecution.FUNCTION_TYPE_ADD_STATE);
				break;
			case ICombatStateTemplate.STATE_TYPE_ABSORB_HP:
				parser = _allFunctionParser.get(IFunctionExecution.FUNCTION_TYPE_ABSORB_HP);
				break;
			}
			if (parser != null) {
				paras = new ArrayList<Integer>(template.getParas().length + 1);
				for (int k = 0; k < template.getParas().length; k++) {
					paras.add(template.getParas()[k]);
				}
				paras.add(template.getStateType());
				KCombatSpecialEffect effect = new KCombatSpecialEffect(ICombatEvent.EVENT_AFTER_ATTACK, true, parser.parse(paras), KConditionFactory.getCondition(new String[]{String.valueOf(ICondition.CONDITION_ALIVE)}, new String[]{""}));
				KCombatSpecialEffect pre = _allSpecialEffect.put(template.getStateTemplateId(), effect);
				if(pre != null) {
					throw new RuntimeException("重复的战斗特殊效果：" + template.getStateTemplateId());
				}
				parser = null;
				paras = null;
			}
		}
		_addEnergyEffect = new KCombatSpecialEffect(ICombatEvent.EVENT_SELF_DEAD, true, _allFunctionParser.get(IFunctionExecution.FUNCTION_TYPE_ADD_ENERGY).parse(null), KConditionFactory.getCondition(new String[]{String.valueOf(ICondition.CONDITION_DEAD)}, new String[]{""}));
	}
	
	static void loadPassiveSkillSpecialEffect() throws Exception {
		@SuppressWarnings("deprecation")
		List<ICombatSkillTemplateData> list = KSupportFactory.getSkillModuleSupport().getAllPassiveSkillTemplateData();
		ICombatSkillTemplateData template;
		KRolePasSkillTemp passSkillTemplate;
		for (int i = 0; i < list.size(); i++) {
			template = list.get(i);
			passSkillTemplate = KSupportFactory.getSkillModuleSupport().getPasSkillTemplate(template.getSkillTemplateId());
			switch (passSkillTemplate.skilltype) {
			case ICombatSkillTemplateData.PASSIVE_SKILL_SPECIAL_EFFECT:
				IFunctionParser parser = _allFunctionParser.get(IFunctionExecution.FUNCTION_TYPE_ADD_STATE);
				List<Integer> paras = new ArrayList<Integer>(template.getSkillArgs().size() + 1);
				paras.addAll(template.getSkillArgs());
				paras.add(template.getSkillTemplateId());
				KCombatSpecialEffect effect = new KCombatSpecialEffect(ICombatEvent.EVENT_AFTER_ATTACK, true, parser.parse(paras), KConditionFactory.getCondition(new String[]{String.valueOf(ICondition.CONDITION_ALIVE)}, new String[]{""}));
				Map<Integer, KCombatSpecialEffect> map = _allPasSkillSpecialEffect.get(template.getSkillTemplateId());
				if (map == null) {
					map = new HashMap<Integer, KCombatSpecialEffect>();
					_allPasSkillSpecialEffect.put(template.getSkillTemplateId(), map);
				}
				map.put(template.getSkillLv(), effect);
				break;
			case ICombatSkillTemplateData.PASSIVE_SKILL_INCREASE_ATTR:
				continue;
			case ICombatSkillTemplateData.PASSIVE_SKILL_PVP_ADD_STATE:
				List<Integer> stateParas = template.getSkillArgs();
				List<Integer> stateIds = new ArrayList<Integer>();
				int tempStateId;
				for(int k = 0; k < stateParas.size(); k++) {
					tempStateId = stateParas.get(k);
					if(tempStateId > 0) {
						stateIds.add(tempStateId);
					}
				}
				_passSkillAddStateMap.put(passSkillTemplate.id, Collections.unmodifiableList(stateIds));
				continue;
			}
		}
	}
	
	static void loadObstructionStates() {
		ObstructionTemplate temp;
		Map<Integer, ObstructionTemplate> allObstructions = KSupportFactory.getNpcModuleSupport().getAllObstructionTemps();
		for (Iterator<ObstructionTemplate> itr = allObstructions.values().iterator(); itr.hasNext();) {
			temp = itr.next();
			_obstStatesByAttack.put(temp.id, temp.directstatus_id);
			_obstStatesAfterDestroyed.put(temp.id, temp.destroystatus_id);
		}
	}
	
	static void loadRandomObstructions(String path) throws Exception {
		KGameExcelFile file = new KGameExcelFile(path);
		KGameExcelRow[] allRows = file.getTable("关卡随机障碍物", 5).getAllDataRows();
		KRandomObstruction temp;
		Map<Integer, KRandomObstruction> map;
		byte levelType;
		for (int i = 0; i < allRows.length; i++) {
			levelType = allRows[i].getByte("levelType");
			map = _randomObstructions.get(levelType);
			if (map == null) {
				map = new HashMap<Integer, KRandomObstruction>();
				_randomObstructions.put(levelType, map);
			}
			temp = new KRandomObstruction(allRows[i]);
			map.put(temp.levelId, temp);
		}
	}
	
	static void setAssistantPetPath(String path) {
		_assistantPetPath = path;
	}
	
	static void loadAssistantPetOfGameLevel() throws Exception {
		KGameExcelFile file = new KGameExcelFile(_assistantPetPath);
		KGameExcelRow[] allRows = file.getTable("随从帮打", 2).getAllDataRows();
		KGameExcelRow row;
		for (int i = 0; i < allRows.length; i++) {
			row = allRows[i];
			int levelId = row.getInt("levelId");
			int petTemplateId = row.getInt("petTemplateId");
			ICombatPet combatPet = KSupportFactory.getPetModuleSupport().createFightingPet(petTemplateId);
			if (combatPet == null) {
				throw new RuntimeException("加载随从帮打出现异常，找不到指定的随从，关卡id：" + levelId + "，随从模板id：" + petTemplateId);
			}
			_assistantPetInstanceMap.put(levelId, combatPet);
		}
	}
	
//	static void loadMountConsume() {
//		Map<Integer, Integer> map = KSupportFactory.getMountModuleSupport().getMountConsume();
//		if(map.size() > 0) {
//			_mountConsumeMap.putAll(map);
//		} else {
//			throw new RuntimeException("机甲消耗数据不存在！");
//		}
//	}
	
	static List<Integer> getPassAddStates(int skillTemplateId) {
		return _passSkillAddStateMap.get(skillTemplateId);
	}
	
	static void submitCombatMonitor() {
		final int delay = 60;
		KGame.newTimeSignal(new KGameTimerTask() {
			
			private final Logger _logger = KGameLogger.getLogger("combatPoolLogger");

			@Override
			public void rejected(RejectedExecutionException e) {

			}

			@Override
			public Object onTimeSignal(KGameTimeSignal timeSignal) throws KGameServerException {
				_logger.info("当前战场数量：{}", _allCurrentCombats.size());
				_logger.info("entrancePool状态：总共借出={}，总共归还={}，cacheSize={}, temporarySize={}", KCombatEntrancePool.getBorrowCount(), KCombatEntrancePool.getReturnCount(),
						KCombatEntrancePool.getCacheSize(), KCombatEntrancePool.getTemporarysize());
				_logger.info("combatMemberPool状态：总共借出={}，总共归还={}，cacheSize={}，temporarySize={}", KCombatMemberFactory.getBorrowCount(), KCombatMemberFactory.getReturnCount(),
						KCombatMemberFactory.getCacheSize(), KCombatMemberFactory.getTemporarySize());
				_logger.info("combatPool状态：总共借出={}，总共归还={}，cacheSize={}，temporarySize={}", KCombatPool.getBorrowCount(), KCombatPool.getReturnCount(), KCombatPool.getCacheSize(),
						KCombatPool.getTemporarySize());
				_logger.info("combatRecorderPool状态：总共借出={}，总共归还={}，roleTypeCacheSize={}，otherTypeCacheSize={}，temporarySize={}", KCombatRecorderPool.getBorrowCount(),
						KCombatRecorderPool.getReturnCount(), KCombatRecorderPool.getRoleTypeSize(), KCombatRecorderPool.getOtherTypeSize(), KCombatRecorderPool.getTemporarysize());
				_logger.info("combatGroundPool状态：{}", KCombatGroundPool.getInfo());
				timeSignal.getTimer().newTimeSignal(this, delay, TimeUnit.SECONDS);
				return "success";
			}

			@Override
			public String getName() {
				return "combatMonitor";
			}

			@Override
			public void done(KGameTimeSignal timeSignal) {

			}
		}, delay, TimeUnit.SECONDS);
	}
	
	static void processCombatFinish(ICombat combat, ICombatResult result) {
		List<ICombatResultHandler> handlers = _combatResultHandlers.get(combat.getCombatType().sign);
		for (int i = 0; i < handlers.size(); i++) {
			try {
				handlers.get(i).processCombatFinish(combat, result);
			} catch (Exception e) {
				_LOGGER.error("处理战斗结束时出现异常！战场id：{}", combat.getSerialId(), e);
			}
		}
	}
	
	static void processCombatResultToRole(long roleId, ICombat combat, ICombatResult result, boolean kick) {
		List<ICombatResultHandler> handlers = _combatResultHandlers.get(combat.getCombatType().sign);
		try {
			for (int i = 0; i < handlers.size(); i++) {
				try {
					handlers.get(i).processCombatResultToRole(roleId, combat, result);
				} catch (Exception e) {
					_LOGGER.error("处理战斗结束时出现异常！战场id：{}", combat.getSerialId(), e);
				} catch (Throwable t) {
					_LOGGER.error("处理战斗结束时出现错误！战场id：{}", combat.getSerialId(), t);
				}
			}
		} finally {
			if (kick) {
				KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
				if (role != null) {
					role.setNotFighting();
				}
			}
		}
	}
	
	static void processRoleExitCombatFinish(long roleId, ICombat combat, ICombatResult result) {
		List<ICombatResultHandler> handlers = _combatResultHandlers.get(combat.getCombatType().sign);
		for (int i = 0; i < handlers.size(); i++) {
			try {
				handlers.get(i).processRoleExitCombatFinish(roleId, result);
			} catch (Exception e) {
				_LOGGER.error("通知处理器，角色退出战斗完毕时出现异常！战场id：{}", combat.getSerialId(), e);
			}
		}
		ICombatMember member = combat.getRoleTypeMemberBySrcId(roleId);
		if (member != null) {
			KSupportFactory.getRoleModuleSupport().syncEnergy(roleId, member.getCurrentEnergy(), member.getCurrentEnergyBean());
		}
	}
	
	static void processRoleEscapeAfterCombatFinish(long roleId) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if(role != null) {
			KSupportFactory.getMapSupport().processRoleFinishBattleReturnToMap(role);
			KDialogService.sendNullDialog(role);
		}
	}
	
	static void handleRoleLeaveGame(KRole role) {
		ICombat combat = getCombat(role.getId());
		if(combat != null) {
			combat.handleRoleLeaveGame(role.getId());
		}
	}
	
	static void handleRoleJoinGame(KRole role) {
		_offlineData.remove(role.getId());
	}
	
	static void setRoleNotFighting(long roleId) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
		if (role != null) {
			setRoleNotFighting(role);
		} else {
			_LOGGER.error("KCombatManager#setRoleNotFighting(long)找不到对应的角色，角色id：{}", roleId);
		}
	}
	
	static void setRoleNotFighting(KRole role) {
		role.setNotFighting();
		try {
			KSupportFactory.getDuplicateMapSupport().notifyPlayerRoleFightStatus(role, false);
		} catch (Exception e) {
			_LOGGER.error("通知副本地图角色离开战斗出现异常，角色id：{}", role.getId(), e);
		}
	}
	
	static ICombat getCombat(KGamePlayerSession session) {
		KRole role = KSupportFactory.getRoleModuleSupport().getRole(session);
		if (role != null) {
			return getCombat(role.getId());
		} else {
			return null;
		}
	}
	
	static ICombat getCombat(long roleId) {
		Integer combatId = _roleIdToCombatId.get(roleId);
		if (combatId != null) {
			return _allCurrentCombats.get(combatId);
		} else {
			return null;
		}
	}
	
	static KCombatSpecialEffect getSpecialEffect(int effectId) {
		return _allSpecialEffect.get(effectId);
	}
	
	static KCombatSpecialEffect getPassiveSkillSpecialEffect(int skillTemplateId, int lv) {
		Map<Integer, KCombatSpecialEffect> map = _allPasSkillSpecialEffect.get(skillTemplateId);
		if (map != null) {
			return map.get(lv);
		}
		return null;
	}
	
	/**
	 * <pre>
	 * 获取技能所对应的召唤物的编号
	 * 如果不是召唤类型的技能，将会返回0
	 * </pre>
	 * @param skillTemplateId
	 * @param skillLv
	 * @return
	 */
	static int getMinionTemplateId(int skillTemplateId, int skillLv) {
		Map<Integer, Integer> map = _summonSkillInfoMap.get(skillTemplateId);
		if(map != null) {
			Integer templateId = map.get(skillLv);
			if(templateId != null) {
				return templateId;
			}
		}
		return 0;
	}
	
	static void forceFinishCombat(KRole role, KCombatType type, boolean isRoleWin) {
		if(role.isFighting()) {
			ICombat combat = getCombat(role.getId());
			if(combat != null && combat.getCombatType() == type) {
				combat.submitCommand(_cmdCreator.createForceFinishCommand(combat, isRoleWin));
			}
		}
	}
	
	static void registerRoleSideHpUpdater(ICombatRoleSideHpUpdater pUpdater) {
		_roleSideHpUpdater.put(pUpdater.getCombatTypeResponse(), pUpdater);
	}
	
	static void startGameLevelBattle(KRole role, List<KGameBattlefield> battleFieldInfos, List<Animation> animationInfos, KCombatType combatType) {
		KGameBattlefield beginBattleField = null;
		KGameBattlefield lastBattleField = null;
		KGameBattlefield currentBattleField = null;
		List<ICombatGroundBuilder> combatGroundBuilderList = new ArrayList<ICombatGroundBuilder>();
		KCombatGroundBuilder builder;
		for (int i = 0; i < battleFieldInfos.size(); i++) {
			currentBattleField = battleFieldInfos.get(i);
			builder = createGameLevelGroundBuilder(currentBattleField, null, true);
			combatGroundBuilderList.add(builder);
			if (currentBattleField.isFirstBattlefield()) {
				beginBattleField = currentBattleField;
			} else if (currentBattleField.isLastBattlefield()) {
				lastBattleField = currentBattleField;
			}
		}
		if (lastBattleField == null) {
			lastBattleField = beginBattleField;
		}
		KCombatGameLevelInfo gameLevelInfo = new KCombatGameLevelInfo(lastBattleField.getBattlefieldId(), lastBattleField.getBattlefieldType());
		KStartCombatArgument arg = new KStartCombatArgument();
		arg.gameLevelInfo = gameLevelInfo;
		arg.roleBornX = beginBattleField.getBornPoint()._corX;
		arg.roleBornY = beginBattleField.getBornPoint()._corY;
		arg.canAutoFight = true;
		arg.animationList = animationInfos;
		arg.alsoSendFinishMsgIfEscape = combatType.alsoSendFinishMsgIfEscape;
		if (!KSupportFactory.getLevelSupport().checkGameLevelIsCompleted(role.getId(), lastBattleField.getLevelId())) {
			ICombatPet pet = _assistantPetInstanceMap.get(lastBattleField.getLevelId());
			if (pet != null) {
				arg.helpPet = pet;
			}
		}
		startCombat(role, combatGroundBuilderList, combatType, arg);
	}
	
	static void startNewGoldActivityCombat(KRole role, KGameBattlefield battleField, int goldCalBase, long timeoutMillis) {
		KCombatGroundBuilder builder = createGameLevelGroundBuilder(battleField, null, true);
		List<KCombatEntrance> entranceList = builder.entranceList;
		KCombatEntrance entrance;
		for (int i = 0; i < entranceList.size(); i++) {
			entrance = entranceList.get(i);
			switch (entrance.getMemberType()) {
			case ICombatMember.MEMBER_TYPE_BOSS_MONSTER:
			case ICombatMember.MEMBER_TYPE_ELITIST_MONSTER:
			case ICombatMember.MEMBER_TYPE_MONSTER:
				entrance.changeMemberType(ICombatMember.MEMBER_TYPE_BARREL_MONSTER);
				break;
			}
		}
		KCombatGameLevelInfo gameLevelInfo = new KCombatGameLevelInfo(battleField.getBattlefieldId(), battleField.getBattlefieldType());
		KStartCombatArgument arg = new KStartCombatArgument();
//		long totalHp = battleField.monsterMap.values().iterator().next()._monsterTemplate.allEffects.get(KGameAttrType.MAX_HP);
		long totalHp = 0;
		for(Iterator<MonsterData> itr = battleField.monsterMap.values().iterator(); itr.hasNext();) {
			totalHp += itr.next()._monsterTemplate.allEffects.get(KGameAttrType.MAX_HP);
		}
		arg.gameLevelInfo = gameLevelInfo;
		arg.roleBornX = battleField.getBornPoint()._corX;
		arg.roleBornY = battleField.getBornPoint()._corY;
		arg.eventList = new ArrayList<ICombatEventListener>();
		arg.eventList.add(new KGoldCombatAfterAtkEvent(goldCalBase, totalHp));
		arg.timeOutMillis = (int)timeoutMillis;
		arg.canAutoFight = false;
		startCombat(role, Arrays.asList((ICombatGroundBuilder) builder), KCombatType.BARREL, arg);
	}
	
	static KActionResult<Integer> startFightWithAICombat(KRole role, long defenderId, ICombatEnv env, KCombatType type, Object attachment, int timeOutMillis) {
		KActionResult<Integer> result = new KActionResult<Integer>();
		if (role.isOnline()) {
			KActionResult<List<KCombatEntrance>> entranceResult = getRoleAIEntranceList(defenderId, env.getEnermyCorX(), env.getEnermyCorY());
			List<KCombatEntrance> defenderList = entranceResult.attachment;
			if (defenderList != null) {
				KCombatEntrance temp;
//				int clientInstancingId = 1;
				for (int i = 0; i < defenderList.size(); i++) {
					temp = defenderList.get(i);
					switch (temp.getMemberType()) {
					case ICombatMember.MEMBER_TYPE_ROLE:
						temp.changeMemberType(ICombatMember.MEMBER_TYPE_ROLE_MONSTER);
						temp.changeForceType(ICombatForce.FORCE_TYPE_MONSTER_SIDE);
						break;
					case ICombatMember.MEMBER_TYPE_PET:
						temp.changeMemberType(ICombatMember.MEMBER_TYPE_PET_MONSTER);
						temp.changeForceType(ICombatForce.FORCE_TYPE_MONSTER_SIDE);
						break;
					}
//					temp.setClientInstancingId(clientInstancingId); // 客户端用instancingId来区分
//					clientInstancingId++;
				}
				KCombatGroundBuilder builder = new KCombatGroundBuilder();
				builder.bgAudioResId(env.getBgMusicResId());
				builder.mapResPath(env.getBgResPath());
				builder.entranceList(defenderList);
				builder.addEnergyEffect(_addEnergyEffect);
				KStartCombatArgument arg = new KStartCombatArgument();
				arg.roleBornX = env.getBornCorX();
				arg.roleBornY = env.getBornCorY();
				arg.resultAttachment = attachment;
				arg.timeOutMillis = timeOutMillis;
				arg.canAutoFight = true;
				result = startCombat(role, Arrays.asList((ICombatGroundBuilder) builder), type, arg);
			} else {
				result.tips = entranceResult.tips;
				result.attachment = ICombatConstant.ERR_CODE_TARGET_NOT_EXIST;
			}
		} else {
			result.tips = CombatTips.getTipsRoleIsOffline(role.getName());
			result.attachment = ICombatConstant.ERR_CODE_ROLE_IS_OFFLINE;
		}
		return result;
	}
	
	private static KActionResult<Integer> startFightWithAICombatWithTeammate(KRole role, long teammateId, List<KCombatEntrance> enemies, KCombatType type, ICombatEnvPlus env, Object attachment, long timeoutMillis) {
		KActionResult<Integer> result = new KActionResult<Integer>();
		if(role.isOnline()) {
			float[] roleCors = env.getRoleCorDatas().get(1);
			KActionResult<List<KCombatEntrance>> entranceResult = getRoleAIEntranceList(teammateId, roleCors[0], roleCors[1]);
			List<KCombatEntrance> teammateList = entranceResult.attachment;
			if (teammateList != null) {
				KCombatEntrance temp;
				for (int i = 0; i < teammateList.size(); i++) {
					temp = teammateList.get(i);
					switch (temp.getMemberType()) {
					case ICombatMember.MEMBER_TYPE_ROLE:
						temp.changeMemberType(ICombatMember.MEMBER_TYPE_TEAM_MATE_ROLE);
						break;
					case ICombatMember.MEMBER_TYPE_PET:
						temp.changeMemberType(ICombatMember.MEMBER_TYPE_TEAM_MATE_PET);
						break;
					}
				}
			}
			roleCors = env.getRoleCorDatas().get(0);
			KCombatGroundBuilder builder = new KCombatGroundBuilder();
			builder.bgAudioResId(env.getBgMusicResId());
			builder.mapResPath(env.getBgResPath());
			builder.entranceList(enemies);
			builder.addEnergyEffect(_addEnergyEffect);
			KStartCombatArgument arg = new KStartCombatArgument();
			arg.assistants = teammateList;
			arg.roleBornX = roleCors[0];
			arg.roleBornY = roleCors[1];
			arg.resultAttachment = attachment;
			arg.alsoSendFinishMsgIfEscape = true;
			if(timeoutMillis > 0) {
				arg.timeOutMillis = (int)timeoutMillis;
			}
			arg.canAutoFight = true;
			result = startCombat(role, Arrays.asList((ICombatGroundBuilder) builder), type, arg);
		} else {
			result.tips = CombatTips.getTipsRoleIsOffline(role.getName());
			result.attachment = ICombatConstant.ERR_CODE_ROLE_IS_OFFLINE;
		}
		return result;
	}
	
	static KActionResult<Integer> startFightWithAICombat(KRole role, long teammateId, long[] enemyIds, KCombatType type, ICombatEnvPlus env, Object attachment, long timeoutMillis) {
		KActionResult<Integer> result = new KActionResult<Integer>();
		if(role.isOnline()) {
			KActionResult<List<KCombatEntrance>> entranceResult = null;
			List<KCombatEntrance> enemies = new ArrayList<KCombatEntrance>();
			float[] cors;
			List<float[]> allCors = env.getEnemyCorDatas();
			for(int i = 0; i < enemyIds.length; i++) {
				cors = allCors.get(i);
				entranceResult = getRoleAIEntranceList(teammateId, cors[0], cors[1]);
				if(entranceResult.attachment != null) {
					enemies.addAll(entranceResult.attachment);
				}
			}
			if (enemies.isEmpty()) {
				result.tips = CombatTips.getTipsTargetNotExists();
				result.attachment = ICombatConstant.ERR_CODE_TARGET_NOT_EXIST;
			} else {
				KCombatEntrance temp;
				for (int i = 0; i < enemies.size(); i++) {
					temp = enemies.get(i);
					switch (temp.getMemberType()) {
					case ICombatMember.MEMBER_TYPE_ROLE:
						temp.changeMemberType(ICombatMember.MEMBER_TYPE_ROLE_MONSTER);
						temp.changeForceType(ICombatForce.FORCE_TYPE_MONSTER_SIDE);
						break;
					case ICombatMember.MEMBER_TYPE_PET:
						temp.changeMemberType(ICombatMember.MEMBER_TYPE_PET_MONSTER);
						temp.changeForceType(ICombatForce.FORCE_TYPE_MONSTER_SIDE);
						break;
					}
				}
				result = startFightWithAICombatWithTeammate(role, teammateId, enemies, type, env, attachment, timeoutMillis);
			}
		} else {
			result.tips = CombatTips.getTipsRoleIsOffline(role.getName());
			result.attachment = ICombatConstant.ERR_CODE_ROLE_IS_OFFLINE;
		}
		return result;
	}
	
	static KActionResult<Integer> startFightWithAICombatByEnemies(KRole role, long teammateId, List<ICombatMirrorDataGroup> list, KCombatType type, ICombatEnvPlus env, Object attachment, long timeoutMillis) {
		KActionResult<Integer> result = new KActionResult<Integer>();
		if(role.isOnline()) {
			List<KCombatEntrance> enemies = new ArrayList<KCombatEntrance>();
			List<float[]> allCors = env.getEnemyCorDatas();
			float[] cors;
			ICombatMirrorDataGroup dataGroup;
			for(int i = 0; i < list.size(); i++) {
				cors = allCors.get(i);
//				enemies.add(KCombatEntrancePool.borrowEntrance(list.get(i), null, cors[0], cors[1], 0));
				dataGroup = list.get(i);
				if(dataGroup.getMountMirror() != null) {
					KCombatEntrance mountEntrance = KCombatEntrancePool.borrowEntrance(dataGroup.getMountMirror(), null, 0, 0, 0);
					enemies.add(KCombatEntrancePool.borrowEntrance(dataGroup.getRoleMirror(), mountEntrance, cors[0], cors[1], 0));
					mountEntrance.inheritBasicAttribute(dataGroup.getRoleMirror());
					mountEntrance.initVehicleAttr(dataGroup.getRoleMirror());
				} else {
					enemies.add(KCombatEntrancePool.borrowEntrance(dataGroup.getRoleMirror(), null, cors[0], cors[1], 0));
				}
				if (dataGroup.getPetMirror() != null) {
					enemies.add(KCombatEntrancePool.borrowEntrance(dataGroup.getPetMirror(), null, cors[0] + SPACE_BETWEEN_PET_AND_ROLE, cors[1], 0));
				}
			}
			if (enemies.isEmpty()) {
				result.tips = CombatTips.getTipsTargetNotExists();
				result.attachment = ICombatConstant.ERR_CODE_TARGET_NOT_EXIST;
			} else {
				KCombatEntrance temp;
				for (int i = 0; i < enemies.size(); i++) {
					temp = enemies.get(i);
					switch (temp.getMemberType()) {
					case ICombatMember.MEMBER_TYPE_ROLE:
						temp.changeMemberType(ICombatMember.MEMBER_TYPE_ROLE_MONSTER);
						temp.changeForceType(ICombatForce.FORCE_TYPE_MONSTER_SIDE);
						break;
					case ICombatMember.MEMBER_TYPE_PET:
						temp.changeMemberType(ICombatMember.MEMBER_TYPE_PET_MONSTER);
						temp.changeForceType(ICombatForce.FORCE_TYPE_MONSTER_SIDE);
						break;
					}
				}
				result = startFightWithAICombatWithTeammate(role, teammateId, enemies, type, env, attachment, timeoutMillis);
			}
		} else {
			result.tips = CombatTips.getTipsRoleIsOffline(role.getName());
			result.attachment = ICombatConstant.ERR_CODE_ROLE_IS_OFFLINE;
		}
		return result;
	}
	
	static void startTowerCombat(KRole role, long friendId, KTowerBattlefield battleFieldInfo) {
		if(role.isOnline()) {
			KActionResult<List<KCombatEntrance>> friendResult = getRoleAIEntranceList(friendId, battleFieldInfo.getFriendBornPoint()._corX, battleFieldInfo.getFriendBornPoint()._corY);
			List<KCombatEntrance> friendList = friendResult.attachment;
			if (friendList != null) {
				KCombatEntrance temp;
				for(int i = 0; i < friendList.size(); i++) {
					temp = friendList.get(i);
					switch(temp.getMemberType()) {
					case ICombatMember.MEMBER_TYPE_ROLE:
						temp.changeMemberType(ICombatMember.MEMBER_TYPE_TEAM_MATE_ROLE);
						break;
					case ICombatMember.MEMBER_TYPE_PET:
						temp.changeMemberType(ICombatMember.MEMBER_TYPE_TEAM_MATE_PET);
						break;
					}
				}
			} else {
				friendList = new ArrayList<KCombatEntrance>();
			}
			if (battleFieldInfo.getDefenceTowerData() != null) {
				ICombatMonster defenderMonster = KSupportFactory.getNpcModuleSupport().getCombatMonster(battleFieldInfo.getDefenceTowerData()._monsterTemplate);
//				KCombatEntrance defenderEntrance = new KCombatEntrance(defenderMonster, null, battleFieldInfo.getDefenceTowerData()._corX, battleFieldInfo.getDefenceTowerData()._corY, 0);
				KCombatEntrance defenderEntrance = KCombatEntrancePool.borrowEntrance(defenderMonster, null, battleFieldInfo.getDefenceTowerData()._corX, battleFieldInfo.getDefenceTowerData()._corY, 0);
				defenderEntrance.changeForceType(ICombatForce.FORCE_TYPE_ROLE_SIDE);
				defenderEntrance.changeMemberType(ICombatMember.MEMBER_TYPE_ASSISTANT);
				friendList.add(defenderEntrance);
			}
			KCombatGroundTowerBuilder towerBuilder = new KCombatGroundTowerBuilder();
			setBasePropertyToBuilder(towerBuilder, battleFieldInfo, _addEnergyEffect);
			KTowerData firstTower = battleFieldInfo.getTowerDataMapByTowerId().get(battleFieldInfo.getBeginTowerId());
			List<KCombatEntrance> monsterEntrances = getTowerMonsters(firstTower.getLeftMonsterMap(), battleFieldInfo.getLeftMonsterBornPoint()._corX, battleFieldInfo.getLeftMonsterBornPoint()._corY);
			monsterEntrances.addAll(getTowerMonsters(firstTower.getRightMonsterMap(), battleFieldInfo.getRightMonsterBornPoint()._corX, battleFieldInfo.getRightMonsterBornPoint()._corY));
			towerBuilder.entranceList(monsterEntrances);
			towerBuilder.currentTowerId(battleFieldInfo.getBeginTowerId());
			towerBuilder.towerDatas(battleFieldInfo.getTowerDataMapByTowerId());
			towerBuilder.nextTowerId(firstTower.getNextTowerId());
			towerBuilder.leftBornPointInfo(battleFieldInfo.getLeftMonsterBornPoint()._corX, battleFieldInfo.getLeftMonsterBornPoint()._corY);
			towerBuilder.rightBornPointInfo(battleFieldInfo.getRightMonsterBornPoint()._corX, battleFieldInfo.getRightMonsterBornPoint()._corY);
			KStartCombatArgument arg = new KStartCombatArgument();
			arg.assistants = friendList;
			arg.roleBornX = battleFieldInfo.getMyBornPoint()._corX;
			arg.roleBornY = battleFieldInfo.getMyBornPoint()._corY;
			arg.gameLevelInfo = new KCombatGameLevelInfo(battleFieldInfo.getBattlefieldId(), battleFieldInfo.getBattlefieldType());
			arg.resultAttachment = friendId;
			arg.alsoSendFinishMsgIfEscape = true;
			arg.totalWaveCount = battleFieldInfo.getTotalWave();
			arg.canAutoFight = true;
			startCombat(role, Arrays.asList((ICombatGroundBuilder)towerBuilder), KCombatType.TOWER_COMBAT, arg);
		}
	}
	
	static void startBarrelCombat(KRole role, KBarrelBattlefield battlefield) {
		Map<BornPointData, List<KBarrelBattleData>> map = battlefield.getBarrelBattleDatas(role.getLevel());
		Map.Entry<BornPointData, List<KBarrelBattleData>> entry;
		List<KBarrelBattleData> list;
		KBarrelBattleData temp;
		KCombatEntrance entrance;
		int size = 0;
		Map<KCombatEntrance, Long> bornTimeMap; // 出现时间
		int dropId = 0;
		int dropRate;
		Map.Entry<Integer, Integer> tempDropEntry;
		for (Iterator<Map.Entry<BornPointData, List<KBarrelBattleData>>> itr = map.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			size += entry.getValue().size();
		}
		bornTimeMap = new HashMap<KCombatEntrance, Long>(size);
		for (Iterator<Map.Entry<BornPointData, List<KBarrelBattleData>>> itr = map.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			list = entry.getValue();
			for (int i = 0; i < list.size(); i++) {
				temp = list.get(i);
				entrance = KCombatEntrancePool.borrowEntrance(KSupportFactory.getNpcModuleSupport().getCombatMonster(temp.monstTemplate), null, temp.bornPoint._corX, temp.bornPoint._corY, 0);
				if (temp.dropMap.size() > 0) {
					dropRate = UtilTool.random(temp.totalDropRate);
					for (Iterator<Map.Entry<Integer, Integer>> itr2 = temp.dropMap.entrySet().iterator(); itr2.hasNext();) {
						tempDropEntry = itr2.next();
						if (dropRate < tempDropEntry.getValue()) {
							dropId = tempDropEntry.getKey();
							break;
						}
					}
				} else {
					dropId = 0;
				}
				entrance.changeMemberType(ICombatMember.MEMBER_TYPE_BARREL_MONSTER);
				entrance.changeDropId(dropId);
				bornTimeMap.put(entrance, temp.bornTimeMillis);
			}
		}
		KStartCombatArgument arg = new KStartCombatArgument();
		KCombatGroundBarrelBuilder barrelBuilder = new KCombatGroundBarrelBuilder();
		setBasePropertyToBuilder(barrelBuilder, battlefield, _addEnergyEffect);
		barrelBuilder.battleFieldTemplateId(battlefield.getBattlefieldId());
		barrelBuilder.entranceMap(bornTimeMap);
		arg.roleBornX = battlefield.getBornPoint()._corX;
		arg.roleBornY = battlefield.getBornPoint()._corY;
		arg.gameLevelInfo = new KCombatGameLevelInfo(battlefield.getBattlefieldId(), KGameBattlefieldTypeEnum.产金活动战场);
		arg.timeOutMillis = (int)battlefield.getTotalBattleTimeMillis();
		arg.canAutoFight = true;
		startCombat(role, Arrays.asList((ICombatGroundBuilder)barrelBuilder), KCombatType.BARREL, arg);
	}
	
	static KActionResult<Integer> startFightByUpdateInfoCombat(KRole role, KGameBattlefield battlefield, List<Animation> animationList, ICombatEnhanceInfo enhance, Map<Integer, ICombatMonsterUpdateInfo> map, List<Long> robotMembers, KCombatType type, Object attachment, int timeOutMillis) {
		KCombatGroundBuilder builder = createGameLevelGroundBuilder(battlefield, map, true);
		if (map != null) {
			builder.updateInfo(map);
		}
		KCombatGameLevelInfo gameLevelInfo = new KCombatGameLevelInfo(battlefield.getBattlefieldId(), battlefield.getBattlefieldType());
		KStartCombatArgument arg = new KStartCombatArgument();
		arg.gameLevelInfo = gameLevelInfo;
		arg.roleBornX = battlefield.getBornPoint()._corX;
		arg.roleBornY = battlefield.getBornPoint()._corY;
		arg.roleEnhanceInfo = enhance;
		arg.animationList = animationList;
		arg.resultAttachment = attachment;
		arg.alsoSendFinishMsgIfEscape = type.alsoSendFinishMsgIfEscape;
		arg.canAutoFight = true;
		if (robotMembers != null && robotMembers.size() > 0) {
			List<KCombatEntrance> robotList = new ArrayList<KCombatEntrance>(robotMembers.size());
			List<KCombatEntrance> tempEntranceList = null;
			for (int i = 0; i < robotMembers.size(); i++) {
				tempEntranceList = getRoleAIEntranceList(robotMembers.get(i), 0, 0).attachment;
				if (tempEntranceList != null) {
					robotList.addAll(tempEntranceList);
				}
			}
			KCombatEntrance temp;
			for (int i = 0; i < robotList.size(); i++) {
				temp = robotList.get(i);
				switch (temp.getSrcObjType()) {
				case ICombatObjectBase.OBJECT_TYPE_ROLE:
					temp.changeMemberType(ICombatMember.MEMBER_TYPE_WORLD_BOSS_OTHER_ROLE);
					break;
				case ICombatObjectBase.OBJECT_TYPE_PET:
					temp.changeMemberType(ICombatMember.MEMBER_TYPE_WORLD_BOSS_OTHER_PET);
					break;
				}
			}
			arg.robotList = robotList;
		}
		if(timeOutMillis > 0) {
			arg.timeOutMillis = timeOutMillis;
		}
		return startCombat(role, Arrays.asList((ICombatGroundBuilder) builder), type, arg);
	}
	
	static void startPetCopyCombat(KRole role, KPetCopyBattlefield battlefield) {
		KCombatGroundBuilder combatGroundBuilder = createGameLevelGroundBuilder(battlefield, null, false);
		List<KCombatEntrance> obstructionEntrances = new ArrayList<KCombatEntrance>();
		List<BattlefieldWaveViewInfo> allWaveInfos = battlefield.allWaveInfo;
		BattlefieldWaveViewInfo tempWave;
		List<ObstructionData> obstructionList;
		KPetCopyBattlefieldDropData dropData;
		ObstructionData currentObstruction;
		KCombatEntrance entrance;
		ICombatObjectBase combatObstruction;
		Map<Integer, Map<KMonstTemplate, Integer>> moreMonsterTemplates = new HashMap<Integer, Map<KMonstTemplate, Integer>>();
		Map<Integer, KPetCopyBattlefieldDropData> dropMap = new HashMap<Integer, KPetCopyBattlefieldDropData>();
		for(int i = 0; i < allWaveInfos.size(); i++) {
			tempWave = allWaveInfos.get(i);
			obstructionList = tempWave.getAllObstructions();
			for(int k = 0; k < obstructionList.size(); k++) {
				currentObstruction = obstructionList.get(k);
				dropData = battlefield.dropMap.get(currentObstruction);
				combatObstruction = KSupportFactory.getNpcModuleSupport().getCombatObstruction(currentObstruction._obsTemplate);
				entrance = KCombatEntrancePool.borrowEntrance(combatObstruction, null, currentObstruction._corX, currentObstruction._corY, currentObstruction._objInstanceId);
				obstructionEntrances.add(entrance);
				entrance.changeForceType(ICombatForce.FORCE_TYPE_MONSTER_SIDE);
				if (dropData != null) {
					switch (dropData.dropType) {
					case CURRENCY:
						entrance.changeDropInfos(KCombatDropInfoFactory.createCurrencyDropInfo(dropData.currencyMap, dropData.resId));
						break;
					case ITEM:
						entrance.changeDropInfos(KCombatDropInfoFactory.createItemDropInfo(dropData.itemMap, dropData.resId));
						break;
					default:
					case MONSTER:
						entrance.clearDropInfos();
						Map.Entry<Integer, Integer> entry;
						KMonstTemplate template;
						Map<KMonstTemplate, Integer> tempMap = new HashMap<KMonstTemplate, Integer>();
						for(Iterator<Map.Entry<Integer, Integer>> itr = dropData.monsterMap.entrySet().iterator(); itr.hasNext();) {
							entry = itr.next();
							template = KSupportFactory.getNpcModuleSupport().getMonstTemplate(entry.getKey());
							if (template != null) {
								tempMap.put(template, entry.getValue());
							}
						}
						moreMonsterTemplates.put(entrance.getClientInstancingId(), tempMap);
						break;
					}
					dropMap.put(entrance.getClientInstancingId(), dropData);
				}
			}
			if(moreMonsterTemplates.size() > 0) {
				combatGroundBuilder.genMonstersWhenDead(moreMonsterTemplates);
			}
		}
		combatGroundBuilder.entranceList.addAll(obstructionEntrances);
		KStartCombatArgument arg = new KStartCombatArgument();
		arg.gameLevelInfo = new KCombatGameLevelInfo(battlefield.battlefieldId, KGameBattlefieldTypeEnum.随从副本战场);
		arg.roleBornX = battlefield.bornPoint._corX;
		arg.roleBornY = battlefield.bornPoint._corY;
		arg.timeOutMillis = (int)battlefield.battleTimeMillis;
		arg.resultAttachment = dropMap;
		arg.alsoSendFinishMsgIfEscape = true;
		arg.canAutoFight = false;
		startCombat(role, Arrays.asList((ICombatGroundBuilder)combatGroundBuilder), KCombatType.PET_COPY, arg);
	}
	
	static int getBattlePower(long roleId) {
		ICombatMirrorDataGroup mirrorData = _offlineData.get(roleId);
		if(mirrorData != null) {
			return mirrorData.getRoleMirror().getBattlePower();
		} else {
			KRole role = KSupportFactory.getRoleModuleSupport().getRole(roleId);
			if(role != null) {
				return role.getBattlePower();
			}
			return 0;
		}
	}
	
	public static void addSummonSkillInfo(int skillTemplateId, int skillLv, int minionTemplateId) {
		ICombatMinionTemplateData template = KSupportFactory.getSkillModuleSupport().getMinionTemplateData(minionTemplateId);
		if(template == null) {
			throw new RuntimeException("不存在召唤物模板数据，模板id是：" + minionTemplateId + "，技能id是：" + skillTemplateId + "，技能等级是：" + skillLv);
		}
		Map<Integer, Integer> map = _summonSkillInfoMap.get(skillTemplateId);
		if (map == null) {
			map = new HashMap<Integer, Integer>();
			_summonSkillInfoMap.put(skillTemplateId, map);
		}
		map.put(skillLv, minionTemplateId);
	}
	
	/**
	 * 
	 * <pre>
	 * 创建战场召唤物实例，如果不存在模板会返回null
	 * <pre>
	 * 
	 * @param ruleTemplateId
	 * @param master
	 * @return
	 */
	public static ICombatMinion getCombatMinion(int skillTemplateId, int skillLv, ICombatMember master) {
		int minionTemplateId = getMinionTemplateId(skillTemplateId, skillLv);
		if (minionTemplateId > 0) {
			ICombatMinionTemplateData templateData = KSupportFactory.getSkillModuleSupport().getMinionTemplateData(minionTemplateId);
			return new KCombatMinion(templateData, master);
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * 获得障碍物受击的时候，会添加到单位的buff
	 * 
	 * @param obstructionTemplateId
	 * @return
	 */
	public static int getObstStateByAttack(int obstructionTemplateId) {
		return getObstructionStateId(obstructionTemplateId, _obstStatesByAttack);
	}
	
	/**
	 * 
	 * 获得障碍物被摧毁之后的添加到单位的buff
	 * 
	 * @param obstructionTemplateId
	 * @return
	 */
	public static int getObstStateAfterDestroyed(int obstructionTemplateId) {
		return getObstructionStateId(obstructionTemplateId, _obstStatesAfterDestroyed);
	}
	
	/**
	 * 
	 * 障碍物被击毁后，是否会对目标产生伤害结算
	 * 
	 * @param obstructionTemplateId
	 * @return
	 */
	public static boolean willObstEffectTarget(int obstructionTemplateId) {
		ObstructionTemplate template = KSupportFactory.getNpcModuleSupport().getObstructionTemp(obstructionTemplateId);
		return template.target != KObstructionTargetType.TARGET_ON_NONE.sign;
	}
	
//	public static int getMountConsume(int mountTemplateId) {
//		Integer beanCount = _mountConsumeMap.get(mountTemplateId);
//		if (beanCount != null) {
//			return beanCount;
//		}
//		return 0;
//	}
	
	public static ICombatCommandCreator getCmdCreateorInstance() {
		return _cmdCreator;
	}
	
	static class KStartCombatArgument {
		float roleBornX;
		float roleBornY; 
		List<KCombatEntrance> assistants;
		Object resultAttachment;
		ICombatGameLevelInfo gameLevelInfo;
		ICombatEnhanceInfo roleEnhanceInfo;
		List<Animation> animationList;
		boolean alsoSendFinishMsgIfEscape;
		int timeOutMillis; // 时限（单位：毫秒）
		int totalWaveCount; // 总共的波数（一般只对塔防有效）
		List<ICombatEventListener> eventList;
		ICombatPet helpPet;
		boolean canAutoFight;
		List<KCombatEntrance> robotList;
	}
	
	static class KCombatGameLevelInfo implements ICombatGameLevelInfo {

		private int _lastBattleFieldId;
		private KGameBattlefieldTypeEnum _lastBattleFieldType;
		
		
		public KCombatGameLevelInfo(int pLastBattleFieldId, KGameBattlefieldTypeEnum pLastBattlefieldType) {
			this._lastBattleFieldId = pLastBattleFieldId;
			this._lastBattleFieldType = pLastBattlefieldType;
		}
		
		@Override
		public int getLastBattleFieldId() {
			return _lastBattleFieldId;
		}

		@Override
		public KGameBattlefieldTypeEnum getLastBattleFieldType() {
			return _lastBattleFieldType;
		}
		
	}
}
