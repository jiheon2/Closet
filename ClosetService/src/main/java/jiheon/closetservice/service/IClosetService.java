package jiheon.closetservice.service;

import jiheon.closetservice.dto.ClosetDTO;
import jiheon.closetservice.repository.entity.ClosetEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IClosetService {

    // 회원의 옷 전체 조회
    List<ClosetDTO> getAllClosetList(String userId) throws Exception;

    // 회원의 파츠별 옷 전체 조회
    List<ClosetDTO> getAllPartsClosetList(String userId, String parts) throws Exception;

    // 회원의 옷 업로드
    int uploadCloset(ClosetDTO pDTO, MultipartFile image) throws Exception;

    // 회원의 옷 삭제
    int deleteCloset(long photoSeq) throws Exception;
}
