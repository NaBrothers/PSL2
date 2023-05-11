package com.nabrothers.psl.core.dao;

import com.nabrothers.psl.core.dto.BilliardRecordDTO;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BilliardRecordDAO {
    @Insert("insert into `BilliardRecord` (gameid,gametype,winnerid,loserid,scorew,scorel)" +
            "values (#{gameId},#{gameType},#{winnerId},#{loserId},#{scoreW},#{scoreL})")
    void insert(BilliardRecordDTO billiardRecordDTO);

    @Select("select * from `BilliardRecord` where gameid = #{gameId}")
    List<BilliardRecordDTO> queryByGameId(Long gameId);

    @Select("select * from `BilliardRecord` where id = #{id}")
    BilliardRecordDTO queryById(Long id);

    @Select("select * from `BilliardRecord`")
    List<BilliardRecordDTO> queryAll();

    @Delete("delete * from 'BilliardRecord' where id = #{id}")
    void deleteById(Long id);
}
