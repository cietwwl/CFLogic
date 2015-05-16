package com.kola.kmp.logic.support;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.util.XmlUtil;

/**
 * 
 * @author PERRY CHAN
 */
public class KSupportFactory {

	private static RoleModuleSupport _roleModuleSupport;
	private static ItemModuleSupport _itemModuleSupport;
	private static MailModuleSupport _mailModuleSupport;
	private static NPCModuleSupport _npcModuleSupport;
	private static RankModuleSupport _rankModuleSupport;
	private static SkillModuleSupport _skillModuleSupport;
	private static DirtyWordSupport _dirtyWordSupport;
	private static CurrencyModuleSupport _currencySupport;
	private static RewardModuleSupport _rewardSupport;
	private static MapModuleSupport _mapSupport;
	private static PetModuleSupport _petSupport;
	private static ChatModuleSupport _chatModuleSupport;
	private static GMSupport _gmSupport;
	private static LevelModuleSupport _levelSupport;
	private static MissionModuleSupport _missionSupport;
	private static RelationShipModuleSupport _relationShipModuleSupport;
	private static VIPModuleSupport _vipModuleSupport;
	private static FashionModuleSupport _fashionModuleSupport;
	private static MountModuleSupport _mountModuleSupport;
	private static GangModuleSupport _gangModuleSupport;
	private static TalentModuleSupport _talentSupport;
	private static CompetitionModuleSupport _competitionModuleSupport;
	private static CombatModuleSupport _combatSupport;
	private static ShopModuleSupport _shopSupport;
	private static KDuplicateMapSupport _duplicateMapSupport;
	private static ActivityModuleSupport _activityModuleSupport;
	private static NoviceGuideSupport _noviceGuideSupport;
	private static ExcitingRewardSupport _excitingRewardSupport;
	private static TeamPVPRankSupport _teamPVPRankSupport;
	private static TeamPVPSupport _teamPVPSupport;

	public static void init(String path) throws Exception {
		Document doc = XmlUtil.openXml(path);
		@SuppressWarnings("unchecked")
		List<Element> list = doc.getRootElement().getChildren("support");
		Field[] allFields = KSupportFactory.class.getDeclaredFields();
		Map<String, Field> fieldByClassName = new HashMap<String, Field>();
		Field tempField;
		for (int i = 0; i < allFields.length; i++) {
			tempField = allFields[i];
			fieldByClassName.put(tempField.getType().getName(), tempField);
		}
		Element temp;
		Class<?> tempClazz;
		Class<?>[] implInterfaces;
		for (int i = 0; i < list.size(); i++) {
			temp = list.get(i);
			tempClazz = Class.forName(temp.getAttributeValue("clazz"));
			implInterfaces = tempClazz.getInterfaces();
			boolean find = false;
			for (int k = 0; k < implInterfaces.length; k++) {
				tempField = fieldByClassName.remove(implInterfaces[k].getName());
				if (tempField != null) {
					tempField.set(null, tempClazz.newInstance());
					find = true;
					break;
				}
			}
			if (!find) {
				throw new RuntimeException("！！！！" + tempClazz.getName() + "不是任何的supoort的类型，但是却出现在support的配置文件！！！！");
			}
		}
		if (fieldByClassName.size() > 0) {
			throw new RuntimeException("部分support对象未赋值！" + fieldByClassName.keySet().toString());
		}
	}

	public static RoleModuleSupport getRoleModuleSupport() {
		return _roleModuleSupport;
	}

	public static ItemModuleSupport getItemModuleSupport() {
		return _itemModuleSupport;
	}

	public static MailModuleSupport getMailModuleSupport() {
		return _mailModuleSupport;
	}

	public static NPCModuleSupport getNpcModuleSupport() {
		return _npcModuleSupport;
	}

	public static RankModuleSupport getRankModuleSupport() {
		return _rankModuleSupport;
	}

	public static SkillModuleSupport getSkillModuleSupport() {
		return _skillModuleSupport;
	}

	public static DirtyWordSupport getDirtyWordSupport() {
		return _dirtyWordSupport;
	}

	public static CurrencyModuleSupport getCurrencySupport() {
		return _currencySupport;
	}

	public static RewardModuleSupport getRewardModuleSupport() {
		return _rewardSupport;
	}

	public static MapModuleSupport getMapSupport() {
		return _mapSupport;
	}

	public static PetModuleSupport getPetModuleSupport() {
		return _petSupport;
	}

	public static ChatModuleSupport getChatSupport() {
		return _chatModuleSupport;
	}

	public static GMSupport getGMSupport() {
		return _gmSupport;
	}

	public static LevelModuleSupport getLevelSupport() {
		return _levelSupport;
	}

	public static MissionModuleSupport getMissionSupport() {
		return _missionSupport;
	}

	public static RelationShipModuleSupport getRelationShipModuleSupport() {
		return _relationShipModuleSupport;
	}

	public static VIPModuleSupport getVIPModuleSupport() {
		return _vipModuleSupport;
	}

	public static FashionModuleSupport getFashionModuleSupport() {
		return _fashionModuleSupport;
	}

	public static MountModuleSupport getMountModuleSupport() {
		return _mountModuleSupport;
	}

	public static GangModuleSupport getGangSupport() {
		return _gangModuleSupport;
	}
	
	public static TalentModuleSupport getTalentSupport() {
		return _talentSupport;
	}
	
	public static CompetitionModuleSupport getCompetitionModuleSupport() {
		return _competitionModuleSupport;
	}
	
	public static CombatModuleSupport getCombatModuleSupport() {
		return _combatSupport;
	}
	
	public static ShopModuleSupport getShopSupport(){
		return _shopSupport;
	}

	public static KDuplicateMapSupport getDuplicateMapSupport(){
		return _duplicateMapSupport;
	}

	public static ActivityModuleSupport getActivityModuleSupport() {
		return _activityModuleSupport;
	}

	public static NoviceGuideSupport getNoviceGuideSupport() {
		return _noviceGuideSupport;
	}
	
	public static ExcitingRewardSupport getExcitingRewardSupport() {
		return _excitingRewardSupport;
	}
	
	public static TeamPVPRankSupport getTeamPVPRankSupport() {
		return _teamPVPRankSupport;
	}

	public static TeamPVPSupport getTeamPVPSupport() {
		return _teamPVPSupport;
	}
}
