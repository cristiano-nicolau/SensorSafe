package com.SensorSafe.API.repository;

import org.springframework.stereotype.Repository;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.SensorSafe.API.model.room.Room;
import com.SensorSafe.API.model.room.RoomStats;

public interface RoomRepository extends MongoRepository<Room, Long>{
    Room findByRoomId(ObjectId roomId);
    Room findByRoomName(String roomName);

    boolean existsByRoomId(ObjectId roomId);
    boolean existsByRoomName(String roomName);
    
    boolean existsByAutomatized(ObjectId roomId);

    RoomStats getStatsByRoomId(ObjectId roomId);

    void deleteByRoomId(ObjectId roomId);
}