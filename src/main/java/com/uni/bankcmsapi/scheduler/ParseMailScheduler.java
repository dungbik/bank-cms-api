package com.uni.bankcmsapi.scheduler;

import com.uni.bankcmsapi.entity.H_TRANSACTION;
import com.uni.bankcmsapi.entity.M_COMPANY;
import com.uni.bankcmsapi.entity.M_DASHBOARD;
import com.uni.bankcmsapi.entity.M_MAIL;
import com.uni.bankcmsapi.model.TodayDashboard;
import com.uni.bankcmsapi.model.Transaction;
import com.uni.bankcmsapi.repository.HTransactionRepository;
import com.uni.bankcmsapi.repository.MDashboardRepository;
import com.uni.bankcmsapi.repository.MMailRepository;
import com.uni.bankcmsapi.service.MstCacheService;
import com.uni.bankcmsapi.service.NotificationService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.mail.*;
import javax.mail.search.FlagTerm;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.uni.bankcmsapi.entity.H_TRANSACTION.Bank;
import static com.uni.bankcmsapi.entity.H_TRANSACTION.TransactionType;

@Slf4j
@RequiredArgsConstructor
@Component
public class ParseMailScheduler {

    private final MstCacheService mstCacheService;
    private final HTransactionRepository hTransactionRepository;
    private final MDashboardRepository mDashboardRepository;
    private final NotificationService notificationService;

    private String HOST = "imap.gmail.com";
    List<IMAPMailService> mailServices = new ArrayList<>();

    private final MMailRepository mMailRepository;

    @PostConstruct
    void init() {
        initMailService();
    }

    @PreDestroy
    void destroyMailService() {
        if (CollectionUtils.isEmpty(mailServices)) {
            return;
        }

        mailServices.forEach(e -> {
            try {
                e.logout();
            } catch (MessagingException ex) {
                log.error("[destroyMailService] logout fail email[{}]", e.getEmail());
            }
        });
    }

    public void initMailService() {
        this.mMailRepository.findAll().forEach((e) -> {
            IMAPMailService mailService = new IMAPMailService();
            try {
                mailService.login(HOST, e.getEmail(), e.getPassword());
            } catch (Exception ex) {
                log.error("[initMailService] login fail email[{}] password[{}]", e.getEmail(), e.getPassword());
            }
            this.mailServices.add(mailService);
        });
    }

