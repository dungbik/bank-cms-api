package com.uni.bankcmsapi.scheduler;

import com.uni.bankcmsapi.entity.H_TRANSACTION;
import com.uni.bankcmsapi.entity.M_DASHBOARD;
import com.uni.bankcmsapi.entity.M_MAIL;
import com.uni.bankcmsapi.model.Company;
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
    private final MMailRepository mMailRepository;

    private String HOST = "imap.gmail.com";
    List<IMAPMailService> mailServices = new ArrayList<>();
    private boolean destroyed = false;


    @PostConstruct
    void init() {
        initMailService();
    }

    @PreDestroy
    void destroy() {
        destroyed = true;

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
        this.mailServices.clear();
        this.mMailRepository.findAll().forEach((e) -> {
            IMAPMailService mailService = new IMAPMailService();
            loginMailService(mailService, e.getEmail(), e.getPassword());
            this.mailServices.add(mailService);
        });
    }

    public void loginMailService(IMAPMailService mailService, String email, String password) {
        try {
            mailService.login(HOST, email, password);
        } catch (Exception ex) {
            log.error("[loginMailService] login fail email[{}] password[{}]", email, password);
        }
    }

    @Async
    @Scheduled(initialDelay = 5000, fixedDelay = 3000)
    public void execute() throws Exception {

        if (destroyed) {
            return;
        }

        if (CollectionUtils.isEmpty(mailServices)) {
            return;
        }

        Map<String, M_MAIL> mMailMap = this.mMailRepository.findAll().stream()
                .collect(Collectors.toMap(e -> e.getEmail(), e -> e));
        Map<String, Company> companyMap = this.mstCacheService.getAllCompany().stream()
                .collect(Collectors.toMap(e -> e.getCompanyName(), e -> e));

        for (IMAPMailService mailService : mailServices) {
            M_MAIL mMail = mMailMap.get(mailService.getEmail());
            if (mMail == null) {
                continue;
            }

            if (!mailService.isLoggedIn()) {
                loginMailService(mailService, mMail.getEmail(), mMail.getPassword());
            }

            int lastNo = 0;
            int retryCnt = 3;
            while (retryCnt-- > 0) {
                try {
                    lastNo = mailService.getMessageCount();
                    break;
                } catch (Exception ex) {
                    log.warn("[ParseMailScheduler] getMessageCount fail ex[{}]", ex);
                    loginMailService(mailService, mMail.getEmail(), mMail.getPassword());
                }
            }
            mailService.getMessageCount();
            int startNo = mMail.getLastNo() + 1;
            int cnt = lastNo - startNo + 1;
            if (cnt > 2) {
                cnt = 2;
            }
            if (cnt <= 0) {
                continue;
            }

            int endNo = startNo + cnt - 1;
            mMail.setLastNo(endNo);
            this.mMailRepository.save(mMail);

            log.info("[ParseMailScheduler] startNo[{}] endNo[{}] lastNo[{}] cnt[{}]", startNo, endNo, lastNo, cnt);

            long start = System.currentTimeMillis();
            Message[] msgArray = null;

            retryCnt = 3;
            while (retryCnt-- > 0) {
                try {
                    msgArray = mailService.getMessages(startNo, endNo);
                    break;
                } catch (Exception ex) {
                    log.warn("[ParseMailScheduler] getMessages fail ex[{}]", ex);
                    loginMailService(mailService, mMail.getEmail(), mMail.getPassword());
                }
            }

            if (msgArray == null) {
                log.warn("[ParseMailScheduler] msgArray is null");
                continue;
            }

            for (int i = 0; i < cnt; i++) {
                Message msg = msgArray[i];
                if (msg == null) {
                    log.error("[ParseMailScheduler] msg is null no[{}]", startNo + i);
                    continue;
                }

                String subject = msg.getSubject();
                if (subject.contains("[메일파싱]")) {
                    String[] splitSubject = subject.split("\\s+");
                    String companyName = splitSubject[splitSubject.length - 1];
                    String content = mailService.getEmailText(msg.getContent());
                    if (content == null) {
                        log.error("[ParseMailScheduler] content is null no[{}]", startNo + i);
                        continue;
                    }

                    String[] contents = content.split("\n");

                    Bank bank = null;
                    for (Bank b : Bank.values()) {
                        if (contents[0].contains(b.name())) {
                            bank = b;
                            break;
                        }
                    }

                    if (bank == null) {
                        log.error("[ParseMailScheduler] unknown bank no[{}] companyName[{}] content[{}]", startNo + i, companyName, content);
                        continue;
                    }

                    Company company = companyMap.get(companyName);
                    if (company == null) {
                        log.error("[ParseMailScheduler] company is null no[{}] subject[{}] companyName[{}] content[{}]", startNo + i, subject, companyName, content);
                        continue;
                    }

                    String name = null;
                    boolean isDeposit = false;
                    int amount = 0;
                    int fee = 0;
                    int balance = 0;
                    int totalAmount = 0;
                    String dateTimeStr = null;

                    if (bank.equals(Bank.KB)) {
                        name = contents[2];
                        isDeposit = contents[3].contains("입금");
                        amount = Integer.parseInt(contents[4].replaceAll("[^0-9]", ""));

                        if (isDeposit) {
                            fee = (int) (amount * company.getFeeRate() / 100);
                            balance = amount - fee;
                        }
                        totalAmount = Integer.parseInt(contents[5].replaceAll("[^0-9]", ""));

                        dateTimeStr = LocalDateTime.now().getYear() + " " + contents[0].substring(4, 15);
                    } else if (bank.equals(Bank.신협)) {
                        String[] firstLineSplit = contents[0].split(" ");

                        String dateStr = firstLineSplit[1];
                        String timeStr = firstLineSplit[2];
                        isDeposit = firstLineSplit[3].contains("입금");
                        dateTimeStr = LocalDateTime.now().getYear() + " " + dateStr + " " + timeStr;

                        amount = Integer.parseInt(firstLineSplit[4].replaceAll("[^0-9]", ""));
                        name = firstLineSplit[5];
                        totalAmount = Integer.parseInt(firstLineSplit[6].replaceAll("[^0-9]", ""));

                        if (isDeposit) {
                            fee = (int) (amount * company.getFeeRate() / 100);
                            balance = amount - fee;
                        }
                    } else if (bank.equals(Bank.광주)) {
                        isDeposit = contents[2].contains("입금");
                        dateTimeStr = LocalDateTime.now().getYear() + " " + contents[1].trim();

                        amount = Integer.parseInt(contents[2].replaceAll("[^0-9]", ""));
                        name = contents[4];
                        totalAmount = Integer.parseInt(contents[3].replaceAll("[^0-9]", ""));

                        if (isDeposit) {
                            fee = (int) (amount * company.getFeeRate() / 100);
                            balance = amount - fee;
                        }
                    }

                    if (name == null || dateTimeStr == null) {
                        log.error("[ParseMailScheduler] parse error no[{}] companyName[{}] name[{}] dateTimeStr[{}] content[{}]", startNo + i, companyName, name, dateTimeStr, content);
                        continue;
                    }

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM/dd HH:mm");
                    LocalDateTime dt = LocalDateTime.now();
                    try {
                        dt = LocalDateTime.parse(dateTimeStr, formatter);
                    } catch (Exception ex) {
                        log.error("[ParseMailScheduler] date parse error dateTimeStr[{}]", dateTimeStr);
                    }

                    H_TRANSACTION hTransaction = new H_TRANSACTION(
                            null, companyName, bank,
                                    isDeposit ? TransactionType.DEPOSIT : TransactionType.WITHDRAW,
                                    name, amount, fee, balance, totalAmount, dt);

                    this.hTransactionRepository.insert(hTransaction);

                    Transaction tx = new Transaction(hTransaction.getId(), companyName, hTransaction.getBank().name(), hTransaction.getTxType().name(), hTransaction.getName(), hTransaction.getAmount(), hTransaction.getFee(), hTransaction.getTotalAmount(), hTransaction.getBalance(), hTransaction.getTxTime());
                    if (tx != null) {
                        this.notificationService.sendAll("tx", tx);
                    }

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

        public int getMessageCount() throws MessagingException {
            return folder.getMessageCount();
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
