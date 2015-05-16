package com.kola.kmp.logic.util.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.kola.kmp.logic.support.KSupportFactory;

/**
 * <pre>
 * 一个富文本化的对象
 * 主要是拆分出非URL标志和URL标志
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
 * [url=http://www.kl321.com/]链接[/url]	链接
 * [c?]文本[-]	变色，c?是颜色符号，参看【颜色表】
 * [ffffff]文本[-]	变色，ffffff是颜色值
 * 	
 * 其中URL的格式如下：	
 * a1:roleId,itemId	查看道具
 * a2:id	获取图片数据
 * a3:id	获取声音数据
 * a4:menuId,menuId,menuId	显示菜单
 * http:	打开网址
 * 
 * @author CamusHuang
 * @creation 2014-4-7 上午10:04:42
 * </pre>
 */
public class HyperText {
	public static final char MARKUP_BEGIN = '[';
	public static final String MARKUP_URL_BEGIN = "[url=";
	public static final char MARKUP_END = ']';

	// /////////////////////////////////////
	// 是否包含URL标记
	private boolean isContainUrlSign;
	/**
	 * 分解得到的元素 只能是'{@link String}'和'{@link HyperURL}'这两种{@link Object}
	 */
	private List<Object> splitObjes;

	private String resultString;

	public HyperText(String text, boolean isClearDirtyWord) {
		split2(this, text, isClearDirtyWord);
		//
		StringBuffer sbf = new StringBuffer();
		for (Object obj : splitObjes) {
			sbf.append(obj.toString());
		}
		resultString = sbf.toString();
	}

	/**
	 * <pre>
	 * 是否包含URL标记
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-7 上午10:34:56
	 * </pre>
	 */
	public boolean isContainUrlSign() {
		return isContainUrlSign;
	}

	/**
	 * <pre>
	 * 
	 * @return 只能是'{@link String}'和'{@link HyperURL}'这两种{@link Object}
	 * @author CamusHuang
	 * @creation 2014-4-7 上午10:35:36
	 * </pre>
	 */
	public List<Object> getSplitObjes() {
		return splitObjes;
	}

	/**
	 * <pre>
	 * 获取结果字符串
	 * 
	 * @return
	 * @author CamusHuang
	 * @creation 2014-4-7 上午10:38:53
	 * </pre>
	 */
	public String toString() {
		return resultString;
	}

	/**
	 * <pre>
	 * 当{@link splitObjes}的内容被外部修改后，外部需要显式调用本方法重新生成结果字符串
	 * 
	 * @author CamusHuang
	 * @creation 2014-4-8 上午10:56:39
	 * </pre>
	 */
	public void remakeResultString() {
		StringBuffer sbf = new StringBuffer();
		for (Object obj : splitObjes) {
			if (obj instanceof HyperURL) {
				((HyperURL) obj).remakeResultString();
			}
			sbf.append(obj.toString());
		}
		resultString = sbf.toString();
	}

	/**
	 * <pre>
	 * 对字符串进行分解
	 * 
	 * @param hText
	 * @author CamusHuang
	 * @creation 2014-4-7 上午10:06:19
	 * </pre>
	 */
	private static void split2(HyperText hText, String orgText, boolean isClearDirtyWord) {
		if (orgText == null || orgText.isEmpty()) {
			hText.splitObjes = Collections.emptyList();
			return;
		}

		// 先拆分字符串
		List<Object> splitObjes = new ArrayList<Object>();
		int maxLen = orgText.length();
		for (int index = 0; index < maxLen;) {

			AtomicInteger nowIndex = new AtomicInteger(index);
			String tag = searchStartTag(orgText, nowIndex);
			if (tag == null) {
				// 后续没有任何富文本标志：后续是纯字符串
				String temp = orgText.substring(index);
				if (isClearDirtyWord) {
					temp = KSupportFactory.getDirtyWordSupport().clearDirtyWords(temp);
				}
				splitObjes.add(temp);
				break;
			} else {
				// 找到富文本标志
				if (nowIndex.get() != index) {
					// 起始标记到当前标志：是一段纯字符串
					String temp = orgText.substring(index, nowIndex.get());
					if (isClearDirtyWord) {
						temp = KSupportFactory.getDirtyWordSupport().clearDirtyWords(temp);
					}
					splitObjes.add(temp);
				}

				if (tag.startsWith(MARKUP_URL_BEGIN)) {
					// 是URL
					splitObjes.add(new HyperURL(tag));
				} else {
					// 不是URL
					splitObjes.add(tag);
				}
				//
				index = nowIndex.get() + tag.length();
			}
		}

		// 再将非URL串成字符串
		hText.splitObjes = new ArrayList<Object>();
		StringBuffer sbf = new StringBuffer();
		for (Object temp : splitObjes) {
			if (temp instanceof String) {
				sbf.append(temp);
			} else {
				// URL
				if (sbf.length() > 0) {
					hText.splitObjes.add(sbf.toString());
					sbf = new StringBuffer();
				}
				hText.splitObjes.add(temp);
				hText.isContainUrlSign = true;
			}
		}
		
		if (sbf.length() > 0) {
			hText.splitObjes.add(sbf.toString());
		}
	}

