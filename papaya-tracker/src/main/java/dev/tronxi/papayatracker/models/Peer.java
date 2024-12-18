package dev.tronxi.papayatracker.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.Objects;

@RedisHash("Peer")
public record Peer(@Id String id, String address, int port, long millis) {

    public static Peer of(PeerDTO peerDTO) {
        String id = peerDTO.address() + ":" + peerDTO.port();
        return new Peer(id, peerDTO.address(), peerDTO.port(), System.currentTimeMillis());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peer peer = (Peer) o;
        return port == peer.port && Objects.equals(address, peer.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }
}
