package com.kola.kmp.logic.util.tips;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jxl.CellView;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.XmlUtil;
import com.kola.kmp.logic.util.KXmlWriter;

/**
 * <pre>
 * Tips字符串工具
 * 
 * 1.开服GlobalModuleInit
 * 2.若不存在 tips.xls，从尝试从tips.xml生成一份初始 tips.xls（用于初次翻译）
 * 2.若存在tips.xls，加载并赋值，同时输出一份tips.xml(主要用于SVN核对变化)；
 * 3.日常开发时，修改Tips代码，请同步修改tips.xls并跟进翻译
 * 
 * 
 * @author CamusHuang
 * @creation 2015-3-17 上午11:18:49
 * </pre>
 */
public class KTipsModule {
	
	private static final KGameLogger _LOGGER = KGameLogger.getLogger(KTipsModule.class);
	
	private static String xmlUrl = "./res/gamedata/logictips.xml";
	private static String excelUrl = "./res/gamedata/logictips.xls";

	public static void init(String xmlUrl, String excelUrl) throws Exception {
		KTipsModule.xmlUrl = xmlUrl;
		KTipsModule.excelUrl = excelUrl;
		//
		File file = new File(excelUrl);
		if(!file.exists()){
			file = new File(xmlUrl);
			if (!file.exists()) {
				_LOGGER.warn("----------------》不存在XML Tips文件，文件 url=" + xmlUrl);
				throw new KGameServerException("不存在XML Tips文件，文件 url=" + xmlUrl);
			}
			xmlToExcel();
		}
		
		//
		loadExcelTips();
		//
		excelToXml();
	}
	
	private static final String 翻译文字 = "翻译文字";
	//
	private static String sheetName = "logicTips";
	private final static int HeaderIndex = 2;
	private final static String col0 = "class";
	private final static String col1 = "field";
	private final static String col2 = "原文字";
	private final static String col3 = "翻译文字";
	
