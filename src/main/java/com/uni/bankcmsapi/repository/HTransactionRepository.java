package com.uni.bankcmsapi.repository;

import com.uni.bankcmsapi.entity.H_TRANSACTION;
import com.uni.bankcmsapi.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public interface HTransactionRepository extends MongoRepository<H_TRANSACTION, String>, HTransactionExtRepository {
}

interface HTransactionExtRepository {

    List<Transaction> findTransaction(String companyName, String startDt, String endDt);
}

@Slf4j
@RequiredArgsConstructor
class HTransactionExtRepositoryImpl implements HTransactionExtRepository {

    private final MongoTemplate mongoTemplate;

    public List<Transaction> findTransaction(String companyName, String startDt, String endDt) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM/dd HH:mm");
        LocalDateTime startTime = LocalDateTime.parse(startDt, formatter);
        LocalDateTime endTime = LocalDateTime.parse(endDt, formatter);

        log.info("[findTransaction] startTime[{}] endTime[{}] companyName[{}]", startTime, endTime, companyName);

        Query query = new Query()
                .addCriteria(Criteria.where("txTime").gte(startTime).lte(endTime))
                .addCriteria(Criteria.where("companyName").is(companyName));
        query.with(Sort.by(Sort.Direction.DESC, "txTime"));

        List<H_TRANSACTION> list = this.mongoTemplate.find(query, H_TRANSACTION.class);
        List<Transaction> transactionList = list.stream()
                .map(e -> new Transaction(e.getBank().name(), e.getTxType().name(), e.getName(), e.getAmount(), e.getFee(), e.getTotalAmount(), e.getBalance(), e.getTxTime())).collect(Collectors.toList());
        return transactionList;

    }
}