    @Async
    @Scheduled(initialDelay = 1000, fixedDelay = 1000)
    public void execute() throws Exception {
        if (CollectionUtils.isEmpty(mailServices)) {
            return;
        }

        Map<String, M_MAIL> mMailMap = this.mMailRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getEmail(), e -> e));
        Map<String, M_COMPANY> mCompanyMap = this.mstCacheService.getAllCompany().stream()
                .collect(Collectors.toMap(e -> e.getCompanyName(), e -> e));

        for (IMAPMailService mailService : mailServices) {
            if (!mailService.isLoggedIn()) {
                continue;
            }

            M_MAIL mMail = mMailMap.get(mailService.getEmail());
            if (mMail == null) {
                continue;
            }

            int lastNo = mailService.getMessageCount();
            int startNo = mMail.getLastNo() + 1;
            int cnt = lastNo - startNo + 1;
            if (cnt > 5) {
                cnt = 5;
            }
            if (cnt <= 0) {
                continue;
            }

            int endNo = startNo + cnt - 1;
            mMail.setLastNo(endNo);
            this.mMailRepository.save(mMail);

            log.info("[ParseMailScheduler] startNo[{}] endNo[{}] lastNo[{}] cnt[{}]", startNo, endNo, lastNo, cnt);

            long start = System.currentTimeMillis();
            Message[] msgArray = mailService.getMessages(startNo, endNo);
            for (int i = 0; i < cnt; i++) {
                Message msg = msgArray[i];
                if (msg == null) {
                    log.error("[ParseMailScheduler] msg is null no[{}]", startNo + i);
                    continue;
                }

                String subject = msg.getSubject();
                if (subject.contains("[메일파싱]")) {
                    String companyName = subject.split(" ")[1];
                    String content = mailService.getEmailText(msg.getContent());
                    if (content == null) {
                        log.error("[ParseMailScheduler] content is null no[{}]", startNo + i);
                        continue;
                    }

                    String[] contents = content.split("\n");
                    String name = contents[2];
                    boolean isDeposit = contents[3].contains("입금");
                    int amount = Integer.parseInt(contents[4].replaceAll("[^0-9]", ""));

                    M_COMPANY mCompany = mCompanyMap.get(companyName);
                    if (mCompany == null) {
                        log.error("[ParseMailScheduler] mCompany is null no[{}] companyName[{}]", startNo + i, companyName);
                        continue;
                    }

                    int fee = 0;
                    int balance = 0;
                    if (isDeposit) {
                        fee = (int) (amount * mCompany.getFeeRate() / 100);
                        balance = amount - fee;
                    }
                    int totalAmount = Integer.parseInt(contents[5].substring(3).replaceAll(",", ""));

                    String dateStr = LocalDateTime.now().getYear() + " " + contents[0].substring(4, 15);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM/dd HH:mm");
                    LocalDateTime dt = LocalDateTime.parse(dateStr, formatter);

                    H_TRANSACTION hTransaction = new H_TRANSACTION(
                            companyName, Bank.KB,
                                    isDeposit ? TransactionType.DEPOSIT : TransactionType.WITHDRAW,
                                    name, amount, fee, balance, totalAmount, dt);
                    Transaction tx = new Transaction(companyName, hTransaction.getBank().name(), hTransaction.getTxType().name(), hTransaction.getName(), hTransaction.getAmount(), hTransaction.getFee(), hTransaction.getTotalAmount(), hTransaction.getBalance(), hTransaction.getTxTime());
                    if (tx != null) {
                        this.notificationService.sendAll("tx", tx);
                    }

                    this.hTransactionRepository.insert(hTransaction);

                    String key = (dt.getYear() * 10000 + dt.getMonth().getValue() * 100 + dt.getDayOfMonth()) + "_" + companyName;
                    this.mDashboardRepository.updateDashboard(key, isDeposit ? amount : 0, isDeposit ? 0 : amount, fee, balance);


                    M_DASHBOARD mDashboard = this.mDashboardRepository.findById(key).orElse(null);
                    if (mDashboard != null) {
                        TodayDashboard todayDashboard = new TodayDashboard(companyName, mDashboard.getTotalDeposit(), mDashboard.getTotalWithdraw(), mDashboard.getTotalFee(), mDashboard.getTotalBalance());
                        this.notificationService.sendAll("dashboard", todayDashboard);
                    }
                }
            }
            log.info("[ParseMailScheduler] elapsedTime[{}]", System.currentTimeMillis() - start);
        }
    }

    class IMAPMailService {
        private Session session;
        private Store store;
        private Folder folder;
        private String protocol = "imaps";
        private String file = "INBOX";

        public boolean isLoggedIn() {
            return store.isConnected();
        }

        public String getEmailText(Object content) {
            if (content instanceof Multipart) {
                return null;
            }

            return (String) content;
        }

        public void login(String host, String username, String password) throws Exception {
            URLName url = new URLName(protocol, host, 993, file, username, password);
            if (session == null) {
                Properties props = null;
                try {
                    props = System.getProperties();
                } catch (SecurityException sex) {
                    props = new Properties();
                }
                session = Session.getInstance(props, null);
            }
            store = session.getStore(url);
            store.connect();
            folder = store.getFolder("inbox"); //inbox는 받은 메일함을 의미
            //folder.open(Folder.READ_WRITE);
            folder.open(Folder.READ_ONLY); //읽기 전용
        }

        public void logout() throws MessagingException {
            folder.close(false);
            store.close();
            store = null;
            session = null;
        }

        public int getMessageCount() {
            int messageCount = 0;
            try {
                messageCount = folder.getMessageCount();
            } catch (MessagingException me) {
                me.printStackTrace();
            }
            return messageCount;
        }

        public Message[] getMessages(boolean onlyNotRead) throws MessagingException {
            if (onlyNotRead) {
                return folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            } else {
                return folder.getMessages();
            }
        }

        public Message[] getMessages(int start, int end) throws MessagingException {
            return folder.getMessages(start, end);
        }

        public String getEmail() {
            return store.getURLName().getUsername();
        }
    }
}
