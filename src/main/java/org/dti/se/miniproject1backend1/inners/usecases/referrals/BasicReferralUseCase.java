package org.dti.se.miniproject1backend1.inners.usecases.referrals;

import org.dti.se.miniproject1backend1.inners.models.entities.Account;
import org.dti.se.miniproject1backend1.inners.models.entities.AccountVoucher;
import org.dti.se.miniproject1backend1.inners.models.entities.Point;
import org.dti.se.miniproject1backend1.inners.models.entities.Voucher;
import org.dti.se.miniproject1backend1.outers.exceptions.referrals.ReferralCodeNotFoundException;
import org.dti.se.miniproject1backend1.outers.repositories.ones.AccountRepository;
import org.dti.se.miniproject1backend1.outers.repositories.ones.AccountVoucherRepository;
import org.dti.se.miniproject1backend1.outers.repositories.ones.PointRepository;
import org.dti.se.miniproject1backend1.outers.repositories.ones.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class BasicReferralUseCase {

    @Autowired
    PointRepository pointRepository;

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    AccountVoucherRepository accountVoucherRepository;

    @Autowired
    AccountRepository accountRepository;

    public Mono<Void> claimReferralCode(String referralCode, Account claimerAccount) {
        return accountRepository
                .findAccountByReferralCode(referralCode)
                .switchIfEmpty(Mono.error(new ReferralCodeNotFoundException()))
                .flatMap(ownerAccount -> {
                    Point point = Point
                            .builder()
                            .id(UUID.randomUUID())
                            .accountId(ownerAccount.getId())
                            .fixedAmount(10000.0)
                            .endedAt(OffsetDateTime.now().plusMonths(3).truncatedTo(ChronoUnit.MICROS))
                            .build();
                    Voucher voucher = Voucher
                            .builder()
                            .id(UUID.randomUUID())
                            .name("Referral Voucher")
                            .description("Acquired from referral code: " + referralCode)
                            .variableAmount(0.10)
                            .startedAt(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS))
                            .endedAt(OffsetDateTime.now().plusMonths(3).truncatedTo(ChronoUnit.MICROS))
                            .build();
                    AccountVoucher accountVoucher = AccountVoucher
                            .builder()
                            .id(UUID.randomUUID())
                            .accountId(claimerAccount.getId())
                            .voucherId(voucher.getId())
                            .build();

                    return Mono
                            .zip(
                                    pointRepository.save(point),
                                    voucherRepository.save(voucher),
                                    accountVoucherRepository.save(accountVoucher)
                            );
                })
                .then();


    }


}
