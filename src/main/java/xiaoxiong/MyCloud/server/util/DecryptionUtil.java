package xiaoxiong.MyCloud.server.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class DecryptionUtil {
	private static Base64.Decoder decoder;
    private static KeyFactory kf;
    private static Cipher c;
    
    public static String dncryption(final String context, final String privateKey) {
        final byte[] b = DecryptionUtil.decoder.decode(privateKey);
        final byte[] s = DecryptionUtil.decoder.decode(context.getBytes(StandardCharsets.UTF_8));
        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(b);
        try {
            final PrivateKey key = DecryptionUtil.kf.generatePrivate(spec);
            DecryptionUtil.c.init(2, key);
            final byte[] f = DecryptionUtil.c.doFinal(s);
            return new String(f);
        }
        catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        catch (InvalidKeyException e2) {
            e2.printStackTrace();
        }
        catch (IllegalBlockSizeException e3) {
            e3.printStackTrace();
        }
        catch (BadPaddingException e4) {
            e4.printStackTrace();
        }
        return null;
    }
    
    static {
        DecryptionUtil.decoder = Base64.getDecoder();
        try {
            DecryptionUtil.kf = KeyFactory.getInstance("RSA");
            DecryptionUtil.c = Cipher.getInstance("RSA");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (NoSuchPaddingException e2) {
            e2.printStackTrace();
        }
    }
}