	private static String searchStartTag(String orgText, AtomicInteger nowIndex) {

		int startIndex = nowIndex.get();
		//
		int maxLen = orgText.length();
		for (int index = startIndex; index < maxLen;) {
			int startPos = orgText.indexOf(MARKUP_BEGIN, index);
			if (startPos < 0) {
				// 找不到"["标志：后续是纯字符串
				return null;
			}

			String tag = analyzeTag(orgText, startPos);
			if (tag != null) {
				// 找到富文本标志
				nowIndex.set(startPos);
				return tag;
			}

			// 找不到富文本标志
			index = startPos + 1;
		}

		// 找不到富文本标志：后续是纯字符串
		return null;
	}

	/**
	 * <pre>
	 * 从指定起始位置，分析后续是否构成一个合法的富文本标志
	 * 
	 * @param orgText 原始字符串
	 * @param startPos 起始标志位'['的位置
	 * @return null表示非有效标志，否则返回相应标志
	 * @author CamusHuang
	 * @creation 2014-4-8 上午9:40:19
	 * </pre>
	 */
	private static String analyzeTag(String orgText, int startPos) {

		int maxLen = orgText.length();

		// CTODO 改成遍历TagTypeEnum进行匹配

		AtomicBoolean isOver = new AtomicBoolean(false);
		int minLen = 0;
		// [u]
		{
			minLen = 3;
			String tag = analyzeTag2(orgText, startPos, minLen, isOver);
			if (tag != null) {
				return tag;
			}
			if (isOver.get()) {
				return null;
			}
		}

		// [\\u]
		{
			minLen = 4;
			String tag = analyzeTag2(orgText, startPos, minLen, isOver);
			if (tag != null) {
				return tag;
			}
			if (isOver.get()) {
				return null;
			}
		}

		// [sub]
		{
			minLen = 5;
			String tag = analyzeTag2(orgText, startPos, minLen, isOver);
			if (tag != null) {
				return tag;
			}
			if (isOver.get()) {
				return null;
			}
		}

		// [\\sub]
		{
			minLen = 6;
			String tag = analyzeTag2(orgText, startPos, minLen, isOver);
			if (tag != null) {
				return tag;
			}
			if (isOver.get()) {
				return null;
			}
		}

		// [ffffff]
		{
			minLen = 8;
			int maxIndex = startPos + minLen;
			if (maxLen < maxIndex) {
				// [ffffff]至少要求有minLen个字符
				return null;
			}
			// 偏移minLen-1个字符是否为"]"
			if (orgText.charAt(maxIndex - 1) == MARKUP_END) {
				boolean isColor = true;
				int tempMax = maxIndex - 1;
				for (int tempIndex = startPos + 1; tempIndex < tempMax; tempIndex++) {
					char c = orgText.charAt(tempIndex);
					if (('0' <= c && c <= '9') || ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z')) {
						// 是颜色值范围
						continue;
					}
					// 非颜色值范围
					isColor = false;
					break;
				}
				if (isColor) {
					String tag = orgText.substring(startPos, maxIndex);
					return tag;
				}
			}
		}
		// [url=a1:]
		{
			minLen = 9;
			int maxIndex = minLen + startPos;
			if (maxLen < maxIndex) {
				// [url=a1:]至少要求有minLen个字符
				return null;
			}
			// 找结束符"]"
			int endPos = orgText.indexOf(MARKUP_END, startPos);
			if (endPos < 0) {
				// 找不到结束符"]"
				return null;
			}
			String tag = orgText.substring(startPos, endPos + 1);
			if (!tag.startsWith(MARKUP_URL_BEGIN)) {
				return null;
			}
			return tag;
		}
	}

