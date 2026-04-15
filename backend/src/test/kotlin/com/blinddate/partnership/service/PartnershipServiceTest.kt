package com.blinddate.partnership.service

import com.blinddate.auth.jwt.UserPrincipal
import com.blinddate.auth.jwt.UserType
import com.blinddate.cafe.entity.Cafe
import com.blinddate.cafe.repository.CafeRepository
import com.blinddate.common.exception.BadRequestException
import com.blinddate.marketplace.entity.MarketplaceAuthorType
import com.blinddate.marketplace.entity.MarketplacePost
import com.blinddate.marketplace.entity.MarketplacePostType
import com.blinddate.organizer.entity.Organizer
import com.blinddate.organizer.repository.OrganizerRepository
import com.blinddate.partnership.entity.Partnership
import com.blinddate.partnership.entity.PartnershipRequester
import com.blinddate.partnership.entity.PartnershipStatus
import com.blinddate.partnership.repository.PartnershipRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Optional

class PartnershipServiceTest {

    private val partnershipRepo = mockk<PartnershipRepository>()
    private val cafeRepo = mockk<CafeRepository>()
    private val organizerRepo = mockk<OrganizerRepository>()
    private val service = PartnershipService(partnershipRepo, cafeRepo, organizerRepo)

    private val cafe = Cafe(name = "Test Cafe", address = "서울시 강남구", slug = "test-cafe")
    private val organizer = Organizer(name = "Org", phoneNumber = "010", email = "org@test.com", password = "pass")

    @Test
    fun `requestPartnership succeeds for organizer on OFFER_SPACE`() {
        val principal = UserPrincipal(id = 1L, userType = UserType.ORGANIZER)
        val post = MarketplacePost(
            type = MarketplacePostType.OFFER_SPACE,
            authorType = MarketplaceAuthorType.CAFE_OWNER,
            authorId = 99L, title = "공간 제공", description = "설명",
            region = "강남", cafeId = 10L
        )

        every { partnershipRepo.findByCafeIdAndOrganizerIdAndStatusIn(10L, 1L, any()) } returns null
        every { cafeRepo.findById(10L) } returns Optional.of(cafe)
        every { organizerRepo.findById(1L) } returns Optional.of(organizer)
        every { partnershipRepo.save(any()) } answers { firstArg() }

        val result = service.requestPartnership(principal, post, "제휴 원합니다")
        assertEquals(PartnershipStatus.PENDING, result.status)
        assertEquals(PartnershipRequester.ORGANIZER, result.requestedBy)
        assertEquals("제휴 원합니다", result.message)
    }

    @Test
    fun `requestPartnership rejected when ACTIVE or PENDING already exists`() {
        val principal = UserPrincipal(id = 1L, userType = UserType.ORGANIZER)
        val post = MarketplacePost(
            type = MarketplacePostType.OFFER_SPACE,
            authorType = MarketplaceAuthorType.CAFE_OWNER,
            authorId = 99L, title = "공간 제공", description = "설명",
            region = "강남", cafeId = 10L
        )

        val existing = Partnership(
            cafe = cafe, organizer = organizer,
            requestedBy = PartnershipRequester.ORGANIZER
        )
        every { partnershipRepo.findByCafeIdAndOrganizerIdAndStatusIn(10L, 1L, any()) } returns existing

        assertThrows(BadRequestException::class.java) {
            service.requestPartnership(principal, post, null)
        }
    }

    @Test
    fun `accept changes status from PENDING to ACTIVE`() {
        val partnership = Partnership(
            cafe = cafe, organizer = organizer,
            requestedBy = PartnershipRequester.ORGANIZER,
            status = PartnershipStatus.PENDING
        )
        every { partnershipRepo.findById(1L) } returns Optional.of(partnership)

        // Responder for ORGANIZER-requested partnership is the cafe side (actorId = cafe.id)
        val result = service.accept(1L, cafe.id)
        assertEquals(PartnershipStatus.ACTIVE, result.status)
        assertNotNull(result.respondedAt)
    }

    @Test
    fun `reject changes status from PENDING to TERMINATED`() {
        val partnership = Partnership(
            cafe = cafe, organizer = organizer,
            requestedBy = PartnershipRequester.ORGANIZER,
            status = PartnershipStatus.PENDING
        )
        every { partnershipRepo.findById(1L) } returns Optional.of(partnership)

        val result = service.reject(1L, cafe.id)
        assertEquals(PartnershipStatus.TERMINATED, result.status)
        assertNotNull(result.respondedAt)
        assertNotNull(result.terminatedAt)
    }

    @Test
    fun `terminate changes status from ACTIVE to TERMINATED`() {
        val partnership = Partnership(
            cafe = cafe, organizer = organizer,
            requestedBy = PartnershipRequester.ORGANIZER,
            status = PartnershipStatus.ACTIVE
        )
        every { partnershipRepo.findById(1L) } returns Optional.of(partnership)

        val result = service.terminate(1L, cafe.id)
        assertEquals(PartnershipStatus.TERMINATED, result.status)
        assertNotNull(result.terminatedAt)
    }

    @Test
    fun `getByCafeId returns partnerships for cafe`() {
        val partnership = Partnership(
            cafe = cafe, organizer = organizer,
            requestedBy = PartnershipRequester.ORGANIZER,
            status = PartnershipStatus.ACTIVE
        )
        every { partnershipRepo.findByCafeId(cafe.id) } returns listOf(partnership)

        val results = service.getByCafeId(cafe.id)

        assertEquals(1, results.size)
        assertEquals(PartnershipStatus.ACTIVE, results[0].status)
    }

    @Test
    fun `getByOrganizerId returns partnerships for organizer`() {
        val partnership = Partnership(
            cafe = cafe, organizer = organizer,
            requestedBy = PartnershipRequester.ORGANIZER,
            status = PartnershipStatus.PENDING
        )
        every { partnershipRepo.findByOrganizerId(organizer.id) } returns listOf(partnership)

        val results = service.getByOrganizerId(organizer.id)

        assertEquals(1, results.size)
        assertEquals(PartnershipStatus.PENDING, results[0].status)
    }

    @Test
    fun `hasActivePartnership returns true when active partnership exists`() {
        val partnership = Partnership(
            cafe = cafe, organizer = organizer,
            requestedBy = PartnershipRequester.ORGANIZER,
            status = PartnershipStatus.ACTIVE
        )
        every { partnershipRepo.findByCafeIdAndOrganizerIdAndStatusIn(cafe.id, organizer.id, listOf(PartnershipStatus.ACTIVE)) } returns partnership

        val result = service.hasActivePartnership(cafe.id, organizer.id)

        assertTrue(result)
    }

    @Test
    fun `hasActivePartnership returns false when no active partnership`() {
        every { partnershipRepo.findByCafeIdAndOrganizerIdAndStatusIn(cafe.id, organizer.id, listOf(PartnershipStatus.ACTIVE)) } returns null

        val result = service.hasActivePartnership(cafe.id, organizer.id)

        assertFalse(result)
    }
}
