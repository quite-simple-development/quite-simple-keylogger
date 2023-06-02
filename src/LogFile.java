import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// loglama işlemlerini / dosya işlemlerini yürüten class
public class LogFile {
    public static String fileName = "Log.txt"; // dosyanın ismi
    private static double maxFileSize = 0;  // önceden hazırlanmış max boyutu
    private static boolean alreadyCreated = false; // başlat durdur başlat yapıldığında dosyanın içeriğini silinmesini önleyen değişken

    // log dosyasına log ekleme
    public static void appendLog(String log) {
        if(isAppendable()) {
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'tarih: 'dd.MM.yy', saat: 'HH:mm:ss");
            String formattedTime = currentTime.format(formatter);
            try {
                FileWriter Writer = new FileWriter(fileName, true);
                Writer.write(log + "  |  " + formattedTime + "\n");
                Writer.close();
            } catch (IOException e) {
                System.out.println("Dosya Acilirken Hata Olustu!");
                e.printStackTrace();
            }
        }

        // log dosyası boyutu sınırına ulaştığında tüm içerik silinir
        else {
            try {
                FileWriter fw = new FileWriter(fileName, false);
                PrintWriter pw = new PrintWriter(fw, false);
                pw.flush();
                pw.close();
                fw.close();
            } catch (IOException e) {
                System.out.println("Dosya Acilirken Hata Olustu!");
                e.printStackTrace();
            }
        }
    }

    // log dosyası boyutunu veren fonksiyon
    private static double FileSizeMB() {
        File file = new File(fileName);
        return (double) file.length()/(1024*1024);
    }
    // log dosyasına ekleme yapılabilir mi kontrol eden fonksyion
    private static boolean isAppendable() {
        double fileSize = FileSizeMB();
        double maxLogSize = (double) 30/(1024*1024);

        //bir log eklendiğinde dosya boyutu aşılıyor mu kontrol edilir
        return (fileSize+maxLogSize <= maxFileSize);
    }

    // log dosyasını ancak bir kere oluşturma fonksiyonu
    public static void CreateFile(int maxFileSize){
        if(!alreadyCreated){
            try {
                FileWriter Writer= new FileWriter(fileName);
                LogFile.maxFileSize = maxFileSize;
                Writer.close();
            } catch (IOException e) {
                System.out.println("Dosya Acilirken Hata Olustu!");
                e.printStackTrace();
            }
            alreadyCreated=true;
        }
    }
}