	private static void loadExcelTips() throws Exception {
		
		_LOGGER.warn("----------------》加载Excel Tips文件");
		
		KGameExcelFile file;
		try {
			file = new KGameExcelFile(excelUrl);
		} catch (BiffException e) {
			throw new KGameServerException(e.getMessage(), e);
		} catch (IOException e) {
			throw new KGameServerException(e.getMessage(), e);
		}
		
		String clazzPath=null;
		Class<?> clazz=null;
		Map<String,Set<String>> bingoMap = new HashMap<String,Set<String>>();
		
		KGameExcelRow[] rows = file.getTable(sheetName, HeaderIndex + 1).getAllDataRows();
		for (KGameExcelRow row : rows) {
			try {
//			    <tipsModule clazz="com.kola.kmp.logic.util.tips.GlobalTips">
//		        <tips name="_tipsServerBusy" dataType="String">服务器繁忙，请稍后再试！</tips>				
				String tempClazzPath = row.getData(col0);
				String fieldName = row.getData(col1);
				String oldString = row.getData(col2);
				String newString = row.getData(col3);
				if (newString.isEmpty() || newString.equals(翻译文字)) {
					newString = oldString;
				}
				//
				Set<String> bingoSet = bingoMap.get(tempClazzPath);
				if(bingoSet==null){
					bingoSet = new HashSet<String>();
					bingoMap.put(tempClazzPath, bingoSet);
				}
				bingoSet.add(fieldName);
				//
				Class<?> tempClazz;
				if(tempClazzPath.equals(clazzPath)){
					tempClazz=clazz;
				} else {
					tempClazz = Class.forName(tempClazzPath);
					clazzPath = tempClazzPath;
					clazz = tempClazz;
				}
				if (tempClazz == null) {
					throw new Exception("TIPS 映射 赋值失败，找不到类= " + clazzPath);
				}
				setValue(clazz, null, fieldName, newString);
			} catch (Exception e) {
				throw new KGameServerException(e.getMessage() + " ，row=" + row.getIndexInFile(), e);
			}
		}
		
		for(Entry<String,Set<String>> e:bingoMap.entrySet()){
			String tempClazzPath = e.getKey();
			Set<String> bingoSet = e.getValue();
			//
			Class<?> tempClazz = Class.forName(tempClazzPath);
			for(Field field:tempClazz.getFields()){
				if(((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC) &&
					!bingoSet.contains(field.getName())){
					_LOGGER.warn("TIPS 映射 赋值缺漏->" + tempClazzPath+":"+field.getName());
				}
			}
		}
		
		_LOGGER.warn("----------------》加载Excel Tips文件--完成");
		
//		System.err.println(GlobalTips.服务器繁忙请稍候再试);
//		System.err.println(GlobalTips.x功能未开放);
	}
	
	private static void setValue(Class clazz, Object target, String fieldName, String value) throws Exception {
		try {
			Field field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
			field.setAccessible(false);
		} catch (Exception e) {
			throw new Exception("反射转化xml，出现异常！clazz:" + clazz.getName() + ",FieldName:" + fieldName + ",FieldValue:" + value, e);
		}
	}
	
	/**
	 * <pre>
	 * 
	 * @throws Exception
	 * @author CamusHuang
	 * @creation 2013-11-26 下午6:50:19
	 * </pre>
	 */
	private static void excelToXml() throws Exception {
		{
			File file = new File(excelUrl);
			if (!file.exists()) {
				// 如果不存在同名文件，则不执行
				_LOGGER.warn("----------------》不存在Excel Tips文件，不再执行加载，文件 url=" + excelUrl);
				return;
			}
			
			file = new File(xmlUrl);
			if (file.exists()) {
				file.delete();
			}
		}
		
		_LOGGER.warn("----------------》输出XML Tips文件");

		// 从EXCEL加载数据
		KGameExcelFile file;
		try {
			file = new KGameExcelFile(excelUrl);
		} catch (BiffException e) {
			throw new KGameServerException(e.getMessage(), e);
		} catch (IOException e) {
			throw new KGameServerException(e.getMessage(), e);
		}
		
		KXmlWriter writer = new KXmlWriter(xmlUrl, true);
		Element root = writer.getRoot();
		Map<String,Element> allElement = new HashMap<String,Element>();
		
		String clazzPath;
		Class<?> clazz;
		
		KGameExcelRow[] rows = file.getTable(sheetName, HeaderIndex + 1).getAllDataRows();
		for (KGameExcelRow row : rows) {
			try {
//			    <tipsModule clazz="com.kola.kmp.logic.util.tips.GlobalTips">
//		        <tips name="_tipsServerBusy" dataType="String">服务器繁忙，请稍后再试！</tips>				
				String tempClazzPath = row.getData(col0);
				String fieldName = row.getData(col1);
				String oldString = row.getData(col2);
				String newString = row.getData(col3);
				//
				Element clazzE = allElement.get(tempClazzPath);
				if(clazzE==null){
					clazzE=new Element("tipsModule");
					clazzE.setAttribute("clazz", tempClazzPath);
					allElement.put(tempClazzPath, clazzE);
					root.addContent(clazzE);
				}
				//
				Element instanceE = new Element("tips");
				clazzE.addContent(instanceE);
				//
				instanceE.setAttribute("name", fieldName);
				instanceE.setAttribute("dataType", "String");
				instanceE.setAttribute(col2, oldString);
				if(newString==null || newString.isEmpty()){
					instanceE.setText(翻译文字);
				} else {
					instanceE.setText(newString);
				}
				
			} catch (Exception e) {
				throw new KGameServerException(e.getMessage() + " ，row=" + row.getIndexInFile(), e);
			}
		}

		writer.output();

		_LOGGER.warn("----------------》输出XML Tips文件--完成");
	}
	
	
	public static void main(String[] s) throws Exception{
		xmlToExcel();
	}
	
	private static void xmlToExcel() throws Exception{
		
		File file = new File(xmlUrl);
		if (!file.exists()) {
			_LOGGER.warn("----------------》不存在XML Tips文件，文件 url=" + xmlUrl);
			return;
		}
		
		file = new File(excelUrl);
		if (file.exists()) {
			_LOGGER.warn("----------------》已存在Excel Tips文件，文件 url=" + excelUrl);
			return;
		}
		
		_LOGGER.warn("----------------》输出Excel Tips文件");
		
		WritableWorkbook book = Workbook.createWorkbook(new File(excelUrl));
		WritableSheet sheet = book.createSheet(sheetName, 0);
		CellView cv = new CellView();
		cv.setAutosize(true);
		sheet.setColumnView(0, cv);
		sheet.setColumnView(1, cv);
		sheet.setColumnView(2, cv);

		sheet.addCell(new jxl.write.Label(0, HeaderIndex, col0));
		sheet.addCell(new jxl.write.Label(1, HeaderIndex, col1));
		sheet.addCell(new jxl.write.Label(2, HeaderIndex, col2));
		sheet.addCell(new jxl.write.Label(3, HeaderIndex, col3));
		
		int RowNow = HeaderIndex + 1;
		
//	    <tipsModule clazz="com.kola.kmp.logic.util.tips.GlobalTips">
//        <tips name="_tipsServerBusy" dataType="String">服务器繁忙，请稍后再试！</tips>	
		Element root = XmlUtil.openXml(xmlUrl).getRootElement();
		List<Element> list = root.getChildren("tipsModule");
		for(int i = 0; i < list.size(); i++) {
			Element child = list.get(i);
			String tempClazzPath = child.getAttributeValue("clazz");
			for(Object tipsO:child.getChildren()){
				Element tipsE = (Element)tipsO;
				String fieldName = tipsE.getAttributeValue("name");
				String oldString = tipsE.getAttributeValue(col2);
				if(oldString==null || oldString.isEmpty()){
					oldString = tipsE.getTextTrim();
				}
				
				sheet.addCell(new jxl.write.Label(0, RowNow, tempClazzPath));
				sheet.addCell(new jxl.write.Label(1, RowNow, fieldName));
				sheet.addCell(new jxl.write.Label(2, RowNow, oldString));
//				sheet.addCell(new jxl.write.Label(3, RowNow, 翻译文字));
				RowNow++;
			}
		}
		
		book.write();
		book.close();
		
		_LOGGER.warn("----------------》输出Excel Tips文件--完成");
	}
}
