package com.artifex.mupdfdemo;

import th.co.arip.rsubook.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

public class GridPreviewActivity extends Activity {
	
	GridView gridPreview;
	PDFSlideAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mupdf_grid_layout);
		
		gridPreview = (GridView) findViewById(R.id.grid_preview);
		adapter = new PDFSlideAdapter(this, MuPDFCoreHelper.core);
		gridPreview.setAdapter(adapter);
		gridPreview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				// TODO Auto-generated method stub
				setResult(position);
				MuPDFCoreHelper.core = null;
				finish();
			}
		});
	}
	
}
