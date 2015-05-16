package com.kola.kmp.logic.other;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;
import com.kola.kgame.cache.util.ReflectPaser;
import com.kola.kgame.cache.util.UtilTool;

/**
 * <pre>
 * 【颜色应用枚举】
 * 本枚举纯粹用于方便记录哪种应用情况使用了哪种【颜色标识】
 * 枚举中指定的【颜色标识】会在加载【颜色表】时进行检查验证
 * 
 * 【颜色表】请参考<@link #KColorManager>
 * 
 * 关于颜色的使用：
 * 1.聊天模块在角色登陆时会将【颜色表】发送给客户端
 * 2.策划在各种表格、配置XML中填写【颜色标识】例如"c0"
 * 3.技术在硬代码中，不直接使用【颜色标识】及【16进制ARGB值】，
 * 	而是使用本枚举，若枚举情况不存在则可直接新增枚举项（【颜色标识】必须存在于XML【颜色表】中）
 * 
 * @author CamusHuang
 * @creation 2014-3-8 下午3:11:27
 * </pre>
 */
public enum KColorFunEnum {

//	白色("c0"), //
//	品质_绿("c5"), //
//	品质_蓝("c4"), //
//	品质_紫("c10"), //
//	品质_橙("c2"), //
//	品质_红("c1"), //
//	淡蓝色("c9"), //
//	任务橙色("c13"), //
//	黄色("c14"), //
//	淡红色("c16"), //
//	正黄色("c17"), //
	黄色("c0"),
	绿色("c1"),
	蓝色("c2"),
	橙色("c3"),
	红色("c4"),
	浅黄色_文本("c5"),
	品质_绿("c6"),
	品质_蓝("c7"),
	品质_紫("c8"),
	品质_橙("c10"),
	品质_红("c9"),
	白色("c11"),
	// --------------------------
	指令_停服公告(黄色), //
	角色名(蓝色), //
//	货币(橙色), //
	属性(蓝色), //
//	道具(红色), //
//	时装(绿色), //
//	VIP(红色), //
//	机甲(橙色), //
//	任务橙色(橙色),
//	军团战状态(橙色),
	私聊发送时间(橙色),
	军团名称(蓝色), //
//	技能名称(红色), //
	科技名称(橙色), //
//	菜单前缀(黄色), //
//	菜单内容(白色); //

	;
	/**
	 * 即c0、c1
	 */
	public final String sign;

	private KColorFunEnum(String pSign) {
		this.sign = pSign;
	}

	private KColorFunEnum(KColorFunEnum pSign) {
		this.sign = pSign.sign;
	}

	/**
	 * <pre>
	 * 【颜色表】请参考./res/gsconfig/color.xls
	 * 【颜色表】定义了【颜色标识】及【16进制RGB值】，例如ffffff
	 * 
	 * @author CamusHuang
	 * @creation 2014-3-8 下午3:20:04
	 * </pre>
	 */
	public static class KColorManager {
		// --------------【颜色表】----------------
		// 所有颜色 KEY=【颜色标识】即c0、c1
		private static Map<String, KColor> colorMap = new HashMap<String, KColorFunEnum.KColor>();
		// 所有颜色
		private static List<KColor> colorList;

		/**
		 * <pre>
		 * 通过【颜色标识】获取枚举对象
		 * 
		 * @param c  即c0、c1
		 * @return
		 * @author CamusHuang
		 * @creation 2012-11-5 上午10:53:13
		 * </pre>
		 */
		public static KColor getColor(String c) {
			return colorMap.get(c);
		}

		public static List<KColor> getAllColors() {
			return colorList;
		}

		public static void loadColors(KGameExcelTable table) throws Exception {
			KGameExcelRow[] rows = table.getAllDataRows();
			if (rows.length < 1) {
				throw new Exception("加载[" + table.getTableName() + "]错误：有效行数为0！");
			}

			colorList = ReflectPaser.parseExcelData(KColor.class, table.getHeaderNames(), rows, true);
			//
			for (KColor color : colorList) {
				colorMap.put(color.sign, color);
				color.init();
			}
		}
	}

	/**
	 * <pre>
	 * 【颜色】
	 * 
	 * @author CamusHuang
	 * @creation 2014-3-8 下午3:10:25
	 * </pre>
	 */
	public static class KColor {
		/**
		 * 【颜色标识】即c0、c1
		 */
		public String sign;
		/**
		 * 【16进制RGB值】
		 */
		public String color;
		/**
		 * 名字
		 */
		public String name;
		
		private String signExt;
		private String colorExt;
		private int colorValue;
		
		private void init(){
			signExt = "["+sign+"]";
			colorExt = "["+color+"]";
			colorValue = UtilTool.getStringToInt(color, 16);
		}

		/**
		 * <pre>
		 * 
		 * 
		 * @return "[c?]"
		 * @author CamusHuang
		 * @creation 2014-5-15 下午12:47:06
		 * </pre>
		 */
		public String getSignExt() {
			return signExt;
		}
		/**
		 * <pre>
		 * 
		 * 
		 * @return "[ffffff]"
		 * @author CamusHuang
		 * @creation 2014-5-15 下午12:47:17
		 * </pre>
		 */
		public String getColorExt() {
			return colorExt;
		}
		
		/**
		 * <pre>
		 * 
		 * 返回颜色值所代表的rgb的数值形式
		 * 
		 * </pre>
		 * @return
		 */
		public int getColorValue() {
			return colorValue;
		}
	}
}
