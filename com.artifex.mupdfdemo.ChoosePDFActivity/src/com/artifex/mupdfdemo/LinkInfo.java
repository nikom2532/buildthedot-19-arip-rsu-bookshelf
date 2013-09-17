package com.artifex.mupdfdemo;

import android.graphics.RectF;
import th.co.arip.rsubook.R;

public class LinkInfo {
	final public RectF rect;

	public LinkInfo(float l, float t, float r, float b) {
		rect = new RectF(l, t, r, b);
	}

	public void acceptVisitor(LinkInfoVisitor visitor) {
	}
}
