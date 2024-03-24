package com.twittersfs.server.repos;

import com.twittersfs.server.entities.Proxy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProxyRepo extends JpaRepository<Proxy, Long> {
    Optional<Proxy> findByIpAndPort(String ip, String port);
}
