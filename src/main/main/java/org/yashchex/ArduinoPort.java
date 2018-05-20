package org.yashchex;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mmarashan
 */

public class ArduinoPort implements SerialPortEventListener {

    private static final String PORT_NAMES[] = {/* "/dev/tty.usbserial-A9007UX1", // Mac*/
            //"/dev/ttyUSB0", "/dev/ttyACM0" // Linux
            //"/dev/arduino", // Use UDEV rules to get this thing*/
            "COM16", "COM4", "COM11", "COM9", "COM8", "COM7", "COM6", "COM5"};
    /**
     * Время блокировки до открытия порта (мсек)
     */
    private static final int TIME_OUT = 100;
    /**
     * Скорость передачи бит/сек с COM port. 38400 рабочая скорость!!! java не успевает обрабатывать
     */
    private static final int DATA_RATE = 9600;
    SerialPort serialPort;
    //regexp для парсинга
    Pattern pattern = Pattern.compile("(\\w+):(\\w+)");
    private String state = "SLEEP";
    private InputStream input;
    private OutputStream output;
    private int secondCommaPosition; // текущая позиция следующей запятой
    private volatile String data = new String(); // данные на входе COM - порта
    //private static Sender blockChainSender = new Sender("192.168.1.227", 50051);
    private List<State> stateHistory = new ArrayList<State>();
    private volatile int getCounter = 0;

    public void initialize() {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            for (String portName : PORT_NAMES) {
                if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    break;
                }
            }
        }

        if (portId == null) {
            System.out.println("Could not find COM port.");
            return;
        }

        try {
            // открыть serial port, и использовать для appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

            // установить параметры порта
            serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // открываем входящий и исходящий потоки
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();

            // добавить слушателей событий
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);

            state = "RUN";
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /**
     * Вызывается при остановке порта. Это предотвратит блокировку порта
     */
    public synchronized void close() {
        if (state == "RUN") {
            if (serialPort != null) {
                serialPort.removeEventListener();
                serialPort.close();
                state = "SLEEP";
            }
        }
    }

    /**
     * Считывание событий на последовательном порту. Считывание информации и
     * распечатка.
     */
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                int available = input.available();
                byte chunk[] = new byte[input.available()];
                input.read(chunk, 0, available);
                addRation(new String(chunk));

            } catch (Exception e) {
                System.err.println(e.toString());
                close();
            }
        }
    }

    public void sendSingleByte(byte myByte) {
        try {
            output.write(myByte);
            output.flush();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    public void sendBytes(byte[] myBytes) {
        try {
            output.write(myBytes);
            output.flush();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /* добавить порцию в строку data */
    public synchronized void addRation(String input) {
        data += input;
        secondCommaPosition = 0;
        String key;
        String value;
        try {
            if (data.indexOf(',') > 0) {
                secondCommaPosition = data.indexOf(',');

                Matcher matcher = pattern.matcher(data.substring(0, secondCommaPosition));
                if (matcher.find()) {
                    key = new String(matcher.group(1));
                    value = new String(matcher.group(2));
                    if (key.equals("state")) { //печатаем только значения, пока без id
                        this.stateHistory.add(new State(value, new Date()));
                    }
                }
                data = data.substring(secondCommaPosition + 1);
            }
        } catch (Exception e) {
            System.out.println(
                    e.toString() + " newCursorBegin,  data.length : " + secondCommaPosition + ",  " + data.length());
        }

    }

    public List<State> getLastUnviewStates() {
        List<State> l = this.stateHistory.subList(getCounter, this.stateHistory.size());
        getCounter = this.stateHistory.size();
        return l;
    }

    public State getLastState() {
        getCounter = this.stateHistory.size();
        return this.stateHistory.get(this.stateHistory.size());
    }

    public void unlock() {
        byte[] b = "unlock".getBytes();
        sendBytes(b);
    }

    public void lock() {
        byte[] b = "lock".getBytes();
        sendBytes(b);
    }


}