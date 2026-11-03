package com.main.recipeblog.controller;

import com.main.recipeblog.service.RecipePdfService;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipes/pdf")
public class RecipePdfController {

    private final RecipePdfService recipePdfService;

    public RecipePdfController(RecipePdfService recipePdfService) {
        this.recipePdfService = recipePdfService;
    }

    // Recipe बदलली आहे का हे तपासण्यासाठी
    @GetMapping("/version")
    public ResponseEntity<String> getVersion() throws Exception {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(recipePdfService.getRecipesVersion());
    }

    // Browser मध्ये PDF दाखवण्यासाठी
    @GetMapping("/preview")
    public ResponseEntity<byte[]> previewPdf() throws Exception {
        return createPdfResponse(false);
    }

    // PDF download करण्यासाठी
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadPdf() throws Exception {
        return createPdfResponse(true);
    }

    private ResponseEntity<byte[]> createPdfResponse(
            boolean download) throws Exception {

        byte[] pdf = recipePdfService.generateAllRecipesPdf();

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setCacheControl(CacheControl.noStore());

        headers.setContentDisposition(
                (download
                        ? ContentDisposition.attachment()
                        : ContentDisposition.inline())
                        .filename("All_Recipes.pdf")
                        .build()
        );

        headers.setContentLength(pdf.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}