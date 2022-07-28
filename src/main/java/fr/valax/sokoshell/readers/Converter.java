package fr.valax.sokoshell.readers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * https://github.com/mateoconlechuga/convbin/blob/master/src/convert.c#L132
 */
public class Converter {

    public static final int TI8X_MAXDATA_SIZE = (0x10000 - 0x130);

    private static final byte CHECKSUM_LEN = 2;
    private static final byte VARB_SIZE_LEN = 2;
    private static final byte VAR_HEADER_LEN = 17;

    private static final byte DATA_SIZE = 0x35;
    private static final byte VAR_HEADER = 0x37;
    private static final byte VAR_SIZE0 = 0x39;
    private static final byte TYPE = 0x3b;
    private static final byte NAME = 0x3c;
    private static final byte ARCHIVE = 0x45;
    private static final byte VAR_SIZE1 = 0x46;
    private static final byte VARB_SIZE = 0x48;
    private static final byte DATA = 0x4a;

    private static final byte MAGIC = 0x0d;

    private static final byte TYPE_APPVAR = 0x15;

    private static final byte[] header = new byte[]{0x2A, 0x2A, 0x54, 0x49, 0x38, 0x33, 0x46, 0x2A, 0x1A, 0x0A, 0x00};

    @SuppressWarnings({"UnnecessaryLocalVariable", "PointlessArithmeticExpression", "PointlessBitwiseExpression"})
    public static byte[] convert(byte[] data, String varName) throws IOException {
        int size = data.length;

        if (size > TI8X_MAXDATA_SIZE) {
            throw new IOException("Max size reached (%f KB). Maximum is %f KB Try reduce the number of levels"
                    .formatted(data.length / 1024f, TI8X_MAXDATA_SIZE / 1024f));
        }

        int file_size = size + DATA + CHECKSUM_LEN;
        int data_size = size + VAR_HEADER_LEN + VARB_SIZE_LEN;
        int var_size = size + VARB_SIZE_LEN;
        int varb_size = size;

        // System.out.printf("file_size: %d\n", file_size);
        // System.out.printf("data_size: %d\n", data_size);
        // System.out.printf("var_size: %d\n", var_size);
        // System.out.printf("varb_size: %d\n", varb_size);
        // System.out.printf("size: %d\n", size);


        byte[] output = new byte[file_size];

        // Write header
        System.arraycopy(header, 0, output, 0, header.length);

        // Write name
        int offset = NAME;
        byte[] name = varName.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(name, 0, output, offset, name.length);

        // System.out.printf("name_size: %d\n", Math.min(name.length, 8));

        // data
        offset = DATA;
        System.arraycopy(data, 0, output, offset, size);

        output[VAR_HEADER] = MAGIC;
        output[TYPE] = TYPE_APPVAR;
        output[ARCHIVE] = (byte) 0x80;

        output[DATA_SIZE + 0] = (byte) ((data_size >> 0) & 0xff);
        output[DATA_SIZE + 1] = (byte) ((data_size >> 8) & 0xff);

        output[VARB_SIZE + 0] = (byte) ((varb_size >> 0) & 0xff);
        output[VARB_SIZE + 1] = (byte) ((varb_size >> 8) & 0xff);

        output[VAR_SIZE0 + 0] = (byte) ((var_size >> 0) & 0xff);
        output[VAR_SIZE0 + 1] = (byte) ((var_size >> 8) & 0xff);
        output[VAR_SIZE1 + 0] = (byte) ((var_size >> 0) & 0xff);
        output[VAR_SIZE1 + 1] = (byte) ((var_size >> 8) & 0xff);

        int checksum = ti8xChecksum(output, data_size);

        output[DATA + size + 0] = (byte) ((checksum >> 0) & 0xff);
        output[DATA + size + 1] = (byte) ((checksum >> 8) & 0xff);

        return output;
    }

    private static int ti8xChecksum(byte[] data, int size) {
        int checksum = 0;

        for (int i = 0; i < size; i++) {
            checksum += Byte.toUnsignedInt(data[VAR_HEADER + i]);
            checksum &= 0xFFFF;

            // System.out.printf("checksum: %d, arr: %d\n", checksum, Byte.toUnsignedInt(data[VAR_HEADER + i]));
        }

        return checksum;
    }

    public static byte[] extract(byte[] in) {
        int length = in.length - DATA - CHECKSUM_LEN;

        if (length < 0) {
            return null;
        } else {
            byte[] output = new byte[length];

            System.arraycopy(in, DATA, output, 0, output.length);

            return output;
        }
    }

    public static byte[] extractFileName(byte[] in) {
        if (NAME + 8 >= in.length) {
            return null;
        } else {
            int offset = NAME;

            int len = 0;
            for (int i = offset; i < offset + 8; i++) {
                if (in[i] == 0) {
                    break;
                }
                len++;
            }

            byte[] out = new byte[len];
            System.arraycopy(in, offset, out, 0, len);

            return out;
        }
    }
}