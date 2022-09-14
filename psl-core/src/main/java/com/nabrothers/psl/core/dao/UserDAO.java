package com.nabrothers.psl.core.dao;

import com.nabrothers.psl.core.dto.UserDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

@Component
public interface UserDAO {
    @Insert("insert into `User` (userId,name) values (#{userId},#{name})")
    void insert(UserDTO userDTO);

    @Select("select * from `User` where id = #{id}")
    UserDTO queryById(Long id);

    @Select("select * from `User` where userId = #{userId}")
    UserDTO queryByUserId(Long id);
}
