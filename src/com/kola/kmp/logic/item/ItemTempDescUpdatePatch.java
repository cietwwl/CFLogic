package com.kola.kmp.logic.item;

import java.lang.reflect.Field;

import org.slf4j.Logger;

import com.koala.game.logging.KGameLogger;
import com.kola.kmp.logic.RunTimeTask;
import com.kola.kmp.logic.item.KItemDataStructs.KItemTempAbs;

/**
 * <pre>
 * 物品模板描述更新
 * 
 * @author CamusHuang
 * @creation 2014-12-23 下午12:24:55
 * </pre>
 */
public class ItemTempDescUpdatePatch implements RunTimeTask {
	private static Logger _LOGGER = KGameLogger.getLogger(ItemTempDescUpdatePatch.class);

	/**
	 * 开启后可获得：@r圣诞时装（战士）x1(突击战士)@r圣诞时装（特工）x1(暗影特工)@r圣诞时装（枪手）x1(枪械师)@r攻击+800@r闪避+250@r暴击抵抗+100@r眩晕抵抗+750@r无视防御+500@r基础属性加成+750%@r技能伤害加成+10%
	 */
	public String run(String data) {
		
		try {

			String[] datas = data.split(";");
			KItemTempAbs temp = KItemDataManager.mItemTemplateManager.getItemTemplate(datas[0]);
			if(temp==null){
				return "物品不存在 id="+datas[0];
			}
			
			Field field = KItemTempAbs.class.getDeclaredField("desc");
			if(field==null){
				return "物品字段不存在=desc";
			}
			
			field.setAccessible(true);
			field.set(temp, datas[1].replaceAll("@r", '\n'+""));
			
			return "执行完成";
		} catch (Exception e) {
			e.printStackTrace();
			return "发生异常：" + e.getMessage();
		}
	}
}
