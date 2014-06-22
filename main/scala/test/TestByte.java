package test;

import scala.collection.mutable.ArrayBuffer;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;

/**
 * Created by bjcheny on 6/14/14.
 */
public class TestByte {
    static public byte[] send() {
//        byte[] bytes = new byte[4];
//        bytes[0] = 0xf;
//        bytes[1] = 0xf;
//        bytes[2] = 0xf;
//        bytes[3] = 0xf;
//        bytes[2] = 'a';
//        bytes[3] = 'b';
        short x = Short.MAX_VALUE;
//        System.out.println(bytes);
//        return String.valueOf(bytes);
//        return bytes;
        byte[] ret = new byte[2];
        ret[0] = (byte)(x & 0xff);
        ret[1] = (byte)((x >> 8) & 0xff);
        return ret;
    }

    static public ByteBuffer sendByteBuffer() {
        String str = "";
        ByteBuffer.wrap(str.getBytes());
        short x = Short.MAX_VALUE;
        return ByteBuffer.allocate(2).putShort(x);
    }

    static public void getByte(byte[] bytes) {
        System.out.println(bytes);
        byte b = bytes[1];
    }

    static public void getByteBuffer(ByteBuffer bytes) {
        System.out.println(bytes);
    }

    static public void getArrayBuffer(ArrayBuffer<Byte> bytes) {
        System.out.println(bytes);
    }

    static public void getString(String str) {
        System.out.println(str);
        byte[] bytes = str.getBytes();
        System.out.println(bytes[1]);
    }

    static public void testByteBuffer() {
        //Create a directBuffer of size 200 bytes
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(200); //Create a nonDirectBuffer of size 200 bytes
        System.out.println("Capacity "+directBuffer.capacity()); //Get the position of the buffer
        System.out.println("server.pvp.CharPosition "+directBuffer.position());

        ByteBuffer nonDirectBuffer = ByteBuffer.allocate(200); //Get the capacity of the buffer
        System.out.println("Capacity "+nonDirectBuffer.capacity()); //Get the position of the buffer
        System.out.println("server.pvp.CharPosition "+nonDirectBuffer.position() );
    }

    static public byte[] sendString() {
        byte[] str = new byte[5];
        str[0] = 'a';
        str[1] = 'b';
        str[2] = 'c';
        str[3] = 'd';
        str[4] = 0;
        return str;
    }

    static public ByteBuffer sendByteBufferWithString() {
        String str = "abcd";
        byte[] strBytes = new byte[5];
        System.arraycopy(str.getBytes(), 0, strBytes, 0, 4);
        strBytes[4] = 0;
        return ByteBuffer.wrap(strBytes);
    }
}
