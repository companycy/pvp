package server.thrift;

import equipmentinfo.XEquipmentInfoThrift;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.List;


/**
 * Created by bjcheny on 6/11/14.
 */
public class ThriftClient {
    /*
    public static void run(String[] args) {
        TTransport transport = new TSocket("localhost", 9090);
        try {
            transport.open();

            TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
            EquipmentInfoService.Client client = new EquipmentInfoService.Client(protocol);
            perform(client);
        } catch (TTransportException e) {
            e.printStackTrace();
        } finally {
            transport.close();
        }
    }

    private static void perform(EquipmentService.Client client) {
        List<XEquipmentInfoThrift> str = null;
        try {
            str = client.EquipedEquipments(0);
        } catch (TException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(str);
    }
    */
}
