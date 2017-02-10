package org.zywx.wbpalmstar.plugin.uexlockpattern.vo;

import java.io.Serializable;

public class SetColorsVO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String OUT_CYCLE_NORMAL;
	private String CYCLE_ONTOUCH;
	private String LINE_COLOR;
	private String ERROR_COLOR;

	public String getOUT_CYCLE_NORMAL() {
		return OUT_CYCLE_NORMAL;
	}

	public String getCYCLE_ONTOUCH() {
		return CYCLE_ONTOUCH;
	}

	public String getLINE_COLOR() {
		return LINE_COLOR;
	}

	public String getERROR_COLOR() {
		return ERROR_COLOR;
	}

}
