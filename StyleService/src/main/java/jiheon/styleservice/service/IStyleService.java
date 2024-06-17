package jiheon.styleservice.service;

import jiheon.styleservice.dto.ShopDTO;
import jiheon.styleservice.dto.StyleDTO;

import java.util.List;
import java.util.Map;

public interface IStyleService {

    void saveVector(String folderPath);

    String answer(String question) throws Exception;

    String answer(StyleDTO pDTO) throws Exception;

    List<String> styleDictionary(String style) throws Exception;

    Map styleInfo(int imageNum) throws Exception;

    List<ShopDTO.Item> shop(String item) throws Exception;
}
