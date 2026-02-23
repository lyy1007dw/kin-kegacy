package com.kin.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kin.family.entity.UserGenealogy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户-家谱关联 Mapper
 *
 * @author candong
 */
@Mapper
public interface UserGenealogyMapper extends BaseMapper<UserGenealogy> {

    @Select("SELECT COUNT(*) FROM user_genealogy WHERE genealogy_id = #{genealogyId} AND role = 'ADMIN'")
    int countAdmins(@Param("genealogyId") Long genealogyId);

    @Select("SELECT * FROM user_genealogy WHERE user_id = #{userId} AND genealogy_id = #{genealogyId}")
    UserGenealogy selectByUserAndGenealogy(@Param("userId") Long userId, @Param("genealogyId") Long genealogyId);
}
