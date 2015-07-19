package com.itheima.mobileguard.services;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.itheima.mobileguard.activities.EnterPwdActivity;
import com.itheima.mobileguard.db.dao.ApplockDao;

public class WatchDogService extends Service {
	private boolean flag = false;
	private ActivityManager am;
	private WatchDogReceiver receiver;
	private ApplockDao dao;
	/**
	 * ��ʱֹͣ������Ӧ�ó������
	 */
	private String tempStopProtectPackname;
	
	private List<RunningTaskInfo> taskInfos;
	
	private RunningTaskInfo taskInfo;
	
	private String packname ;
	
	/**
	 * �������İ����ļ���
	 */
	private  List<String> lockedPacknames;

	private Intent intent;
	
	private ApplockObserver observer;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private class WatchDogReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if ("com.itheima.mobileguard.stopprotect"
					.equals(intent.getAction())) {
				tempStopProtectPackname = intent.getStringExtra("packname");
			} else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
				tempStopProtectPackname = null;
				//ֹͣ���Ź�
				flag = false;
			}else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
				//�������Ź�
				if(flag==false){
					startWatchDog();
				}
			}
		}
	}

	@Override
	public void onCreate() {
		observer = new ApplockObserver(new Handler());
		getContentResolver().registerContentObserver(Uri.parse("content://com.itheima.mobileguard.applock"), 
				true, observer);
		
		dao = new ApplockDao(this); 
		//ֻ���ڷ����һ�δ�����ʱ�� �Ż��ȡ���ݡ� �����ڷ����������У�������ĳ�������������Ϣ ��Ч�ˡ�
		lockedPacknames = dao.findAll();
		intent = new Intent(WatchDogService.this,
				EnterPwdActivity.class);
		// ע��һ���Զ���Ĺ㲥������
		receiver = new WatchDogReceiver();
		IntentFilter filter = new IntentFilter(
				"com.itheima.mobileguard.stopprotect");
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(receiver, filter);
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		startWatchDog();
		super.onCreate();
	}


	private void startWatchDog() {
		new Thread() {
			public void run() {
				flag = true;
				while (flag) {
					// ��������ջ������� ���ʹ�õĴ򿪵�����ջ�ڼ��ϵ���ǰ��
					taskInfos = am.getRunningTasks(1);
					// ���ʹ�õ�����ջ
					taskInfo = taskInfos.get(0);
					packname = taskInfo.topActivity.getPackageName();
					System.out.println(packname);
					// �ж���������Ƿ���Ҫ��������
					//if (dao.find(packname)) {//��ѯ���ݿ� Ч�ʵ� ���ڴ濪����
					if(lockedPacknames.contains(packname)){//��ѯ�ڴ漯�� �ٶȿ� �ڴ濪��С
						// �жϵ�ǰӦ�ó����Ƿ���Ҫ��ʱֹͣ��������������ȷ�����룩
						if (packname.equals(tempStopProtectPackname)) {

						} else {
							// ��Ҫ����
							// ����һ����������Ľ��档
							intent.putExtra("packname", packname);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						}
					} else {
						// ����Ҫ����
					}
					try {
						Thread.sleep(30);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
	}

	private class ApplockObserver extends ContentObserver{

		public ApplockObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			Log.i("ApplockObserver","���ݿ�����ݱ仯�ˣ����»�ȡ �������İ���");
			//���»�ȡ �������İ���
			lockedPacknames = dao.findAll();
		}
		
	}
	
	
	@Override
	public void onDestroy() {
		flag = false;
		unregisterReceiver(receiver);
		receiver = null;
		getContentResolver().unregisterContentObserver(observer);
		observer = null;
		super.onDestroy();
	}

}
