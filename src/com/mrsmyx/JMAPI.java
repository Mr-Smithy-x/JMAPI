package com.mrsmyx;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.mrsmyx.JMAPI.JMAPIListener.PS3OP;
import com.mrsmyx.exceptions.JMAPIException;
import com.mrsmyx.ps3util.PS3Client;
import com.mrsmyx.ps3util.PS3Process;
import com.mrsmyx.ps3util.Temperature;
import com.mrsmyx.utils.Network;
import com.mrsmyx.utils.Response;
import com.mrsmyx.utils.BitConverter;

public class JMAPI extends PS3Client {

    public interface JMAPIListener {
        enum PS3OP {
            IDPS,
            PSID,
            LED,
            DELHISTORY,
            BUZZ,
            DISSYSCALL,
            SYSCALL8MODE,
            CHECKSYSCALL,
            NETWORK_FOUND,
            DISCONNECTED,
            ERROR, FWTYPE, FWVERSION, SCMINVERSION, PSIDSET, IDPSSET, BOOT, SCVERSION
        }

        void onJMAPIError(String error);

        void onJMAPIResponse(PS3OP ps3Op, JMAPI.PS3MAPI_RESPONSECODE responseCode, String message);

        void onJMAPIPS3Process(JMAPI.PS3MAPI_RESPONSECODE responseCode, List<com.mrsmyx.ps3util.PS3Process> processes);

        void onJMAPITemperature(JMAPI.PS3MAPI_RESPONSECODE responseCode, com.mrsmyx.ps3util.Temperature temperature);
    }

    public interface JMAPIMemoryListener {
        void onJMAPIGetMemory(String process, long offset, int size, byte[] signedBytes, int[] unsignedBytes) throws JMAPIException;

        void onJMAPISetMemory(String response, long offset);

        void onJMAPIMemoryError(String response, String process, long offset);
    }

    private JMAPIListener listener;
    private int port = 7887;
    private String ip = null;
    private List<PS3Process> processList = null;
    private boolean isAttached = false;
    private String process = null;

    public enum PS3BOOT {
        REBOOT,
        SOFTREBOOT,
        HARDREBOOT,
        SHUTDOWN
    }

    public enum BUZZER {
        SINGLE,
        DOUBLE,
        TRIPLE
    }

    public enum LEDCOLOR {
        RED,
        GREEN,
        YELLOW
    }

    public enum LEDMODE {
        OFF,
        ON,
        BLINKFAST,
        BLINKSLOW
    }

    public enum DELHISTORY {
        EXCLUDE_DIR,
        INCLUDE_DIR
    }

    public enum SYSCALL8MODE {
        ENABLED,
        ONLY_COBRAMAMBA_AND_PS3API_ENABLED,
        ONLY_PS3MAPI_ENABLED,
        FAKEDISABLED,
        DISABLED
    }

    public enum PS3MAPI_RESPONSECODE {
        DATACONNECTIONALREADYOPEN(125),
        MEMORYSTATUSOK(150),
        COMMANDOK(200),
        REQUESTSUCCESSFUL(226),
        ENTERINGPASSIVEMOVE(227),
        PS3MAPICONNECTED(220),
        PS3MAPICONNECTEDOK(230),
        MEMORYACTIONCOMPLETED(250),
        MEMORYACTIONPENDING(350);

        private int id;

        PS3MAPI_RESPONSECODE(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }
    }

    public JMAPI(String ip) {
        this.ip = ip;
    }

    public JMAPI() {

    }

