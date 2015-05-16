package com.kola.kmp.logic.util.text;

import com.koala.game.util.StringUtil;
import com.kola.kmp.logic.other.KColorFunEnum;
import com.kola.kmp.logic.other.KColorFunEnum.KColor;
import com.kola.kmp.logic.other.KColorFunEnum.KColorManager;
import com.kola.kmp.logic.util.tips.RoleTips;

/**
 * <pre>
 * 需求：
 * 1.能将客户端发来的聊会天进行分析拆解，找出【[url=http://www.kl321.com/]链接[/url]】语音、图片附件格式
 * 2.能按基本格式对文字进行处理，例如对文字进行加粗、染色等处理
 * 
 * 富文本协议：	
 * 标签	效果
 * [b]文本[/b]	加粗
 * [i]文本[/i]	斜体
 * [u]文本[/u]	下划线：
 * [s]文本[/s]	删除线
 * [sub]文本[/sub]	向下缩小
 * [sup]文本[/sup]	向上缩小
 * [emo]xx[/emo]	表情，xx是表情索引，从01开始
 * [c?]文本[-]	变色，c?是颜色符号，参看【颜色表】
 * [ffffff]文本[-]	变色，ffffff是颜色值
 * 
 * [url=http://www.kl321.com/]链接[/url]	链接
 * 
 * 带菜单的角色名：下划线+颜色+菜单 [url=a4:角色名,角色ID,menuId,menuId,menuId][u][ffffff]文本[-][/u][/url]
 * 	
 * 其中URL的格式如下：	
 * a1:roleId,itemId	查看道具
 * a2:id	获取图片数据
 * a3:id	获取声音数据
 * a4:menuId,menuId,menuId	显示菜单
 * http:	打开网址
 * 
 * 
 * @author CamusHuang
 * @creation 2014-4-3 下午12:18:49
 * </pre>
 */
public final class HyperTextTool {
	/**
	 * <pre>
	 * 将一段可能含有"[url=xxx]"标记的文字拆解
	 * 
	 * @param htext
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-7 上午11:08:15
	 * </pre>
	 */
	public static HyperText split(String htext, boolean isClearDirtyWord) {
		return new HyperText(htext, isClearDirtyWord);
	}

	/**
	 * <pre>
	 * 对角色名进行染色
	 * 
	 * @param roleName
	 * @return
	 * @author CamusHuang
	 * @creation 2013-7-9 上午11:48:56
	 * </pre>
	 */
	public static String extRoleName(String roleName) {
		return extColor(roleName, KColorFunEnum.角色名);
	}

	/**
	 * <pre>
	 * 对【未曾染色的原始角色名】进行染色+下线划+菜单项处理
	 * 
	 * @deprecated 可以使用
	 * @param roleId
	 * @param roleName 未曾染色的原始角色名
	 * @return
	 * @author CamusHuang
	 * @creation 2014-7-16 下午4:56:58
	 * </pre>
	 */
	public static String extRoleNameWithMenu(long roleId, String roleName) {
		// 下划线+颜色+菜单 [url=a4:角色名,角色ID,menuId,menuId,menuId][u][ffffff]文本[-][/u][/url]
		String format = RoleTips.getRoleNameWithMenuFormat();
		return StringUtil.format(format, roleName, roleId, roleName);
	}
	
	/**
	 * <pre>
	 * 对文本进行染色
	 * 
	 * @param content
	 * @param type
	 * @return
	 * @author CamusHuang
	 * @creation 2013-9-10 下午6:02:28
	 * </pre>
	 */
	public static String extColor(String content, KColorFunEnum type) {
		return extColor(content, KColorManager.getColor(type.sign));
	}

	/**
	 * <pre>
	 * 对文本进行染色
	 * 
	 * NGUI直接支持的颜色格式：
	 * [颜色值]需要变色文本[-]
	 * [-]是指恢复默认颜色
	 * 
	 * 例如：
	 * [99ff00]NGUI[-]'s labels can have [ff0000]embedded[-] [0099ff]colors[-]
	 * 
	 * @param content
	 * @param type
	 * @return
	 * @author CamusHuang
	 * @creation 2013-9-10 下午6:02:28
	 * </pre>
	 */
	public static String extColor(String content, KColor color) {
		if (color == null) {
			return content;
		}
		StringBuffer sbf = new StringBuffer();
		sbf.append(HyperTextTypeEnum.col.startSign).insert(sbf.length() - 1, color.color);
		sbf.append(content).append(HyperTextTypeEnum.col.endSign);
		return sbf.toString();
	}

