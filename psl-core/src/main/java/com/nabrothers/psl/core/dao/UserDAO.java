package com.nabrothers.psl.core.dao;

import com.nabrothers.psl.core.dto.UserDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface UserDAO {
    @Insert("insert into `User` (userId,name,money) values (#{userId},#{name},#{money})")
    void insert(UserDTO userDTO);

    @Select("select * from `User` where id = #{id}")
    UserDTO queryById(Long id);

    @Select("select * from `User` where userId = #{userId}")
    UserDTO queryByUserId(Long userId);

    @Select("select * from `User` where name = #{name}")
    UserDTO queryByName(String name);

    @Select("select * from `User` where alias like \"%\"#{alias}\"%\"")
    UserDTO queryByAlias(String name);

    @Select("select * from `User`")
    List<UserDTO> queryAll();
}
