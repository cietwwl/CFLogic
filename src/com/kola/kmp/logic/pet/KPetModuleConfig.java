package com.kola.kmp.logic.pet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;

/**
 * 
 * @author PERRY CHAN
 */
public class KPetModuleConfig {

	private static int _petMaxLv; // 随从最大的等级
	private static int _petMaxLvGreatThanRole; // 宠物和角色间的最大等级差距（实际上是宠物的等级最多能比角色大多少）
	private static int _petNameLengthMin; // 随从名字最短的长度
	private static int _petNameLengthMax; // 随从名字最长的长度
	private static int _petChgNameCoolDownTime; // 每次改名的等待间隔
	private static int _petMaxSkillCount; // 随从最大的技能数量
	private static int _petInitSkillCount; // 随从初始的技能数量
	private static int _petMaxStarLv; // 随从最大的升星等级
	
	private static float _expProportion; // 吸收经验比例系数
	private static float _lvProportion; // 宠物升级属性系数
	private static float _starLvProportion; // 宠物升星属性系数
	private static int[] _presentPetData;
	private static int _noviceGuidePetTemplateId; // 新手引导的宠物模板id
	private static KPetTemplate _noviceGuidePetTemplate; // 新手引导的宠物
	private static List<Integer> _presentPetsWhenRoleCreated; // 角色创建的时候赠送的随从
	
	public static void init(String path) throws Exception {
		KGameExcelFile file = new KGameExcelFile(path);
		KGameExcelRow[] allRows = file.getTable("随从模块逻辑配置", 2).getAllDataRows();
		ReflectPaser.parseExcel(KPetModuleConfig.class, allRows, true);
		if(_presentPetData != null && _presentPetData.length > 0) {
			_presentPetsWhenRoleCreated = new ArrayList<Integer>();
			for(int i = 0; i < _presentPetData.length; i++) {
				_presentPetsWhenRoleCreated.add(_presentPetData[i]);
			}
		} else {
			_presentPetsWhenRoleCreated = Collections.emptyList();
		}
	}
	
	static void initProportion(KGameExcelRow row) {
		_expProportion = row.getFloat("expProportion");
		_lvProportion = row.getFloat("lvProportion");
		_starLvProportion = row.getFloat("starLvProportion");
	}
	
	static void initComplete() {
		_noviceGuidePetTemplate = KPetModuleManager.getPetTemplate(_noviceGuidePetTemplateId);
		if(_noviceGuidePetTemplate == null) {
			throw new RuntimeException("新手引导随从模板为null，模板id：" + _noviceGuidePetTemplateId);
		}
	}
	
	/**
	 * 
	 * 获取随从的最大等级
	 * 
	 * @return
	 */
	public static int getPetMaxLv() {
		return _petMaxLv;
	}
	
	/**
	 * 
	 * 获取随从最大能比角色大多少级
	 * 
	 * @return
	 */
	public static int getPetMaxLvGreatThanRole() {
		return _petMaxLvGreatThanRole;
	}
	
	/**
	 * 
	 * 获取随从名字的最短长度
	 * 
	 * @return
	 */
	public static int getPetNameLengthMin() {
		return _petNameLengthMin;
	}
	
	/**
	 * 
	 * 获取随从名字的最大长度
	 * 
	 * @return
	 */
	public static int getPetNameLengthMax() {
		return _petNameLengthMax;
	}
	
	/**
	 * 
	 * 获取随从改名的CD时间（毫秒数）
	 * 
	 * @return
	 */
	public static int getPetChangeNameCoolDownTime() {
		return _petChgNameCoolDownTime;
	}

	/**
	 * 
	 * 获取随从最大的技能槽数量
	 * 
	 * @return
	 */
	public static int getPetMaxSkillCount() {
		return _petMaxSkillCount;
	}

	/**
	 * 
	 * 获取技能初始化的技能槽数量
	 * 
	 * @return
	 */
	public static int getPetInitSkillCount() {
		return _petInitSkillCount;
	}
	
	/**
	 * 
	 * @return
	 */
	public static float getExpProportion() {
		return _expProportion;
	}
	/**
	 * 
	 * @return
	 */
	public static float getLvProportion() {
		return _lvProportion;
	}
	/**
	 * 
	 * @return
	 */
	public static float getStarLvProportion() {
		return _starLvProportion;
	}

	/**
	 * 
	 * @return
	 */
	public static List<Integer> getPresentPetsWhenRoleCreated() {
		return _presentPetsWhenRoleCreated;
	}

	/**
	 * 
	 * @return
	 */
	public static KPetTemplate getNoviceGuidePetTemplate() {
		return _noviceGuidePetTemplate;
	}

	/**
	 * 
	 * 随从最大的升星等级
	 * 
	 * @return
	 */
	public static int getPetMaxStarLv() {
		return _petMaxStarLv;
	}
}