    /**
     * Attaches to the ps3 process with the process id provided by developer
     *
     * @param process Attaches to the process
     * @return whether the process attachment was successful
     * @see PS3Process
     */
    public boolean attach(String process) {
        this.processList = getAllProcesses(new JMAPIListener() {
            @Override
            public void onJMAPIError(String error) {

            }

            @Override
            public void onJMAPIResponse(PS3OP ps3Op, PS3MAPI_RESPONSECODE responseCode, String message) {

            }

            @Override
            public void onJMAPIPS3Process(PS3MAPI_RESPONSECODE responseCode, List<PS3Process> processes) {
                JMAPI.this.processList = processes;
            }

            @Override
            public void onJMAPITemperature(PS3MAPI_RESPONSECODE responseCode, Temperature temperature) {

            }
        });
        if (containsProcess(process)) {
            this.process = process;
            return this.isAttached = true;
        }
        return this.isAttached = false;
    }

    /**
     * Attaches to the ps3 process automatically.
     *
     * @return whether the process attachment was successful
     * @see
     */
    public boolean smartAttach() {
        this.processList = getAllProcesses(new JMAPIListener() {
            @Override
            public void onJMAPIError(String error) {

            }

            @Override
            public void onJMAPIResponse(PS3OP ps3Op, PS3MAPI_RESPONSECODE responseCode, String message) {

            }

            @Override
            public void onJMAPIPS3Process(PS3MAPI_RESPONSECODE responseCode, List<PS3Process> processes) {
                JMAPI.this.processList = processes;
            }

            @Override
            public void onJMAPITemperature(PS3MAPI_RESPONSECODE responseCode, Temperature temperature) {

            }
        });
        PS3Process p = smartContainsProcess();
        if (p != null) {
            this.process = p.getProcess();
            return this.isAttached = true;
        } else {
            return this.isAttached = false;
        }
    }


    protected PS3Process smartContainsProcess() {
        for (PS3Process p : processList) {
            if (p.getTitle().toLowerCase().contains("eboot")) return p;
        }
        return null;
    }

    protected boolean containsProcess(String process) {
        for (PS3Process p : processList) {
            if (p.getProcess().equals(process)) return true;
        }
        return false;
    }

    /**
     * Detaches from the process
     *
     * @see
     */
    public void detach() {
        this.process = null;
        this.isAttached = false;
    }

    /**
     * Scans network
     *
     * @param searchNetwork if true, JMAPI will scan your network for you're ps3.
     * @return If found, JMAPI will automatically connect, if not found you will have to call connect.
     * @see
     */

    public JMAPI(boolean searchNetwork) {
        if (searchNetwork) {
            scanNetwork();
            System.out.println(this.ip);
        }
    }

    /**
     * Connects to console with specified ip
     *
     * @param ip Connect to the ps3 ip
     * @param listener listener allows you to recieve events in which the command was successful, add 'implements JMAPIListener' to the top of your class
     * @see
     */

    public JMAPI(String ip, JMAPIListener listener) {
        this.ip = ip;
        this.listener = listener;
    }

    /**
     * Allows you to receive responses
     *
     * @param listener listener allows you to recieve events in which the command was successful, add 'implements JMAPIListener' to the top of your class
     * @see
     */
    public JMAPI(JMAPIListener listener) {
        this.listener = listener;
    }


    /**
     * Scans your network to look for your ps3
     *
     * @param searchNetwork scans network
     * @param listener listener allows you to recieve events in which the command was successful, add 'implements JMAPIListener' to the top of your class
     * @see
     */

    public JMAPI(boolean searchNetwork, JMAPIListener listener) {
        this.listener = listener;
        if (searchNetwork) {
            scanNetwork();
            System.out.println(this.ip);
        }
    }

    enum VERSION {
        CORE,
        SERVER,
    }


    /**
     * Gets the firmware of your ps3
     * @return PS3 Firmware Version ie. 4.75
     * @see
     */

    public String getFwVersion() {
        String s = "PS3 GETFWVERSION";
        Response res = super.Send(s);
        if (this.listener != null) {
            String str = new StringBuilder(Integer.toHexString(Integer.valueOf(res.getResponse()))).insert(1, ".").toString();
            this.listener.onJMAPIResponse(PS3OP.FWVERSION, res.getResponseCode(), str);
        }
        return res.getResponse();
    }

