package hello.upload.file;

import hello.upload.domain.UploadFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**파일 저장과 관련된 업무 처리*/
/*멀티파트 파일을 서버에 저장*/

@Component
public class FileStore {

    @Value("${file.dir}")
    private String fileDir;

    //파일명의 위치
    public String getFullPath(String filename) {
        return fileDir + filename;
    }

    //이미지파일을 담는다
    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<UploadFile> storeFileResult = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                storeFileResult.add(storeFile(multipartFile));  //업로드파일 추가
            }
        }
        return storeFileResult;
    }

    public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }
        String originalFilename = multipartFile.getOriginalFilename();  //image.jpg(사용자가 올린 파일명)
        String storeFileName = createStoreFileName(originalFilename);   //서버에 저장하는 파일명
        multipartFile.transferTo(new File(getFullPath(storeFileName)));
        return new UploadFile(originalFilename, storeFileName);
    }

    private String createStoreFileName(String originalFilename) {
        //서버에 저장하는 파일명
        //서버 내부에서 관리하는 파일명은 유일한 이름을 생성하는 UUID 를 사용해서 충돌하지 않도록 한다.
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }
    private String extractExt(String originalFilename) { //확장자 꺼내기(.jpg)
        /*확장자를 별도로 추출해서 서버 내부에서 관리하는 파일명에도 붙여준다
        * a.jpg라는 이름으로 업로드 하면-> 51041c62-4274-614a7d994edb.jpg와 같이 저장*/
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }
}
