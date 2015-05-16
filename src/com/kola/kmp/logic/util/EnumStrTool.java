package com.kola.kmp.logic.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import jxl.CellView;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.exception.KGameServerException;
import com.koala.game.logging.KGameLogger;
import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.koala.game.util.XmlUtil;
import com.kola.kmp.logic.util.EnumStrTool.ClazzStruct.Instance;

/**
 * <pre>
 * 枚举字符串工具
 * 
 * 1.开服
 * 2.GlobalModuleInit的时候，如果存在enum.xls，则加载并对枚举重新赋值，同时输出一份enum.xml(主要用于SVN核对变化)；
 * 3.notifyCacheLoadComplete的时候，如果不存在enum.xls，则输出一份enum.xls(主要用于初次翻译)；
 * 4.日常开发时，修改枚举代码，请同步修改enum.xls并跟进翻译
 * 
 * @author CamusHuang
 * @creation 2013-11-26 下午2:52:56
 * </pre>
 */
public class EnumStrTool {

	private static final KGameLogger _LOGGER = KGameLogger.getLogger(EnumStrTool.class);

	private static String xmlUrl = "./res/gamedata/enumStr.xml";
	private static String excelUrl = "./res/gamedata/enumStr.xls";
	//
	private static final String 翻译文字 = "翻译文字";
	//
	private static String sheetName = "enumStr";
	private final static int HeaderIndex = 2;
	private final static String col0 = "class";
	private final static String col1 = "enumName";
	private final static String col2 = "field";
	private final static String col3 = "原文字";
	private final static String col4 = "翻译文字";
	
	public static void loadEnumStrForGlobalModuleInit(String xmlUrl, String excelUrl) throws Exception {
		EnumStrTool.xmlUrl = xmlUrl;
		EnumStrTool.excelUrl = excelUrl;
		//
		// 如果存在enum.xls，则加载并对枚举重新赋值，同时输出一份enum.xml(主要用于SVN核对变化)；
		ExcelToEnum.excelToEnum();
		//
		ExcelToXml.excelToXml();
	}	

	public static void notifyCacheLoadComplete() throws KGameServerException {

		// 如果不存在enum.xls，则输出一份enum.xls(主要用于初次翻译)；
		try {
			EnumToFile.enumToNewFile();
		} catch (Exception e) {
			throw new KGameServerException(e.getMessage(), e);
		}
	}

	/**
	 * <pre>
	 * 如果不存在enum.xls，则输出一份enum.xls(主要用于初次翻译)；
	 * 
	 * @author CamusHuang
	 * @creation 2015-3-10 下午12:31:38
	 * </pre>
	 */
	public static class EnumToFile {

		/**
		 * <pre>
		 * 遍历工程内所有类及内部类，找出枚举并将枚举中String类型的字段输出到Xml中
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2013-11-26 下午6:50:19
		 * </pre>
		 */
		public static void enumToNewFile() throws Exception {
			File file = new File(excelUrl);
			if (file.exists()) {
				return;
			}

			_LOGGER.warn("----------------》输出枚举到EXCEL");

			// 扫描所有枚举
			LinkedHashMap<String, ClazzStruct> enumClazzMap = EnumScanTool.scanEnums();

			// 输出到Excel文件
			toExcel(excelUrl, enumClazzMap);

			_LOGGER.warn("----------------》输出枚举到EXCEL--完成");
		}

		/**
		 * <pre>
		 * 
		 * @param xmlUrl
		 * @param enumClazzMap
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2015-3-17 上午10:45:07
		 * </pre>
		 */
		static void toXml(String xmlUrl, Map<String, ClazzStruct> enumClazzMap) throws Exception {

			File file = new File(xmlUrl);
			if (file.exists()) {
				file.delete();
			}

			KXmlWriter writer = new KXmlWriter(xmlUrl, true);
			Element root = writer.getRoot();
			//
			for (ClazzStruct clazzStruct : enumClazzMap.values()) {
				Element clazzE = new Element("enumClass");
				root.addContent(clazzE);
				//
				clazzE.setAttribute("name", clazzStruct.clazzPath);
				//
				for (Instance instance : clazzStruct.instancesMap.values()) {

					Element instanceE = new Element(instance.instanceName);
					clazzE.addContent(instanceE);
					//
					for (Entry<String, String[]> entry : instance.fieldsMap.entrySet()) {
						String fieldName = entry.getKey();
						String oldString = entry.getValue()[0];

						Element fieldE = new Element(fieldName);
						instanceE.addContent(fieldE);
						//
						{
							fieldE.setAttribute("org", oldString);
							if(entry.getValue()[1]==null || entry.getValue()[1].isEmpty()){
								fieldE.setText(翻译文字);
							} else {
								fieldE.setText(entry.getValue()[1]);
							}
						}
					}
				}
			}

			writer.output();
		}

