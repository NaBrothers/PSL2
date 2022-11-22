package com.nabrothers.psl.core.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

@Component
public interface TransactionDAO {
    @Update("update `User` set money = money + #{amount} where userId = #{userId}")
    Integer addMoneyByUserId(@Param("userId") Long userId, @Param("amount") Long amount);

    @Update("update `User` set money = money - #{amount} where userId = #{userId} and money - #{amount} >= 0")
    Integer deductMoneyByUserId(@Param("userId") Long userId, @Param("amount") Long amount);
}
