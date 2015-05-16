package com.kola.kmp.logic.util.dirtyword;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import com.koala.game.util.XmlUtil;
import com.kola.kmp.logic.support.DirtyWordSupport;

/**
 * <pre>
 * 参考网络上的相关算法，根据现实使用特点，剔除各种人性化需求、复杂或损耗大的算法实现、各种高精深实现，返璞归真
 * 
 * 逻辑：
 * 将所有脏词，按脏词前0~2个字符放到三级MAP进行保存
 * 过滤时，遍历目标String的每个字符，与MAP进行匹配
 * 
 * 缺点：
 * 目前只严格根据脏词表进行匹配，未特别处理不可见字符、乱码字符、空格等
 * 不可见字符：控制字符，ASCII值它们是0到31、以及127
 * PS：不可见字符，常被玩家用于注册角色名，冒充另一个玩家行骗。
 * 
 * 需求：
 * 1.过滤聊天、邮件中的显式脏词，替换成*；（允许敏感词？）
 * 2.过滤聊天、邮件中的不可见字符；（允许空格？）
 * 3.检测注册名称（角色，军团）中的显式脏词、敏感词
 * 4.检测注册名称（角色，军团）中的不可见字符、空格
 * @author PERRY CHAN
 * </pre>
 */
public class KDirtyWordSupportImpl implements DirtyWordSupport{
	
	/** CNEXT 用于替换脏词 */
	private final String DirtyWordReplace = "*";

	/**
	 * <pre>
	 * 脏词 + 敏感词库
	 * </pre>
	 */
	private WordData dirtyAndSensitiveMap = new WordData();

	/**
	 * <pre>
	 * 脏词库
	 * </pre>
	 */
	private WordData dirtyMap = new WordData();

	/**
	 * <pre>
	 * 不可见字符
	 * </pre>
	 */
	private HashSet<Character> unseeCharSet = new HashSet<Character>();
	/** ASCII值最大的不可见字符:提高性能 */
	private char maxUnseeChar;
	/** ASCII值最小的不可见字符:提高性能 */
	private char minUnseeChar;
	
	public KDirtyWordSupportImpl(){
	}
	
	public void init(String WordPath) { 
		//"./res/gsconfig/dirtyWords/dirtyWords.xml"
		// 脏词--加载助脏词表
		HashMap<String, String[]> dirtyWordTemp = new HashMap<String, String[]>();// 用Set，自动排除重复的脏词
		HashMap<String, String[]> sensitiveWordTemp = new HashMap<String, String[]>();// 用Set，自动排除重复的敏感词
		HashSet<Character> unseeCharTemp = new HashSet<Character>();// 用Set，自动排除重复的字符
		// /////////////// 加载模块配置
		Document doc = XmlUtil.openXml(WordPath);
		Element root = doc.getRootElement();
		List<Element> eList = root.getChild("unseeChars").getChildren();
		for (Element e : eList) {
			unseeCharTemp.add((char) Short.parseShort(e.getTextTrim()));
		}
		eList = root.getChild("sensitiveWord").getChildren();
		for (Element e : eList) {
			String tips = e.getAttributeValue("tips");
			if(tips != null && tips.isEmpty()){
				tips = null;
			}
			String word = e.getText();
			sensitiveWordTemp.put(word, new String[]{word, tips});
			// if(!sensitiveWordTemp.add(e.getText())){
			// System.err.println("重复的敏感词："+e.getText());
			// }
		}
		//
		eList = root.getChild("dirtyWords").getChildren();
		for (Element e : eList) {
			String tips = e.getAttributeValue("tips");
			if(tips != null && tips.isEmpty()){
				tips = null;
			}
			String word = e.getText();
			dirtyWordTemp.put(word, new String[]{word, tips});
		}
//		HashSet<String> dirtyWordTemp2 = new LinkedHashSet<String>();// 用Set，自动排除重复的脏词
//		for (Element e : eList) {
//			 if(dirtyWordTemp.add(e.getText().trim())){
//				 System.err.println("<word>"+e.getText().trim()+"</word>");
//			 } else {
//				 dirtyWordTemp2.add(e.getText().trim());
//			 }
//		}
//		for(String temp:dirtyWordTemp2){
//			System.err.println("<!-- <word>"+temp+"</word> -->");
//		}
		// ///////////////

		// 添加脏词到词库
		for (String[] str : dirtyWordTemp.values()) {
			addDirtyWord(str[0], str[1]);
		}
		// 添加敏感词到词库
		for (String[] str : sensitiveWordTemp.values()) {
			addSensitiveWord(str[0], str[1]);
		}
		// 添加不可见字符
		for (Character unseeChar : unseeCharTemp) {
			addUnseeChar(unseeChar);
		}
	}
	
