package com.nabrothers.psl.core.dao;

import com.nabrothers.psl.core.dto.BetRecordDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BetRecordDAO {
    @Insert("insert into `BetRecord` (userId,matchId,win,draw,lose,expect,amount)" +
            "values (#{userId},#{matchId},#{win},#{draw},#{lose},#{expect},#{amount})")
    void insert(BetRecordDTO betRecordDTO);

    @Select("select * from `BetRecord` where id = #{id}")
    BetRecordDTO queryById(Long id);

    @Select("select * from `BetRecord` where matchId = #{matchId}")
    List<BetRecordDTO> queryByMatchId(Long matchId);

    @Select("select * from `BetRecord` where userId = #{userId}")
    List<BetRecordDTO> queryByUserId(Long userId);

    @Update("update `BetRecord` set result = #{result} where id = #{id}")
    void updateResultById(@Param("result") Integer result, @Param("id") Long id);
}