    /**
     * Disables SYSCall mode
     *
     * @see com.mrsmyx.JMAPI.SYSCALL8MODE
     */
    public void disableSysCall(SYSCALL8MODE mode) {
        String s = "PS3 DISABLESYSCALL " + String.valueOf(mode.ordinal());
        Response res = super.Send(s);
        res.getResponse();
        if (this.listener != null) {
            this.listener.onJMAPIResponse(PS3OP.DISSYSCALL, res.getResponseCode(), res.getResponse());
        }
    }

    /**
     * Modes
     * @return Whether this ps3 has the syscall availiable for usage
     * @see
     */
    public boolean checkSysCall(int mode) {
        String s = "PS3 CHECKSYSCALL " + String.valueOf(mode);
        Response res = super.Send(s);
        if (this.listener != null) {
            this.listener.onJMAPIResponse(PS3OP.CHECKSYSCALL, res.getResponseCode(), res.getResponse());
        }
        return Boolean.valueOf(res.getResponse());
    }

    /**
     * Checks whats the ps3 is capable of calling.
     * @return SYSCALL8MODE
     * @see com.mrsmyx.JMAPI.SYSCALL8MODE
     */
    public SYSCALL8MODE partialCheckSysCall() {
        String s = "PS3 PCHECKSYSCALL8";
        Response res = super.Send(s);
        if (this.listener != null) {
            this.listener.onJMAPIResponse(PS3OP.SYSCALL8MODE, res.getResponseCode(), SYSCALL8MODE.values()[Integer.valueOf(res.getResponse())].toString());
        }
        return SYSCALL8MODE.values()[Integer.valueOf(res.getResponse())];
    }

    /**
     * Deletes the ps3 sys history
     * @param mode Include Dir or Exclude?
     * @return whether it was a success
     * @see
     */
    public boolean deleteHistory(DELHISTORY mode) {
        String s = "PS3 DELHISTORY";
        switch (mode) {
            case EXCLUDE_DIR:
                //TODO nothing
                break;
            case INCLUDE_DIR:
                s += "+D";
                break;
        }
        Response res = super.Send(s);
        if (this.listener != null) {
            this.listener.onJMAPIResponse(PS3OP.DELHISTORY, res.getResponseCode(), mode + " : " + res.getResponse());
        }
        return res.getResponseCode() == PS3MAPI_RESPONSECODE.COMMANDOK;
    }

    public void checkSysCall() {
        //TODO : not implemented yet
    }


    /**
     * Gets all the processes that the ps3 is running
     * @return All process running on the ps3
     * @see PS3Process
     */
    public List<PS3Process> getAllProcesses() {
        List<PS3Process> process = new ArrayList<PS3Process>();
        String text = "PROCESS GETALLPID";
        Response res = super.Send(text);
        for (String s : res.getResponse().split("\\|")) {
            if (s.equals("0")) continue;

            Response r = super.Send("PROCESS GETNAME " + s);
            process.add(PS3Process.Build(r.getResponse(), s));
        }
        if (process.size() > 0 && this.listener != null) {
            this.listener.onJMAPIPS3Process(JMAPI.PS3MAPI_RESPONSECODE.REQUESTSUCCESSFUL, process);
        }
        this.processList = process;
        return process;
    }


    protected List<PS3Process> getAllProcesses(JMAPIListener jmapiListener) {
        List<PS3Process> process = new ArrayList<PS3Process>();
        String text = "PROCESS GETALLPID";
        Response res = super.Send(text);
        for (String s : res.getResponse().split("\\|")) {
            if (s.equals("0")) continue;

            Response r = super.Send("PROCESS GETNAME " + s);
            process.add(PS3Process.Build(r.getResponse(), s));
        }
        if (process.size() > 0 && jmapiListener != null) {
            jmapiListener.onJMAPIPS3Process(JMAPI.PS3MAPI_RESPONSECODE.REQUESTSUCCESSFUL, process);
        }
        return process;
    }

