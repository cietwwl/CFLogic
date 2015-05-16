package com.kola.kmp.logic.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.timer.Timer;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;

import com.koala.game.KGame;
import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.StringUtil;
import com.koala.game.util.XmlUtil;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.UtilTool;
import com.kola.kmp.logic.mission.guide.KGuideManager;
import com.kola.kmp.logic.mission.guide.MainMenuFunction;
import com.kola.kmp.logic.other.KFunctionTypeEnum;
import com.kola.kmp.logic.other.KGameAttrType;
import com.kola.kmp.logic.other.KTableInfo;
import com.kola.kmp.logic.role.KRole;
import com.kola.kmp.logic.util.tips.GangResWarTips;
import com.kola.kmp.logic.util.tips.GlobalTips;

/**
 * 
 * @author PERRY CHAN
 */
public class KGameUtilTool {

	public static final Logger _LOGGER = KGameLogger.getLogger(KGameUtilTool.class);

	/**
	 * 根据属性id数组与属性的值数组，生成属性的Map
	 * 
	 * @param attrId
	 * @param attrValue
	 * @return
	 * @throws Exception
	 */
	public static LinkedHashMap<KGameAttrType, Integer> genAttribute(int[] attrId, int[] attrValue, boolean isValueCanBeZero) throws Exception {
		
		if (attrId.length != attrValue.length) {
			throw new Exception("属性id与属性值的长度不一致！");
		}
		LinkedHashMap<KGameAttrType, Integer> tempEffects = new LinkedHashMap<KGameAttrType, Integer>();
		for (int index = 0; index < attrId.length; index++) {
			KGameAttrType attrType = KGameAttrType.getAttrTypeEnum(attrId[index]);
			if (attrType == null) {
				throw new Exception("属性类型不存在 type=" + attrId[index]);
			}
			if (attrValue[index] < 1) {
				if(isValueCanBeZero){
					continue;
				}
				throw new Exception("属性值错误 =" + attrValue[index]);
			}
			
			if (null != tempEffects.put(attrType, attrValue[index])) {
				throw new Exception("属性类型重复 type=" + attrId[index]);
			}
		}
		return tempEffects;
	}

	/**
	 * 
	 * 安全获取属性值
	 * 
	 * @param attrMap
	 * @param attrType
	 * @return
	 */
	public static int getAttrValueSafely(Map<KGameAttrType, Integer> attrMap, KGameAttrType attrType) {
		Integer value = attrMap.get(attrType);
		if (value == null) {
			return 0;
		} else {
			return value.intValue();
		}
	}
	
	/**
	 * 
	 * 安全获取属性值
	 * 
	 * @param attrMap
	 * @param attrType
	 * @return
	 */
	public static long getAttrValueSafelyL(Map<KGameAttrType, Long> attrMap, KGameAttrType attrType) {
		Long value = attrMap.get(attrType);
		if (value == null) {
			return 0;
		} else {
			return value.longValue();
		}
	}

	/**
	 * 
	 * @param src
	 * @param other
	 */
	public static void combinMap(Map<KGameAttrType, Integer> src, Map<KGameAttrType, Integer> other) {
		Map.Entry<KGameAttrType, Integer> entry;
		Integer srcValue;
		for (Iterator<Map.Entry<KGameAttrType, Integer>> itr = other.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			srcValue = src.get(entry.getKey());
			if (srcValue != null) {
				srcValue += entry.getValue();
			} else {
				srcValue = entry.getValue();
			}
			src.put(entry.getKey(), srcValue);
		}
	}
	
	public static void combinMap2(Map<KGameAttrType, AtomicInteger> map, Map<KGameAttrType, Integer> allEffects) {
		Map.Entry<KGameAttrType, Integer> entry;
		AtomicInteger srcValue;
		for (Iterator<Entry<KGameAttrType, Integer>> itr = allEffects.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			srcValue = map.get(entry.getKey());
			if (srcValue != null) {
				srcValue.addAndGet(entry.getValue());
			} else {
				map.put(entry.getKey(), new AtomicInteger(entry.getValue()));
			}
		}
	}

