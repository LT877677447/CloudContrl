package com.kilotrees.dao.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.util.FileUtil;
import com.kilotrees.util.StringUtil;
import com.kilotrees.util.ZIPCompress;

public class SQLiteManager {

	private static final String JDBC_Class_Name = "org.sqlite.JDBC";

	private static final String SQLITE_DB_PATH = "D:/WebServerPrivate/phone_files/other/";

	private static String Contacts_DB_Template_Path = SQLITE_DB_PATH + "contacts2_template.db";
	private static String Contacts_DB_Path = SQLITE_DB_PATH + "data/data/com.android.providers.contacts/databases/" + "contacts2.db";
	private static String Contacts_DB_ZIP_Path = SQLITE_DB_PATH + "contacts2.zip";

	private static String SMS_DB_Template_Path = SQLITE_DB_PATH + "mmssms_template.db";
	private static String SMS_DB_Path = SQLITE_DB_PATH + "data/data/com.android.providers.telephony/databases/" + "mmssms.db";
	private static String SMS_DB_ZIP_Path = SQLITE_DB_PATH + "mmssms.zip";

	static {
		try {
			Class.forName(JDBC_Class_Name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Connection createConnection(String sqliteDBFilePath) {
		try {
			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteDBFilePath);
			return connection;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void fakedPhoneContacts() {
		try {
			// 1. copy the template
			File templateFile = new File(Contacts_DB_Template_Path);
			File dbFile = new File(Contacts_DB_Path);
			if (!dbFile.getParentFile().exists()) {
				dbFile.getParentFile().mkdirs();
			}
			FileUtil.copy(templateFile, dbFile);

			// 2. select the result
			Connection connection = createConnection(Contacts_DB_Path);
			Statement statementQuery = connection.createStatement();
			Statement statementUpdate = connection.createStatement();

			String tableName = "raw_contacts";

			ResultSet rs = statementQuery.executeQuery("select _id, account_id, sourceid, display_name from " + tableName);
			
			Map<String, String> recordsMap = new HashMap<String, String>();
			while (rs.next()) {
				int _id = rs.getInt("_id");
				String sourceid = rs.getString("sourceid"); // phone number
				if (sourceid == null) {
					continue;
				}
				String randomNumbers = StringUtil.randomNumber(8);
				String fakedPhone = sourceid.substring(0, 3) + randomNumbers;

				recordsMap.put("" + _id, fakedPhone);
			}
			
			// 3. update the .db file
			Iterator<String> iterator = recordsMap.keySet().iterator();
			while (iterator.hasNext()) {
				String idKey = iterator.next();
				String fakedPhone = recordsMap.get(idKey);
				statementUpdate.executeUpdate("update " + tableName + " set sourceid=" + fakedPhone + " where _id=" + idKey);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void fakedPhoneSMS() {
		try {
			File templateFile = new File(SMS_DB_Template_Path);
			File dbFile = new File(SMS_DB_Path);
			if (!dbFile.getParentFile().exists()) {
				dbFile.getParentFile().mkdirs();
			}
			FileUtil.copy(templateFile, dbFile);

			Connection connection = createConnection(SMS_DB_Path);
			Statement statementQuery = connection.createStatement();
			Statement statementUpdate = connection.createStatement();

			String tableName = "sms";

			ResultSet rs = statementQuery.executeQuery("select _id, thread_id, address, date, date_sent, body from " + tableName);

			while (rs.next()) {
				int _id = rs.getInt("_id");
				String body = rs.getString("body"); // sms contents

				String randomString = StringUtil.randomString(2);
				String fakedBody = body + randomString;

				statementUpdate.executeUpdate("update " + tableName + " set body=" + fakedBody + " where _id=" + _id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Temporary Methods Here Now
	 */
	public static void zipFakedPhoneContacts(JSONArray prefix_actions) {
		try {
			fakedPhoneContacts();
			ZIPCompress iZipCompact = new ZIPCompress(Contacts_DB_ZIP_Path, null);
			iZipCompact.zipRegularFile(Contacts_DB_Path, Contacts_DB_Path.replace(SQLITE_DB_PATH, ""));
			iZipCompact.close();
			
			JSONObject jsonUNZip = new JSONObject();
			jsonUNZip.put("action", "UNZIP_FILE");
			jsonUNZip.put("zip_file_name", "contacts2.zip");
			jsonUNZip.put("zip_download_url", ServerConfig.getStoragePrivateBaseURL() + "/phone_files/other/contacts2.zip");
			prefix_actions.put(jsonUNZip);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void zipFakedSMS(JSONArray prefix_actions) {
		try {
			fakedPhoneSMS();
			ZIPCompress iZipCompact = new ZIPCompress(SMS_DB_ZIP_Path, null);
			iZipCompact.zipRegularFile(SMS_DB_Path, SMS_DB_Path.replace(SQLITE_DB_PATH, ""));
			iZipCompact.close();
			
			JSONObject jsonUNZip = new JSONObject();
			jsonUNZip.put("action", "UNZIP_FILE");
			jsonUNZip.put("zip_file_name", "mmssms.zip");
			jsonUNZip.put("zip_download_url", ServerConfig.getStoragePrivateBaseURL() + "/phone_files/other/mmssms.zip");
			prefix_actions.put(jsonUNZip);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
