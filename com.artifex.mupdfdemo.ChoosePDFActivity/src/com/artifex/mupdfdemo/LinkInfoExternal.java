package com.artifex.mupdfdemo;

import th.co.arip.rsubook.R;

public class LinkInfoExternal extends LinkInfo {
	final public String url;

	public LinkInfoExternal(float l, float t, float r, float b, String u) {
		super(l, t, r, b);
		url = u;
	}

	@Override
	public void acceptVisitor(LinkInfoVisitor visitor) {
		visitor.visitExternal(this);
	}
}
