import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public class CompareFiles {
    /*public static void main(String[] args) {
        String pathFirst = "compare1.txt";
        String pathSecond = "compare2.txt";

        *//*File fileFirst = new File(pathFirst);
        File fileSecond = new File(pathSecond);

        String firstMD5 = getFileMD5(fileFirst);
        String secondMD5 = getFileMD5(fileSecond);

        System.out.println(firstMD5.equals(secondMD5));*//*

        System.out.println(new CompareFiles().isSameFile(pathFirst,pathSecond));
    }*/

    public Boolean isSameFile(String fileA,String fileB){
        String firstMD5 = getFileMD5(new File(fileA));
        String secondMD5 = getFileMD5(new File(fileB));
        return firstMD5.equals(secondMD5);
    }

    public String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[8192];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            BigInteger bigInt = new BigInteger(1, digest.digest());
            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
