package com.kola.kmp.logic.flow;

import com.kola.kmp.logic.util.tips.FlowTips;

/**
 * 
 * @author PERRY CHAN
 */
public enum KRoleAttrModifyType {

	/**
	 * getFlowDescr(Object... args)需要传入参数任务模板id
	 */
	任务奖励 {

		/**
		 * <pre>
		 * 获取任务奖励类型的流水描述
		 * </pre>
		 * @param args 任务id
		 */
		@Override
		public String getFlowDescr(Object... args) {
			if (args == null || args.length == 0 || !(args[0] instanceof Integer)) {
				return FlowTips.getTipsMissionReward(0);
			}
			return FlowTips.getTipsMissionReward((Integer) args[0]);
		}
	},
	/**
	 * getFlowDescr(Object... args)需要传入参数关卡id
	 */
	关卡奖励 {
		/**
		 * 获取关卡奖励类型的流水描述
		 * @param args 关卡id
		 */
		@Override
		public String getFlowDescr(Object... args) {
			if (args == null || args.length == 0 || !(args[0] instanceof Integer)) {
				return FlowTips.getTipsLevelReward(0);
			}
			return FlowTips.getTipsMissionReward((Integer) args[0]);
		}
	},
	
	/**
	 * getFlowDescr(Object... args)需要传入参数关卡id
	 */
	精英副本奖励 {
		/**
		 * 获取精英副本关卡奖励类型的流水描述
		 * @param args 精英副本关卡id
		 */
		@Override
		public String getFlowDescr(Object... args) {
			if (args == null || args.length == 0 || !(args[0] instanceof Integer)) {
				return FlowTips.getTipsJYCopyReward(0);
			}
			return FlowTips.getTipsJYCopyReward((Integer) args[0]);
		}
	},
	
	/**
	 * getFlowDescr(Object... args)需要传入参数关卡id
	 */
	技术副本奖励 {
		/**
		 * 获取技术副本关卡奖励类型的流水描述
		 * @param args 关卡id
		 */
		@Override
		public String getFlowDescr(Object... args) {
			if (args == null || args.length == 0 || !(args[0] instanceof Integer)) {
				return FlowTips.getTipsJSCopyReward(0);
			}
			return FlowTips.getTipsJSCopyReward((Integer) args[0]);
		}
	},
	
	/**
	 * getFlowDescr(Object... args)需要传入参数关卡id
	 */
	好友副本奖励 {
		/**
		 * 获取好友副本关卡奖励类型的流水描述
		 * @param args 关卡id
		 */
		@Override
		public String getFlowDescr(Object... args) {
			if (args == null || args.length == 0 || !(args[0] instanceof Integer)) {
				return FlowTips.getTipsFriendCopyReward(0);
			}
			return FlowTips.getTipsFriendCopyReward((Integer) args[0]);
		}
	},
	
	/**
	 * getFlowDescr(Object... args)需要传入参数关卡id
	 */
	随从副本奖励 {
		/**
		 * 获取随从副本关卡奖励类型的流水描述
		 * @param args 关卡id
		 */
		@Override
		public String getFlowDescr(Object... args) {
			if (args == null || args.length == 0 || !(args[0] instanceof Integer)) {
				return FlowTips.getTipsPetCopyReward(0);
			}
			return FlowTips.getTipsPetCopyReward((Integer) args[0]);
		}
	},
	/**
	 * getFlowDescr(Object... args)需要传入参数阶级id
	 */
	队伍竞技每日奖励 {
		/**
		 * 获取队伍竞技奖励类型的流水描述
		 * @param args 阶级id
		 */
		@Override
		public String getFlowDescr(Object... args) {
			if (args == null || args.length == 0 || !(args[0] instanceof Integer)) {
				return FlowTips.getTipsTeamPVPReward(0);
			}
			return FlowTips.getTipsTeamPVPReward((Integer) args[0]);
		}
	},
	名人堂点赞 {
		@Override
		public String getFlowDescr(Object... args) {
			return FlowTips.getTipsVisitHeroHall();
		}
	},
	拦截奖励 {
		@Override
		public String getFlowDescr(Object... args) {
			return FlowTips.getTipsBlockReward();
		}
	},
	军团资源战 {
		@Override
		public String getFlowDescr(Object... args) {
			if(args != null && args.length > 0) {
				return args[0].toString();
			}
			return this.name();
		}
	},
	使用道具 {
		@Override
		public String getFlowDescr(Object... args) {
			if(args != null && args.length > 0) {
				return args[0].toString();
			}
			return this.name();
		}
	},
	邮件附件 {
		@Override
		public String getFlowDescr(Object... args) {
			if(args != null && args.length > 0) {
				return args[0].toString();
			}
			return this.name();
		}
	},
	通用奖励 {
		@Override
		public String getFlowDescr(Object... args) {
			if(args != null && args.length > 0) {
				return args[0].toString();
			}
			return this.name();
		}
	},
	GM操作 {
		@Override
		public String getFlowDescr(Object... args) {
			return FlowTips.getTipsGmAction();
		}
	},
	竞技场奖励 {
		@Override
		public String getFlowDescr(Object... args) {
			return FlowTips.getTipsCompetitionReward();
		}
	},
	/**
	 * getFlowDescr(Object... args)需要传入参数载具id
	 */
	物资运输奖励 {
		/**
		 * 获取物资运输奖励类型的流水描述
		 * @param args 载具id
		 */
		@Override
		public String getFlowDescr(Object... args) {
			if (args == null || args.length == 0 || !(args[0] instanceof Integer)) {
				return FlowTips.getTipsTransportReward(0);
			}
			return FlowTips.getTipsTransportReward((Integer) args[0]);
		}
	},
	/**
	 * getFlowDescr(Object... args)需要传入参数载具id
	 */
	物资运输拦截奖励 {
		/**
		 * 获取物资运输拦截奖励类型的流水描述
		 * @param args 载具id
		 */
		@Override
		public String getFlowDescr(Object... args) {
			if (args == null || args.length == 0 || !(args[0] instanceof Integer)) {
				return FlowTips.getTipsInterceptReward(0);
			}
			return FlowTips.getTipsInterceptReward((Integer) args[0]);
		}
	},
	/**
	 * 可以传入任意描述
	 */
	其他 {
		@Override
		public String getFlowDescr(Object... args) {
			if(args != null && args.length > 0) {
				return args[0].toString();
			}
			return this.name();
		}
	},
	;
	
	/**
	 * 默认的参数
	 */
	public static final Object[] DEFAULT_ARGS = new Object[0];
	
	/**
	 * 
	 * 获取流水描述
	 *
	 * 
	 * @param args 第三方参数，如果没有可以传入{@link #DEFAULT_ARGS}
	 * @return
	 */
	public abstract String getFlowDescr(Object... args);
}
