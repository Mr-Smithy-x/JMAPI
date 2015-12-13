package com.mrsmyx.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by charlton on 10/12/15.
 */
public class BitConverter {

    public static int[] ConvertUnsigned(Byte[] bytes){
        int uBytes[] = new int[bytes.length];
        for(int i = 0; i < bytes.length;i++){
            uBytes[i] = bytes[i] & 0xff;
        }
        return uBytes;
    }

    public static byte[] toBytes(char[] chars) {
        byte[] bytes = new byte[chars.length];
        for(int i =0;i < chars.length; i++){
            bytes[i] = (byte)((int)chars[i]);
        }
        return bytes;
    }
}
