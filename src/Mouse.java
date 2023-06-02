import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelListener;

// mouse dinlemesinden sorumlu class
class Mouse implements NativeMouseInputListener, NativeMouseWheelListener {
    // aşırı loglamayı önleyecek değişkenler
    private static final int LOG_DELAY_MS = 400; // aşırı loglanabilecek aksiyonları önlemek için aralık
    private long lastMouseMovedTime = 0;
    private long lastMouseDraggedTime = 0;

    // fareye dinleyiciler atama
    public void StartListening(){
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException exception) {
            System.err.println("Error!");
            System.err.println(exception.getMessage());
            System.exit(1);
        }
        GlobalScreen.addNativeMouseListener(this);
        GlobalScreen.addNativeMouseMotionListener(this);
        GlobalScreen.addNativeMouseWheelListener(this);
    }

    // dinlemeyi durdurma (tüm dinleyicileri siler)
    public void StopListening(){
        GlobalScreen.removeNativeMouseListener(this);
        GlobalScreen.removeNativeMouseMotionListener(this);
        GlobalScreen.removeNativeMouseWheelListener(this);
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    // fareye kısa süredeki tıklama sayısını log dosyasına ekle
    public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {
        LogFile.appendLog("Fare Tiklanma Sayisi: "+nativeMouseEvent.getClickCount());
    }

    // farenin hangi tuşuna basıldığını log dosyasına ekle
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
        LogFile.appendLog(nativeMouseEvent.getButton()+". Fare Tusuna Basildi");
    }

    // farenin hangi tuşunun bırakıldığını log dosyasına ekle
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {
        LogFile.appendLog(nativeMouseEvent.getButton()+". Fare Tusu Birakildi");
    }

    // fare hareketini belli aralıklarda log dosyasıan ekleme (aralıksız çok fazla yazma işlemi olur)
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMouseMovedTime >= LOG_DELAY_MS) {
            LogFile.appendLog("Mouse Hareketi: " + nativeMouseEvent.getX() + ", " + nativeMouseEvent.getY());
            lastMouseMovedTime = currentTime;
        }
    }

    // fare sürükleme hareketini belli aralıklarla log dosyasına ekleme
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMouseDraggedTime >= LOG_DELAY_MS) {
            LogFile.appendLog("Mouse Sürüklendi: " + nativeMouseEvent.getX() + ", " + nativeMouseEvent.getY());
            lastMouseDraggedTime = currentTime;
        }
    }

    // fare tekerleğinin hangi yöne sürüklendiğini log dosyasına ekleme
    public void nativeMouseWheelMoved(NativeMouseWheelEvent nativeMouseWheelEvent) {
        LogFile.appendLog("Mouse Tekerlegi "+nativeMouseWheelEvent.getWheelRotation()+" Yönünde Hareket Etti");
    }
}