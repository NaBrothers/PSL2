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
    @Insert("insert into `BilliardRecord` (gameid,gametype,winnerid,loserid,scorew,scorel)" +
            "values (#{gameId},#{gameType},#{winnerId},#{loserId},#{scoreW},#{scoreL})")
    void insert(BilliardRecordDTO billiardRecordDTO);

    @Select("select * from `BilliardRecord` where gameid = #{gameId}")
    List<BilliardRecordDTO> queryByGameId(Long gameId);

    @Select("select * from `BilliardRecord` where id = #{id}")
    BilliardRecordDTO queryById(Long id);

    @Select("select * from `BilliardRecord`")
    List<BilliardRecordDTO> queryAll();

    @Select("select * from `BilliardRecord` where gameid + #{n} > (select max(gameid) from `BilliardRecord`)")
    List<BilliardRecordDTO> queryLastN(Integer n);

    @Select("select * from `BilliardRecord` where gametype=4")
    List<BilliardRecordDTO> queryFriendly();

    @Select("select * from `BilliardRecord` where gametype<4")
    List<BilliardRecordDTO> queryChampionship();

    @Select("select * from `BilliardRecord` where gametype >= #{min} and gametype <= #{max}")
    List<BilliardRecordDTO> queryGameTypeScope(@Param("min") Integer min, @Param("max") Integer max);

    @Delete("delete * from 'BilliardRecord' where id = #{id}")
    void deleteById(Long id);
}