	/**
	 * <pre>
	 * 
	 * @param orgText
	 * @param targetPos
	 * @param minLen
	 * @param isOver
	 * @return null表示非有效标志，否则返回相应标志
	 * @author CamusHuang
	 * @creation 2014-4-8 上午9:41:40
	 * </pre>
	 */
	private static String analyzeTag2(String orgText, int targetPos, int minLen, AtomicBoolean isOver) {
		int maxLen = orgText.length();
		if (maxLen - targetPos < minLen) {
			// [u]至少要求有minLen个字符
			isOver.set(true);
			return null;
		}
		// 偏移minLen-1个字符是否为"]"
		if (orgText.charAt(targetPos + minLen - 1) != MARKUP_END) {
			return null;
		}

		String tag = orgText.substring(targetPos, targetPos + minLen);
		HyperTextTypeEnum htTypeEnum = HyperTextTypeEnum.getEnum(tag);
		if (htTypeEnum != null) {
			// bingo [u]
			return tag;
		}

		return null;
	}

	/**
	 * <pre>
	 * [url=http://www.kl321.com/]
	 * a1:roleId,itemId	查看道具
	 * a2:id	获取图片数据
	 * a3:id	获取声音数据
	 * a4:menuId,menuId,menuId	显示菜单
	 * http:	打开网址
	 * 
	 * 
	 * @author CamusHuang
	 * @creation 2014-4-7 上午10:44:12
	 * </pre>
	 */
	public static class HyperURL {

		private String resultString;// 即"[url=http://www.kl321.com/]"
		public final HyperUrlTagEnum tag;
		public final String[] params;
		// //////
		public static final String SplitTag = ":";
		public static final String ParamsSplitTag = ",";

		private HyperURL(String urlText) {
			resultString = urlText;
			// 即"http://www.kl321.com/"
			String urlContent = urlText.substring(MARKUP_URL_BEGIN.length(), urlText.length() - 1);
			String[] temp = urlContent.split(SplitTag);
			tag = HyperUrlTagEnum.getEnum(temp[0]);
			params = temp[1].split(ParamsSplitTag);
		}

		private void remakeResultString() {
			StringBuffer sbf = new StringBuffer();
			for (String obj : params) {
				sbf.append(obj);
			}
			resultString = sbf.toString();
		}

		public String toString() {
			return resultString;
		}
	}

	public static enum HyperUrlTagEnum {
		/** a1:roleId,itemId 查看道具 */
		a1,
		/** a2:id 获取图片数据 */
		a2,
		/** a3:id 获取声音数据 */
		a3,
		/** a4:menuId,menuId,menuId 显示菜单 */
		a4,
		/** http: 打开网址 */
		http, ;

		// 所有枚举
		private static final Map<String, HyperUrlTagEnum> typeMap = new HashMap<String, HyperUrlTagEnum>();
		static {
			for (HyperUrlTagEnum type : HyperUrlTagEnum.values()) {
				typeMap.put(type.name(), type);
			}
		}

		// //////////////////
		/**
		 * <pre>
		 * 通过标识数值获取枚举对象
		 * 
		 * @param sign
		 * @return
		 * @author CamusHuang
		 * @creation 2012-11-5 上午10:53:13
		 * </pre>
		 */
		public static HyperUrlTagEnum getEnum(String sign) {
			return typeMap.get(sign);
		}

	}

	public static void main(String[] s) {
		HyperTextTypeEnum type = HyperTextTypeEnum.getEnum("[//emo]");

		String urlText = "asdf[b]a[]s[u]f[//u]a[f[//b][url=a2:1][s][ffffff]链]接[-][//s][//url][emo]1[/emo][url=a3:1][s]链接[//s][//url]";

		HyperText hText = new HyperText(urlText, false);

		for (Object obj : hText.splitObjes) {
			System.err.println(obj);
		}
		System.err.println(hText.isContainUrlSign);
		System.err.println(hText);

		// StringBuffer sbf = new StringBuffer("abcdefg");
		// sbf.insert(7, "111");
		// System.err.println(sbf.toString());
	}
}
