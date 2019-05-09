package com.kilotrees.service.adv.runtime;

import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.service.adv.runtime.api.ITaskRuntime;

public class advruntimefactory {
	public static ITaskRuntime createAdvRuntime(advtaskinfo adv, boolean isReamin) {
		ITaskRuntime runtime = null;
		if (isReamin == false) {
			if (adv.getAdv_type() == advtaskinfo.ADVTYPE_CPA_NORMAL) {
				runtime = new cpanewruntime();
			} else if (adv.getAdv_type() == advtaskinfo.ADVTYPE_CPA_RECHARGE1) {
				runtime = new cparechargeruntime1();
			} else if (adv.getAdv_type() == advtaskinfo.ADVTYPE_CPA_RECHARGE2) {
				runtime = new cparechargeruntime2();
			} else if (adv.getAdv_type() == advtaskinfo.ADVTYPE_FANSACT) {
				runtime = new RuntimeFeedFans();
			}
		} else {
			runtime = new cparemainruntime();
			runtime.setRemain(true);
		}
		runtime.setAdvinfo(adv);
		return runtime;
	}
}
