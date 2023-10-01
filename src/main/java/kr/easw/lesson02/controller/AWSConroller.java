package kr.easw.lesson02.controller;

import com.amazonaws.auth.policy.Resource;
import kr.easw.lesson02.model.dto.AWSKeyDto;
import kr.easw.lesson02.service.AWSService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rest/aws")
public class AWSConroller {
    private final AWSService awsController;

    @PostMapping("/auth")
    private ModelAndView onAuth(AWSKeyDto awsKey) {
        try {
            awsController.initAWSAPI(awsKey);
            return new ModelAndView("redirect:/");
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ModelAndView("redirect:/server-error?errorStatus=" + ex.getMessage());
        }
    }

    @GetMapping("/list")
    private List<String> onFileList() {
        return awsController.getFileList();
    }

    @PostMapping("/upload")
    private ModelAndView onUpload(@RequestParam MultipartFile file) {
        try {
            awsController.upload(file);
            return new ModelAndView("redirect:/?success=true");
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ModelAndView("redirect:/server-error?errorStatus=" + ex.getMessage());
        }
    }

    @GetMapping("/download")
    private ResponseEntity<ByteArrayResource> onDownload(@RequestParam String fileName) {
        try {
            ByteArrayResource resource = awsController.download(fileName);

            if (resource != null) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }}