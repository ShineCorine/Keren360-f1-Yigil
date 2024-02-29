package kr.co.yigil.place.infrastructure;

import static kr.co.yigil.global.exception.ExceptionCode.NOT_FOUND_PLACE_ID;

import java.util.List;
import java.util.Optional;
import kr.co.yigil.global.exception.BadRequestException;
import kr.co.yigil.place.domain.Place;
import kr.co.yigil.place.domain.PlaceReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlaceReaderImpl implements PlaceReader {
    private final PlaceRepository placeRepository;

    @Override
    public Optional<Place> findPlaceByNameAndAddress(String placeName, String placeAddress) {
        return placeRepository.findByNameAndAddress(placeName, placeAddress);
    }

    @Override
    public Place getPlace(Long placeId) {
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_PLACE_ID));
    }

    @Override
    public List<Place> getPopularPlace() {
        return placeRepository.findTop5ByOrderByIdAsc();
    }

}