package com.prgrms.tenwonmoa.domain.accountbook.service;

import static com.prgrms.tenwonmoa.exception.message.Message.*;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prgrms.tenwonmoa.domain.accountbook.Income;
import com.prgrms.tenwonmoa.domain.accountbook.dto.income.FindIncomeResponse;
import com.prgrms.tenwonmoa.domain.accountbook.repository.IncomeRepository;
import com.prgrms.tenwonmoa.domain.user.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IncomeService {

	private final IncomeRepository incomeRepository;

	@Transactional
	public Long save(Income income) {
		return incomeRepository.save(income).getId();
	}

	public FindIncomeResponse findIncome(Long incomeId, User authUser) {
		Income findIncome = findById(incomeId);
		authUser.validateLogin(findIncome.getUser());

		return FindIncomeResponse.of(findIncome);
	}

	public Income findById(Long incomeId) {
		return incomeRepository.findById(incomeId)
			.orElseThrow(() -> new NoSuchElementException(INCOME_NOT_FOUND.getMessage()));
	}

	public void deleteById(Long incomeId) {
		incomeRepository.deleteById(incomeId);
	}

	public void setUserCategoryNull(Long userCategoryId) {
		incomeRepository.updateUserCategoryAsNull(userCategoryId);
	}
}
