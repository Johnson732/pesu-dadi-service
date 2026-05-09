package com.pesudadi.repository;

import com.pesudadi.model.ChatRoom;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryChatRoomRepository {

    private final ConcurrentHashMap<String, ChatRoom> rooms = new ConcurrentHashMap<>();

    public ChatRoom save(ChatRoom room) {
        rooms.put(room.getRoomId(), room);
        return room;
    }

    public Optional<ChatRoom> findById(String roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    public void remove(String roomId) {
        rooms.remove(roomId);
    }
}
