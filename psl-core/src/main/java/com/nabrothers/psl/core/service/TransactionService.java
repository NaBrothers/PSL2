package com.nabrothers.psl.core.service;

import com.nabrothers.psl.core.dao.TransactionDAO;
import com.nabrothers.psl.core.dao.UserDAO;
import com.nabrothers.psl.core.dto.UserDTO;
import com.nabrothers.psl.core.exception.TransactionException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TransactionService {

    @Resource
    private UserDAO userDAO;

    @Resource
    private TransactionDAO transactionDAO;

    public void add(Long userId, Long amount) {
        validateUser(userId);
        validateAmount(amount);
        int ret = transactionDAO.addMoneyByUserId(userId, amount);
    }

    public void deduct(Long userId, Long amount) {
        validateUser(userId);
        validateAmount(amount);
        int ret = transactionDAO.deductMoneyByUserId(userId, amount);
        if (ret < 1) {
            throw new TransactionException("账户余额不足");
        }
    }

    private void validateUser(Long userId) {
        UserDTO user = userDAO.queryByUserId(userId);
        if (user == null) {
            throw new TransactionException("找不到用户[" + userId + "]");
        }
    }

    private void validateAmount(Long amount) {
        if (amount <= 0) {
            throw new TransactionException("金额必须大于0");
        }
    }
}
