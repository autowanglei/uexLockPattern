package org.zywx.wbpalmstar.plugin.uexlockpattern.util;

import java.io.Serializable;

public interface ConstUtils extends Serializable {
	int ERROR_CLEAR_TIME = 300;

	public void getResult(String result);

	public void getError(String error);

	public void getErrorCount(String errorCount);

	public void getToast(String toast);

	public void getForget();

	public void getother();

	public String LOCK = "LOCK";
	public String INIT = "INIT";

	public static final String FILECONTNAME = "org.zywx.wbpalmstar.plugin.uexlockpattern.count";
	public static final String KEYECONTNAME = "org.zywx.wbpalmstar.plugin.init.count";
	/**
	 * 加密密码
	 */
	public static final String FILLOCKNAME = "com.way.locus.LocusPassWordView";
	public static final String KEYELOCKNAME = "com.way.locus.password";

	/** * 密码输入错误次数 */
	public static final String ERRORNAME = "com.way.locus.loginErrorCount";

	public static final String SETORGETNAME = "com.way.locus.setPasswordData";

	public static final String SP_FILE_LOCK_VIEW_CONFIG = "com.way.locus.preferences";
	/** * 密码几行几列,行列相同 */
	public static final String SP_KEY_LOCK_VIEW_LINE = "com.way.locus.preferences";
	/** * 密码最小长度 */
	public static final String SP_KEY_PASSWORD_MIN_LEN = "com.way.locus.preferences.passwordMinLength";
	public static final String JK_PASSWORD_MIN_LEN = "passwordMinLenght";
	public static final int DEF_PASSWORD_MIN_LEN = 3;

}
