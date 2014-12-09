//package example;
//
//import android.app.Activity;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothSocket;
//import android.util.Log;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.math.BigInteger;
//import java.util.UUID;
//
///**
// * Created by ko10ok on 10.05.14.
// */
//public class BrickNXT{
//
//    public class NewDataListener implements Runnable {
//
//        int timeInterval;
//        boolean running;
//
//        public NewDataListener(int timeInterval){
//            this.timeInterval = timeInterval;
//            running = true;
//        }
//
//        @Override
//        public void run() {
//            while(running)
//            {
//                try {
//                    //TODO make receiveMessage and check for new mail. if (!empty) call
//                    onNewData();
//                    Thread.sleep(timeInterval);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        public void onNewData()
//        {
//
//        }
//    }
//
//    NewDataHandler newDataHandler;
//
//    BrickNXT connection;
//    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//
//    BluetoothDevice deviceNXT;
//    BluetoothSocket socketNXT;
//
//    OutputStream mOut;
//    InputStream mIn;
//    //InputStreamReader mInReader;
//    BufferedReader mInReader;
//    BufferedInputStream mBuffInStream;
//
//    public boolean isInitiated=false;
//
//    final String TAG="connector";
//    //final byte MASK_NOREPLY = (byte)0xff;
//    public static byte MASK_NOREPLY = (byte) 0x80;
//
//    String deviceName = null;
//
//    public boolean initiate(){
//
//        try {
//            socketNXT = deviceNXT.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
//            deviceName = deviceNXT.getName() + " | " + deviceNXT.getAddress();
//            socketNXT.connect();
//            mOut = socketNXT.getOutputStream();
//            mIn = socketNXT.getInputStream();
//
//            //mInReader = new BufferedReader( new InputStreamReader(mIn) );
//            mBuffInStream = new BufferedInputStream(mIn);
//
//
//            //set message reader tread
//            new Thread(new NewDataListener(100){
//                @Override
//                public void onNewData(){
//                    Log.d("msg","overrided listener");
//                    sendBTMessage(LCPMessage.getBeepMessage(1000,10));
//                    //TODO check on ansver;
//                }
//            }).start();
//
//            //TODO close device socket??
//            Log.i("conn", "connected");
//            return true;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public synchronized byte[] sendBTMessage(byte[] request) {
//        int lsb = request.length;
//        int msb = request.length >>> 8;
//        try {
//            mOut.write((byte) lsb);
//            mOut.write((byte) msb);
//            mOut.write(request);
//        } catch (IOException e) {
//            Log.e(TAG, "sendBTMessage: Write failed.");
//        }
//        if ((request[0] & MASK_NOREPLY) == 0) { // Reply requested
//            byte[] reply = readBTMessage();
//
//            Log.d("mail","mailbox:"+String.valueOf(reply[3])+" \\ size:" + String.valueOf(reply[4])+  " \\ status:" + String.valueOf(reply[2])+   "|" + String.valueOf(reply[5]) + String.valueOf(reply[6])+ String.valueOf(reply[7]));
//            //Log.d("mail","mailbox:"+ );
//
//            if (reply == null) {
//                return null;
//            }
//            if (reply[0] != 0x02) {
//                // Not a reply?
//                Log.e(TAG, "sendBTMessage read a message that was not a reply.");
//                Log.e(TAG, "Hex: " + toHex(reply.toString()));
//                return new byte[] {(byte) 0xFF};
//            }
//            if (reply[1] != request[1]) {
//                // Reply for incorrect request?
//                Log.e(TAG, "sendBTMessage received a reply for the wrong request.");
//                Log.e(TAG, "Hex: " + toHex(reply.toString()));
//                return new byte[] {(byte) 0xFF};
//            }
//            byte[] replymessage = new byte[reply.length - 2];
//            System.arraycopy(reply, 2, replymessage, 0, replymessage.length);
//            return replymessage;
//        }
//        return new byte[] {0};
//    }
//
//    public byte[] readBTMessage() {
//        byte[] buffer = new byte[66];  // All BT messages are a maximum of 66 bytes long
//        int numBytes;
//        try {
//            numBytes = mIn.read(buffer);
//        } catch (IOException e) {
//            Log.e(TAG, "readBTMessage: Read failed.");
//            return null;
//        }
//        if (numBytes <= 0) return null;
//        int msgLength = buffer[0] + (buffer[1] << 8);
//        if (numBytes != msgLength + 2) {
//            Log.e(TAG, "readBTMessage: BT Message wrong length.");
//            Log.e(TAG, "BT Message: " + toHex(buffer.toString()));
//            return null;
//        }
//        byte[] result = new byte[msgLength];
//        System.arraycopy(buffer, 2, result, 0, msgLength);
//        return result;
//    }
//
//    public boolean close(){
//        try {
//            socketNXT.close();
//            mOut=null;
//            mIn =null;
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public BrickNXT(BluetoothDevice deviceNXT){
//        this.deviceNXT=deviceNXT;
//    }
//
//    public String toHex(String arg) {
//        return String.format("%x", new BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
//    }
//
//    OutputStream getStream() throws IOException {
//        return this.socketNXT.getOutputStream();
//    }
//
//    public byte[] makeMassage(String mailbox, String message){
//
//        //Number of bytes in the packet excluding the initial 2 bytes (little endian).
//
//        int mailboxOffset = 7-2;
//        int messageOffset = mailboxOffset+mailbox.length()+1;
//
//        byte [] packet = new byte[messageOffset+2+message.length()];
//
//        //Message counter (little endian). This can be any value you wish.
//        packet[0]=(byte)1;
//        packet[1]=(byte)0;
//        //Command type being a system command with no reply (0x81 = 129).
//        packet[2]=(byte)129;
//        //System command number for WRITEMAILBOX (0x9E = 158).
//        packet[3]=(byte)158;
//
//        //Length of the mailbox name including zero termination character.
//        packet[4]=(byte)(mailbox.length()+1);
//
//        //Mailbox name that you want the message to be sent to e.g. this value corresponds to 'abc'
//        System.arraycopy(mailbox.getBytes(),0,packet,mailboxOffset,mailbox.length());
//        packet[mailboxOffset+mailbox.length()]=0;
//
//        packet[messageOffset] = (byte)message.length();
//        packet[messageOffset+1] = (byte)(message.length() >>> 8);
//
//        System.arraycopy(message.getBytes(),0,packet,messageOffset+2,message.length());
//        packet[messageOffset+1+message.length()]=0;
//
//        return packet;
//    }
//
//    public interface NewDataHandler {
//
//        public void onNewData();
//    }
//
//    public void setNewDataHandler(Activity handler)
//    {
//        newDataHandler = ((NewDataHandler) newDataHandler);
//    }
//
//    public void onNewData()
//    {
//        newDataHandler.onNewData();
//    }
//
//    public String getDeviceName() {
//        return deviceName;
//    }
//
//}
