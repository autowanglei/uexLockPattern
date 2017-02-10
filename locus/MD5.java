package org.zywx.wbpalmstar.plugin.uexlockpattern.locus;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
	/**
	 * 添加验证头
	 * 
	 * @param curWData
	 *            当前widgetData
	 * @param timeStamp
	 *            当前时间戳
	 * @createAt 2014.04
	 * @author yipeng.zhang
	 * @return
	 */
	// public static String getAppVerifyValue(WWidgetData curWData, long
	// timeStamp) {
	// String value = null;
	// String md5 = getMD5Code(curWData.m_appId + ":" + curWData.m_appkey
	// + ":" + timeStamp);
	// value = "md5=" + md5 + ";ts=" + timeStamp;
	// return value;
	//
	// }

	public static String getMD5Code(String value) {
		if (value == null) {
			value = "";
		}
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.reset();
			md.update(value.getBytes());
			byte[] md5Bytes = md.digest();
			StringBuffer hexValue = new StringBuffer();
			for (int i = 0; i < md5Bytes.length; i++) {
				int val = ((int) md5Bytes[i]) & 0xff;
				if (val < 16)
					hexValue.append("0");
				hexValue.append(Integer.toHexString(val));
			}
			return hexValue.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}
}
