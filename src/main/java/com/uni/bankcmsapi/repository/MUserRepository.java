package com.uni.bankcmsapi.repository;

import com.uni.bankcmsapi.entity.M_USER;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MUserRepository extends MongoRepository<M_USER, String>, MUserExtRepository {
    Optional<M_USER> findByUsername(String username);
}

interface MUserExtRepository {
}

@RequiredArgsConstructor
class MUserExtRepositoryImpl implements MUserExtRepository {

    private final MongoTemplate mongoTemplate;

}