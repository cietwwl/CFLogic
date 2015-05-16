package com.kola.kmp.logic.support;

/**
 * 
 * @author PERRY CHAN
 */
public interface DirtyWordSupport {

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
	public String containDirtyWord(String source);

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
	public String clearDirtyWords(String source);
	
	
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
	public String clearDirtyWords(String source, boolean dontClearUnseeChar);	
}