	/**
	 * <pre>
	 * 
	 * 
	 * @param content
	 * @param cx C1的1
	 * @return
	 * @author CamusHuang
	 * @creation 2014-6-24 上午10:17:52
	 * </pre>
	 */
	public static String extColor(String content, int cx) {
		KColor color = KColorManager.getColor("c"+cx);
		return extColor(content, color);
	}
	
	/**
	 * <pre>
	 * 对文本进行复杂的富文本扩展
	 * 
	 * @param content 文本
	 * @param menuIds 菜单IDs
	 * @param color 变色
	 * @param b 加粗
	 * @param i 斜体
	 * @param u 下划线
	 * @param s 删除线
	 * @param sub 向下缩小
	 * @param sup 向上缩小
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-7 上午11:44:01
	 * </pre>
	 */
	public static String extText(String content, int[] menuIds, KColor color, boolean b, boolean i, boolean u, boolean s, boolean sub, boolean sup) {
		if (content == null || content.isEmpty()) {
			return content;
		}

		StringBuffer sbf = new StringBuffer();
		// 添加前置标记
		if (menuIds != null && menuIds.length > 0) {
			sbf.append(HyperTextTypeEnum.url.startSign);
			sbf.insert(sbf.length() - 1, "a4:");
			for (int id : menuIds) {
				sbf.insert(sbf.length() - 1, id).insert(sbf.length() - 1, ',');
			}
			sbf.deleteCharAt(sbf.length() - 2);
		}
		if (b) {
			sbf.append(HyperTextTypeEnum.b.startSign);
		}
		if (i) {
			sbf.append(HyperTextTypeEnum.i.startSign);
		}
		if (u) {
			sbf.append(HyperTextTypeEnum.u.startSign);
		}
		if (s) {
			sbf.append(HyperTextTypeEnum.s.startSign);
		}
		if (sub) {
			sbf.append(HyperTextTypeEnum.sub.startSign);
		}
		if (sup) {
			sbf.append(HyperTextTypeEnum.sup.startSign);
		}
		if (color != null) {
			sbf.append(HyperTextTypeEnum.col.startSign);
			sbf.insert(sbf.length() - 1, color.color);
		}
		// 添加文本
		sbf.append(content);
		// 添加后置标记
		if (menuIds != null && menuIds.length > 0) {
			sbf.append(HyperTextTypeEnum.url.endSign);
		}
		if (b) {
			sbf.append(HyperTextTypeEnum.b.endSign);
		}
		if (i) {
			sbf.append(HyperTextTypeEnum.i.endSign);
		}
		if (u) {
			sbf.append(HyperTextTypeEnum.u.endSign);
		}
		if (s) {
			sbf.append(HyperTextTypeEnum.s.endSign);
		}
		if (sub) {
			sbf.append(HyperTextTypeEnum.sub.endSign);
		}
		if (sup) {
			sbf.append(HyperTextTypeEnum.sup.endSign);
		}
		if (color != null) {
			sbf.append(HyperTextTypeEnum.col.endSign);
		}
		return sbf.toString();
	}

	/**
	 * <pre>
	 * 将字符串中的"[c?]"替换成"[ffffff]"
	 * 请加载XML TIPS、Excel表格的工具方法内部调用此方法进行统一过滤
	 * 另外运行时也可以视情况手工调用
	 * 
	 * @param tips
	 * @return
	 * @author CamusHuang
	 * @creation 2014-5-15 下午12:41:36
	 * </pre>
	 */
	public static String replaseCx(String tips) {
		String result = tips;
		if (result.contains("[c")) {
			// 有可能包含c?颜色值
			for (KColor c : KColorManager.getAllColors()) {
				result = result.replace(c.getSignExt(), c.getColorExt());
			}
		}
		return result;
	}
}
