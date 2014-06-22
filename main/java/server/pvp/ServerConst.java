package server.pvp;

/**
 * Created by bjcheny on 6/13/14.
 */
public class ServerConst {
  // length const
  public static final int headerLen = 7;
  public static final int msgTypeOffset = 2;
  public static final int clientTimestampOffset = 3;
  public static final int userIdLength = 4;
  public static final int userIdInRoomLength = 1;
  public static final int statusLength = 1;
  public static final int roomIdLength = 4;
  public static final int roomMaxplayerLength = 1;
  public static final int roomCurrentplayersLength = 1;
  public static final int separatorLength = 1;
  public static final int friendscntLength = 1;
  public static final int sessionIdLength = 4;
  public static final int userGamestateLength = 1;
  public static final int timestampLength = 4;
  public static final int buffIdLength = 1;
  public static final int buffValueLength = 4;
  public static final int npcIdLength = 4;
  public static final int positionXLength = 2;
  public static final int positionYLength = 2;
  public static final int positionZLength = 2;
  public static final int rotationLength = 2;
  public static final int hpLength = 4;
  public static final int mpLength = 4;
  public static final int buffToAddLength = 1+1+4;
  public static final int buffToApplyLength = 1+4;
  public static final int buffTypeLength = 1;
  public static final int charIdLength = 1;
  public static final int joystickXLength = 1;
  public static final int joystickZLength = 1;
  public static final int skillIdLength = 1;
  public static final int triggerNumberLength = 1;
  public static final int fieldLength = 1;
  public static final int charCntLength = 1;
  public static final int spawnPointLength = 1;

  // field in status_request
  public static final int JOYSTICK = 0x01;
  public static final int POSITION_LIST = 0x02;
  public static final int ROTATION_LIST = 0x04;
  public static final int STATUS_LIST = 0x08;  
  public static final int STARTEDSKILLID_LIST = 0x10;
  public static final int TRIGGEREDSKILL_LIST = 0x20;

  // field in response of status_request
  public static final int MAIN_POSITION = 0x01;
  public static final int MAIN_ROTATION = 0x02;
  public static final int MAIN_STATUS = 0x04;
  public static final int MAIN_STARTED_SKILLID = 0x08;
  public static final int MAIN_HP = 0x10;
  public static final int MAIN_MP = 0x20;
  public static final int MAIN_BUFF_LIST = 0x40;
  public static final int MAIN_APPLY_BUFF_LIST = 0x80;

  public static final int RIVAL_JOYSTICKPOS = 0x01;
   

  // character state 
  public static final int POSITION = 0;
  public static final int ROTATION = 1;
  public static final int JOYSTICKPOS = 2;
  public static final int CHARSTATUS = 3;
  public static final int STARTEDSKILLID = 4;
  public static final int TRIGGEREDSKILL = 5;
  public static final int APPLIEDBUFF = 6;
  public static final int REMOVEDBUFF = 7;

  public static final int REQUEST_NTP = 2;
  public static final int RESPONSE_NTP = 3;
  
  public static final int REQUEST_LEAVELOBBY = 4;

  public static final int REQUEST_STATUS =  5;
  public static final int RESPONSE_STATUS =  6;
  
  public static final int REQUEST_AUTH =  7;
  public static final int RESPONSE_AUTH = 8;
  
  public static final int REQUEST_ROOMLIST =  100;
  public static final int RESPONSE_ROOMLIST = 101;
  
  public static final int REQUEST_CREATEROOM =  102;
  public static final int RESPONSE_CREATEROOM = 103;
  
  public static final int REQUEST_JOINROOM =  104;
  public static final int RESPONSE_JOINROOM = 105;
  
  public static final int BROADCAST_PLAYER_JOIN =   106;
  public static final int BROADCAST_PLAYER_LEAVE =  107;
  
  public static final int REQUEST_GETREADY =  108;
  public static final int RESPONSE_GETREADY = 109;
  
  public static final int REQUEST_STARTGAME =  110;
  public static final int RESPONSE_STARTGAME = 111;
  
  public static final int REQUEST_LEAVEROOM =  112;
  public static final int RESPONSE_LEAVEROOM = 113;
  
  public static final int REQUEST_CANCEL_READY =  114;
  public static final int RESPONSE_CANCEL_READY = 115;
  
  public static final int BROADCAST_GETREADY =    116;
  public static final int BROADCAST_CANCELREADY = 117;
  
  public static final int BROADCAST_STARTGAME = 118;
  public static final int BROADCAST_QUITGAME =  119;
  
  public static final int REQUEST_QUITGAME =  120;
  public static final int RESPONSE_QUITGAME = 121;
  
  public static final int BROADCAST_HOST_LEAVE =  122;
  
  public static final int REQUEST_GETMATCHER =  123;
  public static final int RESPONSE_GETMATCHER = 124;
  
  public static final int REQUEST_ADDBUFF =  125;
  public static final int RESPONSE_ADDBUFF = 126;
  
  public static final int REQUEST_DEBUFF =  127;
  public static final int RESPONSE_DEBUFF = 128;
  
  public static final int BROADCAST_ADDBUFF = 129;
  public static final int BROADCAST_DEBUFF = 130;
  
  public static final int BROADCAST_TRIGGERBUFF =  131;
  public static final int REPORT_CHARACTERINFO =  132;
  
}
