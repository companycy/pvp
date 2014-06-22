package server.thrift;
/**
 * Created by bjcheny on 6/11/14.
 */


import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import equipmentinfo.XEquipmentInfoService;

public class ThriftServer {

  public static EquipmentInfoServiceHandler handler;
  public static XEquipmentInfoService.Processor processor;
  public static int port = 9090;

  public static void run() {
    try {
      handler = new EquipmentInfoServiceHandler();
      processor = new XEquipmentInfoService.Processor(handler);

      // Runnable threadPool = () -> {threadPool(processor);};
      Runnable threadPool = new Runnable() {
          public void run() {
            threadPool(processor);
          }
        };
      new Thread(threadPool).start();
    } catch (Exception x) {
      x.printStackTrace();
    }
  }

  public static void threadPool(XEquipmentInfoService.Processor processor) {
    TServerTransport trans = null;
    try {
      trans = new TServerSocket(port);
    } catch (Exception e) {
      e.printStackTrace();
    }

    TThreadPoolServer.Args args = new TThreadPoolServer.Args(trans);
    args.protocolFactory(new TBinaryProtocol.Factory());
    args.processor(processor);
    // args.maxWorkerThreads = 10000;
    TServer server = new TThreadPoolServer(args);

    System.out.print("Starting server...");
    server.serve();
  }
}

