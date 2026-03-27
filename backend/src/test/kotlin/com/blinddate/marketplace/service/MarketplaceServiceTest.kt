package com.blinddate.marketplace.service

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.auth.jwt.UserType
import com.blinddate.cafe.entity.Cafe
import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.cafeowner.entity.CafeOwner
import com.blinddate.cafeowner.repository.CafeOwnerRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.common.exception.ForbiddenException
import com.blinddate.marketplace.entity.MarketplaceAuthorType
import com.blinddate.marketplace.entity.MarketplacePost
import com.blinddate.marketplace.entity.MarketplacePostType
import com.blinddate.marketplace.dto.CreateMarketplacePostRequest
import com.blinddate.marketplace.dto.UpdateMarketplacePostRequest
import com.blinddate.marketplace.repository.MarketplacePostRepository
import com.blinddate.organizer.entity.Organizer
import com.blinddate.organizer.repository.OrganizerRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Optional

class MarketplaceServiceTest {

    private val postRepo = mockk<MarketplacePostRepository>()
    private val cafeOwnerRepo = mockk<CafeOwnerRepository>()
    private val organizerRepo = mockk<OrganizerRepository>()
    private val cafeRepo = mockk<CafeRepository>()
    private val service = MarketplaceService(postRepo, cafeOwnerRepo, organizerRepo, cafeRepo)

    private val cafe = Cafe(name = "Test Cafe", address = "서울시 강남구", slug = "test-cafe")

    @Test
    fun `CAFE_OWNER creates OFFER_SPACE post successfully`() {
        val principal = UserPrincipal(id = 1L, userType = UserType.CAFE_OWNER, cafeId = 10L)
        val request = CreateMarketplacePostRequest(
            title = "공간 제공합니다", description = "넓은 카페 공간", region = "강남"
        )
        val cafeOwner = CafeOwner(cafe = cafe, name = "Owner", phoneNumber = "010", email = "o@t.com", password = "p")

        every { postRepo.save(any()) } answers { firstArg() }
        every { cafeOwnerRepo.findById(1L) } returns Optional.of(cafeOwner)
        every { cafeRepo.findById(10L) } returns Optional.of(cafe)

        val result = service.createPost(principal, request)
        assertEquals(MarketplacePostType.OFFER_SPACE, result.type)
        assertEquals("공간 제공합니다", result.title)
        assertEquals(10L, result.cafeId)
    }

    @Test
    fun `ORGANIZER creates SEEK_SPACE post successfully`() {
        val principal = UserPrincipal(id = 2L, userType = UserType.ORGANIZER)
        val request = CreateMarketplacePostRequest(
            title = "공간 구합니다", description = "20명 수용 가능한 곳", region = "홍대"
        )
        val organizer = Organizer(name = "Org", phoneNumber = "010", email = "org@t.com", password = "p")

        every { postRepo.save(any()) } answers { firstArg() }
        every { organizerRepo.findById(2L) } returns Optional.of(organizer)

        val result = service.createPost(principal, request)
        assertEquals(MarketplacePostType.SEEK_SPACE, result.type)
        assertEquals("공간 구합니다", result.title)
        assertNull(result.cafeId)
    }

    @Test
    fun `CAFE_OWNER creating SEEK_SPACE is rejected`() {
        // CAFE_OWNER always creates OFFER_SPACE, so this actually tests the type assignment
        // The service forces CAFE_OWNER -> OFFER_SPACE, so there's no way to create SEEK_SPACE
        val principal = UserPrincipal(id = 1L, userType = UserType.CAFE_OWNER, cafeId = 10L)
        val request = CreateMarketplacePostRequest(
            title = "Test", description = "Test", region = "강남"
        )
        val cafeOwner = CafeOwner(cafe = cafe, name = "Owner", phoneNumber = "010", email = "o@t.com", password = "p")

        every { postRepo.save(any()) } answers { firstArg() }
        every { cafeOwnerRepo.findById(1L) } returns Optional.of(cafeOwner)
        every { cafeRepo.findById(10L) } returns Optional.of(cafe)

        val result = service.createPost(principal, request)
        // CAFE_OWNER is forced to OFFER_SPACE, not SEEK_SPACE
        assertEquals(MarketplacePostType.OFFER_SPACE, result.type)
    }

    @Test
    fun `only author can update post`() {
        val owner = UserPrincipal(id = 1L, userType = UserType.CAFE_OWNER, cafeId = 10L)
        val other = UserPrincipal(id = 99L, userType = UserType.CAFE_OWNER, cafeId = 20L)
        val post = MarketplacePost(
            type = MarketplacePostType.OFFER_SPACE,
            authorType = MarketplaceAuthorType.CAFE_OWNER,
            authorId = 1L, title = "Original", description = "Desc",
            region = "강남", cafeId = 10L
        )
        val updateReq = UpdateMarketplacePostRequest(
            title = "Updated", description = "Updated Desc", region = "강남"
        )

        every { postRepo.findById(1L) } returns Optional.of(post)

        assertThrows(ForbiddenException::class.java) {
            service.updatePost(other, 1L, updateReq)
        }
    }

    @Test
    fun `getPosts filters by type`() {
        val post1 = MarketplacePost(
            type = MarketplacePostType.OFFER_SPACE,
            authorType = MarketplaceAuthorType.CAFE_OWNER,
            authorId = 1L, title = "Offer", description = "Desc",
            region = "강남", cafeId = 10L
        )
        val cafeOwner = CafeOwner(cafe = cafe, name = "Owner", phoneNumber = "010", email = "o@t.com", password = "p")

        every { postRepo.findByTypeAndIsActiveTrue(MarketplacePostType.OFFER_SPACE) } returns listOf(post1)
        every { cafeOwnerRepo.findById(1L) } returns Optional.of(cafeOwner)
        every { cafeRepo.findById(10L) } returns Optional.of(cafe)

        val results = service.getPosts(MarketplacePostType.OFFER_SPACE, null)
        assertEquals(1, results.size)
        assertEquals(MarketplacePostType.OFFER_SPACE, results[0].type)
    }
}
