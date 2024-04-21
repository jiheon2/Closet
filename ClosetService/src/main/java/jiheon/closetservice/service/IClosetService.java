package jiheon.closetservice.service;

import jiheon.closetservice.dto.ClosetDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IClosetService {

    // 회원의 옷 조회
    List<ClosetDTO> getClosetList(ClosetDTO pDTO) throws Exception;

    // 회원의 옷 업로드
    int uploadCloset(ClosetDTO pDTO, MultipartFile multipartFile) throws Exception;

    // 회원의 옷 삭제
    int deleteCloset(ClosetDTO pDTO) throws Exception;
}
