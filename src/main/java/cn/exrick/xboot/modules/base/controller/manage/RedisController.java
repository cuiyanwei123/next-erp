package cn.exrick.xboot.modules.base.controller.manage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.exrick.xboot.common.utils.PageUtil;
import cn.exrick.xboot.common.utils.ResultUtil;
import cn.exrick.xboot.common.vo.PageVo;
import cn.exrick.xboot.common.vo.Result;
import cn.exrick.xboot.common.vo.SearchVo;
import cn.exrick.xboot.modules.base.vo.RedisInfo;
import cn.exrick.xboot.modules.base.vo.RedisVo;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author Exrickx
 */
@Slf4j
@RestController
@Api(description = "Redis缓存管理接口")
@RequestMapping("/erp/redis")
@Transactional
@ApiIgnore()
public class RedisController {

    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @RequestMapping(value = "/getAllByPage", method = RequestMethod.GET)
    @ApiOperation(value = "分页获取全部")
    public Result<Page<RedisVo>> getAllByPage(@RequestParam(required = false) String key, @ModelAttribute SearchVo searchVo,
            @ModelAttribute PageVo pageVo) {
        List<RedisVo> list = new ArrayList<>();
        if (StrUtil.isNotBlank(key)) {
            key = "*" + key + "*";
        } else {
            key = "*";
        }
        for (String s : redisTemplate.keys(key)) {
            RedisVo redisVo = new RedisVo(s, "");
            list.add(redisVo);
        }
        Page<RedisVo> page = new PageImpl<RedisVo>(PageUtil.listToPage(pageVo, list), PageUtil.initPage(pageVo), list.size());
        page.getContent().forEach(e -> {
            String value = "";
            try {
                value = redisTemplate.opsForValue().get(e.getKey());
                if (value.length() > 150) {
                    value = value.substring(0, 149) + "...";
                }
            } catch (Exception exception) {
                value = "非字符格式数据";
            }
            e.setValue(value);
        });
        return new ResultUtil<Page<RedisVo>>().setData(page);
    }

    @RequestMapping(value = "/getByKey/{key}", method = RequestMethod.GET)
    @ApiOperation(value = "通过key获取")
    public Result<Object> getByKey(@PathVariable String key) {
        String value = redisTemplate.opsForValue().get(key);
        return new ResultUtil<>().setData(value);
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ApiOperation(value = "添加或编辑")
    public Result<Object> save(@RequestParam String key, @RequestParam String value) {
        redisTemplate.opsForValue().set(key, value);
        return new ResultUtil<>().setSuccessMsg("删除成功");
    }

    @RequestMapping(value = "/delByKeys", method = RequestMethod.DELETE)
    @ApiOperation(value = "批量删除")
    public Result<Object> delByKeys(@RequestParam String[] keys) {
        for (String key : keys) {
            redisTemplate.delete(key);
        }
        return new ResultUtil<>().setSuccessMsg("删除成功");
    }

    @RequestMapping(value = "/delAll", method = RequestMethod.DELETE)
    @ApiOperation(value = "全部删除")
    public Result<Object> delAll() {
        redisTemplate.delete(redisTemplate.keys("*"));
        return new ResultUtil<>().setSuccessMsg("删除成功");
    }

    @RequestMapping(value = "/getKeySize", method = RequestMethod.GET)
    @ApiOperation(value = "获取实时key大小")
    public Result<Object> getKeySize() {
        Map<String, Object> map = new HashMap<>(16);
        Jedis jedis = jedisPool.getResource();
        map.put("keySize", jedis.dbSize());
        map.put("time", DateUtil.format(new Date(), "HH:mm:ss"));
        if (jedis != null) {
            jedis.close();
        }
        return new ResultUtil<>().setData(map);
    }

    @RequestMapping(value = "/getMemory", method = RequestMethod.GET)
    @ApiOperation(value = "获取实时内存大小")
    public Result<Object> getMemory() {
        Map<String, Object> map = new HashMap<>(16);
        Jedis jedis = jedisPool.getResource();
        String[] strs = jedis.info().split("\n");
        for (String s : strs) {
            String[] detail = s.split(":");
            if ("used_memory".equals(detail[0])) {
                map.put("memory", detail[1].substring(0, detail[1].length() - 1));
                break;
            }
        }
        map.put("time", DateUtil.format(new Date(), "HH:mm:ss"));
        if (jedis != null) {
            jedis.close();
        }
        return new ResultUtil<>().setData(map);
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ApiOperation(value = "获取Redis信息")
    public Result<Object> info() {
        List<RedisInfo> infoList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>(16);
        Jedis jedis = jedisPool.getResource();
        String[] strs = jedis.info().split("\n");
        for (String str1 : strs) {
            RedisInfo redisInfo = new RedisInfo();
            String[] str = str1.split(":");
            if (str.length > 1) {
                String key = str[0];
                String value = str[1];
                redisInfo.setKey(key);
                redisInfo.setValue(value);
                infoList.add(redisInfo);
            }
        }
        if (jedis != null) {
            jedis.close();
        }
        return new ResultUtil<>().setData(infoList);
    }
}
