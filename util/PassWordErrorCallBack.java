package org.zywx.wbpalmstar.plugin.uexlockpattern.util;

public interface PassWordErrorCallBack {
    /**
     * 密码错误回调
     * 
     * @param count
     *            密码错误次数
     */
	public void passWordError(int count);

    /**
     * 密码错误回调
     * 
     * @param errMsg
     *            错误信息
     */
    public void passWordTooShort(String errMsg);
}
