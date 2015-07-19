package com.itheima.mobileguard.receivers;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.itheima.mobileguard.services.UpdateWidgetService;

/**
 * ����Ĺ㲥������
 * @author Administrator
 * 1.дһ���� �̳�AppWidgetProvider
 *
 */
public class MyWidget extends AppWidgetProvider {
	@Override
	public void onEnabled(Context context) {
		//�������� ���ڵĸ���widget
		Intent i = new Intent(context,UpdateWidgetService.class);
		context.startService(i);
		super.onEnabled(context);
	}
	
	@Override
	public void onDisabled(Context context) {
		//ֹͣ���� ����ȥ����widget
		Intent i = new Intent(context,UpdateWidgetService.class);
		context.stopService(i);
		super.onDisabled(context);
	}
}