		private static void toExcel(String excelUrl, Map<String, ClazzStruct> enumClazzMap) throws Exception {

			File file = new File(excelUrl);
			if (file.exists()) {
				file.delete();
			}

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
			sheet.addCell(new jxl.write.Label(4, HeaderIndex, col4));

			// 处理的枚举计数

			int RowNow = HeaderIndex + 1;

			for (ClazzStruct clazzStruct : enumClazzMap.values()) {

				for (Instance instance : clazzStruct.instancesMap.values()) {

					for (Entry<String, String[]> entry : instance.fieldsMap.entrySet()) {
						String fieldName = entry.getKey();
						String oldString = entry.getValue()[0];

						sheet.addCell(new jxl.write.Label(0, RowNow, clazzStruct.clazzPath));
						sheet.addCell(new jxl.write.Label(1, RowNow, instance.instanceName));
						sheet.addCell(new jxl.write.Label(2, RowNow, fieldName));
						sheet.addCell(new jxl.write.Label(3, RowNow, oldString));
						RowNow++;
					}
				}
			}

			book.write();
			book.close();
		}
	}

	/**
	 * <pre>
	 * 遍历EXCEL，按EXCEL中指定的枚举类名、枚举名称、字段，将新的String值赋值到枚举中
	 * 
	 * @author CamusHuang
	 * @creation 2013-11-26 下午7:43:37
	 * </pre>
	 */
	public static class ExcelToEnum {

		/**
		 * <pre>
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2013-11-26 下午6:50:19
		 * </pre>
		 */
		public static void excelToEnum() throws KGameServerException {
			File file = new File(excelUrl);
			if (!file.exists()) {
				// 如果不存在同名文件，则不执行
				_LOGGER.warn("----------------》不存在Excel枚举文件，不再执行加载，文件 url=" + excelUrl);
				return;
			}

			_LOGGER.warn("----------------》加载Excel枚举文件");

			// 从Excel加载数据
			LinkedHashMap<String, ClazzStruct> enumClazzMap = readExcel(excelUrl);

			// 映射赋值
			EnumResetTool.doReflect(enumClazzMap);

			_LOGGER.warn("----------------》加载Excel枚举文件--完成");
//			 System.err.println(KActionType.ACTION_TYPE_WORLDBOSS.name);
//			 System.err.println(KCurrencyTypeEnum.GOLD.name);
		}

		private static LinkedHashMap<String, ClazzStruct> readExcel(String excelUrl) throws KGameServerException {
			// 保存所有从EXCEL加载的数据
			LinkedHashMap<String, ClazzStruct> enumClazzMap = new LinkedHashMap<String, ClazzStruct>();
			//
			KGameExcelFile file;
			try {
				file = new KGameExcelFile(excelUrl);
			} catch (BiffException e) {
				throw new KGameServerException(e.getMessage(), e);
			} catch (IOException e) {
				throw new KGameServerException(e.getMessage(), e);
			}
			KGameExcelRow[] rows = file.getTable(sheetName, HeaderIndex + 1).getAllDataRows();
			for (KGameExcelRow row : rows) {
				try {
					String clazzPath = row.getData(col0);
					String enumName = row.getData(col1);
					String fieldName = row.getData(col2);
					String oldString = row.getData(col3);
					String newString = row.getData(col4);

					ClazzStruct clazzStruct = enumClazzMap.get(clazzPath);
					if (clazzStruct == null) {
						Class clazz = Class.forName(clazzPath);
						if (clazz == null) {
							throw new Exception("枚举类 映射 赋值失败，找不到类= " + clazzPath);
						}
						clazzStruct = new ClazzStruct(clazzPath, clazz);
						enumClazzMap.put(clazzPath, clazzStruct);
					}

					Instance instance = clazzStruct.instancesMap.get(enumName);
					if (instance == null) {
						instance = new Instance(enumName);
						clazzStruct.instancesMap.put(enumName, instance);
					}

					if (instance.fieldsMap.put(fieldName, new String[] { oldString, newString }) != null) {
						throw new Exception("枚举类 映射 赋值失败，定义重复 类= " + clazzPath + " 实例=" + enumName + " 字段=" + fieldName);
					}

				} catch (Exception e) {
					throw new KGameServerException(e.getMessage() + " ，row=" + row.getIndexInFile(), e);
				}
			}

			return enumClazzMap;
		}
	}
	
