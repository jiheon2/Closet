package jiheon.styleservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record ShopDTO(
        String lastBuildDate,
        int total,
        int start,
        int display,
        List<Item> items
) {
    @Builder
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public record Item(
            String title,
            String link,
            String image,
            String author,
            String discount,
            String publisher,
            String pubdate,
            String isbn,
            String description,
            String lprice,
            String hprice,
            String mallName,
            String productId,
            String productType,
            String brand,
            String maker,
            String category1,
            String category2,
            String category3,
            String category4
    ) {}
}
