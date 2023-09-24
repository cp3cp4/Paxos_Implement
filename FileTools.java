import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;

public class FileTools {
    public void fileCover(String originalFilePath, String newFilePath) {
        //file to be read, indicating that it is in the root of the project
        File file = new File(originalFilePath);
        String content;
        if (file.exists()) {

            try {
                String str;
                StringBuffer stringBuffer = new StringBuffer();
                //Read the contents of the file
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                while ((content = bufferedReader.readLine()) != null) {
                    stringBuffer.append(content);
                }
                bufferedReader.close();
                inputStreamReader.close();
                fileInputStream.close();
                //Writing to a file
                File newfile = new File(newFilePath);
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newfile), "UTF-8"));

                bufferedWriter.write(stringBuffer.toString());
                bufferedWriter.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Boolean isSameFile(String fileA,String fileB){
        String firstMD5 = getFileMD5(new File(fileA));
        String secondMD5 = getFileMD5(new File(fileB));
        return firstMD5.equals(secondMD5);
    }

    public Boolean isSameFile(File fileA,File fileB){
        String firstMD5 = getFileMD5(fileA);
        String secondMD5 = getFileMD5(fileB);
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

    public synchronized void fileCover(File originalFile, File newFile) {
        String content;
        if (originalFile.exists()) {
            try {
                String str;
                StringBuffer stringBuffer = new StringBuffer();
                //read file
                FileInputStream fileInputStream = new FileInputStream(originalFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                while ((content = bufferedReader.readLine()) != null) {
                    stringBuffer.append(content);
                    stringBuffer.append("\n");
                }
                bufferedReader.close();
                inputStreamReader.close();
                fileInputStream.close();
                //Writing to a file
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile), "UTF-8"));

                bufferedWriter.write(stringBuffer.toString());
                bufferedWriter.close();

                //delete the last line
		// RandomAccessFile f = new RandomAccessFile(newFile, "rw");
		// long length = f.length() - 1;
		// byte b;
                //do {
		//     f.seek(length);length -= 1;
                //    b = f.readByte();
		// } while(b != 10);
                //f.setLength(length+1);
		//f.writeBytes("$");////////////
                //f.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //public static void main(String[] args) {
    //     File file1 = new File("ContentSever0.txt");
    //    File file2 = new File("ContentSever1.txt");
        //new FileTools().fileCover(file1,file2);
    //    System.out.println(new FileTools().isSameFile(file1,file2));
    //}
}