	/**
	 * <pre>
	 * 遍历EXCEL，按EXCEL中指定的枚举类名、枚举名称、字段，转化到XML中
	 * 
	 * @author CamusHuang
	 * @creation 2013-11-26 下午7:43:37
	 * </pre>
	 */
	public static class ExcelToXml {

		/**
		 * <pre>
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2013-11-26 下午6:50:19
		 * </pre>
		 */
		public static void excelToXml() throws Exception {
			File file = new File(xmlUrl);
			if (file.exists()) {
				file.delete();
			}
			
			file = new File(excelUrl);
			if (!file.exists()) {
				// 如果不存在同名文件，则不执行
				_LOGGER.warn("----------------》不存在Excel枚举文件，不再执行加载，文件 url=" + excelUrl);
				return;
			}
			
			_LOGGER.warn("----------------》输出XML枚举文件");

			// 从EXCEL加载数据
			LinkedHashMap<String, ClazzStruct> enumClazzMap = ExcelToEnum.readExcel(excelUrl);

			EnumToFile.toXml(xmlUrl, enumClazzMap);

			_LOGGER.warn("----------------》输出XML枚举文件--完成");

		}
	}	

	/**
	 * <pre>
	 * 遍历Xml，按Xml中指定的枚举类名、枚举名称、字段，将新的String值赋值到枚举中
	 * 
	 * @deprecated 暂时没有用到
	 * @author CamusHuang
	 * @creation 2013-11-26 下午7:43:37
	 * </pre>
	 */
	public static class XmlToEnum {
		private static String xmlUrl = "./res/gamedata/enumStr.xml";

		/**
		 * <pre>
		 * 
		 * @throws Exception
		 * @author CamusHuang
		 * @creation 2013-11-26 下午6:50:19
		 * </pre>
		 */
		public static void xmlToEnum() throws Exception {
			File file = new File(xmlUrl);
			if (!file.exists()) {
				// 如果不存在同名文件，则不执行
				_LOGGER.warn("----------------》不存在XML枚举字符串文件，不再执行加载，文件 url=" + xmlUrl);
				return;
			}

			_LOGGER.warn("----------------》开始加载 Xml枚举字符串文件");

			// 从Xml加载数据
			LinkedHashMap<String, ClazzStruct> enumClazzMap = readXml(xmlUrl);

			// 映射赋值
			EnumResetTool.doReflect(enumClazzMap);

			_LOGGER.warn("----------------》加载 Xml枚举字符串文件完成");
		}

