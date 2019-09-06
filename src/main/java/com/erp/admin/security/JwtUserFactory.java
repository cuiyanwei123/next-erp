package com.erp.admin.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.erp.admin.db.entity.SysRole;
import com.erp.admin.db.entity.SysUser;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class JwtUserFactory {

    private JwtUserFactory() {
    }

    static JwtUser create(SysUser user) {
        return new JwtUser(
                user.getUid(),
                user.getUsername(),
                user.getPassword(),
                mapToGrantedAuthorities(user.getRoles()),
                user.getLastPasswordResetDate()
        );
    }

    private static Set<GrantedAuthority> mapToGrantedAuthorities(List<SysRole> authorities) {
        return authorities.stream()
                .map(authoritie -> new SimpleGrantedAuthority(authoritie.getName()))
                .collect(Collectors.toSet());
    }
}