    /**
     * Gets the firmware type of your ps3
     * @return Firmware Type, CEX COBRA, DEX COBRA?
     * @see
     */
    public String getFwType() {
        String s = "PS3 GETFWTYPE";
        Response res = super.Send(s);
        if (this.listener != null) {
            this.listener.onJMAPIResponse(PS3OP.FWTYPE, res.getResponseCode(), res.getResponse());
        }
        return res.getResponse();
    }


    protected Response getVersion(VERSION version) {
        String s = version + " GETVERSION";
        Response res = super.Send(s);
        if (this.listener != null) {
            this.listener.onJMAPIResponse(PS3OP.SCVERSION, res.getResponseCode(), res.getResponse());
        }
        return res;
    }

    protected void setBinaryMode(boolean bMode) {
        Response r = Send("TYPE" + ((bMode) ? " I" : " A"));
        System.out.println(r.getResponse());
    }


    /**
     * Read memory that you can alter :)
     * @param address the address in 0x format, index
     * @size of home much you want to read
     * @return byte[]
     * @see
     */
    public byte[] getMemory(long address, int size) throws JMAPIException {
        if (isConnected() && isAttached()) {
            try {
                this.setBinaryMode(true);
                super.OpenDataSocket();
                long uint_address = Long.parseUnsignedLong(Long.toHexString(address), 16);
                String getMem = String.format("MEMORY GET %s %s %s", process, uint_address, size);
                System.out.println(getMem);
                super.quickSend(getMem);
                ConnectDataSocket();
                Byte[] bytez = new Byte[size];
                byte[] b = new byte[size];
                data_sock.getInputStream().read(b, 0, size);
                for (int i = 0; i < size; i++) {
                    bytez[i] = b[i];
                }
                if (super.CloseDataSocket()) {
                    Response r = super.quickRead();
                    System.out.println("Closed: " + r.getResponse());
                }
                setBinaryMode(false);
                return b;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JMAPIException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new JMAPIException("Not connected to network");
        }
        return null;
    }


    public void getMemory(long address, int size, JMAPIMemoryListener jmapiMemoryListener) throws JMAPIException {
        if (isConnected() && isAttached()) {
            try {
                this.setBinaryMode(true);
                super.OpenDataSocket();
                long uint_address = Long.parseUnsignedLong(Long.toHexString(address), 16);
                String getMem = String.format("MEMORY GET %s %s %s", process, uint_address, size);
                System.out.println(getMem);
                super.quickSend(getMem);
                ConnectDataSocket();
                Byte[] bytez = new Byte[size];
                byte[] b = new byte[size];
                data_sock.getInputStream().read(b, 0, size);
                for (int i = 0; i < size; i++) {
                    bytez[i] = b[i];
                }
                if (super.CloseDataSocket()) {
                    Response r = super.quickRead();
                    System.out.println("Closed: " + r.getResponse());
                }
                setBinaryMode(false);
                if (jmapiMemoryListener != null) {
                    jmapiMemoryListener.onJMAPIGetMemory(process, address, size, b, BitConverter.ConvertUnsigned(bytez));
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JMAPIException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (jmapiMemoryListener != null) {
                jmapiMemoryListener.onJMAPIMemoryError("An error occured getting memory", process, address);
            }

        } else {
            throw new JMAPIException("Not connected to network");
        }
    }

    public boolean isAttached() {
        return isAttached;
    }


    public boolean setMemory(long address, char[] bytes) throws JMAPIException {
        if (isConnected() && isAttached()) {
            try {
                setBinaryMode(true);
                super.OpenDataSocket();
                long uint_address = Long.parseUnsignedLong(Long.toHexString(address), 16);
                String getMem = String.format("MEMORY SET %s %s", process, uint_address);
                System.out.println(getMem);
                super.quickSend(getMem);
                ConnectDataSocket();
                byte[] bytes1 = BitConverter.toBytes(bytes);
                data_sock.getOutputStream().write(bytes1, 0, bytes1.length);
                if (super.CloseDataSocket()) {
                    Response r = super.quickRead();
                    System.out.println("Closed: " + r.getResponse());
                }
                setBinaryMode(false);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JMAPIException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new JMAPIException("Not connected to network");
        }
        return false;
    }

    public void setMemory(long address, char[] bytes, JMAPIMemoryListener jmapiMemoryListener) throws JMAPIException {
        if (isConnected() && isAttached()) {
            try {
                setBinaryMode(true);
                super.OpenDataSocket();
                long uint_address = Long.parseUnsignedLong(Long.toHexString(address), 16);
                String getMem = String.format("MEMORY SET %s %s", process, address);
                System.out.println(getMem);
                super.quickSend(getMem);
                ConnectDataSocket();
                byte[] bytes1 = BitConverter.toBytes(bytes);
                data_sock.getOutputStream().write(bytes1, 0, bytes1.length);
                if (super.CloseDataSocket()) {
                    Response r = super.quickRead();
                    System.out.println("Closed: " + r.getResponse());
                }
                if (jmapiMemoryListener != null) {
                    jmapiMemoryListener.onJMAPISetMemory("Data was written", address);
                }
                setBinaryMode(false);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JMAPIException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (jmapiMemoryListener != null)
                jmapiMemoryListener.onJMAPIMemoryError("Data failed to read", process, address);
        } else {
            throw new JMAPIException("Not connected to network");
        }
    }


    protected Response getMinVersion(VERSION version) {
        String s = version + " GETMINVERSION";
        Response res = super.Send(s);
        if (this.listener != null) {
            this.listener.onJMAPIResponse(PS3OP.SCMINVERSION, res.getResponseCode(), res.getResponse());
        }
        return res;
    }

    public void buzzer(BUZZER buzz) throws JMAPIException {
        if (!isConnected()) {
            throw new JMAPIException("Not connected to host.");
        }
        String buzzer = "PS3 BUZZER" + String.valueOf(buzz.ordinal() + 1);
        Response r = super.Send(buzzer);
        if (this.listener != null) {
            this.listener.onJMAPIResponse(PS3OP.BUZZ, r.getResponseCode(), "A buzz was send to the ps3");
        }
        System.out.println(r.getResponse());
    }

    public void boot(PS3BOOT ps3boot) throws JMAPIException {
        if (!isConnected()) {
            throw new JMAPIException("Not connected to host.");
        }
        String boot = "PS3 " + ps3boot;
        Response res = super.Send(boot);
        if (this.listener != null) {
            switch (ps3boot) {
                case REBOOT:
                    this.listener.onJMAPIResponse(PS3OP.BOOT, res.getResponseCode(), "Rebooting console");
                    break;
                case SHUTDOWN:
                    this.listener.onJMAPIResponse(PS3OP.BOOT, res.getResponseCode(), "Shutting down console");
                    break;
                case HARDREBOOT:
                    this.listener.onJMAPIResponse(PS3OP.BOOT, res.getResponseCode(), "Hard Rebooting console");
                    break;
                case SOFTREBOOT:
                    this.listener.onJMAPIResponse(PS3OP.BOOT, res.getResponseCode(), "Soft rebooting console");
                    break;
            }
        }
        System.out.println(res.getResponse());
    }

    public void notify(String message) throws JMAPIException {
        if (!isConnected()) {
            throw new JMAPIException("Not connected to host.");
        }
        String notify = "PS3 NOTIFY " + message;
        Response res = super.Send(notify);
        System.out.println(res.getResponse());
    }

    public void changeLed(LEDCOLOR color, LEDMODE mode) throws JMAPIException {
        if (!isConnected()) {
            throw new JMAPIException("Not connected to host.");
        }
        String led = "PS3 LED " + String.valueOf(color.ordinal()) + " " + String.valueOf(mode.ordinal());
        Response res = super.Send(led);
        System.out.println(res.getResponseCode());
        if (this.listener != null) {
            this.listener.onJMAPIResponse(PS3OP.LED, res.getResponseCode(), res.getResponse());
        }
    }

    public String getIDPS() throws JMAPIException {
        if (!isConnected()) {
            throw new JMAPIException("Not connected to host.");
        }
        String idps = "PS3 GETIDPS";
        Response res = super.Send(idps);
        System.out.println(res.getResponseCode());
        if (this.listener != null) {
            this.listener.onJMAPIResponse(PS3OP.IDPS, res.getResponseCode(), res.getResponse());
        }
        return res.getResponse();
    }

    public String getPSID() throws JMAPIException {
        if (!isConnected()) {
            throw new JMAPIException("Not connected to host.");
        }
        String psid = "PS3 GETPSID";
        Response res = super.Send(psid);
        System.out.println(res.getResponseCode());
        if (this.listener != null) {
            this.listener.onJMAPIResponse(PS3OP.PSID, res.getResponseCode(), res.getResponse());
        }
        return res.getResponse();
    }

    public Response setIDPS(String idps) throws JMAPIException {
        if (!isConnected()) throw new JMAPIException("Not connected to host.");
        String idps_cmd = "PS3 SETIDPS " + idps.substring(0, 16) + " " + idps.substring(16);
        Response res = super.Send(idps_cmd);
        if (this.listener != null) {
            this.listener.onJMAPIResponse(PS3OP.IDPSSET, res.getResponseCode(), res.getResponse());
        }
        return res;
    }


    public Response setPSID(String psid) throws JMAPIException {
        if (!isConnected()) throw new JMAPIException("Not connected to host.");
        String psid_cmd = "PS3 SETPSID " + psid.substring(0, 16) + " " + psid.substring(16);
        Response res = super.Send(psid_cmd);
        if (this.listener != null) {
            this.listener.onJMAPIResponse(PS3OP.PSIDSET, res.getResponseCode(), res.getResponse());
        }
        return res;

    }

    public boolean disconnect() {
        if (!isConnected()) return true;
        try {
            super.Send("DISCONNECT");
            this.processList = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            super.Close();
            if (this.listener != null) {
                this.listener.onJMAPIResponse(PS3OP.DISCONNECTED, PS3MAPI_RESPONSECODE.COMMANDOK, "Disconnected from ps3");
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            if (this.listener != null) {
                this.listener.onJMAPIError("Something happened while disconnecting");
            }
            return false;
        }
    }

    public boolean isConnected() {
        if (super.client != null) {
            if (super.client.isConnected()) {
                return true;
            } else {
                isAttached = true;
                process = null;
                processList = null;
                return false;
            }
        } else {
            this.process = null;
            this.processList = null;
            this.isAttached = false;
            return false;
        }
    }

    public Temperature getTemp() throws JMAPIException {
        String psid = "PS3 GETTEMP";
        Response res = super.Send(psid);
        System.out.println(res.getResponse());
        if (!res.getResponse().contains(":")) {
            String[] temp = res.getResponse().split("\\|");
            if (this.listener != null) {
                this.listener.onJMAPITemperature(res.getResponseCode(), Temperature.instantiate(temp[0], temp[1]));
            }
            return Temperature.instantiate(temp[0], temp[1]);
        } else {
            throw new JMAPIException("Error: could not obtain temperature");
        }
    }

    public boolean scanNetwork() {
        try {
            this.ip = Network.getReachableHosts(Network.scanSubNets());
            if (ip == null) return false;
            this.connect();
            this.buzzer(BUZZER.DOUBLE);
            if (this.listener != null) {
                this.listener.onJMAPIResponse(PS3OP.NETWORK_FOUND, PS3MAPI_RESPONSECODE.COMMANDOK, this.ip);
            }
            return true;
        } catch (SocketException e) {
            e.printStackTrace();
            return false;
        } catch (JMAPIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    public boolean connect() {
        this.processList = null;
        return super.Connect(ip, port);
    }


}
