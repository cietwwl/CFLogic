package com.kola.kmp.logic.combat.function.event;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

/**
 * 
 * @author PERRY CHAN
 */
public class KConditionFactory {

	private static final Map<Integer, String> _conditionMap = new HashMap<Integer, String>();
	
	private static KConditionBaseImpl getCondition(int type) throws Exception {
		String path = _conditionMap.get(type);
		if(path != null) {
			KConditionBaseImpl condition = (KConditionBaseImpl)Class.forName(path).newInstance();
			return condition;
		}
		throw new Exception("不存在类型为[" + type + "]的condition！");
	}
	public static void loadConditionType(Element element) {
		@SuppressWarnings("unchecked")
		List<Element> children = element.getChildren();
		Element child;
		for(int i = 0; i < children.size(); i++) {
			child = children.get(i);
			_conditionMap.put(Integer.parseInt(child.getAttributeValue("type")), child.getAttributeValue("clazz"));
		}
	}
	
	public static ICondition getCondition(String[] types, String[] args) throws Exception {
		if(types.length != args.length) {
			throw new RuntimeException("类型数量与参数数量不一致！类型：" + Arrays.toString(types) + "，参数：" + Arrays.toString(args));
		}
		KConditionBaseImpl condition = null;
		KConditionBaseImpl current;
		for(int i = 0; i < types.length; i++) {
			current = getCondition(Integer.parseInt(types[i]));
			current.parseArgs(args[i]);
			if(condition != null) {
				condition.nextCondition = current;
			}
			condition = current;
		}
		return condition;
	}
}
