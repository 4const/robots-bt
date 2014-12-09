//package example;
//
//import android.app.AlertDialog;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothSocket;
//import android.content.Context;
//import android.util.Log;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ListView;
//import android.widget.SimpleAdapter;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Set;
//
///**
// * Created by ko10ok on 10.05.14.
// */
//public class ConnectorNXT {
//
//    Context context;
//
//    //void makeConnection();
//    BluetoothAdapter mBluetoothAdapter;
//    //Enables Bluetooth if not enabled
//
//    BluetoothSocket socket;
//
//    ArrayList<BluetoothDevice> bounded;
//    AlertDialog dialog;
//
//    Connector connector;
//
//    BrickNXT connection = null;
//    boolean btDeviceSelected=false;
//
//    public ConnectorNXT(Context context){
//        this.context = context;
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (mBluetoothAdapter == null) {
//            // Device does not support Bluetooth
//        }
//        bounded = new ArrayList<BluetoothDevice>();
//
//        //TODO Make callback interface to activity for update connected device's infomation
//
//        try {
//            connector = (Connector) context;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(context.toString() + " must implement connector interface");
//        }
//
//    }
//
//    public boolean isAvailable(){
//        return mBluetoothAdapter.isEnabled();
//    }
//
//    public void enableBT(){
//
//    }
//
//    public void selectTargetDevice(Context context){
//        Set<BluetoothDevice> boundedSet = mBluetoothAdapter.getBondedDevices();
//
//        ArrayList devices = new ArrayList<HashMap<String, String>>();
//        HashMap<String,String> tmp;
//
//        for(BluetoothDevice v:boundedSet)
//        {
//            bounded.add(v);
//
//            tmp = new HashMap<String, String>();
//            tmp.put("Name",v.getName());
//            tmp.put("Address",v.getAddress());
//            devices.add(tmp);
//        }
//
//        SimpleAdapter adapter = new SimpleAdapter(
//                context,
//                devices,
//                android.R.layout.simple_list_item_2,
//                new String[] {"Name","Address"},
//                new int[] {android.R.id.text1, android.R.id.text2}
//        );
//
//        ListView bluetoothList = new ListView(context);
//        bluetoothList.setAdapter(adapter);
//        bluetoothList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                //Log.i("onitem",""+i+" | "+l+" | "+adapterView.getAdapter().getItem(i));
//                onItemSelect(i);
//            }
//        });
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//
//        builder.setView(bluetoothList);
//        builder.setTitle("Olololo");
//
//        dialog = builder.create();
//
//        dialog.show();
//    }
//
//    public boolean isSelected(){
//
//        return btDeviceSelected;
//    }
//
//    private void onItemSelect(int i){
//
//        // Set selected device
//        connection = new BrickNXT(bounded.get(i));
//        // set selected flag
//        btDeviceSelected = true;
//        Log.i("result",""+bounded.get(i).getName()+" | "+bounded.get(i).getAddress());
//        dialog.dismiss();
//        connector.onSelect(bounded.get(i).getName());
//    }
//
//    public void connect(){
//        if(!connection.isInitiated)
//            if(connection.initiate())
//                connector.onConnect();
//
//    }
//
//    private void disconnect(){
//        if(connection.isInitiated)
//            if(connection.close())
//                connector.onDisConnect();
//
//    }
//
//    public BrickNXT getConnection(){
//        return connection;
//    }
//
//    @Deprecated
//    public void waitForConnection(){
//
//        /*
//        BluetoothServerSocket srvSock;
//        BluetoothSocket socket = null;
//        //this.localAdapter;
//        Log.d("wait","ok");
//        try {
//            srvSock = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("MYAPP", MY_UUID_SECURE);
//
//            while (srvSock != null) {
//                try {
//                    Log.d("Str","socketwaiting");
//                    socket = srvSock.accept();
//                    Log.d("Str","socketAccepted");
//                    InputStream tr = socket.getInputStream();
//
//                    byte buf[] = new byte[1000];
//                    tr.read(buf);
//
//
//                    int i=0;
//                    Log.d("Str",""+String.valueOf(buf[0]));
//                    Log.d("Str",""+String.valueOf(buf[1]));
//                    Log.d("Str",""+String.valueOf(buf[2]));
//                    Log.d("Str",""+String.valueOf(buf[3]));
//
//
//                    Log.d("ints",""+String.valueOf(tr.read()));
//                    Log.d("ints1",""+String.valueOf(tr.read()));
//                    Log.d("ints2",""+String.valueOf(tr.read()));
//                    Log.d("ints3",""+String.valueOf(tr.read()));
//                    Log.d("ints4",""+String.valueOf(tr.read()));
//                    Log.d("ints5",""+String.valueOf(tr.read()));
//
//                    //changeT("listening");
//
//                } catch (IOException e) {
//                    break;
//                }
//                if (socket != null) {
//                    //changeT("doneeeee");
//                    try {
//                        srvSock.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                }
//            }
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        */
//        //BluetoothSocket connSocket = ;
//        //connSocket.wait();
//    }
//
//    int testNumber =0;
//    public void test() {
//        Log.d("test","start");
//        /*
//        for(int i=0;i<21;i++){
//            connection.sendBTMessage(LCPMessage.getReadMessage(i,true));
//        }*/
//
//        connection.sendBTMessage(LCPMessage.getBeepMessage(100,50));
//        connection.sendBTMessage(LCPMessage.getBeepMessage(200,50));
//        connection.sendBTMessage(LCPMessage.getBeepMessage(400,50));
//        connection.sendBTMessage(LCPMessage.getBeepMessage(800,50));
//        connection.sendBTMessage(LCPMessage.getBeepMessage(1600,50));
//        connection.sendBTMessage(LCPMessage.getBeepMessage(3200,50));
//        connection.sendBTMessage(LCPMessage.getBeepMessage(6400,50));
//
//
//
//        //String result = new String( connection.sendBTMessage(LCPMessage.getReadMessage(0,true)));
//        //byte t[]={'a','b','c','a','b','c','a','b','c','\0'};
//        //byte t1[]={(byte)1,(byte)0,(byte)129,(byte)158,(byte)4,(byte)97,(byte)98,(byte)99,(byte)0,(byte)7,(byte)0 ,(byte)104 ,(byte)101,(byte)108,(byte)108 ,(byte)111 ,(byte)33 ,(byte)0};
//
//        //Log.d("asd",String.valueOf(t1));
//        //Log.d("asd",String.valueOf(connection.makeMassage("abc", "hello!")));
//
//        //connection.sendBTMessage(connection.makeMassage("abc", "hello!" + String.valueOf(testNumber)));
//
//        //testNumber++;
//
//        //Log.d("message",result);
//        Log.d("test","ended");
//        return;
//
//    }
//
//    public interface Connector{
//        public void onConnect();
//        public void onDisConnect();
//        public void onSelect(String deviceName);
//    }
//}