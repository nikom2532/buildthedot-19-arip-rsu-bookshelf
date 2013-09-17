package com.artifex.mupdfdemo;

import th.co.arip.rsubook.R;

public class OutlineActivityData {
	public OutlineItem items[];
	public int         position;
	static private OutlineActivityData singleton;

	static public void set(OutlineActivityData d) {
		singleton = d;
	}

	static public OutlineActivityData get() {
		if (singleton == null)
			singleton = new OutlineActivityData();
		return singleton;
	}
}
