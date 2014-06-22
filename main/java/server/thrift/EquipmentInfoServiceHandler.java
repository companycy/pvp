package server.thrift;

import equipmentinfo.XEquipmentInfoService;
import equipmentinfo.XEquipmentInfoThrift;
import org.apache.thrift.TException;

import java.util.*;

/**
 * Created by bjcheny on 6/11/14.
 */
public class EquipmentInfoServiceHandler implements XEquipmentInfoService.Iface {
    @Override
    public List<XEquipmentInfoThrift> GetEquipedEquipments(int playerId) throws org.apache.thrift.TException {
        XEquipmentInfoThrift equipmentInfoThrift = new XEquipmentInfoThrift();
        equipmentInfoThrift.itemId = 10;
        equipmentInfoThrift.displayName = "NB!!";
        ArrayList<XEquipmentInfoThrift> list = new ArrayList<XEquipmentInfoThrift>();
        list.add(equipmentInfoThrift);
        return list;
    }
}
