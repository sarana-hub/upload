package hello.upload.controller;

import hello.upload.domain.Item;
import hello.upload.domain.ItemRepository;
import hello.upload.domain.UploadFile;
import hello.upload.file.FileStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemRepository itemRepository;
    private final FileStore fileStore;

    /** 상품 등록 폼 */
    @GetMapping("/items/new")
    public String newItem(@ModelAttribute ItemForm form) {

        return "item-form";
    }

    @PostMapping("/items/new")
    public String saveItem(@ModelAttribute ItemForm form, RedirectAttributes redirectAttributes) throws IOException {
        UploadFile attachFile = fileStore.storeFile(form.getAttachFile());
        List<UploadFile> storeImageFiles = fileStore.storeFiles(form.getImageFiles());

        //데이터베이스에 저장
        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setAttachFile(attachFile);
        item.setImageFiles(storeImageFiles);
        itemRepository.save(item);

        redirectAttributes.addAttribute("id", item.getId());
        redirectAttributes.addAttribute("status", true);

        return "redirect:/items/{id}";
    }

    /** 상품 상세(조회) */
    @GetMapping("/items/{id}")
    public String items(@PathVariable Long id, Model model) {
        Item item = itemRepository.findById(id);
        model.addAttribute("item", item);
        return "item-view";
    }

    /*추가*/
    /** 상품 목록 */
    @GetMapping("/items")
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();    //모든 item 조회
        model.addAttribute("items", items);  //items(모든 item)을 모델에 담는다
        return "items";   //뷰 템플릿 호출
    }


    @ResponseBody
    @GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

    @GetMapping("/attach/{itemId}")
    public ResponseEntity<Resource> downloadAttach(@PathVariable Long itemId) throws MalformedURLException {
        Item item = itemRepository.findById(itemId);
        String storeFileName = item.getAttachFile().getStoreFileName();
        String uploadFileName = item.getAttachFile().getUploadFileName(); //고객이 업로드한 파일명

        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(storeFileName));

        log.info("uploadFileName={}", uploadFileName);

        //첨부파일 다운로드
        String encodedUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8); //파일깨짐 방지
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }


    /*추가*/
    /** 상품 수정 */
    @GetMapping("/items/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Item item = itemRepository.findById(id);
        model.addAttribute("item", item);
        return "editForm";    //수정용 폼 뷰를 호출
    }

    /** 상품 수정 처리 */
    @PostMapping("/items/{id}/edit")
    public String edit(@PathVariable Long id, @ModelAttribute Item item) {
        itemRepository.update(id, item);
        return "redirect:/items/{id}";
        //(뷰 템플릿을 호출하는 대신에) 상품 상세 화면으로 이동하도록 "리다이렉트"를 호출
    }
}
