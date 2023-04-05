package com.nabrothers.psl.core.dao;

import com.nabrothers.psl.core.dto.BilliardRecordDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BilliardRecordDAO {
    @Insert("insert into `BilliardRecord` (gametype,winnerid,loserid,scorew,scorel)" +
            "values (#{gameType},#{winnerId},#{loserId},#{scoreW},#{scoreL})")
    void insert(BilliardRecordDTO billiardRecordDTO);

    // @Select("select * from `BilliardRecord` where winnerid regexp #{userid} or loserid regexp #{userid}")
    // BilliardRecordDTO queryByUserId(char[] userid);

    @Select("select * from `BilliardRecord` where gametype = #{type}")
    List<BilliardRecordDTO> queryByGameType(Integer type);

    @Select("select * from `BilliardRecord` where id = #{id}")
    BilliardRecordDTO queryById(Long id);

    @Select("select * from `BilliardRecord`")
    List<BilliardRecordDTO> queryAll();
}