	/**
	 * <pre>
	 * 将<KGameAttrType, AtomicInteger>转换为<KGameAttrType, Integer>
	 * 
	 * @param map
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-2 下午5:00:31
	 * </pre>
	 */
	public static Map<KGameAttrType, Integer> changeAttMap(Map<KGameAttrType, AtomicInteger> map) {
		//
		Map<KGameAttrType, Integer> result = null;
		if (map.isEmpty()) {
			result = Collections.emptyMap();
		} else {
			result = new HashMap<KGameAttrType, Integer>();
			for (Entry<KGameAttrType, AtomicInteger> entry : map.entrySet()) {
				result.put(entry.getKey(), entry.getValue().get());
			}
		}
		return result;
	}	

	public static void decreaseFromMap(Map<KGameAttrType, Integer> src, Map<KGameAttrType, Integer> dMap) {
		Map.Entry<KGameAttrType, Integer> entry;
		Integer nowValue;
		for (Iterator<Map.Entry<KGameAttrType, Integer>> itr = dMap.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			nowValue = src.get(entry.getKey());
			if (nowValue != null) {
				src.put(entry.getKey(), nowValue - entry.getValue());
			}
		}
	}

	public static Map<KGameAttrType, Integer> getDifferent(Map<KGameAttrType, Integer> src, Map<KGameAttrType, Integer> target) {
		Map<KGameAttrType, Integer> result = new HashMap<KGameAttrType, Integer>();
		Map.Entry<KGameAttrType, Integer> entry;
		src = new HashMap<KGameAttrType, Integer>(src);
		Integer srcValue;
		for (Iterator<Map.Entry<KGameAttrType, Integer>> itr = target.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			srcValue = src.remove(entry.getKey());
			if (srcValue == null) {
				result.put(entry.getKey(), entry.getValue());
			} else if (srcValue != entry.getValue()) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		if (src.size() > 0) {
			for (Iterator<Map.Entry<KGameAttrType, Integer>> itr = src.entrySet().iterator(); itr.hasNext();) {
				result.put(itr.next().getKey(), 0);
			}
		}
		return result;
	}

	/**
	 * <pre>
	 * 可使用{@link #loadSimpleDialy(String, String)}进行读取
	 * 
	 * @param dir
	 * @param fileName
	 * @param ver null表示不需要记录版本号
	 * @param dialys
	 * @author CamusHuang
	 * @creation 2014-4-18 下午7:41:22
	 * </pre>
	 */
	public static void saveSimpleDialy(String dir, String fileName, String ver, List dialys) {
		if (dialys == null) {
			dialys = Collections.emptyList();
		}

		File file = new File(dir);
		file.mkdirs();

		String url = dir + fileName;
		KXmlWriter writer = null;
		try {
			writer = new KXmlWriter(url, true);

			Element root = writer.getRoot();
			root.setAttribute("保存时间", UtilTool.DATE_FORMAT.format(new Date()));
			if (ver != null) {
				root.setAttribute("ver", ver);
			}
			Element element;
			for (Object dialy : dialys) {
				element = new Element("Dialy");
				element.setText(dialy.toString());
				writer.addElement(element);
			}
			writer.output();

			_LOGGER.error("简单日志保存成功！url=" + url);

		} catch (Exception e) {
			_LOGGER.error("简单日志保存出错！url=" + url);
			_LOGGER.error(e.getMessage(), e);
			return;
		}
	}

	/**
	 * <pre>
	 * 简单日志读取方法
	 * 
	 * 可使用{@link #saveSimpleDialy(String, String, String[])}进行输出
	 * 
	 * @param dir
	 * @param fileName
	 * @param ver 如果ver!=null，则必须版本相同才会返回数据
	 * @return
	 * @author CamusHuang
	 * @creation 2013-11-14 上午10:38:10
	 * </pre>
	 */
	public static List<String> loadSimpleDialy(String dir, String fileName, String ver) {
		String url = dir + fileName;
		File file = new File(url);
		if (!file.exists()) {
			return Collections.emptyList();
		}
		if (file.isDirectory()) {
			return Collections.emptyList();
		}

		List<String> result = new ArrayList<String>();
		Document doc = XmlUtil.openXml(file);
		Element root = doc.getRootElement();
		if (ver != null) {
			String tempVer = root.getAttributeValue("ver");
			if (tempVer == null || !tempVer.equals(ver)) {
				return result;
			}
		}

		List<Element> dialyEList = root.getChildren();
		for (Element e : dialyEList) {
			result.add(e.getText());
		}

		return result;
	}

	public static final KGameExcelRow[] getAllDataRows(KGameExcelFile file, Map<Byte, KTableInfo> map, byte type) {
		// final 可向编译器提出内联函数请求
		KTableInfo tableInfo = map.get(type);
		KGameExcelTable table = file.getTable(tableInfo.tableName, tableInfo.headerIndex);
		return table.getAllDataRows();
	}

	/**
	 * <pre>
	 * 本周五21:00
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2013-9-18 上午10:43:53
	 * </pre>
	 */
	public static String genTimeStrForClient(long time) {

		Date date = new Date(time);
		String timeStr = UtilTool.DATE_FORMAT5.format(new Date(time));

		// 由于时效任务不是100%精确，担心当前时间有可能周日晚23:59:59秒,所以当前时间向前偏移1分钟计算与目标时间是否同一个星期
		long nowTime = System.currentTimeMillis() + 1 * Timer.ONE_MINUTE;
		if (!UtilTool.isBetweenDay(nowTime, time)) {
			// 今天
			return StringUtil.format(GangResWarTips.今天时间x, timeStr);
		}

		String weekday = "";
		{
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			switch (cal.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.MONDAY:
				weekday = "一";
				break;
			case Calendar.TUESDAY:
				weekday = "二";
				break;
			case Calendar.WEDNESDAY:
				weekday = "三";
				break;
			case Calendar.THURSDAY:
				weekday = "四";
				break;
			case Calendar.FRIDAY:
				weekday = "五";
				break;
			case Calendar.SATURDAY:
				weekday = "六";
				break;
			case Calendar.SUNDAY:
				weekday = "日";
				break;
			}
		}

		if (time >= UtilTool.getNextWeekStart(nowTime).getTimeInMillis()) {
			return StringUtil.format(GangResWarTips.下周x时间x, weekday, timeStr);
		}
		return StringUtil.format(GangResWarTips.本周x时间x, weekday, timeStr);
	}
	
	/**
	 * <pre>
	 * 本周五21:00
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2013-9-18 上午10:43:53
	 * </pre>
	 */
	public static String genTimeStrForClient2(long time) {

		Date date = new Date(time);
		String timeStr = UtilTool.DATE_FORMAT5.format(new Date(time));

		// 由于时效任务不是100%精确，担心当前时间有可能周日晚23:59:59秒,所以当前时间向前偏移1分钟计算与目标时间是否同一个星期
		long nowTime = System.currentTimeMillis() + 1 * Timer.ONE_MINUTE;
		String weekday = "";
		{
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			switch (cal.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.MONDAY:
				weekday = "一";
				break;
			case Calendar.TUESDAY:
				weekday = "二";
				break;
			case Calendar.WEDNESDAY:
				weekday = "三";
				break;
			case Calendar.THURSDAY:
				weekday = "四";
				break;
			case Calendar.FRIDAY:
				weekday = "五";
				break;
			case Calendar.SATURDAY:
				weekday = "六";
				break;
			case Calendar.SUNDAY:
				weekday = "日";
				break;
			}
		}

		return StringUtil.format("周{}{}", weekday, timeStr);
	}	
	
	/**
	 * <pre>
	 * 检查指定功能是否已经开放
	 * 
	 * @param role
	 * @param funType
	 * @return null表示已经开放，非null表示未开放提示
	 * @author CamusHuang
	 * @creation 2015-1-20 下午4:42:12
	 * </pre>
	 */
	public static String checkFunctionTypeOpen(KRole role, KFunctionTypeEnum funType) {
		if (KGuideManager.checkFunctionIsOpen(role, funType.functionId)) {
			// 功能已经开放
			return null;
		}

		{
			// 功能未开放
			MainMenuFunction function = KGuideManager.getMainMenuFunctionInfoMap().get(funType.functionId);
			if (function == null || function.getOpenRoleLevelLimit() < 1) {
				return StringUtil.format(GlobalTips.x功能未开放, funType.name());
			}

			if (role.getLevel() >= function.getOpenRoleLevelLimit()) {
				return StringUtil.format(GlobalTips.x功能未开放, funType.name());
			}

			return StringUtil.format(GlobalTips.x功能将于x级开放, funType.name(), function.getOpenRoleLevelLimit());
		}
	}	

	public static void main(String[] s) throws Exception {
////		TipsToXml();
//		
//		long nowTime = UtilTool.DATE_FORMAT.parse("2014-06-28 01:16:20").getTime();
////		long nowTime = UtilTool.DATE_FORMAT.parse("1970-01-01 08:00:00").getTime();
//		long PERIOD =Timer.ONE_DAY;
//		System.err.println(nowTime);
//		System.err.println(nowTime/PERIOD);
//		System.err.println(nowTime/PERIOD*PERIOD);
//		System.err.println("---------"+(nowTime-(nowTime/PERIOD*PERIOD)));
//		System.err.println(new Date(nowTime));
//		System.err.println(new Date(nowTime/PERIOD));
//		System.err.println(new Date(nowTime/PERIOD*PERIOD));
		File file = new File("./res/gsconfig/mapModule/mapXmlData");
		File[] targetFiles = file.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().contains("ptfuben") || pathname.getName().contains("bossfb") || pathname.getName().contains("jyfuben");
			}
		});
		
		final Comparator<float[]> comparator = new Comparator<float[]>() {

			@Override
			public int compare(float[] o1, float[] o2) {
				return o1[0] > o2[0] ? 1 : -1;
			}
		};
		
		Document doc;
		List<List<float[]>> list = new ArrayList<List<float[]>>();
		List<List<float[]>> trapList = new ArrayList<List<float[]>>();
		final int type_monster = 8;
		final int type_trap = 32;
		final int type_section = 1024;
		List<Element> elementList;
		Element element;
		int objType;
		List<float[]> mList;
		List<float[]> tList;
		List<float[]> sectionList;
		List<Object[]> output;
		Element subChild;
		DecimalFormat df = new DecimalFormat("0.#");
		float[] oArray;
		List<float[]> tempList;
		float[] tempArray;
		for (int i = 0; i < targetFiles.length; i++) {
			doc = XmlUtil.openXml(targetFiles[i]);
			elementList = doc.getRootElement().getChildren("gameObject");
			mList = new ArrayList<float[]>();
			tList = new ArrayList<float[]>();
			sectionList = new ArrayList<float[]>();
			list.clear();
			trapList.clear();
//			list.add(mList);
//			trapList.add(tList);
			for (int k = 0; k < elementList.size(); k++) {
				element = elementList.get(k);
				objType = Integer.parseInt(element.getAttributeValue("type"));
				switch (objType) {
				case type_monster:
				case type_trap:
				case type_section:
					subChild = element.getChild("transform").getChild("position");
					switch (objType) {
					case type_monster:
						tempList = mList;
						break;
					case type_trap:
						tempList = tList;
						break;
					case type_section:
						tempList = sectionList;
						break;
					default:
						continue;
					}
					tempList.add(new float[] { Float.parseFloat(subChild.getAttributeValue("x")), Float.parseFloat(subChild.getAttributeValue("y")) });
					break;
				default:
					continue;
				}
			}
			Collections.sort(sectionList, comparator);
			Collections.sort(mList, comparator);
			Collections.sort(tList, comparator);
			int mBeginIndex = 0;
			int tBeginIndex = 0;
			float secionX = 0;
			for (int k = 0; k < sectionList.size(); k++) {
				tempList = new ArrayList<float[]>();
				secionX = sectionList.get(k)[0];
				for (; mBeginIndex < mList.size(); mBeginIndex++) {
					tempArray = mList.get(mBeginIndex);
					if (tempArray[0] <= secionX) {
						tempList.add(tempArray);
					} else {
						break;
					}
				}
				list.add(tempList);
				tempList = new ArrayList<float[]>();
				for (; tBeginIndex < tList.size(); tBeginIndex++) {
					tempArray = tList.get(tBeginIndex);
					if (tempArray[0] <= secionX) {
						tempList.add(tempArray);
					} else {
						break;
					}
				}
				trapList.add(tempList);
			}
			output = new ArrayList<Object[]>();
			float x;
			for (int k = 0; k < trapList.size(); k++) {
				tList = trapList.get(k);
				if (tList.size() > 0) {
					Collections.sort(tList, comparator);
					oArray = tList.get(tList.size() - 1);
					x = oArray[0] - 2;
					if (x > 0) {
						output.add(new Object[] { (int) x, df.format(oArray[1]) });
					}
				}
			}
			if (output.size() < 6) {
				for (int k = 0; k < list.size(); k++) {
					mList = list.get(k);
					if (mList.size() > 0) {
						Collections.sort(mList, comparator);
						oArray = mList.get(mList.size() - 1);
						x = oArray[0] - 2;
						if (x > 0) {
							output.add(new Object[] { (int) x, df.format(oArray[1])});
						}
					}
				}
			}
			StringBuilder strBld = new StringBuilder();
			for(int k = 0; k < output.size(); k++) {
				strBld.append(output.get(k)[0]).append(",").append(output.get(k)[1]).append(";");
			}
			System.out.println(targetFiles[i].getName().replace(".assetbundle", "") + ":" + strBld.toString());
		}
	}
	
	private static void expression(){
		// (62.5*（强化等级+2.4）^3/10 + 12.5*((强化等级+2.4)^3/10)^1.2)*部位参数
		int nextStroneLv =1;
		float param = 0.6f;
		double result = (62.5 * Math.pow((nextStroneLv + 2.4), 3) / 10 + 12.5 * Math.pow(Math.pow(nextStroneLv + 2.4, 3)/ 10,1.2)) * param;
		// 十位四舍五入
		System.err.println(result);
		result = Math.rint(result / 100) * 100;
		System.err.println(result);
		
	}
	
	/**
	 * <pre>
	 * 如果表中含有列"gsIds"，则对所有行进行过滤，返回本区相关的行。
	 * 
	 * 1,2,3 表示1，2，3区有效
	 * 1-20,23,24 表示1至20区、23、24区有效
	 * 
	 * @param rows
	 * @return
	 * @author CamusHuang
	 * @creation 2014-12-19 下午4:21:48
	 * </pre>
	 */
	public static final String GSIdsColName = "gsIds";
	public static KGameExcelRow[] filterRowsByGSID(KGameExcelTable table) throws KGameServerException{
		KGameExcelRow[] rows = table.getAllDataRows();
		
		
		
		if(rows.length < 1 || !table.getHeaderNames().contains(GSIdsColName)){
			return rows;
		}

		int gsId = KGame.getGSID();
		
		List<KGameExcelRow> result = new ArrayList<KGameExcelTable.KGameExcelRow>();
		for (KGameExcelRow row : rows) {
			try {
				String[] gsIds = row.getData(GSIdsColName).split(",");
				{
					for (String idStr : gsIds) {
						if(idStr.contains("-")){
							String[] temps = idStr.split("-");
							int sid = Integer.parseInt(temps[0]);
							int eid = Integer.parseInt(temps[1]);
							if(sid<=gsId && gsId<=eid){
								result.add(row);
								break;
							}
						} else {
							if(gsId == Integer.parseInt(idStr)){
								result.add(row);
								break;
							}
						}
					}
				}
			} catch (Exception e) {
				throw new KGameServerException("加载" + table.getTableName() + "错误：" + e.getMessage() + "，Row=" + row.getIndexInFile(), e);
			}
		}
		
		return result.toArray(new KGameExcelRow[result.size()]);
	}

	/**
	 * <pre>
	 * 将标准TIPS文件的内容转换到XML中
	 * 警告：通过指定代码路径获取到指定包内的所有类名进行遍历
	 * 
	 * @author CamusHuang
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws IOException 
	 * @creation 2014-6-6 下午4:15:18
	 * </pre>
	 */
	public static void TipsToXml() throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException, IOException {
		String xmlPath = "./newTips.xml";
		String pack = "com.kola.kmp.logic.util.tips.";
		String dir = "./src/" + pack.replace('.', '/');
		//
		KXmlWriter writer = new KXmlWriter(xmlPath, true);
		Element root = writer.getRoot();
		//
		File dirFile = new File(dir);
		for (File file : dirFile.listFiles()) {
			String fileName = file.getName().replace(".java", "");
			String className = pack + fileName;
			Class clazz = Class.forName(className);
			// <tipsModule clazz="com.kola.kmp.logic.util.tips.MailTips">
			// <tips name="删除成功" dataType="String">删除成功</tips>
			Element element = new Element("tipsModule");
			element.setAttribute("clazz", clazz.getName());
			root.addContent(element);
			{
				for (Field field : clazz.getFields()) {
					if(!Modifier.isStatic(field.getModifiers())){
						continue;
					}
					Element telement = new Element("tips");
					element.addContent(telement);

					telement.setAttribute("name", field.getName());
					telement.setAttribute("dataType", "String");
					field.setAccessible(true);
					Object value = field.get(null);
					telement.setText(value==null?"":value.toString());
					field.setAccessible(false);
				}
			}
		}
		writer.output();
	}
}
