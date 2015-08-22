package com.example.securemobileidentity;

import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.http.ParseException;

import android.content.SharedPreferences;
import android.util.Log;


public class EncryptionManager {
	
	/** The password for the AES encrypt and decrypt, so the device doesn't
	 *  store the private key in plaintext on disk. The app should prompt for it onCreate
	 */
	private String password;	
	public static SharedPreferences s;
	private BigInteger privateKey;
	private int keySize = 2048;
	
		
	public EncryptionManager(SharedPreferences s, String password) throws ParseException, IOException{
		this.s = s;
		this.password = password;
		//publicKey = null;
		//Log.i("encryption", s.getString("private_key", "null"));
		/** Generate a keypair if the SharedPreferences does not yet contain one */
		if (s.getString(Constants.pref_private_key, null) == null)
		{
			Log.i("encryption", "generating a key pair: should only happen once!!");
			generateKeypair();
		}
			
		
	}
	
	
	public String encrypt(String plainText) throws Exception
	{
		// get a key from the keystore
		
		String keyAsString = s.getString(Constants.pref_private_key, null);
		
		byte[] keyBytes = org.bouncycastle.util.encoders.Base64.decode(keyAsString);
		PrivateKey privateKey;
		byte []encodedBytes = null;
		String encryptedText = "";
		
		try {
	        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyBytes);
	        KeyFactory generator = KeyFactory.getInstance("RSA");
	        privateKey = generator.generatePrivate(privateKeySpec);

	       
	        Cipher c = Cipher.getInstance("RSA");
	        c.init(Cipher.ENCRYPT_MODE, privateKey);
	        encodedBytes = c.doFinal(plainText.getBytes());
		   // byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
		    encryptedText = new String(org.bouncycastle.util.encoders.Base64.encode(encodedBytes));
		    
		    Log.i("encryptedText", encryptedText);
		    
		    /*String PubkeyAsString = s.getString(MainActivity.public_key, null);
		    byte[] PubkeyBytes = org.bouncycastle.util.encoders.Base64.decode(PubkeyAsString);
		    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(PubkeyBytes);   
		    PublicKey publicKey = generator.generatePublic(publicKeySpec);
		    byte[] decodedBytes = org.bouncycastle.util.encoders.Base64.decode(encryptedText);
		    Cipher cipher = Cipher.getInstance("RSA");
		    cipher.init(Cipher.DECRYPT_MODE, publicKey);
		    byte[] decryptedBytes = cipher.doFinal(decodedBytes);
		    String decryptedText = new String(decryptedBytes);
		    Log.i("decryptedText", decryptedText);*/
		    
	    } catch (Exception e) 
	    {
	        throw new IllegalArgumentException("Failed to create KeyPair from provided encoded keys", e);
	    }
		
	    

	    return encryptedText;
	}
	
	public String decrypt(String encryptedText, String PubkeyAsString)
	{
		String decryptedText = "";
		
		
		try 
		{
			KeyFactory generator = KeyFactory.getInstance("RSA");
			//String PubkeyAsString = s.getString(MainActivity.public_key, null);
		    byte[] PubkeyBytes = org.bouncycastle.util.encoders.Base64.decode(PubkeyAsString);
		    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(PubkeyBytes);   
		    PublicKey publicKey = generator.generatePublic(publicKeySpec);
		    
		    
		    byte[] decodedBytes = org.bouncycastle.util.encoders.Base64.decode(encryptedText);
		    Cipher cipher = Cipher.getInstance("RSA");
		    cipher.init(Cipher.DECRYPT_MODE, publicKey);
		    byte[] decryptedBytes = cipher.doFinal(decodedBytes);

		    decryptedText = new String(decryptedBytes);
		    Log.i("decryptedText", decryptedText);
		    
	    } catch (Exception e) 
	    {
	        throw new IllegalArgumentException("Failed to create KeyPair from provided encoded keys", e);
	    }
		
		
		return decryptedText;
	}
	
	
	
	
	/**
	 * Generate an RSA keypair
	 */
	public void generateKeypair()
	{
		try 
		{
	        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
	        kpg.initialize(keySize);
	        KeyPair kp = kpg.genKeyPair();
	        Key publicKey = kp.getPublic();
	        Key privateKey = kp.getPrivate();
	        

	        String privateKeyAsString = new String(org.bouncycastle.util.encoders.Base64.encode(privateKey.getEncoded()));
	        String publicKeyAsString = new String(org.bouncycastle.util.encoders.Base64.encode(publicKey.getEncoded()));
	        
	        /*byte[] pub = publicKey.getEncoded();
	        String _pub = new String(org.bouncycastle.util.encoders.Base64.encode(pub));
	        
	        byte[] pub2 = org.bouncycastle.util.encoders.Base64.decode(_pub);

	        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pub2);
	        KeyFactory generator = KeyFactory.getInstance("RSA");
	        PublicKey publicKey2 = generator.generatePublic(publicKeySpec);*/
	        
	        SharedPreferences.Editor e = s.edit();
		    e.putString(Constants.pref_private_key, privateKeyAsString);		
			e.putString(Constants.pref_public_key, publicKeyAsString);
		
			Log.i("public key", publicKeyAsString);
			Log.i("private key", privateKeyAsString);
			
			
			while (!e.commit());
		    
			
	    } catch (Exception e) 
	    {
	    	Log.e("CryptMS", "KEYGEN ERROR " + e.toString());
			throw new RuntimeException("RSA KEYGEN ERROR " + e.toString());
	    }
		
	}

}