	private void addDirtyWord(String dirtyWord) {
		addDirtyWord(dirtyWord, null);
	}

	/**
	 * <pre>
	 * <strong>所有添加的脏词中的大写字母将会被转换成小写</strong>
	 * 
	 * @deprecated 支持动态添加脏词，但一般情况下请勿动态调用
	 * @param str
	 * @author CamusHuang
	 * @creation 2013-2-18 下午6:51:25
	 * </pre>
	 */
	private void addDirtyWord(String dirtyWord, String specialTips) {
		dirtyWord = dirtyWord.toLowerCase();// dirtyWord.trim().toLowerCase();
		if (dirtyWord.length() < 1) {
			return;
		}

		// 添加到脏词库
		if (dirtyMap.allWords.add(dirtyWord)) {
			KLString str = new KLString(dirtyWord, specialTips);
			Character key = str.chars[0];
			Words1 words = dirtyMap.wordMap.get(key);
			if (words == null) {
				words = new Words1(key);
				dirtyMap.wordMap.put(key, words);
			}
			words.addWord(str);
		} else {
			// 脏词重复
		}

		// 添加到脏词+敏感词库
		if (dirtyAndSensitiveMap.allWords.add(dirtyWord)) {
			KLString str = new KLString(dirtyWord, specialTips);
			Character key = str.chars[0];
			Words1 words = dirtyAndSensitiveMap.wordMap.get(key);
			if (words == null) {
				words = new Words1(key);
				dirtyAndSensitiveMap.wordMap.put(key, words);
			}
			words.addWord(str);
		} else {
			// 脏词重复
		}
	}
	
	private void addSensitiveWord(String sensitiveWord) {
		addSensitiveWord(sensitiveWord, null);
	}

	/**
	 * <pre>
	 * <strong>所有添加的敏感词中的大写字母将会被转换成小写</strong>
	 * 
	 * @deprecated 支持动态添加敏感词，但一般情况下请勿动态调用
	 * @param str
	 * @return
	 * @author CamusHuang
	 * @creation 2013-2-18 下午6:51:25
	 * </pre>
	 */
	private void addSensitiveWord(String sensitiveWord, String specialTips) {
		sensitiveWord = sensitiveWord.toLowerCase();// sensitiveWord.trim().toLowerCase();
		if (sensitiveWord.length() < 1) {
			return;
		}

		// 添加到脏词+敏感词库
		if (dirtyAndSensitiveMap.allWords.add(sensitiveWord)) {
			KLString str = new KLString(sensitiveWord, specialTips);
			Character key = str.chars[0];
			Words1 words = dirtyAndSensitiveMap.wordMap.get(key);
			if (words == null) {
				words = new Words1(key);
				dirtyAndSensitiveMap.wordMap.put(key, words);
			}
			words.addWord(str);
		} else {
			// 脏词重复
		}
	}

	/**
	 * <pre>
	 * 添加不可见字符
	 * 
	 * @deprecated 暂时不允许动态调用
	 * @param unseeChar
	 * @author CamusHuang
	 * @creation 2013-2-20 上午9:42:27
	 * </pre>
	 */
	private void addUnseeChar(Character unseeChar) {
		unseeCharSet.add(unseeChar);
		if (maxUnseeChar < unseeChar) {
			maxUnseeChar = unseeChar;
		}
		if (minUnseeChar > unseeChar) {
			minUnseeChar = unseeChar;
		}
	}

