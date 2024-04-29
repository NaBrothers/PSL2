package com.nabrothers.psl.core.dao;

import com.nabrothers.psl.core.dto.BilliardRecordDTO;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BilliardRecordDAO {
    public static final Long season = 1L;

    @Insert("insert into `BilliardRecord` (gameid,gametype,winnerid,loserid,scorew,scorel,season)" +
            "values (#{gameId},#{gameType},#{winnerId},#{loserId},#{scoreW},#{scoreL},#{season})")
    void insert(BilliardRecordDTO billiardRecordDTO);

    @Select("select * from `BilliardRecord` where gameid = #{gameId}")
    List<BilliardRecordDTO> queryByGameId(Long gameId);

    @Select("select * from `BilliardRecord` where id = #{id}")
    BilliardRecordDTO queryById(Long id);

    @Select("select * from `BilliardRecord` where season=#{season}")
    List<BilliardRecordDTO> queryAll(Long season);

    @Select("select * from `BilliardRecord` where gameid + #{n} > (select max(gameid) from `BilliardRecord`) and season=#{season}")
    List<BilliardRecordDTO> queryLastN(Integer n, Long season);

    @Select("select * from `BilliardRecord` where gametype=4 and season=#{season}")
    List<BilliardRecordDTO> queryFriendly(Long season);

    @Select("select * from `BilliardRecord` where gametype<4 and season=#{season}")
    List<BilliardRecordDTO> queryChampionship(Long season);

    @Select("select * from `BilliardRecord` where gametype >= #{min} and gametype <= #{max} and season=#{season}")
    List<BilliardRecordDTO> queryGameTypeScope(@Param("min") Integer min, @Param("max") Integer max, Long season);

    @Delete("delete * from 'BilliardRecord' where id = #{id}")
    void deleteById(Long id);
}
