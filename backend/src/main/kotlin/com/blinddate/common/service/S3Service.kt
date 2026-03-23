package com.blinddate.common.service

import com.blinddate.common.exception.BadRequestException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.util.UUID

@Service
class S3Service(
    @Value("\${aws.s3.bucket}") private val bucket: String,
    @Value("\${aws.s3.region}") private val region: String
) {
    private val presigner: S3Presigner by lazy {
        S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build()
    }

    private val allowedContentTypes = setOf("image/jpeg", "image/png", "image/webp", "image/gif")
    private val maxFileSize = 5 * 1024 * 1024L // 5MB

    data class PresignedUrlResponse(val uploadUrl: String, val fileUrl: String, val key: String)

    fun generatePresignedUrl(contentType: String, directory: String = "uploads"): PresignedUrlResponse {
        if (contentType !in allowedContentTypes) {
            throw BadRequestException("허용되지 않는 파일 형식입니다. (jpg, png, webp, gif만 가능)")
        }

        val extension = contentType.substringAfter("/")
        val key = "$directory/${UUID.randomUUID()}.$extension"

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .build()

        val presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(15))
            .putObjectRequest(putObjectRequest)
            .build()

        val presignedUrl = presigner.presignPutObject(presignRequest)

        return PresignedUrlResponse(
            uploadUrl = presignedUrl.url().toString(),
            fileUrl = "https://$bucket.s3.$region.amazonaws.com/$key",
            key = key
        )
    }
}
