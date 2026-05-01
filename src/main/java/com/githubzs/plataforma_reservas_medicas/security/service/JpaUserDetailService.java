package com.githubzs.plataforma_reservas_medicas.security.service;

import com.githubzs.plataforma_reservas_medicas.security.domine.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class JpaUserDetailService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        var user = appUserRepository.findByDocumentNumberIgnoreCase(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with document number: " + userName));

        var authorities = user.getRoles().stream()
                .map(Enum::name)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        return User.withUsername(user.getDocumentNumber())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
    
}
