import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import com.csvreader.CsvReader;

public class FileReader {
    public static ArrayList<String> readCsvByCsvReader(String filePath) {
        ArrayList<String> strList = null;
        try {
            ArrayList<String[]> arrList = new ArrayList<String[]>();
            strList = new ArrayList<String>();
            CsvReader reader = new CsvReader(filePath, ',', Charset.forName("UTF-8"));
            reader.readHeaders();
            while (reader.readRecord()) {
                arrList.add(reader.getValues()); // 按行读取，并把每一行的数据添加到list集合
            }
            reader.close();
            System.out.println("读取的行数：" + arrList.size());
            // 如果要返回 String[] 类型的 list 集合，则直接返回 arrList
            // 以下步骤是把 String[] 类型的 list 集合转化为 String 类型的 list 集合
            for (int row = 0; row < arrList.size(); row++) {
                // 组装String字符串
                // 如果不知道有多少列，则可再加一个循环
                String ele = arrList.get(row)[0] + "," + arrList.get(row)[1] + ","
                        + arrList.get(row)[2] + "," + arrList.get(row)[3] ;
				System.out.println(ele);
                strList.add(ele);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strList;
    }
    
    public static ArrayList<String> readByBufferedReader(String filePath) {
        File csv = new File(filePath);
        csv.setReadable(true);
        csv.setWritable(true);
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            isr = new InputStreamReader(new FileInputStream(csv), "UTF-8");
            br = new BufferedReader(isr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String line = "";
        ArrayList<String> records = new ArrayList<String>();
        try {
            while ((line = br.readLine()) != null) {
//                System.out.println(line);
                records.add(line);
            }
//            System.out.println("csv表格读取行数：" + records.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }
}
