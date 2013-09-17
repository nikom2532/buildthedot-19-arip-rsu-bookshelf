package com.artifex.mupdfdemo;

import th.co.arip.rsubook.R;

public class ChoosePDFItem {
	enum Type {
		PARENT, DIR, DOC
	}

	final public Type type;
	final public String name;

	public ChoosePDFItem (Type t, String n) {
		type = t;
		name = n;
	}
}
