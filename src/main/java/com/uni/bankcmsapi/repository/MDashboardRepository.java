package com.uni.bankcmsapi.repository;

import com.uni.bankcmsapi.entity.M_DASHBOARD;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MDashboardRepository extends MongoRepository<M_DASHBOARD, String>, MDashboardExtRepository {
}

interface MDashboardExtRepository {

    void updateDashboard(String key, int deposit, int withdraw, int fee, int balance);
}

@RequiredArgsConstructor
class MDashboardExtRepositoryImpl implements MDashboardExtRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public void updateDashboard(String key, int deposit, int withdraw, int fee, int balance) {
        Criteria criteria = Criteria.where("_id").is(key);
        Query query = new Query(criteria);

        Update update = new Update();
        update.inc("totalDeposit", deposit);
        update.inc("totalWithdraw", withdraw);
        update.inc("totalFee", fee);
        update.inc("totalBalance", balance);

        this.mongoTemplate.upsert(query, update, M_DASHBOARD.class);
    }
}