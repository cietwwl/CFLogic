package com.kola.kmp.logic.relationship;


public class KRelationShipDataStructs {

	public static class RSPushData {
		// ----------以下是EXCEL表格直导数据---------
		public int lvl;//推送等级
		public int amount;//推送人数
		public int minlevel;//优先筛选最低等级
		public int maxlevel;//优先筛选最高等级
		
		public int nextMinlevel;//备选筛选最低等级
		public int nextMaxlevel;//备选筛选最高等级
		public boolean isNextRangeAdd;//备选范围从低到高

		// ----------以下是逻辑数据---------

		void notifyCacheLoadComplete() throws Exception {
			
			if (amount < 1) {
				throw new Exception("数值错误 amount = " + amount);
			}

			if (lvl < 1) {
				throw new Exception("数值错误 lvl = " + lvl);
			}
			
			if (minlevel < 1) {
				throw new Exception("数值错误 minlevel = " + minlevel);
			}
			if (minlevel > maxlevel) {
				throw new Exception("数值错误 minlevel = " + minlevel);
			}
			
			//
			if (nextMinlevel < 1) {
				throw new Exception("数值错误 nextMinlevel = " + nextMinlevel);
			}
			if (nextMinlevel > nextMaxlevel) {
				throw new Exception("数值错误 nextMaxlevel = " + nextMaxlevel);
			}
			
			//
			if(minlevel < nextMinlevel && nextMinlevel < maxlevel){
				throw new Exception("范围重叠 nextMinlevel = " + nextMinlevel);
			}
			if(minlevel < nextMaxlevel && nextMaxlevel < maxlevel){
				throw new Exception("范围重叠 nextMaxlevel = " + nextMaxlevel);
			}
			// CTODO 其它约束检查
		}

	}
}
