package com.nabrothers.psl.core.dao;

import com.nabrothers.psl.core.dto.BilliardGameDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BilliardGameDAO {
    @Insert("insert into `BilliardGame` (name,location,players,date,remark)" +
            "values (#{name},#{location},#{players},#{date},#{remark})")
    @Options(useGeneratedKeys = true, keyColumn = "id")
    Long insert(BilliardGameDTO billiardGameDTO);

    @Select("select * from `BilliardGame` where id = #{id}")
    BilliardGameDTO queryById(Long id);

    @Select("select * from `BilliardGame`")
    List<BilliardGameDTO> queryAll();
}
