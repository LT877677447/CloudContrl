package com.kilotrees.util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;


public class GZIPUtil  {
    public static final String GZIP_ENCODE_UTF_8 = "UTF-8"; 
    public static final String GZIP_ENCODE_ISO_8859_1 = "ISO-8859-1";

    public static byte[] compress(String str) {  
        return compress(str, GZIP_ENCODE_UTF_8);  
    }
    
    public static byte[] compress(String str, String encoding) {
		try {
			byte[] bytes = str.getBytes(encoding);
			return compress(bytes); 
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
    }
    
    public static byte[] compress(byte[] bytes) {
    	GZIPOutputStream gzip = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(bytes);
            gzip.flush();
            gzip.close();
            gzip = null;
        } catch ( Exception e) {
            e.printStackTrace();
        } finally {
			if (gzip != null) {
				try {
					gzip.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
        return out.toByteArray();
    }
    
    
    
    public static String uncompressToString(byte[] bytes) {  
    	return uncompressToString(bytes, GZIP_ENCODE_UTF_8);  
    } 
    
    public static String uncompressToString(byte[] bytes, String encoding) {  
        if (bytes == null || bytes.length == 0) {  
            return null;  
        } 
        byte[] outBytes = uncompress(bytes);
        try {
			return new String(outBytes, encoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        return null;
    }
    
    public static byte[] uncompress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
    
    @Test
    public void testCompress() {
    	try {
    		FileInputStream inF = new FileInputStream(new File("D:\\WebServer\\log4j\\zfyuncontrol/error.log2019-01-11-Error.log"));
    		FileOutputStream outF = new FileOutputStream(new File("D:\\WebServer\\log4j\\zfyuncontrol/error.log2019-01-11-Error-test_compress.log"));
    		ByteArrayOutputStream bArray = new ByteArrayOutputStream();
    		byte[] bs = new byte[512];
    		int n = 0;
    		while( (n = inF.read(bs)) != -1)   {
    			bArray.write(bs, 0, n);
    		}
    		inF.close();//11423
    		outF.write(compress(bArray.toByteArray()));
    		outF.close();
    		
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    
    @Test
    public void testUnCompress() {
    	try {
    		FileInputStream inF = new FileInputStream(new File("D:\\WebServer\\log4j\\zfyuncontrol/error.log2019-01-11-Error-test_compress.log"));
    		FileOutputStream outF = new FileOutputStream(new File("D:\\WebServer\\log4j\\zfyuncontrol/error.log2019-01-11-Error-test_uncompress2.log"));
    		ByteArrayOutputStream outB = new ByteArrayOutputStream();
    		byte[] bs = new byte[512];
    		int n=0;
    		while( (n = inF.read(bs)) != -1 ) {
    			outB.write(bs,0,n);
    		}
    		outF.write(uncompress(outB.toByteArray()));
    		inF.close();
    		outF.close();
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
}

