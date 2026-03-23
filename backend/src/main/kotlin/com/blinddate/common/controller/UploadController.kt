package com.blinddate.common.controller

import com.blinddate.common.service.S3Service
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/upload")
class UploadController(private val s3Service: S3Service) {

    data class PresignedUrlRequest(val contentType: String, val directory: String = "uploads")

    @PostMapping("/presigned-url")
    fun getPresignedUrl(@RequestBody request: PresignedUrlRequest) =
        s3Service.generatePresignedUrl(request.contentType, request.directory)
}
