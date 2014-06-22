
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
      console.log('socket closed ', socket.userId); //
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

  function broadcastReadystatus(socket, useridSocketMap, room, readyOrNot) {
    // readStatus can be 0
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

    // pubRedisclient.publish(channel, recvedData); // no need to info backend server
  }

  function leaveRoom(eventData) {
    var socket = eventData.socket;
    var lobby = eventData.lobby;
    var useridSocketMap = eventData.useridSocketMap;
    var recvedData = eventData.recvedData;

    pubDataToRedis(socket, recvedData);
    return;
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
    return;
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
    return;
  }

  module.exports.Pvpserver = server;

}());
