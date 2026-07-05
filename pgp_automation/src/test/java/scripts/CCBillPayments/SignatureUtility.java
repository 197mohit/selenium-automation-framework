package scripts.CCBillPayments;

import com.paytm.LocalConfig;
import com.paytm.framework.api.BaseApi;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;

public class SignatureUtility extends BaseApi {
    private static final String hashAlgo = "SHA-256";
    private static final char[] DIGITS_LOWER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8',
      '9', 'a', 'b', 'c', 'd', 'e', 'f'};

  /*below key is present at billproxy server SHA256_KEY_LOCATION = "/etc/payment_engine/key/test/billproxy_SHA256.key"
  if there is error in signature, make sure this key is correct by matching it from server
  */
    public static final String sha256Key = LocalConfig.SHA_256_KEY;

    public static String Signature(String tin) {
        ArrayList<String> inputList = new ArrayList<>();
        String finalString= null;
        inputList.add("IN");
        inputList.add(tin);
        System.out.println("List size is" + inputList.size());

        if (inputList.size() > 0) {
            try {
                StringBuilder sb = new StringBuilder();
                for (String input : inputList) {
                    System.out.println("Input is "+input);
                        sb.append(input);
                    }
                    sb.append(sha256Key);
                    byte[] signedBytes = MessageDigest.getInstance(hashAlgo).digest(
                    sb.toString().getBytes(StandardCharsets.UTF_8));
                    finalString = new String(encode(signedBytes, DIGITS_LOWER));
                    System.out.println("The final signature : " + finalString);
            } catch (Exception e) {
            e.printStackTrace();
            }
        } else {
              System.out.println("Input reading went failed. Please run the class and give the inputs");
        }
        return finalString;
  }

  protected static char[] encode(byte[] data, char[] toDigits) {
    int l = data.length;
    char[] out = new char[l << 1];
    int i = 0;
    for (int var5 = 0; i < l; ++i) {
      out[var5++] = toDigits[(240 & data[i]) >>> 4];
      out[var5++] = toDigits[15 & data[i]];
    }
    return out;
  }
}