	/**
	 * <pre>
	 * 检查是否包含脏词，如果包含，返回第一遇到的脏词
	 * <strong>用于名称注册时检测，检测范围包含敏感词和脏词</strong>
	 * 敏感词：例如“GM”，“客服代表”
	 * 脏词：例如“性交易”，“AV片”
	 * 不可见字符：例如“回车”
	 * 
	 * @param source
	 * @return NULL表示无脏词（包含不可见字符，会返回“ ”）
	 * </pre>
	 */
	public String containDirtyWord(String source) {
		if (source == null || source.length() < 1) {
			return null;
		}
		// 脏词--检测脏词<strong>用于名称注册时检测，检测范围包含敏感词和脏词</strong>
		// 先剔除不可见字符
		char[] orgSourceChars = source.toCharArray();// 维持原始大小写
		if (containUnseeChar(orgSourceChars)) {
			// 包含不可见字符
			return "空";
		}
		// char[] tempChars = clearUnseeChar(orgSourceChars);
		// if (tempChars.length != orgSourceChars.length) {
		// // 包含不可见字符
		// source = new String(tempChars);// 剔除不可见字符后，对应的字符串，维持原始大小写
		// orgSourceChars = tempChars;// 剔除不可见字符后，维持原始大小写
		// }
		//
		char[] lowerCaseSourceChars = source.toLowerCase().toCharArray();// 全小写

		// 遍历所有字符
		for (int index = 0; index < lowerCaseSourceChars.length; index++) {
			Words1 words1 = dirtyAndSensitiveMap.wordMap.get(lowerCaseSourceChars[index]);
			if (words1 == null) {
				// 不存在以sourceChars[index]开头的脏词
				continue;
			}
			if (words1.str1 != null) {
				// 命中
				// 字符sourceChars[index]是脏词
				// 返回原字符串的子串，主要是维持原字符串的大小写
				if(words1.str1.specialTips != null){
					return words1.str1.specialTips;
				}
				return source.substring(index, index + 1);// words1.str1.dirtyStr;
			}
			// 往后偏移1字符继续匹配
			int index1 = index + 1;
			if (index1 >= lowerCaseSourceChars.length) {
				continue;
			}
			Words2 words2 = words1.wordMap.get(lowerCaseSourceChars[index1]);
			if (words2 == null) {
				// 不存在以sourceChars[index]+sourceChars[index1]开头的脏词
				continue;
			}
			if (words2.str2 != null) {
				// 命中
				// 字符串sourceChars[index]+sourceChars[index1]是脏词
				// 返回原字符串的子串，主要是维持原字符串的大小写
				if(words2.str2.specialTips != null){
					return words2.str2.specialTips;
				}
				return source.substring(index, index1 + 1);// words2.str2.dirtyStr;
			}
			// 往后偏移2字符继续匹配
			int index2 = index + 2;
			if (index2 >= lowerCaseSourceChars.length) {
				continue;
			}
			Words3 words3 = words2.wordMap.get(lowerCaseSourceChars[index2]);
			if (words3 == null) {
				// 不存在以sourceChars[index]+sourceChars[index1]+sourceChars[index2]开头的脏词
				continue;
			}
			if (words3.str3 != null) {
				// 命中
				// 字符串sourceChars[index]+sourceChars[index1]+sourceChars[index2]是脏词
				// 返回原字符串的子串，主要是维持原字符串的大小写
				if(words3.str3.specialTips != null){
					return words3.str3.specialTips;
				}
				return source.substring(index, index2 + 1);// words3.str3.dirtyStr;
			}
			// 往后偏移3字符继续匹配
			int index3 = index + 3;
			if (index3 >= lowerCaseSourceChars.length) {
				continue;
			}
			Words4 words4 = words3.wordMap.get(lowerCaseSourceChars[index3]);
			if (words4 == null) {
				// 不存在以sourceChars[index]+sourceChars[index1]+sourceChars[index2]+sourceChars[index3]开头的脏词
				continue;
			}
			if (words4.str4 != null) {
				// 命中
				// 字符串sourceChars[index]+sourceChars[index1]+sourceChars[index2]+sourceChars[index3]是脏词
				// 返回原字符串的子串，主要是维持原字符串的大小写
				if(words4.str4.specialTips != null){
					return words4.str4.specialTips;
				}
				return source.substring(index, index3 + 1);// words4.str4.dirtyStr;
			}
			// 往后偏移4++字符继续匹配
			int moveIndex = 4;
			int index4 = index + moveIndex;
			if (index4 >= lowerCaseSourceChars.length) {
				continue;
			}
			int sourceReleaseLen = lowerCaseSourceChars.length - index;// 源字符串剩余长度
			for (KLString dirtyStr : words4.wordList) {
				// 遍历比较以sourceChars[index]+sourceChars[index1]+sourceChars[index2]+sourceChars[index3]开头的长度大于4的脏词
				if (sourceReleaseLen < dirtyStr.chars.length) {
					// 源字符串剩余长度比当前脏词短
					continue;
				}

				int indexB = index4;
				boolean isCatch = true;// 是否命中？
				for (int indexA = moveIndex; indexA < dirtyStr.chars.length; indexA++) {
					if (lowerCaseSourceChars[indexB] != dirtyStr.chars[indexA]) {
						// 匹配失败
						isCatch = false;
						break;
					}
					indexB++;
				}
				if (isCatch) {
					// 命中
					if(dirtyStr.specialTips != null){
						return dirtyStr.specialTips;
					}
					return source.substring(index, indexB);
				}
			}
		}
		return null;
	}

