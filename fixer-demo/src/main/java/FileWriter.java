import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileWriter {
	public static void writeNewLine(String path, String context) throws IOException {
		
		BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(path,true)));
		out.write(context+"\n");
		out.close();
	}
	
	public static void writeLineCoveringPrevious(String path, String context) throws IOException {
        FileOutputStream fileOutputStream = null;
        File file = new File(path);
        if(!file.exists()){
            file.createNewFile();
        }
        fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(context.getBytes("utf-8"));
        fileOutputStream.flush();
        fileOutputStream.close();
	}
}
