package kr.co.yigil.travel.application;

import java.util.List;
import kr.co.yigil.comment.application.CommentRedisIntegrityService;
import kr.co.yigil.comment.application.CommentService;
import kr.co.yigil.comment.dto.response.CommentResponse;
import kr.co.yigil.favor.application.FavorRedisIntegrityService;
import kr.co.yigil.file.FileUploadEvent;
import kr.co.yigil.global.exception.BadRequestException;
import kr.co.yigil.global.exception.ExceptionCode;
import kr.co.yigil.member.Member;
import kr.co.yigil.member.application.MemberService;
import kr.co.yigil.place.Place;
import kr.co.yigil.place.dto.request.PlaceDto;
import kr.co.yigil.place.repository.PlaceRepository;
import kr.co.yigil.travel.Spot;
import kr.co.yigil.travel.dto.request.SpotCreateRequest;
import kr.co.yigil.travel.dto.request.SpotUpdateRequest;
import kr.co.yigil.travel.dto.response.SpotCreateResponse;
import kr.co.yigil.travel.dto.response.SpotDeleteResponse;
import kr.co.yigil.travel.dto.response.SpotFindDto;
import kr.co.yigil.travel.dto.response.SpotFindListResponse;
import kr.co.yigil.travel.dto.response.SpotFindResponse;
import kr.co.yigil.travel.dto.response.SpotUpdateResponse;
import kr.co.yigil.travel.repository.SpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SpotService {

    private final SpotRepository spotRepository;
    private final MemberService memberService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CommentService commentService;
    private final PlaceRepository placeRepository;
    private final CommentRedisIntegrityService commentRedisIntegrityService;
    private final FavorRedisIntegrityService favorRedisIntegrityService;

    @Transactional(readOnly = true)
    public SpotFindListResponse getSpotList(Long placeId) {
        List<Spot> spots = spotRepository.findAllByPlaceIdAndIsInCourseFalse(placeId);
        List<SpotFindDto> spotFindDtoList = spots.stream()
                .map(spot -> SpotFindDto.from(
                        spot,
                        favorRedisIntegrityService.ensureFavorCounts(spot).getFavorCount(),
                        commentRedisIntegrityService.ensureCommentCount(spot).getCommentCount())
                )
                .toList();
        return SpotFindListResponse.from(spotFindDtoList); // todo 페이징 적용
    }

    @Transactional
    public SpotCreateResponse createSpot(Long memberId, SpotCreateRequest spotCreateRequest) {
        Member member = memberService.findMemberById(memberId);

        FileUploadEvent event = new FileUploadEvent(this, spotCreateRequest.getFiles(),
                attachFiles -> {
                    Place place = placeRepository.findByName(spotCreateRequest.getPlaceName())
                            .orElseGet(
                                    () -> placeRepository.save(PlaceDto.toEntity(
                                            spotCreateRequest.getPlaceName(),
                                            spotCreateRequest.getPlaceAddress(),
                                            spotCreateRequest.getPlacePointJson())
                                    )
                            );
                    spotRepository.save(SpotCreateRequest.toEntity(member, place, spotCreateRequest, attachFiles));
                });
        applicationEventPublisher.publishEvent(event);
        return new SpotCreateResponse("스팟 정보 생성 성공");
    }

    @Transactional(readOnly = true)
    public SpotFindResponse getSpot(Long spotId) {
        Spot spot = findSpotById(spotId);
        List<CommentResponse> comments = commentService.getCommentList(spotId);
        return SpotFindResponse.from(spot, comments);
    }

    @Transactional
    public SpotUpdateResponse updateSpot(Long memberId, Long spotId,
            SpotUpdateRequest spotUpdateRequest) {
        Member member = memberService.findMemberById(memberId);

        FileUploadEvent event = new FileUploadEvent(this, List.of(spotUpdateRequest.getFiles()),
                attachFiles -> {
                    SpotUpdateRequest.toEntity(member, spotId, spotUpdateRequest, attachFiles);

                });
        applicationEventPublisher.publishEvent(event);

        return new SpotUpdateResponse("스팟 정보 수정 성공");
    }



    public Spot findSpotByIdAndMemberId(Long spotId, Long memberId) {
        return spotRepository.findByIdAndMemberId(spotId, memberId).orElseThrow(
                () -> new BadRequestException(ExceptionCode.NOT_FOUND_SPOT_ID)
        );
    }


    public Spot findSpotById(Long spotId) {
        return spotRepository.findById(spotId).orElseThrow(
                () -> new BadRequestException(ExceptionCode.NOT_FOUND_SPOT_ID)
        );
    }

    @Transactional(readOnly = true)
    public List<Spot> getSpotListFromSpotIds(List<Long> spotIdList) {
        return spotIdList.stream()
                .map(this::findSpotById)
                .toList();
    }

    @Transactional
    public SpotDeleteResponse deleteSpot(Long memberId, Long spotId) {
        Spot spot = findSpotByIdAndMemberId(spotId, memberId);
        spotRepository.delete(spot);
        // todo 댓글, 좋아요 삭제
        return new SpotDeleteResponse("스팟 정보 삭제 성공");
    }
}


