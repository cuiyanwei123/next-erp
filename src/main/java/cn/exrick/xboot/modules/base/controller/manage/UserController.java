package cn.exrick.xboot.modules.base.controller.manage;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.exrick.xboot.common.constant.CommonConstant;
import cn.exrick.xboot.common.utils.PageUtil;
import cn.exrick.xboot.common.utils.ResultUtil;
import cn.exrick.xboot.common.utils.SecurityUtil;
import cn.exrick.xboot.common.vo.PageVo;
import cn.exrick.xboot.common.vo.Result;
import cn.exrick.xboot.common.vo.SearchVo;
import cn.exrick.xboot.modules.base.entity.Role;
import cn.exrick.xboot.modules.base.entity.User;
import cn.exrick.xboot.modules.base.entity.UserRole;
import cn.exrick.xboot.modules.base.service.RoleService;
import cn.exrick.xboot.modules.base.service.UserRoleService;
import cn.exrick.xboot.modules.base.service.UserService;
import cn.exrick.xboot.modules.base.service.mybatis.IUserRoleService;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Exrickx
 */
@Slf4j
@RestController
@Api(description = "用户接口")
@RequestMapping("/erp/user")
@CacheConfig(cacheNames = "user")
@Transactional
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private IUserRoleService iUserRoleService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SecurityUtil securityUtil;

    @PersistenceContext
    private EntityManager entityManager;

    @RequestMapping(value = "/regist", method = RequestMethod.POST)
    @ApiOperation(value = "注册用户")
    public Result<Object> regist(@ModelAttribute User u, @RequestParam String verify, @RequestParam String captchaId) {
        if (StrUtil.isBlank(verify) || StrUtil.isBlank(u.getUsername()) || StrUtil.isBlank(u.getPassword())) {
            return new ResultUtil<>().setErrorMsg("缺少必需表单字段");
        }
        // 验证码
        String code = redisTemplate.opsForValue().get(captchaId);
        if (StrUtil.isBlank(code)) {
            return new ResultUtil<>().setErrorMsg("验证码已过期，请重新获取");
        }
        if (!verify.toLowerCase().equals(code.toLowerCase())) {
            log.error("注册失败，验证码错误：code:" + verify + ",redisCode:" + code.toLowerCase());
            return new ResultUtil<>().setErrorMsg("验证码输入错误");
        }
        if (userService.findByUsername(u.getUsername()) != null) {
            return new ResultUtil<>().setErrorMsg("该用户名已被注册");
        }
        String encryptPass = new BCryptPasswordEncoder().encode(u.getPassword());
        u.setPassword(encryptPass);
        u.setType(CommonConstant.USER_TYPE_NORMAL);
        User user = userService.save(u);
        if (user == null) {
            return new ResultUtil<>().setErrorMsg("注册失败");
        }
        // 默认角色
        List<Role> roleList = roleService.findByDefaultRole(true);
        if (roleList != null && roleList.size() > 0) {
            for (Role role : roleList) {
                UserRole ur = new UserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(role.getId());
                iUserRoleService.save(ur);
            }
        }
        return new ResultUtil<>().setData(user);
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ApiOperation(value = "获取当前登录用户接口")
    public Result<User> getUserInfo() {
        User u = securityUtil.getCurrUser();
        // 清除持久上下文环境 避免后面语句导致持久化
        entityManager.clear();
        u.setPassword(null);
        return new ResultUtil<User>().setData(u);
    }

    @RequestMapping(value = "/unlock", method = RequestMethod.POST)
    @ApiOperation(value = "解锁验证密码")
    public Result<Object> unLock(@RequestParam String password) {
        User u = securityUtil.getCurrUser();
        if (!new BCryptPasswordEncoder().matches(password, u.getPassword())) {
            return new ResultUtil<>().setErrorMsg("密码不正确");
        }
        return new ResultUtil<>().setData(null);
    }

    @RequestMapping(value = "/resetPass", method = RequestMethod.POST)
    @ApiOperation(value = "重置密码")
    public Result<Object> resetPass(@RequestParam String[] ids) {
        for (String id : ids) {
            User u = userService.get(id);
            u.setPassword(new BCryptPasswordEncoder().encode("123456"));
            userService.update(u);
            redisTemplate.delete("user::" + u.getUsername());
        }
        return ResultUtil.success("操作成功");
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ApiOperation(value = "修改用户自己资料", notes = "用户名密码不会修改 需要username更新缓存")
    @CacheEvict(key = "#u.username")
    public Result<Object> editOwn(@ModelAttribute User u) {
        User old = securityUtil.getCurrUser();
        u.setUsername(old.getUsername());
        u.setPassword(old.getPassword());
        User user = userService.update(u);
        if (user == null) {
            return new ResultUtil<>().setErrorMsg("修改失败");
        }
        return new ResultUtil<>().setSuccessMsg("修改成功");
    }

    /**
     * @param u
     * @param roles
     * @return
     */
    @RequestMapping(value = "/admin/edit", method = RequestMethod.POST)
    @ApiOperation(value = "管理员修改资料", notes = "需要通过id获取原用户信息 需要username更新缓存")
    @CacheEvict(key = "#u.username")
    public Result<Object> edit(@ModelAttribute User u, @RequestParam(required = false) String[] roles) {
        User old = userService.get(u.getId());
        // 若修改了用户名
        if (!old.getUsername().equals(u.getUsername())) {
            // 若修改用户名删除原用户名缓存
            redisTemplate.delete("user::" + old.getUsername());
            // 判断新用户名是否存在
            if (userService.findByUsername(u.getUsername()) != null) {
                return new ResultUtil<>().setErrorMsg("该用户名已被存在");
            }
        }
        u.setPassword(old.getPassword());
        User user = userService.update(u);
        if (user == null) {
            return new ResultUtil<>().setErrorMsg("修改失败");
        }
        // 删除该用户角色
        userRoleService.deleteByUserId(u.getId());
        if (roles != null && roles.length > 0) {
            // 新角色
            for (String roleId : roles) {
                UserRole ur = new UserRole();
                ur.setRoleId(roleId);
                ur.setUserId(u.getId());
                userRoleService.save(ur);
            }
        }
        // 手动删除缓存
        redisTemplate.delete("userRole::" + u.getId());
        redisTemplate.delete("userRole::depIds:" + u.getId());
        redisTemplate.delete("userPermission::" + u.getId());
        redisTemplate.delete("permission::userMenuList:" + u.getId());
        return new ResultUtil<>().setSuccessMsg("修改成功");
    }

    /**
     * 线上demo不允许测试账号改密码
     *
     * @param password
     * @param newPass
     * @return
     */
    @RequestMapping(value = "/modifyPass", method = RequestMethod.POST)
    @ApiOperation(value = "修改密码")
    public Result<Object> modifyPass(@ApiParam("旧密码") @RequestParam String password, @ApiParam("新密码") @RequestParam String newPass) {
        User user = securityUtil.getCurrUser();
        if (!new BCryptPasswordEncoder().matches(password, user.getPassword())) {
            return new ResultUtil<>().setErrorMsg("旧密码不正确");
        }
        String newEncryptPass = new BCryptPasswordEncoder().encode(newPass);
        user.setPassword(newEncryptPass);
        userService.update(user);
        // 手动更新缓存
        redisTemplate.delete("user::" + user.getUsername());
        return new ResultUtil<>().setSuccessMsg("修改密码成功");
    }

    @RequestMapping(value = "/getByCondition", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取用户列表")
    public Result<Page<User>> getByCondition(@ModelAttribute User user, @ModelAttribute SearchVo searchVo, @ModelAttribute PageVo pageVo) {
        Page<User> page = userService.findByCondition(user, searchVo, PageUtil.initPage(pageVo));
        for (User u : page.getContent()) {
            // 关联角色
            List<Role> list = iUserRoleService.findByUserId(u.getId());
            u.setRoles(list);
            // 清除持久上下文环境 避免后面语句导致持久化
            entityManager.clear();
            u.setPassword(null);
        }
        return new ResultUtil<Page<User>>().setData(page);
    }

    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    @ApiOperation(value = "获取全部用户数据")
    public Result<List<User>> getByCondition() {
        List<User> list = userService.getAll();
        for (User u : list) {
            // 清除持久上下文环境 避免后面语句导致持久化
            entityManager.clear();
            u.setPassword(null);
        }
        return new ResultUtil<List<User>>().setData(list);
    }

    @RequestMapping(value = "/admin/add", method = RequestMethod.POST)
    @ApiOperation(value = "添加用户")
    public Result<Object> regist(@ModelAttribute User u, @RequestParam(required = false) String[] roles) {
        if (StrUtil.isBlank(u.getUsername()) || StrUtil.isBlank(u.getPassword())) {
            return new ResultUtil<>().setErrorMsg("缺少必需表单字段");
        }
        if (userService.findByUsername(u.getUsername()) != null) {
            return new ResultUtil<>().setErrorMsg("该用户名已被注册");
        }
        String encryptPass = new BCryptPasswordEncoder().encode(u.getPassword());
        u.setPassword(encryptPass);
        User user = userService.save(u);
        if (user == null) {
            return new ResultUtil<>().setErrorMsg("添加失败");
        }
        if (roles != null && roles.length > 0) {
            // 添加角色
            for (String roleId : roles) {
                UserRole ur = new UserRole();
                ur.setUserId(u.getId());
                ur.setRoleId(roleId);
                userRoleService.save(ur);
            }
        }
        return new ResultUtil<>().setData(user);
    }

    @RequestMapping(value = "/admin/disable/{userId}", method = RequestMethod.POST)
    @ApiOperation(value = "后台禁用用户")
    public Result<Object> disable(@ApiParam("用户唯一id标识") @PathVariable String userId) {
        User user = userService.get(userId);
        if (user == null) {
            return new ResultUtil<>().setErrorMsg("通过userId获取用户失败");
        }
        user.setStatus(CommonConstant.USER_STATUS_LOCK);
        userService.update(user);
        // 手动更新缓存
        redisTemplate.delete("user::" + user.getUsername());
        return new ResultUtil<>().setData(null);
    }

    @RequestMapping(value = "/admin/enable/{userId}", method = RequestMethod.POST)
    @ApiOperation(value = "后台启用用户")
    public Result<Object> enable(@ApiParam("用户唯一id标识") @PathVariable String userId) {
        User user = userService.get(userId);
        if (user == null) {
            return new ResultUtil<>().setErrorMsg("通过userId获取用户失败");
        }
        user.setStatus(CommonConstant.USER_STATUS_NORMAL);
        userService.update(user);
        // 手动更新缓存
        redisTemplate.delete("user::" + user.getUsername());
        return new ResultUtil<>().setData(null);
    }

    @RequestMapping(value = "/delByIds/{ids}", method = RequestMethod.DELETE)
    @ApiOperation(value = "批量通过ids删除")
    public Result<Object> delAllByIds(@PathVariable String[] ids) {
        for (String id : ids) {
            User u = userService.get(id);
            // 删除缓存
            redisTemplate.delete("user::" + u.getUsername());
            redisTemplate.delete("userRole::" + u.getId());
            redisTemplate.delete("userRole::depIds:" + u.getId());
            redisTemplate.delete("permission::userMenuList:" + u.getId());
            userService.delete(id);
            // 删除关联角色
            userRoleService.deleteByUserId(id);
        }
        return new ResultUtil<>().setSuccessMsg("批量通过id删除数据成功");
    }
}
