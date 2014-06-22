
(function() {
  'use strict';

  // built-in modules
  var net = require('net');
  var events = require('events');

  // additional module
  var async = require('async');
  // var kue = require('kue');
  // var jobs = kue.createQueue();

  // private module
  var Msg = require('./msg').Msg;
  var NTP_MSG = Msg.NTP_MSG;
  var AUTH_MSG = Msg.AUTH_MSG;
  var PVP_MSG = Msg.PVP_MSG;
  var FRIEND_MSG = Msg.FRIEND_MSG;

  var Const = require('./const').Const;
  var GAME_STATE = Const.GAME_STATE;
  var headerLen = Const.CONST_LENGTH.headerLen;
  var msgTypeOffset = Const.CONST_LENGTH.msgTypeOffset;
  var clientTimestampOffset = Const.CONST_LENGTH.clientTimestampOffset;
  var userIdLength = Const.CONST_LENGTH.userIdLength;
  var statusLength = Const.CONST_LENGTH.statusLength;
  var roomIdLength = Const.CONST_LENGTH.roomIdLength;
  var roomMaxplayerLength = Const.CONST_LENGTH.roomMaxplayerLength;
  var roomCurrentplayersLength = Const.CONST_LENGTH.roomCurrentplayersLength;
  var separatorLength = Const.CONST_LENGTH.separatorLength;
  var friendscntLength = Const.CONST_LENGTH.friendscntLength;
  var sessionIdLength = Const.CONST_LENGTH.sessionIdLength;
  var userGamestateLength = Const.CONST_LENGTH.userGamestateLength;
  var timestampLength = Const.CONST_LENGTH.timestampLength;
  var buffIdLength = Const.CONST_LENGTH.buffIdLength;
  var buffValueLength = Const.CONST_LENGTH.buffValueLength;
  var npcIdLength = Const.CONST_LENGTH.npcIdLength;
  var positionXLength = Const.CONST_LENGTH.positionXLength;
  var positionYLength = Const.CONST_LENGTH.positionYLength;
  var positionZLength = Const.CONST_LENGTH.positionZLength;
  var hpLength = Const.CONST_LENGTH.hpLength;
  var mpLength = Const.CONST_LENGTH.mpLength;
  var buffLength = Const.CONST_LENGTH.buffLength;
  var charIdLength = Const.CONST_LENGTH.charIdLength;
  var rotationLength = Const.CONST_LENGTH.rotationLength;
  var skillIdLength = Const.CONST_LENGTH.skillIdLength;
  var joystickXLength = Const.CONST_LENGTH.joystickXLength;
  var joystickZLength = Const.CONST_LENGTH.joystickZLength;
  var triggerNumberLength = Const.CONST_LENGTH.triggerNumberLength;
  var fieldLength = Const.CONST_LENGTH.fieldLength;
  var charCntLength = Const.CONST_LENGTH.charCntLength;
  var spawnPointLength = Const.CONST_LENGTH.spawnPointLength;
  var numLength = Const.CONST_LENGTH.numLength;

  var POSITION = Const.CHARACTER_STATE.POSITION;
  var ROTATION = Const.CHARACTER_STATE.ROTATION;
  var JOYSTICKPOS = Const.CHARACTER_STATE.JOYSTICKPOS;
  var CHARSTATUS = Const.CHARACTER_STATE.CHARSTATUS;
  var STARTEDSKILLID = Const.CHARACTER_STATE.STARTEDSKILLID;
  var TRIGGEREDSKILL = Const.CHARACTER_STATE.TRIGGEREDSKILL;
  var APPLIEDBUFF = Const.CHARACTER_STATE.APPLIEDBUFF;
  var REMOVEDBUFF = Const.CHARACTER_STATE.REMOVEDBUFF;

  var REQUEST_FIELDBIT = Const.REQUEST_FIELDBIT;
  var RESPONSE_FIELD1BIT = Const.RESPONSE_FIELD1BIT;
  var RESPONSE_FIELD2BIT = Const.RESPONSE_FIELD2BIT;

  var Util = require('./util').Util;

  var BufferUtil = require('./bufferutil').BufferUtil;
  var Lobby = require('./lobby').Lobby;
  var Room = require('./room').Room;
  var Buff = require('./buff').Buff;

  var Character = require('./character').Character;
  var Player = require('./player').Player;
  var Statemachine = require('./statemachine').Statemachine;

  var redis = require("redis");
  var pubRedisclient = require('./redisclient').pubRedisclient;
  var channel = 'room:request';

  var bunyanlog = require('./logger').bunyanlog;

  var socketCnt = 0;             // +/- 9007199254740992, count of sockets
  var socketArr = [];            // just for storage, and there can be hole here
  var useridSocketMap = {};
  var roomTimestamp = 0;
  var lobby = Lobby.createNew();
  var eventEmitter = new events.EventEmitter();

  function initSubRedisclient (useridSocketMap) {  //  initSubRedisclient
    var subRedisclient = redis.createClient(null, null, {
      'return_buffers': true
    });

    subRedisclient.on('ready', function(err) {
      console.log('sub ready!');
      subRedisclient.subscribe('room:response');
    });

    subRedisclient.on('connect', function(err) {
      console.log('sub connect!');
    });

    subRedisclient.on('end', function(err) {
      console.log('sub end!');
    });

    subRedisclient.on('drain', function() {
      console.log('sub drain!');
    });

    subRedisclient.on('error', function (err) {
      console.log('sub error event - ' + subRedisclient.host + ':' + subRedisclient.port + ' - ' + err);
      // subRedisclient = null;           // nullify it
      // subRedisclient = redis.createClient(null, null, {
      //   'detect_buffers': true
      // });
    });

    subRedisclient.on("message", function (channel, message) {
      var offset = 0;
      var userId = message.readUInt32LE(offset); // CAREFULL!!! java use BE by default
      offset += userIdLength;
      // console.log("subclient channel ", channel, message.length); //  + " : " + message, message.length);

      if (userId) {
        // console.log(userId);
        var socket = useridSocketMap[userId];
        var buf = new Buffer(message.length - offset);
        if (buf && socket) {
          // buf.copy(targetBuffer, [targetStart], [sourceStart], [sourceEnd])
          message.copy(buf, 0, offset);

          offset = 0
          var header1 = buf.readUInt8(offset++)
          var header2 = buf.readUInt8(offset++)
          if (header1 === 0xef && header2 === 0xfe) { // do nothing
          } else {
            bunyanlog.info('header invalid ' + header1 + ' ' + header2);
          }

          var msgType = buf.readUInt8(msgTypeOffset);
          switch (msgType) {
          case PVP_MSG.RESPONSE_CREATEROOM: {
            // offset = headerLen;
            // var status = buf.readUInt8(offset);
            // offset += statusLength;
            // var roomId = buf.readUInt32LE(offset);
            // offset += roomIdLength;
            // var charId = buf.readUInt8(offset);
            // offset += charIdLength;
            // console.log(msgType, status, roomId, charId);
            break;
          }
          case PVP_MSG.BROADCAST_STARTGAME: {
            // console.log('broadcast start game');
            break;
          }
          case PVP_MSG.RESPONSE_STATUS: {
            // console.log('response to status');
            break;
          }
          default: {
            console.log(msgType);
            break;
          }
          }
          socket.write(buf);
        }                       // end of buf && socket
      }                         // end of userId
    });

    subRedisclient.on("subscribe", function (channel, count) {
      console.log('sub subscribe!');
    });
  }

  initSubRedisclient(useridSocketMap);


  // Start a TCP Server
  var server = net.createServer(function (socket) {


    if (!socket) {
      console.log('socket is invalid!!!');
      return;
    } else {
      initSocket(socket);
      // socket.setTimeout(2*60*1000);
    }

    socket.id = ++socketCnt;
    socketArr[socket.id] = socket;
    if (!roomTimestamp)         // todo: associated with room
      roomTimestamp = Date.now();

    console.log('new client connected ' + socket.remoteAddress
                + ' length: ' + socketCnt);

    socket.on('error', function(e) {
      // todo: error info, may reconnect after close
      // and if still fails, remove it from socketArr list
      console.log('socket error ' + e + ' addr: ' + socket.remoteAddress);
    });

    socket.on('close', function(e) {
      // will close after end
      console.log('socket closed ', socket.userId); //
      // resetSocket(socket, socketArr);
      forceToLeaveRoom(socket, lobby);
      // --socketCnt;
    });

    socket.on('end', function (e) {
      // console.log('socket end ' + e + ' addr: ' + socket.remoteAddress);
    });

    socket.on('drain', function (e) {
      bunyanlog.info('socket drain ' + e + ' addr: ' + socket.remoteAddress);
    });

    socket.on('timeout', function (e) {
      bunyanlog.info('socket timeout ' + e + ' addr: ' + socket.remoteAddress);
    });

    // Handle incoming messages from socketArr.
    socket.on('data', function(recvedData) {
      var header1 = recvedData.readUInt8(0);
      var header2 = recvedData.readUInt8(1);
      if (header1 === 0xef && header2 === 0xfe) {
      } else {
        bunyanlog.info('header invalid ' + header1 + ' ' + header2);
        return;
      }

      var eventData = {
        'roomTimestamp': roomTimestamp,
        'socketCnt': socketCnt,
        'socketArr': socketArr,
        'socket': socket,
        'lobby': lobby,
        'recvedData': recvedData,
        'useridSocketMap': useridSocketMap,
      };

      var msgType = recvedData.readUInt8(msgTypeOffset);
      eventEmitter.emit(msgType, eventData);
    });                         // end of on 'data'
  });                           // end of createServer

  server.on('connection', function(socket) {
    // console.log('connected on ' + Date.now());
  });

  server.on('error', function (e) {
    if (e.code == 'EADDRINUSE') {
      console.log('Address in use, retrying...');
      setTimeout(function () {
        server.close();
        server.listen(PORT, HOST);
      }, 1000);
    }
  });




  eventEmitter.on(AUTH_MSG.REQUEST_AUTH, function(eventData) {auth(eventData);});
  eventEmitter.on(NTP_MSG.REQUEST_NTP, function(eventData) {syncClock(eventData);});

  eventEmitter.on(PVP_MSG.REQUEST_ROOMLIST, function(eventData) {getRoomlist(eventData);});
  eventEmitter.on(PVP_MSG.REQUEST_CREATEROOM, function(eventData) {createRoom(eventData);});
  eventEmitter.on(PVP_MSG.REQUEST_JOINROOM, function(eventData) {joinRoom(eventData);});
  eventEmitter.on(PVP_MSG.REQUEST_LEAVEROOM, function(eventData) {leaveRoom(eventData);});
  eventEmitter.on(PVP_MSG.REQUEST_GETREADY, function(eventData) {getReady(eventData);});
  eventEmitter.on(PVP_MSG.REQUEST_CANCEL_READY,function(eventData) {cancelReady(eventData);});
  eventEmitter.on(PVP_MSG.REQUEST_STARTGAME, function(eventData) {startGame(eventData);});
  eventEmitter.on(PVP_MSG.REQUEST_QUITGAME, function(eventData) {quitGame(eventData);});
  eventEmitter.on(PVP_MSG.REQUEST_STATUS, function(eventData) {broadcastStatus(eventData);});
  eventEmitter.on(PVP_MSG.REQUEST_GETMATCHER, function(eventData) {getMatcher(eventData);});

  // eventEmitter.on(PVP_MSG.REQUEST_ADDBUFF, function(eventData) {addBuff(eventData);});
  // eventEmitter.on(PVP_MSG.REQUEST_DEBUFF, function(eventData) {deBuff(eventData);});

  // just for test
  eventEmitter.on(PVP_MSG.REPORT_CHARACTERINFO, function(eventData) {addNpc(eventData);});

  eventEmitter.on(FRIEND_MSG.REQUEST_FRIENDSLIST, function(eventData) {getFriendlist(eventData);});
  eventEmitter.on(FRIEND_MSG.REQUEST_SEARCHPLAYER, function(eventData) {searchPlayer(eventData);});
  eventEmitter.on(FRIEND_MSG.REQUEST_ADDFRIEND, function(eventData) {addFriend(eventData);});
  eventEmitter.on(FRIEND_MSG.REQUEST_DELETEFRIEND, function(eventData) {delFriend(eventData);});
  eventEmitter.on(FRIEND_MSG.REQUEST_SENDMSG, function(eventData) {sendTxtmsg(eventData);});
  eventEmitter.on(FRIEND_MSG.REQUEST_FOLLOWFRIEND, function(eventData) {followFriend(eventData);});
  
  function forceToLeaveRoom(socket, lobby, useridSocketMap) {
    var recvedData = new Buffer(headerLen);
    BufferUtil.writeMsgTypeAndTimestamp(recvedData,
                                        PVP_MSG.REQUEST_LEAVELOBBY,
                                        Date.now());
    pubDataToRedis(socket, recvedData); // only fill in header
    return;

    // var room = Lobby.getRoomFromList(lobby, roomId);
    // if (room && room.hostCharId === socket.player.id) {
    //   broadcastHostLeaveroom(socket, useridSocketMap, room);
    //   Room.popFromPlayerlist(room, socket);
    //   for (var i = 0; i < room.charInfoArr.length; i++) {
    //     var playerId = room.charInfoArr[i] || room.charInfoArr[i].userId;
    //     if (playerId && useridSocketMap[playerId]) {
    //       // var playerSocket = useridSocketMap[playerId];
    //       // playerSocket.gamestate =
    //       //   Statemachine.consumeEvent(playerSocket.gamestate,
    //       //                             GAME_STATE.LEAVEROOM);

    //       // transfer host to this player
    //       room.hostCharId = useridSocketMap[playerId].charId;
    //       break;
    //     }
    //   }
    // } else {
    //   broadcastPlayerLeaveroom(socket, useridSocketMap, room);
    // }

    // // necessary for broadcast
    // socket.player.id = 0;
  }

  function addNpc(eventData) {
    var recvedData = eventData.recvedData;
    var socket = eventData.socket;

    var offset = headerLen;
    var npcId = recvedData.readUInt32LE(offset);
    offset += npcIdLength;

    var status = 0;
    do {
      var room = null;
      var roomId = socket.roomId;
      if (!roomId || typeof roomId === 'undefined') {
        var roomName = 'test_addbuf';
        var maxPlayers = 2;
        if (!socket.userId || typeof socket.userId === 'undefined') {
          socket.userId = 888888;
        }
        room = Lobby.getNewRoom(lobby, socket, roomName, maxPlayers);
        if (!room) {
          bunyanlog.info('invalid room');
          status = 1;
          break;
        }
      } else {
        room = Lobby.getRoomFromList(lobby, roomId);
      }
    } while (false);

    if (room) {
      Room.pushToNpcIdlist(npcId);
    } else {
      console.log('failed to push npcid to room npcArr');
    }
  }

  function getMatcher(eventData) {
    // go through all active/free sockets in this useridSocketMap
    // figure out the low ping and close levels
    var lowpingSocketArr = [];
    for (var i = 0; i < useridSocketMap.length; i++) {
      var playerSocket = useridSocketMap[i];
      if (playerSocket) {
      }
    }

    var bestMatcher = null;
    for (i = 0; i < lowpingSocketArr.length; i++) {
      playerSocket = lowpingSocketArr[i];
      if (playerSocket && playerSocket.userId) {
      }
    }

    return bestMatcher;
  }

  function addBuff(room, sender, recvedSkill) {
    // var msgType = PVP_MSG.RESPONSE_ADDBUFF;
    // addOrDeBuff(eventData, msgType);
    for (var i = 0; i < recvedSkill.length; i++) {
      if (recvedSkill[i]) {
        var targetId = recvedSkill[i].targetId;
        var target = null;
        if (Room.isNpcId(targetId)) {
          target = Room.getNpcFromlist(targetId);
        } else {
          target = Room.getPlayerFromlist(targetId);
        }

        // var buffNum = recvedSkill[i].buffNum;
        var buffToAdd = recvedSkill[i].buffToAdd;
        for (var j = 0; j < buffToAdd.length; j++) {
          if (buffToAdd[j]) {
            var buffId = buffToAdd[j].buffId;
            var buffValue = buffToAdd[j].buffValue;
            Character.addBuff(target, buffId, buffValue);
          }                     // end of if buffToAdd
        }                       // end of for buffToAdd
      }                         // end of if recvedSkill
    }                           // end of for recvedSkill
  }

  function followFriend(eventData) {
    var socket = eventData.socket;
    var recvedData = eventData.recvedData;

    var offset = headerLen;
    var friendId = recvedData.readUInt32LE(offset);
    offset += userIdLength;

    var status = 0;
    do {

    } while (false);

    var buf = null;
    if (status === 0) {
      var roomName = 'name_';
      var roomInfo = 'info_';
      var hostName = 'hostname_';
      var roomDataLength = roomIdLength
        + roomCurrentplayersLength
        + roomMaxplayerLength
        + roomName.length + separatorLength
        + roomInfo.length + separatorLength
        + userIdLength
        + hostName.length + separatorLength;
      var playerDataLength = userIdLength
        + hostName.length + separatorLength;
      buf = new Buffer(dataLength
                       + statusLength
                       + roomDataLength
                       + playerDataLength);
    } else {
      buf = new Buffer(dataLength + statusLength);
    }
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        RESPONSE_FOLLOWFRIEND,
                                        socket.authTimestamp);
    offset = dataLength;
    buf.writeUInt8(status, offset++);
    socket.write(buf);
  }

  function getFriendlist(eventData) {
    var socket = eventData.socket;
    var userId = socket.userId;
    var status = 0;
    do {
    } while (false);

    var buf = null;
    if (status === 0) {
      var dataLength = 0;
      var friendsCnt = 0;
      buf = new Buffer(headerLen
                       + statusLength
                       + friendscntLength
                       + dataLength);
    } else {
      buf = new Buffer(headerLen
                       + statusLength);
    }
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        FRIEND_MSG.RESPONSE_FRIENDSLIST,
                                        socket.authTimestamp);
    var offset = headerLen;
    buf.writeUInt8(status, offset++);
    buf.writeUInt8(friendsCnt, offset++);
    socket.write(buf);
  }

  function searchOrAddPlayer(socket, recvedData, msgType) {
    if (!Util.testArguments(arguments)) {
      return;
    }

    var status = 0;
    var offset = headerLen;
    var playerId = recvedData.readUInt32LE(offset);
    offset += userIdLength;
    do {
    } while (false);

    var buf = null;
    if (status === 0) {
      var dataLength = 0;
      buf = new Buffer(headerLen
                       + statusLength
                       + userIdLength
                       + dataLength);
    } else {
      buf = new Buffer(headerLen
                       + statusLength
                       + userIdLength);
    }
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        msgType,
                                        socket.authTimestamp);
    var offset = headerLen;
    buf.writeUInt8(status, offset++);
    buf.writeUInt32LE(playerId, offset);
    offset += userIdLength;

    socket.write(buf);
  }

  function searchPlayer(eventData) {
    var socket = eventData.socket;
    var recvedData = eventData.recvedData;
    var msgType = FRIEND_MSG.RESPONSE_SEARCHPLAYER;
    searchOrAddPlayer(socket, recvedData, msgType);
  }

  function addFriend(eventData) {
    var socket = eventData.socket;
    var recvedData = eventData.recvedData;
    var msgType = FRIEND_MSG.RESPONSE_ADDFRIEND;
    searchOrAddPlayer(socket, recvedData, msgType);
  }

  function delFriend(eventData) {
    var socket = eventData.socket;
    var recvedData = eventData.recvedData;

    var status = 0;
    var offset = headerLen;
    var playerId = recvedData.readUInt32LE(offset);
    offset += userIdLength;
    do {
    } while (false);

    var buf = new Buffer(headerLen
                         + statusLength
                         + userIdLength);
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        FRIEND_MSG.RESPONSE_ADDFRIEND,
                                        socket.authTimestamp);
    var offset = headerLen;
    buf.writeUInt8(status, offset++);
    buf.writeUInt32LE(playerId, offset);
    offset += userIdLength;

    socket.write(buf);
  }

  function sendTxtmsg(eventData) {
    var socket = eventData.socket;
    var recvedData = eventData.recvedData;

    var status = 0;
    var offset = headerLen;
    var playerId = recvedData.readUInt32LE(offset);
    offset += userIdLength;
    do {
      var txtMsg = recvedData.toString('utf8', offset, recvedData.length-1);
      bunyanlog.info(txtMsg);
    } while (false);

    var buf = new Buffer(headerLen
                         + statusLength);
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        FRIEND_MSG.RESPONSE_SENDMSG,
                                        0); // record as the abs time
    var offset = headerLen;
    buf.writeUInt8(status, offset++);
    socket.write(buf);
  }


  function broadcastReadystatus(socket, useridSocketMap, room, readyOrNot) {
    // readStatus can be 0
    // if (!Util.testArguments(arguments)) {
    //   return;
    // }

    var buf = new Buffer(headerLen
                         + charIdLength
                         + statusLength // room ready status
                         + statusLength // user ready status
                        );
    var roomReady = null;
    var msgType = null;
    if (readyOrNot === 1) {
      msgType = PVP_MSG.BROADCAST_CANCELREADY;
      roomReady = 1;
    } else {
      msgType = PVP_MSG.BROADCAST_GETREADY;

      // todo: need to check if all in room are ready or not
      roomReady = 0;
    }
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        msgType,
                                        room.ctimestamp);
    var offset = headerLen;
    buf.writeUInt8(roomReady, offset++);
    buf.writeUInt8(socket.player.id, offset++);
    buf.writeUInt8(readyOrNot, offset++);

    broadcast(socket, room, buf, useridSocketMap);
  }

  function cancelReady(eventData) {
    var socket = eventData.socket;
    var lobby = eventData.lobby;
    var useridSocketMap = eventData.useridSocketMap;
    var recvedData = eventData.recvedData;

    var status = 0;
    do {
      if (socket.gamestate !== GAME_STATE.PLAYER_READY_TO_START) {
        status = 1;
        break;
      }
    } while (false);

    var offset = headerLen;
    var roomId = recvedData.readUInt32LE(offset);
    offset += roomIdLength;
    var room = Lobby.getRoomFromList(lobby, roomId);

    var buf = new Buffer(headerLen
                         + statusLength);
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        PVP_MSG.RESPONSE_CANCEL_READY,
                                        room.ctimestamp);
    var offset = headerLen;
    buf.writeUInt8(status, offset++);
    socket.write(buf);

    socket.gamestate =
      Statemachine.consumeEvent(socket.gamestate,
                                GAME_STATE.CANCELREADY);

    var cancelready = 1;
    broadcastReadystatus(socket, useridSocketMap, room, cancelready);
  }

  function getReady(eventData) {
    var socket = eventData.socket;
    var lobby = eventData.lobby;
    var recvedData = eventData.recvedData;
    var useridSocketMap = eventData.useridSocketMap;

    var status = 0;
    do {
      if (socket.gamestate !== GAME_STATE.PLAYER_WAIT_FOR_READY) {
        console.log('cannot change to ready status');
        status = 1;
        break;
      }
    } while (false);

    var offset = headerLen;
    var roomId = recvedData.readUInt32LE(offset);
    offset += roomIdLength;
    var room = Lobby.getRoomFromList(lobby, roomId);

    var buf = new Buffer(headerLen
                         + statusLength
                        );
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        PVP_MSG.RESPONSE_GETREADY,
                                        room.ctimestamp);
    offset = headerLen;
    buf.writeUInt8(status, offset++);
    socket.write(buf);

    if (status === 0) {
      socket.gamestate =
        Statemachine.consumeEvent(socket.gamestate,
                                  GAME_STATE.GETREADY);
      var getready = 0;
      broadcastReadystatus(socket, useridSocketMap, room, getready);
    }
  }

  function quitGame(eventData) {
    var socket = eventData.socket;
    var lobby = eventData.lobby;
    var recvedData = eventData.recvedData;
    var useridSocketMap = eventData.useridSocketMap;
    if (!Util.testArguments(arguments)) {
      return;
    }


    var offset = headerLen;
    var roomId = recvedData.readUInt32LE(offset);
    offset += roomIdLength;

    var room = Lobby.getRoomFromList(lobby, roomId);
    var status = 0;
    do {
      if (socket.gamestate !== GAME_STATE.GAMING) {
        status = 1;
        break;
      }
    } while (false);

    var buf = new Buffer(headerLen
                         + statusLength);
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        PVP_MSG.RESPONSE_QUITGAME,
                                        room.ctimestamp);
    offset = headerLen;
    buf.writeUInt8(status, offset++);
    socket.write(buf);

    socket.gamestate =
      Statemachine.consumeEvent(socket.gamestate,
                                GAME_STATE.QUITGAME);

    broadcastQuitgame(socket, useridSocketMap, room);
  }

  function broadcastQuitgame(socket, useridSocketMap, room) {
    if (!Util.testArguments(arguments)) {
      return;
    }

    var userName = 'name_' + socket.userId;
    var buf = new Buffer(headerLen
                         + statusLength
                         + charIdLength
                        );
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        PVP_MSG.BROADCAST_QUITGAME,
                                        room.ctimestamp);
    var offset = headerLen;
    var status = 0;
    buf.writeUInt8(status, offset++);
    buf.writeUInt8(socket.player.id, offset++);

    broadcast(socket, room, buf, useridSocketMap);
  }


  function auth(eventData) {
    var socket = eventData.socket;
    var recvedData = eventData.recvedData;
    var useridSocketMap = eventData.useridSocketMap;

    var offset = headerLen;
    var sessionId = recvedData.readUInt32LE(offset);
    offset += sessionIdLength;
    var userId = recvedData.readUInt32LE(offset);
    offset += userId;
    bunyanlog.info(
      'sessionId: ' + sessionId + ' userId: ' + userId
    );

    var status = 0;
    var buf = new Buffer(headerLen
                         + statusLength
                        );
    var authTimestamp = Date.now();
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        AUTH_MSG.RESPONSE_AUTH,
                                        authTimestamp);
    offset = headerLen;
    buf.writeUInt8(status, offset++);
    socket.write(buf);

    if (status === 0) {
      socket.userId = userId;
      socket.userName = 'name_' + socket.userId;
      useridSocketMap[userId] = socket;
      socket.authTimestamp = Date.now();
      socket.gamestate = GAME_STATE.AUTHED;
    }

    pubRedisclient.publish(channel, recvedData);
  }

  function broadcastPlayerLeaveroom(socket, useridSocketMap, room) {
    broadcastJoinOrLeaveRoom(socket, useridSocketMap, room,
                             PVP_MSG.BROADCAST_PLAYER_LEAVE);
  }

  function broadcastHostLeaveroom(socket, useridSocketMap, room) {
    broadcastJoinOrLeaveRoom(socket, useridSocketMap, room,
                             PVP_MSG.BROADCAST_HOST_LEAVE);
  }

  function leaveRoom(eventData) {
    var socket = eventData.socket;
    var lobby = eventData.lobby;
    var useridSocketMap = eventData.useridSocketMap;
    var recvedData = eventData.recvedData;

    pubDataToRedis(socket, recvedData);
    return;

    var offset = headerLen;
    var roomId = recvedData.readUInt32LE(offset);
    offset += roomIdLength;

    var room = Lobby.getRoomFromList(lobby, roomId);
    var buf = new Buffer(headerLen
                         + statusLength);
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        PVP_MSG.RESPONSE_LEAVEROOM,
                                        room.ctimestamp);
    var offset = headerLen;
    var status = 0;
    buf.writeUInt8(status, offset++);
    socket.write(buf);

    socket.gamestate = GAME_STATE.AUTHED; // restore to auth

    Room.popFromPlayerlist(room, socket);

    if (room.hostCharId === socket.player.id) {
      broadcastHostLeaveroom(socket, useridSocketMap, room);
      for (var i = 0; i < room.charInfoArr.length; i++) {
        var playerId = room.charInfoArr[i] || room.charInfoArr[i].userId;
        if (playerId && useridSocketMap[playerId]) {
          // var playerSocket = useridSocketMap[playerId];
          // playerSocket.gamestate =
          //   Statemachine.consumeEvent(playerSocket.gamestate,
          //                             GAME_STATE.LEAVEROOM);

          // transfer host to this player
          room.hostCharId = useridSocketMap[playerId].charId;
          break;
        }
      }
    } else {
      broadcastPlayerLeaveroom(socket, useridSocketMap, room);
    }

    if (Room.isEmpty(room)) {
      Lobby.popFromRoomlist(lobby, room);
    }
  }

  function broadcastStatus(eventData) {
    var socket = eventData.socket;
    var recvedData = eventData.recvedData;
    var socketCnt = eventData.socketCnt;
    var socketArr = eventData.socketArr;

    if (!socket) {
      console.log('socket is invalid!!!');
      return;
    }

    pubDataToRedis(socket, recvedData);
    return;

    var offset = headerLen;
    var recvedDataLength = recvedData.length;
    if (recvedDataLength <= offset) {
      return;
    }

    var fieldBitToSet1 = 0;
    var fieldBitToSet2 = 0;

    var newBufLen = fieldLength + fieldLength; // hold for 2 field bytes

    var field1 = recvedData.readUInt8(offset++);
    var characterState = {};

    var joystickPos = {};
    var joystickPosField = field1 & REQUEST_FIELDBIT.JOYSTICKPOS; // 0x04;
    if (joystickPosField
        && recvedDataLength >= offset + joystickXLength + joystickZLength
       ) {
      joystickPos.x = recvedData.readInt8(offset++);
      joystickPos.z = recvedData.readInt8(offset++);
      characterState[JOYSTICKPOS] = joystickPos;
    }
    fieldBitToSet2 |= RESPONSE_FIELD2BIT.RIVAL_JOYSTICKPOS;

    var position = {};
    var positionListField = field1 & REQUEST_FIELDBIT.POSITION_LIST;
    if (positionListField) {
      position.num = recvedData.readUInt8(offset++);
      var result = getPositionList(recvedData, position.num, offset);
      offset = result[0];
      position.arr = result[1];
      characterState[POSITION] = position;

      // newBufLen +=  numLength + positionXLength + positionYLength + positionZLength;
    }
    // fieldBitToSet1 |= RESPONSE_FIELD1BIT.MAIN_POSITION;

    var rotation = {};
    var rotationListField = field1 & REQUEST_FIELDBIT.ROTATION_LIST;
    if (rotationListField) {
      rotation.num = recvedData.readUInt8(offset++);
      result = getRotationList(recvedData, rotation.num, offset);
      offset = result[0];
      rotation.arr = result[1];
      characterState[ROTATION] = rotation;

      // newBufLen += numLength + rotationLength;
    }
    fieldBitToSet1 |= RESPONSE_FIELD1BIT.MAIN_ROTATION;

    var charStatus = {};
    var statusField = field1 & REQUEST_FIELDBIT.STATUS_LIST; // 0x08;
    if (statusField
        && recvedDataLength >= offset + statusLength
       ) {                      //
      charStatus.num = recvedData.readUInt8(offset++);
      result = getStatuslist(recvedData, charStatus.num, offset);
      offset = result[0];
      charStatus.arr = result[1];
      characterState[CHARSTATUS] = charStatus;

      // newBufLen += numLength + statusLength;
    }
    fieldBitToSet1 |= RESPONSE_FIELD1BIT.MAIN_STATUS;

    var startedSkill = 0;
    var startedSkillField = field1 & REQUEST_FIELDBIT.STARTEDSKILLID_LIST; // 0x10;
    if (startedSkillField
       ) {
      startedSkill.num = recvedData.readUInt8(offset++);
      result = getStartedSkillList(recvedData, startedSkill.num, offset);
      offset = result[0];
      startedSkill.arr = result[1];
      characterState[STARTEDSKILLID] = startedSkill;

      fieldBitToSet1 |= RESPONSE_FIELD1BIT.MAIN_STARTED_SKILLID;
      // newBufLen += charIdLength + skillIdLength;
    }

    fieldBitToSet1 |= RESPONSE_FIELD1BIT.MAIN_HP;
    fieldBitToSet1 |= RESPONSE_FIELD1BIT.MAIN_MP;
    fieldBitToSet2 |= RESPONSE_FIELD2BIT.RIVAL_JOYSTICKPOS;

    var roomId = socket.roomId || 0;
    var room = Lobby.getRoomFromList(lobby, roomId);
    // // Room.pushToPlayerlist(room, socket);

    var triggeredSkill = {};    // character use skill to hit others
    var triggeredSkillField = field1 & REQUEST_FIELDBIT.TRIGGEREDSKILL_LIST; // 0x20;
    if (triggeredSkillField
        && recvedDataLength >= offset + skillIdLength + triggerNum
       ) {
      triggeredSkill.num = recvedData.readUInt8(offset++);
      result = getTriggeredSkillList(recvedData, triggeredSkill.num, offset);
      offset = result[0];
      triggeredSkill.arr = result[1];
      characterState[TRIGGEREDSKILL] = triggeredSkill;
    }

    if (field1 !== 0)
      console.log(socket.player.id, field1, characterState);

    Character.updateState(socket.player, characterState);

    newBufLen += numLength;     // for position
    newBufLen += numLength;     // for rotation
    newBufLen += numLength;     // for status
    newBufLen += numLength;     // for skill
    newBufLen += numLength;     // for hp
    newBufLen += numLength;     // for mp
    newBufLen += numLength;     // for joystick

    var charId = socket.player.id;
    var charInfoArr = room.charInfoArr;

    for (var i = 0; i < charInfoArr.length; i++) {
      var charInfo = charInfoArr[i];
        newBufLen +=
          + charIdLength + rotationLength
          + charIdLength + statusLength
          + charIdLength + joystickXLength + joystickZLength
          + charIdLength + hpLength
          + charIdLength + mpLength;

      if (charInfo.charId === charId
          && fieldBitToSet1 & RESPONSE_FIELD1BIT.MAIN_POSITION) { // rival
        // do nothing
      } else {
        newBufLen += charIdLength + positionXLength + positionYLength + positionZLength;
      }

      if (fieldBitToSet1 & RESPONSE_FIELD1BIT.MAIN_STARTED_SKILLID) {
        // newBufLen += charIdLength + skillIdLength;
      }

        // var buffArr = charInfo.character.buffArr;
        // for (var j = 0; buffArr && j < buffArr.length; j++) {
        //   newBufLen += buffIdLength + timestampLength;
        // }
      // }
    }

    // todo: may be unnecessary now!
    // if (false) {
    //   for (var i = 0; i < charInfoArr.length; i++) {
    //     var charInfo = charInfoArr[i];
    //     if (charInfo.charId === charId) {
    //       var buffArr = charInfo.character.buffArr;
    //       for (var j = 0; buffArr && j < buffArr.length; j++) {
    //         newBufLen += buffIdLength + timestampLength;
    //       }
    //     }
    //   }
    // }
    // todo: may include npcInfoArr in future

    var buf = new Buffer(headerLen
                         + newBufLen
                        );
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        PVP_MSG.RESPONSE_STATUS,
                                        room.ctimestamp); // todo: should be room's

    offset = headerLen;
    buf.writeUInt8(fieldBitToSet1, offset++);
    buf.writeUInt8(fieldBitToSet2, offset++);

    buf.writeUInt8(charInfoArr.length, offset++);
    for (var i = 0; i < charInfoArr.length; i++) {
      var charInfo = charInfoArr[i];
      var character = charInfo.character;
      if (charInfo.charId !== charId
          && character.position && character.position.arr) {
            buf.writeUInt8(character.id, offset++);
            // buf.writeUInt8(character.position.num, offset++);

            var arr = character.position.arr;
            position = arr[arr.length-1];
            buf.writeInt16LE(position.x, offset);
            offset += positionXLength;
            buf.writeInt16LE(position.y, offset);
            offset += positionYLength;
            buf.writeInt16LE(position.z, offset);
            offset += positionZLength;

            // console.log(position);
          }
    }

    buf.writeUInt8(charInfoArr.length, offset++);
    for (var i = 0; i < charInfoArr.length; i++) {
      var charInfo = charInfoArr[i];
      var character = charInfo.character;
          if (character.rotation && character.rotation.arr) {
            buf.writeUInt8(character.id, offset++);
            // buf.writeUInt8(character.rotation.num, offset++);
            buf.writeInt16LE(character.rotation.arr[0].rotation, offset);
          }
          offset += rotationLength;
    }

    buf.writeUInt8(charInfoArr.length, offset++);
    for (var i = 0; i < charInfoArr.length; i++) {
      var charInfo = charInfoArr[i];
      var character = charInfo.character;
          if (character.status && character.status.arr) {
            buf.writeUInt8(character.id, offset++);
            // buf.writeUInt8(character.status.num, offset++);
            buf.writeUInt8(character.status.arr[0].status, offset++);
          }
    }

    buf.writeUInt8(charInfoArr.length, offset++);
    for (var i = 0; i < charInfoArr.length; i++) {
      var charInfo = charInfoArr[i];
        if (character.startedSkill) {
          buf.writeUInt8(character.id, offset++);
          // buf.writeUInt8(character.startedSkill.num, offset++);
          buf.writeUInt8(character.startedSkill.arr[0], offset++);
        }
    }
    socket.player.startedSkill = null;

    buf.writeUInt8(charInfoArr.length, offset++);
    for (var i = 0; i < charInfoArr.length; i++) {
      var charInfo = charInfoArr[i];
      var character = charInfo.character;
        if (character.joystickPos) {
          buf.writeUInt8(character.id, offset++);
          buf.writeInt8(character.joystickPos.x, offset++);
          buf.writeInt8(character.joystickPos.z, offset++);
        }
    }

    buf.writeUInt8(charInfoArr.length, offset++);
    for (var i = 0; i < charInfoArr.length; i++) {
      var charInfo = charInfoArr[i];
      var character = charInfo.character;
        if (character.currentHp) {
          buf.writeUInt8(character.id, offset++);
          buf.writeFloatLE(character.currentHp, offset);
          offset += hpLength;
        }
    }

    buf.writeUInt8(charInfoArr.length, offset++);
    for (var i = 0; i < charInfoArr.length; i++) {
      var charInfo = charInfoArr[i];
      var character = charInfo.character;
        if (character.currentMp) {
          buf.writeUInt8(character.id, offset++);
          buf.writeFloatLE(character.currentMp, offset);
          offset += mpLength;
        }
    }

    for (var i = 0; i < charInfoArr.length; i++) {
      var charInfo = charInfoArr[i];
      // if (charInfo.charId === charId) {
        // var buffArr = charInfo.character.buffArr;
        // for (var j = 0; buffArr && j < buffArr.length; j++) {
        //   var buff = buffArr[j];
        //   buf.writeUInt8(buff.id, offset++);
        //   buf.writeFloatLE(buff.ctimestamp, offset);
        //   offset += timestampLength;
        // }
      // } else {                  // (charInfo.charId !== charId)
        var character = charInfo.character;

        // var buffArr = character.buffArr;
        // for (var j = 0; buffArr && j < buffArr.length; j++) {
        //   var buff = buffArr[j];
        //   buf.writeUInt8(buff.id, offset++);
        //   buf.writeFloatLE(buff.ctimestamp, offset);
        //   offset += timestampLength;
        // }

      // }                         // if is rival
    }                           // end of for charInfoArr

    // write bitfield2 here
    // offset = headerLen + fieldLength;     // after headerLen + fieldBitToSet1
    // buf.writeUInt8(fieldBitToSet2, offset++);
    socket.write(buf);

    // startedSkillId should be cleared after broadcast to all
    if (startedSkillField) {
      buf = new Buffer(headerLen
                       + fieldLength
                       + fieldLength
                       + numLength
                       + charIdLength
                       + skillIdLength);
      BufferUtil.writeMsgTypeAndTimestamp(buf,
                                          PVP_MSG.RESPONSE_STATUS,
                                          room.ctimestamp);

      offset = headerLen;
      fieldBitToSet1 = 0;
      buf.writeUInt8(fieldBitToSet1, offset++);
      fieldBitToSet2 |= RESPONSE_FIELD1BIT.STARTEDSKILLID_LIST;
      buf.writeUInt8(fieldBitToSet2, offset++);
      buf.writeUInt8(1, offset++); // only this socket's update
      buf.writeUInt8(charId, offset++);
      buf.writeUInt8(startedSkill.arr[0].skillId, offset++);
      broadcast(socket, room, buf, useridSocketMap);
      socket.player.startedSkill = null;
    }

  }

  function getBuffList(recvedData, num, offset) {
    var result = [];
    for (var i = 0; i < num; i++) {
      var charId = recvedData.readUInt8(offset++);
      var buffId = recvedData.readUInt8(offset++);
      var buffValue = recvedData.readFloatLE(offset);
      offset += buffValueLength;
      result.push({
        'charId': charId,
        'buffId': buffId,
        'buffValue': buffValue,
      });
    }
    return result;
  }

  function getTriggeredSkillList(recvedData, num, offset) {
    var result = [];
    for (var i = 0; i < num; i++) {
      var charId = recvedData.readUInt8(offset++);
      var skillId = recvedData.readUInt8(offset++);
      var num = recvedData.readUInt8(offset++);
      var buffArr = getBuffList(recvedData, num, offset);
    }
    return result;
  }

  function getStartedSkillList(recvedData, num, offset) {
    var result = [];
    for (var i = 0; i < num; i++) {
      var charId = recvedData.readUInt8(offset++);
      var skillId = recvedData.readUInt8(offset++);
      result.push({
        'charId': charId,
        'skillId': skillId,
      });
    }

    return result;
  }

  function getStatuslist(recvedData, num, offset) {
    var result = [];
    for (var i = 0; i < num; i++) {
      var charId = recvedData.readUInt8(offset++);
      var status = recvedData.readUInt8(offset++);
      result.push({
        'charId': charId,
        'status': status,
      });
    }
    return [offset, result];
  }

  function getRotationList(recvedData, num, offset) {
    var result = [];
    for (var i = 0; i < num; i++) {
      var charId = recvedData.readUInt8(offset++);
      var rotation = recvedData.readInt16LE(offset);
      offset += rotationLength;
      result.push({
        'charId': charId,
        'rotation': rotation,
      });
    }
    return [offset, result];
  }

  function getPositionList(recvedData, num, offset) {
    var result = [];
    for (var i = 0; i < num; i++) {
      var charId = recvedData.readUInt8(offset++);
      var x = recvedData.readInt16LE(offset);
      offset += positionXLength;
      var y = recvedData.readInt16LE(offset);
      offset += positionYLength;
      var z = recvedData.readInt16LE(offset);
      offset += positionZLength;
      result.push({
        'charId':charId,
        'x': x,
        'y': y,
        'z': z,
      });
    }

    return [offset, result];
  }

  function getBuffToRemove(recvedData, offset, target, buffNum) {
    var result = [];
    for (var i = 0; i < buffNum; i++) {
      var buffId = recvedData.readUInt8(offset++);
      var ctimestamp = recvedData.readFloatLE(offset);
      offset += timestampLength;
      var buffToDel = {
        'buffId': buffId,
        'ctimestamp': ctimestamp,
      };
      result.push(buffToDel);
    }
    return [offset, result];
  }

  // send buffer to others in game
  function getTriggeredSkill(recvedData, offset, room, triggerNum) {
    if (!Util.testArguments(arguments)) {
      return [0, 0];
    }

    var target = null;

    var result = [];
    for (var i = 0; i < triggerNum; i++) {
      var targetId = recvedData.readUInt8(offset++);
      if (Room.isNpcId(room, targetId)) {
        target = room || Room.getNpcFromlist(room, targetId);
      } else {
        target = room || Room.getPlayerFromlist(room, targetId);
      }

      var buffNum = recvedData.readUInt8(offset++);
      var a = getBuffToAdd(recvedData, offset, buffNum, target);
      var buffToAdd = a[0];
      offset = a[1];
      result.push({
        'targetId':targetId,
        'buffNum':buffNum,
        'buffToAdd': buffToAdd,
      });

    }

    return [offset, result];
  }

  function getBuffToAdd(recvedData, offset, buffNum, target) {
    var result = [];
    for (var i = 0; i < buffNum; i++) {
      var buffId = recvedData.readUInt8(offset++);
      var skillId = recvedData.readUInt8(offset++);
      var buffValue = recvedData.readFloatLE(offset);
      offset += buffValueLength;
      var buff = {
        'buffId': buffId,
        'skillId': skillId,
        'buffValue': buffValue,
      };
      result.push(buff);
    }

    return [offset, result];
  }

  function getBuffToApply(recvedData, offset, target, buffNum) {

    var result = [];
    for (var i = 0; i < buffNum; i++) {
      var buffId = recvedData.readUInt8(offset++);
      result.push({
        'buffId':buffId,
      });
    }

    return [offset, result];
  }

  function broadcastJoinOrLeaveRoom(socket, useridSocketMap, room, msgType) {
    if (!Util.testArguments(arguments)) {
      return;
    }

    if (room.charInfoArr.length === 0) {
      return;                   // notify nobody
    }

    var buf = null;
    if (msgType === PVP_MSG.BROADCAST_HOST_LEAVE) {
      buf = new Buffer(headerLen);
    } else {
      var userName = socket.userName; // todo:
      var length = roomCurrentplayersLength
        + roomMaxplayerLength
        + charIdLength
        + userName.length + separatorLength;
      buf = new Buffer(headerLen
                       + length);
    }
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        msgType,
                                        room.ctimestamp);

    if (msgType === PVP_MSG.BROADCAST_HOST_LEAVE) {
      // do nothing
    } else {
      var currentPlayers = room.charInfoArr.length;
      var offset = headerLen;
      buf.writeUInt8(currentPlayers, offset++);
      var maxPlayers = room.maxPlayers;
      buf.writeUInt8(maxPlayers, offset++);
      buf.writeUInt8(socket.player.id, offset++);
      buf.write(userName, offset, userName.length);
      offset += userName.length;
      buf.writeUInt8(0, offset++);
    }

    broadcast(socket, room, buf, useridSocketMap);
  }

  function broadcast(socket, room, buf, useridSocketMap) {
    var charInfoArr = room.charInfoArr;
    if (!charInfoArr || typeof charInfoArr === 'undefined') {
      return;
    }

    for (var i = 0; i < charInfoArr.length; i++) {
      if (charInfoArr[i] && charInfoArr[i].userId
          && charInfoArr[i].userId !== socket.userId
          && useridSocketMap[charInfoArr[i].userId]) {
        var playerSocket = useridSocketMap[charInfoArr[i].userId];
        playerSocket.write(buf);
      }
    }
  }

  function joinRoom(eventData) {
    var socket = eventData.socket;
    var lobby = eventData.lobby;
    var recvedData = eventData.recvedData;
    var useridSocketMap = eventData.useridSocketMap;

    pubDataToRedis(socket, recvedData);
    return;


    var offset = headerLen;
    var roomId = recvedData.readUInt32LE(headerLen);
    offset += roomIdLength;

    var status = 0;
    do {
      if (socket.gamestate !== GAME_STATE.AUTHED) {
        status = 2;
        break;
      }

      // todo: check user's status to avoid joining multiple rooms
      var room = Lobby.getRoomFromList(lobby, roomId);
      bunyanlog.info('roomId: ' + roomId + ' room: ' + room);
      if (!room) {
        status = 1;
        break;
      }

      if (Room.isFull(room)) {
        status = 3;
        break;
      }
      Room.pushToPlayerlist(room, socket);
    } while (false);

    var buf = null;
    if (status === 0) {
      var dataLength = 0;
      var charInfoArr = room.charInfoArr;
      for (var i = 0; i < charInfoArr.length; i++) {
        // todo: need to query every valid user_id's id and name
        if (charInfoArr[i]) {
          var playerId = charInfoArr[i].userId;
          if (playerId) {
            var playerSocket = useridSocketMap[playerId];
            var name = playerSocket.userName;
            dataLength += charIdLength // todo: charId instead
              + name.length + separatorLength
              + userGamestateLength;
          }
        }
      }
      bunyanlog.info(dataLength);
      buf = new Buffer(headerLen
                       + statusLength
                       + roomIdLength
                       + charIdLength
                       + charIdLength
                       + roomMaxplayerLength
                       + roomCurrentplayersLength
                       + dataLength);
    } else {
      buf = new Buffer(headerLen
                       + statusLength
                       + roomIdLength);
    }
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        PVP_MSG.RESPONSE_JOINROOM,
                                        room.ctimestamp);

    offset = headerLen;
    buf.writeUInt8(status, offset++);
    buf.writeUInt32LE(roomId, offset);
    offset += roomIdLength;
    buf.writeUInt32LE(room.hostCharId, offset);
    offset += charIdLength;

    if (status === 0) {
      buf.writeUInt8(socket.player.id, offset++);
      buf.writeUInt8(room.maxPlayers, offset++);
      buf.writeUInt8(room.charInfoArr.length, offset++);

      // todo: write to buffer with player_id + player_name + separator
      bunyanlog.info('charInfoArr ' + charInfoArr.length);
      for (i = 0; i < charInfoArr.length; i++) {
        playerId = charInfoArr[i].userId;
        if (playerId && useridSocketMap[playerId]) {
          var playerSocket = useridSocketMap[playerId];
          buf.writeUInt8(playerSocket.player.id, offset++);
          name = playerSocket.userName;
          buf.write(name, offset, name.length);
          offset += name.length;
          buf.writeUInt8(0, offset++);
          var readyStatus = 1;
          if (playerSocket
              && (playerSocket.gamestate === GAME_STATE.PLAYER_READY_TO_START
                  || playerSocket.gamestate === GAME_STATE.HOST_READY_TO_START)) {
            readyStatus = 0;
          }
          buf.writeUInt8(readyStatus, offset++);
        }
      }
    }
    socket.write(buf);

    if (status === 0) {
      broadcastJoinOrLeaveRoom(socket, useridSocketMap, room,
                               PVP_MSG.BROADCAST_PLAYER_JOIN);
      socket.gamestate =
        Statemachine.consumeEvent(socket.gamestate,
                                  GAME_STATE.JOINROOM);
      socket.roomId = roomId;
    }
  }


  function resetSocket(socket, socketArr) {
    // socketArr[socket.id] = null;
    // releasedSocketIdArr.push(socket.id);
    if (!socket || typeof socket === 'undefined') {
      return;
    }

    if (socket.id) {
      // console.log('socket.id: ' + socket.id);
      socket.id = null;
    }
    if (socket.userId) {
      // console.log('socket.userId: ' + socket.userId);
      socket.userId = null;
    }
    if (socket.ntp) {
      socket.ntp = false;
    }
    if (socket.roomId) {
      bunyanlog.info(socket.roomId)
      socket.roomId = null;
    }
    if (socket.gamestate) {
      socket.gamestate = null;
    }
    if (socket.authTimestamp) {
      socket.authTimestamp = null;
    }
    if (socket.player.id) {
      bunyanlog.info(socket.charId);
      socket.charId = null;
    }

    socket.destroy();
    socket.unref();
  }

  function createRoom(eventData) {
    var socket = eventData.socket;
    var lobby = eventData.lobby;
    var recvedData = eventData.recvedData;

    console.log('create room');
    pubDataToRedis(socket, recvedData);

    // var status = 0;
    // do {
    //   if (socket.gamestate !== GAME_STATE.AUTHED) {
    //     status = 2;             // invalid request
    //     break;
    //   }

    //   var maxPlayers = recvedData.readUInt8(headerLen);
    //   var roomName = recvedData.toString('utf8', headerLen+1, recvedData.length-1);
    //   bunyanlog.info('max: ' + maxPlayers + ' name: ' + roomName);

    //   var room = Lobby.getNewRoom(lobby, socket, roomName, maxPlayers);
    //   if (!room) {
    //     bunyanlog.info('invalid room');
    //     status = 1;
    //   }
    // } while (false);

    // var buf = null;
    // var offset = headerLen;
    // if (status === 0 && room) {
    //   buf = new Buffer(headerLen
    //                    + statusLength
    //                    + roomIdLength
    //                    + charIdLength
    //                   );
    // } else {
    //   buf = new Buffer(headerLen
    //                    + statusLength
    //                   );
    // }

    // BufferUtil.writeMsgTypeAndTimestamp(buf,
    //                                     PVP_MSG.RESPONSE_CREATEROOM,
    //                                     room.ctimestamp);
    // buf.writeUInt8(status, offset++);

    // if (status === 0) {
    //   buf.writeUInt32LE(room.id, offset);
    //   offset += roomIdLength;
    //   buf.writeUInt8(socket.player.id, offset++);
    // }

    // socket.write(buf);


    socket.gamestate =
      Statemachine.consumeEvent(socket.gamestate,
                                GAME_STATE.CREATEROOM);

  }

  // add below field to player's socket: id, userId, hostId, hostSocket
  function initSocket(socket) {
    socket.id = null;
    socket.userId = null;
    socket.userName = 'test';   // todo:
    socket.ntp = false;
    socket.roomId = null;
    socket.gamestate = null;
    socket.authTimestamp = null;
    socket.player = Player.createNew(socket); // todo:
  }

  function disconnectOtherPlayers(socket, socketArr) {
    for (var i = 0; i < socketArr.length; i++) {
      var a = socketArr[i];
      if (a && socket
          && a.remoteAddress !== socket.remoteAddress) {
        a.destroy();
      }
    }
  }

  function syncClock(eventData) {
    var socket = eventData.socket;
    var recvedData = eventData.recvedData;
    var roomTimestamp = eventData.roomTimestamp;
    var buf = new Buffer(headerLen
                         + timestampLength);
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        NTP_MSG.RESPONSE_NTP,
                                        roomTimestamp);
    var clientTimestamp = recvedData.readFloatLE(clientTimestampOffset);
    console.log('clientTimestamp: ' + clientTimestamp);

    var offset = headerLen;
    buf.writeFloatLE(clientTimestamp, offset); // send back client's timestamp
    offset += timestampLength;

    console.log('send sync on: ' + Date.now());
    socket.write(buf);          // no delay by default
    socket.ntp = true;
  }

  function getRoomlist(eventData) {
    var socket = eventData.socket;
    var lobby = eventData.lobby;
    var recvedData = eventData.recvedData;

    console.log('get room list');
    pubDataToRedis(socket, recvedData);
    return;

    // do {
    //   if (socket.gamestate !== GAME_STATE.AUTHED) {
    //     break;
    //   }

    //   var roomList = Lobby.getRoomlist(lobby);
    //   var roomCnt = lobby.roomCnt;
    //   var dataLength = 0;
    //   for (var i = 0; i < roomCnt; i++) {
    //     var room = roomList[i];
    //     if (!room || typeof room === 'undefined') {
    //       continue;
    //     }
    //     dataLength += roomIdLength
    //       + roomCurrentplayersLength
    //       + roomMaxplayerLength
    //       + charIdLength;
    //     if (room.name) {
    //       dataLength += room.name.length;
    //     }
    //     dataLength += separatorLength;
    //     if (room.info) {
    //       dataLength += room.info.length;
    //     }
    //     dataLength += separatorLength;
    //     if (room.hostName) {
    //       dataLength += room.hostName.length;
    //     }
    //     dataLength += separatorLength;
    //   }
    // } while (false);

    // var buf = new Buffer(headerLen
    //                      + roomMaxplayerLength
    //                      + dataLength);
    // BufferUtil.writeMsgTypeAndTimestamp(buf,
    //                                     PVP_MSG.RESPONSE_ROOMLIST,
    //                                     socket.authTimestamp);
    // var offset = headerLen;
    // buf.writeUInt8(roomCnt, offset++);

    // for (i = 0; i < roomCnt; i++) {
    //   var room = roomList[i];
    //   if (!room || typeof room === 'undefined') {
    //     continue;
    //   }
    //   buf.writeUInt32LE(room.id, offset);
    //   offset += roomIdLength;
    //   buf.writeUInt8(room.charInfoArr.length, offset++);
    //   buf.writeUInt8(room.maxPlayers, offset++);
    //   // buf.write(string, [offset], [length], [encoding])
    //   buf.write(room.name, offset, room.name.length);
    //   offset += room.name.length;
    //   buf.writeUInt8(0, offset++);
    //   buf.write(room.info, offset, room.info.length);
    //   offset += room.info.length;
    //   buf.writeUInt8(0, offset++);
    //   buf.writeUInt8(room.hostCharId, offset++);
    //   buf.write(room.hostName, offset, room.hostName.length);
    //   offset += room.hostName.length;
    //   buf.writeUInt8(0, offset++);
    // }
    // socket.write(buf);

  }

  function updateState(subRedisclient, hostId, idArr, event) {
    var nextState = StateMachine.consumeEvent(event);
    saveStateToRedis(subRedisclient, nextState, idArr);
  }

  function broadcastStartgame(socket, useridSocketMap, room) {
    if (!Util.testArguments(arguments)) {
      return;
    }

    var totalCnt = Room.getCurrentPlayerCnt(room) + Room.getCurrentNpcCnt(room);
    var buf = new Buffer(headerLen
                         + statusLength
                         + charCntLength
                         + charIdLength * totalCnt
                         + spawnPointLength * totalCnt
                        );
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        PVP_MSG.BROADCAST_STARTGAME,
                                        room.ctimestamp);

    var offset = headerLen;
    var status = 0;
    buf.writeUInt8(status, offset++);
    buf.writeUInt8(totalCnt, offset++);

    // todo:
    var spawnPoint = 1;
    var charInfoArr = room.charInfoArr;
    for (var i = 0; i < charInfoArr.length; i++) {
      var playerId = charInfoArr[i].userId;
      var playerSocket = playerId && useridSocketMap[playerId];
      if (playerSocket) {
        buf.writeUInt8(playerSocket.player.id, offset++);
        buf.writeUInt8(spawnPoint++, offset++);
      }
    }

    var npcInfoArr = room.npcInfoArr;
    for (var i = 0; i < npcInfoArr.length; i++) {
      var npc = npcInfo[i];
      buf.writeUInt8(npc.npcId, offset++);
      buf.writeUInt8(spawnPoint++, offset++);
    }
    socket.write(buf);

    // change all players' game state
    for (var i = 0; i < charInfoArr.length; ++i) {
      var playerId = charInfoArr[i].userId;
      var playerSocket = playerId && useridSocketMap[playerId];
      if (playerSocket) {
        playerSocket.gamestate =
          Statemachine.consumeEvent(playerSocket.gamestate,
                                    GAME_STATE.RECVSTART);
      }
    }

    broadcast(socket, room, buf, useridSocketMap);
  }

  function pubDataToRedis(socket, recvedData) {
    var redisData = new Buffer(userIdLength
                               + recvedData.length);
    if (redisData.length > 12)
      console.log('new data to redis length ', redisData.length);
    var offset = 0;
    redisData.writeUInt32LE(socket.userId, offset);
    offset += userIdLength;

    recvedData.copy(redisData, offset);
    pubRedisclient.publish(channel, redisData);
  }

  function startGame(eventData) {
    var socket = eventData.socket;
    var useridSocketMap = eventData.useridSocketMap;
    var recvedData = eventData.recvedData;

    console.log('start game');
    pubDataToRedis(socket, recvedData);

    var offset = headerLen;
    var roomId = recvedData.readUInt32LE(offset);
    console.log('roomId:', roomId);
    return;


    offset += roomIdLength;
    var room = Lobby.getRoomFromList(lobby, roomId);
    var status = 0;
    do {
      if (socket.gamestate !== GAME_STATE.HOST_READY_TO_START) {
        status = 1;
        break;
      }
    } while (false);
    var buf = new Buffer(headerLen
                         + statusLength
                        );
    BufferUtil.writeMsgTypeAndTimestamp(buf,
                                        PVP_MSG.RESPONSE_STARTGAME,
                                        room.ctimestamp);
    offset = headerLen;
    buf.writeUInt8(status, offset++);
    // socket.write(buf);

    socket.gamestate =
      Statemachine.consumeEvent(socket.gamestate,
                                GAME_STATE.STARTGAME);

    broadcastStartgame(socket, useridSocketMap, room);

  }

  module.exports.Pvpserver = server;

}());
