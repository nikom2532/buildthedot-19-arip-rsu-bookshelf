package com.artifex.mupdfdemo;

import th.co.arip.rsubook.R;

abstract public class LinkInfoVisitor {
	public abstract void visitInternal(LinkInfoInternal li);
	public abstract void visitExternal(LinkInfoExternal li);
	public abstract void visitRemote(LinkInfoRemote li);
}
