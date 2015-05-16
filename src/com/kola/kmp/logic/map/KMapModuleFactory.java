package com.kola.kmp.logic.map;


public class KMapModuleFactory {
	private static KMapModule _module;

	public static KMapModule getModule() {
		return _module;
	}

	/**
	 * 设置Module实例
	 * 
	 * @deprecated 仅供包内使用
	 * @param pModule
	 */
	public static void setModule(KMapModule pModule) {
		if (pModule == null) {
			throw new NullPointerException(KMapModuleFactory.class.getSimpleName() + "#setModule方法，传入的pModule实例为空！");
		} else if (_module != null) {
			throw new RuntimeException(KMapModuleFactory.class.getSimpleName() + "#setModule方法，重复设置Module实例！");
		} else {
			_module = pModule;
		}
	}
}
