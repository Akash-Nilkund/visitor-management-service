package com.vms.backend.service;

import com.vms.backend.entity.Host;
import com.vms.backend.repository.HostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private HostRepository hostRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Host host = hostRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Host not found: " + username));

        return new User(
                host.getUsername(),
                host.getPassword(),         // must be encoded in DB
                List.of(new SimpleGrantedAuthority("ROLE_" + host.getRole()))  // 🔥 major fix
        );
    }
}
