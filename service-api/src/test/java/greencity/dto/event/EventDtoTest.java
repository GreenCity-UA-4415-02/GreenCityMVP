package greencity.dto.event;

import greencity.dto.tag.TagUaEnDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

class EventDtoTest {

    @Test
    void addEventDtoRequest_builder_getters_setters() {
        AddEventDtoRequest dto = AddEventDtoRequest.builder()
            .title("Test Event")
            .description("Description")
            .open(true)
            .tags(Collections.singletonList(
                TagUaEnDto.builder()
                    .id(1L)
                    .nameUa("ТегUA")
                    .nameEn("TagEN")
                    .build()))
            .datesLocations(Collections.singletonList(
                DateLocationDto.builder()
                    .startDate(LocalDateTime.now())
                    .finishDate(LocalDateTime.now().plusHours(2))
                    .address("Kyiv")
                    .latitude(new BigDecimal("50.4501"))
                    .longitude(new BigDecimal("30.5234"))
                    .onlineLink("https://example.com")
                    .build()))
            .build();

        String title = dto.getTitle();
        String desc = dto.getDescription();
        Boolean open = dto.getOpen();
        var tags = dto.getTags();
        var dates = dto.getDatesLocations();

        System.out.println(title + ", " + desc + ", " + open);
        System.out.println(tags);
        System.out.println(dates);
    }

    @Test
    void addEventDtoResponse_builder_getters_setters() {
        AddEventDtoResponse response = AddEventDtoResponse.builder()
            .id(10L)
            .title("Response Event")
            .description("Desc")
            .open(true)
            .tagNames(Collections.singletonList("TagEN"))
            .datesLocations(Collections.singletonList(
                DateLocationDto.builder()
                    .startDate(LocalDateTime.now())
                    .finishDate(LocalDateTime.now().plusHours(1))
                    .address("Kyiv")
                    .build()))
            .images(Collections.singletonList("image.png"))
            .build();

        Long id = response.getId();
        String title = response.getTitle();
        String desc = response.getDescription();
        Boolean open = response.getOpen();
        var tags = response.getTagNames();
        var images = response.getImages();

        System.out.println(id + ", " + title + ", " + desc + ", " + open);
        System.out.println(tags);
        System.out.println(images);
    }

    @Test
    void dateLocationDto_builder_getters_setters() {
        DateLocationDto dto = DateLocationDto.builder()
            .startDate(LocalDateTime.now())
            .finishDate(LocalDateTime.now().plusHours(3))
            .address("Kyiv")
            .latitude(new BigDecimal("50.4501"))
            .longitude(new BigDecimal("30.5234"))
            .onlineLink("https://example.com")
            .build();

        System.out.println(dto.getAddress());
        System.out.println(dto.getLatitude());
        System.out.println(dto.getLongitude());
        System.out.println(dto.getOnlineLink());
    }

    @Test
    void tagUaEnDto_builder_getters_setters() {
        TagUaEnDto tag = TagUaEnDto.builder()
            .id(1L)
            .nameUa("ТегUA")
            .nameEn("TagEN")
            .build();

        System.out.println(tag.getId());
        System.out.println(tag.getNameUa());
        System.out.println(tag.getNameEn());
    }
}
