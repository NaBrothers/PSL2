package com.nabrothers.psl.core.dao;

import com.nabrothers.psl.core.dto.BilliardGameDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BilliardGameDAO {
    @Insert("insert into `BilliardGame` (name,location,players,remark)" +
            "values (#{name},#{location},#{players},#{remark})")
    void insert(BilliardGameDTO billiardGameDTO);

    @Select("select * from `BilliardGame` where id = #{id}")
    BilliardGameDTO queryById(Long id);

    @Select("select * from `BilliardGame`")
    List<BilliardGameDTO> queryAll();
}
