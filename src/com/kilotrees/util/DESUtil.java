package com.kilotrees.util;



import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DESUtil {

    public static void main_(String args[]) {
    	
    	String aa  = "Zbe7lrm4Sc3fNs3dy8gz6UivK5PBpYyzPT5yizZ63xCRBQJNsJfJ7u5cyYQ38oxme2m44sTkEZJm\r\n" + 
    			"32+37GNY/4Ve2nFfUlVnE11aFRjRCPR1lrjcfve2/w7Bot4YNB87bcsmZo5BXiDAbooJ/eCt4cL6\r\n" + 
    			"HB53IVhh/oEZU0T8SezOKoavHGY+0zTQ3f4nYh6019W+zRSK82WM2wZbe6COqo+1+e+rFqoPbhBO\r\n" + 
    			"UV6MIEtCihe9wyaBW9oy8X5su/5jzLvPLY6yjWSl+uXGER5QJP/K7W//5Byxwc5yTewxPY0NX8XH\r\n" + 
    			"XjXRDIuTM/qDkMbPksTAnjL4irRsX4AitkwUJf70jkJ+F+6hEjh5615vZuomdmUsi++a4KjAJIx3\r\n" + 
    			"AH9njr+LsFdZnRrfzQHWLDGGL9Luu/IRUiOTot4pqQuZBcyT+tvqfSJarFKHPTvy8AVaZMce5Zbo\r\n" + 
    			"w6Go7//RAW2KmyoB8vBFddgK3d37vce2ptNoioLgtJOPiRFhi2O5li69ILxDhr/D1EKazFoAc6t2\r\n" + 
    			"DoIbNTbm79V6KgRkb6PBPYmZRj5INfHKGM2eSTe6V+8qJTfmIMjx78dCyh4eKvuKGD7J32YnaTIu\r\n" + 
    			"70Nd86nwO/Z8gLQ4jmNazZwAeX97CGEAi+JWrfdkhTIJpZJaryWFfago/SVcBRYeGL157JFCXFPR\r\n" + 
    			"jbZkIcSkp8jyYIupVg5YpMB6Sj8E/pboPFbVWvFJuZtrURU8ERLzQW2684QgT4HRv1V5mC7NINN0\r\n" + 
    			"HSPEB5VK4IP1MSNFnuZK8TFVBd2hjOxNNab14wN+jX9YsovBb/pp6Z+l8v9F4f5ZAvuPmION67/E\r\n" + 
    			"lZP556cIO2ky89cmm5hdXJZXbcb2jDKeu1bd7L8tcPePAHMLTbNEyE25iowtIvAvNWx8TnYj+te6\r\n" + 
    			"SpAXbb3g1VOesAXLZeBe7jRb7Z9t7e+iPYbrOI6hHbYNCpX6zTF78ALiApbV8T8AMRl/zX7m74Ma\r\n" + 
    			"Jknzken4bjrNH9fq6YlabKsamb4LRN9LMLUGQGchhAi9yM3jIK1RNTE+CW1beH0JHSDUm5p1rwb6\r\n" + 
    			"flGweBq4Y9Pr1C9wynO9gjBlnwiPilkYy8X+Q7uHsZX7YqKYdVbHI9m2ls5vDb+vx1yBEw5WAs9M\r\n" + 
    			"LJCdwQ2faS5LVFx0QU3NE/mQsdf93VmuQEZIRO6kIcewdsaLr0OkvhMvONkMjPOiT0PYXecZn1co\r\n" + 
    			"trsWIRm4feg6vo5FDBmCSIhgN+Xg1mQlH0sQF11pf1mf71BYzkABOZrsm7D+IVA+mfnLBkfhdnfs\r\n" + 
    			"r1qaMesf4ixtxx0C4MyEXW7fuCOWMEhDeaNB7M7pheV9hTfdqTnFvmu0rdFIlH+fjnuPsVZwnl+I\r\n" + 
    			"Q/ppR1QXiHabxeDcDUrYgQAdtKO5eD5s4FE3s/jU" + 
    			"";
        String password = "958888888888028";
//        String str = "task_id=TSK_888888888888870&ledger_id=0715-5802";
        String str = "9+gCumR+0EbMG+UCYjUQDIY+N2yW7sjQ5OQq6xqW5G0WwlIDiYTq+ectzYnIthf0VOF8IIPDOKqz\r\n" + 
        		"Arzxn7Yf67fDpdEofDsX2sLKbQ73PqGlhvT6Ip2Hg8b+LfkC4jj1OCdq7NiHkw3YGTg890kY85cv\r\n" + 
        		"IGZydWIctPg3TIzyNjQy8bLdAZ/dMJVnbIB2ZouD0Lg14SRmMIWWDIZKAuv4DruTQBds+pzlTfJP\r\n" + 
        		"AvZhVJhcQxgF1vfRdww1z3FvTdFYzC67O26pzbs7Dj76an5wUgcmZmIKfmdpg0HsCDgfoVqjypH9\r\n" + 
        		"9+ipOa8YRmznoRpM4XWQTLp2QzaL4Z7cvewkZfCAAGz/ZPRtWcRDCErVcN+W5/7Fs0qpONm0u9cm\r\n" + 
        		"7Su0jN8briCg4bweT6E+Q0dX59D7X1qYMdRoQgTr21OxbNxDunRK315C+QLvnIneK+MwUmEUGZm7\r\n" + 
        		"Zqu0S24of8XLMCNHEz67R2llgl1Qjah7E1m+qZ1ATCBHoCCHUXVMA69A4cn2UhfMfZRVN6jpqaDw\r\n" + 
        		"ORWjv7FR6FbLxL4s4kNSQWuQPyTRalNT1P2rmYB8pAhcZttR/H+YZKiIP0n5VWcbRr39mHSIl1D9\r\n" + 
        		"uVdhrvVb8EI9JWa0tMnzDWTkbVX5kF0m1PHbhK7z2M6jkLJ2cC7hE8jMdCDz5TZZrYYK2EgXlRa7\r\n" + 
        		"zXnaKhW7xxZDD6MpcRt2mRi90ph6ygi0etuXVqfhIs/SJnrBNOGtjGgnkoiMd8n2aAlgxFxVCLyV\r\n" + 
        		"W58T/zA1etslrERGCOf606fLp25mGgGEE/zMHTOpOWZM5YvrmK/YfyJ4eCS33LMaXLMtvQEoJ9DS\r\n" + 
        		"SDespKEG4x1t5nx2NIja93Qq7O4Ep++pV89uyh8gTBcG7YAtZsWeRATO+h+nNm0mTN+UIS9o54Aj\r\n" + 
        		"nKCmgVmeU43EjyrQ4tCFKZQBghkGc1hWC89BLf7t8HXo6p2Ps53QVYycXha/Q7JJMI0DXI89laYg\r\n" + 
        		"jS+zeQqXGckDMLZmFcSo0/0ddtYGDwdwHfkp/yY/OZEvfsfcqAaUGwVPxnTOxNOrCN+iODGmIMId\r\n" + 
        		"Fl0JOdKZqGSmeqqV5x5QyKBweQy8Nd9p89n+xFzxQBlVQjUXWfuzJYATdsM3BdnUCP9XTEYXcQBJ\r\n" + 
        		"p7l4Eny+tKk0jDD2oyDcq2iJYSu4zxwCHFySROcgtgcnrDFuiMNVA36OTDBIgSh+hdudWFJu23Hl\r\n" + 
        		"JgfWD8jhFUT5t6odGXX64Y5/XIpurEVaNGp6HPe/r3UZKxQzzA0jpTfLOqm6SxeObteQjHi1tTER\r\n" + 
        		"CZI5DxgeUkXRQrmvWLBMakilzeEQNJ30Qsu/gn/qCQ8yE4YWcdFXHOegXE2a8ADjuitrafrHxFLh\r\n" + 
        		"vCR5TiATen9H8Fn3enUwWWdVqJ1Zqg8GiSXxLG3M\r\n" + 
        		"";
        String result = DESUtil.encryptString(str, password);
        System.out.println("before：" + result);
        String decryResult = DESUtil.decryptString(result, password);
        System.out.println("after：" + new String(decryResult));
    }

    // to base64 string
    public static String encryptString(String data, String password_16_bytes) {
        return encryptString(data, password_16_bytes.getBytes());
    }

    public static String encryptString(String data, byte[] password_16_bytes) {
        byte[] bt = encrypt(data.getBytes(), password_16_bytes);
        if (bt != null) {
            byte[] encode = Base64.getEncoder().encode(bt);
            String s = new String(encode);
            return s;
        }
        return null;
    }

    // from base64 string
    public static String decryptString(String data, String password_16_bytes) {
        return decryptString(data, password_16_bytes.getBytes());
    }

    public static String decryptString(String data, byte[] password_16_bytes) {
        byte[] decode = Base64.getDecoder().decode(data);
        byte[] bt = decrypt(decode, password_16_bytes);
        if (bt != null) {
            String s = new String(bt);
            return s;
        }
        return null;
    }

    public static byte[] encrypt(byte[] bytes, String key) {
        return encrypt(bytes, key.getBytes());
    }

    public static byte[] encrypt(byte[] bytes, byte[] bytesKey) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(bytesKey);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKey);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, random);
            return cipher.doFinal(bytes);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] bytes, String key) {
        return decrypt(bytes, key.getBytes());
    }

    public static byte[] decrypt(byte[] bytes, byte[] bytesKey) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(bytesKey);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKey);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, random);
            return cipher.doFinal(bytes);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static void main(String[] args) {
		
//    	byte[] deskey = new byte[] {127,17,35,65,82,13,61,35,27,2,63,99,123,29,96,73};
    	String miwen = "9+gCumR+0EbMG+UCYjUQDIY+N2yW7sjQ5OQq6xqW5G0WwlIDiYTq+ectzYnIthf0VOF8IIPDOKqz" + 
    			"Arzxn7Yf67fDpdEofDsX2sLKbQ73PqGlhvT6Ip2Hg8b+LfkC4jj1OCdq7NiHkw3YGTg890kY85cv" + 
    			"IGZydWIctPg3TIzyNjQy8bLdAZ/dMJVnbIB2ZouD0Lg14SRmMIWWDIZKAuv4DruTQBds+pzlTfJP" + 
    			"AvZhVJhcQxgF1vfRdww1z3FvTdFYzC67O26pzbs7Dj76an5wUgcmZmIKfmdpg0HsCDgfoVqjypH9" + 
    			"9+ipOa8YRmznoRpM4XWQTLp2QzaL4Z7cvewkZfCAAGz/ZPRtWcRDCErVcN+W5/7Fs0qpONm0u9cm" + 
    			"7Su0jN8briCg4bweT6E+Q0dX59D7X1qYMdRoQgTr21OxbNxDunRK315C+QLvnIneK+MwUmEUGZm7" + 
    			"Zqu0S24of8XLMCNHEz67R2llgl1Qjah7E1m+qZ1ATCBHoCCHUXVMA69A4cn2UhfMfZRVN6jpqaDw" + 
    			"ORWjv7FR6FbLxL4s4kNSQWuQPyTRalNT1P2rmYB8pAhcZttR/H+YZKiIP0n5VWcbRr39mHSIl1D9" + 
    			"uVdhrvVb8EI9JWa0tMnzDWTkbVX5kF0m1PHbhK7z2M6jkLJ2cC7hE8jMdCDz5TZZrYYK2EgXlRa7" + 
    			"zXnaKhW7xxZDD6MpcRt2mRi90ph6ygi0etuXVqfhIs/SJnrBNOGtjGgnkoiMd8n2aAlgxFxVCLyV" + 
    			"W58T/zA1etslrERGCOf606fLp25mGgGEE/zMHTOpOWZM5YvrmK/YfyJ4eCS33LMaXLMtvQEoJ9DS" + 
    			"SDespKEG4x1t5nx2NIja93Qq7O4Ep++pV89uyh8gTBcG7YAtZsWeRATO+h+nNm0mTN+UIS9o54Aj" + 
    			"nKCmgVmeU43EjyrQ4tCFKZQBghkGc1hWC89BLf7t8HXo6p2Ps53QVYycXha/Q7JJMI0DXI89laYg" + 
    			"jS+zeQqXGckDMLZmFcSo0/0ddtYGDwdwHfkp/yY/OZEvfsfcqAaUGwVPxnTOxNOrCN+iODGmIMId" + 
    			"Fl0JOdKZqGSmeqqV5x5QyKBweQy8Nd9p89n+xFzxQBlVQjUXWfuzJYATdsM3BdnUCP9XTEYXcQBJ" + 
    			"p7l4Eny+tKk0jDD2oyDcq2iJYSu4zxwCHFySROcgtgcnrDFuiMNVA36OTDBIgSh+hdudWFJu23Hl" + 
    			"JgfWD8jhFUT5t6odGXX64Y5/XIpurEVaNGp6HPe/r3UZKxQzzA0jpTfLOqm6SxeObteQjHi1tTER" + 
    			"CZI5DxgeUkXRQrmvWLBMakilzeEQNJ30Qsu/gn/qCQ8yE4YWcdFXHOegXE2a8ADjuitrafrHxFLh" + 
    			"vCR5TiATen9H8Fn3enUwWWdVqJ1Zqg8GiSXxLG3M";
//    	String miwen = "9+gCumR+0EbMG+UCYjUQDIY+N2yW7sjQ5OQq6xqW5G0WwlIDiYTq+ectzYnIthf0VOF8IIPDOKqz\r\n" + 
//    			"Arzxn7Yf67fDpdEofDsX2sLKbQ73PqGlhvT6Ip2Hg8b+LfkC4jj1OCdq7NiHkw3YGTg890kY85cv\r\n" + 
//    			"IGZydWIctPg3TIzyNjQy8bLdAZ/dMJVnbIB2ZouD0Lg14SRmMIWWDIZKAuv4DruTQBds+pzlTfJP\r\n" + 
//    			"AvZhVJhcQxgF1vfRdww1z3FvTdFYzC67O26pzbs7Dj76an5wUgcmZmIKfmdpg0HsCDgfoVqjypH9\r\n" + 
//    			"9+ipOa8YRmznoRpM4XWQTLp2QzaL4Z7cvewkZfCAAGz/ZPRtWcRDCErVcN+W5/7Fs0qpONm0u9cm\r\n" + 
//    			"7Su0jN8briCg4bweT6E+Q0dX59D7X1qYMdRoQgTr21OxbNxDunRK315C+QLvnIneK+MwUmEUGZm7\r\n" + 
//    			"Zqu0S24of8XLMCNHEz67R2llgl1Qjah7E1m+qZ1ATCBHoCCHUXVMA69A4cn2UhfMfZRVN6jpqaDw\r\n" + 
//    			"ORWjv7FR6FbLxL4s4kNSQWuQPyTRalNT1P2rmYB8pAhcZttR/H+YZKiIP0n5VWcbRr39mHSIl1D9\r\n" + 
//    			"uVdhrvVb8EI9JWa0tMnzDWTkbVX5kF0m1PHbhK7z2M6jkLJ2cC7hE8jMdCDz5TZZrYYK2EgXlRa7\r\n" + 
//    			"zXnaKhW7xxZDD6MpcRt2mRi90ph6ygi0etuXVqfhIs/SJnrBNOGtjGgnkoiMd8n2aAlgxFxVCLyV\r\n" + 
//    			"W58T/zA1etslrERGCOf606fLp25mGgGEE/zMHTOpOWZM5YvrmK/YfyJ4eCS33LMaXLMtvQEoJ9DS\r\n" + 
//    			"SDespKEG4x1t5nx2NIja93Qq7O4Ep++pV89uyh8gTBcG7YAtZsWeRATO+h+nNm0mTN+UIS9o54Aj\r\n" + 
//    			"nKCmgVmeU43EjyrQ4tCFKZQBghkGc1hWC89BLf7t8HXo6p2Ps53QVYycXha/Q7JJMI0DXI89laYg\r\n" + 
//    			"jS+zeQqXGckDMLZmFcSo0/0ddtYGDwdwHfkp/yY/OZEvfsfcqAaUGwVPxnTOxNOrCN+iODGmIMId\r\n" + 
//    			"Fl0JOdKZqGSmeqqV5x5QyKBweQy8Nd9p89n+xFzxQBlVQjUXWfuzJYATdsM3BdnUCP9XTEYXcQBJ\r\n" + 
//    			"p7l4Eny+tKk0jDD2oyDcq2iJYSu4zxwCHFySROcgtgcnrDFuiMNVA36OTDBIgSh+hdudWFJu23Hl\r\n" + 
//    			"JgfWD8jhFUT5t6odGXX64Y5/XIpurEVaNGp6HPe/r3UZKxQzzA0jpTfLOqm6SxeObteQjHi1tTER\r\n" + 
//    			"CZI5DxgeUkXRQrmvWLBMakilzeEQNJ30Qsu/gn/qCQ8yE4YWcdFXHOegXE2a8ADjuitrafrHxFLh\r\n" + 
//    			"vCR5TiATen9H8Fn3enUwWWdVqJ1Zqg8GiSXxLG3M\r\n";
//    	String s = decryptString(miwen,deskey);
//    	System.out.println(s);
    	
    	
    	byte[] aaaaa = Base64.getDecoder().decode(miwen.getBytes());
        String aaaa = new String(aaaaa);
    	
    	   String abc = "abc";
           byte[] abcBytes = Base64.getEncoder().encode(abc.getBytes());
           String adbString = new String(abcBytes);
           byte[] efgBytes = Base64.getDecoder().decode(adbString.getBytes());
           String efgStrig = new String(efgBytes);
           
           
           String uuuu = "YWJj";
           byte[] efgBytes_ = Base64.getDecoder().decode(adbString.getBytes());
           String efgStrig_ = new String(efgBytes);
           
           System.out.println("");
    	
    }
    
    
}