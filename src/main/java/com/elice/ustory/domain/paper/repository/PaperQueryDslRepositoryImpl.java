package com.elice.ustory.domain.paper.repository;

import com.elice.ustory.domain.address.Address;
import com.elice.ustory.domain.address.AddressRecommendDTO;
import com.elice.ustory.domain.address.QAddress;
import com.elice.ustory.domain.diaryUser.entity.QDiaryUser;
import com.elice.ustory.domain.paper.entity.Paper;
import com.elice.ustory.domain.paper.entity.QPaper;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.elice.ustory.domain.address.QAddress.address;


@Repository
@RequiredArgsConstructor
@Slf4j
public class PaperQueryDslRepositoryImpl implements PaperQueryDslRepository {

    private static final QPaper paper = QPaper.paper;
    private final JPAQueryFactory queryFactory;
    @Override
    public List<Paper> findAllByDiaryIdAndDateRange(Long diaryId, LocalDateTime requestTime, Pageable pageable, LocalDate startDate, LocalDate endDate) {
        return queryFactory.selectFrom(paper)
                .where(paper.diary.id.eq(diaryId),
                        startDateCondition(startDate),
                        endDateCondition(endDate),
                        paper.createdAt.loe(requestTime),
                        paper.deletedAt.isNull())
                .orderBy(paper.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression startDateCondition(LocalDate startDate) {
        return startDate != null ? paper.createdAt.goe(startDate.atStartOfDay()) : null;
    }

    private BooleanExpression endDateCondition(LocalDate endDate) {
        return endDate != null ? paper.createdAt.loe(endDate.plusDays(1).atStartOfDay().minusNanos(1)) : null;
    }

    @Override
    public List<Paper> findAllPapersByUserId(Long userId) {
        QDiaryUser diaryUser = QDiaryUser.diaryUser;

        return queryFactory.selectFrom(paper)
                .where(paper.diary.id.in(
                                JPAExpressions.select(diaryUser.id.diary.id)
                                        .from(diaryUser)
                                        .where(diaryUser.id.users.id.eq(userId))
                        ),
                        paper.deletedAt.isNull()
                )
                .orderBy(paper.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Paper> findByWriterId(Long writerId, LocalDateTime requestTime, Pageable pageable) {
        return queryFactory.selectFrom(paper)
                .where(paper.writer.id.eq(writerId),
                        paper.createdAt.loe(requestTime),
                        paper.deletedAt.isNull())
                .orderBy(paper.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<Paper> joinPaperByAddress(AddressRecommendDTO addressRecommendDTO) {

        return queryFactory
                .select(paper)
                .from(paper)
                .where(paper.address.city.eq(addressRecommendDTO.getCity()),
                        paper.address.store.eq(addressRecommendDTO.getStore()))
                .orderBy(paper.id.desc())
                .fetch();
    }

}
