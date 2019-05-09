package com.kilotrees.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZIPCompress {

	private ZipOutputStream zipOutputStream = null;
	private FileFilter fileFilter = null;
	
	public ZIPCompress(String fileName, FileFilter fileFilter) {
		try {
			zipOutputStream = new ZipOutputStream(new FileOutputStream(fileName));
		} catch (Exception e) {
			e.printStackTrace();
			close();
		}
		this.fileFilter = fileFilter;
	}

	public void close() {
		if (zipOutputStream != null) {
			try {
				zipOutputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void addFile(File file) throws Exception {
		zipFile(file, zipOutputStream, fileFilter);
	}

	private void zipFile(File file, ZipOutputStream outputStream, FileFilter filter) throws Exception {
		if (file.isDirectory()) {
			
			File[] subFileList = file.listFiles(filter);
			if (subFileList != null) {
				for (File subFile : subFileList) {
					zipFile(subFile, outputStream, filter);
				}
			}
			
		} else {

			// remove first character
			String path = file.getAbsolutePath();
			String toPath = path.substring(1);

			ZipEntry entry = new ZipEntry(toPath);
			outputStream.putNextEntry(entry);

			try {
				FileInputStream fileInputStream = new FileInputStream(file);
				int length = -1;
				byte[] buffer = new byte[10 * 1024];
				while ((length = fileInputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, length);
					outputStream.flush();
				}
				fileInputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			outputStream.closeEntry();
		}

	}
	
	public void zipRegularFile(String filePath, String toPath) throws Exception {
		File file = new File(filePath);
		ZipEntry entry = new ZipEntry(toPath);
		zipOutputStream.putNextEntry(entry);

		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			int length = -1;
			byte[] buffer = new byte[10 * 1024];
			while ((length = fileInputStream.read(buffer)) != -1) {
				zipOutputStream.write(buffer, 0, length);
				zipOutputStream.flush();
			}
			fileInputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		zipOutputStream.closeEntry();
	}
	
	public static void zipFiles(List<String> filePaths, String zipFilePath) {
		try {
			ZIPCompress iZipCompact = new ZIPCompress(zipFilePath, null);
			for (int i = 0; i < filePaths.size(); i++) {
				String path = filePaths.get(i);
				File file = new File(path);
				iZipCompact.addFile(file);
			}
			iZipCompact.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean unzipFile(File file, int uid, int gid) {

		try {

			ZipFile zipFile = new ZipFile(file);
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while (zipEntries.hasMoreElements()) {
				ZipEntry zipEntry = zipEntries.nextElement();

				// get file name
				String fileName = null;
				if (zipEntry.getName().startsWith("/")) {
					fileName = zipEntry.getName();
				} else {
					fileName = "/" + zipEntry.getName();
				}

				if (zipEntry.isDirectory()) {

					if (!new File(fileName).exists()) {
						// new File(fileName).mkdir();
						new File(fileName).mkdirs();
					}

				} else {

					// create directories
					int parentsDirsIndex = fileName.lastIndexOf("/");
					if (parentsDirsIndex > 0) {
						String parentsDirsPath = fileName.substring(0, parentsDirsIndex);
						new File(parentsDirsPath).mkdirs();
					}

					try {
						FileOutputStream outputStream = new FileOutputStream(fileName);
						InputStream inputStream = zipFile.getInputStream(zipEntry);
						int length = -1;
						byte[] buffer = new byte[10 * 1024];
						while ((length = inputStream.read(buffer)) != -1) {
							outputStream.write(buffer, 0, length);
							outputStream.flush();
						}
						outputStream.close();
						inputStream.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}

			zipFile.close();
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}