		private static LinkedHashMap<String, ClazzStruct> readXml(String xmlUrl) throws KGameServerException {
			// 保存所有从EXCEL加载的数据
			LinkedHashMap<String, ClazzStruct> clazzMap = new LinkedHashMap<String, ClazzStruct>();
			//
			Document doc = XmlUtil.openXml(xmlUrl);
			Element root = doc.getRootElement();

			// 遍历所有枚举
			for (Object obj : root.getChildren("enumClass")) {
				Element clazzE = (Element) obj;
				String clazzPath = clazzE.getAttributeValue("name");
				if (clazzMap.containsKey(clazzPath)) {
					throw new KGameServerException("重复定义的枚举类： " + clazzPath);
				}
				ClazzStruct clazzStruct = null;
				{
					try {
						Class clazz = Class.forName(clazzPath);
						if (clazz == null) {
							throw new KGameServerException("不存在的枚举类：" + clazzPath);
						}
						clazzStruct = new ClazzStruct(clazzPath, clazz);
						clazzMap.put(clazzPath, clazzStruct);
					} catch (KGameServerException e) {
						throw e;
					} catch (Exception e) {
						throw new KGameServerException(e);
					}
				}
				// 遍历所有实例
				for (Object obj2 : clazzE.getChildren()) {
					Element instanceE = (Element) obj2;
					String instanceName = instanceE.getName();
					if (clazzStruct.instancesMap.containsKey(instanceName)) {
						throw new KGameServerException("重复定义的枚举实例， " + clazzPath + ":" + instanceName);
					}

					Instance instance = null;
					{
						instance = new Instance(instanceName);
						clazzStruct.instancesMap.put(instanceName, instance);
					}
					// 遍历所有字符串变量
					for (Object obj3 : instanceE.getChildren()) {
						Element fieldE = (Element) obj3;
						// <name org="BOSS副本">翻译文字</name>
						String fieldName = fieldE.getName();
						String org = fieldE.getAttributeValue("org");
						String trans = fieldE.getTextTrim();

						if (instance.fieldsMap.containsKey(fieldName)) {
							throw new KGameServerException("重复定义的变量， " + clazzPath + ":" + instanceName + ":" + fieldName);
						}

						instance.fieldsMap.put(fieldName, new String[] { org, trans });
					}
				}
			}
			return clazzMap;
		}
	}
	
	/**
	 * <pre>
	 * 对枚举中的字符串进行赋值
	 * 
	 * @author CamusHuang
	 * @creation 2015-3-10 下午12:31:43
	 * </pre>
	 */
	public static class EnumResetTool {
		
		static void doReflect(Map<String, ClazzStruct> enumClazzMap) throws KGameServerException {
			try {
				for (ClazzStruct clazzStruct : enumClazzMap.values()) {

					boolean isReflect = false;
					for (Instance instance : clazzStruct.instancesMap.values()) {
						Object enumInstance = searchEnumInstance(clazzStruct.clazz, instance.instanceName);
						if (enumInstance == null) {
							throw new Exception("找不到枚举实例：" + instance.instanceName);
						}

						for (Entry<String, String[]> entry : instance.fieldsMap.entrySet()) {
							String fieldName = entry.getKey();
							String tranStr = entry.getValue()[1];
							if (tranStr.isEmpty() || tranStr.equals(翻译文字)) {
								continue;
							}
							// 赋值
							setValue(clazzStruct.clazz, enumInstance, fieldName, tranStr);
							isReflect = true;
						}
					}

					// 修改的字符串有需要通知枚举进行KEY重新处理的
					if (isReflect) {
						try {
							Method method = clazzStruct.clazz.getDeclaredMethod("resetString");
							if (method != null) {
								method.invoke(null);
							}
						} catch (Exception e) {
						}
					}
				}
			} catch (Exception e) {
				throw new KGameServerException(e.getMessage(), e);
			}
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
		 * 查找指定枚举类的指定实例
		 * 
		 * @param enumClazz
		 * @param instanceName
		 * @return
		 * @author CamusHuang
		 * @throws Exception 
		 * @creation 2013-12-2 下午3:16:33
		 * </pre>
		 */
		private static Object searchEnumInstance(Class enumClazz, String instanceName) throws Exception {
			Field[] fields = enumClazz.getDeclaredFields();
			for (Field field : fields) {
				if (field.isEnumConstant()) {
					if (field.getName().equals(instanceName)) {
						field.setAccessible(true);
						Object obj = field.get(null);
						field.setAccessible(false);
						return obj;
					}
				}
			}
			return null;
		}
	}

	/**
	 * <pre>
	 * 扫描所有枚举类
	 * 
	 * @author CamusHuang
	 * @creation 2015-3-10 下午12:31:43
	 * </pre>
	 */
	public static class EnumScanTool {
		private static String classDirRoot = "./bin";

