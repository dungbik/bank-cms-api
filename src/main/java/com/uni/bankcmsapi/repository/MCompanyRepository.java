package com.uni.bankcmsapi.repository;

import com.uni.bankcmsapi.entity.M_COMPANY;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MCompanyRepository extends MongoRepository<M_COMPANY, String>, MCompanyExtRepository {
}

interface MCompanyExtRepository {
}

@RequiredArgsConstructor
class MCompanyExtRepositoryImpl implements MCompanyExtRepository {

    private final MongoTemplate mongoTemplate;

}