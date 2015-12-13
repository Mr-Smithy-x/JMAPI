import com.mrsmyx.*;
import com.mrsmyx.exceptions.JMAPIException;
import com.mrsmyx.utils.BitConverter;

public class Main {

    public static void print(Object o) {
        System.out.print(o);
    }

    public static void main(String[] args) {
        JMAPI j = new JMAPI(true);
/*
byte[] buffer = new byte[]
				{
					 0x38, 0x60, 0x7F, 0xFF, 0xB0, 0x7F, 0x00, 0xB4
				};
 */
        if (j.isConnected()) {
            if (j.attach("16843264")) {
                try {
                    j.setMemory(0x1185D08, new char[]{0x38, 0x60, 0x7F, 0xFF, 0xB0, 0x7F, 0x00, 0xB4}, new JMAPI.JMAPIMemoryListener() {
                        @Override
                        public void onJMAPIGetMemory(String process, long offset, int size, byte[] signed, int[] memory) throws JMAPIException {
                            System.out.print(String.format("Process: %s ; At index: %s ; With size of: %s ; Memory: ", process, offset, size));
                            for (int b : memory) {
                                System.out.print(Integer.toHexString(b).toUpperCase());
                            }
                            System.out.println("\nWoot woot");
                            j.boot(JMAPI.PS3BOOT.REBOOT);
                        }

                        @Override
                        public void onJMAPISetMemory(String response, long offset) {
                            System.out.println(String.format("%s : %s", offset, response));
                        }

                        @Override
                        public void onJMAPIMemoryError(String response, String process, long offset) {
                            System.out.println("FAILED");
                            j.disconnect();
                        }
                    });
                } catch (Exception e) {
                    // TODO Auto-generated catch block

                    e.printStackTrace();
                }
            }
        }
    }

}
