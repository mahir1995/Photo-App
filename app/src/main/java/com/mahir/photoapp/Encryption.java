package com.mahir.photoapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption
{

    private static SecretKey generateKey() throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        char[] passphraseOrPin = {'p', 'a', 's', 's'};
        byte[] salt = {1, 2, 3, 4};

        final int iterations = 1000;

        final int outputKeyLength = 256;

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(passphraseOrPin, salt, iterations, outputKeyLength);
        return secretKeyFactory.generateSecret(keySpec);
    }

    private static byte[] encodeFile(SecretKey yourKey, byte[] fileData)
            throws Exception
    {
        byte[] encrypted = null;
        byte[] data = yourKey.getEncoded();
        SecretKeySpec skeySpec = new SecretKeySpec(data, 0, data.length, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        encrypted = cipher.doFinal(fileData);
        return encrypted;
    }

    private static byte[] decodeFile(byte[] fileData)
            throws Exception
    {
        byte[] decrypted = null;
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKey yourKey = generateKey();
        cipher.init(Cipher.DECRYPT_MODE, yourKey, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        decrypted = cipher.doFinal(fileData);
        return decrypted;
    }

    public static void saveFile(Bitmap bitmap, String filename)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();

        try
        {
            File file = new File(Environment.getExternalStorageDirectory() + "/MyImages/", filename);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            SecretKey yourKey = generateKey();
            byte[] filesBytes = encodeFile(yourKey, data);
            bos.write(filesBytes);
            bos.flush();
            bos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static Bitmap decodeFile(String filename)
    {
        try
        {
            byte[] decodedData = decodeFile(readFile(filename));
            return BitmapFactory.decodeByteArray(decodedData, 0, decodedData.length);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] readFile(String filename)
    {
        byte[] contents = null;

        File file = new File(Environment.getExternalStorageDirectory() + "/MyImages", filename);
        int size = (int) file.length();
        contents = new byte[size];
        try
        {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            try
            {
                buf.read(contents);
                buf.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return contents;
    }

}
