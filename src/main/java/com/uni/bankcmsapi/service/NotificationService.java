package com.uni.bankcmsapi.service;

import com.uni.bankcmsapi.component.UserComponent;
import com.uni.bankcmsapi.entity.H_TRANSACTION;
import com.uni.bankcmsapi.entity.M_USER;
import com.uni.bankcmsapi.model.TodayDashboard;
import com.uni.bankcmsapi.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final UserComponent userComponent;
    private final MstCacheService mstCacheService;

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe() {

        M_USER mUser = userComponent.getUser();
        if (mUser == null) {
            return null;
        }

        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);

        String username = mUser.getUsername();
        String userKey = username + "_" + System.currentTimeMillis();
        this.emitters.put(userKey, emitter);

//        log.info("[NotificationService] new emitter added emitter[{}] totalSize[{}]", emitter, this.emitters.size());
        emitter.onCompletion(() -> {
            this.emitters.remove(userKey);
//            log.info("[NotificationService] onCompletion userKey[{}] totalSize[{}]", userKey, this.emitters.size());
        });
        emitter.onTimeout(() -> {
//            log.info("[NotificationService] onTimeout");
            emitter.complete();
        });

        sendToClient(emitter, userKey, "subscribe", "subscribe");
        return emitter;
    }

    public void sendAll(String name, Object data) {
        log.info("[NotificationService] sendAll name[{}] data[{}]", name, data);
        this.emitters.entrySet().forEach(map -> {
            String username = map.getKey().split("_")[0];

            if (data instanceof Transaction) {
                M_USER mUser = mstCacheService.findByUsername(username);
                Transaction tx = ((Transaction) data);

                log.info("[NotificationService] Transaction mUser.getCompanyList()[{}] companyName[{}]", mUser.getCompanyList(), tx.getCompanyName());
                if (mUser.hasCompany(tx.getCompanyName())) {
                    log.info("[NotificationService] call sendToClient name[{}] tx[{}]", name, tx);
                    sendToClient(map.getValue(), map.getKey(), name, tx);
                }
            } else if (data instanceof TodayDashboard) {
                M_USER mUser = mstCacheService.findByUsername(username);
                TodayDashboard todayDashboard = ((TodayDashboard) data);
                log.info("[NotificationService] TodayDashboard mUser.getCompanyList()[{}] companyName[{}]", mUser.getCompanyList(), todayDashboard.getCompanyName());
                if (mUser.hasCompany(todayDashboard.getCompanyName())) {
                    log.info("[NotificationService] call sendToClient name[{}] todayDashboard[{}]", name, todayDashboard);
                    sendToClient(map.getValue(), map.getKey(), name, todayDashboard);
                }

            }
        });
    }

    private void sendToClient(SseEmitter emitter, String userKey, String name, Object data) {
        try {
            log.info("[NotificationService] sendToClient emitter[{}] name[{}] data[{}]", emitter, name, data);
            emitter.send(SseEmitter.event()
                    .name(name)
                    .data(data));
        } catch (Exception ex) {
            this.emitters.remove(userKey);
            log.error("[NotificationService] sendToClient fail emitter[{}] name[{}] data[{}]", emitter, name, data);
        }
    }
}
