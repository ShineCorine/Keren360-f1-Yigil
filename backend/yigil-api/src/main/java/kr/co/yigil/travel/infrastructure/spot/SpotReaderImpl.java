package kr.co.yigil.travel.infrastructure.spot;

import static kr.co.yigil.global.exception.ExceptionCode.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import kr.co.yigil.global.Selected;
import kr.co.yigil.global.exception.BadRequestException;
import kr.co.yigil.travel.domain.Spot;
import kr.co.yigil.travel.domain.spot.SpotReader;
import kr.co.yigil.travel.infrastructure.SpotQueryDslRepository;
import kr.co.yigil.travel.infrastructure.SpotRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpotReaderImpl implements SpotReader {

    private final SpotRepository spotRepository;
    private final SpotQueryDslRepository spotQueryDslRepository;

    @Override
    public Spot getSpot(Long spotId) {
        return spotRepository.findById(spotId)
            .orElseThrow(() -> new BadRequestException(NOT_FOUND_SPOT_ID));
    }

    @Override
    public Optional<Spot> findSpotByPlaceIdAndMemberId(Long placeId, Long memberId) {
        return spotRepository.findTopByPlaceIdAndMemberId(placeId, memberId);
    }

    @Override
    public List<Spot> getSpots(List<Long> spotIds) {
        return spotIds.stream()
            .map(this::getSpot)
            .collect(Collectors.toList());
    }

    @Override
    public Slice<Spot> getSpotSliceInPlace(Long placeId, Pageable pageable) {
        return spotRepository.findAllByPlaceIdAndIsInCourseIsFalseAndIsPrivateIsFalse(placeId,
            pageable);
    }

    @Override
    public int getSpotCountInPlace(Long placeId) {
        return spotRepository.countByPlaceId(placeId);
    }

    @Override
    public Page<Spot> getSpotSliceByMemberId(Long memberId, Pageable pageable) {
        return spotRepository.findAllByMemberIdAndIsInCourseIsFalse(memberId, pageable);
    }

    @Override
    public Page<Spot> getMemberSpotList(Long memberId, Selected visibility, Pageable pageable
    ) {
        return spotQueryDslRepository.findAllByMemberIdAndIsPrivate(memberId, visibility, pageable);
    }

    @Override
    public boolean isExistSpot(Long placeId, Long memberId) {
        return spotRepository.existsByPlaceIdAndMemberId(placeId, memberId);
    }
}
