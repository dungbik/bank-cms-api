package com.uni.bankcmsapi.repository;

import com.uni.bankcmsapi.entity.M_MAIL;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MMailRepository extends MongoRepository<M_MAIL, String>, MMailExtRepository {
}

interface MMailExtRepository {
}

@RequiredArgsConstructor
class MMailExtRepositoryImpl implements MMailExtRepository {

    private final MongoTemplate mongoTemplate;

}