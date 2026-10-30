package com.spring.aurora.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.spring.aurora.dao.DebtDao;
import com.spring.aurora.model.ArSummary;
import com.spring.aurora.model.ArSummaryRepository;
import com.spring.aurora.model.Debt;

import lombok.AllArgsConstructor;

@Service("debtService")
@Transactional
@AllArgsConstructor
public class DebtServiceImpl implements DebtService {

    private ArSummaryRepository arSummaryRepo;
    private DebtDao debtDao;

    @Override
    public Debt insert(Debt debt) {
        ArSummary current = this.arSummaryRepo.findByCustomerId(debt.getCustomerId()).orElseGet(ArSummary::new);
        if (current.getCustomerId() != debt.getCustomerId()) {
            current.setCustomerId(debt.getCustomerId());
        }
        BigDecimal arAmount = current.getArAmount().add(new BigDecimal(debt.getAmount()));
        current.setArAmount(arAmount);
        this.arSummaryRepo.save(current);
        return this.debtDao.save(debt);
    }

    @Override
    public Debt insert(Debt debt, long previousOrderId) {
        List<Debt> currentDebt = this.debtDao.findByCustomerIdAndOrderId(debt.getCustomerId(), previousOrderId);
        double currentAmount = currentDebt.size() > 0 ? currentDebt.get(0).getAmount() : 0.00;
        ArSummary current = this.arSummaryRepo.findByCustomerId(debt.getCustomerId()).orElseGet(ArSummary::new);
        if (current.getCustomerId() != debt.getCustomerId()) {
            current.setCustomerId(debt.getCustomerId());
        }
        BigDecimal arAmount = current.getArAmount().subtract(new BigDecimal(currentAmount)).add(new BigDecimal(debt.getAmount()));
        current.setArAmount(arAmount);
        current.setUpdatedAt(LocalDateTime.now());

        Debt saveDebt = currentDebt.size() > 0 ? currentDebt.get(0) : debt;
        debt.setDebtId(saveDebt.getDebtId());
        this.arSummaryRepo.save(current);
        return this.debtDao.save(debt);
    }

    @Override
    public void delete(Debt debt) {
        this.debtDao.delete(debt);
    }

    @Override
    public List<Debt> findAllByCustomerId(long customerId) {
        return this.debtDao.findAllByCustomerId(customerId);
    }

    @Override
    public List<Debt> findAllByCustomerIdAndDate(long customerId, Date date) {
        return new ArrayList<>();
    }

    @Override
    public double findDebtsTotalByCustomerId(long customerId) {
        List<Debt> debts = this.debtDao.findAllByCustomerId(customerId);
        double total = debts.stream().mapToDouble(Debt::getAmount).sum();
        return total;
    }

    @Override
    public double findTotalARs() {
        List<Debt> debts = new ArrayList<>();
        this.debtDao.findAll().forEach(debts::add);
        double total = debts.stream().mapToDouble(Debt::getAmount).sum();
        return total;
    }
}
