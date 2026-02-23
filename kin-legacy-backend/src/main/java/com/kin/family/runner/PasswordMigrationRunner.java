package com.kin.family.runner;

import com.kin.family.entity.User;
import com.kin.family.mapper.UserMapper;
import com.kin.family.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 密码迁移运行器
 * 启动时自动检测并加密明文密码
 *
 * @author candong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UserMapper userMapper;

    @Override
    public void run(String... args) {
        log.info("开始检查用户密码加密状态...");

        List<User> users = userMapper.selectList(null);
        int migratedCount = 0;

        for (User user : users) {
            String password = user.getPassword();
            if (password != null && !PasswordUtil.isEncoded(password)) {
                String encoded = PasswordUtil.encode(password);
                user.setPassword(encoded);
                userMapper.updateById(user);
                migratedCount++;
                log.info("已加密用户 {} 的密码", user.getUsername());
            }
        }

        if (migratedCount > 0) {
            log.info("密码迁移完成，共迁移 {} 个用户", migratedCount);
        } else {
            log.info("所有用户密码已加密，无需迁移");
        }
    }
}