		static LinkedHashMap<String, ClazzStruct> scanEnums() throws KGameServerException {
			try {
				File srcFile = new File(classDirRoot);
				String classPath = "";
				LinkedHashMap<String, ClazzStruct> enumClazzMap = new LinkedHashMap<String, ClazzStruct>();
				scanDir(srcFile, classPath, enumClazzMap);
				return enumClazzMap;
			} catch (Exception e) {
				throw new KGameServerException(e.getMessage(), e);
			}
		}

		private static void scanDir(File dirFile, String classPath, LinkedHashMap<String, ClazzStruct> enumClazzMap) throws Exception {
			for (File file : dirFile.listFiles()) {
				String tempClassPath = (classPath.length() < 1 ? file.getName() : classPath + "." + file.getName());
				if (file.isDirectory()) {
					scanDir(file, tempClassPath, enumClazzMap);
				} else {
					dealFile(file, tempClassPath, enumClazzMap);
				}
			}
		}

		private static void dealFile(File file, String classPath, LinkedHashMap<String, ClazzStruct> enumClazzMap) throws Exception {
			if (!classPath.endsWith(".class") || classPath.contains("$")) {
				return;
			}
			try{
			classPath = classPath.replace(".class", "");
			Class clazz = Class.forName(classPath);

			dealClazz(clazz, enumClazzMap);
			} catch(Throwable e){
				System.err.println(classPath);
				throw new Exception(e);
			}
		}

		private static void dealClazz(Class clazz, LinkedHashMap<String, ClazzStruct> enumClazzMap) throws Exception {
			dealClazzFinal(clazz, enumClazzMap);

			// 内部类
			for (Class clazz2 : clazz.getDeclaredClasses()) {
				dealClazz(clazz2, enumClazzMap);
			}
		}

		private static void dealClazzFinal(Class clazz, LinkedHashMap<String, ClazzStruct> enumClazzMap) throws Exception {

			if (clazz.isEnum()) {
				Field[] fields = clazz.getDeclaredFields();
				for (Field field : fields) {
					// 遍历域，处理枚举实例
					if (field.isEnumConstant()) {
						if (!field.isAccessible()) {
							field.setAccessible(true);
						}
						for (Field field2 : fields) {
							// 遍历域，处理实例内部域
							if (!field2.isEnumConstant() && !Modifier.isStatic(field2.getModifiers())) {
								if (field2.getType() == String.class) {
									// 只输出变量名为name或extName的变量
									// if(!field2.getName().equals("name") &&
									// !field2.getName().equals("extName")){
									// continue;
									// }

									if (!field2.isAccessible()) {
										field2.setAccessible(true);
									}

									ClazzStruct clazzStruct = enumClazzMap.get(clazz.getName());
									if (clazzStruct == null) {
										clazzStruct = new ClazzStruct(clazz.getName(), clazz);
										enumClazzMap.put(clazz.getName(), clazzStruct);
									}
									Instance instance = clazzStruct.instancesMap.get(field.getName());
									if (instance == null) {
										instance = new Instance(field.getName());
										clazzStruct.instancesMap.put(field.getName(), instance);
									}

									Object obj = field2.get(field.get(null));
									instance.fieldsMap.put(field2.getName(), new String[] { obj == null ? "" : obj.toString(), "" });

									if (!field2.isAccessible()) {
										field2.setAccessible(false);
									}
								}
							}
						}

						if (!field.isAccessible()) {
							field.setAccessible(false);
						}
					}
				}
			}
		}
	}

	static class ClazzStruct {
		final String clazzPath;
		final Class clazz;

		// <instanceName,Instance>
		final Map<String, Instance> instancesMap = new LinkedHashMap<String, Instance>();

		ClazzStruct(String clazzPath, Class clazz) {
			this.clazzPath = clazzPath;
			this.clazz = clazz;
		}

		static class Instance {
			final String instanceName;
			// <变量名,newString>
			final Map<String, String[]> fieldsMap = new LinkedHashMap<String, String[]>();

			Instance(String instanceName) {
				this.instanceName = instanceName;
			}
		}
	}
}