	/**
	 * <pre>
	 * 清理包含的脏词，将脏词替换成*号表示，返回清理后得到的字符串
	 * <strong>用于发言或公告，清理范围只包含脏词，而不包含敏感词</strong>
	 * 敏感词：例如“GM”，“客服代表”
	 * 脏词：例如“性交易”，“AV片”
	 * 
	 * PS：源字符串会被先清理不可见字符，再执行脏词清理；因此换行符等不可见字符将被清除。
	 * 
	 * @param source
	 * @return
	 * @author CamusHuang
	 * @creation 2013-2-18 下午4:59:27
	 * </pre>
	 */
	public String clearDirtyWords(String source) {
		return clearDirtyWords(source, false);
	}

	/**
	 * <pre>
	 * 清理包含的脏词，将脏词替换成*号表示，返回清理后得到的字符串
	 * <strong>用于发言或公告，清理范围只包含脏词，而不包含敏感词</strong>
	 * 敏感词：例如“GM”，“客服代表”
	 * 脏词：例如“性交易”，“AV片”
	 * 
	 * PS：源字符串会被先清理不可见字符，再执行脏词清理；
	 * 当dontClearUnseeChar=true且不含有脏词时，换行符等不可见字符将被保留。
	 * 
	 * @param source
	 * @param dontClearUnseeChar 不清理不可见字符
	 * @return
	 * @author CamusHuang
	 * @creation 2013-2-18 下午4:59:27
	 * </pre>
	 */
	public String clearDirtyWords(String sourceA, boolean dontClearUnseeChar) {
		if (sourceA == null || sourceA.length() < 1) {
			return sourceA;
		}

		// 脏词--清理脏词<strong>用于发言或公告，清理范围只包含脏词，而不包含敏感词</strong>
		String sourceB = sourceA;
		char[] orgSourceBChars = sourceB.toCharArray();// 维持原始大小写
		{
			// 先剔除不可见字符
			char[] tempBChars = clearUnseeChar(orgSourceBChars);
			if (tempBChars.length != orgSourceBChars.length) {
				// 包含不可见字符
				sourceB = new String(tempBChars);// 剔除不可见字符后，对应的字符串，维持原始大小写
				orgSourceBChars = tempBChars;// 剔除不可见字符后，维持原始大小写
			}
		}
		//
		StringBuilder result = new StringBuilder(128);
		boolean isContainDirtyWord = false;// 是否被命中清理过不可见字符或脏词
		char[] lowerCaseSourceBChars = sourceB.toLowerCase().toCharArray();// 全小写
		{
			// 遍历所有字符
			KLString dirtyWord;
			for (int index = 0; index < lowerCaseSourceBChars.length; index++) {
				dirtyWord = null;
				try {
					Words1 words1 = dirtyMap.wordMap.get(lowerCaseSourceBChars[index]);
					if (words1 == null) {
						// 不存在以sourceChars[index]开头的脏词
						continue;
					}
					if (words1.str1 != null) {
						// 命中
						// 字符sourceChars[index]是脏词
						// 返回原字符串的子串，主要是维持原字符串的大小写
						dirtyWord = words1.str1;// source.substring(index, index
												// +
												// 1);
						continue;
					}
					// 往后偏移1字符继续匹配
					int index1 = index + 1;
					if (index1 >= lowerCaseSourceBChars.length) {
						continue;
					}
					Words2 words2 = words1.wordMap.get(lowerCaseSourceBChars[index1]);
					if (words2 == null) {
						// 不存在以sourceChars[index]+sourceChars[index1]开头的脏词
						continue;
					}
					if (words2.str2 != null) {
						// 命中
						// 字符串sourceChars[index]+sourceChars[index1]是脏词
						// 返回原字符串的子串，主要是维持原字符串的大小写
						dirtyWord = words2.str2;// source.substring(index,
												// index1 +
												// 1);
						continue;
					}
					// 往后偏移2字符继续匹配
					int index2 = index + 2;
					if (index2 >= lowerCaseSourceBChars.length) {
						continue;
					}
					Words3 words3 = words2.wordMap.get(lowerCaseSourceBChars[index2]);
					if (words3 == null) {
						// 不存在以sourceChars[index]+sourceChars[index1]+sourceChars[index2]开头的脏词
						continue;
					}
					if (words3.str3 != null) {
						// 命中
						// 字符串sourceChars[index]+sourceChars[index1]+sourceChars[index2]是脏词
						// 返回原字符串的子串，主要是维持原字符串的大小写
						dirtyWord = words3.str3;// source.substring(index,
												// index2 +
												// 1);
						continue;
					}
					// 往后偏移3字符继续匹配
					int index3 = index + 3;
					if (index3 >= lowerCaseSourceBChars.length) {
						continue;
					}
					Words4 words4 = words3.wordMap.get(lowerCaseSourceBChars[index3]);
					if (words4 == null) {
						// 不存在以sourceChars[index]+sourceChars[index1]+sourceChars[index2]+sourceChars[index3]开头的脏词
						continue;
					}
					if (words4.str4 != null) {
						// 命中
						// 字符串sourceChars[index]+sourceChars[index1]+sourceChars[index2]+sourceChars[index3]是脏词
						// 返回原字符串的子串，主要是维持原字符串的大小写
						dirtyWord = words4.str4;// source.substring(index,
												// index3 +
												// 1);
						continue;
					}
					// 往后偏移4++字符继续匹配
					int moveIndex = 4;
					int index4 = index + moveIndex;
					if (index4 >= lowerCaseSourceBChars.length) {
						continue;
					}
					int sourceReleaseLen = lowerCaseSourceBChars.length - index;// 源字符串剩余长度
					for (KLString dirtyStr : words4.wordList) {
						// 遍历比较以sourceChars[index]+sourceChars[index1]+sourceChars[index2]+sourceChars[index3]开头的长度大于4的脏词
						if (sourceReleaseLen < dirtyStr.chars.length) {
							// 源字符串剩余长度比当前脏词短
							continue;
						}

						int indexB = index4;
						boolean isCatch = true;// 是否命中？
						for (int indexA = moveIndex; indexA < dirtyStr.chars.length; indexA++) {
							if (lowerCaseSourceBChars[indexB] != dirtyStr.chars[indexA]) {
								// 匹配失败
								isCatch = false;
								break;
							}
							indexB++;
						}
						if (isCatch) {
							// 命中
							dirtyWord = dirtyStr;// source.substring(index,
													// indexB);
							break;
						}
					}
				} finally {
					if (dirtyWord == null) {
						// 未命中
						result.append(orgSourceBChars[index]);
					} else {
						isContainDirtyWord = true;
						result.append(DirtyWordReplace);
						index += (dirtyWord.chars.length - 1);
					}
				}
			}
		}

		if (isContainDirtyWord) {
			// 包含脏词
			return result.toString();
		} else {
			if(dontClearUnseeChar){
				return sourceA;
			} else {
				return sourceB;
			}
		}
	}

