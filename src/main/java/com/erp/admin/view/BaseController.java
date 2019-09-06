package com.erp.admin.view;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import com.erp.admin.security.JwtUser;

import springfox.documentation.annotations.ApiIgnore;

/**
 *
 * @author
 * @version : 1.00
 * @Create Time : 2016年11月22日 下午5:45:14
 * @Description : 基础控制层
 * @History：Editor version Time Operation Description*
 *
 */
@ApiIgnore
@RestController
// @RequestMapping(value = "${api.version}")
public class BaseController {

    @Autowired
    public HttpServletRequest request;

    /**
     * 获取uid
     * 
     * @param uid
     * @return
     */
    public static Integer getUidByToken() {
        JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwtUser.getUid();
    }

    public static String getUnameByToken() {
        JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwtUser.getUsername();
    }

    public static boolean hasRole(String role) {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }
        Collection<GrantedAuthority> grantedAuthorityList = (Collection<GrantedAuthority>) authentication.getAuthorities();
        for (GrantedAuthority authority : grantedAuthorityList) {
            if (role.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasRoleAdmin() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }
        Collection<GrantedAuthority> grantedAuthorityList = (Collection<GrantedAuthority>) authentication.getAuthorities();
        for (GrantedAuthority authority : grantedAuthorityList) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private static Authentication getAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return null;
        }
        return context.getAuthentication();
    }
}
