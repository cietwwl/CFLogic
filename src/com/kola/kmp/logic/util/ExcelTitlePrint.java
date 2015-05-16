package com.kola.kmp.logic.util;

import java.io.File;
import java.util.List;

import com.koala.game.util.KGameExcelFile;
import com.koala.game.util.KGameExcelTable;
import com.koala.game.util.KGameExcelTable.KGameExcelRow;

/**
 * <pre>
 * 打印按当前策划的表头结构
 * 打印出来的内容可以COPY到与表头对应的类中作为匹配加载的ExcelData类
 * 
 * @author CamusHuang
 * @creation 2014-2-23 下午3:55:46
 * </pre>
 */
public class ExcelTitlePrint {

	public static void main(String[] s) throws Exception {
		 printXLS();
//		printFileNamesInDir();
	}

	private static void printXLS() throws Exception {
		// String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\物品表.xls";//EXCE路径
		// int[] sheetIndex = new int[] { 2,3,4,5,6,7};//要打印第几张sheet（从1开始计数）
//		 String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\好友推送.xls";
		String fileUrl = "E:\\project\\-KMP-\\KMPLogic\\res\\gamedata\\mountModule\\机甲表(3).xls";
		// int[] sheetIndex = new int[] { 1,3};
		// String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\背包扩充表.xls";//EXCE路径
		// int[] sheetIndex = new int[] { 1};//要打印第几张sheet（从1开始计数）
		// String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\怪物形象表.xls";//EXCE路径
		// String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\怪物模板.xls";//EXCE路径
		// String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\排行榜点赞表.xls";
		// String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\NPC模板表.xls";
		// String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\时装表.xls";
		// String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\装备系统表.xls";
		// String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\装备齐鸣套装.xls";
		// String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\座驾表.xls";
		// String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\障碍物及陷阱表.xls";
//		 String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\军团表.xls";
//		 String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\vip功能.xls";
		// String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\随机商店表.xls";
//		String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\奖励福利表.xls";
//		 String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\资源争夺.xls";
//		String fileUrl = "E:\\KMP\\策划文档\\s-数据配置表\\军团战表.xls";
		int[] sheetIndex = new int[] { 1,2,3,4,5,6,7,8 };// 要打印第几张sheet（从1开始计数）

		// String fileUrl = "./res/gamedata/npcModule/NPCTemplate.xls";
		// int[] sheetIndex = new int[] { 8 };

		// /////////
		KGameExcelFile file = new KGameExcelFile(fileUrl);
		for (int index : sheetIndex) {
			try {
				print(file, index);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void print(KGameExcelFile file, int sheetIndex) throws Exception {
		int HeaderIndex = 3;
		KGameExcelTable table = file.getTable(file.getAllSheetNames()[sheetIndex - 1], HeaderIndex);

		System.err.println("加载[" + table.getTableName() + "]");

		KGameExcelRow[] rows = table.getAllDataRows();

		KGameExcelRow typeRow = rows[0];
		KGameExcelRow nameRow = rows[1];
		// KGameExcelRow descRow = rows[0];
		List<String> headNames = table.getHeaderNames();
		for (String desc : headNames) {
			String type = typeRow.getData(desc);
			if (type.equals("string")) {
				type = "String";
			}
			if (type.equals("string[]")) {
				type = "String[]";
			}
			if (type.equals("bool")) {
				type = "boolean";
			}
			String name = nameRow.getData(desc);

			if (type.isEmpty() && name.isEmpty() && desc.isEmpty()) {
				continue;
			}
			System.err.println("public " + type + " " + name + ";//" + desc);
		}
	}

	private static void printFileNamesInDir() {
		String dirUrl = "E:\\KMP\\策划文档\\s-数据配置表";
		File dir = new File(dirUrl);
		if (dir.isFile()) {
			return;
		}
		for (File file : dir.listFiles()) {
			System.err.println(file.getName());
		}
	}
}
