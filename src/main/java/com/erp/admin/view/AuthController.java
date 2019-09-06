package com.erp.admin.view;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.erp.admin.db.entity.SysUser;
import com.erp.admin.db.mapper.SysRoleUserMapper;
import com.erp.admin.security.JwtAuthenticationRequest;
import com.erp.admin.security.JwtAuthenticationResponse;
import com.erp.admin.security.JwtUser;
import com.erp.admin.service.AuthService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "auth接口", description = "安全校验API")
@RestController
@RequestMapping(value = "${api.version}")
public class AuthController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Autowired
    private AuthService authService;

    @Autowired
    private SysRoleUserMapper sysRoleUserMapper;

    @ApiOperation(value = "auth", notes = "获取token")
    @RequestMapping(value = "${jwt.route.authentication.path}", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest) throws AuthenticationException {
        final String token = authService.login(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("操作人:{},操作:{},结果:{}", authenticationRequest.getUsername(), "获取token", "成功");
        // Return the token
        return ResponseEntity.ok(new JwtAuthenticationResponse(token));
    }

    // public static void main(String[] args) {
    // System.out.println(new BCryptPasswordEncoder().encode("123456"));
    // }
    @RequestMapping(value = "${jwt.route.authentication.parse}", method = RequestMethod.GET)
    @ApiOperation(value = "解析token", notes = "解析token,获取对应uid,username,rid")
    public Map<String, Object> getUidFromToken(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String username = (String) request.getAttribute("username");
        SysUser su = sysRoleUserMapper.findByUsername(username);
        if (su != null) {
            result.put("uid", su.getUid());
            // TODO 假设一个账号只有一种角色
            if (su.getRoles() != null && su.getRoles().size() > 0) {
                result.put("rid", su.getRoles().get(0).getRid());
            }
        }
        result.put("username", username);
        // addEvent(Constants.EVENT_TYPE_USER,getUidByToken(),getUnameByToken()+"
        // 解析token成功");
        log.info("操作人:{},操作:{},结果:{}", username, "解析token", "成功");
        return result;
    }

    @RequestMapping(value = "${jwt.route.authentication.refresh}", method = RequestMethod.GET)
    public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request) throws AuthenticationException {
        String token = request.getHeader(header);
        String refreshedToken = authService.refresh(token);
        if (refreshedToken == null) {
            return ResponseEntity.badRequest().body(null);
        } else {
            return ResponseEntity.ok(new JwtAuthenticationResponse(refreshedToken));
        }
    }
}
