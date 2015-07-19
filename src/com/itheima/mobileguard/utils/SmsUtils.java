package com.itheima.mobileguard.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Xml;

/**
 * ���ŵĹ����� �ṩ���ű��ݺͻ�ԭ��API
 * 
 * @author Administrator
 * 
 */
public class SmsUtils {
	//1.����һ���ӿڡ�
	//2.�ӿ������ṩһЩ�ص�����
	//3.���÷�����ʱ�� ���ݽӿڶ���
	//4.�ڿ�ʼ�ͱ��ݹ����е��ýӿ�����ķ�����
	
	
	/**
	 * �����һ���ӿڣ������ص���
	 * @author Administrator
	 *
	 */
	public interface BackupStatusCallback{
		/**
		 * �ڶ��ű���֮ǰ���õķ���
		 * @param size �ܵĶ��ŵĸ���
		 */
		public void beforeSmsBackup(int size);
		
		/**
		 * ��sms���ű��ݹ����е��õķ���
		 * @param process ��ǰ�Ľ���
		 */
		public void onSmsBackup(int process);
	}
	/**
	 * ���ݶ���
	 * 
	 * @param context
	 *            ������
	 * @param callback �ӿ�
	 * @return
	 * @throws FileNotFoundException
	 */
	public static boolean backUpSms(Context context,BackupStatusCallback callback)
			throws FileNotFoundException, IllegalStateException, IOException {
		XmlSerializer serializer = Xml.newSerializer();
		File sdDir = Environment.getExternalStorageDirectory();
		long freesize = sdDir.getFreeSpace();
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)
				&& freesize > 1024l * 1024l) {
			File file = new File(Environment.getExternalStorageDirectory(),
					"backup.xml");
			FileOutputStream os = new FileOutputStream(file);
			// ��ʼ��xml�ļ������л���
			serializer.setOutput(os, "utf-8");
			// дxml�ļ���ͷ
			serializer.startDocument("utf-8", true);
			// д���ڵ�
			ContentResolver resolver = context.getContentResolver();
			Uri uri = Uri.parse("content://sms/");
			Cursor cursor = resolver.query(uri, new String[] { "address",
					"body", "type", "date" }, null, null, null);
			// �õ��ܵ���Ŀ�ĸ���
			int size = cursor.getCount();
			//���ý��ȵ��ܴ�С
//			pb.setMax(size);
//			pd.setMax(size);
			callback.beforeSmsBackup(size);
			serializer.startTag(null, "smss");
			serializer.attribute(null, "size", String.valueOf(size));
			int process  = 0;
			while (cursor.moveToNext()) {
				serializer.startTag(null, "sms");
				serializer.startTag(null, "body");
				//:-)
				//���ܻ�������������Ҫ���������������ᵼ�±���ʦ��
				try {
					String bodyencpyt = Crypto.encrypt("123", cursor.getString(1));
					serializer.text(bodyencpyt);
				} catch (Exception e1) {
					e1.printStackTrace();
					serializer.text("���Ŷ�ȡʧ��");
				}
				serializer.endTag(null, "body");
				serializer.startTag(null, "address");
				serializer.text(cursor.getString(0));
				serializer.endTag(null, "address");
				serializer.startTag(null, "type");
				serializer.text(cursor.getString(2));
				serializer.endTag(null, "type");
				serializer.startTag(null, "date");
				serializer.text(cursor.getString(3));
				serializer.endTag(null, "date");
				serializer.endTag(null, "sms");
				try {
					Thread.sleep(600);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//���ý������Ի���Ľ���
				process++;
//				pb.setProgress(process);
//				pd.setProgress(process);
				callback.onSmsBackup(process);
			}
			cursor.close();
			serializer.endTag(null, "smss");
			serializer.endDocument();
			os.flush();
			os.close();
			return true;
		} else {
			throw new IllegalStateException("sd�������ڻ��߿ռ䲻��");
		}
	}
	
	
	public interface RestoreSmsCallBack{
		public void beforeSmsRestore(int size);
		public void onSmsRestore(int progress);
	}
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public static boolean restoreSms(Context context,RestoreSmsCallBack callback){
		//�ж� �Ƿ񱸷��ļ����� ��ȡsd���� �ļ�
		//����xml�ļ���
		//1. ����pull������
		//2.��ʼ��pull�����������ñ��� inputstream
		//3.����xml�ļ� while(�ĵ�ĩβ��
		//{
			//��ȡ���� size �ܸ�����. ���ýӿڵķ��� beforeSmsRestore
			//ÿ��ȡ��һ������ �Ͱ�������� body�����ܣ� address date type��ȡ����
			//���������ṩ��  resolver.insert(Uri.parse("content://sms/"),contentValue);
			//ÿ��ԭ�� count++ ����onSmsRestore(count);
		//}
		return false;
	}
}
