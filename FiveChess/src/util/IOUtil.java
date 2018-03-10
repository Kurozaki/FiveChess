package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by YotWei on 2017/12/5.
 * a util to do something about IO
 */
public class IOUtil {


    public static byte[] readBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[256];
        int mark;
        while ((mark = is.read(buffer)) != -1) {
            bos.write(buffer, 0, mark);
        }
        return bos.toByteArray();
    }

}
