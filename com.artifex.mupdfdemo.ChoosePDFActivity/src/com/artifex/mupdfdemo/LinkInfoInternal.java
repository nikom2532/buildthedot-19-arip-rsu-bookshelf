package com.artifex.mupdfdemo;

import th.co.arip.rsubook.R;

public class LinkInfoInternal extends LinkInfo {
	final public int pageNumber;

	public LinkInfoInternal(float l, float t, float r, float b, int p) {
		super(l, t, r, b);
		pageNumber = p;
	}

	public void acceptVisitor(LinkInfoVisitor visitor) {
		visitor.visitInternal(this);
	}
}
