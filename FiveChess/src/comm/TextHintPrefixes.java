package comm;

/**
 * Created by YotWei on 2017/12/4.
 * text info prefix like '[info], [waning]..'
 */
public class TextHintPrefixes {
    private static String[] textTypes = {"[INFO]", "[WARNING]", "[ERROR]"};
    public static final int TEXT_TYPE_NONE = -1;
    public static final int TEXT_TYPE_INFO = 0;
    public static final int TEXT_TYPE_WARNING = 1;
    public static final int TEXT_TYPE_ERROR = 2;

    public static String textPrefix(int type) {
        if (type >= 0 && type < textTypes.length)
            return textTypes[type];
        return "";
    }
}