	/**
	 * <pre>
	 * 剔除所有隐形字符
	 * 
	 * @param sourceChars
	 * @return
	 * @author CamusHuang
	 * @creation 2013-2-19 下午9:21:32
	 * </pre>
	 */
	private char[] clearUnseeChar(char[] sourceChars) {
		StringBuilder result = new StringBuilder(128);

		// 指定内容是否被替换过
		boolean isSourceChange = false;

		// 循环内容的每一个字符
		for (int i = 0; i < sourceChars.length; i++) {
			if (isUnseeChar(sourceChars[i])) {
				// 排除
				isSourceChange = true;
			} else {
				result.append(sourceChars[i]);
			}
		}

		if (isSourceChange) {
			return result.toString().toCharArray();
		} else {
			return sourceChars;
		}
	}

	private boolean containUnseeChar(char[] sourceChars) {
		// 循环内容的每一个字符
		for (int i = 0; i < sourceChars.length; i++) {
			if (isUnseeChar(sourceChars[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <pre>
	 * 检查指定字符是否隐形字符
	 * 
	 * @param cha
	 * @return
	 * @author CamusHuang
	 * @creation 2013-2-20 上午10:07:01
	 * </pre>
	 */
	private boolean isUnseeChar(char zhar) {
		if (zhar < minUnseeChar || zhar > maxUnseeChar) {
			return false;
		} else {
			return unseeCharSet.contains(zhar);
		}
	}

	/**
	 * <pre>
	 * 词库
	 * 
	 * @author CamusHuang
	 * @creation 2013-2-19 下午8:48:15
	 * </pre>
	 */
	static class WordData {
		/**
		 * <pre>
		 * 用Set保存所有被添加的词，用于排除重复添加的词
		 * </pre>
		 */
		private HashSet<String> allWords = new HashSet<String>();
		/**
		 * <pre>
		 * KEY = 字符串的第0字符
		 * </pre>
		 */
		private HashMap<Character, Words1> wordMap = new HashMap<Character, Words1>();
	}

	/**
	 * <pre>
	 * 保存长度大于等于1的字符串
	 * 
	 * @author CamusHuang
	 * @creation 2013-2-18 下午6:54:14
	 * </pre>
	 */
	private static class Words1 {
		Character keyChar1;// 字符串的第1字符(从1开始计数)
		KLString str1;// 长度等于1的字符串
		/**
		 * <pre>
		 * KEY=字符串的第2字符(从1开始计数)
		 * 长度大于1的字符串
		 * </pre>
		 */
		HashMap<Character, Words2> wordMap = new HashMap<Character, Words2>();

		private Words1(Character key) {
			this.keyChar1 = key;
		}

		private void addWord(KLString str) {
			if (str.chars.length == 1) {
				str1 = str;
				return;
			}
			Character key = str.chars[1];
			Words2 words = wordMap.get(key);
			if (words == null) {
				words = new Words2(key);
				wordMap.put(key, words);
			}
			words.addWord(str);
		}
	}

	/**
	 * <pre>
	 * 保存长度大于等于2的字符串
	 * 
	 * @author CamusHuang
	 * @creation 2013-2-18 下午6:54:39
	 * </pre>
	 */
	private static class Words2 {
		Character keyChar2;// 字符串的第2字符(从1开始计数)
		KLString str2;// 长度等于2的字符串
		/**
		 * <pre>
		 * KEY=字符串的第3字符(从1开始计数)
		 * 长度大于2的字符串
		 * </pre>
		 */
		HashMap<Character, Words3> wordMap = new HashMap<Character, Words3>();

		private Words2(Character key) {
			this.keyChar2 = key;
		}

		private void addWord(KLString str) {
			if (str.chars.length == 2) {
				str2 = str;
				return;
			}
			Character key = str.chars[2];
			Words3 words = wordMap.get(key);
			if (words == null) {
				words = new Words3(key);
				wordMap.put(key, words);
			}
			words.addWord(str);
		}
	}

	private static class Words3 {
		Character keyChar3;// 字符串的第3字符(从1开始计数)
		KLString str3;// 长度等于3的字符串
		/**
		 * <pre>
		 * KEY=字符串的第4字符(从1开始计数)
		 * 长度大于3的字符串
		 * </pre>
		 */
		HashMap<Character, Words4> wordMap = new HashMap<Character, Words4>();

		private Words3(Character key) {
			this.keyChar3 = key;
		}

		private void addWord(KLString str) {
			if (str.chars.length == 3) {
				str3 = str;
				return;
			}
			Character key = str.chars[3];
			Words4 words = wordMap.get(key);
			if (words == null) {
				words = new Words4(key);
				wordMap.put(key, words);
			}
			words.addWord(str);
		}
	}

	private static class Words4 {
		Character keyChar4;// 字符串的第4字符(从1开始计数)
		KLString str4;// 长度等于4的字符串
		/**
		 * <pre>
		 * 长度大于4的字符串
		 * </pre>
		 */
		List<KLString> wordList = new LinkedList<KLString>();

		private Words4(Character key) {
			this.keyChar4 = key;
		}

		private void addWord(KLString str) {
			if (str.chars.length == 4) {
				str4 = str;
				return;
			}
			wordList.add(str);
			// 重排
			Collections.sort(wordList, klComparator);
		}
	}

	private static class KLString {
		String dirtyStr;//原字符串（英文字符小化）
		char[] chars;//字符串对应的字符组
		String specialTips;//用于提示脏词的替代词（例如空格，则提示玩家：不能输入“空格”），可为null表示无需特别提示

		private KLString(String dirtyStr, String specialTips) {
			this.dirtyStr = dirtyStr;
			chars = this.dirtyStr.toCharArray();
			this.specialTips = specialTips;
		}
	}

	static final Comparator<KLString> klComparator = new Comparator<KLString>() {
		@Override
		public int compare(KLString str1, KLString str2) {
			// 字符数较多的优先命中
			return str2.chars.length - str1.chars.length;
		}
	};

	public static void main(String[] s) throws Exception {
		presureTest();
		// System.err.println(support.containDirtyWord("123kiss45fu"+'\t'+"ck67kiss8987654321"));
		// System.err.println(support.clearDirtyWords("123p45fuck67kiss8987654321换"+'\t'+"妻"));

	}

	/**
	 * <pre>
	 * 处理数量 = 50000 
	 * 处理总耗时 = 99.71 毫秒 
	 * 平均耗时 = 0.001994 毫秒
	 * 
	 * 处理数量 = 100000
	 * 处理总耗时 = 134.22 毫秒
	 * 平均耗时     = 0.0013 毫秒
	 * 
	 * 加载耗时 = 250 毫秒
	 * 处理数量 = 1000000
	 * 处理总耗时 = 671003252 纳秒
	 * 处理总耗时 = 671.00 毫秒
	 * 平均耗时     = 0.0007 毫秒
	 * 
	 * 处理数量 = 10000000
	 * 处理总耗时 = 5922.36 毫秒
	 * 平均耗时     = 0.0006 毫秒
	 * 
	 * 处理数量 = 100000000
	 * 处理总耗时 = 56975.43 毫秒
	 * 平均耗时     = 0.0006 毫秒
	 * 
	 * 处理数量 = 100000000
	 * 处理总耗时 = 55133993803 纳秒
	 * 处理总耗时 = 55134.00 毫秒
	 * 平均耗时     = 0.0006 毫秒
	 * 
	 * @author CamusHuang
	 * @throws Exception 
	 * @creation 2013-2-20 下午12:29:57
	 * </pre>
	 */
	private static void presureTest() throws Exception {
		// 脏词--简单性能测试
		Thread.sleep(500);
		long timeA = System.currentTimeMillis();
		KDirtyWordSupportImpl support = new KDirtyWordSupportImpl();
		long timeB = System.currentTimeMillis();
		System.err.println("加载耗时 = " + (timeB - timeA) + " 毫秒");
		String[] words = new String[] { "fu" + '\t' + "ck过来组队啊，你" + '\n' + "妹哦", "操" + '\f' + "你妈，快点关注我", "你个傻 B刁" + '\b' + "你妹", "要开" + '\n' + "奖了，快T" + '\f' + "MD充钱", "小妹，别理他，他想日" + '\r' + "你" };
		// ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
		Thread.sleep(500);
		int Count = 200 * 10000;
		timeA = System.nanoTime();
		for (int i = 0; i < Count; i++) {
			for (String word : words) {
				// System.err.println(support.clearDirtyWords(word));
				// rwLock.writeLock().lock();
				word = support.clearDirtyWords(word);
				// rwLock.writeLock().unlock();
			}
		}
		System.err.println("处理数量 = " + (Count * words.length));
		timeB = System.nanoTime();
		System.err.println("处理总耗时 = " + (timeB - timeA) + " 纳秒");
		float time = (float) (timeB - timeA) / 1000000;
		BigDecimal b = new BigDecimal(time);
		System.err.println("处理总耗时 = " + b.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + " 毫秒");
		time = (float) ((timeB - timeA) / Count / words.length) / 1000000;
		b = new BigDecimal(time);
		System.err.println("平均耗时     = " + b.setScale(4, BigDecimal.ROUND_HALF_UP).toString() + " 毫秒");
	}
}
